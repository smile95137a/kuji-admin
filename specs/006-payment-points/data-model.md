# 資料模型：付款與點數系統 (Payment & Points System)

**功能**：`006-payment-points`  
**階段**：1 — 設計  
**日期**：2026-03-22

---

## 概覽

錢包系統使用**三個實體**：

| 實體（表格） | 用途 | 新增 / 現有 |
|----------------|---------|---------------|
| `User` (`users`) | 儲存 `gold_coins` + `bonus_coins` 餘額，含樂觀鎖 | 現有 — 擴充 |
| `WalletTransaction` (`wallet_transaction`) | 每次餘額變動的不可變稽核日誌 | 現有 — 擴充 |
| `RechargeOrder` (`recharge_order`) | 金流業者狀態機；每次付款嘗試一筆記錄 | **新增** |
| `RechargePlan` (`recharge_plan`) | 管理員管理的固定金額套餐 | 現有 — 驗證欄位 |
| `PrizeBox` (`prize_box`) | 儲存獎品庫存；`status` + `recycle_bonus` 驅動回收功能 | 現有 — 無結構描述變更 |

---

## 實體 1：User（擴充錢包欄位）

**表格**：`users`  
**狀態**：現有 — 僅新增 `CHECK` 約束

```sql
-- Fields already present (verify in production schema):
gold_coins   BIGINT NOT NULL DEFAULT 0,   -- Gold balance (purchased)
bonus_coins  BIGINT NOT NULL DEFAULT 0,   -- Bonus balance (earned)
version      INT    NOT NULL DEFAULT 0,   -- Optimistic lock

-- ADD if not present:
total_recharged BIGINT NOT NULL DEFAULT 0, -- Cumulative Gold purchased (analytics)

-- ADD DB-level safety net (MySQL 8.0.16+):
CONSTRAINT chk_no_negative_balances CHECK (gold_coins >= 0 AND bonus_coins >= 0)
```

### 與錢包功能相關的欄位

| 欄位 | 類型 | 約束 | 說明 |
|--------|------|-------------|-------------|
| `id` | VARCHAR(36) | PK | UUID |
| `gold_coins` | BIGINT | NOT NULL, DEFAULT 0, ≥ 0 | 金幣餘額 |
| `bonus_coins` | BIGINT | NOT NULL, DEFAULT 0, ≥ 0 | 紅利點數餘額 |
| `version` | INT | NOT NULL, DEFAULT 0 | 樂觀鎖計數器 |
| `total_recharged` | BIGINT | NOT NULL, DEFAULT 0 | 累計購買金幣總量 |

### 驗證規則
- `gold_coins ≥ 0`（隨時強制執行，由 CHECK 約束 + 應用層防護）
- `bonus_coins ≥ 0`（隨時強制執行）
- 餘額更新**必須**在 WHERE 子句中包含 `AND version = :version`（樂觀鎖）
- 優先消費金幣；僅在 `gold_coins = 0` 時才使用紅利點數

### MyBatis Mapper 方法（新增）

```xml
<!-- UserMapper.xml -->
<update id="updateBalanceWithVersion">
  UPDATE users
  SET gold_coins = #{goldCoins},
      bonus_coins = #{bonusCoins},
      version = version + 1,
      updated_at = NOW()
  WHERE id = #{userId}
    AND version = #{version}
</update>
```

---

## 實體 2：WalletTransaction（擴充）

**表格**：`wallet_transaction`  
**狀態**：現有 — 若缺少欄位則擴充

