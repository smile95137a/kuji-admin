# Banner 模組實作計畫

## 📋 需求摘要

根據 `banner.prompt.md`，Banner 為平台首頁的**付費曝光位**，核心功能：
- 提供店家付費宣傳管道
- 引導玩家進入指定店家頁面
- 提升店家商品的整體曝光與轉換率

## 🎯 核心規則

### 1. 點擊行為
- ✅ 點擊後導向「店家頁面」
- ❌ 不可導向單一商品
- ❌ 不可導向外部連結
- ❌ 不可不綁定店家

### 2. 關聯規則
- 每個 Banner **只能綁定一個店家**
- 一個店家可以有多個 Banner
- Banner 圖片由店家提供，Admin 上架管理

### 3. 顯示規則
- 僅首頁主 Banner（輪播區）
- 支援手動排序（影響輪播優先順序）
- 支援上下架排程

### 4. 權限規則
| 角色 | 權限 |
|------|------|
| Admin | 新增/編輯/上下架/排序所有 Banner |
| StoreOwner | 無權限 |
| StoreEditor | 無權限 |
| 玩家 | 僅瀏覽與點擊 |

---

## 🗂️ 資料表設計

### banner 表
```sql
CREATE TABLE `banner` (
  `id` VARCHAR(36) PRIMARY KEY COMMENT 'UUID',
  `store_id` VARCHAR(36) NOT NULL COMMENT '關聯店家 ID',
  `title` VARCHAR(100) NOT NULL COMMENT 'Banner 標題',
  `image_url` VARCHAR(500) NOT NULL COMMENT 'Banner 圖片 URL',
  `order_num` INT DEFAULT 0 COMMENT '排序號碼（越小越前）',
  `status` VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '狀態：DRAFT/ACTIVE/INACTIVE',
  `start_time` DATETIME COMMENT '上架時間',
  `end_time` DATETIME COMMENT '下架時間',
  `remark` VARCHAR(500) COMMENT '備註（費用、洽談紀錄等）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_by` VARCHAR(36) COMMENT '建立者 ID',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updated_by` VARCHAR(36) COMMENT '更新者 ID',
  FOREIGN KEY (`store_id`) REFERENCES `store`(`id`),
  INDEX `idx_status_order` (`status`, `order_num`),
  INDEX `idx_time_range` (`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Banner 管理';
```

### 欄位說明
- **id**: UUID 主鍵
- **store_id**: 關聯店家（必填，FK）
- **title**: Banner 標題（方便管理識別）
- **image_url**: 圖片 URL（使用本地上傳系統）
- **order_num**: 排序號碼（手動排序，越小越前）
- **status**: 狀態（DRAFT/ACTIVE/INACTIVE）
- **start_time/end_time**: 排程上下架時間
- **remark**: 備註（費用資訊、洽談紀錄等）

---

## 📦 實作架構

### 1. Entity
```java
@Data
@Table(name = "banner")
public class Banner {
    private String id;
    private String storeId;
    private String title;
    private String imageUrl;
    private Integer orderNum;
    private String status;  // DRAFT, ACTIVE, INACTIVE
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String remark;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
```

### 2. Enum
```java
public enum BannerStatusEnum {
    DRAFT("DRAFT", "草稿"),
    ACTIVE("ACTIVE", "上架"),
    INACTIVE("INACTIVE", "下架");
}
```

### 3. DTO

#### BannerCreateReq
```java
@Data
public class BannerCreateReq {
    @NotBlank(message = "店家ID不可為空")
    private String storeId;
    
    @NotBlank(message = "標題不可為空")
    @Length(max = 100, message = "標題不可超過100字")
    private String title;
    
    @NotBlank(message = "圖片URL不可為空")
    private String imageUrl;
    
    private Integer orderNum = 0;
    
    private String status = "DRAFT";
    
    @Future(message = "上架時間必須為未來時間")
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    @Length(max = 500, message = "備註不可超過500字")
    private String remark;
}
```

#### BannerUpdateReq
```java
@Data
public class BannerUpdateReq {
    @NotBlank(message = "ID不可為空")
    private String id;
    
