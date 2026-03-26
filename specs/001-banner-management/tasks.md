# Tasks: 橫幅廣告管理 (Banner Management)

**Input**: 設計文件來源 `/specs/001-banner-management/`
**Prerequisites**: plan.md ✅、spec.md ✅、research.md ✅、data-model.md ✅、contracts/ ✅（5 份）、quickstart.md ✅

**技術棧**: Java 21、Spring Boot 3.3.3、MyBatis 3.0.5、Spring Security 6 + JWT、Lombok、AWS SDK v2 (S3)、MySQL 8.3  
**專案根目錄**: `src/main/java/com/group/admin/`

---

## 格式說明：`[ID] [P?] [Story?] 描述（檔案路徑）`

- **[P]**：可平行執行（不同檔案，無相依關係）
- **[Story]**：對應使用者故事（US1–US4）
- 每個任務均包含精確檔案路徑

---

## 第一階段：環境設定（Setup）

**目的**：確保資料庫結構就緒，為後續所有開發工作奠定基礎

- [ ] T001 執行 data-model.md 的 `banner` 資料表 DDL（`CREATE TABLE IF NOT EXISTS banner`，含 `idx_banner_store_id`、`idx_banner_status_order`、`idx_banner_schedule` 三個索引）——在目標 MySQL 執行個體上執行

---

## 第二階段：基礎建設（Foundational）

**目的**：建立所有使用者故事皆依賴的核心實體、DTO 及介面骨架

**⚠️ 重要**：所有使用者故事均依賴本階段完成後才能開始實作

- [ ] T002 [P] 建立 `Banner.java` 實體類別（欄位：`id`、`storeId`、`title`、`imageUrl`、`linkUrl`、`orderNum`、`status`、`startTime`、`endTime`、`createdAt`、`updatedAt`；使用 Lombok `@Data`，無 JPA，UUID 主鍵）`src/main/java/com/group/admin/entity/Banner.java`
- [ ] T003 [P] 建立 `BannerCreateReq.java` 請求 DTO（`@NotBlank storeId`、`@NotBlank imageUrl`；選用：`title`、`orderNum`（預設 0）、`status`（預設 "DRAFT"）、`LocalDateTime startTime`、`LocalDateTime endTime`）`src/main/java/com/group/admin/req/BannerCreateReq.java`
- [ ] T004 [P] 建立 `BannerUpdateReq.java` 請求 DTO（所有欄位均為選用，支援部分更新語義：`storeId`、`title`、`imageUrl`、`orderNum`、`status`、`startTime`、`endTime`）`src/main/java/com/group/admin/req/BannerUpdateReq.java`
- [ ] T005 [P] 建立 `BannerRes.java` 回應 DTO（欄位：`id`、`storeId`、`storeName`（JOIN 自 store）、`storeLogoUrl`（JOIN 自 store，可為 null）、`title`、`imageUrl`、`linkUrl`（計算值：`/stores/{storeId}`）、`orderNum`、`status`、`startTime`、`endTime`、`createdAt`、`updatedAt`）`src/main/java/com/group/admin/res/BannerRes.java`
- [ ] T006 [P] 建立 `BannerCondition.java` 管理員列表篩選條件類別（欄位：`storeId`、`status`、`keyword`，全部為選用）`src/main/java/com/group/admin/condition/BannerCondition.java`
- [ ] T007 建立 `BannerMapper.java` MyBatis Mapper 介面（方法簽名：`insert(Banner)`、`selectById(String)`、`selectList(BannerCondition)`、`updateByPrimaryKeySelective(Banner)`、`deleteByPrimaryKey(String)`、`selectActiveBanners()`、`autoPublishBanners()`、`autoUnpublishBanners()`、`unpublishBannersByStoreId(String)`）`src/main/java/com/group/admin/mapper/BannerMapper.java`
- [ ] T008 建立 `BannerService.java` Service 介面（方法簽名：`createBanner(BannerCreateReq)`、`getBannerById(String)`、`queryBanners(QueryReq<BannerCondition>)`、`updateBanner(String, BannerUpdateReq)`、`deleteBanner(String)`、`getActiveBanners()`、`autoPublishBanners()`、`autoUnpublishBanners()`、`unpublishBannersByStoreId(String)`）`src/main/java/com/group/admin/service/BannerService.java`

