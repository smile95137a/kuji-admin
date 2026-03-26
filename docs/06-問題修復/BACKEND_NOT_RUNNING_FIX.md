# 後端應用未啟動問題 - 快速修復指南

## 問題診斷結果

### 當前狀態：
- ✅ Nginx 正常運行（Port 80）
- ✅ Nginx 配置正確
- ❌ **後端應用未運行**（Port 8080）
- ❌ 所有 API 請求返回 502 Bad Gateway

---

## 🚀 立即修復步驟

### 步驟 1：檢查 JAR 文件是否存在

```bash
ls -lh /home/ec2-user/kuji-admin/admin-1.0.0.jar
ls -lh /home/ec2-user/admin-1.0.0.jar
```

---

### 步驟 2：啟動後端應用

#### 方式 A：使用完整部署腳本（推薦）

```bash
# 設定變數
APP_DIR="/home/ec2-user/kuji-admin"
LOG_DIR="/home/ec2-user/logs"
JAR_NAME="admin-1.0.0.jar"

# 確保目錄存在
mkdir -p $APP_DIR
mkdir -p $LOG_DIR

# 檢查 JAR 位置
if [ -f "$APP_DIR/$JAR_NAME" ]; then
    echo "✅ JAR 文件在 $APP_DIR"
elif [ -f "/home/ec2-user/$JAR_NAME" ]; then
    echo "✅ JAR 文件在 /home/ec2-user，正在複製..."
    cp /home/ec2-user/$JAR_NAME $APP_DIR/
else
    echo "❌ 找不到 JAR 文件！"
    exit 1
fi

# 啟動應用
cd $APP_DIR
nohup java -jar \
    -Dspring.profiles.active=prod \
    -Duser.timezone=Asia/Taipei \
    -Xms512m \
    -Xmx2048m \
    -XX:+UseG1GC \
    $JAR_NAME > $LOG_DIR/app.log 2>&1 &

echo "🚀 服務已啟動，PID: $!"
sleep 10

# 查看日誌
echo -e "\n=== 最新日誌 ==="
tail -50 $LOG_DIR/app.log
```

#### 方式 B：快速啟動（如果確定 JAR 位置）

```bash
cd /home/ec2-user/kuji-admin
nohup java -jar \
    -Dspring.profiles.active=prod \
    -Duser.timezone=Asia/Taipei \
    -Xms512m -Xmx2048m -XX:+UseG1GC \
    admin-1.0.0.jar > /home/ec2-user/logs/app.log 2>&1 &

echo "PID: $!"
```

---

### 步驟 3：驗證啟動成功

等待 10-15 秒後執行：

```bash
# 檢查進程
echo "=== Java 進程 ===" && ps aux | grep admin-1.0.0.jar | grep -v grep

# 檢查端口
echo -e "\n=== 端口監聽 ===" && sudo netstat -tlnp | grep 8080

# 檢查日誌
echo -e "\n=== 應用日誌 ===" && tail -30 /home/ec2-user/logs/app.log

# 測試 API
echo -e "\n=== 健康檢查 ===" && curl -s http://localhost:8080/api/actuator/health
```

**預期結果：**
```bash
# Java 進程
ec2-user  12345  ... java -jar ... admin-1.0.0.jar

# 端口監聽
tcp6  0  0 :::8080  :::*  LISTEN  12345/java

# 健康檢查
{"status":"UP"}
```

---

### 步驟 4：通過 Nginx 測試

```bash
# 本地測試
curl -s http://localhost/api/actuator/health

# 查看 Nginx 錯誤日誌
sudo tail -50 /var/log/nginx/error.log
```

---

## 🔍 故障排除

### 問題 A：JAR 文件不存在

**檢查：**
```bash
find /home/ec2-user -name "admin-1.0.0.jar" -type f 2>/dev/null
```

**如果找不到：**
需要從本地 Windows 重新上傳：

```cmd
# 在本地 Windows CMD 執行
cd c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin

# 先確認 JAR 存在
dir target\admin-1.0.0.jar

# 上傳到 EC2
scp -i C:\Users\user\OneDrive\Desktop\dream\ourkuji\ourkuji.pem target\admin-1.0.0.jar ec2-user@18.179.187.129:/home/ec2-user/kuji-admin/
```

---

### 問題 B：啟動失敗（查看日誌）

```bash
tail -100 /home/ec2-user/logs/app.log
```

**常見錯誤：**

1. **資料庫連線失敗：**
```
Could not open JDBC Connection
```
**解決：** 檢查 RDS 端點和安全組

2. **Port 8080 被佔用：**
```
Address already in use
```
**解決：**
```bash
sudo netstat -tlnp | grep 8080
kill -9 <PID>
```

3. **記憶體不足：**
```
OutOfMemoryError
```
**解決：** 降低 `-Xmx` 參數：
```bash
java -jar -Xms256m -Xmx1024m ...
```

---

### 問題 C：啟動後立即停止

