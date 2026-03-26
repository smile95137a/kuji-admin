# KUJI 後台前台 API 完整測試指南

> 📅 最後更新：2025-12-25
> 
> 🎯 目的：驗證所有前台後台 API 功能正常，req/res 格式正確

## 測試腳本說明

### 快速開始

```batch
# 1. 啟動後端服務
mvn spring-boot:run

# 2. 等待服務啟動完成（看到 "Started AdminApplication" 訊息）

# 3. 執行測試腳本
test-all-apis.bat
```

### 腳本功能

- ✅ **後台 API 測試**（8 項）
  - 後台登入與 Token 取得
  - 使用者選單查詢
  - 店家列表查詢
  - 縣市資料查詢
  - 推薦碼查詢
  - 商品與獎品 CRUD

- ✅ **前台 API 測試**（9 項）
  - 前台註冊與登入
  - 忘記密碼流程
  - 行政區資料 API
  - Token 刷新

- ✅ **權限測試**（5 項）
  - 未登入訪問保護 API
  - 前台 token 訪問後台
  - 後台 token 訪問前台
  - 錯誤登入資訊
  - 重複註冊

## 測試覆蓋項目明細

### 第一部分：後台 API（需要 ROLE_ADMIN）

#### 1. 後台登入
```bash
POST /api/admin/auth/login
{
  "username": "admin@kuji.com",
  "password": "admin123"
}

# 預期回應
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGc...",
    "refreshToken": "eyJhbGc...",
    "userId": "uuid-string",
    "username": "admin@kuji.com",
    "roles": ["ROLE_ADMIN"]
  }
}
```

#### 2. 取得使用者選單
```bash
GET /api/admin/users/menu
Authorization: Bearer {ADMIN_TOKEN}

# 預期回應：樹狀選單結構
{
  "success": true,
  "data": [
    {
      "id": "uuid",
      "name": "系統管理",
      "path": "/admin/system",
      "children": [...]
    }
  ]
}
```

#### 3. 取得店家列表
```bash
POST /api/admin/store/list
Authorization: Bearer {ADMIN_TOKEN}
{
  "condition": {},
  "page": 1,
  "size": 20
}

# 預期回應：店家列表
{
  "success": true,
  "data": [
    {
      "id": "uuid",
      "name": "測試店家",
      "city": "台北市",
      "district": "中正區"
    }
  ]
}
```

#### 4. 取得所有縣市（後台版）
```bash
GET /api/admin/district/cities
Authorization: Bearer {ADMIN_TOKEN}

# 預期回應
{
  "success": true,
  "data": ["台北市", "新北市", "桃園市", ...]
}
```

#### 5. 取得推薦碼列表
```bash
GET /api/admin/referral-codes/my-store
Authorization: Bearer {ADMIN_TOKEN}

# 預期回應
{
  "success": true,
  "data": [
    {
      "code": "STORE001",
      "status": "ACTIVE",
      "usageCount": 5
    }
  ]
}
```

#### 6. 創建商品與獎品
```bash
POST /api/admin/lottery-with-prizes
Authorization: Bearer {ADMIN_TOKEN}
{
  "lottery": {
    "title": "測試一番賞",
    "pricePerDraw": 100,
    "category": "OFFICIAL_ICHIBAN",
    "status": "OFF_SHELF"
  },
  "prizes": [
    {
      "name": "A賞",
      "quantity": 1,
      "grade": "A"
    }
  ]
}

# 預期回應
{
  "success": true,
  "data": {
    "id": "lottery-uuid",
    "title": "測試一番賞",
    "prizes": [...]
  }
}
```

#### 7. 查詢商品詳情
```bash
GET /api/admin/lottery-with-prizes/{lotteryId}
Authorization: Bearer {ADMIN_TOKEN}

# 預期回應
{
  "success": true,
  "data": {
    "id": "lottery-uuid",
    "title": "測試一番賞",
    "pricePerDraw": 100,
    "prizes": [...]
  }
}
```

#### 8. 更新商品
```bash
PUT /api/admin/lottery-with-prizes/{lotteryId}
Authorization: Bearer {ADMIN_TOKEN}
{
  "lottery": {
    "title": "測試一番賞（更新）",
    "pricePerDraw": 120
  }
}

# 預期回應
{
  "success": true,
  "data": {
    "id": "lottery-uuid",
    "title": "測試一番賞（更新）",
    "pricePerDraw": 120
  }
}
```

