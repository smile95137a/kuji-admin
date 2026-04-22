-- ============================================================
-- 027-order-payment-shipping: 訂單補齊金流與物流追蹤欄位
-- ============================================================

-- T001: order 表補 tracking_url（物流外部查詢連結）
ALTER TABLE `order`
  ADD COLUMN `tracking_url` VARCHAR(500) NULL COMMENT '物流追蹤外部連結' AFTER `tracking_no`;

-- T002: order 表補 gomypay_trade_no（GoMyPay 交易編號，用於對帳）
ALTER TABLE `order`
  ADD COLUMN `gomypay_trade_no` VARCHAR(100) NULL COMMENT 'GoMyPay 交易編號' AFTER `payment_method`;

-- T003: 若 payment_status 欄位尚未存在則補上（防呆）
-- 已存在則此行可跳過，手動執行時請確認
-- ALTER TABLE `order` ADD COLUMN `payment_status` VARCHAR(30) NOT NULL DEFAULT 'UNPAID' COMMENT 'PAYMENT_PENDING/PAID/FAILED/REFUNDED';

-- T004: shipping_method 表補 tracking_url_template（用於生成物流查詢連結）
ALTER TABLE shipping_method
  ADD COLUMN `tracking_url_template` VARCHAR(500) NULL
    COMMENT '物流查詢 URL 模板，{trackingNo} 會被替換為實際單號。如：https://www.t-cat.com.tw/Inquire/TraceDetail.aspx?BillID={trackingNo}';

-- T005: 更新初始資料的追蹤 URL 模板
UPDATE shipping_method SET tracking_url_template = 'https://www.t-cat.com.tw/Inquire/TraceDetail.aspx?BillID={trackingNo}' WHERE code = 'HOME_DELIVERY';
UPDATE shipping_method SET tracking_url_template = 'https://eservice.7-11.com.tw/e-tracking/search.aspx?TBSTKECNO={trackingNo}' WHERE code = 'SEVEN_ELEVEN';
UPDATE shipping_method SET tracking_url_template = NULL WHERE code = 'FAMILY_MART';
