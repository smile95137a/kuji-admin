# Data Model: 商品與抽獎管理 (Product & Lottery Management)

**Feature**: `011-product-lottery`  
**Phase**: 1 — Design  
**Date**: 2026-03-22

---

## Entity Overview

```
Store (1) ──────────────── (N) Lottery
                                  │
                    ┌─────────────┼──────────────┐
                    │             │               │
                 (N) LotteryPrize  (N) DrawRecord  (N) ProtectionRound
                                                       (= LotterySession)
```

---

## 1. Lottery (抽獎商品)

**Table**: `lottery`  
**Java Class**: `com.group.admin.entity.Lottery`

### Fields

| Column | Java Field | Type | Nullable | Default | Notes |
|--------|-----------|------|----------|---------|-------|
| `id` | `id` | `VARCHAR(36)` | NO | — | UUID PK |
| `store_id` | `storeId` | `VARCHAR(36)` | NO | — | FK → `store.id` |
| `title` | `title` | `VARCHAR(200)` | NO | — | Product name |
| `image_url` | `imageUrl` | `VARCHAR(500)` | YES | NULL | Cover image |
| `gallery_images` | `galleryImages` | `TEXT` | YES | NULL | JSON array of image URLs |
| `category` | `category` | `VARCHAR(50)` | NO | — | See `LotteryCategoryEnum` |
| `sub_category` | `subCategory` | `VARCHAR(50)` | YES | NULL | e.g., LOTTERY_MODE / SCRATCH_MODE for CUSTOM_GASHAPON |
| `description` | `description` | `TEXT` | YES | NULL | Rich-text description |
| `content` | `content` | `TEXT` | YES | NULL | Rules / terms |
| `price_per_draw` | `pricePerDraw` | `BIGINT` | NO | — | Price in smallest currency unit |
| `discounted_price` | `discountedPrice` | `BIGINT` | YES | NULL | Price after discount trigger |
| `auto_discount_enabled` | `autoDiscountEnabled` | `TINYINT(1)` | NO | `0` | 0=off, 1=on |
| `discount_trigger_level` | `discountTriggerLevel` | `VARCHAR(20)` | YES | NULL | e.g., "A,B" — depletion of these levels triggers discount |
| `allow_multi_draw` | `allowMultiDraw` | `TINYINT(1)` | NO | `0` | Multi-draw enabled |
| `multi_draw_options` | `multiDrawOptions` | `VARCHAR(100)` | YES | NULL | JSON array e.g. `[10,50]` |
| `scheduled_at` | `scheduledAt` | `DATETIME` | YES | NULL | Auto-publish datetime (CONFIGURED→ON_SHELF) |
| `start_time` | `startTime` | `DATETIME` | YES | NULL | Draw start time (ON_SHELF→DRAWABLE) |
| `end_time` | `endTime` | `DATETIME` | YES | NULL | Optional hard end time |
| `total_draws` | `totalDraws` | `INT` | YES | NULL | Computed from prize quantities at CONFIGURED |
| `remaining_draws` | `remainingDraws` | `INT` | YES | NULL | Decremented on each draw |
| `max_draws` | `maxDraws` | `INT` | YES | NULL | Max draws per session (optional cap) |
| `protection_draws` | `protectionDraws` | `INT` | NO | `0` | Max draws in one protection window |
| `protection_minutes` | `protectionMinutes` | `INT` | NO | `5` | Protection window timeout in minutes |
| `free_draw_enabled` | `freeDrawEnabled` | `TINYINT(1)` | NO | `0` | Free draw toggle |
| `last_prize_mode` | `lastPrizeMode` | `VARCHAR(20)` | YES | NULL | `LAST_DRAW` or `POOL_IN` |
| `tickets_generated` | `ticketsGenerated` | `TINYINT(1)` | NO | `0` | 1 = ticket pool has been generated |
| `designated_prize_numbers` | `designatedPrizeNumbers` | `TEXT` | YES | NULL | JSON mapping of ticket# → prizeId |
| `status` | `status` | `VARCHAR(30)` | NO | `'DRAFT'` | See `LotteryStatusEnum` |
| `source_lottery_id` | `sourceLotteryId` | `VARCHAR(36)` | YES | NULL | ← **NEW** FK to original if copied |
| `configured_at` | `configuredAt` | `DATETIME` | YES | NULL | ← **NEW** timestamp when moved to CONFIGURED |
| `drawable_at` | `drawableAt` | `DATETIME` | YES | NULL | ← **NEW** timestamp when moved to DRAWABLE |
| `order_num` | `orderNum` | `INT` | YES | `0` | Display sort order |
| `weight` | `weight` | `INT` | YES | `0` | Algorithmic weight for ranking |
| `hot_count` | `hotCount` | `INT` | YES | `0` | Draw count used for hot-ranking |
| `theme` | `theme` | `VARCHAR(100)` | YES | NULL | UI theme tag |
| `tags` | `tags` | `VARCHAR(500)` | YES | NULL | Comma-separated search tags |
| `bonus_enabled` | `bonusEnabled` | `BOOLEAN` | YES | NULL | Bonus points enabled |
| `bonus_points_per_draw` | `bonusPointsPerDraw` | `INT` | YES | NULL | Points per draw |
| `bonus_cost_per_draw` | `bonusCostPerDraw` | `INT` | YES | NULL | Points cost per draw |
| `play_mode` | `playMode` | `VARCHAR(50)` | YES | NULL | Additional play-mode tag |
| `game_mode` | `gameMode` | `VARCHAR(50)` | YES | NULL | e.g., GACHA, ICHIBAN |
| `created_by` | `createdBy` | `VARCHAR(36)` | YES | NULL | FK → user.id |
| `remark` | `remark` | `VARCHAR(500)` | YES | NULL | Internal notes |
| `created_at` | `createdAt` | `DATETIME` | NO | `NOW()` | |
| `updated_at` | `updatedAt` | `DATETIME` | NO | `NOW()` | |

