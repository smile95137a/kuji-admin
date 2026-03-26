# 🚀 KUJI 後端部署 - CORS 問題修復完成

## ✅ 已完成的修正

### 1. CORS 配置更新
- 檔案: `application-prod.yml`
- 已新增允許的來源:
  - `http://18.179.187.129`
  - `http://18.179.187.129:80`
  - `http://18.179.187.129:3000`
  - `http://18.179.187.129:5173`

### 2. 部署腳本已建立
- ✅ `quick-deploy.bat` - 快速部署（推薦使用）
- ✅ `deploy-to-ec2.bat` - 完整部署流程
- ✅ `start.sh` - EC2 啟動腳本
- ✅ `stop.sh` - EC2 停止腳本
- ✅ `test-cors.bat` - CORS 測試腳本

---

## 🎯 立即部署步驟

### 方式一：使用快速部署腳本（最簡單）

```cmd
cd c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin
quick-deploy.bat
```

這個腳本會自動：
1. 檢查 JAR 檔案（沒有就自動打包）
2. 上傳到 EC2
3. 重啟應用程式
4. 測試健康檢查

---

### 方式二：手動步驟

#### 步驟 1: 打包專案
```cmd
cd c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin
mvn clean package -DskipTests -Pprod
```

#### 步驟 2: 上傳到 EC2
```cmd
set KEY=C:\Users\user\Downloads\kuji-backend.pem
scp -i "%KEY%" target\admin-1.0.0.jar ec2-user@18.179.187.129:/home/ec2-user/kuji-backend/
scp -i "%KEY%" start.sh ec2-user@18.179.187.129:/home/ec2-user/kuji-backend/
scp -i "%KEY%" stop.sh ec2-user@18.179.187.129:/home/ec2-user/kuji-backend/
```

#### 步驟 3: SSH 登入並重啟
```cmd
ssh -i "%KEY%" ec2-user@18.179.187.129
```

然後在 EC2 上執行：
```bash
cd /home/ec2-user/kuji-backend
chmod +x *.sh
./stop.sh
sleep 2
./start.sh
```

---

## 🧪 測試 CORS

部署完成後，執行測試：

```cmd
cd c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin
test-cors.bat
```

或手動測試：

```cmd
curl -H "Origin: http://18.179.187.129" ^
     -H "Access-Control-Request-Method: POST" ^
     -H "Access-Control-Request-Headers: Content-Type" ^
     -X OPTIONS ^
     http://18.179.187.129:8080/api/admin/auth/login -i
```

**預期看到的 Header:**
```
Access-Control-Allow-Origin: http://18.179.187.129
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, PATCH, OPTIONS
Access-Control-Allow-Credentials: true
```

---

## 🔍 檢查應用程式狀態

### 從 Windows 遠端檢查

```cmd
# 健康檢查
curl http://18.179.187.129:8080/actuator/health

# 測試登入 API
curl -X POST http://18.179.187.129:8080/api/admin/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"admin@kuji.com\",\"password\":\"admin123\"}"
```

### SSH 登入 EC2 檢查

```cmd
ssh -i "C:\Users\user\Downloads\kuji-backend.pem" ec2-user@18.179.187.129
```

在 EC2 上執行：
```bash
# 查看進程
ps aux | grep java

# 查看日誌
tail -f /home/ec2-user/kuji-backend/logs/application.log

# 測試本地健康檢查
curl http://localhost:8080/actuator/health
```

---

## 🌐 前端配置

確認前端 API URL 設定為：

```javascript
// 前端環境變數
VITE_API_URL=http://18.179.187.129:8080/api
// 或
REACT_APP_API_URL=http://18.179.187.129:8080/api
```

前端請求範例：
```javascript
axios.post('http://18.179.187.129:8080/api/admin/auth/login', {
  email: 'admin@kuji.com',
  password: 'admin123'
}, {
  withCredentials: true  // 重要：啟用 CORS credentials
})
```

---

## ⚠️ 故障排除

### 問題 1: CORS 錯誤仍然存在

**檢查步驟:**
1. 確認後端已重新部署
2. 檢查瀏覽器 Console 的完整錯誤訊息
3. 確認前端請求的 URL 完全匹配（包含 port）
4. 確認使用 `withCredentials: true`

**解決方案:**
```cmd
# 重新部署
quick-deploy.bat

# 等待 10 秒後測試
test-cors.bat
```

### 問題 2: 連線被拒絕

**檢查 EC2 Security Group:**
- Inbound Rules 必須開放 port 8080
- Source: 0.0.0.0/0 (或特定 IP)

**檢查應用程式:**
```bash
ssh -i "C:\Users\user\Downloads\kuji-backend.pem" ec2-user@18.179.187.129
ps aux | grep java
netstat -tlnp | grep 8080
```

### 問題 3: 應用程式無法啟動

**查看日誌:**
```bash
tail -n 100 /home/ec2-user/kuji-backend/logs/application.log
```

**常見原因:**
- 資料庫連線失敗 → 檢查 RDS Security Group
- Port 被占用 → `./stop.sh` 後再 `./start.sh`
- 記憶體不足 → 調整 `-Xmx` 參數

---

## 📊 部署檢查清單

部署前：
- [ ] 確認 `application-prod.yml` 的 CORS 設定正確
- [ ] 確認 RDS 資料庫可連線
- [ ] 確認程式碼已更新到最新版本

部署中：
- [ ] Maven 打包成功
- [ ] JAR 檔案上傳成功
- [ ] 舊程式已停止
- [ ] 新程式已啟動

部署後：
- [ ] 健康檢查返回 `{"status":"UP"}`
- [ ] CORS preflight 返回正確的 headers
- [ ] 登入 API 可正常使用
- [ ] 前端可正常呼叫 API

---

## 🎉 成功指標

當您看到以下結果，表示部署成功：

1. **健康檢查成功:**
```json
{
  "status": "UP"
}
```

2. **CORS Headers 正確:**
```
Access-Control-Allow-Origin: http://18.179.187.129
Access-Control-Allow-Credentials: true
```

3. **登入 API 正常:**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGc...",
    "userInfo": {...}
  }
}
```

4. **前端可以正常登入並呼叫 API**

---

## 📞 需要協助

如果遇到問題，請提供：
1. 錯誤訊息截圖（瀏覽器 Console）
2. 後端日誌（最後 50 行）
3. 執行的命令和輸出

查看日誌：
```bash
ssh -i "C:\Users\user\Downloads\kuji-backend.pem" ec2-user@18.179.187.129
tail -n 50 /home/ec2-user/kuji-backend/logs/application.log
```

---

**建議立即執行:**
```cmd
cd c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin
quick-deploy.bat
```

等待部署完成後執行：
```cmd
test-cors.bat
```
