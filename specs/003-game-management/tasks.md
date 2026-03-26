# 任務清單：遊戲管理（抽獎機制）

**輸入**：設計文件來自 `/specs/003-game-management/`  
**參考文件**：plan.md、spec.md、data-model.md、research.md、contracts/、quickstart.md  
**分支**：`003-game-management` | **建立日期**：2026-03-22

**格式說明**：
- **[P]**：可平行執行（不同檔案，無未完成的前置依賴）
- **[USn]**：任務所屬使用者故事
- 任務描述中包含精確檔案路徑

---

## 第一階段：設置（現有實體驗證）

**目的**：確認所有既有實體的欄位與 data-model.md 規範一致，確保後續實作不因欄位缺失而失敗。

- [ ] T001 [P] 核查 `src/main/java/com/group/admin/entity/Lottery.java` 包含所有必要欄位：`protectionMinutes`、`autoDiscountEnabled`、`discountedPrice`、`totalDraws`、`allowMultiDraw`、`multiDrawOptions`，如缺少則補充對應欄位與 MyBatis `@Column` 對映
- [ ] T002 [P] 核查 `src/main/java/com/group/admin/entity/LotteryPrize.java` 包含所有必要欄位：`weight`、`isLastPrize`、`isGrandPrize`、`remaining`、`level`、`prizeType`，如缺少則補充對應欄位
- [ ] T003 [P] 核查 `src/main/java/com/group/admin/entity/LotteryLock.java` 包含所有必要欄位：`lotteryId`、`userId`、`lockStartTime`、`lockEndTime`、`isActive`，如缺少則補充對應欄位
- [ ] T004 [P] 核查 `src/main/java/com/group/admin/entity/LotteryDrawRecord.java` 包含所有必要欄位：`costType`、`costAmount`、`status`（SUCCESS/PENDING/FAILED）、`sessionId`、`isOpenerDraw`，如缺少則補充對應欄位

**檢查點**：四個既有實體欄位已與 data-model.md 完全對齊

---

## 第二階段：基礎架構（阻斷性前置條件）

**目的**：建立所有使用者故事實作前必須就緒的共用基礎設施——Mapper SQL 查詢、DTO、Service 介面簽名。

**⚠️ 重要**：此階段完成前，任何使用者故事均不可開始實作。

