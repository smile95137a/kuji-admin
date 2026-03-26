# 實作計畫：推薦碼 (Referral Code)

**Branch**: `012-referral-code` | **Date**: 2026-03-22 | **Spec**: [spec.md](./spec.md)
**Input**: 功能規格來自 `/specs/012-referral-code/spec.md`

## 摘要

實作一套與店家綁定的推薦碼系統，允許管理員建立與特定店家關聯的英數字推薦碼。新使用者在註冊時可選擇性提供推薦碼，進行一次性且不可變更的綁定。所有推薦資料均儲存，供未來的獎勵邏輯使用（v1.0 僅為追蹤功能）。

本專案已包含 `ReferralCode` / `ReferralRecord` Entity、MyBatis mapper、Service 層，以及管理後台和公開前台 Controller 的部分實作。本計畫聚焦於**補全缺失部分**：新增專用的 `/disable` 端點、`/admin/referral-stats` 分析端點，以及 POST 形式的 `/api/auth/validate-referral` 註冊輔助端點——同時強化自我推薦防護與店家停用攔截。

## 技術背景

**語言/版本**: Java 21
**主要依賴套件**: Spring Boot 3.3.3, Spring Security 6, MyBatis 3.0.5, MyBatis Generator (Example pattern), JWT (jjwt), Lombok
**儲存**: MySQL 8.3 — 資料表 `referral_code`, `referral_record`；與 `store` 及 `user` 具 FK 關係
**測試**: JUnit 5, Spring Boot Test, Mockito
**目標平台**: AWS EC2 Linux (Amazon Linux 2023)
**專案類型**: REST API (web-service)
**效能目標**: 推薦碼驗證 ≤ 1 秒（SC-001）；對註冊成功率無可量測的影響（SC-004）
**限制**: 推薦碼綁定為一次性且不可變更；代碼格式為大寫英數字；功能必須對抽獎/訂單/付款流程無副作用（FR-009）
**規模/範疇**: 每店多碼；每用戶一筆紀錄；管理後台含店家層級統計

## 架構規範檢查

*關卡：在第 0 階段研究前必須通過。第 1 階段設計後重新確認。*

> 架構規範檔案包含預留位置內容（尚未針對本專案客製化）。套用通用軟體工程關卡：

| 規範項目 | 狀態 | 說明 |
|------|--------|-------|
| 不破壞現有 API | PASS | 僅新增端點；現有 `/admin/referral-codes` 為擴充性新增 |
| 遵守 JWT 安全模型 | PASS | 管理端點要求 ROLE_ADMIN；驗證端點為公開存取 |
| 推薦追蹤對核心流程無副作用 | PASS | 註冊流程以 try/catch 包裝推薦邏輯；失敗記錄日誌，不拋出例外 |
| 不可變紀錄限制 | PASS | ReferralRecord 僅允許 insert；不對紀錄執行 update 操作 |
| 資料隔離：v1.0 僅為追蹤 | PASS | 獎勵欄位已儲存，但獎勵邏輯延後至 v2 |

**設計後重新確認**：未發現新的違規情形。Repository 模式已在程式碼庫中建立。

## 專案結構

### 文件（本功能）

```text
specs/012-referral-code/
├── plan.md              ← This file
├── research.md          ← Phase 0 output
├── data-model.md        ← Phase 1 output
├── quickstart.md        ← Phase 1 output
├── contracts/
│   ├── POST_admin_referral-codes.md
│   ├── PUT_admin_referral-codes_{id}_disable.md
│   ├── GET_admin_referral-codes.md
│   ├── GET_admin_referral-stats.md
│   └── POST_api_auth_validate-referral.md
└── tasks.md             ← Phase 2 output (/speckit.tasks — NOT created here)
```

### 原始碼（現有 + 新增）

```text
src/main/java/com/group/admin/
├── entity/
│   ├── ReferralCode.java                     EXISTS — no changes needed
│   └── ReferralRecord.java                   EXISTS — no changes needed
│
├── mapper/
│   ├── ReferralCodeMapper.java               EXISTS — no changes needed
│   └── ReferralRecordMapper.java             EXISTS — no changes needed
│
├── repository/
│   ├── ReferralCodeRepository.java           EXISTS — add selectStatsByStore()
│   └── ReferralRecordRepository.java         EXISTS — add selectTimelineByStore()
│
├── service/
│   ├── ReferralCodeService.java              EXISTS — add disableCode(), getReferralStats()
│   └── impl/ReferralCodeServiceImpl.java     EXISTS — implement new methods
│
├── controller/
│   ├── admin/
│   │   └── AdminReferralCodeController.java  EXISTS — add PUT /{id}/disable, GET /stats
│   └── api/
│       └── ReferralCodeValidateController.java  EXISTS — add POST /validate-referral
│
├── dto/request/
│   └── ReferralValidateReq.java              NEW — { "code": "ABC123" }
│
└── dto/response/
    └── ReferralStatsRes.java                 NEW — per-store stats with timeline

src/main/resources/mapper/
├── ReferralCodeMapper.xml                    EXISTS — may add stats SQL fragment
└── ReferralRecordMapper.xml                  EXISTS — no changes needed

src/test/java/com/group/admin/
├── service/ReferralCodeServiceTest.java      NEW — unit tests
└── controller/AdminReferralCodeControllerTest.java  NEW — integration tests
```

**結構決策**：單一 Spring Boot 專案（`com.group.admin`）。所有推薦碼元件嵌入現有的分層架構（entity → mapper → repository → service → controller → DTO）。

## 複雜度追蹤

> 無架構規範違規。無需複雜度說明。
