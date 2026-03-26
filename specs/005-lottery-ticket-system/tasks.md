# 任務清單：抽獎票券系統（雙號碼與刮刮樂機制）

**輸入**：設計文件來自 `/specs/005-lottery-ticket-system/`  
**前置條件**：plan.md ✅、spec.md ✅、research.md ✅、data-model.md ✅、contracts/ ✅、quickstart.md ✅

**服務層現況**：`LotteryTicketServiceImpl` 核心邏輯已大致實作完成。  
**主要交付項目**：兩個新的 Controller 端點、兩個 Response DTO、SCRATCH_PLAYER 指定閘門強化、樂觀鎖驗證、單元測試。

## 格式：`[ID] [P?] [Story?] 描述（含完整檔案路徑）`

- **[P]**：可平行執行（不同檔案，無相依未完成任務）
- **[US1/US2/US3/US4]**：對應 spec.md 中的使用者故事編號
- 每個任務包含精確的檔案路徑

---

## 第 1 階段：設置（環境驗證）

**目的**：確認資料庫結構與索引符合 data-model.md 規範，避免後續實作因缺少欄位或索引而中斷。

- [ ] T001 驗證 `lottery_ticket` 資料表欄位包含 `ticket_number`、`revealed_number`、`is_designated_prize`、`designated_by`（執行 `SHOW COLUMNS FROM lottery_ticket`，對照 data-model.md）
- [ ] T002 [P] 驗證 `lottery_session` 資料表欄位包含 `player_designated_numbers`、`opener_draw_count`、`opener_total_cost`、`free_draw_triggered`（執行 `SHOW COLUMNS FROM lottery_session`，對照 data-model.md）
- [ ] T003 [P] 驗證並補建缺少的索引：`idx_lottery_status (lottery_id, status)`、`idx_lottery_revealed (lottery_id, revealed_number)`（執行 `SHOW INDEX FROM lottery_ticket`；若缺少則執行 data-model.md § 資料庫遷移說明中的 `ALTER TABLE` 語句）
- [ ] T004 [P] 驗證 `lottery_session` 的 `idx_session_lottery_status (lottery_id, status)` 索引是否存在（執行 `SHOW INDEX FROM lottery_session WHERE Key_name = 'idx_session_lottery_status'`；若缺少則新增）

**檢查點**：資料庫結構與索引驗證完成 — 可開始進行基礎階段

---

## 第 2 階段：基礎（共用 DTO — 阻斷前置條件）

**目的**：建立兩個新的 Response DTO 類別，所有使用者故事階段的 Controller 端點均依賴它們。

**⚠️ 重要**：此階段完成前不得實作任何使用者故事的 Controller 端點。

- [ ] T005 建立 `TicketListResponse.java`（含 `TicketView` 內部類別）於 `src/main/java/com/group/admin/res/lottery/TicketListResponse.java`：外層欄位 `lotteryId`、`gameMode`、`totalTickets`、`availableCount`、`drawnCount`、`List<TicketView> tickets`；`TicketView` 包含 `ticketNumber`、`status`，以及 DRAWN 專用欄位（`revealedNumber`、`prizeId`、`prizeLevel`、`prizeName`、`prizeImageUrl`、`isGrandPrize`、`drawnBy`、`drawnAt`）（參照 quickstart.md 步驟 1）
- [ ] T006 [P] 建立 `DesignationCheckResponse.java`（含 `GrandPrize` 內部類別）於 `src/main/java/com/group/admin/res/lottery/DesignationCheckResponse.java`：欄位 `required`、`gameMode`、`sessionId`、`isOpener`、`requiredDesignationCount`、`grandPrizes`、`availableRevealedNumbers`、`openerNickname`、`message`、`alreadyDesignated`（參照 quickstart.md 步驟 1 與 contracts/GET-lottery-id-designation-check.md）

**檢查點**：DTO 類別已建立 — 所有使用者故事的 Controller 端點現在可以開始實作

---

## 第 3 階段：使用者故事 1 — 一番賞 / 扭蛋隨機抽獎（優先級：P1）🎯 MVP

**目標**：為 RANDOM 模式抽獎活動提供票券列表 API，嚴格執行資訊隱藏（FR-005、FR-006、SC-001），並確保樂觀並發鎖正確防止重複抽獎（FR-012、SC-005）。