- [ ] T005 [P] 在 `src/main/resources/mapper/LotteryPrizeMapper.xml` 新增四項 SQL 查詢：`selectByPrimaryKeyForUpdate`（SELECT...FOR UPDATE）、`countGrandPrizesWithStock`（統計 is_grand_prize=1 且 remaining>0 的數量）、`selectLastPrize`（取得 is_last_prize=1 且 remaining>0）、`selectAvailablePrizes`（取得該抽獎活動所有 remaining>0 的獎品，按 order_num 排序）
- [ ] T006 [P] 在 `src/main/resources/mapper/LotteryLockMapper.xml` 新增兩項 SQL 查詢：`selectActiveLock`（取得 lottery_id 對應的有效鎖定，條件：is_active=1 且 lock_end_time > NOW()）、`expireStaleLocksBeforeTime`（批次將 lock_end_time < #{now} 的鎖定設為 is_active=0）
- [ ] T007 [P] 在 `src/main/resources/mapper/LotteryDrawRecordMapper.xml` 新增 `selectByLotteryIdPaged`（分頁查詢，LEFT JOIN lottery_prize 取得 prize_name、prize_level，支援 userId、status、startDate、endDate 動態條件，ORDER BY created_at DESC，LIMIT/OFFSET 分頁）及 `countByLotteryIdFiltered`（計算符合條件的總筆數，供分頁使用）
- [ ] T008 [P] 在 `src/main/java/com/group/admin/mapper/LotteryPrizeMapper.java` 補充對應的 Java 介面方法宣告：`selectByPrimaryKeyForUpdate`、`countGrandPrizesWithStock`、`selectLastPrize`、`selectAvailablePrizes`
- [ ] T009 [P] 在 `src/main/java/com/group/admin/mapper/LotteryLockMapper.java` 補充對應的 Java 介面方法宣告：`selectActiveLock`、`expireStaleLocksBeforeTime`
- [ ] T010 [P] 在 `src/main/java/com/group/admin/mapper/LotteryDrawRecordMapper.java` 補充對應的 Java 介面方法宣告：`selectByLotteryIdPaged`、`countByLotteryIdFiltered`
- [ ] T011 [P] 建立 `src/main/java/com/group/admin/req/DrawReq.java`（欄位：`count` Integer，預設值 1，驗證：@Min(1) @Max(10)，Lombok @Data @Builder）
- [ ] T012 [P] 建立 `src/main/java/com/group/admin/req/AdminDrawHistoryReq.java`（欄位：`page` Integer=1、`size` Integer=20、`userId` String、`status` String、`startDate` LocalDateTime、`endDate` LocalDateTime，Lombok @Data @Builder）
- [ ] T013 [P] 建立 `src/main/java/com/group/admin/res/LockStatusRes.java`（欄位：`canDraw` Boolean、`isLockedByMe` Boolean、`lockedByOther` Boolean、`remainingSeconds` Long、`lockExpiresAt` LocalDateTime、`protectionMinutes` Integer，Lombok @Data @Builder）
- [ ] T014 [P] 強化 `src/main/java/com/group/admin/res/DrawResultRes.java`（確保包含 draws 陣列元素欄位：`drawRecordId`、`lotteryId`、`prizeId`、`prizeName`、`prizeLevel`、`prizeImageUrl`、`prizeType`、`isLastPrize`、`costType`、`costAmount`、`createdAt`；以及頂層欄位：`lotteryRemaining`、`lockAcquired`、`lockExpiresAt`、`priceMayHaveChanged`、`newPricePerDraw`）
- [ ] T015 強化 `src/main/java/com/group/admin/service/DrawService.java` 介面：新增方法簽名 `DrawResultRes executeDraw(String lotteryId, String userId, DrawReq req)`，Javadoc 說明原子性與並發控制要求
- [ ] T016 強化 `src/main/java/com/group/admin/service/LotteryLockService.java` 介面：確認包含 `acquireLock(String lotteryId, String userId)`、`checkLock(String lotteryId, String userId)` 回傳 `LockCheckResult`、`releaseLock(String lotteryId, String userId)`、`cleanExpiredLocks()` 四個方法簽名

**檢查點**：基礎架構就緒——所有使用者故事可開始獨立實作

---

## 第三階段：使用者故事 1 — 玩家從固定獎池抽獎（優先級：P1）🎯 MVP

**目標**：實作核心抽獎演算法——從固定獎池以加權隨機方式（等機率 1/N）抽獎、防止超額抽獎、記錄每次抽獎。

**獨立測試**：建立一個含已知獎品的抽獎活動，多次呼叫 `POST /api/lottery/{id}/draw`，確認：每次回傳有效獎品、`lotteryRemaining` 遞減、所有獎品耗盡後狀態變為 SOLD_OUT、第 N+1 次抽獎回傳 LOTTERY_SOLD_OUT（410）。

### 使用者故事 1 實作

- [ ] T017 [US1] 在 `src/main/java/com/group/admin/service/impl/DrawServiceImpl.java` 實作 `executeDrawInternal` 私有方法：使用 `selectAvailablePrizes` 取得有庫存的獎品清單、依 `weight` 進行加權隨機選獎、使用 `selectByPrimaryKeyForUpdate` 取得悲觀鎖、如 remaining ≤ 0 則拋出 `LOTTERY_SOLD_OUT` 例外並觸發回滾、遞減 `LotteryPrize.remaining`（UPDATE lottery_prize SET remaining = remaining - 1）、新增 `LotteryDrawRecord`（status=SUCCESS、costType、costAmount、createdAt）
- [ ] T018 [US1] 在 `src/main/java/com/group/admin/service/impl/DrawServiceImpl.java` 實作 `executeDraw` 公開方法（@Transactional）：驗證 Lottery 存在且 status == ON_SHELF（否則拋 LOTTERY_NOT_ON_SHELF 422）、驗證 count 合法（1 或多抽選項）、計算總點數成本 = pricePerDraw × count、驗證玩家錢包餘額（整合既有錢包服務）、以迴圈呼叫 `executeDrawInternal` count 次（整個方法在同一 @Transactional 邊界內）、最後一次抽獎後如 totalDraws 已耗盡則將 Lottery.status 更新為 SOLD_OUT
- [ ] T019 [US1] 在 `src/main/java/com/group/admin/controller/api/LotteryDrawController.java` 新增/強化 `POST /api/lottery/{id}/draw` 端點：接收 `DrawReq`、從 JWT 取得 userId、呼叫 `DrawService.executeDraw`、以 `ApiResponse<DrawResultRes>` 包裝回傳、加上適當的 @PreAuthorize

