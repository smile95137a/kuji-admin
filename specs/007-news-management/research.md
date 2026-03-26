# Research: 最新消息管理 (News Management)

**Feature**: 007-news-management  
**Date**: 2026-03-22  
**Phase**: 0 — Research

---

## 1. 現有實作稽核

### 決策
對功能規格與現有程式碼進行差異分析，而非從頭設計。

### 調查結果

| Component | Exists? | Location | Notes |
|-----------|---------|----------|-------|
| `News` entity | ✅ YES | `entity/News.java` | UUID PK, all required fields present |
| `NewsMapper` | ✅ YES | `mapper/NewsMapper.java` + `resources/mapper/NewsMapper.xml` | MBG-generated, full CRUD |
| `NewsExample` | ✅ YES | `example/NewsExample.java` | MBG criteria builder |
| `NewsService` / `NewsServiceImpl` | ✅ YES | `service/` | Full CRUD, publish/unpublish, getPublishedNews |
| `AdminNewsController` | ✅ YES | `controller/admin/AdminNewsController.java` | POST/PUT/DELETE/GET/{id}; list via POST /admin/news/list |
| `NewsController` (public) | ✅ YES | `controller/api/NewsController.java` | GET /news, GET /news/{id} |
| `NewsCreateReq` / `NewsUpdateReq` | ✅ YES | `req/news/` | @NotBlank validation on title+content |
| `NewsRes` | ✅ YES | `res/news/NewsRes.java` | Builder pattern, includes all fields |
| Scheduler infrastructure | ✅ YES | `scheduler/ScheduledTasks.java` | Spring @Scheduled enabled, has email + log tasks |
| Auto-publish/unpublish scheduler | ❌ NO | — | **Gap #1**: Must implement |

### 原因說明
本專案使用 MyBatis Generator（MBG）建立持久層架構，所有 CRUD 操作已可正常運作。此功能的主要工作為：
1. 新增 `@Scheduled` 消息狀態自動轉換任務
2. 修正狀態命名不一致問題
3. 修正公開 API 路徑前綴
4. 對齊後台列表端點簽名

---

## 2. 狀態命名策略

### 決策
使用 `DRAFT / PUBLISHED / UNPUBLISHED`（依規格定義），將 `ARCHIVED` 對應至 `UNPUBLISHED`。

### 原因說明
- 功能規格（FR-002）明確定義三種狀態：草稿（DRAFT）、已發布（PUBLISHED）、已下架（UNPUBLISHED）。
- 現有程式碼將下架狀態稱為 `ARCHIVED`，功能上等同但命名不一致。
- 由於 DB 欄位為 `VARCHAR`（無 DB 層級的 enum 限制），重新命名需要：
  - 更新 `NewsServiceImpl` 常數：`"ARCHIVED"` → `"UNPUBLISHED"`
  - 更新 `NewsRes.statusName` 對應
  - 遷移 MySQL 現有資料：`UPDATE news SET status = 'UNPUBLISHED' WHERE status = 'ARCHIVED'`
- 規格與前端團隊的預期值一致。

### 已考量的替代方案
- 保留 `ARCHIVED`：拒絕 — 規格命名具權威性；前端需要額外的轉換層。
- 使用 DB enum：拒絕 — 需要 ALTER TABLE；VARCHAR 對未來狀態更具彈性。

---

## 3. 排程自動上架／自動下架

### 決策
在 `ScheduledTasks.java` 中新增兩個每 60 秒執行一次的 `@Scheduled` 方法。

### 原因說明
- **SC-002**：「排程文章在設定時間後 1 分鐘內自動轉換狀態。」
- Spring `@Scheduled(fixedRate = 60000)`（60 秒）可滿足 1 分鐘 SLA。
- 沿用同檔案中 `retryFailedEmails()` 的既有模式。
- 為清晰起見，使用兩個獨立任務：`autoPublishNews()` 與 `autoUnpublishNews()`。

**Auto-publish logic**:
```sql
UPDATE news SET status = 'PUBLISHED', updated_at = NOW()
WHERE status = 'DRAFT'
  AND scheduled_at IS NOT NULL
  AND scheduled_at <= NOW()
```

**Auto-unpublish logic**:
```sql
UPDATE news SET status = 'UNPUBLISHED', updated_at = NOW()
WHERE status = 'PUBLISHED'
  AND end_time IS NOT NULL
  AND end_time <= NOW()
```

