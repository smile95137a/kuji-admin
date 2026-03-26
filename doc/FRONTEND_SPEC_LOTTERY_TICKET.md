# 前端開發規格 — 抽獎籤位系統

> **最後更新**：2026-02-26  
> **對應後端**：`LotteryDrawController.java`  
> **Base URL**：`/api/lottery/draw/{lotteryId}` (context-path `/api` + mapping `/lottery/draw`)

---

## 一、各遊戲模式行為差異

| 行為 | RANDOM（一番賞 / 扭蛋 / 卡牌）| SCRATCH_STORE / SCRATCH_PLAYER（刮刮樂）|
|------|-------------------------------|----------------------------------------|
| 籤位選取方式 | 點選籤位格子，或隨機 | 點選實體卡片（ticketNumber）|
| 抽完後顯示 | 直接顯示獎品等級＋名稱 | 先播刮卡動畫 → 顯示 revealedNumber → 顯示獎品 |
| 是否有 revealedNumber | ❌ null | ✅ 整數（1-N）|
| 未抽籤位顯示 | 編號＋可抽狀態 | 卡片背面（不透露任何資訊）|
| 已抽籤位顯示 | 獎品等級＋名稱 | revealedNumber＋獎品等級＋名稱 |

> `gameMode` 的值從**商品詳情/瀏覽 API** 取得，不從 `/tickets` response 取得。

---

## 二、前端 UI 狀態機（每張籤位）

```
AVAILABLE
  │ 玩家點擊
  ▼
POST /api/lottery/draw/{id}/draw
  { count: 1, ticket: ["<uuid>"] }
  │
  ├── [RANDOM 模式] Response（陣列，取第 0 筆）
  │     success = true → 直接顯示獎品資訊
  │     更新該 ticketNumber 狀態為 DRAWN
  │
  └── [SCRATCH 模式] Response（含 revealedNumber）
        success = true → 播刮卡動畫（約 1-2 秒，純視覺效果）
        動畫結束 → 顯示 revealedNumber（大字）
        → 顯示 prizeLevel / prizeName（或「謝謝惠顧」）
        → 更新狀態為 DRAWN

LOCKED → 顯示「鎖定中，請稍待」（不可點擊）
DRAWN  → 顯示已抽結果（不可點擊）
```

> ⚠️ **動畫結束前禁止顯示獎品資訊**，防止截圖跳過動畫作弊。

---

## 三、API 端點快速參考

| 方法 | 路徑 | 說明 |
|------|------|------|
| GET | `/api/lottery/draw/{id}/tickets` | 取得所有籤位（未抽不含獎品 / revealedNumber）|
| POST | `/api/lottery/draw/{id}/draw` | 執行抽獎（指定 UUID 或隨機）|
| POST | `/api/lottery/draw/{id}/designate` | SCRATCH_PLAYER 開套玩家指定大獎位置 |
| GET | `/api/lottery/draw/{id}/session` | 查詢開套場次資訊 |

---

## 四、API 詳細規格

### 4.1 GET `/api/lottery/draw/{lotteryId}/tickets`

**Response**
```json
{
  "tickets": [
    { "id": "uuid", "ticketNumber": 1, "status": "AVAILABLE" },
    { "id": "uuid", "ticketNumber": 2, "status": "LOCKED" },
    {
      "id": "uuid",
      "ticketNumber": 3,
      "status": "DRAWN",
      "revealedNumber": 23,
      "prizeLevel": "A",
      "prizeName": "炭治郎公仔",
      "prizeImageUrl": "https://..."
    }
  ],
  "session": {
    "sessionId": "uuid",
    "isOpener": false,
    "openerNickname": null,
    "protectionDraws": 5,
    "protectionEndTime": "2026-02-26T10:35:00",
    "openerDrawCount": 2,
    "freeDrawEnabled": true,
    "status": "ACTIVE"
  }
}
```

- **未抽籤位（AVAILABLE / LOCKED）**：只有 `id`、`ticketNumber`、`status`，**不含**任何獎品欄位或 `revealedNumber`。
- **已抽籤位（DRAWN）**：包含 `revealedNumber`（刮刮樂）+ 獎品欄位。
- RANDOM 模式已抽籤位的 `revealedNumber` 為 `null`。
- `session` 在使用者未登入時為 `null`。

---

### 4.2 POST `/api/lottery/draw/{lotteryId}/draw`

**Request Body**
```json
{
  "count": 1,
  "ticket": ["<lottery_ticket.id 的 UUID>"]
}
```

| 欄位 | 類型 | 必填 | 說明 |
|------|------|------|------|
| `count` | int | ✅ | 本次抽獎張數，1–10 |
| `ticket` | string[] | ❌ | 指定票券的 UUID 列表；省略則隨機抽；長度必須等於 count，不可重複 |