**獨立測試**：建立含 80 張籤的 RANDOM 模式抽獎活動；呼叫 `GET /api/lottery/draw/{lotteryId}/tickets`，確認所有 AVAILABLE 籤的回應 JSON 中不包含 `prizeId`、`prizeLevel`、`prizeName`、`prizeImageUrl`、`revealedNumber`、`isGrandPrize`；抽第 15 號籤後再次呼叫，確認籤 #15 的 DRAWN 回應包含完整獎品資訊；抽完全部 80 籤確認無重複且 LOTTERY_SOLD_OUT 正確回傳。

### 使用者故事 1 的實作

- [ ] T007 [US1] 在 `src/main/java/com/group/admin/service/LotteryTicketService.java` 介面中新增方法宣告：`TicketListResponse getTicketsForFrontend(String lotteryId)`（FR-005、FR-006；參照 quickstart.md 步驟 2）
- [ ] T008 [US1] 在 `src/main/java/com/group/admin/service/impl/LotteryTicketServiceImpl.java` 中實作 `getTicketsForFrontend(String lotteryId)`：對 AVAILABLE 票券只填入 `ticketNumber` 和 `status`（其餘欄位設為 null / 省略）；對 DRAWN 票券填入完整欄位（含 SCRATCH 模式的 `revealedNumber`）；依 `ticketNumber` 升冪排序；填入 `totalTickets`、`availableCount`、`drawnCount`（FR-005、FR-006、SC-001）
- [ ] T009 [P] [US1] 在 `src/main/java/com/group/admin/controller/api/LotteryDrawController.java` 中新增端點 `GET /{lotteryId}/tickets`：呼叫 `ticketService.getTicketsForFrontend(lotteryId)`，回傳 `ResponseEntity<TicketListResponse>`；加上 `@Operation(summary = "取得票券列表（資訊隱藏已強制執行）")` 注解（contracts/GET-lottery-id-tickets.md；quickstart.md 步驟 3）
- [ ] T010 [US1] 在 `src/main/java/com/group/admin/service/impl/LotteryTicketServiceImpl.java` 的 `draw()` 方法中驗證樂觀鎖：確認 `ticketMapper.updateStatusToDrawn(ticket.getId(), userId)` 後檢查受影響列數 == 1；若 == 0 則拋出 `LotteryException(LotteryErrorCode.TICKET_ALREADY_DRAWN)`（FR-012、SC-005；quickstart.md 步驟 5）
- [ ] T011 [US1] 確認 `src/main/resources/mapper/LotteryTicketMapper.xml` 中的 `updateStatusToDrawn` 使用 `WHERE id=#{id} AND status='AVAILABLE'`，確保樂觀並發正確（FR-012；quickstart.md 步驟 5）
- [ ] T012 [US1] 在 `src/test/java/com/group/admin/service/LotteryTicketServiceTest.java` 中新增 RANDOM 模式單元測試：(a) `GET /tickets` 回應中 AVAILABLE 籤無獎品資訊洩漏（SC-001）；(b) DRAWN 籤回傳正確獎品欄位；(c) 並發 `draw()` 的第二次呼叫拋出 `TICKET_ALREADY_DRAWN`；(d) 無 AVAILABLE 籤時回傳 `LOTTERY_SOLD_OUT`（對應 US1 AC-1、AC-2、AC-3）

**檢查點**：使用者故事 1 可獨立運作 — RANDOM 模式票券列表與抽獎功能完整且無資訊洩漏

---

## 第 4 階段：使用者故事 2 — 店家指定大獎位置的刮刮樂（SCRATCH_STORE）（優先級：P1）

**目標**：確認 SCRATCH_STORE 模式在 `getTicketsForFrontend()` 中正確隱藏 `revealedNumber`，且抽獎時依照店家預先指定的 `designatedPrizeNumbers` 正確揭示大獎。

**獨立測試**：建立含 60 張籤的 SCRATCH_STORE 抽獎活動，`designatedPrizeNumbers=[23,45]`；呼叫 `GET /tickets` 確認所有 AVAILABLE 籤不含 `revealedNumber`；抽取 `revealedNumber == 23` 的籤，確認回應 `isGrandPrize: true`，`revealedNumber: 23`；抽取 `revealedNumber` 非 23/45 的籤，確認為非大獎。

