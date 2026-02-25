
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

---

# 新架構規範（2025-12-25 更新）

## API 目錄結構

### 前後台分離原則
```
controller/
├── admin/           # 後台管理 API → /admin/**
│   ├── AdminLotteryController.java
│   ├── AdminStoreController.java
│   └── AdminUserController.java
└── api/             # 前台 API → /api/**
    ├── LotteryController.java
    ├── UserController.java
    └── OrderController.java
```

## StoreID 自動帶入機制

### 問題背景
店家負責人新增商品時，前端無法取得 StoreID，需要從 JWT Token 自動提取。

### 解決方案

#### 1. 擴充 UserPrincipal
```java
@Data
@Builder
public class UserPrincipal implements UserDetails {
    private String userId;
    private String username;
    private List<String> roles;
    private List<String> storeIds;  // ← 新增店家 ID 列表
    // ...
}
```

#### 2. Filter 中查詢並設定 StoreID
```java
// AdminJwtAuthenticationFilter.java
StoreUserExample example = new StoreUserExample();
example.createCriteria().andAdminUserIdEqualTo(adminUser.getId());
List<StoreUser> storeUsers = storeUserMapper.selectByExample(example);

List<String> storeIds = storeUsers.stream()
        .map(StoreUser::getStoreId)
        .collect(Collectors.toList());

UserPrincipal principal = UserPrincipal.builder()
        .storeIds(storeIds)  // ← 設定店家 ID
        .build();
```

#### 3. SecurityUtils 提供取值方法
```java
/**
 * 取得當前使用者的主要店家 ID
 */
public static String getCurrentUserPrimaryStoreId() {
    UserPrincipal principal = (UserPrincipal) SecurityContextHolder
            .getContext()
            .getAuthentication()
            .getPrincipal();
    
    List<String> storeIds = principal.getStoreIds();
    return storeIds.isEmpty() ? null : storeIds.get(0);
}
```

#### 4. Controller 中自動帶入
```java
@PostMapping
public ResponseEntity<LotteryRes> createLottery(@RequestBody LotteryCreateReq req) {
    // ✅ 自動取得並設定 storeId
    String storeId = SecurityUtils.getCurrentUserPrimaryStoreId();
    req.setStoreId(storeId);
    
    return ResponseEntity.ok(service.createLottery(req));
}
```

## 查詢 API 設計模式

### 核心原則
1. **查詢條件與查詢行為分離**：Condition + QueryReq
2. **所有條件可選**：使用 MyBatis 動態 SQL
3. **前端做分頁**：後端返回全部資料（List 不用 PageHelper）
4. **組合而非繼承**：QueryReq 包裝 Condition

### BaseCondition（通用查詢條件）
```java
@Data
public abstract class BaseCondition {
    private LocalDateTime createdAtStart;  // 建立時間起
    private LocalDateTime createdAtEnd;    // 建立時間迄
    private String keyword;                // 關鍵字搜尋
}
```

### QueryReq（通用查詢請求）
```java
@Data
public class QueryReq<T> {
    private T condition;       // 查詢條件（可選）
    private Integer page;      // 分頁參數（前端用）
    private Integer size;
    private String sortBy;     // 排序欄位
    private String sortOrder;  // ASC/DESC
}
```

### 具體 Condition 範例
```java
@Data
@EqualsAndHashCode(callSuper = true)
public class LotteryCondition extends BaseCondition {
    private String storeId;    // 後端自動帶入
    private String title;      // 模糊查詢
    private String status;     // ON_SHELF/OFF_SHELF
    private String category;
    private Long priceMin;
    private Long priceMax;
}
```

### Controller 範例（後台）
```java
@PostMapping("/admin/lottery/list")
public ResponseEntity<List<LotteryRes>> queryLotteries(
        @RequestBody(required = false) QueryReq<LotteryCondition> req) {
    
    // ✅ 自動帶入 storeId
    String storeId = SecurityUtils.getCurrentUserPrimaryStoreId();
    if (req == null) req = new QueryReq<>();
    if (req.getCondition() == null) req.setCondition(new LotteryCondition());
    req.getCondition().setStoreId(storeId);
    
    // ✅ 返回全部資料（前端做分頁）
    return ResponseEntity.ok(service.queryLotteries(req));
}
```

### Service 範例（動態 SQL）
```java
@Override
public List<LotteryRes> queryLotteries(QueryReq<LotteryCondition> req) {
    LotteryCondition condition = req != null ? req.getCondition() : null;
    
    LotteryExample example = new LotteryExample();
    LotteryExample.Criteria criteria = example.createCriteria();
    
    // ✅ 所有條件都是可選的
    if (condition != null) {
        if (condition.getStoreId() != null) {
            criteria.andStoreIdEqualTo(condition.getStoreId());
        }
        if (condition.getTitle() != null && !condition.getTitle().isEmpty()) {
            criteria.andTitleLike("%" + condition.getTitle() + "%");
        }
        if (condition.getStatus() != null) {
            criteria.andStatusEqualTo(condition.getStatus());
        }
    }
    
    // 排序
    if (req != null && req.getSortBy() != null) {
        String order = req.getSortOrder() != null ? req.getSortOrder() : "ASC";
        example.setOrderByClause(req.getSortBy() + " " + order);
    }
    
    // ✅ 查詢全部資料
    List<Lottery> lotteries = lotteryMapper.selectByExample(example);
    return lotteries.stream().map(this::toRes).collect(Collectors.toList());
}
```

