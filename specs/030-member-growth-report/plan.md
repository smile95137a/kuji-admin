# Implementation Plan: 030 - 會員成長報表

**Branch**: `030-member-growth-report` | **Date**: 2026-04-28 | **Spec**: [spec.md](spec.md)  
**Input**: Feature specification from `/specs/030-member-growth-report/spec.md`

## Summary

新增 `POST /admin/report/member-growth` API，讓後台 Admin 查詢前台會員成長趨勢。報表涵蓋：新增會員數（每日明細 + 按註冊方式分類）、活躍會員數與 ARPU（金幣 / 紅利分開）、7 天與 30 天留存率。  
技術方案：沿用現有 `AdminReportController` + `ReportService` 架構，以 `JdbcTemplate` 撰寫原生 SQL，不引入新框架。

## Technical Context

**Language/Version**: Java 21 + Spring Boot 3.3.3  
**Primary Dependencies**: MyBatis Generator (entities/mappers), JdbcTemplate (analytics SQL), Lombok, Swagger/OpenAPI  
**Storage**: MySQL（`user`、`lottery_ticket`、`wallet_transaction`、`order` 表）  
**Testing**: JUnit 5 + MockMvc + Mockito  
**Target Platform**: Linux server（Spring Boot REST API）  
**Project Type**: Web service — Admin 後台 REST API  
**Performance Goals**: 查詢 100,000 名會員資料時回應時間 < 5 秒（SC-001）  
**Constraints**: ARPU 精確到小數點後 1 位；僅 ADMIN 角色可存取（無 STORE_OWNER）  
**Scale/Scope**: 單一 API 端點，3 支新 Java 檔案（Condition + DTO + 服務方法），現有類別新增方法

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

> ⚠️ 專案 constitution.md 尚為範本（未填入實際原則）。以下套用從現有程式碼庫反推的隱性規範。

| # | 隱性規範（從現有程式碼反推） | 狀態 |
|---|---|---|
| 1 | 所有 Admin 報表 API 位於 `AdminReportController`，路徑前綴 `/admin/report/` | ✅ 符合 |
| 2 | Condition 類別繼承 `BaseCondition`，DTO 使用 `@Data @Builder` | ✅ 符合 |
| 3 | 報表邏輯在 `ReportServiceImpl` 以 `JdbcTemplate` 實作 | ✅ 符合 |
| 4 | 安全控管：存取控制用 `@PreAuthorize`，此功能為 Admin-only | ✅ 符合 |
| 5 | 金額精度：使用 `BigDecimal` + `RoundingMode.HALF_UP` | ✅ 符合 |
| 6 | 不新增多餘的 Repository / Service 層；報表沿用既有 `ReportService` 擴展 | ✅ 符合 |

**Post-design re-check**: 通過 ✅ — 設計未違反任何已識別慣例。

## Project Structure

### Documentation (this feature)

```text
specs/030-member-growth-report/
├── plan.md              ← 本文件
├── research.md          ← Phase 0 研究結論
├── data-model.md        ← Phase 1 實體與 DTO 設計
├── quickstart.md        ← Phase 1 快速開發指南
├── contracts/
│   └── POST_admin_report_member-growth.md  ← API 合約
└── tasks.md             ← Phase 2 (/speckit.tasks 產生，非本命令)
```

### Source Code (repository root)

```text
src/main/java/com/group/admin/
├── condition/report/
│   └── MemberGrowthReportCondition.java          ← 新增
├── dto/res/report/
│   └── MemberGrowthReportRes.java                ← 新增
├── service/
│   └── ReportService.java                        ← 新增方法簽名
├── service/impl/
│   └── ReportServiceImpl.java                    ← 新增實作方法
└── controller/admin/
    └── AdminReportController.java                ← 新增端點

src/test/java/com/group/admin/
└── controller/
    └── AdminReportControllerTest.java            ← 新增測試（或擴充）
```

**Structure Decision**: 單一 Web Service 專案，沿用 Option 1 簡化結構。新增 3 支生產程式碼檔案 + 既有類別新增方法；測試覆蓋 Controller 層（MockMvc + Mockito）。

## Complexity Tracking

> 無憲法違規，無需填寫此表。
