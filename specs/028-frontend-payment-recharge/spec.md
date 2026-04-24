# 028 - 前台支付與儲值流程 Frontend Spec

## 版本資訊
- **版本**: 1.0.0
- **建立日期**: 2026-04-18
- **作者**: 系統規格
- **狀態**: Draft

---

## 背景與目標

本 spec 說明前台玩家的「錢包儲值」與「出貨費支付」兩大支付流程，以及刮刮樂遊戲板的正確渲染方式（格子位置修正）。

### 目前支付架構

| 支付類型 | 說明 | 金流 |
|---------|------|------|
| **抽獎扣點** | 從錢包扣除金幣/紅利 | 無第三方，純內部扣點 |
| **錢包儲值** | 購買方案 → 取得金幣+紅利 | ⚠️ 目前測試模式（直接完成，不走金流） |
| **出貨費支付** | 賞品出貨時支付運費 | GoMyPay（已整合） |

> ⚠️ **重要**：儲值目前是「測試模式」— `POST /api/recharge` 會直接完成並立即入帳，**不需要跳轉金流頁**。
> 未來串接真實金流後，流程會改為 PENDING → GoMyPay → Callback → COMPLETED。

---

## 一、錢包餘額顯示

### API
```
GET /api/user/me
Authorization: Bearer {token}
```

### Response 關鍵欄位
```json
{
  "success": true,
  "data": {
    "id": "user-uuid",
    "nickname": "玩家A",
    "goldCoins": 1500,       // 儲值金（可抽獎、不可提現）
    "bonusCoins": 200,       // 紅利金（可抽獎、部分操作限制）
    "totalRecharge": 3000    // 累計儲值金額（台幣）
  }
}
```

### 前端展示規則

| 幣種 | 顯示名稱 | 圖示建議 | 說明 |
|------|---------|---------|------|
| `goldCoins` | 金幣 | 🪙 | 儲值所得，主要抽獎貨幣 |
| `bonusCoins` | 紅利 | 💎 | 儲值贈送或活動發放，部分場景可用 |

---

## 二、查詢儲值方案

### API
```
GET /api/recharge-plans
（公開 API，不需要登入）
```

### Response
```json
{
  "success": true,
  "data": [
    {
      "id": "plan-uuid-001",
      "name": "入門方案",
      "description": "適合新手嘗試",
      "amount": 300,            // 台幣，用戶付這個金額
      "goldCoins": 300,         // 取得金幣
      "bonusCoins": 30,         // 贈送紅利
      "isActive": true,
      "isPromotional": false,
      "displayOrder": 1,
      "startTime": null,        // null = 永遠有效
      "endTime": null,
      "isInPeriod": true,       // 是否在活動期間
      "bonusPercentage": "贈送 10%"
    },
    {
      "id": "plan-uuid-002",
      "name": "超值方案",
      "amount": 1000,
      "goldCoins": 1000,
      "bonusCoins": 200,
      "isPromotional": true,    // 活動方案，可加上 badge
      "bonusPercentage": "贈送 20%"
    }
  ]
}
```

### 前端展示規則
- 依 `displayOrder` ASC 排列
- `isPromotional: true` 加上「活動」badge
- `isInPeriod: false` 或 `isActive: false` → 灰色/隱藏
- 清楚顯示：付 {amount} 元 → 得 {goldCoins} 金幣 + {bonusCoins} 紅利

---

## 三、執行儲值（⚠️ 測試模式）

> 目前系統為「測試模式」：呼叫 API 後，儲值**立即完成**，不跳轉支付頁面。

### API
```
POST /api/recharge
Authorization: Bearer {token}
Content-Type: application/json
```

### Request Body
```json
{
  "planId": "plan-uuid-001"    // 儲值方案 ID
}
```

### Response（成功）
```json
{
  "success": true,
  "data": {
    "id": "recharge-record-uuid",
    "planId": "plan-uuid-001",
    "amount": 300,
    "goldCoins": 300,
    "bonusCoins": 30,
    "paymentStatus": "COMPLETED",    // 直接完成
    "paymentGateway": "STUB",
    "transactionId": null,
    "createdAt": "2026-04-18T10:00:00",
    "paidAt": "2026-04-18T10:00:00"
  }
}
```

