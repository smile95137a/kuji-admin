# Quickstart: 最新消息管理 (News Management)

**Feature**: 007-news-management  
**Date**: 2026-03-22

---

## 概覽

最新消息管理模組讓平台管理員能夠發布公告與消息文章。
玩家無需驗證即可瀏覽已發布的文章。排程自動上架與
自動下架由 Spring `@Scheduled` 背景任務處理。

---

## 前置條件

- Java 21 + Maven 3.8+
- MySQL 8.3 執行中（本機或 AWS RDS）
- Spring Boot 應用程式運行於連接埠 `8080`（預設）
- 有效的管理員 JWT Token（透過 `POST /admin/auth/login` 取得）

---

## 1. 資料庫設定

### 確認資料表存在

```sql
SHOW CREATE TABLE news;
```

### 建立資料表（若不存在）

```sql
CREATE TABLE IF NOT EXISTS `news` (
  `id`           VARCHAR(36)   NOT NULL,
  `title`        VARCHAR(255)  NOT NULL,
  `content`      LONGTEXT      NULL,
  `image_url`    VARCHAR(512)  NULL,
  `status`       VARCHAR(20)   NOT NULL DEFAULT 'DRAFT',
  `category`     VARCHAR(50)   NULL     DEFAULT 'ANNOUNCEMENT',
  `important`    TINYINT(1)    NOT NULL DEFAULT 0,
  `scheduled_at` DATETIME      NULL,
  `end_time`     DATETIME      NULL,
  `created_by`   VARCHAR(36)   NULL,
  `created_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_news_status` (`status`),
  KEY `idx_news_scheduled_at` (`scheduled_at`),
  KEY `idx_news_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 遷移現有的 ARCHIVED 狀態（若有資料）

```sql
-- 一次性遷移：將狀態命名與規格對齊
UPDATE news SET status = 'UNPUBLISHED' WHERE status = 'ARCHIVED';
```

---

## 2. 啟動伺服器

```bash
# 從專案根目錄
cd C:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin
mvn spring-boot:run

# 或直接執行 JAR
java -jar target/admin-*.jar
```

伺服器啟動位址：`http://localhost:8080`

---

## 3. 取得管理員 JWT Token

```bash
curl -X POST http://localhost:8080/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "yourpassword"}'
```

從回應中複製 `token`。在所有管理員請求中以 `Authorization: Bearer <token>` 方式使用。

---

## 4. 後台：建立消息文章

```bash
curl -X POST http://localhost:8080/admin/news \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "春節活動開跑！",
    "content": "春節期間推出限定活動，歡迎參與。",
    "imageUrl": "https://cdn.example.com/spring.jpg",
    "status": "DRAFT",
    "category": "EVENT",
    "important": false
  }'
```

**預期回應** `200 OK`：
```json
{
  "id": "<generated-uuid>",
  "title": "春節活動開跑！",
  "status": "DRAFT",
  "statusName": "草稿",
  ...
}
```

---

## 5. 後台：立即發布

```bash
# 將 <id> 替換為建立回應中的 UUID
curl -X POST http://localhost:8080/admin/news/<id>/publish \
  -H "Authorization: Bearer <token>"
```

**預期**：文章 `status` 變更為 `PUBLISHED`。

---

## 6. 後台：設定排程自動上架

```bash
# 將 scheduledAt 設為未來時間以啟用自動上架
curl -X PUT http://localhost:8080/admin/news/<id> \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "DRAFT",
    "scheduledAt": "2026-04-01T10:00:00",
    "endTime": "2026-04-30T23:59:59"
  }'
```

`@Scheduled` 任務（每 60 秒執行）會自動：
1. 當 `scheduled_at <= NOW()` 時發布文章
2. 當 `end_time <= NOW()` 時下架文章

---

## 7. 前台：瀏覽已發布消息

```bash
# 取得所有已發布文章
curl http://localhost:8080/api/news

# 取得最新 5 筆（首頁小工具用）
curl "http://localhost:8080/api/news?limit=5"
```

**預期**：回傳僅含 `status: "PUBLISHED"` 的 `NewsRes` 陣列。

---

## 8. 前台：取得文章詳情

```bash
curl http://localhost:8080/api/news/<id>
```

**預期**：回傳包含完整 `content` 的單筆 `NewsRes`。  
**錯誤**：若文章為草稿、已下架或不存在，回傳 `404`。

---

## 9. 後台：列出所有文章（含草稿）

```bash
# 所有文章
curl http://localhost:8080/admin/news \
  -H "Authorization: Bearer <token>"

# 依狀態篩選
curl "http://localhost:8080/admin/news?status=DRAFT" \
  -H "Authorization: Bearer <token>"
```

---

## 10. 後台：更新文章

```bash
curl -X PUT http://localhost:8080/admin/news/<id> \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "春節活動延長至三月！",
    "endTime": "2026-03-31T23:59:59"
  }'
```

僅更新所提供的欄位（部分更新）。

---

## 11. 後台：刪除文章

```bash
curl -X DELETE http://localhost:8080/admin/news/<id> \
  -H "Authorization: Bearer <token>"
```

回傳 `200 OK`，空回應主體。**無法復原。**

---

## 12. Swagger UI（API 探索介面）

開啟瀏覽器：`http://localhost:8080/swagger-ui/index.html`

- Tag: **後台-最新消息管理** — Admin CRUD endpoints
- Tag: **前台-最新消息** — Public browse endpoints

---

## 主要實作缺口（開發人員參考）

以下修改為使功能符合規格合約的必要項目：

| 缺口 | 檔案 | 所需修改 |
|-----|------|----------------|
| 自動上架排程 | `scheduler/ScheduledTasks.java` | 新增 `autoPublishNews()` + `autoUnpublishNews()` @Scheduled 方法 |
| 自動上架服務 | `service/NewsService.java` + `impl/` | 新增 `autoPublishScheduledNews()` + `autoUnpublishExpiredNews()` |
| 自動上架 Mapper | `mapper/NewsMapper.java` + XML | 新增批次 UPDATE 查詢 |
| 公開路徑 | `controller/api/NewsController.java` | 將 `@RequestMapping("/news")` 改為 `@RequestMapping("/api/news")` |
| 後台列表 GET | `controller/admin/AdminNewsController.java` | 新增 `GET /admin/news`，支援可選的 `?status=` 參數 |
| 狀態命名 | `service/impl/NewsServiceImpl.java` | 將所有狀態常數中的 `"ARCHIVED"` 改為 `"UNPUBLISHED"` |
| 狀態命名 | `res/news/NewsRes.java` `getStatusName()` | 更新 `"ARCHIVED"` → `"UNPUBLISHED"` 對應 |
| DB 遷移 | MySQL | 執行 `UPDATE news SET status='UNPUBLISHED' WHERE status='ARCHIVED'` |

---

## 狀態參照

| 狀態 | 代碼 | 顯示名稱 | 對公開端點可見 |
|--------|------|---------|------------------|
| 草稿 | `DRAFT` | 草稿 | ❌ 否 |
| 已發布 | `PUBLISHED` | 已發布 | ✅ 是（若符合時間條件） |
| 已下架 | `UNPUBLISHED` | 已下架 | ❌ 否 |

## 分類參照

| 分類 | 代碼 | 顯示名稱 |
|----------|------|---------|
| 公告 | `ANNOUNCEMENT` | 公告 |
| 活動 | `EVENT` | 活動 |
| 系統 | `SYSTEM` | 系統 |