### 已考量的替代方案
- Spring `@Scheduled(cron = "0 * * * * ?")`（每分鐘）：等效；選擇 fixedRate 以求簡潔。
- Quartz Scheduler：對兩個簡單任務而言過於複雜；且不在目前依賴中。
- 事件驅動（Kafka/SQS）：v1.0 不在範圍內，需要基礎架構變更。

---

## 4. 公開 API 路徑前綴

### 決策
將 `NewsController` 的 `@RequestMapping` 從 `/news` 改為 `/api/news`。

### 原因說明
- 所有其他公開（非管理員、非認證）控制器均使用 `/api/` 前綴：
  - `/api/district/**`、`/api/marquee/**`、`/api/recharge-plan/**`、`/api/lottery/**` 等。
- 規格要求 `GET /api/news` 與 `GET /api/news/{id}`。
- Security 設定依路徑前綴套用規則；`/api/news` 將與其他公開 `/api/**` 端點獲得相同處理。

### 已考量的替代方案
- 保留 `/news`：拒絕 — 與其他 API 不一致；違反規格合約。

---

## 5. 後台列表端點模式

### 決策
新增 `GET /admin/news` 作為標準列表端點（無 Request Body），同時保留現有的
`POST /admin/news/list`（支援複雜篩選條件）。

### 原因說明
- 規格合約要求 `GET /admin/news` 作為標準列表端點。
- RESTful 慣例：GET 用於取得集合，POST 僅在篩選條件複雜時使用。
- 保留 `POST /admin/news/list` 以支援進階篩選（向下相容）。
- `GET /admin/news` 回傳所有消息，預設排序為 created_at DESC，支援可選的 `?status=` 查詢參數。

---

## 6. 排程欄位對應

### 決策
使用現有的 `scheduled_at` 欄位作為 `published_at`（文章生效發布的時間）。
使用現有的 `end_time` 作為 `unpublished_at`。

### 原因說明
- DB 已有 `scheduled_at`（TIMESTAMP）與 `end_time`（TIMESTAMP）。
- `scheduled_at` 語意：「此文章上線／已上線的時間」— 等同於 `published_at`。
- `end_time` 語意：「此文章下線／已下線的時間」— 等同於 `unpublished_at`。
- 無需修改 DB schema；僅在 API 回應中重新命名以提升清晰度。

---

## 7. 內文欄位（BLOB）處理

### 決策
保留 `content` 為 `LONGVARCHAR`（MySQL 中的 TEXT/LONGTEXT），透過 `selectByExampleWithBLOBs` 存取。

### 原因說明
- MBG 將 BLOB 欄位分離至 `ResultMapWithBLOBs`；所有需要取得內文的查詢必須使用
  `selectByExampleWithBLOBs` 或 `selectByPrimaryKey`（包含 BLOB）。
- `NewsServiceImpl` 中已正確實作此邏輯。
- 無需修改。

---

## 8. 安全性 — 公開消息存取

### 決策
`GET /api/news` 與 `GET /api/news/{id}` 不需要身份驗證。

### 原因說明
- 安全設定目前為 `.anyRequest().permitAll()` — 所有端點預設為公開。
- 消息瀏覽明確為免驗證功能（玩家無需登入即可瀏覽）。
- 管理員寫入端點透過方法層級的 `@PreAuthorize("hasRole('ADMIN')")` 保護。

---

## 9. 分頁策略

### 決策
v1.0 公開列表使用 `limit` 參數（整數）。不採用游標式／分頁式分頁。

### 原因說明
- 規格邊界情境：「大量文章 → 前端處理分頁／無限滾動。」
- 後端提供 `limit` 參數（已在 `getPublishedNews(Integer limit)` 中實作）。
- 完整分頁（page/size）為 v2.0 需求；規格未要求。

---

## 已解決的疑問摘要

| # | 問題 | 解決方案 |
|---|----------|-----------|
| 1 | 排程功能是否存在？ | 否 — 新增 @Scheduled 任務（60 秒間隔） |
| 2 | 狀態命名：ARCHIVED 還是 UNPUBLISHED？ | 依規格改為 UNPUBLISHED |
| 3 | 公開路徑：/news 還是 /api/news？ | 改為 /api/news |
| 4 | 後台列表：GET 還是 POST？ | 新增 GET /admin/news；保留 POST /admin/news/list |
| 5 | published_at 還是 scheduled_at？ | 同一欄位；在回應中公開為 publishedAt |
| 6 | 內文欄位類型？ | LONGVARCHAR（TEXT）；使用 BLOBs 查詢 |
| 7 | 公開消息是否需要驗證？ | 不需要 |
| 8 | 分頁？ | v1.0 僅使用 limit 參數 |
