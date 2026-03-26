# 前台忘記密碼功能測試指南

## ✅ 已完成功能

### 1. 忘記密碼/重設密碼 API
- `POST /api/auth/forgot-password` - 請求重設密碼（發送郵件）
- `POST /api/auth/reset-password` - 使用 token 重設密碼

### 2. 郵件服務與模板
- ✅ Thymeleaf 郵件模板
  - `verification-email.html` - 註冊驗證碼郵件
  - `password-reset-email.html` - 密碼重設郵件
- ✅ EmailService 整合 Thymeleaf

### 3. 縣市資料 API
- `GET /api/district/cities` - 取得所有縣市
- `GET /api/district/districts/{city}` - 取得指定縣市的鄉鎮市區
- `GET /api/district/tree` - 取得樹狀結構
- `GET /api/district/all` - 取得所有行政區
- `GET /api/district?city=XXX&district=XXX` - 查詢指定行政區

---

## 📧 1. 忘記密碼流程測試

### Step 1: 請求重設密碼

**請求：**
```http
POST http://localhost:8080/api/auth/forgot-password
Content-Type: application/json

{
  "email": "user@example.com"
}
```

**成功回應：**
```json
{
  "success": true,
  "data": {
    "message": "如果此 Email 已註冊，將會收到密碼重設郵件"
  },
  "error": null,
  "meta": {
    "timestamp": "2026-01-21T...",
    "requestId": "uuid"
  }
}
```

**後端行為：**
1. 查詢 `user` 表的 `email` 欄位
2. 如果使用者存在且 `provider='EMAIL'`：
   - 生成 UUID 作為 `password_reset_token`
   - 設定 `password_reset_expires` 為 1 小時後
   - 更新使用者記錄
   - 呼叫 `emailService.sendPasswordResetEmail()`
3. 發送 HTML 郵件（使用 Thymeleaf 模板）

---

### Step 2: 檢查郵件

**郵件內容包含：**
- 📧 主旨：`[KUJI 一番賞] 密碼重設請求`
- 🔗 重設連結：`http://localhost:3000/reset-password?token=<uuid>`
- 🔑 驗證碼：UUID（可複製）
- ⏰ 有效期限：30 分鐘

**郵件範例預覽：**
```html
您好，用戶名稱！

我們收到了您的密碼重設請求。請點擊下方按鈕來重設您的密碼：

[重設密碼] (按鈕連結到前端)

如果按鈕無法使用，請複製以下驗證碼手動完成密碼重設：
┌─────────────────────────────┐
│ abc123-def456-ghi789-jkl012 │
└─────────────────────────────┘

⚠️ 重要提醒：
• 此驗證碼將在 30 分鐘後失效
• 如果您沒有要求重設密碼，請忽略此郵件
• 請勿將此驗證碼分享給任何人
```

---

### Step 3: 重設密碼

**請求：**
```http
POST http://localhost:8080/api/auth/reset-password
Content-Type: application/json

{
  "token": "abc123-def456-ghi789-jkl012",
  "newPassword": "NewPass123",
  "confirmPassword": "NewPass123"
}
```

**驗證規則：**
- `newPassword` 長度 8-20 字元
- 必須包含大小寫字母和數字
- `newPassword` 與 `confirmPassword` 必須一致

**成功回應：**
```json
{
  "success": true,
  "data": {
    "message": "密碼重設成功，請使用新密碼登入"
  },
  "error": null
}
```

**失敗回應（token 過期）：**
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "BUSINESS_ERROR",
    "message": "重設連結無效或已過期",
    "details": null
  }
}
```

---

## 🗺️ 2. 縣市資料 API 測試

### 2.1 取得所有縣市

```http
GET http://localhost:8080/api/district/cities
```

**回應：**
```json
{
  "success": true,
  "data": [
    "台北市",
    "新北市",
    "桃園市",
    "台中市",
    "台南市",
    "高雄市",
    ...
  ]
}
```

---

### 2.2 取得指定縣市的行政區

```http
GET http://localhost:8080/api/district/districts/台北市
```

**回應：**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "city": "台北市",
      "district": "中正區",
      "zipCode": "100"
    },
    {
      "id": 2,
      "city": "台北市",
      "district": "大同區",
      "zipCode": "103"
    },
    ...
  ]
}
```

---

### 2.3 取得樹狀結構（適合下拉選單）

```http
GET http://localhost:8080/api/district/tree
```

**回應：**
```json
{
  "success": true,
  "data": {
    "台北市": [
      {"id": 1, "city": "台北市", "district": "中正區", "zipCode": "100"},
      {"id": 2, "city": "台北市", "district": "大同區", "zipCode": "103"}
    ],
    "新北市": [
      {"id": 23, "city": "新北市", "district": "板橋區", "zipCode": "220"},
      ...
    ]
  }
}
```

---

### 2.4 查詢指定行政區

```http
GET http://localhost:8080/api/district?city=台北市&district=中正區
```

**回應：**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "city": "台北市",
    "district": "中正區",
    "zipCode": "100"
  }
}
```

---

## 🧪 完整測試流程（curl 命令）

### 測試 1：忘記密碼完整流程

```bash
# Step 1: 先註冊一個測試帳號
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123",
    "nickname": "測試用戶"
  }'

# Step 2: 請求重設密碼
curl -X POST http://localhost:8080/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com"
  }'

# Step 3: 檢查郵件（從 email_log 表或 SMTP 伺服器）
# 取得 password_reset_token

