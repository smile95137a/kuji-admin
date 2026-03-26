# wallet_transaction 表修復 - 快速參考卡

## ⚡ 3 分鐘快速修復

### 複製並執行此 SQL（在你的 MySQL 客戶端）

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

-- 驗證
DESC wallet_transaction;
```

### ✅ 驗證是否成功

```
+------------------+----------+------+-----+-------------------+------------------+
| Field            | Type     | Null | Key | Default           | Extra            |
+------------------+----------+------+-----+-------------------+------------------+
| id               | varchar  | NO   | PRI | NULL              |                  |
| user_id          | varchar  | NO   | MUL | NULL              |                  |
| transaction_type | varchar  | | NO   | MUL | NULL              |                  |
... (共 10 個欄位)
```

如果看到 10 個欄位，✅ 成功！

## 🎯 下一步

1. ✅ 執行上面的 SQL（完成表建立）
2. ✅ 重新部署應用或重啟應用
3. ✅ 測試 API：`POST /api/recharge`

## 🧪 測試 API

```bash
curl -X POST http://18.179.187.129:8080/api/recharge \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {{your_jwt_token}}" \
  -d '{
    "planId": "{{plan_id}}",
    "paymentMethod": "CREDIT_CARD",
    "remark": "test"
  }'

# 預期返回：
{
  "success": true,
  "data": {
    "paymentStatus": "COMPLETED",
    ...
  }
}
```

## 📚 詳細文檔

- 完整步驟：`WALLET_TRANSACTION_EXECUTION_GUIDE.md`
- 診斷工具：`diagnose-user-table.sql`
- 兼容性說明：`WALLET_TRANSACTION_FK_COMPATIBILITY_FIX.md`

---

**時間**: ~3 分鐘
**難度**: ⭐ 簡單
**狀態**: ⏳ 等待執行
