# API 合約：公開消息 API

**功能**：007-news-management  
**日期**：2026-03-22  
**基礎路徑**：`/api/news`  
**驗證**：不需要（公開端點）  
**控制器**：`com.group.admin.controller.api.NewsController`

> **備注**：控制器的 `@RequestMapping` 必須從 `/news` 更新為 `/api/news`，
> 以符合此合約並與所有其他公開 API 控制器保持一致。

---

## 端點

### 1. 列出已發布消息

**`GET /api/news`**

回傳所有已發布的消息文章，依 `scheduled_at`（發布日期）降冪排序。
僅回傳 `status = PUBLISHED` 且 `scheduled_at <= NOW()` 且
（`end_time IS NULL` 或 `end_time > NOW()`）的文章。

不需要驗證。

#### 查詢參數

| 參數 | 型別 | 必填 | 描述 |
|-----------|------|----------|-------------|
| `limit` | Integer | 否 | 最多回傳的文章數量（首頁小工具用）。省略則不限制。 |

#### 請求範例

```
GET /api/news
GET /api/news?limit=5
```

#### 回應 `200 OK`

```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "title": "春節活動開跑！",
    "content": "春節期間推出限定活動，詳情如下...",
    "imageUrl": "https://cdn.example.com/news/spring.jpg",
    "status": "PUBLISHED",
    "statusName": "已發布",
    "category": "ANNOUNCEMENT",
    "categoryName": "公告",
    "important": false,
    "scheduledAt": "2026-02-01T10:00:00",
    "endTime": "2026-02-28T23:59:59",
    "createdBy": null,
    "createdAt": "2026-01-25T14:00:00",
    "updatedAt": "2026-01-25T14:00:00"
  },
  {
    "id": "660e8400-e29b-41d4-a716-446655440001",
    "title": "系統維護通知",
    "content": "預計於2026年2月15日凌晨2時進行系統維護...",
    "imageUrl": null,
    "status": "PUBLISHED",
    "statusName": "已發布",
    "category": "SYSTEM",
    "categoryName": "系統",
    "important": true,
    "scheduledAt": "2026-02-10T09:00:00",
    "endTime": null,
    "createdBy": null,
    "createdAt": "2026-02-08T10:00:00",
    "updatedAt": "2026-02-08T10:00:00"
  }
]
```

> **備注**：`createdBy`（管理員 UUID）包含在回應 schema 中，但出於隱私考量，
> 對公開端點的消費者可能為 `null` 或省略。前端不應向玩家顯示 `createdBy`。

#### 排序方式

文章依 `scheduled_at DESC` 排序（最新發布優先）。
`scheduled_at` 相同的文章以 `created_at DESC` 作為次要排序。

#### 篩選邏輯（伺服器端）

```
WHERE status = 'PUBLISHED'
  AND scheduled_at <= NOW()
  AND (end_time IS NULL OR end_time > NOW())
ORDER BY scheduled_at DESC
```

#### 錯誤回應

| 狀態碼 | 情境 |
|--------|-----------|
| `500 Internal Server Error` | 意外的伺服器錯誤 |

> 此端點**不會**回傳 401/403 — 完全公開存取。

---

### 2. 取得已發布消息詳情

**`GET /api/news/{id}`**

回傳單篇已發布消息文章的完整內容。
若文章不存在或狀態不為 `PUBLISHED` 則回傳 `404`。

不需要驗證。

#### 路徑參數

| 參數 | 型別 | 描述 |
|-----------|------|-------------|
| `id` | String (UUID) | 消息文章 ID |

#### 請求範例

```
GET /api/news/550e8400-e29b-41d4-a716-446655440000
```

#### 回應 `200 OK`

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "春節活動開跑！",
  "content": "春節期間推出限定活動，詳情如下...\n\n活動時間：2026年2月1日 至 2月28日\n活動詳情：...",
  "imageUrl": "https://cdn.example.com/news/spring.jpg",
  "status": "PUBLISHED",
  "statusName": "已發布",
  "category": "ANNOUNCEMENT",
  "categoryName": "公告",
  "important": false,
  "scheduledAt": "2026-02-01T10:00:00",
  "endTime": "2026-02-28T23:59:59",
  "createdBy": null,
  "createdAt": "2026-01-25T14:00:00",
  "updatedAt": "2026-01-25T14:00:00"
}
```

#### 錯誤回應

| 狀態碼 | 情境 | 回應主體 |
|--------|-----------|------|
| `404 Not Found` | 文章 ID 不存在 | `{"message": "該消息不存在或已下架"}` |
| `404 Not Found` | 文章存在但狀態非 PUBLISHED | `{"message": "該消息不存在或已下架"}` |
| `500 Internal Server Error` | 意外的伺服器錯誤 | — |

> **安全性備注**：透過 ID 存取草稿文章時回傳 404（而非 403），
> 以避免洩漏未發布內容的相關資訊。

---

## 存取規則

| 情境 | 預期行為 |
|----------|--------------------|
| 玩家請求列表 | 僅回傳 PUBLISHED 且時間有效（經時間檢查）的文章 |
| 玩家透過 ID 請求草稿文章 | 回傳 404（非 403） |
| 玩家透過 ID 請求 UNPUBLISHED 文章 | 回傳 404 |
| 玩家請求 scheduledAt 為未來時間的文章 | 不在列表中；透過 ID 查詢回傳 404 |
| 玩家請求已過期的文章（end_time 已過） | 不在列表中；透過 ID 查詢回傳 404 |

---

## 公開端點的欄位可見性

| 欄位 | 公開列表 | 公開詳情 | 備注 |
|-------|-------------|---------------|-------|
| `id` | ✅ | ✅ | |
| `title` | ✅ | ✅ | |
| `content` | ✅ | ✅ | 兩者均為完整內文 |
| `imageUrl` | ✅ | ✅ | |
| `status` | ✅ | ✅ | 公開回應中永遠為 "PUBLISHED" |
| `statusName` | ✅ | ✅ | 公開回應中永遠為 "已發布" |
| `category` | ✅ | ✅ | |
| `categoryName` | ✅ | ✅ | |
| `important` | ✅ | ✅ | 前端可顯示標記 |
| `scheduledAt` | ✅ | ✅ | 作為顯示用的發布日期 |
| `endTime` | ✅ | ✅ | 可選顯示 |
| `createdBy` | ⚠️ 隱藏 | ⚠️ 隱藏 | 管理員 UUID — 前端不應顯示 |
| `createdAt` | ✅ | ✅ | |
| `updatedAt` | ✅ | ✅ | |

---

## 回應 Schema：`NewsRes`（公開端點）

```json
{
  "id": "string (UUID)",
  "title": "string",
  "content": "string (long text)",
  "imageUrl": "string | null",
  "status": "PUBLISHED",
  "statusName": "已發布",
  "category": "ANNOUNCEMENT | EVENT | SYSTEM",
  "categoryName": "公告 | 活動 | 系統",
  "important": "boolean",
  "scheduledAt": "datetime  (= publishedAt)",
  "endTime": "datetime | null  (= unpublishedAt)",
  "createdBy": "string | null  (hide from UI)",
  "createdAt": "datetime",
  "updatedAt": "datetime"
}
```
