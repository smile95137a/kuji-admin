# JWT 雙鏈安全架構 Skill

## 適用情境
- 新增路由保護規則（新增 permitAll 或 hasRole 路徑）
- 新增角色或調整角色權限
- 修改 JWT Token 結構
- 遇到 403 / 401 錯誤需要除錯
- 新增前台或後台 Filter 邏輯

---

## 雙鏈架構概覽

```
請求進來
   │
   ├── /admin/**  → Order(1) adminSecurityFilterChain
   │                  Filter: AdminJwtAuthenticationFilter
   │                  資料來源: admin_user 表
   │                  角色: ROLE_ADMIN / ROLE_STORE_OWNER / ROLE_STORE_EDITOR
   │
   └── /api/**    → Order(2) apiSecurityFilterChain
                      Filter: ApiJwtAuthenticationFilter
                      資料來源: 依 JWT 的 userType 決定
                        → "admin" → admin_user 表
                        → "user"  → user 表
                      角色: ROLE_USER + 所有後台角色
```

---

## JWT Token 結構

```json
{
  "sub": "admin@kuji.com",
  "userId": "uuid-string",
  "userType": "admin",        // "admin" 或 "user"
  "roles": ["ROLE_ADMIN"],    // 必須包含完整前綴 ROLE_
  "exp": 1234567890,
  "iat": 1234567890
}
```

> ⚠️ `roles` 陣列必須存放完整前綴（`ROLE_ADMIN`），不可只存 `ADMIN`。

---

## SecurityConfig 路由規則

### 後台路由（/admin/**）

```java
// SecurityConfig.java - adminSecurityFilterChain (Order 1)
.requestMatchers("/admin/auth/**").permitAll()           // 登入免認證
.requestMatchers("/admin/**").hasAnyRole("ADMIN", "STORE_OWNER", "STORE_EDITOR")
```

### 前台路由（/api/**）

```java
// SecurityConfig.java - apiSecurityFilterChain (Order 2)
.requestMatchers("/api/auth/**", "/login/oauth2/**").permitAll()
.requestMatchers("/api/district/**").permitAll()          // 行政區
.requestMatchers("/api/marquee/**").permitAll()           // 跑馬燈
.requestMatchers("/api/ws/**").permitAll()                // WebSocket
.requestMatchers("/api/recharge-plan/**").permitAll()     // 儲值方案
.requestMatchers("/api/stores/list").permitAll()          // 公開店家列表
.requestMatchers("/api/lottery/list").permitAll()         // 公開商品列表
.requestMatchers("/api/lottery/browse/**").permitAll()    // 公開商品瀏覽
.requestMatchers("/api/news/published").permitAll()       // 公開新聞
.requestMatchers("/api/banners").permitAll()              // 公開輪播
.requestMatchers("/api/**").hasAnyRole("USER", "ADMIN", "STORE_OWNER", "STORE_EDITOR")
```

### 新增公開路由的方法

在對應的 FilterChain Bean 中，`anyRequest()` 之前插入新的 `permitAll()` 規則：

```java
// 新增 /api/categories 為公開路由
.requestMatchers("/api/categories/**").permitAll()  // ← 插入這行
.requestMatchers("/api/**").hasAnyRole(...)
```

---

## Filter 關鍵設計細節

### ⚠️ 使用 getServletPath() 而非 getRequestURI()

因為有 `context-path = /api`，必須用 `getServletPath()` 來匹配路徑：

```java
// AdminJwtAuthenticationFilter.java
String path = request.getServletPath();  // ✅ 正確：/admin/auth/login
// request.getRequestURI()              // ❌ 錯誤：/api/admin/auth/login（含 context-path）

if (!path.startsWith("/admin/")) {
    filterChain.doFilter(request, response);
    return;
}
```

### AdminJwtAuthenticationFilter 流程

