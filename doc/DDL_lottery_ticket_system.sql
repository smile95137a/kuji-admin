-- =============================================
-- KUJI 抽獎籤位系統 DDL
-- 版本: 2025-12-25
-- 說明: 支援一番賞、扭蛋、卡牌、刮刮樂等多種遊戲模式
-- =============================================

-- =============================================
-- 一、修改現有表：lottery（新增欄位）
-- =============================================

-- 遊戲模式相關欄位
ALTER TABLE lottery ADD COLUMN game_mode VARCHAR(30) DEFAULT 'RANDOM' 
    COMMENT '遊戲模式：RANDOM(一番賞/扭蛋/卡牌隨機)/SCRATCH_STORE(刮刮樂-店家指定)/SCRATCH_PLAYER(刮刮樂-玩家指定)' 
    AFTER sub_category;

-- 開套保護機制欄位
ALTER TABLE lottery ADD COLUMN protection_draws INT DEFAULT 0 
    COMMENT '開套保護抽數（0=無保護，例如設5表示開套5抽內免單機會）' 
    AFTER max_draws;

ALTER TABLE lottery ADD COLUMN protection_minutes INT DEFAULT 5 
    COMMENT '保護時間（分鐘），開套玩家獨佔商品的時間' 
    AFTER protection_draws;

-- 免單機制欄位
ALTER TABLE lottery ADD COLUMN free_draw_enabled TINYINT DEFAULT 0 
    COMMENT '是否啟用開套免單：0=否, 1=是' 
    AFTER protection_minutes;

-- 刮刮樂店家指定大獎欄位
ALTER TABLE lottery ADD COLUMN designated_prize_numbers VARCHAR(500) 
    COMMENT '店家指定大獎的 revealed_number 列表 (JSON Array，例如 [15,45,78]，刮刮樂-店家指定模式用)' 
    AFTER free_draw_enabled;

-- 是否已生成籤位（用於判斷商品是否可以開始抽獎）
ALTER TABLE lottery ADD COLUMN tickets_generated TINYINT DEFAULT 0 
    COMMENT '籤位是否已生成：0=否, 1=是' 
    AFTER designated_prize_numbers;


-- =============================================
-- 二、新增表：lottery_ticket（籤位表）
-- =============================================
-- 核心設計：每個籤位是一筆資料
-- 重要：前台 API 不能返回未抽籤位的獎品資訊！

