# Tasks: 商品管理重整

**Input**: `specs/019-product-overhaul/` 下的設計文件  
**Prerequisites**: `plan.md`、`spec.md`、`research.md`、`data-model.md`、`contracts/`

**Tests**: 本功能規格明確要求獨立測試、驗收情境與 `mvn test` 驗證，因此每個 user story 都包含對應測試任務。

**Organization**: 任務依 user story 分組，確保每個故事都能獨立實作、獨立測試、獨立驗收。

## Format: `[ID] [P?] [Story] Description`

- **[P]**：可平行執行（不同檔案、無未完成前置依賴）
- **[Story]**：對應 user story（`[US1]`、`[US2]`、`[US3]`、`[US4]`）
- 每個任務都包含明確檔案路徑

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: 準備資料庫腳本、MBG 設定與共用測試資料

- [X] T001 建立 `sql/019_product_overhaul.sql`，定義 `lottery` 的新欄位、廢棄欄位與歷史資料遷移 SQL
- [X] T002 [P] 更新 `generatorConfig.xml` 以納入 `lottery.payment_type`、`lottery.free_draw_threshold`、`lottery.delist_strategy`
- [X] T003 [P] 更新 `src/main/resources/data/scratch-lottery-test-data.sql` 與 `doc/test_data_corrected.sql` 的商品樣本資料，補齊新欄位預設值與 `NULL` 語意

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: 所有 user stories 共用的 persistence、DTO 與測試骨架

**⚠️ CRITICAL**: 本階段完成前，不應開始任何 user story 實作

- [X] T004 同步 `src/main/java/com/group/admin/entity/Lottery.java`、`src/main/java/com/group/admin/example/LotteryExample.java`、`src/main/java/com/group/admin/mapper/LotteryMapper.java`、`src/main/resources/mapper/LotteryMapper.xml`，使其與新欄位/廢棄欄位一致
- [X] T005 [P] 更新 `src/main/java/com/group/admin/req/lottery/LotteryCreateReq.java` 與 `src/main/java/com/group/admin/req/lottery/LotteryUpdateReq.java` 的欄位說明、允收值與廢棄欄位註記
- [X] T006 [P] 更新 `src/main/java/com/group/admin/res/lottery/LotteryRes.java` 與 `src/main/java/com/group/admin/res/lottery/LotteryWithPrizesRes.java` 的新欄位輸出與 OpenAPI 描述
- [X] T007 在 `src/main/java/com/group/admin/service/impl/LotteryServiceImpl.java` 建立共用正規化入口（`resolvePlayMode`、`resolveGameMode`、`resolveDelistStrategy`、`normalizeFreeDrawThreshold`）
- [X] T008 [P] 建立或補齊 `src/test/java/com/group/admin/service/LotteryServiceImplTest.java`、`src/test/java/com/group/admin/controller/admin/AdminLotteryControllerTest.java`、`src/test/java/com/group/admin/integration/LotteryFlowIntegrationTest.java` 的測試骨架與 fixture

**Checkpoint**: Foundation 完成後，各 user story 可依優先序展開

---

## Phase 3: User Story 1 - 店家建立商品時欄位精簡化 (Priority: P1) 🎯 MVP

**Goal**: 建立商品時依 `category + subCategory` 自動推導 `playMode`、`gameMode`、`delistStrategy`，並正規化 `freeDrawThreshold`

**Independent Test**: 後台建立 `OFFICIAL_ICHIBAN`、`GACHA`、`CUSTOM_GACHA + LOTTERY_MODE`、`CUSTOM_GACHA + SCRATCH_MODE` 商品時，欄位自動帶入與驗證規則均符合 spec；`freeDrawThreshold=NULL` 不阻擋刮刮樂建立，`freeDrawThreshold=0` 會失敗

### Tests for User Story 1

- [X] T009 [P] [US1] 在 `src/test/java/com/group/admin/service/LotteryServiceImplTest.java` 新增 `category/subCategory` 對 `playMode`、`gameMode`、`freeDrawThreshold` 正規化的成功/失敗測試
- [X] T010 [P] [US1] 在 `src/test/java/com/group/admin/controller/admin/AdminLotteryControllerTest.java` 新增 `/admin/lottery` 與 `/admin/lottery/with-prizes` 的建立/更新欄位驗證測試

### Implementation for User Story 1

- [X] T011 [US1] 在 `src/main/java/com/group/admin/service/impl/LotteryServiceImpl.java` 實作建立流程的 `category/subCategory` 衍生欄位規則與 `freeDrawThreshold` 可空語意
- [X] T012 [US1] 在 `src/main/java/com/group/admin/service/impl/LotteryServiceImpl.java` 實作更新流程的欄位清理與非 `SCRATCH_MODE` 強制回填 `freeDrawThreshold=NULL`
- [X] T013 [US1] 在 `src/main/java/com/group/admin/service/impl/LotteryServiceImpl.java` 與 `src/main/resources/mapper/LotteryMapper.xml` 移除 `multiDrawOptions`、`allowMultiDraw`、`protectionDraws`、`protectionMinutes` 的寫入依賴

