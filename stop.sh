#!/bin/bash
# KUJI 後端停止腳本

APP_DIR="/home/ec2-user/kuji-backend"
PID_FILE="$APP_DIR/app.pid"

if [ ! -f "$PID_FILE" ]; then
    echo "⚠️  應用程式未運行（找不到 PID 檔案）"
    exit 0
fi

PID=$(cat "$PID_FILE")

if ! ps -p $PID > /dev/null 2>&1; then
    echo "⚠️  應用程式未運行（PID $PID 不存在）"
    rm -f "$PID_FILE"
    exit 0
fi

echo "🛑 停止應用程式 (PID: $PID)..."
kill $PID

# 等待最多 30 秒
for i in {1..30}; do
    if ! ps -p $PID > /dev/null 2>&1; then
        echo "✅ 應用程式已停止"
        rm -f "$PID_FILE"
        exit 0
    fi
    sleep 1
done

# 如果還沒停止，強制終止
echo "⚠️  正常停止超時，強制終止..."
kill -9 $PID
rm -f "$PID_FILE"
echo "✅ 應用程式已強制停止"
