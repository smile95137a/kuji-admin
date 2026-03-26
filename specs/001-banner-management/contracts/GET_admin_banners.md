# Contract: GET /admin/banners

**模組**: Banner 管理  
**Auth**: JWT Bearer — 需要 `ROLE_ADMIN`  
**Context Path**: `/api`（完整 URL：`POST /api/admin/banners/list`）

> **方法選擇說明**：此端點使用 `POST` 搭配 JSON 請求主體（遵循專案全域的列表/查詢端點慣例——參見 `POST /admin/stores/list`、`POST /admin/lottery/list`）。路由為 `/admin/banners/list`。

---

## 描述

回傳管理後台 UI 使用的廣告分頁列表（包含所有狀態）。支援依店家、狀態及標題關鍵字搜尋進行篩選。

---

## 請求

### 標頭

| 標頭 | 值 | 必填 |
|--------|-------|----------|
| `Authorization` | `Bearer <jwt_token>` | ✅ |
| `Content-Type` | `application/json` | ✅ |

### 請求主體

遵循專案的 `QueryReq<BannerCondition>` 包裝模式：

```json
{
  "condition": {
    "storeId": "string (UUID, optional — filter by store)",
    "status":  "DRAFT | PUBLISHED | UNPUBLISHED  (optional — filter by status)",
    "keyword": "string (optional — search in title)"
  },
  "pageNum":  1,
  "pageSize": 20
}
```

所有條件欄位均為選用。空請求主體 `{}` 以預設分頁回傳所有廣告。

### 請求範例

```json
POST /api/admin/banners/list
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

{
  "condition": {
    "status": "PUBLISHED"
  },
  "pageNum":  1,
  "pageSize": 10
}
```

---

## 回應

### 200 OK — 成功

```json
[
  {
    "id":           "f7e8d9c0-b1a2-3456-789a-bcdef0123456",
    "storeId":      "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "storeName":    "阿里山伴手禮館",
    "storeLogoUrl": "https://test-ourkuji.s3.ap-northeast-1.amazonaws.com/logo/store-abc.png",
    "title":        "春季特賣活動",
    "imageUrl":     "https://test-ourkuji.s3.ap-northeast-1.amazonaws.com/banner/2026/spring-sale.jpg",
    "linkUrl":      "/stores/a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "orderNum":     1,
    "status":       "PUBLISHED",
    "startTime":    "2026-04-01T00:00:00",
    "endTime":      "2026-04-30T23:59:59",
    "createdAt":    "2026-03-22T10:30:00",
    "updatedAt":    "2026-03-22T10:30:00"
  },
  {
    "id":           "11223344-5566-7788-99aa-bbccddeeff00",
    "storeId":      "b2c3d4e5-f6a7-8901-bcde-f01234567891",
    "storeName":    "東京零食屋",
    "storeLogoUrl": null,
    "title":        "夏日新品上市",
    "imageUrl":     "https://test-ourkuji.s3.ap-northeast-1.amazonaws.com/banner/2026/summer-new.jpg",
    "linkUrl":      "/stores/b2c3d4e5-f6a7-8901-bcde-f01234567891",
    "orderNum":     2,
    "status":       "PUBLISHED",
    "startTime":    null,
    "endTime":      null,
    "createdAt":    "2026-03-20T09:00:00",
    "updatedAt":    "2026-03-21T14:00:00"
  }
]
```

若無符合篩選條件的廣告，回傳空陣列 `[]`。

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

- 回傳**所有狀態**（DRAFT、PUBLISHED、UNPUBLISHED）——此為管理後台視圖。
- 預設排序：`order_num ASC`，次要排序為 `created_at DESC`。
- 包含**停用店家**的廣告——管理員需要查看所有廣告，不論店家狀態。
- `storeName` 和 `storeLogoUrl` 在查詢時從 `store` 資料表 JOIN 取得。
- 分頁使用專案已配置的 PageHelper 外掛。

---

## Controller 對應

```java
@PostMapping("/list")
public ResponseEntity<List<BannerRes>> queryBanners(
        @RequestBody(required = false) QueryReq<BannerCondition> req) { ... }
```
