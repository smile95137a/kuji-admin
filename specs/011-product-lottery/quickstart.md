# Quickstart: 商品與抽獎管理 (Product & Lottery Management)

**Feature**: `011-product-lottery`  
**Branch**: `011-product-lottery`  
**Date**: 2026-03-22

---

## Prerequisites

- Java 21 + Maven installed
- MySQL 8.3 running (local or RDS)
- Application configured in `src/main/resources/application.yml` (or `application-dev.yml`)
- JWT token for an account with `STORE_OWNER` or `ADMIN` role

---

## Step 1: Apply Database Migration

Run the migration SQL to add new columns:

```sql
-- From: specs/011-product-lottery/data-model.md § 5. Migration SQL
-- Or execute the file directly:
mysql -u root -p kuji_db < specs/011-product-lottery/data-model.md  -- (extract SQL block)
```

Or copy-paste the SQL block from `data-model.md § 5` into your MySQL client:

```sql
ALTER TABLE lottery
    ADD COLUMN source_lottery_id  VARCHAR(36)  NULL  AFTER remark,
    ADD COLUMN configured_at      DATETIME     NULL  AFTER source_lottery_id,
    ADD COLUMN drawable_at        DATETIME     NULL  AFTER configured_at,
    ADD COLUMN remaining_draws    INT          NULL  AFTER total_draws,
    ADD COLUMN discount_trigger_level VARCHAR(20) NULL AFTER discounted_price;

ALTER TABLE lottery_prize
    ADD COLUMN recycle_bonus BIGINT NOT NULL DEFAULT 0 AFTER point_value;

-- Optional index additions
ALTER TABLE lottery ADD INDEX idx_lottery_scheduled_at (scheduled_at);
ALTER TABLE lottery ADD INDEX idx_lottery_start_time   (start_time);
```

Verify:
```sql
DESCRIBE lottery;
DESCRIBE lottery_prize;
-- Should show new columns
```

---

## Step 2: Update `LotteryStatusEnum`

Add the three new status values to `LotteryStatusEnum.java`:

```java
CONFIGURED("CONFIGURED", "已配置"),
DRAWABLE("DRAWABLE", "可抽"),
SOLD_OUT("SOLD_OUT", "售完"),
```

---

## Step 3: Build the Application

```bash
cd C:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin
mvn clean package -DskipTests
```

Expected: `BUILD SUCCESS`

---

## Step 4: Start the Application

```bash
java -jar target/admin-*.jar --spring.profiles.active=dev
```

Or using the start script:
```bash
./start.sh
```

Verify health:
```
GET http://localhost:8080/actuator/health
# Expected: {"status":"UP"}
```

---

## Step 5: Create Your First Lottery Product

### 5a. Obtain a JWT Token

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"store_owner@example.com","password":"yourpassword"}'
```

Copy the `token` from the response. Use it as `$TOKEN` in subsequent requests.

### 5b. Create a Lottery in DRAFT

```bash
curl -X POST http://localhost:8080/admin/lottery \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "測試一番賞",
    "category": "OFFICIAL_ICHIBAN",
    "pricePerDraw": 100,
    "protectionDraws": 5,
    "protectionMinutes": 3,
    "prizes": [
      {"name":"A賞","level":"A","quantity":1,"recycleBonus":100,"isLastPrize":false},
      {"name":"B賞","level":"B","quantity":3,"recycleBonus":50,"isLastPrize":false},
      {"name":"C賞","level":"C","quantity":6,"recycleBonus":0,"isLastPrize":false},
      {"name":"最後賞","level":"LAST","quantity":1,"recycleBonus":0,"isLastPrize":true}
    ],
    "lastPrizeMode": "LAST_DRAW"
  }'