**檢查點**：基礎架構就緒——可開始平行實作各使用者故事

---

## 第三階段：使用者故事 1 — 管理員建立並發佈橫幅廣告（優先級：P1）🎯 MVP

**目標**：管理員可完整進行廣告 CRUD 操作（建立、查詢、更新、刪除），已發佈廣告立即出現在公開輪播中

**獨立測試標準**：
1. `POST /api/admin/banners` 建立含 `status: "PUBLISHED"` 的廣告 → HTTP 201，回傳完整 `BannerRes`（含 `storeName`、`linkUrl`）
2. `POST /api/admin/banners/list` 回傳所有廣告（含 DRAFT/PUBLISHED/UNPUBLISHED）
3. `PUT /api/admin/banners/{id}` 更新 `status` 為 `UNPUBLISHED` → HTTP 200
4. `DELETE /api/admin/banners/{id}` → HTTP 204
5. `GET /api/banners` 僅回傳 `status = PUBLISHED` 且符合時間範圍且店家為 ACTIVE 的廣告，依 `order_num ASC` 排序

### US1 實作

- [ ] T009 [US1] `BannerMapper.xml`：新增 `BannerResultMap`（映射所有欄位含 `store_name`→`storeName`、`store_logo_url`→`storeLogoUrl`）及 `insert` SQL 語句（插入所有欄位，`created_at`/`updated_at` 使用 NOW()）`src/main/resources/mapper/BannerMapper.xml`
- [ ] T010 [US1] `BannerMapper.xml`：新增 `selectById` SQL 語句（`SELECT b.*, s.store_name, s.logo_url AS store_logo_url, CONCAT('/stores/', b.store_id) AS link_url FROM banner b LEFT JOIN store s ON b.store_id = s.id WHERE b.id = #{id}`）`src/main/resources/mapper/BannerMapper.xml`
- [ ] T011 [US1] `BannerMapper.xml`：新增 `selectList` SQL 語句（管理員查詢：LEFT JOIN store，`<if>` 動態條件篩選 storeId/status/keyword（title LIKE），`ORDER BY b.order_num ASC, b.created_at DESC`，使用 PageHelper 分頁）`src/main/resources/mapper/BannerMapper.xml`
- [ ] T012 [US1] `BannerMapper.xml`：新增 `updateByPrimaryKeySelective` SQL 語句（`<set>` 動態僅更新非 null 欄位，強制更新 `updated_at = NOW()`）及 `deleteByPrimaryKey` SQL 語句（`DELETE FROM banner WHERE id = #{id}`）`src/main/resources/mapper/BannerMapper.xml`
- [ ] T013 [US1] `BannerMapper.xml`：新增 `selectActiveBanners` SQL 語句（公開輪播查詢：`INNER JOIN store s ON b.store_id = s.id AND s.status = 'ACTIVE'`，`WHERE b.status = 'PUBLISHED' AND (b.start_time IS NULL OR b.start_time <= NOW()) AND (b.end_time IS NULL OR b.end_time >= NOW())`，`ORDER BY b.order_num ASC, b.created_at ASC`）`src/main/resources/mapper/BannerMapper.xml`
- [ ] T014 [US1] `BannerServiceImpl.java`：實作 `createBanner`（驗證 storeId 對應店家存在且未刪除，否則拋出 400 `店家不存在或已刪除`；驗證 startTime 必須早於 endTime；生成 UUID；`linkUrl` 計算為 `/stores/{storeId}`，不接受客戶端輸入；設定 `createdAt`/`updatedAt` 為 Asia/Taipei 時區當前時間；`orderNum` null 時預設為 0；`status` null 時預設為 `DRAFT`）`src/main/java/com/group/admin/service/impl/BannerServiceImpl.java`
- [ ] T015 [US1] `BannerServiceImpl.java`：實作 `getBannerById`（查詢不存在時拋出 404 `廣告不存在`）、`queryBanners`（使用 PageHelper 包裝 `BannerCondition` 分頁查詢）、`deleteBanner`（先確認存在再硬刪除，不存在則拋出 404）`src/main/java/com/group/admin/service/impl/BannerServiceImpl.java`
- [ ] T016 [US1] `BannerServiceImpl.java`：實作 `updateBanner`（先確認廣告存在，否則拋出 404；使用 `updateByPrimaryKeySelective` 部分更新；若 `storeId` 有變更則重新計算 `linkUrl`；驗證 startTime/endTime 順序；更新後重新查詢並回傳完整 `BannerRes`）、`getActiveBanners`（直接呼叫 `selectActiveBanners()`）`src/main/java/com/group/admin/service/impl/BannerServiceImpl.java`
- [ ] T017 [US1] `AdminBannerController.java`：建立控制器（`@RestController`、`@RequestMapping("/admin/banners")`、`@PreAuthorize("hasRole('ADMIN')")`）；實作 `POST /` createBanner（`@ResponseStatus(HttpStatus.CREATED)`，`@Valid @RequestBody BannerCreateReq`）；實作 `POST /list` queryBanners（`@RequestBody(required = false) QueryReq<BannerCondition>`）`src/main/java/com/group/admin/controller/admin/AdminBannerController.java`
- [ ] T018 [US1] `AdminBannerController.java`：實作 `PUT /{id}` updateBanner（`@PathVariable String id`，`@Valid @RequestBody BannerUpdateReq`，回傳 HTTP 200 更新後的 `BannerRes`）；實作 `DELETE /{id}` deleteBanner（`@ResponseStatus(HttpStatus.NO_CONTENT)`，回傳 HTTP 204）`src/main/java/com/group/admin/controller/admin/AdminBannerController.java`
- [ ] T019 [US1] `BannerController.java`：建立公開端點控制器（`@RestController`、`@RequestMapping("/banners")`，**無** `@PreAuthorize`）；實作 `GET /` getActiveBanners（回傳 `List<BannerRes>`，永遠 HTTP 200，無廣告時回傳空陣列 `[]`，不得回傳 404）`src/main/java/com/group/admin/controller/api/BannerController.java`

