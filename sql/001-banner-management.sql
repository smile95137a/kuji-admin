-- Migration: 001-banner-management
-- Description: Create banner table for homepage carousel management
-- Date: 2026-03-22

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
  INDEX `idx_banner_store_id`       (`store_id`),
  INDEX `idx_banner_status_order`   (`status`, `order_num`, `created_at`),
  INDEX `idx_banner_schedule`       (`status`, `start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
