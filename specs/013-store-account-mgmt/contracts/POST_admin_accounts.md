# 合約：POST /admin/accounts

**功能**：`013-store-account-mgmt`  
**方法**：`POST`  
**路徑**：`/admin/accounts`  
**驗證**：必要 — Bearer JWT（角色：僅 ADMIN）  
**說明**：建立新的 StoreOwner 或 StoreEditor 帳號。產生隨機初始密碼，以電子郵件寄送，並將帳號綁定至指定店家。

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
  "email": "owner@store.com",
  "displayName": "張小明",
  "phone": "0912345678",
  "roleType": "STORE_OWNER",
  "storeId": "550e8400-e29b-41d4-a716-446655440000",
  "remark": "Optional notes"
}
```

### 欄位定義

| 欄位 | 類型 | 必要 | 驗證 | 備註 |
|-------|------|----------|------------|-------|
| `email` | String | 是 | 有效 email 格式；在 `admin_user` 中唯一 | 作為登入 username |
| `displayName` | String | 是 | 1–100 字元 | 顯示於管理後台 |
| `phone` | String | 否 | 選填 | 聯絡電話 |
| `roleType` | String | 是 | 列舉：`STORE_OWNER` \| `STORE_EDITOR` | 決定店家綁定類型 |
| `storeId` | String | 是 | 有效 UUID；店家必須存在 | 綁定的目標店家 |
| `remark` | String | 否 | 最多 500 字元 | 內部備註 |

---

## 回應

### 成功 — `201 Created`

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "uuid-of-new-account",
    "email": "owner@store.com",
    "displayName": "張小明",
    "phone": "0912345678",
    "status": "PENDING",
    "forceChangePassword": true,
    "roleType": "STORE_OWNER",
    "storeId": "550e8400-e29b-41d4-a716-446655440000",
    "storeName": "夢想扭蛋店",
    "createdBy": "admin-user-uuid",
    "createdAt": "2026-03-22T10:00:00"
  }
}
```

### 錯誤回應

| HTTP 狀態碼 | `code` | `message` | 觸發條件 |
|-------------|--------|-----------|---------|
| `409 Conflict` | 409 | `"Email already registered"` | `email` 已存在於 `admin_user` |
| `404 Not Found` | 404 | `"Store not found"` | `storeId` 不存在 |
| `409 Conflict` | 409 | `"Store already has an owner"` | `roleType=STORE_OWNER` 但店家已有擁有者 |
| `400 Bad Request` | 400 | `"Validation failed: ..."` | 缺少必填欄位或格式無效 |
| `403 Forbidden` | 403 | `"Access denied"` | 呼叫者非 ADMIN 角色 |
| `500 Internal Server Error` | 500 | `"Account creation failed"` | 交易回滾（DB 或 Redis 錯誤） |

---

## 副作用

1. **插入 `admin_user` 記錄**，`status=PENDING`，`force_change_password=true`
2. **插入 `store_user` 記錄**，設定對應的 `role_type`
3. **更新 `store.owner_id`**（僅當 `roleType=STORE_OWNER` 時）
4. **非同步寄送電子郵件**至 `email`，包含初始密碼（明文，一次性使用）
5. **稽核**：`created_by` = 呼叫管理員的 `adminUserId`；`created_at` = now()
6. **整個操作為原子性** — 任何步驟失敗則所有 DB 變更回滾

---

## 業務規則

- `email` 在 `admin_user` 中必須全域唯一（與前台 `user` 表分離）
- 一家店最多只能有**一個** `STORE_OWNER`；允許多個 `STORE_EDITOR` 綁定
- 初始密碼為 8–12 字元，由 `SecureRandom` 產生，保證含大寫 + 小寫 + 數字
- 初始密碼**僅透過電子郵件寄送**，絕不在 API 回應中回傳
- `AdminUser` 建立與 `StoreUser` 綁定在單一交易中完成（FR-012）

---

## 實作備註

```
Controller:  AdminAccountController.createAccount()
Service:     AdminAccountServiceImpl.createAccount(CreateAdminAccountReq, adminUserId)
Mapper:      AdminUserMapper.insertSelective(AdminUser)
             StoreUserMapper.insertSelective(StoreUser)
             StoreMapper.updateByPrimaryKeySelective(Store)  ← ownerId update
Email:       EmailService.sendInitialPasswordEmail(email, displayName, initialPassword)
Transaction: @Transactional on service method
```
