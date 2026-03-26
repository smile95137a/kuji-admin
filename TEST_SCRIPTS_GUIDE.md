# KUJI API 測試腳本使用指南

## 📋 檔案說明

本專案提供兩種 API 測試腳本：

### 1. `test-all-apis-powershell.bat` ⭐ **推薦使用**

- **優點**：
  - ✅ 使用 PowerShell `Invoke-RestMethod`，穩定可靠
  - ✅ 沒有 cmd 的引號問題
  - ✅ 支援彩色輸出（綠色=成功，紅色=失敗）
  - ✅ 自動解析 JSON 回應
  - ✅ 完整的錯誤處理

- **缺點**：
  - ⚠️ 需要 Windows PowerShell 3.0+ (Win7 SP1/Win8+ 內建)

### 2. `test-all-apis.bat`

- **優點**：
  - ✅ 純 cmd 腳本，無額外依賴

- **缺點**：
  - ❌ cmd 的 curl 引號處理複雜
  - ❌ 變數展開時機問題
  - ⚠️ **目前有技術問題，建議使用 PowerShell 版本**

---

## 🚀 快速開始

### 前置條件

1. **啟動後端服務**
   ```bash
   cd c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin
   mvn spring-boot:run
   ```

2. **等待服務啟動完成**
   - 看到 "Started AdminApplication" 訊息

### 執行測試（推薦）

```batch
# 直接雙擊執行
test-all-apis-powershell.bat

# 或在 cmd 執行
cd c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin
test-all-apis-powershell.bat
```

---

## 📊 測試涵蓋範圍

### 第一部分：後台 API 測試（5 項）

| # | 測試項目 | API | 說明 |
|---|----------|-----|------|
| 1 | 後台登入 | `POST /admin/auth/login` | 取得 admin token |
| 2 | 使用者選單 | `GET /admin/users/menu` | 需要 admin token |
| 3 | 店家列表 | `POST /admin/store/list` | 需要 admin token |
| 4 | 所有縣市(後台) | `GET /admin/district/cities` | 需要 admin token |
| 5 | 推薦碼 | `GET /admin/referral-codes/my-store` | 需要 admin token |

### 第二部分：前台 API 測試（7 項）

| # | 測試項目 | API | 說明 |
|---|----------|-----|------|
| 6 | 前台註冊 | `POST /auth/register` | 使用隨機 email |
| 7 | 前台登入 | `POST /auth/login` | 取得 user token |
| 8 | 忘記密碼 | `POST /auth/forgot-password` | 發送重設郵件 |
| 9 | 重設密碼(無效) | `POST /auth/reset-password` | **預期失敗** |
| 10 | 所有縣市(前台) | `GET /district/cities` | 公開 API |
| 11 | 台北市行政區 | `GET /district/districts/台北市` | 公開 API |
| 12 | 行政區樹 | `GET /district/tree` | 公開 API |

### 第三部分：權限測試（3 項）

| # | 測試項目 | 說明 |
|---|----------|------|
| 13 | 未登入訪問後台 | **預期失敗** - 應返回 403 |
| 14 | 前台 token 訪問後台 | **預期失敗** - 權限不足 |
| 15 | 後台 token 訪問前台 | **預期成功** - admin 可訪問前台公開 API |

---

## 📝 測試結果範例

### ✅ 成功情況

```
================================
  KUJI API 測試腳本
================================

第一部分：後台 API 測試
========================================

[1] 後台登入...
PASS
Token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOi...

[2] 取得使用者選單...
PASS

[3] 取得店家列表...
PASS

========================================
  測試結果摘要
========================================
通過: 15
失敗: 0

所有測試通過！
```

### ❌ 失敗情況

```
[1] 後台登入...
ERROR: 遠端伺服器傳回一個錯誤: (500) 內部伺服器錯誤。

========================================
  測試結果摘要
========================================
通過: 8
失敗: 7

測試失敗
```

---

## 🔍 故障排除

### 問題 1：所有測試都失敗

**錯誤訊息**：
```
ERROR: 無法連線至遠端伺服器
ERROR: 遠端伺服器傳回一個錯誤: (500) 內部伺服器錯誤。
```

**解決方案**：
1. 確認後端服務是否啟動：
   ```bash
   netstat -ano | findstr :8080
   ```

2. 檢查 Spring Boot 日誌：
   ```bash
   # 查看最後 50 行日誌
   tail -n 50 app.log
   ```

3. 確認資料庫連線：
   ```sql
   -- 登入 MySQL
   mysql -u root -p kuji_db

   -- 檢查 admin 帳號
   SELECT * FROM admin_user WHERE username = 'admin@kuji.com';

   -- 檢查角色
   SELECT * FROM role WHERE code = 'ROLE_ADMIN';
   ```

---

### 問題 2：後台登入失敗

**錯誤訊息**：
```
[1] 後台登入...
FAIL
```

**檢查步驟**：