**檢查點**：使用者故事 1 可獨立測試——固定獎池抽獎、SOLD_OUT 保護、抽獎記錄全部正常運作

---

## 第四階段：使用者故事 2 — 玩家抽獎時的保護時間（優先級：P1）

**目標**：實作每個抽獎活動每次只有一位玩家持有保護鎖定，其他玩家在保護期間無法抽獎；鎖定到期後下一位玩家可接手。

**獨立測試**：玩家 A 抽獎（取得鎖定）→ 玩家 B 在 5 分鐘內嘗試抽獎 → B 收到 409 LOTTERY_LOCKED 含 `remainingSeconds` → 等待鎖定到期 → B 再次抽獎成功並取得新鎖定。

### 使用者故事 2 實作

- [ ] T020 [US2] 重寫 `src/main/java/com/group/admin/service/impl/LotteryLockServiceImpl.java`：`acquireLock` — 使用 `selectActiveLock` 先查詢是否已有有效鎖定，若無則建立新 LotteryLock（isActive=1、lockEndTime=now+protectionMinutes）；`checkLock` — 回傳 `LockCheckResult`（canDraw、isLockedByMe、lockedByOther、remainingSeconds、lockExpiresAt、protectionMinutes）；`releaseLock` — 將指定鎖定設為 isActive=0；`cleanExpiredLocks` — 呼叫 `expireStaleLocksBeforeTime(LocalDateTime.now())`
- [ ] T021 [US2] 在 `DrawServiceImpl.executeDraw` 中整合保護鎖定邏輯（此步驟修改 T018 實作的方法）：抽獎前呼叫 `LotteryLockService.checkLock`，若 lockedByOther=true 則拋出 LOTTERY_LOCKED（409）含 remainingSeconds；若無鎖定則呼叫 `acquireLock`；在 `DrawResultRes` 中填入 `lockAcquired`、`lockExpiresAt`
- [ ] T022 [US2] 新增 `src/main/java/com/group/admin/controller/api/LotteryLockController.java`：實作 `GET /api/lottery/{id}/lock-status` 端點，從 JWT 取得 userId，呼叫 `LotteryLockService.checkLock`，以 `ApiResponse<LockStatusRes>` 包裝回傳，若 lotteryId 不存在則回傳 404 LOTTERY_NOT_FOUND
- [ ] T023 [US2] 新增 `src/main/java/com/group/admin/scheduler/LockCleanupScheduler.java`：使用 @Component、@Scheduled(fixedDelay = 60000)（每 60 秒執行），呼叫 `LotteryLockService.cleanExpiredLocks()`，以 log.info 記錄清理結果

**檢查點**：使用者故事 1 + 2 均可獨立測試——保護鎖定拒絕其他玩家、持有者可繼續、到期後自動釋放

---

## 第五階段：使用者故事 3 — 最後賞機制（優先級：P2）

**目標**：當抽獎活動只剩 1 次時，若已指定最後賞，最後一抽直接給予該獎品（不走隨機演算法）。

**獨立測試**：建立含 is_last_prize=1 獎品的抽獎活動，抽到剩餘 1 次後，再抽一次，確認永遠回傳最後賞獎品（isLastPrize=true），並且 `lotteryRemaining` 歸零、Lottery 狀態變為 SOLD_OUT。

### 使用者故事 3 實作

