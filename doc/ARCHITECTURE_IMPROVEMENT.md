# 🎯 系統架構改善說明文件

## 📌 已完成的改善項目

### 1. **統一 API 回應格式（ApiResponse）**

所有 API 都使用統一的回應格式，透過 `GlobalResponseAspect` 自動包裝。

#### ApiResponse 結構：
```json
{
  "success": true,
  "data": { ... },
  "error": null,
  "meta": {
    "timestamp": "2025-12-11T10:30:00Z",
    "requestId": "uuid-xxx"
  }
}
```

#### 使用範例：
```java
// 1. 成功回應（帶資料）
return ApiResponse.success(user);

// 2. 成功回應（無資料）
return ApiResponse.success();

// 3. 成功回應（帶訊息）
return ApiResponse.successWithMessage("操作成功");

// 4. 分頁回應
return ApiResponse.successPage(PageResponse.of(items, total, pageRequest));

// 5. 失敗回應
return ApiResponse.error("ERR_001", "使用者不存在");

// 6. 失敗回應（帶詳細資訊）
return ApiResponse.error("VALIDATION_ERROR", "參數驗證失敗", validationErrors);
```

---

### 2. **完整的分頁機制**

#### PageRequest（分頁請求參數）：
```java
@Data
public class PageRequest {
    private Integer page = 1;           // 當前頁碼（從 1 開始）
    private Integer pageSize = 20;      // 每頁大小（最大 100）
    private String sortBy;              // 排序欄位
    private String sortOrder = "DESC";  // 排序方向（ASC/DESC）
    
    public int getOffset() { ... }      // 計算 SQL OFFSET
    public int getLimit() { ... }       // 取得 SQL LIMIT
    public void validate() { ... }      // 驗證並修正參數
}
```

#### PageResponse（分頁回應）：
```java
@Data
public class PageResponse<T> {
    private Integer page;               // 當前頁碼
    private Integer pageSize;           // 每頁大小
    private Long total;                 // 總筆數
    private Integer totalPages;         // 總頁數
    private List<T> items;              // 當前頁資料
    private Boolean hasNext;            // 是否有下一頁
    private Boolean hasPrevious;        // 是否有上一頁
    
    // 靜態工廠方法
    public static <T> PageResponse<T> of(List<T> items, Long total, PageRequest pageRequest) { ... }
    public static <T> PageResponse<T> empty(PageRequest pageRequest) { ... }
}
```

#### Controller 使用範例：
```java
@GetMapping("/users")
public ApiResponse<PageResponse<User>> getUsers(PageRequest pageRequest) {
    pageRequest.validate(); // 驗證參數
    
    List<User> items = userService.findAll(pageRequest);
    Long total = userService.count();
    
    return ApiResponse.successPage(PageResponse.of(items, total, pageRequest));
}
```

#### MyBatis Mapper 使用範例：
```xml
<select id="selectWithPage" resultType="User">
    SELECT * FROM user
    WHERE status = 1
    <if test="pageRequest.sortBy != null">
        ORDER BY ${pageRequest.sortBy} ${pageRequest.sortOrder}
    </if>
    LIMIT #{pageRequest.offset}, #{pageRequest.limit}
</select>
```

---

### 3. **前後台分離的 Security + JWT 架構**

#### 架構說明：

1. **UserPrincipal**（統一的使用者主體）
   - 支援前台 `User` 和後台 `AdminUser`
   - 包含 userId、username、roles、isAdmin、isUser 等資訊

2. **AdminJwtAuthenticationFilter**（後台 JWT 過濾器）
   - 僅處理 `/admin/**` 路徑（排除 `/admin/auth/**`）
   - 驗證後台使用者並載入角色（Admin, StoreOwner, StoreEditor）
   - 設定 `UserPrincipal` 到 Spring Security Context

3. **ApiJwtAuthenticationFilter**（前台 JWT 過濾器）
   - 僅處理 `/api/**` 路徑（排除 `/api/auth/**`）
   - 驗證前台使用者（固定為 USER 角色）
   - 設定 `UserPrincipal` 到 Spring Security Context

