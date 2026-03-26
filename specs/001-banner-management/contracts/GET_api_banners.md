# Contract: GET /api/banners (Public Carousel)

**模組**: Banner 管理  
**Auth**: 無（公開端點）  
**Context Path**: `/api`（完整 URL：`GET /api/banners`）

---

## 描述

回傳目前有效的廣告有序列表，供首頁輪播顯示使用。這是前端使用的公開端點，無需認證。

有效廣告須同時滿足以下所有條件：
1. `status = 'PUBLISHED'`
2. `startTime IS NULL OR startTime <= NOW()`
3. `endTime IS NULL OR endTime >= NOW()`
4. 連結店家的 `status = 'ACTIVE'`（FR-008）

結果依 `orderNum ASC` 排序，相同順序時以 `createdAt ASC` 作為次要排序（FR-007 + 邊緣情況規格）。

---

## 請求

### 標頭

無需任何標頭。

### 查詢參數

無。

### 請求範例

```http
GET /api/banners
```

---

## 回應

### 200 OK — 有效廣告存在

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
    "orderNum":     1,
    "status":       "PUBLISHED",
    "startTime":    null,
    "endTime":      null,
    "createdAt":    "2026-03-20T09:00:00",
    "updatedAt":    "2026-03-21T14:00:00"
  }
]
```

> 上方兩則廣告的 `orderNum` 均為 1。以 `createdAt ASC` 作為次要排序，因此較早建立的廣告（2026-03-20）排在前面。

### 200 OK — 無有效廣告

```json
[]
```

回傳空陣列——永遠不會是 404。當陣列為空時，前端輪播元件負責優雅地隱藏輪播區塊（依規格中的邊緣情況處理）。

---

## 行為說明

- **無需認證**——這是公開的 GET 端點。
- 查詢使用單一 SQL JOIN（參見 `BannerMapper.selectActiveBanners()`）；無 N+1 查詢。
- `linkUrl` 由伺服器在 SQL 中計算為 `CONCAT('/stores/', b.store_id)`——不儲存於資料庫，且無法被覆蓋。
- 連結 `INACTIVE` 店家的廣告透過 `INNER JOIN` 上 `store.status = 'ACTIVE'` 被**排除**（FR-008）。
- DRAFT 和 UNPUBLISHED 廣告**永遠不會**被回傳，無論店家狀態為何。
- 回應中包含 `status` 欄位以供完整性參考；客戶端可忽略（值永遠是 `PUBLISHED`）。
- 回應不分頁——回傳完整有效集合。預計數量：< 50 筆。
- **快取**：v1.0 無伺服器端快取。HTTP 標頭的瀏覽器/CDN 快取可在未來版本加入。

---

## 錯誤情況

此端點無認證錯誤。唯一可能的伺服器錯誤為：

### 500 Internal Server Error

```json
{ "code": 500, "message": "系統錯誤，請稍後再試" }
```

伺服器端記錄日誌；永遠不會將 stack trace 回傳給客戶端。

---

## 前端整合說明

```javascript
// Typical usage
const response = await fetch('/api/banners');
const banners = await response.json();   // always an array, possibly empty

// Navigate on click
const handleClick = (banner) => {
  router.push(banner.linkUrl);           // e.g. /stores/a1b2c3d4-...
};

// Handle broken image gracefully (edge case from spec)
const handleImageError = (e) => {
  e.target.style.display = 'none';      // skip broken image in carousel
};
```

---

## Controller Mapping

```java
// BannerController.java  (api package — no @PreAuthorize)
@RestController
@RequestMapping("/banners")
public class BannerController {

    @GetMapping
    public ResponseEntity<List<BannerRes>> getActiveBanners() { ... }
}
```
