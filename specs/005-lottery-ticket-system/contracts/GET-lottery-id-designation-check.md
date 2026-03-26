# Contract: GET /api/lottery/{id}/designation-check

**Feature**: 005-lottery-ticket-system  
**Method**: `GET`  
**Path**: `/api/lottery/{id}/designation-check`  
**Auth**: Bearer JWT (any authenticated user)  
**Controller**: `LotteryDrawController` → `GET /{lotteryId}/designation-check`  
**Service**: `LotteryTicketService.getDesignationStatus(lotteryId, userId)`

---

## Purpose

Allows the frontend to **poll or check** whether an active SCRATCH_PLAYER session requires the current user to designate grand prize positions before drawing. This endpoint is called:

1. Before rendering the lottery draw UI — to know whether to show the designation panel.
2. After a `POST /draw` returns `requiresDesignation: true` — to fetch the full context (required count, available numbers).
3. By non-openers — to know whether to show "Waiting for opener to designate..." message.

For non-SCRATCH_PLAYER lotteries, always returns `{ required: false }`.

---

## Path Parameters

| Parameter | Type | Required | Description |
|---|---|---|---|
| `id` | `string (UUID)` | ✅ | Lottery ID |

---

## Response — 200 OK (no designation needed)

```json
{
  "required": false,
  "gameMode": "RANDOM",
  "sessionId": null,
  "isOpener": false
}
```

---

## Response — 200 OK (designation required, caller IS the opener)

```json
{
  "required": true,
  "gameMode": "SCRATCH_PLAYER",
  "sessionId": "s-uuid",
  "isOpener": true,
  "requiredDesignationCount": 2,
  "grandPrizes": [
    {
      "prizeId": "grand-prize-uuid-1",
      "prizeName": "特賞",
      "prizeLevel": "A",
      "prizeImageUrl": "https://cdn.example.com/prizes/a.jpg"
    },
    {
      "prizeId": "grand-prize-uuid-2",
      "prizeName": "頭賞",
      "prizeLevel": "S",
      "prizeImageUrl": "https://cdn.example.com/prizes/s.jpg"
    }
  ],
  "availableRevealedNumbers": [1, 2, 4, 7, 9, 12, ..., 60]
}
```

---

## Response — 200 OK (designation required, caller is NOT the opener)

```json
{
  "required": true,
  "gameMode": "SCRATCH_PLAYER",
  "sessionId": "s-uuid",
  "isOpener": false,
  "openerNickname": "Player123",
  "message": "等待開套玩家指定大獎位置 (Waiting for opener to designate grand prizes)"
}
```

---

## Response — 200 OK (designation already completed)

```json
{
  "required": false,
  "gameMode": "SCRATCH_PLAYER",
  "sessionId": "s-uuid",
  "isOpener": false,
  "alreadyDesignated": true
}
```

---

## Response Fields Summary

| Field | Type | When Present | Description |
|---|---|---|---|
| `required` | `boolean` | Always | `true` if opener must still designate |
| `gameMode` | `string` | Always | Lottery's game mode |
| `sessionId` | `string\|null` | Always | Active session UUID or null |
| `isOpener` | `boolean` | Always | Whether the caller is the opener of the active session |
| `requiredDesignationCount` | `integer` | When `required=true && isOpener=true` | How many grand prize positions must be designated |
| `grandPrizes` | `array` | When `required=true && isOpener=true` | Prize options available to designate |
| `availableRevealedNumbers` | `array[int]` | When `required=true && isOpener=true` | All AVAILABLE ticket revealedNumbers to choose from |
| `openerNickname` | `string` | When `required=true && isOpener=false` | Display name of the current opener |
| `alreadyDesignated` | `boolean` | When SCRATCH_PLAYER && designation done | `true` once `playerDesignatedNumbers != null` |
| `message` | `string` | When `required=true && isOpener=false` | Localised wait message |

---

## Error Responses

| HTTP | Code | Condition |
|---|---|---|
| `401` | `UNAUTHORIZED` | Missing or invalid JWT |
| `404` | `LOTTERY_NOT_FOUND` | No lottery with given `id` |

---

## Business Logic Summary

```
1. Fetch lottery by id → 404 if not found
2. If lottery.gameMode != 'SCRATCH_PLAYER':
   → return { required: false, gameMode, sessionId: null, isOpener: false }
3. Fetch ACTIVE session for lotteryId
4. If no session OR session.playerDesignatedNumbers IS NOT NULL:
   → return { required: false, gameMode, sessionId, isOpener: (caller==opener), alreadyDesignated: true/false }
5. Session exists & playerDesignatedNumbers IS NULL:
   → required = true
   → isOpener = (caller == session.openerUserId)
6. If isOpener:
   → fetch grandPrizes (lottery_prize WHERE is_grand_prize=1)
   → fetch availableRevealedNumbers (lottery_ticket WHERE status='AVAILABLE' ORDER BY revealed_number)
   → return full opener response
7. If !isOpener:
   → fetch openerNickname from user table
   → return waiting response
```

---

## Acceptance Criteria Mapping

| Spec | AC |
|---|---|
| US-3 AC-1 | Opener sees `required=true` with designation panel data after first draw triggers session |
| US-3 AC-3 | Non-opener sees `isOpener=false` + wait message |
| FR-008 | Endpoint confirms designation status before draws |
| Edge Case | Two concurrent openers: only the one who won the `sessionLocks` mutex becomes opener; other gets `isOpener=false` |
