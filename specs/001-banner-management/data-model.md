# Data Model: 橫幅廣告管理 (Banner Management)

**功能**: `001-banner-management`  
**階段**: 1 — 設計  
**日期**: 2026-03-22

---

## 實體

### Banner

首頁輪播廣告的主要資料表。

| 欄位 | Java 欄位 | 類型 | 可為 NULL | 備註 |
|--------|-----------|------|----------|-------|
| `id` | `id` | `VARCHAR(36)` PK | NOT NULL | UUID，於插入時生成 |
| `store_id` | `storeId` | `VARCHAR(36)` FK→`store.id` | NOT NULL | FR-002：每則廣告必須連結一個店家 |
| `title` | `title` | `VARCHAR(200)` | NULL | 選用的顯示標題 |
| `image_url` | `imageUrl` | `VARCHAR(500)` | NOT NULL | 完整 S3/CDN URL，透過 `/admin/upload` 上傳 |
| `link_url` | `linkUrl` | `VARCHAR(500)` | NULL | 計算值為 `/stores/{storeId}`；FR-004：不允許外部 URL |
| `order_num` | `orderNum` | `INT` | NOT NULL | 預設 0；輪播排序鍵（ASC）；相同順序時次要排序：`created_at ASC` |
| `status` | `status` | `VARCHAR(20)` | NOT NULL | `DRAFT` / `PUBLISHED` / `UNPUBLISHED` |
| `start_time` | `startTime` | `DATETIME` | NULL | 排程發布時間；NULL = 發布後立即生效 |
| `end_time` | `endTime` | `DATETIME` | NULL | 排程下架時間；NULL = 永不到期 |
| `created_at` | `createdAt` | `DATETIME` | NOT NULL | 插入時設定（Asia/Taipei 時區） |
| `updated_at` | `updatedAt` | `DATETIME` | NOT NULL | 插入與更新時設定 |

**索引**：
- PRIMARY KEY (`id`)
- INDEX `idx_banner_store_id` (`store_id`)
- INDEX `idx_banner_status_order` (`status`, `order_num`, `created_at`) — 優化公開輪播查詢
- INDEX `idx_banner_schedule` (`status`, `start_time`, `end_time`) — 優化排程執行查詢

