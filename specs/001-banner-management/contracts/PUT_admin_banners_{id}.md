# Contract: PUT /admin/banners/{id}

**模組**: Banner 管理  
**Auth**: JWT Bearer — 需要 `ROLE_ADMIN`  
**Context Path**: `/api`（完整 URL：`PUT /api/admin/banners/{id}`）

---

## 描述

更新現有廣告。所有欄位均為選用——僅更新請求中有提供（非 null）的欄位（透過 `updateSelective` 實作部分更新語義）。變更 `status` 將觸發資料模型中定義的狀態機規則。

---

## 請求

### 路徑參數

| 參數 | 類型 | 必填 | 描述 |
|-----------|------|----------|-------------|
| `id` | `string (UUID)` | ✅ | 要更新的廣告 ID |

### 標頭

| 標頭 | 值 | 必填 |
|--------|-------|----------|
| `Authorization` | `Bearer <jwt_token>` | ✅ |
| `Content-Type` | `application/json` | ✅ |

### 請求主體

```json
{
  "storeId":   "string (UUID, optional — reassign store)",
  "title":     "string (optional)",
  "imageUrl":  "string (optional — new S3/CDN URL)",
  "orderNum":  1,
  "status":    "DRAFT | PUBLISHED | UNPUBLISHED  (optional)",
  "startTime": "2026-05-01T00:00:00  (optional, null to clear)",
  "endTime":   "2026-05-31T23:59:59  (optional, null to clear)"
}
```

### 欄位規則

| 欄位 | 必填 | 限制條件 |
|-------|----------|-------------|
| `storeId` | ❌ | 若提供，必須參照現有且未刪除的店家 |
| `imageUrl` | ❌ | 若提供，必須非空 |
| `orderNum` | ❌ | 整數 ≥ 0 |
| `status` | ❌ | 必須為 `DRAFT`、`PUBLISHED`、`UNPUBLISHED` 之一 |
| `startTime` | ❌ | 若同時提供 startTime 和 endTime，startTime 必須早於 endTime |
| `endTime` | ❌ | 若同時提供，必須晚於 startTime |

### 請求範例

```json
PUT /api/admin/banners/f7e8d9c0-b1a2-3456-789a-bcdef0123456
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

{
  "orderNum": 2,
  "status":   "PUBLISHED",
  "endTime":  "2026-05-31T23:59:59"
}
```

---

## 回應

### 200 OK — 成功

回傳完整更新後的 `BannerRes` 物件：

```json
{
  "id":           "f7e8d9c0-b1a2-3456-789a-bcdef0123456",
  "storeId":      "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "storeName":    "阿里山伴手禮館",
  "storeLogoUrl": "https://test-ourkuji.s3.ap-northeast-1.amazonaws.com/logo/store-abc.png",
  "title":        "春季特賣活動",
  "imageUrl":     "https://test-ourkuji.s3.ap-northeast-1.amazonaws.com/banner/2026/spring-sale.jpg",
  "linkUrl":      "/stores/a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "orderNum":     2,
  "status":       "PUBLISHED",
  "startTime":    "2026-04-01T00:00:00",
  "endTime":      "2026-05-31T23:59:59",
  "createdAt":    "2026-03-22T10:30:00",
  "updatedAt":    "2026-03-22T11:15:00"
}
```

### 400 Bad Request — 驗證錯誤

```json
{
  "code":    400,
  "message": "發佈時間必須早於下架時間",
  "errors": [
    { "field": "endTime", "message": "發佈時間必須早於下架時間" }
  ]
}
```

### 404 Not Found

```json
{ "code": 404, "message": "廣告不存在" }
```

### 401 Unauthorized

```json
{ "code": 401, "message": "未授權，請先登入" }
```

### 403 Forbidden

```json
{ "code": 403, "message": "無操作權限" }
```

---

## 行為說明

- 使用 `updateByPrimaryKeySelective`（MyBatis）——僅將請求主體中非 null 的欄位寫入資料庫。
- `updatedAt` 無論哪些欄位被變更，都會更新為 NOW()。
- `linkUrl` 始終根據 `storeId` 重新計算——客戶端無法設定此值。
- 若 `status` 轉換為 `PUBLISHED`，廣告立即出現在公開輪播中（前提是店家為 ACTIVE 且符合排程條件）。
- 允許變更 `storeId`；`linkUrl` 將自動反映新店家。

---

## Controller 對應

```java
@PutMapping("/{id}")
public ResponseEntity<BannerRes> updateBanner(
        @PathVariable String id,
        @Valid @RequestBody BannerUpdateReq req) { ... }
```
