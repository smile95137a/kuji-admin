# API 契約： GET /api/prize-box

**功能**：獎品盒 (010-prize-box)  
**端點**：`GET /api/prize-box`  
**用途**：查詢玩家當前獎品盒內容（IN_BOX 狀態），以店家分組顯示

---

## 概覽

玩家開啟獎品盒頁面時呼叫此端點，取得所有尚未出貨/回收的獎品，按店家分組排列。這是出貨與回收操作的基礎視圖。

---

## 驗證

**必要性**：✅ JWT Bearer Token  
**標頭**：`Authorization: Bearer <token>`  
**身份識別**：`userId` 從 JWT 中提取（`SecurityUtils.getCurrentUserId()`）

---

## 請求

```http
GET /api/prize-box
Authorization: Bearer {jwt_token}
```

### 查詢參數

無（玩家身份從 JWT 決定，無需額外參數）

---

## 回應

### 成功 — 200 OK

```json
[
  {
    "storeId": "store-uuid-001",
    "storeName": "扭蛋王店",
    "itemCount": 2,
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
        "status": "IN_BOX",
        "statusName": "在獎品盒中",
        "isRecyclable": true,
        "isShippable": true,
        "recycleBonus": 50,
        "createdAt": "2026-03-20T14:30:00"
      },
      {
        "id": "prizebox-uuid-002",
        "userId": "user-uuid-001",
        "lotteryId": "lottery-uuid-001",
        "lotteryTitle": "鬼滅之刃 一番賞 Vol.3",
        "lotteryImageUrl": "https://cdn.kuji.com/lotteries/kimetsu-v3.jpg",
        "prizeId": "prize-uuid-002",
        "prizeName": "C賞 善逸 壓克力板",
        "prizeLevel": "C",
        "prizeImageUrl": "https://cdn.kuji.com/prizes/zenitsu-acrylic.jpg",
        "prizeValue": 350,
        "storeId": "store-uuid-001",
        "storeName": "扭蛋王店",
        "status": "IN_BOX",
        "statusName": "在獎品盒中",
        "isRecyclable": false,
        "isShippable": true,
        "recycleBonus": 0,
        "createdAt": "2026-03-21T09:15:00"
      }
    ]
  },
  {
    "storeId": "store-uuid-002",
    "storeName": "一番賞天堂",
    "itemCount": 1,
    "items": [
      {
        "id": "prizebox-uuid-003",
        "userId": "user-uuid-001",
        "lotteryId": "lottery-uuid-002",
        "lotteryTitle": "海賊王 FILM RED 一番賞",
        "lotteryImageUrl": "https://cdn.kuji.com/lotteries/op-film-red.jpg",
        "prizeId": "prize-uuid-010",
        "prizeName": "Last賞 路飛 大型公仔",
        "prizeLevel": "Last",
        "prizeImageUrl": "https://cdn.kuji.com/prizes/luffy-last.jpg",
        "prizeValue": 3500,
        "storeId": "store-uuid-002",
        "storeName": "一番賞天堂",
        "status": "IN_BOX",
        "statusName": "在獎品盒中",
        "isRecyclable": true,
        "isShippable": true,
        "recycleBonus": 200,
        "createdAt": "2026-03-22T11:00:00"
      }
    ]
  }
]
```

### 空獎品盒 — 200 OK

```json
[]
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

### PrizeBoxSummaryRes

| 欄位 | 型別 | 說明 |
|-------|------|-------------|
| `storeId` | String (UUID) | 店家 ID |
| `storeName` | String | 店家名稱 |
| `itemCount` | Integer | 本店家獎品數量 |
| `items` | Array\<PrizeBoxItemRes\> | 獎品列表 |

### PrizeBoxItemRes

| 欄位 | 型別 | 可空 | 說明 |
|-------|------|----------|-------------|
| `id` | String (UUID) | ❌ | 獎品盒項目 ID |
| `userId` | String (UUID) | ❌ | 玩家 ID |
| `lotteryId` | String (UUID) | ❌ | 來源一番賞 ID |
| `lotteryTitle` | String | ✅ | 一番賞名稱 |
| `lotteryImageUrl` | String | ✅ | 一番賞圖片 URL |
| `prizeId` | String (UUID) | ❌ | 獎項 ID |
| `prizeName` | String | ✅ | 獎項名稱 |
| `prizeLevel` | String | ✅ | 獎項等級（A/B/C/.../Last）|
| `prizeImageUrl` | String | ✅ | 獎項圖片 URL |
| `prizeValue` | Long | ✅ | 獎品市值（新增） |
| `storeId` | String (UUID) | ❌ | 店家 ID |
| `storeName` | String | ✅ | 店家名稱 |
| `status` | String | ❌ | `IN_BOX` |
| `statusName` | String | ❌ | `在獎品盒中` |
| `isRecyclable` | Boolean | ❌ | 是否可回收（`recycleBonus > 0`） |
| `isShippable` | Boolean | ❌ | 是否可出貨（新增） |
| `recycleBonus` | Long | ✅ | 回收可得 Bonus 點數 |
| `createdAt` | DateTime | ❌ | 獲獎時間（ISO 8601）|

---

## 實作備註

1. **排序**： 此端點只返回 `status = IN_BOX`，按 `created_at DESC` 排序
2. **分組**： Service 層按 `storeId` 分組，非 DB query 分組
3. **isRecyclable**: 必須是 `recycleBonus != null && recycleBonus > 0`（修正現有 bug）
4. **isShippable**: 從 `PrizeBox.isShippable` 欄位讀取（`1 = true`）
5. **效能**： 每件獎品需查 `Lottery`, `LotteryPrize`, `Store` — 若獎品盒過大考慮 JOIN 查詢優化

---

## 測試情境

| # | 情境 | 預期結果 |
|---|----------|----------|
| 1 | 玩家有來自 2 家店家的獎品 | 回傳 2 個分組 |
| 2 | 玩家無 IN_BOX 獎品 | 回傳 `[]` |
| 3 | 獎品 recycleBonus=0 | `isRecyclable: false` |
| 4 | 未登入 | 401 |
| 5 | 剛完成抽獎 | 新獎品立即出現 |
