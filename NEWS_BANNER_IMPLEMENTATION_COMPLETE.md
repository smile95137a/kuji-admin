# News & Banner 模組實作完成報告

## 📊 實作總覽

已完成 **News（最新消息）** 和 **Banner（首頁輪播）** 兩個完整模組的實作。

### ✅ 已完成項目

#### 1. 資料庫層
- ✅ `DDL_news_banner.sql` - News 表結構定義
- ✅ 執行 DDL 建立 news 表
- ✅ 執行 MyBatis Generator 生成 Mapper/Example/Entity

#### 2. DTO 層（8 個檔案）
- ✅ `NewsCondition.java` - News 查詢條件
- ✅ `NewsCreateReq.java` - News 新增請求
- ✅ `NewsUpdateReq.java` - News 更新請求
- ✅ `NewsRes.java` - News 回應（含 @Builder）
- ✅ `BannerCondition.java` - Banner 查詢條件
- ✅ `BannerCreateReq.java` - Banner 新增請求
- ✅ `BannerUpdateReq.java` - Banner 更新請求
- ✅ `BannerRes.java` - Banner 回應（含 @Builder）

#### 3. Service 層（4 個檔案）
- ✅ `NewsService.java` - News Service 介面
- ✅ `NewsServiceImpl.java` - News Service 實作（320 行）
- ✅ `BannerService.java` - Banner Service 介面
- ✅ `BannerServiceImpl.java` - Banner Service 實作（370 行）

#### 4. Controller 層（4 個檔案）
- ✅ `AdminNewsController.java` - 後台 News 管理（7 個 endpoint）
- ✅ `AdminBannerController.java` - 後台 Banner 管理（8 個 endpoint）
- ✅ `NewsController.java` - 前台 News 瀏覽（2 個 endpoint）
- ✅ `BannerController.java` - 前台 Banner 輪播（1 個 endpoint）

---

## 🎯 API 端點總覽

### 後台 News API（需 Admin 權限）

| 方法 | 路徑 | 功能 |
|------|------|------|
| POST | `/api/admin/news/list` | 查詢最新消息列表（支援條件查詢） |
| GET | `/api/admin/news/{id}` | 查詢單一最新消息詳情 |
| POST | `/api/admin/news` | 新增最新消息 |
| PUT | `/api/admin/news/{id}` | 更新最新消息 |
| DELETE | `/api/admin/news/{id}` | 刪除最新消息 |
| POST | `/api/admin/news/{id}/publish` | 上架最新消息 |
| POST | `/api/admin/news/{id}/unpublish` | 下架最新消息 |

### 後台 Banner API（需 Admin 權限）

| 方法 | 路徑 | 功能 |
|------|------|------|
| POST | `/api/admin/banner/list` | 查詢 Banner 列表（支援條件查詢） |
| GET | `/api/admin/banner/{id}` | 查詢單一 Banner 詳情 |
| POST | `/api/admin/banner` | 新增 Banner |
| PUT | `/api/admin/banner/{id}` | 更新 Banner |
| DELETE | `/api/admin/banner/{id}` | 刪除 Banner |
| POST | `/api/admin/banner/{id}/publish` | 上架 Banner |
| POST | `/api/admin/banner/{id}/unpublish` | 下架 Banner |
| PUT | `/api/admin/banner/{id}/order?orderNum={num}` | 更新 Banner 排序 |

### 前台 News API（無需登入）

| 方法 | 路徑 | 功能 |
|------|------|------|
| GET | `/api/news?limit={num}` | 查詢最新消息列表（僅 PUBLISHED） |
| GET | `/api/news/{id}` | 查詢單一最新消息詳情 |

### 前台 Banner API（無需登入）

| 方法 | 路徑 | 功能 |
|------|------|------|
| GET | `/api/banner/carousel` | 查詢輪播 Banner（僅 PUBLISHED 且店家 ACTIVE） |

---

## 🔑 核心功能特性

### News 模組

#### 狀態管理
- **DRAFT**（草稿）：僅後台可見
- **PUBLISHED**（已上架）：前台可見
- **ARCHIVED**（已下架）：前台不可見

#### 動態查詢
- 標題模糊查詢
- 狀態篩選
- 建立時間範圍
- 關鍵字搜尋

#### 排程機制
- `scheduled_at`：上架時間
- `end_time`：下架時間
- 前台自動過濾未到上架時間或已過下架時間的消息

#### 前台特性
- 僅顯示 PUBLISHED 狀態
- 按 `scheduled_at` 降序排列（最新的在前面）
- 支援 `limit` 參數（首頁顯示最新 N 則）

---

### Banner 模組

#### 狀態管理
- **PUBLISHED**（已上架）：前台可見
- **UNPUBLISHED**（未上架）：前台不可見

#### 店家關聯
- 每個 Banner 必須綁定一個店家
- 點擊導向店家頁面（顯示該店家所有上架商品）
- 店家停用時，該店家的 Banner 不顯示

#### 手動排序
- `order_num` 欄位控制輪播順序
- 數字越小優先級越高
- 支援動態更新排序

#### 動態查詢
- 店家 ID 查詢
- 標題模糊查詢
- 狀態篩選
- 建立時間範圍

#### 前台特性
- 僅顯示 PUBLISHED 狀態
- 過濾店家狀態（只顯示 ACTIVE 店家的 Banner）
- 按 `order_num` 升序排列
- 自動檢查 `start_time` 和 `end_time`

---

## 💻 Service 層實作要點

### NewsServiceImpl

#### queryNews()
```java
// 動態條件查詢
if (condition.getTitle() != null) {
    criteria.andTitleLike("%" + condition.getTitle() + "%");
}
if (condition.getStatus() != null) {
    criteria.andStatusEqualTo(condition.getStatus());
}
// 預設按建立時間降序
example.setOrderByClause("created_at DESC");
```

