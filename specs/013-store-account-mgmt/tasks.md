# Tasks: 店家帳號管理 (Store Account Management)

**Input**: `specs/013-store-account-mgmt/` 的設計文件  
**Prerequisites**: plan.md ✅, spec.md ✅, data-model.md ✅, research.md ✅, contracts/ ✅, quickstart.md ✅  
**Branch**: `013-store-account-mgmt`  
**Generated**: 2026-03-22  

**Tests**: 本規格未明確要求 TDD 方式，不包含測試任務。

**Organisation**: 任務依使用者故事分組，每個故事可獨立實作與測試。

---

## Format: `[ID] [P?] [Story?] Description`

- **[P]**: 可平行執行（不同檔案，無相依性）
- **[Story]**: 對應的使用者故事（US1、US2、US3、US4）
- 說明中包含確切的檔案路徑

---

## Phase 1：初始設定（Infrastructure Setup）

**目的**：新增 Redis 基礎設施依賴並完成專案初始化。現有資料庫結構無需變更（data-model.md 確認）。

- [ ] T001 在 `pom.xml` 的 `<dependencies>` 區塊新增 `spring-boot-starter-data-redis` 依賴（參考 quickstart.md §1）
- [ ] T002 在 `src/main/resources/application.yml` 新增 `spring.data.redis` 設定區塊（host、port、timeout: 2000ms、lettuce pool；參考 quickstart.md §2）
- [ ] T003 [P] 建立 `src/main/java/com/group/admin/config/RedisConfig.java`：宣告 `StringRedisTemplate` 及 `RedisConnectionFactory` Beans（設定 `GenericObjectPoolConfig`；無需自訂 serializer，預設 String 即可）

**Checkpoint**：Redis 依賴加入完成、設定就緒，`mvn compile` 不報錯。

---

## Phase 2：基礎建設（Foundational — 阻塞所有使用者故事）

**目的**：Token 黑名單服務與 JWT Filter 強化 — **所有使用者故事均依賴此 Phase 完成。**

**⚠️ 重要**：此 Phase 完成前，任何使用者故事的實作皆不可進行。

- [ ] T004 [P] 建立 `src/main/java/com/group/admin/service/TokenBlacklistService.java` 介面，定義兩個方法：`void invalidateUser(String adminUserId)` 及 `boolean isBlacklisted(String adminUserId, long tokenGen)`（依 research.md R-001 世代計數器協議）
- [ ] T005 建立 `src/main/java/com/group/admin/service/impl/TokenBlacklistServiceImpl.java`：注入 `StringRedisTemplate`；`invalidateUser()` 執行 `INCR blacklist_gen:{adminUserId}`（TTL 30 天）；`isBlacklisted()` 讀取計數器並比較 `redisGen > tokenGen`；Redis 無法連線時預設**拒絕**（丟出例外），安全優於可用性（data-model.md Redis 降級策略）
- [ ] T006 修改 `src/main/java/com/group/admin/config/SecurityConfig.java`：注入 `TokenBlacklistService` Bean，並傳遞給 `AdminJwtAuthenticationFilter` 建構式或 setter
- [ ] T007 修改 `src/main/java/com/group/admin/security/AdminJwtAuthenticationFilter.java`：(1) 每次已驗證請求從 JWT 宣告讀取 `gen` 值，與 Redis `blacklist_gen:{adminUserId}` 比對，若 `redisGen > tokenGen` 則回傳 `401 Unauthorized`；(2) 若 JWT 宣告 `forceChangePassword=true` 且請求路徑不為 `/admin/auth/**`，則回傳 `403 Forbidden` 含 `"Password change required before accessing this resource"` 訊息（contract: POST_admin_auth_first-login.md）

**Checkpoint**：`TokenBlacklistService` Bean 已正確注入；Filter 攔截邏輯可透過單元測試或手動呼叫驗證。

---

## Phase 3：使用者故事 1 — 管理員建立店家負責人帳號（P1）🎯 MVP

