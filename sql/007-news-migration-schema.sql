-- T001: Verify news table has required columns
-- The following columns should already exist based on the NewsMapper.xml configuration:
--   category, important, scheduled_at, end_time
-- Run this only if they are missing:

-- ALTER TABLE news ADD COLUMN IF NOT EXISTS category VARCHAR(50) DEFAULT 'ANNOUNCEMENT' COMMENT '分類 (ANNOUNCEMENT/EVENT/SYSTEM)';
-- ALTER TABLE news ADD COLUMN IF NOT EXISTS important TINYINT(1) DEFAULT 0 COMMENT '是否重要';
-- ALTER TABLE news ADD COLUMN IF NOT EXISTS scheduled_at DATETIME DEFAULT NULL COMMENT '排程上架時間';
-- ALTER TABLE news ADD COLUMN IF NOT EXISTS end_time DATETIME DEFAULT NULL COMMENT '下架時間';