### 使用者故事 2 的實作

- [ ] T013 [US2] 確認 `src/main/java/com/group/admin/service/impl/LotteryTicketServiceImpl.java` 的 `getTicketsForFrontend()` 對 SCRATCH_STORE 的 AVAILABLE 籤正確省略 `revealedNumber`（即使資料庫中已存有值也不回傳），符合 data-model.md 前端可見性規則（FR-005、US2 AC-3）
- [ ] T014 [US2] 確認 `src/main/java/com/group/admin/service/impl/LotteryTicketServiceImpl.java` 的 `generateScratchTickets()` 中，SCRATCH_STORE 模式於建立時解析 `lottery.designatedPrizeNumbers`，並以 `is_designated_prize=1`、`designated_by='STORE'` 更新對應票券（FR-007；若邏輯已存在則僅驗證，不需修改）
- [ ] T015 [US2] 確認 `src/main/java/com/group/admin/service/impl/LotteryTicketServiceImpl.java` 的 `autoAssignNonGrandPrizes()` 在 SCRATCH_STORE 指定後，對所有 AVAILABLE 非大獎票券執行單次批次 UPDATE（FR-009、SC-004 < 2 秒；驗證 `LotteryTicketMapper.xml` 是否具備 `batchUpdatePrizes` 查詢）
- [ ] T016 [US2] 在 `src/test/java/com/group/admin/service/LotteryTicketServiceTest.java` 中新增 SCRATCH_STORE 單元測試：(a) AVAILABLE 籤的 `revealedNumber` 在 `GET /tickets` 回應中為 null；(b) 抽取 `revealedNumber == designatedPrizeNumbers[0]` 的籤回傳 `isGrandPrize: true`；(c) 抽取非大獎籤回傳 `isGrandPrize: false`（對應 US2 AC-1、AC-2、AC-3）

**檢查點**：使用者故事 1 與 2 均可獨立運作 — RANDOM 和 SCRATCH_STORE 模式均完整且無資訊洩漏

---

## 第 5 階段：使用者故事 3 — 玩家指定大獎位置的刮刮樂（SCRATCH_PLAYER）（優先級：P2）

**目標**：完成 SCRATCH_PLAYER 指定閘門（FR-008）：第一位抽獎玩家成為開套玩家並收到 HTTP 202，非開套玩家在指定完成前收到 HTTP 423，開套玩家透過 `POST /designate` 完成指定後後續抽獎正常進行；提供 `GET /designation-check` 端點供前端輪詢狀態。

**獨立測試**：建立 SCRATCH_PLAYER 抽獎活動；玩家 A 第一次抽獎，確認回傳 202 `requiresDesignation: true`；玩家 B 嘗試抽獎，確認回傳 423 `DESIGNATION_PENDING`；玩家 A 呼叫 `GET /designation-check` 確認 `isOpener: true`；玩家 B 呼叫 `GET /designation-check` 確認 `isOpener: false` + 等待訊息；玩家 A 呼叫 `POST /designate` 指定大獎位置；確認後續抽獎正常；確認非開套玩家呼叫 `POST /designate` 回傳 403。

### 使用者故事 3 的實作

