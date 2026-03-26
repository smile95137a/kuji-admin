# Data Model: 最新消息管理 (News Management)

**Feature**: 007-news-management  
**Date**: 2026-03-22  
**Phase**: 1 — Design

---

## 實體：NewsArticle（DB 資料表：`news`）

### 用途
儲存平台級最新消息與公告。管理員建立並管理文章；
玩家在前台瀏覽已發布的文章。

---

## DB Schema

```sql
CREATE TABLE `news` (
  `id`           VARCHAR(36)   NOT NULL COMMENT 'UUID primary key',
  `title`        VARCHAR(255)  NOT NULL COMMENT '標題',
  `content`      LONGTEXT      NULL     COMMENT '內文（長文）',
  `image_url`    VARCHAR(512)  NULL     COMMENT '封面圖片 URL（選配）',
  `status`       VARCHAR(20)   NOT NULL DEFAULT 'DRAFT'
                               COMMENT '狀態：DRAFT | PUBLISHED | UNPUBLISHED',
  `category`     VARCHAR(50)   NULL     DEFAULT 'ANNOUNCEMENT'
                               COMMENT '分類：ANNOUNCEMENT | EVENT | SYSTEM',
  `important`    TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '是否重要提醒',
  `scheduled_at` DATETIME      NULL     COMMENT '排程上架時間 (published_at)',
  `end_time`     DATETIME      NULL     COMMENT '排程下架時間 (unpublished_at)',
  `created_by`   VARCHAR(36)   NULL     COMMENT '建立者 Admin UUID',
  `created_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
  `updated_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
                               ON UPDATE CURRENT_TIMESTAMP COMMENT '最後修改時間',
  PRIMARY KEY (`id`),
  KEY `idx_news_status` (`status`),
  KEY `idx_news_scheduled_at` (`scheduled_at`),
  KEY `idx_news_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='最新消息文章表';
```

---

## 欄位說明

| 欄位 | Java 型別 | DB 型別 | 可為空 | 描述 |
|-------|-----------|---------|----------|-------------|
| `id` | `String` | `VARCHAR(36)` | NO | UUID，由 Service 於建立時生成 |
| `title` | `String` | `VARCHAR(255)` | NO | 文章標題，不可空 |
| `content` | `String` | `LONGTEXT` | YES | 內文（長文），MyBatis BLOB 欄位 |
| `image_url` | `String` | `VARCHAR(512)` | YES | 封面圖片 URL |
| `status` | `String` | `VARCHAR(20)` | NO | DRAFT \| PUBLISHED \| UNPUBLISHED |
| `category` | `String` | `VARCHAR(50)` | YES | ANNOUNCEMENT \| EVENT \| SYSTEM |
| `important` | `Boolean` | `TINYINT(1)` | NO | 重要提醒標記 |
| `scheduled_at` | `LocalDateTime` | `DATETIME` | YES | 排程上架時間（兼作 publishedAt） |
| `end_time` | `LocalDateTime` | `DATETIME` | YES | 排程下架時間 |
| `created_by` | `String` | `VARCHAR(36)` | YES | 建立者 Admin UUID（from JWT） |
| `created_at` | `LocalDateTime` | `DATETIME` | NO | 建立時間，自動設定 |
| `updated_at` | `LocalDateTime` | `DATETIME` | NO | 最後修改時間，自動更新 |

---

## 狀態機

```
         ┌──────────────────────────────────────┐
         │              DRAFT                   │
         │  （建立時的初始狀態）                │
         └───────────┬──────────────────────────┘
                     │  手動發布（POST /{id}/publish）
                     │  或 scheduled_at <= NOW()（@Scheduled）
                     ▼
         ┌──────────────────────────────────────┐
         │            PUBLISHED                 │
         │  （公開可見於 /api/news）            │
         └───────────┬──────────────────────────┘
                     │  手動下架（POST /{id}/unpublish）
                     │  或 end_time <= NOW()（@Scheduled）
                     ▼
         ┌──────────────────────────────────────┐
         │           UNPUBLISHED                │
         │  （對公開端點隱藏；可在              │
         │   後台列表中看見）                   │
         └──────────────────────────────────────┘

