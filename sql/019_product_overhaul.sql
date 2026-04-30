-- 019_product_overhaul
-- 商品管理重整：新增 payment_type / free_draw_threshold / delist_strategy，
-- 並廢棄 multi_draw_options / allow_multi_draw / protection_draws / protection_minutes。

-- 1) 新增欄位
ALTER TABLE `lottery`
  ADD COLUMN `payment_type` VARCHAR(20) NOT NULL DEFAULT 'GOLD' COMMENT '支付方式：GOLD/BONUS' AFTER `game_mode`,
  ADD COLUMN `free_draw_threshold` INT NULL COMMENT '免費抽/免單門檻；僅 CUSTOM_GACHA + SCRATCH_MODE 使用' AFTER `payment_type`,
  ADD COLUMN `delist_strategy` VARCHAR(30) NOT NULL DEFAULT 'MANUAL' COMMENT '下架策略：GRAND_PRIZE_DRAWN/ALL_DRAWN/MANUAL' AFTER `free_draw_threshold`;

-- 2) 廢棄舊欄位（先保留欄位，改為可空與標記註解）
ALTER TABLE `lottery`
  MODIFY COLUMN `multi_draw_options` VARCHAR(100) NULL COMMENT '已廢棄',
  MODIFY COLUMN `allow_multi_draw` TINYINT NULL COMMENT '已廢棄',
  MODIFY COLUMN `protection_draws` INT NULL COMMENT '已廢棄，免費抽改由 free_draw_threshold 管理',
  MODIFY COLUMN `protection_minutes` INT NULL COMMENT '已廢棄，保護期改由 system_config 管理';

-- 3) 歷史資料遷移
UPDATE `lottery`
SET `payment_type` = 'GOLD'
WHERE `payment_type` IS NULL OR `payment_type` = '';

UPDATE `lottery`
SET `delist_strategy` = 'MANUAL'
WHERE `delist_strategy` IS NULL OR `delist_strategy` = '';

-- 依商業規則：free_draw_threshold = NULL 代表未啟用免費抽/免單機制
UPDATE `lottery`
SET `free_draw_threshold` = NULL
WHERE `category` = 'CUSTOM_GACHA'
  AND `sub_category` = 'SCRATCH_MODE'
  AND `free_draw_threshold` IS NULL;