**目標**：管理員可建立 STORE_OWNER 帳號，系統原子性完成帳號建立 + 店家綁定 + 初始密碼電子郵件寄送，並提供帳號列表功能。

**獨立測試**：管理員提交含 email 及目標 storeId 的建立請求，驗證：(1) 回傳 `201 Created` 含 PENDING 狀態；(2) `admin_user` 表新增記錄；(3) `store_user` 表新增 STORE_OWNER 記錄；(4) `store.owner_id` 更新為新帳號 ID；(5) 初始密碼電子郵件非同步寄出（參考 quickstart.md §5）。

### Phase 3 實作任務

- [ ] T008 [P] [US1] 建立 `src/main/java/com/group/admin/req/admin/CreateAdminAccountReq.java`：欄位包含 `email`（`@Email @NotBlank`）、`displayName`（`@NotBlank @Size(max=100)`）、`phone`（選填）、`roleType`（`@NotNull` 列舉 `STORE_OWNER`/`STORE_EDITOR`）、`storeId`（`@NotBlank`）、`remark`（`@Size(max=500)` 選填）
- [ ] T009 [P] [US1] 建立 `src/main/java/com/group/admin/res/admin/AdminAccountRes.java`：欄位包含 `id`、`email`、`displayName`、`phone`、`status`、`forceChangePassword`、`lastLoginAt`、`roleType`、`storeId`、`storeName`、`createdBy`、`createdAt`（對應 contract: POST_admin_accounts.md 及 GET_admin_accounts.md 回應結構）
- [ ] T010 [US1] 建立 `src/main/java/com/group/admin/service/AdminAccountService.java` 介面，宣告：`AdminAccountRes createAccount(CreateAdminAccountReq req, String adminUserId)`、`Page<AdminAccountRes> listAccounts(AccountFilterCondition filters, int page, int size)`、`AdminAccountRes updateStatus(String id, String status, String remark, String adminUserId)`、`AdminAccountRes updateRole(String id, String roleType, String storeId, String adminUserId)`
- [ ] T011 [US1] 修改 `src/main/java/com/group/admin/service/EmailService.java`：新增 `sendInitialPasswordEmail(String to, String displayName, String initialPassword)` 方法（標註 `@Async`，使用 Thymeleaf 範本變數 `displayName`、`initialPassword`、`loginUrl`）；同時建立 `src/main/resources/templates/initial-password-email.html` Thymeleaf 範本（含帳號資訊、初始密碼及登入連結）
- [ ] T012 [US1] 建立 `src/main/java/com/group/admin/service/impl/AdminAccountServiceImpl.java` 並實作 `createAccount()`：(1) 以 `SecureRandom` 產生 8–12 字元密碼（保證含 1 個大寫 + 1 個小寫 + 1 個數字；research.md R-002 演算法）；(2) BCrypt 編碼密碼；(3) 驗證 email 唯一性（`AdminUserMapper` 查詢）；(4) 驗證 storeId 存在；(5) STORE_OWNER 時驗證店家尚未有擁有者；(6) `@Transactional`：`AdminUserMapper.insertSelective()`（status=PENDING, forceChangePassword=true, createdBy=adminUserId）+ `StoreUserMapper.insertSelective()`（roleType=STORE_OWNER）+ `StoreMapper.updateByPrimaryKeySelective()`（ownerId 更新）；(7) 呼叫 `EmailService.sendInitialPasswordEmail()` 非同步寄送；(8) 回傳 `AdminAccountRes`
- [ ] T013 [US1] 建立 `src/main/java/com/group/admin/controller/admin/AdminAccountController.java`：實作 `POST /admin/accounts` 端點（`@PreAuthorize` 限 ADMIN 角色），呼叫 `AdminAccountService.createAccount()`，成功回傳 `201 Created`；依 contract 規格回傳對應錯誤碼（409/404/400/403/500）
- [ ] T014 [US1] 在 `AdminUserMapper.java` 及對應的 XML Mapper（`AdminUserMapper.xml`）新增自訂方法 `selectAccountsWithRole(@Param("filters") AccountFilterCondition filters)` 及 `countAccountsWithRole(@Param("filters") AccountFilterCondition filters)`：SQL 使用 `LEFT JOIN store_user` + `LEFT JOIN store`，支援依 status、roleType、storeId、keyword（email/displayName LIKE）動態篩選；同時建立 `src/main/java/com/group/admin/req/admin/AccountFilterCondition.java` 查詢條件物件（含 status、roleType、storeId、keyword、sortBy、sortDir）
- [ ] T015 [US1] 實作 `AdminAccountServiceImpl.listAccounts()`（呼叫 T014 自訂 Mapper 方法，組裝 Page 回應）；在 `AdminAccountController` 新增 `GET /admin/accounts` 端點，支援查詢參數 `page`（預設 0）、`size`（預設 20, 最大 100）、`status`、`roleType`、`storeId`、`keyword`、`sortBy`（預設 `createdAt`）、`sortDir`（預設 `DESC`）

