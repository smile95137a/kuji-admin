# 功能規格書：稽核日誌系統（Audit Log System）

**功能編號**：032  
**功能分支**：`032-audit-log-system`  
**建立日期**：2026-04-28  
**狀態**：草稿  
**輸入**：AOP 自動切入、分表設計、@Async 非同步寫入、後台查詢 API

---

## 背景與目標

### 現況問題
- 系統目前所有 `log.info()` / `log.warn()` / `log.error()` 只輸出至 console/file（SLF4J），**沒有任何操作被寫入 DB**。
- 雖然已存在 `SystemLog` entity、`SystemLogMapper`、`SystemLogService`、`AdminSystemLogController`，但從未被任何 Controller 呼叫過，整套基礎設施完全閒置。
- 後台管理員無法追蹤「誰做了什麼、什麼時候、結果如何」，工程師也無法快速定位問題。

### 目標
1. 以 **AOP + 自訂 annotation `@AuditLog`** 的方式，對重要操作自動切入記錄，零侵入業務邏輯。
2. 所有非重要操作維持現有 SLF4J log；通用失敗事件表 `log_error_event` **不納入本次 scope**，另開 feature 處理。
3. 使用 **`@Async` + ThreadPoolTaskExecutor** 非同步寫入，API 回應速度不受影響。
4. **廢棄並刪除** 現有閒置的 `system_log` 相關程式碼（entity / mapper / example / repository / service / controller）。
5. 分 5 張專用 Log 表，便於管理員按類別關聯查詢，不混用 JSON blob 替代欄位；其中 `log_admin_action` 作為後續所有後台敏感操作審計的唯一標準。

---

## 一、分表設計（DDL）

### 1.1 `log_auth`（認證日誌）

```sql
CREATE TABLE log_auth (
    id          VARCHAR(36)  PRIMARY KEY COMMENT 'UUID',
    user_id     VARCHAR(36)  COMMENT '使用者 ID（登入失敗時可能為 null）',
    user_type   VARCHAR(20)  NOT NULL COMMENT 'USER / ADMIN',
    email       VARCHAR(255) COMMENT '登入 email',
    login_method VARCHAR(30) NOT NULL COMMENT 'EMAIL / GOOGLE / REFRESH_TOKEN / FORGOT_PASSWORD',
    result      VARCHAR(10)  NOT NULL COMMENT 'SUCCESS / FAIL',
    error_message VARCHAR(500) COMMENT '失敗原因',
    ip          VARCHAR(50)  COMMENT '來源 IP',
    user_agent  VARCHAR(500) COMMENT '瀏覽器資訊',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id    (user_id),
    INDEX idx_email      (email),
    INDEX idx_result     (result),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='認證日誌（登入/登出/OAuth）';
```

### 1.2 `log_draw`（抽獎日誌）

每一抽記錄一筆。

```sql
CREATE TABLE log_draw (
    id            VARCHAR(36)  PRIMARY KEY COMMENT 'UUID',
    user_id       VARCHAR(36)  NOT NULL COMMENT '玩家 ID',
    lottery_id    VARCHAR(36)  NOT NULL COMMENT '商品 ID',
    lottery_title VARCHAR(200) COMMENT '商品名稱（snapshot，商品刪除後仍可查）',
    category      VARCHAR(50)  COMMENT 'GACHA / OFFICIAL_ICHIBAN / TRADING_CARD / CUSTOM_GACHA',
    play_mode     VARCHAR(30)  COMMENT 'LOTTERY_MODE / SCRATCH_MODE',
    game_mode     VARCHAR(30)  COMMENT 'RANDOM / SCRATCH_STORE / SCRATCH_PLAYER',
    ticket_id     VARCHAR(36)  COMMENT '籤位 UUID',
    ticket_number INT          COMMENT '籤位序號',
    prize_level   VARCHAR(20)  COMMENT '獎品等級（A/B/C/LAST/THANKS）',
    prize_name    VARCHAR(200) COMMENT '獎品名稱',
    is_grand_prize TINYINT(1)  DEFAULT 0 COMMENT '是否為大獎',
    deducted_gold  BIGINT      DEFAULT 0 COMMENT '扣除儲值金',
    deducted_bonus BIGINT      DEFAULT 0 COMMENT '扣除紅利金',
    result        VARCHAR(10)  NOT NULL COMMENT 'SUCCESS / FAIL',
    error_message VARCHAR(500) COMMENT '失敗原因',
    duration_ms   INT          COMMENT 'API 執行時間（ms）',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id    (user_id),
    INDEX idx_lottery_id (lottery_id),
    INDEX idx_result     (result),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽獎日誌（每抽一筆）';
```

