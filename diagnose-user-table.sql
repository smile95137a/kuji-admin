-- 診斷 user 表的字符集和欄位定義
USE kuji;

-- 檢查 user 表的字符集
SELECT TABLE_NAME, TABLE_COLLATION 
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA='kuji' AND TABLE_NAME='user';

-- 檢查 user 表的 id 欄位詳細信息
SELECT COLUMN_NAME, COLUMN_TYPE, COLLATION_NAME, IS_NULLABLE, COLUMN_KEY
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA='kuji' AND TABLE_NAME='user' AND COLUMN_NAME='id';

-- 檢查是否有現存的 wallet_transaction 表
SHOW TABLES LIKE 'wallet_transaction';

-- 列出所有表和它們的字符集
SELECT TABLE_NAME, TABLE_COLLATION 
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA='kuji'
ORDER BY TABLE_NAME;
