-- V033: 為 wallet_transaction 報表查詢新增索引
-- 用途：033-platform-revenue-report 使用 related_id LEFT JOIN 做 storeBreakdown 時避免全表掃描

-- 1. related_id 索引（storeBreakdown LEFT JOIN lottery_ticket / order 時必需）
SET @exist := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE table_schema = DATABASE()
      AND table_name = 'wallet_transaction'
      AND index_name = 'idx_wt_related_id'
);
SET @sqlstmt := IF(
    @exist = 0,
    'CREATE INDEX `idx_wt_related_id` ON `wallet_transaction` (`related_id`)',
    'SELECT ''Index idx_wt_related_id already exists'''
);
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. (transaction_type, coin_type, created_at) 複合索引（聚合查詢 totalRecharge / totalSpend 優化）
SET @exist := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE table_schema = DATABASE()
      AND table_name = 'wallet_transaction'
      AND index_name = 'idx_wt_type_coin_created'
);
SET @sqlstmt := IF(
    @exist = 0,
    'CREATE INDEX `idx_wt_type_coin_created` ON `wallet_transaction` (`transaction_type`, `coin_type`, `created_at`)',
    'SELECT ''Index idx_wt_type_coin_created already exists'''
);
PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 預估效能改善：
--   10 萬筆 wallet_transaction，storeBreakdown 查詢從 ~2s → ~50ms
--   totalRecharge / totalSpend 聚合從 ~500ms → ~20ms
