-- ====================================================
-- 補充推薦碼系統缺少的欄位
-- 不刪除現有資料，只新增欄位
-- ====================================================

-- 1. referral_code 表補充欄位
ALTER TABLE referral_code 
ADD COLUMN store_id VARCHAR(36) COMMENT '所屬店家 ID' AFTER code,
ADD COLUMN description VARCHAR(200) COMMENT '推薦碼描述' AFTER store_id,
ADD INDEX idx_store_id (store_id);

-- 2. referral_record 表補充欄位
ALTER TABLE referral_record
ADD COLUMN user_id VARCHAR(36) COMMENT '被推薦人 ID' AFTER id,
ADD COLUMN store_id VARCHAR(36) COMMENT '所屬店家 ID' AFTER referral_code_id,
ADD COLUMN used_code VARCHAR(20) COMMENT '使用的推薦碼（快照）' AFTER store_id,
ADD COLUMN referred_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '推薦時間' AFTER used_code,
ADD INDEX idx_user_id (user_id),
ADD INDEX idx_store_id (store_id);

-- 3. 驗證新增的欄位
SELECT '=== referral_code 表結構 ===' as info;
DESCRIBE referral_code;

SELECT '=== referral_record 表結構 ===' as info;
DESCRIBE referral_record;

-- ====================================================
-- 執行完成後，請執行 MyBatis Generator 重新生成 Entity
-- mvn mybatis-generator:generate
-- 然後手動加回 Mapper 的自定義方法（我會提供）
-- ====================================================
