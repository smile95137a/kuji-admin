-- ==================================================
-- 功能 032: 稽核日誌系統（Audit Log System）
-- 建立日期：2026-05-02
-- 說明：5 張分類 log 表，搭配 AOP @AuditLog 非同步寫入
-- ==================================================

-- 1. 認證日誌
CREATE TABLE IF NOT EXISTS `log_auth` (
    `id`           VARCHAR(36)  NOT NULL PRIMARY KEY COMMENT 'UUID',
    `user_id`      VARCHAR(36)  COMMENT '使用者 ID（登入失敗時可能為 null）',
    `user_type`    VARCHAR(20)  NOT NULL COMMENT 'USER / ADMIN',
    `email`        VARCHAR(255) COMMENT '登入 email',
    `login_method` VARCHAR(30)  NOT NULL COMMENT 'EMAIL / GOOGLE / REFRESH_TOKEN / FORGOT_PASSWORD',
    `result`       VARCHAR(10)  NOT NULL COMMENT 'SUCCESS / FAIL',
    `error_message` VARCHAR(500) COMMENT '失敗原因',
    `ip`           VARCHAR(50)  COMMENT '來源 IP',
    `user_agent`   VARCHAR(500) COMMENT '瀏覽器資訊',
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_log_auth_user_id`    (`user_id`),
    INDEX `idx_log_auth_email`      (`email`),
    INDEX `idx_log_auth_result`     (`result`),
    INDEX `idx_log_auth_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='認證日誌（登入/登出/OAuth）';

-- 2. 抽獎日誌
CREATE TABLE IF NOT EXISTS `log_draw` (
    `id`             VARCHAR(36)  NOT NULL PRIMARY KEY COMMENT 'UUID',
    `user_id`        VARCHAR(36)  NOT NULL COMMENT '玩家 ID',
    `lottery_id`     VARCHAR(36)  NOT NULL COMMENT '商品 ID',
    `lottery_title`  VARCHAR(200) COMMENT '商品名稱（snapshot）',
    `category`       VARCHAR(50)  COMMENT 'GACHA / OFFICIAL_ICHIBAN / TRADING_CARD / CUSTOM_GACHA',
    `play_mode`      VARCHAR(30)  COMMENT 'LOTTERY_MODE / SCRATCH_MODE',
    `game_mode`      VARCHAR(30)  COMMENT 'RANDOM / SCRATCH_STORE / SCRATCH_PLAYER',
    `ticket_id`      VARCHAR(36)  COMMENT '籤位 UUID',
    `ticket_number`  INT          COMMENT '籤位序號',
    `prize_level`    VARCHAR(20)  COMMENT '獎品等級（A/B/C/LAST/THANKS）',
    `prize_name`     VARCHAR(200) COMMENT '獎品名稱',
    `is_grand_prize` TINYINT(1)   DEFAULT 0 COMMENT '是否為大獎',
    `deducted_gold`  BIGINT       DEFAULT 0 COMMENT '扣除儲值金',
    `deducted_bonus` BIGINT       DEFAULT 0 COMMENT '扣除紅利金',
    `result`         VARCHAR(10)  NOT NULL COMMENT 'SUCCESS / FAIL',
    `error_message`  VARCHAR(500) COMMENT '失敗原因',
    `duration_ms`    INT          COMMENT 'API 執行時間（ms）',
    `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_log_draw_user_id`    (`user_id`),
    INDEX `idx_log_draw_lottery_id` (`lottery_id`),
    INDEX `idx_log_draw_result`     (`result`),
    INDEX `idx_log_draw_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽獎日誌（每抽一筆）';

-- 3. 儲值日誌
CREATE TABLE IF NOT EXISTS `log_recharge` (
    `id`                   VARCHAR(36)  NOT NULL PRIMARY KEY COMMENT 'UUID',
    `user_id`              VARCHAR(36)  NOT NULL COMMENT '玩家 ID',
    `recharge_id`          VARCHAR(36)  COMMENT '儲值單 ID',
    `plan_id`              VARCHAR(36)  COMMENT '方案 ID',
    `plan_name`            VARCHAR(100) COMMENT '方案名稱（snapshot）',
    `amount`               BIGINT       COMMENT '付款金額（台幣分）',
    `gold_added`           BIGINT       DEFAULT 0 COMMENT '入帳儲值金',
    `bonus_added`          BIGINT       DEFAULT 0 COMMENT '入帳紅利金',
    `payment_method`       VARCHAR(50)  COMMENT 'GOMYPAY / 免費入帳...',
    `payment_gateway_ref`  VARCHAR(200) COMMENT '金流回傳的交易參考號',
    `result`               VARCHAR(10)  NOT NULL COMMENT 'SUCCESS / FAIL',
    `error_message`        VARCHAR(500) COMMENT '失敗原因',
    `ip`                   VARCHAR(50)  COMMENT '來源 IP',
    `created_at`           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_log_recharge_user_id`     (`user_id`),
    INDEX `idx_log_recharge_recharge_id` (`recharge_id`),
    INDEX `idx_log_recharge_result`      (`result`),
    INDEX `idx_log_recharge_created_at`  (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='儲值日誌';

-- 4. 訂單操作日誌
CREATE TABLE IF NOT EXISTS `log_order` (
    `id`              VARCHAR(36)  NOT NULL PRIMARY KEY COMMENT 'UUID',
    `operator_id`     VARCHAR(36)  NOT NULL COMMENT '操作者 ID（玩家或管理員）',
    `operator_type`   VARCHAR(20)  NOT NULL COMMENT 'USER / ADMIN',
    `order_id`        VARCHAR(36)  NOT NULL COMMENT '訂單 ID',
    `action`          VARCHAR(50)  NOT NULL COMMENT 'CREATE / CANCEL / SHIP_REQUEST / SHIPPED / COMPLETE',
    `prize_box_count` INT          COMMENT '賞品盒數量',
    `total_amount`    BIGINT       COMMENT '訂單金額',
    `tracking_number` VARCHAR(100) COMMENT '物流單號（SHIPPED 時才有）',
    `result`          VARCHAR(10)  NOT NULL COMMENT 'SUCCESS / FAIL',
    `error_message`   VARCHAR(500) COMMENT '失敗原因',
    `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_log_order_operator_id` (`operator_id`),
    INDEX `idx_log_order_order_id`    (`order_id`),
    INDEX `idx_log_order_action`      (`action`),
    INDEX `idx_log_order_created_at`  (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='訂單操作日誌';

-- 5. 後台管理操作日誌
CREATE TABLE IF NOT EXISTS `log_admin_action` (
    `id`              VARCHAR(36)   NOT NULL PRIMARY KEY COMMENT 'UUID',
    `admin_id`        VARCHAR(36)   NOT NULL COMMENT '後台操作者 ID',
    `admin_email`     VARCHAR(255)  COMMENT '後台操作者 email（snapshot）',
    `admin_role`      VARCHAR(50)   COMMENT '操作時的角色（ROLE_ADMIN / ROLE_STORE_OWNER...）',
    `target_type`     VARCHAR(50)   NOT NULL COMMENT 'LOTTERY / STORE / ADMIN_USER / ORDER / PRIZE_BOX / ...',
    `target_id`       VARCHAR(36)   COMMENT '被操作對象的 ID',
    `target_name`     VARCHAR(200)  COMMENT '被操作對象的名稱（snapshot）',
    `action`          VARCHAR(50)   NOT NULL COMMENT 'CREATE / UPDATE / DELETE / ON_SHELF / OFF_SHELF / ENABLE / DISABLE / RESET_PASSWORD / ...',
    `before_snapshot` MEDIUMTEXT    COMMENT '操作前的完整 JSON 快照',
    `after_snapshot`  MEDIUMTEXT    COMMENT '操作後的完整 JSON 快照',
    `result`          VARCHAR(10)   NOT NULL COMMENT 'SUCCESS / FAIL',
    `error_message`   VARCHAR(500)  COMMENT '失敗原因',
    `ip`              VARCHAR(50)   COMMENT '來源 IP',
    `created_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_log_admin_admin_id`    (`admin_id`),
    INDEX `idx_log_admin_target_type` (`target_type`),
    INDEX `idx_log_admin_target_id`   (`target_id`),
    INDEX `idx_log_admin_action`      (`action`),
    INDEX `idx_log_admin_created_at`  (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='後台管理操作日誌';
