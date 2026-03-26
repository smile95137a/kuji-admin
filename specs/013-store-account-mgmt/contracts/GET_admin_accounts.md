# 合約：GET /admin/accounts

**功能**：`013-store-account-mgmt`  
**方法**：`GET`  
**路徑**：`/admin/accounts`  
**驗證**：必要 — Bearer JWT（角色：ADMIN）  
**說明**：列出並篩選店家帳號（StoreOwner 和 StoreEditor），支援分頁。回傳含店家綁定資訊的分頁結果。

---

## 請求

### 標頭

| 標頭 | 值 | 必要 |
|--------|-------|----------|
| `Authorization` | `Bearer <accessToken>` | 是 |

### 查詢參數

| 參數 | 類型 | 必要 | 預設值 | 備註 |
|-----------|------|----------|---------|-------|
| `page` | Integer | 否 | `0` | 從 0 開始的頁碼索引 |
| `size` | Integer | 否 | `20` | 每頁筆數；最大 100 |
| `status` | String | 否 | — | 依 `PENDING` \| `ACTIVE` \| `INACTIVE` 篩選 |
| `roleType` | String | 否 | — | 依 `STORE_OWNER` \| `STORE_EDITOR` 篩選 |
| `storeId` | String | 否 | — | 篩選綁定至特定店家的帳號 |
| `keyword` | String | 否 | — | 搜尋 `email` 或 `displayName`（LIKE %keyword%） |
| `sortBy` | String | 否 | `createdAt` | `createdAt` \| `email` \| `displayName` \| `lastLoginAt` |
| `sortDir` | String | 否 | `DESC` | `ASC` \| `DESC` |

### 請求範例

```http
GET /admin/accounts?status=ACTIVE&roleType=STORE_OWNER&page=0&size=20
Authorization: Bearer <accessToken>
```

---

## 回應

### 成功 — `200 OK`

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "id": "uuid-1",
        "email": "owner@store.com",
        "displayName": "張小明",
        "phone": "0912345678",
        "status": "ACTIVE",
        "forceChangePassword": false,
        "lastLoginAt": "2026-03-21T15:30:00",
        "roleType": "STORE_OWNER",
        "storeId": "550e8400-e29b-41d4-a716-446655440000",
        "storeName": "夢想扭蛋店",
        "createdBy": "admin-uuid",
        "createdAt": "2026-03-20T10:00:00"
      },
      {
        "id": "uuid-2",
        "email": "owner2@store.com",
        "displayName": "李大華",
        "phone": null,
        "status": "PENDING",
        "forceChangePassword": true,
        "lastLoginAt": null,
        "roleType": "STORE_OWNER",
        "storeId": "660e8400-e29b-41d4-a716-446655440001",
        "storeName": "快樂扭蛋屋",
        "createdBy": "admin-uuid",
        "createdAt": "2026-03-22T09:00:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 42,
    "totalPages": 3
  }
}
```

### 回應欄位定義

| 欄位 | 類型 | 備註 |
|-------|------|-------|
| `id` | String | `AdminUser.id`（UUID） |
| `email` | String | 登入電子郵件 |
| `displayName` | String | 顯示名稱 |
| `phone` | String \| null | 電話號碼 |
| `status` | String | `PENDING` / `ACTIVE` / `INACTIVE` |
| `forceChangePassword` | Boolean | `true` 表示首次登入密碼變更待完成 |
| `lastLoginAt` | String \| null | ISO 8601 日期時間；從未登入則為 null |
| `roleType` | String | `STORE_OWNER` 或 `STORE_EDITOR` |
| `storeId` | String \| null | 綁定的店家 ID（無綁定則為 null） |
| `storeName` | String \| null | 綁定的店家名稱（從 `store` 表 join） |
| `createdBy` | String | 建立此帳號的管理員 user ID |
| `createdAt` | String | ISO 8601 建立時間戳記 |

### 分頁欄位

| 欄位 | 類型 | 備註 |
|-------|------|-------|
| `page` | Integer | 目前頁碼（從 0 開始） |
| `size` | Integer | 每頁筆數 |
| `totalElements` | Long | 符合條件的總記錄數 |
| `totalPages` | Integer | 總頁數 |

### 錯誤回應

| HTTP 狀態碼 | `code` | `message` | 觸發條件 |
|-------------|--------|-----------|---------|
| `400 Bad Request` | 400 | `"Invalid status filter"` | `status` 不在列舉中 |
| `400 Bad Request` | 400 | `"Invalid roleType filter"` | `roleType` 不在列舉中 |
| `403 Forbidden` | 403 | `"Access denied"` | 呼叫者非 ADMIN 角色 |

---

## 業務規則

- 僅回傳 `STORE_OWNER` 和 `STORE_EDITOR` 類型帳號（不含平台超級管理員）
- 若帳號沒有 `StoreUser` 綁定（孤立帳號），仍會回傳，`storeId=null`，`storeName=null`
- `keyword` 搜尋不區分大小寫，匹配 email 或 displayName 的部分內容
- 無論 `forceChangePassword` 旗標為何，帳號均會回傳

---

## 實作備註

```
Controller:  AdminAccountController.listAccounts()
Service:     AdminAccountServiceImpl.listAccounts(filters, pageable)

Query Strategy (MyBatis):
  Option A: Custom @Select SQL with LEFT JOIN to store_user and store tables
            filtered by admin_user.status, store_user.role_type, store.id, keyword
  Option B: AdminUserExample for base filtering + separate StoreUser lookup per user
            (N+1 risk; use Option A for production)

Recommended SQL sketch:
  SELECT au.*, su.role_type, su.store_id, s.store_name
  FROM admin_user au
  LEFT JOIN store_user su ON su.admin_user_id = au.id
  LEFT JOIN store s ON s.id = su.store_id
  WHERE (:status IS NULL OR au.status = :status)
    AND (:roleType IS NULL OR su.role_type = :roleType)
    AND (:storeId IS NULL OR su.store_id = :storeId)
    AND (:keyword IS NULL OR au.email LIKE CONCAT('%', :keyword, '%')
         OR au.display_name LIKE CONCAT('%', :keyword, '%'))
  ORDER BY au.created_at DESC
  LIMIT :size OFFSET :page * :size

Add custom method to AdminUserMapper:
  List<AdminAccountDetailDO> selectAccountsWithRole(@Param("filters") AccountFilterCondition filters)
  Long countAccountsWithRole(@Param("filters") AccountFilterCondition filters)
```
