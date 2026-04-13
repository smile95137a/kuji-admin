CREATE TABLE IF NOT EXISTS `system_config` (
  `id`            VARCHAR(36)   NOT NULL PRIMARY KEY,
  `config_key`    VARCHAR(100)  NOT NULL UNIQUE,
  `config_value`  VARCHAR(500)  NOT NULL,
  `config_type`   VARCHAR(20)   NOT NULL DEFAULT 'STRING' COMMENT 'INTEGER / STRING / BOOLEAN',
  `config_group`  VARCHAR(50)   NOT NULL DEFAULT 'GENERAL' COMMENT '參數分組',
  `description`   VARCHAR(500)  NULL COMMENT '參數說明',
  `version`       INT           NOT NULL DEFAULT 0 COMMENT '樂觀鎖版本號',
  `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系統參數設定表';

INSERT INTO `system_config` (`id`, `config_key`, `config_value`, `config_type`, `config_group`, `description`)
SELECT UUID(), 'protection_initial_minutes', '5', 'INTEGER', 'DRAW', '保護初始時間（分鐘）'
WHERE NOT EXISTS (SELECT 1 FROM `system_config` WHERE config_key = 'protection_initial_minutes');

INSERT INTO `system_config` (`id`, `config_key`, `config_value`, `config_type`, `config_group`, `description`)
SELECT UUID(), 'protection_extension_minutes', '2', 'INTEGER', 'DRAW', '每次操作延長時間（分鐘）'
WHERE NOT EXISTS (SELECT 1 FROM `system_config` WHERE config_key = 'protection_extension_minutes');

INSERT INTO `system_config` (`id`, `config_key`, `config_value`, `config_type`, `config_group`, `description`)
SELECT UUID(), 'protection_max_minutes', '10', 'INTEGER', 'DRAW', '保護最大時間（分鐘）'
WHERE NOT EXISTS (SELECT 1 FROM `system_config` WHERE config_key = 'protection_max_minutes');

INSERT INTO `system_config` (`id`, `config_key`, `config_value`, `config_type`, `config_group`, `description`)
SELECT UUID(), 'max_draws_per_request', '10', 'INTEGER', 'DRAW', '單次 API 最大抽獎數'
WHERE NOT EXISTS (SELECT 1 FROM `system_config` WHERE config_key = 'max_draws_per_request');
