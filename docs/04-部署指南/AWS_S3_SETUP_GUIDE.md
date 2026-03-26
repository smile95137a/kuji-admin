# AWS S3 Bucket 設定指南

## 📦 Bucket 資訊
- **Name**: `test-ourkuji`
- **Region**: `ap-northeast-1` (Tokyo)
- **ARN**: `arn:aws:s3:::test-ourkuji`

---

## 🔧 設定步驟

### 步驟 1：設定 Bucket Policy（允許公開讀取）

1. 進入 AWS S3 Console
2. 選擇 `test-ourkuji` bucket
3. 點擊 **Permissions** 標籤
4. 滾動到 **Bucket policy** 區塊
5. 點擊 **Edit**
6. 貼上以下 JSON：

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

7. 點擊 **Save changes**

**說明**：此設定允許任何人讀取 bucket 內的檔案（圖片需要公開存取），但不允許寫入。

---

### 步驟 2：關閉 Block Public Access（如果需要）

1. 在 **Permissions** 標籤
2. 找到 **Block public access (bucket settings)**
3. 點擊 **Edit**
4. 取消勾選以下選項：
   - ✅ Block public access to buckets and objects granted through **new** public bucket or access point policies
   - ✅ Block public and cross-account access to buckets and objects through **any** public bucket or access point policies

5. 點擊 **Save changes**
6. 輸入 `confirm` 確認

**⚠️ 注意**：僅取消與 bucket policy 相關的選項，保留其他安全設定。

---

### 步驟 3：設定 CORS（如果前端需要）

如果前端需要直接從瀏覽器存取 S3，需要設定 CORS：

1. 在 **Permissions** 標籤
2. 滾動到 **Cross-origin resource sharing (CORS)** 區塊
3. 點擊 **Edit**
4. 貼上以下 JSON：

```json
[
  {
    "AllowedHeaders": [
      "*"
    ],
    "AllowedMethods": [
      "GET",
      "HEAD"
    ],
    "AllowedOrigins": [
      "*"
    ],
    "ExposeHeaders": [
      "ETag"
    ],
    "MaxAgeSeconds": 3000
  }
]
```

5. 點擊 **Save changes**

**說明**：此設定允許所有來源（`*`）讀取圖片。正式環境建議限制為特定域名。

---

### 步驟 4：建立資料夾結構

在 S3 Console 中建立以下資料夾：

```
test-ourkuji/
├── news/           # 新聞圖片
├── banner/         # Banner 圖片
├── lottery/        # 抽獎商品圖片
├── prize/          # 獎品圖片
├── store/          # 店家圖片
└── user/           # 使用者頭像
```

**操作**：
1. 點擊 **Create folder**
2. 輸入資料夾名稱（例如：`news/`）
3. 點擊 **Create folder**
4. 重複步驟建立其他資料夾

---

## 🔐 IAM 權限設定

### 選項 A：使用 IAM Role（推薦）

#### 1. 建立 IAM Policy

1. 進入 IAM Console
2. 點擊 **Policies** → **Create policy**
3. 選擇 **JSON** 標籤
4. 貼上以下內容：

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "S3AccessForKujiAdmin",
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

5. 點擊 **Next**
6. 名稱：`KujiAdminS3Access`
7. 描述：`Allow KUJI Admin to upload/delete images to S3`
8. 點擊 **Create policy**

#### 2. 建立 IAM Role

1. 在 IAM Console
2. 點擊 **Roles** → **Create role**
3. 選擇 **AWS service**
4. Use case: **EC2**
5. 點擊 **Next**
6. 搜尋並勾選剛建立的 `KujiAdminS3Access` policy
7. 點擊 **Next**
8. 名稱：`KujiAdminEC2Role`
9. 描述：`Role for KUJI Admin EC2 to access S3`
10. 點擊 **Create role**

#### 3. 附加 Role 到 EC2

1. 進入 EC2 Console
2. 選擇 EC2 Instance (`18.179.187.129`)
3. 點擊 **Actions** → **Security** → **Modify IAM role**
4. 選擇 `KujiAdminEC2Role`
5. 點擊 **Update IAM role**

**驗證**：SSH 到 EC2，執行：
```bash
aws s3 ls s3://test-ourkuji/
```

如果看到資料夾列表，表示設定成功！

---

### 選項 B：使用 Access Key（不推薦）

如果無法使用 IAM Role，可以使用 Access Key：

#### 1. 建立 IAM User

1. 進入 IAM Console
2. 點擊 **Users** → **Create user**
3. 名稱：`kuji-admin-s3-user`
4. 點擊 **Next**
5. 勾選 **Attach policies directly**
6. 搜尋並勾選剛建立的 `KujiAdminS3Access` policy
7. 點擊 **Next** → **Create user**

#### 2. 建立 Access Key

1. 點擊剛建立的 `kuji-admin-s3-user`
2. 點擊 **Security credentials** 標籤
3. 滾動到 **Access keys** 區塊
4. 點擊 **Create access key**
5. 選擇 **Application running on an AWS compute service**
6. 勾選確認框
7. 點擊 **Next**
8. 描述：`KUJI Admin S3 Access`
9. 點擊 **Create access key**
10. **重要**：複製 Access Key ID 和 Secret Access Key

