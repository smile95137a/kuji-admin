# Data Model: 遊戲管理（抽獎機制）

**Feature**: `003-game-management`
**Date**: 2026-03-22

> All entities already exist in the codebase. This document captures the canonical field definitions, relationships, validation rules, and state machines relevant to the draw-mechanics feature.

---

## Entity: Lottery (抽獎活動)

**Table**: `lottery`
**Package**: `com.group.admin.entity.Lottery`

### Key Fields for Draw Mechanics

| Field | Type | DB Column | Description |
|-------|------|-----------|-------------|
| `id` | `String` | `id` VARCHAR(36) PK | UUID |
| `storeId` | `String` | `store_id` | Owning store |
| `pricePerDraw` | `Long` | `price_per_draw` | Current price per draw (point cost) |
| `discountedPrice` | `Long` | `discounted_price` | Discounted price after auto-reduction |
| `autoDiscountEnabled` | `Byte` | `auto_discount_enabled` | 0=off, 1=on |
| `allowMultiDraw` | `Byte` | `allow_multi_draw` | 0=single only, 1=multi allowed |
| `multiDrawOptions` | `String` | `multi_draw_options` | JSON array, e.g. `[5,10]` |
| `protectionMinutes` | `Integer` | `protection_minutes` | Lock duration in minutes (default 5) |
| `totalDraws` | `Integer` | `total_draws` | Fixed pool size (sum of all prize quantities) |
| `status` | `String` | `status` | DRAFT / ON_SHELF / SOLD_OUT / ARCHIVED |

### Status State Machine

```
DRAFT ──→ ON_SHELF ──→ SOLD_OUT ──→ ARCHIVED
                  ↘ ARCHIVED (manual)
```

- Transition to `SOLD_OUT`: triggered automatically when last prize is drawn.
- Draw is only permitted when `status == ON_SHELF`.

### Validation Rules
- `pricePerDraw > 0`
- `protectionMinutes` in `[1, 60]` (business rule; default 5)
- `totalDraws == SUM(lottery_prize.quantity)` — must be consistent at creation time

---

## Entity: LotteryPrize (獎品)

**Table**: `lottery_prize`
**Package**: `com.group.admin.entity.LotteryPrize`

### Fields

| Field | Type | DB Column | Description |
|-------|------|-----------|-------------|
| `id` | `String` | `id` VARCHAR(36) PK | UUID |
| `lotteryId` | `String` | `lottery_id` | FK → lottery.id |
| `name` | `String` | `name` | Prize display name |
| `imageUrl` | `String` | `image_url` | Prize image |
| `level` | `String` | `level` | A / B / C / D / LAST / THANKS |
| `prizeNumber` | `String` | `prize_number` | Unique number within lottery |
| `quantity` | `Integer` | `quantity` | Fixed total stock |
| `remaining` | `Integer` | `remaining` | Current stock (decremented on draw) |
| `weight` | `Integer` | `weight` | Weight for weighted random selection |
| `prizeType` | `String` | `prize_type` | PHYSICAL / DIGITAL / POINTS |
| `pointValue` | `Long` | `point_value` | If prizeType==POINTS |
| `isLastPrize` | `Byte` | `is_last_prize` | 0/1 — last-prize designator (最後賞) |
| `isGrandPrize` | `Byte` | `is_grand_prize` | 0/1 — triggers auto-discount when all sold |
| `orderNum` | `Integer` | `order_num` | Display order |
| `createdAt` | `LocalDateTime` | `created_at` | |
| `updatedAt` | `LocalDateTime` | `updated_at` | |

### Validation Rules
- At most **one** `is_last_prize = 1` per lottery
- `remaining <= quantity` always
- `remaining` decremented inside `@Transactional` with `SELECT FOR UPDATE`
- `weight >= 1`; equal-probability draw achieved by setting all weights equal

### Draw Probability Formula
```
P(prize_i) = weight_i / SUM(weight_j for all j with remaining_j > 0)
```
For equal-probability (FR-002): all weights are equal → P = 1 / remaining_total.

---

## Entity: LotteryDrawRecord (抽獎紀錄)

**Table**: `lottery_draw_record`
**Package**: `com.group.admin.entity.LotteryDrawRecord`

### Fields

| Field | Type | DB Column | Description |
|-------|------|-----------|-------------|
| `id` | `String` | `id` VARCHAR(36) PK | UUID |
| `lotteryId` | `String` | `lottery_id` | FK → lottery.id |
| `userId` | `String` | `user_id` | Player who drew |
| `prizeId` | `String` | `prize_id` | FK → lottery_prize.id |
| `ticketId` | `String` | `ticket_id` | Associated ticket (nullable for RANDOM mode) |
| `sessionId` | `String` | `session_id` | Opener session ID |
| `isOpenerDraw` | `Byte` | `is_opener_draw` | 0/1 |
| `triggeredFreeDraw` | `Byte` | `triggered_free_draw` | 0/1 |
| `selectedNumber` | `String` | `selected_number` | Ticket number (SCRATCH mode) |
| `costType` | `String` | `cost_type` | GOLD / BONUS |
| `costAmount` | `Long` | `cost_amount` | Points deducted |
| `status` | `String` | `status` | SUCCESS / PENDING / FAILED |
| `createdAt` | `LocalDateTime` | `created_at` | Draw timestamp |

