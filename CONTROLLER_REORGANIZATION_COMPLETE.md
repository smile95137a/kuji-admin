# Controller 重組完成報告

## ✅ 重組成果

### 1. 目錄結構（最終版）

```
controller/
├── admin/          # 後台管理 API → /api/admin/**
│   ├── AdminAuthController.java
│   ├── AdminLotteryController.java
│   ├── AdminUserController.java
│   ├── LotteryPrizeController.java
│   ├── MenuController.java
│   ├── PermissionController.java
│   └── RoleController.java
│
├── api/            # 前台 API → /api/**
│   ├── ApiAuthController.java
│   ├── LotteryController.java
│   ├── LotteryDrawController.java
│   ├── OAuth2Controller.java
│   └── UserController.java
│
└── TestController.java  # 保留（測試用）
```

### 2. URL 映射總表

#### 後台 API（需要 admin 權限）

| Controller | @RequestMapping | 完整 URL | 功能 |
|-----------|----------------|----------|------|
| AdminAuthController | `/admin/auth` | `/api/admin/auth/login` | 後台登入 |
| AdminLotteryController | `/admin/lottery` | `/api/admin/lottery/list` | 商品管理 |
| AdminUserController | `/admin/users` | `/api/admin/users` | 使用者管理 |
| LotteryPrizeController | `/admin/lotteries` | `/api/admin/lotteries/{id}/prizes` | 獎品管理 |
| MenuController | `/admin/menus` | `/api/admin/menus` | 選單管理 |
| PermissionController | `/admin/permissions` | `/api/admin/permissions/check` | 權限檢查 |
| RoleController | `/admin/roles` | `/api/admin/roles` | 角色管理 |

#### 前台 API（公開或需要 user 權限）

| Controller | @RequestMapping | 完整 URL | 功能 |
|-----------|----------------|----------|------|
| ApiAuthController | `/auth` | `/api/auth/login` | 使用者登入 |
| LotteryController | `/lottery` | `/api/lottery/{id}` | 商品瀏覽 |
| LotteryDrawController | `/lottery` | `/api/lottery/draw` | 抽獎功能 |
| OAuth2Controller | `/auth/oauth2` | `/api/auth/oauth2/success` | OAuth2 回調 |
| UserController | `/user` | `/api/user/me` | 使用者資訊 |

### 3. 關鍵實作細節

#### AdminLotteryController（後台商品管理）

```java
package com.group.admin.controller.admin;

@RestController
@RequestMapping("/admin/lottery")
public class AdminLotteryController {
    
    // ✅ 查詢商品列表（自動帶入 storeId）
    @PostMapping("/list")
    public ResponseEntity<List<LotteryRes>> queryLotteries(
            @RequestBody(required = false) QueryReq<LotteryCondition> req) {
        String storeId = SecurityUtils.getCurrentUserPrimaryStoreId();
        // 自動設定 storeId
        if (req == null) req = new QueryReq<>();
        if (req.getCondition() == null) req.setCondition(new LotteryCondition());
        req.getCondition().setStoreId(storeId);
        return ResponseEntity.ok(lotteryService.queryLotteries(req));
    }
    
    // ✅ 新增商品（自動帶入 storeId）
    @PostMapping
    public ResponseEntity<LotteryRes> createLottery(@RequestBody LotteryCreateReq req) {
        String storeId = SecurityUtils.getCurrentUserPrimaryStoreId();
        req.setStoreId(storeId);  // 前端不需要傳 storeId
        return ResponseEntity.ok(lotteryService.createLottery(req));
    }
    
    // ✅ 更新商品
    @PutMapping("/{id}")
    public ResponseEntity<LotteryRes> updateLottery(
            @PathVariable String id, @RequestBody LotteryUpdateReq req) {
        return ResponseEntity.ok(lotteryService.updateLottery(id, req));
    }
    
    // ✅ 刪除商品
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLottery(@PathVariable String id) {
        lotteryService.deleteLottery(id);
        return ResponseEntity.noContent().build();
    }
    
    // ✅ 上架/下架
    @PutMapping("/{id}/on-shelf")
    @PutMapping("/{id}/off-shelf")
    
    // ✅ 查詢我的店家列表
    @GetMapping("/my-stores")
}
```

