-- 035: 確保商品主題字典表存在
-- 用途：
-- 1. 補齊 lottery_theme / lottery_theme_alias / lottery_tag 三張分類字典表
-- 2. 從既有 lottery.theme 回填主題字典
-- 3. 建立 canonical 主題名稱自身的 alias，讓前台 /category/themes 與後台商品新增頁可正常解析

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品主題字典';

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品主題同義詞';

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品標籤字典';

INSERT IGNORE INTO `lottery_theme` (`id`, `name`, `image_url`, `status`, `display_order`, `created_at`, `updated_at`)
SELECT
  UUID(),
  TRIM(l.`theme`) AS `name`,
  MAX(NULLIF(TRIM(l.`image_url`), '')) AS `image_url`,
  'ACTIVE',
  0,
  NOW(),
  NOW()
FROM `lottery` l
WHERE l.`theme` IS NOT NULL
  AND TRIM(l.`theme`) <> ''
GROUP BY TRIM(l.`theme`);

INSERT IGNORE INTO `lottery_theme_alias` (`id`, `theme_id`, `alias_name`, `normalized_name`, `status`, `created_at`, `updated_at`)
SELECT
  UUID(),
  t.`id`,
  t.`name`,
  LOWER(TRIM(t.`name`)),
  'ACTIVE',
  NOW(),
  NOW()
FROM `lottery_theme` t
WHERE t.`status` = 'ACTIVE';