**Checkpoint**：此時使用者故事 1 可完整且獨立測試。`POST /admin/accounts`（STORE_OWNER）及 `GET /admin/accounts` 均可正常運作。

---

## Phase 4：使用者故事 2 — 新用戶首次登入並修改密碼（P1）

**目標**：新帳號首次登入時強制修改密碼，成功後帳號從 PENDING 轉為 ACTIVE，舊 Token 立即失效。

**獨立測試**：新帳號以初始密碼登入（回應含 `forceChangePassword: true`）→ 嘗試存取其他端點回傳 `403` → 呼叫 `POST /admin/auth/first-login/change-password` 成功 → 帳號 status 變為 ACTIVE、forceChangePassword=false → 舊 Token 回傳 `401`（參考 quickstart.md §6）。

### Phase 4 實作任務

- [ ] T016 [US2] 修改 `src/main/java/com/group/admin/controller/admin/AdminAuthController.java` 的 `firstLoginChangePassword()` 方法：確認並完善以下流程：(1) BCrypt `passwordEncoder.matches()` 驗證 oldPassword；(2) 新舊密碼不得相同（400）；(3) newPassword 最少 8 字元（400）；(4) `forceChangePassword` 已為 false 時回傳 403；(5) BCrypt 編碼 newPassword；(6) 更新 `AdminUser`（password, forceChangePassword=false, status=ACTIVE, updatedAt=now()）；(7) 呼叫 `TokenBlacklistService.invalidateUser(adminUserId)` 使舊 Token 失效（contract: POST_admin_auth_first-login.md）；(8) 產生並回傳含當前 blacklist_gen 世代值的新 Token 對
- [ ] T017 [US2] 驗證並確認 `AdminJwtAuthenticationFilter`（T007 完成後）的 `forceChangePassword` 防護邏輯正確攔截：僅允許 `/admin/auth/**` 路徑通過，其餘所有路徑一律回傳 `403 Forbidden`（符合 contract: POST_admin_auth_first-login.md「關鍵行為：在其他端點前的防護」規格）

**Checkpoint**：此時使用者故事 2 可完整且獨立測試。新帳號首次登入強制修改密碼流程完整運作。

---

## Phase 5：使用者故事 3 — 管理員建立店家編輯帳號（P2）

**目標**：管理員可建立 STORE_EDITOR 帳號，透過 StoreUser 多對多關聯綁定至店家，不更新 Store.ownerId。

**獨立測試**：管理員為店家 A 建立 STORE_EDITOR 帳號，驗證：(1) 回傳 201 Created；(2) `store_user` 新增 STORE_EDITOR 記錄；(3) `store.owner_id` **不變**；(4) 同一店家可建立多個 STORE_EDITOR（參考 quickstart.md 冒煙測試流程）。

### Phase 5 實作任務

