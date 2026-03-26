# API 契約： GET /api/prize-box/history

**功能**：獎品盒 (010-prize-box)  
**端點**：`GET /api/prize-box/history`  
**用途**：查詢玩家獎品盒的完整歷史記錄（含已出貨與已回收）

---

## 概覽

玩家查看「歷史記錄」頁面時呼叫此端點，回傳所有狀態的獎品（IN_BOX + SHIPPED + RECYCLED），按獲獎時間倒序排列。**此端點為現有實作缺失，需新增。**

---

## 驗證

**必要性**：✅ JWT Bearer Token  
**標頭**：`Authorization: Bearer <token>`

---

## 請求

```http
GET /api/prize-box/history
Authorization: Bearer {jwt_token}
```

### 查詢參數

| 參數 | 型別 | 必填 | 說明 |
|-----------|------|----------|-------------|
| `status` | String | ❌ | 選填：過濾狀態 `IN_BOX` / `SHIPPED` / `RECYCLED` |
| `page` | Integer | ❌ | 頁碼（從 1 開始，預設 1）|
| `size` | Integer | ❌ | 每頁筆數（預設 20，最大 100）|

---

## 回應

### 成功 — 200 OK

```json
{
  "total": 15,
  "page": 1,
  "size": 20,
  "items": [
    {
      "id": "prizebox-uuid-001",
      "userId": "user-uuid-001",
      "lotteryId": "lottery-uuid-001",
      "lotteryTitle": "鬼滅之刃 一番賞 Vol.3",
      "lotteryImageUrl": "https://cdn.kuji.com/lotteries/kimetsu-v3.jpg",
      "prizeId": "prize-uuid-001",
      "prizeName": "A賞 炭治郎 公仔",
      "prizeLevel": "A",
      "prizeImageUrl": "https://cdn.kuji.com/prizes/tanjiro-figure.jpg",
      "prizeValue": 1200,
      "storeId": "store-uuid-001",
      "storeName": "扭蛋王店",
      "status": "SHIPPED",
      "statusName": "已出貨",
      "isRecyclable": true,
      "isShippable": true,
      "recycleBonus": 50,
      "createdAt": "2026-03-20T14:30:00",
      "shippedAt": "2026-03-21T10:00:00",
      "recycledAt": null
    },
    {
      "id": "prizebox-uuid-005",
      "userId": "user-uuid-001",
      "lotteryId": "lottery-uuid-003",
      "lotteryTitle": "進擊的巨人 最終季 一番賞",
      "lotteryImageUrl": "https://cdn.kuji.com/lotteries/aot-final.jpg",
      "prizeId": "prize-uuid-020",
      "prizeName": "B賞 艾連 亞克力立牌",
      "prizeLevel": "B",
      "prizeImageUrl": "https://cdn.kuji.com/prizes/eren-acrylic.jpg",
      "prizeValue": 480,
      "storeId": "store-uuid-002",
      "storeName": "一番賞天堂",
      "status": "RECYCLED",
      "statusName": "已回收",
      "isRecyclable": true,
      "isShippable": false,
      "recycleBonus": 30,
      "createdAt": "2026-03-18T08:20:00",
      "shippedAt": null,
      "recycledAt": "2026-03-19T15:45:00"
    }
  ]
}
```

### 無歷史記錄 — 200 OK

```json
{
  "total": 0,
  "page": 1,
  "size": 20,
  "items": []
}
```

### 未授權 — 401

```json
{
  "code": 401,
  "message": "未授權，請重新登入"
}
```

---

## 回應 Schema

### 分頁包裝結構

| 欄位 | 型別 | 說明 |
|-------|------|-------------|
| `total` | Integer | 總筆數 |
| `page` | Integer | 當前頁碼 |
| `size` | Integer | 每頁筆數 |
| `items` | Array\<PrizeBoxItemRes\> | 獎品列表 |

### PrizeBoxItemRes（歷史視圖）

| 欄位 | 型別 | 可空 | 說明 |
|-------|------|----------|-------------|
| `id` | String | ❌ | 獎品盒項目 ID |
| `lotteryId` | String | ❌ | 來源一番賞 ID |
| `lotteryTitle` | String | ✅ | 一番賞名稱 |
| `lotteryImageUrl` | String | ✅ | 一番賞圖片 |
| `prizeId` | String | ❌ | 獎項 ID |
| `prizeName` | String | ✅ | 獎項名稱 |
| `prizeLevel` | String | ✅ | 獎項等級 |
| `prizeImageUrl` | String | ✅ | 獎項圖片 URL |
| `prizeValue` | Long | ✅ | 獎品市值 |
| `storeId` | String | ❌ | 店家 ID |
| `storeName` | String | ✅ | 店家名稱 |
| `status` | String | ❌ | `IN_BOX` / `SHIPPED` / `RECYCLED` |
| `statusName` | String | ❌ | 中文狀態名稱 |
| `isRecyclable` | Boolean | ❌ | 是否可回收 |
| `isShippable` | Boolean | ❌ | 是否可出貨 |
| `recycleBonus` | Long | ✅ | 回收 Bonus 點數 |
| `createdAt` | DateTime | ❌ | 獲獎時間 |
| `shippedAt` | DateTime | ✅ | 出貨時間（僅 SHIPPED）|
| `recycledAt` | DateTime | ✅ | 回收時間（僅 RECYCLED）|

---

## 實作備註

### PrizeBoxService 需新增方法

```java
/**
 * 查詢玩家獎品盒歷史（所有狀態）
 * @param userId 玩家 ID
 * @param status 狀態篩選（null = 全部）
 * @param page 頁碼（從 1 開始）
 * @param size 每頁筆數
 * @return 分頁獎品列表
 */
PageResult<PrizeBoxItemRes> getPrizeBoxHistory(String userId, String status, int page, int size);
```

### SQL 查詢

```sql
SELECT * FROM prize_box
WHERE user_id = #{userId}
  AND (#{status} IS NULL OR status = #{status})
ORDER BY created_at DESC
LIMIT #{offset}, #{size}
```

### 新增 Controller 方法

```java
@GetMapping("/history")
public ResponseEntity<PageResult<PrizeBoxItemRes>> getHistory(
    @RequestParam(required = false) String status,
    @RequestParam(defaultValue = "1") int page,
    @RequestParam(defaultValue = "20") int size
) {
    String userId = SecurityUtils.getCurrentUserId();
    return ResponseEntity.ok(prizeBoxService.getPrizeBoxHistory(userId, status, page, size));
}
```

---

## 測試情境

| # | 情境 | 預期結果 |
|---|----------|----------|
| 1 | 玩家有 IN_BOX + SHIPPED + RECYCLED 記錄 | 全部回傳，按 createdAt DESC |
| 2 | 過濾 `?status=SHIPPED` | 只回傳已出貨記錄 |
| 3 | 過濾 `?status=RECYCLED` | 只回傳已回收記錄 |
| 4 | 無歷史記錄 | `{ total: 0, items: [] }` |
| 5 | 分頁 `?page=2&size=5` | 回傳第 2 頁，每頁 5 筆 |
| 6 | 未登入 | 401 |
| 7 | SHIPPED 記錄有 `shippedAt` | `shippedAt` 非 null |
| 8 | RECYCLED 記錄有 `recycledAt` | `recycledAt` 非 null |