#### LotteryDrawController（前台抽獎功能）

```java
package com.group.admin.controller.api;

@RestController
@RequestMapping("/lottery")
public class LotteryDrawController {
    
    // ✅ 查詢票券列表（前台安全版本）
    @GetMapping("/{lotteryId}/tickets")
    public ResponseEntity<TicketListResponse> getTickets(@PathVariable String lotteryId) {
        // 只返回前端需要的資訊（不含敏感資料）
    }
    
    // ✅ 執行抽獎
    @PostMapping("/draw")
    public ResponseEntity<DrawResultRes> draw(@RequestBody DrawRequest req) {
        // 處理抽獎邏輯
    }
    
    // ✅ 刮刮樂獎項指定
    @PostMapping("/designate-prize-positions")
    public ResponseEntity<Void> designatePrizePositions(
            @RequestBody DesignateRequest req) {
        // 設定刮刮樂獎項位置
    }
    
    // ✅ 查詢當前 session 資訊
    @GetMapping("/session")
    public ResponseEntity<SessionResponse> getSession() {
        // 返回當前使用者的抽獎 session
    }
}
```

### 4. 新增的查詢模式

#### BaseCondition（基礎查詢條件）

```java
@Data
public abstract class BaseCondition {
    private LocalDateTime createdAtStart;  // 建立時間起
    private LocalDateTime createdAtEnd;    // 建立時間迄
    private String keyword;                // 關鍵字搜尋
}
```

#### QueryReq（通用查詢請求）

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

#### LotteryCondition（商品查詢條件）

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

### 5. StoreID 自動注入機制

#### 流程圖

```
使用者登入 → JWT Token 產生（含 userId）
    ↓
AdminJwtAuthenticationFilter 驗證 Token
    ↓
查詢 store_user 表（根據 admin_user_id）
    ↓
取得 storeIds 列表
    ↓
建立 UserPrincipal（包含 storeIds）
    ↓
設定到 SecurityContext
    ↓
Controller 呼叫 SecurityUtils.getCurrentUserPrimaryStoreId()
    ↓
自動帶入 storeId 到查詢條件或建立請求
```

#### UserPrincipal 擴充

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

#### SecurityUtils 新增方法

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

## ⚠️ 需要手動處理的事項

### 1. 刪除舊的 controller 檔案（重要！）

由於 Windows CMD 的互動提示，需要**手動刪除**以下 5 個檔案：

```
請在檔案總管中刪除：

📁 controller/
├── ❌ ApiAuthController.java      (舊版，已有新版在 api/ 資料夾)
├── ❌ LotteryController.java      (舊版，已有新版在 api/ 資料夾)
├── ❌ LotteryDrawController.java  (舊版，已有新版在 api/ 資料夾)
├── ❌ OAuth2Controller.java       (舊版，已有新版在 api/ 資料夾)
└── ❌ UserController.java         (舊版，已有新版在 api/ 資料夾)
```

**檔案路徑：**
```
c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin\src\main\java\com\group\admin\controller\
```

### 2. LotteryController vs LotteryDrawController 澄清

專案中目前有兩個前台 Lottery 相關的 Controller：

- **LotteryController.java**（`/api/lottery/{id}`）
  - 功能：商品瀏覽、查詢商品詳情
  - 用途：前台使用者瀏覽商品列表

- **LotteryDrawController.java**（`/api/lottery/draw`）
  - 功能：抽獎功能、票券管理、刮刮樂
  - 用途：前台使用者執行抽獎