- [ ] T018 [US3] 擴充 `AdminAccountServiceImpl.createAccount()` 支援 `roleType=STORE_EDITOR`：`@Transactional` 執行 `AdminUserMapper.insertSelective()`（status=PENDING, forceChangePassword=true）+ `StoreUserMapper.insertSelective()`（roleType=STORE_EDITOR）；**不執行** `Store.ownerId` 更新；驗證同一店家不得重複指定 STORE_OWNER（STORE_EDITOR 則允許多個）；電子郵件寄送邏輯與 US1 相同（非同步）
- [ ] T019 [US3] 確認 `AdminAccountController` 的 `POST /admin/accounts` 端點能正確處理 `roleType=STORE_EDITOR` 請求（由 T018 的 `createAccount()` roleType 分支處理，無需新增控制器方法）；驗證回應結構與合約 POST_admin_accounts.md 一致

**Checkpoint**：此時使用者故事 3 可完整且獨立測試。`POST /admin/accounts`（STORE_EDITOR）正常運作。

---

## Phase 6：使用者故事 4 — 管理員啟用或停用帳號（P2）

**目標**：管理員可啟用/停用帳號（停用時 Redis INCR 立即使 Token 失效），並可變更帳號角色/店家綁定。

**獨立測試**：停用已啟用帳號 → 立即使用舊 Token 呼叫任何端點回傳 `401`；重新啟用帳號 → 舊 Token 仍無效需重新登入；變更角色至 STORE_EDITOR → 舊 STORE_OWNER 記錄刪除，新 STORE_EDITOR 記錄新增（參考 quickstart.md §7）。

### Phase 6 實作任務

- [ ] T020 [P] [US4] 建立 `src/main/java/com/group/admin/req/admin/UpdateAccountStatusReq.java`：欄位 `status`（`@NotBlank`，僅允許 `ACTIVE`/`INACTIVE`）及 `remark`（`@Size(max=500)` 選填）
- [ ] T021 [P] [US4] 建立 `src/main/java/com/group/admin/req/admin/UpdateAccountRoleReq.java`：欄位 `roleType`（`@NotBlank`，`STORE_OWNER`/`STORE_EDITOR`）及 `storeId`（`@NotBlank` 有效 UUID）
- [ ] T022 [US4] 實作 `AdminAccountServiceImpl.updateStatus(String id, String status, String remark, String adminUserId)`：(1) 驗證 id 存在（404）；(2) 驗證 status 不為 PENDING（400）；(3) 驗證管理員不能停用自己（403）；(4) 更新 `admin_user.status`、`updated_by`、`updated_at`（選填 remark）；(5) status=INACTIVE 時呼叫 `TokenBlacklistService.invalidateUser(id)` 執行 Redis INCR（SC-003：< 1 秒；contract: PUT_admin_accounts_{id}_status.md）；(6) 回傳更新後的帳號資訊
- [ ] T023 [US4] 實作 `AdminAccountServiceImpl.updateRole(String id, String roleType, String storeId, String adminUserId)`：`@Transactional`：(1) 驗證帳號及 storeId 存在；(2) 查詢並刪除該帳號現有的 `StoreUser` 記錄（`StoreUserMapper.deleteByExample()`）；(3) 若舊角色為 STORE_OWNER，清除舊店家的 `Store.ownerId`（設為 NULL）；(4) 插入新 `StoreUser` 記錄；(5) 若新角色為 STORE_OWNER，驗證目標店家尚無擁有者，並設定 `Store.ownerId`；(6) 更新 `admin_user.updated_by`、`updated_at`；(7) 回傳更新後資訊（contract: PUT_admin_accounts_{id}_role.md）
- [ ] T024 [US4] 在 `AdminAccountController` 新增兩個端點：(1) `PUT /admin/accounts/{id}/status`（呼叫 `updateStatus()`，回傳 200 OK；`@PreAuthorize` 限 ADMIN）；(2) `PUT /admin/accounts/{id}/role`（呼叫 `updateRole()`，回傳 200 OK；`@PreAuthorize` 限 ADMIN）

