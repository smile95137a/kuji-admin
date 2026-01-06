-- News 表 DDL（最新消息管理）
CREATE TABLE IF NOT EXISTS news (
    id VARCHAR(36) PRIMARY KEY COMMENT '最新消息 ID (UUID)',
    title VARCHAR(200) NOT NULL COMMENT '標題',
    content TEXT NOT NULL COMMENT '內文（長文）',
    image_url VARCHAR(255) COMMENT '封面圖片 URL（可選）',
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '狀態：DRAFT/PUBLISHED/ARCHIVED',
    scheduled_at DATETIME COMMENT '上架時間',
    end_time DATETIME COMMENT '下架時間',
    created_by VARCHAR(36) COMMENT '建立者 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最後修改時間',
    INDEX idx_status (status),
    INDEX idx_scheduled_at (scheduled_at),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='最新消息表';

-- 更新 Banner 表結構（確保符合需求）
-- 注意：如果 banner 表已存在且有 link_url 欄位，需要先刪除
-- ALTER TABLE banner DROP COLUMN IF EXISTS link_url;

-- 插入測試數據（可選）
-- 如需插入測試數據，請先確認 store 表中已有數據
-- 
-- INSERT INTO news (id, title, content, status, scheduled_at, created_at, updated_at)
-- VALUES 
-- (UUID(), '平台維護公告', '系統將於本週六凌晨進行例行維護，維護期間無法使用服務。', 'PUBLISHED', NOW(), NOW(), NOW()),
-- (UUID(), '春節活動開跑！', '春節期間推出限定活動，參加抽獎就有機會獲得豐富獎品！', 'PUBLISHED', NOW(), NOW(), NOW());

-- 插入測試 Banner 數據（需要已存在的店家）
-- 注意：store 表的欄位是 store_name，不是 name
-- 
-- INSERT INTO banner (id, store_id, title, image_url, order_num, status, start_time, created_at, updated_at)
-- SELECT 
--     UUID(),
--     s.id,
--     CONCAT(s.store_name, ' 限時優惠'),
--     'https://via.placeholder.com/1200x400',
--     1,
--     'PUBLISHED',
--     NOW(),
--     NOW(),
--     NOW()
-- FROM store s
-- LIMIT 1;
