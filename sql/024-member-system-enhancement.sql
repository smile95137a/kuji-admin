-- ============================================================
-- Feature 024: 會員系統完善
-- ============================================================

-- 1. user 表新增帳號鎖定欄位
ALTER TABLE `user`
    ADD COLUMN IF NOT EXISTS `failed_login_attempts` INT         NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS `locked_until`          DATETIME    DEFAULT NULL;

-- 2. admin_user 表新增帳號鎖定欄位
ALTER TABLE `admin_user`
    ADD COLUMN IF NOT EXISTS `failed_login_attempts` INT         NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS `locked_until`          DATETIME    DEFAULT NULL;

-- 3. user_token_blacklist（前台 token 世代計數器，與後台 admin_token_blacklist 對稱）
CREATE TABLE IF NOT EXISTS `user_token_blacklist` (
    `user_id`       VARCHAR(36)  NOT NULL,
    `blacklist_gen` INT          NOT NULL DEFAULT 0,
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. user_login_history（前後台登入記錄）
CREATE TABLE IF NOT EXISTS `user_login_history` (
    `id`           VARCHAR(36)  NOT NULL,
    `user_id`      VARCHAR(36)  NOT NULL,
    `user_type`    VARCHAR(20)  NOT NULL COMMENT 'user / admin',
    `login_time`   DATETIME     NOT NULL,
    `ip_address`   VARCHAR(45)  DEFAULT NULL,
    `device_info`  VARCHAR(500) DEFAULT NULL  COMMENT 'User-Agent',
    `login_method` VARCHAR(20)  DEFAULT NULL  COMMENT 'EMAIL / GOOGLE / FACEBOOK',
    `status`       VARCHAR(20)  NOT NULL      COMMENT 'SUCCESS / FAILED / LOCKED',
    `fail_reason`  VARCHAR(200) DEFAULT NULL,
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_login_history_user_id` (`user_id`),
    KEY `idx_user_login_history_login_time` (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. admin_audit_log（後台操作審計日誌）
CREATE TABLE IF NOT EXISTS `admin_audit_log` (
    `id`            VARCHAR(36)  NOT NULL,
    `operator_id`   VARCHAR(36)  NOT NULL COMMENT '操作者 admin_user.id',
    `operator_name` VARCHAR(100) DEFAULT NULL,
    `action`        VARCHAR(100) NOT NULL  COMMENT '動作代碼，如 USER_COIN_ADJUST',
    `target_type`   VARCHAR(50)  DEFAULT NULL COMMENT '操作對象類型，如 user / store',
    `target_id`     VARCHAR(36)  DEFAULT NULL,
    `before_value`  TEXT         DEFAULT NULL COMMENT '修改前的值 (JSON)',
    `after_value`   TEXT         DEFAULT NULL COMMENT '修改後的值 (JSON)',
    `remark`        VARCHAR(500) DEFAULT NULL,
    `ip_address`    VARCHAR(45)  DEFAULT NULL,
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_admin_audit_log_operator` (`operator_id`),
    KEY `idx_admin_audit_log_target` (`target_type`, `target_id`),
    KEY `idx_admin_audit_log_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
