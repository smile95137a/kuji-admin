# API 403 錯誤修復總結

## 問題描述
使用有效的 admin JWT token 呼叫 `/api/**` 路徑時，一律回傳 403 Forbidden 錯誤。

## 根本原因
1. **ApiJwtAuthenticationFilter** 原本只認 `User` 表的前台使用者
2. Admin token 的使用者在 `AdminUser` 表，導致查詢失敗
3. 查詢失敗後 filter 不設定認證資訊，直接放行
4. **SecurityConfig** 的 API chain 要求 `hasRole("USER")`
5. Admin token 帶的是 `ROLE_ADMIN`，因此被拒絕

## 解決方案

### 1. 修改 `ApiJwtAuthenticationFilter.java`
**位置：** `src/main/java/com/group/admin/security/ApiJwtAuthenticationFilter.java`

**主要變更：**
- 新增支援 AdminUser 的認證邏輯
- 從 JWT token 的 `userType` 欄位判斷使用者類型
- 如果 `userType == "admin"`，從 AdminUser 表查詢並賦予對應角色
- 如果 `userType == "user"` 或空值，從 User 表查詢並賦予 ROLE_USER

**新增依賴：**
```java
private final AdminUserMapper adminUserMapper;
private final AdminUserRoleMapper adminUserRoleMapper;
private final RoleMapper roleMapper;
```

**新增方法：**
- `authenticateAdmin()` - 認證後台管理員
- `authenticateUser()` - 認證前台使用者

**認證流程：**
```
JWT Token
    ↓
取得 userType
    ↓
┌──────────────────┐
│ userType="admin" │
└──────────────────┘
    ↓
查詢 AdminUser 表 → 取得角色 → 設定 ROLE_Admin/StoreOwner/StoreEditor
    
┌──────────────────┐
│ userType="user"  │
└──────────────────┘
    ↓
查詢 User 表 → 設定 ROLE_USER
```

### 2. 修改 `SecurityConfig.java`
**位置：** `src/main/java/com/group/admin/config/SecurityConfig.java`

**修改內容：**
```java
// 修改前
.requestMatchers("/api/**").hasRole("USER")

// 修改後
.requestMatchers("/api/**").hasAnyRole("USER", "Admin", "StoreOwner", "StoreEditor")
```

**說明：**
- `/api/**` 路徑現在同時接受前台 USER 和後台管理角色
- 保持 `/admin/**` 路徑專屬於後台管理

## 測試驗證

### 測試案例 1：Admin Token 訪問 /api/admin/menus/accessible
```bash
# 登入取得 admin token
POST http://localhost:8080/admin/auth/login
{
  "username": "admin@kuji.com",
  "password": "admin123"
}

# 使用 token 訪問 API
GET http://localhost:8080/api/admin/menus/accessible
Authorization: Bearer {admin_token}

# 預期結果：200 OK，返回選單清單
```

### 測試案例 2：Admin Token 訪問 /admin/menus/accessible
```bash
# 使用同一個 admin token
GET http://localhost:8080/admin/menus/accessible
Authorization: Bearer {admin_token}

# 預期結果：200 OK，返回選單清單（與 /api 路徑相同）
```

### 測試案例 3：User Token 訪問 /api/**
```bash
# 前台使用者登入
POST http://localhost:8080/api/auth/login
{
  "email": "user1@test.com",
  "password": "Test1234"
}

# 使用 user token 訪問前台 API
GET http://localhost:8080/api/some-frontend-endpoint
Authorization: Bearer {user_token}

# 預期結果：200 OK（前台功能正常）
```

## 架構改進

### 前後台路徑規劃
```
/admin/**        → AdminJwtAuthenticationFilter (Order 1)
                 → 需要 Admin/StoreOwner/StoreEditor 角色
                 → 用於純後台管理介面

/api/**          → ApiJwtAuthenticationFilter (Order 2)
                 → 接受 USER 或 Admin/StoreOwner/StoreEditor 角色
                 → 用於前後台共用的 RESTful API
                 → 根據 JWT 的 userType 自動切換認證邏輯

/api/auth/**     → 公開路徑（不需認證）
/admin/auth/**   → 公開路徑（不需認證）
```

### JWT Token 結構
```json
{
  "sub": "admin@kuji.com",
  "userId": "70dc7e33-6053-46eb-834e-24087ad436ce",
  "roles": ["ROLE_ADMIN"],
  "userType": "admin",  // 🔑 關鍵欄位
  "exp": 1766647829,
  "iat": 1766561429
}
```

## 日誌輸出改進
修改後的 filter 會輸出更清楚的日誌：

```
✅ [API] 後台管理員認證成功: admin@kuji.com (角色: [Admin])
✅ [API] 前台使用者認證成功: user1@test.com
❌ 後台管理員不存在: unknown@test.com
❌ 前台使用者不存在: unknown@test.com
```

## 安全性考量

### ✅ 優點
1. 統一 API 路徑前綴（/api/**），方便前端管理
2. 自動根據 token 類型切換認證邏輯
3. 保持前後台資料隔離（不同資料表）
4. 支援角色細粒度控制

### ⚠️ 注意事項
1. 確保 JWT token 生成時正確設定 `userType` 欄位
2. 前台 User 不能訪問後台專屬功能（需在 Controller 層額外檢查）
3. 建議在 Controller 使用 `@PreAuthorize` 進一步限制權限

### 建議的 Controller 權限控制
```java
@RestController
@RequestMapping("/api/admin/menus")
public class MenuController {
    
    // 僅後台角色可訪問
    @PreAuthorize("hasAnyRole('Admin', 'StoreOwner', 'StoreEditor')")
    @GetMapping("/accessible")
    public ResponseEntity<?> getAccessibleMenuTree() {
        // ...
    }
}
```

## 後續優化建議

1. **統一認證邏輯**
   - 考慮將 AdminJwtAuthenticationFilter 和 ApiJwtAuthenticationFilter 合併
   - 使用策略模式處理不同使用者類型

2. **增強日誌追蹤**
   - 記錄每次 API 呼叫的使用者類型、角色、IP
   - 建立 AOP 統一記錄認證失敗

3. **API 文檔更新**
   - 更新 Swagger 文檔說明 `/api/**` 支援多種角色
   - 標註哪些 API 需要後台角色

## 修改檔案清單
- ✅ `ApiJwtAuthenticationFilter.java` - 新增 admin 認證邏輯
- ✅ `SecurityConfig.java` - 允許 /api/** 接受後台角色
- ✅ `API_403_FIX_SUMMARY.md` - 本文件

## 測試清單
- [ ] Admin token 訪問 /api/admin/menus/accessible → 200 OK
- [ ] Admin token 訪問 /admin/menus/accessible → 200 OK
- [ ] User token 訪問 /api/** (前台功能) → 200 OK
- [ ] User token 訪問 /api/admin/** (後台功能) → 403 Forbidden (期望行為)
- [ ] 無 token 訪問 /api/** → 403 Forbidden
- [ ] 過期 token → 403 Forbidden

---
**修復日期：** 2025-12-24  
**修復人員：** GitHub Copilot  
**問題編號：** #403-API-AUTH
