# 任務清單：最新消息管理 (News Management)

**功能**：007-news-management  
**輸入文件**：plan.md、spec.md、data-model.md、research.md、contracts/、quickstart.md  
**產生日期**：2026-03-22

> **背景說明**：核心 CRUD 架構已存在（`News` 實體、`NewsMapper`、`NewsService`、
> `AdminNewsController`、`NewsController`）。本任務清單聚焦於四個主要缺口的修補：
> 狀態命名對齊（ARCHIVED → UNPUBLISHED）、公開 API 路徑修正（/news → /api/news）、
> 後台列表端點新增（GET /admin/news）、以及排程自動上架／下架任務的實作。

---

## 格式說明：`[ID] [P?] [Story?] 描述`

- **[P]**：可平行執行（不同檔案、無未完成依賴）
- **[Story]**：對應使用者故事（US1、US2、US3）
- 每個任務包含明確的檔案路徑

---

## Phase 1：環境確認與準備

**目標**：確認現有資料庫結構並準備遷移腳本，為所有後續任務奠定基礎。

- [ ] T001 確認 `news` 資料表已包含所有必要欄位（`category`、`important`、`scheduled_at`、`end_time`），對照 data-model.md 的 DB Schema 執行 `SHOW CREATE TABLE news` 驗證
- [ ] T002 [P] 建立資料庫遷移腳本 `sql/007-news-migration.sql`，內容為 `UPDATE news SET status = 'UNPUBLISHED' WHERE status = 'ARCHIVED'`（部署前一次性執行）

**檢查點**：資料表結構確認完畢，遷移腳本就位，可進入基礎修補階段。

---

## Phase 2：基礎修補（阻塞所有使用者故事的必要前置作業）

**目標**：修正橫跨所有使用者故事的狀態命名不一致問題，確保後續所有端點回傳正確的狀態代碼。

**⚠️ 重要**：此階段完成前，所有使用者故事的實作均無法正確運作。

- [ ] T003 [P] 修正 `src/main/java/com/group/admin/service/impl/NewsServiceImpl.java` 中所有狀態常數，將字串 `"ARCHIVED"` 替換為 `"UNPUBLISHED"`（含 publish/unpublish/query 邏輯）
- [ ] T004 [P] 更新 `src/main/java/com/group/admin/res/news/NewsRes.java`，新增 `statusName`（草稿／已發布／已下架）及 `categoryName`（公告／活動／系統）計算欄位，移除原有 `"ARCHIVED"` 對應

**檢查點**：基礎修補完成 — 使用者故事實作可平行展開。

---

## Phase 3：使用者故事 1 — 管理員建立並發布消息文章（優先級：P1）🎯 MVP

**目標**：讓管理員能透過後台完整執行消息文章的建立、編輯、手動發布/下架及刪除操作。對應 spec.md US1 驗收情境全數通過。

**獨立測試**：管理員建立一篇消息文章，狀態設為「已發布」，確認文章出現在後台列表（`GET /admin/news`）；再將狀態設為「草稿」，確認文章不出現在前台公開列表。

### 使用者故事 1 實作任務

- [ ] T005 [P] [US1] 審查並確認 `src/main/java/com/group/admin/req/news/NewsCreateReq.java` 的驗證規則：`title` 須為 `@NotBlank`（最多 255 字元）、`content` 須為 `@NotBlank`，`status` 預設 `DRAFT`，`scheduledAt`/`endTime` 若同時存在則 `endTime` 必須晚於 `scheduledAt`
- [ ] T006 [P] [US1] 審查並確認 `src/main/java/com/group/admin/req/news/NewsUpdateReq.java` 支援部分更新語意（所有欄位選填，`null` 欄位不更新），驗證 `status` 僅接受 `DRAFT`、`PUBLISHED`、`UNPUBLISHED`
- [ ] T007 [US1] 在 `src/main/java/com/group/admin/controller/admin/AdminNewsController.java` 新增 `GET /admin/news` 列表端點，支援可選查詢參數 `?status=`，預設排序 `created_at DESC`，套用 `@PreAuthorize("hasRole('ADMIN')")`
- [ ] T008 [US1] 在 `src/main/java/com/group/admin/controller/admin/AdminNewsController.java` 新增 `POST /admin/news/{id}/publish` 手動發布端點，設定 `status = PUBLISHED`、`scheduled_at = NOW()`，回傳更新後的 `NewsRes`，套用 `@PreAuthorize("hasRole('ADMIN')")`
- [ ] T009 [US1] 在 `src/main/java/com/group/admin/controller/admin/AdminNewsController.java` 新增 `POST /admin/news/{id}/unpublish` 手動下架端點，設定 `status = UNPUBLISHED`、`end_time = NOW()`，回傳更新後的 `NewsRes`，套用 `@PreAuthorize("hasRole('ADMIN')")`
- [ ] T010 [US1] 確認 `src/main/java/com/group/admin/controller/admin/AdminNewsController.java` 中 `POST /admin/news`（建立）、`PUT /admin/news/{id}`（更新）、`DELETE /admin/news/{id}`（刪除）、`GET /admin/news/{id}`（取得單篇，任意狀態）端點完整且正確，建立時從 JWT 擷取 `createdBy`
- [ ] T011 [US1] 確認 `src/main/java/com/group/admin/service/impl/NewsServiceImpl.java` 的 `publish()` 及 `unpublish()` 方法邏輯正確：`publish()` 設定 `status=PUBLISHED` + `scheduledAt=now()`；`unpublish()` 設定 `status=UNPUBLISHED` + `endTime=now()`；文章不存在時拋出 `404` 例外