- [ ] T017 [US3] 在 `src/main/java/com/group/admin/service/LotteryTicketService.java` 介面中新增方法宣告：`DesignationCheckResponse getDesignationStatus(String lotteryId, String userId)`（參照 quickstart.md 步驟 2）
- [ ] T018 [US3] 在 `src/main/java/com/group/admin/service/impl/LotteryTicketServiceImpl.java` 中實作 `getDesignationStatus(String lotteryId, String userId)`，對應 contracts/GET-lottery-id-designation-check.md 的四種回應情境：(1) 非 SCRATCH_PLAYER 模式 → `required: false`；(2) 無 ACTIVE Session 或已指定完成 → `required: false`，`alreadyDesignated`；(3) 開套玩家且尚未指定 → `required: true`，`isOpener: true`，填入 `grandPrizes` 與 `availableRevealedNumbers`；(4) 非開套玩家且尚未指定 → `required: true`，`isOpener: false`，填入 `openerNickname` 與 `message`
- [ ] T019 [P] [US3] 在 `src/main/java/com/group/admin/controller/api/LotteryDrawController.java` 中新增端點 `GET /{lotteryId}/designation-check`：以 `@AuthenticationPrincipal` 取得 `userId`，呼叫 `ticketService.getDesignationStatus(lotteryId, userId)`，回傳 `ResponseEntity<DesignationCheckResponse>`；加上 `@Operation(summary = "查詢 SCRATCH_PLAYER 指定狀態")` 注解（contracts/GET-lottery-id-designation-check.md；quickstart.md 步驟 3）
- [ ] T020 [US3] 在 `src/main/java/com/group/admin/service/impl/LotteryTicketServiceImpl.java` 的 `draw()` 中強化 SCRATCH_PLAYER 指定閘門：取得/建立 Session 後，若 `gameMode == SCRATCH_PLAYER` 且 `session.playerDesignatedNumbers == null`：呼叫者為開套玩家 → 回傳 `DrawResult.designationRequired(sessionId, grandPrizeCount, availableRevealedNums)`（HTTP 202）；呼叫者非開套玩家 → 拋出 `LotteryException(LotteryErrorCode.DESIGNATION_PENDING)`（HTTP 423）（FR-008；quickstart.md 步驟 4）
- [ ] T021 [US3] 確認 `src/main/java/com/group/admin/service/impl/LotteryTicketServiceImpl.java` 的 `getOrCreateSession()` 使用 `ConcurrentHashMap<String, Object> sessionLocks`（依 lotteryId）的 `synchronized` 區塊，並在鎖內進行雙重鎖定檢查（re-check active session）以防止兩位玩家同時成為開套玩家（data-model.md § 開套玩家並發鎖；research.md § 3）
- [ ] T022 [US3] 確認 `src/main/java/com/group/admin/controller/api/LotteryDrawController.java` 的 `POST /{lotteryId}/designate` 端點呼叫 `ticketService.designatePrizePositions()`，並在 Service 層驗證：(a) gameMode == SCRATCH_PLAYER；(b) 存在 ACTIVE Session；(c) 呼叫者為 openerUserId（否則 403 NOT_OPENER）；(d) `playerDesignatedNumbers IS NULL`（否則 400 ALREADY_DESIGNATED）；(e) `designations.size()` 等於預期大獎數（否則 400 WRONG_DESIGNATION_COUNT）（contracts/POST-lottery-id-designate.md；FR-013）
- [ ] T023 [US3] 在 `src/test/java/com/group/admin/service/LotteryTicketServiceTest.java` 中新增 SCRATCH_PLAYER 單元測試：(a) 第一次抽獎回傳 `requiresDesignation: true`（202）；(b) 非開套玩家在指定前抽獎拋出 `DESIGNATION_PENDING`（423）；(c) 指定完成後後續抽獎正常回傳獎品；(d) 非開套玩家呼叫 `designatePrizePositions()` 拋出 `NOT_OPENER`（403）（對應 US3 AC-1、AC-2、AC-3）

**檢查點**：使用者故事 3 可獨立運作 — SCRATCH_PLAYER 指定閘門完整，`GET /designation-check` 四種情境均正確回應

---

## 第 6 階段：使用者故事 4 — 開套玩家免費抽獎（退款）機制（優先級：P3）

**目標**：驗證並強化 `checkAndTriggerFreeDraw()` 的五個觸發條件，確保退款只在開套玩家、保護抽數內、抽到大獎時觸發一次（FR-011、SC-003），且 `freeDrawTriggered` 標誌防止雙重退款。

**獨立測試**：建立 `freeDrawEnabled=true`、`protectionDraws=5` 的抽獎活動；開套玩家在第 3 抽中大獎，確認 `triggeredFreeDraw: true`，`refundAmount = 3 × pricePerDraw`；再次查詢 Session 確認 `freeDrawTriggered=1`；建立第二個回合，開套玩家抽 5 次無大獎，確認無退款；以非開套玩家身分中大獎，確認無退款。

### 使用者故事 4 的實作