```

Note the `id` in the response — used as `$LOTTERY_ID` below.

### 5c. Transition to CONFIGURED

```bash
curl -X PUT http://localhost:8080/admin/lottery/$LOTTERY_ID/status \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"targetStatus":"CONFIGURED"}'
```

Expected: `currentStatus: "CONFIGURED"`, `totalDraws: 10`, `remainingDraws: 10`

### 5d. Transition to ON_SHELF → DRAWABLE

```bash
# Immediately on shelf (no scheduled_at)
curl -X PUT http://localhost:8080/admin/lottery/$LOTTERY_ID/status \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"targetStatus":"ON_SHELF"}'

# Open draws
curl -X PUT http://localhost:8080/admin/lottery/$LOTTERY_ID/status \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"targetStatus":"DRAWABLE"}'
```

### 5e. Verify Public Listing

```bash
curl http://localhost:8080/api/lottery?status=DRAWABLE
```

Should return the product in the list.

### 5f. View Product Detail

```bash
curl http://localhost:8080/api/lottery/$LOTTERY_ID
```

Should return full detail including `prizes` array with `remaining` counts.

---

## Step 6: Test Product Copy

```bash
curl -X POST http://localhost:8080/admin/lottery/$LOTTERY_ID/copy \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"測試一番賞 (複製)"}'
```

Verify:
- Response contains new `id` different from original
- `status: "DRAFT"`
- `sourceLotteryId` = original `$LOTTERY_ID`

---

## Step 7: Verify Scheduled Transitions (Optional)

1. Create a lottery, set `scheduledAt` to 2 minutes in the future, transition to `CONFIGURED`
2. Wait for `ScheduledTasks.lotteryStatusTransitionTask()` to run (every 60 seconds)
3. After `scheduledAt` passes, status should automatically become `ON_SHELF`

```bash
# Poll the detail endpoint
curl http://localhost:8080/admin/lottery/$LOTTERY_ID
# Watch status change from CONFIGURED → ON_SHELF
```

---

## Step 8: Force-Off Test

```bash
curl -X PUT http://localhost:8080/admin/lottery/$LOTTERY_ID/status \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"targetStatus":"FORCED_OFF","reason":"Testing force-off"}'
```

Verify:
- Public `GET /api/lottery/{id}` returns `403 LOTTERY_NOT_PUBLIC`
- Admin endpoint still returns the product with `status: FORCED_OFF`

---

## Key Files Reference

| What | Where |
|------|-------|
| Lottery entity | `src/main/java/com/group/admin/entity/Lottery.java` |
| LotteryPrize entity | `src/main/java/com/group/admin/entity/LotteryPrize.java` |
| Status enum | `src/main/java/com/group/admin/enums/LotteryStatusEnum.java` |
| Admin controller | `src/main/java/com/group/admin/controller/admin/AdminLotteryController.java` |
| Public controller | `src/main/java/com/group/admin/controller/api/LotteryBrowseController.java` |
| Service interface | `src/main/java/com/group/admin/service/LotteryService.java` |
| Scheduler | `src/main/java/com/group/admin/scheduler/ScheduledTasks.java` |
| Lottery XML mapper | `src/main/resources/mapper/LotteryMapper.xml` |
| Prize XML mapper | `src/main/resources/mapper/LotteryPrizeMapper.xml` |
| API contracts | `specs/011-product-lottery/contracts/` |
| Data model | `specs/011-product-lottery/data-model.md` |
| Research | `specs/011-product-lottery/research.md` |

---

## Troubleshooting

| Problem | Solution |
|---------|---------|
| `FIELD_LOCKED` error on update | Check current lottery status — some fields are immutable in `IN_PROGRESS` |
| Scheduled transition not firing | Check `@EnableScheduling` is on `AdminApplication.java`; check `ScheduledTasks` bean is loaded |
| `totalDraws = null` after create | Transition to `CONFIGURED` first — `totalDraws` is computed then |
| `recycle_bonus` column not found | Run the migration SQL from Step 1 |
| 403 on public detail endpoint | Product is in `DRAFT`/`CONFIGURED`/`FORCED_OFF` — transition it to `ON_SHELF` or higher |
