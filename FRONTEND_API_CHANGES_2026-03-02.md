# 前端 API 異動通知（2026-03-02）

## 異動摘要

本次更新涉及 **三大改動**：

1. **刮刮樂大獎號碼公開** — 指定大獎後，所有玩家都能看到哪些 `revealedNumber` 是大獎
2. **保護時間延遲啟動** — 保護時間改為「首次抽獎」時才啟動，查看不會觸發
3. **扭蛋 synchronized** — 扭蛋抽獎使用 synchronized 確保同時只有一個請求，無保護時間

---

## 一、刮刮樂大獎號碼公開

### 新增欄位：`designatedWinningNumbers`

#### 影響的 API：

### 1. `GET /api/lottery/draw/{lotteryId}/tickets` — 取得籤位列表

**回應結構變化** — `TicketListResponse` 新增 `designatedWinningNumbers` 欄位：

```json
{
  "success": true,
  "data": {
    "tickets": [ ... ],
    "session": { ... },
    "designatedWinningNumbers": [
      {
        "revealedNumber": 5,
        "prizeId": "uuid-prize-1",
        "prizeName": "A賞 限定公仔",
        "prizeLevel": "GRAND",
        "prizeImageUrl": "https://..."
      },
      {
        "revealedNumber": 12,
        "prizeId": "uuid-prize-1",
        "prizeName": "A賞 限定公仔",
        "prizeLevel": "GRAND",
        "prizeImageUrl": "https://..."
      }
    ]
  }
}
```

**欄位說明：**

| 欄位 | 類型 | 說明 |
|------|------|------|
| `revealedNumber` | Integer | 中獎號碼（刮開後會顯示的數字） |
| `prizeId` | String | 對應的獎品 ID |
| `prizeName` | String | 獎品名稱 |
| `prizeLevel` | String | 獎品等級（如 GRAND） |
| `prizeImageUrl` | String | 獎品圖片 URL |

**顯示邏輯：**
- 刮刮樂模式（`SCRATCH_MODE` / `SCRATCH_CARD_MODE`）：若陣列不為空，前端應顯示「🏆 中獎號碼：5, 12」
- 一番賞/扭蛋/卡牌：此陣列通常為空 `[]`
- 同一個 `prizeId` 可出現多次（大獎數量 > 1 時，會分配到不同的 `revealedNumber`）

**重要：**
- 每張票的 `revealedNumber` 仍然隱藏（只有 DRAWN 狀態才會顯示）
- 玩家只知道「中獎號碼是 5 和 12」，但不知道自己刮的那張是幾號，刮開後才揭曉

---

### 2. `POST /api/lottery/draw/{lotteryId}/designate` — 指定大獎位置

**回應結構變化** — 原本返回 `void`，現在返回 `DesignateResponse`：

```json
// 舊版回應：（空 body）
// 新版回應：
{
  "success": true,
  "data": {
    "success": true,
    "message": "大獎位置指定完成，共 2 個",
    "designatedWinningNumbers": [
      {
        "revealedNumber": 5,
        "prizeId": "uuid-prize-1",
        "prizeName": "A賞 限定公仔",
        "prizeLevel": "GRAND",
        "prizeImageUrl": "https://..."
      },
      {
        "revealedNumber": 12,
        "prizeId": "uuid-prize-1",
        "prizeName": "A賞 限定公仔",
        "prizeLevel": "GRAND",
        "prizeImageUrl": "https://..."
      }
    ]
  }
}
```

**前端行為：**
- 指定完成後，直接使用回應中的 `designatedWinningNumbers` 更新 UI
- 不需再額外呼叫 `/tickets` API

---

### 指定大獎的完整流程

```
玩家開套 → 抽獎 → 系統攔截回傳 designationRequired: true
                    ↓
            前端顯示指定 UI：
            - 可選號碼：availableNumbers = [1,2,3,...,20]
            - 大獎清單：grandPrizes = [{ prizeId, prizeName, quantity: 2 }]
            - 玩家將 2 個號碼分配給大獎
                    ↓
            POST /designate { designations: [{revealedNumber: 5, prizeId: "..."}, {revealedNumber: 12, prizeId: "..."}] }
                    ↓
            回應包含 designatedWinningNumbers
                    ↓
            前端顯示：「🏆 中獎號碼：5, 12」
                    ↓
            玩家繼續抽獎
```

