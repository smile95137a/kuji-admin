# Contract: POST /admin/banners

**模組**: Banner 管理  
**Auth**: JWT Bearer — 需要 `ROLE_ADMIN`  
**Context Path**: `/api`（完整 URL：`POST /api/admin/banners`）

---

## 描述

建立一則與特定店家連結的新廣告。圖片必須在呼叫此端點前，先透過 `POST /api/admin/upload` 完成上傳。新廣告預設為 `DRAFT` 狀態，除非明確傳入 `status: "PUBLISHED"`。

---

## 請求

### 標頭

| 標頭 | 值 | 必填 |
|--------|-------|----------|
| `Authorization` | `Bearer <jwt_token>` | ✅ |
| `Content-Type` | `application/json` | ✅ |

### 請求主體

```json
{
  "storeId":   "string (UUID, required)",
  "title":     "string (optional, max 200 chars)",
  "imageUrl":  "string (required — S3/CDN URL from upload endpoint)",
  "orderNum":  0,
  "status":    "DRAFT | PUBLISHED | UNPUBLISHED  (optional, default: DRAFT)",
  "startTime": "2026-04-01T00:00:00  (optional, ISO-8601 datetime)",
  "endTime":   "2026-06-30T23:59:59  (optional, ISO-8601 datetime)"
}
```

### 欄位規則

| 欄位 | 必填 | 限制條件 |
|-------|----------|-------------|
| `storeId` | ✅ | 必須參照現有且未刪除的店家 |
| `imageUrl` | ✅ | 非空；必須為有效 URL 字串 |
| `title` | ❌ | 最多 200 字元 |
| `orderNum` | ❌ | 整數 ≥ 0；預設為 `0` |
| `status` | ❌ | 必須為 `DRAFT`、`PUBLISHED`、`UNPUBLISHED` 之一；預設為 `DRAFT` |
| `startTime` | ❌ | 若提供，必須早於 `endTime` |
| `endTime` | ❌ | 若提供，必須晚於 `startTime` |

### 請求範例

```json
POST /api/admin/banners
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

{
  "storeId":   "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "title":     "春季特賣活動",
  "imageUrl":  "https://test-ourkuji.s3.ap-northeast-1.amazonaws.com/banner/2026/spring-sale.jpg",
  "orderNum":  1,
  "status":    "PUBLISHED",
  "startTime": "2026-04-01T00:00:00",
  "endTime":   "2026-04-30T23:59:59"
}
```

---

## 回應

### 201 Created — 成功

```json
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
}
```

### 400 Bad Request — 驗證錯誤

```json
{
  "code":    400,
  "message": "廣告圖片為必填",
  "errors": [
    { "field": "imageUrl", "message": "廣告圖片為必填" }
  ]
}
```

可能的驗證訊息：
- `店家不存在或已刪除` — storeId 參照已刪除或不存在的店家
- `廣告圖片為必填` — imageUrl 為空
- `發佈時間必須早於下架時間` — startTime ≥ endTime
- `顯示順序不得為負數` — orderNum < 0
- `狀態值無效` — status 不在允許的值集合中

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

- `id` 由伺服器生成 UUID（不接受客戶端提供）。
- `linkUrl` 由伺服器端計算為 `/stores/{storeId}`——不接受客戶端輸入（FR-004）。
- `createdAt` 和 `updatedAt` 設定為目前伺服器時間（Asia/Taipei 時區）。
- 若 `status = PUBLISHED` 且未設定 `startTime`，廣告立即在輪播中顯示。
- 若 `storeId` 對應的店家存在但 `status = INACTIVE`，廣告仍會被接受（直到店家重新啟用前，不會出現在公開輪播中）。

---

## Controller 對應

```java
// AdminBannerController.java
@RestController
@RequestMapping("/admin/banners")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBannerController {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<BannerRes> createBanner(@Valid @RequestBody BannerCreateReq req) { ... }
}
```
