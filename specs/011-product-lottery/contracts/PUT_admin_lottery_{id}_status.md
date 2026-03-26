# Contract: PUT /admin/lottery/{id}/status

**Feature**: `011-product-lottery`  
**Method**: `PUT`  
**Path**: `/admin/lottery/{id}/status`  
**Auth**: Required — JWT Bearer Token (Admin or owning Store Owner)  
**Description**: Triggers a manual lifecycle status transition for a lottery product. Validates FSM rules server-side before applying the transition.

---

## Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | String (UUID) | The lottery product ID |

---

## Request

### Headers

| Header | Value |
|--------|-------|
| `Authorization` | `Bearer <jwt>` |
| `Content-Type` | `application/json` |

### Body

```json
{
  "targetStatus": "CONFIGURED",
  "reason": "Prize pool complete, ready to schedule"
}
```

| Field | Required | Type | Description |
|-------|----------|------|-------------|
| `targetStatus` | ✅ | Enum | The desired target status (see FSM below) |
| `reason` | ❌ | String | Optional operator note (stored in `remark` for audit) |

### Valid `targetStatus` Values

| Value | Description |
|-------|-------------|
| `CONFIGURED` | Mark as fully configured (prize pool must be complete) |
| `ON_SHELF` | Manually publish (bypasses scheduledAt) |
| `DRAWABLE` | Manually open draws (bypasses startTime) |
| `FORCED_OFF` | Admin force-off (from any non-terminal state) |
| `DRAFT` | Re-activate from `FORCED_OFF` |

---

## Response

### 200 OK

```json
{
  "success": true,
  "message": "狀態已更新",
  "data": {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "previousStatus": "DRAFT",
    "currentStatus": "CONFIGURED",
    "transitionedAt": "2026-03-22T09:30:00"
  }
}
```

### Error Responses

| HTTP | Error Code | Condition |
|------|-----------|-----------|
| `400` | `INVALID_TRANSITION` | The requested transition is not allowed from the current state |
| `400` | `PRIZE_POOL_EMPTY` | Transitioning to `CONFIGURED` but no prizes defined |
| `400` | `MISSING_PRICE` | `pricePerDraw` not set when transitioning to `CONFIGURED` |
| `401` | `UNAUTHORIZED` | No valid JWT |
| `403` | `FORBIDDEN` | User does not own this lottery's store (non-Admin) |
| `404` | `LOTTERY_NOT_FOUND` | No lottery with given `id` |

---

## Allowed Transitions (FSM)

| Current Status | Allowed `targetStatus` Values |
|---------------|------------------------------|
| `DRAFT` | `CONFIGURED`, `FORCED_OFF` |
| `CONFIGURED` | `ON_SHELF`, `FORCED_OFF` |
| `ON_SHELF` | `DRAWABLE`, `FORCED_OFF` |
| `DRAWABLE` | `FORCED_OFF` |
| `IN_PROGRESS` | `FORCED_OFF` |
| `SOLD_OUT` | _(no manual transitions)_ |
| `FORCED_OFF` | `DRAFT` |

> Note: `IN_PROGRESS` and `SOLD_OUT` are set automatically by the draw service, not by this endpoint. `IN_PROGRESS` is set on the first draw; `SOLD_OUT` is set when `remainingDraws` reaches 0.

---

## Business Rules

1. **CONFIGURED gate**: Before transitioning to `CONFIGURED`, the system validates:
   - At least one prize exists in the pool
   - `pricePerDraw` > 0
   - If `autoDiscountEnabled = true`: `discountedPrice` and `discountTriggerLevel` must be set
   - If any prize has `isLastPrize = true`: `lastPrizeMode` must be set
   - `totalDraws` and `remainingDraws` are computed and written atomically with the transition

2. **Ticket generation**: When transitioning to `CONFIGURED` (or `ON_SHELF` if skipping), the ticket pool is generated if `ticketsGenerated = 0`.

3. **Force-off**: `FORCED_OFF` is always available as a target from any non-terminal state. Admin role is required to force-off another store's product.

4. **Re-activate**: Transitioning from `FORCED_OFF` → `DRAFT` clears `configuredAt`, `drawableAt`, and resets `remainingDraws` to `totalDraws`.

5. **Scheduled transitions**: Setting `scheduledAt` on a `CONFIGURED` product does NOT require calling this endpoint — the scheduler handles it automatically. This endpoint is for manual overrides.
