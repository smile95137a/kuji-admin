# KUJI 後端部署指南

## 🧩 UAT 自動載入設定（推薦）

目標：
- 啟用 `uat` profile 後，Spring Boot 自動載入 UAT 公開設定
- 敏感資訊固定放在 server 外部檔案，不用每次 deploy 重新輸入環境變數
- 真實金鑰不進 Git

### 1. 專案內設定

專案已新增 [src/main/resources/application-uat.yml](../../src/main/resources/application-uat.yml)：

- 啟用方式：`-Dspring.profiles.active=uat`
- 自動額外載入：`./config/application-uat-secrets.yml`
- 內含 UAT 的 GoMyPay return / notify URL 範例與基本 log 設定

### 2. Server 上固定放一份 secrets 檔

請在 jar 同層建立：`./config/application-uat-secrets.yml`

範例：

```yaml
spring:
  datasource:
    url: jdbc:mysql://your-uat-db-host:3306/kuji?useSSL=true&serverTimezone=Asia/Taipei&characterEncoding=UTF-8
    username: your_uat_db_user
    password: your_uat_db_password
    driver-class-name: com.mysql.cj.jdbc.Driver

  mail:
    username: your_gmail_account@gmail.com
    password: your_gmail_app_password

google:
  client-id: your_google_client_id

jwt:
  secret: your_uat_jwt_secret

payment:
  gateway:
    gomypay:
      shop-id: your_gomypay_shop_id
      hash-key: your_gomypay_hash_key
      hash-iv: your_gomypay_hash_iv
      verify-customer-id: your_gomypay_verify_customer_id
```

### 3. 啟動方式

第一次在 server 設定完成後，後續只要更新 jar 並重啟即可。

```bash
java -Dspring.profiles.active=uat -jar target/admin-1.0.0.jar
```

如果你是用 systemd，可把 profile 固定在 service 檔：

```ini
[Service]
WorkingDirectory=/opt/kuji-admin
ExecStart=/usr/bin/java -Dspring.profiles.active=uat -jar /opt/kuji-admin/admin-1.0.0.jar
Restart=always
```

### 4. 注意事項

- `application-uat.yml` 可進 Git
- `./config/application-uat-secrets.yml` 不可進 Git
- 專案 `.gitignore` 已補上 `config/application-*-secrets.yml`
- GoMyPay 的 `notify-url` 必須是第三方能打到的 UAT 後端網址，不能是 `localhost`

---

## 🚀 快速部署到 EC2

### 方式一：使用快速部署腳本（推薦）

```cmd
quick-deploy.bat
```

這個腳本會：
1. ✅ 檢查並打包 JAR
2. ✅ 上傳到 EC2
3. ✅ 重啟應用程式
4. ✅ 測試健康檢查

---

### 方式二：使用完整部署腳本

```cmd
deploy-to-ec2.bat
```

這個腳本會執行完整的建置流程。

---

## 🔧 手動部署步驟

### 1. 打包專案

```cmd
mvn clean package -DskipTests -Pprod
```

### 2. 上傳到 EC2

```cmd
set KEY_FILE=C:\Users\user\Downloads\kuji-backend.pem
set EC2_HOST=ec2-user@18.179.187.129

scp -i "%KEY_FILE%" target\admin-1.0.0.jar %EC2_HOST%:/home/ec2-user/kuji-backend/
scp -i "%KEY_FILE%" start.sh %EC2_HOST%:/home/ec2-user/kuji-backend/
scp -i "%KEY_FILE%" stop.sh %EC2_HOST%:/home/ec2-user/kuji-backend/
```

### 3. SSH 登入 EC2

```cmd
ssh -i "%KEY_FILE%" %EC2_HOST%
```

### 4. 在 EC2 上執行

```bash
cd /home/ec2-user/kuji-backend
chmod +x *.sh
./stop.sh
./start.sh
```

---

## 🔍 檢查與除錯

### 檢查應用程式狀態