    private String storeId;
    private String title;
    private String imageUrl;
    private Integer orderNum;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String remark;
}
```

#### BannerRes
```java
@Data
@Builder
public class BannerRes {
    private String id;
    private String storeId;
    private String storeName;  // 店家名稱（Join）
    private String title;
    private String imageUrl;
    private Integer orderNum;
    private String status;
    private String statusName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String remark;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
```

#### BannerCondition
```java
@Data
@EqualsAndHashCode(callSuper = true)
public class BannerCondition extends BaseCondition {
    private String storeId;
    private String title;
    private String status;
    private LocalDateTime startTimeFrom;
    private LocalDateTime startTimeTo;
}
```

### 4. Controller

#### AdminBannerController（後台管理）
```java
@RestController
@RequestMapping("/api/admin/banner")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminBannerController {
    
    private final BannerService bannerService;
    
    @PostMapping
    public ResponseEntity<BannerRes> createBanner(@Valid @RequestBody BannerCreateReq req);
    
    @PutMapping
    public ResponseEntity<BannerRes> updateBanner(@Valid @RequestBody BannerUpdateReq req);
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBanner(@PathVariable String id);
    
    @PostMapping("/list")
    public ResponseEntity<List<BannerRes>> queryBanners(
        @RequestBody(required = false) QueryReq<BannerCondition> req);
    
    @GetMapping("/{id}")
    public ResponseEntity<BannerRes> getBannerById(@PathVariable String id);
    
    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
        @PathVariable String id, 
        @RequestParam String status);
    
    @PutMapping("/reorder")
    public ResponseEntity<Void> reorderBanners(
        @RequestBody List<BannerOrderReq> orders);
}
```

#### BannerController（前台 API）
```java
@RestController
@RequestMapping("/api/banners")
@RequiredArgsConstructor
public class BannerController {
    
    private final BannerService bannerService;
    
    /**
     * 取得首頁 Banner 列表（輪播用）
     * - 只返回 ACTIVE 狀態
     * - 自動過濾時間範圍
     * - 過濾已停用店家
     * - 依 order_num 排序
     */
    @GetMapping
    public ResponseEntity<List<BannerRes>> getActiveBanners();
}
```

### 5. Service

#### BannerService 介面
```java
public interface BannerService {
    BannerRes createBanner(BannerCreateReq req);
    BannerRes updateBanner(BannerUpdateReq req);
    void deleteBanner(String id);
    List<BannerRes> queryBanners(QueryReq<BannerCondition> req);
    BannerRes getBannerById(String id);
    void updateStatus(String id, String status);
    void reorderBanners(List<BannerOrderReq> orders);
    List<BannerRes> getActiveBanners();
}
```

#### BannerServiceImpl 關鍵邏輯
```java
@Service
@RequiredArgsConstructor
public class BannerServiceImpl implements BannerService {
    
    /**
     * 取得前台 Banner
     * - status = ACTIVE
     * - 當前時間在 start_time ~ end_time 範圍內
     * - 關聯店家狀態 = ACTIVE
     * - 依 order_num ASC 排序
     */
    @Override
    public List<BannerRes> getActiveBanners() {
        BannerExample example = new BannerExample();
        BannerExample.Criteria criteria = example.createCriteria();
        
        criteria.andStatusEqualTo("ACTIVE");
        
        LocalDateTime now = LocalDateTime.now();
        criteria.andStartTimeLessThanOrEqualTo(now);
        criteria.andEndTimeGreaterThanOrEqualTo(now);
        
        example.setOrderByClause("order_num ASC");
        
        List<Banner> banners = bannerMapper.selectByExample(example);
        
        // 過濾已停用店家
        return banners.stream()
            .filter(banner -> isStoreActive(banner.getStoreId()))
            .map(this::toRes)
            .collect(Collectors.toList());
    }
    
