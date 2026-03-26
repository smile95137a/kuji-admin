# Research: 商品與抽獎管理 (Product & Lottery Management)

**Feature**: `011-product-lottery`  
**Phase**: 0 — Research & Unknown Resolution  
**Date**: 2026-03-22

---

## 1. Product Copy Feature (FR-013)

### Decision
Implement copy as a deep-clone operation via a dedicated endpoint `POST /admin/lottery/{id}/copy`. The copy creates a new `Lottery` record in `DRAFT` status, resets all draw-counters/timestamps, and clones all associated `LotteryPrize` rows with full quantities restored.

### Rationale
- Deep-clone is the safest approach: the original product remains unmodified in whatever lifecycle state it is in; the copy starts clean in DRAFT.
- A `sourceLotteryId` column is added to `lottery` for audit/traceability — operators can see which product a copy originated from.
- Resetting `remainingDraws`, `status = DRAFT`, and nulling `scheduledAt` / `startTime` ensures the copy cannot accidentally go live before the operator configures it.
- Existing MyBatis insert pattern (`UUID.randomUUID()` + `insert(lottery)`) supports this without new framework dependencies.

### Alternatives Considered
| Alternative | Why Rejected |
|-------------|-------------|
| Client-side copy (re-POST of the same payload) | Requires frontend to fetch all prize details, serialize, and re-submit — error-prone and chatty |
| Shared prize pool between original and copy | Violates FR-005 (each product has its own fixed pool); dangerous for remaining-count integrity |
| Template / draft system (separate entity) | Over-engineered; a plain DRAFT copy achieves the same goal with zero extra tables |

---

## 2. Scheduled Status Transitions with Spring `@Scheduled`

### Decision
Extend the existing `ScheduledTasks` component with a single `lotteryStatusTransitionTask()` method annotated `@Scheduled(fixedDelay = 60000)` (every 60 seconds). The task queries:

1. **CONFIGURED → ON_SHELF**: `SELECT * FROM lottery WHERE status = 'CONFIGURED' AND scheduled_at <= NOW()`
2. **ON_SHELF → DRAWABLE**: `SELECT * FROM lottery WHERE status = 'ON_SHELF' AND start_time <= NOW()`

Each matching lottery is updated in a transaction. The final **IN_PROGRESS → SOLD_OUT** transition is triggered synchronously at draw-time (when `remainingDraws` reaches 0), not by the scheduler, to satisfy SC-005 (< 5 s).

### Rationale
- `@Scheduled(fixedDelay)` is already used in `ScheduledTasks` (email retry, log cleanup) — extending it avoids new scheduler infrastructure.
- A 60-second polling interval is acceptable because the spec does not require sub-minute precision for `scheduledAt` transitions; SC-005 only requires < 5 s for the SOLD_OUT transition, which is handled synchronously.
- `fixedDelay` (not `fixedRate`) prevents overlap if MySQL is slow.
- Using a single DB query per transition direction (one UPDATE with WHERE clause) is more efficient than loading all products.

### Implementation Pattern
```java
// In ScheduledTasks.java
@Scheduled(fixedDelay = 60_000)
public void lotteryStatusTransitionTask() {
    // CONFIGURED → ON_SHELF (scheduled_at has passed)
    lotteryService.promoteScheduledLotteries();
    // ON_SHELF → DRAWABLE (start_time has passed)
    lotteryService.promoteDrawableLotteries();
}
```

```sql
-- promoteScheduledLotteries
UPDATE lottery
SET status = 'ON_SHELF', updated_at = NOW()
WHERE status = 'CONFIGURED'
  AND scheduled_at IS NOT NULL
  AND scheduled_at <= NOW();

-- promoteDrawableLotteries
UPDATE lottery
SET status = 'DRAWABLE', updated_at = NOW()
WHERE status = 'ON_SHELF'
  AND start_time IS NOT NULL
  AND start_time <= NOW();
```

### Alternatives Considered
| Alternative | Why Rejected |
|-------------|-------------|
| Spring `TaskScheduler` with dynamic scheduling per product | More complex; requires storing ScheduledFuture handles; harder to restart after EC2 reboot |
| Database event / MySQL Event Scheduler | Couples business logic to DB engine; not portable; harder to test |
| Quartz Scheduler | Full RDBMS-backed scheduler with clustering support — overkill for this feature; add if/when multi-instance scaling is needed |
| WebSocket push after transition | Out of scope; frontend can poll or use SSE in future iteration |

---

## 3. Lifecycle State Machine Alignment

### Decision
Add three new values to `LotteryStatusEnum`: `CONFIGURED`, `DRAWABLE`, and `SOLD_OUT`. The existing `ENDED` value is kept for backward compatibility (mapped to SOLD_OUT semantically where needed). `FORCED_OFF` becomes `OFF_SHELF` in the spec vocabulary but the code value remains `FORCED_OFF` to avoid migration of existing data.

### Complete FSM

```
DRAFT ──(configure)──► CONFIGURED ──(scheduled_at passes / manual)──► ON_SHELF
                                                                          │
                                                                    (start_time passes)
                                                                          │
                                                                          ▼
                                                                      DRAWABLE ──(first draw)──► IN_PROGRESS
                                                                                                     │
                                                                                             (remainingDraws=0)
                                                                                                     │
                                                                                                     ▼
                                                                                                 SOLD_OUT

Any state ──(admin force-off)──► FORCED_OFF (= OFF_SHELF in UI)
```

### Allowed Transitions Table