### Status Transitions
```
PENDING → SUCCESS  (normal completion)
        → FAILED   (exception/rollback logged)
```

### Validation Rules (FR-009)
- Every draw produces exactly one record
- `costAmount > 0` for paid draws
- `status = SUCCESS` only after prize stock is decremented and wallet deducted

---

## Entity: LotteryLock (保護鎖定)

**Table**: `lottery_lock`
**Package**: `com.group.admin.entity.LotteryLock`

### Fields

| Field | Type | DB Column | Description |
|-------|------|-----------|-------------|
| `id` | `String` | `id` VARCHAR(36) PK | UUID |
| `lotteryId` | `String` | `lottery_id` | FK → lottery.id |
| `userId` | `String` | `user_id` | Lock holder |
| `lockStartTime` | `LocalDateTime` | `lock_start_time` | When lock was acquired |
| `lockEndTime` | `LocalDateTime` | `lock_end_time` | Expiry = start + protectionMinutes |
| `isActive` | `Byte` | `is_active` | 0=expired/released, 1=active |
| `createdAt` | `LocalDateTime` | `created_at` | |

### Lock State Machine
```
[NEW LOCK] isActive=1, lockEndTime = now + protectionMinutes
     │
     ├── Draw by same user    → lock unchanged, draw proceeds
     ├── Draw by other user   → REJECTED (return remainingSeconds)
     ├── lockEndTime reached  → @Scheduled cleanup sets isActive=0
     └── Explicit release     → isActive=0
```

### Constraints
- At most **one active lock** per `lotteryId` at any time
- `lockEndTime - lockStartTime == protectionMinutes` (from Lottery config)
- Unique index recommended: `(lottery_id, is_active)` with partial where `is_active=1`

---

## Entity Relationships

```
Lottery (1)
├── (M) LotteryPrize     lotteryId → lottery.id
├── (M) LotteryDrawRecord lotteryId → lottery.id
└── (1) LotteryLock      lotteryId → lottery.id  [at most one active]

LotteryDrawRecord
├── prizeId   → LotteryPrize.id
└── userId    → User.id (external)
```

---

## New Mapper Queries Required

### LotteryPrizeMapper additions

```xml
<!-- Lock single prize row for update -->
<select id="selectByPrimaryKeyForUpdate" resultMap="BaseResultMap">
  SELECT id, remaining, is_last_prize, is_grand_prize, weight
  FROM lottery_prize
  WHERE id = #{id}
  FOR UPDATE
</select>

<!-- Count grand prizes with stock remaining -->
<select id="countGrandPrizesWithStock" resultType="int">
  SELECT COUNT(*) FROM lottery_prize
  WHERE lottery_id = #{lotteryId}
    AND is_grand_prize = 1
    AND remaining > 0
</select>

<!-- Get last prize for lottery -->
<select id="selectLastPrize" resultMap="BaseResultMap">
  SELECT * FROM lottery_prize
  WHERE lottery_id = #{lotteryId}
    AND is_last_prize = 1
    AND remaining > 0
  LIMIT 1
</select>

<!-- Get all prizes with remaining stock for weighted draw -->
<select id="selectAvailablePrizes" resultMap="BaseResultMap">
  SELECT * FROM lottery_prize
  WHERE lottery_id = #{lotteryId}
    AND remaining > 0
  ORDER BY order_num
</select>
```

### LotteryLockMapper additions

```xml
<!-- Get active lock for a lottery -->
<select id="selectActiveLock" resultMap="BaseResultMap">
  SELECT * FROM lottery_lock
  WHERE lottery_id = #{lotteryId}
    AND is_active = 1
    AND lock_end_time > NOW()
  LIMIT 1
</select>

<!-- Expire stale locks (for scheduler) -->
<update id="expireStaleLocksBeforeTime">
  UPDATE lottery_lock
  SET is_active = 0
  WHERE is_active = 1
    AND lock_end_time &lt; #{now}
</update>
```

### LotteryDrawRecordMapper additions

```xml
<!-- Paginated draw history for admin -->
<select id="selectByLotteryIdPaged" resultMap="BaseResultMap">
  SELECT ldr.*, lp.name AS prize_name, lp.level AS prize_level
  FROM lottery_draw_record ldr
  LEFT JOIN lottery_prize lp ON ldr.prize_id = lp.id
  WHERE ldr.lottery_id = #{lotteryId}
    <if test="userId != null">AND ldr.user_id = #{userId}</if>
    <if test="status != null">AND ldr.status = #{status}</if>
  ORDER BY ldr.created_at DESC
  LIMIT #{limit} OFFSET #{offset}
</select>
```