**檢查點**：使用者故事 1 應完整可運作——管理員可建立/查詢/更新/刪除廣告，已發佈廣告出現在輪播中

---

## 第四階段：使用者故事 2 — 管理員排程廣告發佈（優先級：P2）

**目標**：廣告依設定的 `startTime`/`endTime` 自動發佈與下架，無需管理員手動操作，狀態轉換延遲 ≤ 1 分鐘

**獨立測試標準**：
1. 建立 `startTime` 設為 1 分鐘後的廣告（`status = DRAFT`）→ 等待 60–120 秒 → `GET /api/banners` 回傳該廣告
2. 建立 `endTime` 設為 1 分鐘後的 PUBLISHED 廣告 → 等待後 → 廣告從輪播中消失
3. 應用程式日誌中出現 `[scheduled] Banner auto-publish` / `[scheduled] Banner auto-unpublish` 訊息

### US2 實作

- [ ] T020 [US2] `BannerMapper.xml`：新增 `autoPublishBanners` SQL 語句（`UPDATE banner SET status = 'PUBLISHED', updated_at = NOW() WHERE status IN ('DRAFT', 'UNPUBLISHED') AND start_time IS NOT NULL AND start_time <= NOW() AND (end_time IS NULL OR end_time > NOW())`）`src/main/resources/mapper/BannerMapper.xml`
- [ ] T021 [US2] `BannerMapper.xml`：新增 `autoUnpublishBanners` SQL 語句（`UPDATE banner SET status = 'UNPUBLISHED', updated_at = NOW() WHERE status = 'PUBLISHED' AND end_time IS NOT NULL AND end_time < NOW()`）`src/main/resources/mapper/BannerMapper.xml`
- [ ] T022 [US2] `BannerServiceImpl.java`：實作 `autoPublishBanners()`（呼叫 mapper，記錄 log `[scheduled] Banner auto-publish: {} banners published`，回傳影響筆數）及 `autoUnpublishBanners()`（呼叫 mapper，記錄 log `[scheduled] Banner auto-unpublish: {} banner(s) unpublished`，回傳影響筆數）`src/main/java/com/group/admin/service/impl/BannerServiceImpl.java`
- [ ] T023 [US2] `ScheduledTasks.java`：注入 `BannerService`；新增 `bannerScheduleTick()` 方法，標注 `@Scheduled(fixedRate = 60000)`，依序呼叫 `bannerService.autoPublishBanners()` 與 `bannerService.autoUnpublishBanners()`（遵循 research.md 決策：使用現有 `@EnableScheduling` 基礎設施，無需新增排程框架）`src/main/java/com/group/admin/scheduler/ScheduledTasks.java`

