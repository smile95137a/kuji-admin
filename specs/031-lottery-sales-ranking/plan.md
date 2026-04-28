# Implementation Plan: 031 - 商品銷售排行報表

**Branch**: `031-lottery-sales-ranking` | **Date**: 2026-04-28 | **Spec**: `specs/031-lottery-sales-ranking/spec.md`  
**Input**: Feature specification from `/specs/031-lottery-sales-ranking/spec.md`

## Summary

新增 `POST /admin/report/lottery-sales` API，統計各商品（Lottery）全生命期的銷售表現，包含抽籤次數（`drawCount`，來自 `lottery_ticket` 表 `status=DRAWN` 筆數）與營收（`revenue`，透過 `order_item.lottery_id` JOIN `order`，排除 `CANCELLED` 訂單）。結果依 `drawCount` 降序排列，預設回傳前 20 名（最大 100）。StoreOwner 只能查看自己店家的商品，後端強制綁定 `storeId`；Admin 可跨店查看或依 `storeId` 過濾。整個功能 **新增 3 個檔案、修改 2 個現有檔案**，不需新增資料表。

## Technical Context

**Language/Version**: Java 21  
**Primary Dependencies**: Spring Boot 3.3.3, MyBatis 3.0.5 (MBG), Spring Security + JWT (jjwt 0.9.1), Lombok, Springdoc OpenAPI  
**Storage**: MySQL 8 — 現有表 `lottery`, `lottery_ticket`, `order_item`, `order`, `store`（無需新增表或欄位）  
**Testing**: JUnit 5 + MockMvc + Mockito (現有 controller-testing 模式)  
**Target Platform**: 後台 REST API（Linux server）  
**Project Type**: Web Service — Spring Boot 單體應用  
**Performance Goals**: 50,000 筆籤位資料下查詢 < 3 秒（SC-001）  
**Constraints**: `revenue` 計算與實際訂單金額誤差為 0（精確計算，SC-002）；排序結果與手動 SQL 一致（SC-003）  
**Scale/Scope**: 單一新 API 端點，依附現有 `AdminReportController` + `ReportService` 架構

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

> 本專案 constitution.md 為預設範本（未正式填入），無法依其進行具體 gate 驗證。以下依現有專案慣例進行一致性確認：

| Gate | 狀態 | 說明 |
|------|------|------|
| 不新增不必要的架構層 | ✅ PASS | 沿用 `ReportService` + `JdbcTemplate` 模式，無額外架構 |
| 不新增資料庫表 | ✅ PASS | 純讀取現有 5 張表，零 DDL 變更 |
| Security 隔離正確 | ✅ PASS | StoreOwner 強制綁定 storeId（與其他報表一致）|
| 效能目標可達成 | ✅ PASS | 子查詢分解 + 現有索引可支撐 50k 資料 < 3s |
| 不引入新依賴 | ✅ PASS | 純使用現有 `JdbcTemplate`，無新依賴 |

**Post-Phase 1 Re-check**: ✅ 設計確認後仍全部通過，無違規需記錄。

## Project Structure

### Documentation (this feature)

```text
specs/031-lottery-sales-ranking/
├── plan.md              # 此檔案
├── research.md          # Phase 0 研究結論
├── data-model.md        # 實體關係與 SQL 設計
├── quickstart.md        # 開發者快速指南
├── contracts/
│   └── POST_admin_report_lottery-sales.md   # API 合約
└── tasks.md             # Phase 2 任務清單（/speckit.tasks 產出）
```

### Source Code (repository root)

```text
src/main/java/com/group/admin/
├── condition/report/
│   └── LotterySalesRankingCondition.java      ← NEW
├── dto/res/report/
│   └── LotterySalesRankingRes.java            ← NEW
├── service/
│   ├── ReportService.java                     ← MODIFY (新增介面方法)
│   └── impl/
│       └── ReportServiceImpl.java             ← MODIFY (實作)
└── controller/admin/
    └── AdminReportController.java             ← MODIFY (新增端點)

src/test/java/com/group/admin/
└── controller/admin/
    └── AdminReportControllerLotteryRankingTest.java  ← NEW
```

**Structure Decision**: 與現有 5 個報表完全一致的單體 Web Service 架構。新增 Condition + Res 兩個 DTO，於現有 `ReportService` 介面加方法，`ReportServiceImpl` 用 `JdbcTemplate` 實作，`AdminReportController` 加端點。無前端改動（純後台 API）。
