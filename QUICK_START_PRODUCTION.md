# 🚀 正式環境快速部署指南

## 完成進度：✅ 所有代碼變更已完成！

---

## 📋 部署前檢查（2 分鐘）

```bash
# 在專案目錄執行
check-prod-config.bat
```

**預期結果**：所有項目顯示 `[OK]`

---

## 🎯 部署流程（3 步驟，約 10 分鐘）

### Step 1: 本地編譯 JAR（5 分鐘）

```bash
cd c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin

# 清理並編譯
mvn clean package -DskipTests
```

**檢查**：確認 `target/admin-1.0.0.jar` 存在

---

### Step 2: 上傳到 EC2（2 分鐘）

```bash
# 上傳 JAR 檔
scp -i your-key.pem target/admin-1.0.0.jar ec2-user@18.179.187.129:/home/ec2-user/

# 上傳部署腳本
scp -i your-key.pem deploy.sh ec2-user@18.179.187.129:/home/ec2-user/
```

**提示**：請準備好 EC2 的 SSH Key

---

### Step 3: 執行部署（3 分鐘）

```bash
# SSH 到 EC2
ssh -i your-key.pem ec2-user@18.179.187.129

# 賦予執行權限
chmod +x deploy.sh

# 執行部署
./deploy.sh
```

**部署腳本會自動：**
- ✅ 停止舊服務
- ✅ 備份舊版本
- ✅ 部署新版本
- ✅ 啟動服務
- ✅ 檢查狀態

---

## 🧪 驗證部署（2 分鐘）

### 1. 檢查服務狀態

```bash
# 檢查進程
ps aux | grep admin-1.0.0.jar

# 查看日誌
tail -f /home/ec2-user/logs/app.log
```

**預期**：看到 `Started AdminApplication in X.XXX seconds`

### 2. 測試登入 API

```bash
curl -X POST http://18.179.187.129:8080/api/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin@kuji.com","password":"admin123"}'
```

**預期**：返回 JWT token

### 3. 測試 S3 上傳

```bash
# 取得 Token
TOKEN="<從上一步取得的 token>"

# 上傳測試圖片
curl -X POST http://18.179.187.129:8080/api/admin/news \
  -H "Authorization: Bearer $TOKEN" \
  -F "title=S3 Test" \
  -F "content=Test" \
  -F "imageFile=@test.jpg"
```

**預期**：回應中的 `imageUrl` 包含 S3 網址

---

## ⚙️ AWS S3 設定（首次部署需要）

### 必要步驟：

1. **設定 S3 Bucket Policy**（允許公開讀取）
   - 參考：`AWS_S3_SETUP_GUIDE.md` → 步驟 1

2. **設定 EC2 IAM Role**（S3 存取權限）
   - 參考：`AWS_S3_SETUP_GUIDE.md` → 步驟 4 → 選項 A

3. **測試 S3 存取**
   ```bash
   aws s3 ls s3://test-ourkuji/
   ```

**詳細指南**：查看 `AWS_S3_SETUP_GUIDE.md`（10 頁完整教學）

---

## 📚 文檔資源

| 文檔 | 用途 | 頁數 |
|------|------|-----|
| `PRODUCTION_MIGRATION_COMPLETE.md` | ✅ 變更摘要報告 | 6 頁 |
| `PRODUCTION_CONFIG_SUMMARY.md` | 📋 配置快速參考 | 4 頁 |
| `PRODUCTION_DEPLOYMENT_GUIDE.md` | 📖 完整部署手冊 | 15 頁 |
| `AWS_S3_SETUP_GUIDE.md` | 🔧 S3 設定教學 | 10 頁 |
| `deploy.sh` | 🤖 自動化部署腳本 | - |
| `check-prod-config.bat` | ✔️ 配置檢查工具 | - |

---

## 🆘 遇到問題？

### 問題 1：編譯失敗

```bash
# 清除快取
mvn clean

# 重新下載依賴
mvn dependency:purge-local-repository

# 重新編譯
mvn clean package -DskipTests
```

### 問題 2：無法連接 EC2

```bash
# 檢查 SSH Key 權限
chmod 400 your-key.pem

# 檢查 EC2 安全群組
# 確保允許 SSH (port 22) 從你的 IP
```

### 問題 3：服務無法啟動

```bash
# 查看完整日誌
tail -100 /home/ec2-user/logs/app.log

# 檢查端口
netstat -tuln | grep 8080

# 檢查 Java 版本
java -version  # 需要 Java 21
```

### 問題 4：S3 上傳失敗

```bash
# 測試 S3 存取
aws s3 ls s3://test-ourkuji/

# 如果失敗，檢查 IAM Role
aws sts get-caller-identity

# 查看詳細錯誤
grep -i "s3" /home/ec2-user/logs/app.log
```

**詳細故障排除**：查看 `PRODUCTION_DEPLOYMENT_GUIDE.md` → 故障排除章節

---

## 📞 快速聯絡資訊

**環境資訊：**
- EC2 IP: `18.179.187.129`
- RDS: `database-1.clsi2geo699r.ap-northeast-1.rds.amazonaws.com`
- S3: `test-ourkuji`
- Region: `ap-northeast-1`

**帳號密碼：**
- DB 使用者: `admin` / `WUfan0667.`
- 管理員: `admin@kuji.com` / `admin123`

---

## ✅ 完成檢查清單

部署完成後，確認：

- [ ] 服務正常運行（`ps aux | grep admin`）
- [ ] 登入 API 正常（返回 token）
- [ ] S3 上傳功能正常（圖片 URL 可存取）
- [ ] 資料庫連線正常（查看日誌無錯誤）
- [ ] 日誌正常輸出（`tail -f /home/ec2-user/logs/app.log`）

---

## 🎉 部署成功！

API 端點：`http://18.179.187.129:8080/api`

**測試登入**：
```bash
curl http://18.179.187.129:8080/api/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin@kuji.com","password":"admin123"}'
```

**查看 Swagger 文檔**：
```
http://18.179.187.129:8080/api/swagger-ui/index.html
```

---

**祝你部署順利！有問題隨時查看文檔！** 🚀