**檢查點**：使用者故事 2 應完整可運作——排程自動發佈/下架在 60 秒內生效

---

## 第五階段：使用者故事 3 — 管理員管理廣告顯示順序（優先級：P2）

**目標**：廣告依 `orderNum ASC` 在首頁輪播中排列，`orderNum` 相同時以 `createdAt ASC` 為次要排序；管理員可透過 PUT 即時調整順序

**獨立測試標準**：
1. 建立 3 個 `orderNum` 分別為 3、1、2 的廣告 → `GET /api/banners` 回傳順序為 orderNum 1→2→3
2. 透過 `PUT /api/admin/banners/{id}` 更改 orderNum → 下次 GET 立即反映新順序
3. 兩個相同 `orderNum` 的廣告 → 較早 `createdAt` 者排在前面

### US3 實作

- [ ] T024 [US3] `BannerCreateReq.java`：確認 `orderNum` 欄位標注 `@Min(0)` 驗證（`顯示順序不得為負數`），若客戶端未傳入則 service 層預設為 `0`；確認 `status` 欄位有效值驗證（僅接受 `DRAFT`/`PUBLISHED`/`UNPUBLISHED`，否則拋出 `狀態值無效`）`src/main/java/com/group/admin/req/BannerCreateReq.java`
- [ ] T025 [US3] `BannerUpdateReq.java`：確認 `orderNum` 欄位標注 `@Min(0)` 驗證；確認 `status` 欄位有效值驗證（`DRAFT`/`PUBLISHED`/`UNPUBLISHED`）`src/main/java/com/group/admin/req/BannerUpdateReq.java`
- [ ] T026 [US3] `BannerMapper.xml`：確認 `selectList`（管理員）使用 `ORDER BY b.order_num ASC, b.created_at DESC`，`selectActiveBanners`（公開輪播）使用 `ORDER BY b.order_num ASC, b.created_at ASC`（遵循 research.md 決策：相同 `orderNum` 以 `createdAt ASC` 為次要排序）`src/main/resources/mapper/BannerMapper.xml`

**檢查點**：使用者故事 3 應完整可運作——輪播依設定順序顯示廣告，順序更新立即生效

---

## 第六階段：使用者故事 4 — 廣告可見性與店家狀態連動（優先級：P3）

**目標**：連結至已停用（INACTIVE）店家的廣告自動從公開輪播中排除；店家被刪除時其所有廣告自動下架（UNPUBLISHED）

