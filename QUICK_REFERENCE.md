# 🚀 KUJI 快速命令參考

## 📦 部署命令

### 快速部署（推薦）
```cmd
cd c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin
deploy-and-test.bat
```

### 只上傳不測試
```cmd
quick-deploy.bat
```

---

## 🔑 SSH 連線

### 登入 EC2
```cmd
ssh -i C:\Users\user\OneDrive\Desktop\dream\ourkuji\ourkuji.pem ec2-user@18.179.187.129
```

### 在 EC2 上的常用命令
```bash
# 進入專案目錄
cd /home/ec2-user/kuji-backend

# 查看日誌（即時）
tail -f logs/application.log

# 查看最後 100 行
tail -n 100 logs/application.log

# 搜尋 CORS 相關日誌
grep -i cors logs/application.log | tail -20

# 搜尋錯誤
grep -i error logs/application.log | tail -50

# 檢查應用程式狀態
ps aux | grep java

# 停止應用程式
./stop.sh

# 啟動應用程式
./start.sh

# 重啟
./stop.sh && sleep 2 && ./start.sh

# 測試本地健康檢查
curl http://localhost:8080/actuator/health
```

---

## 🧪 測試命令

### 測試健康檢查
```cmd
curl http://18.179.187.129:8080/actuator/health
```

### 測試 CORS
```cmd
curl -H "Origin: http://18.179.187.129" ^
     -H "Access-Control-Request-Method: POST" ^
     -H "Access-Control-Request-Headers: Content-Type" ^
     -X OPTIONS ^
     http://18.179.187.129:8080/api/admin/auth/login -I
```

### 測試登入 API
```cmd
curl -X POST http://18.179.187.129:8080/api/admin/auth/login ^
  -H "Content-Type: application/json" ^
  -H "Origin: http://18.179.187.129" ^
  -d "{\"email\":\"admin@kuji.com\",\"password\":\"admin123\"}"
```

---

## 🌐 重要 URL

| 用途 | URL |
|------|-----|
| 前端登入 | http://18.179.187.129/kuji/login |
| 後端 API | http://18.179.187.129:8080/api |
| 健康檢查 | http://18.179.187.129:8080/actuator/health |
| Swagger (如有) | http://18.179.187.129:8080/swagger-ui.html |

---

## 🔧 本地開發

### 啟動本地後端
```cmd
cd c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin
mvn spring-boot:run
```

### 打包
```cmd
mvn clean package -DskipTests
```

### 執行測試
```cmd
mvn test
```

---

## 📝 日誌查看技巧

### 即時監控（最推薦）
```bash
ssh -i C:\Users\user\OneDrive\Desktop\dream\ourkuji\ourkuji.pem ec2-user@18.179.187.129 "tail -f /home/ec2-user/kuji-backend/logs/application.log"
```

### 查看啟動日誌
```bash
ssh -i C:\Users\user\OneDrive\Desktop\dream\ourkuji\ourkuji.pem ec2-user@18.179.187.129 "head -100 /home/ec2-user/kuji-backend/logs/application.log"
```

### 查看最新錯誤
```bash
ssh -i C:\Users\user\OneDrive\Desktop\dream\ourkuji\ourkuji.pem ec2-user@18.179.187.129 "grep -i error /home/ec2-user/kuji-backend/logs/application.log | tail -20"
```

---

## 🐛 常見問題快速修復

### CORS 錯誤
1. 檢查 `application-prod.yml` 的 `cors.allowed-origins`
2. 重新部署：`deploy-and-test.bat`
3. 查看日誌確認 CORS 配置已載入

### 應用程式無法啟動
1. SSH 登入 EC2
2. 查看日誌：`tail -100 logs/application.log`
3. 檢查 port 占用：`netstat -tlnp | grep 8080`
4. 強制停止：`kill -9 $(cat app.pid)`

### 資料庫連線失敗
1. 檢查 RDS 是否運行
2. 檢查 Security Group
3. 查看日誌中的連線錯誤訊息

---

## 📚 文件快速索引

| 問題 | 查看文件 |
|------|----------|
| 所有文件總覽 | `README_DOCS.md` |
| CORS 問題 | `CORS_FIX_AND_DEPLOY.md` |
| 完整部署指南 | `DEPLOY_GUIDE.md` |
| API 測試 | `API_TEST_GUIDE.md` |
| 商品獎品池 | `LOTTERY_PRIZE_POOL_IMPLEMENTATION.md` |
| 角色測試 | `ROLE_PERMISSION_TEST_GUIDE.md` |
| 目前狀態 | `CURRENT_STATUS_AND_NEXT_STEPS.md` |

---

## 🎯 今天要做的事

### 1. 修復 CORS（進行中）
```cmd
deploy-and-test.bat  # 已執行
```

### 2. 測試前端登入
- 開啟：http://18.179.187.129/kuji/login
- 登入：admin@kuji.com / admin123

### 3. 實作商品獎品池（下一步）
- 參考：`LOTTERY_PRIZE_POOL_IMPLEMENTATION.md`
- 建立資料表
- 實作功能

---

**最常用的三個命令：**
1. `deploy-and-test.bat` - 部署
2. `ssh -i C:\Users\user\OneDrive\Desktop\dream\ourkuji\ourkuji.pem ec2-user@18.179.187.129` - 登入
3. `tail -f /home/ec2-user/kuji-backend/logs/application.log` - 查看日誌