4. **SecurityConfig**（安全配置）
   - 使用 `@Order` 分離前後台配置
   - Order(1)：後台配置（/admin/**）
   - Order(2)：前台配置（/api/**）
   - Order(3)：預設配置（其他路徑）

#### 路徑權限配置：

| 路徑 | 權限要求 | 說明 |
|------|---------|------|
| `/admin/auth/**` | permitAll | 後台登入、註冊 |
| `/admin/**` | ROLE_Admin / ROLE_StoreOwner / ROLE_StoreEditor | 後台管理功能 |
| `/api/auth/**` | permitAll | 前台登入、註冊、OAuth |
| `/api/**` | ROLE_USER | 前台功能 |
| 其他 | permitAll | 開放存取 |

#### 在 Controller 中取得當前使用者：
```java
@GetMapping("/me")
public ApiResponse<UserInfo> getCurrentUser(Authentication authentication) {
    UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
    
    String userId = principal.getUserId();
    List<String> roles = principal.getRoles();
    boolean isAdmin = principal.isAdminUser();
    
    // ... 處理邏輯
}
```

---

### 4. **OAuth2 Google 登入說明**

目前你的 `ApiAuthController.loginWithGoogle()` 只是接收一個 `idToken`，這不是標準的 OAuth2 流程。

#### ❌ 目前的做法（不正確）：
```java
@PostMapping("/google")
public ResponseEntity<AuthRes> google(@RequestBody AuthGoogleReq req) {
    // 只接收 idToken，沒有真正的 OAuth2 流程
    var res = userService.loginWithGoogle(req);
    return ResponseEntity.ok(res);
}
```

#### ✅ 正確的 OAuth2 流程：

**方案 A：使用 Spring Security OAuth2 Client（推薦）**

1. 在 `application.yml` 配置 Google Client：
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: YOUR_GOOGLE_CLIENT_ID
            client-secret: YOUR_GOOGLE_CLIENT_SECRET
            scope:
              - email
              - profile
            redirect-uri: "{baseUrl}/api/auth/oauth2/callback/google"
        provider:
          google:
            authorization-uri: https://accounts.google.com/o/oauth2/auth
            token-uri: https://oauth2.googleapis.com/token
            user-info-uri: https://www.googleapis.com/oauth2/v3/userinfo
            user-name-attribute: sub
```

2. 建立 OAuth2 成功處理器：
```java
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                         HttpServletResponse response,
                                         Authentication authentication) {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        
        // 1. 查詢或建立使用者
        User user = userService.findOrCreateByEmail(email, name);
        
        // 2. 產生 JWT Token
        String token = jwtUtil.generateToken(email);
        
        // 3. 導向前端（帶著 token）
        response.sendRedirect("https://your-frontend.com/auth/callback?token=" + token);
    }
}
```

3. 在 SecurityConfig 中配置：
```java
http
    .oauth2Login(oauth2 -> oauth2
        .successHandler(oAuth2SuccessHandler)
    );
```

4. 前端流程：
```
1. 使用者點擊「Google 登入」
2. 導向：GET /oauth2/authorization/google
3. 跳轉到 Google 登入頁面
4. 使用者登入成功後，Google 回調：GET /api/auth/oauth2/callback/google?code=xxx
5. Spring Security 自動處理 code 換 token
6. 觸發 OAuth2SuccessHandler
7. 導向前端並帶著 JWT Token
```

**方案 B：手動處理 Google ID Token（目前的做法）**

如果你堅持使用目前的方式（前端取得 ID Token 後傳給後端），則需要：

1. 在後端驗證 ID Token 的有效性：
```java
@Service
public class GoogleOAuth2Service {
    public String verifyIdToken(String idToken) throws Exception {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(...)
            .setAudience(Collections.singletonList(CLIENT_ID))
            .build();
            
        GoogleIdToken token = verifier.verify(idToken);
        if (token != null) {
            GoogleIdToken.Payload payload = token.getPayload();
            return payload.getEmail();
        }
        throw new IllegalArgumentException("Invalid ID token");
    }
}
```

2. 在 Service 中驗證：
```java
public AuthRes loginWithGoogle(AuthGoogleReq req) {
    String email = googleOAuth2Service.verifyIdToken(req.getIdToken());
    User user = findOrCreateByEmail(email);
    
    // 產生 JWT
    String accessToken = jwtUtil.generateToken(email);
    return new AuthRes(accessToken, ...);
}
```

**我的建議：**
- 如果是「網頁前端」→ 使用方案 A（標準 OAuth2 流程）
- 如果是「手機 APP」→ 使用方案 B（驗證 ID Token）

---

## 📋 下一步待辦事項

1. ✅ ApiResponse 和分頁機制（已完成）
2. ✅ Security + JWT 分離（已完成）
3. ⏳ 實作 OAuth2 Google 登入（你需要選擇方案）
4. ⏳ 實作 RBAC 權限管理（@RequirePermission 註解）
5. ⏳ 建立所有後台 CRUD Controller
6. ⏳ 撰寫 API 文件
7. ⏳ 撰寫測試

---

## 🔧 需要你確認的問題

### 1. OAuth2 登入方式
你想使用哪種方式？
- A. 標準 OAuth2 流程（導向 Google 登入頁）
- B. 前端取得 ID Token 後傳給後端

### 2. AdminUser 欄位
目前 `AdminUser` 沒有 `storeId` 欄位，但你的需求中提到「店家可以建立小編」。
是否需要在 `AdminUser` 中加入以下欄位：
- `storeId`（所屬店家 ID）
- `email`（Email 欄位）
- `nickname`（顯示名稱）

### 3. 錯誤碼規範
是否需要建立統一的錯誤碼常數類別？例如：
```java
public class ErrorCode {
    public static final String USER_NOT_FOUND = "ERR_USER_001";
    public static final String INVALID_PASSWORD = "ERR_AUTH_001";
    public static final String INSUFFICIENT_PERMISSION = "ERR_PERM_001";
    // ...
}
```

### 4. 日誌規範
是否需要統一的日誌規範？例如：
- 所有 Controller 入口記錄請求參數
- 所有 Service 異常記錄錯誤堆疊
- 敏感資料（密碼）不記錄

---

## 📁 新增的檔案清單

```
src/main/java/com/group/admin/
├── page/
│   ├── PageRequest.java        ✅ 分頁請求參數
│   └── PageResponse.java       ✅ 分頁回應格式
├── result/
│   └── ApiResponse.java        ✅ 統一 API 回應（已重構）
├── security/
│   ├── UserPrincipal.java      ✅ 統一使用者主體
│   ├── AdminJwtAuthenticationFilter.java  ✅ 後台 JWT 過濾器
│   └── ApiJwtAuthenticationFilter.java    ✅ 前台 JWT 過濾器
└── config/
    └── SecurityConfig.java     ✅ 安全配置（已重構）
```

---

## 💡 使用建議

### Controller 撰寫範例：
```java
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    
    private final AdminUserService adminUserService;
    
    /**
     * 查詢後台使用者列表（分頁）
     */
    @GetMapping
    public ApiResponse<PageResponse<AdminUser>> getUsers(
            PageRequest pageRequest,
            @RequestParam(required = false) String keyword) {
        
        pageRequest.validate();
        
        List<AdminUser> items = adminUserService.findAll(pageRequest, keyword);
        Long total = adminUserService.count(keyword);
        
        return ApiResponse.successPage(PageResponse.of(items, total, pageRequest));
    }
    
    /**
     * 新增後台使用者
     */
    @PostMapping
    public ApiResponse<AdminUser> createUser(@RequestBody @Valid CreateAdminUserReq req) {
        AdminUser user = adminUserService.create(req);
        return ApiResponse.success(user);
    }
    
    /**
     * 更新後台使用者
     */
    @PutMapping("/{id}")
    public ApiResponse<AdminUser> updateUser(
            @PathVariable String id,
            @RequestBody @Valid UpdateAdminUserReq req) {
        AdminUser user = adminUserService.update(id, req);
        return ApiResponse.success(user);
    }
    
    /**
     * 刪除後台使用者
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable String id) {
        adminUserService.delete(id);
        return ApiResponse.success();
    }
}
```

---

## ⚠️ 注意事項

1. **編譯警告**：新建的 Filter 會有 `@NonNull` 警告，這不影響功能，是 IDE 的提示。

2. **測試編譯**：請執行以下命令測試：
   ```bash
   mvn clean compile -DskipTests
   ```

3. **資料庫欄位**：確認你的資料庫 schema 與 Entity 類別一致。

4. **JWT Secret**：確認 `application.yml` 中有配置 `jwt.secret`。

---

## 📞 聯絡與回饋

請確認以上的架構是否符合你的需求，然後我會繼續實作：
1. OAuth2 Google 登入（需要你選擇方案）
2. RBAC 權限管理
3. 所有後台 CRUD Controller
4. API 文件
5. 測試

如有任何問題或需要調整，請告訴我！
