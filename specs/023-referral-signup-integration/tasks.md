# 任務清單：推薦碼於使用者註冊流程整合

**功能分支**：`023-referral-signup-integration`  
**優先級**：P1  
**狀態**：後端實作完成，待 DB 遷移後完全上線  

---

## 第 1 階段：資料庫 & 資料模型

### T001 — 建立 DB 遷移指令碼 ✅
**檔案**：`sql/V_2026_04_14__add_referral_signup_integration.sql`  
**指令碼複本**：`src/main/resources/db/migration/V_2026_04_14__add_referral_signup_integration.sql`  
**說明**：`DataSourceInitializer` 將於應用啟動時自動偵測並執行（若欄位已存在則跳過）  
- [X] SQL 指令碼建立完畢  
- [ ] **待確認**：在 MySQL 執行後驗證欄位存在  
  ```bash
  # 手動確認：在 MySQL 執行
  SELECT column_name FROM information_schema.COLUMNS
  WHERE table_name='user' AND column_name IN ('referral_bound_at','is_oauth_new_user');
  ```

---

### T002 — MBG 重新生成 + 手動補欄位
**說明**：`User.java` 已手動加入 `referralBoundAt` 和 `isOauthNewUser` getter/setter  
- [X] `User.java` 已手動加入 `referralBoundAt`, `isOauthNewUser` 欄位  
- [ ] **待執行**：DB 遷移成功後，執行 MBG 讓 UserMapper.xml 也反映新欄位  
  ```bash
  mvn mybatis-generator:generate
  ```
  > ⚠️ MBG 執行前必須確保 DB 欄位已存在，否則會還原欄位

---

## 第 2 階段：後端 API 層 ✅ 全部完成

### T003 — DTO 類別 ✅
- [X] `req/referral/ReferralValidationReq.java`
- [X] `res/referral/ReferralValidationRes.java`
- [X] `req/referral/ApplyReferralReq.java`

### T004 — AuthRegisterReq 已有 referralCode ✅
- [X] `AuthRegisterReq.referralCode` 欄位已存在（`@Size(max=20)`, 選填）

### T005 — 推薦碼驗證端點 ✅
- [X] `POST /api/auth/validate-referral` — `ApiAuthController.validateReferral()`
  - 公開端點（無需登入）
  - 回傳 `ReferralValidationRes`（isValid, referrerName, storeId, errorMessage）

### T006 — Email 註冊推薦碼綁定修復 ✅
- [X] `UserServiceImpl.register()` — `useCode()` 成功後同步寫入 `user.referralCode` + `user.referredStoreId` + `user.referralBoundAt`

### T007 — OAuth 新用戶標記 ✅
- [X] `UserServiceImpl.loginWithGoogle()` — 新用戶建立時 `setIsOauthNewUser(1)`
- [X] `AuthRes.isNewUser` 新增欄位，OAuth 新用戶登入回傳 `isNewUser: true`

### T008 — OAuth 補推薦碼端點 ✅
- [X] `UserService.applyReferral(userId, code)` 介面方法
- [X] `UserServiceImpl.applyReferral()` 實作（含一次性鎖定保護）
- [X] `POST /api/user/apply-referral` — `UserController.applyReferral()`
  - 需要 JWT 認證
  - 已綁定則拋 `IllegalArgumentException`

---

## 第 3 階段：待完成事項

### T009 — Security 白名單確認 ⚠️
**確認** `/api/auth/validate-referral` 已在 `SecurityConfig` 的 permitAll 清單中  
（通常 `/api/auth/**` 已全部開放，但請確認）

### T010 — MBG 執行（DB 遷移後） ⏳
- [ ] 執行 `mvn mybatis-generator:generate`  
- [ ] 確認 `UserMapper.xml` 的 `<resultMap>` 包含 `referral_bound_at`, `is_oauth_new_user`

### T011 — 前端整合 ⏳
- [ ] 官網註冊表單新增推薦碼輸入框（選填）
- [ ] 輸入時呼叫 `POST /api/auth/validate-referral` 即時驗證
- [ ] Google OAuth 登入後若 `isNewUser: true`，顯示補碼彈窗
- [ ] 補碼彈窗呼叫 `POST /api/user/apply-referral`


- [ ] 驗證規則正確
- [ ] 與現有欄位不衝突

**估時**：15 分鐘  
**狀態**：⏳ 未開始

---

### T005 — 建立 ReferralValidationController

**任務**：新增公開驗證端點  
**檔案**：`src/main/java/com/group/admin/controller/api/ReferralValidationController.java`  
**端點**：`POST /api/auth/validate-referral`  
**驗收標準**：
- [ ] 端點無需認證（@PermitAll）
- [ ] 接受 JSON 請求體：{ "code": "..." }
- [ ] 調用 `ReferralCodeService.validateAndGetReferralCode()`
- [ ] 回傳 JSON：{ "valid": bool, "code": "...", "storeName": "...", "reason": "..." }
- [ ] 適當錯誤處理（400 Bad Request, 500 Internal Server Error）

