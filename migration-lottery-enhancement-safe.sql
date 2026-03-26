-- ====================================================
-- 商品表欄位增強 Migration（安全版本）
-- 執行日期：2026-01-19
-- 說明：新增商品主題、圖集、內容、標籤、紅利等欄位
-- 特色：檢查欄位是否存在，避免重複執行錯誤
-- ====================================================

-- 使用 Stored Procedure 來安全地新增欄位
DELIMITER $$

DROP PROCEDURE IF EXISTS add_lottery_columns$$

CREATE PROCEDURE add_lottery_columns()
BEGIN
    -- 檢查並新增 play_mode
    IF NOT EXISTS (
        SELECT * FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = 'kuji' AND TABLE_NAME = 'lottery' AND COLUMN_NAME = 'play_mode'
    ) THEN
        ALTER TABLE lottery ADD COLUMN play_mode VARCHAR(20) DEFAULT 'LOTTERY_MODE' COMMENT '遊玩模式：LOTTERY_MODE（抽籤型）/ SCRATCH_MODE（刮刮樂型）';
        SELECT 'Column play_mode added successfully' AS result;
    ELSE
        SELECT 'Column play_mode already exists' AS result;
    END IF;
    
    -- 檢查並新增 hot_count
    IF NOT EXISTS (
        SELECT * FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = 'kuji' AND TABLE_NAME = 'lottery' AND COLUMN_NAME = 'hot_count'
    ) THEN
        ALTER TABLE lottery ADD COLUMN hot_count INT DEFAULT 0 COMMENT '熱門程度（用於顯示熱門標籤）';
        SELECT 'Column hot_count added successfully' AS result;
    ELSE
        SELECT 'Column hot_count already exists' AS result;
    END IF;
    
    -- 檢查並新增 theme
    IF NOT EXISTS (
        SELECT * FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = 'kuji' AND TABLE_NAME = 'lottery' AND COLUMN_NAME = 'theme'
    ) THEN
        ALTER TABLE lottery ADD COLUMN theme VARCHAR(100) DEFAULT NULL COMMENT '商品主題分類（火影忍者、航海王、鬼滅之刃等）';
        SELECT 'Column theme added successfully' AS result;
    ELSE
        SELECT 'Column theme already exists' AS result;
    END IF;
    
    -- 檢查並新增 gallery_images
    IF NOT EXISTS (
        SELECT * FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = 'kuji' AND TABLE_NAME = 'lottery' AND COLUMN_NAME = 'gallery_images'
    ) THEN
        ALTER TABLE lottery ADD COLUMN gallery_images TEXT DEFAULT NULL COMMENT '商品圖集（JSON 陣列，多張圖片 URL）';
        SELECT 'Column gallery_images added successfully' AS result;
    ELSE
        SELECT 'Column gallery_images already exists' AS result;
    END IF;
    
    -- 檢查並新增 content
    IF NOT EXISTS (
        SELECT * FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = 'kuji' AND TABLE_NAME = 'lottery' AND COLUMN_NAME = 'content'
    ) THEN
        ALTER TABLE lottery ADD COLUMN content TEXT DEFAULT NULL COMMENT '商品詳細內容（HTML 格式）';
        SELECT 'Column content added successfully' AS result;
    ELSE
        SELECT 'Column content already exists' AS result;
    END IF;
    
    -- 檢查並新增 tags
    IF NOT EXISTS (
        SELECT * FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = 'kuji' AND TABLE_NAME = 'lottery' AND COLUMN_NAME = 'tags'
    ) THEN
        ALTER TABLE lottery ADD COLUMN tags VARCHAR(500) DEFAULT NULL COMMENT '標籤列表（JSON 陣列）';
        SELECT 'Column tags added successfully' AS result;
    ELSE
        SELECT 'Column tags already exists' AS result;
    END IF;
    
    -- 檢查並新增 bonus_enabled
    IF NOT EXISTS (
        SELECT * FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = 'kuji' AND TABLE_NAME = 'lottery' AND COLUMN_NAME = 'bonus_enabled'
    ) THEN
        ALTER TABLE lottery ADD COLUMN bonus_enabled TINYINT(1) DEFAULT 0 COMMENT '是否啟用紅利點數';
        SELECT 'Column bonus_enabled added successfully' AS result;
    ELSE
        SELECT 'Column bonus_enabled already exists' AS result;
    END IF;
    
    -- 檢查並新增 bonus_points_per_draw
    IF NOT EXISTS (
        SELECT * FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = 'kuji' AND TABLE_NAME = 'lottery' AND COLUMN_NAME = 'bonus_points_per_draw'
    ) THEN
        ALTER TABLE lottery ADD COLUMN bonus_points_per_draw INT DEFAULT 0 COMMENT '每抽贈送紅利點數';
        SELECT 'Column bonus_points_per_draw added successfully' AS result;
    ELSE
        SELECT 'Column bonus_points_per_draw already exists' AS result;
    END IF;
    
    -- 檢查並新增 bonus_cost_per_draw
    IF NOT EXISTS (
        SELECT * FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = 'kuji' AND TABLE_NAME = 'lottery' AND COLUMN_NAME = 'bonus_cost_per_draw'
    ) THEN
        ALTER TABLE lottery ADD COLUMN bonus_cost_per_draw INT DEFAULT 0 COMMENT '每抽消耗紅利點數';
        SELECT 'Column bonus_cost_per_draw added successfully' AS result;
    ELSE
        SELECT 'Column bonus_cost_per_draw already exists' AS result;
    END IF;
    