**Checkpoint**：此時使用者故事 4 可完整且獨立測試。啟用/停用及角色變更端點正常運作，停用後 Token 立即失效。

---

## Phase 7：完善與橫切關注點（Polish & Cross-Cutting Concerns）

**目的**：確認所有合約、稽核軌跡及端對端流程符合規格。

- [ ] T025 [P] 驗證所有端點的錯誤回應是否符合合約規格（`contracts/` 目錄）：`POST /admin/accounts`（409 重複 email、409 店家已有擁有者、404 店家不存在、400 驗證失敗、403 非 ADMIN）；`PUT /admin/accounts/{id}/status`（404 帳號不存在、400 無效狀態值、403 停用自己）；`PUT /admin/accounts/{id}/role`（404 帳號/店家不存在、409 店家已有擁有者）；`GET /admin/accounts`（400 無效篩選條件）
- [ ] T026 確認所有帳號操作（建立、狀態更新、角色更新）均正確寫入稽核欄位：`created_by`/`updated_by` 設為呼叫管理員的 adminUserId；`created_at`/`updated_at` 設為正確時間戳記（FR-010 稽核軌跡要求）
- [ ] T027 依據 `specs/013-store-account-mgmt/quickstart.md` 執行完整端對端冒煙測試：(1) §5 建立 StoreOwner 帳號，驗證 201 + PENDING + 電子郵件；(2) §6 首次登入密碼變更，驗證 status=ACTIVE + 舊 Token 失效 + 新 Token 可用；(3) §7 停用帳號，驗證停用後使用舊 Token 立即回傳 `401 Unauthorized`（SC-003 < 1 秒）；(4) §8 列出帳號，驗證分頁與篩選正常

---

## 相依關係與執行順序

### Phase 相依關係

```
Phase 1（初始設定）
    └──→ Phase 2（基礎建設）     ← 阻塞所有使用者故事
              ├──→ Phase 3（US1 P1）🎯 MVP
              ├──→ Phase 4（US2 P1）  ← 依賴 Phase 2（JWT Filter 強化）
              ├──→ Phase 5（US3 P2）  ← 依賴 Phase 3（AdminAccountServiceImpl 已存在）
              └──→ Phase 6（US4 P2）  ← 依賴 Phase 3（AdminAccountController 已存在）
                        └──→ Phase 7（完善）
```

### 使用者故事相依關係

- **US1（P1）**：Phase 2 完成後可開始 — 無其他故事相依
- **US2（P1）**：Phase 2 完成後可開始（AdminJwtAuthenticationFilter T007 已完成）— 無其他故事相依
- **US3（P2）**：US1 Phase 3 完成後可開始（`AdminAccountServiceImpl` 已存在，只需擴充 STORE_EDITOR 分支）
- **US4（P2）**：US1 Phase 3 完成後可開始（`AdminAccountController` 框架已存在，新增端點）

### 各故事內部執行順序

```
Phase 3 US1:
  T008, T009（平行）→ T010 → T011 → T012 → T013 → T014 → T015

Phase 4 US2:
  T016 → T017（驗證 T007）

Phase 5 US3:
  T018 → T019

Phase 6 US4:
  T020, T021（平行）→ T022 → T023 → T024
```

### 平行執行機會

- **Phase 1**：T003 可與 T001+T002 平行
- **Phase 2**：T004 可與 T006 準備工作平行；T005 完成後才可做 T006、T007
- **US1 Phase 3**：T008、T009 可平行；T014 可在 T012 進行時平行進行
- **US3、US4（P2）**：Phase 2 完成後，若有多位開發者，US1（P1）和 US2（P1）可平行進行；US3 和 US4 在 US1 完成後可平行進行

---

## 平行執行範例

### 使用者故事 1 — 帳號建立基礎

