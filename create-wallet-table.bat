@echo off
REM 連接到 AWS RDS 並創建 wallet_transaction 表
REM 請先確保已安裝 MySQL Command Line Client

setlocal enabledelayedexpansion

REM 設定資料庫參數
set DB_HOST=18.179.187.129
set DB_PORT=3306
set DB_USER=admin
set DB_NAME=kuji

echo ====================================
echo 連接到 RDS 資料庫並創建 wallet_transaction 表
echo ====================================
echo.
echo 資料庫信息:
echo   主機: %DB_HOST%
echo   埠: %DB_PORT%
echo   使用者: %DB_USER%
echo   資料庫: %DB_NAME%
echo.

mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p %DB_NAME% << EOF
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

SHOW TABLES LIKE 'wallet_transaction';
DESC wallet_transaction;
EOF

echo.
echo ✅ 表創建完成！
pause
