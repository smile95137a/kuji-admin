# EC2 IAM Role 設定指南

## 在 AWS Console 建立 IAM Role

### Step 1: 建立 IAM Policy
1. 前往 AWS Console → IAM → Policies → Create Policy
2. 選擇 JSON，貼上以下內容：

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

3. 命名為：`KUJI-S3-Upload-Policy`

### Step 2: 建立 IAM Role
1. IAM → Roles → Create Role
2. Trusted entity type: **AWS service**
3. Use case: **EC2**
4. 附加剛才建立的 Policy: `KUJI-S3-Upload-Policy`
5. 命名為：`KUJI-EC2-S3-Role`

### Step 3: 附加 Role 到 EC2
1. EC2 Console → 選擇你的實例
2. Actions → Security → Modify IAM role
3. 選擇 `KUJI-EC2-S3-Role`
4. Update IAM role

### Step 4: 重啟應用程式
```bash
ssh ubuntu@18.179.187.129
cd /home/ubuntu/kuji-server
sudo systemctl restart kuji-admin
sudo systemctl status kuji-admin
```

**優點**：
- ✅ 最安全，不需要在程式碼或環境變數中存放憑證
- ✅ 自動輪替憑證
- ✅ 符合 AWS 最佳實踐

---

## 方案 2：設定永久環境變數（快速但較不安全）

如果無法使用 IAM Role，可以設定環境變數：

### 編輯 systemd service 檔案
```bash
sudo nano /etc/systemd/system/kuji-admin.service
```

在 `[Service]` 區段加入：
```ini
[Service]
Environment="AWS_ACCESS_KEY_ID=你的AccessKey"
Environment="AWS_SECRET_ACCESS_KEY=你的SecretKey"
Environment="SPRING_PROFILES_ACTIVE=prod"
...
```

### 重新載入並重啟
```bash
sudo systemctl daemon-reload
sudo systemctl restart kuji-admin
sudo systemctl status kuji-admin
```

---

## 方案 3：直接寫在 application-prod.yml（最不安全，不推薦）

僅供測試使用，**不要提交到 Git**：

```yaml
aws:
  s3:
    bucket-name: test-ourkuji
    region: ap-northeast-1
    access-key: AKIA...你的Key
    secret-key: xxx...你的Secret
```

---

## 驗證 S3 權限

### 測試上傳
```bash
# 使用 AWS CLI 測試
aws s3 ls s3://test-ourkuji --region ap-northeast-1

# 如果有設定 IAM Role，應該可以看到 bucket 內容
```

### 檢查應用程式日誌
```bash
sudo journalctl -u kuji-admin -f
# 或
tail -f /home/ubuntu/kuji-server/logs/app.log
```

---

## 推薦流程

1. **立即解決**：使用方案 2 設定環境變數
2. **長期最佳**：改用方案 1 的 IAM Role
3. **避免使用**：方案 3（硬編碼憑證）

---

## 常見錯誤

### 錯誤 1: "Unable to load credentials from any provider"
**原因**: 環境變數未設定或 IAM Role 未附加
**解法**: 按照上述步驟設定

### 錯誤 2: "Access Denied"
**原因**: IAM Policy 權限不足
**解法**: 確認 Policy 包含 `s3:PutObject` 權限

### 錯誤 3: 重啟後憑證失效
**原因**: 環境變數只在當前 Session 有效
**解法**: 寫入 systemd service 檔案或使用 IAM Role