> `ticket` 的值是 `lottery_ticket.id`（UUID），**不是** `ticketNumber`（序號）。

**Response（正常情況）**：永遠是陣列 `DrawResult[]`（即使只抽 1 張）
```json
[
  {
    "success": true,
    "ticketId": "uuid",
    "ticketNumber": 45,
    "revealedNumber": 23,
    "prizeId": "uuid",
    "prizeLevel": "A",
    "prizeName": "炭治郎公仔（大）",
    "prizeImageUrl": "https://...",
    "isGrandPrize": true,
    "triggeredFreeDraw": true,
    "refundAmount": 1500,
    "message": "恭喜中大獎！開套免單，退還 1500 元！"
  }
]
```

| 欄位 | 類型 | 說明 |
|------|------|------|
| `success` | boolean | 是否成功抽獎 |
| `ticketId` | string | 票券 UUID |
| `ticketNumber` | int | 實體卡物理編號（印在卡上）|
| `revealedNumber` | int? | 刮刮樂：刮開後的號碼；RANDOM 模式為 `null` |
| `prizeId` | string? | 獎品 ID（謝謝惠顧時為 null）|
| `prizeLevel` | string? | 獎品等級（A/B/C…）|
| `prizeName` | string? | 獎品名稱 |
| `prizeImageUrl` | string? | 獎品圖片 URL |
| `isGrandPrize` | boolean | 是否為大獎 |
| `triggeredFreeDraw` | boolean | 是否觸發開套免單 |
| `refundAmount` | long | 退款金額（0 = 不退款）|
| `message` | string | 給玩家看的訊息 |

**Response（SCRATCH_PLAYER 開套者尚未指定大獎時）**
```json
{
  "designationRequired": true,
  "message": "請先指定大獎位置",
  "availableNumbers": [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
}
```
> 前端收到 `designationRequired: true` 時，應引導使用者進入指定大獎流程，**不顯示**抽獎結果。

**驗證錯誤（HTTP 400）**：字串純文字，例如：
- `"count 必須至少為 1"`
- `"ticket 列表的長度必須等於 count"`
- `"ticket 列表不可包含重複項目"`
- `"ticket 列表必須包含有效的 UUID 格式"`

---

### 4.3 POST `/api/lottery/draw/{lotteryId}/designate`

**Request Body**
```json
{
  "designations": [
    { "revealedNumber": 15, "prizeId": "uuid-of-prize" },
    { "revealedNumber": 32, "prizeId": "uuid-of-prize" }
  ]
}
```

| 欄位 | 類型 | 說明 |
|------|------|------|
| `designations[].revealedNumber` | int | 要設為大獎的 revealedNumber（來自 `/draw` response 的 `availableNumbers`）|
| `designations[].prizeId` | string | 對應的獎品 ID（從商品 API 取得）|

**Response**：HTTP 200，無 body。

---

### 4.4 GET `/api/lottery/draw/{lotteryId}/session`

**Response**
```json
{
  "sessionId": "uuid",
  "isOpener": true,
  "openerNickname": null,
  "protectionDraws": 5,
  "protectionEndTime": "2026-02-26T10:35:00",
  "openerDrawCount": 2,
  "freeDrawEnabled": true,
  "status": "ACTIVE"
}
```

| 欄位 | 類型 | 說明 |
|------|------|------|
| `sessionId` | string | 場次 UUID |
| `isOpener` | boolean | 當前使用者是否為開套者 |
| `openerNickname` | string? | 開套者暱稱（目前後端回傳 null）|
| `protectionDraws` | int | 開套保護抽數（0 = 無保護）|
| `protectionEndTime` | string? | 保護結束時間（ISO 8601），null = 無保護期 |
| `openerDrawCount` | int | 開套者已抽次數 |
| `freeDrawEnabled` | boolean | 是否啟用開套免單 |
| `status` | string | `ACTIVE` / `COMPLETED` / `EXPIRED` |

---

## 五、刮刮樂動畫觸發時序

```
① 玩家點擊卡片（ticketNumber = 5，對應 UUID = "abc-123"）
② 前端 → POST /api/lottery/draw/{id}/draw
         { "count": 1, "ticket": ["abc-123"] }
③ 後端回傳（陣列）：
   [
     { "ticketNumber": 5, "revealedNumber": 23, "prizeLevel": "A", "prizeName": "炭治郎公仔" }
   ]
④ 前端：立刻儲存 response，開始播刮開動畫（純視覺，約 1-2 秒）
⑤ 動畫結束 → 顯示 revealedNumber = 23（大字）
⑥ 顯示 prizeLevel = A / prizeName = 炭治郎公仔（或謝謝惠顧）
```