- [ ] T024 [US4] 驗證 `src/main/java/com/group/admin/service/impl/LotteryTicketServiceImpl.java` 的 `checkAndTriggerFreeDraw()` 實作全部五個條件（data-model.md § 免費抽獎觸發條件）：(1) `session.freeDrawEnabled == 1`；(2) `session.freeDrawTriggered == 0`；(3) `session.openerDrawCount <= session.protectionDraws`；(4) `prize.isGrandPrize == 1`；(5) `callerId == session.openerUserId`；觸發時執行 `walletService.addGold(openerUserId, session.openerTotalCost)` 並設 `freeDrawTriggered=1`
- [ ] T025 [US4] 確認 `src/main/java/com/group/admin/service/impl/LotteryTicketServiceImpl.java` 的 `draw()` 在開套玩家每次抽獎後正確遞增 `session.openerDrawCount` 並累加 `session.openerTotalCost += pricePerDraw`（research.md § 5；data-model.md LotterySession 欄位）
- [ ] T026 [US4] 確認 `checkAndTriggerFreeDraw()` 中 `freeDrawTriggered=1` 的更新以資料庫交易執行，防止高並發下雙重退款（research.md § 5 — 雙大獎邊界案例）
- [ ] T027 [US4] 在 `src/test/java/com/group/admin/service/LotteryTicketServiceTest.java` 中新增免費抽獎退款單元測試：(a) 開套玩家第 3 抽中大獎（protectionDraws=5）→ 退款 = 3 × pricePerDraw，`freeDrawTriggered=1`；(b) 開套玩家耗盡 protectionDraws 無大獎 → 無退款；(c) 非開套玩家中大獎 → 無退款；(d) `freeDrawEnabled=0` → 無退款；(e) 雙大獎：僅第一個大獎觸發退款（對應 US4 AC-1、AC-2、AC-3）

**檢查點**：所有四個使用者故事均可獨立運作 — 完整的抽獎票券系統功能完成

---

## 最終階段：整合驗收與收尾

**目的**：跨使用者故事的品質驗證、OpenAPI 文件確認、quickstart.md 手動冒煙測試。

- [ ] T028 [P] 確認 `src/main/java/com/group/admin/controller/api/LotteryDrawController.java` 中所有新端點（`GET /tickets`、`GET /designation-check`）均具有完整的 `@Operation` 注解，包含 summary 與 HTTP 回應碼描述（Springdoc OpenAPI 2.3.0）
- [ ] T029 執行完整單元測試套件驗證所有使用者故事：`mvn test -pl . -Dtest=LotteryTicketServiceTest`，確認全部通過（quickstart.md § 測試）
- [ ] T030 [P] 依 quickstart.md 手動冒煙測試 RANDOM 模式：建立含 5 張籤的抽獎活動，呼叫 `GET /tickets` 確認無 prizeId（SC-001），抽第 3 號籤，再次 `GET /tickets` 確認籤 #3 現在顯示完整獎品資訊
- [ ] T031 [P] 依 quickstart.md 手動冒煙測試 SCRATCH_PLAYER 指定流程：玩家 A 抽獎取得 202、呼叫 `GET /designation-check`、呼叫 `POST /designate`、確認後續抽獎正常
- [ ] T032 [P] 確認 `src/main/java/com/group/admin/controller/api/LotteryDrawController.java` 中 `POST /{lotteryId}/draw` 對 SCRATCH_PLAYER 指定需求回傳正確的 HTTP 202 狀態碼（而非 200）（contracts/POST-lottery-id-draw.md § 回應 202）

---

## 相依關係與執行順序

### 階段相依關係

- **第 1 階段（設置）**：無相依 — 可立即開始；T001-T004 可並行
- **第 2 階段（基礎）**：依賴第 1 階段完成 — **阻斷所有使用者故事**；T005-T006 可並行
- **第 3-6 階段（使用者故事）**：均依賴第 2 階段完成
  - US1（第 3 階段）與 US2（第 4 階段）可並行（均為 P1，無互相依賴）
  - US3（第 5 階段）依賴 US1 的 `getTicketsForFrontend()` 介面已定義
  - US4（第 6 階段）依賴 US3 的 Session 機制（`openerDrawCount`、`openerTotalCost`）
- **最終階段**：依賴 US1-US4 全部完成