**DDL（增量 — 若資料表尚未存在則執行）**：
```sql
CREATE TABLE IF NOT EXISTS `banner` (
  `id`          VARCHAR(36)  NOT NULL,
  `store_id`    VARCHAR(36)  NOT NULL,
  `title`       VARCHAR(200) DEFAULT NULL,
  `image_url`   VARCHAR(500) NOT NULL,
  `link_url`    VARCHAR(500) DEFAULT NULL,
  `order_num`   INT          NOT NULL DEFAULT 0,
  `status`      VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
  `start_time`  DATETIME     DEFAULT NULL,
  `end_time`    DATETIME     DEFAULT NULL,
  `created_at`  DATETIME     NOT NULL,
  `updated_at`  DATETIME     NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_banner_store_id`       (`store_id`),
  INDEX `idx_banner_status_order`   (`status`, `order_num`, `created_at`),
  INDEX `idx_banner_schedule`       (`status`, `start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

### Store（參考 — 現有，本功能僅讀取）

Banner 模組使用的相關欄位：

| 欄位 | Java 欄位 | 類型 | 備註 |
|--------|-----------|------|-------|
| `id` | `id` | `VARCHAR(36)` PK | UUID |
| `store_name` | `storeName` | `VARCHAR(200)` | 顯示於管理員廣告列表 |
| `logo_url` | `logoUrl` | `VARCHAR(500)` | 選用，顯示於輪播項目旁邊 |
| `status` | `status` | `VARCHAR(20)` | `ACTIVE` / `INACTIVE`；篩選公開輪播（FR-008） |

`store` 資料表無需變更結構。

---

## 關聯

```
Store (1) ────────< Banner (many)
  id ←──── store_id

One store may have zero or more banners.
One banner must belong to exactly one store (NOT NULL FK, FR-002).
```

---

## 狀態機：Banner 狀態

```
                 ┌──────────────────────────────────────────────────┐
                 │                    Admin: publish                 │
                 │          OR startTime reached (scheduler)         │
                 ▼                                                   │
   [DRAFT] ─────────────────────────────────────► [PUBLISHED] ──────┘
                                                       │
                                                       │  Admin: unpublish
                                                       │  OR endTime reached (scheduler)
                                                       ▼
                                               [UNPUBLISHED]
                                                       │
                                                       │  Admin: re-publish (manual)
                                                       └──────────────► [PUBLISHED]

   New banner defaults to: DRAFT
   Admin may create directly as PUBLISHED (status field in BannerCreateReq)
```

### 狀態轉換規則

| 轉換 | 執行者 | 條件 |
|-----------|-------|-----------|
| DRAFT → PUBLISHED | 管理員（明確操作） | 建立/更新請求中 `status` 設為 PUBLISHED |
| DRAFT → PUBLISHED | 排程器 | `startTime` ≤ NOW() 且狀態為 DRAFT |
| PUBLISHED → UNPUBLISHED | 管理員（明確操作） | PUT 帶入 `status = UNPUBLISHED` |
| PUBLISHED → UNPUBLISHED | 排程器 | `endTime` < NOW() 且狀態為 PUBLISHED |
| UNPUBLISHED → PUBLISHED | 管理員（明確操作） | PUT 帶入 `status = PUBLISHED` |

---

## 驗證規則

| 欄位 | 規則 | 錯誤訊息 |
|-------|------|--------------|
| `storeId` | 必填；必須參照現有且未刪除的店家 | `店家不存在或已刪除` |
| `imageUrl` | 必填；必須為非空字串 | `廣告圖片為必填` |
| `orderNum` | 選用；預設為 0；必須 ≥ 0 | `顯示順序不得為負數` |
| `startTime` | 選用；若提供則必須早於 `endTime` | `發佈時間必須早於下架時間` |
| `endTime` | 選用；若提供了 `startTime`，`endTime` 必須晚於 `startTime` | `下架時間必須晚於發佈時間` |
| `status` | 必須為以下之一：`DRAFT`、`PUBLISHED`、`UNPUBLISHED` | `狀態值無效` |

---

## 請求 / 回應 DTO

### BannerCreateReq

```java
public class BannerCreateReq {
    @NotBlank
    private String storeId;          // required
    private String title;            // optional
    @NotBlank
    private String imageUrl;         // required — pre-uploaded S3 URL
    private Integer orderNum;        // optional, default 0
    private String status;           // optional, default "DRAFT"
    private LocalDateTime startTime; // optional
    private LocalDateTime endTime;   // optional
}
```

### BannerUpdateReq

```java
public class BannerUpdateReq {
    private String storeId;          // optional — reassign store
    private String title;
    private String imageUrl;
    private Integer orderNum;
    private String status;           // DRAFT / PUBLISHED / UNPUBLISHED
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
```

### BannerRes

```java
public class BannerRes {
    private String id;
    private String storeId;
    private String storeName;        // joined from store
    private String storeLogoUrl;     // joined from store (optional)
    private String title;
    private String imageUrl;
    private String linkUrl;          // computed: "/stores/{storeId}"
    private Integer orderNum;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### BannerCondition (admin list filter)

```java
public class BannerCondition {
    private String storeId;   // filter by store
    private String status;    // filter by status
    private String keyword;   // search in title
}
```

---

## 排程執行邏輯

在 `ScheduledTasks.java` 中每 **60 秒**執行一次（`@Scheduled(fixedRate = 60000)`）：

```
1. Auto-publish:
   UPDATE banner SET status = 'PUBLISHED', updated_at = NOW()
   WHERE status IN ('DRAFT', 'UNPUBLISHED')
     AND start_time IS NOT NULL
     AND start_time <= NOW()
     AND (end_time IS NULL OR end_time > NOW())

2. Auto-unpublish:
   UPDATE banner SET status = 'UNPUBLISHED', updated_at = NOW()
   WHERE status = 'PUBLISHED'
     AND end_time IS NOT NULL
     AND end_time < NOW()
```

---

## 公開輪播查詢

由 `BannerMapper.selectActiveBanners()` 執行：

```sql
SELECT
    b.id,
    b.store_id,
    s.store_name,
    s.logo_url     AS store_logo_url,
    b.title,
    b.image_url,
    CONCAT('/stores/', b.store_id) AS link_url,
    b.order_num,
    b.status,
    b.start_time,
    b.end_time,
    b.created_at,
    b.updated_at
FROM banner b
INNER JOIN store s ON b.store_id = s.id AND s.status = 'ACTIVE'
WHERE b.status = 'PUBLISHED'
  AND (b.start_time IS NULL OR b.start_time <= NOW())
  AND (b.end_time   IS NULL OR b.end_time   >= NOW())
ORDER BY b.order_num ASC, b.created_at ASC
```
