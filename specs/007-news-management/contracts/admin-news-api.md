# API 合約：後台消息管理

**功能**：007-news-management  
**日期**：2026-03-22  
**基礎路徑**：`/admin/news`  
**驗證**：JWT Bearer Token，所有端點均需要 `ADMIN` 角色  
**控制器**：`com.group.admin.controller.admin.AdminNewsController`

---

## 通用標頭

```
Authorization: Bearer <jwt-token>
Content-Type: application/json
Accept: application/json
```

---

## 端點

### 1. 建立消息文章

**`POST /admin/news`**

建立一篇新的消息文章。若未指定狀態，預設為 `DRAFT`。

#### 請求主體

```json
{
  "title": "春節活動開跑！",
  "content": "春節期間推出限定活動，詳情如下...",
  "imageUrl": "https://cdn.example.com/news/spring.jpg",
  "status": "DRAFT",
  "category": "ANNOUNCEMENT",
  "important": false,
  "scheduledAt": "2026-02-01T10:00:00",
  "endTime": "2026-02-28T23:59:59"
}
```

| 欄位 | 型別 | 必填 | 驗證 | 描述 |
|-------|------|----------|-----------|-------------|
| `title` | String | ✅ 是 | NotBlank，最多 255 字元 | 文章標題 |
| `content` | String | ✅ 是 | NotBlank | 內文（長文） |
| `imageUrl` | String | 否 | 有效的 URL 格式 | 封面圖片 URL |
| `status` | String | 否 | DRAFT \| PUBLISHED \| UNPUBLISHED；預設：DRAFT | 初始狀態 |
| `category` | String | 否 | ANNOUNCEMENT \| EVENT \| SYSTEM；預設：ANNOUNCEMENT | 分類 |
| `important` | Boolean | 否 | 預設：false | 重要提醒標記 |
| `scheduledAt` | DateTime | 否 | ISO-8601；若與 endTime 同時設定：必須早於 endTime | 排程上架時間 |
| `endTime` | DateTime | 否 | ISO-8601；必須晚於 scheduledAt | 排程下架時間 |

