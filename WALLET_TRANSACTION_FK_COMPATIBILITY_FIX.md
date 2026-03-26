# wallet_transaction 表外鍵兼容性修復

## 錯誤分析
```
Error Code: 3780
Referencing column 'user_id' and referenced column 'id' in foreign key constraint 'wallet_transaction_ibfk_1' are incompatible.
```

**原因**: MySQL 外鍵約束要求主鍵和外鍵的字符集、編碼、欄位長度必須完全匹配。

## 修復方案（3 選 1）

### ✅ 方案 A：使用更新的 SQL（推薦）

執行 `create-wallet-transaction-table.sql` 中的 SQL：

```sql
CREATE TABLE IF NOT EXISTS `wallet_transaction` (
  `id` VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci PRIMARY KEY COMMENT 'UUID',
  `user_id` VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '玩家 ID',
  `transaction_type` VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '類型',
  `coin_type` VARCHAR(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '幣種：GOLD/BONUS',
  `amount` BIGINT NOT NULL COMMENT '金額',
  `balance_after` BIGINT NOT NULL COMMENT '異動後餘額',
  `related_id` VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '關聯 ID',
  `description` VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '說明',
  `created_by` VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '操作者 ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
  CONSTRAINT `fk_wallet_transaction_user` 
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  INDEX `idx_user_time` (`user_id`, `created_at` DESC),
  INDEX `idx_type` (`transaction_type`),
  INDEX `idx_related_id` (`related_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='點數異動記錄';
```

**特點**：
- ✅ 明確指定字符集：`CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci`
- ✅ 命名外鍵約束：`fk_wallet_transaction_user`
- ✅ 設定外鍵行為：`ON DELETE RESTRICT ON UPDATE CASCADE`
- ✅ 預防未來的兼容性問題

### 📋 方案 B：先建表，後添加外鍵（備選）

**Step 1**: 執行 `create-wallet-transaction-table-no-fk.sql` 建立無外鍵的表

```sql
CREATE TABLE IF NOT EXISTS `wallet_transaction` (
  `id` VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci PRIMARY KEY,
  `user_id` VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `transaction_type` VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `coin_type` VARCHAR(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `amount` BIGINT NOT NULL,
  `balance_after` BIGINT NOT NULL,
  `related_id` VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `description` VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `created_by` VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_user_time` (`user_id`, `created_at` DESC),
  INDEX `idx_type` (`transaction_type`),
  INDEX `idx_related_id` (`related_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**Step 2**: 驗證表建立成功後，執行以下命令添加外鍵

```sql
ALTER TABLE wallet_transaction
ADD CONSTRAINT `fk_wallet_transaction_user` 
FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) 
ON DELETE RESTRICT ON UPDATE CASCADE;
```

### 🔍 方案 C：診斷後手動調整

**Step 1**: 先執行診斷 SQL（`diagnose-user-table.sql`）

```sql
-- 檢查 user 表的字符集和編碼
SELECT TABLE_NAME, TABLE_COLLATION 
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA='kuji' AND TABLE_NAME='user';

-- 檢查 user.id 欄位的詳細信息
SELECT COLUMN_NAME, COLUMN_TYPE, COLLATION_NAME 
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA='kuji' AND TABLE_NAME='user' AND COLUMN_NAME='id';
```

**Step 2**: 根據診斷結果調整表定義

如果 `user.id` 使用 `latin1` 或其他編碼，需要調整為相同編碼：

```sql
-- 例：如果 user 使用 latin1，則 wallet_transaction 也要用 latin1
CREATE TABLE IF NOT EXISTS `wallet_transaction` (
  `id` VARCHAR(36) CHARACTER SET latin1 COLLATE latin1_swedish_ci PRIMARY KEY,
  `user_id` VARCHAR(36) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  -- ... 其他欄位
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
```

## 執行步驟

### 快速修復（推薦方案 A）

1. **連接到 RDS 資料庫**
   ```bash
   mysql -h 18.179.187.129 -u admin -p kuji
   ```

2. **複製並執行 SQL**（來自 `create-wallet-transaction-table.sql`）

3. **驗證表建立**
   ```sql
   DESC wallet_transaction;
   SHOW CREATE TABLE wallet_transaction\G
   ```

4. **檢查外鍵**
   ```sql
   SELECT CONSTRAINT_NAME, TABLE_NAME, COLUMN_NAME, REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME
   FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
   WHERE TABLE_NAME='wallet_transaction' AND COLUMN_NAME='user_id';
   ```

## 驗證清單

- [ ] 表 `wallet_transaction` 已建立
- [ ] 表有 10 個欄位（id, user_id, transaction_type 等）
- [ ] 外鍵約束存在（可選，但推薦）
- [ ] 字符集為 `utf8mb4`
- [ ] 編碼為 `utf8mb4_unicode_ci`

## 建立後測試

```sql
-- 測試插入（確保外鍵有效）
INSERT INTO wallet_transaction (
  id, user_id, transaction_type, coin_type, amount, 
  balance_after, description, created_at
) VALUES (
  '550e8400-e29b-41d4-a716-446655440000',
  '{{existing_user_id}}',
  'TEST',
  'GOLD',
  100,
  1000,
  'Test transaction',
  NOW()
);

-- 查看是否插入成功
SELECT * FROM wallet_transaction LIMIT 1;
```

## 如果仍有問題

### 問題 1：外鍵约束仍然失败
```
Error 3780: ...
```
**解決**：
1. 刪除現有的 wallet_transaction 表
   ```sql
   DROP TABLE IF EXISTS wallet_transaction;
   ```
2. 使用方案 B（先建表，後添加外鍵）

### 問題 2：INSERT 失敗（外鍵約束）
```
Error 1452: Cannot add or update a child row: a foreign key constraint fails
```
**原因**：user_id 不存在於 user 表
**解決**：確保使用現存的 user.id

```sql
-- 查看現存的 user
SELECT id FROM user LIMIT 5;

-- 使用其中一個 ID 進行測試
INSERT INTO wallet_transaction (...)
VALUES (..., '{{valid_user_id}}', ...);
```

### 問題 3：字符集不匹配
```
Error 3780: ... are incompatible
```
**解決**：執行診斷 SQL，確保 `user` 表和 `wallet_transaction` 的字符集一致

## 相關檔案
- `create-wallet-transaction-table.sql` - 推薦版本（含外鍵）
- `create-wallet-transaction-table-no-fk.sql` - 無外鍵版本
- `diagnose-user-table.sql` - 診斷工具
- `WALLET_TRANSACTION_TABLE_FIX.md` - 初始修復指南

---

**最後更新**: 2026-02-08
**建議方案**: 方案 A（推薦）或方案 B（如果 A 失敗）
