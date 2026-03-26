# wallet_transaction 表建立 - 完整執行步驟

## 問題總結
- **錯誤代碼**: 3780
- **原因**: 外鍵約束中的 `user_id` 和 `user.id` 字符集/編碼不兼容
- **解決方案**: 分 2 步建表

## 執行步驟

### 📋 Step 1: 建立表（無外鍵）

執行以下 SQL（複製自 `create-wallet-transaction-table.sql`）：

```sql
USE kuji;

CREATE TABLE IF NOT EXISTS `wallet_transaction` (
  `id` VARCHAR(36) PRIMARY KEY COMMENT 'UUID',
  `user_id` VARCHAR(36) NOT NULL COMMENT '玩家 ID',
  `transaction_type` VARCHAR(20) NOT NULL COMMENT '類型：RECHARGE/DRAW/CONSUME/RECYCLE/REFUND/BONUS_GRANT/ADMIN_ADJUST',
  `coin_type` VARCHAR(10) NOT NULL COMMENT '幣種：GOLD/BONUS',
  `amount` BIGINT NOT NULL COMMENT '金額（正數=增加，負數=減少）',
  `balance_after` BIGINT NOT NULL COMMENT '異動後餘額',
  `related_id` VARCHAR(36) COMMENT '關聯 ID（抽獎ID、訂單ID、儲值ID等）',
  `description` VARCHAR(500) COMMENT '說明',
  `created_by` VARCHAR(36) COMMENT '操作者 ID（系統調整時記錄管理員）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_user_time` (`user_id`, `created_at` DESC),
  INDEX `idx_type` (`transaction_type`),
  INDEX `idx_related_id` (`related_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='點數異動記錄';
```

**預期結果**：✅ Query OK, 0 rows affected

### ✅ Step 2: 驗證表建立成功

執行以下驗證命令：

```sql
-- 查看表是否存在
SHOW TABLES LIKE 'wallet_transaction';

-- 檢查表結構
DESC wallet_transaction;

-- 檢查表的詳細信息
SELECT TABLE_NAME, TABLE_COLLATION, ENGINE 
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA='kuji' AND TABLE_NAME='wallet_transaction';
```

**預期輸出**：
```
+-----------------------+
| wallet_transaction    |
+-----------------------+

+------------------+----------+------+-----+-------------------+------------------+
| Field            | Type     | Null | Key | Default           | Extra            |
+------------------+----------+------+-----+-------------------+------------------+
| id               | varchar  | NO   | PRI | NULL              |                  |
| user_id          | varchar  | NO   | MUL | NULL              |                  |
| transaction_type | varchar  | NO   | MUL | NULL              |                  |
| coin_type        | varchar  | NO   |     | NULL              |                  |
| amount           | bigint   | NO   |     | NULL              |                  |
| balance_after    | bigint   | NO   |     | NULL              |                  |
| related_id       | varchar  | YES  | MUL | NULL              |                  |
| description      | varchar  | YES  |     | NULL              |                  |
| created_by       | varchar  | YES  |     | NULL              |                  |
| created_at       | datetime | NO   |     | CURRENT_TIMESTAMP | DEFAULT_GENERATED |
+------------------+----------+------+-----+-------------------+------------------+
```

### 🔗 Step 3: 添加外鍵約束（可選但推薦）

**等 Step 1 和 2 成功後**，執行以下命令添加外鍵：

```sql
-- 先檢查是否有現存的外鍵
SELECT CONSTRAINT_NAME 
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_NAME='wallet_transaction' AND COLUMN_NAME='user_id';

-- 如果沒有外鍵，添加新的
ALTER TABLE wallet_transaction
ADD CONSTRAINT `fk_wallet_transaction_user` 
FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE;
```

**預期結果**：✅ Query OK, 0 rows affected

### 📍 Step 4: 驗證外鍵（可選）

```sql
-- 查看外鍵約束
SELECT CONSTRAINT_NAME, TABLE_NAME, COLUMN_NAME, REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_NAME='wallet_transaction' AND COLUMN_NAME='user_id';

-- 查看完整的表創建語句
SHOW CREATE TABLE wallet_transaction\G
```

## 故障排除

### ❌ 如果 Step 1 失敗（仍然 Error 3780）

**原因**: 外鍵仍在約束中

**解決**: 刪除已存在的表並重新建立

```sql
-- 刪除現存的表
DROP TABLE IF EXISTS wallet_transaction;

-- 重新執行 Step 1 的建表語句
CREATE TABLE IF NOT EXISTS `wallet_transaction` (
  ... (複製完整的 SQL)
);
```

### ❌ 如果 Step 3 失敗（添加外鍵時出錯）

**可能原因**：
1. `user` 表的 `id` 欄位編碼不同
2. `user_id` 中存在不在 `user.id` 中的值

**解決方案**：

```sql
-- 檢查 user 表的 id 欄位信息
SELECT COLUMN_NAME, COLUMN_TYPE, COLLATION_NAME 
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA='kuji' AND TABLE_NAME='user' AND COLUMN_NAME='id';

-- 檢查是否有孤立的 user_id（不在 user 表中）
SELECT DISTINCT user_id FROM wallet_transaction 
WHERE user_id NOT IN (SELECT id FROM user);

-- 如果有孤立記錄，刪除它們
DELETE FROM wallet_transaction 
WHERE user_id NOT IN (SELECT id FROM user);

-- 再次嘗試添加外鍵
ALTER TABLE wallet_transaction
ADD CONSTRAINT `fk_wallet_transaction_user` 
FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE;
```

## 應用端修改

表建立成功後，應用程式應該能正常使用：

### 1. RechargeServiceImpl 中的代碼已正確
```java
WalletTransaction transaction = new WalletTransaction();
transaction.setId(UUID.randomUUID().toString());
transaction.setUserId(userId);
transaction.setTransactionType("RECHARGE");
transaction.setCoinType("GOLD");
transaction.setAmount(record.getGoldCoins());
transaction.setBalanceAfter(goldAfter);
transaction.setDescription("儲值：" + plan.getName());
transaction.setRelatedId(record.getId());
transaction.setCreatedAt(now);
walletTransactionMapper.insert(transaction);
```

### 2. 重新部署或重啟應用
```bash
# 方案 1: 重新打包
mvn clean package -DskipTests

# 方案 2: 直接重啟（如果已部署）
# 在 EC2 上停止並重啟 JAR
pkill -f admin-1.0.0.jar
nohup java -jar target/admin-1.0.0.jar > app.log 2>&1 &
```

## 完整流程總結

```
1. 執行 create-wallet-transaction-table.sql (Step 1)
   ↓
2. 驗證表建立成功 (Step 2)
   ↓
3. 添加外鍵約束 (Step 3 - 可選)
   ↓
4. 驗證外鍵 (Step 4 - 可選)
   ↓
5. 重新部署應用
   ↓
6. 測試 POST /api/recharge API
```

## 預期測試結果

```bash
POST /api/recharge HTTP/1.1
Host: 18.179.187.129:8080
Content-Type: application/json

{
  "planId": "{{your_plan_id}}",
  "paymentMethod": "CREDIT_CARD",
  "remark": "test recharge"
}

# 預期返回（成功）
{
  "success": true,
  "data": {
    "id": "recharge-id-xxx",
    "planId": "{{plan_id}}",
    "amount": 500,
    "goldCoins": 1000,
    "bonusCoins": 100,
    "paymentStatus": "COMPLETED",
    "transactionId": "TEST-xxxxxxxx",
    "paidAt": "2026-02-08T..."
  },
  "meta": {
    "timestamp": "2026-02-08T...",
    "requestId": "xxx"
  }
}
```

## 檔案清單

- ✅ `create-wallet-transaction-table.sql` - 主要建表 SQL（已更新）
- ✅ `create-wallet-transaction-table-no-fk.sql` - 備選版本
- ✅ `WALLET_TRANSACTION_TABLE_FIX.md` - 初始指南
- ✅ `WALLET_TRANSACTION_FK_COMPATIBILITY_FIX.md` - 詳細診斷
- ✅ `WALLET_TRANSACTION_EXECUTION_GUIDE.md` - 本檔案

---

**狀態**: ⏳ 等待執行 SQL
**更新時間**: 2026-02-08
**推薦步驟**: Step 1 + Step 2 + Step 3（可選）