### 使用者故事相依關係

```
第1階段（設置）
    └── 第2階段（基礎 DTO）
            ├── US1（P1）─── 可立即開始
            │     └── 定義 getTicketsForFrontend() 介面
            ├── US2（P1）─── 可與 US1 並行
            └── US3（P2）─── 使用 US1 的介面（可在 US1 DTO 完成後開始）
                  └── US4（P3）─── 依賴 US3 的 Session 機制
```

### 各使用者故事內部順序

1. Service 介面方法（`LotteryTicketService.java`）
2. Service 實作（`LotteryTicketServiceImpl.java`）
3. Controller 端點（`LotteryDrawController.java`）
4. 單元測試（`LotteryTicketServiceTest.java`）

### 並行機會

- T001-T004（設置）：全部可並行
- T005-T006（基礎 DTO）：可並行
- T009 與 T010（US1 Controller 端點）：可並行（不同方法）
- US1（T007-T012）與 US2（T013-T016）：可並行（P1 同優先級）
- T019（Controller 端點）與 T020-T022（Service 邏輯）：部分可並行（US3 內）
- T028、T030-T032（收尾驗收）：可並行

---

## 平行執行範例

### 使用者故事 1 的平行範例

```bash
# 同時建立兩個 DTO（第 2 階段）：
Task: "建立 TicketListResponse.java 於 src/main/java/com/group/admin/res/lottery/"
Task: "建立 DesignationCheckResponse.java 於 src/main/java/com/group/admin/res/lottery/"

# US1 + US2 並行（均為 P1）：
Task: "US1 — 實作 getTicketsForFrontend() 與 GET /tickets 端點"
Task: "US2 — 驗證 SCRATCH_STORE 資訊隱藏與 autoAssignNonGrandPrizes()"
```

### 使用者故事 3 的平行範例

```bash
# US3 內的並行（T019 Controller 可與 T020-T021 Service 並行）：
Task: "新增 GET /{lotteryId}/designation-check 端點至 LotteryDrawController.java"
Task: "驗證 sessionLocks ConcurrentHashMap 並發控制 in LotteryTicketServiceImpl.java"
```

---

## 實作策略

### MVP 優先（僅使用者故事 1）

1. 完成第 1 階段：設置（T001-T004）
2. 完成第 2 階段：基礎 DTO（T005-T006）
3. 完成第 3 階段：使用者故事 1（T007-T012）
4. **停下並驗證**：RANDOM 模式票券列表與抽獎完整可用，無資訊洩漏
5. 可交付 / 展示

### 漸進式交付

1. 完成設置 + 基礎 → 基礎就緒
2. 加入 US1（RANDOM 模式 + `GET /tickets`）→ 獨立測試 → 交付（MVP！）
3. 加入 US2（SCRATCH_STORE 驗證）→ 獨立測試 → 交付
4. 加入 US3（SCRATCH_PLAYER 指定閘門 + `GET /designation-check`）→ 獨立測試 → 交付
5. 加入 US4（免費抽獎退款驗證）→ 獨立測試 → 交付
6. 每個故事增加價值，不破壞前一個故事

### 平行團隊策略

有多位開發者時：

1. 團隊一起完成設置 + 基礎
2. 基礎完成後：
   - 開發者 A：US1（RANDOM 模式票券列表）
   - 開發者 B：US2（SCRATCH_STORE 驗證）
   - 開發者 C：US3（SCRATCH_PLAYER 指定閘門）+ US4（免費抽獎退款）

---

## 備註

- `[P]` 任務 = 不同檔案，無相依關係，可同時執行
- `[USn]` 標籤 = 追蹤任務與 spec.md 使用者故事的對應關係
- 每個使用者故事應可獨立完成並獨立測試
- 提交：每個任務或邏輯群組完成後提交
- 在每個檢查點停下，獨立驗證該使用者故事
- **服務層已大致完成**：多數任務為驗證/強化現有邏輯，而非從零建構
- **主要新增項目**：T005、T006（DTO）、T009（Controller GET /tickets）、T019（Controller GET /designation-check）、T020（指定閘門）、T010（樂觀鎖驗證）
- 避免：模糊任務、相同檔案衝突、破壞故事獨立性的跨故事相依關係
