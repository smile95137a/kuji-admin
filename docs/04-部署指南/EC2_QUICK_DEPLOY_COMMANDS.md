# 🚀 EC2 快速部署命令卡

## 📋 複製以下命令直接在 EC2 上執行

### 步驟 1️⃣：安裝 Java 21

```bash
# 安裝 Amazon Corretto 21
sudo rpm --import https://yum.corretto.aws/corretto.key && \
sudo curl -L -o /etc/yum.repos.d/corretto.repo https://yum.corretto.aws/corretto.repo && \
sudo yum install -y java-21-amazon-corretto-devel && \
java -version
```

**預期輸出**：顯示 `openjdk version "21.0.x"`

---

### 步驟 2️⃣：一鍵部署應用

```bash
# 完整部署命令（複製整段）
APP_DIR="/home/ec2-user/kuji-admin" && \
LOG_DIR="/home/ec2-user/logs" && \
JAR_NAME="admin-1.0.0.jar" && \
echo "🔧 建立目錄..." && \
mkdir -p $APP_DIR $LOG_DIR && \
echo "🛑 停止舊服務..." && \
PID=$(ps aux | grep "$JAR_NAME" | grep -v grep | awk '{print $2}') && \
[ -n "$PID" ] && kill -15 $PID && sleep 5 && \
echo "💾 備份舊版本..." && \
[ -f "$APP_DIR/$JAR_NAME" ] && mv "$APP_DIR/$JAR_NAME" "$APP_DIR/$JAR_NAME.backup.$(date +%Y%m%d_%H%M%S)" && \
echo "📦 部署新版本..." && \
cp $JAR_NAME $APP_DIR/ && \
echo "✅ 部署檔案完成！" && \
cd $APP_DIR && \
echo "🚀 啟動服務..." && \
nohup java -jar \
    -Dspring.profiles.active=prod \
    -Duser.timezone=Asia/Taipei \
    -Xms512m \
    -Xmx2048m \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    $JAR_NAME \
    > $LOG_DIR/app.log 2>&1 & \
NEW_PID=$! && \
echo "📝 服務已啟動，PID: $NEW_PID" && \
echo "⏳ 等待 30 秒檢查服務狀態..." && \
sleep 30 && \
if ps -p $NEW_PID > /dev/null; then \
    echo ""; \
    echo "✅✅✅ 服務運行正常！✅✅✅"; \
    echo ""; \
    echo "📋 最近 50 行日誌："; \
    tail -50 $LOG_DIR/app.log; \
else \
    echo ""; \
    echo "❌❌❌ 服務啟動失敗 ❌❌❌"; \
    echo ""; \
    echo "📋 錯誤日誌："; \
    tail -100 $LOG_DIR/app.log; \
fi
```

---

### 步驟 3️⃣：驗證部署

```bash
# 檢查服務狀態
ps aux | grep admin-1.0.0.jar | grep -v grep

# 檢查端口
netstat -tuln | grep 8080

# 查看日誌
tail -50 /home/ec2-user/logs/app.log
```

---

### 步驟 4️⃣：測試 API（在 EC2 上）

```bash
# Health Check
curl http://localhost:8080/api/actuator/health

# 登入測試
curl -X POST http://localhost:8080/api/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin@kuji.com","password":"admin123"}'
```

---

## 🔍 常用管理命令

### 查看日誌

```bash
# 即時日誌（按 Ctrl+C 退出）
tail -f /home/ec2-user/logs/app.log

# 最近 100 行
tail -100 /home/ec2-user/logs/app.log

# 搜尋錯誤
grep -i "error" /home/ec2-user/logs/app.log
```

### 停止服務

```bash
# 找到 PID
PID=$(ps aux | grep admin-1.0.0.jar | grep -v grep | awk '{print $2}')

# 優雅停止
kill -15 $PID

# 等待 5 秒後強制停止（如果還在運行）
sleep 5 && PID=$(ps aux | grep admin-1.0.0.jar | grep -v grep | awk '{print $2}') && [ -n "$PID" ] && kill -9 $PID
```

### 重啟服務

```bash
# 停止並重啟（複製整段）
PID=$(ps aux | grep admin-1.0.0.jar | grep -v grep | awk '{print $2}') && \
[ -n "$PID" ] && kill -15 $PID && sleep 5 && \
cd /home/ec2-user/kuji-admin && \
nohup java -jar \
    -Dspring.profiles.active=prod \
    -Duser.timezone=Asia/Taipei \
    -Xms512m \
    -Xmx2048m \
    -XX:+UseG1GC \
    admin-1.0.0.jar \
    > /home/ec2-user/logs/app.log 2>&1 & \
echo "服務已重啟，PID: $!"
```

---

## 🆘 遇到問題？

### 問題 1：找不到 Java
```bash
# 檢查 Java 路徑
which java

# 檢查版本
java -version

# 如果找不到，重新安裝
sudo yum install -y java-21-amazon-corretto-devel
```

### 問題 2：端口被佔用
```bash
# 查看 8080 端口
netstat -tuln | grep 8080

# 找出佔用進程
sudo lsof -i :8080

# 殺掉進程
kill -9 <PID>
```

### 問題 3：資料庫連線失敗
```bash
# 測試資料庫連線（需要安裝 mysql 客戶端）
sudo yum install -y mysql

# 測試連線
mysql -h database-1.clsi2geo699r.ap-northeast-1.rds.amazonaws.com \
      -u admin -pWUfan0667. kuji
```

### 問題 4：記憶體不足
```bash
# 檢查記憶體
free -m

# 如果記憶體不足，減少 JVM 配置
# 修改啟動命令：-Xms256m -Xmx1024m
```

---

## 📚 完整文件

| 文件 | 說明 |
|------|------|
| `EC2_JAVA_SETUP_GUIDE.md` | Java 安裝詳細教學 |
| `PRODUCTION_DEPLOYMENT_GUIDE.md` | 完整部署手冊 |
| `DEPLOY_FIX_INSTRUCTIONS.md` | 部署問題修正 |
| `QUICK_START_PRODUCTION.md` | 快速開始指南 |
| `AWS_S3_SETUP_GUIDE.md` | S3 設定教學 |

---

## ✅ 部署成功標誌

看到以下訊息表示成功：

```
✅✅✅ 服務運行正常！✅✅✅

Started AdminApplication in X.XXX seconds
Tomcat started on port(s): 8080
```

---

## 🎯 下一步

部署成功後：

1. 在本機測試 API：`curl http://18.179.187.129:8080/api/actuator/health`
2. 測試登入：使用 Postman 或 curl 測試登入 API
3. 設定 S3：參考 `AWS_S3_SETUP_GUIDE.md`
4. 設定 Systemd：參考 `PRODUCTION_DEPLOYMENT_GUIDE.md`

---

*本機 IP: 18.179.187.129*  
*API Base URL: http://18.179.187.129:8080/api*  
*Admin 帳號: admin@kuji.com / admin123*
