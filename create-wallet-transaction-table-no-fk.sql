-- 建立 wallet_transaction 表 (無外鍵版本 - 如果上面的版本失敗，使用此版本)
-- 此表用於記錄所有錢幣異動（儲值、消費、贈送、退款等）

CREATE TABLE IF NOT EXISTS `wallet_transaction` (
  `id` VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci PRIMARY KEY COMMENT 'UUID',
  `user_id` VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '玩家 ID',
  `transaction_type` VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '類型：RECHARGE/DRAW/CONSUME/RECYCLE/REFUND/BONUS_GRANT/ADMIN_ADJUST',
  `coin_type` VARCHAR(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '幣種：GOLD/BONUS',
  `amount` BIGINT NOT NULL COMMENT '金額（正數=增加，負數=減少）',
  `balance_after` BIGINT NOT NULL COMMENT '異動後餘額',
  `related_id` VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '關聯 ID（抽獎ID、訂單ID、儲值ID等）',
  `description` VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '說明',
  `created_by` VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '操作者 ID（系統調整時記錄管理員）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
  INDEX `idx_user_time` (`user_id`, `created_at` DESC),
  INDEX `idx_type` (`transaction_type`),
  INDEX `idx_related_id` (`related_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='點數異動記錄';

-- 等表建立成功後，再添加外鍵約束
-- ALTER TABLE wallet_transaction
-- ADD CONSTRAINT `fk_wallet_transaction_user` 
-- FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE;

-- 驗證表是否建立成功
SELECT TABLE_NAME, TABLE_COLLATION FROM information_schema.TABLES WHERE TABLE_SCHEMA='kuji' AND TABLE_NAME='wallet_transaction';

-- 檢查表結構
DESC wallet_transaction;
