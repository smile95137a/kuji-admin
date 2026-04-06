# Tasks: 刮刮樂獎項規則修正與編號指定流程

**輸入文件**：`specs/015-scratch-lottery-prize-rules/`  
**分支**：`015-scratch-lottery-prize-rules`

## 格式說明：`[ID] [P?] [Story?] 描述`

- **[P]**：可並行執行（不同檔案、無相互依賴）
- **[Story]**：對應的 User Story（US1、US2、US3、US4）
- 每個任務包含完整的檔案路徑

---

## Phase 1：基礎設施（DB 遷移 + MBG 重新產生）

**目的**：為後續所有實作提供必要的資料庫欄位，必須最先完成。

**⚠️ 重要**：Phase 1 完成前，所有 User Story 的實作均無法啟動

- [X] T001 建立 DB 遷移腳本 `sql/add-designation-deadline.sql`，新增 `designation_deadline DATETIME NULL` 欄位到 `lottery_session` 表
- [ ] T002 在 MySQL 執行 `sql/add-designation-deadline.sql` 遷移腳本（確認執行成功）
- [X] T003 手動更新 `LotterySession.java` entity + `LotterySessionMapper.xml` 加入 `designation_deadline` 欄位映射（所有 CRUD 語句）

**Checkpoint**：DB 欄位已新增，MBG 已重新產生 → 可開始後續實作

---

## Phase 2：基礎 DTO 擴充（阻擋前置）

**目的**：擴充 `SessionInfo` record，讓後續所有使用 SessionInfo 的地方都能傳遞 `designationDeadline`。

**⚠️ 重要**：Phase 2 完成前，US2 的 Session 逾時邏輯與 US3 的攔截邏輯均無法實作

- [X] T004 在 `src/main/java/com/group/admin/service/LotteryTicketService.java` 的 `SessionInfo` record 尾端新增 `LocalDateTime designationDeadline` 欄位（共 11 個欄位）
- [X] T005 在 `src/main/java/com/group/admin/service/impl/LotteryTicketServiceImpl.java` 更新「找到現有場次」分支的 `new SessionInfo(...)` 建構呼叫（新增 `activeSession.getDesignationDeadline()` 作為第 11 個參數）
- [X] T006 在 `src/main/java/com/group/admin/service/impl/LotteryTicketServiceImpl.java` 更新「建立新場次」分支的 `new SessionInfo(...)` 建構呼叫（新建場次 `designationDeadline` 暫填 null，後在 T009 中補充設定邏輯）

**Checkpoint**：`SessionInfo` 擴充完成，專案可以順利編譯 → 可開始 US1、US2、US3 的實作

---

## Phase 3：User Story 1 — 後台刮刮樂商品獎品規則強制執行（P1）🎯 MVP

**目標**：刮刮樂商品新增/更新獎品時，系統強制只允許 1 個大獎（`isGrandPrize=true`，`quantity=1`），不允許設定非大獎獎品，違反時拋出明確的錯誤訊息。

**獨立驗證**：以 POST `/admin/lottery-with-prizes` 新增一個 `gameMode=SCRATCH_PLAYER` 的商品，設定 2 個 `isGrandPrize=true` 的獎品，確認 API 回傳錯誤「刮刮樂模式：大獎數量必須剛好為 1」。再設定 1 個大獎 + 1 個非大獎獎品，也應被拒絕。最後設定 1 個大獎，商品成功建立。

### US1 實作

- [X] T007 [US1] 在 `src/main/java/com/group/admin/service/impl/LotteryServiceImpl.java` 新增 `private void validateScratchPrizes(String gameMode, List<?> prizes)` 輔助方法，驗證：(1) 大獎總數量（`isGrandPrize=true` 的 `quantity` 加總）= 1，(2) 非大獎獎品數量 = 0，違反時拋出 `BusinessException`
- [X] T008 [US1] 在 `LotteryServiceImpl.java` 的 `createLotteryWithPrizes()` 方法中，於儲存獎品之前呼叫 `validateScratchPrizes(req.getGameMode(), req.getPrizes())`
- [X] T009 [US1] 在 `LotteryServiceImpl.java` 的獎品更新流程（`updateLotteryWithPrizes()` 或對應方法）中，同樣呼叫 `validateScratchPrizes(gameMode, updatedPrizes)`，防止更新時繞過驗證

**Checkpoint**：US1 完成 — 刮刮樂商品的獎品規則完整執行，設定非法獎品結構時系統正確拒絕

---

