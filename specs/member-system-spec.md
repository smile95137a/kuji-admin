# 會員系統完善規格書 (Member System Spec)

**版本**: v1.0  
**日期**: 2026-04-17  
**狀態**: 待實作

---

## 一、背景與目標

### 現況問題
1. 前台用戶無 logout endpoint，token 只能靠過期失效
2. Email 驗證欄位存在但從未強制執行
3. 前後台均無登入失敗次數追蹤，存在暴力破解風險
4. 錢包欄位在 `user` 表與 `user_wallet` 表同時存在（資料重複）
5. 無登入裝置/歷史記錄
6. 後台無完整操作審計日誌
7. Admin 無法手動調整用戶點數

### 目標
完善前後台會員制度，補齊安全機制、資料一致性、管理功能三大面向。

---

## 二、資料庫變更

### 2.1 廢棄 `user_wallet` 表
- **決策**: `user` 表為唯一錢包資料來源
- **動作**: 將所有讀寫錢包的程式碼統一使用 `user.gold_coins` / `user.bonus_coins`
- **遷移**: 執行 `UPDATE user u JOIN user_wallet uw ON u.id = uw.user_id SET u.gold_coins = uw.gold_coins, u.bonus_coins = uw.bonus_coins, u.total_recharged = uw.total_recharged WHERE uw 資料更新`;
- **最終**: DROP TABLE `user_wallet`

### 2.2 地址欄位設計
- `user` 表保留地址欄位，作為「默認地址快取」（快速取得，無需 JOIN）
- `user_address` 表儲存完整地址簿（多筆地址）
- 當 `user_address` 更新默認地址時，同步更新 `user` 表對應欄位
- **不刪除任何欄位**，兩者同時維護

### 2.3 新增 `user_login_history` 表
```sql
CREATE TABLE `user_login_history` (
  `id`           VARCHAR(36) PRIMARY KEY,
  `user_id`      VARCHAR(36) NOT NULL,
  `user_type`    VARCHAR(20) NOT NULL,   -- 'user' or 'admin'
  `login_time`   DATETIME NOT NULL,
  `ip_address`   VARCHAR(45),
  `device_info`  VARCHAR(500),           -- User-Agent
  `login_method` VARCHAR(20),            -- EMAIL / GOOGLE / FACEBOOK
  `status`       VARCHAR(20) NOT NULL,   -- SUCCESS / FAILED / LOCKED
  `fail_reason`  VARCHAR(200),           -- 失敗原因
  `created_at`   DATETIME NOT NULL
) ENGINE=InnoDB;
```

### 2.4 `user` 表新增欄位
```sql
ALTER TABLE `user`
  ADD COLUMN `failed_login_attempts` INT DEFAULT 0,
  ADD COLUMN `locked_until`          DATETIME DEFAULT NULL;
```

### 2.5 `admin_user` 表新增欄位
```sql
ALTER TABLE `admin_user`
  ADD COLUMN `failed_login_attempts` INT DEFAULT 0,
  ADD COLUMN `locked_until`          DATETIME DEFAULT NULL;
```

### 2.6 新增 `admin_audit_log` 表（後台操作審計）
```sql
CREATE TABLE `admin_audit_log` (
  `id`            VARCHAR(36) PRIMARY KEY,
  `operator_id`   VARCHAR(36) NOT NULL,   -- 操作者 admin_user.id
  `operator_name` VARCHAR(100),
  `action`        VARCHAR(100) NOT NULL,  -- 動作代碼，例如 USER_COIN_ADJUST
  `target_type`   VARCHAR(50),            -- 操作對象類型，例如 user / store / lottery
  `target_id`     VARCHAR(36),            -- 操作對象 ID
  `before_value`  TEXT,                   -- 修改前的值 (JSON)
  `after_value`   TEXT,                   -- 修改後的值 (JSON)
  `remark`        VARCHAR(500),           -- 備註
  `ip_address`    VARCHAR(45),
  `created_at`    DATETIME NOT NULL
) ENGINE=InnoDB;
```

### 2.7 `user_token_blacklist` 表（前台 token 撤銷）
```sql
CREATE TABLE `user_token_blacklist` (
  `user_id`        VARCHAR(36) PRIMARY KEY,
  `blacklist_gen`  INT DEFAULT 0,
  `updated_at`     DATETIME
) ENGINE=InnoDB;
```
> 設計與後台 `admin_token_blacklist` 一致，採用 generation counter 策略。

---

## 三、功能規格

### 3.1 Email 驗證（前台）

#### 規則
- **Email 登入**：未驗證 Email 的用戶，登入時返回 `403 EMAIL_NOT_VERIFIED`
- **Google OAuth 登入**：自動視為已驗證（`email_verified = 1`），不需再驗證
- **驗證 token**：16 位元隨機字符串，有效期 24 小時

#### API

| Method | Path | 說明 |
|--------|------|------|
| POST | `/api/auth/resend-verification` | 重新發送驗證信 |
| GET  | `/api/auth/verify-email?token={token}` | 驗證 Email（點擊連結） |

