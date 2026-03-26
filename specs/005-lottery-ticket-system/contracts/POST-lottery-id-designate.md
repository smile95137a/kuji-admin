# 合約：POST /api/lottery/{id}/designate

**功能**: 005-lottery-ticket-system  
**方法**: `POST`  
**路徑**: `/api/lottery/{id}/designate`  
**驗證**: Bearer JWT — 必須為活躍 Session 的**開套玩家**  
**控制器**: `LotteryDrawController` → `POST /{lotteryId}/designate`  
**服務**: `LotteryTicketService.designatePrizePositions(lotteryId, userId, designations)`

---

## 目的

**僅限 SCRATCH_PLAYER 模式。** 開套玩家（Session 的第一位抽獎者）呼叫此端點，將大獎分配到特定的 `revealedNumber` 位置。指定完成後：

1. 大獎票券以 `prize_id`、`prize_level`、`is_designated_prize = 1`、`designated_by = 'PLAYER'` 更新。
2. `autoAssignNonGrandPrizes()` 自動將剩餘（非大獎）獎品分配給所有其他 AVAILABLE 票券。
3. `session.playerDesignatedNumbers` 填入所選 `revealedNumbers` 的 JSON 陣列。
4. 此 Session 後續的抽獎請求正常進行。

依 FR-013，此端點接受 **`revealedNumbers`**（而非 `ticketNumbers`）。

---

## 路徑參數

| 參數 | 類型 | 必填 | 說明 |
|---|---|---|---|
| `id` | `string (UUID)` | ✅ | 抽獎活動 ID |

---

## 請求本文

```json
{
  "designations": [
    { "revealedNumber": 23, "prizeId": "grand-prize-uuid-1" },
    { "revealedNumber": 45, "prizeId": "grand-prize-uuid-2" }
  ]
}
```

| 欄位 | 類型 | 必填 | 說明 |
|---|---|---|---|
| `designations` | `array` | ✅ | 大獎分配清單 |
| `designations[].revealedNumber` | `integer` | ✅ | 指定為大獎的 revealed number（在請求中必須唯一） |
| `designations[].prizeId` | `string (UUID)` | ✅ | 必須為此抽獎活動中 `is_grand_prize = 1` 的獎品 |

---

## 回應 — 200 OK

```json
{
  "success": true,
  "message": "大獎位置指定成功",
  "sessionId": "s-uuid",
  "designatedWinningNumbers": [
    {
      "revealedNumber": 23,
      "prizeId": "grand-prize-uuid-1",
      "prizeName": "特賞",
      "prizeLevel": "A",
      "prizeImageUrl": "https://cdn.example.com/prizes/a.jpg"
    },
    {
      "revealedNumber": 45,
      "prizeId": "grand-prize-uuid-2",
      "prizeName": "頭賞",
      "prizeLevel": "S",
      "prizeImageUrl": "https://cdn.example.com/prizes/s.jpg"
    }
  ],
  "autoAssignedCount": 58
}
```

| 欄位 | 類型 | 說明 |
|---|---|---|
| `success` | `boolean` | 200 時固定為 `true` |
| `message` | `string` | 本地化確認訊息 |
| `sessionId` | `string` | 活躍 Session ID |
| `designatedWinningNumbers` | `array` | 大獎分配的回饋，含獎品詳細資訊 |
| `autoAssignedCount` | `integer` | 自動分配的非大獎票券數量（FR-009, SC-004） |

---

## 錯誤回應

| HTTP | 代碼 | 條件 |
|---|---|---|
| `400` | `WRONG_DESIGNATION_COUNT` | 指定數量與抽獎活動所需大獎數量不符 |
| `400` | `INVALID_REVEALED_NUMBER` | 一或多個 `revealedNumber` 值超出範圍或已被抽出 |
| `400` | `INVALID_PRIZE_ID` | 獎品不存在、不屬於此抽獎活動，或 `is_grand_prize = 0` |
| `400` | `ALREADY_DESIGNATED` | Session 已完成指定（冪等性防護） |
| `401` | `UNAUTHORIZED` | 遺失或無效的 JWT |
| `403` | `NOT_OPENER` | 呼叫者不是活躍 Session 的開套玩家（US-3 AC-3） |
| `404` | `LOTTERY_NOT_FOUND` | 找不到指定 `id` 的抽獎活動 |
| `409` | `WRONG_GAME_MODE` | 抽獎活動 gameMode 不為 `SCRATCH_PLAYER` |
| `409` | `NO_ACTIVE_SESSION` | 不存在 ACTIVE Session |

---

## 冪等性

此端點**不具冪等性** — 對同一 Session 呼叫兩次會回傳 `400 ALREADY_DESIGNATED`。若第一次呼叫部分失敗（例如部分 UPDATE 後發生 DB 錯誤），Service 會回滾交易，狀態保持 `playerDesignatedNumbers = NULL`，呼叫者可重試。

---

## 業務邏輯摘要

```
1. Validate lottery gameMode == SCRATCH_PLAYER
2. Fetch ACTIVE session for lotteryId
   → 409 NO_ACTIVE_SESSION if none
3. Validate caller == session.openerUserId
   → 403 NOT_OPENER if mismatch
4. Validate session.playerDesignatedNumbers IS NULL
   → 400 ALREADY_DESIGNATED if already set
5. Validate designations.size() == expected grand prize count
6. For each designation (revealedNumber, prizeId):
   a. Validate prize exists & isGrandPrize=1 & belongs to lottery
   b. Fetch ticket WHERE lottery_id=? AND revealed_number=? AND status='AVAILABLE'
   c. UPDATE ticket: prize_id, prize_level, is_designated_prize=1, designated_by='PLAYER'
7. UPDATE session: playerDesignatedNumbers = JSON([revealedNumbers])
8. Call autoAssignNonGrandPrizes(lotteryId)     ← assigns non-grand prizes in batch
9. Return response with designated numbers + autoAssignedCount
```

---

## 驗收標準對應

| 規格 | 驗收標準 |
|---|---|
| US-3 AC-2 | 指定後，後續抽獎使用已指定的 revealedNumbers |
| US-3 AC-3 | 非開套玩家嘗試指定 → 403 |
| FR-008 | 在 SCRATCH_PLAYER 中指定為抽獎進行前的必要條件 |
| FR-009 | 指定後執行 autoAssignNonGrandPrizes() |
| FR-013 | 接受 revealedNumbers（而非 ticketNumbers） |
| SC-004 | 自動分配在 2 秒內完成 |
