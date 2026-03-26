-- =====================================================
-- 會員系統擴充 & 日誌系統 & 報表基礎 SQL
-- 執行日期: 2026-01-15
-- =====================================================

-- 1. 擴充 user 表 - 新增地址與發票欄位
ALTER TABLE `user` 
ADD COLUMN `line_id` VARCHAR(100) DEFAULT NULL COMMENT 'LINE ID' AFTER `phone_number`,
ADD COLUMN `recipient_name` VARCHAR(100) DEFAULT NULL COMMENT '收貨人姓名' AFTER `line_id`,
ADD COLUMN `recipient_phone` VARCHAR(20) DEFAULT NULL COMMENT '收貨人電話' AFTER `recipient_name`,
ADD COLUMN `city` VARCHAR(50) DEFAULT NULL COMMENT '縣市' AFTER `recipient_phone`,
ADD COLUMN `district` VARCHAR(50) DEFAULT NULL COMMENT '行政區' AFTER `city`,
ADD COLUMN `address_detail` VARCHAR(255) DEFAULT NULL COMMENT '詳細地址' AFTER `district`,
ADD COLUMN `invoice_type` VARCHAR(20) DEFAULT NULL COMMENT '發票類型: PERSONAL/COMPANY' AFTER `address_detail`,
ADD COLUMN `invoice_email` VARCHAR(100) DEFAULT NULL COMMENT '接收發票信箱' AFTER `invoice_type`,
ADD COLUMN `carrier_code` VARCHAR(50) DEFAULT NULL COMMENT '載具條碼' AFTER `invoice_email`,
ADD COLUMN `tax_id` VARCHAR(20) DEFAULT NULL COMMENT '統一編號（公司用）' AFTER `carrier_code`,
ADD COLUMN `company_name` VARCHAR(100) DEFAULT NULL COMMENT '公司名稱' AFTER `tax_id`,
ADD COLUMN `referral_code` VARCHAR(50) DEFAULT NULL COMMENT '使用的推薦碼' AFTER `company_name`,
ADD COLUMN `referred_store_id` VARCHAR(50) DEFAULT NULL COMMENT '推薦來源店家ID' AFTER `referral_code`,
ADD COLUMN `email_verification_token` VARCHAR(100) DEFAULT NULL COMMENT '信箱驗證 token' AFTER `referred_store_id`,
ADD COLUMN `email_verification_expires` DATETIME DEFAULT NULL COMMENT '驗證 token 過期時間' AFTER `email_verification_token`,
ADD COLUMN `password_reset_token` VARCHAR(100) DEFAULT NULL COMMENT '密碼重設 token' AFTER `email_verification_expires`,
ADD COLUMN `password_reset_expires` DATETIME DEFAULT NULL COMMENT '重設 token 過期時間' AFTER `password_reset_token`;

