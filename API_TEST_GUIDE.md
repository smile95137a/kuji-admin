# KUJI Admin System - API 測試指南

## 🚀 啟動專案

### 方法 1: 從 IDE 直接運行（推薦）

1. 在 VS Code 或 IntelliJ IDEA 中開啟 `AdminApplication.java`
2. 點擊 `Run` 按鈕或使用快捷鍵啟動
3. 等待看到日誌：
```
Started AdminApplication in X.XXX seconds
開始執行系統資料初始化...
✓ 角色資料初始化完成（3 筆）
✓ 選單資料初始化完成（19 筆）
...
系統資料初始化完成！
```

### 方法 2: 使用 Maven 命令

```bash
mvn spring-boot:run -Pdev
```

### 方法 3: 使用打包的 JAR

```bash
mvn clean package -DskipTests
java -Dspring.profiles.active=dev -jar target/admin-1.0.0.jar
```

---

## 📋 測試端點列表

基礎 URL: `http://localhost:8080/api`

### 1. 健康檢查

**端點:** `GET /test/health`

**說明:** 檢查系統是否正常運行

**cURL:**
```bash
curl http://localhost:8080/api/test/health
```

**預期回應:**
```json
{
  "status": "UP",
  "timestamp": "2025-12-21T23:30:00",
  "message": "KUJI Admin System is running!"
}
```

---

### 2. 資料庫連接測試

**端點:** `GET /test/db-check`

**說明:** 檢查資料庫連接並返回各表統計資料

**cURL:**
```bash
curl http://localhost:8080/api/test/db-check
```

**預期回應:**
```json
{
  "status": "SUCCESS",
  "database": "Connected",
  "statistics": {
    "roles": 3,
    "menus": 19,
    "adminUsers": 4,
    "stores": 2,
    "users": 3,
    "lotteries": 2
  }
}
```

---

### 3. 查詢所有角色

**端點:** `GET /test/roles`

**cURL:**
```bash
curl http://localhost:8080/api/test/roles
```

**預期回應:**
```json
[
  {
    "id": "uuid-xxx",
    "name": "系統管理員",
    "code": "ROLE_ADMIN",
    "description": "平台最高權限管理者，可管理所有店家與系統設定",
    "createdAt": "2025-12-21T23:30:00",
    "updatedAt": "2025-12-21T23:30:00"
  },
  ...
]
```

---

### 4. 查詢所有選單

**端點:** `GET /test/menus`

**cURL:**
```bash
curl http://localhost:8080/api/test/menus
```

---

### 5. 查詢所有管理員

**端點:** `GET /test/admin-users`

**cURL:**
```bash
curl http://localhost:8080/api/test/admin-users
```

**注意:** 密碼欄位會被遮罩為 `******`

---

### 6. 查詢所有店家

**端點:** `GET /test/stores`

**cURL:**
```bash
curl http://localhost:8080/api/test/stores
```

---

### 7. 查詢所有會員

**端點:** `GET /test/users`

**cURL:**
```bash
curl http://localhost:8080/api/test/users
```

---

### 8. 查詢所有商品

**端點:** `GET /test/lotteries`

**cURL:**
```bash
curl http://localhost:8080/api/test/lotteries
```

---

### 9. 測試管理員登入

**端點:** `POST /test/admin-login`

**說明:** 測試管理員登入功能

**cURL:**
```bash
curl -X POST http://localhost:8080/api/test/admin-login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin@kuji.com",
    "password": "admin123"
  }'
```

**預期回應:**
```json
{
  "status": "SUCCESS",
  "message": "登入成功",
  "data": {
    "accessToken": "eyJhbGc...",
    "refreshToken": "eyJhbGc...",
    "expiresIn": 3600,
    "tokenType": "Bearer",
    "adminUser": {
      "id": "uuid-xxx",
      "username": "admin@kuji.com",
      "displayName": "系統管理員",
      "email": "admin@kuji.com"
    }
  }
}
```

---

### 10. 取得預設測試帳號

**端點:** `GET /test/default-accounts`

**說明:** 返回所有預設測試帳號資訊

**cURL:**
```bash
curl http://localhost:8080/api/test/default-accounts
```

**預期回應:**
```json
{
  "adminAccounts": [
    {
      "username": "admin@kuji.com",
      "password": "admin123",
      "role": "系統管理員"
    },
    {
      "username": "owner@teststore.com",
      "password": "Test1234",
      "role": "店家負責人"
    },
    ...
  ],
  "userAccounts": [...],
  "stores": [...],
  "lotteries": [...]
}
```

---

### 11. 系統資訊

**端點:** `GET /test/system-info`

**cURL:**
```bash
curl http://localhost:8080/api/test/system-info
```

**預期回應:**
```json
{
  "applicationName": "KUJI Admin System",
  "version": "1.0.0",
  "javaVersion": "21.0.x",
  "os": "Windows 11 ...",
  "timestamp": "2025-12-21T23:30:00",
  "profiles": "dev"
}
```

---

## 🧪 完整測試流程

### 1. 啟動專案並驗證