**估時**：1 小時  
**狀態**：⏳ 未開始

---

### T006 — 擴充 UserService.register()

**任務**：整合推薦碼綁定邏輯到官網註冊  
**檔案**：
- `src/main/java/com/group/admin/service/UserService.java`
- `src/main/java/com/group/admin/service/impl/UserServiceImpl.java`

**驗收標準**：
- [ ] 簽名：`User register(AuthRegisterReq req)` 保持不變
- [ ] 若 `req.referralCode` 非空：
  - 呼叫 `referralCodeService.validateAndGetReferralCode()`
  - 驗證成功 → 設定 `user.referralCode` 和 `user.referredStoreId`
  - 驗證失敗 → 拋出 `BusinessException`（註冊中止）
- [ ] 建立 User 記錄後，若綁定推薦碼 → 建立 `referral_record`
- [ ] `@Transactional` 確保整體原子性
- [ ] 邏輯測試覆蓋 ✓

**估時**：2 小時  
**狀態**：⏳ 未開始

---

### T007 — 實作 ReferralCodeService 方法（核心）

**任務**：在 ReferralCodeServiceImpl 新增 / 擴充方法  
**檔案**：`src/main/java/com/group/admin/service/impl/ReferralCodeServiceImpl.java`  
**新方法**：

1. **`validateAndGetReferralCode(String code)`**
   - 驗證邏輯：存在 + 活躍 + 未超限 + 未過期 + 店家活躍
   - 回傳 `ReferralCode` 物件或 `null`
   - 不拋異常；由呼叫端判斷

2. **`applyReferralForUser(String userId, String code)`**
   - 驗證用戶存在
   - 驗證用戶未綁定推薦碼 → 若已綁定 → 拋 `BusinessException`
   - 驗證推薦碼有效性
   - 防自推薦檢查
   - 更新 `user` 表（`referralCode`, `referredStoreId`, `referralBoundAt`）
   - 建立 `referral_record`（signup_method = 'OAUTH'）
   - 遞增推薦碼使用計數
   - `@Transactional` 包裝

3. **`createReferralRecord(User, String code, String method)`**
   - 私有方法
   - 建立 `ReferralRecord` 物件
   - 填充欄位：userId, code, storeId, signupMethod, createdAt
   - 插入 DB

**驗收標準**：
- [ ] 三個方法實作完成
- [ ] 異常情況處理正確（推薦碼不存在、已停用、已超限、已過期、店家停用、已綁定、自推薦）
- [ ] 交易邏輯正確
- [ ] 邏輯測試覆蓋

**估時**：2-3 小時  
**狀態**：⏳ 未開始

---

### T008 — 修改 UserService.loginWithGoogle()

**任務**：整合 OAuth 新用戶標記  
**檔案**：`src/main/java/com/group/admin/service/impl/UserServiceImpl.java`  
**驗收標準**：
- [ ] 新用戶建立時，設置 `user.isOauthNewUser = 1`
- [ ] 既存用戶登入，不改動此欄位
- [ ] AuthRes 新增 `isNewUser` 欄位（對應 is_oauth_new_user）
- [ ] 測試覆蓋

**估時**：1 小時  
**狀態**：⏳ 未開始

---

### T009 — 新增 UserController.applyReferral()

**任務**：在 UserController 新增已認證端點供 OAuth 新用戶補碼  
**檔案**：`src/main/java/com/group/admin/controller/api/UserController.java`  
**端點**：`POST /api/user/apply-referral`  
**驗收標準**：
- [ ] 需認證 `@PreAuthorize("isAuthenticated()")`
- [ ] 接受 JSON：{ "code": "..." }
- [ ] 從 JWT 提取 userId：`SecurityUtils.getCurrentUserId()`
- [ ] 調用 `referralCodeService.applyReferralForUser(userId, code)`
- [ ] 異常處理：推薦碼無效 / 已綁定 / 超限
- [ ] 回傳 204 No Content（成功）

**估時**：1 小時  
**狀態**：⏳ 未開始

---

### T010 — 修改 UserRes DTO

**任務**：新增回傳欄位支援前端判斷  
**檔案**：`src/main/java/com/group/admin/res/user/UserRes.java`  
**新欄位**：
- `referralCode` (String) — 推薦碼（已綁定時回傳）
- `isNewUser` (boolean) — OAuth 新用戶標記（用於決定是否進補碼流程）
- `referredStoreId` (String) — 推薦來源店家 ID

**估時**：30 分鐘  
**狀態**：⏳ 未開始

