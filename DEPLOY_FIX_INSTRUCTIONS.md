# 🚀 部署修正指南

## 問題診斷

您已經在 EC2 伺服器上了！JAR 檔案 `admin-1.0.0.jar` 也已經在 `/home/ec2-user/` 目錄中。

問題是 `deploy.sh` 腳本原本只檢查 `target/admin-1.0.0.jar`，但實際上 JAR 檔案在當前目錄。

## ✅ 解決方案（在 EC2 上執行）

### 方法 1：更新 deploy.sh 腳本（推薦）

在 EC2 伺服器上執行以下命令來更新 `deploy.sh`：

```bash
# 備份舊腳本
cp deploy.sh deploy.sh.backup

# 編輯腳本
nano deploy.sh
```

找到這一段（大約在第 76-85 行）：

```bash
# 複製新版本
echo -e "${GREEN}Step 5: Deploy new version${NC}"
if [ -f "target/$JAR_NAME" ]; then
    cp "target/$JAR_NAME" "$APP_DIR/"
    echo "New version deployed!"
else
    echo -e "${RED}Error: target/$JAR_NAME not found!${NC}"
    echo "Please run 'mvn clean package' first."
    exit 1
fi
```

替換為：

```bash
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
```

然後：
- 按 `Ctrl+O` 儲存
- 按 `Enter` 確認
- 按 `Ctrl+X` 退出

執行部署：

```bash
chmod +x deploy.sh
./deploy.sh
```

---

### 方法 2：快速部署（不修改腳本）

如果您想直接部署，執行以下命令：

```bash
# 設定變數
APP_DIR="/home/ec2-user/kuji-admin"
LOG_DIR="/home/ec2-user/logs"
JAR_NAME="admin-1.0.0.jar"

# 建立目錄
mkdir -p $APP_DIR
mkdir -p $LOG_DIR

# 停止舊服務
PID=$(ps aux | grep "$JAR_NAME" | grep -v grep | awk '{print $2}')
if [ -n "$PID" ]; then
    echo "停止舊服務 PID: $PID"
    kill -15 $PID
    sleep 5
    # 確認是否終止
    PID=$(ps aux | grep "$JAR_NAME" | grep -v grep | awk '{print $2}')
    if [ -n "$PID" ]; then
        kill -9 $PID
    fi
fi

# 備份舊版本
if [ -f "$APP_DIR/$JAR_NAME" ]; then
    BACKUP_NAME="$JAR_NAME.backup.$(date +%Y%m%d_%H%M%S)"
    mv "$APP_DIR/$JAR_NAME" "$APP_DIR/$BACKUP_NAME"
    echo "已備份: $BACKUP_NAME"
fi

# 部署新版本
cp $JAR_NAME $APP_DIR/
echo "部署完成！"

# 啟動服務
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
echo "服務已啟動，PID: $NEW_PID"

# 等待 30 秒檢查狀態
echo "等待 30 秒檢查服務狀態..."
sleep 30

# 檢查服務
if ps -p $NEW_PID > /dev/null; then
    echo "✅ 服務運行正常！"
    tail -50 $LOG_DIR/app.log
else
    echo "❌ 服務啟動失敗，請檢查日誌："
    tail -100 $LOG_DIR/app.log
fi
```

---

## 📋 驗證部署

部署完成後，執行以下檢查：

### 1. 檢查服務狀態

```bash
# 檢查進程
ps aux | grep admin-1.0.0.jar

# 檢查端口
netstat -tuln | grep 8080

# 查看日誌
tail -50 /home/ec2-user/logs/app.log
```

### 2. 測試 API

```bash
# Health Check
curl http://localhost:8080/api/actuator/health

# 或從外部
curl http://18.179.187.129:8080/api/actuator/health

# 登入測試
curl -X POST http://localhost:8080/api/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin@kuji.com","password":"admin123"}'
```

---

## 🔧 常見問題

### 問題 1：端口已被佔用

```bash
# 找出佔用 8080 的進程
sudo lsof -i :8080

# 或
netstat -tuln | grep 8080
```

### 問題 2：記憶體不足

```bash
# 檢查記憶體
free -m

# 調整 JVM 參數（減少記憶體使用）
# 修改啟動命令中的 -Xms256m -Xmx1024m
```

### 問題 3：資料庫連線失敗

```bash
# 測試資料庫連線
mysql -h database-1.clsi2geo699r.ap-northeast-1.rds.amazonaws.com \
      -u admin -p kuji
# 密碼: WUfan0667.

# 檢查安全群組設定
```

---

## 📚 相關文件

- **完整部署指南**: `PRODUCTION_DEPLOYMENT_GUIDE.md`
- **快速開始**: `QUICK_START_PRODUCTION.md`
- **S3 設定**: `AWS_S3_SETUP_GUIDE.md`

---

## 🎯 下一步

部署成功後：

1. ✅ 測試登入 API
2. ✅ 測試 S3 圖片上傳
3. ✅ 檢查資料庫連線
4. ✅ 設定 Systemd 服務（開機自動啟動）
5. ✅ 設定日誌輪替
6. ✅ 配置備份策略

---

*最後更新：2026-01-14*
