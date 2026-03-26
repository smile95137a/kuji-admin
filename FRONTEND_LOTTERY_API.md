# 前台抽獎 API 文件

> 最後更新：2026-03-02  
> Base URL：`http://18.179.187.129/api`  
> 所有需要登入的 API 請帶 `Authorization: Bearer <token>`

---

## 目錄

- [一、商品瀏覽 API](#一商品瀏覽-api)
  - [GET /lottery/browse/{id}/detail](#get-lotterybrowseiddetail)
- [二、抽獎 API](#二抽獎-api)
  - [POST /lottery/draw/{lotteryId}/draw](#post-lotterydrawlotteryiddraw)
  - [POST /lottery/draw/{lotteryId}/designate](#post-lotterydrawlotteryiddesignate)
  - [GET /lottery/draw/{lotteryId}/session](#get-lotterydrawlotteryidsession)
- [三、資料結構總覽](#三資料結構總覽)
- [四、遊戲模式流程](#四遊戲模式流程)

---

## 一、商品瀏覽 API

### GET /lottery/browse/{id}/detail

取得商品完整詳情。**這是主要入口，包含所有資訊。**  
不需要登入也可以呼叫，但未登入時 `session` 欄位會是 null。

**回應：**

```jsonc
{
  "success": true,
  "data": {
    "lottery": {
      "id": "f0a00002-...",
      "title": "鬼滅之刃一番賞",
      "category": "OFFICIAL_ICHIBAN",   // OFFICIAL_ICHIBAN | TRADING_CARD | GACHA | CUSTOM_GACHA
      "playMode": "LOTTERY_MODE",        // LOTTERY_MODE | SCRATCH_MODE | SCRATCH_CARD_MODE
      "gameMode": "RANDOM",              // RANDOM | SCRATCH_STORE | SCRATCH_PLAYER
      "status": "ON_SHELF",
      "totalTickets": 80,
      "remainingTickets": 65,
      "pricePerDraw": 200,
      "protectionMinutes": 5
      // ...其他商品欄位
    },
    "prizes": [
      {
        "id": "prize-uuid",
        "name": "SSP 大獎",
        "level": "SSP",
        "quantity": 1,
        "imageUrl": "https://...",
        "isGrandPrize": true,
        "isLastPrize": false
      }
    ],
    "tickets": [
      // 未抽籤位：只有 id、ticketNumber、status
      { "id": "uuid", "ticketNumber": 1, "status": "AVAILABLE" },
      // 已抽籤位：包含完整獎品資訊
      {
        "id": "uuid",
        "ticketNumber": 2,
        "status": "DRAWN",
        "revealedNumber": 42,        // 刮刮樂才有，一番賞/扭蛋為 null
        "prizeId": "prize-uuid",
        "prizeLevel": "A",
        "prizeName": "A賞 炭治郎",
        "prizeImageUrl": "https://...",
        "isGrandPrize": false,
        "isLastPrize": false,
        "drawnByNickname": "玩家甲",
        "drawnAt": "2026-03-02T10:00:00"
      }
    ],
    "session": {
      // 未登入或無進行中場次時為 null
      "isOpener": true,              // 是否為本場開套玩家
      "openerNickname": null,        // 開套玩家暱稱（暫未實作）
      "protectionEndTime": "2026-03-02T10:05:00",  // null 表示保護尚未啟動
      "status": "ACTIVE",            // ACTIVE | EXPIRED
      "canDraw": true,               // 目前是否可以抽獎
      "cannotDrawReason": null       // 不能抽獎的原因文字
    },
    "designatedWinningNumbers": [
      // 刮刮樂(SCRATCH_STORE / SCRATCH_PLAYER)專用
      // 公開告知所有玩家「幾號有大獎」，一番賞/扭蛋此陣列為空 []
      {
        "revealedNumber": 7,         // 此號碼刮開後是大獎
        "prizeId": "prize-uuid",
        "prizeName": "SSP 大獎",
        "prizeLevel": "SSP",
        "prizeImageUrl": "https://..."
      }
    ]
  }
}
```

---

## 二、抽獎 API

---

### POST /lottery/draw/{lotteryId}/draw

執行抽獎。**需要登入。**

**Request Body：**

```jsonc
{
  "count": 1,          // 必填，抽幾張（1~10）
  "ticket": [          // 選填，指定特定票券的 UUID 列表
    "ticket-uuid-1",
    "ticket-uuid-2"
  ]
  // 不傳 ticket 陣列 = 隨機抽
}
```

> ⚠️ 傳 `ticket` 時，陣列長度必須等於 `count`，且不可重複、必須是有效 UUID。

---

**回應 A：正常抽獎結果（一番賞 / 扭蛋 / 刮刮樂 SCRATCH_STORE）**

```jsonc
{
  "success": true,
  "data": {
    "playMode": "LOTTERY_MODE",    // LOTTERY_MODE | SCRATCH_MODE
    "gameMode": "RANDOM",          // RANDOM | SCRATCH_STORE | SCRATCH_PLAYER
    "results": [
      {
        "success": true,
        "ticketId": "uuid",
        "ticketNumber": 15,
        "revealedNumber": 42,      // 刮刮樂才有（刮開後顯示的號碼），其他為 null
        "prizeId": "uuid",
        "prizeLevel": "B",
        "prizeName": "B賞 善逸",
        "prizeImageUrl": "https://...",
        "isGrandPrize": false,
        "triggeredFreeDraw": false, // 是否觸發免單
        "refundAmount": 0,         // 免單退還金額（觸發免單才有值）
        "message": null
      }
    ],
    "protectionEndTime": "2026-03-02T10:05:00"
    // 扭蛋(GACHA)此欄位為 null
    // 其他類型：首次抽獎啟動保護，此後每次都回傳保護結束時間
    // null 表示「剛啟動保護，請用此回應時間 + 商品 protectionMinutes 自行計算」→ 實際會直接給時間
  }
}
```

> **`protectionEndTime` 說明：**
> - 扭蛋：永遠是 `null`（扭蛋沒有保護時間）
> - 首次抽其他類型商品：啟動保護，回傳保護結束時間（ISO 格式）
> - 前端可顯示倒數計時提示「目前由 xxx 開套中，保護時間剩餘 X 分鐘」

---

**回應 B：刮刮樂 SCRATCH_PLAYER — 開套者需先指定大獎位置**

首次抽獎如果 `gameMode=SCRATCH_PLAYER` 且尚未指定，系統會攔截並要求先指定：

```jsonc
{
  "success": true,
  "data": {
    "designationRequired": true,
    "message": "請先指定大獎位置（共需指定 2 個號碼）",
    "availableNumbers": [1, 3, 5, 7, 9, 12, ...],  // 可選的 revealedNumber（格子號碼）
    "grandPrizes": [
      {
        "prizeId": "uuid",
        "prizeName": "SSP 大獎",
        "prizeLevel": "SSP",
        "quantity": 1,             // 此獎項需要指定幾個號碼
        "prizeImageUrl": "https://..."
      },
      {
        "prizeId": "uuid",
        "prizeName": "SP 大獎",
        "prizeLevel": "SP",
        "quantity": 1,
        "prizeImageUrl": "https://..."
      }
    ]
  }
}
```

> 收到此回應後，前端應顯示選號 UI，讓開套者從 `availableNumbers` 中選擇對應數量的格子，再呼叫 `/designate`。  
> `grandPrizes[].quantity` 加總 = 共需選幾個號碼。

---

**回應 C：被保護時間擋住（其他玩家正在開套）**

```jsonc
{
  "success": false,
  "error": {
    "message": "商品正在被其他玩家抽獎中，請稍後再試"
  }
}
```

---

### POST /lottery/draw/{lotteryId}/designate

**刮刮樂 SCRATCH_PLAYER 專用。**  
開套者在首次抽獎前，指定哪些號碼是大獎。  
**需要登入，且必須是本場開套者。**

**Request Body：**

```jsonc
{
  "designations": [
    { "revealedNumber": 7,  "prizeId": "ssp-prize-uuid" },
    { "revealedNumber": 23, "prizeId": "sp-prize-uuid"  }
  ]
}
```

> `revealedNumber` 必須從 `/draw` 回應的 `availableNumbers` 中選。  
> 每個 `prizeId` 對應 `grandPrizes` 裡的獎項，需按獎項的 `quantity` 數量分配。

**回應：**

```jsonc
{
  "success": true,
  "data": {
    "success": true,
    "message": "大獎位置指定完成，共 2 個",
    "designatedWinningNumbers": [
      { "revealedNumber": 7,  "prizeId": "uuid", "prizeName": "SSP 大獎", "prizeLevel": "SSP", "prizeImageUrl": "https://..." },
      { "revealedNumber": 23, "prizeId": "uuid", "prizeName": "SP 大獎",  "prizeLevel": "SP",  "prizeImageUrl": "https://..." }
    ]
  }
}
```

指定完成後，立即呼叫 `POST /draw` 開始抽獎。

---

### GET /lottery/draw/{lotteryId}/session

取得目前場次資訊（唯讀，不建立場次）。  
**需要登入。**

**使用時機：** 前端想確認「目前是誰在開套、保護時間還剩多久」。

**回應（有進行中場次）：**

```jsonc
{
  "success": true,
  "data": {
    "sessionId": "uuid",
    "isOpener": false,             // 當前登入者是否是開套者
    "openerNickname": null,        // 開套者暱稱（暫未實作）
    "protectionDraws": 5,          // 保護期內幾張（設定值）
    "protectionEndTime": "2026-03-02T10:05:00",  // 保護結束時間；尚未啟動為 null
    "openerDrawCount": 3,          // 開套者已抽幾張
    "freeDrawEnabled": false,      // 是否已觸發免單
    "status": "ACTIVE"             // ACTIVE | EXPIRED
  }
}
```

**回應（無進行中場次）：**

```jsonc
{
  "success": true,
  "data": null
}
```

---

## 三、資料結構總覽

### ticket 狀態

| status | 說明 |
|--------|------|
| `AVAILABLE` | 可抽，未抽 |
| `DRAWN` | 已抽 |
| `LOCKED` | 鎖定中（免單保留等） |

### category（商品分類）

| category | 說明 |
|----------|------|
| `OFFICIAL_ICHIBAN` | 一番賞 |
| `TRADING_CARD` | 卡牌 |
| `GACHA` | 扭蛋 |
| `CUSTOM_GACHA` | 自訂扭蛋 |

### gameMode 與 playMode 對照

| playMode | gameMode | 說明 |
|----------|----------|------|
| `LOTTERY_MODE` | `RANDOM` | 一番賞 / 卡牌（隨機） |
| `SCRATCH_MODE` | `RANDOM` | 刮刮樂（全隨機，無大獎指定） |
| `SCRATCH_MODE` | `SCRATCH_STORE` | 刮刮樂（店家事先指定大獎號碼） |
| `SCRATCH_MODE` | `SCRATCH_PLAYER` | 刮刮樂（開套者進入後指定大獎號碼） |
| `SCRATCH_CARD_MODE` | `RANDOM` | 扭蛋 |

---

## 四、遊戲模式流程

### 一番賞 / 卡牌（LOTTERY_MODE + RANDOM）

```
進入商品頁
  → GET /browse/{id}/detail
  → 顯示剩餘籤位數量、獎品表
  → 玩家點「抽獎」
  → POST /draw/{id}/draw  { count: 1 }
  → 顯示結果（prizeLevel, prizeName, prizeImageUrl）
  → 回應帶 protectionEndTime → 顯示「開套保護中 X 分鐘」倒數
```

### 扭蛋（GACHA / SCRATCH_CARD_MODE）

```
進入商品頁
  → GET /browse/{id}/detail
  → 玩家點「抽獎」
  → POST /draw/{id}/draw  { count: 1 }
  → ⚡ 後端 synchronized，確保同時只有一人
  → 顯示結果
  → protectionEndTime 永遠為 null（不顯示保護倒數）
```

### 刮刮樂 SCRATCH_STORE（店家指定大獎）

```
進入商品頁
  → GET /browse/{id}/detail
  → designatedWinningNumbers 已有資料 → 顯示「這些號碼是大獎！」
  → 玩家點格子
  → POST /draw/{id}/draw  { count: 1, ticket: ["uuid"] }
  → 顯示 revealedNumber（刮開後的號碼）與獎品
```

### 刮刮樂 SCRATCH_PLAYER（開套者指定大獎）

```
進入商品頁
  → GET /browse/{id}/detail
  → designatedWinningNumbers = []（尚未指定）
  → 開套者點「開始抽獎」
  → POST /draw/{id}/draw  { count: 1 }
  → 後端回應 designationRequired: true
      附帶 availableNumbers（可選格子）、grandPrizes（要指定幾個哪些獎）
  → 前端顯示「選號 UI」，開套者選好格子
  → POST /draw/{id}/designate  { designations: [...] }
  → 指定完成 → 回應帶 designatedWinningNumbers（公告給所有玩家）
  → 立即 POST /draw/{id}/draw 開始抽獎
  → 其他玩家重新 GET /browse/{id}/detail
      → designatedWinningNumbers 已有資料，可看到哪些號碼是大獎
```

---

## 五、重要注意事項

### ⚠️ revealedNumber 與 ticketNumber 不同

| 欄位 | 說明 |
|------|------|
| `ticketNumber` | 籤位物理序號 (1~N)，玩家選格子用 |
| `revealedNumber` | 刮開後才顯示的隨機號碼，用於大獎指定判斷 |

- 指定大獎時傳 `revealedNumber`，不是 `ticketNumber`
- `revealedNumber` 只在 `status=DRAWN` 的票券上才會回傳給前端（刮開前不洩漏）

### ⚠️ 保護時間邏輯

- 保護時間在**首次抽獎當下**才啟動，不是進入頁面就啟動
- `protectionEndTime = null` → 尚未有人開始抽（可以自由進入）
- `canDraw: false` → 有保護時間，且當前使用者不是開套者
- 開套者本人在保護時間內**不受影響**，可以持續抽

### ⚠️ 何時呼叫 /session

- 絕大多數情況下 `/detail` 已包含 session 資訊，不需要另外呼叫
- `/session` 適合用在：輪詢確認保護時間是否已結束（其他玩家等待時）
