# 📋 News & Banner Service/Controller 實作指南

## 前置作業確認

✅ **請先完成**：
1. 執行 `DDL_news_banner.sql` 建立 news 表
2. 執行 `mvn mybatis-generator:generate` 生成 Mapper/Example
3. 確認以下檔案已生成：
   - `NewsMapper.java` / `NewsMapper.xml` / `NewsExample.java`
   - `BannerMapper.java` / `BannerMapper.xml` / `BannerExample.java`

---

## 📦 檔案清單（需建立）

### Service 層
- [ ] `service/NewsService.java` ✅ 已建立
- [ ] `service/impl/NewsServiceImpl.java` ⬅️ 待建立
- [ ] `service/BannerService.java` ⬅️ 待建立
- [ ] `service/impl/BannerServiceImpl.java` ⬅️ 待建立

### Controller 層（後台）
- [ ] `controller/admin/AdminNewsController.java` ⬅️ 待建立
- [ ] `controller/admin/AdminBannerController.java` ⬅️ 待建立

### Controller 層（前台）
- [ ] `controller/api/NewsController.java` ⬅️ 待建立
- [ ] `controller/api/BannerController.java` ⬅️ 待建立

---

## 🔧 NewsServiceImpl 實作要點

### 依賴注入
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class NewsServiceImpl implements NewsService {
    
    private final NewsMapper newsMapper;
    private final AdminUserMapper adminUserMapper; // 用於取得建立者資訊
}
```

### 查詢列表（queryNews）
```java
@Override
public List<NewsRes> queryNews(QueryReq<NewsCondition> req) {
    NewsCondition condition = req != null ? req.getCondition() : null;
    
    NewsExample example = new NewsExample();
    NewsExample.Criteria criteria = example.createCriteria();
    
    // 動態條件
    if (condition != null) {
        if (condition.getTitle() != null && !condition.getTitle().isEmpty()) {
            criteria.andTitleLike("%" + condition.getTitle() + "%");
        }
        if (condition.getStatus() != null) {
            criteria.andStatusEqualTo(condition.getStatus());
        }
        if (condition.getCreatedAtStart() != null) {
            criteria.andCreatedAtGreaterThanOrEqualTo(condition.getCreatedAtStart());
        }
        if (condition.getCreatedAtEnd() != null) {
            criteria.andCreatedAtLessThanOrEqualTo(condition.getCreatedAtEnd());
        }
    }
    
    // 排序：預設按建立時間降序
    if (req != null && req.getSortBy() != null) {
        String order = req.getSortOrder() != null ? req.getSortOrder() : "DESC";
        example.setOrderByClause(req.getSortBy() + " " + order);
    } else {
        example.setOrderByClause("created_at DESC");
    }
    
    List<News> newsList = newsMapper.selectByExample(example);
    return newsList.stream().map(this::convertToRes).collect(Collectors.toList());
}
```

### 新增（createNews）
```java
@Override
@Transactional
public NewsRes createNews(NewsCreateReq req) {
    String currentUserId = SecurityUtils.getCurrentAdminUserId();
    
    News news = new News();
    news.setId(UUID.randomUUID().toString());
    news.setTitle(req.getTitle());
    news.setContent(req.getContent());
    news.setImageUrl(req.getImageUrl());
    news.setStatus(req.getStatus() != null ? req.getStatus() : "DRAFT");
    news.setScheduledAt(req.getScheduledAt());
    news.setEndTime(req.getEndTime());
    news.setCreatedBy(currentUserId);
    news.setCreatedAt(LocalDateTime.now());
    news.setUpdatedAt(LocalDateTime.now());
    
    newsMapper.insert(news);
    
    log.info("✅ 新增最新消息成功: id={}, title={}", news.getId(), news.getTitle());
    return convertToRes(news);
}
```

### 上下架（publishNews / unpublishNews）
```java
@Override
@Transactional
public NewsRes publishNews(String id) {
    News news = newsMapper.selectByPrimaryKey(id);
    if (news == null) {
        throw new BusinessException("最新消息不存在");
    }
    
    news.setStatus("PUBLISHED");
    news.setScheduledAt(LocalDateTime.now()); // 更新上架時間
    news.setUpdatedAt(LocalDateTime.now());
    newsMapper.updateByPrimaryKey(news);
    
    log.info("✅ 上架最新消息: id={}", id);
    return convertToRes(news);
}

