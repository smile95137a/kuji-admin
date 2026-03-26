# Research: 遊戲管理（抽獎機制）— Game Management / Draw Mechanics

**Feature**: `003-game-management`
**Date**: 2026-03-22
**Status**: Complete — all NEEDS CLARIFICATION resolved

---

## 1. Concurrent Draw Prevention

### Decision
Use **DB-level optimistic locking via `SELECT FOR UPDATE`** on the `lottery_prize.remaining` column, combined with the existing **application-level `LotteryLock`** (opener protection lock) as the outer guard.

### Rationale
The project already has `LotteryLockServiceImpl` which provides the **first line of defence**: only the lock-holder (the opener) may draw during the protection window. This eliminates the vast majority of concurrent-draw races.

For the remaining edge cases — same user submitting duplicate requests, or races at lock acquisition — a `SELECT remaining FOR UPDATE` inside the draw transaction prevents a phantom second decrement from ever committing.

Two-layer strategy:
```
Layer 1 (outer): LotteryLock — one opener at a time per lottery
Layer 2 (inner): SELECT FOR UPDATE on lottery_prize.remaining — atomic stock decrement
```

### Alternatives Considered

| Alternative | Why Rejected |
|-------------|-------------|
| **Redis distributed lock** (Redisson) | Adds an infrastructure dependency (Redis) not currently in the project. Overkill for ~10 concurrent players per lottery. |
| **@Version / Hibernate optimistic lock** | Project uses MyBatis (not JPA). Manual version-column retry loops are more code than FOR UPDATE. |
| **Serializable transaction isolation** | Locks the entire table — severe throughput impact for unrelated lotteries. |
| **Pessimistic lottery-row lock** | Coarser than needed; contention point is prize stock, not the lottery header. |

### Implementation Note

```sql
-- Inside @Transactional draw transaction
SELECT id, remaining
FROM lottery_prize
WHERE id = #{prizeId}
FOR UPDATE;
```

If `remaining <= 0` after the lock is acquired → rollback, return SOLD_OUT error.

---

## 2. Protection Lock Expiry Cleanup

### Decision
Use a **Spring @Scheduled task** to clean expired locks, running every **1 minute** by default (configurable cron expression).

### Rationale
Expired lock cleanup is already scaffolded in `LotteryLockService.cleanExpiredLocks()`. Formalising it in a `LockCleanupScheduler` in the existing `scheduler` package is consistent with the architecture and requires zero new dependencies.

Cleanup query:
```sql
UPDATE lottery_lock
SET is_active = 0
WHERE is_active = 1
  AND lock_end_time < NOW();
```

Running every 60 seconds means worst-case stale lock visibility is 60 s beyond expiry — acceptable for a 5-minute protection window.

### Alternatives Considered

| Alternative | Why Rejected |
|-------------|-------------|
| **Lazy expiry only** | Leaves rows dirty indefinitely; makes monitoring inaccurate. |
| **Redis TTL** | Requires Redis; DB row approach is self-contained. |
| **MySQL event scheduler** | Disabled by default on AWS RDS; adds ops complexity. |

---

## 3. DB-Level Locking vs Redis

### Decision
**DB-level locking** (`SELECT FOR UPDATE`) is the right choice for this project's current scale.

### Rationale

| Criterion | DB Lock (chosen) | Redis Lock |
|-----------|-----------------|------------|
| Infrastructure | MySQL already present | Requires Redis server + Redisson |
| Latency | <5 ms overhead | <1 ms but extra network hop |
| Failure mode | Released on connection close (safe) | Needs TTL + fencing token |
| Consistency | ACID-guaranteed with transaction | Eventual; split-brain risk |
| Scale threshold | Good up to ~100 concurrent users | Needed at >1 000 concurrent |

For ~10 concurrent players per lottery and a <2 s draw target, adding Redis violates YAGNI. The dual-layer strategy is sufficient.

---

## 4. Last-Prize (最後賞) Logic

### Decision
When `lottery.remaining == 1` AND a `lottery_prize` row exists with `is_last_prize = 1`, the draw **bypasses** the random algorithm and directly returns that designated prize.

### Rationale
This is unambiguous from FR-007 and the acceptance criteria in User Story 3. The implementation:

1. At draw entry — check total remaining in lottery.
2. If remaining == 1 — query for `is_last_prize = 1` prize with `remaining > 0`.
3. If found — return it directly (no random selection).
4. Decrement stock, create draw record as normal.

This keeps the last-prize guarantee deterministic and testable (SC-003).

### Alternatives Considered

| Alternative | Why Rejected |
|-------------|-------------|
| Weighted 100% toward last prize | Relies on weight calculation; easier to short-circuit explicitly |
| Flag on lottery level only | Per-prize flag (`is_last_prize`) is already in `LotteryPrize` entity |

---

## 5. Auto Price Reduction (自動降價)

### Decision
Trigger price reduction **synchronously** at the end of the draw transaction that depletes the grand-prize stock. Log the event in a `lottery_price_change_log` record (inline with draw transaction).

### Rationale
FR-008 requires "immediate" trigger (SC-004). An event/async approach (e.g., ApplicationEvent) could introduce lag. A synchronous check inside the draw transaction:

1. After prize stock decrement — query count of `is_grand_prize = 1` prizes with `remaining > 0`.
2. If count == 0 AND `lottery.auto_discount_enabled == 1` → update `lottery.discounted_price`.
3. Emit a log record for FR-012 (trigger time + new price).

This is ACID-safe: if the draw rolls back, the price change also rolls back.

### Alternatives Considered

| Alternative | Why Rejected |
|-------------|-------------|
| Spring ApplicationEvent (async) | Non-atomic; price might update even if draw fails |
| Nightly batch job | Does not meet "immediate" requirement (SC-004) |
| Separate price-reduction endpoint | Requires admin action; defeats automation purpose |

---

## 6. Multi-Draw Atomicity (FR-011)

### Decision
Each individual draw within a multi-draw request is an **independent event** but **all draws in a batch share a single @Transactional boundary**. If any single draw fails (e.g., stock exhausted mid-batch), the entire batch rolls back.

### Rationale
FR-011: "Multi-draw, each draw is an independent event" means each produces its own prize and record. But partial success (some won, some failed) would leave an inconsistent state. Full batch atomicity is cleaner UX and easier to reason about.

---

## Summary of Resolved Unknowns

| Unknown | Resolution |
|---------|-----------|
| Concurrent draw prevention approach | DB FOR UPDATE + application LotteryLock (dual layer) |
| Redis vs DB lock | DB lock sufficient; Redis deferred to scale phase |
| Lock expiry cleanup mechanism | @Scheduled task every 60 s in scheduler package |
| Last-prize selection logic | Explicit check when remaining==1 before random selection |
| Auto-discount trigger point | Synchronous check at end of each draw transaction |
| Multi-draw atomicity | Single @Transactional boundary for entire batch |