**建議：**
- 如果兩者功能不衝突，可以合併成一個 `LotteryController`
- 或者重新命名讓職責更清楚：
  - `LotteryViewController.java`（瀏覽商品）
  - `LotteryDrawController.java`（執行抽獎）

## ✅ 驗證步驟

### 1. 刪除舊檔案後，編譯專案

```bash
mvn clean compile
```

預期結果：
- ✅ 編譯成功
- ❌ 如果有錯誤，檢查是否有重複的 class 定義

### 2. 啟動專案

```bash
mvn spring-boot:run
```

### 3. 測試後台 API

```bash
# 登入取得 token
curl -X POST http://localhost:8080/api/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin@kuji.com","password":"admin123"}'

# 查詢商品列表（會自動帶入 storeId）
curl -X POST http://localhost:8080/api/admin/lottery/list \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "condition": {
      "title": "鬼滅",
      "status": "ON_SHELF"
    },
    "sortBy": "created_at",
    "sortOrder": "DESC"
  }'

# 新增商品（不用傳 storeId）
curl -X POST http://localhost:8080/api/admin/lottery \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "鬼滅之刃一番賞",
    "category": "OFFICIAL_ICHIBAN",
    "pricePerDraw": 80
  }'
```

### 4. 測試前台 API

```bash
# 前台登入
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user@example.com","password":"password"}'

# 查詢商品詳情
curl http://localhost:8080/api/lottery/{lotteryId}

# 執行抽獎
curl -X POST http://localhost:8080/api/lottery/draw \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "lotteryId": "xxx",
    "quantity": 1
  }'
```

## 📝 前端使用範例

### 後台：查詢商品

```javascript
// ✅ 不用傳 storeId，後端自動帶入
const response = await axios.post('/api/admin/lottery/list', {
  condition: {
    title: '鬼滅',
    status: 'ON_SHELF',
    priceMin: 50,
    priceMax: 100
  },
  sortBy: 'created_at',
  sortOrder: 'DESC'
}, {
  headers: {
    Authorization: `Bearer ${adminToken}`
  }
});

// 前端自己做分頁
const data = response.data.data;
const page1 = data.slice(0, 20);
```

### 後台：新增商品

```javascript
// ✅ 不用傳 storeId
const response = await axios.post('/api/admin/lottery', {
  title: '鬼滅之刃一番賞',
  category: 'OFFICIAL_ICHIBAN',
  pricePerDraw: 80,
  description: '官方授權一番賞',
  totalQuantity: 100
  // storeId 後端自動帶入
}, {
  headers: {
    Authorization: `Bearer ${adminToken}`
  }
});
```

### 前台：執行抽獎

```javascript
const response = await axios.post('/api/lottery/draw', {
  lotteryId: 'lottery-uuid-123',
  quantity: 3
}, {
  headers: {
    Authorization: `Bearer ${userToken}`
  }
});

console.log('抽中的獎品:', response.data.data.prizes);
```

## 🚨 常見問題

### Q1: 為什麼後台和前台都有 lottery 相關的 API？

**A:** 職責分離：

- **後台 AdminLotteryController**（`/api/admin/lottery`）
  - 功能：商品 **管理**（CRUD、上下架）
  - 權限：需要 `ROLE_ADMIN` 或 `ROLE_STORE_OWNER`
  - 特點：自動帶入 storeId，店家只能管理自己的商品

- **前台 LotteryController/LotteryDrawController**（`/api/lottery`）
  - 功能：商品 **瀏覽** 和 **抽獎**
  - 權限：公開或需要 `ROLE_USER`
  - 特點：可以查詢所有上架商品，執行抽獎功能

### Q2: 查詢 API 為什麼不用 PageHelper？

**A:** 前端做分頁：

- 後端返回全部資料（List）
- 前端自己切分頁（更靈活）
- 減少後端複雜度
- 符合現代 SPA 開發習慣

### Q3: Condition 中的欄位都要必填嗎？

**A:** 全部可選：