**指定請求格式（不變）：**
```json
POST /api/lottery/draw/{lotteryId}/designate
{
  "designations": [
    { "revealedNumber": 5, "prizeId": "uuid-prize-grand" },
    { "revealedNumber": 12, "prizeId": "uuid-prize-grand" }
  ]
}
```

> ⚠️ 注意：`revealedNumber` 不是 `ticketNumber`！
> - `ticketNumber` = 物理格子位置（玩家看到的序號 1~N）
> - `revealedNumber` = 刮開後顯示的號碼（會隨機打亂）

---

## 二、保護時間延遲啟動

### 核心改動

| 項目 | 舊版行為 | 新版行為 |
|------|----------|----------|
| 保護時間觸發點 | 查看籤位時建立 session，**立即啟動保護** | 查看不建立 session；**首次抽獎時才啟動保護** |
| `getTickets` 行為 | 呼叫 `getOrCreateSession`（建立 session + 保護） | 呼叫 `getActiveSession`（唯讀，不建立） |
| session 欄位 | `protectionEndTime` 一開始就有值 | 首次抽獎前 `protectionEndTime = null` |

### 影響的 API：

### 1. `GET /api/lottery/draw/{lotteryId}/tickets`

**session 欄位可能為 null：**
```json
{
  "tickets": [...],
  "session": null,  // 🆕 如果沒人抽獎，session 為 null
  "designatedWinningNumbers": [...]
}
```

**前端處理：**
```javascript
const { tickets, session, designatedWinningNumbers } = response.data;

if (session === null) {
  // 尚無人開套，不需顯示保護時間倒數
} else if (session.protectionEndTime === null) {
  // Session 存在但保護未啟動（可能是 SCRATCH_PLAYER 指定階段）
} else {
  // 保護時間已啟動
  if (!session.isOpener) {
    // 顯示「其他玩家正在遊玩，剩餘 XX 秒」
    showProtectionCountdown(session.protectionEndTime);
  }
}
```

### 2. `POST /api/lottery/draw/{lotteryId}/draw` — 抽獎回應

**回應結構變化** — `DrawBatchResponse` 新增 `protectionEndTime` 欄位：

```json
{
  "success": true,
  "data": {
    "playMode": "SCRATCH_MODE",
    "gameMode": "SCRATCH_STORE",
    "results": [
      {
        "success": true,
        "ticketId": "uuid-ticket",
        "ticketNumber": 3,
        "revealedNumber": 7,
        "prizeId": "uuid-prize",
        "prizeLevel": "B",
        "prizeName": "B賞 模型",
        "prizeImageUrl": "https://...",
        "isGrandPrize": false,
        "triggeredFreeDraw": false,
        "refundAmount": 0,
        "message": "抽獎成功！恭喜獲得 B賞 模型"
      }
    ],
    "protectionEndTime": "2026-03-02T01:55:00"
  }
}
```

**欄位說明：**

| 欄位 | 類型 | 說明 |
|------|------|------|
| `protectionEndTime` | String (ISO DateTime) | 保護結束時間。**首次抽獎時設定，後續抽獎也會回傳**。扭蛋(GACHA)為 `null`。 |

**前端處理：**
```javascript
const { results, protectionEndTime } = response.data;

// 顯示抽獎結果
showDrawResults(results);

// 🆕 首次抽獎會收到 protectionEndTime，開始倒數
if (protectionEndTime) {
  startProtectionCountdown(protectionEndTime);
}
```

### 3. `GET /api/lottery/draw/{lotteryId}/session` — 場次查詢

**變化：**
- 改為唯讀查詢（不會建立新 session）
- 若無進行中場次，回傳 `null`（body 為 null）

```json
// 有場次時：
{
  "success": true,
  "data": {
    "sessionId": "uuid-session",
    "isOpener": true,
    "protectionEndTime": "2026-03-02T01:55:00",  // 可能為 null（保護未啟動）
    "openerDrawCount": 3,
    ...
  }
}

// 無場次時：
{
  "success": true,
  "data": null
}
```

---

## 三、扭蛋 synchronized

### 核心規則

| 類別 | 保護時間 | synchronized | 說明 |
|------|----------|--------------|------|
| GACHA（扭蛋） | ❌ 無 | ✅ 有 | 同一商品同時只處理一個抽獎請求 |
| OFFICIAL_ICHIBAN（一番賞） | ✅ 有 | ❌ 無 | 首次抽獎啟動保護，保護期內其他玩家不能抽 |
| TRADING_CARD（卡牌） | ✅ 有 | ❌ 無 | 同上 |
| CUSTOM_GACHA + SCRATCH_MODE（刮刮樂） | ✅ 有 | ❌ 無 | 同上 |