```bash
# 查看完整日誌
cat /home/ec2-user/logs/app.log

# 檢查 Java 版本
java -version
```

確保 Java 版本是 21：
```
openjdk version "21.0.x"
```

---

## 📋 完整驗證清單

啟動後依次檢查：

```bash
# 1. 進程存在
ps aux | grep admin-1.0.0.jar | grep -v grep
# ✅ 應該看到一行 Java 進程

# 2. 端口監聽
sudo netstat -tlnp | grep 8080
# ✅ 應該看到 :::8080 LISTEN

# 3. 本地健康檢查（直接）
curl -s http://localhost:8080/api/actuator/health
# ✅ 應該返回 {"status":"UP"}

# 4. 通過 Nginx 健康檢查
curl -s http://localhost/api/actuator/health
# ✅ 應該返回 {"status":"UP"}

# 5. 外部訪問（在本地 Windows CMD）
curl http://18.179.187.129/api/actuator/health
# ✅ 應該返回 {"status":"UP"}
```

---

## 🎯 為什麼會停止？

可能原因：

1. **手動停止：** 之前執行了 `kill` 命令
2. **系統重啟：** EC2 實例重啟後應用未自動啟動
3. **應用崩潰：** 記憶體不足或異常導致退出
4. **部署替換：** 上傳新 JAR 時停止了舊進程但未啟動新的

---

## 💡 防止未來停止 - 設置自動啟動

### 方式 A：使用 Systemd（推薦）

建立服務文件：
```bash
sudo tee /etc/systemd/system/kuji-admin.service > /dev/null <<'EOF'
[Unit]
Description=KUJI Admin API Service
After=network.target

[Service]
Type=simple
User=ec2-user
WorkingDirectory=/home/ec2-user/kuji-admin
ExecStart=/usr/bin/java -jar \
    -Dspring.profiles.active=prod \
    -Duser.timezone=Asia/Taipei \
    -Xms512m -Xmx2048m -XX:+UseG1GC \
    /home/ec2-user/kuji-admin/admin-1.0.0.jar
Restart=on-failure
RestartSec=10
StandardOutput=append:/home/ec2-user/logs/app.log
StandardError=append:/home/ec2-user/logs/app.log

[Install]
WantedBy=multi-user.target
EOF

# 重新加載 systemd
sudo systemctl daemon-reload

# 啟動服務
sudo systemctl start kuji-admin

# 設置開機自動啟動
sudo systemctl enable kuji-admin

# 查看狀態
sudo systemctl status kuji-admin
```

**管理命令：**
```bash
sudo systemctl start kuji-admin      # 啟動
sudo systemctl stop kuji-admin       # 停止
sudo systemctl restart kuji-admin    # 重啟
sudo systemctl status kuji-admin     # 狀態
sudo journalctl -u kuji-admin -f     # 查看日誌
```

---

## 🔄 完整重新部署流程（參考）

如果需要完整重新部署：

### 在本地 Windows：

```cmd
cd c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin

REM 重新編譯（如果有修改）
mvn clean package -DskipTests

REM 上傳到 EC2
scp -i C:\Users\user\OneDrive\Desktop\dream\ourkuji\ourkuji.pem target\admin-1.0.0.jar ec2-user@18.179.187.129:/home/ec2-user/
```

### 在 EC2：

```bash
# 停止舊服務（如果有）
PID=$(ps aux | grep admin-1.0.0.jar | grep -v grep | awk '{print $2}')
[ -n "$PID" ] && kill -15 $PID && sleep 5

# 備份並部署新版本
cd /home/ec2-user/kuji-admin
[ -f "admin-1.0.0.jar" ] && mv admin-1.0.0.jar admin-1.0.0.jar.backup.$(date +%Y%m%d_%H%M%S)
cp ~/admin-1.0.0.jar .

# 啟動
nohup java -jar \
    -Dspring.profiles.active=prod \
    -Duser.timezone=Asia/Taipei \
    -Xms512m -Xmx2048m -XX:+UseG1GC \
    admin-1.0.0.jar > /home/ec2-user/logs/app.log 2>&1 &

echo "PID: $!"
```

---

## ✅ 成功檢查清單

完成後確認：

- [ ] Java 進程運行中
- [ ] Port 8080 監聽中
- [ ] 直接訪問 8080 健康檢查成功
- [ ] 通過 Nginx 訪問健康檢查成功
- [ ] 外部訪問健康檢查成功（需開放 Security Group）
- [ ] 日誌無錯誤
- [ ] 考慮設置 Systemd 自動啟動

---

## 📞 需要協助？

如果啟動失敗，請提供：

1. **JAR 文件檢查結果：**
   ```bash
   ls -lh /home/ec2-user/kuji-admin/admin-1.0.0.jar
   ```

2. **啟動日誌：**
   ```bash
   tail -100 /home/ec2-user/logs/app.log
   ```

3. **錯誤訊息：**
   任何紅色錯誤訊息或異常堆疊

立即執行步驟 2 的啟動命令，然後運行步驟 3 的驗證命令！🚀