## Phase 4：User Story 2 — 前台開套玩家指定大獎（SCRATCH_PLAYER）（P1）

**目標**：SCRATCH_PLAYER 模式下，(a) 開套玩家必須先指定大獎才能抽獎，(b) 非開套玩家在等待期間被阻擋並看到倒數計時，(c) 開套者有 10 分鐘超時機制，逾時自動釋放場次讓下一位玩家接手。

**獨立驗證**：
1. 以 POST `/api/lottery/draw/{id}/draw` 嘗試抽獎（開套者身份），確認回傳 `designationRequired: true`
2. 以第二位玩家嘗試抽獎，確認回傳 `awaitingDesignation: true` 與 `openerDeadline`
3. 模擬超時（修改 DB 中 `designation_deadline` 為過去時間），再次進入確認新玩家成為開套者

### US2 實作

- [X] T010 [US2] 在 `src/main/java/com/group/admin/service/impl/LotteryTicketServiceImpl.java` 的 `getOrCreateSession()` 方法中，**建立新場次**時：若 `lottery.getGameMode()` 為 `SCRATCH_PLAYER`，設定 `newSession.setDesignationDeadline(LocalDateTime.now().plusMinutes(10))`；否則設定為 null
- [X] T011 [US2] 在 `getOrCreateSession()` 的**找到現有 ACTIVE 場次**分支中，新增逾時判斷：若 `gameMode=SCRATCH_PLAYER` 且 `session.playerDesignatedNumbers` 為 null 且 `designationDeadline` 已過期 → 將現有場次標記為 EXPIRED，以當前 userId 建立新場次（`designationDeadline = now + 10分鐘`），並回傳新場次（`isOpener=true`）
- [X] T012 [P] [US2] 在 `src/main/java/com/group/admin/controller/api/LotteryDrawController.java` 新增內部 DTO `DesignationPendingResponse(boolean awaitingDesignation, String message, String openerDeadline)`
- [X] T013 [US2] 在 `LotteryDrawController.draw()` 中，現有 `if (session.isOpener())` 指定大獎攔截邏輯之後，新增非開套玩家攔截：若 `!session.isOpener()` AND `SCRATCH_PLAYER.equals(lottery.getGameMode())` AND `session.playerDesignatedNumbers()` 為 null/空白 → 取得 `session.designationDeadline()` 並回傳 `DesignationPendingResponse`

**Checkpoint**：US2 完成 — 開套玩家必看指定畫面、非開套玩家看到倒數、10 分鐘後自動輪換

---

## Phase 5：User Story 3 — 店家指定模式上架前置驗證（SCRATCH_STORE）（P2）

**目標**：`SCRATCH_STORE` 商品在 `designatedPrizeNumbers` 未設定時，系統阻擋上架操作。`SCRATCH_PLAYER` 商品不受此限制，可在指定完成前上架。

**獨立驗證**：以 PATCH `/admin/lottery/{id}/status` 嘗試將一個 `gameMode=SCRATCH_STORE`、`designatedPrizeNumbers=null` 的商品設為 `ON_SHELF`，確認回傳錯誤「SCRATCH_STORE：請先指定大獎號碼才能上架」。設定 `designatedPrizeNumbers` 後再試，確認成功上架。

### US3 實作

- [X] T014 [US3] 在 `src/main/java/com/group/admin/service/impl/LotteryServiceImpl.java` 新增 `private void validateCanGoOnShelf(Lottery lottery)` 輔助方法：若 `gameMode=SCRATCH_STORE` 且 `designatedPrizeNumbers` 為 null 或空白，拋出 `BusinessException("SCRATCH_STORE 模式：請先在後台指定大獎號碼才能上架")`
- [X] T015 [US3] 在 `LotteryServiceImpl.updateStatus()` 方法中，當 `status.equals("ON_SHELF")` 時，在現有籤位生成邏輯之前呼叫 `validateCanGoOnShelf(lottery)`
- [X] T016 [US3] 在 `LotteryServiceImpl.changeStatus()` 方法中，當 `targetStatus.equals("ON_SHELF")` 時，在現有籤位生成邏輯之前呼叫 `validateCanGoOnShelf(lottery)`

**Checkpoint**：US3 完成 — SCRATCH_STORE 上架有守門，SCRATCH_PLAYER 上架不受影響

---

## Phase 6：User Story 4 — 籤號分配時機驗證（P2）

**目標**：確認各種商品模式的籤號分配時機符合規格：一番賞/扭蛋立即分配、SCRATCH_STORE 等待店家指定、SCRATCH_PLAYER 等待開套玩家指定。