---

### 第二部分：前台 API（無需認證或 ROLE_USER）

#### 1. 前台註冊
```bash
POST /api/auth/register
{
  "email": "test@example.com",
  "password": "Test123",
  "nickname": "測試用戶"
}

# 預期回應
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGc...",
    "userId": "user-uuid",
    "email": "test@example.com"
  }
}
```

#### 2. 前台登入
```bash
POST /api/auth/login
{
  "email": "test@example.com",
  "password": "Test123"
}

# 預期回應
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGc...",
    "refreshToken": "eyJhbGc...",
    "userId": "user-uuid"
  }
}
```

#### 3. 忘記密碼請求
```bash
POST /api/auth/forgot-password
{
  "email": "test@example.com"
}

# 預期回應
{
  "success": true,
  "data": {
    "message": "密碼重設郵件已發送"
  }
}
```

#### 4. 重設密碼（無效 token）
```bash
POST /api/auth/reset-password
{
  "token": "invalid-token",
  "newPassword": "NewPass123",
  "confirmPassword": "NewPass123"
}

# 預期回應（失敗）
{
  "success": false,
  "error": {
    "code": "INVALID_TOKEN",
    "message": "重設連結無效或已過期"
  }
}
```

#### 5. 取得所有縣市（前台版）
```bash
GET /api/district/cities

# 預期回應
{
  "success": true,
  "data": ["台北市", "新北市", "桃園市", ...]
}
```

#### 6. 取得指定縣市的行政區
```bash
GET /api/district/districts/台北市

# 預期回應
{
  "success": true,
  "data": [
    {
      "zipCode": "100",
      "city": "台北市",
      "district": "中正區"
    },
    ...
  ]
}
```

#### 7. 取得行政區樹狀結構
```bash
GET /api/district/tree

# 預期回應
{
  "success": true,
  "data": {
    "台北市": [
      {"zipCode": "100", "district": "中正區"},
      {"zipCode": "103", "district": "大同區"}
    ],
    "新北市": [...]
  }
}
```

#### 8. 查詢指定行政區
```bash
GET /api/district?city=台北市&district=中正區

# 預期回應
{
  "success": true,
  "data": {
    "zipCode": "100",
    "city": "台北市",
    "district": "中正區"
  }
}
```

#### 9. Token 刷新
```bash
POST /api/auth/refresh
{
  "refreshToken": "eyJhbGc..."
}

# 預期回應
{
  "success": true,
  "data": {
    "accessToken": "new-token..."
  }
}
```

---

### 第三部分：權限與錯誤處理測試

#### 1. 未登入訪問後台（應該失敗）
```bash
GET /api/admin/users/menu
# 不帶 Authorization header

# 預期回應（403）
{
  "success": false,
  "error": {
    "code": "UNAUTHORIZED",
    "message": "未授權訪問"
  }
}
```

#### 2. 前台 token 訪問後台（應該失敗）
```bash
GET /api/admin/users/menu
Authorization: Bearer {USER_TOKEN}

# 預期回應（403）
{
  "success": false,
  "error": {
    "code": "FORBIDDEN",
    "message": "權限不足"
  }
}
```

#### 3. 後台 token 訪問前台公開 API（應該成功）
```bash
GET /api/district/cities
Authorization: Bearer {ADMIN_TOKEN}

# 預期回應（成功）
{
  "success": true,
  "data": ["台北市", "新北市", ...]
}
```

#### 4. 錯誤的登入資訊（應該失敗）
```bash
POST /api/admin/auth/login
{
  "username": "admin@kuji.com",
  "password": "wrongpass"
}

# 預期回應
{
  "success": false,
  "error": {
    "code": "INVALID_CREDENTIALS",
    "message": "帳號或密碼錯誤"
  }
}
```

#### 5. 註冊重複 Email（應該失敗）
```bash
POST /api/auth/register
{
  "email": "test@example.com",  # 已存在
  "password": "Test123",
  "nickname": "測試用戶"
}

# 預期回應
{
  "success": false,
  "error": {
    "code": "EMAIL_ALREADY_EXISTS",
    "message": "Email 已被使用"
  }
}
```

---

## 測試結果判定標準

### ✅ PASS 條件