**獨立測試標準**：
1. `UPDATE store SET status = 'INACTIVE' WHERE id = '{storeId}'` → `GET /api/banners` 回傳不含該店家廣告
2. `UPDATE store SET status = 'ACTIVE' WHERE id = '{storeId}'` → 廣告重新出現於輪播（前提：仍在排程時間內且狀態為 PUBLISHED）
3. 刪除店家 → 該店家所有廣告 `status` 變更為 `UNPUBLISHED`

### US4 實作

- [ ] T027 [P] [US4] `BannerMapper.xml`：新增 `unpublishBannersByStoreId` SQL 語句（`UPDATE banner SET status = 'UNPUBLISHED', updated_at = NOW() WHERE store_id = #{storeId} AND status != 'UNPUBLISHED'`，用於店家刪除時的級聯軟下架）`src/main/resources/mapper/BannerMapper.xml`
- [ ] T028 [US4] `BannerServiceImpl.java`：實作 `unpublishBannersByStoreId(String storeId)`（呼叫 mapper 批次更新，記錄 log `[cascade] Unpublished {} banner(s) for deleted store {}`；**不**處理店家停用 INACTIVE 的情況——公開輪播已透過 selectActiveBanners 的 INNER JOIN 自動排除）`src/main/java/com/group/admin/service/impl/BannerServiceImpl.java`
- [ ] T029 [US4] `StoreServiceImpl.java`（或現有店家刪除方法）：在店家刪除流程中注入 `BannerService`，於執行店家刪除前呼叫 `bannerService.unpublishBannersByStoreId(storeId)`（遵循 research.md 決策：service 層級聯；廣告記錄保留不硬刪除以維護計費稽核軌跡）`src/main/java/com/group/admin/service/impl/StoreServiceImpl.java`

**檢查點**：使用者故事 4 應完整可運作——店家停用時廣告即時從輪播消失，店家刪除時廣告自動下架

---

## 最終階段：收尾與橫向關注點（Polish）

**目的**：強化錯誤處理、驗證邊緣情況並執行端對端整合確認

- [ ] T030 [P] 確認全域例外處理器（`GlobalExceptionHandler` 或等效類別）能正確攔截並格式化以下來自 BannerService 的例外：`404 廣告不存在`、`400 店家不存在或已刪除`、`400 廣告圖片為必填`、`400 發佈時間必須早於下架時間`、`400 顯示順序不得為負數`、`400 狀態值無效`，符合 contracts/ 定義的錯誤回應格式（`{ "code": 4xx, "message": "..." }`）`src/main/java/com/group/admin/...（現有全域例外處理器檔案）`
- [ ] T031 [P] 確認 `AdminBannerController.java` 端點安全設定：`@PreAuthorize("hasRole('ADMIN')")` 正確套用至所有管理後台操作，未攜帶有效 JWT 的請求回傳 `401 未授權，請先登入`，ADMIN 角色以外的請求回傳 `403 無操作權限` `src/main/java/com/group/admin/controller/admin/AdminBannerController.java`
- [ ] T032 依照 `quickstart.md` 執行完整端對端驗證流程：(1) 套用 DDL；(2) 取得 JWT token；(3) 上傳圖片；(4) 建立 PUBLISHED 廣告；(5) `GET /api/banners` 確認出現；(6) 排程測試（設 startTime 為 1 分鐘後）；(7) 停用店家確認廣告消失；(8) 刪除廣告確認 HTTP 204

---

## 相依性與執行順序

### 階段相依性

- **第一階段（Setup）**：無相依——立即可開始
- **第二階段（Foundational）**：依賴第一階段完成——**封鎖所有使用者故事**
- **第三至六階段（User Stories）**：均依賴第二階段完成
  - US1（P1）→ US2（P2）→ US3（P2）→ US4（P3）（按優先級順序執行，或多人時平行進行）
- **最終階段（Polish）**：依賴所有所需使用者故事完成後執行

### 使用者故事相依性