| From | To | Trigger | Actor |
|------|----|---------|-------|
| DRAFT | CONFIGURED | All required fields set + prize pool complete | Admin API |
| CONFIGURED | ON_SHELF | `scheduled_at` passes OR manual promote | `@Scheduled` / Admin API |
| ON_SHELF | DRAWABLE | `start_time` passes OR manual promote | `@Scheduled` / Admin API |
| DRAWABLE | IN_PROGRESS | First draw executed | Draw service (auto) |
| IN_PROGRESS | SOLD_OUT | `remainingDraws == 0` | Draw service (auto) |
| Any (except SOLD_OUT/FORCED_OFF) | FORCED_OFF | Admin force-off | Admin API |
| FORCED_OFF | DRAFT | Admin re-activate | Admin API |

---

## 4. Prize Pool & recycleBonus

### Decision
Add `recycle_bonus BIGINT DEFAULT 0 NOT NULL` to the `lottery_prize` table. A value of `0` means not recyclable. Non-zero values represent the bonus amount (in points or currency units, decided by the store owner). The `LotteryPrize` entity gains a `Long recycleBonus` field; MyBatis XML mapper gains the column mapping.

### Rationale
- Single integer column is sufficient; no separate "recyclable" boolean flag needed (0 = not recyclable is self-documenting).
- Managed by the store owner at prize-creation/edit time; not affected by draw logic.
- Consistent with existing `pointValue` field on `LotteryPrize` (also a `Long`).

---

## 5. Last Prize (最後賞) Mechanism

### Decision
Two modes, controlled by `lastPrizeMode` field on `Lottery`:

| Mode | Value | Behaviour |
|------|-------|-----------|
| Traditional (最終一抽) | `LAST_DRAW` | A prize with `isLastPrize = 1` is awarded **in addition** to the regular random draw when `remainingDraws == 1` |
| Pool-in variant (N+1) | `POOL_IN` | The last prize is inserted into the ticket pool as an extra ticket; it has equal probability of being drawn on any draw. No special final-draw logic. |

### Alternatives Considered
| Alternative | Why Rejected |
|-------------|-------------|
| Always give last prize at last draw | Does not support the N+1 variant described in US-4 AC-3 |
| Separate "last prize pool" table | Overkill; `isLastPrize` flag on prize + `lastPrizeMode` on lottery is sufficient |

---

## 6. Auto Price Reduction (降價機制)

### Decision
When `autoDiscountEnabled = 1` and `discountTriggerLevel` is set (e.g., `"AB"` meaning all A and B grade prizes drawn), the system checks at draw-time whether the trigger condition is met. If yes, subsequent draws use `discountedPrice` instead of `pricePerDraw`.

- `discountTriggerLevel`: `VARCHAR(20)` on `lottery` — a comma-separated list of prize levels whose depletion triggers the discount (e.g., `"A,B"`).
- Logic lives in `DrawService.calculateDrawPrice()`.

---

## 7. Protection Round (保護回合)

### Decision
Reuse the existing `LotterySession` / `LotteryLock` mechanism. `LotterySession` effectively IS the `ProtectionRound` described in the spec. No new table is needed. Documentation refers to it as `ProtectionRound` for clarity.

Key fields already present:
- `protectionDraws` on `Lottery` — max draws per protection window
- `protectionMinutes` on `Lottery` — timeout in minutes
- `LotterySession.protectionStartTime`, `protectionEndTime` — the active window
- `LotteryLock.isActive` — boolean lock flag per lottery

---

## 8. Concurrency Safety for Last Ticket

### Decision
Use a database-level pessimistic lock (`SELECT ... FOR UPDATE`) on the `lottery` row when decrementing `remainingDraws`. This prevents two concurrent draws from both seeing `remainingDraws = 1` and both completing.

### Pattern
```java
@Transactional
public DrawResult executeDraw(String lotteryId, String userId, int count) {
    // 1. Lock row
    Lottery lottery = lotteryMapper.selectForUpdate(lotteryId);
    if (lottery.getRemainingDraws() < count) throw new InsufficientTicketsException();
    // 2. Decrement
    lottery.setRemainingDraws(lottery.getRemainingDraws() - count);
    // 3. Auto SOLD_OUT
    if (lottery.getRemainingDraws() == 0) lottery.setStatus("SOLD_OUT");
    lotteryMapper.updateByPrimaryKey(lottery);
    // 4. Pick prizes ...
}
```

---

## Summary of Resolved Unknowns

| Unknown | Resolution |
|---------|-----------|
| How to implement product copy | Deep-clone via `POST /admin/lottery/{id}/copy`, `sourceLotteryId` for audit |
| Scheduled status transition mechanism | Extend `ScheduledTasks` with `@Scheduled(fixedDelay=60s)` + bulk SQL UPDATE |
| New lifecycle states | Add `CONFIGURED`, `DRAWABLE`, `SOLD_OUT` to `LotteryStatusEnum` |
| `recycleBonus` storage | New `recycle_bonus BIGINT DEFAULT 0` column on `lottery_prize` |
| Last prize modes | `lastPrizeMode` on `Lottery` (LAST_DRAW / POOL_IN) |
| Auto discount trigger | `discountTriggerLevel VARCHAR(20)` on `lottery`, checked at draw-time |
| Protection round entity | Reuse `LotterySession` — no new table |
| Concurrency on last ticket | Pessimistic lock (`SELECT FOR UPDATE`) inside `@Transactional` draw method |
