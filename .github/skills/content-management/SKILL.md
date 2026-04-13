---
name: content-management
description: "塃告/跗寶/新聞内容管理。塃告上傳、誦句配置、新聞發布、內容查詢、前台顯示邏輯。"
---

# 內容管理（Banner / Marquee / News）

## When to Use
- 新增或修改 Banner（輪播）
- 修改跑馬燈（Marquee）內容
- 管理最新消息（News）
- 了解各模組的欄位與顯示規則
## 核心原則
- **統一節目變統**：Content 为理江幕、Marquee 等的一般化主設，接受 `contentType` 區分
- **前後台分離**：上傳排版（店家）與前台顯示（使用者）分離
- **狀態管理**：Banner 有 Active/Inactive 狀態，前台只顯示 Active
- **順序控制**：甏配 `order` 或 `sequence` 控制顯示順序
---

## 三個模組速覽

| 模組 | 資料表 | 前台 API | 後台 API |
|------|-------|---------|---------|
| Banner | `banner` | `GET /api/banners` | `/admin/banners/**` |
| Marquee | `marquee` | `GET /api/marquee` | `/admin/marquee/**` |
| News | `news` | `GET /api/news/published` | `/admin/news/**` |

---

## Banner 模組

### 欄位說明
```java
banner.getStoreId()       // 綁定的店家（必填）
banner.getImageUrl()      // 圖片 URL（S3 上傳後填入）
banner.getLinkUrl()       // 點擊跳轉的連結
banner.getSortOrder()     // 排序（數字越小越前）
banner.getIsActive()      // 1=顯示, 0=隱藏
```

### 前台查詢（只回傳啟用中）
```java
BannerExample example = new BannerExample();
example.createCriteria().andIsActiveEqualTo(1);
example.setOrderByClause("sort_order ASC");
List<Banner> banners = bannerMapper.selectByExample(example);
```

### 與店家連動
- Banner 必須綁定一個啟用中的 Store
- 店家停用時，相關 Banner 建議一併停用
- 前台點擊 Banner → 跳轉到對應店家頁面

---

## Marquee（跑馬燈）模組

### 欄位說明
```java
marquee.getContent()      // 跑馬燈文字內容
marquee.getSortOrder()    // 排序
marquee.getIsActive()     // 1=顯示, 0=隱藏
marquee.getStartTime()    // 顯示開始時間（null=立即）
marquee.getEndTime()      // 顯示結束時間（null=不限）
```

### 前台查詢
```java
MarqueeExample example = new MarqueeExample();
example.createCriteria().andIsActiveEqualTo(1);
example.setOrderByClause("sort_order ASC");
// 前台顯示所有啟用中的跑馬燈，拼成一條滾動文字
```

### 時間控制
```java
// 判斷是否在有效期內
public boolean isEffective(Marquee marquee) {
    LocalDateTime now = LocalDateTime.now();
    boolean afterStart = marquee.getStartTime() == null || !now.isBefore(marquee.getStartTime());
    boolean beforeEnd = marquee.getEndTime() == null || now.isBefore(marquee.getEndTime());
    return marquee.getIsActive() != null && marquee.getIsActive() == 1 && afterStart && beforeEnd;
}
```

---

## News（最新消息）模組

### 欄位說明
```java
news.getTitle()           // 標題
news.getContent()         // 富文本內容（HTML）
news.getImageUrl()        // 封面圖片
news.getCategory()        // 分類（NEWS / ANNOUNCEMENT / PROMOTION）
news.getIsPublished()     // 1=已發布, 0=草稿
news.getPublishedAt()     // 發布時間
news.getViewCount()       // 瀏覽次數（只增不減）
```

### 前台查詢（只回傳已發布）
```java
NewsExample example = new NewsExample();
example.createCriteria()
    .andIsPublishedEqualTo(1)
    .andPublishedAtLessThanOrEqualTo(LocalDateTime.now());
example.setOrderByClause("published_at DESC");
```

### 瀏覽次數遞增
```java
// GET /api/news/{id} 被呼叫時
news.setViewCount(news.getViewCount() != null ? news.getViewCount() + 1 : 1);
newsMapper.updateByPrimaryKey(news);
```

---

## 通用操作規範

### 排序調整
```java
// PUT /admin/banners/{id}/sort
// Body: { "sortOrder": 3 }
banner.setSortOrder(req.getSortOrder());
banner.setUpdatedAt(LocalDateTime.now());
bannerMapper.updateByPrimaryKey(banner);
```

### 啟用/停用
```java
// PUT /admin/banners/{id}/toggle
banner.setIsActive(banner.getIsActive() == 1 ? 0 : 1);
banner.setUpdatedAt(LocalDateTime.now());
bannerMapper.updateByPrimaryKey(banner);
```

### 圖片上傳
圖片上傳統一使用 S3 上傳 API：
```
POST /admin/upload/banner   → Banner 圖片
POST /admin/upload/news     → News 封面圖
```

---

## ⚠️ 禁止操作

- ❌ 不要讓前台看到 `isPublished=0` 的 News
- ❌ Banner 圖片 URL 不要手動填寫，必須通過 S3 上傳 API 取得
- ❌ 不要讓店家停用後 Banner 還顯示（啟用/停用時同步更新）
- ❌ 修改 viewCount 時不要直接讓前端傳值（後端自動遞增）