END$$

DELIMITER ;

-- 執行 Stored Procedure
CALL add_lottery_columns();

-- 刪除 Stored Procedure（清理）
DROP PROCEDURE IF EXISTS add_lottery_columns;

-- ====================================================
-- 驗證新增結果
-- ====================================================

SELECT 
    COLUMN_NAME, 
    DATA_TYPE, 
    COLUMN_TYPE, 
    COLUMN_DEFAULT, 
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'kuji' 
AND TABLE_NAME = 'lottery'
AND COLUMN_NAME IN (
    'play_mode', 
    'hot_count', 
    'theme', 
    'gallery_images', 
    'content', 
    'tags', 
    'bonus_enabled', 
    'bonus_points_per_draw', 
    'bonus_cost_per_draw'
)
ORDER BY ORDINAL_POSITION;

-- ====================================================
-- 更新現有資料（可選）
-- ====================================================

-- 更新現有商品，設定預設值
UPDATE lottery 
SET 
    play_mode = COALESCE(play_mode, 'LOTTERY_MODE'),
    hot_count = COALESCE(hot_count, 0),
    bonus_enabled = COALESCE(bonus_enabled, 0),
    bonus_points_per_draw = COALESCE(bonus_points_per_draw, 0),
    bonus_cost_per_draw = COALESCE(bonus_cost_per_draw, 0)
WHERE id IS NOT NULL;

-- ====================================================
-- 測試插入新商品（可選）
-- ====================================================

/*
INSERT INTO lottery (
    id, store_id, title, category, price_per_draw, max_draws,
    play_mode, status, hot_count, theme, gallery_images, content, tags,
    bonus_enabled, bonus_points_per_draw, bonus_cost_per_draw,
    created_at, updated_at
) VALUES (
    UUID(),
    '550e8400-e29b-41d4-a716-446655440000',
    '鬼滅之刃一番賞測試',
    'OFFICIAL_ICHIBAN',
    80,
    100,
    'LOTTERY_MODE',
    'DRAFT',
    999,
    '鬼滅之刃',
    '["https://example.com/images/1.jpg", "https://example.com/images/2.jpg"]',
    '【活動說明】\n- 單抽 / 多抽（10、50）\n- 啟用自動折扣後，每抽 100 元',
    '["鬼滅之刃", "一番賞", "熱門"]',
    1,
    10,
    200,
    NOW(),
    NOW()
);
*/