| 使用者故事 | 優先級 | 前置依賴 | 獨立可測試 |
|-----------|--------|----------|-----------|
| US1：建立並發佈廣告 | P1 | 第二階段 | ✅ |
| US2：排程廣告發佈 | P2 | US1（Mapper XML / ServiceImpl 共享檔案） | ✅（獨立測試排程器） |
| US3：管理顯示順序 | P2 | US1 | ✅（多廣告排序驗證） |
| US4：店家狀態連動 | P3 | US1 | ✅（停用/刪除店家測試） |

### 同一使用者故事內的執行順序

- Mapper XML（SQL 語句）→ ServiceImpl（業務邏輯）→ Controller（端點）
- 相同 XML 檔案內的任務需按序執行（同一檔案不可平行）
- 不同檔案（DTOs、Mapper/Service）標注 [P] 可平行執行

---

## 平行執行範例

### 第二階段（Foundational）——可全部平行執行

```
同時啟動：
T002 建立 Banner.java 實體
T003 建立 BannerCreateReq.java
T004 建立 BannerUpdateReq.java
T005 建立 BannerRes.java
T006 建立 BannerCondition.java
——T002–T006 完成後——
T007 建立 BannerMapper.java 介面
T008 建立 BannerService.java 介面
```

### 第三階段 US1——Mapper XML 內需順序；Controller 可平行

```
順序執行（同一 XML 檔案）：
T009 → T010 → T011 → T012 → T013（BannerMapper.xml）

可平行（不同檔案，依賴 Mapper XML 完成）：
T014 BannerServiceImpl createBanner
T015 BannerServiceImpl queryBanners/getBannerById/deleteBanner
T016 BannerServiceImpl updateBanner/getActiveBanners

Controller 待 Service 完成後：
T017 AdminBannerController (POST create + list)
T018 AdminBannerController (PUT + DELETE)
T019 BannerController (public GET)  ← 可與 T017/T018 平行
```

---

## 實作策略

### MVP 優先（僅使用者故事 1）

1. 完成第一階段：環境設定
2. 完成第二階段：基礎建設（⚠️ 封鎖所有故事）
3. 完成第三階段：使用者故事 1
4. **停止並驗證**：`POST /api/admin/banners` → `GET /api/banners` 端對端測試
5. 若驗證通過則部署

### 增量交付

1. Setup + Foundational → 基礎就緒
2. +US1 → 廣告 CRUD + 公開輪播 → 部署（MVP）
3. +US2 → 自動排程發佈/下架 → 部署
4. +US3 → 確認顯示順序邏輯 → 部署
5. +US4 → 店家狀態連動 → 部署
6. Polish → 強化錯誤處理 → 正式上線

### 多人平行策略

1. 團隊共同完成第一、二階段
2. 第二階段完成後：
   - 開發者 A：US1（核心 CRUD）
   - 開發者 B：US2（排程器，待 T007/T008 完成後即可開始 Mapper XML 部分）
3. US1 完成後：
   - 開發者 A：US3（驗證排序、orderNum 驗證）
   - 開發者 B：US4（店家級聯邏輯）

---

## 備註

- **[P]** 任務 = 不同檔案且無相依，可平行執行
- **[Story]** 標籤將任務對應至特定使用者故事，確保可追溯性
- 每個使用者故事均可獨立完成與測試
- BannerMapper.xml 所有任務因位於同一 XML 檔案，建議順序執行
- 圖片上傳透過現有 `POST /api/admin/upload`（`S3Service`，資料夾 `banner/`）——非本 feature 範疇，無需修改
- `linkUrl` 計算邏輯（`/stores/{storeId}`）由 server 端執行，不接受客戶端輸入（FR-004）
- 廣告刪除不影響 S3 圖片——圖片生命週期由資產管理系統另行處理
- `@EnableScheduling` 已在 `AsyncConfig.java` 設定——無需新增排程基礎設施（research.md 決策）
- 每完成一個任務或邏輯群組後建議 git commit
- 可在任何檢查點停止，獨立驗證當前使用者故事
