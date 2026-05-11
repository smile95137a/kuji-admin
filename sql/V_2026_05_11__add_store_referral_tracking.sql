-- 兼容較舊版 MySQL：避免直接使用 IF NOT EXISTS 語法

SET @exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'store'
      AND COLUMN_NAME = 'referrer_store_id'
);
SET @sql := IF(@exists = 0,
    'ALTER TABLE store ADD COLUMN referrer_store_id VARCHAR(36) NULL COMMENT ''推薦該店家進駐的來源店家 ID'' AFTER owner_id',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'store'
      AND COLUMN_NAME = 'referral_code_id'
);
SET @sql := IF(@exists = 0,
    'ALTER TABLE store ADD COLUMN referral_code_id VARCHAR(36) NULL COMMENT ''店家進駐時使用的推薦碼 ID'' AFTER referrer_store_id',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'store'
      AND COLUMN_NAME = 'activated_at'
);
SET @sql := IF(@exists = 0,
    'ALTER TABLE store ADD COLUMN activated_at DATETIME NULL COMMENT ''店家啟用成功時間'' AFTER status',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 對齊 referral_code.id 的欄位定義（長度/字元集/排序規則），避免 FK 3780
SET @referralCodeTableExists := (
    SELECT COUNT(*)
    FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'referral_code'
);

SET @storeReferralCodeColumnExists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'store'
      AND COLUMN_NAME = 'referral_code_id'
);

SET @targetLength := (
    SELECT CHARACTER_MAXIMUM_LENGTH
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'referral_code'
      AND COLUMN_NAME = 'id'
    LIMIT 1
);

SET @targetCharset := (
    SELECT CHARACTER_SET_NAME
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'referral_code'
      AND COLUMN_NAME = 'id'
    LIMIT 1
);

SET @targetCollation := (
    SELECT COLLATION_NAME
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'referral_code'
      AND COLUMN_NAME = 'id'
    LIMIT 1
);

SET @sql := IF(@referralCodeTableExists > 0 AND @storeReferralCodeColumnExists > 0 AND @targetLength IS NOT NULL,
    CONCAT(
  'ALTER TABLE store MODIFY COLUMN referral_code_id VARCHAR(', @targetLength, ')',
        IF(@targetCharset IS NOT NULL, CONCAT(' CHARACTER SET ', @targetCharset), ''),
        IF(@targetCollation IS NOT NULL, CONCAT(' COLLATE ', @targetCollation), ''),
  ' NULL',
        ' COMMENT ''店家進駐時使用的推薦碼 ID'''
    ),
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exists := (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME = 'store'
      AND CONSTRAINT_NAME = 'fk_store_referrer_store'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);
SET @sql := IF(@exists = 0,
    'ALTER TABLE store ADD CONSTRAINT fk_store_referrer_store FOREIGN KEY (referrer_store_id) REFERENCES store(id) ON DELETE SET NULL',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exists := (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME = 'store'
      AND CONSTRAINT_NAME = 'fk_store_referral_code'
      AND CONSTRAINT_TYPE = 'FOREIGN KEY'
);
SET @sql := IF(@exists = 0,
    'ALTER TABLE store ADD CONSTRAINT fk_store_referral_code FOREIGN KEY (referral_code_id) REFERENCES referral_code(id) ON DELETE SET NULL',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exists := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'store'
      AND INDEX_NAME = 'idx_store_referrer_store_id'
);
SET @sql := IF(@exists = 0,
    'CREATE INDEX idx_store_referrer_store_id ON store(referrer_store_id)',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exists := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'store'
      AND INDEX_NAME = 'idx_store_referral_code_id'
);
SET @sql := IF(@exists = 0,
    'CREATE INDEX idx_store_referral_code_id ON store(referral_code_id)',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exists := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'store'
      AND INDEX_NAME = 'idx_store_activated_at'
);
SET @sql := IF(@exists = 0,
    'CREATE INDEX idx_store_activated_at ON store(activated_at)',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;