    private boolean isStoreActive(String storeId) {
        Store store = storeMapper.selectByPrimaryKey(storeId);
        return store != null && "ACTIVE".equals(store.getStatus());
    }
}
```

---

## 🧪 測試計畫

### 1. 後台測試

#### 測試案例 1：新增 Banner
```bash
POST /api/admin/banner
Authorization: Bearer {admin-token}
Content-Type: application/json

{
  "storeId": "{STORE_1_ID}",
  "title": "新年特惠活動",
  "imageUrl": "/img/banner/new-year-2026.jpg",
  "orderNum": 1,
  "status": "ACTIVE",
  "startTime": "2026-01-01T00:00:00",
  "endTime": "2026-01-31T23:59:59",
  "remark": "費用：NT$ 10,000"
}

# 預期：成功新增
```

#### 測試案例 2：查詢 Banner
```bash
POST /api/admin/banner/list
Authorization: Bearer {admin-token}

{
  "condition": {
    "status": "ACTIVE"
  },
  "sortBy": "order_num",
  "sortOrder": "ASC"
}

# 預期：返回所有上架中的 Banner，依排序號碼排列
```

#### 測試案例 3：排序 Banner
```bash
PUT /api/admin/banner/reorder
Authorization: Bearer {admin-token}

[
  { "id": "banner-1-id", "orderNum": 1 },
  { "id": "banner-2-id", "orderNum": 2 },
  { "id": "banner-3-id", "orderNum": 3 }
]

# 預期：批次更新排序
```

#### 測試案例 4：更新狀態
```bash
PUT /api/admin/banner/{id}/status?status=INACTIVE
Authorization: Bearer {admin-token}

# 預期：Banner 下架
```

#### 測試案例 5：StoreOwner 無權限
```bash
POST /api/admin/banner
Authorization: Bearer {store-owner-token}

# 預期：403 Forbidden
```

### 2. 前台測試

#### 測試案例 1：取得輪播 Banner
```bash
GET /api/banners

# 預期：返回所有符合條件的 Banner
# - status = ACTIVE
# - 時間範圍內
# - 店家為 ACTIVE
# - 依 order_num 排序
```

#### 測試案例 2：點擊 Banner 跳轉
```
前端邏輯：
1. 取得 Banner 的 storeId
2. 導向 /store/{storeId}
3. 店家頁面顯示該店家所有上架商品
```

---

## 📅 實作步驟

### Phase 1: 資料庫與 MyBatis（30 分鐘）
- [ ] 1. 建立 DDL（banner 表）
- [ ] 2. 執行 DDL 建立資料表
- [ ] 3. 更新 generatorConfig.xml
- [ ] 4. 執行 MyBatis Generator
- [ ] 5. 檢查 Entity、Mapper、Example 是否正確生成

### Phase 2: Enum 與 DTO（20 分鐘）
- [ ] 6. 建立 BannerStatusEnum
- [ ] 7. 建立 BannerCreateReq
- [ ] 8. 建立 BannerUpdateReq
- [ ] 9. 建立 BannerRes
- [ ] 10. 建立 BannerCondition
- [ ] 11. 建立 BannerOrderReq

### Phase 3: Service 層（40 分鐘）
- [ ] 12. 建立 BannerService 介面
- [ ] 13. 實作 BannerServiceImpl
- [ ] 14. 實作 createBanner()
- [ ] 15. 實作 updateBanner()
- [ ] 16. 實作 deleteBanner()
- [ ] 17. 實作 queryBanners()
- [ ] 18. 實作 getActiveBanners()（前台用）
- [ ] 19. 實作 updateStatus()
- [ ] 20. 實作 reorderBanners()

### Phase 4: Controller 層（30 分鐘）
- [ ] 21. 建立 AdminBannerController
- [ ] 22. 實作 CRUD API
- [ ] 23. 實作狀態更新 API
- [ ] 24. 實作排序 API
- [ ] 25. 建立 BannerController（前台）
- [ ] 26. 實作 getActiveBanners()

### Phase 5: DataInitializer（10 分鐘）
- [ ] 27. 加入測試 Banner 資料（3~5 筆）
- [ ] 28. 關聯到測試店家

### Phase 6: 測試與驗證（30 分鐘）
- [ ] 29. 編譯專案（mvn clean package -DskipTests）
- [ ] 30. 重啟應用程式
- [ ] 31. 執行後台測試（CRUD、排序、權限）
- [ ] 32. 執行前台測試（輪播顯示）
- [ ] 33. 驗證排程自動上下架
- [ ] 34. 驗證店家停用時 Banner 不顯示

### Phase 7: 文件與交付（10 分鐘）
- [ ] 35. 建立 API 測試文件
- [ ] 36. 更新 FRONTEND_API_REFERENCE.json
- [ ] 37. 建立 Postman 測試案例
- [ ] 38. 撰寫實作完成報告

---

## ⚠️ 注意事項

### 1. 圖片上傳
使用既有的 LocalFileServiceImpl：
```java
// 上傳 Banner 圖片
fileService.uploadFile(file, "banner");
// 返回：/img/banner/{uuid}.ext
```

### 2. 排程邏輯
系統需要定時任務檢查 start_time/end_time：
```java
@Scheduled(cron = "0 */5 * * * *")  // 每 5 分鐘執行
public void autoUpdateBannerStatus() {
    // 檢查並自動上下架
}
```

### 3. 店家停用時的處理
- Banner 不自動下架（保留紀錄）
- 前台查詢時過濾掉已停用店家的 Banner
- 後台查詢時仍可看到（方便管理）

### 4. 權限控管
- Admin 才能操作 Banner
- StoreOwner/StoreEditor 無權限
- 前台 API 無需登入

---

## 🎯 成功標準

- ✅ Admin 可新增/編輯/刪除 Banner
- ✅ Admin 可手動排序 Banner
- ✅ Admin 可設定上下架排程
- ✅ 前台正確顯示 ACTIVE 的 Banner
- ✅ 點擊 Banner 跳轉到店家頁面
- ✅ 店家停用時 Banner 不顯示於前台
- ✅ StoreOwner 無法操作 Banner（403）
- ✅ 所有 API 返回統一格式（ApiResponse）

---

## 📚 參考文件

- banner.prompt.md - Banner 模組需求定義
- copilot-instructions.md - 專案開發規範
- FRONTEND_BACKEND_STORE_API_SEPARATION.md - 前後台分離架構

---

**預計完成時間**：2~3 小時  
**風險評估**：低（架構已成熟，遵循既有模式）  
**優先級**：高（核心付費功能）