CREATE TABLE lottery_ticket (
    id VARCHAR(36) PRIMARY KEY COMMENT '籤位 ID (UUID)',
    lottery_id VARCHAR(36) NOT NULL COMMENT '所屬抽獎活動 ID',
    ticket_number INT NOT NULL COMMENT '籤位編號 (從 1 開始)；刮刮樂=實體卡物理序號',

    -- ========== 刮刮樂專用：刮開後顯示的亂數號碼 ==========
    -- 一番賞/扭蛋/卡牌：NULL（不適用）
    -- 刮刮樂：1-N 亂數，與 ticket_number 無關，建立時 shuffle 分配
    revealed_number INT NULL COMMENT '刮刮樂：刮開後揭露的號碼；一番賞/扭蛋為 NULL',

    -- ========== 獎品分配 ==========
    -- 一番賞/扭蛋/卡牌：建立時隨機分配
    -- 刮刮樂(店家)：依 revealed_number 是否在得獎名單內決定
    -- 刮刮樂(玩家)：開套時玩家指定 revealed_number 後再更新
    prize_id VARCHAR(36) COMMENT '分配到的獎項 ID (NULL=謝謝惠顧/安慰獎)',
    prize_level VARCHAR(20) COMMENT '獎品等級快取 (A/B/C/.../LAST/THANKS)',
    
    -- ========== 抽取狀態 ==========
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE' 
        COMMENT '狀態：AVAILABLE(可抽)/DRAWN(已抽)/LOCKED(鎖定中)',
    drawn_by VARCHAR(36) COMMENT '抽取者的使用者 ID',
    drawn_at DATETIME COMMENT '抽取時間',
    
    -- ========== 刮刮樂專用 ==========
    -- 標記這個籤位是否為「指定的大獎位置」
    is_designated_prize TINYINT DEFAULT 0 
        COMMENT '是否為指定大獎位置：0=否, 1=是（刮刮樂用）',
    designated_by VARCHAR(20) 
        COMMENT '指定者類型：STORE(店家)/PLAYER(開套玩家)',
    
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- ========== 索引 ==========
    UNIQUE KEY uk_lottery_ticket (lottery_id, ticket_number),
    INDEX idx_lottery_status (lottery_id, status),
    INDEX idx_prize_id (prize_id),
    INDEX idx_drawn_by (drawn_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽獎籤位表 - 每個籤位一筆資料';


-- =============================================
-- 三、新增表：lottery_session（開套場次表）
-- =============================================
-- 追蹤每次開套的狀態，包含保護時間、免單機制

CREATE TABLE lottery_session (
    id VARCHAR(36) PRIMARY KEY COMMENT '場次 ID (UUID)',
    lottery_id VARCHAR(36) NOT NULL COMMENT '抽獎活動 ID',
    opener_user_id VARCHAR(36) NOT NULL COMMENT '開套玩家 ID',
    
    -- ========== 開套保護機制 ==========
    protection_draws INT DEFAULT 0 
        COMMENT '保護抽數（從 lottery 複製，0=無保護）',
    protection_start_time DATETIME 
        COMMENT '保護開始時間',
    protection_end_time DATETIME 
        COMMENT '保護結束時間',
    
    -- ========== 開套抽獎統計 ==========
    opener_draw_count INT DEFAULT 0 
        COMMENT '開套玩家已抽次數（在保護期內）',
    opener_total_cost BIGINT DEFAULT 0 
        COMMENT '開套玩家已花費金額（在保護期內）',
    
    -- ========== 免單機制 ==========
    free_draw_enabled TINYINT DEFAULT 0 
        COMMENT '此場次是否啟用免單（從 lottery 複製）',
    free_draw_triggered TINYINT DEFAULT 0 
        COMMENT '是否已觸發免單：0=否, 1=是',
    free_draw_refund_amount BIGINT DEFAULT 0 
        COMMENT '免單退款金額',
    free_draw_triggered_at DATETIME 
        COMMENT '觸發免單時間',
    free_draw_prize_id VARCHAR(36) 
        COMMENT '觸發免單的獎品 ID',
    
    -- ========== 刮刮樂(玩家指定)專用 ==========
    player_designated_numbers VARCHAR(500) 
        COMMENT '玩家指定的大獎 revealed_number 列表 (JSON Array，例如 [50])',
    
    -- ========== 場次狀態 ==========
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' 
        COMMENT '狀態：ACTIVE(進行中)/COMPLETED(已完成)/EXPIRED(已過期)',
    completed_at DATETIME 
        COMMENT '完成時間',
    
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- ========== 索引 ==========
    INDEX idx_lottery_id (lottery_id),
    INDEX idx_opener_user_id (opener_user_id),
    INDEX idx_status (status),
    INDEX idx_protection_end_time (protection_end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽獎開套場次表 - 追蹤開套玩家與免單機制';


-- =============================================
-- 四、修改現有表：lottery_draw_record（新增欄位）
-- =============================================

-- 關聯到籤位
ALTER TABLE lottery_draw_record ADD COLUMN ticket_id VARCHAR(36) 
    COMMENT '抽中的籤位 ID' 
    AFTER prize_id;

-- 關聯到場次
ALTER TABLE lottery_draw_record ADD COLUMN session_id VARCHAR(36) 
    COMMENT '所屬開套場次 ID' 
    AFTER ticket_id;

-- 是否為開套玩家的抽獎
ALTER TABLE lottery_draw_record ADD COLUMN is_opener_draw TINYINT DEFAULT 0 
    COMMENT '是否為開套玩家的抽獎：0=否, 1=是' 
    AFTER session_id;

-- 是否觸發免單
ALTER TABLE lottery_draw_record ADD COLUMN triggered_free_draw TINYINT DEFAULT 0 
    COMMENT '此次抽獎是否觸發免單：0=否, 1=是' 
    AFTER is_opener_draw;


-- =============================================
-- 五、新增索引優化
-- =============================================

-- lottery_ticket 查詢優化
CREATE INDEX idx_ticket_available ON lottery_ticket (lottery_id, status, ticket_number);

-- lottery_session 查詢優化
CREATE INDEX idx_session_active ON lottery_session (lottery_id, status, protection_end_time);


-- =============================================
-- 六、測試資料
-- =============================================

-- 注意：以下測試資料假設您已有 lottery 和 lottery_prize 資料
-- 實際使用時，籤位會在商品建立/上架時由程式自動生成

-- 範例：為 ID 為 'xxx' 的一番賞商品生成 80 個籤位
-- INSERT INTO lottery_ticket (id, lottery_id, ticket_number, prize_id, prize_level, status)
-- VALUES 
--   (UUID(), 'xxx', 1, 'prize_c_id', 'C', 'AVAILABLE'),
--   (UUID(), 'xxx', 2, 'prize_f_id', 'F', 'AVAILABLE'),
--   (UUID(), 'xxx', 3, NULL, 'THANKS', 'AVAILABLE'),
--   ...
--   (UUID(), 'xxx', 13, 'prize_a_id', 'A', 'AVAILABLE'),  -- A賞落在13號
--   ...
--   (UUID(), 'xxx', 80, 'prize_last_id', 'LAST', 'AVAILABLE');
