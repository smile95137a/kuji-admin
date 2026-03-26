# KUJI Admin 正式環境部署指南

## 環境資訊

### AWS 資源

**EC2 Instance:**
- IP: `18.179.187.129`
- User: `ec2-user`
- Region: `ap-northeast-1` (Tokyo)

**RDS MySQL:**
- Endpoint: `database-1.clsi2geo699r.ap-northeast-1.rds.amazonaws.com`
- Port: `3306`
- Database: `kuji`
- Username: `admin`
- Password: `WUfan0667.`

**S3 Bucket:**
- Name: `test-ourkuji`
- Region: `ap-northeast-1`
- ARN: `arn:aws:s3:::test-ourkuji`

---

## 部署前準備

### 0. 檢查並安裝 Java 21（重要！）

**⚠️ 在 EC2 上必須先安裝 Java 21**

```bash
# 檢查 Java 版本
java -version

# 如果沒有安裝或版本不對，請執行：
sudo rpm --import https://yum.corretto.aws/corretto.key
sudo curl -L -o /etc/yum.repos.d/corretto.repo https://yum.corretto.aws/corretto.repo
sudo yum install -y java-21-amazon-corretto-devel

# 驗證安裝
java -version
```

詳細安裝步驟請參考：[EC2_JAVA_SETUP_GUIDE.md](./EC2_JAVA_SETUP_GUIDE.md)

### 1. 設定 AWS Credentials

在 EC2 上設定環境變數或使用 IAM Role（推薦）：

```bash
# 方法 A：環境變數（臨時）
export AWS_ACCESS_KEY=your_access_key
export AWS_SECRET_KEY=your_secret_key

# 方法 B：AWS CLI 配置（推薦）
aws configure
# 輸入 Access Key ID
# 輸入 Secret Access Key
# Region: ap-northeast-1
# Output format: json
```

### 2. 設定 S3 Bucket 權限