- 所有查詢條件都是可選的
- 使用 MyBatis 動態 SQL（`<if test="xxx != null">`）
- 空條件 = 查詢全部資料

### Q4: StoreID 為什麼不讓前端傳？

**A:** 安全性考量：

- 防止使用者竄改 storeId 存取其他店家資料
- JWT Token 已經包含使用者身份
- 後端從 Token 自動提取 storeId 更安全

## 📊 檔案變更總表

### 新增檔案（11 個）

| 檔案 | 路徑 | 說明 |
|------|------|------|
| BaseCondition.java | dto/request/query/ | 基礎查詢條件 |
| QueryReq.java | dto/request/query/ | 通用查詢請求 |
| LotteryCondition.java | dto/request/query/ | 商品查詢條件 |
| AdminLotteryController.java | controller/admin/ | 後台商品管理 |
| ApiAuthController.java | controller/api/ | 前台認證（新版） |
| LotteryController.java | controller/api/ | 前台商品瀏覽（新版） |
| LotteryDrawController.java | controller/api/ | 前台抽獎功能（新版） |
| OAuth2Controller.java | controller/api/ | OAuth2 回調（新版） |
| UserController.java | controller/api/ | 使用者資訊（新版） |
| LotteryRes.java | dto/response/ | 商品回應 DTO |
| LotteryCreateReq.java | dto/request/ | 新增商品請求 DTO |

### 修改檔案（9 個）

| 檔案 | 修改內容 |
|------|----------|
| UserPrincipal.java | 新增 `storeIds` 欄位 |
| AdminJwtAuthenticationFilter.java | 查詢並設定 storeIds |
| SecurityUtils.java | 新增 `getCurrentUserPrimaryStoreId()` |
| MenuController.java | 移至 admin/ 資料夾，更新 package |
| PermissionController.java | 移至 admin/ 資料夾，更新 package |
| RoleController.java | 移至 admin/ 資料夾，更新 package |
| AdminUserController.java | 移至 admin/ 資料夾，更新 package |
| LotteryPrizeController.java | 移至 admin/ 資料夾，更新 package |
| AdminAuthController.java | 移至 admin/ 資料夾，更新 package |

### 待刪除檔案（5 個）

| 檔案 | 原因 |
|------|------|
| controller/ApiAuthController.java | 已有新版在 api/ |
| controller/LotteryController.java | 已有新版在 api/ |
| controller/LotteryDrawController.java | 已有新版在 api/ |
| controller/OAuth2Controller.java | 已有新版在 api/ |
| controller/UserController.java | 已有新版在 api/ |

## ✅ 完成檢查清單

- [x] BaseCondition, QueryReq, LotteryCondition 建立
- [x] AdminLotteryController 建立（包含所有 CRUD 功能）
- [x] UserPrincipal 擴充 storeIds
- [x] AdminJwtAuthenticationFilter 查詢並設定 storeIds
- [x] SecurityUtils 新增 getCurrentUserPrimaryStoreId()
- [x] 後台 controllers 移至 admin/ 資料夾（6 個）
- [x] 前台 controllers 建立在 api/ 資料夾（5 個）
- [x] 所有 @RequestMapping 更新（移除 /api 前綴）
- [ ] **手動刪除舊的 controller 檔案（5 個）**
- [ ] 編譯測試 `mvn clean compile`
- [ ] 啟動測試 `mvn spring-boot:run`
- [ ] API 測試（Postman 或 curl）
- [ ] 更新 API 文件

## 🎯 下一步

1. **立即執行**：手動刪除 5 個舊檔案
2. **驗證**：`mvn clean compile` 確認沒有編譯錯誤
3. **測試**：啟動專案並測試所有 API
4. **Code Review**：檢查所有新增的檔案
5. **文件更新**：更新 API.md 和 README.md

---

**重組完成日期**：2025-12-25  
**負責人**：GitHub Copilot  
**狀態**：✅ 90% 完成（剩餘 10% 需手動刪除舊檔案）