#### 3. 在 EC2 設定環境變數

SSH 到 EC2：
```bash
export AWS_ACCESS_KEY=your_access_key_id_here
export AWS_SECRET_KEY=your_secret_access_key_here

# 永久設定（加到 ~/.bashrc）
echo 'export AWS_ACCESS_KEY=your_access_key_id_here' >> ~/.bashrc
echo 'export AWS_SECRET_KEY=your_secret_access_key_here' >> ~/.bashrc
source ~/.bashrc
```

---

## 🧪 測試 S3 功能

### 1. 在 EC2 上測試 AWS CLI

```bash
# 列出 bucket 內容
aws s3 ls s3://test-ourkuji/

# 上傳測試檔案
echo "Hello S3" > test.txt
aws s3 cp test.txt s3://test-ourkuji/test.txt

# 下載測試檔案
aws s3 cp s3://test-ourkuji/test.txt test-download.txt

# 檢查內容
cat test-download.txt

# 刪除測試檔案
aws s3 rm s3://test-ourkuji/test.txt
```

### 2. 測試公開存取

上傳圖片後，在瀏覽器開啟：
```
https://test-ourkuji.s3.ap-northeast-1.amazonaws.com/test.txt
```

如果看到內容，表示公開讀取設定成功！

### 3. 測試應用程式上傳

```bash
# 取得 JWT Token
TOKEN=$(curl -s -X POST http://18.179.187.129:8080/api/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin@kuji.com","password":"admin123"}' \
  | jq -r '.data.token')

# 上傳圖片
curl -X POST http://18.179.187.129:8080/api/admin/news \
  -H "Authorization: Bearer $TOKEN" \
  -F "title=S3 Test" \
  -F "content=Testing S3 upload" \
  -F "imageFile=@test-image.jpg"
```

檢查回應中的 `imageUrl`，應該是：
```
https://test-ourkuji.s3.ap-northeast-1.amazonaws.com/news/xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx.jpg
```

---

## 📊 監控與成本管理

### 1. 設定 CloudWatch Metrics

在 S3 Console：
1. 選擇 bucket
2. 點擊 **Metrics** 標籤
3. 啟用 **Request metrics**

### 2. 設定 Billing Alerts

在 AWS Billing Console：
1. 點擊 **Budgets**
2. 點擊 **Create budget**
3. 選擇 **Cost budget**
4. 名稱：`KUJI S3 Monthly Budget`
5. 預算金額：`$5`
6. 設定警報：達到 80% 時發送郵件

### 3. 設定 Lifecycle Policy（自動刪除舊檔案）

在 S3 Console：
1. 選擇 bucket
2. 點擊 **Management** 標籤
3. 點擊 **Create lifecycle rule**
4. 名稱：`Delete old files`
5. 規則範圍：選擇特定前綴（例如：`temp/`）
6. Lifecycle rule actions：
   - ✅ Expire current versions of objects
   - Days after object creation: `30`
7. 點擊 **Create rule**

**建議**：為臨時檔案（如 `temp/`）設定 30 天自動刪除。

---

## 🔒 安全檢查清單

- [ ] Bucket Policy 僅允許 `s3:GetObject`（讀取）
- [ ] Block Public Access 僅開放必要權限
- [ ] IAM Role 僅賦予必要的 S3 權限
- [ ] Access Key（如果使用）已安全儲存
- [ ] CloudWatch Metrics 已啟用
- [ ] Billing Alerts 已設定
- [ ] CORS 設定正確（如果前端需要）
- [ ] 測試公開圖片存取
- [ ] 測試應用程式上傳功能

---

## 🐛 常見問題

### Q1: 上傳成功但無法存取圖片

**原因**：Bucket Policy 未設定或 Block Public Access 阻擋

**解決**：
1. 檢查 Bucket Policy 是否正確
2. 確認 Block Public Access 設定

### Q2: AccessDenied 錯誤

**原因**：IAM 權限不足

**解決**：
1. 檢查 IAM Role 是否附加到 EC2
2. 檢查 IAM Policy 是否包含正確的 Actions 和 Resource

### Q3: CORS 錯誤

**原因**：CORS 設定未包含前端域名

**解決**：
1. 更新 CORS 設定
2. 將 `AllowedOrigins` 改為前端域名（例如：`["https://yourdomain.com"]`）

### Q4: 圖片上傳後 URL 無法開啟

**原因**：可能是 ACL 權限問題

**解決**：
在 `S3ServiceImpl.java` 確認：
```java
.acl(ObjectCannedACL.PUBLIC_READ)
```

---

## 📞 支援資源

- **AWS S3 文檔**：https://docs.aws.amazon.com/s3/
- **IAM 最佳實踐**：https://docs.aws.amazon.com/IAM/latest/UserGuide/best-practices.html
- **應用程式配置**：查看 `application-prod.yml`
- **程式碼實作**：查看 `S3ServiceImpl.java`

---

**設定完成後，記得執行測試確認所有功能正常！** ✅
