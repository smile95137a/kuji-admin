# 一番賞完整流程測試指南

## 🔔 快速測試方式（推薦）

### 使用 VS Code REST Client 擴展

1. **開啟測試文件**: 專案根目錄的 `lottery-test.http`
2. **依序點擊 "Send Request"** 執行每個請求
3. **觀察回應**並驗證流程

### 測試文件路徑
```
c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin\lottery-test.http
```

---

## 流程概述

```
後台建立商品 → 自動生成籤位 → 前台查詢商品 → 進入商品詳情 → 抽獎 → 獎品進入賞品盒 → 出貨建立訂單 → 後台查看訂單
```

## API 清單

### 後台 API（需要 Admin Token）

| 步驟 | API | 方法 | 說明 |
|------|-----|------|------|
| 1 | `/admin/auth/login` | POST | 登入取得 Token |
| 2 | `/admin/lottery/with-prizes` | POST | 建立商品與獎品（自動生成籤位）|
| 3 | `/admin/order/list` | POST | 查詢訂單列表 |

### 前台 API

| 步驟 | API | 方法 | 說明 |
|------|-----|------|------|
| 1 | `/lottery/browse/list` | POST | 查詢商品列表（簡化版）|
| 2 | `/lottery/browse/store/{storeId}` | GET | 查詢店家商品列表 |
| 3 | `/lottery/browse/{id}` | GET | 取得商品基本資訊 |
| 4 | `/lottery/browse/{id}/detail` | GET | 取得商品詳情（含獎品+籤位+場次）|
| 5 | `/lottery/draw/{id}/draw` | POST | 執行抽獎 |
| 6 | `/prize-box` | GET | 查詢賞品盒 |
| 7 | `/prize-box/ship` | POST | 出貨（建立訂單）|

## 詳細測試步驟

### Step 1: 後台登入

```bash
POST /api/admin/auth/login
Content-Type: application/json

{
    "email": "admin@kuji.com",
    "password": "admin123"
}
```

回應（取得 token）：
```json
{
    "success": true,
    "data": {
        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        "userId": "xxx"
    }
}
```

### Step 2: 建立商品與獎品

```bash
POST /api/admin/lottery/with-prizes
Authorization: Bearer {admin_token}
Content-Type: application/json

{
    "lottery": {
        "storeId": "{store_id}",
        "title": "鬼滅之刃一番賞",
        "description": "限量發售",
        "category": "CUSTOM_LOTTERY",
        "subCategory": "LOTTERY_MODE",
        "pricePerDraw": 100,
        "maxDraws": 10,
        "status": "ON_SHELF"
    },
    "prizes": [
        {
            "name": "A賞 - 炭治郎公仔",
            "level": "A",
            "quantity": 1,
            "isGrandPrize": true
        },
        {
            "name": "B賞 - 禰豆子公仔",
            "level": "B",
            "quantity": 2
        },
        {
            "name": "C賞 - 鑰匙圈",
            "level": "C",
            "quantity": 3
        },
        {
            "name": "D賞 - 貼紙",
            "level": "D",
            "quantity": 4
        }
    ]
}
```

回應（商品建立成功，籤位自動生成）：
```json
{
    "success": true,
    "data": {
        "lottery": {
            "id": "lottery-uuid",
            "title": "鬼滅之刃一番賞",
            "maxDraws": 10
        },
        "prizes": [
            { "id": "prize-1", "name": "A賞 - 炭治郎公仔", "level": "A", "quantity": 1 },
            { "id": "prize-2", "name": "B賞 - 禰豆子公仔", "level": "B", "quantity": 2 }
        ]
    }
}
```

### Step 3: 前台查詢商品列表（簡化版）

```bash
POST /api/lottery/browse/list
Content-Type: application/json

{}
```

回應：
```json
{
    "success": true,
    "data": [
        {
            "id": "lottery-uuid",
            "storeId": "store-uuid",
            "storeName": "測試店家",
            "title": "鬼滅之刃一番賞",
            "imageUrl": "https://...",
            "pricePerDraw": 100,
            "maxDraws": 10,
            "remainingDraws": 10,
            "status": "ON_SHELF"
        }
    ]
}
```

### Step 4: 前台查詢商品詳情（完整版）

```bash
GET /api/lottery/browse/{lotteryId}/detail
Authorization: Bearer {user_token}  # 可選，未登入也能看
```

回應：
```json
{
    "success": true,
    "data": {
        "lottery": {
            "id": "lottery-uuid",
            "title": "鬼滅之刃一番賞",
            "description": "限量發售",
            "pricePerDraw": 100,
            "maxDraws": 10,
            "remainingDraws": 10
        },
        "prizes": [
            { "name": "A賞 - 炭治郎公仔", "level": "A", "quantity": 1, "remaining": 1, "isGrandPrize": true },
            { "name": "B賞 - 禰豆子公仔", "level": "B", "quantity": 2, "remaining": 2 }
        ],
        "tickets": [
            { "ticketNumber": 1, "status": "AVAILABLE" },
            { "ticketNumber": 2, "status": "AVAILABLE" },
            { "ticketNumber": 3, "status": "AVAILABLE" }
        ],
        "session": {
            "isOpener": true,
            "canDraw": true,
            "protectionEndTime": null
        }
    }
}
```