- [ ] T024 [US3] 在 `src/main/java/com/group/admin/service/impl/DrawServiceImpl.java` 的 `executeDrawInternal` 中整合最後賞邏輯：在隨機選獎前，先取得當前 Lottery.totalDraws 剩餘數量；若剩餘 == 1，呼叫 `LotteryPrizeMapper.selectLastPrize(lotteryId)` 取得最後賞；若找到則跳過隨機演算法，直接使用該獎品（仍需 SELECT FOR UPDATE）；若未指定最後賞則繼續正常隨機流程；在 `DrawResultRes.draws[]` 中標記 `isLastPrize = true`

**檢查點**：使用者故事 3 可獨立測試——最後賞保證機制正常，一般抽獎不受影響

---

## 第六階段：使用者故事 4 — 大獎售罄後自動降價（優先級：P3）

**目標**：當抽獎活動中所有設定為 `isGrandPrize=1` 的獎品全部抽完後，立即在同一筆交易中將 `pricePerDraw` 更新為 `discountedPrice`，並記錄價格變動。

**獨立測試**：建立啟用自動降價（autoDiscountEnabled=1）且含 A 賞的抽獎活動，抽完最後一個 A 賞後，確認回應中 `priceMayHaveChanged=true`、`newPricePerDraw` 等於設定的折扣後價格，後續抽獎使用新價格。

### 使用者故事 4 實作

- [ ] T025 [US4] 在 `src/main/java/com/group/admin/service/impl/DrawServiceImpl.java` 的 `executeDrawInternal` 中整合自動降價邏輯：每次獎品庫存遞減後，呼叫 `LotteryPrizeMapper.countGrandPrizesWithStock(lotteryId)`；若結果 == 0 且 `lottery.autoDiscountEnabled == 1` 且 `lottery.discountedPrice > 0`，則在同一 @Transactional 中執行 `UPDATE lottery SET price_per_draw = discounted_price WHERE id = #{lotteryId}`；在 `DrawResultRes` 頂層欄位填入 `priceMayHaveChanged = true`、`newPricePerDraw = discountedPrice`
- [ ] T026 [US4] 在 `src/main/resources/mapper/LotteryMapper.xml`（或對應 Mapper）新增 `updatePriceAfterGrandPrizeSoldOut` SQL：`UPDATE lottery SET price_per_draw = discounted_price, updated_at = NOW() WHERE id = #{lotteryId}`，並在 `LotteryMapper.java` 介面新增對應方法宣告

**檢查點**：使用者故事 4 可獨立測試——大獎售罄時價格立即更新，停用時不觸發

---

## 第七階段：後台管理 — 抽獎歷史記錄查詢

**目標**：提供後台管理員與店家查詢特定抽獎活動的完整抽獎紀錄，含分頁、多條件篩選及匯總統計。

**獨立測試**：呼叫 `GET /admin/lottery/{id}/draws?page=1&size=20`，確認：分頁正確、records 陣列含 prizeName、prizeLevel、summary 包含正確 totalDraws、successDraws、totalRevenue；以 ROLE_STORE_OWNER 存取其他店家的抽獎活動時收到 403。

### 後台管理實作

- [ ] T027 [P] 新增 `src/main/java/com/group/admin/res/AdminDrawHistoryRes.java`：包含 `page`、`size`、`total`（long）、`totalPages`、`records`（List<DrawRecordItem>）、`summary`（DrawSummary）；內部靜態類別 `DrawRecordItem` 含 id、lotteryId、userId、prizeId、prizeName、prizeLevel、prizeImageUrl、isLastPrize、costType、costAmount、status、createdAt；內部靜態類別 `DrawSummary` 含 totalDraws、successDraws、failedDraws、totalRevenue、remainingDraws
- [ ] T028 新增 `src/main/java/com/group/admin/service/impl/AdminDrawHistoryServiceImpl.java`（或在既有 Service 中擴充）：實作 `getDrawHistory(String lotteryId, String callerId, String callerRole, AdminDrawHistoryReq req)`；驗證 Lottery 存在（否則拋 LOTTERY_NOT_FOUND 404）；ROLE_STORE_OWNER 需驗證 lottery.storeId == caller.storeId（否則拋 ACCESS_DENIED 403）；呼叫 `LotteryDrawRecordMapper.selectByLotteryIdPaged` 取得分頁資料，計算 totalPages；另行計算 summary（totalDraws、successDraws、failedDraws、totalRevenue = SUM(costAmount)、remainingDraws）
- [ ] T029 新增 `src/main/java/com/group/admin/controller/admin/AdminDrawHistoryController.java`：實作 `GET /admin/lottery/{id}/draws` 端點，接收 `AdminDrawHistoryReq` 查詢參數，從 JWT 取得 callerId 與 callerRole，呼叫 AdminDrawHistoryService，以 `ApiResponse<AdminDrawHistoryRes>` 包裝回傳，加上 @PreAuthorize("hasAnyRole('ADMIN','STORE_OWNER')")

