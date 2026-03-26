# 合約：POST /api/lottery/{id}/draw

**功能**: 005-lottery-ticket-system  
**方法**: `POST`  
**路徑**: `/api/lottery/{id}/draw`  
**驗證**: Bearer JWT（已認證使用者）  
**控制器**: `LotteryDrawController` → `POST /{lotteryId}/draw`  
**服務**: `LotteryTicketService.draw(lotteryId, userId, ticketNumber, drawCount)`

---

## 目的

在特定抽獎活動的票券上執行抽獎。處理三種遊戲模式：  
- **RANDOM**：玩家指定 `ticketNumber`；獎品在抽獎活動建立時預先分配。  
- **SCRATCH_STORE**：與 RANDOM 相同；大獎由店家預先指定。  
- **SCRATCH_PLAYER**：開套玩家第一次抽獎觸發指定需求（HTTP 202）；後續抽獎解析獎品。

同時處理：
- 錢包扣款（`pricePerDraw` 金幣）
- 開套玩家 Session 建立 / 保護視窗管理
- 免費抽獎退款觸發（FR-011）

---

## 路徑參數

| 參數 | 類型 | 必填 | 說明 |
|---|---|---|---|
| `id` | `string (UUID)` | ✅ | 抽獎活動 ID |

---

## 請求本文

```json
{
  "ticketNumber": 15
}
```

| 欄位 | 類型 | 必填 | 說明 |
|---|---|---|---|
| `ticketNumber` | `integer` | ✅ | 玩家想要抽取的實體格子號碼（1…N） |

---

## 回應 — 200 OK（抽獎成功）

```json
{
  "success": true,
  "gameMode": "RANDOM",
  "ticketId": "t-uuid",
  "ticketNumber": 15,
  "revealedNumber": null,
  "prizeId": "p-uuid",
  "prizeLevel": "B",
  "prizeName": "B賞",
  "prizeImageUrl": "https://cdn.example.com/prizes/b.jpg",
  "isGrandPrize": false,
  "triggeredFreeDraw": false,
  "refundAmount": null,
  "sessionInfo": {
    "sessionId": "s-uuid",
    "isOpener": false,
    "protectionEndTime": "2026-03-22T14:30:00",
    "openerDrawCount": 3,
    "freeDrawEnabled": true
  }
}
```

### SCRATCH 模式範例（抽獎後填入 revealedNumber）

```json
{
  "success": true,
  "gameMode": "SCRATCH_STORE",
  "ticketNumber": 7,
  "revealedNumber": 23,
  "prizeId": "grand-prize-uuid",
  "prizeLevel": "A",
  "prizeName": "特賞",
  "isGrandPrize": true,
  "triggeredFreeDraw": true,
  "refundAmount": 3000,
  "sessionInfo": { ... }
}
```

---

## 回應 — 202 Accepted（SCRATCH_PLAYER 需要指定）

當呼叫者是全新 SCRATCH_PLAYER Session 的開套玩家，且必須在任何抽獎進行前指定大獎位置時回傳。

```json
{
  "success": false,
  "requiresDesignation": true,
  "message": "請先指定大獎位置 (Please designate grand prize revealed_numbers first)",
  "sessionId": "s-uuid",
  "requiredDesignationCount": 2,
  "availableRevealedNumbers": [1, 2, 3, ..., 60]
}
```

---

## 錯誤回應

| HTTP | 代碼 | 條件 |
|---|---|---|
| `400` | `TICKET_ALREADY_DRAWN` | 指定 `ticketNumber` 的票券不處於 AVAILABLE 狀態 |
| `400` | `LOTTERY_SOLD_OUT` | 沒有 AVAILABLE 票券剩餘 |
| `400` | `INVALID_TICKET_NUMBER` | `ticketNumber` 超出範圍 [1, N] |
| `401` | `UNAUTHORIZED` | 遺失或無效的 JWT |
| `402` | `INSUFFICIENT_BALANCE` | 使用者金幣餘額 < `pricePerDraw` |
| `404` | `LOTTERY_NOT_FOUND` | 找不到指定 `id` 的抽獎活動 |
| `409` | `LOTTERY_NOT_ON_SHELF` | 抽獎活動狀態不為 `ON_SHELF` |
| `423` | `DESIGNATION_PENDING` | SCRATCH_PLAYER Session 已存在但開套玩家尚未完成指定（非開套玩家抽獎被封鎖） |
| `423` | `PROTECTION_ACTIVE` | 另一玩家的保護視窗正在生效（非開套玩家須等待） |

---

## 業務邏輯摘要

```
1. Validate lottery exists & status == ON_SHELF
2. Fetch ticket by (lotteryId, ticketNumber) WHERE status = 'AVAILABLE'
   → 404/400 if not found
3. Check canDrawNow(lotteryId, userId)
   → 423 PROTECTION_ACTIVE if blocked
4. If SCRATCH_PLAYER AND session.playerDesignatedNumbers IS NULL:
   → If caller == opener: return 202 requiresDesignation
   → If caller != opener: return 423 DESIGNATION_PENDING
5. getOrCreateSession(lotteryId, userId)  [synchronized per lotteryId]
6. Deduct wallet: goldBalance -= pricePerDraw
7. UPDATE lottery_ticket SET status='DRAWN', drawn_by=?, drawn_at=NOW()
   WHERE id=? AND status='AVAILABLE'    ← optimistic concurrency (FR-012)
   → If 0 rows affected: ticket already drawn (race) → 400
8. Decrement prize.remaining
9. Add prize to user's prize box
10. startProtection(sessionId) on opener's first draw
11. session.openerDrawCount++ / openerTotalCost += pricePerDraw  (opener only)
12. checkAndTriggerFreeDraw(sessionId, prizeId)
    → If triggered: refund openerTotalCost to opener wallet
13. Record LotteryDrawRecord
14. Return DrawResult
```

---

## 驗收標準對應

| 規格 | 驗收標準 |
|---|---|
| US-1 AC-1 | RANDOM 抽獎回傳預分配獎品 |
| US-1 AC-3 | 售罄的抽獎活動回傳錯誤 |
| US-2 AC-1 | SCRATCH_STORE：revealedNumber=23 → 大獎 |
| US-3 AC-1 | SCRATCH_PLAYER：第一次抽獎回傳 202 requiresDesignation |
| US-4 AC-1 | 開套玩家在 protectionDraws 內抽到大獎觸發免費抽獎 |
| US-4 AC-2 | 耗盡 protectionDraws 未中大獎不退款 |
| US-4 AC-3 | 非開套玩家中大獎：不退款 |
| SC-005 | 樂觀鎖防止重複抽獎 |