@Override
@Transactional
public NewsRes unpublishNews(String id) {
    News news = newsMapper.selectByPrimaryKey(id);
    if (news == null) {
        throw new BusinessException("最新消息不存在");
    }
    
    news.setStatus("ARCHIVED");
    news.setEndTime(LocalDateTime.now()); // 更新下架時間
    news.setUpdatedAt(LocalDateTime.now());
    newsMapper.updateByPrimaryKey(news);
    
    log.info("✅ 下架最新消息: id={}", id);
    return convertToRes(news);
}
```

### 前台查詢（getPublishedNews）
```java
@Override
public List<NewsRes> getPublishedNews(Integer limit) {
    NewsExample example = new NewsExample();
    example.createCriteria().andStatusEqualTo("PUBLISHED");
    example.setOrderByClause("scheduled_at DESC");
    
    List<News> newsList = newsMapper.selectByExample(example);
    
    // 限制數量
    if (limit != null && limit > 0 && newsList.size() > limit) {
        newsList = newsList.subList(0, limit);
    }
    
    return newsList.stream().map(this::convertToRes).collect(Collectors.toList());
}
```

### Entity 轉 DTO（convertToRes）
```java
private NewsRes convertToRes(News news) {
    NewsRes res = new NewsRes();
    res.setId(news.getId());
    res.setTitle(news.getTitle());
    res.setContent(news.getContent());
    res.setImageUrl(news.getImageUrl());
    res.setStatus(news.getStatus());
    res.setStatusName(getStatusName(news.getStatus()));
    res.setScheduledAt(news.getScheduledAt());
    res.setEndTime(news.getEndTime());
    res.setCreatedBy(news.getCreatedBy());
    res.setCreatedAt(news.getCreatedAt());
    res.setUpdatedAt(news.getUpdatedAt());
    return res;
}

private String getStatusName(String status) {
    switch (status) {
        case "DRAFT": return "草稿";
        case "PUBLISHED": return "已上架";
        case "ARCHIVED": return "已下架";
        default: return status;
    }
}
```

---

## 🔧 BannerServiceImpl 實作要點

### 依賴注入
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class BannerServiceImpl implements BannerService {
    
    private final BannerMapper bannerMapper;
    private final StoreMapper storeMapper; // 用於檢查店家狀態
}
```

### 查詢列表（queryBanners）
```java
@Override
public List<BannerRes> queryBanners(QueryReq<BannerCondition> req) {
    BannerCondition condition = req != null ? req.getCondition() : null;
    
    BannerExample example = new BannerExample();
    BannerExample.Criteria criteria = example.createCriteria();
    
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
    
    // 排序：預設按 order_num 升序
    if (req != null && req.getSortBy() != null) {
        String order = req.getSortOrder() != null ? req.getSortOrder() : "ASC";
        example.setOrderByClause(req.getSortBy() + " " + order);
    } else {
        example.setOrderByClause("order_num ASC, created_at DESC");
    }
    
    List<Banner> banners = bannerMapper.selectByExample(example);
    return banners.stream().map(this::convertToRes).collect(Collectors.toList());
}
```

### 新增（createBanner）
```java
@Override
@Transactional
public BannerRes createBanner(BannerCreateReq req) {
    // 檢查店家是否存在
    Store store = storeMapper.selectByPrimaryKey(req.getStoreId());
    if (store == null) {
        throw new BusinessException("店家不存在");
    }
    
    Banner banner = new Banner();
    banner.setId(UUID.randomUUID().toString());
    banner.setStoreId(req.getStoreId());
    banner.setTitle(req.getTitle());
    banner.setImageUrl(req.getImageUrl());
    banner.setOrderNum(req.getOrderNum() != null ? req.getOrderNum() : 0);
    banner.setStatus(req.getStatus() != null ? req.getStatus() : "UNPUBLISHED");
    banner.setStartTime(req.getStartTime());
    banner.setEndTime(req.getEndTime());
    banner.setCreatedAt(LocalDateTime.now());
    banner.setUpdatedAt(LocalDateTime.now());
    
    bannerMapper.insert(banner);
    
    log.info("✅ 新增 Banner 成功: id={}, storeId={}", banner.getId(), banner.getStoreId());
    return convertToRes(banner);
}
```

