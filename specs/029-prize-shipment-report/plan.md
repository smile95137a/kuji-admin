# Implementation Plan: 029 - 獎品出貨報表

**Branch**: `029-prize-shipment-report` | **Date**: 2026-04-28 | **Spec**: [spec.md](spec.md)  
**Input**: Feature specification from `/specs/029-prize-shipment-report/spec.md`

## Summary

新增 `POST /admin/report/prize-shipment` 報表 API，以 **Order** 為核心資料來源，回傳出貨狀態計數（PENDING/PREPARING/SHIPPED/COMPLETED）、平均出貨天數（`avgShipDays`）、逾期未備貨筆數（`overdueCount`）、每日出貨明細（`dailyDetails`）及跨店家統計（`storeDetails`，Admin 限定）。  
需先執行 DDL 在 `order` 表新增 `preparing_at DATETIME NULL`，並更新 MBG 產生的 Order 實體與 Mapper XML，以及在 `OrderServiceImpl.markAsPreparing()` 中自動設定此時間戳。  
實作依循現有報表的三層架構：Condition → ReportService / ReportServiceImpl (JdbcTemplate) → AdminReportController。

## Technical Context

**Language/Version**: Java 21  
**Primary Dependencies**: Spring Boot 3.3.3, MyBatis 3.0.5, JdbcTemplate (報表 SQL 直接透過 JdbcTemplate 執行)  
**Storage**: MySQL（InnoDB）  
**Testing**: JUnit 5 + Spring Boot Test + MockMvc + Mockito  
**Target Platform**: Linux Server（後台 Admin API）  
**Project Type**: Web Service（Spring Boot Admin Backend）  
**Performance Goals**: 10,000 筆訂單下查詢 < 3 秒（SC-001）  
**Constraints**: StoreOwner 嚴格隔離，後端強制覆蓋 storeId；avgShipDays 精確至 0.1 天；排除 CANCELLED 訂單  
**Scale/Scope**: 單一後台服務，新增 1 個 API endpoint + 1 個 DDL migration + Order 實體更新

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

> ⚠️ constitution.md 尚為範本（尚未填入專案特定原則）。依現有代碼庫慣例執行以下閘門檢查：

| Gate | Status | Notes |
|------|--------|-------|
| 遵循三層報表架構（Condition / Res / Service / Controller） | ✅ PASS | 與現有 `RevenueReport`、`ReferralReport` 等完全對齊 |
| StoreOwner 資料隔離（後端強制 storeId） | ✅ PASS | 沿用 `AdminReportController` 現有 `SecurityUtils.getCurrentUserPrimaryStoreId()` 模式 |
| DDL backward compatible（新增 nullable 欄位） | ✅ PASS | `preparing_at DATETIME NULL`，舊資料不受影響，avgShipDays 跳過 null |
| 效能（JdbcTemplate + index） | ✅ PASS | 報表一律用 JdbcTemplate；需加複合索引（詳見 data-model.md） |
| 無新增 project / module | ✅ PASS | 全部在現有 `com.group.admin` package 內新增 |

**Post-Design Re-check**: ✅ PASS — 設計未引入新的架構層或違規複雜度。

## Project Structure

### Documentation (this feature)

```text
specs/029-prize-shipment-report/
├── plan.md              ← 本文件
├── research.md          ← Phase 0 output
├── data-model.md        ← Phase 1 output
├── quickstart.md        ← Phase 1 output
├── contracts/
│   └── prize-shipment-report-api.md   ← Phase 1 output
└── tasks.md             ← Phase 2 output (/speckit.tasks command)
```

### Source Code

```text
src/main/java/com/group/admin/
├── condition/report/
│   └── PrizeShipmentReportCondition.java          ← NEW
├── dto/res/report/
│   └── PrizeShipmentReportRes.java                ← NEW
├── service/
│   └── ReportService.java                         ← MODIFY（新增方法）
├── service/impl/
│   └── ReportServiceImpl.java                     ← MODIFY（實作新方法）
│   └── OrderServiceImpl.java                      ← MODIFY（markAsPreparing 設定 preparingAt）
├── controller/admin/
│   └── AdminReportController.java                 ← MODIFY（新增 endpoint）
├── entity/
│   └── Order.java                                 ← MODIFY（新增 preparingAt 欄位）
└── mapper/
    └── OrderMapper.xml（resources）               ← MODIFY（新增 preparing_at 映射）

sql/
└── V029__add_order_preparing_at.sql               ← NEW（DDL migration）

src/test/java/com/group/admin/
└── controller/admin/
    └── AdminReportControllerPrizeShipmentTest.java ← NEW
```

**Structure Decision**: 單一 Spring Boot 後台專案，沿用現有 `src/` 結構，不新增模組。所有報表相關類別集中於 `condition/report/`、`dto/res/report/`、`service/ReportService`、`service/impl/ReportServiceImpl` 及 `controller/admin/AdminReportController`。

## Complexity Tracking

> *(無 Constitution 違規，本表空白)*
