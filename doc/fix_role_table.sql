-- =============================================
-- 修正 role 表格：新增 code 欄位
-- ⚠️ 警告：此 SQL 僅供 LOCAL 開發環境使用！
-- ⚠️ 資料庫：localhost:3306/kuji
-- ⚠️ 請勿在正式環境執行！
-- =============================================

-- 1. 新增 code 欄位（允許 NULL 以便現有資料順利遷移）
ALTER TABLE role 
ADD COLUMN code VARCHAR(50) AFTER name;

-- 2. 為現有資料填充預設值
UPDATE role SET code = 'ROLE_ADMIN' WHERE name = '系統管理員';
UPDATE role SET code = 'ROLE_STORE_OWNER' WHERE name = '店家負責人';
UPDATE role SET code = 'ROLE_STORE_EDITOR' WHERE name = '店家編輯';

-- 3. 設定 code 為 NOT NULL 並加上 UNIQUE 約束
ALTER TABLE role 
MODIFY COLUMN code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色代碼：ROLE_ADMIN/ROLE_STORE_OWNER/ROLE_STORE_EDITOR';

-- 4. 驗證結果
SELECT id, name, code, description FROM role;