確保 S3 Bucket 允許公開讀取（圖片需要公開存取）：

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "PublicReadGetObject",
      "Effect": "Allow",
      "Principal": "*",
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::test-ourkuji/*"
    }
  ]
}
```

### 3. 設定 IAM Policy（如果使用 IAM Role）

為 EC2 Instance 附加以下 Policy：

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:PutObject",
        "s3:GetObject",
        "s3:DeleteObject",
        "s3:ListBucket"
      ],
      "Resource": [
        "arn:aws:s3:::test-ourkuji",
        "arn:aws:s3:::test-ourkuji/*"
      ]
    }
  ]
}
```

---

## 本地編譯

### 1. 更新依賴

```bash
mvn clean install -DskipTests
```

### 2. 編譯打包

```bash
mvn clean package -DskipTests
```

執行成功後，會在 `target/` 目錄生成 `admin-1.0.0.jar`

---

## 部署到 EC2

### 方法 A：使用部署腳本（推薦）

```bash
# 1. 上傳 JAR 和腳本到 EC2
scp -i C:\Users\user\OneDrive\Desktop\dream\ourkuji\ourkuji.pem  target/admin-1.0.0.jar ec2-user@18.179.187.129:/home/ec2-user/
scp -i C:\Users\user\OneDrive\Desktop\dream\ourkuji\ourkuji.pem deploy.sh ec2-user@18.179.187.129:/home/ec2-user/

# 2. SSH 到 EC2
ssh -i C:\Users\user\OneDrive\Desktop\dream\ourkuji\ourkuji.pem ec2-user@18.179.187.129

# 3. 執行部署腳本
chmod +x deploy.sh
./deploy.sh
```

### 方法 B：手動部署

```bash
# 1. SSH 到 EC2
ssh -i your-key.pem ec2-user@18.179.187.129

# 2. 建立目錄
mkdir -p /home/ec2-user/kuji-admin
mkdir -p /home/ec2-user/logs

# 3. 上傳 JAR（在本機執行）
scp -i your-key.pem target/admin-1.0.0.jar ec2-user@18.179.187.129:/home/ec2-user/kuji-admin/

# 4. 停止舊服務
ps aux | grep admin-1.0.0.jar
kill -15 <PID>

# 5. 啟動新服務
cd /home/ec2-user/kuji-admin
nohup java -jar \
    -Dspring.profiles.active=prod \
    -Duser.timezone=Asia/Taipei \
    -Xms512m \
    -Xmx2048m \
    admin-1.0.0.jar \
    > /home/ec2-user/logs/app.log 2>&1 &

# 6. 檢查日誌
tail -f /home/ec2-user/logs/app.log
```

---

## 驗證部署

### 1. 檢查服務狀態

```bash
# 檢查進程
ps aux | grep admin-1.0.0.jar

# 檢查端口
netstat -tuln | grep 8080

# 檢查日誌
tail -100 /home/ec2-user/logs/app.log
```

### 2. 測試 API

```bash
# Health Check
curl http://18.179.187.129:8080/api/actuator/health

# 登入測試
curl -X POST http://18.179.187.129:8080/api/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin@kuji.com","password":"admin123"}'
```

### 3. 測試 S3 上傳

```bash
# 使用 Postman 或 curl 上傳圖片
curl -X POST http://18.179.187.129:8080/api/admin/news \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@test-image.jpg" \
  -F "title=Test News"
```

---

## 環境變數配置

### 建議的環境變數（可選）

創建 `/home/ec2-user/kuji-admin/.env`：

```bash
# AWS Credentials（如果不使用 IAM Role）
AWS_ACCESS_KEY=your_access_key
AWS_SECRET_KEY=your_secret_key

# JWT Secret（建議使用強密鑰）
JWT_SECRET=your_very_secure_secret_key_here

# Google OAuth（如果需要）
GOOGLE_CLIENT_ID=your_google_client_id
```

修改啟動命令讀取環境變數：

```bash
source /home/ec2-user/kuji-admin/.env
nohup java -jar \
    -Dspring.profiles.active=prod \
    -Daws.s3.access-key=$AWS_ACCESS_KEY \
    -Daws.s3.secret-key=$AWS_SECRET_KEY \
    -Djwt.secret=$JWT_SECRET \
    admin-1.0.0.jar \
    > /home/ec2-user/logs/app.log 2>&1 &
```

---

## 設定開機自動啟動（Systemd）

### 1. 創建 Systemd Service

```bash
sudo nano /etc/systemd/system/kuji-admin.service
```

內容：

```ini
[Unit]
Description=KUJI Admin Backend Service
After=network.target

[Service]
Type=simple
User=ec2-user
WorkingDirectory=/home/ec2-user/kuji-admin
ExecStart=/usr/bin/java -jar \
    -Dspring.profiles.active=prod \
    -Duser.timezone=Asia/Taipei \
    -Xms512m \
    -Xmx2048m \
    /home/ec2-user/kuji-admin/admin-1.0.0.jar
SuccessExitStatus=143
Restart=on-failure
RestartSec=10

StandardOutput=append:/home/ec2-user/logs/app.log
StandardError=append:/home/ec2-user/logs/app.log

[Install]
WantedBy=multi-user.target
```

### 2. 啟用服務

```bash
sudo systemctl daemon-reload
sudo systemctl enable kuji-admin
sudo systemctl start kuji-admin

# 檢查狀態
sudo systemctl status kuji-admin
```

### 3. 管理服務

```bash
# 啟動
sudo systemctl start kuji-admin

# 停止
sudo systemctl stop kuji-admin

# 重啟
sudo systemctl restart kuji-admin

# 查看日誌
sudo journalctl -u kuji-admin -f
```

---

## 監控與維護

### 1. 日誌管理

```bash
# 查看即時日誌
tail -f /home/ec2-user/logs/app.log

# 查看最近 100 行
tail -100 /home/ec2-user/logs/app.log

# 搜尋錯誤
grep -i "error" /home/ec2-user/logs/app.log

# 日誌輪替（避免檔案過大）
sudo nano /etc/logrotate.d/kuji-admin
```

`/etc/logrotate.d/kuji-admin` 內容：

```
/home/ec2-user/logs/app.log {
    daily
    rotate 7
    compress
    delaycompress
    missingok
    notifempty
    create 644 ec2-user ec2-user
}
```

### 2. 資料庫備份

```bash
# 每日備份（Crontab）
crontab -e

# 添加：每天凌晨 2 點備份
0 2 * * * mysqldump -h database-1.clsi2geo699r.ap-northeast-1.rds.amazonaws.com -u admin -pWUfan0667. kuji > /home/ec2-user/backups/kuji_$(date +\%Y\%m\%d).sql
```

### 3. S3 費用監控

在 AWS Console 設定 Billing Alerts：
- 監控 S3 儲存空間
- 監控 S3 請求次數
- 設定預算警報

---

## 故障排除

### 問題 1：服務無法啟動

```bash
# 檢查 Java 是否安裝
java -version

# 檢查端口是否被佔用
netstat -tuln | grep 8080

# 檢查磁碟空間
df -h

# 檢查記憶體
free -m
```

### 問題 2：S3 上傳失敗

```bash
# 檢查 AWS Credentials
aws s3 ls s3://test-ourkuji

# 檢查 IAM 權限
aws sts get-caller-identity

# 測試 S3 上傳
aws s3 cp test.txt s3://test-ourkuji/test.txt
```

### 問題 3：資料庫連線失敗

```bash
# 測試資料庫連線
mysql -h database-1.clsi2geo699r.ap-northeast-1.rds.amazonaws.com -u admin -pWUfan0667. kuji

# 檢查安全群組
# 確保 RDS 安全群組允許 EC2 的 IP
```

---

## 效能優化建議

### 1. JVM 參數調整

```bash
-Xms1024m \
-Xmx2048m \
-XX:+UseG1GC \
-XX:MaxGCPauseMillis=200 \
-XX:+HeapDumpOnOutOfMemoryError \
-XX:HeapDumpPath=/home/ec2-user/logs/heap-dump.hprof
```

### 2. Nginx 反向代理（可選）

安裝 Nginx：

```bash
sudo yum install nginx -y
sudo systemctl start nginx
sudo systemctl enable nginx
```

配置 `/etc/nginx/conf.d/kuji-admin.conf`：

```nginx
server {
    listen 80;
    server_name 18.179.187.129;

    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

重啟 Nginx：

```bash
sudo systemctl restart nginx
```

### 3. CloudFront CDN（可選）

為 S3 設定 CloudFront 加速圖片載入：
- Origin: test-ourkuji.s3.ap-northeast-1.amazonaws.com
- Viewer Protocol Policy: Redirect HTTP to HTTPS
- Compress Objects: Yes

---

## 安全檢查清單

- [ ] RDS 僅允許 EC2 安全群組存取
- [ ] S3 Bucket 僅公開讀取，不允許公開寫入
- [ ] EC2 安全群組僅開放必要端口（80, 443, 8080）
- [ ] JWT Secret 使用強密鑰
- [ ] 資料庫密碼定期更新
- [ ] AWS Credentials 使用 IAM Role 而非 Access Key
- [ ] 啟用 CloudWatch 監控
- [ ] 設定自動備份

---

## 聯絡資訊

部署問題請參考：
- 本文檔：`PRODUCTION_DEPLOYMENT_GUIDE.md`
- 應用日誌：`/home/ec2-user/logs/app.log`
- AWS Console: https://console.aws.amazon.com

祝部署順利！ 🚀