**Checkpoint**: 完成後，店家可依新表單規則建立/更新商品，且 US1 可獨立驗收

---

## Phase 4: User Story 2 - 商品支付方式選擇 (Priority: P1)

**Goal**: 商品可設定 `paymentType=GOLD/BONUS`，抽獎時依商品設定扣對應錢包與記錄消費類型

**Independent Test**: 建立 `paymentType=BONUS` 的商品後，前台抽獎扣紅利而非金幣；未指定 `paymentType` 的商品預設為 `GOLD`

### Tests for User Story 2

- [X] T014 [P] [US2] 在 `src/test/java/com/group/admin/service/LotteryServiceImplTest.java` 新增 `paymentType` 預設值、合法值與非 `DRAFT` 禁改測試
- [ ] T015 [P] [US2] 在 `src/test/java/com/group/admin/integration/LotteryFlowIntegrationTest.java` 新增 `paymentType=BONUS` 商品的扣款與消費紀錄整合測試

### Implementation for User Story 2

- [X] T016 [US2] 在 `src/main/java/com/group/admin/service/impl/LotteryServiceImpl.java` 實作 `paymentType` 預設值、驗證與非 `DRAFT` 禁改規則
- [X] T017 [P] [US2] 在 `src/main/java/com/group/admin/service/impl/DrawServiceImpl.java` 依 `Lottery.paymentType` 切換餘額檢查、`costType` 與 `DRAW_GOLD` / `DRAW_BONUS` 消費紀錄
- [X] T018 [P] [US2] 在 `src/main/java/com/group/admin/service/impl/LotteryTicketServiceImpl.java` 依 `Lottery.paymentType` 切換扣款邏輯與抽獎交易記錄

**Checkpoint**: 完成後，US2 可獨立驗證商品支付幣種與抽獎扣款一致

---

## Phase 5: User Story 3 - 自動下架策略 (Priority: P2)

**Goal**: 商品依 `delistStrategy` 在抽獎後正確流轉到 `ENDED` 或 `SOLD_OUT`

**Independent Test**: `GRAND_PRIZE_DRAWN`、`ALL_DRAWN`、`MANUAL` 三種策略在抽獎後都會進入正確狀態；刮刮樂固定採 `GRAND_PRIZE_DRAWN`

### Tests for User Story 3

- [ ] T019 [P] [US3] 在 `src/test/java/com/group/admin/service/LotteryServiceImplTest.java` 新增 `GRAND_PRIZE_DRAWN`、`ALL_DRAWN`、`MANUAL` 的狀態流轉單元測試
- [ ] T020 [P] [US3] 在 `src/test/java/com/group/admin/integration/LotteryFlowIntegrationTest.java` 新增抽獎後 `checkAndDelist()` 狀態更新整合測試

### Implementation for User Story 3

- [ ] T021 [US3] 在 `src/main/java/com/group/admin/service/impl/LotteryServiceImpl.java` 實作依分類固定或接收 `delistStrategy` 的規則，並修正 `checkAndDelist()` 的 `ENDED` / `SOLD_OUT` 流轉
- [ ] T022 [P] [US3] 在 `src/main/java/com/group/admin/service/impl/DrawServiceImpl.java` 統一一般抽獎路徑的 `lotteryService.checkAndDelist(lotteryId)` 呼叫時機
- [ ] T023 [P] [US3] 在 `src/main/java/com/group/admin/service/impl/LotteryTicketServiceImpl.java` 統一刮刮樂/籤位抽獎路徑的 `lotteryService.checkAndDelist(lotteryId)` 呼叫時機

**Checkpoint**: 完成後，US3 可獨立驗證抽獎後下架策略與狀態流轉

---

## Phase 6: User Story 4 - 合併重複 Controller (Priority: P2)

**Goal**: 後台 `with-prizes` 能力收斂到 `AdminLotteryController`，前台商品讀取維持單一 `LotteryController` 邊界與既有路徑相容

**Independent Test**: `/admin/lottery/with-prizes/**` 在合併後仍可正常建立/更新/查詢；搜尋不到 `AdminLotteryWithPrizesController`；public `/lottery` 列表與詳情路徑保持可用

### Tests for User Story 4

- [ ] T024 [P] [US4] 在 `src/test/java/com/group/admin/controller/admin/AdminLotteryControllerTest.java` 補 `/admin/lottery/with-prizes/**` 合併後路由與回應測試
- [ ] T025 [P] [US4] 在 `src/test/java/com/group/admin/controller/api/LotteryControllerTest.java` 驗證 public `/lottery` 列表與詳情路徑的相容測試

### Implementation for User Story 4