> ⚠️ `revealedNumber` 必須從後端 response 取得；前端**禁止**自行推算。  
> ⚠️ 動畫播完前不得顯示任何獎品資訊（避免截圖跳過動畫作弊）。

---

## 六、各模式完整流程

### 6.1 RANDOM 模式（一番賞 / 扭蛋）

```
① GET /tickets                    → 取得所有籤位（AVAILABLE/DRAWN/LOCKED）
② 玩家選擇一個籤位（顯示 ticketNumber，POST 傳 UUID）
③ POST /draw { count: 1, ticket: ["uuid"] }
⑤ Response 陣列，取第 0 筆 → 顯示獎品（revealedNumber 為 null）
⑥ 更新籤位狀態為 DRAWN
⑦ 若 triggeredFreeDraw = true → 顯示「免單！退還 {refundAmount} 元」彈窗
```

### 6.2 SCRATCH_STORE 模式（刮刮樂 - 店家指定）

```
① GET /tickets                    → 取得所有籤位（AVAILABLE/DRAWN/LOCKED）
② GET /session                    → 取得場次資訊（保護時間等）
③ 玩家選擇一張卡片（顯示 ticketNumber，POST 傳 UUID）
④ POST /draw { count: 1, ticket: ["uuid"] }
⑤ 播刮開動畫 → 顯示 revealedNumber → 顯示獎品
⑥ 更新籤位狀態為 DRAWN
```

### 6.3 SCRATCH_PLAYER 模式（刮刮樂 - 玩家指定）

```
── 開套者流程 ──

① GET /tickets + GET /session
② POST /draw → 後端回傳 { designationRequired: true, availableNumbers: [1..N] }
③ 前端偵測 designationRequired = true → 顯示「選擇大獎號碼」介面
   - 顯示可用票券列表，讓玩家勾選想設為大獎的票券
   - 同時顯示獎品列表（從商品 API 取得 prizeId）
④ 玩家確認選擇
⑤ POST /designate { designations: [{ revealedNumber: 15, prizeId: "..." }, ...] }
⑥ 成功後重新 POST /draw 進入正常抽獎流程

── 後續玩家流程 ──

① POST /draw → 直接返回 DrawResult（開套者已完成指定）
```

> `designations` 中的 `revealedNumber` 是 `availableNumbers` 的值（1–N 範圍的刮開號碼）  
> `prizeId` 從商品的獎品列表 API 取得

---

## 七、保護時間提示（前端）

```
session.protectionEndTime 存在 → 顯示倒數計時
倒數歸零 → 自動重新 GET /tickets（後端此時 session 已 EXPIRED，其他玩家可抽）
isOpener = true  → 顯示「你是開套玩家」提示 + 免單剩餘次數
isOpener = false + 保護中 → 顯示「等待開套玩家完成保護抽」提示
```

---

## 八、點擊後的 UI 流程

```
RANDOM 模式：
玩家點擊籤位
  → 呼叫 /draw（ticket: [uuid]）
  → 收到 Response（陣列）
  → 直接顯示獎品（prizeLevel + prizeName + prizeImageUrl）
  → 更新該籤位狀態為 DRAWN

SCRATCH 模式：
玩家點擊籤位
  → 呼叫 /draw（ticket: [uuid]）
  → 收到 Response（含 revealedNumber）← 此時已拿到所有資料
  → 播刮卡動畫（約 1.5 秒，純視覺效果）
  → 動畫結束 → 顯示 revealedNumber（大字數字）
  → 顯示 prizeLevel / prizeName（或「謝謝惠顧」）
  → 更新該籤位狀態為 DRAWN
```

---

## 九、免單彈窗邏輯

```javascript
// DrawResult 陣列回來後
results.forEach(result => {
  if (result.triggeredFreeDraw && result.refundAmount > 0) {
    showModal({
      title: '恭喜！開套免單',
      message: `已退還 ${result.refundAmount} 元至你的帳戶`,
    });
  }
});
```

---

## 十、注意事項

1. **票券 UUID vs ticketNumber**
   - 前端**顯示**用 `ticketNumber`（印在卡上的序號）
   - 前端**POST 抽獎**用 `id`（`lottery_ticket.id`，UUID）
   - 不要把兩者混用

2. **draw response 永遠是陣列**
   - 即使只抽 1 張，response 也是 `[DrawResult]`，取 `[0]` 使用

3. **designationRequired 偵測**
   ```javascript
   if (response.designationRequired === true) {
     // 引導開套者指定大獎流程
   } else {
     // 正常處理 DrawResult[]
   }
   ```

4. **gameMode 來源**
   - 從商品詳情 / 瀏覽 API 取得，不從 `/tickets` 或 `/session` 取得

5. **revealedNumber 安全**
   - 未抽籤位的 `revealedNumber` 後端**不會傳送**，前端不可自行推算
