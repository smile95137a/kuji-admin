-- ====================================================
-- 商品表欄位增強 Migration
-- 執行日期：2026-01-19
-- 說明：新增商品主題、圖集、內容、標籤、紅利等欄位
-- ====================================================

-- 檢查是否需要執行（避免重複執行）
-- SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS 
-- WHERE TABLE_SCHEMA = 'kuji' AND TABLE_NAME = 'lottery' AND COLUMN_NAME = 'play_mode';

-- 新增欄位（MySQL 語法）
-- 注意：如果欄位已存在會報錯，請忽略或先檢查

-- 遊玩模式
ALTER TABLE lottery ADD COLUMN play_mode VARCHAR(20) DEFAULT 'LOTTERY_MODE' COMMENT '遊玩模式：LOTTERY_MODE（抽籤型）/ SCRATCH_MODE（刮刮樂型）';

-- 熱門程度
ALTER TABLE lottery ADD COLUMN hot_count INT DEFAULT 0 COMMENT '熱門程度（用於顯示熱門標籤）';

-- 商品主題分類
ALTER TABLE lottery ADD COLUMN theme VARCHAR(100) DEFAULT NULL COMMENT '商品主題分類（火影忍者、航海王、鬼滅之刃等）';

-- 商品圖集
ALTER TABLE lottery ADD COLUMN gallery_images TEXT DEFAULT NULL COMMENT '商品圖集（JSON 陣列，多張圖片 URL）';

-- 商品詳細內容
ALTER TABLE lottery ADD COLUMN content TEXT DEFAULT NULL COMMENT '商品詳細內容（HTML 格式）';

-- 標籤列表
ALTER TABLE lottery ADD COLUMN tags VARCHAR(500) DEFAULT NULL COMMENT '標籤列表（JSON 陣列）';

-- 紅利點數相關
ALTER TABLE lottery ADD COLUMN bonus_enabled TINYINT(1) DEFAULT 0 COMMENT '是否啟用紅利點數';
ALTER TABLE lottery ADD COLUMN bonus_points_per_draw INT DEFAULT 0 COMMENT '每抽贈送紅利點數';
ALTER TABLE lottery ADD COLUMN bonus_cost_per_draw INT DEFAULT 0 COMMENT '每抽消耗紅利點數';

-- 驗證新增結果
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
-- 範例資料（可選）
-- ====================================================

-- 更新現有商品，設定預設值
UPDATE lottery 
SET 
    play_mode = 'LOTTERY_MODE',
    hot_count = 0,
    bonus_enabled = 0,
    bonus_points_per_draw = 0,
    bonus_cost_per_draw = 0
WHERE play_mode IS NULL;

-- 測試插入新商品
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

-- ====================================================
-- 回滾腳本（如需要）
-- ====================================================

/*
ALTER TABLE lottery
DROP COLUMN IF EXISTS play_mode,
DROP COLUMN IF EXISTS hot_count,
DROP COLUMN IF EXISTS theme,
DROP COLUMN IF EXISTS gallery_images,
DROP COLUMN IF EXISTS content,
DROP COLUMN IF EXISTS tags,
DROP COLUMN IF EXISTS bonus_enabled,
DROP COLUMN IF EXISTS bonus_points_per_draw,
DROP COLUMN IF EXISTS bonus_cost_per_draw;
*/