**檢查點**：使用者故事 1 完整可測試——管理員可建立、編輯、發布、下架、刪除消息文章。

---

## Phase 4：使用者故事 2 — 管理員排程消息發布（優先級：P2）

**目標**：實作排程背景任務，使設定了 `scheduled_at`／`end_time` 的草稿文章能在指定時間自動轉換狀態，滿足 SC-002（1 分鐘內自動轉換）。

**獨立測試**：建立一篇 `status=DRAFT`、`scheduled_at` 設為過去 1 分鐘前的文章；等待 60 秒後，確認文章 `status` 已自動變更為 `PUBLISHED`。再設定 `end_time` 為過去時間；確認 `status` 自動變更為 `UNPUBLISHED`。

### 使用者故事 2 實作任務

- [ ] T012 [P] [US2] 在 `src/main/java/com/group/admin/mapper/NewsMapper.java` 新增兩個自訂方法簽名：`int autoPublishNews()` 與 `int autoUnpublishNews()`（非 MBG 生成，手動新增）
- [ ] T013 [P] [US2] 在 `src/main/resources/mapper/NewsMapper.xml` 新增對應的 SQL 語句：
  - `autoPublishNews`：`UPDATE news SET status='PUBLISHED', updated_at=NOW() WHERE status='DRAFT' AND scheduled_at IS NOT NULL AND scheduled_at <= NOW()`
  - `autoUnpublishNews`：`UPDATE news SET status='UNPUBLISHED', updated_at=NOW() WHERE status='PUBLISHED' AND end_time IS NOT NULL AND end_time <= NOW()`
- [ ] T014 [US2] 在 `src/main/java/com/group/admin/service/NewsService.java` 介面新增方法簽名：`int autoPublishScheduledNews()` 與 `int autoUnpublishExpiredNews()`
- [ ] T015 [US2] 在 `src/main/java/com/group/admin/service/impl/NewsServiceImpl.java` 實作 `autoPublishScheduledNews()`（呼叫 `newsMapper.autoPublishNews()`，加 `@Transactional`）與 `autoUnpublishExpiredNews()`（呼叫 `newsMapper.autoUnpublishNews()`，加 `@Transactional`），並記錄 log（已發布／已下架的文章數量）
- [ ] T016 [US2] 在 `src/main/java/com/group/admin/scheduler/ScheduledTasks.java` 新增兩個排程方法：`@Scheduled(fixedRate = 60000) autoPublishNews()` 呼叫 `newsService.autoPublishScheduledNews()`；`@Scheduled(fixedRate = 60000) autoUnpublishNews()` 呼叫 `newsService.autoUnpublishExpiredNews()`，沿用同檔案 `retryFailedEmails()` 的既有模式

**檢查點**：使用者故事 2 完整可測試——排程任務每 60 秒執行，符合 SC-002 1 分鐘 SLA。

---

## Phase 5：使用者故事 3 — 玩家瀏覽消息列表（優先級：P2）

**目標**：修正公開 API 路徑並確認前台過濾邏輯正確，讓玩家無需驗證即可瀏覽已發布消息，並以發布時間遞減排序。

**獨立測試**：準備三篇已發布消息，`scheduled_at` 各不相同；呼叫 `GET /api/news`（不帶 Token），確認回傳結果按 `scheduled_at DESC` 排序，且僅含 `status=PUBLISHED` 文章。再透過 `GET /api/news/{id}` 請求草稿文章，確認回傳 `404`。