-- 2. 建立行政區資料表（台灣縣市區域）
CREATE TABLE IF NOT EXISTS `district` (
    `id` VARCHAR(50) NOT NULL COMMENT '主鍵',
    `city` VARCHAR(50) NOT NULL COMMENT '縣市',
    `district_name` VARCHAR(50) NOT NULL COMMENT '行政區名稱',
    `zip_code` VARCHAR(10) DEFAULT NULL COMMENT '郵遞區號',
    `order_num` INT DEFAULT 0 COMMENT '排序',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_city` (`city`),
    INDEX `idx_district` (`district_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='行政區資料表';

-- 3. 系統操作日誌表
CREATE TABLE IF NOT EXISTS `system_log` (
    `id` VARCHAR(50) NOT NULL COMMENT '主鍵',
    `log_type` VARCHAR(50) NOT NULL COMMENT '日誌類型: AUTH/TRANSACTION/ORDER/ERROR/EMAIL/SYSTEM',
    `action` VARCHAR(100) NOT NULL COMMENT '操作行為',
    `user_id` VARCHAR(50) DEFAULT NULL COMMENT '操作用戶ID',
    `user_type` VARCHAR(20) DEFAULT NULL COMMENT '用戶類型: USER/ADMIN/SYSTEM',
    `target_type` VARCHAR(50) DEFAULT NULL COMMENT '目標類型',
    `target_id` VARCHAR(50) DEFAULT NULL COMMENT '目標ID',
    `request_ip` VARCHAR(50) DEFAULT NULL COMMENT '請求IP',
    `request_url` VARCHAR(255) DEFAULT NULL COMMENT '請求URL',
    `request_method` VARCHAR(10) DEFAULT NULL COMMENT '請求方法',
    `request_params` TEXT DEFAULT NULL COMMENT '請求參數（JSON）',
    `response_status` INT DEFAULT NULL COMMENT '回應狀態碼',
    `response_body` TEXT DEFAULT NULL COMMENT '回應內容（摘要）',
    `error_message` TEXT DEFAULT NULL COMMENT '錯誤訊息',
    `error_stack` TEXT DEFAULT NULL COMMENT '錯誤堆疊',
    `duration_ms` BIGINT DEFAULT NULL COMMENT '執行時間（毫秒）',
    `extra_data` JSON DEFAULT NULL COMMENT '額外資料',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_log_type` (`log_type`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_action` (`action`),
    INDEX `idx_created_at` (`created_at`),
    INDEX `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系統操作日誌';

-- 4. 郵件發送記錄表
CREATE TABLE IF NOT EXISTS `email_log` (
    `id` VARCHAR(50) NOT NULL COMMENT '主鍵',
    `email_type` VARCHAR(50) NOT NULL COMMENT '郵件類型: VERIFICATION/PASSWORD_RESET/NOTIFICATION/ORDER',
    `to_email` VARCHAR(100) NOT NULL COMMENT '收件人信箱',
    `to_name` VARCHAR(100) DEFAULT NULL COMMENT '收件人姓名',
    `subject` VARCHAR(255) NOT NULL COMMENT '郵件主旨',
    `content` TEXT DEFAULT NULL COMMENT '郵件內容',
    `template_name` VARCHAR(100) DEFAULT NULL COMMENT '使用的模板名稱',
    `template_params` JSON DEFAULT NULL COMMENT '模板參數',
    `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '狀態: PENDING/SENT/FAILED',
    `error_message` TEXT DEFAULT NULL COMMENT '發送失敗原因',
    `sent_at` DATETIME DEFAULT NULL COMMENT '實際發送時間',
    `retry_count` INT DEFAULT 0 COMMENT '重試次數',
    `related_type` VARCHAR(50) DEFAULT NULL COMMENT '關聯類型',
    `related_id` VARCHAR(50) DEFAULT NULL COMMENT '關聯ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_email_type` (`email_type`),
    INDEX `idx_to_email` (`to_email`),
    INDEX `idx_status` (`status`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='郵件發送記錄';

-- 5. 跑馬燈訊息表
CREATE TABLE IF NOT EXISTS `marquee` (
    `id` VARCHAR(50) NOT NULL COMMENT '主鍵',
    `content` VARCHAR(500) NOT NULL COMMENT '跑馬燈內容',
    `link_url` VARCHAR(255) DEFAULT NULL COMMENT '點擊連結',
    `link_type` VARCHAR(20) DEFAULT NULL COMMENT '連結類型: INTERNAL/EXTERNAL',
    `priority` INT DEFAULT 0 COMMENT '優先級（數字越大越優先）',
    `bg_color` VARCHAR(20) DEFAULT NULL COMMENT '背景顏色',
    `text_color` VARCHAR(20) DEFAULT NULL COMMENT '文字顏色',
    `start_time` DATETIME DEFAULT NULL COMMENT '開始顯示時間',
    `end_time` DATETIME DEFAULT NULL COMMENT '結束顯示時間',
    `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否啟用',
    `created_by` VARCHAR(50) DEFAULT NULL COMMENT '建立者',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_is_active` (`is_active`),
    INDEX `idx_time_range` (`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='跑馬燈訊息';

-- 6. 報表快照表（用於儲存日報/週報/月報）
CREATE TABLE IF NOT EXISTS `report_snapshot` (
    `id` VARCHAR(50) NOT NULL COMMENT '主鍵',
    `report_type` VARCHAR(50) NOT NULL COMMENT '報表類型: REVENUE/REFERRAL/LOTTERY_RESULT/RECHARGE/BONUS_GRANT',
    `period_type` VARCHAR(20) NOT NULL COMMENT '週期類型: DAILY/WEEKLY/MONTHLY',
    `period_start` DATE NOT NULL COMMENT '週期開始日期',
    `period_end` DATE NOT NULL COMMENT '週期結束日期',
    `store_id` VARCHAR(50) DEFAULT NULL COMMENT '店家ID（若為店家報表）',
    `data` JSON NOT NULL COMMENT '報表資料',
    `summary` JSON DEFAULT NULL COMMENT '摘要統計',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_report_type` (`report_type`),
    INDEX `idx_period` (`period_type`, `period_start`, `period_end`),
    INDEX `idx_store` (`store_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='報表快照';

-- 7. 初始化台灣縣市區域資料
INSERT INTO `district` (`id`, `city`, `district_name`, `zip_code`, `order_num`) VALUES
-- 台北市
('TPE-ZZ', '臺北市', '中正區', '100', 1),
('TPE-DT', '臺北市', '大同區', '103', 2),
('TPE-ZS', '臺北市', '中山區', '104', 3),
('TPE-SL', '臺北市', '松山區', '105', 4),
('TPE-DA', '臺北市', '大安區', '106', 5),
('TPE-WH', '臺北市', '萬華區', '108', 6),
('TPE-XY', '臺北市', '信義區', '110', 7),
('TPE-SZ', '臺北市', '士林區', '111', 8),
('TPE-BT', '臺北市', '北投區', '112', 9),
('TPE-NH', '臺北市', '內湖區', '114', 10),
('TPE-NK', '臺北市', '南港區', '115', 11),
('TPE-WS', '臺北市', '文山區', '116', 12),
-- 新北市
('NWT-BL', '新北市', '板橋區', '220', 1),
('NWT-SJ', '新北市', '三重區', '241', 2),
('NWT-ZH', '新北市', '中和區', '235', 3),
('NWT-YH', '新北市', '永和區', '234', 4),
('NWT-XZ', '新北市', '新莊區', '242', 5),
('NWT-XD', '新北市', '新店區', '231', 6),
('NWT-TY', '新北市', '土城區', '236', 7),
('NWT-LZ', '新北市', '蘆洲區', '247', 8),
('NWT-SZ', '新北市', '樹林區', '238', 9),
('NWT-TL', '新北市', '汐止區', '221', 10),
('NWT-YG', '新北市', '鶯歌區', '239', 11),
('NWT-SX', '新北市', '三峽區', '237', 12),
('NWT-DG', '新北市', '淡水區', '251', 13),
('NWT-RC', '新北市', '瑞芳區', '224', 14),
('NWT-WL', '新北市', '五股區', '248', 15),
('NWT-TM', '新北市', '泰山區', '243', 16),
('NWT-LK', '新北市', '林口區', '244', 17),
('NWT-SG', '新北市', '深坑區', '222', 18),
('NWT-SK', '新北市', '石碇區', '223', 19),
('NWT-PL', '新北市', '坪林區', '232', 20),
('NWT-SL', '新北市', '三芝區', '252', 21),
('NWT-SM', '新北市', '石門區', '253', 22),
('NWT-BL2', '新北市', '八里區', '249', 23),
('NWT-PX', '新北市', '平溪區', '226', 24),
('NWT-SG2', '新北市', '雙溪區', '227', 25),
('NWT-GS', '新北市', '貢寮區', '228', 26),
('NWT-JN', '新北市', '金山區', '208', 27),
('NWT-WL2', '新北市', '萬里區', '207', 28),
('NWT-WG', '新北市', '烏來區', '233', 29),
-- 桃園市
('TYN-ZL', '桃園市', '中壢區', '320', 1),
('TYN-TY', '桃園市', '桃園區', '330', 2),
('TYN-DX', '桃園市', '大溪區', '335', 3),
('TYN-YM', '桃園市', '楊梅區', '326', 4),
('TYN-LZ', '桃園市', '蘆竹區', '338', 5),
('TYN-DY', '桃園市', '大園區', '337', 6),
('TYN-GS', '桃園市', '龜山區', '333', 7),
('TYN-BT', '桃園市', '八德區', '334', 8),
('TYN-LT', '桃園市', '龍潭區', '325', 9),
('TYN-PS', '桃園市', '平鎮區', '324', 10),
('TYN-XW', '桃園市', '新屋區', '327', 11),
('TYN-GY', '桃園市', '觀音區', '328', 12),
('TYN-FS', '桃園市', '復興區', '336', 13),
-- 台中市
('TXG-ZQ', '臺中市', '中區', '400', 1),
('TXG-DQ', '臺中市', '東區', '401', 2),
('TXG-NQ', '臺中市', '南區', '402', 3),
('TXG-XQ', '臺中市', '西區', '403', 4),
('TXG-BQ', '臺中市', '北區', '404', 5),
('TXG-BT', '臺中市', '北屯區', '406', 6),
('TXG-XT', '臺中市', '西屯區', '407', 7),
('TXG-NT', '臺中市', '南屯區', '408', 8),
('TXG-TZ', '臺中市', '太平區', '411', 9),
('TXG-DL', '臺中市', '大里區', '412', 10),
('TXG-WF', '臺中市', '霧峰區', '413', 11),
('TXG-WR', '臺中市', '烏日區', '414', 12),
('TXG-FY', '臺中市', '豐原區', '420', 13),
('TXG-HL', '臺中市', '后里區', '421', 14),
('TXG-SS', '臺中市', '石岡區', '422', 15),
('TXG-DK', '臺中市', '東勢區', '423', 16),
('TXG-HE', '臺中市', '和平區', '424', 17),
('TXG-XS', '臺中市', '新社區', '426', 18),
('TXG-TY', '臺中市', '潭子區', '427', 19),
('TXG-DY', '臺中市', '大雅區', '428', 20),
('TXG-SD', '臺中市', '神岡區', '429', 21),
('TXG-DJ', '臺中市', '大肚區', '432', 22),
('TXG-SL', '臺中市', '沙鹿區', '433', 23),
('TXG-LJ', '臺中市', '龍井區', '434', 24),
('TXG-WQ', '臺中市', '梧棲區', '435', 25),
('TXG-QS', '臺中市', '清水區', '436', 26),
('TXG-DZ', '臺中市', '大甲區', '437', 27),
('TXG-WL', '臺中市', '外埔區', '438', 28),
('TXG-DA', '臺中市', '大安區', '439', 29),
-- 台南市
('TNN-ZX', '臺南市', '中西區', '700', 1),
('TNN-DQ', '臺南市', '東區', '701', 2),
('TNN-NQ', '臺南市', '南區', '702', 3),
('TNN-BQ', '臺南市', '北區', '704', 4),
('TNN-AQ', '臺南市', '安平區', '708', 5),
('TNN-AN', '臺南市', '安南區', '709', 6),
('TNN-YK', '臺南市', '永康區', '710', 7),
('TNN-GL', '臺南市', '歸仁區', '711', 8),
('TNN-XH', '臺南市', '新化區', '712', 9),
('TNN-ZD', '臺南市', '左鎮區', '713', 10),
('TNN-YJ', '臺南市', '玉井區', '714', 11),
('TNN-ND', '臺南市', '楠西區', '715', 12),
('TNN-NH', '臺南市', '南化區', '716', 13),
('TNN-RD', '臺南市', '仁德區', '717', 14),
('TNN-GS', '臺南市', '關廟區', '718', 15),
('TNN-LY', '臺南市', '龍崎區', '719', 16),
('TNN-GC', '臺南市', '官田區', '720', 17),
('TNN-MC', '臺南市', '麻豆區', '721', 18),
('TNN-JA', '臺南市', '佳里區', '722', 19),
('TNN-XY', '臺南市', '西港區', '723', 20),
('TNN-QG', '臺南市', '七股區', '724', 21),
('TNN-JL', '臺南市', '將軍區', '725', 22),
('TNN-XY2', '臺南市', '學甲區', '726', 23),
('TNN-BY', '臺南市', '北門區', '727', 24),
('TNN-XY3', '臺南市', '新營區', '730', 25),
('TNN-HB', '臺南市', '後壁區', '731', 26),
('TNN-BH', '臺南市', '白河區', '732', 27),
('TNN-DS', '臺南市', '東山區', '733', 28),
('TNN-LJ', '臺南市', '六甲區', '734', 29),
('TNN-XT', '臺南市', '下營區', '735', 30),
('TNN-LY2', '臺南市', '柳營區', '736', 31),
('TNN-YS', '臺南市', '鹽水區', '737', 32),
('TNN-SH', '臺南市', '善化區', '741', 33),
('TNN-DQ2', '臺南市', '大內區', '742', 34),
('TNN-SJ', '臺南市', '山上區', '743', 35),
('TNN-XS', '臺南市', '新市區', '744', 36),
('TNN-AK', '臺南市', '安定區', '745', 37),
-- 高雄市
('KHH-XY', '高雄市', '新興區', '800', 1),
('KHH-QJ', '高雄市', '前金區', '801', 2),
('KHH-LY', '高雄市', '苓雅區', '802', 3),
('KHH-YC', '高雄市', '鹽埕區', '803', 4),
('KHH-GS', '高雄市', '鼓山區', '804', 5),
('KHH-QZ', '高雄市', '旗津區', '805', 6),
('KHH-QZ2', '高雄市', '前鎮區', '806', 7),
('KHH-SJ', '高雄市', '三民區', '807', 8),
('KHH-NK', '高雄市', '楠梓區', '811', 9),
('KHH-XZ', '高雄市', '小港區', '812', 10),
('KHH-ZY', '高雄市', '左營區', '813', 11),
('KHH-RW', '高雄市', '仁武區', '814', 12),
('KHH-DS', '高雄市', '大社區', '815', 13),
('KHH-GS2', '高雄市', '岡山區', '820', 14),
('KHH-LS', '高雄市', '路竹區', '821', 15),
('KHH-AL', '高雄市', '阿蓮區', '822', 16),
('KHH-TL', '高雄市', '田寮區', '823', 17),
('KHH-YC2', '高雄市', '燕巢區', '824', 18),
('KHH-QD', '高雄市', '橋頭區', '825', 19),
('KHH-ZG', '高雄市', '梓官區', '826', 20),
('KHH-MY', '高雄市', '彌陀區', '827', 21),
('KHH-YA', '高雄市', '永安區', '828', 22),
('KHH-HD', '高雄市', '湖內區', '829', 23),
('KHH-FS', '高雄市', '鳳山區', '830', 24),
('KHH-DS2', '高雄市', '大寮區', '831', 25),
('KHH-LS2', '高雄市', '林園區', '832', 26),
('KHH-NZ', '高雄市', '鳥松區', '833', 27),
('KHH-DL', '高雄市', '大樹區', '840', 28),
('KHH-QS', '高雄市', '旗山區', '842', 29),
('KHH-MS', '高雄市', '美濃區', '843', 30),
('KHH-LG', '高雄市', '六龜區', '844', 31),
('KHH-ND', '高雄市', '內門區', '845', 32),
('KHH-SD', '高雄市', '杉林區', '846', 33),
('KHH-JX', '高雄市', '甲仙區', '847', 34),
('KHH-TY', '高雄市', '桃源區', '848', 35),
('KHH-NM', '高雄市', '那瑪夏區', '849', 36),
('KHH-MZ', '高雄市', '茂林區', '851', 37),
('KHH-QJ2', '高雄市', '茄萣區', '852', 38)
ON DUPLICATE KEY UPDATE `id` = VALUES(`id`);

-- 建立索引（使用安全的索引建立方式）
-- 檢查索引是否存在,若不存在才建立
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
               WHERE table_schema = DATABASE() 
               AND table_name = 'user' 
               AND index_name = 'idx_user_city');
SET @sqlstmt := IF(@exist = 0, 'CREATE INDEX `idx_user_city` ON `user` (`city`)', 'SELECT ''Index idx_user_city already exists''');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
               WHERE table_schema = DATABASE() 
               AND table_name = 'user' 
               AND index_name = 'idx_user_referral');
SET @sqlstmt := IF(@exist = 0, 'CREATE INDEX `idx_user_referral` ON `user` (`referral_code`)', 'SELECT ''Index idx_user_referral already exists''');
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

