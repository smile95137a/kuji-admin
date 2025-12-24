# 🎯 終極修復方案 - 403 問題根本解決

## 🔍 根本原因（已確認）

### 問題：`request.getRequestURI()` vs `request.getServletPath()`

**你的配置：**
```yaml
server:
  servlet:
    context-path: /api
```

**當瀏覽器訪問：** `http://localhost:8080/api/admin/menus/accessible`

**Filter 收到的值：**
```java
request.getRequestURI()    = "/api/admin/menus/accessible"  // ❌ 包含 context-path
request.getServletPath()   = "/admin/menus/accessible"      // ✅ 已去掉 context-path
```

**原始程式碼的問題：**
```java
String path = request.getRequestURI();  // = "/api/admin/menus/accessible"
if (!path.startsWith("/admin/")) {      // ❌ 不符合！因為是 /api/admin/
    跳過認證;                            // 結果：沒有認證 → 403
}
```

## ✅ 已修復

### 1. AdminJwtAuthenticationFilter.java
**修改前：**
```java
String path = request.getRequestURI();  // /api/admin/menus/accessible
if (!path.startsWith("/admin/")) {      // ❌ 檢查失敗
```

**修改後：**
```java
String path = request.getServletPath();  // /admin/menus/accessible
log.info("收到請求: URI={}, ServletPath={}", request.getRequestURI(), path);
if (!path.startsWith("/admin/")) {       // ✅ 檢查通過
```

### 2. ApiJwtAuthenticationFilter.java
同樣的修正，改用 `getServletPath()`

### 3. SecurityConfig.java
角色名稱已改為大寫：`ADMIN`, `STORE_OWNER`, `STORE_EDITOR`

## 🚀 立即執行步驟

### 步驟 1：停止當前應用
在 VS Code Terminal 按 `Ctrl + C`

### 步驟 2：等待編譯完成
看到：
```
[INFO] BUILD SUCCESS
```

### 步驟 3：重新啟動
```bash
mvn spring-boot:run
```

或

```bash
java -jar target\admin-1.0.0.jar
```

### 步驟 4：登入
```http
POST http://localhost:8080/api/admin/auth/login
Content-Type: application/json

{
  "username": "admin@kuji.com",
  "password": "admin123"
}
```

### 步驟 5：測試 API
```http
GET http://localhost:8080/api/admin/menus/accessible
Authorization: Bearer {你的_access_token}
```

## 📊 預期日誌輸出

### ✅ 成功的情況
```
🔍 [AdminJwtFilter] 收到請求: URI=/api/admin/menus/accessible, ServletPath=/admin/menus/accessible
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
      "code": "STORE_MANAGEMENT",
      "path": "/admin/stores",
      ...
    }
  ]
}
```

## 🔧 完整架構說明

### Request 處理流程

```
瀏覽器
  ↓
http://localhost:8080/api/admin/menus/accessible
  ↓
Tomcat (接收請求)
  ↓
context-path 處理 (/api)
  ↓
Spring Security Filter Chain
  ↓
AdminSecurityFilterChain (Order 1) - 匹配 /admin/**
  ↓
AdminJwtAuthenticationFilter
  - getServletPath() = /admin/menus/accessible ✅
  - 檢查 startsWith("/admin/") ✅
  - 驗證 JWT Token ✅
  - 查詢 AdminUser ✅
  - 查詢 Role (ROLE_ADMIN) ✅
  - 設定 SecurityContext ✅
  ↓
Authorization Check
  - 要求: hasAnyRole("ADMIN", "STORE_OWNER", "STORE_EDITOR")
  - 實際: ROLE_ADMIN
  - 結果: ✅ 通過
  ↓
MenuController.getAccessibleMenuTree()
  ↓
回傳選單資料
```

## 📝 getRequestURI() vs getServletPath() 差異

| 方法 | 包含 context-path? | 你的專案實際值 | 應該用在 |
|------|-------------------|--------------|---------|
| `getRequestURI()` | ✅ 是 | `/api/admin/menus/accessible` | 日誌記錄 |
| `getServletPath()` | ❌ 否 | `/admin/menus/accessible` | 路徑比對 ✅ |
| `getContextPath()` | N/A | `/api` | 取得 context-path |

## 🎯 為什麼之前一直 403？

1. ✅ Token 有效（有 ROLE_ADMIN）
2. ✅ 使用者存在（admin@kuji.com）
3. ❌ **Filter 用錯方法取路徑**
   - 用了 `getRequestURI()` → `/api/admin/...`
   - 檢查 `startsWith("/admin/")` → 失敗
   - Filter 跳過認證
   - SecurityContext 沒有設定
   - Spring Security 拒絕訪問 → 403

## 🔄 其他路徑的處理

### 前台 User API (假設有的話)
```
http://localhost:8080/api/user/profile
  ↓
ServletPath = /user/profile
  ↓
AdminJwtFilter: 不是 /admin/**，跳過
  ↓
ApiJwtFilter: 處理 (但目前 ApiJwtFilter 會處理所有路徑)
```

### 登入 API
```
http://localhost:8080/api/admin/auth/login
  ↓
ServletPath = /admin/auth/login
  ↓
AdminJwtFilter: 是 /admin/auth/** 路徑，跳過認證 ✅
  ↓
SecurityConfig: permitAll() ✅
  ↓
AdminAuthController.login()
```

## ⚡ 測試檢查清單

重啟後測試：

- [ ] 登入成功，取得 token
- [ ] 日誌顯示：`URI=/api/admin/menus/accessible, ServletPath=/admin/menus/accessible`
- [ ] 日誌顯示：`🔐 [AdminJwtFilter] 開始認證`
- [ ] 日誌顯示：`✅ [AdminJwtFilter] 認證成功`
- [ ] API 回傳 200 OK
- [ ] 取得選單資料

## 🐛 如果還是有問題

請提供：
1. 完整的日誌（從 "收到請求" 開始）
2. 你的 Postman Request Headers
3. Token payload（去 jwt.io 解析）

---

**修復狀態：** ✅ 完成
**預計結果：** 403 → 200 OK
**需要動作：** 重啟應用並測試

**這次一定可以！** 🎉
