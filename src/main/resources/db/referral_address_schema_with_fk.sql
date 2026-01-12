-- ====================================================
-- 推薦碼系統 & 使用者地址 資料表（啟用外鍵版本）
-- ⚠️ 此版本僅在 store 和 user 表使用 VARCHAR(36) UUID 時可用
-- 執行前請先確認主鍵型態！
-- ====================================================

-- 1. 推薦碼表
CREATE TABLE IF NOT EXISTS referral_code (
    id VARCHAR(36) PRIMARY KEY COMMENT '主鍵 UUID',
    code VARCHAR(20) NOT NULL UNIQUE COMMENT '推薦碼（唯一，大寫字母數字）',
    store_id VARCHAR(36) NOT NULL COMMENT '所屬店家 ID',
    description VARCHAR(200) COMMENT '推薦碼描述',
    is_active TINYINT(1) DEFAULT 1 COMMENT '是否啟用：1=啟用，0=停用',
    used_count INT DEFAULT 0 COMMENT '使用次數',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    
    INDEX idx_code (code),
    INDEX idx_store_id (store_id),
    INDEX idx_is_active (is_active),
    
    CONSTRAINT fk_referral_code_store FOREIGN KEY (store_id) REFERENCES store(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='推薦碼表';

-- 2. 推薦記錄表
CREATE TABLE IF NOT EXISTS referral_record (
    id VARCHAR(36) PRIMARY KEY COMMENT '主鍵 UUID',
    user_id VARCHAR(36) NOT NULL UNIQUE COMMENT '被推薦人 ID（一人只能被推薦一次）',
    referral_code_id VARCHAR(36) NOT NULL COMMENT '推薦碼 ID',
    store_id VARCHAR(36) NOT NULL COMMENT '所屬店家 ID',
    used_code VARCHAR(20) NOT NULL COMMENT '使用的推薦碼（快照）',
    referred_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '推薦時間',
    
    INDEX idx_user_id (user_id),
    INDEX idx_referral_code_id (referral_code_id),
    INDEX idx_store_id (store_id),
    INDEX idx_referred_at (referred_at),
    
    CONSTRAINT fk_referral_record_user FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE,
    CONSTRAINT fk_referral_record_code FOREIGN KEY (referral_code_id) REFERENCES referral_code(id) ON DELETE CASCADE,
    CONSTRAINT fk_referral_record_store FOREIGN KEY (store_id) REFERENCES store(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='推薦關係記錄表';

-- 3. 使用者收件地址表
CREATE TABLE IF NOT EXISTS user_address (
    id VARCHAR(36) PRIMARY KEY COMMENT '主鍵 UUID',
    user_id VARCHAR(36) NOT NULL COMMENT '使用者 ID',
    label VARCHAR(50) COMMENT '地址標籤（如：家、公司）',
    recipient_name VARCHAR(100) NOT NULL COMMENT '收件人姓名',
    recipient_phone VARCHAR(20) NOT NULL COMMENT '收件人電話',
    city VARCHAR(50) NOT NULL COMMENT '城市',
    district VARCHAR(50) NOT NULL COMMENT '區域',
    zip_code VARCHAR(10) COMMENT '郵遞區號',
    address VARCHAR(500) NOT NULL COMMENT '詳細地址',
    is_default TINYINT(1) DEFAULT 0 COMMENT '是否為預設地址：1=是，0=否',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    
    INDEX idx_user_id (user_id),
    INDEX idx_is_default (is_default),
    
    CONSTRAINT fk_user_address_user FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='使用者收件地址表';

-- ====================================================
-- 預設資料（可選）
-- ====================================================

-- 範例：為第一家店建立推薦碼（需要先有店家資料）
-- INSERT INTO referral_code (id, code, store_id, description, is_active, used_count)
-- SELECT UUID(), 'KUJI2024', id, '2024 新年特惠推薦碼', 1, 0
-- FROM store LIMIT 1;
