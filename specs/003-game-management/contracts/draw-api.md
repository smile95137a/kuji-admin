# Contract: Draw API — POST /api/lottery/{id}/draw

**Feature**: `003-game-management`
**Version**: 1.0
**Auth**: Bearer JWT (player role)

---

## Endpoint

```
POST /api/lottery/{id}/draw
```

### Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | string (UUID) | Yes | Lottery ID |

### Request Body

```json
{
  "count": 1
}
```

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| `count` | integer | No (default: 1) | 1 ≤ count ≤ 10; must be in lottery.multiDrawOptions if allowMultiDraw=1 | Number of draws to perform |

---

## Response: Success (200 OK)

```json
{
  "success": true,
  "message": "抽獎成功",
  "errorCode": null,
  "data": {
    "draws": [
      {
        "drawRecordId": "550e8400-e29b-41d4-a716-446655440000",
        "lotteryId": "lottery-uuid-123",
        "prizeId": "prize-uuid-abc",
        "prizeName": "A賞 超人力霸王",
        "prizeLevel": "A",
        "prizeImageUrl": "https://cdn.example.com/prizes/ultraman-a.jpg",
        "prizeType": "PHYSICAL",
        "isLastPrize": false,
        "costType": "GOLD",
        "costAmount": 300,
        "createdAt": "2026-03-22T14:30:00"
      }
    ],
    "lotteryRemaining": 79,
    "lockAcquired": true,
    "lockExpiresAt": "2026-03-22T14:35:00",
    "priceMayHaveChanged": false,
    "newPricePerDraw": null
  },
  "timestamp": "2026-03-22T14:30:00.123"
}
```

### Response Data Fields

| Field | Type | Description |
|-------|------|-------------|
| `draws` | array | One element per draw performed |
| `draws[].drawRecordId` | string | UUID of the created LotteryDrawRecord |
| `draws[].lotteryId` | string | Lottery UUID |
| `draws[].prizeId` | string | Prize UUID |
| `draws[].prizeName` | string | Prize display name |
| `draws[].prizeLevel` | string | A / B / C / D / LAST / THANKS |
| `draws[].prizeImageUrl` | string | Prize image URL |
| `draws[].prizeType` | string | PHYSICAL / DIGITAL / POINTS |
| `draws[].isLastPrize` | boolean | true = 最後賞 was awarded |
| `draws[].costType` | string | GOLD or BONUS |
| `draws[].costAmount` | long | Points deducted for this draw |
| `draws[].createdAt` | datetime | Draw timestamp |
| `lotteryRemaining` | integer | Remaining draw count after this operation |
| `lockAcquired` | boolean | Whether a new protection lock was created |
| `lockExpiresAt` | datetime | When the protection lock expires (null if no lock) |
| `priceMayHaveChanged` | boolean | true = auto-discount was triggered by this draw |
| `newPricePerDraw` | long | New price if discount triggered, else null |

---

## Error Responses

### 400 Bad Request — Invalid count

```json
{
  "success": false,
  "message": "抽獎次數無效，請選擇允許的多抽選項",
  "errorCode": "DRAW_INVALID_COUNT",
  "data": null,
  "timestamp": "2026-03-22T14:30:00.123"
}
```

### 402 Payment Required — Insufficient balance

```json
{
  "success": false,
  "message": "餘額不足，請先儲值",
  "errorCode": "INSUFFICIENT_BALANCE",
  "data": null,
  "timestamp": "2026-03-22T14:30:00.123"
}
```

### 409 Conflict — Lottery locked by another player

```json
{
  "success": false,
  "message": "此抽獎活動正由其他玩家進行中，請 243 秒後再試",
  "errorCode": "LOTTERY_LOCKED",
  "data": {
    "lockedByUserId": null,
    "remainingSeconds": 243
  },
  "timestamp": "2026-03-22T14:30:00.123"
}
```

### 410 Gone — Lottery sold out

```json
{
  "success": false,
  "message": "抽獎活動已售罄",
  "errorCode": "LOTTERY_SOLD_OUT",
  "data": null,
  "timestamp": "2026-03-22T14:30:00.123"
}
```

### 422 Unprocessable Entity — Lottery not on shelf

```json
{
  "success": false,
  "message": "抽獎活動尚未開始或已結束",
  "errorCode": "LOTTERY_NOT_ON_SHELF",
  "data": null,
  "timestamp": "2026-03-22T14:30:00.123"
}
```

---

## Business Logic Flow

```
1. Validate lottery exists and status == ON_SHELF
2. Validate count (1 or allowed multi-draw option)
3. Check LotteryLock for lotteryId:
   a. No active lock → try to acquire lock (lockDuration = lottery.protectionMinutes)
   b. Active lock held by caller → proceed (holder can continue)
   c. Active lock held by other → return 409 LOTTERY_LOCKED
4. Calculate total cost = pricePerDraw * count
5. Validate wallet balance >= cost
6. For each draw (i=1..count):
   a. If remaining == 1 AND is_last_prize prize exists → select last prize (no random)
   b. Else → weighted random selection from available prizes
   c. SELECT FOR UPDATE on selected prize row
   d. If remaining == 0 → rollback all, return 410
   e. Decrement prize.remaining by 1
   f. Add to PrizeBox
   g. Create LotteryDrawRecord (status=SUCCESS)
   h. After decrement: if grand prizes all sold AND autoDiscountEnabled → update pricePerDraw
7. Deduct wallet (single transaction covering all draws)
8. Return aggregated result
```

---

## Notes

- Lock is acquired on the **first** draw call; subsequent calls within the window proceed without re-acquiring.
- Multi-draw (`count > 1`) shares a single `@Transactional` boundary — all succeed or all roll back.
- `lockedByUserId` is intentionally omitted from the 409 response body for privacy.
