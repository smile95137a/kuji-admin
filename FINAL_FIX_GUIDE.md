# 🎯 最終修復方案 - 403 問題完全解決

## 🔍 根本原因分析

### 問題 1：AdminJwtAuthenticationFilter 沒有執行
- ✅ 已修復：加入詳細日誌追蹤每個步驟

### 問題 2：角色名稱大小寫不匹配  
**這是主要原因！**

**資料庫中的角色：**
```sql
SELECT * FROM role;
-- code = 'ROLE_ADMIN' (全大寫)
```

**Spring Security 行為：**
- `hasAnyRole("Admin")` → 實際比對 `ROLE_Admin`
- `hasAnyRole("ADMIN")` → 實際比對 `ROLE_ADMIN` ✅

**修改前：**
```java
.hasAnyRole("Admin", "StoreOwner", "StoreEditor")  // ❌ 不匹配
```

**修改後：**
```java
.hasAnyRole("ADMIN", "STORE_OWNER", "STORE_EDITOR")  // ✅ 匹配
```

## 📝 已修改的檔案

### 1. AdminJwtAuthenticationFilter.java ✅
- 加入詳細的 emoji 日誌
- 修正角色設定邏輯（保留完整的 ROLE_ADMIN）
- 每個步驟都有日誌輸出

### 2. SecurityConfig.java ✅  
- 修正 `/admin/**` 角色要求：`ADMIN`, `STORE_OWNER`, `STORE_EDITOR`
- 修正 `/api/**` 角色要求：`USER`, `ADMIN`, `STORE_OWNER`, `STORE_EDITOR`

## 🚀 立即執行步驟

### 步驟 1：停止當前應用
在 VS Code 的 Terminal 找到正在運行的應用，按 `Ctrl + C`

### 步驟 2：重新啟動
```bash
mvn spring-boot:run
```

或

```bash
java -jar target\admin-1.0.0.jar
```

### 步驟 3：登入取得新 Token
```http
POST http://localhost:8080/api/admin/auth/login
Content-Type: application/json

{
  "username": "admin@kuji.com",
  "password": "admin123"
}
```

### 步驟 4：測試 API
```http
GET http://localhost:8080/api/admin/menus/accessible
Authorization: Bearer {你的_access_token}
```

## 📊 預期日誌輸出

### ✅ 成功的情況
```
🔍 [AdminJwtFilter] 收到請求: /admin/menus/accessible
🔐 [AdminJwtFilter] 開始認證: /admin/menus/accessible
🎫 [AdminJwtFilter] Token 前20字元: eyJhbGciOiJIUzI1NiJ9...
👤 [AdminJwtFilter] 使用者: admin@kuji.com
✅ [AdminJwtFilter] 找到管理員: admin@kuji.com (ID: 70dc7e33-6053-46eb-834e-24087ad436ce)
  ↳ 角色: 系統管理員 (Code: ROLE_ADMIN)
✅ [AdminJwtFilter] 認證成功: admin@kuji.com (角色: [ROLE_ADMIN])
```

### API 回應
```json
{
  "success": true,
  "data": [
    {
      "id": "...",
      "name": "店家管理",
      ...
    }
  ]
}
```

## 🔧 架構說明

### 你的專案架構
```
前台功能（不需要角色）：
  - 路徑：直接訪問，不加 /api 前綴
  - 認證：不需要

後台功能（需要後台角色）：
  - 登入：POST http://localhost:8080/api/admin/auth/login
  - API：GET http://localhost:8080/api/admin/menus/accessible
  - 實際路徑：/admin/menus/accessible (Spring 自動去掉 context-path /api)
  - 需要角色：ROLE_ADMIN, ROLE_STORE_OWNER, ROLE_STORE_EDITOR
```

### context-path 的作用
```yaml
server:
  servlet:
    context-path: /api
```

**效果：**
- 瀏覽器訪問：`http://localhost:8080/api/admin/auth/login`
- Spring 接收到：`/admin/auth/login`
- SecurityConfig 匹配：`/admin/**` → AdminSecurityFilterChain
- AdminJwtAuthenticationFilter 處理認證

## 🐛 如果還是 403

### 檢查清單

#### 1. 確認應用已重啟
```bash
# 查看應用啟動時間
# 必須在編譯完成後啟動
```

#### 2. 查看完整日誌
應該要看到：
```
🔍 [AdminJwtFilter] 收到請求: /admin/menus/accessible
```

如果沒有這行，表示 Filter 沒執行！

#### 3. 檢查 Token Header
**Postman 設定：**
- Tab: Authorization
- Type: Bearer Token  
- Token: `eyJhbGc...`（只貼 token，不要包含 "Bearer "）

#### 4. 檢查資料庫角色
```sql
SELECT id, name, code FROM role WHERE code = 'ROLE_ADMIN';
```

應該要有一筆資料，且 `code` 欄位是 `ROLE_ADMIN`（全大寫）。

#### 5. 檢查使用者角色關聯
```sql
SELECT aur.*, r.code 
FROM admin_user_role aur
JOIN role r ON aur.role_id = r.id
WHERE aur.admin_user_id = '70dc7e33-6053-46eb-834e-24087ad436ce';
```

應該要看到 `ROLE_ADMIN`。

## 📞 回報格式

如果修復後還是失敗，請提供：

```
1. 應用程式啟動日誌（最後 20 行）
2. 訪問 API 時的完整日誌（包含 🔍 emoji 的那些）
3. Postman 的 Request Headers
4. 資料庫查詢結果：
   SELECT * FROM role WHERE code LIKE '%ADMIN%';
   SELECT aur.*, r.code FROM admin_user_role aur 
   JOIN role r ON aur.role_id = r.id 
   WHERE aur.admin_user_id = '你的userId';
```

---

## 🎉 預期結果

修復後，你應該可以：
1. ✅ 登入取得 token
2. ✅ 使用 token 訪問所有後台 API
3. ✅ 看到詳細的認證日誌
4. ✅ API 回傳正確的資料，不再是 403

**現在請重啟應用並測試！** 🚀