### 使用者故事 3 實作任務

- [ ] T017 [US3] 修正 `src/main/java/com/group/admin/controller/api/NewsController.java` 的 `@RequestMapping`，從 `"/news"` 改為 `"/api/news"`（依 research.md 決策 4 及公開 API 合約）
- [ ] T018 [US3] 確認 `src/main/java/com/group/admin/controller/api/NewsController.java` 的 `GET /api/news` 端點：不需驗證、支援可選 `?limit=` 查詢參數、呼叫 `newsService.getPublishedNews(limit)`
- [ ] T019 [US3] 確認 `src/main/java/com/group/admin/controller/api/NewsController.java` 的 `GET /api/news/{id}` 端點：文章不存在或非 `PUBLISHED` 狀態時回傳 `404`，錯誤訊息為 `"該消息不存在或已下架"`
- [ ] T020 [US3] 確認 `src/main/java/com/group/admin/service/impl/NewsServiceImpl.java` 的 `getPublishedNews()` 過濾邏輯符合合約：`WHERE status='PUBLISHED' AND scheduled_at <= NOW() AND (end_time IS NULL OR end_time > NOW()) ORDER BY scheduled_at DESC`
- [ ] T021 [US3] 確認 Security 設定（`SecurityConfig.java` 或 `WebSecurityConfig.java`）已將 `/api/news/**` 列為允許匿名存取的公開路徑（`permitAll()`），確保玩家無需 JWT 即可存取

**檢查點**：使用者故事 3 完整可測試——玩家可無驗證存取已發布消息列表與詳情，草稿不洩漏。

---

## Phase 6：收尾與橫切關注點

**目標**：執行資料庫遷移、補充 Swagger 文件標籤、並依 quickstart.md 執行端對端驗收測試，確保所有使用者故事協同運作。

- [ ] T022 [P] 為 `src/main/java/com/group/admin/controller/admin/AdminNewsController.java` 加入 Swagger 標籤 `@Tag(name = "後台-最新消息管理")`，並為每個端點加入 `@Operation` 說明
- [ ] T023 [P] 為 `src/main/java/com/group/admin/controller/api/NewsController.java` 加入 Swagger 標籤 `@Tag(name = "前台-最新消息")`，並為每個端點加入 `@Operation` 說明
- [ ] T024 執行資料庫遷移腳本 `sql/007-news-migration.sql`（`UPDATE news SET status='UNPUBLISHED' WHERE status='ARCHIVED'`）於目標 MySQL 執行個體（本機或 AWS RDS）
- [ ] T025 依照 `specs/007-news-management/quickstart.md` 執行端對端驗收測試，依序驗證：建立草稿 → 後台列表可見 → 手動發布 → 前台 `/api/news` 可見 → 手動下架 → 前台不可見 → 排程自動上架（`scheduled_at` 設為過去）→ 60 秒後前台可見
- [ ] T026 [P] 架構合規性最終審查：確認 FR-007（文章內容無外部 URL 連結）是否需要在 `NewsCreateReq` 加入驗證備注；確認所有 `AdminNewsController` 端點均有 `@PreAuthorize("hasRole('ADMIN')")`；確認 `NewsController` 無 `@PreAuthorize` 以維持公開存取

---

## 依賴關係與執行順序

### 階段依賴

- **Phase 1（確認準備）**：無依賴，可立即開始
- **Phase 2（基礎修補）**：依賴 Phase 1 完成 — **阻塞所有使用者故事**
- **使用者故事（Phase 3～5）**：均依賴 Phase 2 完成；三個使用者故事彼此獨立，可平行開發
- **Phase 6（收尾）**：依賴所有使用者故事完成

### 使用者故事間的依賴

- **US1（P1）**：Phase 2 完成後即可開始，無跨故事依賴
- **US2（P2）**：Phase 2 完成後即可開始，無跨故事依賴（純新增排程邏輯，不改動 US1 端點）
- **US3（P2）**：Phase 2 完成後即可開始，無跨故事依賴（僅修正路徑及確認過濾邏輯）

### 使用者故事 1 內部依賴

```
T005, T006（審查 Req DTO）可平行
    ↓
T007（新增 GET /admin/news）
T008（新增 /publish 端點）   ← 可平行
T009（新增 /unpublish 端點） ← 可平行
    ↓
T010（確認現有端點完整性）
T011（確認 Service publish/unpublish 邏輯）
```

