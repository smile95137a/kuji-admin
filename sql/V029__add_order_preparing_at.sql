-- V029__add_order_preparing_at.sql
-- Feature: 029 - 獎品出貨報表
-- Purpose: 新增 preparing_at 欄位用於計算平均出貨天數；建立兩個複合索引優化報表查詢
--
-- ⚠️  Run `SHOW INDEX FROM \`order\`` before applying to avoid duplicate index errors.
--     If idx_order_store_shipped_at already exists, skip that CREATE INDEX statement.

ALTER TABLE `order`
  ADD COLUMN `preparing_at` DATETIME NULL COMMENT '備貨開始時間（狀態轉為 PREPARING 時自動記錄）'
  AFTER `shipped_at`;

-- 複合索引：涵蓋狀態計數 + 日期範圍查詢（狀態報表的主要路徑）
CREATE INDEX idx_order_store_status_created ON `order`(store_id, status, created_at);

-- 複合索引：涵蓋每日出貨明細查詢（shipped_at GROUP BY）
CREATE INDEX idx_order_store_shipped_at ON `order`(store_id, shipped_at);
