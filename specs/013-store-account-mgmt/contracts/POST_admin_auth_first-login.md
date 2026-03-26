# 合約：POST /admin/auth/first-login

**功能**：`013-store-account-mgmt`  
**方法**：`POST`  
**路徑**：`/admin/auth/first-login/change-password`  
**驗證**：必要 — Bearer JWT（任何角色；在初始登入時發行的 Token）  
**說明**：首次登入時強制變更密碼。僅在已驗證使用者具有 `forceChangePassword=true` 時可存取。驗證舊（初始）密碼，設定新密碼，將帳號狀態從 `PENDING` 轉換為 `ACTIVE`，並回傳新 Token。

> **備註**：此端點已存在於 `AdminAuthController`。本合約說明 `013-store-account-mgmt` 功能所需的行為，以確保實作正確處理 `forceChangePassword` 和 `PENDING → ACTIVE` 狀態轉換。

---

## 請求

### 標頭

| 標頭 | 值 | 必要 |
|--------|-------|----------|
| `Authorization` | `Bearer <accessToken>` | 是 |
| `Content-Type` | `application/json` | 是 |

### 請求體

```json
{
  "oldPassword": "Abc12345",
  "newPassword": "MyNew$ecure123"
}
```

### 欄位定義

| 欄位 | 類型 | 必要 | 驗證 | 備註 |
|-------|------|----------|------------|-------|
| `oldPassword` | String | 是 | 非空 | 電子郵件中寄送的系統產生初始密碼 |
| `newPassword` | String | 是 | 最少 8 字元 | 必須與 `oldPassword` 不同 |

---

## 回應

### 成功 — `200 OK`

回傳新的 JWT Token 對（舊 Token 透過 Redis 世代計數器遞增而失效）：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "expiresIn": 86400,
    "user": {
      "id": "uuid-of-account",
      "email": "owner@store.com",
      "displayName": "張小明",
      "status": "ACTIVE",
      "forceChangePassword": false,
      "roleType": "STORE_OWNER",
      "storeIds": ["550e8400-e29b-41d4-a716-446655440000"]
    }
  }
}
```

### 錯誤回應

| HTTP 狀態碼 | `code` | `message` | 觸發條件 |
|-------------|--------|-----------|---------|
| `400 Bad Request` | 400 | `"Old password is incorrect"` | `oldPassword` 與儲存的 BCrypt 雜湊不符 |
| `400 Bad Request` | 400 | `"New password must be different from old password"` | `newPassword` == `oldPassword` |
| `400 Bad Request` | 400 | `"New password too short"` | `newPassword` < 8 字元 |
| `403 Forbidden` | 403 | `"Password change not required"` | `forceChangePassword` 已為 `false` |
| `401 Unauthorized` | 401 | `"Unauthorized"` | Token 遺失或無效 |

---

## 關鍵行為：在其他端點前的防護

`/admin/**` 下的任何端點（除 `/admin/auth/**` 外）必須檢查 `forceChangePassword`：

- 若 JWT 宣告或資料庫中 `forceChangePassword = true`：回傳 `403 Forbidden`，回應體為：
  ```json
  {
    "code": 403,
    "message": "Password change required before accessing this resource"
  }
  ```
- 此行為在 `AdminJwtAuthenticationFilter` 或專用 filter／interceptor 中強制執行。

---

## 副作用

1. **`admin_user.password`** 更新為 `newPassword` 的 BCrypt 雜湊
2. **`admin_user.force_change_password`** 設為 `false`
3. **`admin_user.status`** 從 `PENDING` 變更為 `ACTIVE`
4. **`admin_user.updated_at`** = now()
5. **Redis `INCR blacklist_gen:{adminUserId}`** — 用於呼叫此端點的舊 Token 失效；回應中回傳的新 Token 攜帶新的世代編號
6. **回傳新的存取 + 刷新 Token**，`gen = 目前 blacklist_gen`

---

## 業務規則

- 此端點可以 `PENDING` 狀態的 Token 呼叫（初始登入允許儘管狀態為 `PENDING`，但只有此端點和 `/admin/auth/logout` 可存取）
- 密碼變更成功後，帳號完全啟用，所有管理功能均可存取
- 使用者無法跳過此步驟 — 任何其他管理端點都會拒絕 `forceChangePassword=true` 的請求
- 密碼複雜度由後端驗證：最少 8 字元；前端強制要求大小寫及數字

---

## 實作備註

```
Controller:  AdminAuthController.firstLoginChangePassword()  ← already exists
Service:     AdminUserService (or AdminAuthService) — ensure:
             1. BCrypt verify oldPassword
             2. BCrypt encode newPassword
             3. Update AdminUser: password, forceChangePassword=false, status=ACTIVE
             4. TokenBlacklistService.invalidateUser(adminUserId)  ← NEW: add this call
             5. Generate and return new token pair with current gen

Filter:      AdminJwtAuthenticationFilter — add check:
             if (principal.isForceChangePassword() && !isPasswordChangePath) → 403
```
