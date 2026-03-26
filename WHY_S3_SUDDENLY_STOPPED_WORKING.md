# 為什麼 S3 圖片上傳突然失效？原因分析

## 🤔 問題現象

**之前**：圖片上傳正常
**現在**：出現 `Unable to load credentials` 錯誤

## 📊 常見原因排行榜

### 1. EC2 實例重啟或服務重啟 ⭐⭐⭐⭐⭐（最常見）

**為什麼會發生**：
- EC2 實例重啟（手動重啟、系統維護、意外關機）
- 應用程式重新部署
- `systemctl restart kuji-admin` 重啟服務

**為什麼會導致問題**：
```bash
# 如果你之前是這樣設定環境變數（只在當前 Session 有效）
export AWS_ACCESS_KEY_ID=xxx
export AWS_SECRET_ACCESS_KEY=xxx

# 重啟後這些環境變數就消失了！
```

**解決方法**：
- ✅ 使用 IAM Role（永久有效，推薦）
- ✅ 將環境變數寫入 systemd service 檔案（永久有效）
- ❌ 不要只在 shell 中 export（重啟後失效）

---

### 2. AWS Access Key 過期或失效 ⭐⭐⭐⭐

**為什麼會發生**：
- AWS Access Key 有效期限到期（如果設定了）
- 在 AWS Console 中刪除或禁用了 Access Key
- Access Key 被輪替（Rotate）了

**如何檢查**：
1. 前往 AWS Console → IAM → Users → Security credentials
2. 查看 Access Key 的狀態：
   - ✅ Active（正常）
   - ❌ Inactive（已禁用）
   - ❌ 不存在（已刪除）

**解決方法**：
- 重新生成 Access Key
- 或改用 IAM Role

---

### 3. IAM Role 被移除或權限變更 ⭐⭐⭐

**為什麼會發生**：
- EC2 的 IAM Role 被手動移除
- IAM Role 的 Policy 被修改，移除了 S3 權限
- IAM Role 被刪除

**如何檢查**：
```bash
# 在 EC2 上執行
curl http://169.254.169.254/latest/meta-data/iam/security-credentials/

# 如果返回空白或錯誤，代表沒有 IAM Role
```

或在 AWS Console：
1. EC2 Console → 選擇實例
2. 查看 **IAM Role** 欄位
3. 如果是空的，就是被移除了

**解決方法**：
- 重新附加 IAM Role
- 檢查 Policy 是否包含 S3 權限

---

### 4. 環境變數設定錯誤 ⭐⭐⭐

**為什麼會發生**：
- 部署新版本時忘記設定環境變數
- systemd service 檔案被覆蓋
- 環境變數拼寫錯誤

**如何檢查**：
```bash
# 檢查 systemd service 的環境變數
sudo systemctl show kuji-admin | grep Environment

# 應該看到類似這樣：
# Environment=AWS_ACCESS_KEY_ID=xxx AWS_SECRET_ACCESS_KEY=xxx
```

**常見錯誤**：
```bash
# ❌ 錯誤：拼寫錯誤
Environment="AWS_ACESS_KEY_ID=xxx"  # ACESS 少一個 C

# ❌ 錯誤：多了空格
Environment=" AWS_ACCESS_KEY_ID=xxx"

# ✅ 正確
Environment="AWS_ACCESS_KEY_ID=xxx"
```

---

### 5. S3 Bucket 權限變更 ⭐⭐

**為什麼會發生**：
- Bucket Policy 被修改
- 公開存取設定被變更
- IAM Policy 的 S3 權限被縮減

**如何檢查**：
```bash
# 使用 AWS CLI 測試
aws s3 ls s3://test-ourkuji --region ap-northeast-1
aws s3 cp test.txt s3://test-ourkuji/test/
```

**解決方法**：
- 檢查 Bucket Policy
- 確認 IAM Policy 包含必要的 S3 權限

---

### 6. 網路問題 ⭐

**為什麼會發生**：
- EC2 的 Security Group 設定變更
- VPC/Subnet 設定變更
- AWS S3 endpoint 無法連線

**如何檢查**：
```bash
# 測試連線
curl https://s3.ap-northeast-1.amazonaws.com

# 檢查 DNS 解析
nslookup s3.ap-northeast-1.amazonaws.com
```

---

## 🕵️ 診斷步驟（按順序執行）

### Step 1: 檢查服務是否有重啟過

```bash
# 查看服務啟動時間
sudo systemctl status kuji-admin | grep "Active:"

# 查看最近的重啟記錄
sudo journalctl -u kuji-admin | grep "Started\|Stopped" | tail -10
```

如果啟動時間是最近幾小時/天，很可能是重啟導致的。

---

### Step 2: 檢查 IAM Role 狀態

```bash
# 在 EC2 上執行
curl http://169.254.169.254/latest/meta-data/iam/security-credentials/
```

**正常輸出**：顯示 Role 名稱
```
KUJI-EC2-S3-Role
```

**異常輸出**：
```
<?xml version="1.0" encoding="iso-8859-1"?>
<!DOCTYPE html PUBLIC ...
```
或空白 → 代表沒有 IAM Role

---

### Step 3: 檢查環境變數

```bash
# 檢查 systemd service 設定
sudo systemctl show kuji-admin | grep Environment

# 檢查當前執行中的環境變數
sudo cat /proc/$(pgrep -f "java.*admin.*jar")/environ | tr '\0' '\n' | grep AWS
```

