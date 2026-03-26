-- ========================================
-- 缺少的表 DDL（KUJI 專案）
-- 執行前請確認資料庫連線
-- ========================================

-- 1. 系統日誌表
CREATE TABLE IF NOT EXISTS `system_log` (
  `id` VARCHAR(36) NOT NULL COMMENT '日誌 ID',
  `user_id` VARCHAR(36) DEFAULT NULL COMMENT '操作用戶 ID',
  `username` VARCHAR(100) DEFAULT NULL COMMENT '操作用戶名稱',
  `action` VARCHAR(100) NOT NULL COMMENT '操作動作',
  `module` VARCHAR(100) DEFAULT NULL COMMENT '操作模組',
  `ip_address` VARCHAR(50) DEFAULT NULL COMMENT 'IP 地址',
  `user_agent` VARCHAR(500) DEFAULT NULL COMMENT '用戶代理',
  `request_url` VARCHAR(500) DEFAULT NULL COMMENT '請求 URL',
  `request_method` VARCHAR(10) DEFAULT NULL COMMENT '請求方法（GET/POST/PUT/DELETE）',
  `request_params` TEXT DEFAULT NULL COMMENT '請求參數',
  `response_status` INT DEFAULT NULL COMMENT '回應狀態碼',
  `error_message` TEXT DEFAULT NULL COMMENT '錯誤訊息',
  `execution_time` BIGINT DEFAULT NULL COMMENT '執行時間（毫秒）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_action` (`action`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系統操作日誌表';

-- 2. 用戶地址表
CREATE TABLE IF NOT EXISTS `user_address` (
  `id` VARCHAR(36) NOT NULL COMMENT '地址 ID',
  `user_id` VARCHAR(36) NOT NULL COMMENT '用戶 ID',
  `recipient_name` VARCHAR(100) NOT NULL COMMENT '收件人姓名',
  `recipient_phone` VARCHAR(20) NOT NULL COMMENT '收件人電話',
  `postal_code` VARCHAR(10) DEFAULT NULL COMMENT '郵遞區號',
  `city` VARCHAR(50) DEFAULT NULL COMMENT '城市',
  `district` VARCHAR(50) DEFAULT NULL COMMENT '區域',
  `address` VARCHAR(500) NOT NULL COMMENT '詳細地址',
  `is_default` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否為預設地址（0=否, 1=是）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_is_default` (`is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用戶地址表';

-- 3. 跑馬燈表
CREATE TABLE IF NOT EXISTS `marquee` (
  `id` VARCHAR(36) NOT NULL COMMENT '跑馬燈 ID',
  `title` VARCHAR(200) NOT NULL COMMENT '標題',
  `content` TEXT NOT NULL COMMENT '內容',
  `link_url` VARCHAR(500) DEFAULT NULL COMMENT '連結 URL',
  `start_time` DATETIME DEFAULT NULL COMMENT '開始顯示時間',
  `end_time` DATETIME DEFAULT NULL COMMENT '結束顯示時間',
  `is_enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否啟用（0=停用, 1=啟用）',
  `display_order` INT NOT NULL DEFAULT 0 COMMENT '顯示順序（數字越小越前面）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
  PRIMARY KEY (`id`),
  INDEX `idx_is_enabled` (`is_enabled`),
  INDEX `idx_display_order` (`display_order`),
  INDEX `idx_start_end_time` (`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='跑馬燈公告表';

-- 4. 推薦碼表
CREATE TABLE IF NOT EXISTS `referral_code` (
  `id` VARCHAR(36) NOT NULL COMMENT '推薦碼 ID',
  `code` VARCHAR(50) NOT NULL COMMENT '推薦碼（唯一）',
  `owner_id` VARCHAR(36) DEFAULT NULL COMMENT '擁有者 ID（可以是 admin 或 store）',
  `owner_type` VARCHAR(20) NOT NULL COMMENT '擁有者類型（ADMIN/STORE）',
  `reward_gold` BIGINT NOT NULL DEFAULT 0 COMMENT '推薦者獲得的 Gold',
  `reward_bonus` BIGINT NOT NULL DEFAULT 0 COMMENT '被推薦者獲得的 Bonus',
  `max_usage` INT DEFAULT NULL COMMENT '最大使用次數（NULL=無限制）',
  `used_count` INT NOT NULL DEFAULT 0 COMMENT '已使用次數',
  `valid_from` DATETIME DEFAULT NULL COMMENT '有效期起始',
  `valid_until` DATETIME DEFAULT NULL COMMENT '有效期結束',
  `is_active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否啟用（0=停用, 1=啟用）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  INDEX `idx_owner` (`owner_id`, `owner_type`),
  INDEX `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='推薦碼表';

-- 5. 推薦記錄表
CREATE TABLE IF NOT EXISTS `referral_record` (
  `id` VARCHAR(36) NOT NULL COMMENT '記錄 ID',
  `referral_code_id` VARCHAR(36) NOT NULL COMMENT '推薦碼 ID',
  `referral_code` VARCHAR(50) NOT NULL COMMENT '推薦碼（冗餘，方便查詢）',
  `referrer_id` VARCHAR(36) DEFAULT NULL COMMENT '推薦人 ID',
  `referee_id` VARCHAR(36) NOT NULL COMMENT '被推薦人 ID（註冊的用戶）',
  `referee_username` VARCHAR(100) DEFAULT NULL COMMENT '被推薦人用戶名',
  `reward_gold` BIGINT NOT NULL DEFAULT 0 COMMENT '推薦者獲得的 Gold',
  `reward_bonus` BIGINT NOT NULL DEFAULT 0 COMMENT '被推薦者獲得的 Bonus',
  `is_reward_given` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已發放獎勵（0=否, 1=是）',
  `reward_given_at` DATETIME DEFAULT NULL COMMENT '獎勵發放時間',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
  PRIMARY KEY (`id`),
  INDEX `idx_referral_code_id` (`referral_code_id`),
  INDEX `idx_referrer_id` (`referrer_id`),
  INDEX `idx_referee_id` (`referee_id`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='推薦記錄表';

-- 6. 郵件日誌表
CREATE TABLE IF NOT EXISTS `email_log` (
  `id` VARCHAR(36) NOT NULL COMMENT '日誌 ID',
  `recipient_email` VARCHAR(200) NOT NULL COMMENT '收件人 Email',
  `subject` VARCHAR(500) NOT NULL COMMENT '郵件主題',
  `content` TEXT DEFAULT NULL COMMENT '郵件內容',
  `template_name` VARCHAR(100) DEFAULT NULL COMMENT '使用的範本名稱',
  `status` VARCHAR(20) NOT NULL COMMENT '狀態（PENDING/SENT/FAILED）',
  `error_message` TEXT DEFAULT NULL COMMENT '錯誤訊息',
  `sent_at` DATETIME DEFAULT NULL COMMENT '發送時間',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
  PRIMARY KEY (`id`),
  INDEX `idx_recipient_email` (`recipient_email`),
  INDEX `idx_status` (`status`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='郵件發送日誌表';

-- 7. 行政區域表
CREATE TABLE IF NOT EXISTS `district` (
  `id` VARCHAR(36) NOT NULL COMMENT '區域 ID',
  `city` VARCHAR(50) NOT NULL COMMENT '城市',
  `district_name` VARCHAR(50) NOT NULL COMMENT '區域名稱',
  `postal_code` VARCHAR(10) DEFAULT NULL COMMENT '郵遞區號',
  `display_order` INT NOT NULL DEFAULT 0 COMMENT '顯示順序',
  `is_active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否啟用（0=停用, 1=啟用）',
  PRIMARY KEY (`id`),
  INDEX `idx_city` (`city`),
  INDEX `idx_postal_code` (`postal_code`),
  INDEX `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='行政區域表';

-- 8. 報表快照表
CREATE TABLE IF NOT EXISTS `report_snapshot` (
  `id` VARCHAR(36) NOT NULL COMMENT '快照 ID',
  `report_type` VARCHAR(50) NOT NULL COMMENT '報表類型（DAILY/WEEKLY/MONTHLY/YEARLY）',
  `report_date` DATE NOT NULL COMMENT '報表日期',
  `store_id` VARCHAR(36) DEFAULT NULL COMMENT '店家 ID（NULL=全站報表）',
  `total_revenue` BIGINT NOT NULL DEFAULT 0 COMMENT '總營收',
  `total_orders` INT NOT NULL DEFAULT 0 COMMENT '總訂單數',
  `total_users` INT NOT NULL DEFAULT 0 COMMENT '總用戶數',
  `total_draws` INT NOT NULL DEFAULT 0 COMMENT '總抽獎次數',
  `data_json` TEXT DEFAULT NULL COMMENT '詳細數據（JSON 格式）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_report` (`report_type`, `report_date`, `store_id`),
  INDEX `idx_report_date` (`report_date`),
  INDEX `idx_store_id` (`store_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='報表快照表';

-- ========================================
-- 插入範例數據（可選）
-- ========================================

-- 台灣行政區域範例（台北市）
INSERT INTO `district` (`id`, `city`, `district_name`, `postal_code`, `display_order`, `is_active`) VALUES
(UUID(), '台北市', '中正區', '100', 1, 1),
(UUID(), '台北市', '大同區', '103', 2, 1),
(UUID(), '台北市', '中山區', '104', 3, 1),
(UUID(), '台北市', '松山區', '105', 4, 1),
(UUID(), '台北市', '大安區', '106', 5, 1),
(UUID(), '台北市', '萬華區', '108', 6, 1),
(UUID(), '台北市', '信義區', '110', 7, 1),
(UUID(), '台北市', '士林區', '111', 8, 1),
(UUID(), '台北市', '北投區', '112', 9, 1),
(UUID(), '台北市', '內湖區', '114', 10, 1),
(UUID(), '台北市', '南港區', '115', 11, 1),
(UUID(), '台北市', '文山區', '116', 12, 1);

-- 跑馬燈範例
INSERT INTO `marquee` (`id`, `title`, `content`, `link_url`, `start_time`, `end_time`, `is_enabled`, `display_order`) VALUES
(UUID(), '歡迎來到 KUJI 一番賞', '全台最大的一番賞線上抽獎平台！', NULL, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 1, 1),
(UUID(), '新年優惠活動', '新年期間所有商品 85 折！', NULL, NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 1, 2);

-- ========================================
-- 執行完成後，請執行以下命令重新生成 Entity
-- ========================================
-- cd c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin
-- mvn mybatis-generator:generate
-- ========================================
