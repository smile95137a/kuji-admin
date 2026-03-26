# 🎰 刮刮樂 & 抽卡 使用指南

## 概要

系統已實作完整的刮刮樂（SCRATCH_MODE）與一番賞/抽卡（LOTTERY_MODE）功能。
兩種模式共用同一套商品（Lottery）+ 獎品（Prize）架構，差異在於 `playMode` 欄位。

---

## 🎯 一、兩種模式說明

| 模式 | playMode 值 | 說明 |
|------|------------|------|
| **一番賞 / 抽卡** | `LOTTERY_MODE` | 隨機抽取，每抽一次隨機獲得一個獎品，直到抽完 |
| **刮刮樂** | `SCRATCH_MODE` | 有固定籤數（含「謝謝惠顧」），玩家可選籤位，也支援店家指定大賞位置 |

---

## 🔧 二、後台設定流程

### Step 1：建立商品 + 獎品（整合 API）

**API**: `POST /api/admin/lottery-with-prizes`

```json
{
  "lottery": {
    "title": "鬼滅之刃一番賞",
    "description": "限定版一番賞",
    "content": "<p>詳細活動說明（HTML）</p>",
    "imageUrl": "https://s3.../image.jpg",
    "price": 800,
    "playMode": "LOTTERY_MODE",   // ← 重點：選擇模式
    "maxDraws": 20,               // 總籤數（刮刮樂模式需設定）
    "currency": "GOLD",           // GOLD=金幣, BONUS=紅利
    "storeId": "uuid-store-1",
    "status": "DRAFT"
  },
  "prizes": [
    {
      "name": "炭治郎公仔",
      "description": "限定版公仔",
      "content": "獎品詳細介紹",
      "level": "A",
      "quantity": 1,
      "prizeType": "physical",
      "isGrandPrize": true,
      "isLastPrize": false
    },
    {
      "name": "禰豆子掛飾",
      "description": "精美掛飾",
      "level": "B",
      "quantity": 3,
      "prizeType": "physical",
      "isGrandPrize": false,
      "isLastPrize": false
    },
    {
      "name": "角色貼紙組",
      "description": "收藏貼紙",
      "level": "C",
      "quantity": 6,
      "prizeType": "physical",
      "isGrandPrize": false,
      "isLastPrize": false
    }
  ]
}
```

### Step 2：上架商品

**API**: `POST /api/admin/lottery-with-prizes/{lotteryId}/on-shelf`

上架後前台用戶就能看到此商品。

### Step 3（選用）：更新商品與獎品

**API**: `PUT /api/admin/lottery-with-prizes/{lotteryId}`

> ⚠️ 只有 **草稿 (DRAFT)** 或 **已下架 (OFF_SHELF)** 狀態可以修改

---

## 🎲 三、一番賞 / 抽卡模式（LOTTERY_MODE）

### 特點
- 每次抽取隨機獲得一個獎品
- 機率基於 `1 / 剩餘總數量` 均等
- 所有獎品抽完即結束

### 前台抽獎 API

**API**: `POST /api/lottery/draw/{lotteryId}/draw`

```json
{
  "count": 1
}
```

**回應**:
```json
[
  {
    "ticketId": "uuid-ticket-1",
    "ticketUuid": "T-001",
    "prizeId": "uuid-prize-1",
    "prizeName": "炭治郎公仔",
    "prizeLevel": "A",
    "prizeImageUrl": "https://...",
    "isWinning": true,
    "drawnAt": "2026-02-10T10:00:00"
  }
]
```

---

## 🎫 四、刮刮樂模式（SCRATCH_MODE）

### 特點
- 有固定籤數（`maxDraws`），包含有獎品的籤和「謝謝惠顧」無獎品的籤
- 系統自動產生籤號，隨機分配獎品到各籤位
- 玩家可以自己選籤號
- 支援店家/玩家 **指定大賞位置**

### maxDraws 設定邏輯
- `maxDraws` = 獎品總數量 + 謝謝惠顧數量
- 例如：10個獎品 + `maxDraws=20` → 有10張謝謝惠顧
- 如果 `maxDraws` 未設定或小於獎品總數，系統會自動調整為 `獎品總數 + 5`

### 前台 API

#### 4.1 查看籤列表

**API**: `GET /api/lottery/draw/{lotteryId}/tickets`

顯示所有籤號，未抽的籤不會顯示獎品（保密）。

**回應範例**:
```json
[
  {
    "id": "uuid-ticket-1",
    "ticketUuid": "01",
    "status": "AVAILABLE",
    "isWinning": null,
    "prize": null
  },
  {
    "id": "uuid-ticket-2",
    "ticketUuid": "02",
    "status": "DRAWN",
    "isWinning": true,
    "prize": {
      "name": "炭治郎公仔",
      "level": "A"
    }
  },
  {
    "id": "uuid-ticket-3",
    "ticketUuid": "03",
    "status": "DRAWN",
    "isWinning": false,
    "prize": null,
    "displayText": "謝謝惠顧"
  }
]
```

