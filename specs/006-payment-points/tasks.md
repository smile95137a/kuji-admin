# 任務清單：付款與點數系統（Payment & Points System）

**功能**：`006-payment-points`
**輸入**：`specs/006-payment-points/` 的設計文件（plan.md、spec.md、data-model.md、research.md、contracts/、quickstart.md）
**技術棧**：Java 21 · Spring Boot 3.3.3 · MyBatis 3.0.5 · MySQL 8.3 · JWT · Lombok
**套件根目錄**：`src/main/java/com/group/admin/`

## 格式說明：`[ID] [P?] [US?] 說明（含檔案路徑）`

- **[P]**：可並行執行（不同檔案、無未完成相依）
- **[US#]**：對應的使用者故事（US1–US5）
- 每個任務說明均含明確的絕對或相對檔案路徑

---

## 第一階段：環境設定（Setup）

**目的**：確認開發環境就緒、建立分支、設定本地設定檔

- [ ] T001 確認開發環境（Java 21、Maven 3.9+、MySQL 8.3）並切換至 `006-payment-points` 分支（參考 `specs/006-payment-points/quickstart.md` 第 1–2 節）
- [ ] T002 在 `src/main/resources/application-local.yml` 中新增 `payment.gateway`（`provider: stub`、`stub.always-success: true`、`callback-base-url`）與 `wallet`（`recharge-order.ttl-minutes: 30`、`optimistic-lock.max-retries: 3`）設定區段

---

## 第二階段：基礎建設（Foundational）

**目的**：所有使用者故事均依賴的核心基礎設施 — 必須在任何故事開工前全部完成

**⚠️ 阻塞項目**：此階段完成前，任何使用者故事皆無法開始

- [ ] T003 在執行遷移前對現有資料進行預置驗證（`SELECT COUNT(*) FROM users WHERE gold_coins < 0 OR bonus_coins < 0`；必須為 0），並建立遷移腳本 `src/main/resources/db/migration/V006c__users_wallet_constraints.sql`（在 `users` 表新增 `total_recharged BIGINT NOT NULL DEFAULT 0` 欄位與 `CONSTRAINT chk_no_negative_balances CHECK (gold_coins >= 0 AND bonus_coins >= 0)`）
- [ ] T004 建立資料庫遷移腳本 `src/main/resources/db/migration/V006__create_recharge_order.sql`（含完整 `recharge_order` 表結構、`ENUM('PENDING','SUCCESS','FAILED','EXPIRED')` 狀態欄位、`UNIQUE INDEX uq_gateway_order (gateway_provider, gateway_order_id)`、所有索引及外鍵約束，詳見 data-model.md 實體 3）
- [ ] T005 建立資料庫遷移腳本 `src/main/resources/db/migration/V006b__extend_wallet_transaction.sql`（補充 `gold_delta`、`bonus_delta`、`gold_after`、`bonus_after`、`reference_id`、`reason` 欄位，以 `IF NOT EXISTS` 保護現有欄位；確認 `(user_id, created_at DESC)` 複合索引存在）
- [ ] T006 [P] 建立或驗證 `TransactionType` enum（RECHARGE / DRAW / BONUS_GRANT / RECYCLE / ADMIN_ADJUST / REFUND，含 Javadoc 說明各類型用途）at `src/main/java/com/group/admin/enums/TransactionType.java`
- [ ] T007 [P] 建立或驗證 `RechargeOrderStatus` enum（PENDING / SUCCESS / FAILED / EXPIRED，含狀態機注解說明合法轉換路徑）at `src/main/java/com/group/admin/enums/RechargeOrderStatus.java`
- [ ] T008 [P] 擴充 `WalletTransaction` 實體（新增 `goldDelta`、`bonusDelta`、`goldAfter`、`bonusAfter`（Long）、`referenceId`（String）、`reason`（String）欄位；`transactionType` 標注 `@Enumerated(EnumType.STRING)`；Lombok `@Data`、`@Builder`）at `src/main/java/com/group/admin/entity/WalletTransaction.java`
- [ ] T009 建立 `RechargeOrder` 實體（含 `id`、`userId`、`planId`、`goldAmount`、`bonusAmount`、`priceTwd`、`status`（`RechargeOrderStatus`）、`gatewayProvider`、`gatewayOrderId`、`gatewayRawResp`、`paidAt`、`expiredAt`、`createdAt`、`updatedAt` 欄位；Lombok `@Data`、`@Builder`）at `src/main/java/com/group/admin/entity/RechargeOrder.java`
- [ ] T010 在 `src/main/resources/mapper/UserMapper.xml` 中新增 `updateBalanceWithVersion` UPDATE 語句（`SET gold_coins = #{goldCoins}, bonus_coins = #{bonusCoins}, version = version + 1, updated_at = NOW() WHERE id = #{userId} AND version = #{version}`；對應 `UserMapper.java` 介面方法簽名）
- [ ] T011 [P] 建立 `WalletTransactionMapper.java`（`insertTransaction`、`selectByUserIdPaged`（含可選 `transactionType` 過濾、`startDate`/`endDate` 過濾）方法）及對應 `src/main/resources/mapper/WalletTransactionMapper.xml`（含 `<resultMap>`、分頁查詢、排序 `ORDER BY created_at DESC`）
- [ ] T012 建立 `RechargeOrderMapper.java`（`insert`、`selectById`、`updateStatusByIdAndExpectStatus`（CAS 更新，回傳影響行數）、`updateExpiredOrders`（批次到期）方法）及對應 `src/main/resources/mapper/RechargeOrderMapper.xml`
- [ ] T013 [P] 建立 `PaymentGatewayClient` 介面（`GatewayInitResult charge(RechargeOrder order)` 與 `GatewayCallbackResult verifyCallback(String rawPayload, String signature)` 方法）及 `GatewayCallbackResult` record（`merchantOrderId`、`success`、`gatewayOrderId`、`amountTwd`、`paidAt`、`rawPayload`）at `src/main/java/com/group/admin/gateway/PaymentGatewayClient.java`
- [ ] T014 建立 `StubPaymentGatewayClient` 實作（`@ConditionalOnProperty(name="payment.gateway.provider", havingValue="stub")`；`charge()` 回傳包含 `payUrl = /api/wallet/recharge/callback/stub?orderId={id}&success=true` 的結果；`verifyCallback()` 直接回傳 success=true）at `src/main/java/com/group/admin/gateway/StubPaymentGatewayClient.java`
- [ ] T015 在 Spring Security 設定中將 `POST /api/wallet/recharge/callback` 加入 `permitAll()` 白名單（免 JWT，僅依靠 HMAC 簽章驗證保護；確認 `/api/wallet/recharge/callback/stub` 僅限非 prod 環境）at `src/main/java/com/group/admin/config/SecurityConfig.java`
- [ ] T016 [P] 建立 `WalletProperties`（`@ConfigurationProperties(prefix="wallet")`）與 `PaymentProperties`（`@ConfigurationProperties(prefix="payment.gateway")`）設定類別（綁定 `ttl-minutes`、`max-retries`、`provider`、gateway 憑證欄位）at `src/main/java/com/group/admin/config/WalletProperties.java`

**檢查點**：基礎建設完成 — 執行 `mvn clean package -DskipTests` 確認編譯通過；所有使用者故事可開始並行實作

---

## 第三階段：使用者故事 1 — 玩家以信用卡儲值 Gold（優先級：P1）🎯 MVP

**目標**：玩家選取儲值方案 → 發起信用卡付款 → 業者回呼後 Gold（與可選 Bonus）原子性入帳；付款失敗時餘額不變

**獨立測試**：選取「入門包 500」→ 執行 Stub 回呼（quickstart.md 第 5c–5d 節）→ 確認 `goldBalance` 增加 500（+50 bonusBalance）

### 使用者故事 1 實作

- [ ] T017 [P] [US1] 建立 `RechargeReq` DTO（`@NotBlank @Pattern(regexp="...") String planId`；加上 Swagger/OpenAPI `@Schema` 說明）at `src/main/java/com/group/admin/req/wallet/RechargeReq.java`
- [ ] T018 [P] [US1] 建立 `RechargeRes` DTO（`rechargeOrderId`、`payUrl`、`goldAmount`、`bonusAmount`、`priceTwd`（BigDecimal）、`expiredAt`（LocalDateTime）欄位）at `src/main/java/com/group/admin/res/wallet/RechargeRes.java`
- [ ] T019 [P] [US1] 建立 `GatewayCallbackReq` DTO（解析業者 webhook 載荷；含 `merchantOrderId`、`status`、`transactionId`、`amount`、`currency`、`paidTime` 欄位；使用 `@JsonProperty` 對應業者欄位命名）at `src/main/java/com/group/admin/req/wallet/GatewayCallbackReq.java`
- [ ] T020 [US1] 在 `RechargeService` 介面中定義 `createRechargeOrder(String userId, String planId): RechargeRes` 與 `handleCallback(GatewayCallbackResult result): void` 方法簽名 at `src/main/java/com/group/admin/service/RechargeService.java`
- [ ] T021 [US1] 實作 `RechargeServiceImpl.createRechargeOrder`（查詢 `RechargePlan` 驗證有效性與時間範圍 → 建立 PENDING `RechargeOrder`（id=UUID，expiredAt=now+ttl）→ 呼叫 `PaymentGatewayClient.charge()` → 回傳 `RechargeRes`；套餐不存在拋 404，業者失敗拋 502）at `src/main/java/com/group/admin/service/impl/RechargeServiceImpl.java`
- [ ] T022 [US1] 實作 `RechargeServiceImpl.handleCallback`（冪等保護：`status != PENDING` 則靜默回傳 → 原子 CAS 更新 `PENDING→SUCCESS`（rowsAffected==0 代表並發已處理）→ 樂觀鎖入帳 `gold_coins`/`bonus_coins` → `INSERT wallet_transaction`（RECHARGE type；bonus_amount > 0 時額外插入 BONUS_GRANT 記錄）；全程 `@Transactional`）at `src/main/java/com/group/admin/service/impl/RechargeServiceImpl.java`
- [ ] T023 [US1] 實作 `RechargeController`（`POST /api/wallet/recharge`：需 JWT，從 SecurityContext 取 userId；`POST /api/wallet/recharge/callback`：免 JWT + HMAC 驗簽，無論結果均回傳 `{"result":"OK"}`；`POST /api/wallet/recharge/callback/stub`：`@Profile("!prod")` 限制，接受 `?orderId&success` 參數模擬回呼）at `src/main/java/com/group/admin/controller/api/RechargeController.java`
- [ ] T024 [US1] 擴充/驗證 `RechargePlanController` 中的 `GET /api/recharge-plans` 端點（公開端點；查詢條件：`is_active=1 AND (valid_from IS NULL OR valid_from <= NOW()) AND (valid_until IS NULL OR valid_until >= NOW())`；按 `sort_order` 升冪排序）at `src/main/java/com/group/admin/controller/api/RechargePlanController.java`

**檢查點**：US1 可獨立測試 — 執行 quickstart.md 第 5b、5c、5d 節完整冒煙測試，確認 `goldBalance` 與 `bonusBalance` 正確增加

---

## 第四階段：使用者故事 2 — 玩家以點數抽獎（優先級：P1）

**目標**：抽獎時原子性扣除點數；優先扣 Gold，Gold 耗盡才用 Bonus；餘額不足則拒絕 422；並發衝突最多重試 3 次

**獨立測試**：持有 1000 Gold 的玩家執行 500 點抽獎 → `goldBalance` 變為 500；100 Gold 玩家執行 500 點抽獎 → HTTP 422；200 Gold + 300 Bonus 玩家執行 400 點 → Gold 扣至 0、Bonus 扣 200

### 使用者故事 2 實作

- [ ] T025 [P] [US2] 建立 `InsufficientBalanceException`（繼承 `RuntimeException`；含 `userId` 與 `required`/`available` 欄位供除錯）at `src/main/java/com/group/admin/exception/InsufficientBalanceException.java`
- [ ] T026 [US2] 在 `WalletService` 介面中定義 `deductCoins(String userId, long amount, String referenceId, String reason): void` 與 `boolean hasEnoughBalance(User user, long amount)` 方法 at `src/main/java/com/group/admin/service/WalletService.java`
- [ ] T027 [US2] 實作 `WalletServiceImpl.deductCoins`（`hasEnoughBalance` 預先快速失敗檢查（拋 `InsufficientBalanceException`）→ 金幣優先計算 `newGold`/`newBonus` → 樂觀鎖 `updateBalanceWithVersion`（rowsAffected==0 則重試，最多 3 次，耗盡後拋 `ConcurrentModificationException`）→ `INSERT wallet_transaction` DRAW 流水；`@Transactional`）at `src/main/java/com/group/admin/service/impl/WalletServiceImpl.java`
- [ ] T028 [US2] 在抽獎/訂單服務的 `@Transactional` 邊界內整合 `walletService.deductCoins` 呼叫（確認 `InsufficientBalanceException` 正確觸發交易回滾；`referenceId` 傳入 orderId）at `src/main/java/com/group/admin/service/impl/OrderServiceImpl.java`（或對應的 `LotteryServiceImpl.java`）
- [ ] T029 [US2] 在全域例外處理器中新增對應規則（`InsufficientBalanceException` → HTTP 422、`ConcurrentModificationException` → HTTP 409 Conflict、`PrizeNotRecyclableException` → HTTP 409/422（依 status））at `src/main/java/com/group/admin/exception/GlobalExceptionHandler.java`

**檢查點**：US2 可獨立測試 — 驗證金幣優先扣款邏輯（`testDeductGoldFirst`、`testDeductGoldThenBonus`）、餘額不足 422、並發衝突 409

---

## 第五階段：使用者故事 3 — 玩家查看點數交易紀錄（優先級：P2）

**目標**：玩家可查看完整分頁交易紀錄；每筆記錄顯示類型、金額及交易後餘額；無交易時回傳空列表；1 秒內回應

**獨立測試**：儲值後執行抽獎 → 呼叫 `GET /api/wallet/transactions` → 確認兩筆記錄（RECHARGE / DRAW）均含正確 `goldDelta` 與 `goldAfter`；空帳戶回傳 `content: []`

### 使用者故事 3 實作

- [ ] T030 [P] [US3] 建立 `WalletRes` DTO（`userId`、`goldBalance`、`bonusBalance`、`totalRecharged` Long 欄位；Lombok `@Data`）at `src/main/java/com/group/admin/res/wallet/WalletRes.java`
- [ ] T031 [P] [US3] 建立 `TransactionRes` DTO（`id`、`transactionType`（String）、`goldDelta`、`bonusDelta`、`goldAfter`、`bonusAfter`（Long）、`referenceId`、`reason`（String）、`createdAt`（LocalDateTime）欄位）at `src/main/java/com/group/admin/res/wallet/TransactionRes.java`
- [ ] T032 [US3] 在 `WalletService` 介面中定義 `getWallet(String userId): WalletRes` 與 `getTransactions(String userId, int page, int size, String type, LocalDateTime startDate, LocalDateTime endDate): PageResult<TransactionRes>` 方法 at `src/main/java/com/group/admin/service/WalletService.java`
- [ ] T033 [US3] 實作 `WalletServiceImpl.getWallet`（查詢 `users` 表對應至 `WalletRes`）與 `WalletServiceImpl.getTransactions`（呼叫 `WalletTransactionMapper.selectByUserIdPaged`；`type` 參數可選過濾；結果對應至 `TransactionRes` 列表；封裝為分頁回應）at `src/main/java/com/group/admin/service/impl/WalletServiceImpl.java`
- [ ] T034 [US3] 實作 `WalletController`（`GET /api/wallet` 需 JWT，回傳 `WalletRes`；`GET /api/wallet/transactions?page&size&type&startDate&endDate` 需 JWT，`size` 最大 100，回傳分頁 `TransactionRes`；`type` enum 值無效時回傳 400）at `src/main/java/com/group/admin/controller/api/WalletController.java`

**檢查點**：US3 可獨立測試 — 執行 quickstart.md 第 5a、5e 節冒煙測試；確認回應格式（`totalElements`、`totalPages`、`content`）正確

---

## 第六階段：使用者故事 4 — 玩家將獎品回收換 Bonus（優先級：P3）

**目標**：玩家可將 `status=AVAILABLE` + `is_recyclable=1` 的獎品永久回收換取 Bonus 點數（非 Gold）；已出貨/已回收則拒絕；回收不可撤銷

**獨立測試**：持有可回收獎品的玩家呼叫 `POST /api/prize-box/recycle` → 獎品狀態變為 RECYCLED → `bonusBalance` 增加 `recycle_bonus` 對應點數；status=SHIPPED 的獎品回傳 409

### 使用者故事 4 實作

- [ ] T035 [P] [US4] 建立 `RecycleReq` DTO（`@NotBlank String prizeBoxId` 欄位）at `src/main/java/com/group/admin/req/wallet/RecycleReq.java`
- [ ] T036 [P] [US4] 建立 `RecycleRes` DTO（`prizeBoxId`、`bonusAwarded`、`newBonusBalance` Long、`transactionId` String 欄位）at `src/main/java/com/group/admin/res/wallet/RecycleRes.java`
- [ ] T037 [P] [US4] 建立 `PrizeNotRecyclableException`（帶 `prizeBoxId`、`reason`（SHIPPED / ALREADY_RECYCLED / NOT_RECYCLABLE）欄位；`SHIPPED`/`ALREADY_RECYCLED` 對應 HTTP 409；`NOT_RECYCLABLE` 對應 HTTP 422）at `src/main/java/com/group/admin/exception/PrizeNotRecyclableException.java`
- [ ] T038 [US4] 在 `WalletService` 介面中定義 `recyclePrize(String userId, String prizeBoxId): RecycleRes` 方法 at `src/main/java/com/group/admin/service/WalletService.java`
- [ ] T039 [US4] 實作 `WalletServiceImpl.recyclePrize`（`SELECT * FROM prize_box WHERE id=? AND user_id=? FOR UPDATE` → 驗證 `status=AVAILABLE`（否則拋 409）+ `is_recyclable=1`（否則拋 422）→ `UPDATE prize_box SET status='RECYCLED', recycled_at=NOW()` → 樂觀鎖入帳 `bonus_coins += recycle_bonus` → `INSERT wallet_transaction`（type=RECYCLE，referenceId=prizeBoxId）→ 回傳 `RecycleRes`；全程 `@Transactional`）at `src/main/java/com/group/admin/service/impl/WalletServiceImpl.java`
- [ ] T040 [US4] 實作/擴充 `PrizeBoxController` 中的 `POST /api/prize-box/recycle` 端點（需 JWT；驗證 `prizeBoxId` 屬於當前登入使用者（否則 403）；呼叫 `walletService.recyclePrize`；回傳 `RecycleRes`）at `src/main/java/com/group/admin/controller/api/PrizeBoxController.java`

**檢查點**：US4 可獨立測試 — 執行 quickstart.md 第 5f 節冒煙測試；確認 `bonusBalance` 正確增加且 `prize_box.status = 'RECYCLED'`

---

## 第七階段：管理員功能（FR-009 / FR-010）

**目標**：管理員可管理儲值套餐（CRUD）與手動調整玩家點數（含原因欄位 + 稽核日誌）；商店角色無法存取（FR-011）

**獨立測試**：管理員新增套餐 → 玩家端 `GET /api/recharge-plans` 可見；管理員呼叫 `POST /admin/wallet/adjust` +10 Bonus → 玩家 `bonusBalance` 增加 10 且 `wallet_transaction` 存在 ADMIN_ADJUST 記錄

### 管理員功能實作

- [ ] T041 [P] [US5] 建立 `AdminAdjustReq` DTO（`@NotBlank String userId`；`@NotNull CurrencyType currency`（GOLD/BONUS enum）；`@NotNull @NotZero long delta`；`@NotBlank @Size(min=5,max=500) String reason`；加上 `@Valid` 驗證）at `src/main/java/com/group/admin/req/wallet/AdminAdjustReq.java`
- [ ] T042 [P] [US5] 建立 `AdminAdjustRes` DTO（`userId`、`currency`、`delta`、`goldBalanceAfter`、`bonusBalanceAfter` Long、`transactionId`、`adminId`、`reason` String、`adjustedAt` LocalDateTime 欄位）at `src/main/java/com/group/admin/res/wallet/AdminAdjustRes.java`
- [ ] T043 [US5] 在 `WalletService` 介面中定義 `adminAdjust(String adminId, AdminAdjustReq req): AdminAdjustRes` 方法 at `src/main/java/com/group/admin/service/WalletService.java`
- [ ] T044 [US5] 實作 `WalletServiceImpl.adminAdjust`（樂觀鎖讀取使用者餘額 → 計算調整後餘額 → 負向調整導致餘額 < 0 則拋 422 `"Adjustment would result in negative balance"` → `updateBalanceWithVersion` → `INSERT wallet_transaction`（type=ADMIN_ADJUST，reason=req.reason，referenceId=null）→ 回傳 `AdminAdjustRes`；`@Transactional`）at `src/main/java/com/group/admin/service/impl/WalletServiceImpl.java`
- [ ] T045 [US5] 實作/擴充 `AdminWalletController`（`POST /admin/wallet/adjust`：`@PreAuthorize("hasRole('ADMIN')")`，從 SecurityContext 取 adminId；`GET /admin/wallet/user/{userId}`：回傳使用者資訊（`nickname`、`email`）+ 餘額 + 分頁交易歷史；兩端點均需 Admin JWT，回傳 403 給 STORE 角色）at `src/main/java/com/group/admin/controller/admin/AdminWalletController.java`
- [ ] T046 [P] [US5] 建立 `RechargePlanReq` DTO（`@NotBlank @Size(max=100) String name`；`@NotNull @Positive long goldAmount`；`@Min(0) long bonusAmount`；`@NotNull @Positive BigDecimal priceTwd`；`Boolean isActive`；`Integer sortOrder`；`LocalDateTime validFrom`；`LocalDateTime validUntil`；含 `validFrom < validUntil` 跨欄位驗證）at `src/main/java/com/group/admin/req/wallet/RechargePlanReq.java`
- [ ] T047 [US5] 實作/擴充 `AdminRechargePlanController`（`GET /admin/recharge-packages?isActive&page&size`；`POST /admin/recharge-packages` → 201 Created；`PUT /admin/recharge-packages/{id}`（PATCH 語意，僅更新已提供欄位）；`DELETE /admin/recharge-packages/{id}`（軟刪除：`is_active=false`，不刪資料列）；全部端點限 ADMIN 角色）at `src/main/java/com/group/admin/controller/admin/AdminRechargePlanController.java`

**檢查點**：管理員功能可獨立測試 — 執行 quickstart.md 第 5g 節冒煙測試；確認 `transactionId` 回傳且 `bonusBalanceAfter` 正確增加

---

## 第八階段：收尾與跨功能優化（Polish）

**目的**：涵蓋所有故事的橫向關切事項與生產環境就緒準備

- [ ] T048 [P] 建立 `RechargeOrderExpiryJob` 排程器（`@Scheduled(cron = "0 */5 * * * *")`；呼叫 `RechargeOrderMapper.updateExpiredOrders`（批次將 `expired_at < NOW() AND status = 'PENDING'` 的訂單設為 EXPIRED）；記錄每次執行的處理筆數）at `src/main/java/com/group/admin/scheduler/RechargeOrderExpiryJob.java`
- [ ] T049 [P] 在 `RechargePlanMapper.xml` 中新增時間範圍過濾查詢語句（`is_active = 1 AND (valid_from IS NULL OR valid_from <= NOW()) AND (valid_until IS NULL OR valid_until >= NOW())`；按 `sort_order ASC` 排序），對應 `RechargePlanMapper.java` 新方法 at `src/main/resources/mapper/RechargePlanMapper.xml`
- [ ] T050 [P] 建立生產環境種子資料腳本（含 4 個儲值套餐 INSERT：體驗包 100、入門包 500、大禮包 1000、超值包 2000）at `src/main/resources/db/seed/V006__seed_recharge_plans.sql`（詳見 quickstart.md 第 2b 節）
- [ ] T051 在 `src/main/resources/application-prod.yml` 補充生產設定（`payment.gateway.provider: tappay`（或選定業者）；停用 Stub 端點；設定 `callback-base-url` 為正式域名）並更新 quickstart.md 第 7 節生產環境部署檢查清單
- [ ] T052 執行 quickstart.md 完整冒煙測試（第 5a–5g 節全部情境），確認各使用者故事端對端流程通過：錢包餘額、套餐列表、發起儲值、模擬回呼、交易歷史、獎品回收、管理員調整

---

## 相依關係與執行順序

### 階段相依

| 階段 | 相依 | 說明 |
|------|------|------|
| 第一階段（Setup） | 無 | 立即開始 |
| 第二階段（Foundational） | 第一階段完成 | **阻塞所有使用者故事** |
| 第三–七階段（使用者故事） | 第二階段完成 | 各故事間彼此獨立，可並行實作 |
| 第八階段（Polish） | 所有所需故事完成 | 最後收尾 |

### 使用者故事相依

| 使用者故事 | 優先級 | 可開始條件 | 對其他故事的相依 |
|---------|---------|----------|----------------|
| US1 — 信用卡儲值 Gold | P1 | 第二階段完成後 | 無 |
| US2 — 抽獎扣款 | P1 | 第二階段完成後 | 無（WalletService 介面在 Foundational 定義） |
| US3 — 交易紀錄查詢 | P2 | 第二階段完成後 | 實作獨立；交易資料由 US1/US2 運行時產生 |
| US4 — 獎品回收換 Bonus | P3 | 第二階段完成後 | 無 |
| US5 — 管理員功能 | P2/P3 | 第二階段完成後 | 無 |

### 故事內部執行順序

1. **DTO / 例外類別**（`[P]` 標記，可並行）
2. **服務介面定義**（方法簽名）
3. **服務實作**（業務邏輯、樂觀鎖、交易）
4. **Controller 端點**（路由、認證、請求驗證）
5. **整合驗證**（冒煙測試）

---

## 並行執行範例

### 第二階段並行機會

```bash
# 可同時執行（不同檔案）：
Task T006: TransactionType enum
Task T007: RechargeOrderStatus enum
Task T008: WalletTransaction entity 擴充
Task T011: WalletTransactionMapper + XML
Task T013: PaymentGatewayClient 介面 + record
Task T016: WalletProperties / PaymentProperties 設定類別
```

### 使用者故事 1 並行機會

```bash
# 可同時執行的 DTO 建立任務：
Task T017: RechargeReq DTO
Task T018: RechargeRes DTO
Task T019: GatewayCallbackReq DTO
```

### 跨故事並行（第二階段完成後）

```bash
# 開發者 A：
Task T017–T024（US1 信用卡儲值）

# 開發者 B：
Task T025–T029（US2 抽獎扣款）

# 開發者 C：
Task T030–T034（US3 交易紀錄）
Task T035–T040（US4 獎品回收）— 可在 US3 完成後繼續

# 開發者 D：
Task T041–T047（US5 管理員功能）
```

---

## 實作策略

### MVP 優先（僅使用者故事 1）

1. 完成第一階段：環境設定
2. 完成第二階段：基礎建設（**關鍵 — 阻塞所有故事**）
3. 完成第三階段：US1 信用卡儲值
4. **停止並驗證**：執行 quickstart.md 第 5b、5c、5d 節確認端對端流程
5. 若 US1 通過，可部署/展示 MVP（儲值功能上線）

### 遞增交付

1. Setup + Foundational → 基礎就緒
2. US1 → 獨立測試 → 部署（**MVP！儲值功能上線**）
3. US2 → 獨立測試 → 部署（抽獎扣款上線，遊戲循環完整）
4. US3 → 獨立測試 → 部署（交易紀錄上線，增加使用者信任）
5. US4 → 獨立測試 → 部署（獎品回收上線，貨幣持續流通）
6. US5（Admin）→ 獨立測試 → 部署（管理員後台完整）
7. Polish → 全功能上線

### 並行團隊策略

多位開發者時：

1. 全員共同完成 **第一、二階段**（Setup + Foundational）
2. Foundational 完成後各自分工：
   - **開發者 A**：US1（信用卡儲值）
   - **開發者 B**：US2（抽獎扣款）
   - **開發者 C**：US3（交易紀錄）＋ US4（獎品回收）
   - **開發者 D**：US5（管理員功能）
3. 各故事獨立完成並整合

---

## 備註

- `[P]` = 可並行執行（不同檔案、無未完成相依任務）
- `[US#]` = 對應使用者故事，確保任務可追溯至業務需求
- 每個使用者故事皆可獨立完成並測試，不依賴其他故事的實作
- 樂觀鎖重試上限：3 次；耗盡後回傳 HTTP 409 Conflict
- 生產環境務必停用 `/api/wallet/recharge/callback/stub` Stub 端點
- `reason` 欄位在 `ADMIN_ADJUST` 交易類型時為必填（5–500 字元）；缺少時回傳 HTTP 400
- 負餘額三層防護：應用層 `hasEnoughBalance()` → 樂觀鎖版本檢查 → DB `CHECK` 約束
- 每個任務或邏輯群組完成後提交（`git commit`）
- 在各階段檢查點停下來獨立驗證故事後再繼續下一階段