```bash
# 平行執行 DTO 建立：
Task: "建立 CreateAdminAccountReq.java (T008)"
Task: "建立 AdminAccountRes.java (T009)"

# 平行執行帳號服務核心 + Mapper：
Task: "實作 AdminAccountServiceImpl.createAccount() (T012)"
Task: "新增 AdminUserMapper 自訂方法 (T014)"
```

### 使用者故事 4 — 帳號生命週期管理

```bash
# 平行執行 Request DTO：
Task: "建立 UpdateAccountStatusReq.java (T020)"
Task: "建立 UpdateAccountRoleReq.java (T021)"

# 序列執行服務實作後再加 Controller：
Task: "實作 updateStatus() (T022)" → "實作 updateRole() (T023)" → "新增 Controller 端點 (T024)"
```

---

## 實作策略

### MVP（僅使用者故事 1）

1. 完成 Phase 1：初始設定（T001–T003）
2. 完成 Phase 2：基礎建設（T004–T007）— **⚠️ 必須完成，阻塞後續**
3. 完成 Phase 3：使用者故事 1（T008–T015）
4. **停止並驗證**：測試 `POST /admin/accounts`（STORE_OWNER）及 `GET /admin/accounts`
5. 可部署示範 MVP

### 增量交付

1. Setup + Foundational → 基礎就緒（Phase 1–2）
2. US1（P1）→ 獨立測試 → 部署 **MVP**（Phase 3）
3. US2（P1）→ 獨立測試 → 首次登入流程完整（Phase 4）
4. US3（P2）→ 獨立測試 → 店家編輯帳號功能完整（Phase 5）
5. US4（P2）→ 獨立測試 → 帳號生命週期管理完整（Phase 6）
6. 完善與冒煙測試（Phase 7）

### 多人平行策略

若有兩位以上開發者：

1. 所有人完成 Phase 1 + Phase 2
2. Phase 2 完成後：
   - **開發者 A**：US1（P1）Phase 3
   - **開發者 B**：US2（P1）Phase 4
3. US1 完成後（Phase 3 checkpoint 通過）：
   - **開發者 A**：US3（P2）Phase 5
   - **開發者 B**：US4（P2）Phase 6

---

## 任務總覽

| Phase | 範圍 | 任務數 |
|-------|------|--------|
| Phase 1：初始設定 | T001–T003 | 3 |
| Phase 2：基礎建設 | T004–T007 | 4 |
| Phase 3：US1 P1 店家負責人帳號建立 🎯 | T008–T015 | 8 |
| Phase 4：US2 P1 首次登入密碼變更 | T016–T017 | 2 |
| Phase 5：US3 P2 店家編輯帳號建立 | T018–T019 | 2 |
| Phase 6：US4 P2 帳號啟用／停用 | T020–T024 | 5 |
| Phase 7：完善與橫切關注點 | T025–T027 | 3 |
| **合計** | | **27** |

### 平行機會

- Phase 1 + Phase 2 內共 4 個可平行任務（[P] 標記）
- Phase 3 US1 內共 2 個可平行任務
- Phase 6 US4 內共 2 個可平行任務
- **US1 + US2 可由不同開發者同步進行（Phase 2 完成後）**
- **US3 + US4 可由不同開發者同步進行（Phase 3 完成後）**

---

## 備註

- `[P]` 標記 = 不同檔案，無相依性，可安全平行執行
- `[USn]` 標記對應 `spec.md` 中的使用者故事，確保可追溯性
- 每個使用者故事應可獨立完成與測試
- 在每個 **Checkpoint** 停下來，獨立驗證故事功能
- 每個任務或邏輯群組後提交（commit）
- 現有資料庫結構無需修改（data-model.md 確認）— 僅新增 Redis 基礎設施
- 密碼絕不在 API 回應中回傳，僅透過電子郵件寄送（FR-011）
- 所有服務操作需標註 `@Transactional` 確保原子性（FR-012）