**檢查點**：管理端查詢正常，ROLE_STORE_OWNER 越權存取被正確拒絕

---

## 最終階段：收尾與橫切關注點

**目的**：整合所有使用者故事後的品質強化、錯誤處理統一化與端到端驗證。

- [ ] T030 [P] 確認專案中已有錯誤碼常數（建議集中於 `src/main/java/com/group/admin/common/ErrorCode.java` 或既有常數類別）：新增 `LOTTERY_LOCKED`（409）、`LOTTERY_SOLD_OUT`（410）、`INSUFFICIENT_BALANCE`（402）、`LOTTERY_NOT_ON_SHELF`（422）、`DRAW_INVALID_COUNT`（400）、`LOTTERY_NOT_FOUND`（404）、`ACCESS_DENIED`（403）
- [ ] T031 [P] 確認 `DrawServiceImpl` 的所有例外情境均已加上適當的 log.warn / log.error 記錄（超額抽獎嘗試、並發鎖定衝突、交易回滾原因）
- [ ] T032 [P] 在 `src/main/resources/application.yml`（或 application.properties）確認 `@Scheduled` 已啟用（加上 `@EnableScheduling` 至主應用程式或 Scheduler 設定類別）
- [ ] T033 按照 `specs/003-game-management/quickstart.md` 執行端到端驗證：Workflow 1（單次抽獎流程）、Workflow 2（多抽 10 次）、Workflow 3（管理後台查詢歷史記錄）、Workflow 4（最後賞場景）

---

## 依賴關係與執行順序

### 階段依賴

```
第一階段（設置）     — 無依賴，立即開始
第二階段（基礎架構）  — 依賴第一階段完成 → 阻斷所有使用者故事
第三階段（US1）      — 依賴第二階段完成 → MVP 核心
第四階段（US2）      — 依賴第二、三階段（整合鎖定邏輯至 DrawServiceImpl）
第五階段（US3）      — 依賴第三階段（擴充 executeDrawInternal）
第六階段（US4）      — 依賴第三階段（擴充 executeDrawInternal）
第七階段（管理後台）  — 依賴第二階段（Mapper 就緒）可與 US3/US4 平行進行
最終階段（收尾）     — 依賴所有前置階段完成
```

### 使用者故事依賴

- **使用者故事 1（P1）**：依賴第二階段 → 核心 MVP，無其他故事依賴
- **使用者故事 2（P1）**：依賴使用者故事 1（需整合至 DrawServiceImpl）
- **使用者故事 3（P2）**：依賴使用者故事 1（擴充 executeDrawInternal），與 US2 無直接依賴
- **使用者故事 4（P3）**：依賴使用者故事 1（擴充 executeDrawInternal），與 US2/US3 無直接依賴
- **管理後台查詢**：依賴第二階段（Mapper），可與 US3/US4 平行進行

### 各故事內部執行順序