### 前端影響

- **扭蛋**：`protectionEndTime` 永遠為 `null`，不需顯示保護時間倒數
- **扭蛋**：如果請求返回慢，可能是在等待鎖，前端可顯示 loading
- **其他類別**：首次抽獎回應中會帶 `protectionEndTime`，前端顯示倒數；保護期間其他玩家抽獎會被拒絕（回傳 `success: false, message: "商品正在被其他玩家抽獎中，請稍後再試"`）

---

## 四、完整 API 回應範例

### 刮刮樂（SCRATCH_STORE 模式）完整流程

#### Step 1: 查看籤位
```
GET /api/lottery/draw/{lotteryId}/tickets
```
```json
{
  "tickets": [
    { "id": "uuid-1", "ticketNumber": 1, "status": "AVAILABLE" },
    { "id": "uuid-2", "ticketNumber": 2, "status": "AVAILABLE" },
    ...
  ],
  "session": null,
  "designatedWinningNumbers": [
    { "revealedNumber": 5, "prizeId": "...", "prizeName": "A賞 限定公仔", "prizeLevel": "GRAND", "prizeImageUrl": "..." },
    { "revealedNumber": 12, "prizeId": "...", "prizeName": "A賞 限定公仔", "prizeLevel": "GRAND", "prizeImageUrl": "..." }
  ]
}
```
前端顯示：「🏆 中獎號碼：5, 12 → 刮中這些號碼即可獲得 A賞 限定公仔！」

#### Step 2: 抽獎（刮卡）
```
POST /api/lottery/draw/{lotteryId}/draw
{ "count": 1, "ticket": ["uuid-1"] }
```
```json
{
  "playMode": "SCRATCH_MODE",
  "gameMode": "SCRATCH_STORE",
  "results": [{
    "success": true,
    "ticketId": "uuid-1",
    "ticketNumber": 1,
    "revealedNumber": 7,
    "prizeLevel": "B",
    "prizeName": "B賞 模型",
    "isGrandPrize": false,
    ...
  }],
  "protectionEndTime": "2026-03-02T02:00:00"
}
```
前端：刮卡動畫揭露 `revealedNumber = 7`，不在中獎號碼裡，顯示 B 賞。同時開始保護時間倒數。

#### Step 3: 繼續抽（刮中大獎！）
```json
{
  "results": [{
    "success": true,
    "revealedNumber": 5,
    "prizeLevel": "GRAND",
    "prizeName": "A賞 限定公仔",
    "isGrandPrize": true,
    ...
  }],
  "protectionEndTime": "2026-03-02T02:00:00"
}
```
前端：🎉 刮卡揭露 `revealedNumber = 5`，**命中中獎號碼！** 顯示大獎動畫。

---

### 刮刮樂（SCRATCH_PLAYER 模式）完整流程

#### Step 1: 查看籤位
```
GET /api/lottery/draw/{lotteryId}/tickets
```
```json
{
  "tickets": [...],
  "session": null,
  "designatedWinningNumbers": []  // 尚未指定
}
```

#### Step 2: 嘗試抽獎 → 被攔截要求指定
```
POST /api/lottery/draw/{lotteryId}/draw
{ "count": 1 }
```
```json
{
  "designationRequired": true,
  "message": "請先指定大獎位置（共需指定 2 個號碼）",
  "availableNumbers": [1, 2, 3, ..., 20],
  "grandPrizes": [
    { "prizeId": "uuid-grand", "prizeName": "A賞", "prizeLevel": "GRAND", "quantity": 2, "prizeImageUrl": "..." }
  ]
}
```
前端：顯示指定 UI，讓玩家選 2 個號碼（因為 quantity=2）

#### Step 3: 玩家指定大獎號碼
```
POST /api/lottery/draw/{lotteryId}/designate
{
  "designations": [
    { "revealedNumber": 5, "prizeId": "uuid-grand" },
    { "revealedNumber": 12, "prizeId": "uuid-grand" }
  ]
}
```
```json
{
  "success": true,
  "message": "大獎位置指定完成，共 2 個",
  "designatedWinningNumbers": [
    { "revealedNumber": 5, "prizeId": "uuid-grand", "prizeName": "A賞", ... },
    { "revealedNumber": 12, "prizeId": "uuid-grand", "prizeName": "A賞", ... }
  ]
}
```
前端：更新 UI 顯示「🏆 中獎號碼：5, 12」

