# Quickstart: 遊戲管理（抽獎機制）— Game Management / Draw Mechanics

**Feature**: `003-game-management`
**Date**: 2026-03-22

---

## What This Feature Does

Implements the core draw execution engine for KUJI 一番賞:

- **Equal-probability draw** (1/N) from a fixed prize pool
- **Opener protection lock** — only one player may draw at a time per lottery (5 min default)
- **最後賞 (last prize)** guarantee — the final draw always yields the designated last prize
- **Auto price reduction** — when all grand prizes sell out, price drops automatically
- **Multi-draw** — draw 1, 5, or 10 at once (configurable per lottery)
- Full **draw record audit trail** with admin query API

---

## Prerequisites

- Java 21 + Maven 3.9+
- MySQL 8.3 running (local or AWS RDS)
- The `admin` Spring Boot application running

---

## Quick Setup

### 1. Start the application

```powershell
cd C:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin
mvn spring-boot:run
```

Or use the existing start script:

```powershell
.\start.sh   # on Linux EC2
```

### 2. Verify the draw endpoint is available

```powershell
Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -Method GET
```

---

## Core Workflows

### Workflow 1: Execute a Single Draw

**Step 1: Get a JWT token (player login)**

```http
POST /api/auth/login
Content-Type: application/json

{
  "phone": "0912345678",
  "password": "yourpassword"
}
```

Copy the `token` from the response.

**Step 2: Check protection lock status**

```http
GET /api/lottery/{lotteryId}/lock-status
Authorization: Bearer {token}
```

Expected response when open:
```json
{ "data": { "canDraw": true, "lockedByOther": false } }
```

**Step 3: Execute the draw**

```http
POST /api/lottery/{lotteryId}/draw
Authorization: Bearer {token}
Content-Type: application/json

{
  "count": 1
}
```

Success response contains `draws[0].prizeName`, `draws[0].prizeLevel`, and updated `lotteryRemaining`.

---

### Workflow 2: Multi-Draw (10 at once)

Lottery must have `allowMultiDraw = 1` and `multiDrawOptions` containing `10`.

```http
POST /api/lottery/{lotteryId}/draw
Authorization: Bearer {token}
Content-Type: application/json

{
  "count": 10
}
```

Returns `draws` array with 10 elements. All 10 succeed or all 10 roll back.

---

### Workflow 3: Admin — View Draw History

```http
GET /admin/lottery/{lotteryId}/draws?page=1&size=20
Authorization: Bearer {adminToken}
```

Filter by date range:

```http
GET /admin/lottery/{lotteryId}/draws?startDate=2026-03-01T00:00:00&endDate=2026-03-31T23:59:59
Authorization: Bearer {adminToken}
```

---

### Workflow 4: Test Last-Prize Scenario

1. Create a lottery with exactly 2 prizes remaining.
2. Mark one prize as `is_last_prize = 1`.
3. Draw once — random result.
4. Draw again (only 1 remaining) — **must return the last-prize item**.

---

## Key Error Codes

| Code | HTTP | Meaning | Fix |
|------|------|---------|-----|
| `LOTTERY_LOCKED` | 409 | Another player is drawing | Wait `remainingSeconds` |
| `LOTTERY_SOLD_OUT` | 410 | No draws remaining | Lottery is exhausted |
| `INSUFFICIENT_BALANCE` | 402 | Not enough points | Top up wallet |
| `LOTTERY_NOT_ON_SHELF` | 422 | Lottery inactive | Check lottery status |
| `DRAW_INVALID_COUNT` | 400 | Count not allowed | Use 1 or valid multi-draw option |

---

## Protection Lock Behaviour

| Scenario | Result |
|----------|--------|
| First draw (no active lock) | Lock created for caller (duration = `protectionMinutes`) |
| Subsequent draw by same caller | Lock extended / draw proceeds normally |
| Draw attempt by different user | 409 returned with `remainingSeconds` |
| Lock expires (timeout) | Any player can now acquire lock |
| Scheduled cleanup (every 60 s) | Expired lock rows marked `is_active = 0` |

---

## Auto Price Reduction

Triggered automatically when:
- `lottery.autoDiscountEnabled == 1`
- All `lottery_prize` rows with `isGrandPrize = 1` have `remaining == 0`

The `pricePerDraw` is updated to `discountedPrice` in the same transaction.  
The draw response includes `priceMayHaveChanged: true` and `newPricePerDraw` when triggered.

---

## Running Tests

```powershell
cd C:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin
mvn test -pl . -Dtest="DrawServiceImplTest,LotteryLockServiceImplTest,LotteryDrawControllerTest"
```

Run all tests:

```powershell
mvn test
```

---

## Relevant Source Files

| File | Purpose |
|------|---------|
| `service/impl/DrawServiceImpl.java` | Core draw algorithm (weighted random + last prize) |
| `service/LotteryLockService.java` | Protection lock interface |
| `service/impl/LotteryLockServiceImpl.java` | Lock acquire / release / check |
| `scheduler/LockCleanupScheduler.java` | Expired lock cleanup (every 60 s) |
| `controller/api/LotteryDrawController.java` | `POST /api/lottery/{id}/draw` |
| `controller/api/LotteryLockController.java` | `GET /api/lottery/{id}/lock-status` |
| `controller/admin/AdminDrawHistoryController.java` | `GET /admin/lottery/{id}/draws` |
| `mapper/LotteryPrizeMapper.xml` | SQL for stock decrement + FOR UPDATE |
| `mapper/LotteryLockMapper.xml` | SQL for lock operations |
| `mapper/LotteryDrawRecordMapper.xml` | SQL for draw record insert + history query |

---

## Design Decisions (Summary)

- **No Redis** — DB-level `SELECT FOR UPDATE` + application `LotteryLock` is sufficient for current scale
- **Single @Transactional per batch** — entire multi-draw atomically succeeds or rolls back
- **Last prize is explicit** — direct selection, not probabilistic
- **Auto-discount is synchronous** — same transaction, guaranteed consistency
- **Lock cleanup is scheduled** — every 60 s via Spring `@Scheduled`

For detailed rationale see [research.md](./research.md).
