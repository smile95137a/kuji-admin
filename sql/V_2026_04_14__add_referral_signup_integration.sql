-- =======================
-- 資料庫遷移指令碼
-- 推薦碼於使用者註冊流程整合
-- Date: 2026-04-14
-- =======================

-- 1. user 表新增欄位
ALTER TABLE user ADD COLUMN IF NOT EXISTS referral_code VARCHAR(50) 
  DEFAULT NULL UNIQUE COMMENT '推薦碼（一次性，不可變更）';

ALTER TABLE user ADD COLUMN IF NOT EXISTS referred_store_id VARCHAR(36) 
  DEFAULT NULL COMMENT '推薦來源店家 ID';

ALTER TABLE user ADD COLUMN IF NOT EXISTS referral_bound_at TIMESTAMP 
  DEFAULT NULL COMMENT '推薦碼綁定時間';

ALTER TABLE user ADD COLUMN IF NOT EXISTS is_oauth_new_user TINYINT(1) 
  DEFAULT 0 COMMENT '標記：是否為 OAuth 新用戶首次登入';

-- 2. 新增外鍵約束
ALTER TABLE user ADD CONSTRAINT IF NOT EXISTS fk_user_referred_store
  FOREIGN KEY (referred_store_id) REFERENCES store(id) ON DELETE SET NULL;

-- 3. referral_record 表新增欄位
ALTER TABLE referral_record ADD COLUMN IF NOT EXISTS signup_method ENUM('EMAIL', 'OAUTH') 
  DEFAULT 'EMAIL' COMMENT 'EMAIL=官網註冊時綁定, OAUTH=登入後補碼';

-- 4. 新增索引以加速查詢
CREATE INDEX idx_user_referral_code ON user(referral_code);
CREATE INDEX idx_user_referred_store_id ON user(referred_store_id);
CREATE INDEX idx_referral_record_signup_method ON referral_record(signup_method);

-- 驗證遷移成功
SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_DEFAULT, EXTRA
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'kuji' AND TABLE_NAME = 'user'
AND COLUMN_NAME IN ('referral_code', 'referred_store_id', 'referral_bound_at', 'is_oauth_new_user');

SELECT COLUMN_NAME, COLUMN_TYPE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'kuji' AND TABLE_NAME = 'referral_record'
AND COLUMN_NAME = 'signup_method';
