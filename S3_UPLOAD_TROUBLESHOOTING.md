# S3 圖片上傳問題排查與解決指南

## 📋 問題現象

```json
{
    "success": false,
    "error": {
        "code": "BUSINESS_ERROR",
        "message": "Unable to load credentials from any of the providers..."
    }
}
```

**原因**：AWS 憑證遺失或過期

---

## 🔍 快速診斷

### Step 1: SSH 連線到 EC2
```bash
ssh ubuntu@18.179.187.129
```

### Step 2: 檢查應用程式狀態
```bash
sudo systemctl status kuji-admin
```

### Step 3: 檢查環境變數
```bash
echo $AWS_ACCESS_KEY_ID
echo $AWS_SECRET_ACCESS_KEY
```

如果兩個都是空的，就是憑證問題。

### Step 4: 檢查 IAM Role
```bash
curl http://169.254.169.254/latest/meta-data/iam/security-credentials/
```

如果有輸出 Role 名稱，代表有設定 IAM Role。

---

## ✅ 解決方案（按推薦順序）

### 方案 1：使用 IAM Role（最推薦）⭐⭐⭐⭐⭐

**為什麼推薦**：
- ✅ 最安全（不需要在程式碼中存放憑證）
- ✅ 自動輪替憑證
- ✅ 符合 AWS 最佳實踐

**操作步驟**：

#### 1. 在 AWS Console 建立 IAM Policy

前往：IAM → Policies → Create Policy → JSON

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "s3:PutObject",
                "s3:PutObjectAcl",
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

命名為：`KUJI-S3-Upload-Policy`

#### 2. 建立 IAM Role

前往：IAM → Roles → Create Role

- Trusted entity type: **AWS service**
- Use case: **EC2**
- Permissions: 選擇剛才建立的 `KUJI-S3-Upload-Policy`
- Role name: `KUJI-EC2-S3-Role`

#### 3. 附加 Role 到 EC2

前往：EC2 Console → 選擇實例

- Actions → Security → **Modify IAM role**
- 選擇 `KUJI-EC2-S3-Role`
- Update IAM role

#### 4. 重啟應用程式

```bash
ssh ubuntu@18.179.187.129
sudo systemctl restart kuji-admin
sudo systemctl status kuji-admin
```

#### 5. 測試上傳

用瀏覽器訪問：`http://18.179.187.129/api/admin/upload/banner`

---

### 方案 2：設定永久環境變數（次推薦）⭐⭐⭐

**適用情況**：
- 無法設定 IAM Role
- 需要快速修復

**操作步驟**：

#### 1. 編輯 systemd service 檔案

```bash
sudo nano /etc/systemd/system/kuji-admin.service
```

#### 2. 在 `[Service]` 區段加入環境變數

```ini
[Unit]
Description=KUJI Admin Backend Service
After=network.target

[Service]
Type=simple
User=ubuntu
WorkingDirectory=/home/ubuntu/kuji-server
Environment="SPRING_PROFILES_ACTIVE=prod"
Environment="AWS_ACCESS_KEY_ID=你的_ACCESS_KEY"
Environment="AWS_SECRET_ACCESS_KEY=你的_SECRET_KEY"
ExecStart=/usr/bin/java -jar admin-1.0.0.jar
Restart=on-failure
RestartSec=10
StandardOutput=append:/home/ubuntu/kuji-server/logs/app.log
StandardError=append:/home/ubuntu/kuji-server/logs/error.log

[Install]
WantedBy=multi-user.target
```

#### 3. 重新載入並重啟

```bash
sudo systemctl daemon-reload
sudo systemctl restart kuji-admin
sudo systemctl status kuji-admin
```

#### 4. 驗證環境變數

```bash
sudo systemctl show kuji-admin | grep Environment
```

---

### 方案 3：使用快速修復腳本 ⭐⭐⭐⭐

```bash
# 1. 上傳腳本到 EC2
scp fix-s3-credentials.sh ubuntu@18.179.187.129:~/

# 2. 執行腳本
ssh ubuntu@18.179.187.129
chmod +x fix-s3-credentials.sh
./fix-s3-credentials.sh
```

腳本會自動：
- 檢查 IAM Role 狀態
- 檢查環境變數
- 提供互動式修復選項

---

## 🧪 測試與驗證

### 測試 1：檢查應用程式日誌