#### createNews()
```java
// 自動產生 UUID
news.setId(UUID.randomUUID().toString());
// 自動帶入當前使用者
String currentUserId = SecurityUtils.getCurrentAdminUserId();
news.setCreatedBy(currentUserId);
// 預設狀態為 DRAFT
news.setStatus(req.getStatus() != null ? req.getStatus() : "DRAFT");
```

#### publishNews()
```java
// 上架時自動設定時間
news.setStatus("PUBLISHED");
news.setScheduledAt(LocalDateTime.now());
```

#### getPublishedNews()
```java
// 前台查詢（多條件 OR）
criteria.andStatusEqualTo("PUBLISHED");
criteria.andScheduledAtLessThanOrEqualTo(LocalDateTime.now());

// 檢查下架時間（未設定 OR 未到時間）
criteria2.andEndTimeIsNull();
criteria3.andEndTimeGreaterThan(LocalDateTime.now());
```

---

### BannerServiceImpl

#### createBanner()
```java
// 檢查店家是否存在
Store store = storeMapper.selectByPrimaryKey(req.getStoreId());
if (store == null) {
    throw new RuntimeException("店家不存在");
}
// 預設排序為 0
banner.setOrderNum(req.getOrderNum() != null ? req.getOrderNum() : 0);
```

#### getCarouselBanners()
```java
// 1. 查詢 PUBLISHED 且時間有效的 Banner
criteria.andStatusEqualTo("PUBLISHED");
criteria.andStartTimeLessThanOrEqualTo(LocalDateTime.now());

// 2. 過濾店家狀態
return banners.stream()
    .filter(banner -> {
        Store store = storeMapper.selectByPrimaryKey(banner.getStoreId());
        return store != null && "ACTIVE".equals(store.getStatus());
    })
    .collect(Collectors.toList());
```

#### convertToRes()
```java
// 查詢店家名稱
Store store = storeMapper.selectByPrimaryKey(banner.getStoreId());
String storeName = store != null ? store.getStoreName() : "未知店家";

return BannerRes.builder()
    .storeName(storeName)  // 自動帶入店家名稱
    .statusName(getStatusName(banner.getStatus()))  // 中文狀態
    .build();
```

---

## 🔒 權限控管

### 後台 API
```java
@PreAuthorize("hasRole('ADMIN')")
public class AdminNewsController { ... }
```
- 所有後台 API 都需要 `ROLE_ADMIN` 權限
- StoreOwner 和 StoreEditor 無法存取

### 前台 API
- 無需登入即可存取
- 自動過濾未上架或已下架的內容
- News：只顯示 PUBLISHED
- Banner：只顯示 PUBLISHED + 店家 ACTIVE

---

## 📝 資料驗證

### NewsCreateReq
```java
@NotBlank(message = "標題不能為空")
private String title;

@NotBlank(message = "內容不能為空")
private String content;
```

### BannerCreateReq
```java
@NotBlank(message = "店家 ID 不能為空")
private String storeId;

@NotBlank(message = "標題不能為空")
private String title;

@NotBlank(message = "圖片 URL 不能為空")
private String imageUrl;
```

---

## 🧪 測試建議

### 後台 News 測試
1. **新增草稿** → 前台不可見
2. **上架** → 前台可見
3. **下架** → 前台不可見
4. **條件查詢** → 測試各種組合
5. **排程** → 測試 scheduled_at 和 end_time

### 後台 Banner 測試
1. **新增 Banner** → 檢查店家是否存在
2. **上架** → 前台輪播顯示
3. **更新排序** → 檢查輪播順序
4. **店家停用** → Banner 不應顯示
5. **時間範圍** → 測試 start_time 和 end_time

### 前台測試
1. **News 列表** → 測試 limit 參數
2. **News 詳情** → 測試未上架的 ID
3. **Banner 輪播** → 檢查排序和店家過濾

---

## 📊 檔案統計

| 類型 | 數量 | 總行數（約） |
|------|------|-------------|
| DTO | 8 | 600 |
| Service 介面 | 2 | 160 |
| Service 實作 | 2 | 690 |
| Controller | 4 | 380 |
| **總計** | **16** | **1,830** |

---

## 🚀 下一步驟

### 1. 編譯測試
```bash
mvn clean compile -DskipTests
```

### 2. 啟動應用
```bash
mvn spring-boot:run
```

### 3. 測試 API
使用 Postman 或 curl 測試：

#### 後台新增 News
```bash
POST /api/admin/news
Authorization: Bearer {admin_token}
{
  "title": "測試公告",
  "content": "這是測試內容",
  "status": "DRAFT"
}
```

#### 後台上架 News
```bash
POST /api/admin/news/{id}/publish
Authorization: Bearer {admin_token}
```

#### 前台查詢 News
```bash
GET /api/news?limit=5
```

#### 後台新增 Banner
```bash
POST /api/admin/banner
Authorization: Bearer {admin_token}
{
  "storeId": "store-uuid",
  "title": "春節優惠",
  "imageUrl": "https://example.com/banner.jpg",
  "orderNum": 1
}
```

#### 前台查詢輪播
```bash
GET /api/banner/carousel
```

---

## ✅ 實作完成

所有 News 和 Banner 模組的程式碼已完成，包含：
- ✅ 資料庫 DDL
- ✅ MyBatis Mapper/Entity/Example
- ✅ DTO（Request/Response）
- ✅ Service 層（介面 + 實作）
- ✅ Controller 層（前後台分離）
- ✅ 權限控管
- ✅ 資料驗證
- ✅ Swagger 文件

**準備進行編譯測試和 API 測試！** 🎉