```bash
# 查看進程
ps aux | grep java

# 查看 PID
cat /home/ec2-user/kuji-backend/app.pid

# 測試健康檢查
curl http://localhost:8080/actuator/health
```

### 查看日誌

```bash
# 即時日誌
tail -f /home/ec2-user/kuji-backend/logs/application.log

# 最後 100 行
tail -n 100 /home/ec2-user/kuji-backend/logs/application.log

# 搜尋錯誤
grep -i error /home/ec2-user/kuji-backend/logs/application.log
```

### 測試 CORS

```cmd
curl -H "Origin: http://18.179.187.129" ^
     -H "Access-Control-Request-Method: POST" ^
     -H "Access-Control-Request-Headers: Content-Type" ^
     -X OPTIONS ^
     http://18.179.187.129:8080/api/admin/auth/login -v
```

### 測試登入 API

```cmd
curl -X POST http://18.179.187.129:8080/api/admin/auth/login ^
  -H "Content-Type: application/json" ^
  -H "Origin: http://18.179.187.129" ^
  -d "{\"email\":\"admin@kuji.com\",\"password\":\"admin123\"}"
```

---

## 🌐 環境資訊

- **EC2 IP**: 18.179.187.129
- **後端 Port**: 8080
- **Profile**: prod
- **資料庫**: RDS MySQL (database-1.clsi2geo699r.ap-northeast-1.rds.amazonaws.com)

### API 端點

- 後端 API: http://18.179.187.129:8080/api
- 健康檢查: http://18.179.187.129:8080/actuator/health
- 後台登入: http://18.179.187.129:8080/api/admin/auth/login
- Swagger: http://18.179.187.129:8080/swagger-ui.html

### CORS 允許的來源

- http://localhost:5173
- http://localhost:3000
- http://18.179.187.129
- http://18.179.187.129:3000
- http://18.179.187.129:5173
- http://18.179.187.129:80

---

## ⚠️ 常見問題

### Q1: CORS 錯誤

**症狀**: 前端顯示 CORS policy blocked

**解決方案**:
1. 檢查 `application-prod.yml` 中的 `cors.allowed-origins`
2. 確認前端 URL 完全匹配（包含 port）
3. 重新部署後端

### Q2: 連線被拒絕

**症狀**: Connection refused

**解決方案**:
1. 檢查應用程式是否運行: `ps aux | grep java`
2. 檢查 port 是否監聽: `netstat -tlnp | grep 8080`
3. 檢查 EC2 Security Group 是否開放 8080 port

### Q3: 資料庫連線失敗

**症狀**: Cannot create PoolableConnectionFactory

**解決方案**:
1. 檢查 RDS 是否運行
2. 檢查 Security Group 是否允許 EC2 連線
3. 確認資料庫帳密正確

---

## 📋 快速命令參考

### Windows 本地端

```cmd
# 快速部署
quick-deploy.bat

# 完整部署
deploy-to-ec2.bat

# 只打包
mvn clean package -DskipTests -Pprod

# 登入 EC2
ssh -i "C:\Users\user\Downloads\kuji-backend.pem" ec2-user@18.179.187.129
```

### EC2 伺服器端

```bash
# 進入專案目錄
cd /home/ec2-user/kuji-backend

# 停止應用程式
./stop.sh

# 啟動應用程式
./start.sh

# 查看日誌
tail -f logs/application.log

# 檢查狀態
ps aux | grep java
curl http://localhost:8080/actuator/health
```

---

## 🎯 部署檢查清單

部署前確認：
- [ ] 程式碼已 commit 並 push
- [ ] 測試通過
- [ ] application-prod.yml 配置正確
- [ ] CORS 來源已更新
- [ ] JWT secret 已設定

部署後確認：
- [ ] 應用程式成功啟動（查看日誌）
- [ ] 健康檢查返回 UP
- [ ] 可以連線資料庫
- [ ] CORS 正常運作
- [ ] 登入 API 正常

---

## 📞 需要協助

如有問題，請提供：
1. 錯誤訊息截圖
2. 應用程式日誌（最後 50 行）
3. 部署步驟說明
