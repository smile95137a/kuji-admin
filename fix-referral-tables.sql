-- ====================================================
-- 修復推薦碼系統表結構
-- 問題：現有 Entity 結構與 Service 層使用的欄位不匹配
-- 解決：刪除舊表，建立正確結構
-- ====================================================

-- 檢查是否已有正確結構（執行前請先備份資料）
-- SHOW CREATE TABLE referral_code;
-- SHOW CREATE TABLE referral_record;

-- 1. 刪除舊表（如果存在）
DROP TABLE IF EXISTS referral_record;
DROP TABLE IF EXISTS referral_code;

-- 2. 建立推薦碼表（正確結構）
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
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='推薦碼表';

-- 3. 建立推薦記錄表（正確結構）
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
    
    FOREIGN KEY (referral_code_id) REFERENCES referral_code(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='推薦關係記錄表';

-- 4. 驗證表結構
SELECT 'referral_code 表結構：' as info;
DESCRIBE referral_code;

SELECT 'referral_record 表結構：' as info;
DESCRIBE referral_record;

-- 5. 建立測試推薦碼（可選）
-- INSERT INTO referral_code (id, code, store_id, description, is_active, used_count)
-- VALUES 
--     (UUID(), 'WELCOME2025', 'your-store-id', '2025 新年歡迎碼', 1, 0),
--     (UUID(), 'FRIEND10', 'your-store-id', '好友推薦優惠', 1, 0);

-- ====================================================
-- 執行完成後，請執行 MyBatis Generator 重新生成 Entity
-- mvn mybatis-generator:generate (不要用這個！會刪除自定義方法)
-- 或使用 MBGAutoRunner（保留自定義方法）
-- ====================================================