1. **回應格式正確**
   ```json
   {
     "success": true,
     "data": {...},
     "error": null,
     "meta": {
       "timestamp": "2025-12-25T10:00:00",
       "requestId": "uuid"
     }
   }
   ```

2. **HTTP 狀態碼正確**
   - 成功：200 OK
   - 創建：200 OK（專案使用 ResponseEntity.ok()）
   - 錯誤：200 OK（錯誤訊息在 response body 的 success:false）

3. **資料結構正確**
   - Token 回應包含 `accessToken`, `refreshToken`, `userId`
   - 列表回應包含 `data` 陣列
   - 詳情回應包含完整物件

4. **權限驗證正確**
   - 未授權請求返回 `success: false`
   - 前台 token 無法訪問後台
   - 後台 token 可以訪問前台公開 API

### ❌ FAIL 條件

1. **回應格式錯誤**
   - 缺少 `success` 欄位
   - 缺少 `data` 或 `error` 欄位
   - 缺少 `meta` 欄位

2. **業務邏輯錯誤**
   - 應該失敗的請求返回成功
   - 應該成功的請求返回失敗
   - Token 驗證邏輯錯誤

3. **資料錯誤**
   - 返回 `null` 或空物件（除非該資料確實為空）
   - 欄位類型錯誤
   - 缺少必要欄位

---

## 故障排除

### 問題 1：所有測試都失敗

**可能原因**：後端服務未啟動

**解決方案**：
```batch
# 檢查服務是否啟動
netstat -ano | findstr :8080

# 如果沒有輸出，啟動服務
mvn spring-boot:run

# 等待看到以下訊息
# Started AdminApplication in X.XXX seconds
```

---

### 問題 2：後台登入失敗

**可能原因**：資料庫未初始化

**檢查**：
```sql
-- 確認 admin 帳號存在
SELECT * FROM admin_user WHERE username = 'admin@kuji.com';

-- 確認角色存在
SELECT * FROM role WHERE code = 'ROLE_ADMIN';
```

**解決方案**：
- 執行 `DataInitializer` 會在首次啟動時自動建立
- 或手動重置資料庫

---

### 問題 3：前台註冊失敗（Email 已存在）

**原因**：測試腳本使用隨機 Email，不應該重複

**檢查**：
```batch
# 查看腳本中的 timestamp 變數
set "timestamp=%random%"
set "test_email=test!timestamp!@example.com"
```

**解決方案**：
- 重新執行腳本（會生成新的隨機 Email）
- 或手動清理測試資料

---

### 問題 4：忘記密碼 Email 未發送

**可能原因**：SMTP 未設定

**檢查 application.yml**：
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${GMAIL_USERNAME}
    password: ${GMAIL_APP_PASSWORD}
```

**解決方案**：
1. 取得 Gmail App Password：https://myaccount.google.com/apppasswords
2. 設定環境變數：
   ```batch
   set GMAIL_USERNAME=your-email@gmail.com
   set GMAIL_APP_PASSWORD=xxxx-xxxx-xxxx-xxxx
   ```
3. 重啟服務

---

### 問題 5：權限測試失敗

**檢查點**：

1. **SecurityConfig 路由順序**
   ```java
   @Bean
   @Order(1)
   public SecurityFilterChain adminSecurityFilterChain(...) {
       http.securityMatcher("/admin/**")...
   }
   
   @Bean
   @Order(2)
   public SecurityFilterChain apiSecurityFilterChain(...) {
       http.securityMatcher("/api/**")...
   }
   ```

2. **UserPrincipal roles 格式**
   ```java
   // ✅ 正確
   principal.getRoles(); // ["ROLE_ADMIN"]
   
   // ❌ 錯誤
   principal.getRoles(); // ["ADMIN"]
   ```

3. **Filter 路徑匹配**
   ```java
   // ✅ 正確
   String path = request.getServletPath();
   
   // ❌ 錯誤
   String path = request.getRequestURI(); // 包含 context-path
   ```

---

## 測試報告範例

### 成功範例
```
================================
  KUJI 後台前台 API 完整測試
================================

========================================
  第一部分：後台 API 測試
========================================

[測試] 後台登入
請求: POST http://localhost:8080/api/admin/auth/login
✓ PASS

[測試] 取得使用者選單
請求: GET http://localhost:8080/api/admin/users/menu
✓ PASS

... (略)

