# Implementation Plan: 店家帳號管理 (Store Account Management)

**Branch**: `013-store-account-mgmt` | **Date**: 2026-03-22 | **Spec**: [spec.md](./spec.md)  
**Input**: Feature specification from `/specs/013-store-account-mgmt/spec.md`

## 摘要

為 KUJI 後台建立店家帳號管理 API。平台管理員可建立 StoreOwner（透過 `Store.ownerId` 與店家 1:1 綁定）及 StoreEditor（透過 `StoreUser` 與店家 M:N 綁定）帳號，系統自動產生 8–12 字元初始密碼並以電子郵件寄送；新使用者在首次登入時必須變更密碼（由 JWT filter 檢查 `forceChangePassword` 強制執行）；管理員可啟用／停用帳號，停用時透過 Redis 黑名單**立即**使 JWT Token 失效；並支援分頁列表與篩選帳號。所有核心實體（`AdminUser`、`StoreUser`、`AdminUserStatus`）均已存在，本功能新增管理層：`AdminAccountService`、`AdminAccountController`、`TokenBlacklistService`（Redis）及電子郵件整合。

## 技術背景

**語言／版本**：Java 21, Spring Boot 3.3.3  
**主要依賴**：Spring Security、JJWT（HS256，存取 Token 24h／刷新 Token 30d）、MyBatis 3.0.5（Example 模式）、`spring-boot-starter-data-redis`（新增）、`spring-boot-starter-mail` + Thymeleaf（現有 `EmailService`）  
**儲存**：MySQL 8.3（主要資料）、Redis（Token 黑名單 — TTL = Token 剩餘有效時間）  
**測試**：JUnit 5 + Spring Boot Test  
**目標平台**：AWS EC2 Linux 伺服器  
**專案類型**：REST API（web-service）  
**效能目標**：停用帳號後 Token 失效 < 1 秒（SC-003）；完整帳號建立 + 電子郵件寄送 < 3 分鐘（SC-001）  
**限制條件**：停用時 Token 失效為**強制**安全需求（FR-007, SC-003）— 使用 Redis 黑名單，非盡力而為；密碼僅以 BCrypt 雜湊儲存（FR-011）；所有帳號操作須記錄完整稽核軌跡，含操作者 ID + 時間戳記（FR-010）  
**規模／範圍**：後台 CRUD；低並發（數十位管理員，數百個店家帳號）

## 架構審查

*關卡：必須在第 0 階段研究前通過。第 1 階段設計後需重新確認。*

> 專案架構文件為含有未填入佔位內容的範本，未強制規定特定命名原則。適用標準 Spring Boot REST API 工程審查關卡：

| 關卡 | 狀態 | 備註 |
|------|--------|-------|
| 安全：停用時 Token 失效 | ✅ 通過 | 規劃 Redis 黑名單 — 強制需求 FR-007 |
| 安全：BCrypt 密碼雜湊 | ✅ 通過 | `BCryptPasswordEncoder` 已在 `SecurityConfig` 中配置 |
| 安全：不儲存明文密碼 | ✅ 通過 | 任何持久化呼叫前均以 BCrypt 處理 |
| 安全：強制修改密碼防護 | ✅ 通過 | `AdminJwtAuthenticationFilter` 檢查 `forceChangePassword` 宣告 |
| 原子性：帳號 + 店家綁定 | ✅ 通過 | Service 使用 `@Transactional`；任何步驟失敗即回滾（FR-012） |
| 稽核軌跡 | ✅ 通過 | `AdminUser` 上有 `createdBy`/`updatedBy`/`createdAt`/`updatedAt` |
| 電子郵件：非同步，不阻塞 | ✅ 通過 | `EmailService` 標註 `@Async`；失敗記錄於 `EmailLog` |

**無違規 — 無需說明複雜度原因。**

**第 1 階段後重新確認**：✅ 通過 — Redis 是滿足強制 Token 失效需求的最小新增；未偵測到過度設計。

## 專案結構

### 文件（本功能）

```text
specs/013-store-account-mgmt/
├── plan.md              # This file (/speckit.plan output)
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/
│   ├── POST_admin_accounts.md
│   ├── PUT_admin_accounts_{id}_status.md
│   ├── PUT_admin_accounts_{id}_role.md
│   ├── POST_admin_auth_first-login.md
│   └── GET_admin_accounts.md
└── tasks.md             # Phase 2 output (/speckit.tasks — NOT created by /speckit.plan)
```

### 原始碼（專案根目錄）

```text
src/main/java/com/group/admin/
├── config/
│   ├── RedisConfig.java                          # NEW — Redis connection + StringRedisTemplate bean
│   └── SecurityConfig.java                       # MODIFY — inject TokenBlacklistService into JWT filters
├── service/
│   ├── AdminAccountService.java                  # NEW — interface (create, updateStatus, updateRole, list)
│   ├── TokenBlacklistService.java                # NEW — interface (blacklist, isBlacklisted)
│   └── impl/
│       ├── AdminAccountServiceImpl.java          # NEW — account CRUD, password gen, email, @Transactional
│       └── TokenBlacklistServiceImpl.java        # NEW — Redis-backed (StringRedisTemplate, TTL = token exp)
├── controller/admin/
│   └── AdminAccountController.java              # NEW — REST endpoints
├── security/
│   └── AdminJwtAuthenticationFilter.java        # MODIFY — check TokenBlacklistService on each request
├── req/admin/
│   ├── CreateAdminAccountReq.java               # NEW
│   ├── UpdateAccountStatusReq.java              # NEW
│   └── UpdateAccountRoleReq.java                # NEW
├── res/admin/
│   └── AdminAccountRes.java                     # NEW
└── service/
    └── EmailService.java                         # MODIFY — add sendInitialPasswordEmail(to, name, pwd)

src/main/resources/
└── application.yml                              # MODIFY — add spring.data.redis config block
```

**現有實體（無需變更資料庫結構）**：

| 實體 | 關鍵欄位 | 狀態 |
|--------|-----------|--------|
| `AdminUser` | id (UUID), username, password (BCrypt), email, displayName, phone, status, forceChangePassword, lastLoginAt, createdBy, createdAt | 已存在 |
| `StoreUser` | id (UUID), storeId, adminUserId, roleType, createdAt | 已存在 |
| `Store` | id (UUID), ownerId (→ AdminUser.id), storeName, status | 已存在 |
| `AdminUserStatus` | PENDING, ACTIVE, INACTIVE | 已存在（列舉） |

**架構決策**：單一 Spring Boot 專案（`com.group.admin`）。所有新檔案遵循現有套件命名慣例。Redis 是唯一的新基礎設施依賴。

## 複雜度追蹤

> 無違規 — 無需說明複雜度原因。