## 前端使用範例

### 後台：查詢商品
```javascript
// 不用傳 storeId，後端自動帶入
const response = await axios.post('/api/admin/lottery/list', {
  condition: {
    title: '鬼滅',
    status: 'ON_SHELF'
  },
  sortBy: 'created_at',
  sortOrder: 'DESC'
});

// 前端自己做分頁
const data = response.data.data;
const page1 = data.slice(0, 20);
```

### 後台：新增商品
```javascript
// 不用傳 storeId
const response = await axios.post('/api/admin/lottery', {
  title: '鬼滅之刃一番賞',
  category: 'OFFICIAL_ICHIBAN',
  pricePerDraw: 80
  // storeId 後端自動帶入
});
```

## 開發工作流程（更新）

### 新增查詢 API
1. 建立 `XXXCondition extends BaseCondition`
2. Controller 接收 `QueryReq<XXXCondition>`
3. 自動帶入 storeId（如果需要）
4. Service 使用 MyBatis Example 動態查詢
5. 返回 `List<Res>`（不用 PageHelper）

### 新增商品/資料 API
1. 取得 storeId：`SecurityUtils.getCurrentUserPrimaryStoreId()`
2. 設定到 Req：`req.setStoreId(storeId)`
3. Service 驗證 storeId 不為空
4. 寫入資料庫

## 常見錯誤（新增）

### 問題：前端拿不到 storeId
```java
// ❌ 錯誤：要求前端傳 storeId
@PostMapping
public ResponseEntity<LotteryRes> create(@RequestBody LotteryCreateReq req) {
    if (req.getStoreId() == null) throw new BusinessException("缺少 storeId");
}

// ✅ 正確：後端自動帶入
@PostMapping
public ResponseEntity<LotteryRes> create(@RequestBody LotteryCreateReq req) {
    String storeId = SecurityUtils.getCurrentUserPrimaryStoreId();
    req.setStoreId(storeId);
}
```

### 問題：查詢 API 返回空
```java
// 檢查點 1：是否自動帶入 storeId
log.info("查詢條件: {}", req.getCondition());

// 檢查點 2：MyBatis Example 是否正確設定條件
if (condition.getStoreId() != null) {  // ← 一定要檢查 null
    criteria.andStoreIdEqualTo(condition.getStoreId());
}
```

## 不得隨意更動（風險區）更新

- ❌ 不要在前端傳 storeId（後端自動帶入）
- ❌ 不要在 Service 使用 PageHelper（前端做分頁）
- ❌ 不要在 Controller 返回 Page 物件（返回 List）
- ❌ 不要忘記檢查 Condition 欄位是否為 null
- ❌ 不要在查詢 API 要求所有條件必填（全部可選）



---

# 刮刮樂架構規範（2026-02-10 更新）

## 雙號碼機制（關鍵！）

ticketNumber = 物理序號（1~N），玩家選的格子；revealedNumber = 刮開後顯示的亂數，用於大獎指定。
兩者不可混用。前端指定大獎時傳的是 revealedNumber，不是 ticketNumber。
AVAILABLE 狀態下，revealedNumber 不回傳給前端（安全隱藏）。

## gameMode vs playMode 速查

playMode = LOTTERY_MODE / SCRATCH_MODE（路由層）。
gameMode = SCRATCH_STORE（店家預先指定大獎 revealedNumber）/ SCRATCH_PLAYER（開套玩家呼叫 /designate 指定）/ RANDOM（全隨機）。

## autoAssignNonGrandPrizes() 說明

在 generateScratchTickets（SCRATCH_STORE）和 designatePrizePositions（SCRATCH_PLAYER）結束後自動呼叫。
對所有 isDesignatedPrize=0 且 status=AVAILABLE 的籤位，隨機分配非大獎獎品。
多餘籤位保持 prizeId=null，視同謝謝惠顧。

## checkDesignationRequired() 回傳格式

只有 gameMode=SCRATCH_PLAYER 且 session 未完成指定時才攔截，回傳：
{ designationRequired: true, availableNumbers: [revealedNumber...], grandPrizes: [{prizeId, prizeName, prizeLevel, quantity, prizeImageUrl}...] }
前端用 grandPrizes[].quantity 加總，決定要提交幾個指定位置。

## 不得隨意更動（刮刮樂篇）

不要在 getAvailableRevealedNumbers() 返回 ticketNumber。
不要對 isDesignatedPrize=1 的籤位執行 autoAssignNonGrandPrizes。
不要在 designatePrizePositions 後忘記呼叫 autoAssignNonGrandPrizes。
不要讓前端傳 ticketNumber 給 /designate（應傳 revealedNumber）。
不要把 designationRequired 邏輯套用到 SCRATCH_STORE。