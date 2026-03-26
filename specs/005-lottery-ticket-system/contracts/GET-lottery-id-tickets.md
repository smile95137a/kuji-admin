# Contract: GET /api/lottery/{id}/tickets

**Feature**: 005-lottery-ticket-system  
**Method**: `GET`  
**Path**: `/api/lottery/{id}/tickets`  
**Auth**: Bearer JWT (any authenticated user)  
**Controller**: `LotteryDrawController` → `GET /{lotteryId}/tickets`  
**Service**: `LotteryTicketService.getTicketsForFrontend(lotteryId)`

---

## Purpose

Return the full ticket list for a lottery. Enforces **strict info-hiding** (FR-005, FR-006, SC-001):  
- AVAILABLE tickets expose **only** `ticketNumber` and `status`.  
- DRAWN tickets expose full prize information and (for SCRATCH modes) `revealedNumber`.

---

## Path Parameters

| Parameter | Type | Required | Description |
|---|---|---|---|
| `id` | `string (UUID)` | ✅ | Lottery ID |

---

## Query Parameters

| Parameter | Type | Required | Default | Description |
|---|---|---|---|---|
| `status` | `string` | ❌ | (all) | Filter by ticket status: `AVAILABLE` or `DRAWN` |

---

## Response — 200 OK

```json
{
  "lotteryId": "a1b2c3d4-...",
  "gameMode": "SCRATCH_STORE",
  "totalTickets": 60,
  "availableCount": 58,
  "drawnCount": 2,
  "tickets": [
    {
      "ticketNumber": 1,
      "status": "AVAILABLE"
    },
    {
      "ticketNumber": 2,
      "status": "AVAILABLE"
    },
    {
      "ticketNumber": 7,
      "status": "DRAWN",
      "revealedNumber": 23,
      "prizeId": "p1-uuid",
      "prizeLevel": "A",
      "prizeName": "特賞",
      "prizeImageUrl": "https://cdn.example.com/prizes/p1.jpg",
      "isGrandPrize": true,
      "drawnBy": "user-uuid",
      "drawnAt": "2026-03-22T14:05:33"
    }
  ]
}
```

### Field Rules by Status

| Field | AVAILABLE | DRAWN |
|---|---|---|
| `ticketNumber` | ✅ | ✅ |
| `status` | ✅ | ✅ |
| `revealedNumber` | ❌ omitted | ✅ (SCRATCH modes only; null for RANDOM) |
| `prizeId` | ❌ omitted | ✅ |
| `prizeLevel` | ❌ omitted | ✅ |
| `prizeName` | ❌ omitted | ✅ |
| `prizeImageUrl` | ❌ omitted | ✅ |
| `isGrandPrize` | ❌ omitted | ✅ |
| `drawnBy` | ❌ omitted | ✅ |
| `drawnAt` | ❌ omitted | ✅ |

---

## Error Responses

| HTTP | Code | Condition |
|---|---|---|
| `401` | `UNAUTHORIZED` | Missing or invalid JWT |
| `404` | `LOTTERY_NOT_FOUND` | No lottery with given `id` |

---

## Implementation Notes

- The service method `getTicketsForFrontend(lotteryId)` already implements the filtering logic. The controller endpoint simply maps the result to the response DTO.
- `revealedNumber` must be `null` in the response for RANDOM mode tickets (even if DRAWN), since RANDOM mode does not use revealed numbers.
- Response is sorted by `ticketNumber` ascending.
- For large lotteries (> 200 tickets), consider adding cursor-based pagination in a future iteration. For the current scope (max ~200 tickets), return all tickets in one call.

---

## Acceptance Criteria Mapping

| Spec | AC |
|---|---|
| US-1 AC-2 | AVAILABLE tickets in RANDOM mode show only number + status |
| US-2 AC-3 | AVAILABLE tickets in SCRATCH_STORE show only number + status; revealedNumber hidden |
| SC-001 | Zero prize-info fields present in AVAILABLE ticket objects |
