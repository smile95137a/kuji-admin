#!/bin/bash
# 執行 Referral Signup 數據庫遷移
# 用法：bash run-migration.sh

set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
SQL_FILE="${SCRIPT_DIR}/sql/V_2026_04_14__add_referral_signup_integration.sql"

if [ ! -f "$SQL_FILE" ]; then
    echo "❌ SQL 檔案不存在: $SQL_FILE"
    exit 1
fi

echo "🚀 開始執行 Referral Signup 遷移..."
echo "📄 SQL 檔案: $SQL_FILE"
echo ""

# 讀取 MySQL 配置（如果有 .env）
if [ -f "${SCRIPT_DIR}/.env" ]; then
    source "${SCRIPT_DIR}/.env"
fi

# 預設配置
DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-3306}"
DB_USER="${DB_USER:-root}"
DB_PASS="${DB_PASS:-root}"
DB_NAME="${DB_NAME:-kuji}"

echo "📌 數據庫配置:"
echo "   主機: $DB_HOST:$DB_PORT"
echo "   用戶: $DB_USER"
echo "   數據庫: $DB_NAME"
echo ""

# 執行 SQL 遷移
mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" < "$SQL_FILE"

echo ""
echo "✅ Referral Signup 遷移完成"
echo ""
echo "驗證結果："
mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" << EOF
SELECT COUNT(*) as 'referral_code 欄位數' FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'user' 
AND COLUMN_NAME = 'referral_code' 
AND TABLE_SCHEMA = '$DB_NAME';

SELECT COUNT(*) as '新索引數' FROM INFORMATION_SCHEMA.STATISTICS 
WHERE TABLE_NAME = 'user' 
AND TABLE_SCHEMA = '$DB_NAME'
AND INDEX_NAME IN ('idx_referral_code', 'idx_referred_store_id');
EOF
