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
2. 所有非重要操作維持現有 SLF4J log，但**失敗時自動寫 `log_error_event` 表**。
3. 使用 **`@Async` + ThreadPoolTaskExecutor** 非同步寫入，API 回應速度不受影響。
4. **廢棄並刪除** 現有閒置的 `system_log` 相關程式碼（entity / mapper / example / repository / service / controller）。
5. 分 5 張專用 Log 表，便於管理員按類別關聯查詢，不混用 JSON blob 替代欄位。

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
| 沒有 `@AuditLog` 但拋例外 | `GlobalExceptionHandler` 捕捉後呼叫 log service | （此 spec 不實作，待 033） |

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

> ⚠️ 業務欄位抽取限制：AOP 只能取得 **方法參數** 和 **回傳值**。  
> 若需要商品 title、方案名稱等需要查 DB 的欄位（snapshot），應在 Service 層取得後放入 log context，或抽取後以 `@Async` 補查。此版本以「能從參數取到的記，取不到的 null」為原則，不另外查 DB。

---

## 三、非同步寫入架構

### 3.1 ThreadPoolTaskExecutor 設定

```java
@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean(name = "auditLogExecutor")
    public Executor auditLogExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("audit-log-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
```

- `CallerRunsPolicy`：佇列滿時退回呼叫方線程同步執行，確保不丟 log

### 3.2 Service 層寫入模式

```java
@Async("auditLogExecutor")
public void writeDrawLog(LogDrawEntry entry) {
    try {
        logDrawMapper.insert(entry);
    } catch (Exception e) {
        log.error("❌ 寫入 log_draw 失敗: {}", e.getMessage());
        // 寫失敗只記 console，不影響主流程
    }
}
```

---

## 四、各 Annotation 標記對象

### 4.1 `AuditLogType.AUTH` → 標記在 `ApiAuthController` & `OAuth2Controller`

| 方法 | action |
|------|--------|
| `register()` | USER_REGISTER |
| `login()` | USER_LOGIN |
| `googleOAuth()` | GOOGLE_LOGIN |
| `refreshToken()` | REFRESH_TOKEN |
| `forgotPassword()` | FORGOT_PASSWORD |
| `resetPassword()` | RESET_PASSWORD |
| `AdminAuthController.login()` | ADMIN_LOGIN |

### 4.2 `AuditLogType.DRAW` → 標記在 `LotteryDrawController`

| 方法 | action |
|------|--------|
| `draw()` | DRAW（每一抽展開成多筆） |
| `designatePrizePositions()` | DESIGNATE_PRIZE |

### 4.3 `AuditLogType.RECHARGE` → 標記在 `RechargeController`

| 方法 | action |
|------|--------|
| `createRecharge()` | CREATE_RECHARGE |
| `confirmPayment()` | CONFIRM_PAYMENT |
| `recordPaymentFailed()` | PAYMENT_FAILED |

### 4.4 `AuditLogType.ORDER` → 標記在 `OrderController` & `AdminOrderController`

| 方法 | action |
|------|--------|
| `createOrder()` | CREATE |
| `cancelOrder()` | CANCEL |
| `submitShipInfo()` | SHIP_REQUEST |
| `admin shipOrder()` | SHIPPED |
| `admin completeOrder()` | COMPLETE |

### 4.5 `AuditLogType.ADMIN_ACTION` → 標記在後台 controller 的寫入方法

| Controller | 方法 | action |
|------------|------|--------|
| `AdminLotteryController` | create / update / delete | CREATE / UPDATE / DELETE |
| `AdminLotteryController` | onShelf / offShelf / changeStatus | ON_SHELF / OFF_SHELF / STATUS_CHANGE |
| `AdminLotteryController` | copy | COPY |
| `AdminStoreController` | create / update / updateStatus | CREATE / UPDATE / STATUS_CHANGE |
| `AdminUserController` | createOwner / createEditor | CREATE |
| `AdminUserController` | enable / disable / resetPassword / delete | ENABLE / DISABLE / RESET_PASSWORD / DELETE |
| `AdminFrontendUserController` | 點數調整相關 | COIN_ADJUST |

---

## 五、廢棄刪除清單

以下檔案在此 spec 實作時一併刪除：

