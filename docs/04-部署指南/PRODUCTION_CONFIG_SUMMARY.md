# KUJI Admin 正式環境配置摘要

## ✅ 已完成的配置變更

### 1. 環境配置

**`application.yml`**
- ✅ 預設 Profile 改為 `prod`

**`application-prod.yml`**
- ✅ 資料庫：`database-1.clsi2geo699r.ap-northeast-1.rds.amazonaws.com`
- ✅ 使用者：`admin` / 密碼：`WUfan0667.`
- ✅ 資料庫名稱：`kuji`
- ✅ AWS S3 Bucket：`test-ourkuji`
- ✅ S3 Region：`ap-northeast-1`
- ✅ S3 Base URL：`https://test-ourkuji.s3.ap-northeast-1.amazonaws.com`

### 2. 依賴更新

**`pom.xml`**
- ✅ 新增 AWS SDK for S3 (`software.amazon.awssdk:s3`)
- ✅ 新增 AWS SDK Auth (`software.amazon.awssdk:auth`)

### 3. 程式碼變更

**新增檔案：**
- ✅ `config/S3Config.java` - AWS S3 配置類別
- ✅ `service/impl/S3ServiceImpl.java` - 真實 S3 上傳實作（prod profile）
- ✅ `deploy.sh` - EC2 部署腳本
- ✅ `PRODUCTION_DEPLOYMENT_GUIDE.md` - 完整部署指南

**修改檔案：**
- ✅ `service/impl/LocalFileServiceImpl.java` - 加上 `@Profile("dev")`，僅在開發環境啟用

---

## 🚀 部署步驟

### Step 1: 本地編譯

```bash
cd c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin

# 清理並編譯
mvn clean package -DskipTests -Pprod
```

預期結果：`target/admin-1.0.0.jar`

### Step 2: 上傳到 EC2

```bash
# 上傳 JAR 檔案
scp -i C:\Users\user\OneDrive\Desktop\dream\ourkuji\ourkuji.pem target/admin-1.0.0.jar ec2-user@18.179.187.129:/home/ec2-user/

# 上傳部署腳本
scp -i C:\Users\user\OneDrive\Desktop\dream\ourkuji\ourkuji.pem deploy.sh ec2-user@18.179.187.129:/home/ec2-user/
```

### Step 3: SSH 到 EC2

```bash
ssh -i C:\Users\user\OneDrive\Desktop\dream\ourkuji\ourkuji.pem ec2-user@18.179.187.129
```

### Step 4: 執行部署

```bash
# 賦予執行權限
chmod +x deploy.sh

# 執行部署
./deploy.sh
```

### Step 5: 驗證部署

```bash
# 檢查服務狀態
ps aux | grep admin-1.0.0.jar

# 查看日誌
tail -f /home/ec2-user/logs/app.log

# 測試 API
curl http://18.179.187.129:8080/api/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin@kuji.com","password":"admin123"}'
```

---

## 🔐 AWS Credentials 設定

### 方法 A：使用 IAM Role（推薦）

1. 在 AWS Console 創建 IAM Role
2. 附加 S3 存取權限
3. 將 Role 附加到 EC2 Instance
4. 不需要在程式中設定 Access Key

### 方法 B：使用 Access Key

在 EC2 上設定環境變數：

```bash
export AWS_ACCESS_KEY=your_access_key_here
export AWS_SECRET_KEY=your_secret_key_here

# 或修改 application-prod.yml
# aws.s3.access-key: your_access_key
# aws.s3.secret-key: your_secret_key
```

---

## 📊 S3 Bucket 權限設定

確保 S3 Bucket 允許公開讀取：

**Bucket Policy：**

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

**IAM Policy（如果使用 Access Key）：**

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

## 🔍 測試 S3 功能

### 1. 測試上傳新聞圖片

```bash
curl -X POST http://18.179.187.129:8080/api/admin/news \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: multipart/form-data" \
  -F "title=Test News" \
  -F "content=Test Content" \
  -F "imageFile=@test-image.jpg"
```

### 2. 驗證 S3 URL

圖片 URL 應該是：
```
https://test-ourkuji.s3.ap-northeast-1.amazonaws.com/news/xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx.jpg
```

### 3. 在瀏覽器中開啟 URL

確認圖片可以公開存取。

---

## 📁 檔案上傳資料夾結構

S3 Bucket 內的資料夾結構：

```
test-ourkuji/
├── news/           # 新聞圖片
├── banner/         # Banner 圖片
├── lottery/        # 抽獎商品圖片
├── prize/          # 獎品圖片
├── store/          # 店家圖片
└── user/           # 使用者頭像
```

---

## ⚠️ 重要注意事項

### 開發環境 vs 正式環境

| 項目 | 開發環境 (dev) | 正式環境 (prod) |
|------|---------------|----------------|
| Profile | `dev` | `prod` |
| 檔案儲存 | 本地 `static/img/` | AWS S3 |
| Service | `LocalFileServiceImpl` | `S3ServiceImpl` |
| URL 格式 | `/img/news/xxx.jpg` | `https://test-ourkuji.s3...` |
| 資料庫 | 測試資料庫 | 正式資料庫 |

### 切換環境

```bash
# 啟動開發環境
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 啟動正式環境
java -jar -Dspring.profiles.active=prod admin-1.0.0.jar
```

---

## 🐛 常見問題

### 1. S3 上傳失敗：Access Denied

**原因**：AWS Credentials 未設定或 IAM 權限不足

**解決**：
- 檢查 IAM Role 是否附加到 EC2
- 或設定環境變數 `AWS_ACCESS_KEY` 和 `AWS_SECRET_KEY`

### 2. 圖片無法公開存取

**原因**：S3 Bucket Policy 未設定

**解決**：
- 在 S3 Console 設定 Bucket Policy（見上方範例）
- 或在上傳時設定 ACL 為 `public-read`

### 3. 服務無法啟動

**原因**：端口 8080 被佔用或資料庫連線失敗

**解決**：
```bash
# 檢查端口
netstat -tuln | grep 8080

# 測試資料庫連線
mysql -h database-1.clsi2geo699r.ap-northeast-1.rds.amazonaws.com -u admin -pWUfan0667. kuji

# 查看日誌
tail -100 /home/ec2-user/logs/app.log
```

---

## 📞 部署支援

完整文檔：
- 📖 `PRODUCTION_DEPLOYMENT_GUIDE.md` - 詳細部署指南
- 🚀 `deploy.sh` - 自動化部署腳本
- ⚙️ `application-prod.yml` - 正式環境配置

環境資訊：
- **EC2**: `18.179.187.129`
- **RDS**: `database-1.clsi2geo699r.ap-northeast-1.rds.amazonaws.com`
- **S3**: `test-ourkuji`
- **Region**: `ap-northeast-1` (Tokyo)

---

**配置完成！準備部署！** 🎉