### 使用者故事 2 內部依賴

```
T012（NewsMapper.java 介面）  ← 可平行開始
T013（NewsMapper.xml SQL）    ← 可平行開始
    ↓（兩者完成後）
T014（NewsService.java 介面新增方法）
    ↓
T015（NewsServiceImpl.java 實作）
    ↓
T016（ScheduledTasks.java 排程方法）
```

### 使用者故事 3 內部依賴

```
T017（修正 @RequestMapping）
    ↓
T018（確認列表端點）
T019（確認詳情端點）   ← 可平行
T020（確認 Service 過濾邏輯）← 可平行
    ↓
T021（確認 Security 設定）
```

---

## 平行執行範例

### 使用者故事 1

```bash
# Phase 2 完成後，同時啟動：
Task A: "T005 — 審查 NewsCreateReq 驗證規則"
Task B: "T006 — 審查 NewsUpdateReq 部分更新語意"
# 兩者完成後繼續 T007、T008、T009（可三路平行）
```

### 使用者故事 2

```bash
# Phase 2 完成後，同時啟動：
Task A: "T012 — 新增 NewsMapper.java 方法簽名"
Task B: "T013 — 實作 NewsMapper.xml SQL 語句"
# 兩者完成後依序執行 T014 → T015 → T016
```

### 多人開發者平行策略

```bash
# Phase 2 完成後，三位開發者同時開始：
Developer A: Phase 3 (US1) — T005 ~ T011
Developer B: Phase 4 (US2) — T012 ~ T016
Developer C: Phase 5 (US3) — T017 ~ T021
# 各自完成後匯合至 Phase 6
```

---

## 實作策略

### MVP 優先（僅完成使用者故事 1）

1. 完成 Phase 1：確認環境
2. 完成 Phase 2：基礎修補（**必要前置，阻塞所有故事**）
3. 完成 Phase 3：使用者故事 1（管理員 CRUD + 發布/下架）
4. **停止並驗證**：後台建立文章、狀態正確、端點可用
5. 部署或展示 MVP

### 增量交付

1. 完成 Phase 1 + Phase 2 → 基礎就緒
2. 完成 Phase 3（US1）→ 獨立測試 → 部署（MVP！）
3. 完成 Phase 4（US2）→ 獨立測試 → 排程功能上線
4. 完成 Phase 5（US3）→ 獨立測試 → 前台公開端點正確
5. 完成 Phase 6 → 端對端驗收 → 功能完整交付

### 注意事項

- **不需要從零建構**：大多數任務為「確認」或「修補」現有程式碼，非全新建立
- **最高風險任務**：T003（狀態常數替換）— 需搜尋所有 `"ARCHIVED"` 出現位置，勿遺漏
- **資料遷移時機**：T024 應在 Phase 3～5 程式碼部署**之前**或**同時**執行，避免新舊狀態混用
- **FR-007 備注**：API 層目前未強制限制外部連結；T026 審查時決定是否需要加入 `@Pattern` 驗證

---

## 摘要

| 項目 | 數量 |
|------|------|
| 總任務數 | 26 |
| Phase 1（確認準備） | 2 |
| Phase 2（基礎修補） | 2 |
| Phase 3（US1 — 管理員建立發布） | 7 |
| Phase 4（US2 — 排程自動轉換） | 5 |
| Phase 5（US3 — 玩家瀏覽列表） | 5 |
| Phase 6（收尾） | 5 |
| 可平行執行任務（[P] 標記） | 12 |
| 使用者故事間的平行機會 | 3 個故事可同時開發 |

### 各使用者故事獨立測試標準

| 使用者故事 | 獨立驗收測試 |
|-----------|-------------|
| US1（建立發布） | 管理員建立草稿 → 後台列表可見；發布後前台可見；下架後前台不可見 |
| US2（排程發布） | 設 `scheduled_at` 為過去 → 60 秒內自動變 PUBLISHED；設 `end_time` 為過去 → 60 秒內自動變 UNPUBLISHED |
| US3（玩家瀏覽） | `GET /api/news` 無 Token 成功、僅含 PUBLISHED、依 scheduledAt DESC 排序；草稿 ID 回傳 404 |

### 建議 MVP 範圍

**僅完成 Phase 1 + Phase 2 + Phase 3（US1）** 即可交付可運作的後台消息管理，
涵蓋建立、編輯、發布、下架、刪除全流程。排程與前台可視為後續迭代。
