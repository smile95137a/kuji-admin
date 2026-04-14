-- ============================================================
-- 021-order-logistics: 運送方式管理 + 訂單物流欄位
-- ============================================================

-- T001: 建立 shipping_method 表
CREATE TABLE `shipping_method` (
  `id`          VARCHAR(36)   NOT NULL PRIMARY KEY,
  `name`        VARCHAR(100)  NOT NULL COMMENT '運送方式名稱（如 7-11 取貨）',
  `code`        VARCHAR(50)   NOT NULL UNIQUE COMMENT '代碼（如 SEVEN_ELEVEN）',
  `provider`    VARCHAR(100)  NULL COMMENT '物流商名稱（如 綠界）',
  `fee`         BIGINT        NOT NULL DEFAULT 0 COMMENT '運費（分為單位）',
  `status`      VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE / INACTIVE',
  `sort_order`  INT           NOT NULL DEFAULT 0 COMMENT '排序',
  `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='運送方式管理表';

-- T002: 插入初始資料
INSERT INTO `shipping_method` (`id`, `name`, `code`, `provider`, `fee`, `status`, `sort_order`) VALUES
(UUID(), '宅配到府', 'HOME_DELIVERY', '黑貓宅急便', 100, 'ACTIVE', 1),
(UUID(), '7-11 取貨', 'SEVEN_ELEVEN', '綠界', 60, 'ACTIVE', 2),
(UUID(), '全家取貨', 'FAMILY_MART', '綠界', 60, 'ACTIVE', 3);

-- T003: 訂單表新增欄位
ALTER TABLE `order`
  ADD COLUMN `shipping_method_id` VARCHAR(36) NULL COMMENT 'FK → shipping_method' AFTER `shipping_method`,
  ADD COLUMN `shipping_fee`       BIGINT NOT NULL DEFAULT 0 COMMENT '運費' AFTER `shipping_method_id`,
  ADD COLUMN `payment_method`     VARCHAR(30) NOT NULL DEFAULT 'STUB' COMMENT 'STUB / MASTERCARD / GOLD_COIN' AFTER `payment_status`;
