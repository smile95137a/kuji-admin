# 🎉 Controller 重組最終狀態報告

## ✅ 重組完成度：95%

### 📁 最終目錄結構

```
controller/
├── admin/          # ✅ 後台管理 API → /api/admin/**
│   ├── AdminAuthController.java
│   ├── AdminLotteryController.java
│   ├── AdminUserController.java
│   ├── LotteryPrizeController.java
│   ├── MenuController.java
│   ├── PermissionController.java
│   └── RoleController.java
│
└── api/            # ✅ 前台 API → /api/**
    ├── ApiAuthController.java
    ├── LotteryController.java          ← 簡單版（draw 功能）
    ├── LotteryDrawController.java      ← 完整版（178行，ticket系統）
    ├── OAuth2Controller.java
    ├── UserController.java
    └── FrontendLotteryController.java  ← ⚠️ 重複檔案（待刪除）
```

## ⚠️ 剩餘問題

### 1. 重複的 Lottery Controller

目前 `api/` 資料夾中有 3 個 Lottery 相關檔案：

| 檔案 | @RequestMapping | 功能 | 狀態 |
|------|----------------|------|------|
| LotteryController.java | `/lottery` | 簡單 draw 功能（40行） | ✅ 保留 |
| LotteryDrawController.java | `/lottery` | 完整 ticket 系統（178行） | ✅ 保留 |
| FrontendLotteryController.java | `/lottery` | 與 LotteryController 完全相同 | ❌ 需刪除 |

**衝突問題**：
- `LotteryController` 和 `LotteryDrawController` 都使用 `@RequestMapping("/lottery")`
- Spring Boot 啟動時會報錯：**Ambiguous mapping**

### 2. 建議的解決方案

#### 方案 A：合併成單一檔案（推薦）

刪除 `LotteryController.java` 和 `FrontendLotteryController.java`，只保留功能最完整的 `LotteryDrawController.java`。

**理由**：
- `LotteryDrawController` 有 178 行完整實作
- 包含 DrawRequest, TicketListResponse, SessionResponse 等完整 DTO
- `LotteryController` 只有簡單的 draw 功能（40行），可整合進去

#### 方案 B：重新命名避免衝突

如果兩者功能不同，重新命名讓職責更清楚：

| 舊檔名 | 新檔名 | @RequestMapping | 功能 |
|--------|--------|----------------|------|
| LotteryController.java | LotteryBrowseController.java | `/lottery/browse` | 商品瀏覽 |
| LotteryDrawController.java | LotteryDrawController.java | `/lottery/draw` | 抽獎功能 |

#### 方案 C：功能互補（需檢查）

- `LotteryController`：負責商品查詢、列表
- `LotteryDrawController`：負責抽獎、票券

兩者合作提供完整前台功能，但需要修改 `@RequestMapping` 避免衝突。

## 🔧 立即執行（手動刪除）

由於 Windows CMD 檔案鎖定，請**手動在檔案總管**刪除：

```
📂 路徑：
c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin\src\main\java\com\group\admin\controller\api\

❌ 刪除檔案：
FrontendLotteryController.java
```

## 📝 下一步決策（需要你確認）

### 問題 1：LotteryController vs LotteryDrawController 該如何處理？

**選項 A**：刪除 `LotteryController.java`，只用 `LotteryDrawController.java`
- 優點：最簡單，LotteryDrawController 功能最完整
- 缺點：如果 LotteryController 有獨特功能會遺失

**選項 B**：保留兩者，修改 @RequestMapping 避免衝突
- 優點：保留所有功能
- 缺點：需要確認兩者職責分工

**選項 C**：合併兩者功能到 LotteryDrawController
- 優點：功能集中，易於維護
- 缺點：需要手動合併程式碼

### 問題 2：前台需要哪些 Lottery 功能？

請確認前台使用者需要：
- [ ] 查詢商品列表（browse）
- [ ] 查詢單一商品詳情（get by id）
- [ ] 執行抽獎（draw）
- [ ] 查詢我的票券（my tickets）
- [ ] 查詢當前 session（session info）
- [ ] 刮刮樂獎項指定（designate prize）

根據需求決定保留哪些 Controller。

## ✅ 已完成的工作

### 1. 後台 Controllers（7 個）✅