#### 4.2 抽指定籤號

**API**: `POST /api/lottery/draw/{lotteryId}/draw`

```json
{
  "ticketUuids": ["05", "12"]
}
```

也可以隨機抽（不指定籤號）:
```json
{
  "count": 2
}
```

#### 4.3 指定大賞位置（Designate）

**API**: `POST /api/lottery/draw/{lotteryId}/designate`

允許在抽獎前指定大賞（`isGrandPrize=true`）的籤號位置。

```json
{
  "positions": {
    "uuid-grand-prize-id": "07"
  }
}
```

> `positions` 是 Map<prizeId, ticketUuid>，指定哪個大賞放在哪個籤號

#### 4.4 查詢抽獎 Session

**API**: `GET /api/lottery/draw/{lotteryId}/session`

取得當前抽獎的進度資訊。

---

## 📋 五、刮刮樂設定範例（完整流程）

### 情境：建立一組20張的刮刮樂，其中5個有獎品

```json
POST /api/admin/lottery-with-prizes

{
  "lottery": {
    "title": "夏日刮刮樂",
    "description": "刮出夏日驚喜",
    "price": 200,
    "playMode": "SCRATCH_MODE",
    "maxDraws": 20,
    "currency": "GOLD",
    "storeId": "uuid-store-1"
  },
  "prizes": [
    {
      "name": "大獎：Switch 主機",
      "level": "A",
      "quantity": 1,
      "prizeType": "physical",
      "isGrandPrize": true
    },
    {
      "name": "二獎：AirPods",
      "level": "B",
      "quantity": 2,
      "prizeType": "physical"
    },
    {
      "name": "三獎：100元禮券",
      "level": "C",
      "quantity": 2,
      "prizeType": "digital",
      "pointValue": 100
    }
  ]
}
```

結果：系統產生 20 張籤（01-20），其中 5 張有獎品（隨機分配），15 張是「謝謝惠顧」。

### 上架後
```
POST /api/admin/lottery-with-prizes/{lotteryId}/on-shelf
```

### 玩家刮籤
```json
POST /api/lottery/draw/{lotteryId}/draw
{
  "ticketUuids": ["07"]
}
```

---

## 🔑 六、獎品等級對照表

| level 代碼 | 中文名稱 |
|-----------|---------|
| A | A賞 |
| B | B賞 |
| C | C賞 |
| D | D賞 |
| E | E賞 |
| F | F賞 |
| G | G賞 |
| LAST | 最後賞 |
| GRAND | 大賞 |

---

## 📊 七、獎品類型

| prizeType | 說明 |
|-----------|------|
| `physical` | 實體獎品（需寄送） |
| `digital` | 數位獎品（序號/兌換碼） |
| `point` | 點數回饋（直接入錢包） |

---

## ⚠️ 八、注意事項

1. **playMode 不可變更**：建立後不能從 LOTTERY_MODE 改成 SCRATCH_MODE（或反之）
2. **上架後不可修改獎品**：需先下架 → 修改 → 重新上架
3. **刮刮樂 maxDraws**：必須 ≥ 獎品總數量
4. **大賞指定**：只能在籤尚未被抽走前指定
5. **貨幣設定**：`currency` 決定使用金幣還是紅利消費
   - `GOLD`：使用金幣
   - `BONUS`：使用紅利

---

## 🔗 九、API 路徑總覽

### 後台 API（需 Admin 權限）

| 方法 | 路徑 | 說明 |
|------|------|------|
| POST | `/api/admin/lottery-with-prizes` | 建立商品+獎品 |
| PUT | `/api/admin/lottery-with-prizes/{id}` | 更新商品+獎品 |
| GET | `/api/admin/lottery-with-prizes/{id}` | 查詢商品+獎品詳情 |
| POST | `/api/admin/lottery-with-prizes/list` | 查詢商品列表 |
| POST | `/api/admin/lottery-with-prizes/{id}/on-shelf` | 上架 |
| POST | `/api/admin/lottery-with-prizes/{id}/off-shelf` | 下架 |
| DELETE | `/api/admin/lottery-with-prizes/{id}` | 刪除 |

### 前台 API

| 方法 | 路徑 | 說明 |
|------|------|------|
| GET | `/api/lottery/browse` | 瀏覽所有上架商品 |
| GET | `/api/lottery/browse/{id}` | 商品詳情 |
| GET | `/api/lottery/draw/{id}/tickets` | 查看籤列表（刮刮樂） |
| POST | `/api/lottery/draw/{id}/draw` | 抽獎/刮獎 |
| POST | `/api/lottery/draw/{id}/designate` | 指定大賞位置 |
| GET | `/api/lottery/draw/{id}/session` | 查詢抽獎 Session |