---

### T011 — 修改 AuthRes DTO

**任務**：新增 `isNewUser` 欄位  
**檔案**：`src/main/java/com/group/admin/res/AuthRes.java`  
**效果**：登入後回傳，前端判斷是否進新用戶導覽  
**估時**：15 分鐘  
**狀態**：⏳ 未開始

---

## 第 3 階段：安全性

### T012 — 實作應用層一次性防護

**任務**：確保 `referralCode` 不可變更  
**檔案**：`src/main/java/com/group/admin/service/impl/ReferralCodeServiceImpl.java`

**驗收標準**：
- [ ] `applyReferralForUser()` 檢查 `user.referralCode != null` → 拋 `BusinessException`
- [ ] `UserController` 不暴露修改 `referralCode` 的端點
- [ ] 測試覆蓋

**估時**：1 小時  
**狀態**：⏳ 未開始

---

### T013 — 新增 DB UNIQUE 約束

**任務**：確保資料層一次性不可變  
**檔案**：已含在 T001（DB 遷移）  
**驗收標準**：
- [ ] `user` 表 `referral_code` 欄位 UNIQUE
- [ ] 執行 DDL 後驗證約束生效

**估時**：已含在 T001  
**狀態**：⏳ 未開始

---

### T014 — 推薦碼驗證端點限速

**任務**：防止暴力破解驗證端點  
**檔案**：
- `src/main/java/com/group/admin/controller/api/ReferralValidationController.java`
- （可選）建立 AOP RateLimit 註解

**驗收標準**：
- [ ] `/api/auth/validate-referral` 限制：每 IP 每 60 秒最多 10 次
- [ ] 超限回傳 429 Too Many Requests

**估時**：1 小時  
**狀態**：⏳ 未開始

---

## 第 4 階段：API 合約 & 文件

### T015 — 編寫 API 合約（驗證端點）

**檔案**：`specs/023-referral-signup-integration/contracts/POST_api_auth_validate_referral.md`  
**內容**：
- 請求體、回應體、錯誤情況
- 範例

**估時**：1 小時  
**狀態**：⏳ 未開始

---

### T016 — 編寫 API 合約（補碼端點）

**檔案**：`specs/023-referral-signup-integration/contracts/POST_api_user_apply_referral.md`  
**內容**：
- 認證要求、請求體、回應體
- 錯誤情況（已綁定、推薦碼無效、超限）

**估時**：1 小時  
**狀態**：⏳ 未開始

---

### T017 — 修改 API 合約（註冊端點）

**檔案**：`specs/023-referral-signup-integration/contracts/POST_api_auth_register.md`  
**修改**：
- 新增可選欄位 `referralCode`
- 說明驗證邏輯

**估時**：30 分鐘  
**狀態**：⏳ 未開始

---

### T018 — 編寫 API 合約（GET /api/user/me）

**檔案**：`specs/023-referral-signup-integration/contracts/GET_api_user_me.md`  
**修改**：
- 新增回傳欄位：`referralCode`, `isNewUser`, `referredStoreId`

**估時**：30 分鐘  
**狀態**：⏳ 未開始

---

## 第 5 階段：測試

### T019 — 撰寫 Service 層單元測試

**檔案**：`src/test/java/com/group/admin/service/impl/ReferralCodeServiceImplSignupTest.java`  
**測試用例**：
- [ ] `validateAndGetReferralCode_Valid()` — 有效碼回傳對象
- [ ] `validateAndGetReferralCode_NotExists()` — 不存在回傳 null
- [ ] `validateAndGetReferralCode_Disabled()` — 已停用回傳 null
- [ ] `validateAndGetReferralCode_ExceedLimit()` — 已超限回傳 null
- [ ] `validateAndGetReferralCode_Expired()` — 已過期回傳 null
- [ ] `validateAndGetReferralCode_StoreInactive()` — 店家停用回傳 null
- [ ] `applyReferralForUser_Success()` — 綁定成功
- [ ] `applyReferralForUser_AlreadyBound()` — 已綁定拋異常
- [ ] `applyReferralForUser_InvalidCode()` — 無效碼拋異常

**驗收標準**：
- [ ] 所有用例通過
- [ ] 覆蓋率 ≥ 90%

**估時**：2 小時  
**狀態**：⏳ 未開始

---

### T020 — 撰寫 Controller 層集成測試

**檔案**：`src/test/java/com/group/admin/controller/api/ReferralValidationControllerTest.java`  
**測試用例**：
- [ ] `validateReferral_Valid()` — 有效碼回傳 200 + 店家名稱
- [ ] `validateReferral_Invalid()` — 無效碼回傳 200 + valid=false + reason
- [ ] `validateReferral_EmptyCode()` — 空碼回傳 400 Bad Request