#### 回應 `200 OK`

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "春節活動開跑！",
  "content": "春節期間推出限定活動，詳情如下...",
  "imageUrl": "https://cdn.example.com/news/spring.jpg",
  "status": "DRAFT",
  "statusName": "草稿",
  "category": "ANNOUNCEMENT",
  "categoryName": "公告",
  "important": false,
  "scheduledAt": "2026-02-01T10:00:00",
  "endTime": "2026-02-28T23:59:59",
  "createdBy": "admin-uuid-001",
  "createdAt": "2026-03-22T09:00:00",
  "updatedAt": "2026-03-22T09:00:00"
}
```

#### 錯誤回應

| 狀態碼 | 情境 |
|--------|-----------|
| `400 Bad Request` | title 或 content 為空；endTime 早於 scheduledAt |
| `401 Unauthorized` | JWT 遺失或無效 |
| `403 Forbidden` | 使用者不具備 ADMIN 角色 |

---

### 2. 更新消息文章

**`PUT /admin/news/{id}`**

更新消息文章。僅更新請求主體中非 null 的欄位（部分更新）。

#### 路徑參數

| 參數 | 型別 | 描述 |
|-----------|------|-------------|
| `id` | String (UUID) | 消息文章 ID |

#### 請求主體

```json
{
  "title": "春節活動延長！",
  "status": "PUBLISHED",
  "endTime": "2026-03-15T23:59:59"
}
```

> 所有欄位皆為選填。省略不想修改的欄位。

| 欄位 | 型別 | 驗證 | 描述 |
|-------|------|-----------|-------------|
| `title` | String | 最多 255 字元 | 新標題 |
| `content` | String | — | 新內文 |
| `imageUrl` | String | 有效的 URL 或 null | 新封面圖片 URL |
| `status` | String | DRAFT \| PUBLISHED \| UNPUBLISHED | 新狀態 |
| `category` | String | ANNOUNCEMENT \| EVENT \| SYSTEM | 新分類 |
| `important` | Boolean | — | 重要提醒標記 |
| `scheduledAt` | DateTime | ISO-8601 | 新上架時間 |
| `endTime` | DateTime | ISO-8601；必須晚於 scheduledAt | 新下架時間 |

#### 回應 `200 OK`

與建立回應結構相同，反映更新後的值。

#### 錯誤回應

| 狀態碼 | 情境 |
|--------|-----------|
| `400 Bad Request` | 無效的狀態值；endTime 早於 scheduledAt |
| `401 Unauthorized` | JWT 遺失或無效 |
| `403 Forbidden` | 使用者不具備 ADMIN 角色 |
| `404 Not Found` | 指定 ID 的文章不存在 |

---

### 3. 刪除消息文章

**`DELETE /admin/news/{id}`**

永久刪除一篇消息文章。此操作無法復原。

#### 路徑參數

| 參數 | 型別 | 描述 |
|-----------|------|-------------|
| `id` | String (UUID) | 消息文章 ID |

#### 回應 `200 OK`

HTTP 200，空回應主體。

#### 錯誤回應

| 狀態碼 | 情境 |
|--------|-----------|
| `401 Unauthorized` | JWT 遺失或無效 |
| `403 Forbidden` | 使用者不具備 ADMIN 角色 |
| `404 Not Found` | 指定 ID 的文章不存在 |

---

### 4. 列出所有消息（後台）

**`GET /admin/news`**

回傳所有消息文章，不限狀態。支援可選的狀態篩選。
預設排序：`created_at DESC`。

#### 查詢參數

| 參數 | 型別 | 必填 | 描述 |
|-----------|------|----------|-------------|
| `status` | String | 否 | 依狀態篩選：DRAFT \| PUBLISHED \| UNPUBLISHED |

#### 請求範例

```
GET /admin/news?status=DRAFT
Authorization: Bearer <jwt-token>
```

#### Response `200 OK`

```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "title": "春節活動開跑！",
    "content": "春節期間推出限定活動，詳情如下...",
    "imageUrl": "https://cdn.example.com/news/spring.jpg",
    "status": "DRAFT",
    "statusName": "草稿",
    "category": "ANNOUNCEMENT",
    "categoryName": "公告",
    "important": false,
    "scheduledAt": "2026-02-01T10:00:00",
    "endTime": "2026-02-28T23:59:59",
    "createdBy": "admin-uuid-001",
    "createdAt": "2026-03-22T09:00:00",
    "updatedAt": "2026-03-22T09:00:00"
  }
]
```

#### 錯誤回應

| 狀態碼 | 情境 |
|--------|-----------|
| `401 Unauthorized` | JWT 遺失或無效 |
| `403 Forbidden` | 使用者不具備 ADMIN 角色 |

---

### 5. 取得單篇消息文章（後台）

**`GET /admin/news/{id}`**

回傳任意狀態文章的完整詳情。

#### 路徑參數

| 參數 | 型別 | 描述 |
|-----------|------|-------------|
| `id` | String (UUID) | 消息文章 ID |

#### 回應 `200 OK`

與建立回應結構相同。

#### 錯誤回應

| 狀態碼 | 情境 |
|--------|-----------|
| `401 Unauthorized` | JWT 遺失或無效 |
| `403 Forbidden` | 使用者不具備 ADMIN 角色 |
| `404 Not Found` | 文章不存在 |

---

### 6. 手動發布消息

**`POST /admin/news/{id}/publish`**

立即發布一篇文章。設定 `status = PUBLISHED` 且 `scheduled_at = NOW()`。

#### 回應 `200 OK`

回傳更新後的 `NewsRes`，`status: "PUBLISHED"`，`statusName: "已發布"`。

---

### 7. 手動下架消息

**`POST /admin/news/{id}/unpublish`**

立即下架一篇文章。設定 `status = UNPUBLISHED` 且 `end_time = NOW()`。

#### 回應 `200 OK`

回傳更新後的 `NewsRes`，`status: "UNPUBLISHED"`，`statusName: "已下架"`。

---

### 8. 進階列表查詢（現有功能）

**`POST /admin/news/list`**

支援複雜篩選條件（標題搜尋、狀態、日期範圍、關鍵字）。

#### 請求主體

```json
{
  "condition": {
    "title": "春節",
    "status": "PUBLISHED",
    "category": "EVENT",
    "important": true,
    "keyword": "活動",
    "createdAtStart": "2026-01-01",
    "createdAtEnd": "2026-03-31"
  },
  "sortBy": "created_at",
  "sortOrder": "DESC"
}
```

#### 回應 `200 OK`

回傳 `NewsRes` 物件陣列。

---

## 回應 Schema：`NewsRes`

```json
{
  "id": "string (UUID)",
  "title": "string",
  "content": "string (long text)",
  "imageUrl": "string | null",
  "status": "DRAFT | PUBLISHED | UNPUBLISHED",
  "statusName": "草稿 | 已發布 | 已下架",
  "category": "ANNOUNCEMENT | EVENT | SYSTEM",
  "categoryName": "公告 | 活動 | 系統",
  "important": "boolean",
  "scheduledAt": "datetime | null  (= publishedAt)",
  "endTime": "datetime | null  (= unpublishedAt)",
  "createdBy": "string (admin UUID) | null",
  "createdAt": "datetime",
  "updatedAt": "datetime"
}
```