1. **確認資料庫初始化**：
   ```sql
   -- 應該有 3 個角色
   SELECT * FROM role;

   -- 應該有 admin 帳號
   SELECT username, password FROM admin_user WHERE username = 'admin@kuji.com';
   ```

2. **檢查密碼是否正確**：
   - 預設密碼：`admin123`
   - 如果修改過，更新腳本中的密碼

3. **查看後端日誌**：
   ```bash
   grep "AdminAuthController" app.log | tail -20
   ```

---

### 問題 3：推薦碼 API 失敗

**錯誤訊息**：
```
[5] 取得推薦碼...
ERROR: 遠端伺服器傳回一個錯誤: (500) 內部伺服器錯誤。
```

**原因**：
- Admin 用戶沒有關聯的店家
- `store_user` 表中沒有該 admin 的記錄

**解決方案**：

```sql
-- 檢查 admin 是否有店家
SELECT su.*, s.name 
FROM store_user su
JOIN store s ON su.store_id = s.id
WHERE su.admin_user_id = (SELECT id FROM admin_user WHERE username = 'admin@kuji.com');

-- 如果沒有，創建一個測試店家並關聯
INSERT INTO store (id, name, city, district, address, phone, status, created_at, updated_at)
VALUES (UUID(), '測試店家', '台北市', '中正區', '測試地址', '02-12345678', 'ACTIVE', NOW(), NOW());

INSERT INTO store_user (admin_user_id, store_id, created_at)
VALUES (
    (SELECT id FROM admin_user WHERE username = 'admin@kuji.com'),
    (SELECT id FROM store WHERE name = '測試店家' LIMIT 1),
    NOW()
);
```

---

### 問題 4：忘記密碼功能無法發送郵件

**錯誤訊息**：
```
[8] 忘記密碼請求...
PASS
```

**說明**：
- API 呼叫成功，但郵件未發送

**檢查 SMTP 設定**：

1. **查看 `application.yml`**：
   ```yaml
   spring:
     mail:
       host: smtp.gmail.com
       port: 587
       username: ${GMAIL_USERNAME}
       password: ${GMAIL_APP_PASSWORD}
   ```

2. **設定環境變數**：
   ```batch
   set GMAIL_USERNAME=your-email@gmail.com
   set GMAIL_APP_PASSWORD=your-app-password
   ```

3. **重啟服務**：
   ```bash
   # 停止 Spring Boot
   Ctrl+C

   # 重新啟動
   mvn spring-boot:run
   ```

---

### 問題 5：PowerShell 執行策略錯誤

**錯誤訊息**：
```
無法載入檔案，因為這個系統上已停用指令碼執行。
```

**解決方案**：

```batch
# 以管理員身份開啟 PowerShell
Set-ExecutionPolicy RemoteSigned -Scope CurrentUser

# 或暫時允許
powershell -ExecutionPolicy Bypass -File test-all-apis-powershell.bat
```

---

## 🛠️ 自訂測試

### 修改測試項目

編輯 `test-all-apis-powershell.bat`：

```batch
:: 新增測試：取得所有商品
echo.
echo [16] 取得所有商品...
powershell -Command "try { $r = Invoke-RestMethod -Uri '%BASE_URL%/admin/lottery/list' -Method Post -Body '{}' -Headers @{Authorization='Bearer !ADMIN_TOKEN!'} -ContentType 'application/json'; if ($r.success) { Write-Host 'PASS' -ForegroundColor Green; exit 0 } else { Write-Host 'FAIL' -ForegroundColor Red; exit 1 } } catch { Write-Host 'ERROR:' $_.Exception.Message -ForegroundColor Red; exit 1 }"
if !errorlevel! equ 0 (set /a PASS+=1) else (set /a FAIL+=1)
```

### 修改 BASE_URL

如果後端不在 localhost：

```batch
set BASE_URL=http://18.179.187.129:8080/api
```

---

## 📚 相關文檔

- [API 測試完整指南](./API_TEST_COMPLETE_GUIDE.md)
- [忘記密碼實作文檔](./FORGOT_PASSWORD_IMPLEMENTATION.md)
- [Copilot 指南](../.github/copilot-instructions.md)

---

## 🎯 總結

### ✅ 推薦使用

```batch
test-all-apis-powershell.bat
```

- 穩定可靠
- 彩色輸出
- 完整錯誤處理
- **零配置，開箱即用**

### 測試前檢查清單

- [ ] 後端服務已啟動
- [ ] 資料庫已初始化（有 admin 帳號和角色）
- [ ] 如需測試推薦碼功能，admin 需要關聯店家
- [ ] 如需測試忘記密碼，需設定 SMTP

### 預期結果

- **正常情況**：15 個測試中，至少 10-12 個通過
- **可能失敗的測試**：
  - 推薦碼（admin 未關聯店家）
  - 商品相關（未創建商品）
  - 郵件功能（SMTP 未設定）

---

📅 **最後更新**：2026-01-21  
📧 **問題回報**：請查看 Spring Boot 日誌或開 Issue