### 1.3 `log_recharge`（儲值日誌）

```sql
CREATE TABLE log_recharge (
    id                   VARCHAR(36)  PRIMARY KEY COMMENT 'UUID',
    user_id              VARCHAR(36)  NOT NULL COMMENT '玩家 ID',
    recharge_id          VARCHAR(36)  COMMENT '儲值單 ID',
    plan_id              VARCHAR(36)  COMMENT '方案 ID',
    plan_name            VARCHAR(100) COMMENT '方案名稱（snapshot）',
    amount               BIGINT       COMMENT '付款金額（台幣分）',
    gold_added           BIGINT       DEFAULT 0 COMMENT '入帳儲值金',
    bonus_added          BIGINT       DEFAULT 0 COMMENT '入帳紅利金',
    payment_method       VARCHAR(50)  COMMENT 'GOMYPAY / 免費入帳...',
    payment_gateway_ref  VARCHAR(200) COMMENT '金流回傳的交易參考號',
    result               VARCHAR(10)  NOT NULL COMMENT 'SUCCESS / FAIL',
    error_message        VARCHAR(500) COMMENT '失敗原因',
    ip                   VARCHAR(50)  COMMENT '來源 IP',
    created_at           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id      (user_id),
    INDEX idx_recharge_id  (recharge_id),
    INDEX idx_result       (result),
    INDEX idx_created_at   (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='儲值日誌';
```

### 1.4 `log_order`（訂單操作日誌）

```sql
CREATE TABLE log_order (
    id              VARCHAR(36)  PRIMARY KEY COMMENT 'UUID',
    operator_id     VARCHAR(36)  NOT NULL COMMENT '操作者 ID（玩家或管理員）',
    operator_type   VARCHAR(20)  NOT NULL COMMENT 'USER / ADMIN',
    order_id        VARCHAR(36)  NOT NULL COMMENT '訂單 ID',
    action          VARCHAR(50)  NOT NULL COMMENT 'CREATE / CANCEL / SHIP_REQUEST / SHIPPED / COMPLETE',
    prize_box_count INT          COMMENT '賞品盒數量',
    total_amount    BIGINT       COMMENT '訂單金額',
    tracking_number VARCHAR(100) COMMENT '物流單號（SHIPPED 時才有）',
    result          VARCHAR(10)  NOT NULL COMMENT 'SUCCESS / FAIL',
    error_message   VARCHAR(500) COMMENT '失敗原因',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_operator_id (operator_id),
    INDEX idx_order_id    (order_id),
    INDEX idx_action      (action),
    INDEX idx_created_at  (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='訂單操作日誌';
```

### 1.5 `log_admin_action`（後台管理操作日誌）

