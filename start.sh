#!/bin/bash
# KUJI 後端啟動腳本

APP_NAME="admin"
JAR_NAME="admin-1.0.0.jar"
APP_DIR="/home/ec2-user/kuji-backend"
LOG_DIR="$APP_DIR/logs"
PID_FILE="$APP_DIR/app.pid"
LOG_FILE="$LOG_DIR/application.log"

# 建立日誌目錄
mkdir -p "$LOG_DIR"

# 檢查是否已經在運行
if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if ps -p $PID > /dev/null 2>&1; then
        echo "⚠️  應用程式已在運行 (PID: $PID)"
        exit 1
    else
        echo "🧹 清理舊的 PID 檔案"
        rm -f "$PID_FILE"
    fi
fi

# 切換到應用目錄
cd "$APP_DIR" || exit 1

# 檢查 JAR 檔案
if [ ! -f "$JAR_NAME" ]; then
    echo "❌ 找不到 JAR 檔案: $JAR_NAME"
    exit 1
fi

# 設定環境變數
export SPRING_PROFILES_ACTIVE=prod
export JWT_SECRET="KUJI_PRODUCTION_SECRET_KEY_2026_VERY_SECURE_DO_NOT_SHARE"

# 啟動應用程式
echo "🚀 啟動 KUJI 後端..."
nohup java -jar \
    -Xms512m \
    -Xmx1024m \
    -Dspring.profiles.active=prod \
    -Dserver.port=8080 \
    "$JAR_NAME" \
    > "$LOG_FILE" 2>&1 &

# 儲存 PID
echo $! > "$PID_FILE"

# 等待啟動
sleep 5

# 檢查是否成功啟動
if ps -p $(cat "$PID_FILE") > /dev/null 2>&1; then
    echo "✅ 應用程式啟動成功 (PID: $(cat "$PID_FILE"))"
    echo "📝 日誌位置: $LOG_FILE"
    echo "🔍 查看日誌: tail -f $LOG_FILE"
    echo "🌐 健康檢查: curl http://localhost:8080/actuator/health"
else
    echo "❌ 應用程式啟動失敗"
    echo "查看日誌獲取詳細資訊: tail -n 50 $LOG_FILE"
    rm -f "$PID_FILE"
    exit 1
fi
