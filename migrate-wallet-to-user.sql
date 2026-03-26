-- =====================================================
-- 錢包欄位遷移：user_wallet → user 表
-- 日期：2026-02-08
-- 說明：將 goldCoins / bonusCoins / totalRecharged / version
--       統一存放在 user 表，不再使用 user_wallet 表
-- 
-- 相容性：MySQL 5.7+（使用存儲過程避免版本差異）
-- =====================================================

-- Step 1: 新增缺少的欄位到 user 表（如果欄位不存在）
-- 使用存儲過程避免 "column already exists" 錯誤
DELIMITER //
DROP PROCEDURE IF EXISTS add_columns_if_not_exists //
CREATE PROCEDURE add_columns_if_not_exists()
BEGIN
    DECLARE CONTINUE HANDLER FOR 1060 BEGIN END;
    
    ALTER TABLE `user` ADD COLUMN `total_recharged` BIGINT DEFAULT 0 COMMENT '累計儲值金額';
    ALTER TABLE `user` ADD COLUMN `version` INT DEFAULT 0 COMMENT '樂觀鎖版本號';
END //
DELIMITER ;

CALL add_columns_if_not_exists();
DROP PROCEDURE IF EXISTS add_columns_if_not_exists;

-- Step 2: 從 user_wallet 表遷移資料到 user 表（如果有差異）
UPDATE `user` u
INNER JOIN `user_wallet` w ON u.id = w.user_id
SET u.gold_coins = w.gold_coins,
    u.bonus_coins = w.bonus_coins,
    u.total_recharged = w.total_recharged,
    u.version = w.version
WHERE u.gold_coins != w.gold_coins 
   OR u.bonus_coins != w.bonus_coins
   OR u.total_recharged IS NULL;

-- Step 3: 確保所有 user 的金幣欄位有預設值
UPDATE `user` SET gold_coins = 0 WHERE gold_coins IS NULL;
UPDATE `user` SET bonus_coins = 0 WHERE bonus_coins IS NULL;
UPDATE `user` SET total_recharged = 0 WHERE total_recharged IS NULL;
UPDATE `user` SET version = 0 WHERE version IS NULL;

-- Step 4: 驗證遷移結果
SELECT 
  u.id,
  u.email,
  u.gold_coins AS user_gold,
  u.bonus_coins AS user_bonus,
  u.total_recharged AS user_total_recharged,
  w.gold_coins AS wallet_gold,
  w.bonus_coins AS wallet_bonus,
  w.total_recharged AS wallet_total_recharged
FROM `user` u
LEFT JOIN `user_wallet` w ON u.id = w.user_id
LIMIT 20;

-- ⚠️ 注意事項
-- 1. 確認資料遷移完成後，可選擇備份 user_wallet 表：
--    RENAME TABLE `user_wallet` TO `user_wallet_backup`;
--
-- 2. 完全刪除 user_wallet 表（確認所有資料都遷移完成後）：
--    DROP TABLE `user_wallet`;
--
-- 3. 删除備份表（如果確認不需要）：
--    DROP TABLE `user_wallet_backup`;
