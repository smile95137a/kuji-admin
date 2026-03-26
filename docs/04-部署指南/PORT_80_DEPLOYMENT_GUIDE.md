# Port 80 直接部署指南

## 方案概述

讓 Spring Boot 應用程式直接監聽 80 port，無需 Nginx 反向代理。

⚠️ **重要提醒**：
- Port 80 需要 root 權限
- 應用程式將以 root 身份運行（有安全風險）
- 建議僅用於測試環境

---

## 步驟 1：修改 application.yml（本地 Windows）

```yaml
server:
  port: 80  # 改為 80
  servlet:
    context-path: /api
```

---

## 步驟 2：重新編譯

```cmd
cd c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin
mvn clean package -DskipTests
```

---

## 步驟 3：上傳到 EC2

```cmd
scp -i C:\Users\user\OneDrive\Desktop\dream\ourkuji\ourkuji.pem target/admin-1.0.0.jar ec2-user@18.179.187.129:/home/ec2-user/
```

---

## 步驟 4：在 EC2 上以 sudo 啟動（重要！）

SSH 連線到 EC2：
```bash
ssh -i "C:\Users\user\OneDrive\Desktop\dream\ourkuji\ourkuji.pem" ec2-user@18.179.187.129
```

執行部署腳本（需要 sudo）：

```bash
# 設定變數
APP_DIR="/home/ec2-user/kuji-admin"
LOG_DIR="/home/ec2-user/logs"
JAR_NAME="admin-1.0.0.jar"

# 停止舊服務
PID=$(ps aux | grep "$JAR_NAME" | grep -v grep | awk '{print $2}')
if [ -n "$PID" ]; then
    echo "🛑 停止舊服務 (PID: $PID)"
    sudo kill -15 $PID
    sleep 5
fi

# 備份舊版本
if [ -f "$APP_DIR/$JAR_NAME" ]; then
    sudo mv "$APP_DIR/$JAR_NAME" "$APP_DIR/$JAR_NAME.backup.$(date +%Y%m%d_%H%M%S)"
fi

# 複製新版本
sudo cp ~/$JAR_NAME $APP_DIR/

# ⚠️ 使用 sudo 啟動（因為需要 port 80）
cd $APP_DIR
sudo nohup java -jar \
    -Dspring.profiles.active=prod \
    -Duser.timezone=Asia/Taipei \
    -Xms512m \
    -Xmx2048m \
    -XX:+UseG1GC \
    $JAR_NAME > $LOG_DIR/app.log 2>&1 &

echo "🚀 服務已啟動，PID: $!"
sleep 10

# 查看日誌
tail -50 $LOG_DIR/app.log
```

---

## 步驟 5：配置 AWS Security Group

AWS Console → EC2 → Security Groups：

**添加規則：**
- Type: HTTP
- Protocol: TCP
- Port Range: **80**
- Source: 0.0.0.0/0
- Description: KUJI Admin API (Port 80)

**移除規則（如果有）：**
- Port 8080 的規則可以移除（不再需要）

---

## 步驟 6：測試訪問

### 本地測試（EC2 內）
```bash
curl http://localhost/api/actuator/health
```

### 外部測試
```bash
curl http://18.179.187.129/api/actuator/health
```

或瀏覽器訪問：
```
http://18.179.187.129/api/actuator/health
```

**預期結果：**
```json
{
  "status": "UP"
}
```

---

## 驗證服務狀態

```bash
# 查看進程（應該看到以 root 運行）
ps aux | grep admin-1.0.0.jar

# 查看端口（應該看到 *:80）
sudo netstat -tlnp | grep :80

# 查看日誌
tail -f /home/ec2-user/logs/app.log
```

---

## ⚠️ 安全性注意事項

### 風險：
1. **應用程式以 root 運行**：如果應用程式有漏洞，攻擊者可能獲得 root 權限
2. **無法使用 setcap**：Java 不支援 Linux capabilities

### 建議：
- 僅在測試環境使用此方案
- **生產環境強烈建議使用 Nginx 反向代理**（見下方）

---

## 更安全的方案：Nginx 反向代理（推薦）

如果您想要：
- 應用程式以普通用戶運行
- 外部訪問使用標準 80 port
- 未來支援 HTTPS

請改用 Nginx 方案：

### 快速配置（5 分鐘）

1. **安裝 Nginx**：
```bash
sudo yum install -y nginx
```

2. **配置反向代理**：
```bash
sudo tee /etc/nginx/conf.d/kuji.conf > /dev/null <<'EOF'
server {
    listen 80;
    server_name 18.179.187.129;

    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /actuator/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
EOF
```

3. **啟動 Nginx**：
```bash
sudo systemctl start nginx
sudo systemctl enable nginx
```

4. **應用程式保持 8080**：
   - 不需要改 application.yml
   - 不需要 sudo 運行
   - Security Group 只開放 80（不開放 8080）

5. **訪問方式**：
   - 外部：`http://18.179.187.129/api/actuator/health`
   - Nginx 自動轉發到內部 `http://localhost:8080/api/actuator/health`

---

## 方案比較總結

| 方案 | 應用程式端口 | 外部訪問端口 | 需要 root | 安全性 | 複雜度 |
|------|------------|------------|----------|--------|--------|
| 直接 Port 80 | 80 | 80 | ✅ 是 | ⚠️ 低 | 簡單 |
| Port 8080 + SG | 8080 | 8080 | ❌ 否 | ✅ 高 | 簡單 |
| Nginx 反向代理 | 8080 | 80 | ❌ 否 | ✅ 高 | 中等 |

---

## 建議選擇

### 如果是測試環境：
- ✅ **使用 Port 80 直接部署**（本文檔方案）
- 快速、簡單

### 如果是生產環境：
- ✅ **使用 Nginx 反向代理**（見上方）
- 安全、標準、可擴展

---

## 故障排除

### 問題 1：Permission denied (port 80)
```bash
# 原因：沒有使用 sudo
# 解決：確保使用 sudo 啟動
sudo nohup java -jar ...
```

### 問題 2：無法訪問 port 80
```bash
# 檢查防火牆
sudo firewall-cmd --list-all

# 如果開啟了防火牆，需要允許 port 80
sudo firewall-cmd --permanent --add-service=http
sudo firewall-cmd --reload
```

### 問題 3：服務啟動後立即停止
```bash
# 查看日誌
tail -100 /home/ec2-user/logs/app.log

# 常見原因：port 80 已被佔用
sudo netstat -tlnp | grep :80
```

---

## 完成檢查清單

- [ ] 修改 application.yml 改為 port 80
- [ ] 重新編譯專案
- [ ] 上傳 JAR 到 EC2
- [ ] 使用 sudo 啟動服務
- [ ] 配置 Security Group 開放 port 80
- [ ] 測試本地訪問 `curl http://localhost/api/actuator/health`
- [ ] 測試外部訪問 `curl http://18.179.187.129/api/actuator/health`
- [ ] 驗證 API 功能正常
- [ ] （可選）考慮未來遷移到 Nginx 方案

---

## 下一步

部署完成後，可以測試完整 API：

```bash
# 測試登入 API
curl -X POST http://18.179.187.129/api/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@kuji.com",
    "password": "admin123"
  }'
```

🎉 部署完成！
