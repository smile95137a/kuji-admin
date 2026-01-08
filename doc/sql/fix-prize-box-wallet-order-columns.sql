-- =====================================================
-- 資料表欄位修正腳本
-- 版本：v1.1
-- 修正日期：2026-01-09
-- 說明：修正 prize_box、order、order_item、recharge_plan 欄位
-- =====================================================

-- =====================================================
-- 1. prize_box 新增欄位
-- =====================================================
ALTER TABLE `prize_box`
  ADD COLUMN `recycled_at` DATETIME COMMENT '回收時間' AFTER `recycle_bonus`,
  ADD COLUMN `shipped_at` DATETIME COMMENT '出貨時間' AFTER `recycled_at`,
  ADD COLUMN `order_id` VARCHAR(36) COMMENT '關聯訂單 ID' AFTER `shipped_at`,
  ADD INDEX `idx_order` (`order_id`);

-- =====================================================
-- 2. order 欄位重命名 + 新增欄位
-- =====================================================
ALTER TABLE `order`
  CHANGE COLUMN `order_no` `order_number` VARCHAR(50) NOT NULL UNIQUE COMMENT '訂單編號',
  CHANGE COLUMN `shipping_status` `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '狀態：PENDING/PREPARING/SHIPPED/COMPLETED/CANCELLED',
  ADD COLUMN `payment_status` VARCHAR(20) NOT NULL DEFAULT 'SUCCESS' COMMENT '支付狀態：PENDING/SUCCESS/FAILED/CANCELLED' AFTER `status`;

-- 更新索引
ALTER TABLE `order`
  DROP INDEX `idx_status`,
  ADD INDEX `idx_status` (`status`),
  DROP INDEX `idx_order_no`,
  ADD INDEX `idx_order_number` (`order_number`);

-- =====================================================
-- 3. order_item 新增欄位
-- =====================================================
ALTER TABLE `order_item`
  ADD COLUMN `prize_grade` VARCHAR(10) COMMENT '獎品等級（冗餘）' AFTER `prize_name`,
  ADD COLUMN `prize_image` VARCHAR(500) COMMENT '獎品圖片 URL（冗餘）' AFTER `prize_grade`;

-- =====================================================
-- 4. recharge_plan 欄位重命名 + 新增欄位
-- =====================================================
ALTER TABLE `recharge_plan`
  CHANGE COLUMN `display_order` `order_num` INT DEFAULT 0 COMMENT '顯示順序',
  CHANGE COLUMN `start_time` `start_date` DATETIME COMMENT '活動開始時間',
  CHANGE COLUMN `end_time` `end_date` DATETIME COMMENT '活動結束時間',
  DROP COLUMN `is_promotional`,
  ADD COLUMN `deleted_at` DATETIME COMMENT '刪除時間（軟刪除）' AFTER `updated_at`;

-- 更新索引
ALTER TABLE `recharge_plan`
  DROP INDEX `idx_active_order`,
  ADD INDEX `idx_active_order` (`is_active`, `order_num`),
  DROP INDEX `idx_time_range`,
  ADD INDEX `idx_time_range` (`start_date`, `end_date`);

-- =====================================================
-- 完成
-- =====================================================
-- 執行完成後，請執行以下命令重新生成 Entity/Mapper：
-- mvn mybatis-generator:generate