備注：
- UNPUBLISHED → PUBLISHED：透過 PUT /admin/news/{id}（設定 status=PUBLISHED）
  或 POST /admin/news/{id}/publish
- 任何狀態 → DELETE：透過 DELETE /admin/news/{id} 永久刪除
- 狀態遷移：現有 ARCHIVED 資料列 → UPDATE news SET status='UNPUBLISHED'
```

---

## 驗證規則

| 規則 | 欄位 | 限制 | 錯誤訊息 |
|------|-------|-----------|---------------|
| 標題必填 | `title` | `@NotBlank` | "標題不能為空" |
| 建立時內文必填 | `content` | `@NotBlank` | "內文不能為空" |
| 狀態值 | `status` | Enum: DRAFT, PUBLISHED, UNPUBLISHED | "無效狀態" |
| 排程邏輯 | `scheduled_at`, `end_time` | 若兩者都設定：`end_time` 必須晚於 `scheduled_at` | "下架時間必須晚於上架時間" |
| 圖片 URL 格式 | `image_url` | 選填；若有填寫則須為有效的 URL 格式 | "圖片網址格式不正確" |

---

## Java 實體：`News.java`（已存在 — 無需修改）

```java
// com.group.admin.entity.News
public class News {
    private String id;
    private String title;
    private String imageUrl;
    private String status;       // "DRAFT" | "PUBLISHED" | "UNPUBLISHED"
    private String category;     // "ANNOUNCEMENT" | "EVENT" | "SYSTEM"
    private Boolean important;
    private LocalDateTime scheduledAt;  // published_at
    private LocalDateTime endTime;      // unpublished_at
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String content;      // BLOB — in ResultMapWithBLOBs only
    // getters/setters...
}
```

---

## Java 回應 DTO：`NewsRes.java`（已存在 — 小幅增強）

```java
// com.group.admin.res.news.NewsRes
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class NewsRes {
    private String id;
    private String title;
    private String content;
    private String imageUrl;
    private String status;           // "DRAFT" | "PUBLISHED" | "UNPUBLISHED"
    private String statusName;       // "草稿" | "已發布" | "已下架"
    private String category;
    private String categoryName;
    private Boolean important;
    private LocalDateTime scheduledAt;   // also serves as publishedAt
    private LocalDateTime endTime;       // also serves as unpublishedAt
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

---

## 自動排程邏輯（新增 — 位於 `ScheduledTasks.java`）

```java
// Runs every 60 seconds
@Scheduled(fixedRate = 60000)
public void autoPublishNews() {
    // Find DRAFT articles where scheduled_at <= NOW()
    // Update status to PUBLISHED
}

@Scheduled(fixedRate = 60000)
public void autoUnpublishNews() {
    // Find PUBLISHED articles where end_time <= NOW()
    // Update status to UNPUBLISHED
}
```

**待新增的 Service 方法**：
```java
// NewsService.java
int autoPublishScheduledNews();   // 回傳已發布的文章數量
int autoUnpublishExpiredNews();   // 回傳已下架的文章數量
```

**待新增的 Mapper 查詢**（位於 `NewsMapper.java` + `NewsMapper.xml`）：
```java
// 自訂查詢 — 非 MBG 生成
int autoPublishNews();    // UPDATE news SET status='PUBLISHED', updated_at=NOW() WHERE ...
int autoUnpublishNews();  // UPDATE news SET status='UNPUBLISHED', updated_at=NOW() WHERE ...
```

---

## 資料遷移

```sql
-- 部署前執行一次，將現有資料的狀態命名與新規範對齊
UPDATE news SET status = 'UNPUBLISHED' WHERE status = 'ARCHIVED';
```

---

## 索引

| 索引 | 欄位 | 用途 |
|-------|---------|---------|
| PRIMARY | `id` | 主鍵查詢 |
| `idx_news_status` | `status` | 篩選已發布文章（公開列表） |
| `idx_news_scheduled_at` | `scheduled_at` | 排程查詢，依發布日期排序 |
| `idx_news_created_at` | `created_at` | 後台列表預設排序 |