### Category Enum — `LotteryCategoryEnum`

| Code | Display Name (zh) |
|------|------------------|
| `OFFICIAL_ICHIBAN` | 官方一番賞 |
| `GASHAPON` | 扭蛋 |
| `TRADING_CARD` | 集換卡牌 |
| `CUSTOM_GASHAPON` | 自訂扭蛋 |

### Status FSM — `LotteryStatusEnum`

| Code | Display Name (zh) | Description |
|------|------------------|-------------|
| `DRAFT` | 草稿 | Created, not configured |
| `CONFIGURED` | 已配置 | ← **NEW** Prize pool complete, awaiting scheduled_at |
| `ON_SHELF` | 上架中 | Visible to public, draw not yet open |
| `DRAWABLE` | 可抽 | ← **NEW** Draw open, awaiting first draw |
| `IN_PROGRESS` | 抽獎中 | Active drawing |
| `SOLD_OUT` | 售完 | ← **NEW** All tickets drawn |
| `FORCED_OFF` | 強制下架 | Admin-forced off |

### Validation Rules

- `price_per_draw` must be > 0
- `total_draws` = SUM of `lottery_prize.quantity` where `is_last_prize = 0` (last prize not counted in pool for `LAST_DRAW` mode)
- Transition to `CONFIGURED` requires: `title`, `category`, `price_per_draw`, at least one prize in pool
- `scheduled_at` must be in the future when set (or null for immediate ON_SHELF)
- `discounted_price` required if `auto_discount_enabled = 1`
- `discount_trigger_level` required if `auto_discount_enabled = 1`

### Indexes

```sql
INDEX idx_lottery_store_id (store_id)
INDEX idx_lottery_status (status)
INDEX idx_lottery_scheduled_at (scheduled_at)
INDEX idx_lottery_start_time (start_time)
```

---

## 2. LotteryPrize (獎品)

**Table**: `lottery_prize`  
**Java Class**: `com.group.admin.entity.LotteryPrize`

### Fields

| Column | Java Field | Type | Nullable | Default | Notes |
|--------|-----------|------|----------|---------|-------|
| `id` | `id` | `VARCHAR(36)` | NO | — | UUID PK |
| `lottery_id` | `lotteryId` | `VARCHAR(36)` | NO | — | FK → `lottery.id` |
| `name` | `name` | `VARCHAR(200)` | NO | — | Prize name |
| `image_url` | `imageUrl` | `VARCHAR(500)` | YES | NULL | |
| `level` | `level` | `VARCHAR(10)` | NO | — | A/B/C/D/LAST |
| `prize_number` | `prizeNumber` | `VARCHAR(20)` | YES | NULL | Display number |
| `quantity` | `quantity` | `INT` | NO | — | Initial count in pool |
| `remaining` | `remaining` | `INT` | NO | — | Current remaining |
| `weight` | `weight` | `INT` | YES | `0` | Display sort weight |
| `prize_type` | `prizeType` | `VARCHAR(50)` | YES | NULL | Physical/Digital/etc. |
| `point_value` | `pointValue` | `BIGINT` | YES | NULL | Point value for wallet credit |
| `recycle_bonus` | `recycleBonus` | `BIGINT` | NO | `0` | ← **NEW** Bonus if recycled; 0 = not recyclable |
| `is_last_prize` | `isLastPrize` | `TINYINT(1)` | NO | `0` | 1 = this is the 最後賞 |
| `is_grand_prize` | `isGrandPrize` | `TINYINT(1)` | NO | `0` | 1 = grand prize flag |
| `order_num` | `orderNum` | `INT` | YES | `0` | Display order |
| `description` | `description` | `TEXT` | YES | NULL | |
| `content` | `content` | `TEXT` | YES | NULL | |
| `created_at` | `createdAt` | `DATETIME` | NO | `NOW()` | |
| `updated_at` | `updatedAt` | `DATETIME` | NO | `NOW()` | |

### Prize Level Enum

| Code | Display | Recyclable Default |
|------|---------|-------------------|
| `A` | A賞 | Store decides |
| `B` | B賞 | Store decides |
| `C` | C賞 | Store decides |
| `D` | D賞 | Store decides |
| `LAST` | 最後賞 | Store decides |

### Validation Rules

