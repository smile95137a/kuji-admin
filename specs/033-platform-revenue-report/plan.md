# Implementation Plan: 033 - 平台營收總覽報表

**Branch**: `033-platform-revenue-report` | **Date**: 2026-05-05 | **Spec**: [spec.md](spec.md)  
**Input**: Feature specification from `/specs/033-platform-revenue-report/spec.md`

## 現況註記（2026-05-11）

1. 本計畫對應的 API 與前後台基本實作已存在。
2. 目前主線改為跨 repo 契約收斂與管理模組巡檢，非新增報表能力。
3. 執行實作前請先對照 `docs/AI_HANDOFF_CURRENT.md` 最新節點，避免沿用過期假設。

## Summary

新增 `POST /admin/report/platform-revenue` 端點，提供 Admin 查詢指定時間區間內的平台
整體儲值、消費、淨收入、消費幣別拆分、每日趨勢、店家消費貢獻，以及相較上一個同長度
區間的成長率。實作沿用既有 report layer 的 `JdbcTemplate` 聚合查詢模式，新增專用
`Condition` / `Res` DTO，不重用既有 `RevenueReportRes`，避免語意混淆。

## Technical Context

**Language/Version**: Java 21, Spring Boot 3.x  
**Primary Dependencies**: Spring Web, Spring Security, JdbcTemplate (MySQL), Lombok, Swagger/OpenAPI, AOP AuditLog  
**Storage**: MySQL — 主要資料表：`wallet_transaction`、`lottery_ticket`、`lottery`、`store`、`order`  
**Testing**: JUnit 5 + Mockito + MockMvc（controller slice）  
**Target Platform**: Linux server (Spring Boot JAR)  
**Project Type**: Web Service（REST API, Backend-only）  
**Performance Goals**: 10 萬筆 `wallet_transaction` 內查詢 < 3 秒  
**Constraints**: 僅 Admin 可存取；`dailyRevenue` 必須補齊零交易日期；`storeBreakdown` 不顯示 unknown bucket  
**Scale/Scope**: 單平台全域聚合，店家數 < 100，查詢期間通常 7~90 天

## Constitution Check

*Constitution 仍為佔位範本，無可機械驗證的 gates；改依本專案既有分層規則審查：*

| 檢查項目 | 狀態 | 說明 |
|---------|------|------|
| 沿用既有分層架構（Controller/Service/Impl） | ✅ PASS | 在 `AdminReportController`、`ReportService`、`ReportServiceImpl` 擴充 |
| 不新增不必要 Entity / Mapper | ✅ PASS | 純 JdbcTemplate 聚合，不需要 MBG 新實體 |
| 權限最小化 | ✅ PASS | `@PreAuthorize("hasRole('ADMIN')")`，StoreOwner 不可存取 |
| 稽核要求 | ✅ PASS | 沿用既有 `@AuditLog` AOP 記錄敏感報表存取 |
| 效能風險 | ✅ PASS | 補 `wallet_transaction.related_id` 與 `(transaction_type, coin_type, created_at)` 索引 |

*Post-design re-check: No violations.*

## Project Structure

### Documentation (this feature)

```text
specs/033-platform-revenue-report/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── POST_admin_report_platform-revenue.md
└── tasks.md
```

### Source Code (repository root)

```text
src/main/java/com/group/admin/
├── condition/report/
│   └── PlatformRevenueReportCondition.java      # NEW
├── dto/res/report/
│   └── PlatformRevenueReportRes.java            # NEW
├── controller/admin/
│   └── AdminReportController.java               # MODIFY — add platform-revenue endpoint
├── service/
│   └── ReportService.java                       # MODIFY — add getPlatformRevenueReport()
└── service/impl/
    └── ReportServiceImpl.java                   # MODIFY — implement platform revenue aggregation

src/test/java/com/group/admin/
└── controller/admin/
    └── AdminReportControllerPlatformRevenueTest.java   # NEW

sql/
└── V033__add_wallet_transaction_indexes.sql     # NEW/UPDATED — safe index migration
```

**Structure Decision**: 單一 Spring Boot 後端專案；沿用既有 report layer 與 `JdbcTemplate`
聚合模式；不新增前端、不新增資料表，只補 DTO、Controller、Service 與 SQL migration。

## Complexity Tracking

> 無 Constitution violations 需要記錄。
