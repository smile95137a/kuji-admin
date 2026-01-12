-- ====================================================
-- 資料庫主鍵遷移腳本：BIGINT → VARCHAR(36) UUID
-- ⚠️ 警告：這是破壞性操作，執行前請務必備份資料庫！
-- ====================================================

-- 使用說明：
-- 1. 備份資料庫：mysqldump -h ... -u admin -p kuji > backup_$(date +%Y%m%d_%H%M%S).sql
-- 2. 確認當前沒有重要資料或在測試環境執行
-- 3. 分段執行以下腳本

-- ====================================================
-- 階段一：檢查當前狀態
-- ====================================================

-- 檢查 store 表的 id 型態
SELECT 
    COLUMN_NAME, 
    DATA_TYPE, 
    COLUMN_TYPE,
    COLUMN_KEY
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'kuji' 
  AND TABLE_NAME = 'store' 
  AND COLUMN_NAME = 'id';

-- 檢查 user 表的 id 型態
SELECT 
    COLUMN_NAME, 
    DATA_TYPE, 
    COLUMN_TYPE,
    COLUMN_KEY
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'kuji' 
  AND TABLE_NAME = 'user' 
  AND COLUMN_NAME = 'id';

-- ====================================================
-- 階段二：如果 id 是 BIGINT，執行以下遷移
-- ====================================================

-- ⚠️ 注意：以下操作會刪除所有依賴的外鍵約束和資料
-- 建議方案：在開發初期直接重建資料庫

-- 方案 A：完全重建（推薦用於開發階段）
-- DROP DATABASE IF EXISTS kuji;
-- CREATE DATABASE kuji CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- USE kuji;
-- 然後執行 DDL_UUID.sql

-- 方案 B：保留資料的遷移（複雜，僅在有重要資料時使用）
-- 以下是 store 表的遷移範例

SET FOREIGN_KEY_CHECKS = 0;

-- 1. 創建臨時 UUID 欄位
ALTER TABLE store ADD COLUMN id_uuid VARCHAR(36) AFTER id;

-- 2. 為每筆資料生成 UUID
UPDATE store SET id_uuid = UUID();

-- 3. 更新所有引用 store.id 的外鍵表（需要先找出所有依賴表）
-- 範例：如果有 lottery 表依賴 store
-- ALTER TABLE lottery ADD COLUMN store_id_uuid VARCHAR(36) AFTER store_id;
-- UPDATE lottery l 
-- INNER JOIN store s ON l.store_id = s.id 
-- SET l.store_id_uuid = s.id_uuid;

-- 4. 刪除舊的外鍵約束
-- ALTER TABLE lottery DROP FOREIGN KEY fk_lottery_store;
-- ALTER TABLE lottery DROP COLUMN store_id;
-- ALTER TABLE lottery CHANGE COLUMN store_id_uuid store_id VARCHAR(36) NOT NULL;

-- 5. 刪除 store 表的舊主鍵和 id 欄位
ALTER TABLE store DROP PRIMARY KEY;
ALTER TABLE store DROP COLUMN id;

-- 6. 將 id_uuid 重命名為 id 並設為主鍵
ALTER TABLE store CHANGE COLUMN id_uuid id VARCHAR(36) NOT NULL;
ALTER TABLE store ADD PRIMARY KEY (id);

-- 7. 重新建立外鍵約束
-- ALTER TABLE lottery 
-- ADD CONSTRAINT fk_lottery_store 
-- FOREIGN KEY (store_id) REFERENCES store(id) ON DELETE CASCADE;

SET FOREIGN_KEY_CHECKS = 1;

-- ====================================================
-- 階段三：驗證遷移結果
-- ====================================================

-- 檢查 store 表結構
DESCRIBE store;

-- 檢查 user 表結構
DESCRIBE `user`;

-- 檢查資料完整性
SELECT COUNT(*) FROM store;
SELECT COUNT(*) FROM `user`;

-- ====================================================
-- 結論與建議
-- ====================================================

-- 🔴 如果資料庫是開發/測試環境且資料不重要：
--    建議使用「方案 A」直接重建資料庫，然後執行 DDL_UUID.sql

-- 🟡 如果已有重要資料：
--    需要完整的遷移腳本，需要識別所有依賴表並逐一遷移

-- 🟢 推薦做法（開發階段）：
--    1. 備份當前資料庫
--    2. 刪除並重建資料庫
--    3. 執行 DDL_UUID.sql（使用 VARCHAR(36) UUID）
--    4. 執行 referral_address_schema.sql（啟用外鍵約束）
--    5. 執行 DataInitializer 初始化基礎資料
