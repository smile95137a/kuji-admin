# 合約：GET /api/stores/{id}

**用途**：公開店家詳情頁面。返回完整店家資訊及所有上架商品。  
**認證**：無（公開 — `permitAll()`）  
**路由**：`GET /api/stores/{id}`

---

## 請求

### 路徑參數
| 參數 | 型別 | 說明 |
|-------|------|-------------|
| `id` | UUID string | 店家 ID |

### Headers
```
(無需認證)
```

### 範例
```
GET /api/stores/a1b2c3d4-e5f6-7890-abcd-ef1234567890
```

---

## 回應

### 200 OK
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
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
  "products": [
    {
      "id": "lottery-uuid-1",
      "name": "草莓甜甜圈扭蛋",
      "coverImageUrl": "https://cdn.example.com/lottery/strawberry.png",
      "pricePerDraw": 50,
      "status": "ON_SHELF"
    },
    {
      "id": "lottery-uuid-2",
      "name": "巧克力系列限定",
      "coverImageUrl": "https://cdn.example.com/lottery/chocolate.png",
      "pricePerDraw": 80,
      "status": "ON_SHELF"
    }
  ]
}
```

> `coverImageUrl` 可能為 `null` — 前端顯示預設封面圖片。  
> `products` 僅包含 `status = 'ON_SHELF'` 的抽獎商品。若沒有上架商品則為 `[]`。

---

## 錯誤回應

### 404 Not Found — 店家不存在
```json
{
  "code": "STORE_NOT_FOUND",
  "message": "店家不存在"
}
```

### 404 Not Found — 店家已停用
```json
{
  "code": "STORE_NOT_FOUND",
  "message": "店家不存在"
}
```

> **安全備注**：已停用的店家返回與不存在的店家相同的 404。這可防止惡意列舉已停用的店家 ID。回應主體不揭露店家存在但已停用的事實（AC-2.3）。

---

## 商業規則

1. 若 `store.status == DISABLED`，返回 **404**（與不存在相同 — 不揭露存在性）。
2. `products` 列表僅包含該店家 `lottery.status = 'ON_SHELF'` 的項目。
3. `remark`（內部管理員欄位）**永遠不包含**在公開回應中。
4. `ownerId` / `ownerDisplayName` **永遠不包含**在公開回應中。
5. 商品預設排序：`created_at DESC`（最新商品優先）。

---

## 回應中的商品欄位

| 欄位 | 型別 | 說明 |
|-------|------|-------------|
| `id` | String | 抽獎商品 UUID |
| `name` | String | 商品名稱 |
| `coverImageUrl` | String \| null | 商品圖片 |
| `pricePerDraw` | Integer | 每次抽獎點數 |
| `status` | String | 此情境下固定為 `ON_SHELF` |

> 完整商品詳情可透過現有抽獎端點取得。此列表作為導覽索引使用。

---

## 實作備注

```java
// StoreController.java
@GetMapping("/{id}")
@Operation(summary = "取得店家詳情", description = "含所有上架商品")
public ResponseEntity<StoreDetailRes> getStoreDetail(@PathVariable String id) {
    StoreDetailRes detail = storeService.getPublicStoreDetail(id);
    return ResponseEntity.ok(detail);
}

// StoreServiceImpl.java
public StoreDetailRes getPublicStoreDetail(String storeId) {
    Store store = storeMapper.selectByPrimaryKey(storeId);
    // Return 404 for both non-existent and disabled stores
    if (store == null || !StoreStatusEnum.ENABLED.getCode().equals(store.getStatus())) {
        throw new ResourceNotFoundException("店家不存在");
    }

    // Fetch ON_SHELF lotteries
    LotteryExample lotteryFilter = new LotteryExample();
    lotteryFilter.createCriteria()
        .andStoreIdEqualTo(storeId)
        .andStatusEqualTo(LotteryStatusEnum.ON_SHELF.getCode());
    lotteryFilter.setOrderByClause("created_at DESC");
    List<Lottery> lotteries = lotteryMapper.selectByExample(lotteryFilter);

    return toStoreDetailRes(store, lotteries);
}
```

---

## 安全性

- 公開存取端點 — 無需認證。
- Spring Security API 鏈中設定 `permitAll()`。
- `remark` 與 `ownerId` 欄位在 `StoreDetailRes` DTO 中已排除（永不對應）。