#### 流程
```
1. 用戶 POST /register
   → 建立帳號，email_verified = 0
   → 生成 email_verification_token（UUID + 有效期 24h）
   → 發送驗證信（含 link）
   → 返回 201，提示「請驗證 Email 後登入」

2. 用戶點擊驗證連結 GET /api/auth/verify-email?token=xxx
   → 查找 token，檢查有效期
   → 設置 email_verified = 1，清除 token
   → 返回 200

3. 用戶 POST /login（Email 方式）
   → 若 email_verified = 0，返回 403 { code: "EMAIL_NOT_VERIFIED" }
   → 前端提示「請先驗證Email，或重新發送驗證信」
```

---

### 3.2 登入失敗保護 / 帳號鎖定

#### 規則（前台 & 後台均適用）
- 連續登入失敗 5 次 → 帳號鎖定 15 分鐘
- 鎖定期間嘗試登入返回 `423 ACCOUNT_LOCKED`，並告知解鎖剩餘時間
- 登入成功後 `failed_login_attempts` 重置為 0
- 鎖定時間到自動解鎖（不需手動操作）
- Admin 可手動解鎖前台用戶帳號

#### 後台管理介面
- 用戶列表顯示「鎖定狀態」
- 單筆操作：「解除鎖定」按鈕
- 解鎖操作記錄至 `admin_audit_log`

#### Error Codes
```json
{ "code": "ACCOUNT_LOCKED", "message": "帳號已鎖定，請於 12 分鐘後再試" }
{ "code": "EMAIL_NOT_VERIFIED", "message": "請先驗證 Email" }
{ "code": "INVALID_CREDENTIALS", "message": "帳號或密碼錯誤，還剩 3 次機會" }
```

---

### 3.3 前台用戶 Logout

#### API

| Method | Path | 說明 | Auth |
|--------|------|------|------|
| POST | `/api/auth/logout` | 登出，撤銷 token | ✅ Bearer |

#### 流程
```
POST /api/auth/logout
Authorization: Bearer {accessToken}

1. 驗證 accessToken
2. 從 token 取得 userId
3. 遞增 user_token_blacklist.blacklist_gen
4. 返回 200 { message: "已成功登出" }
5. 客戶端刪除本地 token
```

#### ApiJwtAuthenticationFilter 修改
- 驗證 token 時，額外檢查 `user_token_blacklist.blacklist_gen`
- token 中的 `gen` 與資料庫 `blacklist_gen` 不符 → 返回 401

---

### 3.4 登入記錄 / 裝置追蹤

#### 記錄時機
- 登入**成功**：記錄一筆 status=SUCCESS
- 登入**失敗**（密碼錯誤）：記錄一筆 status=FAILED
- 登入**被鎖定**：記錄一筆 status=LOCKED

#### 收集欄位
- `ip_address`：從 `X-Forwarded-For` 或 `RemoteAddr` 取得
- `device_info`：User-Agent header
- `login_method`：EMAIL / GOOGLE / FACEBOOK（從 provider 判斷）

#### 後台 API（Admin 查看用戶登入記錄）

| Method | Path | 說明 |
|--------|------|------|
| GET | `/admin/users/{userId}/login-history` | 查詢用戶登入記錄（分頁） |

**Response 範例**：
```json
{
  "data": [
    {
      "loginTime": "2026-04-17T10:30:00",
      "ipAddress": "118.163.xxx.xxx",
      "deviceInfo": "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0...)",
      "loginMethod": "GOOGLE",
      "status": "SUCCESS"
    }
  ]
}
```

---

### 3.5 後台操作審計日誌

#### 記錄的操作類型

| action | 說明 |
|--------|------|
| `USER_COIN_ADJUST` | 手動調整用戶點數 |
| `USER_STATUS_CHANGE` | 修改用戶狀態 |
| `USER_UNLOCK` | 解除帳號鎖定 |
| `ADMIN_CREATE` | 建立後台帳號 |
| `ADMIN_STATUS_CHANGE` | 修改後台帳號狀態 |
| `ADMIN_ROLE_CHANGE` | 修改角色 |
| `STORE_STATUS_CHANGE` | 修改店家狀態 |
| `LOTTERY_STATUS_CHANGE` | 修改商品上/下架 |
| `PASSWORD_RESET` | 管理員重置密碼 |

#### 後台 API

| Method | Path | 說明 |
|--------|------|------|
| GET | `/admin/audit-logs` | 查詢操作日誌（可過濾操作者、時間、類型） |
| GET | `/admin/audit-logs/user/{userId}` | 查詢特定用戶的操作歷史 |

**查詢參數**：`operatorId`, `targetType`, `targetId`, `action`, `startDate`, `endDate`, `page`, `size`

---

### 3.6 後台手動調整用戶點數

#### 規則
- **可調整類型**：金幣（GOLD）、紅利金（BONUS）
- **調整方式**：增加（+）或扣除（-），輸入絕對數字
- **最低餘額**：調整後不能低於 0
- **必填備註**：調整時必須填寫原因（客服補償、系統誤差、退款等）
- **自動記錄**：調整結果寫入 `wallet_transaction` 表
- **審計記錄**：寫入 `admin_audit_log`