- [ ] T026 [US4] 將 `src/main/java/com/group/admin/controller/admin/AdminLotteryWithPrizesController.java` 的建立/更新/查詢/列表能力收斂到 `src/main/java/com/group/admin/controller/admin/AdminLotteryController.java`
- [ ] T027 [US4] 刪除 `src/main/java/com/group/admin/controller/admin/AdminLotteryWithPrizesController.java` 並同步清理 `src/test/java/com/group/admin/controller/admin/AdminLotteryWithPrizesControllerTest.java`
- [ ] T028 [US4] 整理 `src/main/java/com/group/admin/controller/api/LotteryController.java` 與 `src/test/java/com/group/admin/controller/api/LotteryControllerTest.java`，確認 public list/detail 由單一 controller 維持相容

**Checkpoint**: 完成後，US4 可獨立驗證 controller 收斂與路徑相容

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: 收尾文件、前端協作說明與整體驗證

- [ ] T029 [P] 更新 `docs/02-API文件/LOTTERY_WITH_PRIZES_API_GUIDE.md` 與 `docs/02-API文件/FRONTEND_API_COMPLETE_GUIDE.md` 的新欄位與固定規則
- [ ] T030 [P] 更新 `frontend/admin/05-product-management.md` 與 `frontend/admin/README.md` 的表單顯示、送值與 `freeDrawThreshold` 語意
- [ ] T031 執行 `mvn clean compile`、`mvn test`、`mvn clean package -DskipTests`，並依結果確認 `specs/019-product-overhaul/quickstart.md` 的驗證流程可重現

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: 無依賴，可立即開始
- **Phase 2 (Foundational)**: 依賴 Phase 1 完成，且會阻塞所有 user stories
- **Phase 3 (US1)**: 依賴 Phase 2 完成
- **Phase 4 (US2)**: 依賴 Phase 2 完成；可與 US1 平行，但會共享 `LotteryServiceImpl.java`，需協調合併順序
- **Phase 5 (US3)**: 依賴 US1 完成的欄位正規化基礎；建議在 US1 後執行
- **Phase 6 (US4)**: 依賴 US1 的 request/response 規則與 US3 的 service 邏輯定稿後執行
- **Phase 7 (Polish)**: 依賴所有目標 user stories 完成

### User Story Dependencies

- **US1 (P1)**: 無其他故事依賴，是 MVP 基礎
- **US2 (P1)**: 依賴共用欄位/DTO 基礎，但可不等待 US3、US4
- **US3 (P2)**: 依賴 US1 的 `delistStrategy` 與 `freeDrawThreshold` 正規化完成
- **US4 (P2)**: 依賴 US1 的 contract 穩定後再做 controller 收斂，避免合併期間反覆改路由

### Within Each User Story

- 先寫測試，再讓測試失敗，再補實作
- DTO / model 正規化先於 service 行為
- service 邏輯先於 controller 收斂
- 每個故事完成後都應能單獨執行其獨立測試標準

---

## Parallel Opportunities

- Setup 階段的 `T002`、`T003` 可與 `T001` 平行準備
- Foundational 階段的 `T005`、`T006`、`T008` 可平行
- US1 的 `T009` 與 `T010` 可平行
- US2 的 `T017` 與 `T018` 可平行
- US3 的 `T022` 與 `T023` 可平行
- US4 的 `T024` 與 `T025` 可平行
- Polish 階段的 `T029` 與 `T030` 可平行

---

## Parallel Example: User Story 1

```text
T009 [US1] src/test/java/com/group/admin/service/LotteryServiceImplTest.java
T010 [US1] src/test/java/com/group/admin/controller/admin/AdminLotteryControllerTest.java
```

```text
T011 [US1] src/main/java/com/group/admin/service/impl/LotteryServiceImpl.java
T013 [US1] src/main/resources/mapper/LotteryMapper.xml
```

## Parallel Example: User Story 2

```text
T017 [US2] src/main/java/com/group/admin/service/impl/DrawServiceImpl.java
T018 [US2] src/main/java/com/group/admin/service/impl/LotteryTicketServiceImpl.java
```

---

## Implementation Strategy

### MVP First (US1 Only)

1. 完成 Phase 1: Setup
2. 完成 Phase 2: Foundational
3. 完成 Phase 3: US1
4. 執行 US1 的獨立測試與後台建立流程驗證
5. 若 US1 穩定，再擴展到 US2

### Incremental Delivery

1. Setup + Foundational 完成後，先交付 US1
2. 在 US1 穩定後交付 US2，完成支付方式切換
3. 接著交付 US3，完成自動下架
4. 最後交付 US4 與收尾文件，降低 controller 合併造成的回歸風險

### Parallel Team Strategy

1. 團隊先共同完成 T001-T008
2. 之後可分流：
   - 開發者 A：US1
   - 開發者 B：US2
   - 開發者 C：US3
3. 待 service 規則穩定後，再由單一開發者收斂 US4 的 controller 合併

---

## Notes

- 所有任務都遵守 `- [ ] T### [P?] [US?] 描述 + 檔案路徑` 格式
- `[P]` 任務代表可平行，不代表可以忽略 phase 依賴
- 本次 feature 的核心風險在 `LotteryServiceImpl.java` 與抽獎服務兩條路徑，合併時要避免不同 user story 在同一檔案互相覆蓋
- 完成每個 story 後都應先跑對應測試，再進行下一階段