### 前端流程（測試模式）
```
用戶選擇方案
    ↓
POST /api/recharge
    ↓
status = COMPLETED → 立即更新錢包餘額
    ↓
GET /api/user/me → 重新取得最新餘額
    ↓
顯示「儲值成功！獲得 {goldCoins} 金幣 + {bonusCoins} 紅利」
```

### paymentStatus 說明

| 狀態 | 說明 | 前端處理 |
|------|------|---------|
| `PENDING` | 待支付（未來真實金流會用到） | 等待 → 輪詢或 WebSocket |
| `COMPLETED` | 已完成 | 更新錢包餘額 + 顯示成功 |
| `FAILED` | 失敗 | 顯示失敗原因 `failReason` |
| `REFUNDED` | 已退款 | 顯示退款狀態 |

---

## 四、查詢儲值記錄

### API
```
GET /api/recharge/history?page=1&size=10
Authorization: Bearer {token}
```

### Response
```json
{
  "success": true,
  "data": [
    {
      "id": "recharge-record-uuid",
      "planId": "plan-uuid-001",
      "amount": 300,
      "goldCoins": 300,
      "bonusCoins": 30,
      "paymentStatus": "COMPLETED",
      "createdAt": "2026-04-18T10:00:00",
      "paidAt": "2026-04-18T10:00:00"
    }
  ]
}
```

### 前端展示
- 依時間降序顯示（最新在上）
- 顯示 `paymentStatus` 的中文標籤：`COMPLETED` = 成功 / `FAILED` = 失敗 / `PENDING` = 處理中
- 顯示取得的金幣與紅利數量
- 支援分頁

---

## 五、出貨費支付（GoMyPay）

> 這是賞品出貨時才會觸發的支付流程，與儲值獨立。

### 觸發時機
當用戶申請賞品出貨時（在賞品盒頁面選擇出貨），後端建立訂單並產生 GoMyPay 付款連結。

### API
```
POST /api/order
Authorization: Bearer {token}
Content-Type: application/json
```

### Request Body
```json
{
  "prizeBoxIds": ["prize-box-uuid-1", "prize-box-uuid-2"],
  "addressId": "user-address-uuid",
  "note": "備註（可選）"
}
```

### Response（包含 GoMyPay URL）
```json
{
  "success": true,
  "data": {
    "orderId": "order-uuid",
    "totalAmount": 80,               // 運費（台幣）
    "paymentUrl": "https://pay.gomypay.asia/...",   // GoMyPay 付款頁
    "expireAt": "2026-04-18T10:30:00"               // 付款期限
  }
}
```

### 前端出貨支付流程
```
用戶選擇要出貨的賞品盒
    ↓
確認地址 + 顯示運費
    ↓
POST /api/order
    ↓
取得 paymentUrl → 導向 GoMyPay 付款頁（window.open 或 redirect）
    ↓
[GoMyPay 處理付款]
    ↓
GoMyPay 通知後端（/api/payment/callback）
    ↓
前端輪詢訂單狀態（GET /api/order/{id}）或等待導回頁
    ↓
訂單 status = PAID → 顯示「出貨申請成功」
```

### 訂單狀態查詢
```
GET /api/order/{orderId}
Authorization: Bearer {token}
```

Response `status` 欄位：

| 狀態 | 說明 |
|------|------|
| `PENDING_PAYMENT` | 待支付 |
| `PAID` | 已付款 |
| `PROCESSING` | 處理中 |
| `SHIPPED` | 已出貨 |
| `DELIVERED` | 已送達 |
| `CANCELLED` | 已取消 |
| `REFUNDED` | 已退款 |

---

## 六、API 快速索引

