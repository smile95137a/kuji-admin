# 🔐 AWS 憑證洩漏修復指南

## ⚠️ 問題說明

在 commit `86dbcf15` 中，AWS 憑證被硬編碼在配置檔中並提交到 Git：
- **Access Key**: `AKIA************` (已隱藏)
- **Secret Key**: `************************************` (已隱藏)

GitHub 已阻止此次推送，但憑證已存在於本地 Git 歷史中。

---

## ✅ 已完成的修復

### 1. 移除硬編碼憑證
已將以下檔案中的憑證改為環境變數：
- `src/main/resources/application-dev.yml`
- `src/main/resources/application-prod.yml`

修改後的配置：
```yaml
aws:
  s3:
    access-key: ${AWS_ACCESS_KEY_ID:}
    secret-key: ${AWS_SECRET_ACCESS_KEY:}
```

### 2. 建立環境變數範本
已建立 `.env.example` 檔案作為範本。

### 3. 更新 .gitignore
已新增以下規則防止未來洩漏：
```
.env
.env.local
.env.*.local
*.log
app.log
**/application-local.yml
**/application-*.yml.local
test-result.txt
temp_token.txt
```

---

## 🚨 必須立即執行的操作

### 步驟 1：撤銷洩漏的 AWS 憑證（最重要！）

**前往 AWS Console 立即撤銷這些憑證：**

1. 登入 [AWS IAM Console](https://console.aws.amazon.com/iam/)
2. 點選「使用者」→ 找到對應的 IAM 使用者
3. 點選「安全憑證」標籤
4. 找到以 `AKIA` 開頭的 Access Key
5. 點選「停用」或「刪除」

⚠️ **這一步非常重要！** 即使 GitHub 阻止了推送，憑證已經存在於您的本地 Git 歷史中，可能已被其他方式洩漏。

### 步驟 2：生成新的 AWS 憑證

1. 在 AWS IAM Console 中建立新的 Access Key
2. 下載並安全保存新憑證
3. 配置到環境變數（見下方說明）

### 步驟 3：清理 Git 歷史

由於憑證已提交到本地 Git 歷史，需要移除這個 commit：

#### 方案 A：重置到上一個 commit（推薦）
```bash
# 查看最近的 commit
git log --oneline -5

# 重置到洩漏憑證之前的 commit
git reset --soft HEAD~1

# 重新提交修改（不含憑證）
git add .
git commit -m "fix: 移除硬編碼的 AWS 憑證，改用環境變數"
```

#### 方案 B：修改最後一個 commit
```bash
# 添加修改後的檔案
git add src/main/resources/application-dev.yml
git add src/main/resources/application-prod.yml
git add .gitignore
git add .env.example

# 修改最後一個 commit
git commit --amend -m "fix: 移除硬編碼的 AWS 憑證，改用環境變數"
```

#### 方案 C：使用 git filter-repo 徹底移除（最安全）
```bash
# 安裝 git-filter-repo（如果沒有）
pip install git-filter-repo

# 移除包含憑證的檔案歷史
git filter-repo --path src/main/resources/application-dev.yml --invert-paths
git filter-repo --path src/main/resources/application-prod.yml --invert-paths

# 重新添加修改後的檔案
git add src/main/resources/application-dev.yml
git add src/main/resources/application-prod.yml
git commit -m "fix: 移除硬編碼的 AWS 憑證，改用環境變數"
```

### 步驟 4：配置新的環境變數

#### Windows (PowerShell)
```powershell
# 臨時設定（本次會話有效）
$env:AWS_ACCESS_KEY_ID = "your_new_access_key"
$env:AWS_SECRET_ACCESS_KEY = "your_new_secret_key"

# 永久設定（系統環境變數）
[System.Environment]::SetEnvironmentVariable('AWS_ACCESS_KEY_ID', 'your_new_access_key', 'User')
[System.Environment]::SetEnvironmentVariable('AWS_SECRET_ACCESS_KEY', 'your_new_secret_key', 'User')
```

#### Windows (CMD)
```cmd
setx AWS_ACCESS_KEY_ID "your_new_access_key"
setx AWS_SECRET_ACCESS_KEY "your_new_secret_key"
```

#### Linux/Mac
```bash
# 編輯 ~/.bashrc 或 ~/.zshrc
export AWS_ACCESS_KEY_ID=your_new_access_key
export AWS_SECRET_ACCESS_KEY=your_new_secret_key

# 套用變更
source ~/.bashrc
```

#### 使用 .env 檔案（推薦）
```bash
# 複製範本
cp .env.example .env

# 編輯 .env 填入真實憑證
# AWS_ACCESS_KEY_ID=your_new_access_key
# AWS_SECRET_ACCESS_KEY=your_new_secret_key
```

### 步驟 5：驗證配置

```bash
# 啟動應用並檢查日誌
mvn spring-boot:run

# 確認 S3 功能正常
curl -X POST http://localhost:8080/api/test-s3-upload
```

### 步驟 6：重新推送到 GitHub

```bash
# 推送修改後的 commit
git push origin main

# 如果已經推送過舊 commit，需要強制推送
git push origin main --force
```

---

## 📋 檢查清單

在推送之前，請確認：

- [ ] **已在 AWS Console 撤銷舊憑證**
- [ ] **已生成新的 AWS 憑證**
- [ ] **已配置新憑證到環境變數**
- [ ] **已清理 Git 歷史（移除包含憑證的 commit）**
- [ ] **已測試應用正常啟動**
- [ ] **已測試 S3 上傳功能**
- [ ] **確認 `application-*.yml` 不包含明文憑證**
- [ ] **確認 `.env` 檔案在 `.gitignore` 中**

---

## 🔒 未來防範措施

### 1. 使用 pre-commit hook 防止憑證提交

建立 `.git/hooks/pre-commit` 檔案：
```bash
#!/bin/bash
# 檢查是否包含 AWS 憑證
if git diff --cached | grep -E "AKIA[0-9A-Z]{16}"; then
    echo "錯誤：檢測到 AWS Access Key，請移除後再提交！"
    exit 1
fi
```

### 2. 使用 AWS Secrets Manager

在生產環境中，考慮使用 AWS Secrets Manager 管理憑證：
```java
@Configuration
public class AwsConfig {
    @Bean
    public AWSSecretsManager secretsManager() {
        return AWSSecretsManagerClientBuilder
            .standard()
            .withRegion("ap-northeast-1")
            .build();
    }
}
```

### 3. 定期輪換憑證

設定定期（例如每 90 天）輪換 AWS 憑證的提醒。

### 4. 使用 IAM 角色（推薦）

如果在 EC2 上運行，使用 IAM 角色而非 Access Key：
```yaml
aws:
  s3:
    bucket-name: test-ourkuji
    region: ap-northeast-1
    # 不需要 access-key 和 secret-key
    # Spring Cloud AWS 會自動從 IAM 角色取得憑證
```

---

## 📞 需要協助？

如果在修復過程中遇到問題：

1. **AWS 帳戶問題**：聯繫 AWS Support
2. **Git 操作問題**：查看 [Git 官方文件](https://git-scm.com/doc)
3. **應用配置問題**：查看 Spring Boot 環境變數配置文件

---

## 📝 相關文件

- [AWS IAM 最佳實踐](https://docs.aws.amazon.com/IAM/latest/UserGuide/best-practices.html)
- [GitHub Secret Scanning](https://docs.github.com/en/code-security/secret-scanning)
- [Spring Boot 外部化配置](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)

---

**修復完成時間**：2026-01-21  
**嚴重程度**：🔴 高危（High）  
**狀態**：⏳ 等待執行步驟 1-6
