# Implementation Plan: 034 - 店家績效比較報表

**Branch**: `034-store-performance-report` | **Date**: 2026-04-28 | **Spec**: [spec.md](spec.md)  
**Input**: Feature specification from `/specs/034-store-performance-report/spec.md`

## Summary

新增 `POST /admin/report/store-performance` 端點，提供 Admin 跨店家績效比較視圖及
StoreOwner 查詢自己店家詳細績效的功能。核心 KPI 包含 `totalRevenue`、`drawCount`、
`activeUsers`、`shipRate`、`overdueRate`、`avgShipDays`（029 依賴），支援時間範圍篩選與自
訂排序。實作沿用既有 `JdbcTemplate` 聚合 SQL 模式，遵循 `QueryReq<Condition>` + 
`ReportRes` 雙層結構，在 `AdminReportController` 新增一個方法。

## Technical Context

**Language/Version**: Java 21, Spring Boot 3.x  
**Primary Dependencies**: Spring Web, Spring Security, JdbcTemplate (MySQL), Lombok, Swagger/OpenAPI  
**Storage**: MySQL — 主要資料表：`order`、`lottery_ticket`、`lottery`、`wallet_transaction`、`store`、`user`  
**Testing**: JUnit 5 + Mockito + MockMvc (Spring Boot Test)  
**Target Platform**: Linux server (Spring Boot JAR)  
**Project Type**: Web Service (REST API, Backend-only)  
**Performance Goals**: 全店家聚合查詢 < 5 秒 (NFR-001)  
**Constraints**: StoreOwner 只能查自己店；sortBy 欄位需白名單驗證防 SQL Injection  
**Scale/Scope**: 單平台多店家，預計店家數 < 100，單次查詢聚合全部

## Constitution Check

*Constitution 為未填入的佔位範本，無強制 gates 需評估。依既有專案慣例審查：*

| 檢查項目 | 狀態 | 說明 |
|---------|------|------|
| 沿用既有分層架構（Controller/Service/Impl） | ✅ PASS | 加入 AdminReportController 新方法，新增 StorePerformanceCondition + Res |
| 不新增多餘 Repository/Entity | ✅ PASS | 純 JdbcTemplate SQL，不需 MBG/Entity |
| StoreOwner 資料隔離 | ✅ PASS | Controller 層強制 storeId override |
| sortBy SQL Injection 防護 | ✅ PASS | 白名單 Set<String> 驗證 |
| 029 依賴 preparing_at 缺失 | ✅ HANDLED | avgShipDays 回傳 null（已澄清） |

*Post-design re-check: No violations.*

## Project Structure

### Documentation (this feature)

```text
specs/034-store-performance-report/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/
│   └── POST_admin_report_store-performance.md
└── tasks.md             # Phase 2 output (/speckit.tasks — NOT created here)
```

### Source Code (repository root)

```text
src/main/java/com/group/admin/
├── condition/report/
│   └── StorePerformanceCondition.java        # NEW
├── dto/res/report/
│   └── StorePerformanceReportRes.java        # NEW  (含 StoreItem + DailyStat inner classes)
├── controller/admin/
│   └── AdminReportController.java            # MODIFY — add storePerformance endpoint
├── service/
│   └── ReportService.java                    # MODIFY — add getStorePerformanceReport()
└── service/impl/
    └── ReportServiceImpl.java                # MODIFY — implement getStorePerformanceReport()

src/test/java/com/group/admin/
└── controller/
    └── AdminReportControllerTest.java        # MODIFY — add store-performance test cases
```

**Structure Decision**: 單一後端專案（Spring Boot），純後端延伸現有 report 分層，
無前端變更、無新 DB 實體、無 MBG 執行。

## Complexity Tracking

> 無 Constitution violations 需要記錄。
