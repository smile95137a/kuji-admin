# Contract: Lock Status API — GET /api/lottery/{id}/lock-status

**Feature**: `003-game-management`
**Version**: 1.0
**Auth**: Bearer JWT (player role)

---

## Endpoint

```
GET /api/lottery/{id}/lock-status
```

### Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | string (UUID) | Yes | Lottery ID |

### Query Parameters

None.

---

## Response: Success (200 OK)

### Case A — No active lock (lottery is open)

```json
{
  "success": true,
  "message": "可以抽獎",
  "errorCode": null,
  "data": {
    "canDraw": true,
    "isLockedByMe": false,
    "lockedByOther": false,
    "remainingSeconds": null,
    "lockExpiresAt": null,
    "protectionMinutes": 5
  },
  "timestamp": "2026-03-22T14:30:00.123"
}
```

### Case B — Locked by the caller themselves

```json
{
  "success": true,
  "message": "您正在進行抽獎中",
  "errorCode": null,
  "data": {
    "canDraw": true,
    "isLockedByMe": true,
    "lockedByOther": false,
    "remainingSeconds": 243,
    "lockExpiresAt": "2026-03-22T14:35:00",
    "protectionMinutes": 5
  },
  "timestamp": "2026-03-22T14:30:00.123"
}
```

### Case C — Locked by another player

```json
{
  "success": true,
  "message": "此抽獎活動正由其他玩家進行中",
  "errorCode": null,
  "data": {
    "canDraw": false,
    "isLockedByMe": false,
    "lockedByOther": true,
    "remainingSeconds": 243,
    "lockExpiresAt": "2026-03-22T14:35:00",
    "protectionMinutes": 5
  },
  "timestamp": "2026-03-22T14:30:00.123"
}
```

---

## Response Data Fields

| Field | Type | Description |
|-------|------|-------------|
| `canDraw` | boolean | Whether the calling user is permitted to draw |
| `isLockedByMe` | boolean | True if caller holds the current active lock |
| `lockedByOther` | boolean | True if another user holds the lock |
| `remainingSeconds` | long | Seconds until lock expires (null if no lock) |
| `lockExpiresAt` | datetime | Lock expiry timestamp (null if no lock) |
| `protectionMinutes` | integer | Configured protection window for this lottery |

---

## Error Responses

### 404 Not Found — Lottery does not exist

```json
{
  "success": false,
  "message": "抽獎活動不存在",
  "errorCode": "LOTTERY_NOT_FOUND",
  "data": null,
  "timestamp": "2026-03-22T14:30:00.123"
}
```

---

## Notes

- This endpoint is **read-only** — it does not create or modify locks.
- Use before showing the draw UI to reflect correct lock state.
- `lockedByOther` and `canDraw` are the primary fields for UI rendering logic.
- `lockedByUserId` is **never** exposed in the response (privacy).