```
Mapper XML（T005–T007）→ Mapper Java 介面（T008–T010）→ DTO/Res（T011–T014）→ Service 介面（T015–T016）
  → DrawServiceImpl 核心（T017–T018）→ Controller（T019）           ← US1
    → LotteryLockServiceImpl（T020）→ 整合至 DrawService（T021）→ LockController（T022）→ Scheduler（T023）  ← US2
    → 最後賞邏輯整合（T024）                                         ← US3（可與 US2 平行）
    → 自動降價邏輯整合（T025–T026）                                  ← US4（可與 US2/US3 平行）
    → 管理後台（T027–T029）                                          ← 可與 US2/US3/US4 平行
  → 收尾（T030–T033）
```

---

## 平行執行範例

### 第一階段：可完全平行

```
同時啟動：
  任務：核查 Lottery.java 欄位（T001）
  任務：核查 LotteryPrize.java 欄位（T002）
  任務：核查 LotteryLock.java 欄位（T003）
  任務：核查 LotteryDrawRecord.java 欄位（T004）
```

### 第二階段：Mapper XML 與 DTO 可平行

```
批次一（平行）：
  任務：LotteryPrizeMapper.xml 新增查詢（T005）
  任務：LotteryLockMapper.xml 新增查詢（T006）
  任務：LotteryDrawRecordMapper.xml 新增查詢（T007）
  任務：DrawReq.java（T011）
  任務：AdminDrawHistoryReq.java（T012）
  任務：LockStatusRes.java（T013）
  任務：DrawResultRes.java 強化（T014）

批次二（依賴批次一 Mapper XML）：
  任務：LotteryPrizeMapper.java 方法宣告（T008）
  任務：LotteryLockMapper.java 方法宣告（T009）
  任務：LotteryDrawRecordMapper.java 方法宣告（T010）
```

### US2 完成後，US3 / US4 / 管理後台可完全平行

```
同時啟動（依賴 US1 DrawServiceImpl 完成）：
  任務：最後賞邏輯整合至 DrawServiceImpl（T024）   ← US3
  任務：自動降價邏輯整合至 DrawServiceImpl（T025）  ← US4
  任務：LotteryMapper 降價 SQL（T026）               ← US4 前置
  任務：AdminDrawHistoryRes.java（T027）             ← 管理後台
```

---

## 實作策略

### MVP 優先（僅使用者故事 1）

1. 完成第一階段：設置（T001–T004）
2. 完成第二階段：基礎架構（T005–T016）**← 關鍵阻斷點**
3. 完成第三階段：使用者故事 1（T017–T019）
4. **暫停並驗證**：單次抽獎、多抽、SOLD_OUT 保護均正常
5. 可部署示範 MVP

### 增量交付

1. 完成設置 + 基礎架構 → 基礎就緒
2. 新增使用者故事 1（抽獎核心）→ 獨立測試 → 部署 **（MVP！）**
3. 新增使用者故事 2（保護時間）→ 獨立測試 → 部署
4. 新增使用者故事 3（最後賞）→ 獨立測試 → 部署
5. 新增使用者故事 4（自動降價）→ 獨立測試 → 部署
6. 新增管理後台查詢 → 獨立測試 → 部署
7. 每個故事交付都不破壞前一個故事

### 雙人開發平行策略

基礎架構完成後：

```
開發者 A：使用者故事 1（T017–T019）→ 使用者故事 2（T020–T023）
開發者 B：使用者故事 3（T024）→ 使用者故事 4（T025–T026）→ 管理後台（T027–T029）
```

---

## 備注

- **[P]** 任務 = 不同檔案，無未完成依賴，可安全平行化
- **[USn]** 標籤將任務對映至 spec.md 中的特定使用者故事
- 所有抽獎操作必須在 `@Transactional` 邊界內執行（FR-010）
- `SELECT FOR UPDATE` 確保庫存遞減的原子性（防止超額抽獎 SC-001）
- 保護鎖定 100% 阻擋其他玩家（SC-002）；由第二階段 LotteryLockMapper + 第四階段 LotteryLockServiceImpl 保證
- 所有回應必須使用既有 `ApiResponse<T>` 包裝格式
- 所有 Mapper 使用 MyBatis XML 風格（不使用 @Select 等註解方式）
- 提交時機：每個任務或每個邏輯群組完成後即提交
- 在各檢查點暫停，獨立驗證使用者故事