```
① 取得 ServletPath，非 /admin/** 則跳過
② 跳過 /admin/auth/** 登入路徑
③ 提取 Bearer Token
④ jwtUtil.validateToken(token)
⑤ 查詢 admin_user（用 username = JWT sub）
⑥ 查詢 StoreUser → 取得 storeIds 列表
⑦ 查詢 admin_user_role → role → 取得 roles 列表（含 ROLE_ 前綴）
⑧ 建立 UserPrincipal
⑨ 設定 SecurityContextHolder
```

### ApiJwtAuthenticationFilter 流程

```
① 取得 ServletPath，非 /api/** 則跳過
② 跳過 /api/auth/** 等公開路徑
③ 提取 Bearer Token
④ 從 JWT 讀取 userType
⑤ userType="admin" → 查 admin_user；userType="user" → 查 user 表
⑥ 設定對應角色（ROLE_USER 或後台角色）
⑦ 設定 SecurityContextHolder
```

---

## UserPrincipal 結構

```java
@Data
@Builder
public class UserPrincipal implements UserDetails {
    private String userId;       // UUID
    private String username;     // email
    private List<String> roles;  // ["ROLE_ADMIN"]，含完整前綴
    private List<String> storeIds; // 所屬店家列表（ADMIN 為空）
}
```

---

## SecurityUtils 正確用法

```java
// ✅ 取得當前使用者 ID（admin 和 user 通用）
String userId = SecurityUtils.getCurrentUserId();

// ✅ 取得當前使用者的主要店家 ID
String storeId = SecurityUtils.getCurrentUserPrimaryStoreId();
// 或取得所有店家
List<String> storeIds = SecurityUtils.getCurrentUserStoreIds();

// ✅ 角色判斷
boolean isAdmin = SecurityUtils.isAdmin();
boolean isOwner = SecurityUtils.isStoreOwner();
boolean isEditor = SecurityUtils.isStoreEditor();

// ❌ 錯誤：直接 cast principal 為 String
String id = (String) auth.getPrincipal();  // ClassCastException！
```

---

## Controller 方法級權限

```java
// ✅ 注意：hasRole('ADMIN') 會自動加 ROLE_ 前綴
@PreAuthorize("hasRole('ADMIN')")              // 僅 ADMIN
@PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER')")  // ADMIN 或 STORE_OWNER
@PreAuthorize("hasRole('ADMIN') or hasRole('STORE_OWNER')")
```

---

## 403 Forbidden 除錯清單

**步驟 1**：確認 Filter 日誌有無認證成功

```
✅ [AdminJwtFilter] 認證成功: admin@kuji.com (角色: [ROLE_ADMIN])
```

若看到 `⚠️ Token 驗證失敗` 或 `❌ 找不到管理員` → Token 有問題。

**步驟 2**：確認 UserPrincipal 角色含完整前綴

```java
principal.getRoles();  // 應該是 ["ROLE_ADMIN"]，不是 ["ADMIN"]
```

**步驟 3**：確認 SecurityConfig 的 hasAnyRole 大小寫

```java
.hasAnyRole("ADMIN")    // ✅ 正確（Spring 會加 ROLE_ 前綴）
.hasAnyRole("Admin")    // ❌ 錯誤（大小寫不符）
```

**步驟 4**：確認路由是否有進到正確的 FilterChain

- `/admin/**` → `adminSecurityFilterChain`（Order 1）
- `/api/**` → `apiSecurityFilterChain`（Order 2）

---

## ⚠️ 禁止操作

- ❌ 不要改變 FilterChain 的 Order 順序（會導致路由匹配錯誤）
- ❌ 不要在 Filter 中使用 `getRequestURI()`（含 context-path）
- ❌ 不要移除 roles 的 `ROLE_` 前綴
- ❌ 不要直接 cast `authentication.getPrincipal()` 為 String
- ❌ 不要在 SecurityConfig 的 `hasAnyRole()` 中包含 `ROLE_` 前綴（Spring 會自動加）
