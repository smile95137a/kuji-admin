
<!--
kuji-admin 專案的 Copilot 指南（中文）
以下內容為 AI 編碼代理人要快速上手時可參考的具體規則與範例。
最後更新：2025-12-25
-->

# kuji-admin Copilot 指南

## 摘要
- **技術棧**：Spring Boot 3.3.3 + Java 21 + MyBatis 3.0.5 + Spring Security + JWT
- **啟動類**：`com.group.admin.AdminApplication`
- **Context Path**：`/api`（所有 URL 以 http://localhost:8080/api 開頭）
- **資料庫**：MySQL 8.3，使用 UUID 作為主鍵策略

## 快速操作（命令）
```bash
# 建構與打包（不跑測試）
mvn clean package -DskipTests

# 開發模式執行
mvn spring-boot:run

# 或用 JAR 執行
java -jar target/admin-1.0.0.jar

# MyBatis Generator（重新生成 Entity/Mapper/Example）
mvn mybatis-generator:generate
```

## 核心架構設計

### 1. 雙路由安全架構（關鍵！）
專案使用 **多鏈 SecurityFilterChain** 分離前後台認證：

- **後台路由 `/admin/**`**（Order 1，優先）
  - Filter: `AdminJwtAuthenticationFilter`
  - 角色：`ROLE_ADMIN`, `ROLE_STORE_OWNER`, `ROLE_STORE_EDITOR`
  - 驗證：查詢 `admin_user` 表
  
- **前台路由 `/api/**`**（Order 2）
  - Filter: `ApiJwtAuthenticationFilter`（支援 admin token 與 user token）
  - 角色：`ROLE_USER` + 所有後台角色
  - 驗證：根據 JWT 的 `userType` 欄位決定查詢 `user` 或 `admin_user` 表

⚠️ **關鍵實作細節**：
- Filter 使用 `request.getServletPath()` 而非 `getRequestURI()` 來匹配路徑（因為有 context-path）
- SecurityConfig 的 `hasRole('ADMIN')` 會自動加上 `ROLE_` 前綴，所以資料庫存 `ROLE_ADMIN`
- `@PreAuthorize("hasRole('ADMIN')")` 也會自動加 `ROLE_` 前綴
- `UserPrincipal` 的 `roles` 必須包含完整的 `ROLE_ADMIN`（不能只存 `ADMIN`）

### 2. JWT Token 結構
```json
{
  "sub": "admin@kuji.com",
  "userId": "uuid-string",
  "userType": "admin",  // 或 "user"
  "roles": ["ROLE_ADMIN"],
  "exp": 1234567890,
  "iat": 1234567890
}
```

### 3. SecurityUtils 正確用法
```java
// ✅ 正確：getCurrentAdminUserId() 會從 UserPrincipal 取得 userId
String userId = SecurityUtils.getCurrentAdminUserId();

// ❌ 錯誤：不要假設 principal 是 String
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
String userId = (String) auth.getPrincipal(); // 會 ClassCastException！
```

### 4. 統一回應格式（AOP 自動包裝）
```java
// Controller 回傳 ResponseEntity 或物件，AOP 會自動包成：
{
  "success": true,
  "data": {...},
  "error": null,
  "meta": {
    "timestamp": "2025-12-25T...",
    "requestId": "uuid"
  }
}
```
- 實作：`aop/GlobalResponseAspect.java`
- 不要手動建立 `ApiResponse`，讓 AOP 處理

## MyBatis 慣例

### Entity/Mapper/Example 三件套
```
entity/Menu.java           ← POJO（對應 menu 表）
mapper/MenuMapper.java     ← 介面（CRUD 方法）
example/MenuExample.java   ← 動態查詢構建器
mapper/MenuMapper.xml      ← SQL 映射檔
```

### Example 查詢範例
```java
MenuExample example = new MenuExample();
example.createCriteria()
    .andIsVisibleEqualTo(true)
    .andParentIdIsNull();
example.setOrderByClause("order_num ASC");
List<Menu> menus = menuMapper.selectByExample(example);
```

### ⚠️ 常見錯誤
- **不要修改** Example 類別的生成內容
- **不要** 在 XML 中使用 `#{example.xxx}`，Example 是查詢條件容器，不是參數物件
- 更新/刪除時用 `updateByPrimaryKey()` 或 `updateByExample()`，不要自己寫 SQL

## 資料初始化

`DataInitializer.java` 在首次啟動時自動建立：
- 3 個角色：ROLE_ADMIN, ROLE_STORE_OWNER, ROLE_STORE_EDITOR
- 19 個選單（7 個頂層 + 12 個子選單）
- 角色選單權限關聯
- 測試帳號：admin@kuji.com / admin123