| API | Method | 需要登入 | 說明 |
|-----|--------|---------|------|
| `/api/recharge-plans` | GET | ✖ | 取得所有有效儲值方案 |
| `/api/recharge` | POST | ✔ | 建立儲值（測試模式：直接完成） |
| `/api/recharge/history` | GET | ✔ | 我的儲值記錄 |
| `/api/recharge/{id}/confirm` | POST | ✔ | 確認支付（未來金流用） |
| `/api/recharge/{id}/failure` | POST | ✔ | 記錄支付失敗（未來金流用） |
| `/api/user/me` | GET | ✔ | 取得用戶資訊含錢包餘額 |
| `/api/order` | POST | ✔ | 建立出貨訂單（含 GoMyPay URL） |
| `/api/order/{id}` | GET | ✔ | 查詢訂單狀態 |
| `/api/payment/callback` | POST | Webhook | GoMyPay 通知（後端接收，前端不呼叫） |

---

## 七、刮刮樂遊戲板渲染修正（⚠️ Bug Fix）

### 問題描述
前端在抽籤後刷新遊戲板時，格子位置發生跳動/排列改變。

### 根本原因
前端用「清單索引（list index）」當作格子位置，當已刮格子被過濾掉後，剩餘格子的索引改變，導致位置偏移。

### 後端已提供正確資料
```
GET /api/lottery/draw/{lotteryId}/tickets
```

後端回傳**全部格子**（含 AVAILABLE 和 DRAWN），按 `ticketNumber ASC` 排序：
```json
{
  "tickets": [
    { "ticketNumber": 1,  "status": "DRAWN",     "revealedNumber": 47, "prizeName": "Apple Watch" },
    { "ticketNumber": 2,  "status": "AVAILABLE",  "revealedNumber": null },
    { "ticketNumber": 3,  "status": "DRAWN",     "revealedNumber": 23, "prizeName": null },
    ...
  ]
}
```

> ⚠️ `revealedNumber` 僅在 `status = DRAWN` 時才有值，AVAILABLE 時為 null（安全隱藏）

### 前端正確實作方式

```typescript
// ✅ 正確：用 ticketNumber 固定格子位置
const renderGrid = (tickets: Ticket[]) => {
  // 建立固定大小的格子陣列
  const grid = new Array(totalTickets).fill(null);
  
  // 每個格子依 ticketNumber 放到對應位置（index = ticketNumber - 1）
  tickets.forEach(ticket => {
    grid[ticket.ticketNumber - 1] = ticket;
  });
  
  return grid.map((ticket, index) => (
    <ScratchTile
      key={index + 1}             // key 永遠不變
      position={index + 1}        // 固定位置
      ticket={ticket}
      status={ticket?.status ?? 'AVAILABLE'}
    />
  ));
};

// ❌ 錯誤：用清單索引當位置（會跳動！）
const renderGrid = (tickets: Ticket[]) => {
  return tickets
    .filter(t => t.status === 'AVAILABLE')  // ← 過濾後索引改變 → 格子跳位
    .map((ticket, index) => (
      <ScratchTile key={index} position={index} ticket={ticket} />
    ));
};
```

### 格子狀態顯示規則

| `status` | 前端顯示 | 顯示資訊 |
|---------|---------|---------|
| `AVAILABLE` | 未刮（可點擊） | 僅顯示 `ticketNumber` |
| `DRAWN` + `revealedNumber` 有值 | 已刮（顯示號碼） | 顯示 `revealedNumber` + 獎品名稱（若有） |
| `DRAWN` + `prizeId = null` | 銘謝惠顧 | 顯示謝謝惠顧圖示 |

### 所有商品是否都有此問題？
是的。所有 `SCRATCH_MODE` 商品的 `revealedNumber` 都是隨機打亂的，刮開後的數字**不等於格子序號**，這是設計如此（雙號碼安全機制）。前端不應假設 `revealedNumber = ticketNumber`。

---

## 八、注意事項 / Constraints

- ❌ 儲值目前不走真實金流，**不要**顯示信用卡輸入框或跳轉 GoMyPay（那是出貨費才用的）
- ❌ 不要讓用戶手動輸入儲值金額，必須選方案
- ❌ 遊戲板不要過濾掉 DRAWN 格子，會導致位置跳動
- ✔ 每次抽獎後重新呼叫 `/tickets` API 更新格子狀態，不要直接移除格子
- ✔ 錢包餘額每次儲值後重新呼叫 `/api/user/me` 更新顯示
- ✔ `bonusCoins` 是紅利，不一定等於金幣，需要分開顯示

---

*最後更新：2026-04-18*
