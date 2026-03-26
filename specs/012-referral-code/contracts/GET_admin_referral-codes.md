# 合約：GET /admin/referral-codes

**列出所有推薦碼（管理員檢視）**

## 基本資訊

| 欄位 | 值 |
|-------|-------|
| Method | GET |
| Path | `/admin/referral-codes` |
| Auth | Bearer JWT (ROLE_ADMIN) |
| Controller | `AdminReferralCodeController.getAll()` |
| Service | `ReferralCodeService.getAll()` |
| Status | EXISTS — ROLE_ADMIN gate already in place |

## 請求

### 標頭
```
Authorization: Bearer <admin_jwt_token>
```

### 查詢參數（選用篩選）

| 參數 | 型別 | 說明 |
|-----------|------|-------------|
| storeId | String | 依店家 ID 篩選 |
| isActive | Boolean | 依啟用狀態篩選（true/false） |

*備註：篩選為選用。若未提供參數，則回傳所有代碼。*

## 回應

### 200 OK
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440001",
      "code": "ABC12345",
      "storeId": "550e8400-e29b-41d4-a716-446655440000",
      "storeName": "Dream Store",
      "description": "2026 Spring Campaign",
      "isActive": true,
      "usedCount": 12,
      "maxUsage": 100,
      "validFrom": "2026-03-01T00:00:00",
      "validUntil": "2026-06-30T23:59:59",
      "createdAt": "2026-03-22T10:00:00"
    },
    {
      "id": "550e8400-e29b-41d4-a716-446655440002",
      "code": "XYZ98765",
      "storeId": "550e8400-e29b-41d4-a716-446655440003",
      "storeName": "Another Store",
      "description": null,
      "isActive": false,
      "usedCount": 5,
      "maxUsage": null,
      "validFrom": null,
      "validUntil": null,
      "createdAt": "2026-02-01T08:00:00"
    }
  ]
}
```

### 200 OK — 空清單
```json
{
  "code": 200,
  "message": "success",
  "data": []
}
```

### 403 Forbidden
```json
{
  "code": 403,
  "message": "權限不足",
  "data": null
}
```

## 附加端點（現有於同一 Controller）

### GET /admin/referral-codes/store/{storeId}
回傳指定店家的推薦碼。ROLE_ADMIN 和 ROLE_STORE_OWNER 皆可存取。

### GET /admin/referral-codes/my-store
回傳已認證的 STORE_OWNER 所屬店家的推薦碼。從 JWT 自動解析 storeId。

### GET /admin/referral-codes/{id}
依 UUID 回傳單一推薦碼。

### GET /admin/referral-codes/{id}/records
回傳指定推薦碼的 `ReferralRecord` 清單。

### GET /admin/referral-codes/store/{storeId}/records
回傳指定店家的所有推薦紀錄。

### GET /admin/referral-codes/validate/{code}
回傳布林值 `true/false`，表示代碼是否有效且活躍。（管理員端驗證工具）