```bash
# 1. 健康檢查
curl http://localhost:8080/api/test/health

# 2. 資料庫檢查
curl http://localhost:8080/api/test/db-check

# 3. 系統資訊
curl http://localhost:8080/api/test/system-info
```

### 2. 驗證初始資料

```bash
# 查詢角色（應該有 3 筆）
curl http://localhost:8080/api/test/roles

# 查詢選單（應該有 19 筆）
curl http://localhost:8080/api/test/menus

# 查詢管理員（應該有 4 筆）
curl http://localhost:8080/api/test/admin-users

# 查詢店家（應該有 2 筆）
curl http://localhost:8080/api/test/stores

# 查詢會員（應該有 3 筆）
curl http://localhost:8080/api/test/users

# 查詢商品（應該有 2 筆）
curl http://localhost:8080/api/test/lotteries
```

### 3. 測試登入功能

```bash
# 測試管理員登入
curl -X POST http://localhost:8080/api/test/admin-login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin@kuji.com",
    "password": "admin123"
  }'

# 測試店家負責人登入
curl -X POST http://localhost:8080/api/test/admin-login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "owner@teststore.com",
    "password": "Test1234"
  }'
```

---

## 🎯 使用 Postman 測試

### 匯入 Collection

建立新的 Collection：`KUJI Admin API Tests`

### 設定環境變數

- `baseUrl`: `http://localhost:8080/api`
- `accessToken`: 登入後取得的 token

### 測試步驟

1. **健康檢查**
   - GET `{{baseUrl}}/test/health`

2. **資料庫檢查**
   - GET `{{baseUrl}}/test/db-check`

3. **查詢預設帳號**
   - GET `{{baseUrl}}/test/default-accounts`

4. **測試登入**
   - POST `{{baseUrl}}/test/admin-login`
   - Body (JSON):
     ```json
     {
       "username": "admin@kuji.com",
       "password": "admin123"
     }
     ```
   - 從回應中取得 `accessToken`，保存到環境變數

5. **查詢各項資料**
   - GET `{{baseUrl}}/test/roles`
   - GET `{{baseUrl}}/test/menus`
   - GET `{{baseUrl}}/test/admin-users`
   - GET `{{baseUrl}}/test/stores`
   - GET `{{baseUrl}}/test/users`
   - GET `{{baseUrl}}/test/lotteries`

---

## 🐛 常見問題排查

### 1. 連接資料庫失敗

**檢查項目:**
- MySQL 服務是否啟動
- 資料庫 `kuji` 是否已建立
- `application-dev.yml` 中的連接資訊是否正確

### 2. 初始資料未載入

**檢查項目:**
- 查看啟動日誌是否有 "開始執行系統資料初始化..."
- 執行 `/test/db-check` 查看資料數量
- 如果資料已存在，DataInitializer 會跳過初始化

### 3. Mapper XML 重複載入錯誤

**已解決:** 在 `application.yml` 中已關閉 DevTools restart 功能

### 4. 無法從 IDE 啟動

**檢查項目:**
- 確認 Java 21 已正確安裝
- 確認 Maven 依賴已正確下載
- 執行 `mvn clean compile` 確保編譯成功

---

## 📊 Swagger UI 測試

啟動專案後，可以使用 Swagger UI 進行互動式測試：

**URL:** `http://localhost:8080/api/swagger-ui/index.html`

在 Swagger UI 中可以：
- 查看所有 API 端點
- 直接測試 API
- 查看請求/回應格式
- 測試不同的參數組合

---

## ✅ 成功啟動檢查清單

- [ ] AdminApplication 成功啟動（無錯誤日誌）
- [ ] 看到 "系統資料初始化完成！" 訊息
- [ ] `/test/health` 返回 `status: UP`
- [ ] `/test/db-check` 顯示正確的統計資料
- [ ] 所有角色、選單、管理員、店家、會員、商品都已初始化
- [ ] 管理員登入測試成功
- [ ] Swagger UI 可以正常訪問

---

## 📝 預設測試帳號總覽

### 後台管理者

| 帳號 | 密碼 | 角色 | 說明 |
|------|------|------|------|
| admin@kuji.com | admin123 | 系統管理員 | 最高權限 |
| owner@teststore.com | Test1234 | 店家負責人 | KUJI 測試商店 |
| owner2@teststore.com | Test1234 | 店家負責人 | 動漫周邊專賣店 |
| editor@teststore.com | Test1234 | 店家編輯 | KUJI 測試商店小編 |

### 前台會員

| 帳號 | 密碼 | 金點 | 紅利 |
|------|------|------|------|
| user1@test.com | Test1234 | 1000 | 500 |
| user2@test.com | Test1234 | 2500 | 300 |
| googleuser@gmail.com | Google OAuth | 500 | 100 |

---

## 🎉 開始測試

現在您可以：

1. **從 IDE 啟動 AdminApplication**
2. **執行上述測試命令驗證系統功能**
3. **使用 Postman 或 Swagger UI 進行完整測試**
4. **開始開發您的業務邏輯！**

祝您測試順利！🚀