#### API

| Method | Path | 說明 | Auth |
|--------|------|------|------|
| POST | `/admin/users/{userId}/coin-adjust` | 手動調整點數 | ROLE_ADMIN |

**Request**:
```json
{
  "coinType": "GOLD",        // GOLD | BONUS
  "adjustType": "ADD",       // ADD | DEDUCT
  "amount": 500,
  "remark": "客服補償 - 訂單 #ORD-20260417"
}
```

**Response**:
```json
{
  "userId": "uuid",
  "coinType": "GOLD",
  "adjustType": "ADD",
  "amount": 500,
  "balanceBefore": 1200,
  "balanceAfter": 1700,
  "remark": "客服補償 - 訂單 #ORD-20260417",
  "operatorId": "admin-uuid",
  "createdAt": "2026-04-17T10:30:00"
}
```

---

## 四、修改範圍總覽

### 後端變更

#### 新增 DDL
- `user` 表：`+failed_login_attempts`, `+locked_until`
- `admin_user` 表：`+failed_login_attempts`, `+locked_until`
- 新建 `user_login_history` 表
- 新建 `admin_audit_log` 表
- 新建 `user_token_blacklist` 表

#### 廢棄/刪除
- `user_wallet` 表 → 遷移後 DROP
- `UserWallet` entity / mapper / service（已刪除，確認清除）

#### 新增 Entity / Mapper（MBG）
- `UserLoginHistory`
- `AdminAuditLog`
- `UserTokenBlacklist`

#### 修改 Service
- `UserServiceImpl`
  - `login()` → 加入失敗次數檢查、鎖定判斷、成功後重置、記錄 login_history
  - `register()` → Google OAuth 設 `emailVerified=1`，Email 設 `emailVerified=0` + 發送驗證信
  - `verifyEmail()` → 新增方法
  - `resendVerificationEmail()` → 新增方法
  - `logout()` → 新增，遞增 `user_token_blacklist.blacklist_gen`
  - 移除所有 `UserWallet` 相關引用

- `AdminAuthServiceImpl`
  - `login()` → 加入失敗次數檢查、鎖定判斷、記錄 login_history

- `AdminUserServiceImpl`
  - `adjustUserCoin()` → 新增方法（手動調整 + 記錄審計）
  - `unlockUser()` → 新增方法

#### 新增 Service
- `AuditLogService` → 記錄操作日誌的通用 service
- `LoginHistoryService` → 記錄登入歷史
- `UserTokenBlacklistService` → 前台 token blacklist（與後台 AdminTokenBlacklistService 對稱）

#### 修改 Controller
- `ApiAuthController`
  - 新增 `POST /api/auth/logout`
  - 新增 `POST /api/auth/resend-verification`
  - 新增 `GET /api/auth/verify-email`

- `AdminUserController`
  - 新增 `GET /admin/users/{userId}/login-history`
  - 新增 `POST /admin/users/{userId}/coin-adjust`
  - 新增 `POST /admin/users/{userId}/unlock`

- 新增 `AdminAuditLogController`
  - `GET /admin/audit-logs`
  - `GET /admin/audit-logs/user/{userId}`

#### 修改 Filter
- `ApiJwtAuthenticationFilter`
  - 驗證時加入 `user_token_blacklist` generation check

---

## 五、不在此次範圍

以下功能**暫不實作**：
- 2FA / MFA（TOTP）
- 密碼過期政策
- API Key 管理
- 用戶偏好設定（通知、語言）
- 批量用戶管理（CSV 匯出）
- Admin 模擬登入（impersonation）

---

## 六、測試檢查清單

- [ ] Email 登入未驗證 → 403 EMAIL_NOT_VERIFIED
- [ ] Google OAuth 登入 → 不需驗證，直接成功
- [ ] 驗證連結點擊 → 設置 email_verified=1
- [ ] 驗證 token 過期 → 返回適當錯誤
- [ ] 連續失敗 4 次 → 第 5 次提示「還剩 1 次」
- [ ] 連續失敗 5 次 → 鎖定，返回 ACCOUNT_LOCKED
- [ ] 15 分鐘後自動解鎖 → 可正常登入
- [ ] Admin 手動解鎖 → 即時生效，記錄 audit_log
- [ ] 前台 logout → token gen 遞增，舊 token 立即失效
- [ ] 後台 logout → 同上（現有機制驗證）
- [ ] 金幣調整 +500 → wallet_transaction + audit_log 各一筆
- [ ] 金幣調整 -999999 → 返回餘額不足錯誤
- [ ] login_history 正確記錄成功/失敗/鎖定三種狀態

---

## 七、依賴關係

```
DDL 變更
  └─→ MBG 生成 Entity/Mapper
        └─→ Service 實作
              ├─→ AuditLogService（被所有有審計需求的 service 使用）
              ├─→ LoginHistoryService（被 login 方法使用）
              └─→ UserTokenBlacklistService（被 logout + Filter 使用）
                    └─→ Controller 實作
                          └─→ Filter 修改（logout gen check）
```