⚠️ **注意**：`tickets` 中的未抽籤位不會顯示 `prizeLevel` 或 `prizeName`（安全設計）

### Step 5: 執行抽獎

```bash
POST /api/lottery/draw/{lotteryId}/draw
Authorization: Bearer {user_token}
Content-Type: application/json

{
    "ticketNumber": null,  // null = 隨機抽, 或指定號碼如 5
    "drawCount": 1
}
```

回應：
```json
{
    "success": true,
    "data": {
        "success": true,
        "ticketId": "ticket-uuid",
        "ticketNumber": 5,
        "prizeId": "prize-1",
        "prizeLevel": "A",
        "prizeName": "A賞 - 炭治郎公仔",
        "prizeImageUrl": "https://...",
        "isGrandPrize": true,
        "triggeredFreeDraw": false,
        "refundAmount": 0,
        "message": "抽獎成功！恭喜獲得 A賞 - 炭治郎公仔"
    }
}
```

### Step 6: 查詢賞品盒

```bash
GET /api/prize-box
Authorization: Bearer {user_token}
```

回應：
```json
{
    "success": true,
    "data": [
        {
            "id": "prize-box-uuid",
            "userId": "user-uuid",
            "storeId": "store-uuid",
            "storeName": "測試店家",
            "lotteryId": "lottery-uuid",
            "lotteryTitle": "鬼滅之刃一番賞",
            "prizeId": "prize-1",
            "prizeName": "A賞 - 炭治郎公仔",
            "prizeLevel": "A",
            "prizeImageUrl": "https://...",
            "status": "IN_BOX",
            "recycleBonus": 50,
            "createdAt": "2026-01-27T10:30:00"
        }
    ]
}
```

### Step 7: 出貨（建立訂單）

```bash
POST /api/prize-box/ship
Authorization: Bearer {user_token}
Content-Type: application/json

{
    "prizeBoxIds": ["prize-box-uuid-1", "prize-box-uuid-2"],
    "shippingMethod": "HOME_DELIVERY",
    "recipientName": "王小明",
    "recipientPhone": "0912345678",
    "recipientAddress": "台北市信義區松仁路100號"
}
```

回應：
```json
{
    "success": true,
    "data": ["order-uuid-1"]
}
```

### Step 8: 後台查詢訂單

```bash
POST /api/admin/order/list
Authorization: Bearer {admin_token}
Content-Type: application/json

{}
```

回應：
```json
{
    "success": true,
    "data": [
        {
            "id": "order-uuid-1",
            "userId": "user-uuid",
            "storeId": "store-uuid",
            "status": "PENDING",
            "totalAmount": 100,
            "shippingMethod": "HOME_DELIVERY",
            "items": [
                {
                    "prizeBoxId": "prize-box-uuid-1",
                    "prizeName": "A賞 - 炭治郎公仔"
                }
            ],
            "createdAt": "2026-01-27T10:35:00"
        }
    ]
}
```

## 保護時間機制測試

當玩家 A 開始抽獎時，會建立場次（Session），在保護時間內：

1. 玩家 A（開套者）可以繼續抽獎
2. 玩家 B 嘗試抽獎會收到：
   ```json
   {
       "success": false,
       "message": "商品正在被其他玩家抽獎中，請稍後再試"
   }
   ```

## API 回傳格式對照表

| API | 用途 | 回傳類型 | 說明 |
|-----|------|----------|------|
| `/lottery/browse/list` | 商品列表頁 | `List<LotteryListItemRes>` | 簡化版，只含必要欄位 |
| `/lottery/browse/{id}` | 商品基本資訊 | `LotteryRes` | 完整商品資訊 |
| `/lottery/browse/{id}/detail` | 商品詳情頁 | `LotteryDetailRes` | 商品+獎品+籤位+場次 |
| `/lottery/draw/{id}/tickets` | 籤位列表 | `TicketListResponse` | 籤位+場次 |
| `/lottery/draw/{id}/draw` | 抽獎 | `DrawResult` | 抽獎結果 |

## 常見問題

### Q: 籤位沒有生成？
A: 確認：
1. `maxDraws` > 0
2. 有設定獎品（`prizes` 陣列不為空）
3. 查看 log 是否有 `✅ 籤位生成完成`

### Q: 抽獎返回「商品正在被其他玩家抽獎中」？
A: 這是保護時間機制，可能：
1. 另一個玩家正在抽這個商品
2. 你的 Session 過期了（等待幾分鐘後重試）

### Q: 前台籤位看不到獎品資訊？
A: 這是正確的！未抽籤位不會顯示獎品資訊（安全設計）
