-- fix-missing-tables.sql
-- 補建伺服器缺少的兩張表：system_config 和 recharge_order
-- 執行方式：mysql -h <host> -u <user> -p kuji < fix-missing-tables.sql

-- ===== 1. system_config =====
CREATE TABLE IF NOT EXISTS `system_config` (
  `id`            VARCHAR(36)   NOT NULL PRIMARY KEY,
  `config_key`    VARCHAR(100)  NOT NULL UNIQUE,
  `config_value`  VARCHAR(500)  NOT NULL,
  `config_type`   VARCHAR(20)   NOT NULL DEFAULT 'STRING' COMMENT 'INTEGER / STRING / BOOLEAN',
  `config_group`  VARCHAR(50)   NOT NULL DEFAULT 'GENERAL' COMMENT '參數分組',
  `description`   VARCHAR(500)  NULL     COMMENT '參數說明',
  `version`       INT           NOT NULL DEFAULT 0 COMMENT '樂觀鎖版本號',
  `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系統參數設定表';

INSERT IGNORE INTO `system_config` (`id`, `config_key`, `config_value`, `config_type`, `config_group`, `description`) VALUES
(UUID(), 'protection_initial_minutes',   '5',  'INTEGER', 'DRAW', '保護初始時間（分鐘）'),
(UUID(), 'protection_extension_minutes', '2',  'INTEGER', 'DRAW', '每次操作延長時間（分鐘）'),
(UUID(), 'protection_max_minutes',       '10', 'INTEGER', 'DRAW', '保護最大時間（分鐘）'),
(UUID(), 'max_draws_per_request',        '10', 'INTEGER', 'DRAW', '單次 API 最大抽獎數');

-- ===== 2. recharge_order =====
CREATE TABLE IF NOT EXISTS `recharge_order` (
    `id`                VARCHAR(36)   NOT NULL PRIMARY KEY,
    `user_id`           VARCHAR(36)   NOT NULL,
    `plan_id`           VARCHAR(36)   NOT NULL,
    `gold_amount`       BIGINT        NOT NULL,
    `bonus_amount`      BIGINT        NOT NULL DEFAULT 0,
    `price_twd`         DECIMAL(10,2) NOT NULL,
    `status`            VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    `gateway_provider`  VARCHAR(50)   NULL,
    `gateway_order_id`  VARCHAR(100)  NULL,
    `gateway_raw_resp`  TEXT          NULL,
    `paid_at`           DATETIME      NULL,
    `expired_at`        DATETIME      NOT NULL,
    `created_at`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_ro_user_id` (`user_id`),
    INDEX `idx_ro_status`  (`status`),
    INDEX `idx_ro_expired` (`expired_at`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充值訂單表';
