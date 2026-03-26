# 合約：PUT /admin/accounts/{id}/status

**功能**：`013-store-account-mgmt`  
**方法**：`PUT`  
**路徑**：`/admin/accounts/{id}/status`  
**驗證**：必要 — Bearer JWT（角色：僅 ADMIN）  
**說明**：啟用或停用店家帳號。停用帳號時，透過 Redis 世代計數器立即使該使用者所有有效 Token 失效（強制安全需求 FR-007, SC-003）。

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
  "status": "INACTIVE",
  "remark": "Suspended due to policy violation"
}
```

### 欄位定義

| 欄位 | 類型 | 必要 | 驗證 | 備註 |
|-------|------|----------|------------|-------|
| `status` | String | 是 | 列舉：`ACTIVE` \| `INACTIVE` | `PENDING` 不是此端點的有效目標狀態 |
| `remark` | String | 否 | 最多 500 字元 | 狀態變更的選填原因 |

---

## 回應

### 成功 — `200 OK`

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "uuid-of-account",
    "email": "owner@store.com",
    "displayName": "張小明",
    "status": "INACTIVE",
    "updatedBy": "admin-user-uuid",
    "updatedAt": "2026-03-22T10:05:00"
  }
}
```

### 錯誤回應

| HTTP 狀態碼 | `code` | `message` | 觸發條件 |
|-------------|--------|-----------|---------|
| `404 Not Found` | 404 | `"Account not found"` | `id` 不存在於 `admin_user` |
| `400 Bad Request` | 400 | `"Invalid status value"` | `status` 不是 `ACTIVE` 或 `INACTIVE` |
| `400 Bad Request` | 400 | `"Cannot set status to PENDING"` | 嘗試透過此端點設定 `PENDING` |
| `403 Forbidden` | 403 | `"Access denied"` | 呼叫者非 ADMIN 角色 |

---

## 關鍵安全行為：Token 失效

當 `status` 變更為 `INACTIVE` 時：

1. MySQL 中的 `admin_user.status` 更新為 `INACTIVE`。
2. 呼叫 `TokenBlacklistService.invalidateUser(adminUserId)`。
3. 在 Redis 執行 `INCR blacklist_gen:{adminUserId}`。
4. 此使用者的**所有現有 Token** 在下次使用時將被拒絕（在此 API 呼叫後 < 1 秒）。
5. 使用者無法取得新 Token，因為 `AdminAuthController.login()` 檢查 `status == ACTIVE`。

當 `status` 變更為 `ACTIVE` 時：
1. MySQL 中的 `admin_user.status` 更新為 `ACTIVE`。
2. 不更改 Redis — 現有 Token 保留其（現在較低的）世代值；使用者必須重新登入。
3. 重新啟用後發行的新 Token 將攜帶當前 `blacklist_gen` 值並被接受。

---

## 副作用

1. **更新 `admin_user.status`**
2. **`admin_user.updated_by`** = 呼叫管理員的 ID；`admin_user.updated_at` = now()
3. **停用時**：Redis `INCR blacklist_gen:{id}` — 立即 Token 失效
4. **若提供 `remark`，選擇性更新 `admin_user.remark`**

---

## 業務規則

- 管理員無法透過此端點停用**自己的**帳號（回傳 `403`）
- `PENDING` → `INACTIVE` 允許（管理員可在首次登入前拒絕新帳號）
- `PENDING` → `ACTIVE` **不允許**透過此端點；只有首次登入密碼變更才觸發 `PENDING → ACTIVE`
- 停用時 Token 失效為**強制需求**（SC-003：< 1 秒）

---

## 實作備註

```
Controller:  AdminAccountController.updateAccountStatus()
Service:     AdminAccountServiceImpl.updateStatus(id, status, remark, adminUserId)
Mapper:      AdminUserMapper.updateByPrimaryKeySelective(AdminUser)
Redis:       TokenBlacklistService.invalidateUser(id)  ← only when INACTIVE
Blacklist:   StringRedisTemplate.opsForValue().increment("blacklist_gen:" + id)
Filter:      AdminJwtAuthenticationFilter checks blacklist_gen on every request
```