**估時**：1 小時  
**狀態**：⏳ 未開始

---

### T021 — 撰寫 User 註冊集成測試

**檔案**：`src/test/java/com/group/admin/controller/api/UserControllerSignupTest.java`  
**測試用例**：
- [ ] `register_WithValidReferral()` — 官網註冊 + 推薦碼成功
- [ ] `register_WithInvalidReferral()` — 官網註冊 + 無效碼失敗
- [ ] `register_WithoutReferral()` — 官網註冊無碼正常
- [ ] `loginGoogle_NewUser_ReturnsIsNewUser()` — OAuth 新用戶回傳 isNewUser=true
- [ ] `applyReferral_NewOAuthUser_Success()` — OAuth 新用戶補碼成功
- [ ] `applyReferral_ExistingUser_Forbidden()` — 既存用戶補碼失敗
- [ ] `applyReferral_AlreadyBound_Forbidden()` — 已綁定補碼失敗

**估時**：2-3 小時  
**狀態**：⏳ 未開始

---

## 第 6 階段：前端文件

### T022 — 更新 01-auth.md

**路徑**：`frontend/client/01-auth.md`  
**修改**：
- [ ] 官網註冊合約新增 `referralCode` 欄位說明
- [ ] 新增「推薦碼驗證」章節（POST /api/auth/validate-referral）
- [ ] 新增「Google 登入新用戶導覽」章節

**估時**：1 小時  
**狀態**：⏳ 未開始

---

### T023 — 更新 02-user-profile.md

**路徑**：`frontend/client/02-user-profile.md`  
**修改**：
- [ ] `GET /api/user/me` 新增回傳欄位說明
- [ ] 新增「補上推薦碼」章節（POST /api/user/apply-referral）

**估時**：1 小時  
**狀態**：⏳ 未開始

---

### T024 — 更新前端開發提示文件

**路徑**：`frontend/client/PROMPT-FOR-FRONTEND.md`  
**新增**：
- [ ] 官網註冊表單中推薦碼欄位的實作建議
- [ ] Google 登入後新用戶導覽彈窗邏輯
- [ ] 補碼流程 UX 建議

**估時**：1 小時  
**狀態**：⏳ 未開始

---

## 第 7 階段：整合驗收

### T025 — 完整流程手動測試

**測試場景**：

1. **場景 A：官網註冊 + 推薦碼**
   - [ ] 進註冊頁面
   - [ ] 填入 Email / 密碼 / 昵稱 / 推薦碼（假設碼有效）
   - [ ] 點提交 → 預期：首次登入成功，user.referralCode 已設定
   - [ ] 登入後檢查 user.referralCode 是否不可變

2. **場景 B：官網註冊無推薦碼**
   - [ ] 進註冊頁面，空留推薦碼
   - [ ] 提交 → 預期：正常完成，user.referralCode = null

3. **場景 C：Google 新用戶補碼**
   - [ ] 用新 Google 帳號登入（首次）
   - [ ] 預期：收到 isNewUser=true 訊號，進新用戶導覽
   - [ ] 點「補推薦碼」
   - [ ] 輸入有效推薦碼 → 預期：绑定成功
   - [ ] 登出重新登入 → 預期：isNewUser 不再返回

4. **場景 D：Google 既存用戶**
   - [ ] 已有帳號的用戶用 Google 登入（關聯 Email）
   - [ ] 預期：isNewUser=false，直接登入無導覽

**估時**：2 小時  
**狀態**：⏳ 未開始

---

### T026 — 後端最終構建驗証

**命令**：
```bash
mvn clean package -DskipTests
```

**驗收標準**：
- [ ] 編譯通過無警告
- [ ] JAR 包生成

**估時**：30 分鐘  
**狀態**：⏳ 未開始

---

### T027 — 提交代碼審查

**步驟**：
- [ ] Git 分支名稱：`feature/023-referral-signup-integration`
- [ ] Commit message 清晰
- [ ] PR 描述涵蓋變更摘要、測試覆蓋、已知限制

**估時**：30 分鐘  
**狀態**：⏳ 未開始

---

## 優先級與依賴關係

```
T001 (DB)
   ↓
T002 (MBG)
   ↓
T003-T004 (DTO)
   ↓
T005-T011 (API 層核心)
   ↓
T012-T014 (安全)
   ↓
T019-T021 (測試)
   ↓
T015-T024 (文件)
   ↓
T025-T027 (驗收)
```

**平行可進行**：T003-T004, T005-T011, T015-T018

---

## 總計工期

- **資料庫**：2-4 小時
- **後端實作**：8-10 小時
- **安全性**：2-3 小時
- **測試**：5-6 小時
- **文件**：3-4 小時
- **驗收**：2-3 小時

**總計**：約 22-30 小時（3-4 個工作日）

