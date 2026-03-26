# wallet_transaction 表缺失修復指南

## 問題診斷
- **錯誤代碼**: Table 'kuji.wallet_transaction' doesn't exist
- **發生位置**: POST /api/recharge 調用時
- **根本原因**: `wallet_transaction` 表尚未在資料庫中創建

## 快速修復步驟

### 方案 A：使用 MySQL Command Line（推薦）

#### 1. 連接到 RDS 資料庫
```bash
mysql -h 18.179.187.129 -P 3306 -u admin -p kuji
```
- 輸入你的 MySQL 密碼

#### 2. 執行以下 SQL 創建表
```sql
-- 建立 wallet_transaction 表
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
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
  INDEX `idx_user_time` (`user_id`, `created_at` DESC),
  INDEX `idx_type` (`transaction_type`),
  INDEX `idx_related_id` (`related_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='點數異動記錄';
```

#### 3. 驗證表已創建
```sql
-- 檢查表是否存在
SHOW TABLES LIKE 'wallet_transaction';

-- 查看表結構
DESC wallet_transaction;
```

### 方案 B：使用 DBeaver/MySQL Workbench GUI

1. 連接到 RDS 資料庫（18.179.187.129）
2. 選擇 `kuji` 資料庫
3. 新增 Query
4. 複製上面的 CREATE TABLE SQL
5. 執行

### 方案 C：使用提供的 SQL 文件

1. 找到 `create-wallet-transaction-table.sql` 文件
2. 在你的 MySQL 客戶端中執行：
```bash
mysql -h 18.179.187.129 -u admin -p kuji < create-wallet-transaction-table.sql
```

## 表結構詳解

| 欄位名 | 類型 | 說明 | 備註 |
|-------|------|------|------|
| `id` | VARCHAR(36) | 交易ID（UUID） | PRIMARY KEY |
| `user_id` | VARCHAR(36) | 玩家ID | FK → user.id |
| `transaction_type` | VARCHAR(20) | 交易類型 | RECHARGE/DRAW/etc |
| `coin_type` | VARCHAR(10) | 幣種 | GOLD/BONUS |
| `amount` | BIGINT | 金額變化 | +: 增加, -: 減少 |
| `balance_after` | BIGINT | 交易後餘額 | 用於審計 |
| `related_id` | VARCHAR(36) | 關聯ID | 儲值ID/訂單ID等 |
| `description` | VARCHAR(500) | 說明文字 | 例：儲值：方案名稱 |
| `created_by` | VARCHAR(36) | 操作者ID | 系統調整時填入 |
| `created_at` | DATETIME | 建立時間 | DEFAULT CURRENT_TIMESTAMP |

## 索引說明

- `idx_user_time`: 查詢特定使用者的交易記錄時使用
- `idx_type`: 統計不同類型交易時使用
- `idx_related_id`: 查詢關聯交易時使用

## 創建後的驗證

### 1. 確認表存在
```sql
SELECT TABLE_NAME FROM information_schema.TABLES 
WHERE TABLE_SCHEMA='kuji' AND TABLE_NAME='wallet_transaction';
```
應該返回一行結果。

### 2. 確認表結構
```sql
DESC wallet_transaction;
```
應該顯示 10 個欄位。

### 3. 確認外鍵
```sql
SELECT CONSTRAINT_NAME, TABLE_NAME, COLUMN_NAME, REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_NAME='wallet_transaction' AND COLUMN_NAME='user_id';
```

## 建立後的測試

### 1. 測試插入
```sql
INSERT INTO wallet_transaction (
  id, user_id, transaction_type, coin_type, amount, 
  balance_after, description, created_at
) VALUES (
  UUID(), '{{user_id}}', 'TEST', 'GOLD', 100, 
  1000, '測試交易', NOW()
);
```

### 2. 測試查詢
```sql
SELECT * FROM wallet_transaction LIMIT 5;
```

## 故障排除

### 如果遇到外鍵錯誤
```
Error 1452: Cannot add or update a child row: a foreign key constraint fails
```
**原因**: user_id 不存在
**解決**: 確保 user 表中存在該 user_id

### 如果遇到索引錯誤
```
Error 1061: Duplicate key name
```
**原因**: 索引已存在
**解決**: 使用 `CREATE TABLE IF NOT EXISTS` 應該能避免

## 完成後的步驟

1. ✅ 表已創建
2. ✅ 重新部署應用或重啟應用
3. ✅ 再次調用 POST /api/recharge API
4. ✅ 驗證金幣已更新

## 預期結果

```bash
POST /api/recharge
Content-Type: application/json

{
  "planId": "plan-id-123",
  "paymentMethod": "CREDIT_CARD",
  "remark": "test recharge"
}

# 預期返回（成功）
{
  "success": true,
  "data": {
    "id": "recharge-id-xxx",
    "planId": "plan-id-123",
    "amount": 500,
    "goldCoins": 1000,
    "bonusCoins": 100,
    "paymentStatus": "COMPLETED",
    "transactionId": "TEST-xxxxxxxx",
    "paidAt": "2026-02-07T20:30:00"
  },
  "meta": {
    "timestamp": "2026-02-07T20:30:00Z",
    "requestId": "xxx"
  }
}
```

## 相關文件
- SQL 建表文件: `create-wallet-transaction-table.sql`
- RechargeServiceImpl: 使用 WalletTransactionMapper 創建審計記錄
- WalletTransactionMapper.xml: MyBatis 映射配置

---

**時間**: 2026-02-08
**狀態**: ❌ 表缺失 → ⏳ 等待執行 SQL