### 更新排序（updateOrder）
```java
@Override
@Transactional
public BannerRes updateOrder(String id, Integer orderNum) {
    Banner banner = bannerMapper.selectByPrimaryKey(id);
    if (banner == null) {
        throw new BusinessException("Banner 不存在");
    }
    
    banner.setOrderNum(orderNum);
    banner.setUpdatedAt(LocalDateTime.now());
    bannerMapper.updateByPrimaryKey(banner);
    
    log.info("✅ 更新 Banner 排序: id={}, orderNum={}", id, orderNum);
    return convertToRes(banner);
}
```

### 前台輪播查詢（getCarouselBanners）
```java
@Override
public List<BannerRes> getCarouselBanners() {
    BannerExample example = new BannerExample();
    BannerExample.Criteria criteria = example.createCriteria();
    
    // 只查詢已上架的 Banner
    criteria.andStatusEqualTo("PUBLISHED");
    
    // 檢查時間範圍
    LocalDateTime now = LocalDateTime.now();
    criteria.andStartTimeLessThanOrEqualTo(now);
    // end_time 可以為 null（無限期）
    
    example.setOrderByClause("order_num ASC");
    
    List<Banner> banners = bannerMapper.selectByExample(example);
    
    // 過濾掉店家已停用的 Banner
    return banners.stream()
            .filter(banner -> {
                Store store = storeMapper.selectByPrimaryKey(banner.getStoreId());
                return store != null && "ACTIVE".equals(store.getStatus());
            })
            .map(this::convertToRes)
            .collect(Collectors.toList());
}
```

### Entity 轉 DTO（convertToRes）
```java
private BannerRes convertToRes(Banner banner) {
    BannerRes res = new BannerRes();
    res.setId(banner.getId());
    res.setStoreId(banner.getStoreId());
    res.setTitle(banner.getTitle());
    res.setImageUrl(banner.getImageUrl());
    res.setOrderNum(banner.getOrderNum());
    res.setStatus(banner.getStatus());
    res.setStatusName(getStatusName(banner.getStatus()));
    res.setStartTime(banner.getStartTime());
    res.setEndTime(banner.getEndTime());
    res.setCreatedAt(banner.getCreatedAt());
    res.setUpdatedAt(banner.getUpdatedAt());
    
    // 查詢店家名稱
    Store store = storeMapper.selectByPrimaryKey(banner.getStoreId());
    if (store != null) {
        res.setStoreName(store.getName());
    }
    
    return res;
}

private String getStatusName(String status) {
    return "PUBLISHED".equals(status) ? "已上架" : "未上架";
}
```

---

## 🎮 AdminNewsController 實作（後台）

