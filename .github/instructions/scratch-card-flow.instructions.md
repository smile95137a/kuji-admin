---
description: "Use when working on scratch card lottery, SCRATCH_STORE/SCRATCH_PLAYER modes, grand prize designation, or ticket number mechanics."
---

# 🎴 刮刮樂完整遊玩流程 API 指南

## 📋 目錄
- [遊戲模式說明](#遊戲模式說明)
- [完整流程圖](#完整流程圖)
- [API 調用順序](#api-調用順序)
- [情境 1：一般玩家（跟別人的套）](#情境-1一般玩家跟別人的套)
- [情境 2：開套玩家（自己開新套）](#情境-2開套玩家自己開新套)
- [情境 3：店家指定大獎模式](#情境-3店家指定大獎模式)
- [錯誤處理](#錯誤處理)

---

## 🎮 遊戲模式說明

系統支援兩種刮刮樂模式：

| 模式 | playMode 值 | 大獎指定者 | 適用場景 |
|------|------------|-----------|---------|
| **店家指定** | `SCRATCH_STORE` | 店家 | 固定大獎位置，玩家不知道 |
| **玩家指定** | `SCRATCH_PLAYER` | 開套玩家 | 開套者可選擇大獎位置，增加策略性 |

---

## 📊 完整流程圖

```
前端啟動
    ↓
① 查詢刮刮樂列表
    ↓
② 查詢單一刮刮樂詳情（確認 playMode）
    ↓
③ 查詢籤位列表（取得可選號碼）
    ↓
【分支】
    ├── 情境 A：跟別人的套 → 直接到 ⑦ 執行抽獎
    │
    └── 情境 B：開新套（我是開套者）
            ↓
        ④ 取得場次資訊（確認是否為開套者）
            ↓
        ⑤ 檢查是否需要指定大獎（playMode=SCRATCH_PLAYER 時）
            ↓
        ⑥ 指定大獎位置（選擇 3 個號碼）
            ↓
        ⑦ 執行抽獎
            ↓
        ⑧ 查看獎品結果
            ↓
        ⑨ 重複抽獎或結束
```

---

## 🔢 API 調用順序

### Base URL
```
http://localhost:8080/api
```

### 認證
所有前台 API 都需要 JWT Token：
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

---

## ① 查詢刮刮樂列表

### Request
```http
GET /api/lottery/list?category=SCRATCH_CARD
Authorization: Bearer {token}
```

### Response
```json
{
  "success": true,
  "data": [
    {
      "id": "lottery-uuid-001",
      "title": "神秘寶藏刮刮樂",
      "imageUrl": "https://...",
      "category": "SCRATCH_CARD",
      "playMode": "SCRATCH_PLAYER",  // 🔑 關鍵：判斷是否需要玩家指定
      "pricePerDraw": 50,
      "totalDraws": 100,
      "remainingDraws": 85,
      "status": "ON_SHELF"
    },
    {
      "id": "lottery-uuid-002",
      "title": "幸運777刮刮樂",
      "playMode": "SCRATCH_STORE",  // 店家已指定，玩家不用選
      "pricePerDraw": 80,
      "remainingDraws": 92
    }
  ]
}
```

**前端判斷邏輯：**
- `playMode = "SCRATCH_PLAYER"` → 需要玩家指定大獎（如果是開套者）
- `playMode = "SCRATCH_STORE"` → 店家已指定，直接抽

---

## ② 查詢單一刮刮樂詳情

### Request
```http
GET /api/lottery/{lotteryId}
Authorization: Bearer {token}
```

### Response
```json
{
  "success": true,
  "data": {
    "id": "lottery-uuid-001",
    "title": "神秘寶藏刮刮樂",
    "description": "100張刮刮樂，3張大獎由開套者指定",
    "imageUrl": "https://...",
    "category": "SCRATCH_CARD",
    "playMode": "SCRATCH_PLAYER",
    "pricePerDraw": 50,
    "totalDraws": 100,
    "remainingDraws": 85,
    "status": "ON_SHELF",
    "prizes": [
      {
        "id": "prize-uuid-A",
        "name": "iPhone 15 Pro",
        "level": "A",
        "quantity": 3,
        "remaining": 3,
        "isGrandPrize": true
      },
      {
        "name": "AirPods Pro",
        "level": "B",
        "quantity": 10,
        "remaining": 8
      },
      {
        "name": "Apple Watch",
        "level": "C",
        "quantity": 20,
        "remaining": 18
      }
    ]
  }
}
```

**前端顯示：**
- 商品資訊、獎品清單
- 如果 `playMode = "SCRATCH_PLAYER"`，提示「開套者可指定大獎位置」

---

## ③ 查詢籤位列表

### Request
```http
GET /api/lottery/draw/{lotteryId}/tickets
Authorization: Bearer {token}
```

### Response
```json
{
  "success": true,
  "data": {
    "tickets": [
      {
        "id": "ticket-uuid-001",
        "number": 1,
        "status": "AVAILABLE",
        "prizeId": null,        // ⚠️ 未抽籤位不顯示獎品
        "prizeName": null,
        "prizeLevel": null
      },
      {
        "id": "ticket-uuid-002",
        "number": 2,
        "status": "AVAILABLE"
      },
      {
        "id": "ticket-uuid-010",
        "number": 10,
        "status": "DRAWN",
        "prizeId": "prize-uuid-C",
        "prizeName": "Apple Watch",  // ✅ 已抽籤位顯示獎品
        "prizeLevel": "C"
      }
    ],
    "session": {
      "sessionId": "session-uuid-001",
      "isOpener": false,  // 🔑 判斷是否為開套者
      "openerNickname": "玩家A",
      "protectionDraws": 10,
      "protectionEndTime": "2025-12-25T12:00:00",
      "openerDrawCount": 5,
      "freeDrawEnabled": false,
      "status": "ACTIVE"
    }
  }
}
```

**前端判斷邏輯：**
- `session.isOpener = true` → 我是開套者
  - 如果 `playMode = "SCRATCH_PLAYER"`，需要先指定大獎
- `session.isOpener = false` → 我不是開套者
  - 直接選號抽獎

**前端顯示：**
- 顯示 100 個籤位格子
- `status = "AVAILABLE"` → 可選（顯示為可點擊）
- `status = "DRAWN"` → 已抽（顯示獎品或銘謝惠顧）
- `status = "LOCKED"` → 被鎖定（灰色不可選）

---

## ④ 取得場次資訊

### Request
```http
GET /api/lottery/draw/{lotteryId}/session
Authorization: Bearer {token}
```

### Response
```json
{
  "success": true,
  "data": {
    "sessionId": "session-uuid-001",
    "isOpener": true,  // 🔑 我是開套者
    "openerNickname": null,
    "protectionDraws": 10,
    "protectionEndTime": "2025-12-25T12:00:00",
    "openerDrawCount": 0,  // 目前已抽 0 次
    "freeDrawEnabled": false,
    "status": "ACTIVE"
  }
}
```

**前端判斷：**
- `isOpener = true` + `openerDrawCount = 0` → 還沒開始抽，需要先指定大獎

---

## ⑤ 檢查是否需要指定大獎（自動檢查）

這個步驟會在執行抽獎時自動檢查，如果需要指定大獎，會返回特殊回應。

### Request（嘗試抽獎）
```http
POST /api/lottery/draw/{lotteryId}/draw
Authorization: Bearer {token}
Content-Type: application/json

{
  "count": 1,
  "ticket": ["ticket-uuid-010"]
}
```

### Response（需要指定大獎）
```json
{
  "success": true,
  "data": {
    "designationRequired": true,  // 🔑 需要先指定
    "message": "請先指定大獎位置",
    "availableNumbers": [1, 2, 3, 4, 5, ..., 100]  // 可選號碼列表
  }
}
```

**前端處理：**
1. 彈出大獎指定 UI
2. 讓玩家選擇 3 個號碼（假設有 3 個大獎）
3. 呼叫 ⑥ 指定大獎 API

---

## ⑥ 指定大獎位置

### Request
```http
POST /api/lottery/draw/{lotteryId}/designate
Authorization: Bearer {token}
Content-Type: application/json

{
  "prizeNumbers": [7, 33, 88]  // 玩家選擇的 3 個號碼
}
```

### Response
```json
{
  "success": true,
  "data": null
}
```

**前端提示：**
- "大獎位置已設定，開始抽獎吧！"
- 關閉指定 UI，回到籤位列表

---

## ⑦ 執行抽獎

### 模式 A：指定籤位抽獎（推薦）
```http
POST /api/lottery/draw/{lotteryId}/draw
Authorization: Bearer {token}
Content-Type: application/json

{
  "count": 3,  // 抽 3 次
  "ticket": [
    "ticket-uuid-010",
    "ticket-uuid-025",
    "ticket-uuid-077"
  ]
}
```

### 模式 B：隨機抽獎
```http
POST /api/lottery/draw/{lotteryId}/draw
Authorization: Bearer {token}
Content-Type: application/json

{
  "count": 5  // 隨機抽 5 次，不指定號碼
}
```

### Response（成功）
```json
{
  "success": true,
  "data": [
    {
      "success": true,
      "message": "恭喜中獎！",
      "ticketId": "ticket-uuid-010",
      "ticketNumber": 10,
      "prizeId": "prize-uuid-C",
      "prizeName": "Apple Watch",
      "prizeLevel": "C",
      "prizeImageUrl": "https://...",
      "isGrandPrize": false,
      "isLastPrize": false,
      "costType": "GOLD",
      "costAmount": 50,
      "bonusRefunded": 0,
      "drawTime": "2025-12-25T14:30:00",
      "canWithdraw": true
    },
    {
      "success": true,
      "message": "🎊 中大獎了！",
      "ticketNumber": 25,
      "prizeName": "iPhone 15 Pro",
      "prizeLevel": "A",
      "isGrandPrize": true,  // 🎉 大獎
      "costAmount": 50
    },
    {
      "success": true,
      "message": "銘謝惠顧",
      "ticketNumber": 77,
      "prizeId": null,
      "prizeName": null,
      "costAmount": 50
    }
  ]
}
```

**前端處理：**
1. 播放刮開動畫
2. 顯示獎品資訊（或銘謝惠顧）
3. 如果 `isGrandPrize = true`，播放特效
4. 更新籤位列表（標記為已抽）
5. 更新餘額

---

## ⑧ 查看獎品結果（抽獎後）

抽獎結果會在 ⑦ 的回應中直接返回，不需要額外 API。

**前端顯示內容：**
- 獎品圖片、名稱、等級
- 花費金額
- 是否為大獎/最後賞
- 是否可提領（實體獎品）

---

## ⑨ 繼續抽或結束

### 繼續抽
重複執行 ③ → ⑦ 的流程

### 查看抽獎紀錄（選用）
```http
GET /api/user/draw-history?lotteryId={lotteryId}
Authorization: Bearer {token}
```

### Response
```json
{
  "success": true,
  "data": [
    {
      "lotteryTitle": "神秘寶藏刮刮樂",
      "prizeName": "iPhone 15 Pro",
      "prizeLevel": "A",
      "isGrandPrize": true,
      "drawTime": "2025-12-25T14:30:00",
      "costAmount": 50
    }
  ]
}
```

---

## 🎬 完整情境演練

### 情境 1：一般玩家（跟別人的套）

#### 步驟 1：進入商品頁
```http
GET /api/lottery/lottery-uuid-001
```
→ 看到 `playMode = "SCRATCH_PLAYER"`，顯示「開套者可指定大獎」

#### 步驟 2：查看籤位
```http
GET /api/lottery/draw/lottery-uuid-001/tickets
```
→ 看到 `session.isOpener = false`，我不是開套者
→ 顯示 100 個籤位，15 個已被抽（灰色），85 個可選

#### 步驟 3：選擇 3 個號碼抽獎
```http
POST /api/lottery/draw/lottery-uuid-001/draw
{
  "count": 3,
  "ticket": ["ticket-uuid-010", "ticket-uuid-020", "ticket-uuid-030"]
}
```
→ 直接返回抽獎結果（不需要指定大獎）

#### 步驟 4：查看結果
```json
{
  "success": true,
  "data": [
    { "prizeName": "AirPods Pro", "prizeLevel": "B" },
    { "prizeName": null, "message": "銘謝惠顧" },
    { "prizeName": "Apple Watch", "prizeLevel": "C" }
  ]
}
```

---

### 情境 2：開套玩家（自己開新套）

#### 步驟 1：查看商品
```http
GET /api/lottery/lottery-uuid-002
```
→ `playMode = "SCRATCH_PLAYER"`，需要玩家指定

#### 步驟 2：查看籤位
```http
GET /api/lottery/draw/lottery-uuid-002/tickets
```
→ `session.isOpener = true`，我是開套者！
→ `openerDrawCount = 0`，還沒抽過

#### 步驟 3：嘗試抽獎（系統會攔截）
```http
POST /api/lottery/draw/lottery-uuid-002/draw
{
  "count": 1,
  "ticket": ["ticket-uuid-005"]
}
```

#### 步驟 4：系統要求指定大獎
```json
{
  "success": true,
  "data": {
    "designationRequired": true,
    "message": "請先指定大獎位置",
    "availableNumbers": [1, 2, 3, ..., 100]
  }
}
```

#### 步驟 5：指定大獎位置
前端彈出 UI，玩家選擇 [7, 33, 88]
```http
POST /api/lottery/draw/lottery-uuid-002/designate
{
  "prizeNumbers": [7, 33, 88]
}
```
→ 成功

#### 步驟 6：再次抽獎
```http
POST /api/lottery/draw/lottery-uuid-002/draw
{
  "count": 1,
  "ticket": ["ticket-uuid-005"]
}
```
→ 這次成功返回抽獎結果

#### 步驟 7：享受保護期
- 前 10 抽有保護期（其他玩家不能搶）
- 可以從容選號

---

### 情境 3：店家指定大獎模式

#### 步驟 1：查看商品
```http
GET /api/lottery/lottery-uuid-003
```
→ `playMode = "SCRATCH_STORE"`，店家已指定大獎

#### 步驟 2：查看籤位
```http
GET /api/lottery/draw/lottery-uuid-003/tickets
```
→ `session.isOpener = true` 或 `false` 都一樣
→ 不需要指定大獎

#### 步驟 3：直接抽獎
```http
POST /api/lottery/draw/lottery-uuid-003/draw
{
  "count": 5,
  "ticket": ["ticket-uuid-001", "ticket-uuid-002", ...]
}
```
→ 直接返回結果，不會被攔截

---

## ❌ 錯誤處理

### 錯誤 1：餘額不足
```json
{
  "success": false,
  "error": {
    "message": "餘額不足，請先儲值",
    "code": "INSUFFICIENT_BALANCE"
  }
}
```
→ 前端提示跳轉儲值頁

### 錯誤 2：籤位已被抽走
```json
{
  "success": true,
  "data": [
    {
      "success": false,
      "message": "籤位 #10 已被抽走",
      "ticketNumber": 10
    }
  ]
}
```
→ 前端提示「籤位被搶走了，請重新選擇」

### 錯誤 3：保護期內不能抽
```json
{
  "success": false,
  "error": {
    "message": "此套餐在保護期內（剩餘 5 抽），請稍後再試",
    "code": "PROTECTION_ACTIVE"
  }
}
```
→ 前端顯示保護期倒數計時

### 錯誤 4：未指定大獎就嘗試抽獎
（已在情境 2 示範，返回 `designationRequired: true`）

---

## 📝 前端開發建議

### 1. 狀態管理
```typescript
interface GameState {
  lotteryId: string;
  playMode: 'SCRATCH_PLAYER' | 'SCRATCH_STORE';
  isOpener: boolean;
  hasDesignated: boolean;  // 是否已指定大獎
  tickets: Ticket[];
  selectedTickets: string[];  // 已選籤位
}
```

### 2. 流程控制
```typescript
async function startGame(lotteryId: string) {
  // 1. 取得商品資訊
  const lottery = await getLottery(lotteryId);
  
  // 2. 取得籤位
  const { tickets, session } = await getTickets(lotteryId);
  
  // 3. 判斷是否需要指定大獎
  if (lottery.playMode === 'SCRATCH_PLAYER' && 
      session.isOpener && 
      session.openerDrawCount === 0) {
    // 顯示大獎指定 UI
    showDesignationUI();
  } else {
    // 直接顯示籤位選擇 UI
    showTicketSelection();
  }
}
```

### 3. 抽獎動畫
```typescript
async function executeDraw(tickets: string[]) {
  // 1. 發送請求
  const results = await drawTickets(lotteryId, tickets);
  
  // 2. 逐一播放刮開動畫
  for (const result of results) {
    await playScratchAnimation(result.ticketNumber);
    await showPrizeResult(result);
    await delay(1000);  // 每張間隔 1 秒
  }
  
  // 3. 更新籤位狀態
  refreshTickets();
}
```

---

## 🔄 API 快速參考表

| API | Method | 用途 | 必要性 |
|-----|--------|------|--------|
| `/api/lottery/list` | GET | 查詢商品列表 | ⭐⭐⭐ |
| `/api/lottery/{id}` | GET | 查詢商品詳情 | ⭐⭐⭐ |
| `/api/lottery/draw/{id}/tickets` | GET | 查詢籤位列表 | ⭐⭐⭐ |
| `/api/lottery/draw/{id}/session` | GET | 查詢場次資訊 | ⭐⭐ |
| `/api/lottery/draw/{id}/designate` | POST | 指定大獎位置 | ⭐⭐（SCRATCH_PLAYER 必要）|
| `/api/lottery/draw/{id}/draw` | POST | 執行抽獎 | ⭐⭐⭐ |
| `/api/user/draw-history` | GET | 查看抽獎紀錄 | ⭐ |

---

## 🎯 核心重點整理

1. **判斷 playMode**
   - `SCRATCH_PLAYER` → 需要玩家指定（如果是開套者）
   - `SCRATCH_STORE` → 店家已指定，直接抽

2. **判斷 isOpener**
   - `true` + `openerDrawCount = 0` → 需要先指定大獎
   - `false` → 跟別人的套，直接抽

3. **指定大獎流程**
   - 只有 `SCRATCH_PLAYER` 模式的開套者需要
   - 嘗試抽獎時系統會自動檢查並返回 `designationRequired: true`
   - 呼叫 `/designate` API 完成指定

4. **抽獎模式**
   - 指定籤位：傳 `ticket` 陣列（推薦）
   - 隨機抽獎：只傳 `count`

5. **安全機制**
   - 未抽籤位不會顯示獎品資訊
   - 開套者有保護期（前 10 抽）
   - 餘額不足自動攔截

---

## 📞 技術支援

如有疑問，請查看：
- 完整 API 規格：`API_DOCUMENTATION_COMPLETE.md`
- Controller 實作：`LotteryDrawController.java`
- Service 邏輯：`LotteryTicketService.java`

---

**最後更新：2026-02-11**