- `quantity` must be ≥ 1
- `remaining` initialized to `quantity` on creation
- Only one prize may have `is_last_prize = 1` per lottery
- `recycle_bonus` must be ≥ 0

---

## 3. LotteryDrawRecord (抽獎紀錄)

**Table**: `lottery_draw_record`  
**Java Class**: `com.group.admin.entity.LotteryDrawRecord`

> **No schema changes required for this feature.** Existing fields cover all requirements.

### Key Fields (existing)

| Column | Java Field | Type | Notes |
|--------|-----------|------|-------|
| `id` | `id` | `VARCHAR(36)` | UUID PK |
| `lottery_id` | `lotteryId` | `VARCHAR(36)` | FK → lottery |
| `user_id` | `userId` | `VARCHAR(36)` | FK → user |
| `prize_id` | `prizeId` | `VARCHAR(36)` | FK → lottery_prize |
| `ticket_id` | `ticketId` | `VARCHAR(36)` | FK → lottery_ticket |
| `session_id` | `sessionId` | `VARCHAR(36)` | FK → lottery_session |
| `selected_number` | `selectedNumber` | `VARCHAR(20)` | |
| `cost_amount` | `costAmount` | `BIGINT` | Actual price paid |
| `status` | `status` | `VARCHAR(20)` | |
| `created_at` | `createdAt` | `DATETIME` | Draw timestamp |

---

## 4. ProtectionRound (保護回合) — alias LotterySession

**Table**: `lottery_session`  
**Java Class**: `com.group.admin.entity.LotterySession`

> **No schema changes required.** `LotterySession` fully models the ProtectionRound concept.

### Key Fields (existing)

| Column | Java Field | Type | Notes |
|--------|-----------|------|-------|
| `id` | `id` | `VARCHAR(36)` | UUID PK |
| `lottery_id` | `lotteryId` | `VARCHAR(36)` | FK → lottery |
| `opener_user_id` | `openerUserId` | `VARCHAR(36)` | The player holding the lock |
| `protection_draws` | `protectionDraws` | `INT` | Max draws in session |
| `protection_start_time` | `protectionStartTime` | `DATETIME` | Window start |
| `protection_end_time` | `protectionEndTime` | `DATETIME` | Window expiry |
| `free_draw_enabled` | `freeDrawEnabled` | `TINYINT(1)` | |
| `status` | `status` | `VARCHAR(20)` | ACTIVE / EXPIRED / CLOSED |

---

## 5. Migration SQL

```sql
-- =========================================================
-- V011: Product Lottery Enhancements
-- Applies to: lottery, lottery_prize tables
-- =========================================================

-- 5.1  New columns on lottery
ALTER TABLE lottery
    ADD COLUMN source_lottery_id VARCHAR(36)  NULL COMMENT '複製來源商品ID' AFTER remark,
    ADD COLUMN configured_at     DATETIME     NULL COMMENT '進入已配置狀態時間' AFTER source_lottery_id,
    ADD COLUMN drawable_at       DATETIME     NULL COMMENT '進入可抽狀態時間'   AFTER configured_at,
    ADD COLUMN remaining_draws   INT          NULL COMMENT '剩餘抽獎次數'       AFTER total_draws,
    ADD COLUMN discount_trigger_level VARCHAR(20) NULL COMMENT '觸發降價的獎品等級,例如A,B' AFTER discounted_price;

-- 5.2  New column on lottery_prize
ALTER TABLE lottery_prize
    ADD COLUMN recycle_bonus BIGINT NOT NULL DEFAULT 0 COMMENT '回收獎勵金額; 0=不可回收' AFTER point_value;

-- 5.3  Extend status enum if stored as ENUM type
--      (If status is VARCHAR, no DDL change needed — just insert new values via service)
--      If column is ENUM, run:
-- ALTER TABLE lottery MODIFY COLUMN status ENUM(
--     'DRAFT','CONFIGURED','ON_SHELF','DRAWABLE','IN_PROGRESS','SOLD_OUT','FORCED_OFF'
-- ) NOT NULL DEFAULT 'DRAFT';

-- 5.4  Indexes
ALTER TABLE lottery ADD INDEX idx_lottery_scheduled_at (scheduled_at);
ALTER TABLE lottery ADD INDEX idx_lottery_start_time   (start_time);
ALTER TABLE lottery ADD INDEX idx_lottery_source_id    (source_lottery_id);
ALTER TABLE lottery_prize ADD INDEX idx_prize_lottery_level (lottery_id, level);
```

---

## 6. Entity Relationship Summary

```
lottery (1)──────────────────────────────── (N) lottery_prize
   │  PK: id                                       PK: id
   │  FK: store_id → store.id                      FK: lottery_id
   │  NEW: source_lottery_id → lottery.id          NEW: recycle_bonus
   │
   ├──(1)──────────────────────────────── (N) lottery_draw_record
   │                                              FK: lottery_id, user_id, prize_id
   │
   ├──(1)──────────────────────────────── (0..1) lottery_lock
   │                                              (active draw lock per lottery)
   │
   └──(1)──────────────────────────────── (N) lottery_session
                                              (protection round per user per lottery)
```