#### Step 4: 繼續抽獎
回應同 SCRATCH_STORE 模式。

---

## 五、前端需要修改的地方

### 1. 籤位列表頁面
- [ ] 處理 `session` 可能為 `null` 的情況
- [ ] 處理 `session.protectionEndTime` 可能為 `null` 的情況
- [ ] 顯示 `designatedWinningNumbers`（中獎號碼公告）
- [ ] 同一 `prizeId` 可能有多個不同 `revealedNumber`，用逗號合併顯示

### 2. 抽獎結果頁面
- [ ] 從 `DrawBatchResponse` 讀取 `protectionEndTime` 並顯示倒數計時
- [ ] 扭蛋模式下 `protectionEndTime` 為 `null`，不顯示倒數

### 3. 指定大獎頁面（刮刮樂專用）
- [ ] 指定完成後，讀取回應中的 `designatedWinningNumbers` 更新 UI
- [ ] 不再需要額外呼叫 `/tickets` 重新取得資料

### 4. 場次/Session 相關
- [ ] `GET /session` 回傳可能為 `null`（body），前端需判斷
- [ ] `protectionEndTime` 可能為 `null`（保護未啟動或扭蛋模式）

### 5. 錯誤處理
- [ ] 保護時間內其他玩家抽獎會收到：`{ "success": false, "message": "商品正在被其他玩家抽獎中，請稍後再試" }`
- [ ] 前端可顯示友善提示 + 倒數計時（如果有 session 的 protectionEndTime）

---

## 六、TypeScript 型別參考

```typescript
/** 籤位列表回應 */
interface TicketListResponse {
  tickets: LotteryTicketRes[];
  session: SessionResponse | null;
  designatedWinningNumbers: DesignatedWinningNumber[];  // 🆕
}

/** 已指定的大獎中獎號碼 */
interface DesignatedWinningNumber {
  revealedNumber: number;
  prizeId: string;
  prizeName: string;
  prizeLevel: string;
  prizeImageUrl: string | null;
}

/** 抽獎回應 */
interface DrawBatchResponse {
  playMode: string;
  gameMode: string;
  results: DrawResult[];
  protectionEndTime: string | null;  // 🆕 ISO DateTime 或 null
}

/** 指定大獎回應 */
interface DesignateResponse {
  success: boolean;
  message: string;
  designatedWinningNumbers: DesignatedWinningNumber[];  // 🆕
}

/** 指定大獎要求回應（攔截抽獎時返回） */
interface DesignationRequiredResponse {
  designationRequired: true;
  message: string;
  availableNumbers: number[];
  grandPrizes: GrandPrizeInfo[];
}

/** 大獎資訊 */
interface GrandPrizeInfo {
  prizeId: string;
  prizeName: string;
  prizeLevel: string;
  quantity: number;  // 此獎品需要指定幾個 revealedNumber
  prizeImageUrl: string | null;
}

/** 場次資訊 */
interface SessionResponse {
  sessionId: string;
  isOpener: boolean;
  openerNickname: string | null;
  protectionDraws: number;
  protectionEndTime: string | null;  // 🆕 可能為 null（保護未啟動）
  openerDrawCount: number;
  freeDrawEnabled: boolean;
  status: string;
}
```

---

## 七、常見問題

### Q: 為什麼 `protectionEndTime` 有時候是 `null`？
A: 兩種情況：
1. **扭蛋模式** — 扭蛋不使用保護時間，永遠為 `null`
2. **保護未啟動** — Session 已建立（如 SCRATCH_PLAYER 指定階段），但尚未有人抽獎

### Q: 查看籤位時 `session` 為 `null` 是正常的嗎？
A: 是的。新版改為查看不會建立 session，只有抽獎時才會建立。`session = null` 表示目前沒有人在玩。

### Q: 大獎可以有多個嗎？
A: 可以。例如一個大獎 `quantity = 2`，會被指定到兩個不同的 `revealedNumber`。前端會在 `designatedWinningNumbers` 中看到兩筆記錄（`prizeId` 相同但 `revealedNumber` 不同）。

### Q: 我怎麼判斷抽獎回應是「正常結果」還是「需要指定大獎」？
A: 檢查回應是否有 `designationRequired` 欄位：
```javascript
if (response.data.designationRequired) {
  // 需要指定大獎 → 顯示指定 UI
} else {
  // 正常抽獎結果 → 顯示結果
}
```