```sql
CREATE TABLE IF NOT EXISTS wallet_transaction (
    id              VARCHAR(36)   NOT NULL PRIMARY KEY,  -- UUID
    user_id         VARCHAR(36)   NOT NULL,
    transaction_type ENUM(
        'RECHARGE',       -- Gold added via payment
        'DRAW',           -- Gold/Bonus spent on lottery draw
        'BONUS_GRANT',    -- Bonus added by recharge plan bonus or admin
        'RECYCLE',        -- Bonus added by prize recycling
        'ADMIN_ADJUST',   -- Manual admin adjustment (+ or -)
        'REFUND'          -- Reserved for future use
    ) NOT NULL,
    gold_delta      BIGINT NOT NULL DEFAULT 0,   -- Change in gold (negative = deduction)
    bonus_delta     BIGINT NOT NULL DEFAULT 0,   -- Change in bonus (negative = deduction)
    gold_after      BIGINT NOT NULL,             -- Gold balance AFTER this transaction
    bonus_after     BIGINT NOT NULL,             -- Bonus balance AFTER this transaction
    reference_id    VARCHAR(36)   NULL,          -- orderId / prizeBoxId / rechargeOrderId
    reason          VARCHAR(500)  NULL,          -- Human-readable reason (admin adjustments)
    created_at      DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

    INDEX idx_wt_user_id       (user_id),
    INDEX idx_wt_user_created  (user_id, created_at DESC),
    INDEX idx_wt_reference     (reference_id),
    CONSTRAINT fk_wt_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 欄位說明

| 欄位 | 類型 | 說明 |
|--------|------|-------------|
| `id` | VARCHAR(36) | UUID 主鍵 |
| `user_id` | VARCHAR(36) | 外鍵 → users.id |
| `transaction_type` | ENUM | RECHARGE / DRAW / BONUS_GRANT / RECYCLE / ADMIN_ADJUST / REFUND |
| `gold_delta` | BIGINT | 金幣帶符號變動量（負數為扣款） |
| `bonus_delta` | BIGINT | 紅利點數帶符號變動量（負數為扣款） |
| `gold_after` | BIGINT | 此操作後的金幣餘額快照 |
| `bonus_after` | BIGINT | 此操作後的紅利點數餘額快照 |
| `reference_id` | VARCHAR(36) | 外部 ID（recharge_order.id、order.id、prize_box.id） |
| `reason` | VARCHAR(500) | 自由文字；ADMIN_ADJUST 時必填 |
| `created_at` | DATETIME(3) | 毫秒精度，用於稽核排序 |

### 驗證規則
- 插入後記錄**不可變** — 應用程式碼不允許 UPDATE/DELETE
- `reference_id` 在 RECHARGE、DRAW、RECYCLE 時必填；ADMIN_ADJUST、BONUS_GRANT 時可選

---

## 實體 3：RechargeOrder（新增）

**表格**：`recharge_order`  
**狀態**：**新增** — 建立遷移檔案

```sql
CREATE TABLE recharge_order (
    id                  VARCHAR(36)  NOT NULL PRIMARY KEY,    -- UUID (used as merchant_order_id)
    user_id             VARCHAR(36)  NOT NULL,
    plan_id             VARCHAR(36)  NOT NULL,                -- FK → recharge_plan.id
    gold_amount         BIGINT       NOT NULL,                -- Gold to credit on success
    bonus_amount        BIGINT       NOT NULL DEFAULT 0,      -- Bonus to credit on success
    price_twd          DECIMAL(10,2) NOT NULL,               -- Charged amount (NTD)
    status              ENUM('PENDING','SUCCESS','FAILED','EXPIRED') NOT NULL DEFAULT 'PENDING',
    gateway_provider    VARCHAR(50)  NULL,                    -- 'TAPPAY' | 'ECPAY' | 'NEWEBPAY'
    gateway_order_id    VARCHAR(100) NULL,                    -- Gateway's own transaction ID
    gateway_raw_resp    TEXT         NULL,                    -- Full gateway callback JSON (audit)
    paid_at             DATETIME     NULL,                    -- Populated on SUCCESS
    expired_at          DATETIME     NOT NULL,                -- PENDING TTL (30 min from created_at)
    created_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE INDEX uq_gateway_order (gateway_provider, gateway_order_id),
    INDEX idx_ro_user_id  (user_id),
    INDEX idx_ro_status   (status),
    INDEX idx_ro_expired  (expired_at, status),
    CONSTRAINT fk_ro_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_ro_plan FOREIGN KEY (plan_id) REFERENCES recharge_plan(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 欄位說明

| 欄位 | 類型 | 說明 |
|--------|------|-------------|
| `id` | VARCHAR(36) | UUID；作為 `merchantOrderId` 傳送給業者 |
| `user_id` | VARCHAR(36) | 付款的玩家 |
| `plan_id` | VARCHAR(36) | 所購買的儲值套餐 |
| `gold_amount` | BIGINT | 成功後入帳的金幣（從套餐複製） |
| `bonus_amount` | BIGINT | 成功後入帳的紅利點數（從套餐複製） |
| `price_twd` | DECIMAL(10,2) | 收取的新台幣金額 |
| `status` | ENUM | PENDING → SUCCESS / FAILED / EXPIRED |
| `gateway_provider` | VARCHAR(50) | 處理此付款的業者 |
| `gateway_order_id` | VARCHAR(100) | 業者交易參考編號 |
| `gateway_raw_resp` | TEXT | 原始回呼載荷（稽核用） |
| `paid_at` | DATETIME | 成功付款時間戳記 |
| `expired_at` | DATETIME | 自動到期時間（created_at + 30 分鐘） |

### 狀態機

```
              [玩家完成付款]
  PENDING ──────────────────► SUCCESS
     │                           (wallet credited atomically)
     │ [付款被拒]
     ├────────────────────────► FAILED
     │
     │ [排程器：expired_at 已過]
     └────────────────────────► EXPIRED
```

### 驗證規則
- 僅 `PENDING → SUCCESS` 的轉換才入帳錢包（恰好一次）
- `UNIQUE (gateway_provider, gateway_order_id)` 防止重複回呼
- 排程器每 5 分鐘執行一次，將超過 `expired_at` 的 PENDING 訂單設為過期

---

## 實體 4：RechargePlan

**表格**：`recharge_plan`  
**狀態**：現有 — 驗證以下欄位是否存在；若缺少 `is_active` / `sort_order` 則新增

```sql
-- Verify / extend:
CREATE TABLE IF NOT EXISTS recharge_plan (
    id              VARCHAR(36)   NOT NULL PRIMARY KEY,
    name            VARCHAR(100)  NOT NULL,               -- e.g., "入門包 500"
    gold_amount     BIGINT        NOT NULL,               -- Gold granted
    bonus_amount    BIGINT        NOT NULL DEFAULT 0,     -- Bonus granted (optional gift)
    price_twd       DECIMAL(10,2) NOT NULL,               -- NTD price
    is_active       TINYINT(1)    NOT NULL DEFAULT 1,     -- Soft-delete / hide
    sort_order      INT           NOT NULL DEFAULT 0,     -- Display ordering
    valid_from      DATETIME      NULL,                   -- NULL = always available
    valid_until     DATETIME      NULL,                   -- NULL = no expiry
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 驗證規則
- `gold_amount > 0`（必填）
- `price_twd > 0`（必填）
- `bonus_amount ≥ 0`（零 = 無加贈）
- 僅 `is_active = 1` 的套餐顯示給玩家
- 時間限制套餐：過濾 `valid_from <= NOW() <= valid_until`（NULL = 無限制）

---

## 實體 5：PrizeBox（回收欄位 — 無結構描述變更）

**表格**：`prize_box`  
**狀態**：現有 — 欄位已存在

| 欄位 | 類型 | 說明 |
|--------|------|-------------|
| `id` | VARCHAR(36) | UUID 主鍵 |
| `user_id` | VARCHAR(36) | 擁有該獎品的玩家 |
| `status` | ENUM/VARCHAR | `AVAILABLE` → 可回收；`SHIPPED` → 不可回收 |
| `is_recyclable` | TINYINT(1) | 抽獎層級旗標：此獎品類型是否可回收？ |
| `recycle_bonus` | BIGINT | 回收時給予的紅利點數（在抽獎配置時設定） |
| `recycled_at` | DATETIME | 狀態變更為 RECYCLED 時設定 |

### 回收資格
獎品可回收的條件：
- `status = 'AVAILABLE'`（尚未出貨）
- `is_recyclable = 1`（抽獎允許回收）
- `recycled_at IS NULL`（尚未回收）

---

## 實體關聯

```
users (1) ──────────────────── (N) wallet_transaction
users (1) ──────────────────── (N) recharge_order
users (1) ──────────────────── (N) prize_box
recharge_plan (1) ──────────── (N) recharge_order
prize_box (N) ──────────────── references wallet_transaction.reference_id
recharge_order (N) ─────────── references wallet_transaction.reference_id
```

---

## 索引與效能說明

| 表格 | 索引 | 原因 |
|-------|-------|--------|
| `wallet_transaction` | `(user_id, created_at DESC)` | 分頁交易歷史查詢 |
| `recharge_order` | `(status, expired_at)` | 到期排程器查詢 |
| `recharge_order` | `UNIQUE (gateway_provider, gateway_order_id)` | 冪等性強制執行 |
| `prize_box` | `(user_id, status)` | 過濾可回收獎品 |

---

## 遷移檔案

```
src/main/resources/db/migration/
├── V006__create_recharge_order.sql
└── V006b__extend_wallet_transaction.sql   -- if columns missing in existing table
```

> 部署前執行遷移。先驗證 `users.gold_coins >= 0` CHECK 約束對現有資料無衝突（`SELECT COUNT(*) FROM users WHERE gold_coins < 0`）。
