# 合約：PUT /admin/accounts/{id}/role

**功能**：`013-store-account-mgmt`  
**方法**：`PUT`  
**路徑**：`/admin/accounts/{id}/role`  
**驗證**：必要 — Bearer JWT（角色：僅 ADMIN）  
**說明**：變更店家帳號的角色和／或店家綁定。移除現有 `StoreUser` 綁定並建立新的。若從 `STORE_OWNER` 變更為 `STORE_EDITOR`，同時清除 `Store.ownerId`。

---

## 請求

### 路徑參數

| 參數 | 類型 | 必要 | 備註 |
|-----------|------|----------|-------|
| `id` | String (UUID) | 是 | 要修改的帳號 `AdminUser.id` |

### 標頭

| 標頭 | 值 | 必要 |
|--------|-------|----------|
| `Authorization` | `Bearer <accessToken>` | 是 |
| `Content-Type` | `application/json` | 是 |

### 請求體

```json
{
  "roleType": "STORE_EDITOR",
  "storeId": "550e8400-e29b-41d4-a716-446655440000"
}
```

### 欄位定義

| 欄位 | 類型 | 必要 | 驗證 | 備註 |
|-------|------|----------|------------|-------|
| `roleType` | String | 是 | 列舉：`STORE_OWNER` \| `STORE_EDITOR` | 帳號的新角色 |
| `storeId` | String | 是 | 有效 UUID；店家必須存在 | 新的店家綁定（可為相同或不同店家） |

---

## 回應

### 成功 — `200 OK`

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "uuid-of-account",
    "email": "editor@store.com",
    "displayName": "王大明",
    "roleType": "STORE_EDITOR",
    "storeId": "550e8400-e29b-41d4-a716-446655440000",
    "storeName": "夢想扭蛋店",
    "updatedBy": "admin-user-uuid",
    "updatedAt": "2026-03-22T10:10:00"
  }
}
```

### 錯誤回應

| HTTP 狀態碼 | `code` | `message` | 觸發條件 |
|-------------|--------|-----------|---------|
| `404 Not Found` | 404 | `"Account not found"` | `id` 不存在 |
| `404 Not Found` | 404 | `"Store not found"` | `storeId` 不存在 |
| `409 Conflict` | 409 | `"Store already has an owner"` | 設定 `STORE_OWNER` 但目標店家已有擁有者 |
| `400 Bad Request` | 400 | `"Invalid role type"` | `roleType` 不在列舉中 |
| `403 Forbidden` | 403 | `"Access denied"` | 呼叫者非 ADMIN 角色 |

---

## 副作用

1. **刪除舊的 `store_user` 記錄**（此 `admin_user_id` 的現有綁定）
2. **插入新的 `store_user` 記錄**，更新 `role_type` 和／或 `store_id`
3. **清除 `store.owner_id`**（設為 NULL），若舊角色為 `STORE_OWNER` 且店家變更或角色從 OWNER 變更
4. **設定 `store.owner_id`** 為此使用者的 ID，若新角色為 `STORE_OWNER`
5. **更新 `admin_user.updated_by`** 和 **`admin_user.updated_at`**
6. **Token 失效**：使用者現有 Token 中的 `storeIds` 宣告在此變更後將過時。現有 Token **不會**自動加入黑名單；使用者在下次登入後店家存取權將更新。若需要立即撤銷店家存取，請先呼叫停用端點。
7. **整個操作為原子性**（`@Transactional`）

---

## 業務規則

- 一個帳號在本功能中只能有**一個有效角色綁定**（前一個綁定會被取代）
- 對已有擁有者的店家設定 `STORE_OWNER` 將被拒絕
- `STORE_EDITOR` 可綁定至多家店 — 但此端點每次只管理主要綁定（多店鋪編輯者需多次呼叫，或使用未來功能的批次端點）
- JWT `storeIds` 宣告更新僅在重新登入後生效（角色變更本身不加入黑名單）

---

## 實作備註

```
Controller:  AdminAccountController.updateAccountRole()
Service:     AdminAccountServiceImpl.updateRole(id, roleType, storeId, adminUserId)
             ├── StoreUserMapper.deleteByExample(where adminUserId = id)
             ├── StoreUserMapper.insertSelective(new StoreUser)
             └── StoreMapper.updateByPrimaryKeySelective(store)  ← ownerId changes
Transaction: @Transactional — all or nothing
```