```sql
CREATE TABLE log_admin_action (
    id              VARCHAR(36)   PRIMARY KEY COMMENT 'UUID',
    admin_id        VARCHAR(36)   NOT NULL COMMENT '後台操作者 ID',
    admin_email     VARCHAR(255)  COMMENT '後台操作者 email（snapshot）',
    admin_role      VARCHAR(50)   COMMENT '操作時的角色（ROLE_ADMIN / ROLE_STORE_OWNER...）',
    target_type     VARCHAR(50)   NOT NULL COMMENT 'LOTTERY / STORE / ADMIN_USER / ORDER / PRIZE_BOX / ...',
    target_id       VARCHAR(36)   COMMENT '被操作對象的 ID',
    target_name     VARCHAR(200)  COMMENT '被操作對象的名稱（snapshot）',
    action          VARCHAR(50)   NOT NULL COMMENT 'CREATE / UPDATE / DELETE / ON_SHELF / OFF_SHELF / ENABLE / DISABLE / RESET_PASSWORD / ...',
    before_snapshot MEDIUMTEXT    COMMENT '操作前的完整 JSON 快照',
    after_snapshot  MEDIUMTEXT    COMMENT '操作後的完整 JSON 快照',
    result          VARCHAR(10)   NOT NULL COMMENT 'SUCCESS / FAIL',
    error_message   VARCHAR(500)  COMMENT '失敗原因',
    ip              VARCHAR(50)   COMMENT '來源 IP',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_admin_id     (admin_id),
    INDEX idx_target_type  (target_type),
    INDEX idx_target_id    (target_id),
    INDEX idx_action       (action),
    INDEX idx_created_at   (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='後台管理操作日誌';
```

---

## 二、AOP 設計

### 2.1 自訂 Annotation `@AuditLog`

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {
    AuditLogType type();        // 對應哪張 log 表
    String action() default ""; // 操作名稱（如 "玩家抽獎"、"後台上架商品"）
}
```

### 2.2 `AuditLogType` Enum

```java
public enum AuditLogType {
    AUTH,           // → log_auth
    DRAW,           // → log_draw
    RECHARGE,       // → log_recharge
    ORDER,          // → log_order
    ADMIN_ACTION    // → log_admin_action
}
```

### 2.3 AOP Pointcut 策略

| 情境 | 切入方式 | 寫表 |
|------|---------|------|
| 有 `@AuditLog` 的方法 | `@Around` 攔截，無論成功失敗都寫 | 對應的 log 表 |
| 沒有 `@AuditLog` 但拋例外 | 維持現有 SLF4J / GlobalExceptionHandler 行為 | （此 spec 不實作 DB 錯誤事件表，另開 feature） |

### 2.4 AOP 取值來源

| 欄位 | 取得方式 |
|------|---------|
| `user_id` / `admin_id` | `SecurityUtils.getCurrentUserId()` |
| `ip` | `HttpServletRequest.getRemoteAddr()`（考慮 X-Forwarded-For） |
| `user_agent` | `HttpServletRequest.getHeader("User-Agent")` |
| `duration_ms` | `System.currentTimeMillis()` 前後相減 |
| `result` | 方法是否拋出例外 → SUCCESS / FAIL |
| `error_message` | 例外的 `getMessage()` |
| 業務欄位（lottery_id 等） | 方法參數 / 傳回值抽取（透過 `JoinPoint.getArgs()`） |

#### before_snapshot / after_snapshot 記錄機制

- 由 Service 層在執行前查好 entity 狀態，呼叫 `AuditContext.setBefore(Object)`，AOP 攔截時自動從 ThreadLocal 取得，寫入 before_snapshot 欄位。
- after_snapshot 亦同理，Service 操作後呼叫 `AuditContext.setAfter(Object)`。
- 若未設定則為 null，不強制。

---

## Clarifications
### Session 2026-04-28
- Q: log_admin_action 的 before_snapshot 欄位由誰負責查詢與傳遞？AOP 還是 Service？ → A: Service 層查好 entity 狀態，透過 ThreadLocal 傳給 AOP。

### Session 2026-04-30
- Q: Audit Log 是否成為後續所有審計的唯一標準？ → A: 是，後台敏感操作統一寫 `log_admin_action`，不要再新增或沿用另一套 `admin_audit_log`。
- Q: `log_error_event` 是否納入本次 scope？ → A: 先不做，032 固定為 5 張 log 表；錯誤事件表另開 feature。