========================================
  測試結果摘要
========================================

通過: 22
失敗: 0

========================================
  🎉 所有測試通過！
========================================
```

### 失敗範例
```
[測試] 後台登入
請求: POST http://localhost:8080/api/admin/auth/login
✗ FAIL
{"success":false,"error":{"code":"INVALID_CREDENTIALS","message":"帳號或密碼錯誤"}}

[測試] 取得使用者選單
請求: GET http://localhost:8080/api/admin/users/menu
✗ FAIL
{"success":false,"error":{"code":"UNAUTHORIZED","message":"未授權訪問"}}

========================================
  測試結果摘要
========================================

通過: 18
失敗: 4

========================================
  以下測試失敗：
========================================
✗ 後台登入
✗ 取得使用者選單
✗ 創建商品與獎品
✗ 查詢商品詳情
```

---

## 手動測試（使用 curl）

如果需要單獨測試某個 API：

### 1. 取得 Token
```batch
curl -X POST http://localhost:8080/api/admin/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"admin@kuji.com\",\"password\":\"admin123\"}"
```

### 2. 使用 Token 訪問 API
```batch
set TOKEN=eyJhbGc...

curl -X GET http://localhost:8080/api/admin/users/menu ^
  -H "Authorization: Bearer %TOKEN%"
```

### 3. 測試忘記密碼流程
```batch
# Step 1: 請求重設密碼
curl -X POST http://localhost:8080/api/auth/forgot-password ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"test@example.com\"}"

# Step 2: 從資料庫查詢 token
mysql -u root -p kuji_db -e "SELECT password_reset_token FROM user WHERE email='test@example.com';"

# Step 3: 使用 token 重設密碼
curl -X POST http://localhost:8080/api/auth/reset-password ^
  -H "Content-Type: application/json" ^
  -d "{\"token\":\"從資料庫取得的token\",\"newPassword\":\"NewPass123\",\"confirmPassword\":\"NewPass123\"}"
```

---

## 整合測試清單

- [ ] 後端服務啟動正常
- [ ] 後台登入成功並取得 Token
- [ ] 後台選單查詢返回樹狀結構
- [ ] 店家列表查詢正常
- [ ] 縣市資料 API 正常
- [ ] 商品 CRUD 功能正常
- [ ] 前台註冊成功並取得 Token
- [ ] 前台登入成功
- [ ] 忘記密碼流程正常（Email 功能需 SMTP 設定）
- [ ] 行政區 API 全部正常
- [ ] Token 刷新功能正常
- [ ] 權限驗證正確（後台 token 無法越權）
- [ ] 錯誤處理正確（返回適當的錯誤訊息）

---

## 附錄：完整 API 列表

### 後台 API（需要 ROLE_ADMIN）

| 方法 | 路徑 | 說明 |
|------|------|------|
| POST | /api/admin/auth/login | 後台登入 |
| GET | /api/admin/users/menu | 取得使用者選單 |
| POST | /api/admin/store/list | 取得店家列表 |
| GET | /api/admin/district/cities | 取得所有縣市 |
| GET | /api/admin/referral-codes/my-store | 取得推薦碼 |
| POST | /api/admin/lottery-with-prizes | 創建商品與獎品 |
| GET | /api/admin/lottery-with-prizes/{id} | 查詢商品詳情 |
| PUT | /api/admin/lottery-with-prizes/{id} | 更新商品 |

### 前台 API（公開或 ROLE_USER）

| 方法 | 路徑 | 說明 |
|------|------|------|
| POST | /api/auth/register | 前台註冊 |
| POST | /api/auth/login | 前台登入 |
| POST | /api/auth/forgot-password | 忘記密碼 |
| POST | /api/auth/reset-password | 重設密碼 |
| POST | /api/auth/refresh | Token 刷新 |
| GET | /api/district/cities | 取得所有縣市 |
| GET | /api/district/districts/{city} | 取得指定縣市的行政區 |
| GET | /api/district/tree | 取得行政區樹狀結構 |
| GET | /api/district | 查詢指定行政區 |

---

📝 **備註**：
- 測試腳本會自動判斷成功/失敗並統計結果
- 如果所有測試通過，腳本會返回 exit code 0
- 如果有任何測試失敗，腳本會返回 exit code 1，並列出失敗項目
- 建議在部署前執行此測試腳本確保系統穩定