**正常輸出**：
```
AWS_ACCESS_KEY_ID=AKIA...
AWS_SECRET_ACCESS_KEY=xxx...
```

**異常輸出**：空白或找不到

---

### Step 4: 檢查 Access Key 有效性

```bash
# 使用 AWS CLI 測試
aws sts get-caller-identity

# 正常輸出
{
    "UserId": "AIDAI...",
    "Account": "123456789",
    "Arn": "arn:aws:iam::123456789:user/xxx"
}

# 異常輸出
An error occurred (InvalidClientTokenId) when calling the GetCallerIdentity operation: The security token included in the request is invalid.
```

---

### Step 5: 查看應用程式日誌

```bash
# 查看最近的錯誤
sudo journalctl -u kuji-admin -n 100 | grep -i "credential\|s3\|aws"

# 或查看日誌檔案
tail -100 /home/ubuntu/kuji-server/logs/app.log | grep -i "credential\|s3"
```

尋找關鍵錯誤訊息：
- `Unable to load credentials`
- `Access Denied`
- `InvalidClientTokenId`
- `ExpiredToken`

---

## 📅 時間線分析法

讓我們回推問題發生的時間：

```
時間軸分析：
├─ T-7 天：上傳正常 ✅
├─ T-3 天：上傳正常 ✅
├─ T-1 天：發生了某件事？🤔
└─ T-現在：上傳失敗 ❌
```

**問自己以下問題**：

1. ❓ **最近有沒有重啟過 EC2 或服務？**
   - 如果有 → 99% 是環境變數失效

2. ❓ **最近有沒有重新部署應用程式？**
   - 如果有 → 檢查部署腳本是否設定環境變數

3. ❓ **最近有沒有在 AWS Console 做過任何變更？**
   - IAM Role
   - Access Key
   - Bucket Policy
   - Security Group

4. ❓ **最近有沒有修改過 application.yml 或 S3Config.java？**
   - 檢查 Git 提交記錄

---

## 🎯 最可能的原因（90% 機率）

根據你的錯誤訊息：
```
Unable to load credentials from any of the providers in the chain
```

這代表：
1. ✅ 應用程式正常運作（沒有程式碼錯誤）
2. ✅ S3Config 配置正確
3. ❌ **憑證來源全部失效**

**最可能的情況**：
- EC2/服務重啟後，環境變數消失
- 且沒有設定 IAM Role

**證據**：
- 錯誤訊息列出所有失敗的 Provider：
  - SystemPropertyCredentialsProvider ❌
  - EnvironmentVariableCredentialsProvider ❌
  - ProfileCredentialsProvider ❌
  - InstanceProfileCredentialsProvider ❌（沒有 IAM Role）

---

## ✅ 立即解決方案

### 方案 A：快速修復（5 分鐘）

```bash
# 1. SSH 到 EC2
ssh ubuntu@18.179.187.129

# 2. 編輯 systemd service
sudo nano /etc/systemd/system/kuji-admin.service

# 3. 在 [Service] 區段加入（替換成你的實際 Key）
Environment="AWS_ACCESS_KEY_ID=AKIA..."
Environment="AWS_SECRET_ACCESS_KEY=xxx..."

# 4. 重新載入並重啟
sudo systemctl daemon-reload
sudo systemctl restart kuji-admin

# 5. 驗證
curl -X POST http://localhost/api/admin/upload/banner \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@test.jpg"
```

### 方案 B：長期解決（推薦，15 分鐘）

設定 IAM Role，參考：`EC2_S3_CREDENTIALS_FIX.md`

---

## 🔮 預防未來再次發生

### 1. 監控設定

建立 CloudWatch Alarm：
```json
{
  "MetricName": "4xxErrors",
  "Namespace": "AWS/S3",
  "Statistic": "Sum",
  "Period": 300,
  "EvaluationPeriods": 1,
  "Threshold": 10,
  "ComparisonOperator": "GreaterThanThreshold"
}
```

### 2. 健康檢查

建立定時檢查腳本：
```bash
#!/bin/bash
# /home/ubuntu/check-s3-health.sh

aws s3 ls s3://test-ourkuji --region ap-northeast-1 > /dev/null 2>&1
if [ $? -ne 0 ]; then
    echo "S3 credentials check failed at $(date)" | \
    mail -s "URGENT: S3 Credentials Failed" admin@example.com
fi
```

加入 crontab：
```bash
*/30 * * * * /home/ubuntu/check-s3-health.sh
```

### 3. 部署檢查清單

每次部署前確認：
- [ ] IAM Role 已附加
- [ ] 環境變數已設定在 systemd service
- [ ] S3 bucket 存在且有權限
- [ ] 測試上傳功能

---

## 📞 還是不行？

提供以下資訊以獲得進一步協助：

```bash
# 1. 系統資訊
cat /etc/os-release
java -version

# 2. 服務狀態
sudo systemctl status kuji-admin

# 3. 環境變數
sudo systemctl show kuji-admin | grep Environment

# 4. IAM Role
curl http://169.254.169.254/latest/meta-data/iam/security-credentials/

# 5. 最近的日誌
sudo journalctl -u kuji-admin -n 200

# 6. AWS CLI 測試
aws s3 ls s3://test-ourkuji --region ap-northeast-1
```

---

## 💡 總結

**最可能原因**：EC2/服務重啟 + 環境變數未永久化

**最快解決**：設定環境變數到 systemd service

**最佳解決**：使用 IAM Role

**預防方法**：設定監控 + 定期檢查
