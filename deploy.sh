#!/bin/bash

# ====================================================
# KUJI Admin 後端部署腳本（AWS EC2）
# ====================================================

echo "======================================================"
echo "KUJI Admin Backend Deployment Script"
echo "======================================================"
echo ""

# 環境變數
export APP_NAME="kuji-admin"
export JAR_NAME="admin-1.0.0.jar"
export APP_DIR="/home/ec2-user/kuji-admin"
export LOG_DIR="/home/ec2-user/logs"

# AWS 資訊
export AWS_ACCESS_KEY="${AWS_ACCESS_KEY:-}"
export AWS_SECRET_KEY="${AWS_SECRET_KEY:-}"
export JWT_SECRET="${JWT_SECRET:-KUJI_PRODUCTION_SECRET_KEY_2026_VERY_SECURE_DO_NOT_SHARE}"

# 顏色
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}Step 1: Check environment${NC}"
echo "APP_DIR: $APP_DIR"
echo "LOG_DIR: $LOG_DIR"
echo ""

# 建立目錄
echo -e "${GREEN}Step 2: Create directories${NC}"
mkdir -p $APP_DIR
mkdir -p $LOG_DIR
echo "Directories created!"
echo ""

# 停止舊服務
echo -e "${YELLOW}Step 3: Stop old service${NC}"
PID=$(ps aux | grep "$JAR_NAME" | grep -v grep | awk '{print $2}')
if [ -n "$PID" ]; then
    echo "Found running process: PID $PID"
    kill -15 $PID
    echo "Waiting for process to stop..."
    sleep 5
    
    # 強制終止（如果還在運行）
    PID=$(ps aux | grep "$JAR_NAME" | grep -v grep | awk '{print $2}')
    if [ -n "$PID" ]; then
        echo "Force killing process: PID $PID"
        kill -9 $PID
    fi
    echo "Old service stopped!"
else
    echo "No running service found."
fi
echo ""

# 備份舊版本
echo -e "${GREEN}Step 4: Backup old version${NC}"
if [ -f "$APP_DIR/$JAR_NAME" ]; then
    BACKUP_NAME="$JAR_NAME.backup.$(date +%Y%m%d_%H%M%S)"
    mv "$APP_DIR/$JAR_NAME" "$APP_DIR/$BACKUP_NAME"
    echo "Backup created: $BACKUP_NAME"
else
    echo "No old version to backup."
fi
echo ""

# 複製新版本
echo -e "${GREEN}Step 5: Deploy new version${NC}"
if [ -f "$JAR_NAME" ]; then
    # JAR 在當前目錄
    echo "Found JAR in current directory"
    cp "$JAR_NAME" "$APP_DIR/"
    echo "New version deployed!"
elif [ -f "target/$JAR_NAME" ]; then
    # JAR 在 target 目錄（本地編譯）
    echo "Found JAR in target directory"
    cp "target/$JAR_NAME" "$APP_DIR/"
    echo "New version deployed!"
else
    echo -e "${RED}Error: $JAR_NAME not found!${NC}"
    echo "Please ensure JAR file is in current directory or run 'mvn clean package'"
    exit 1
fi
echo ""

# 啟動服務
echo -e "${GREEN}Step 6: Start service${NC}"
cd $APP_DIR

nohup java -jar \
    -Dspring.profiles.active=prod \
    -Duser.timezone=Asia/Taipei \
    -Xms512m \
    -Xmx2048m \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    $JAR_NAME \
    > $LOG_DIR/app.log 2>&1 &

NEW_PID=$!
echo "Service started with PID: $NEW_PID"
echo ""

# 等待啟動
echo -e "${YELLOW}Step 7: Wait for startup (30s)${NC}"
sleep 30

# 檢查狀態
echo -e "${GREEN}Step 8: Check service status${NC}"
PID=$(ps aux | grep "$JAR_NAME" | grep -v grep | awk '{print $2}')
if [ -n "$PID" ]; then
    echo -e "${GREEN}✅ Service is running! PID: $PID${NC}"
    echo ""
    echo "Check logs:"
    echo "  tail -f $LOG_DIR/app.log"
    echo ""
    echo "API endpoint:"
    echo "  http://18.179.187.129:8080/api"
    echo ""
    echo -e "${GREEN}Deployment completed successfully!${NC}"
else
    echo -e "${RED}❌ Service failed to start!${NC}"
    echo "Check logs for details:"
    echo "  tail -100 $LOG_DIR/app.log"
    exit 1
fi