檢查初始化狀態：查詢 `role` 表是否有 `code='ROLE_ADMIN'`

## 權限檢查模式

### Service 層（推薦）
```java
if (!permissionService.isAdmin(adminUserId)) {
    throw new BusinessException("權限不足");
}
```

### Controller 層（方法級）
```java
@PreAuthorize("hasRole('ADMIN')")  // 注意：ADMIN 不含 ROLE_ 前綴
@GetMapping("/sensitive-data")
public ResponseEntity<Data> getSensitiveData() { ... }
```

## 修改時必查的檔案

### 安全相關
- `config/SecurityConfig.java`：多鏈配置、路徑匹配
- `security/AdminJwtAuthenticationFilter.java`：後台 JWT 驗證
- `security/ApiJwtAuthenticationFilter.java`：前台 JWT 驗證（支援雙類型）
- `security/UserPrincipal.java`：認證主體，包含 userId/username/roles
- `util/SecurityUtils.java`：從 SecurityContext 取得當前使用者資訊
- `util/JwtUtil.java`：JWT 產生與驗證

### 回應處理
- `aop/GlobalResponseAspect.java`：統一回應包裝與執行時間記錄
- `handler/GlobalExceptionHandler.java`：全域例外處理
- `result/ApiResponse.java`：標準回應格式

### 資料存取
- `config/DataInitializer.java`：系統初始化資料
- `service/impl/PermissionServiceImpl.java`：權限檢查邏輯
- `service/impl/MenuServiceImpl.java`：選單樹構建範例

## 常見問題除錯

### 問題：API 返回 403 Forbidden
```java
// 檢查點 1：Filter 是否正確設定 SecurityContext
log.info("認證成功: {} (角色: {})", username, roleNames);
// 應該看到 [ROLE_ADMIN] 而非 [ADMIN]

// 檢查點 2：SecurityConfig 的 hasAnyRole 是否正確
.requestMatchers("/admin/**").hasAnyRole("ADMIN")  // ✅ 正確
.requestMatchers("/admin/**").hasAnyRole("Admin")  // ❌ 錯誤（大小寫）

// 檢查點 3：UserPrincipal 的 roles 是否包含完整前綴
principal.getRoles();  // 應該是 ["ROLE_ADMIN"]
```

### 問題：SecurityUtils.getCurrentAdminUserId() 返回 null
```java
// 原因：getCurrentUserId() 只檢查 instanceof String
// 解決：已修正為檢查 instanceof UserPrincipal

// 驗證：在 Filter 中確認
UsernamePasswordAuthenticationToken authentication = 
    new UsernamePasswordAuthenticationToken(principal, null, authorities);
SecurityContextHolder.getContext().setAuthentication(authentication);
```

### 問題：選單 API 返回 data: null
```java
// 檢查點 1：isAdmin() 是否返回 true
log.info("是否為管理員: {}", permissionService.isAdmin(userId));

// 檢查點 2：資料庫角色代碼是否正確
SELECT code FROM role WHERE id = ?;  // 應該是 'ROLE_ADMIN'

// 檢查點 3：PermissionServiceImpl 的常數是否正確
private static final String ROLE_ADMIN = "ROLE_ADMIN";  // ✅
private static final String ROLE_ADMIN = "ADMIN";       // ❌
```

## 開發工作流程

1. **新增 Entity**：執行 MyBatis Generator（記得更新 generatorConfig.xml）
2. **新增 API**：Controller → Service → Mapper/XML
3. **測試**：使用 Postman/curl 測試，檢查 JWT token 格式
4. **驗證**：執行 `mvn clean package -DskipTests` 確保編譯通過
5. **日誌**：在關鍵處加 log.info，使用 emoji 方便視覺追蹤（🔍 🎭 ✅ ❌）

## 不得隨意更動（風險區）

- ❌ 不要改 `GlobalResponseAspect` 的 pointcut 或回傳邏輯
- ❌ 不要改 SecurityConfig 的 Order 順序（會導致路由匹配錯誤）
- ❌ 不要在 Filter 中使用 `request.getRequestURI()`（會包含 context-path）
- ❌ 不要移除 UserPrincipal.roles 中的 `ROLE_` 前綴
- ❌ 不要在 MyBatis XML 使用 Example 物件的屬性

## 需要協助時

提供以下資訊可加快問題定位：
1. 完整的錯誤日誌（包含 Filter 的 emoji 日誌）
2. JWT token 內容（用 jwt.io 解碼）
3. 呼叫的 API endpoint 與 HTTP method
4. 預期行為 vs 實際行為

