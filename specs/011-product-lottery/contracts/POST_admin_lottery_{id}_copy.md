# Contract: POST /admin/lottery/{id}/copy

**Feature**: `011-product-lottery`  
**Method**: `POST`  
**Path**: `/admin/lottery/{id}/copy`  
**Auth**: Required — JWT Bearer Token (Admin or owning Store Owner)  
**Description**: Creates a deep copy of an existing lottery product. The copy starts in `DRAFT` status with all prize quantities restored to their original values.

---

## Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | String (UUID) | The source lottery product ID to copy |

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
  "title": "2026 鬼滅之刃 一番賞 (復刻版)",
  "scheduledAt": null,
  "startTime": null
}
```

| Field | Required | Type | Description |
|-------|----------|------|-------------|
| `title` | ❌ | String | Override title for the copy. If omitted, defaults to `{original title} (複製)` |
| `scheduledAt` | ❌ | ISO 8601 | New scheduled publish time. If omitted, left null. |
| `startTime` | ❌ | ISO 8601 | New draw start time. If omitted, left null. |

---

## Response

### 201 Created

```json
{
  "success": true,
  "message": "商品已複製",
  "data": {
    "id": "f9e8d7c6-b5a4-3210-fedc-ba9876543210",
    "status": "DRAFT",
    "title": "2026 鬼滅之刃 一番賞 (復刻版)",
    "sourceLotteryId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "totalPrizes": 50,
    "createdAt": "2026-03-22T11:00:00"
  }
}
```

### Error Responses

| HTTP | Error Code | Condition |
|------|-----------|-----------|
| `401` | `UNAUTHORIZED` | No valid JWT |
| `403` | `FORBIDDEN` | User does not own the source lottery's store |
| `404` | `LOTTERY_NOT_FOUND` | No lottery with given `id` |

---

## Business Rules

1. **Deep clone**: All `LotteryPrize` records are duplicated with new UUIDs. `quantity` is preserved; `remaining` is reset to `quantity`.
2. **Status reset**: The copy always starts in `DRAFT`.
3. **Counter reset**: `totalDraws`, `remainingDraws`, `hotCount` are reset to `null` / `0`.
4. **Timestamp reset**: `scheduledAt`, `startTime`, `endTime`, `configuredAt`, `drawableAt`, `createdAt`, `updatedAt` are reset. New values from request body are applied if provided.
5. **Audit trail**: `sourceLotteryId` on the copy is set to the source lottery's `id`.
6. **Tickets**: `ticketsGenerated` is reset to `0`. No ticket pool is generated for the copy until it transitions to CONFIGURED.
7. **Ownership**: The copy belongs to the same store as the original. Cross-store copying is not allowed.
8. **Lock/Session data**: Not copied. The copy starts with no active sessions or locks.
