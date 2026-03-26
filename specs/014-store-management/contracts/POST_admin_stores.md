# 合約：POST /admin/stores

**用途**：以單一交易**原子性**地建立新店家及其負責人帳號。  
**認證**：僅限 `ROLE_ADMIN`  
**路由**：`POST /admin/stores`

---

## 請求

### Headers
```
Authorization: Bearer <admin-jwt>
Content-Type: application/json
```

### Body
```json
{
  "storeName": "甜甜圈抽獎屋",
  "shortDescription": "各式甜甜圈主題抽獎",
  "longDescription": "全台最可愛的甜甜圈抽獎平台，每週上架新品...",
  "logoUrl": "https://cdn.example.com/stores/logo-abc.png",
  "coverImageUrl": "https://cdn.example.com/stores/cover-abc.png",
  "email": "contact@donut-lottery.tw",
  "phone": "02-1234-5678",
  "address": "台北市大安區忠孝東路四段100號",
  "businessHours": "週一至週五 10:00–20:00",
  "facebookUrl": "https://facebook.com/donut-lottery",
  "instagramUrl": "https://instagram.com/donut_lottery",
  "lineId": "@donut_lottery",
  "remark": "內部備注：VIP 合作店家",
  "owner": {
    "username": "donut_owner",
    "password": "InitPass@123",
    "displayName": "甜甜圈店長",
    "email": "owner@donut-lottery.tw",
    "phone": "0912-345-678"
  }
}
```

### 欄位限制

| 欄位 | 必填 | 最大長度 | 備注 |
|-------|----------|-----------|-------|
| `storeName` | ✅ | 100 | 非唯一 |
| `shortDescription` | ❌ | 255 | |
| `longDescription` | ❌ | LONGTEXT | |
| `logoUrl` | ❌ | 500 | 若有提供須為有效 URL |
| `coverImageUrl` | ❌ | 500 | 若有提供須為有效 URL |
| `email` | ❌ | 100 | |
| `phone` | ❌ | 30 | |
| `address` | ❌ | 255 | |
| `businessHours` | ❌ | 255 | 自由格式文字 |
| `facebookUrl` | ❌ | 500 | |
| `instagramUrl` | ❌ | 500 | |
| `lineId` | ❌ | 100 | |
| `remark` | ❌ | LONGTEXT | 僅限內部使用，不對外公開 |
| `owner` | ✅ | — | 巢狀物件 |
| `owner.username` | ✅ | 50 | 唯一；英數字 + 底線 |
| `owner.password` | ✅ | — | 最少 8 字元；以 BCrypt 雜湊儲存 |
| `owner.displayName` | ✅ | 100 | |
| `owner.email` | ❌ | 100 | |
| `owner.phone` | ❌ | 30 | |

---

## 回應

### 201 Created
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "storeName": "甜甜圈抽獎屋",
  "shortDescription": "各式甜甜圈主題抽獎",
  "logoUrl": "https://cdn.example.com/stores/logo-abc.png",
  "coverImageUrl": "https://cdn.example.com/stores/cover-abc.png",
  "email": "contact@donut-lottery.tw",
  "phone": "02-1234-5678",
  "address": "台北市大安區忠孝東路四段100號",
  "businessHours": "週一至週五 10:00–20:00",
  "facebookUrl": "https://facebook.com/donut-lottery",
  "instagramUrl": "https://instagram.com/donut_lottery",
  "lineId": "@donut_lottery",
  "status": "ENABLED",
  "ownerId": "f1e2d3c4-b5a6-9870-fedc-ba9876543210",
  "ownerUsername": "donut_owner",
  "ownerDisplayName": "甜甜圈店長",
  "createdAt": "2026-03-22T10:30:00",
  "updatedAt": "2026-03-22T10:30:00",
  "createdBy": "admin-uuid-here"
}
```

### 400 Bad Request — 驗證失敗
```json
{
  "code": "VALIDATION_ERROR",
  "message": "欄位驗證失敗",
  "errors": [
    { "field": "storeName", "message": "店家名稱不可為空" },
    { "field": "owner.username", "message": "帳號長度必須在 4–50 之間" }
  ]
}
```

### 409 Conflict — 帳號名稱已被使用
```json
{
  "code": "USERNAME_CONFLICT",
  "message": "帳號名稱已存在，請更換",
  "field": "owner.username"
}
```

### 401 / 403 — 認證失敗
來自現有 `AdminJwtAuthenticationFilter` 的標準 JWT 錯誤回應。

---

## 交易行為

```
BEGIN TRANSACTION
  INSERT INTO store (...)           → if fails → ROLLBACK (no orphan)
  INSERT INTO admin_user (...)      → if fails → ROLLBACK (store row removed)
  INSERT INTO admin_user_role (...) → if fails → ROLLBACK (both rows removed)
COMMIT
```

**圖片上傳**（若有）必須在交易**開始前**完成，以縮短交易持有時間。若 DB 交易在成功上傳後失敗，S3 上的孤立物件是可接受的（可由 S3 生命週期規則清理）。

---

## 安全性

- Controller 方法上設定 `@PreAuthorize("hasRole('ADMIN')")`。
- `createdBy` 欄位從 JWT 的 `SecurityUtils.getCurrentUserId()` 自動帶入。
- 初始密碼以 `BCrypt` 雜湊儲存；永遠不在回應中返回。
- `owner.password` 欄位從所有回應 DTO 中排除。
