# 合約：POST /admin/referral-codes

**為店家建立推薦碼**

## 基本資訊

| 欄位 | 值 |
|-------|-------|
| Method | POST |
| Path | `/admin/referral-codes` |
| Auth | Bearer JWT (ROLE_ADMIN or ROLE_STORE_OWNER) |
| Content-Type | application/json |
| Controller | `AdminReferralCodeController.create()` |
| Service | `ReferralCodeService.create()` |
| Status | EXISTS — verify code auto-generation is 8-char uppercase alphanumeric |

## 請求

### 標頭
```
Authorization: Bearer <admin_jwt_token>
Content-Type: application/json
```

### 請求體
```json
{
  "storeId": "550e8400-e29b-41d4-a716-446655440000",
  "description": "2026 Spring Campaign",
  "maxUsage": 100,
  "validFrom": "2026-03-01T00:00:00",
  "validUntil": "2026-06-30T23:59:59"
}
```

| 欄位 | 型別 | 必填 | 驗證規則 |
|-------|------|----------|------------|
| storeId | String (UUID) | 是 | 必須關聯至存在且活躍的店家 |
| description | String | 否 | 最多 255 字元 |
| maxUsage | Integer | 否 | 若設定：>= 1；null 表示無限制 |
| validFrom | LocalDateTime | 否 | ISO-8601 日期時間 |
| validUntil | LocalDateTime | 否 | ISO-8601；若兩者皆設定，必須晚於 validFrom |

**備註**：請求中**不接受** `code` 欄位。代碼由伺服器端自動生成為 8 字元大寫英數字字串。

## 回應

### 201 Created
```json
{
  "code": 201,
  "message": "推薦碼建立成功",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "code": "ABC12345",
    "storeId": "550e8400-e29b-41d4-a716-446655440000",
    "storeName": "Dream Store",
    "description": "2026 Spring Campaign",
    "isActive": true,
    "usedCount": 0,
    "maxUsage": 100,
    "validFrom": "2026-03-01T00:00:00",
    "validUntil": "2026-06-30T23:59:59",
    "createdAt": "2026-03-22T10:00:00"
  }
}
```

### 400 Bad Request — 店家不存在或已停用
```json
{
  "code": 400,
  "message": "店家不存在或已停用",
  "data": null
}
```

### 403 Forbidden — 權限不足
```json
{
  "code": 403,
  "message": "權限不足",
  "data": null
}
```

## 業務規則

1. `code` 以 `UUID.randomUUID().toString().replace("-","").substring(0,8).toUpperCase()` 生成
2. 碰撞重試：若生成的代碼已存在，則重新生成（最多 5 次重試）
3. `isActive` 預設為 `true`
4. `usedCount` 初始值為 `0`
5. `ownerId` 設為已認證的管理員使用者 ID；`ownerType` = "ADMIN"
6. ROLE_STORE_OWNER 角色的使用者僅能為其管理的店家建立代碼
