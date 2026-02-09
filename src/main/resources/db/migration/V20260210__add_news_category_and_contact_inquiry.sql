-- ==========================================
-- 1. News 表新增 category 和 important 欄位
-- ==========================================
ALTER TABLE `news` ADD COLUMN `category` VARCHAR(20) DEFAULT 'ANNOUNCEMENT' COMMENT '分類：ALL/ANNOUNCEMENT/EVENT/SYSTEM' AFTER `status`;
ALTER TABLE `news` ADD COLUMN `important` TINYINT(1) DEFAULT 0 COMMENT '是否為重要提醒：0=否, 1=是' AFTER `category`;

-- ==========================================
-- 2. 建立合作諮詢表 contact_inquiry
-- ==========================================
CREATE TABLE IF NOT EXISTS `contact_inquiry` (
    `id` VARCHAR(36) NOT NULL COMMENT '主鍵 UUID',
    `company_name` VARCHAR(100) NOT NULL COMMENT '公司名稱',
    `contact_name` VARCHAR(50) NOT NULL COMMENT '聯絡人姓名',
    `email` VARCHAR(100) NOT NULL COMMENT '電子信箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '連絡電話',
    `cooperation_type` VARCHAR(50) DEFAULT NULL COMMENT '合作類型',
    `description` TEXT DEFAULT NULL COMMENT '需求簡述',
    `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '處理狀態：PENDING/PROCESSING/COMPLETED/REJECTED',
    `remark` TEXT DEFAULT NULL COMMENT '後台備註',
    `processed_by` VARCHAR(36) DEFAULT NULL COMMENT '處理人 ID',
    `processed_at` DATETIME DEFAULT NULL COMMENT '處理時間',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    PRIMARY KEY (`id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='合作諮詢表';

-- ==========================================
-- 3. 建立消費紀錄表 consumption_record
-- ==========================================
CREATE TABLE IF NOT EXISTS `consumption_record` (
    `id` VARCHAR(36) NOT NULL COMMENT '主鍵 UUID',
    `user_id` VARCHAR(36) NOT NULL COMMENT '用戶 ID',
    `type` VARCHAR(30) NOT NULL COMMENT '消費類型：DRAW_GOLD/DRAW_BONUS/SHIPPING_FEE',
    `lottery_id` VARCHAR(36) DEFAULT NULL COMMENT '相關賞品 ID',
    `lottery_title` VARCHAR(200) DEFAULT NULL COMMENT '賞品名稱',
    `order_id` VARCHAR(36) DEFAULT NULL COMMENT '相關訂單 ID',
    `order_number` VARCHAR(50) DEFAULT NULL COMMENT '訂單編號',
    `gold_amount` BIGINT DEFAULT 0 COMMENT '消費金幣數',
    `bonus_amount` BIGINT DEFAULT 0 COMMENT '消費紅利數',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '消費說明',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_type` (`type`),
    INDEX `idx_created_at` (`created_at`),
    INDEX `idx_lottery_id` (`lottery_id`),
    INDEX `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消費紀錄表（金幣紅利消費+運費支付）';

-- ==========================================
-- 4. lottery_prize 表新增 content 欄位
-- ==========================================
ALTER TABLE `lottery_prize` ADD COLUMN `content` TEXT DEFAULT NULL COMMENT '獎項詳細內容' AFTER `description`;
