-- =====================================================
-- 賞品盒 + 金流 + 訂單系統 DDL
-- 版本：v1.0
-- 建立日期：2026-01-09
-- =====================================================

-- =====================================================
-- 1. prize_box（賞品盒）
-- =====================================================
CREATE TABLE `prize_box` (
  `id` VARCHAR(36) PRIMARY KEY COMMENT 'UUID',
  `user_id` VARCHAR(36) NOT NULL COMMENT '玩家 ID',
  `lottery_id` VARCHAR(36) NOT NULL COMMENT '商品 ID',
  `prize_id` VARCHAR(36) NOT NULL COMMENT '獎項 ID',
  `store_id` VARCHAR(36) NOT NULL COMMENT '店家 ID',
  `draw_result_id` VARCHAR(36) COMMENT '抽獎結果 ID（預留）',
  `status` VARCHAR(20) NOT NULL DEFAULT 'IN_BOX' COMMENT '狀態：IN_BOX/SHIPPED/RECYCLED',
  `is_recyclable` TINYINT DEFAULT 1 COMMENT '是否可回收（1=可回收，0=不可回收）',
  `recycle_bonus` BIGINT COMMENT '回收可得紅利',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
  FOREIGN KEY (`lottery_id`) REFERENCES `lottery`(`id`),
  FOREIGN KEY (`prize_id`) REFERENCES `lottery_prize`(`id`),
  FOREIGN KEY (`store_id`) REFERENCES `store`(`id`),
  INDEX `idx_user_status` (`user_id`, `status`),
  INDEX `idx_store` (`store_id`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='賞品盒';

-- =====================================================
-- 2. user_wallet（玩家錢包）
-- =====================================================
CREATE TABLE `user_wallet` (
  `id` VARCHAR(36) PRIMARY KEY COMMENT 'UUID',
  `user_id` VARCHAR(36) NOT NULL UNIQUE COMMENT '玩家 ID（唯一）',
  `gold_coins` BIGINT NOT NULL DEFAULT 0 COMMENT '金幣（儲值金）',
  `bonus_coins` BIGINT NOT NULL DEFAULT 0 COMMENT '紅利幣',
  `total_recharged` BIGINT NOT NULL DEFAULT 0 COMMENT '累計儲值金額（台幣）',
  `version` INT NOT NULL DEFAULT 0 COMMENT '樂觀鎖版本號',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
  INDEX `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='玩家錢包';

-- =====================================================
-- 3. wallet_transaction（點數異動記錄）
-- =====================================================
CREATE TABLE `wallet_transaction` (
  `id` VARCHAR(36) PRIMARY KEY COMMENT 'UUID',
  `user_id` VARCHAR(36) NOT NULL COMMENT '玩家 ID',
  `transaction_type` VARCHAR(20) NOT NULL COMMENT '類型：RECHARGE/DRAW/RECYCLE/REFUND/ADMIN_ADJUST',
  `coin_type` VARCHAR(10) NOT NULL COMMENT '幣種：GOLD/BONUS',
  `amount` BIGINT NOT NULL COMMENT '金額（正數=增加，負數=減少）',
  `balance_after` BIGINT NOT NULL COMMENT '異動後餘額',
  `related_id` VARCHAR(36) COMMENT '關聯 ID（抽獎ID、訂單ID、儲值ID等）',
  `description` VARCHAR(500) COMMENT '說明',
  `created_by` VARCHAR(36) COMMENT '操作者 ID（系統調整時記錄管理員）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
  INDEX `idx_user_time` (`user_id`, `created_at` DESC),
  INDEX `idx_type` (`transaction_type`),
  INDEX `idx_related_id` (`related_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='點數異動記錄';

-- =====================================================
-- 4. recharge_plan（儲值方案）
-- =====================================================
CREATE TABLE `recharge_plan` (
  `id` VARCHAR(36) PRIMARY KEY COMMENT 'UUID',
  `name` VARCHAR(100) NOT NULL COMMENT '方案名稱',
  `description` VARCHAR(500) COMMENT '方案說明',
  `amount` BIGINT NOT NULL COMMENT '儲值金額（台幣，單位：元）',
  `gold_coins` BIGINT NOT NULL COMMENT '獲得金幣',
  `bonus_coins` BIGINT DEFAULT 0 COMMENT '贈送紅利',
  `is_active` TINYINT DEFAULT 1 COMMENT '是否啟用（1=啟用，0=停用）',
  `is_promotional` TINYINT DEFAULT 0 COMMENT '是否為活動方案（1=是，0=否）',
  `display_order` INT DEFAULT 0 COMMENT '顯示順序（數字越小越前面）',
  `start_time` DATETIME COMMENT '活動開始時間',
  `end_time` DATETIME COMMENT '活動結束時間',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
  INDEX `idx_active_order` (`is_active`, `display_order`),
  INDEX `idx_time_range` (`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='儲值方案';

-- =====================================================
-- 5. recharge_record（儲值記錄）
-- =====================================================
CREATE TABLE `recharge_record` (
  `id` VARCHAR(36) PRIMARY KEY COMMENT 'UUID',
  `user_id` VARCHAR(36) NOT NULL COMMENT '玩家 ID',
  `plan_id` VARCHAR(36) COMMENT '儲值方案 ID（若方案被刪除則可為 NULL）',
  `amount` BIGINT NOT NULL COMMENT '儲值金額（台幣，單位：元）',
  `gold_coins` BIGINT NOT NULL COMMENT '獲得金幣',
  `bonus_coins` BIGINT DEFAULT 0 COMMENT '贈送紅利',
  `payment_method` VARCHAR(20) NOT NULL COMMENT '支付方式：CREDIT_CARD',
  `payment_status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '支付狀態：PENDING/SUCCESS/FAILED/CANCELLED',
  `payment_gateway` VARCHAR(50) COMMENT '金流商：MASTERCARD',
  `transaction_id` VARCHAR(100) COMMENT '交易序號（金流商回傳）',
  `payment_info` TEXT COMMENT '金流回傳資訊（JSON 格式）',
  `fail_reason` VARCHAR(500) COMMENT '失敗原因',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
  `paid_at` DATETIME COMMENT '付款完成時間',
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
  FOREIGN KEY (`plan_id`) REFERENCES `recharge_plan`(`id`) ON DELETE SET NULL,
  INDEX `idx_user_time` (`user_id`, `created_at` DESC),
  INDEX `idx_status` (`payment_status`),
  INDEX `idx_transaction_id` (`transaction_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='儲值記錄';

-- =====================================================
-- 6. order（訂單）
-- =====================================================
CREATE TABLE `order` (
  `id` VARCHAR(36) PRIMARY KEY COMMENT 'UUID',
  `order_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '訂單編號（格式：ORD + YYYYMMDD + 6位流水號）',
  `user_id` VARCHAR(36) NOT NULL COMMENT '玩家 ID',
  `store_id` VARCHAR(36) NOT NULL COMMENT '店家 ID',
  `total_items` INT NOT NULL COMMENT '商品總數',
  `shipping_method` VARCHAR(20) NOT NULL COMMENT '配送方式：HOME_DELIVERY/SEVEN_ELEVEN/FAMILY_MART',
  `shipping_status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '狀態：PENDING/PREPARING/SHIPPED/COMPLETED/CANCELLED',
  `recipient_name` VARCHAR(100) NOT NULL COMMENT '收件人姓名',
  `recipient_phone` VARCHAR(20) NOT NULL COMMENT '收件人電話',
  `recipient_address` VARCHAR(500) COMMENT '收件地址（宅配）',
  `store_code` VARCHAR(20) COMMENT '超商店號（超商取貨）',
  `store_name` VARCHAR(100) COMMENT '超商店名（超商取貨）',
  `store_address` VARCHAR(500) COMMENT '超商地址（超商取貨）',
  `tracking_no` VARCHAR(100) COMMENT '物流單號',
  `remark` VARCHAR(500) COMMENT '備註',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
  `shipped_at` DATETIME COMMENT '出貨時間',
  `completed_at` DATETIME COMMENT '完成時間',
  `cancelled_at` DATETIME COMMENT '取消時間',
  `cancelled_by` VARCHAR(36) COMMENT '取消者 ID（Admin 或 StoreOwner）',
  `cancel_reason` VARCHAR(500) COMMENT '取消原因',
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
  FOREIGN KEY (`store_id`) REFERENCES `store`(`id`),
  INDEX `idx_user` (`user_id`),
  INDEX `idx_store` (`store_id`),
  INDEX `idx_status` (`shipping_status`),
  INDEX `idx_order_no` (`order_no`),
  INDEX `idx_created_at` (`created_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='訂單';

-- =====================================================
-- 7. order_item（訂單明細）
-- =====================================================
CREATE TABLE `order_item` (
  `id` VARCHAR(36) PRIMARY KEY COMMENT 'UUID',
  `order_id` VARCHAR(36) NOT NULL COMMENT '訂單 ID',
  `prize_box_id` VARCHAR(36) NOT NULL COMMENT '賞品盒 ID',
  `lottery_id` VARCHAR(36) NOT NULL COMMENT '商品 ID',
  `lottery_title` VARCHAR(255) NOT NULL COMMENT '商品名稱（冗餘，防止商品被刪除）',
  `lottery_image_url` VARCHAR(500) COMMENT '商品圖片',
  `prize_id` VARCHAR(36) NOT NULL COMMENT '獎項 ID',
  `prize_name` VARCHAR(255) NOT NULL COMMENT '獎項名稱（冗餘）',
  `prize_image_url` VARCHAR(500) COMMENT '獎項圖片',
  `prize_level` VARCHAR(10) COMMENT '獎項等級（A/B/C/D/E/F/G/Last）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
  FOREIGN KEY (`order_id`) REFERENCES `order`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`prize_box_id`) REFERENCES `prize_box`(`id`),
  INDEX `idx_order` (`order_id`),
  INDEX `idx_prize_box` (`prize_box_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='訂單明細';

-- =====================================================
-- 8. order_status_log（訂單狀態變更記錄）
-- =====================================================
CREATE TABLE `order_status_log` (
  `id` VARCHAR(36) PRIMARY KEY COMMENT 'UUID',
  `order_id` VARCHAR(36) NOT NULL COMMENT '訂單 ID',
  `from_status` VARCHAR(20) COMMENT '原狀態',
  `to_status` VARCHAR(20) NOT NULL COMMENT '新狀態',
  `operator_id` VARCHAR(36) COMMENT '操作者 ID',
  `operator_type` VARCHAR(20) COMMENT '操作者類型：SYSTEM/ADMIN/STORE_OWNER',
  `remark` VARCHAR(500) COMMENT '備註',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
  FOREIGN KEY (`order_id`) REFERENCES `order`(`id`) ON DELETE CASCADE,
  INDEX `idx_order` (`order_id`),
  INDEX `idx_created_at` (`created_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='訂單狀態變更記錄';

-- =====================================================
-- 初始化資料
-- =====================================================

-- 儲值方案初始資料（僅供測試，正式環境由 Admin 後台新增）
INSERT INTO `recharge_plan` (`id`, `name`, `description`, `amount`, `gold_coins`, `bonus_coins`, `is_active`, `is_promotional`, `display_order`) VALUES
(UUID(), '入門方案', '首次儲值推薦', 100, 100, 10, 1, 0, 1),
(UUID(), '標準方案', '最多人選擇', 300, 300, 50, 1, 0, 2),
(UUID(), '超值方案', '多送紅利', 500, 500, 100, 1, 0, 3),
(UUID(), '豪華方案', '額外贈送 30%', 1000, 1000, 300, 1, 0, 4),
(UUID(), '限時活動', '新年特惠', 1000, 1000, 500, 1, 1, 0);

-- =====================================================
-- 版本說明
-- =====================================================
-- v1.0 - 2026-01-09
-- - 建立賞品盒、錢包、訂單核心資料表
-- - 支援 Gold/Bonus 雙幣種
-- - 支援訂單狀態流轉記錄
-- - 支援超商取貨與宅配
-- - 樂觀鎖防止併發問題
-- =====================================================