```java
package com.group.admin.controller.admin;

@RestController
@RequestMapping("/admin/news")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "後台最新消息管理", description = "Admin News Management API")
@PreAuthorize("hasRole('ADMIN')")
public class AdminNewsController {
    
    private final NewsService newsService;
    
    /**
     * 查詢最新消息列表
     */
    @PostMapping("/list")
    @Operation(summary = "查詢最新消息列表", description = "支援條件查詢與排序")
    public ResponseEntity<List<NewsRes>> queryNews(
            @RequestBody(required = false) QueryReq<NewsCondition> req) {
        log.info("🔍 [後台] 查詢最新消息列表: {}", req);
        List<NewsRes> result = newsService.queryNews(req);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 查詢最新消息詳情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查詢最新消息詳情")
    public ResponseEntity<NewsRes> getNews(@PathVariable String id) {
        log.info("🔍 [後台] 查詢最新消息詳情: id={}", id);
        NewsRes result = newsService.getNewsById(id);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 新增最新消息
     */
    @PostMapping
    @Operation(summary = "新增最新消息")
    public ResponseEntity<NewsRes> createNews(@Valid @RequestBody NewsCreateReq req) {
        log.info("➕ [後台] 新增最新消息: title={}", req.getTitle());
        NewsRes result = newsService.createNews(req);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 更新最新消息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新最新消息")
    public ResponseEntity<NewsRes> updateNews(
            @PathVariable String id,
            @Valid @RequestBody NewsUpdateReq req) {
        log.info("✏️ [後台] 更新最新消息: id={}", id);
        NewsRes result = newsService.updateNews(id, req);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 刪除最新消息
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "刪除最新消息")
    public ResponseEntity<Void> deleteNews(@PathVariable String id) {
        log.info("🗑️ [後台] 刪除最新消息: id={}", id);
        newsService.deleteNews(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 上架最新消息
     */
    @PutMapping("/{id}/publish")
    @Operation(summary = "上架最新消息")
    public ResponseEntity<NewsRes> publishNews(@PathVariable String id) {
        log.info("📢 [後台] 上架最新消息: id={}", id);
        NewsRes result = newsService.publishNews(id);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 下架最新消息
     */
    @PutMapping("/{id}/unpublish")
    @Operation(summary = "下架最新消息")
    public ResponseEntity<NewsRes> unpublishNews(@PathVariable String id) {
        log.info("📥 [後台] 下架最新消息: id={}", id);
        NewsRes result = newsService.unpublishNews(id);
        return ResponseEntity.ok(result);
    }
}
```

---

## 🎮 NewsController 實作（前台）

```java
package com.group.admin.controller.api;

@RestController
@RequestMapping("/news")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "前台最新消息", description = "Frontend News API")
public class NewsController {
    
    private final NewsService newsService;
    
    /**
     * 查詢最新消息列表（僅已上架）
     */
    @GetMapping("/list")
    @Operation(summary = "查詢最新消息列表", description = "僅返回已上架的最新消息")
    public ResponseEntity<List<NewsRes>> getNewsList(
            @RequestParam(required = false) Integer limit) {
        log.info("🔍 [前台] 查詢最新消息列表: limit={}", limit);
        List<NewsRes> result = newsService.getPublishedNews(limit);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 查詢最新消息詳情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查詢最新消息詳情")
    public ResponseEntity<NewsRes> getNewsDetail(@PathVariable String id) {
        log.info("🔍 [前台] 查詢最新消息詳情: id={}", id);
        NewsRes result = newsService.getNewsById(id);
        
        // 前台只能查看已上架的
        if (!"PUBLISHED".equals(result.getStatus())) {
            throw new BusinessException("最新消息不存在或已下架");
        }
        
        return ResponseEntity.ok(result);
    }
}
```

---

## 📌 關鍵注意事項

### 1. 權限控管
- **後台 Controller**：`@PreAuthorize("hasRole('ADMIN')")`
- **前台 Controller**：無權限控管（公開）

### 2. 狀態定義
- **News**：`DRAFT`, `PUBLISHED`, `ARCHIVED`
- **Banner**：`PUBLISHED`, `UNPUBLISHED`

### 3. 前台查詢限制
- News：僅返回 `status = 'PUBLISHED'`
- Banner：僅返回 `status = 'PUBLISHED'` 且店家 `status = 'ACTIVE'`

### 4. 排序規則
- News：預設 `created_at DESC`（最新優先）
- Banner：預設 `order_num ASC`（手動排序）

### 5. 店家關聯檢查
- Banner 必須綁定有效店家
- 前台輪播需過濾停用店家

---

## 🚀 實作完成後

1. **編譯專案**：`mvn clean compile`
2. **啟動專案**：`mvn spring-boot:run`
3. **測試 API**：使用 Postman 測試所有端點

需要我提供完整的檔案內容嗎？