# Step 4: 使用 token 重設密碼
curl -X POST http://localhost:8080/api/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{
    "token": "<從郵件或 DB 取得的 token>",
    "newPassword": "NewPass123",
    "confirmPassword": "NewPass123"
  }'

# Step 5: 使用新密碼登入
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "NewPass123"
  }'
```

---

### 測試 2：縣市資料 API

```bash
# 取得所有縣市
curl http://localhost:8080/api/district/cities

# 取得台北市的行政區
curl http://localhost:8080/api/district/districts/台北市

# 取得樹狀結構
curl http://localhost:8080/api/district/tree

# 查詢指定行政區
curl "http://localhost:8080/api/district?city=台北市&district=中正區"
```

---

## ⚙️ SMTP 設定（Gmail）

確保 `application-dev.yml` 或 `application-prod.yml` 有正確設定：

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${GMAIL_USERNAME}  # your-email@gmail.com
    password: ${GMAIL_APP_PASSWORD}  # 應用程式密碼（非帳號密碼）
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true

app:
  frontend-url: http://localhost:3000  # 前端網址
```

**取得 Gmail 應用程式密碼：**
1. 登入 Google 帳戶
2. 前往「管理您的 Google 帳戶」
3. 點選「安全性」
4. 啟用「兩步驟驗證」
5. 點選「應用程式密碼」
6. 選擇「郵件」和「其他」，輸入「KUJI Server」
7. 複製產生的 16 位密碼

**設定環境變數（Windows）：**
```cmd
set GMAIL_USERNAME=your-email@gmail.com
set GMAIL_APP_PASSWORD=xxxx-xxxx-xxxx-xxxx
```

---

## 📊 資料庫檢查

### 檢查 user 表的 password_reset_token

```sql
SELECT 
  id, 
  email, 
  password_reset_token, 
  password_reset_expires,
  created_at,
  updated_at
FROM user
WHERE email = 'test@example.com';
```

### 檢查郵件發送記錄

```sql
SELECT 
  id,
  email_type,
  to_email,
  subject,
  status,
  sent_at,
  error_message,
  created_at
FROM email_log
WHERE email_type = 'PASSWORD_RESET'
ORDER BY created_at DESC
LIMIT 10;
```

---

## 🐛 常見問題排除

### 問題 1：郵件未發送
**檢查：**
- SMTP 設定是否正確
- Gmail 應用程式密碼是否有效
- 查看 `email_log` 表的 `status` 和 `error_message`

### 問題 2：Token 無效或過期
**原因：**
- `password_reset_expires` 已超過當前時間
- Token 已被使用（重設後會清除）

**解決：**
- 重新請求忘記密碼
- 確認在 30 分鐘內完成重設

### 問題 3：Google 登入帳號無法重設密碼
**設計：**
- 只有 `provider='EMAIL'` 的帳號才能重設密碼
- Google 登入帳號請使用 Google 的密碼管理功能

---

## 📝 前端整合範例

### React 忘記密碼頁面

```jsx
import { useState } from 'react';
import axios from 'axios';

function ForgotPasswordPage() {
  const [email, setEmail] = useState('');
  const [message, setMessage] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const response = await axios.post('/api/auth/forgot-password', { email });
      setMessage(response.data.message);
    } catch (error) {
      setMessage(error.response?.data?.error || '發送失敗');
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <input
        type="email"
        placeholder="請輸入註冊的 Email"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        required
      />
      <button type="submit">發送重設郵件</button>
      {message && <p>{message}</p>}
    </form>
  );
}
```

### React 重設密碼頁面

```jsx
import { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import axios from 'axios';

function ResetPasswordPage() {
  const [searchParams] = useSearchParams();
  const [token, setToken] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [message, setMessage] = useState('');

  useEffect(() => {
    const tokenFromUrl = searchParams.get('token');
    if (tokenFromUrl) setToken(tokenFromUrl);
  }, [searchParams]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (newPassword !== confirmPassword) {
      setMessage('兩次輸入的密碼不一致');
      return;
    }

    try {
      const response = await axios.post('/api/auth/reset-password', {
        token,
        newPassword,
        confirmPassword
      });
      setMessage(response.data.message);
      // 3 秒後跳轉到登入頁
      setTimeout(() => window.location.href = '/login', 3000);
    } catch (error) {
      setMessage(error.response?.data?.error || '重設失敗');
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <input
        type="password"
        placeholder="新密碼（8-20字元，含大小寫英數）"
        value={newPassword}
        onChange={(e) => setNewPassword(e.target.value)}
        required
      />
      <input
        type="password"
        placeholder="確認密碼"
        value={confirmPassword}
        onChange={(e) => setConfirmPassword(e.target.value)}
        required
      />
      <button type="submit">重設密碼</button>
      {message && <p>{message}</p>}
    </form>
  );
}
```

---

## ✅ 測試檢查清單

- [ ] 註冊新帳號成功
- [ ] 請求忘記密碼（已註冊的 Email）
- [ ] 收到密碼重設郵件
- [ ] 郵件內容正確（token、連結、有效期限）
- [ ] 使用正確 token 重設密碼成功
- [ ] 使用過期 token 重設失敗
- [ ] 使用新密碼登入成功
- [ ] Google 登入帳號無法重設密碼（預期行為）
- [ ] 縣市資料 API 正常回傳
- [ ] 樹狀結構正確

---

## 🎉 完成！

三個需求已全部實作完成：
1. ✅ 前台忘記密碼功能（含郵件發送）
2. ✅ Mail Service 和 Mail Template（Thymeleaf）
3. ✅ 縣市資料 API（`/api/district/**`）

**重啟應用後即可測試！**
