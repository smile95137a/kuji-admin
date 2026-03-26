# 任務清單：商品與抽獎管理 (Product & Lottery Management)

**功能分支**：`011-product-lottery`
**輸入**：設計文件來自 `specs/011-product-lottery/`
**前置條件**：plan.md ✅ | spec.md ✅ | research.md ✅ | data-model.md ✅ | contracts/ ✅ | quickstart.md ✅

**測試**：本功能規格未明確要求測試先行，測試任務略去。

**組織方式**：任務依使用者故事分組，讓每個故事可獨立實作與驗測。

## 格式：`[ID] [P?] [Story?] 描述（含檔案路徑）`

- **[P]**：可並行執行（操作不同檔案、無依賴關係）
- **[US#]**：對應的使用者故事編號
- 每個任務均含明確的目標檔案路徑

---

## Phase 1：初始化設定 (Setup)

**目的**：套用資料庫結構異動，確保所有後續程式碼可安全編譯

- [ ] T001 建立資料庫遷移腳本，新增 `lottery` 資料表的 `source_lottery_id`、`configured_at`、`drawable_at`、`remaining_draws`、`discount_trigger_level` 欄位，以及 `lottery_prize` 資料表的 `recycle_bonus` 欄位，並加入 `idx_lottery_scheduled_at`、`idx_lottery_start_time`、`idx_lottery_source_id`、`idx_prize_lottery_level` 索引，存放於 `src/main/resources/db/migration/V011__product_lottery_enhancements.sql`
- [ ] T002 [P] 在 `src/main/java/com/group/admin/enums/LotteryStatusEnum.java` 中新增 `CONFIGURED`（已配置）、`DRAWABLE`（可抽）、`SOLD_OUT`（售完）三個列舉值（依照 quickstart.md Step 2 的 Java 範例實作）

---

## Phase 2：基礎元件 (Foundational)

**目的**：本階段的基礎元件是所有使用者故事的前置條件，**必須全數完成後才能開始任何使用者故事**

⚠️ **關鍵**：以下元件未完成前，任何使用者故事均無法開始

- [ ] T003 在 `src/main/java/com/group/admin/entity/Lottery.java` 中新增 `sourceLotteryId`（String）、`configuredAt`（LocalDateTime）、`drawableAt`（LocalDateTime）、`remainingDraws`（Integer）、`discountTriggerLevel`（String）五個欄位，並補上對應 Lombok `@Data` / getter/setter（遵循現有 plain-POJO MyBatis 模式）
- [ ] T004 [P] 在 `src/main/java/com/group/admin/entity/LotteryPrize.java` 中新增 `recycleBonus`（Long，預設 0）欄位
- [ ] T005 在 `src/main/resources/mapper/LotteryMapper.xml` 中更新 `<resultMap>` 及 `<sql id="Base_Column_List">` 以涵蓋 T003 所有新欄位；同時新增 `selectScheduledForPromotion`（CONFIGURED → ON_SHELF 查詢）與 `selectDrawableForStart`（ON_SHELF → DRAWABLE 查詢）兩個 SELECT 語句
- [ ] T006 [P] 在 `src/main/resources/mapper/LotteryPrizeMapper.xml` 的 `<resultMap>` 與 `<sql id="Base_Column_List">` 中新增 `recycle_bonus` 欄位映射
- [ ] T007 [P] 建立 `src/main/java/com/group/admin/req/CreateLotteryReq.java`，包含 `title`、`category`、`subCategory`、`description`、`content`、`imageUrl`、`galleryImages`、`pricePerDraw`、`discountedPrice`、`autoDiscountEnabled`、`discountTriggerLevel`、`allowMultiDraw`、`multiDrawOptions`、`scheduledAt`、`startTime`、`protectionDraws`、`protectionMinutes`、`lastPrizeMode`、`bonusEnabled`、`orderNum`、`tags`、`theme`、`prizes`（List）欄位（依照 `contracts/POST_admin_lottery.md` Field Constraints 定義）
- [ ] T008 [P] 建立 `src/main/java/com/group/admin/req/UpdateLotteryReq.java`，包含 `PUT /admin/lottery/{id}` 合約中所有可更新欄位，並在各欄位加入 Javadoc 標示欄位鎖定規則（依照 `contracts/PUT_admin_lottery_{id}.md` Field Lock Rules 定義）
- [ ] T009 [P] 建立 `src/main/java/com/group/admin/req/LotteryStatusChangeReq.java`，包含 `targetStatus`（String，必填）與 `reason`（String，選填）欄位（依照 `contracts/PUT_admin_lottery_{id}_status.md` 定義）
- [ ] T010 [P] 建立 `src/main/java/com/group/admin/req/LotteryListReq.java`，包含 `page`（預設 1）、`pageSize`（預設 20，最大 50）、`category`、`storeId`、`status`、`keyword`、`sort`（HOT / NEW / PRICE_ASC / PRICE_DESC）欄位（依照 `contracts/GET_api_lottery.md` Query Parameters 定義）
- [ ] T011 [P] 建立 `src/main/java/com/group/admin/res/LotteryDetailRes.java`，包含 `contracts/GET_api_lottery_{id}.md` 回應格式所有欄位：`id`、`storeId`、`storeName`、`storeLogoUrl`、`title`、`category`、`subCategory`、`description`、`content`、`imageUrl`、`galleryImages`、`pricePerDraw`、`discountedPrice`、`currentPrice`、`autoDiscountEnabled`、`discountTriggerLevel`、`discountTriggered`、`allowMultiDraw`、`multiDrawOptions`、`status`、`totalDraws`、`remainingDraws`、`scheduledAt`、`startTime`、`protectionDraws`、`protectionMinutes`、`lastPrizeMode`、`hasLastPrize`、`hotCount`、`isProtected`、`protectionExpiresAt`、`prizes`（List）、`recentDraws`（List）、`createdAt`
- [ ] T012 [P] 建立 `src/main/java/com/group/admin/res/LotteryListItemRes.java`，包含 `contracts/GET_api_lottery.md` items 陣列中所有欄位：`id`、`storeId`、`storeName`、`title`、`category`、`subCategory`、`imageUrl`、`pricePerDraw`、`currentPrice`、`status`、`totalDraws`、`remainingDraws`、`hotCount`、`tags`、`scheduledAt`、`startTime`、`isProtected`、`hasLastPrize`、`lastPrizeMode`
- [ ] T013 在 `src/main/java/com/group/admin/mapper/LotteryMapper.java` 中新增 `selectScheduledForPromotion()`、`selectDrawableForStart()`、`selectForUpdate(String lotteryId)` 三個方法介面宣告，以配合 T005 的 XML 語句

**Checkpoint**：基礎元件就緒，各使用者故事可開始並行實作

---

## Phase 3：使用者故事 1 — 店家負責人建立並配置抽獎商品（優先級：P1）🎯 MVP

**目標**：讓店家負責人可以建立含獎品池的抽獎商品並儲存為草稿，包含更新和複製功能

**獨立測試**：店家負責人建立「官方一番賞」類型商品，新增 A～D 獎品，設定每次抽獎價格，儲存為草稿。
呼叫 `POST /admin/lottery` 應回傳 `status: "DRAFT"` 並含有 `id`；
呼叫 `POST /admin/lottery/{id}/copy` 應回傳新的 `id`，`sourceLotteryId` = 原 id，`status: "DRAFT"`。

### 使用者故事 1 實作

- [ ] T014 [P] [US1] 在 `src/main/java/com/group/admin/service/LotteryService.java` 中新增 `createLottery(CreateLotteryReq req, String operatorId, String storeId)`、`updateLottery(String id, UpdateLotteryReq req, String operatorId)`、`copyLottery(String sourceId, String newTitle, LocalDateTime scheduledAt, LocalDateTime startTime, String operatorId)` 三個方法介面宣告
- [ ] T015 [P] [US1] 在 `src/main/java/com/group/admin/service/LotteryPrizeService.java` 中新增 `createPrizesForLottery(String lotteryId, List<CreateLotteryReq.PrizeItem> prizes)` 與 `copyPrizesForLottery(String sourceLotteryId, String newLotteryId)` 方法介面宣告（支援建立時一併儲存獎品，以及複製時深度複製所有獎品並重設 remaining = quantity）
- [ ] T016 [US1] 在 `src/main/java/com/group/admin/service/impl/LotteryServiceImpl.java` 中實作 `createLottery()` 方法：以 `UUID.randomUUID().toString()` 產生 id，`status = DRAFT`，`storeId` 自 JWT 解析（不允許請求端自行指定），`createdBy` 自 JWT subject 設定，若請求帶有 `prizes` 則同步呼叫 `LotteryPrizeService.createPrizesForLottery()`
- [ ] T017 [US1] 在 `src/main/java/com/group/admin/service/impl/LotteryServiceImpl.java` 中實作 `updateLottery()` 方法：先查詢現有狀態，逐一比對請求欄位是否在當前狀態下可修改（依照 `contracts/PUT_admin_lottery_{id}.md` Field Lock Rules），若有被鎖定欄位且值有異動則拋出 `FIELD_LOCKED` 錯誤，通過驗證後執行 UPDATE 並刷新 `updatedAt`
- [ ] T018 [US1] 在 `src/main/java/com/group/admin/service/impl/LotteryServiceImpl.java` 中實作 `copyLottery()` 方法：查詢來源商品，產生新 UUID，設定 `sourceLotteryId` = 來源 id，`status = DRAFT`，重置 `totalDraws = null`、`remainingDraws = null`、`hotCount = 0`、`ticketsGenerated = 0`，清空所有時間戳記欄位，套用請求提供的 `newTitle`（若為空則附加「(複製)」後綴），呼叫 `LotteryPrizeService.copyPrizesForLottery()` 複製所有獎品
- [ ] T019 [US1] 在 `src/main/java/com/group/admin/controller/admin/AdminLotteryController.java` 中新增 `POST /admin/lottery` 端點（對應合約 `POST_admin_lottery.md`）：解析 JWT 取得 `operatorId` 與 `storeId`，呼叫 `LotteryService.createLottery()`，回傳 HTTP 201 含 `LotteryDetailRes` 的精簡版欄位（id、status、title、category、pricePerDraw、totalDraws、remainingDraws、createdAt）
- [ ] T020 [US1] 在 `src/main/java/com/group/admin/controller/admin/AdminLotteryController.java` 中新增 `PUT /admin/lottery/{id}` 端點（對應合約 `PUT_admin_lottery_{id}.md`）：驗證呼叫者擁有該商品所屬的店家，呼叫 `LotteryService.updateLottery()`，回傳 HTTP 200 含 id、status、title、updatedAt
- [ ] T021 [US1] 在 `src/main/java/com/group/admin/controller/admin/AdminLotteryController.java` 中新增 `POST /admin/lottery/{id}/copy` 端點（對應合約 `POST_admin_lottery_{id}_copy.md`）：驗證呼叫者擁有來源商品的店家，呼叫 `LotteryService.copyLottery()`，回傳 HTTP 201 含 id、status、title、sourceLotteryId、totalPrizes、createdAt
- [ ] T022 [US1] 在 `src/main/java/com/group/admin/service/impl/LotteryServiceImpl.java` 的 `createLottery()` 與 `updateLottery()` 中加入下列業務規則驗證：`INVALID_CATEGORY`（不合法分類）、`INVALID_PRICE`（pricePerDraw ≤ 0）、`SUBCATEGORY_REQUIRED`（category=CUSTOM_GASHAPON 且缺少 subCategory）、`MULTIPLE_LAST_PRIZE`（多個 isLastPrize=true）、`DISCOUNT_CONFIG_INCOMPLETE`（autoDiscountEnabled=true 但缺少 discountedPrice 或 discountTriggerLevel）

**Checkpoint**：使用者故事 1 完成——可獨立測試建立、更新、複製抽獎商品

---

## Phase 4：使用者故事 2 — 玩家從進行中的抽獎商品抽獎（優先級：P1）

**目標**：玩家可瀏覽公開的抽獎商品清單與詳情，抽獎邏輯具備悲觀鎖定並在售完時自動轉換狀態

**獨立測試**：
1. 呼叫 `GET /api/lottery?status=IN_PROGRESS` 應回傳進行中商品列表，包含 `remainingDraws`、`isProtected`、`currentPrice`。
2. 呼叫 `GET /api/lottery/{id}` 應回傳完整獎品池（含各獎品 remaining 數量）與最近 10 筆抽獎紀錄（匿名）。
3. 執行最後一抽後，商品 `status` 應在 5 秒內自動變為 `SOLD_OUT`（SC-005）。

### 使用者故事 2 實作

- [ ] T023 [P] [US2] 在 `src/main/resources/mapper/LotteryMapper.xml` 中新增 `selectForUpdate` 語句（`SELECT ... FROM lottery WHERE id = #{id} FOR UPDATE`），配合 T013 的方法介面，用於抽獎前的悲觀行鎖定
- [ ] T024 [P] [US2] 在 `src/main/resources/mapper/LotteryMapper.xml` 中新增公開清單查詢語句：`status IN ('ON_SHELF','DRAWABLE','IN_PROGRESS')` 篩選、LEFT JOIN store 取得 `storeName`、支援 `category`、`storeId`、`keyword`（LIKE title 或 tags）篩選、支援 HOT / NEW / PRICE_ASC / PRICE_DESC 排序、使用 PageHelper 或 LIMIT/OFFSET 分頁
- [ ] T025 [US2] 在 `src/main/java/com/group/admin/controller/api/LotteryBrowseController.java` 中實作 `GET /api/lottery` 端點（對應合約 `GET_api_lottery.md`）：呼叫 Mapper 公開清單查詢，組裝 `LotteryListItemRes`（含 `currentPrice`：若 discountTriggered 則回傳 discountedPrice，否則回傳 pricePerDraw；`isProtected`：查詢 lottery_lock 是否有效），回傳分頁結果（items、total、pageNum、pageSize、pages）
- [ ] T026 [US2] 在 `src/main/java/com/group/admin/controller/api/LotteryBrowseController.java` 中實作 `GET /api/lottery/{id}` 端點（對應合約 `GET_api_lottery_{id}.md`）：查詢商品及獎品池，若狀態為 DRAFT / CONFIGURED / FORCED_OFF 則回傳 `403 LOTTERY_NOT_PUBLIC`；組裝 `LotteryDetailRes`（含 `prizes[]` 的 remaining 數量、最近 10 筆匿名抽獎紀錄、`isProtected`、`protectionExpiresAt`、`discountTriggered`、`currentPrice`）
- [ ] T027 [US2] 在 `src/main/java/com/group/admin/service/impl/LotteryServiceImpl.java` 中擴充或建立 `executeDraw(String lotteryId, String userId, int count)` 方法：以 `@Transactional` 包裹，呼叫 `selectForUpdate()` 取得悲觀鎖，驗證 `remainingDraws >= count`（不足則拋 `InsufficientTicketsException`），遞減 `remainingDraws`，若剩餘未達 IN_PROGRESS 且為第一抽則更新 status = IN_PROGRESS，呼叫隨機選獎邏輯（1/N 等機率），寫入 `LotteryDrawRecord`，最後呼叫 T028 的 SOLD_OUT 轉換檢查
- [ ] T028 [US2] 在 `src/main/java/com/group/admin/service/impl/LotteryServiceImpl.java` 的 `executeDraw()` 尾端加入同步 SOLD_OUT 轉換：若 `remainingDraws == 0` 則在同一交易中將 `status` 設為 `SOLD_OUT`、`updatedAt = NOW()`，確保滿足 SC-005（最後一次抽獎後 5 秒內轉換完成）

**Checkpoint**：使用者故事 2 完成——公開瀏覽 API 可正常回傳資料，抽獎邏輯具備悲觀鎖且售完時自動轉換

---

## Phase 5：使用者故事 3 — 店家負責人管理商品生命週期（優先級：P2）

**目標**：管理員可透過 API 手動觸發狀態轉換，系統每 60 秒自動推進排程的狀態轉換，售完後不可進一步抽獎

**獨立測試**：
1. `PUT /admin/lottery/{id}/status` 帶 `targetStatus: "CONFIGURED"` 應計算並回傳 `totalDraws` 與 `remainingDraws`。
2. 設定 `scheduledAt` 為未來 2 分鐘後，轉換至 CONFIGURED，等待排程器執行，觀察狀態自動變為 ON_SHELF。
3. 強制下架（FORCED_OFF）後，`GET /api/lottery/{id}` 應回傳 `403 LOTTERY_NOT_PUBLIC`。

### 使用者故事 3 實作

- [ ] T029 [US3] 在 `src/main/java/com/group/admin/service/impl/LotteryServiceImpl.java` 中實作 `changeStatus(String lotteryId, String targetStatus, String reason, String operatorId)` 方法：查詢現有商品，依照 research.md 的完整 FSM 轉換表（DRAFT→CONFIGURED、CONFIGURED→ON_SHELF、ON_SHELF→DRAWABLE、ANY→FORCED_OFF、FORCED_OFF→DRAFT）驗證合法性，不合法則拋 `INVALID_TRANSITION` 錯誤
- [ ] T030 [US3] 在 `src/main/java/com/group/admin/service/impl/LotteryServiceImpl.java` 的 `changeStatus()` 中實作 CONFIGURED 閘道驗證：確認至少有一個獎品存在於池中、`pricePerDraw > 0`、若 `autoDiscountEnabled = true` 則 `discountedPrice` 與 `discountTriggerLevel` 必填、若有 `isLastPrize=1` 的獎品則 `lastPrizeMode` 必填；驗證通過後計算 `totalDraws`（= SUM of lottery_prize.quantity，LAST_DRAW 模式下不計入 isLastPrize=1 的獎品）並原子性寫入 `totalDraws`、`remainingDraws`、`configuredAt = NOW()`
- [ ] T031 [US3] 在 `src/main/java/com/group/admin/controller/admin/AdminLotteryController.java` 中新增 `PUT /admin/lottery/{id}/status` 端點（對應合約 `PUT_admin_lottery_{id}_status.md`）：驗證呼叫者擁有商品店家（非 Admin 角色才需要），呼叫 `LotteryService.changeStatus()`，回傳 HTTP 200 含 id、previousStatus、currentStatus、transitionedAt
- [ ] T032 [P] [US3] 在 `src/main/java/com/group/admin/scheduler/ScheduledTasks.java` 中新增 `lotteryStatusTransitionTask()` 方法，標記 `@Scheduled(fixedDelay = 60_000)`，依序呼叫 `lotteryService.promoteScheduledLotteries()` 與 `lotteryService.promoteDrawableLotteries()`（參照 research.md §2 的實作模式）
- [ ] T033 [P] [US3] 在 `src/main/resources/mapper/LotteryMapper.xml` 中新增 `promoteScheduledLotteries` UPDATE 語句：`UPDATE lottery SET status='ON_SHELF', updated_at=NOW() WHERE status='CONFIGURED' AND scheduled_at IS NOT NULL AND scheduled_at <= NOW()`
- [ ] T034 [P] [US3] 在 `src/main/resources/mapper/LotteryMapper.xml` 中新增 `promoteDrawableLotteries` UPDATE 語句：`UPDATE lottery SET status='DRAWABLE', updated_at=NOW() WHERE status='ON_SHELF' AND start_time IS NOT NULL AND start_time <= NOW()`
- [ ] T035 [US3] 在 `src/main/java/com/group/admin/service/LotteryService.java` 中新增 `promoteScheduledLotteries()` 與 `promoteDrawableLotteries()` 介面宣告，並在 `src/main/java/com/group/admin/service/impl/LotteryServiceImpl.java` 中實作，分別呼叫 T033、T034 的 Mapper 方法
- [ ] T036 [US3] 在 `src/main/java/com/group/admin/service/impl/LotteryServiceImpl.java` 的 `changeStatus()` 中實作 `FORCED_OFF → DRAFT` 重新啟用邏輯：清除 `configuredAt`、`drawableAt`，將 `remainingDraws` 重設為 `totalDraws`，若 `reason` 非空則寫入 `remark` 欄位

**Checkpoint**：使用者故事 3 完成——生命週期完整可測（手動轉換 + 排程自動推進 + 強制下架）

---

## Phase 6：使用者故事 4 — 最後大獎與降價機制（優先級：P2）

**目標**：最終一抽觸發最後賞（LAST_DRAW 模式），或最後賞以等機率進入票券池（POOL_IN 模式）；高等獎品售完後自動降價

**獨立測試**：
1. 商品設為 LAST_DRAW 模式，抽至 `remainingDraws = 1` 時執行最後一抽，應額外獲得 `isLastPrize=1` 的獎品。
2. 商品設為 POOL_IN 模式，最後賞應以等機率出現於任何一次抽獎中。
3. 所有 A、B 等級獎品抽完後，`GET /api/lottery/{id}` 的 `discountTriggered` 應為 `true`，`currentPrice` 應等於 `discountedPrice`。

### 使用者故事 4 實作

- [ ] T037 [P] [US4] 在 `src/main/resources/mapper/LotteryPrizeMapper.xml` 中新增 `selectLastPrizeByLotteryId` 查詢語句：`SELECT * FROM lottery_prize WHERE lottery_id = #{lotteryId} AND is_last_prize = 1 LIMIT 1`；同步在 `src/main/java/com/group/admin/mapper/LotteryPrizeMapper.java` 中新增對應方法宣告
- [ ] T038 [US4] 在 `src/main/java/com/group/admin/service/impl/LotteryServiceImpl.java` 的 `executeDraw()` 中實作 LAST_DRAW 最後賞邏輯：當 `lastPrizeMode = LAST_DRAW` 且 `remainingDraws == 1`（抽前）時，在發放隨機獎品後額外查詢 `isLastPrize=1` 的獎品並一同附加至本次抽獎結果，寫入 `LotteryDrawRecord`（符合 US4 驗收情境 1）
- [ ] T039 [US4] 在 `src/main/java/com/group/admin/service/impl/LotteryServiceImpl.java` 中實作 POOL_IN 最後賞邏輯：建立抽獎票券池時（轉換至 CONFIGURED 觸發 `ticketsGenerated = 0`），若 `lastPrizeMode = POOL_IN` 則將 `isLastPrize=1` 的獎品額外加入票券池（N+1 模式），使最後賞與一般獎品享有相等的 1/N+1 機率（符合 US4 驗收情境 3）
- [ ] T040 [US4] 在 `src/main/java/com/group/admin/service/impl/LotteryServiceImpl.java` 的 `executeDraw()` 中實作 autoDiscount 降價計價邏輯：若 `autoDiscountEnabled = true`，在決定抽獎費用前檢查 `discountTriggerLevel`（如 "A,B"）對應等級的所有獎品 `remaining` 是否均為 0；若全部售完則使用 `discountedPrice`，否則使用 `pricePerDraw`（符合 US4 驗收情境 2）
- [ ] T041 [US4] 在 `src/main/java/com/group/admin/controller/api/LotteryBrowseController.java` 的 `GET /api/lottery/{id}` 回應組裝中加入 `discountTriggered` 計算（查詢 discountTriggerLevel 指定等級獎品是否全部 remaining=0）與 `currentPrice` 計算（依 discountTriggered 決定回傳 discountedPrice 或 pricePerDraw），確保與 `contracts/GET_api_lottery_{id}.md` Response Fields 一致

**Checkpoint**：使用者故事 4 完成——最後大獎兩種模式均可運作，降價邏輯正確觸發

---

## Phase 7：完善與橫切關注點 (Polish & Cross-Cutting Concerns)

**目的**：安全設定驗證、整合測試、程式碼品質提升

- [ ] T042 [P] 確認 Spring Security 設定（`src/main/java/com/group/admin/config/SecurityConfig.java` 或同等設定類別）：`/api/lottery/**` 允許匿名公開存取；`/admin/lottery/**` 需要 `ADMIN` 或 `STORE_OWNER` 角色的有效 JWT
- [ ] T043 [P] 執行並驗證 `src/main/resources/db/migration/V011__product_lottery_enhancements.sql` 遷移腳本（本機或 RDS），執行後以 `DESCRIBE lottery` 與 `DESCRIBE lottery_prize` 確認所有新欄位與索引已正確建立
- [ ] T044 依照 `specs/011-product-lottery/quickstart.md` 完整執行端對端整合驗測：Step 5（建立→CONFIGURED→ON_SHELF→DRAWABLE）→ 執行抽獎至 SOLD_OUT → Step 6（複製商品）→ Step 7（排程自動轉換）→ Step 8（強制下架驗測）；確認每個步驟的 API 回應符合合約定義
- [ ] T045 [P] 為所有新增的 Service 實作方法補充 Javadoc 說明（包含 `LotteryServiceImpl` 中的 `createLottery`、`updateLottery`、`copyLottery`、`changeStatus`、`executeDraw`、`promoteScheduledLotteries`、`promoteDrawableLotteries`），並將錯誤碼字串常數（`INVALID_TRANSITION`、`PRIZE_POOL_EMPTY`、`FIELD_LOCKED` 等）統一提取至 `src/main/java/com/group/admin/` 下的共用常數類別

---

## 依賴關係與執行順序

### 階段依賴

- **Phase 1（初始化）**：無依賴，可立即開始
- **Phase 2（基礎元件）**：依賴 Phase 1 完成（T001 的 Migration 必須先執行才能正常編譯 T003/T004）
- **Phase 3 ~ 6（使用者故事）**：全部依賴 Phase 2 完成；故事間可並行，但建議依優先級 P1 → P2 順序
- **Phase 7（完善）**：依賴全部使用者故事完成

### 使用者故事依賴

- **US1 (P1)**：Phase 2 完成後可立即開始，無其他依賴
- **US2 (P1)**：Phase 2 完成後可開始；部分任務（T027、T028）依賴 US1 的商品建立功能才有可測試的資料
- **US3 (P2)**：Phase 2 完成後可開始；`changeStatus` 邏輯（T029、T030）依賴 US1 的商品存在
- **US4 (P2)**：依賴 US2 的 `executeDraw()` 骨架（T027）先完成

### 各故事內部順序

- 介面宣告（Service 介面）→ Mapper XML → 服務實作 → Controller 端點 → 驗證規則
- 可並行任務（標記 [P]）在各自故事內可同步執行（操作不同檔案）

### 並行機會

- Phase 1 的 T001 與 T002 可並行
- Phase 2 的 T004、T006~T012 均可與 T003/T005 並行（不同檔案）
- US1 的 T014 與 T015 可並行（不同介面）
- US2 的 T023 與 T024 可並行（同一 XML 檔案的不同語句區塊，建議分工後合併）
- US3 的 T032、T033、T034 可並行（不同方法/語句）
- US4 的 T037 與其他 US4 任務可並行
- Phase 7 的 T042 與 T043 可並行

---

## 並行執行範例

### 使用者故事 1（Phase 3）

```bash
# 以下任務可同時指派給不同開發者：
任務 T014：建立 LotteryService.java 介面
任務 T015：建立 LotteryPrizeService.java 介面

# T016-T018 依序實作（同一檔案 LotteryServiceImpl.java，避免衝突）

# T019-T021 可並行（同一 Controller 檔案，建議一人負責避免 merge 衝突）
```

### 使用者故事 3（Phase 5）

```bash
# 以下 Mapper 語句可並行開發：
任務 T033：promoteScheduledLotteries 語句
任務 T034：promoteDrawableLotteries 語句

# T029-T030 為同一方法的不同邏輯區塊，建議同一開發者依序完成
```

---

## 實作策略

### MVP 優先（僅完成使用者故事 1）

1. 完成 Phase 1：初始化設定
2. 完成 Phase 2：基礎元件（**必要條件，封鎖所有故事**）
3. 完成 Phase 3：使用者故事 1（建立、更新、複製商品）
4. **停止並驗測**：呼叫 `POST /admin/lottery` → `GET /admin/lottery/{id}` → `POST /admin/lottery/{id}/copy`，確認三個端點全數通過
5. 可部署 / 示範 MVP

### 漸進式交付

1. Phase 1 + Phase 2 → 基礎元件就緒
2. Phase 3（US1）→ 獨立驗測 → 部署（MVP！）
3. Phase 4（US2）→ 獨立驗測 → 部署（玩家可瀏覽並抽獎）
4. Phase 5（US3）→ 獨立驗測 → 部署（完整生命週期管理）
5. Phase 6（US4）→ 獨立驗測 → 部署（最後大獎與降價功能完整上線）
6. Phase 7 → 完善與橫切 → 正式上線

### 雙人並行策略

Phase 2 完成後：

- **開發者 A**：US1（商品 CRUD）+ US3（生命週期）— 偏向 Admin 後台
- **開發者 B**：US2（公開瀏覽 API）+ US4（最後大獎/降價）— 偏向玩家前台

---

## 備注

- `[P]` 任務 = 操作不同檔案，無相互依賴，可並行執行
- `[US#]` 標籤用於追蹤任務對應哪個使用者故事
- 每個使用者故事應可獨立完成並驗測，無需其他故事的協助
- 所有 Controller 端點須依照 `specs/011-product-lottery/contracts/` 對應合約定義實作
- 所有 Mapper 遵循既有 plain-POJO MyBatis 模式（無 JPA 標記）
- UUID 主鍵統一使用 `UUID.randomUUID().toString()` 產生
- 回應格式由 `GlobalResponseAspect` 自動包裝為 `ApiResponse<T>`，Controller 直接回傳資料物件即可
- 每完成一個任務或邏輯群組後建議提交一次 commit
- 可在任何 Checkpoint 暫停並獨立驗測該使用者故事
