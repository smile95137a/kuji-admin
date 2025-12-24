# 🚨 緊急測試指南 - 解決 403 問題

## 📋 當前狀態
- ✅ 可以登入：`POST /api/admin/auth/login`
- ❌ 所有其他 API：403 Forbidden
- 🔧 已修改：`ApiJwtAuthenticationFilter` 加入詳細日誌追蹤

## 🔄 立即執行步驟

### 步驟 1：停止當前應用
在 VS Code Terminal 找到正在運行的應用程式，按 `Ctrl + C` 停止

### 步驟 2：重新啟動應用
```bash
mvn spring-boot:run
```

或者使用 JAR：
```bash
java -jar target/admin-1.0.0.jar
```

### 步驟 3：等待啟動完成
看到這行表示啟動成功：
```
Started AdminApplication in X.XXX seconds
```

### 步驟 4：測試 API（使用 Postman）

#### 4.1 登入取得 Token
```http
POST http://localhost:8080/api/admin/auth/login
Content-Type: application/json

{
  "username": "admin@kuji.com",
  "password": "admin123"
}
```

**預期回應：**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGc...",
    "user": {
      "roles": ["ROLE_ADMIN"]
    }
  }
}
```

#### 4.2 訪問選單 API
```http
GET http://localhost:8080/api/admin/menus/accessible
Authorization: Bearer {你的 accessToken}
```

**注意：** 
- Header 名稱是 `Authorization`
- 值是 `Bearer ` + token（Bearer 後面有一個空格）
- 例如：`Bearer eyJhbGciOiJIUzI1NiJ9...`

## 🔍 檢查日誌輸出

重啟後，當你訪問 API 時，應該會看到以下日誌：

### ✅ 成功的情況
```
🔍 [ApiJwtAuthenticationFilter] 收到請求: /api/admin/menus/accessible
🔐 [ApiJwtAuthenticationFilter] 開始處理認證: /api/admin/menus/accessible
🎫 [ApiJwtAuthenticationFilter] Token: eyJhbGciOiJIUzI1NiJ9...
👤 [ApiJwtAuthenticationFilter] 使用者: admin@kuji.com, 類型: admin
🔑 [ApiJwtAuthenticationFilter] 執行後台管理員認證: admin@kuji.com
🔍 [Admin Auth] 開始查詢後台管理員: admin@kuji.com
✅ [Admin Auth] 找到管理員: admin@kuji.com (ID: xxx)
🎭 [Admin Auth] 找到 1 個角色關聯
  ↳ 角色: 系統管理員 (Code: ROLE_ADMIN)
✅ [Admin Auth] 後台管理員認證成功: admin@kuji.com (角色: [ADMIN])
```

### ❌ 失敗的情況會顯示
```
⚠️  [ApiJwtAuthenticationFilter] 未提供 Token
⚠️  [ApiJwtAuthenticationFilter] Token 驗證失敗
⚠️  [ApiJwtAuthenticationFilter] 無法取得使用者 Email
❌ [Admin Auth] 後台管理員不存在: admin@kuji.com
```

## 🐛 如果還是 403

### 檢查清單

#### 1. 確認應用程式已重啟
```bash
# 檢查最後編譯時間
dir target\admin-1.0.0.jar
```

#### 2. 查看完整啟動日誌
```bash
# 查看最近 100 行日誌
powershell -Command "Get-Content app.log -Tail 100"
```

#### 3. 確認 Token 格式正確
**在 Postman 中：**
- Tab: `Authorization`
- Type: `Bearer Token`
- Token: 貼上你的 accessToken（不要包含 "Bearer " 前綴）

或者使用 Header：
- Key: `Authorization`
- Value: `Bearer eyJhbGc...`（注意 Bearer 後面有空格）

#### 4. 檢查 Token 是否過期
Token payload 中的 `exp` 欄位：
```json
{
  "exp": 1766648471  // Unix timestamp
}
```

用這個網站檢查：https://www.unixtimestamp.com/
或在 Terminal：
```bash
powershell -Command "[DateTimeOffset]::FromUnixTimeSeconds(1766648471).LocalDateTime"
```

#### 5. 驗證 JWT Token 內容
訪問：https://jwt.io/
貼上你的 token，確認：
- ✅ `userType: "admin"` 存在
- ✅ `sub` 或 email 正確
- ✅ `exp` 還沒過期

## 📝 常見錯誤排查

### 錯誤 1：Token 沒有 `userType` 欄位
**症狀：** 日誌顯示 `類型: null`

**解決：** 檢查 `AdminAuthController.login()` 是否正確生成 token：
```java
String token = jwtUtil.generateToken(adminUser.getUsername(), "admin");
```

### 錯誤 2：SecurityConfig 沒有允許 ROLE_ADMIN
**症狀：** 日誌顯示認證成功，但還是 403

**檢查：** `SecurityConfig.java` 的 `/api/**` 設定：
```java
.requestMatchers("/api/**").hasAnyRole("USER", "Admin", "StoreOwner", "StoreEditor")
```

### 錯誤 3：角色名稱不匹配
**症狀：** 日誌顯示 `角色: [ADMIN]`，但 SecurityConfig 要求 `Admin`

**原因：** Spring Security 的角色比對是大小寫敏感的！

**檢查資料庫：**
```sql
SELECT * FROM role WHERE code = 'ROLE_ADMIN';
```

應該回傳：
- `code`: `ROLE_ADMIN`
- `name`: 不限（顯示用）

## 🎯 最終測試步驟

1. ✅ 重啟應用程式
2. ✅ 登入取得新的 token
3. ✅ 複製完整的 accessToken
4. ✅ 在 Postman 設定 Authorization: Bearer {token}
5. ✅ 呼叫 `GET /api/admin/menus/accessible`
6. ✅ 查看應用程式日誌（不是 Postman 的回應）
7. ✅ 將日誌完整貼給我看

## 📞 回報格式

如果還是失敗，請提供：

```
1. 登入回應（完整 JSON）
2. Token 內容（去 jwt.io 解析後的 payload）
3. 訪問 API 的請求 Header
4. 應用程式日誌（從 "收到請求" 開始的完整日誌）
5. 最終的錯誤訊息
```

---

**重要提醒：**
- 🔴 每次修改代碼後，**必須**重啟應用
- 🔴 每次重啟後，**必須**重新登入取得新 token
- 🔴 Postman 的 Token 要確實更新成新的

**目前時間：** 2025-12-24 15:45
**Token 有效期：** 24 小時
**預期 Token 到期時間：** 2025-12-25 15:41 左右

如果超過這個時間，token 會過期！