**獨立驗證**：分別建立 OFFICIAL_ICHIBAN、GACHA、SCRATCH_STORE（含 designatedPrizeNumbers）、SCRATCH_PLAYER 四種商品並觸發上架，查詢 `lottery_ticket` 表確認各商品的籤位狀態符合預期。

### US4 實作

- [ ] T017 [US4] 檢查 `LotteryTicketServiceImpl.generateScratchTickets()` 的 SCRATCH_PLAYER 分支：確認在 `winningRevealedNumbers` 為空時，所有籤位 `prizeId=null`、`prizeLevel="THANKS"`、`isDesignatedPrize=0`（代表「待指定」狀態），若有偏差則修正
- [ ] T018 [US4] 檢查 `LotteryTicketServiceImpl.generateScratchTickets()` 的 SCRATCH_STORE 分支：確認 `autoAssignNonGrandPrizes()` 在刮刮樂（只有 1 個大獎、無非大獎獎品）情境下，正確地讓所有非大獎籤位維持 `prizeId=null`（謝謝惠顧），若有偏差則修正
- [ ] T019 [P] [US4] 確認 `LotteryServiceImpl` 中 `OFFICIAL_ICHIBAN` 和 `GACHA` 類型商品的上架流程，籤位仍然立即分配（調用 `generateRandomTickets()`），未受本次改動影響

**Checkpoint**：US4 完成 — 四種模式籤號分配時機全部正確

---

## Phase 7：收尾與建構驗證

**目的**：確認所有修改不破壞現有功能，專案可以正常建構。

- [X] T020 [P] 執行 `mvn clean package -DskipTests`，確認 `BUILD SUCCESS`，0 個編譯錯誤
- [ ] T021 [P] 檢查 `LotteryDrawController.java` 的 `SessionResponse.from()` 方法，確認 `designationDeadline` 有被正確傳入（若前台 Session 狀態 API 有回傳 `SessionResponse`，需包含 `designationDeadline` 供前端顯示倒數）
- [ ] T022 人工測試：以 Postman 或 curl 測試 SCRATCH_PLAYER 完整流程：建立商品 → 上架 → 第一位玩家嘗試抽獎（確認回傳指定要求）→ 指定大獎號碼 → 再次抽獎（確認成功）

---

## 依賴關係與執行順序

### 階段依賴

```
Phase 1（DB 遷移）
    └── Phase 2（SessionInfo 擴充）
            ├── Phase 3（US1 獎品驗證）      ← 可與 US2、US3 並行
            ├── Phase 4（US2 指定流程）      ← 依賴 Phase 2
            ├── Phase 5（US3 上架驗證）      ← 可與 US2 並行
            └── Phase 6（US4 籤號分配確認） ← 可與 US1、US3 並行
                        └── Phase 7（建構驗證）
```

### User Story 依賴

| Story | 前置條件 | 可與哪些並行 |
|-------|---------|------------|
| US1 (P1) | Phase 2 完成 | US3、US4 |
| US2 (P1) | Phase 2 完成 | US1、US3 |
| US3 (P2) | Phase 2 完成 | US1、US4 |
| US4 (P2) | Phase 2 完成 | US1、US3 |

---

## 並行執行範例

**Phase 3-6 任務可以同時啟動（Phase 2 完成後）：**

```
同時執行：
- T007-T009（US1：獎品驗證）
- T010-T013（US2：Session 逾時 + 非開套者攔截）
- T014-T016（US3：SCRATCH_STORE 上架驗證）
- T017-T019（US4：籤號分配確認）
```

---

## 實作策略

### MVP 優先（US1 + US2）

1. 完成 Phase 1：DB 遷移 + MBG
2. 完成 Phase 2：SessionInfo 擴充
3. 完成 Phase 3：US1 獎品驗證
4. 完成 Phase 4：US2 指定流程
5. **停下來驗證**：測試 US1 + US2 的獨立流程
6. 可先上線這兩個修正

### 完整交付

1. 完成 Phase 1-2（基礎）
2. 並行完成 Phase 3-6（各 Story）
3. 完成 Phase 7（建構驗證）
4. 部署

---

## 備註

- `[P]` 任務 = 不同檔案，無相互依賴，可並行
- `[Story]` 標籤對應 spec.md 中的 User Story
- 每個 User Story 可獨立實作與測試
- 每完成一個 Task 後建議 commit 一次
- 任何 Checkpoint 都可以暫停並獨立驗證該 Story 的功能