| 檔案 | 路徑 |
|------|------|
| `SystemLog.java` | `entity/SystemLog.java` |
| `SystemLogExample.java` | `example/SystemLogExample.java` |
| `SystemLogMapper.java` | `mapper/SystemLogMapper.java` |
| `SystemLogMapper.xml` | `resources/mapper/SystemLogMapper.xml` |
| `SystemLogRepository.java` | `repository/SystemLogRepository.java` |
| `SystemLogService.java` | `service/SystemLogService.java` |
| `SystemLogServiceImpl.java` | `service/impl/SystemLogServiceImpl.java` |
| `AdminSystemLogController.java` | `controller/admin/AdminSystemLogController.java` |

> DB 端：`system_log` 表也需要 `DROP TABLE IF EXISTS system_log;`

---

## 六、後台查詢 API（新增）

### 6.1 認證日誌

```
GET /admin/audit-log/auth?userType=&result=&email=&start=&end=&page=&size=
```

### 6.2 抽獎日誌

```
GET /admin/audit-log/draw?userId=&lotteryId=&result=&start=&end=&page=&size=
```

### 6.3 儲值日誌

```
GET /admin/audit-log/recharge?userId=&result=&start=&end=&page=&size=
```

### 6.4 訂單操作日誌

```
GET /admin/audit-log/order?orderId=&operatorType=&action=&start=&end=&page=&size=
```

### 6.5 後台操作日誌

```
GET /admin/audit-log/admin-action?adminId=&targetType=&action=&start=&end=&page=&size=
```

> 所有查詢均需 `ROLE_ADMIN` 或 `ROLE_STORE_OWNER`（店家只能查自己 storeId 相關的資料）。  
> 後端返回全部符合筆數 + 分頁（`page` / `size`），由後端做 DB 分頁（`LIMIT`/`OFFSET`），避免大量資料一次回傳。

---

## 七、Response 格式範例

### `GET /admin/audit-log/draw` 回應
```json
{
  "success": true,
  "data": {
    "total": 1523,
    "page": 1,
    "size": 20,
    "items": [
      {
        "id": "uuid",
        "userId": "user-uuid",
        "lotteryId": "lottery-uuid",
        "lotteryTitle": "鬼滅之刃一番賞",
        "category": "OFFICIAL_ICHIBAN",
        "ticketNumber": 5,
        "prizeLevel": "A",
        "prizeName": "炭治郎公仔",
        "isGrandPrize": false,
        "deductedGold": 80,
        "deductedBonus": 0,
        "result": "SUCCESS",
        "durationMs": 42,
        "createdAt": "2026-04-28T14:30:00"
      }
    ]
  }
}
```

---

## 八、非功能需求

| 項目 | 規格 |
|------|------|
| 寫入延遲 | `@Async`，API 回應時間增加 < 1ms |
| 失敗處理 | log 寫入失敗只記 console error，不影響主流程、不拋例外 |
| 資料保留 | 預設保留 180 天，管理員可手動清除 |
| 敏感資料 | `log_auth` 不記錄明文密碼；`log_recharge` 不記錄完整卡號 |
| Thread Pool | `corePoolSize=2`, `maxPoolSize=5`, `queueCapacity=500` |

---

## 九、驗收情境

### 情境 1：玩家抽獎成功

**在** 玩家成功抽一籤的情況下，  
**當** `POST /api/lottery/{id}/draw` 回傳 200，  
**則** `log_draw` 新增一筆，`result=SUCCESS`、`ticket_number` 正確、`deducted_gold` 與實際扣款一致。

### 情境 2：玩家登入失敗

**在** 玩家輸入錯誤密碼的情況下，  
**當** `POST /api/auth/login` 回傳 401，  
**則** `log_auth` 新增一筆，`result=FAIL`、`error_message` 說明原因、`ip` 正確記錄。

### 情境 3：後台管理員上架商品

**在** 管理員呼叫上架 API 成功的情況下，  
**當** `PUT /admin/lottery/{id}/status` → `ON_SHELF`，  
**則** `log_admin_action` 新增一筆，`action=ON_SHELF`、`before_snapshot` 包含舊狀態 JSON、`after_snapshot` 包含新狀態 JSON。

### 情境 4：Log 寫入失敗不影響主流程

**在** DB 連線異常導致 log 寫入失敗的情況下，  
**當** 玩家執行抽獎，  
**則** 抽獎正常成功回傳 200，console 記錄 error，主流程不受影響。

### 情境 5：舊 system_log 相關程式碼不存在

**在** 實作完成後，  
**當** 全域搜尋 `SystemLog` / `systemLogService` / `SystemLogMapper`，  
**則** 零結果。