```bash
# 即時查看日誌
sudo journalctl -u kuji-admin -f

# 或查看檔案日誌
tail -f /home/ubuntu/kuji-server/logs/app.log
```

尋找類似這樣的日誌：
```
✅ S3Client 初始化成功
✅ 圖片上傳成功: banner/xxx.jpg
```

### 測試 2：使用 AWS CLI 測試 S3

```bash
# 安裝 AWS CLI（如果還沒有）
sudo apt update
sudo apt install awscli -y

# 測試列出 bucket
aws s3 ls s3://test-ourkuji --region ap-northeast-1

# 測試上傳檔案
echo "test" > test.txt
aws s3 cp test.txt s3://test-ourkuji/test/test.txt
```

### 測試 3：測試 API 上傳

使用 Postman 或 curl：

```bash
curl -X POST http://18.179.187.129/api/admin/upload/banner \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@test-image.jpg"
```

---

## ❌ 常見錯誤與解決

### 錯誤 1: "Unable to load credentials"

**原因**：沒有設定憑證或 IAM Role
**解決**：按照上述方案 1 或方案 2 設定

### 錯誤 2: "Access Denied"

**原因**：IAM Policy 權限不足
**解決**：確認 Policy 包含以下權限：
- `s3:PutObject`
- `s3:PutObjectAcl` (如果需要公開讀取)
- `s3:GetObject`
- `s3:DeleteObject`
- `s3:ListBucket`

### 錯誤 3: 重啟後憑證失效

**原因**：環境變數只在當前 Session 有效
**解決**：
- 寫入 systemd service 檔案（方案 2）
- 或使用 IAM Role（方案 1）

### 錯誤 4: "Bucket does not exist"

**原因**：Bucket 名稱或區域錯誤
**檢查**：
```bash
# 確認 application-prod.yml 中的設定
cat /home/ubuntu/kuji-server/application-prod.yml | grep -A 5 "aws:"
```

應該是：
```yaml
aws:
  s3:
    bucket-name: test-ourkuji
    region: ap-northeast-1
```

---

## 📊 監控與維護

### 設定日誌輪替

```bash
sudo nano /etc/logrotate.d/kuji-admin
```

```
/home/ubuntu/kuji-server/logs/*.log {
    daily
    rotate 7
    compress
    delaycompress
    missingok
    notifempty
    create 0644 ubuntu ubuntu
}
```

### 定期檢查憑證狀態

建立 cron job：

```bash
crontab -e
```

加入：
```bash
0 */6 * * * /usr/bin/aws s3 ls s3://test-ourkuji --region ap-northeast-1 || echo "S3 credentials check failed" | mail -s "KUJI S3 Alert" admin@example.com
```

---

## 🔐 安全最佳實踐

### ✅ 推薦做法

1. **使用 IAM Role**：而非 Access Key
2. **最小權限原則**：只給必要的 S3 權限
3. **定期輪替憑證**：如果使用 Access Key
4. **監控異常活動**：使用 CloudWatch

### ❌ 避免做法

1. ❌ 將憑證硬編碼在 application.yml
2. ❌ 將憑證提交到 Git
3. ❌ 使用 Root Account 的 Access Key
4. ❌ 給予 S3 全域權限（`s3:*`）

---

## 📞 需要協助？

### 檢查清單

- [ ] 已確認 IAM Role 或環境變數設定
- [ ] 已重啟應用程式
- [ ] 已測試 AWS CLI S3 連線
- [ ] 已檢查應用程式日誌
- [ ] 已測試 API 上傳功能

### 提供以下資訊以獲得協助

1. **應用程式日誌**：
   ```bash
   sudo journalctl -u kuji-admin -n 100
   ```

2. **環境變數檢查**：
   ```bash
   sudo systemctl show kuji-admin | grep Environment
   ```

3. **IAM Role 檢查**：
   ```bash
   curl http://169.254.169.254/latest/meta-data/iam/security-credentials/
   ```

4. **錯誤訊息截圖**

---

## 📚 相關文件

- [AWS S3 IAM 最佳實踐](https://docs.aws.amazon.com/IAM/latest/UserGuide/best-practices.html)
- [EC2 Instance Profile](https://docs.aws.amazon.com/IAM/latest/UserGuide/id_roles_use_switch-role-ec2.html)
- [Spring Boot AWS SDK Configuration](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/credentials.html)

