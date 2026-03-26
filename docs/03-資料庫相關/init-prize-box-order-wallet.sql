-- ======================================================
-- KUJI 獎品盒、訂單與錢包系統 - 資料庫初始化腳本
-- 執行順序：在現有資料庫基礎上新增表格
-- ======================================================

USE kuji;

-- ======================================================
-- 1. 錢包系統（Wallet System）
-- ======================================================

-- 玩家錢包表
CREATE TABLE IF NOT EXISTS wallet (
    id VARCHAR(50) PRIMARY KEY COMMENT '錢包ID',
    user_id VARCHAR(50) NOT NULL UNIQUE COMMENT '玩家ID',
    gold BIGINT DEFAULT 0 COMMENT '儲值金（可消費）',
    bonus BIGINT DEFAULT 0 COMMENT '紅利金（贈送）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '玩家錢包';

-- 錢包交易記錄表
CREATE TABLE IF NOT EXISTS wallet_transaction (
    id VARCHAR(50) PRIMARY KEY COMMENT '交易ID',
    wallet_id VARCHAR(50) NOT NULL COMMENT '錢包ID',
    user_id VARCHAR(50) NOT NULL COMMENT '玩家ID',
    type VARCHAR(20) NOT NULL COMMENT '交易類型：RECHARGE/CONSUME/BONUS_GRANT/PRIZE_RECYCLE/SYSTEM_ADJUST',
    amount BIGINT NOT NULL COMMENT '金額（正數=增加，負數=減少）',
    currency_type VARCHAR(10) NOT NULL COMMENT '貨幣類型：GOLD/BONUS',
    balance_after BIGINT NOT NULL COMMENT '交易後餘額',
    reference_id VARCHAR(50) COMMENT '關聯ID（抽獎ID/訂單ID等）',
    reference_type VARCHAR(20) COMMENT '關聯類型：DRAW/ORDER/SYSTEM',
    description VARCHAR(200) COMMENT '描述',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '交易時間',
    INDEX idx_wallet_user (wallet_id, user_id),
    INDEX idx_user_created (user_id, created_at),
    INDEX idx_created (created_at),
    INDEX idx_type (type),
    FOREIGN KEY (wallet_id) REFERENCES wallet(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '錢包交易記錄';

-- ======================================================
-- 2. 抽獎結果與賞品盒（Prize Box）
-- ======================================================

-- 抽獎結果表（賞品盒）
CREATE TABLE IF NOT EXISTS draw_result (
    id VARCHAR(50) PRIMARY KEY COMMENT '抽獎結果ID',
    user_id VARCHAR(50) NOT NULL COMMENT '玩家ID',
    lottery_id VARCHAR(50) NOT NULL COMMENT '商品ID',
    store_id VARCHAR(50) NOT NULL COMMENT '店家ID',
    prize_id VARCHAR(50) NOT NULL COMMENT '獎品ID',
    prize_name VARCHAR(100) NOT NULL COMMENT '獎品名稱',
    prize_image_url VARCHAR(500) COMMENT '獎品圖片',
    prize_level VARCHAR(10) NOT NULL COMMENT '獎項等級：A/B/C/D/E/LAST',
    status VARCHAR(20) DEFAULT 'IN_PRIZE_BOX' COMMENT '狀態：IN_PRIZE_BOX/IN_ORDER/RECYCLED',
    order_id VARCHAR(50) COMMENT '訂單ID（出貨後）',
    drawn_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '抽中時間',
    recycled_at DATETIME COMMENT '回收時間',
    INDEX idx_user_status (user_id, status),
    INDEX idx_store_status (store_id, status),
    INDEX idx_lottery (lottery_id),
    INDEX idx_order (order_id),
    INDEX idx_drawn_at (drawn_at),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (lottery_id) REFERENCES lottery(id) ON DELETE CASCADE,
    FOREIGN KEY (store_id) REFERENCES store(id) ON DELETE CASCADE,
    FOREIGN KEY (prize_id) REFERENCES prize(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '抽獎結果（賞品盒）';

-- ======================================================
-- 3. 訂單系統（Order System）
-- ======================================================

-- 訂單主表
CREATE TABLE IF NOT EXISTS `order` (
    id VARCHAR(50) PRIMARY KEY COMMENT '訂單ID',
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '訂單編號',
    user_id VARCHAR(50) NOT NULL COMMENT '玩家ID',
    store_id VARCHAR(50) NOT NULL COMMENT '店家ID（單店家訂單）',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '狀態：PENDING/PREPARING/SHIPPED/COMPLETED/CANCELLED',
    
    -- 收件資訊
    shipping_type VARCHAR(20) NOT NULL COMMENT '配送方式：HOME/CVS_711/CVS_FAMILY',
    recipient_name VARCHAR(50) NOT NULL COMMENT '收件人姓名',
    recipient_phone VARCHAR(20) NOT NULL COMMENT '收件人電話',
    recipient_address VARCHAR(200) COMMENT '收件地址（宅配）',
    cvs_store_name VARCHAR(100) COMMENT '超商店名',
    cvs_store_code VARCHAR(50) COMMENT '超商店號',
    
    -- 訂單備註
    user_note VARCHAR(500) COMMENT '玩家備註',
    admin_note VARCHAR(500) COMMENT '店家備註',
    tracking_number VARCHAR(100) COMMENT '物流單號',
    
    -- 時間記錄
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    preparing_at DATETIME COMMENT '準備出貨時間',
    shipped_at DATETIME COMMENT '已出貨時間',
    completed_at DATETIME COMMENT '完成時間',
    cancelled_at DATETIME COMMENT '取消時間',
    cancel_reason VARCHAR(500) COMMENT '取消原因',
    
    INDEX idx_user (user_id),
    INDEX idx_store_status (store_id, status),
    INDEX idx_status (status),
    INDEX idx_order_no (order_no),
    INDEX idx_created (created_at),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (store_id) REFERENCES store(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '訂單主表';

-- 訂單明細表
CREATE TABLE IF NOT EXISTS order_item (
    id VARCHAR(50) PRIMARY KEY COMMENT '明細ID',
    order_id VARCHAR(50) NOT NULL COMMENT '訂單ID',
    draw_result_id VARCHAR(50) NOT NULL COMMENT '抽獎結果ID',
    prize_id VARCHAR(50) NOT NULL COMMENT '獎品ID',
    prize_name VARCHAR(100) NOT NULL COMMENT '獎品名稱',
    prize_image_url VARCHAR(500) COMMENT '獎品圖片',
    lottery_id VARCHAR(50) NOT NULL COMMENT '商品ID',
    lottery_title VARCHAR(100) NOT NULL COMMENT '商品標題',
    prize_level VARCHAR(10) NOT NULL COMMENT '獎項等級',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    INDEX idx_order (order_id),
    INDEX idx_draw_result (draw_result_id),
    INDEX idx_lottery (lottery_id),
    FOREIGN KEY (order_id) REFERENCES `order`(id) ON DELETE CASCADE,
    FOREIGN KEY (draw_result_id) REFERENCES draw_result(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '訂單明細';

-- ======================================================
-- 4. 獎品池系統（Prize Pool）
-- ======================================================

-- 商品獎品池配置表
CREATE TABLE IF NOT EXISTS lottery_prize_pool (
    id VARCHAR(50) PRIMARY KEY COMMENT '獎品池ID',
    lottery_id VARCHAR(50) NOT NULL COMMENT '商品ID',
    prize_id VARCHAR(50) NOT NULL COMMENT '獎品ID',
    prize_level VARCHAR(10) NOT NULL COMMENT '獎項等級：A/B/C/D/E/LAST',
    total_quantity INT NOT NULL COMMENT '總數量',
    remaining_quantity INT NOT NULL COMMENT '剩餘數量',
    probability DECIMAL(5,2) COMMENT '中獎機率（%）',
    is_active TINYINT(1) DEFAULT 1 COMMENT '是否啟用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    INDEX idx_lottery (lottery_id),
    INDEX idx_prize (prize_id),
    INDEX idx_lottery_active (lottery_id, is_active),
    UNIQUE KEY uk_lottery_prize_level (lottery_id, prize_id, prize_level),
    FOREIGN KEY (lottery_id) REFERENCES lottery(id) ON DELETE CASCADE,
    FOREIGN KEY (prize_id) REFERENCES prize(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '商品獎品池';

-- ======================================================
-- 5. 初始化測試資料（開發環境）
-- ======================================================

-- 為測試用戶創建錢包（如果 user 表已有資料）
INSERT IGNORE INTO wallet (id, user_id, gold, bonus)
SELECT 
    CONCAT('wallet-', user.id) as id,
    user.id as user_id,
    10000 as gold,  -- 測試用：每人 10000 Gold
    500 as bonus    -- 測試用：每人 500 Bonus
FROM user
WHERE user.email IN ('user@example.com', 'test@example.com');

-- ======================================================
-- 6. 視圖（可選）- 方便查詢
-- ======================================================

-- 賞品盒視圖（包含完整資訊）
CREATE OR REPLACE VIEW v_prize_box AS
SELECT 
    dr.id as draw_result_id,
    dr.user_id,
    dr.lottery_id,
    l.title as lottery_title,
    l.image_url as lottery_image_url,
    dr.store_id,
    s.name as store_name,
    dr.prize_id,
    dr.prize_name,
    dr.prize_image_url,
    dr.prize_level,
    dr.status,
    dr.order_id,
    dr.drawn_at,
    dr.recycled_at
FROM draw_result dr
LEFT JOIN lottery l ON dr.lottery_id = l.id
LEFT JOIN store s ON dr.store_id = s.id
WHERE dr.status = 'IN_PRIZE_BOX';

-- 訂單概覽視圖
CREATE OR REPLACE VIEW v_order_overview AS
SELECT 
    o.id as order_id,
    o.order_no,
    o.user_id,
    u.username as user_name,
    o.store_id,
    s.name as store_name,
    o.status,
    COUNT(oi.id) as item_count,
    o.shipping_type,
    o.recipient_name,
    o.recipient_phone,
    o.created_at,
    o.shipped_at
FROM `order` o
LEFT JOIN user u ON o.user_id = u.id
LEFT JOIN store s ON o.store_id = s.id
LEFT JOIN order_item oi ON o.id = oi.order_id
GROUP BY o.id, o.order_no, o.user_id, u.username, o.store_id, s.name, 
         o.status, o.shipping_type, o.recipient_name, o.recipient_phone, 
         o.created_at, o.shipped_at;

-- ======================================================
-- 7. 完成提示
-- ======================================================

SELECT '✅ 資料庫初始化完成！' as message;
SELECT '📦 已建立表格：wallet, wallet_transaction, draw_result, order, order_item, lottery_prize_pool' as tables;
SELECT '👁️ 已建立視圖：v_prize_box, v_order_overview' as views;
SELECT '💰 測試錢包已建立（10000 Gold + 500 Bonus）' as test_data;
