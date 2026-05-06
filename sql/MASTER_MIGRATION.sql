-- ============================================================
-- KUJI Admin - MASTER MIGRATION SCRIPT
-- 22 個 worktree 合併後需要執行的所有 SQL
-- 說明：此腳本已加上 IF NOT EXISTS 防止重複執行
-- 執行環境：MySQL 8.0+
-- ============================================================
-- 執行順序：
--   PHASE 0 - 新增獨立輔助表（無外鍵依賴）
--   PHASE 1 - 金流相關表（user_wallet / recharge）
--   PHASE 2 - 訂單 / 賞品盒相關表
--   PHASE 3 - 推薦碼 / 地址 / 抽獎籤位表
--   PHASE 4 - ALTER TABLE（現有表新增欄位）
--   PHASE 5 - ALTER TABLE（現有表欄位改名，需手動確認）
--   PHASE 6 - 初始資料 INSERT
--   PHASE 7 - 資料正規化（enum 值大寫）
-- ============================================================

-- ============================================================
-- PHASE 0：新增獨立輔助表
-- ============================================================

-- 001: banner（首頁輪播）
CREATE TABLE IF NOT EXISTS `banner` (
  `id`          VARCHAR(36)  NOT NULL,
  `store_id`    VARCHAR(36)  NOT NULL,
  `title`       VARCHAR(200) DEFAULT NULL,
  `image_url`   VARCHAR(500) NOT NULL,
  `link_url`    VARCHAR(500) DEFAULT NULL,
  `order_num`   INT          NOT NULL DEFAULT 0,
  `status`      VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
  `start_time`  DATETIME     DEFAULT NULL,
  `end_time`    DATETIME     DEFAULT NULL,
  `created_at`  DATETIME     NOT NULL,
  `updated_at`  DATETIME     NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_banner_store_id`     (`store_id`),
  INDEX `idx_banner_status_order` (`status`, `order_num`, `created_at`),
  INDEX `idx_banner_schedule`     (`status`, `start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='首頁輪播橫幅';

-- 013: admin_token_blacklist（後台 JWT 黑名單）
CREATE TABLE IF NOT EXISTS `admin_token_blacklist` (
  `admin_user_id` VARCHAR(36) NOT NULL,
  `blacklist_gen` INT NOT NULL DEFAULT 0,
  `updated_at`    DATETIME,
  PRIMARY KEY (`admin_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='後台 Token 黑名單';

-- 016: system_config（系統參數）
CREATE TABLE IF NOT EXISTS `system_config` (
  `id`           VARCHAR(36)  NOT NULL PRIMARY KEY,
  `config_key`   VARCHAR(100) NOT NULL UNIQUE,
  `config_value` VARCHAR(500) NOT NULL,
  `config_type`  VARCHAR(20)  NOT NULL DEFAULT 'STRING' COMMENT 'INTEGER / STRING / BOOLEAN',
  `config_group` VARCHAR(50)  NOT NULL DEFAULT 'GENERAL' COMMENT '參數分組',
  `description`  VARCHAR(500) NULL     COMMENT '參數說明',
  `version`      INT          NOT NULL DEFAULT 0 COMMENT '樂觀鎖版本號',
  `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系統參數設定表';

-- 009: permission_audit_log（權限變更審計）
CREATE TABLE IF NOT EXISTS `permission_audit_log` (
  `id`               VARCHAR(36) NOT NULL PRIMARY KEY,
  `operator_id`      VARCHAR(36) NOT NULL,
  `target_role_id`   VARCHAR(36) NOT NULL,
  `action`           VARCHAR(50) NOT NULL,
  `before_snapshot`  TEXT,
  `after_snapshot`   TEXT,
  `created_at`       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_audit_role`     (`target_role_id`),
  INDEX `idx_audit_operator` (`operator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='權限變更審計日誌';

-- system_log（系統操作日誌）
CREATE TABLE IF NOT EXISTS `system_log` (
  `id`              VARCHAR(36)  NOT NULL,
  `user_id`         VARCHAR(36)  DEFAULT NULL,
  `username`        VARCHAR(100) DEFAULT NULL,
  `action`          VARCHAR(100) NOT NULL,
  `module`          VARCHAR(100) DEFAULT NULL,
  `ip_address`      VARCHAR(50)  DEFAULT NULL,
  `user_agent`      VARCHAR(500) DEFAULT NULL,
  `request_url`     VARCHAR(500) DEFAULT NULL,
  `request_method`  VARCHAR(10)  DEFAULT NULL,
  `request_params`  TEXT         DEFAULT NULL,
  `response_status` INT          DEFAULT NULL,
  `error_message`   TEXT         DEFAULT NULL,
  `execution_time`  BIGINT       DEFAULT NULL,
  `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_user_id`   (`user_id`),
  INDEX `idx_action`    (`action`),
  INDEX `idx_created_at`(`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系統操作日誌表';

-- email_log（郵件發送日誌）
CREATE TABLE IF NOT EXISTS `email_log` (
  `id`              VARCHAR(36)  NOT NULL,
  `recipient_email` VARCHAR(200) NOT NULL,
  `subject`         VARCHAR(500) NOT NULL,
  `content`         TEXT         DEFAULT NULL,
  `template_name`   VARCHAR(100) DEFAULT NULL,
  `status`          VARCHAR(20)  NOT NULL COMMENT 'PENDING/SENT/FAILED',
  `error_message`   TEXT         DEFAULT NULL,
  `sent_at`         DATETIME     DEFAULT NULL,
  `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_recipient_email` (`recipient_email`),
  INDEX `idx_status`          (`status`),
  INDEX `idx_created_at`      (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='郵件發送日誌表';

-- marquee（跑馬燈公告）
CREATE TABLE IF NOT EXISTS `marquee` (
  `id`            VARCHAR(36) NOT NULL,
  `title`         VARCHAR(200) NOT NULL,
  `content`       TEXT         NOT NULL,
  `link_url`      VARCHAR(500) DEFAULT NULL,
  `start_time`    DATETIME     DEFAULT NULL,
  `end_time`      DATETIME     DEFAULT NULL,
  `is_enabled`    TINYINT(1)   NOT NULL DEFAULT 1,
  `display_order` INT          NOT NULL DEFAULT 0,
  `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_is_enabled`    (`is_enabled`),
  INDEX `idx_display_order` (`display_order`),
  INDEX `idx_start_end_time`(`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='跑馬燈公告表';

-- district（行政區域）
CREATE TABLE IF NOT EXISTS `district` (
  `id`            VARCHAR(36) NOT NULL,
  `city`          VARCHAR(50) NOT NULL,
  `district_name` VARCHAR(50) NOT NULL,
  `postal_code`   VARCHAR(10) DEFAULT NULL,
  `display_order` INT         NOT NULL DEFAULT 0,
  `is_active`     TINYINT(1)  NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  INDEX `idx_city`        (`city`),
  INDEX `idx_postal_code` (`postal_code`),
  INDEX `idx_is_active`   (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='行政區域表';

-- report_snapshot（報表快照）
CREATE TABLE IF NOT EXISTS `report_snapshot` (
  `id`            VARCHAR(36) NOT NULL,
  `report_type`   VARCHAR(50) NOT NULL COMMENT 'DAILY/WEEKLY/MONTHLY/YEARLY',
  `report_date`   DATE        NOT NULL,
  `store_id`      VARCHAR(36) DEFAULT NULL,
  `total_revenue` BIGINT      NOT NULL DEFAULT 0,
  `total_orders`  INT         NOT NULL DEFAULT 0,
  `total_users`   INT         NOT NULL DEFAULT 0,
  `total_draws`   INT         NOT NULL DEFAULT 0,
  `data_json`     TEXT        DEFAULT NULL,
  `created_at`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_report` (`report_type`, `report_date`, `store_id`),
  INDEX `idx_report_date` (`report_date`),
  INDEX `idx_store_id`    (`store_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='報表快照表';

-- contact_inquiry（合作諮詢）
CREATE TABLE IF NOT EXISTS `contact_inquiry` (
  `id`               VARCHAR(36)  NOT NULL,
  `company_name`     VARCHAR(100) NOT NULL,
  `contact_name`     VARCHAR(50)  NOT NULL,
  `email`            VARCHAR(100) NOT NULL,
  `phone`            VARCHAR(20)  DEFAULT NULL,
  `cooperation_type` VARCHAR(50)  DEFAULT NULL,
  `description`      TEXT         DEFAULT NULL,
  `status`           VARCHAR(20)  DEFAULT 'PENDING' COMMENT 'PENDING/PROCESSING/COMPLETED/REJECTED',
  `remark`           TEXT         DEFAULT NULL,
  `processed_by`     VARCHAR(36)  DEFAULT NULL,
  `processed_at`     DATETIME     DEFAULT NULL,
  `created_at`       DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `updated_at`       DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_status`     (`status`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合作諮詢表';

-- lottery_theme（商品主題字典）
CREATE TABLE IF NOT EXISTS `lottery_theme` (
  `id`            VARCHAR(36)  NOT NULL,
  `name`          VARCHAR(100) NOT NULL,
  `image_url`     VARCHAR(500) DEFAULT NULL,
  `status`        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/INACTIVE',
  `display_order` INT          NOT NULL DEFAULT 0,
  `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_lottery_theme_name` (`name`),
  INDEX `idx_lottery_theme_status_order` (`status`, `display_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品主題字典（跨店家共享）';

-- lottery_tag（商品標籤字典）
CREATE TABLE IF NOT EXISTS `lottery_tag` (
  `id`            VARCHAR(36)  NOT NULL,
  `name`          VARCHAR(100) NOT NULL,
  `status`        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/INACTIVE',
  `display_order` INT          NOT NULL DEFAULT 0,
  `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_lottery_tag_name` (`name`),
  INDEX `idx_lottery_tag_status_order` (`status`, `display_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品標籤字典（全域）';

-- lottery_theme_alias（主題同義詞）
CREATE TABLE IF NOT EXISTS `lottery_theme_alias` (
  `id`              VARCHAR(36)  NOT NULL,
  `theme_id`        VARCHAR(36)  NOT NULL,
  `alias_name`      VARCHAR(100) NOT NULL,
  `normalized_name` VARCHAR(100) NOT NULL,
  `status`          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/INACTIVE',
  `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_theme_alias_normalized_name` (`normalized_name`),
  INDEX `idx_theme_alias_theme_id` (`theme_id`),
  INDEX `idx_theme_alias_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='主題同義詞對照表';

-- ============================================================
-- PHASE 1：金流相關表（依賴 user 表）
-- ============================================================

-- user_wallet（玩家錢包）
CREATE TABLE IF NOT EXISTS `user_wallet` (
  `id`              VARCHAR(36) NOT NULL PRIMARY KEY,
  `user_id`         VARCHAR(36) NOT NULL UNIQUE,
  `gold_coins`      BIGINT      NOT NULL DEFAULT 0,
  `bonus_coins`     BIGINT      NOT NULL DEFAULT 0,
  `total_recharged` BIGINT      NOT NULL DEFAULT 0,
  `version`         INT         NOT NULL DEFAULT 0,
  `created_at`      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`      DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='玩家錢包';

-- wallet_transaction（錢包異動紀錄）
CREATE TABLE IF NOT EXISTS `wallet_transaction` (
  `id`            VARCHAR(36)  NOT NULL PRIMARY KEY,
  `user_id`       VARCHAR(36)  NOT NULL,
  `coin_type`     VARCHAR(20)  NOT NULL COMMENT 'GOLD / BONUS',
  `type`          VARCHAR(50)  NOT NULL COMMENT 'TransactionTypeEnum',
  `amount`        BIGINT       NOT NULL,
  `balance_before`BIGINT       NOT NULL DEFAULT 0,
  `balance_after` BIGINT       NOT NULL DEFAULT 0,
  `gold_delta`    BIGINT       NULL DEFAULT NULL,
  `bonus_delta`   BIGINT       NULL DEFAULT NULL,
  `gold_after`    BIGINT       NULL DEFAULT NULL,
  `bonus_after`   BIGINT       NULL DEFAULT NULL,
  `reference_id`  VARCHAR(36)  NULL DEFAULT NULL,
  `reason`        VARCHAR(500) NULL DEFAULT NULL,
  `description`   VARCHAR(500) NULL,
  `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_user_id`    (`user_id`),
  INDEX `idx_type`       (`type`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='錢包異動紀錄';

-- recharge_plan（儲值方案）
CREATE TABLE IF NOT EXISTS `recharge_plan` (
  `id`            VARCHAR(36)    NOT NULL PRIMARY KEY,
  `name`          VARCHAR(100)   NOT NULL,
  `gold_amount`   BIGINT         NOT NULL,
  `bonus_amount`  BIGINT         NOT NULL DEFAULT 0,
  `price_twd`     DECIMAL(10,2)  NOT NULL,
  `is_featured`   TINYINT(1)     NOT NULL DEFAULT 0,
  `is_active`     TINYINT(1)     NOT NULL DEFAULT 1,
  `order_num`     INT            DEFAULT 0,
  `start_date`    DATETIME       NULL,
  `end_date`      DATETIME       NULL,
  `deleted_at`    DATETIME       NULL,
  `created_at`    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_is_active`  (`is_active`),
  INDEX `idx_order_num`  (`order_num`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='儲值方案';

-- recharge_order（儲值訂單）
CREATE TABLE IF NOT EXISTS `recharge_order` (
  `id`               VARCHAR(36)   NOT NULL PRIMARY KEY,
  `user_id`          VARCHAR(36)   NOT NULL,
  `plan_id`          VARCHAR(36)   NOT NULL,
  `gold_amount`      BIGINT        NOT NULL,
  `bonus_amount`     BIGINT        NOT NULL DEFAULT 0,
  `price_twd`        DECIMAL(10,2) NOT NULL,
  `status`           VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
  `gateway_provider` VARCHAR(50)   NULL,
  `gateway_order_id` VARCHAR(100)  NULL,
  `gateway_raw_resp` TEXT          NULL,
  `paid_at`          DATETIME      NULL,
  `expired_at`       DATETIME      NOT NULL,
  `created_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_ro_user_id` (`user_id`),
  INDEX `idx_ro_status`  (`status`),
  INDEX `idx_ro_expired` (`expired_at`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='儲值訂單';

-- ============================================================
-- PHASE 2：訂單 / 賞品盒相關表（依賴 user / lottery / store）
-- ============================================================

-- prize_box（賞品盒）
CREATE TABLE IF NOT EXISTS `prize_box` (
  `id`              VARCHAR(36) NOT NULL PRIMARY KEY,
  `user_id`         VARCHAR(36) NOT NULL,
  `lottery_id`      VARCHAR(36) NOT NULL,
  `prize_id`        VARCHAR(36) NOT NULL,
  `store_id`        VARCHAR(36) NOT NULL,
  `draw_result_id`  VARCHAR(36) NULL,
  `status`          VARCHAR(20) NOT NULL DEFAULT 'IN_BOX' COMMENT 'IN_BOX/SHIPPED/RECYCLED',
  `is_recyclable`   TINYINT(1)  DEFAULT 1,
  `recycle_bonus`   BIGINT      NULL,
  `recycled_at`     DATETIME    NULL,
  `shipped_at`      DATETIME    NULL,
  `order_id`        VARCHAR(36) NULL,
  `created_at`      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`      DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_user_status` (`user_id`, `status`),
  INDEX `idx_store`       (`store_id`),
  INDEX `idx_order`       (`order_id`),
  INDEX `idx_created_at`  (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='賞品盒';

-- shipping_method（運送方式）
CREATE TABLE IF NOT EXISTS `shipping_method` (
  `id`         VARCHAR(36)  NOT NULL PRIMARY KEY,
  `name`       VARCHAR(100) NOT NULL COMMENT '運送方式名稱',
  `code`       VARCHAR(50)  NOT NULL UNIQUE COMMENT '代碼',
  `provider`   VARCHAR(100) NULL,
  `fee`        BIGINT       NOT NULL DEFAULT 0,
  `status`     VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
  `sort_order` INT          NOT NULL DEFAULT 0,
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='運送方式管理表';

-- ============================================================
-- PHASE 3：推薦碼 / 地址 / 籤位表
-- ============================================================

-- referral_code（推薦碼）
CREATE TABLE IF NOT EXISTS `referral_code` (
  `id`          VARCHAR(36)  NOT NULL,
  `code`        VARCHAR(50)  NOT NULL,
  `owner_id`    VARCHAR(36)  DEFAULT NULL,
  `owner_type`  VARCHAR(20)  NOT NULL COMMENT 'ADMIN/STORE',
  `store_id`    VARCHAR(36)  NULL,
  `description` VARCHAR(200) NULL,
  `reward_gold` BIGINT       NOT NULL DEFAULT 0,
  `reward_bonus`BIGINT       NOT NULL DEFAULT 0,
  `max_usage`   INT          DEFAULT NULL,
  `used_count`  INT          NOT NULL DEFAULT 0,
  `valid_from`  DATETIME     DEFAULT NULL,
  `valid_until` DATETIME     DEFAULT NULL,
  `is_active`   TINYINT(1)   NOT NULL DEFAULT 1,
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  INDEX `idx_owner`     (`owner_id`, `owner_type`),
  INDEX `idx_store_id`  (`store_id`),
  INDEX `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推薦碼表';

-- referral_record（推薦紀錄）
CREATE TABLE IF NOT EXISTS `referral_record` (
  `id`               VARCHAR(36) NOT NULL,
  `referral_code_id` VARCHAR(36) NOT NULL,
  `referral_code`    VARCHAR(50) NOT NULL COMMENT '推薦碼快照',
  `user_id`          VARCHAR(36) NULL COMMENT '被推薦人 ID',
  `store_id`         VARCHAR(36) NULL,
  `used_code`        VARCHAR(20) NULL COMMENT '使用的推薦碼快照',
  `referred_at`      DATETIME    DEFAULT CURRENT_TIMESTAMP,
  `referrer_id`      VARCHAR(36) NULL,
  `referee_id`       VARCHAR(36) NOT NULL,
  `referee_username` VARCHAR(100) NULL,
  `reward_gold`      BIGINT      NOT NULL DEFAULT 0,
  `reward_bonus`     BIGINT      NOT NULL DEFAULT 0,
  `is_reward_given`  TINYINT(1)  NOT NULL DEFAULT 0,
  `reward_given_at`  DATETIME    NULL,
  `signup_method`    VARCHAR(20) DEFAULT 'EMAIL' COMMENT 'EMAIL/OAUTH',
  `created_at`       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_referral_code_id` (`referral_code_id`),
  INDEX `idx_referrer_id`      (`referrer_id`),
  INDEX `idx_referee_id`       (`referee_id`),
  INDEX `idx_user_id`          (`user_id`),
  INDEX `idx_store_id`         (`store_id`),
  INDEX `idx_created_at`       (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推薦紀錄表';

-- user_address（用戶收件地址）
CREATE TABLE IF NOT EXISTS `user_address` (
  `id`              VARCHAR(36)  NOT NULL,
  `user_id`         VARCHAR(36)  NOT NULL,
  `recipient_name`  VARCHAR(100) NOT NULL,
  `recipient_phone` VARCHAR(20)  NOT NULL,
  `postal_code`     VARCHAR(10)  DEFAULT NULL,
  `city`            VARCHAR(50)  DEFAULT NULL,
  `district`        VARCHAR(50)  DEFAULT NULL,
  `address`         VARCHAR(500) NOT NULL,
  `is_default`      TINYINT(1)   NOT NULL DEFAULT 0,
  `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_user_id`   (`user_id`),
  INDEX `idx_is_default`(`is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用戶地址表';

-- lottery_session（抽獎場次，依賴 lottery）
CREATE TABLE IF NOT EXISTS `lottery_session` (
  `id`                       VARCHAR(36)  NOT NULL PRIMARY KEY,
  `lottery_id`               VARCHAR(36)  NOT NULL,
  `opener_user_id`           VARCHAR(36)  NOT NULL,
  `status`                   VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
  `protection_draws`         INT          NOT NULL DEFAULT 10,
  `protection_end_time`      DATETIME     NULL,
  `opener_draw_count`        INT          NOT NULL DEFAULT 0,
  `free_draw_enabled`        TINYINT(1)   NOT NULL DEFAULT 0,
  `player_designated_numbers` TEXT         NULL,
  `designation_deadline`     DATETIME     NULL COMMENT 'SCRATCH_PLAYER 指定大獎截止時間',
  `created_at`               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`               DATETIME     NULL,
  INDEX `idx_lottery_id`       (`lottery_id`),
  INDEX `idx_opener_user_id`   (`opener_user_id`),
  INDEX `idx_status`           (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽獎場次';

-- lottery_ticket（抽獎籤位，依賴 lottery / lottery_prize）
CREATE TABLE IF NOT EXISTS `lottery_ticket` (
  `id`                  VARCHAR(36) NOT NULL PRIMARY KEY,
  `lottery_id`          VARCHAR(36) NOT NULL,
  `session_id`          VARCHAR(36) NULL,
  `ticket_number`       INT         NOT NULL COMMENT '序號 1~N',
  `revealed_number`     INT         NULL COMMENT '刮開後顯示的亂數（刮刮樂用）',
  `prize_id`            VARCHAR(36) NULL,
  `status`              VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE' COMMENT 'AVAILABLE/DRAWN/LOCKED',
  `is_designated_prize` TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '是否為大獎指定位',
  `drawn_by_user_id`    VARCHAR(36) NULL,
  `drawn_at`            DATETIME    NULL,
  `created_at`          DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_lottery_id`     (`lottery_id`),
  INDEX `idx_session_id`     (`session_id`),
  INDEX `idx_status`         (`status`),
  INDEX `idx_ticket_number`  (`lottery_id`, `ticket_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽獎籤位';

-- ============================================================
-- PHASE 4：ALTER TABLE 新增欄位（MySQL 相容版）
-- 使用 stored procedure 模擬 IF NOT EXISTS
-- ============================================================

DROP PROCEDURE IF EXISTS AddColIfNotExists;
DELIMITER $$
CREATE PROCEDURE AddColIfNotExists(
    IN p_table VARCHAR(64),
    IN p_col   VARCHAR(64),
    IN p_def   TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME  = p_table
          AND COLUMN_NAME = p_col
    ) THEN
        SET @sql = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN `', p_col, '` ', p_def);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

-- ---------- user 表 ----------
CALL AddColIfNotExists('user','line_id',                   'VARCHAR(100) DEFAULT NULL');
CALL AddColIfNotExists('user','recipient_name',             'VARCHAR(100) DEFAULT NULL');
CALL AddColIfNotExists('user','recipient_phone',            'VARCHAR(20)  DEFAULT NULL');
CALL AddColIfNotExists('user','city',                       'VARCHAR(50)  DEFAULT NULL');
CALL AddColIfNotExists('user','district',                   'VARCHAR(50)  DEFAULT NULL');
CALL AddColIfNotExists('user','address_detail',             'VARCHAR(255) DEFAULT NULL');
CALL AddColIfNotExists('user','invoice_type',               'VARCHAR(20)  DEFAULT NULL COMMENT ''PERSONAL/COMPANY''');
CALL AddColIfNotExists('user','invoice_email',              'VARCHAR(100) DEFAULT NULL');
CALL AddColIfNotExists('user','carrier_code',               'VARCHAR(50)  DEFAULT NULL');
CALL AddColIfNotExists('user','tax_id',                     'VARCHAR(20)  DEFAULT NULL');
CALL AddColIfNotExists('user','company_name',               'VARCHAR(100) DEFAULT NULL');
CALL AddColIfNotExists('user','referral_code',              'VARCHAR(50)  DEFAULT NULL COMMENT ''使用的推薦碼''');
CALL AddColIfNotExists('user','referred_store_id',          'VARCHAR(36)  DEFAULT NULL');
CALL AddColIfNotExists('user','referral_bound_at',          'TIMESTAMP    DEFAULT NULL');
CALL AddColIfNotExists('user','is_oauth_new_user',          'TINYINT(1)   DEFAULT 0');
CALL AddColIfNotExists('user','email_verification_token',   'VARCHAR(100) DEFAULT NULL');
CALL AddColIfNotExists('user','email_verification_expires', 'DATETIME     DEFAULT NULL');
CALL AddColIfNotExists('user','password_reset_token',       'VARCHAR(100) DEFAULT NULL');
CALL AddColIfNotExists('user','password_reset_expires',     'DATETIME     DEFAULT NULL');
CALL AddColIfNotExists('user','total_recharged',            'BIGINT       NOT NULL DEFAULT 0');

-- user 表：索引（忽略已存在錯誤）
CREATE INDEX IF NOT EXISTS `idx_user_referral_code`     ON `user`(`referral_code`);
CREATE INDEX IF NOT EXISTS `idx_user_referred_store_id` ON `user`(`referred_store_id`);

-- ---------- lottery 表 ----------
CALL AddColIfNotExists('lottery','play_mode',              'VARCHAR(20)  DEFAULT ''LOTTERY_MODE'' COMMENT ''LOTTERY_MODE/SCRATCH_MODE''');
CALL AddColIfNotExists('lottery','game_mode',              'VARCHAR(20)  NULL COMMENT ''RANDOM/SCRATCH_STORE/SCRATCH_PLAYER''');
CALL AddColIfNotExists('lottery','sub_category',           'VARCHAR(30)  NULL COMMENT ''LOTTERY_MODE/SCRATCH_MODE''');
CALL AddColIfNotExists('lottery','source_lottery_id',      'VARCHAR(36)  NULL');
CALL AddColIfNotExists('lottery','configured_at',          'DATETIME     NULL');
CALL AddColIfNotExists('lottery','drawable_at',            'DATETIME     NULL');
CALL AddColIfNotExists('lottery','remaining_draws',        'INT          NULL');
CALL AddColIfNotExists('lottery','discount_trigger_level', 'VARCHAR(20)  NULL');
CALL AddColIfNotExists('lottery','last_prize_mode',        'VARCHAR(20)  NULL COMMENT ''LAST_DRAW/POOL_IN''');
CALL AddColIfNotExists('lottery','hot_count',              'INT          NOT NULL DEFAULT 0');
CALL AddColIfNotExists('lottery','theme',                  'VARCHAR(50)  NULL');
CALL AddColIfNotExists('lottery','tags',                   'VARCHAR(255) NULL');
CALL AddColIfNotExists('lottery','gallery_urls',           'TEXT         NULL');
CALL AddColIfNotExists('lottery','content',                'TEXT         NULL');
CALL AddColIfNotExists('lottery','bonus_rate',             'DECIMAL(5,2) DEFAULT 0.00');
CALL AddColIfNotExists('lottery','payment_type',           'VARCHAR(20)  NOT NULL DEFAULT ''GOLD''');
CALL AddColIfNotExists('lottery','free_draw_threshold',    'INT          NULL');
CALL AddColIfNotExists('lottery','delist_strategy',        'VARCHAR(30)  NOT NULL DEFAULT ''ALL_DRAWN''');

-- lottery 表：索引
CREATE INDEX IF NOT EXISTS `idx_lottery_scheduled_at` ON `lottery`(`scheduled_at`);
CREATE INDEX IF NOT EXISTS `idx_lottery_start_time`   ON `lottery`(`start_time`);
CREATE INDEX IF NOT EXISTS `idx_lottery_source_id`    ON `lottery`(`source_lottery_id`);

-- lottery 表：MODIFY（放寬 NOT NULL 限制）
ALTER TABLE `lottery`
  MODIFY COLUMN `multi_draw_options` VARCHAR(100) NULL,
  MODIFY COLUMN `allow_multi_draw`   TINYINT(1)   NULL DEFAULT 0,
  MODIFY COLUMN `protection_draws`   INT          NULL,
  MODIFY COLUMN `protection_minutes` INT          NULL;

-- ---------- lottery_prize 表 ----------
CALL AddColIfNotExists('lottery_prize','recycle_bonus','BIGINT NOT NULL DEFAULT 0 COMMENT ''回收獎勵紅利；0=不可回收''');
CREATE INDEX IF NOT EXISTS `idx_prize_lottery_level` ON `lottery_prize`(`lottery_id`, `level`);

-- ---------- news 表 ----------
CALL AddColIfNotExists('news','category',  'VARCHAR(20)  DEFAULT ''ANNOUNCEMENT'' COMMENT ''ALL/ANNOUNCEMENT/EVENT/SYSTEM''');
CALL AddColIfNotExists('news','important', 'TINYINT(1)   DEFAULT 0');

-- ---------- order 表 ----------
CALL AddColIfNotExists('order','shipping_method_id','VARCHAR(36) NULL COMMENT ''FK → shipping_method''');
CALL AddColIfNotExists('order','shipping_fee',       'BIGINT      NOT NULL DEFAULT 0');
CALL AddColIfNotExists('order','payment_method',     'VARCHAR(30) NOT NULL DEFAULT ''STUB'' COMMENT ''STUB/MASTERCARD/GOLD_COIN''');
CALL AddColIfNotExists('order','payment_status',     'VARCHAR(20) NOT NULL DEFAULT ''SUCCESS'' COMMENT ''PENDING/SUCCESS/FAILED/CANCELLED''');

-- ---------- order_item 表 ----------
CALL AddColIfNotExists('order_item','prize_grade','VARCHAR(10)  NULL');
CALL AddColIfNotExists('order_item','prize_image','VARCHAR(500) NULL');

-- ---------- store 表 ----------
CALL AddColIfNotExists('store','created_by','VARCHAR(36) NULL COMMENT ''建立者 ID''');

-- ---------- prize_box 表 ----------
CALL AddColIfNotExists('prize_box','is_shippable','TINYINT(1) NOT NULL DEFAULT 1 COMMENT ''是否可出貨（1=可, 0=不可）''');

-- ---------- wallet_transaction 表 ----------
CALL AddColIfNotExists('wallet_transaction','gold_delta',   'BIGINT       NULL DEFAULT NULL');
CALL AddColIfNotExists('wallet_transaction','bonus_delta',  'BIGINT       NULL DEFAULT NULL');
CALL AddColIfNotExists('wallet_transaction','gold_after',   'BIGINT       NULL DEFAULT NULL');
CALL AddColIfNotExists('wallet_transaction','bonus_after',  'BIGINT       NULL DEFAULT NULL');
CALL AddColIfNotExists('wallet_transaction','reference_id', 'VARCHAR(36)  NULL DEFAULT NULL');
CALL AddColIfNotExists('wallet_transaction','reason',       'VARCHAR(500) NULL DEFAULT NULL');

DROP PROCEDURE IF EXISTS AddColIfNotExists;

-- ============================================================
-- PHASE 5：欄位改名（CHANGE COLUMN，需確認舊欄位是否存在）
-- ⚠️ 若已執行過則會報錯，請先 DESCRIBE 對應表確認後再執行
-- ============================================================

-- 確認方式：
--   DESCRIBE `order`;         → 看是 order_no 還是 order_number
--   DESCRIBE `recharge_plan`; → 看是 display_order 還是 order_num

-- 【order 表】 order_no → order_number, shipping_status → status
-- 若 order_no 欄位存在才執行：
-- ALTER TABLE `order` CHANGE COLUMN `order_no` `order_number` VARCHAR(50) NOT NULL UNIQUE COMMENT '訂單編號';
-- ALTER TABLE `order` CHANGE COLUMN `shipping_status` `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING';

-- 【recharge_plan 表】 display_order → order_num, start_time → start_date, end_time → end_date
-- 若 display_order 欄位存在才執行：
-- ALTER TABLE `recharge_plan` CHANGE COLUMN `display_order` `order_num` INT DEFAULT 0;
-- ALTER TABLE `recharge_plan` CHANGE COLUMN `start_time` `start_date` DATETIME;
-- ALTER TABLE `recharge_plan` CHANGE COLUMN `end_time` `end_date` DATETIME;
-- ALTER TABLE `recharge_plan` DROP COLUMN `is_promotional`;  -- 若欄位存在
-- CALL AddColIfNotExists('recharge_plan','deleted_at','DATETIME NULL');

-- ============================================================
-- PHASE 6：初始資料 INSERT（INSERT IGNORE 防重複）
-- ============================================================

-- system_config 預設參數
INSERT IGNORE INTO `system_config` (`id`, `config_key`, `config_value`, `config_type`, `config_group`, `description`) VALUES
  (UUID(), 'protection_initial_minutes',   '5',  'INTEGER', 'DRAW', '保護初始時間（分鐘）'),
  (UUID(), 'protection_extension_minutes', '2',  'INTEGER', 'DRAW', '每次操作延長時間（分鐘）'),
  (UUID(), 'protection_max_minutes',       '10', 'INTEGER', 'DRAW', '保護最大時間（分鐘）'),
  (UUID(), 'max_draws_per_request',        '10', 'INTEGER', 'DRAW', '單次 API 最大抽獎數');

-- shipping_method 預設資料
INSERT IGNORE INTO `shipping_method` (`id`, `name`, `code`, `provider`, `fee`, `status`, `sort_order`) VALUES
  (UUID(), '宅配到府',  'HOME_DELIVERY', '黑貓宅急便', 100, 'ACTIVE', 1),
  (UUID(), '7-11 取貨', 'SEVEN_ELEVEN',  '綠界',        60, 'ACTIVE', 2),
  (UUID(), '全家取貨',  'FAMILY_MART',   '綠界',        60, 'ACTIVE', 3);

-- marquee 初始公告
INSERT IGNORE INTO `marquee` (`id`, `title`, `content`, `start_time`, `end_time`, `is_enabled`, `display_order`) VALUES
  (UUID(), '歡迎來到 KUJI 一番賞', '全台最大的一番賞線上抽獎平台！', NOW(), DATE_ADD(NOW(), INTERVAL 365 DAY), 1, 1);

-- lottery_theme / lottery_tag 舊資料回填
-- 說明：將既有 lottery.theme / lottery.tags 內容同步灌入字典表，避免上線後前台分類為空
INSERT IGNORE INTO `lottery_theme` (`id`, `name`, `image_url`, `status`, `display_order`, `created_at`, `updated_at`)
SELECT
  UUID(),
  TRIM(l.`theme`) AS `name`,
  NULL,
  'ACTIVE',
  0,
  NOW(),
  NOW()
FROM `lottery` l
WHERE l.`theme` IS NOT NULL
  AND TRIM(l.`theme`) <> '';

-- tags = JSON array（例：["火影","動漫"]）
INSERT IGNORE INTO `lottery_tag` (`id`, `name`, `status`, `display_order`, `created_at`, `updated_at`)
SELECT
  UUID(),
  TRIM(jt.`tag`) AS `name`,
  'ACTIVE',
  0,
  NOW(),
  NOW()
FROM `lottery` l
JOIN JSON_TABLE(
  l.`tags`,
  '$[*]' COLUMNS (
    `tag` VARCHAR(100) PATH '$'
  )
) jt
WHERE l.`tags` IS NOT NULL
  AND TRIM(l.`tags`) <> ''
  AND JSON_VALID(l.`tags`)
  AND JSON_TYPE(CAST(l.`tags` AS JSON)) = 'ARRAY'
  AND TRIM(jt.`tag`) <> '';

-- tags = 逗號字串（例：火影,動漫）
INSERT IGNORE INTO `lottery_tag` (`id`, `name`, `status`, `display_order`, `created_at`, `updated_at`)
SELECT
  UUID(),
  TRIM(jt.`tag`) AS `name`,
  'ACTIVE',
  0,
  NOW(),
  NOW()
FROM `lottery` l
JOIN JSON_TABLE(
  CONCAT(
    '["',
    REPLACE(
      REPLACE(
        REPLACE(TRIM(l.`tags`), '\\', '\\\\'),
        '"', '\\"'
      ),
      ',',
      '","'
    ),
    '"]'
  ),
  '$[*]' COLUMNS (
    `tag` VARCHAR(100) PATH '$'
  )
) jt
WHERE l.`tags` IS NOT NULL
  AND TRIM(l.`tags`) <> ''
  AND (
    NOT JSON_VALID(l.`tags`)
    OR JSON_TYPE(CAST(l.`tags` AS JSON)) <> 'ARRAY'
  )
  AND TRIM(jt.`tag`) <> '';

-- district 初始資料（台北市）
INSERT IGNORE INTO `district` (`id`, `city`, `district_name`, `postal_code`, `display_order`, `is_active`) VALUES
  (UUID(), '台北市', '中正區', '100', 1, 1), (UUID(), '台北市', '大同區', '103', 2, 1),
  (UUID(), '台北市', '中山區', '104', 3, 1), (UUID(), '台北市', '松山區', '105', 4, 1),
  (UUID(), '台北市', '大安區', '106', 5, 1), (UUID(), '台北市', '萬華區', '108', 6, 1),
  (UUID(), '台北市', '信義區', '110', 7, 1), (UUID(), '台北市', '士林區', '111', 8, 1),
  (UUID(), '台北市', '北投區', '112', 9, 1), (UUID(), '台北市', '內湖區', '114', 10, 1),
  (UUID(), '台北市', '南港區', '115', 11, 1), (UUID(), '台北市', '文山區', '116', 12, 1);

-- ============================================================
-- PHASE 7：資料正規化（僅在有舊資料時需要）
-- ============================================================

-- V017：enum 值統一改大寫
UPDATE `wallet_transaction` SET `coin_type`   = UPPER(`coin_type`)   WHERE `coin_type`   IN ('gold', 'bonus');
UPDATE `point_log`           SET `point_type`  = UPPER(`point_type`)  WHERE `point_type`  IN ('gold', 'bonus');

-- ============================================================
-- PHASE 8：索引補充（Unique，確認重複資料後再執行）
-- ============================================================

-- referral_record：每個 user 只能被推薦一次
-- ⚠️ 若 user_id 有重複資料，此指令會失敗，請先清理
-- ALTER TABLE `referral_record` ADD UNIQUE INDEX `idx_referral_record_user_id` (`user_id`);

-- ============================================================
-- PHASE 9：資料品質檢查（健康檢查 / 重複主題候選）
-- ============================================================

-- 1) 字典資料量
-- SELECT COUNT(*) AS active_theme_count FROM lottery_theme WHERE status = 'ACTIVE';
-- SELECT COUNT(*) AS active_tag_count   FROM lottery_tag   WHERE status = 'ACTIVE';

-- 2) 疑似重複主題（忽略大小寫 + 空白正規化）
-- SELECT
--   LOWER(REGEXP_REPLACE(TRIM(name), '\\s+', ' ')) AS normalized_name,
--   COUNT(*) AS cnt,
--   GROUP_CONCAT(name ORDER BY name SEPARATOR ' | ') AS duplicated_names
-- FROM lottery_theme
-- WHERE status = 'ACTIVE'
-- GROUP BY LOWER(REGEXP_REPLACE(TRIM(name), '\\s+', ' '))
-- HAVING COUNT(*) > 1;

-- 3) 商品 theme 未收錄字典
-- SELECT DISTINCT l.theme
-- FROM lottery l
-- LEFT JOIN lottery_theme t ON t.name = l.theme AND t.status = 'ACTIVE'
-- WHERE l.theme IS NOT NULL
--   AND TRIM(l.theme) <> ''
--   AND t.id IS NULL;

-- 4) 商品 tags 含非法值（不在 ACTIVE 字典）
-- SELECT l.id AS lottery_id, l.title, l.tags
-- FROM lottery l
-- WHERE l.tags IS NOT NULL
--   AND TRIM(l.tags) <> ''
--   AND EXISTS (
--     SELECT 1
--     FROM JSON_TABLE(
--       CASE
--         WHEN JSON_VALID(l.tags) AND JSON_TYPE(CAST(l.tags AS JSON)) = 'ARRAY' THEN l.tags
--         ELSE CONCAT(
--           '["',
--           REPLACE(REPLACE(REPLACE(TRIM(l.tags), '\\\\', '\\\\\\\\'), '"', '\\\\"'), ',', '","'),
--           '"]'
--         )
--       END,
--       '$[*]' COLUMNS (tag VARCHAR(100) PATH '$')
--     ) jt
--     LEFT JOIN lottery_tag tg
--       ON LOWER(tg.name) = LOWER(TRIM(jt.tag))
--      AND tg.status = 'ACTIVE'
--     WHERE TRIM(jt.tag) <> ''
--       AND tg.id IS NULL
--   );

-- ============================================================
-- 完成！
-- 執行後建議：mvn mybatis-generator:generate 重新生成 Entity
-- ============================================================