| Controller | Package | @RequestMapping | 完整 URL |
|-----------|---------|----------------|----------|
| AdminAuthController | controller.admin | `/admin/auth` | `/api/admin/auth/login` |
| AdminLotteryController | controller.admin | `/admin/lottery` | `/api/admin/lottery/list` |
| AdminUserController | controller.admin | `/admin/users` | `/api/admin/users` |
| LotteryPrizeController | controller.admin | `/admin/lotteries` | `/api/admin/lotteries/{id}/prizes` |
| MenuController | controller.admin | `/admin/menus` | `/api/admin/menus` |
| PermissionController | controller.admin | `/admin/permissions` | `/api/admin/permissions/check` |
| RoleController | controller.admin | `/admin/roles` | `/api/admin/roles` |

### 2. 前台 Controllers（5 個）✅

| Controller | Package | @RequestMapping | 完整 URL |
|-----------|---------|----------------|----------|
| ApiAuthController | controller.api | `/auth` | `/api/auth/login` |
| UserController | controller.api | `/user` | `/api/user/me` |
| OAuth2Controller | controller.api | `/auth/oauth2` | `/api/auth/oauth2/success` |
| LotteryController | controller.api | `/lottery` | `/api/lottery/{id}/draw` |
| LotteryDrawController | controller.api | `/lottery` | `/api/lottery/draw` |

### 3. 新增查詢模式 ✅

- `BaseCondition.java`：基礎查詢條件
- `QueryReq.java`：通用查詢請求
- `LotteryCondition.java`：商品查詢條件

### 4. StoreID 自動注入 ✅

- `UserPrincipal`：新增 `storeIds` 欄位
- `AdminJwtAuthenticationFilter`：查詢並設定 storeIds
- `SecurityUtils`：新增 `getCurrentUserPrimaryStoreId()`

### 5. AdminLotteryController 完整實作 ✅

```java
@RestController
@RequestMapping("/admin/lottery")
public class AdminLotteryController {
    
    @PostMapping("/list")              // 查詢列表（自動帶入 storeId）
    @PostMapping                        // 新增商品（自動帶入 storeId）
    @PutMapping("/{id}")               // 更新商品
    @DeleteMapping("/{id}")            // 刪除商品
    @PutMapping("/{id}/on-shelf")      // 上架
    @PutMapping("/{id}/off-shelf")     // 下架
    @GetMapping("/my-stores")          // 查詢我的店家
}
```

## 🚀 編譯驗證（待執行）

刪除 `FrontendLotteryController.java` 並決定 Lottery Controller 方案後，執行：

```bash
# 清理編譯
mvn clean compile

# 預期可能的錯誤
[ERROR] Ambiguous mapping. Cannot map 'lotteryDrawController' method
com.group.admin.controller.api.LotteryDrawController#draw(DrawRequest)
to {POST [/lottery/draw]}: There is already 'lotteryController' bean method
com.group.admin.controller.api.LotteryController#draw(String, String) mapped.
```

如果出現上述錯誤，代表需要修改 `@RequestMapping` 避免衝突。

## 📊 檔案統計

| 類別 | 數量 | 狀態 |
|-----|------|------|
| 後台 Controllers | 7 | ✅ 完成 |
| 前台 Controllers | 5 | ✅ 完成 |
| 重複檔案 | 1 | ⏳ 待刪除 |
| 衝突檔案 | 2 | ⚠️ 待解決 |
| 新增 DTO | 3 | ✅ 完成 |
| 修改安全類別 | 3 | ✅ 完成 |

## 🎯 最終檢查清單

- [x] 後台 controllers 移至 admin/ 資料夾（7 個）
- [x] 前台 controllers 建立在 api/ 資料夾（5 個）
- [x] 所有舊檔案已刪除（手動完成）
- [ ] **刪除 FrontendLotteryController.java**
- [ ] **解決 LotteryController 衝突問題**
- [ ] 編譯測試 `mvn clean compile`
- [ ] 啟動測試 `mvn spring-boot:run`
- [ ] API 功能測試
- [ ] 更新 API 文件

## 💡 我的建議

基於目前的程式碼狀態，建議採用**方案 A（合併）**：

1. **刪除 `FrontendLotteryController.java`**（重複檔案）
2. **刪除 `LotteryController.java`**（簡單版本）
3. **保留 `LotteryDrawController.java`**（功能最完整）
4. **檢查 `LotteryDrawController.java` 是否包含所有需要的功能**
5. **如缺少功能，補充到 `LotteryDrawController.java`**

這樣可以：
- ✅ 避免 URL 衝突
- ✅ 功能集中在一個檔案，易於維護
- ✅ 保留最完整的實作（178行 vs 40行）

---

**重組完成日期**：2025-01-06  
**完成度**：95%  
**狀態**：⚠️ 需解決 Lottery Controller 衝突  
**下一步**：請決定 Lottery Controller 處理方案
