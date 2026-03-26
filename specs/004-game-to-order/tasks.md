# 任務清單：遊戲至訂單流程（Game-to-Order Flow）

**功能**：`004-game-to-order`  
**輸入**：`specs/004-game-to-order/`（plan.md、spec.md、data-model.md、research.md、contracts/、quickstart.md）  
**技術堆疊**：Java 21 + Spring Boot 3.3.3、MyBatis 3.0.5、MySQL 8.3、JUnit 5 + Mockito  
**目前狀態**：核心實體、mapper 及 service 均已存在 — 任務聚焦於**驗證缺口、補實作及撰寫測試**

## 格式說明：`[ID] [P?] [Story?] 描述（含檔案路徑）`

- **[P]**：可平行執行（不同檔案、無未完成的依賴任務）
- **[Story]**：對應使用者故事（US1 / US2 / US3 / US4）
- 每個任務均包含明確的檔案路徑

---

## 第一階段：設置（Setup）

**目的**：確認專案結構與測試環境就緒，以支援後續驗證與補實作任務

- [ ] T001 確認 `pom.xml` 包含所有必要測試相依套件（JUnit 5、Mockito、Spring Boot Test），位於 `pom.xml`
- [ ] T002 [P] 確認專案目錄結構符合 plan.md：`src/main/java/com/group/admin/` 下各層（`controller/api/`、`service/impl/`、`entity/`、`mapper/`、`enums/`、`req/prizebox/`、`res/prizebox/`、`res/order/`）均已存在

**檢查點**：設置確認完成，可進入基礎層驗證

---

## 第二階段：基礎（Foundational）— 阻擋所有使用者故事的先決條件

**目的**：在進行任何使用者故事前，先驗證並修補跨故事共用的核心邏輯

**⚠️ 重要**：此階段完成前，所有使用者故事任務不得開始

- [ ] T003 驗證 `LotteryTicketServiceImpl` 在成功抽獎後確實呼叫 `PrizeBoxService.addToPrizeBox(userId, lotteryId, prizeId, storeId, recycleBonus)` 並插入 `status = IN_BOX` 記錄，位於 `src/main/java/com/group/admin/service/impl/LotteryTicketServiceImpl.java`
- [ ] T004 [P] 驗證 `PrizeBoxServiceImpl.shipPrizes()` 使用 `Collectors.groupingBy(PrizeBox::getStoreId)` 依店家分組，並對每個店家呼叫一次 `OrderService.createOrdersFromPrizeBox()`，位於 `src/main/java/com/group/admin/service/impl/PrizeBoxServiceImpl.java`
- [ ] T005 驗證並確認/補實作 `OrderServiceImpl.cancelOrder()` 中的獎品返回邏輯：取消後需遍歷所有關聯 `OrderItem`，依 `prizeBoxId` 取得 `PrizeBox`，重置 `status = IN_BOX`、`orderId = null`、`shippedAt = null`，並於 `@Transactional` 範圍內更新；若邏輯缺失則依 research.md 第 4 節定義的模式補實作，位於 `src/main/java/com/group/admin/service/impl/OrderServiceImpl.java`
- [ ] T006 [P] 驗證所有端點路徑符合合約（`GET /prize-box`、`POST /prize-box/ship`、`POST /prize-box/recycle`、`POST /order/list`、`GET /order/{orderId}`、`POST /order/{orderId}/cancel`），位於 `src/main/java/com/group/admin/controller/api/PrizeBoxController.java` 及 `src/main/java/com/group/admin/controller/api/OrderController.java`

**檢查點**：基礎層驗證完成 — 使用者故事實作可平行進行

---

## 第三階段：使用者故事 1 — 抽獎結果進入獎品盒（優先級：P1）🎯 MVP

**目標**：確認成功抽獎後獎品自動進入玩家獎品盒（`status = IN_BOX`），且玩家可透過 API 查看獎品清單

**獨立測試**：玩家完成一次抽獎 → 呼叫 `GET /api/prize-box` → 確認新獎品出現且 `status = IN_BOX`；`POST /api/order/list` 回傳空清單（尚未建立訂單）

### 使用者故事 1 實作任務

- [ ] T007 [US1] 驗證 `PrizeBoxServiceImpl.addToPrizeBox()` 正確設置 `userId`、`lotteryId`、`prizeId`、`storeId`、`status = IN_BOX`，並使用 UUID 主鍵，位於 `src/main/java/com/group/admin/service/impl/PrizeBoxServiceImpl.java`
- [ ] T008 [P] [US1] 驗證 `PrizeBoxController.getMyPrizeBox()` 呼叫 `SecurityUtils.getCurrentUserId()` 並委派給 `PrizeBoxServiceImpl.getPrizeBox(userId)`；確認回應結構符合 `contracts/prize-box-list.md`（含 `id`、`prizeName`、`prizeLevel`、`prizeImageUrl`、`storeName`、`status`、`statusName`、`isRecyclable`、`recycleBonus`、`createdAt` 欄位），位於 `src/main/java/com/group/admin/controller/api/PrizeBoxController.java`
- [ ] T009 [P] [US1] 驗證 `PrizeBoxItemRes` 包含合約所需的所有回應欄位，且 `statusName` 對應正確中文標籤（`IN_BOX` → `"在獎品盒中"`、`SHIPPED` → `"已出貨"`、`RECYCLED` → `"已回收"`），位於 `src/main/java/com/group/admin/res/prizebox/PrizeBoxItemRes.java`
- [ ] T010 [US1] 撰寫整合測試 `PrizeBoxFlowIntegrationTest`：(1) `GET /api/prize-box` 回傳 `IN_BOX` 項目清單；(2) 空清單時回傳 `data: []`；(3) 已 `SHIPPED` 的項目不出現在清單中；(4) 結果依 `createdAt DESC` 排序，位於 `src/test/java/com/group/admin/integration/PrizeBoxFlowIntegrationTest.java`

**檢查點**：使用者故事 1 完整可測試 — 抽獎 → 獎品盒流程就緒

---

## 第四階段：使用者故事 2 — 玩家從獎品盒出貨獎品（優先級：P1）

**目標**：玩家選取 `IN_BOX` 項目後，系統建立訂單並將獎品狀態轉換為 `SHIPPED`；多店家項目自動分單；取消訂單後獎品返回 `IN_BOX`

**獨立測試**：呼叫 `POST /api/prize-box/ship` → 回傳訂單 ID 陣列 → 確認 `PrizeBox.status = SHIPPED`、`PrizeBox.orderId` 已設定；取消後 `PrizeBox.status = IN_BOX`、`orderId = null`

### 使用者故事 2 實作任務

- [ ] T011 [US2] 確認/補實作 `PrizeBoxServiceImpl.shipPrizes()` 中的擁有者驗證：若任何 `prizeBoxId` 的 `userId ≠ currentUserId` 則拋出 `BusinessException`（400，訊息：`"獎品不屬於您"`）；若任何項目 `status ≠ IN_BOX` 則拋出 `BusinessException`（400，訊息：`"獎品已出貨，無法再次出貨"`），位於 `src/main/java/com/group/admin/service/impl/PrizeBoxServiceImpl.java`
- [ ] T012 [P] [US2] 驗證 `PrizeBoxShipReq.java` 包含合約所需的所有欄位（`prizeBoxIds`、`shippingMethod`、`recipientName`、`recipientPhone`、`recipientAddress`、`storeCode`、`storeName`、`storeAddress`、`remark`），且 `@NotEmpty` 標注於 `prizeBoxIds`，位於 `src/main/java/com/group/admin/req/prizebox/PrizeBoxShipReq.java`
- [ ] T013 [P] [US2] 驗證 `OrderServiceImpl.createOrdersFromPrizeBox()` 正確插入 `Order` 及關聯的 `OrderItem`（含非正規化欄位：`prizeName`、`prizeLevel`、`prizeImageUrl`、`lotteryTitle`），且整個方法包裝在 `@Transactional` 中，位於 `src/main/java/com/group/admin/service/impl/OrderServiceImpl.java`
- [ ] T014 [US2] 驗證出貨配送驗證邏輯：宅配（`HOME_DELIVERY`）時 `recipientAddress` 為必填；超商取貨（`SEVEN_ELEVEN` / `FAMILY_MART`）時 `storeCode` + `storeName` 為必填；`recipientName` / `recipientPhone` 缺省時自動從 `User` 資料填入，填入後仍為空則拋出 `BusinessException`，位於 `src/main/java/com/group/admin/service/impl/PrizeBoxServiceImpl.java` 或 `src/main/java/com/group/admin/service/impl/OrderServiceImpl.java`
- [ ] T015 [P] [US2] 撰寫單元測試 `PrizeBoxServiceTest`：(1) 擁有者驗證失敗 → 拋出 `BusinessException`；(2) 已 `SHIPPED` 的獎品再次出貨 → 拋出 `BusinessException`；(3) 多店家 prizeBoxIds → 呼叫 `createOrdersFromPrizeBox()` 兩次，位於 `src/test/java/com/group/admin/service/PrizeBoxServiceTest.java`
- [ ] T016 [P] [US2] 撰寫單元測試 `OrderServiceTest`：(1) `cancelOrder()` 重置所有關聯 `PrizeBox.status → IN_BOX`、`orderId → null`、`shippedAt → null`；(2) 非 `PENDING` 狀態的訂單被玩家取消 → 拋出 `BusinessException`，位於 `src/test/java/com/group/admin/service/OrderServiceTest.java`
- [ ] T017 [US2] 撰寫整合測試 `OrderCancelIntegrationTest`：完整流程 — `POST /api/prize-box/ship` 建立訂單 → `POST /api/order/{orderId}/cancel` 取消 → 確認 `PrizeBox.status = IN_BOX`、`orderId = null`、`shippedAt = null`；確認取消後積分/錢包餘額不退還，位於 `src/test/java/com/group/admin/integration/OrderCancelIntegrationTest.java`

**檢查點**：使用者故事 2 完整可測試 — 出貨 → 訂單建立 → 取消返回流程就緒

---

## 第五階段：使用者故事 3 — 玩家選擇繼續抽獎或查看獎品盒（優先級：P2）

**目標**：確認 `GET /api/prize-box` 能正確回傳多次抽獎後累積的所有 `IN_BOX` 項目，支援玩家在前端執行「繼續抽獎」或「前往獎品盒」的導航選擇

**獨立測試**：玩家從兩個不同店家各抽一次彩券 → `GET /api/prize-box` 回傳兩筆 `IN_BOX` 項目 → 繼續第三次抽獎 → 確認三筆項目均出現，且每次新增後立即可見

### 使用者故事 3 實作任務

- [ ] T018 [US3] 確認 `PrizeBoxServiceImpl.getPrizeBox(userId)` 使用 `PrizeBoxExample` 依 `userId` 篩選且 `status = IN_BOX`，並依 `createdAt DESC` 排序，正確支援多次抽獎後累積獎品的查詢場景，位於 `src/main/java/com/group/admin/service/impl/PrizeBoxServiceImpl.java`
- [ ] T019 [P] [US3] 撰寫整合測試（新增至現有 `PrizeBoxFlowIntegrationTest`）：(1) 多次抽獎後 `GET /api/prize-box` 回傳所有累積 `IN_BOX` 獎品（包含不同店家的項目）；(2) 出貨後呼叫 `GET /api/prize-box` 已出貨項目不再出現，位於 `src/test/java/com/group/admin/integration/PrizeBoxFlowIntegrationTest.java`

**檢查點**：使用者故事 3 完整可測試 — 累積獎品查詢及連續抽獎流程就緒

---

## 第六階段：使用者故事 4 — 玩家查看訂單狀態（優先級：P2）

**目標**：玩家可查看訂單清單（含篩選與分頁）及個別訂單詳情（含出貨項目、配送資訊、所有狀態欄位）

**獨立測試**：出貨後呼叫 `POST /api/order/list` → 確認訂單出現且 `shippingStatus = PENDING`；呼叫 `GET /api/order/{orderId}` → 確認回傳完整訂單詳情含 `items` 陣列；呼叫他人訂單 ID → 回傳 403

### 使用者故事 4 實作任務

- [ ] T020 [US4] 驗證 `OrderController.getMyOrders()` 使用 `SecurityUtils.getCurrentUserId()` 隔離玩家資料（玩家無法取得他人訂單）；確認回應結構符合 `contracts/order-list.md`（含 `list`、`total`、`pageNum`、`pageSize` 分頁欄位及訂單狀態中文標籤），位於 `src/main/java/com/group/admin/controller/api/OrderController.java`
- [ ] T021 [P] [US4] 驗證 `OrderController.getOrderDetail()` 回傳完整 `OrderDetailRes`（含 `items` 陣列、`orderNo`、`storeName`、`shippingMethod`、`shippingStatus`、`shippingStatusName` 及所有時間戳記欄位）；確認找不到訂單時回傳 404、非訂單擁有者時回傳 403，位於 `src/main/java/com/group/admin/controller/api/OrderController.java`
- [ ] T022 [P] [US4] 驗證 `OrderDetailRes` 及 `OrderItemRes` 包含合約所需的所有欄位（`shippingStatusName` 對應正確中文標籤）；確認 `OrderItemRes` 含 `prizeBoxId`、`lotteryTitle`、`prizeName`、`prizeLevel`、`prizeImageUrl`，位於 `src/main/java/com/group/admin/res/order/OrderDetailRes.java`
- [ ] T023 [US4] 撰寫整合測試 `OrderFlowIntegrationTest`：(1) `POST /api/order/list` 回傳已建立訂單且含正確狀態與分頁；(2) `GET /api/order/{orderId}` 回傳完整訂單詳情含 `items`；(3) 依狀態篩選訂單清單（`condition.status = PENDING`）；(4) 無效訂單 ID → 404；(5) 他人訂單 ID → 403，位於 `src/test/java/com/group/admin/integration/OrderFlowIntegrationTest.java`

**檢查點**：使用者故事 4 完整可測試 — 訂單查詢流程就緒

---

## 最終階段：收尾與橫切關注點（Polish & Cross-Cutting Concerns）

**目的**：端對端驗收驗證、多店家分單確認，以及可選增強功能

- [ ] T024 [P] 執行 `quickstart.md` 中的完整端對端驗證流程（步驟 1–5：抽獎 → 查看獎品盒 → 出貨 → 查看訂單清單 → 取消訂單）並驗證 `quickstart.md` 第 8 節的資料庫 SQL 查詢結果，位於 `src/main/java/com/group/admin/`（整體驗證）
- [ ] T025 [P] 撰寫多店家訂單分單整合測試 `MultiStoreShipIntegrationTest`：從兩個不同店家各取一個 `prizeBoxId` → `POST /api/prize-box/ship` → 確認回傳兩個訂單 ID → 確認每筆訂單 `storeId` 正確對應各自店家（驗證 FR-004、SC-004），位於 `src/test/java/com/group/admin/integration/MultiStoreShipIntegrationTest.java`
- [ ] T026 [P] 可選增強：在 `OrderController.java` 新增 `GET /api/orders` 便利別名端點，接受查詢參數（`?status=&page=&size=`）並委派給相同的 service 方法（詳見 `contracts/order-list.md` 末尾備註），位於 `src/main/java/com/group/admin/controller/api/OrderController.java`
- [ ] T027 [P] 可選增強：為 `GET /api/prize-box` 新增 `?status=` 查詢參數篩選，支援未來「透過獎品盒查看出貨歷史」視圖（詳見 research.md 第 5 節），位於 `src/main/java/com/group/admin/controller/api/PrizeBoxController.java` 及 `src/main/java/com/group/admin/service/impl/PrizeBoxServiceImpl.java`

---

## 依賴關係與執行順序

### 階段依賴

- **第一階段（設置）**：無依賴 — 可立即開始
- **第二階段（基礎）**：依賴第一階段完成 — **阻擋所有使用者故事**
- **第三至四階段（US1、US2 — P1）**：均依賴第二階段完成；P1 優先，可平行進行
- **第五至六階段（US3、US4 — P2）**：可在 P1 完成後進行；US3 依賴 US1（獎品盒查詢 API），US4 依賴 US2（需有已建立訂單）
- **最終階段（收尾）**：依賴所有目標使用者故事完成

### 使用者故事依賴

- **US1（P1）**：僅依賴第二階段完成 — 無跨故事依賴
- **US2（P1）**：依賴第二階段（尤其是 **T005** 取消返回邏輯） — 功能上獨立可測試
- **US3（P2）**：依賴 US1 完成（需要獎品盒查詢 API 就緒）
- **US4（P2）**：依賴 US2 完成（需要已建立的訂單資料）

### 各使用者故事內部執行順序

- 驗證既有程式碼（Verify）→ 補實作缺口（Gap fix）→ 撰寫測試（Tests）
- 同一故事內標記 `[P]` 的任務可平行進行
- **T005 為最高優先缺口任務** — 影響 US2 全部測試及整體資料一致性

### 平行執行機會

- 第二階段：T003、T004、T006 可平行執行（T005 建議優先單獨處理）
- 第三階段：T007、T008、T009 可平行執行後再進行 T010
- 第四階段：T012、T013、T015、T016 可平行執行；T014 依賴 T011；T017 依賴 T005 + T011
- 第五階段：T019 可在 T018 後立即平行進行
- 第六階段：T020、T021、T022 可平行執行後再進行 T023
- 最終階段：T024、T025、T026、T027 均可平行執行

---

## 平行執行範例

### 使用者故事 1 平行執行

```bash
# 可同時進行：
任務（T007）：「驗證 PrizeBoxServiceImpl.addToPrizeBox() 欄位設置」
任務（T008）：「驗證 PrizeBoxController.getMyPrizeBox() 回應結構」
任務（T009）：「驗證 PrizeBoxItemRes DTO 欄位完整性」

# T007-T009 完成後進行：
任務（T010）：「撰寫 PrizeBoxFlowIntegrationTest 整合測試」
```

### 使用者故事 2 平行執行

```bash
# 優先進行（阻擋其他任務）：
任務（T011）：「確認/補實作 shipPrizes() 擁有者驗證」
（T005 已在第二階段完成）

# T011 完成後可同時進行：
任務（T012）：「驗證 PrizeBoxShipReq.java 合約欄位」
任務（T013）：「驗證 createOrdersFromPrizeBox() 非正規化欄位插入」
任務（T015）：「撰寫 PrizeBoxServiceTest 單元測試」
任務（T016）：「撰寫 OrderServiceTest 單元測試」

# T011-T016 全部完成後進行：
任務（T014）：「驗證宅配/超商取貨驗證邏輯」
任務（T017）：「撰寫 OrderCancelIntegrationTest 整合測試」
```

### 使用者故事 4 平行執行

```bash
# 可同時進行：
任務（T020）：「驗證 getMyOrders() 使用者隔離及回應結構」
任務（T021）：「驗證 getOrderDetail() 完整回應及錯誤處理」
任務（T022）：「驗證 OrderDetailRes / OrderItemRes DTO 欄位完整性」

# T020-T022 全部完成後進行：
任務（T023）：「撰寫 OrderFlowIntegrationTest 整合測試」
```

---

## 實作策略

### MVP 優先（使用者故事 1 + 2）

1. 完成**第一階段**：確認設置
2. 完成**第二階段**：基礎驗證（重點：T005 取消返回邏輯）
3. 完成**第三階段（US1）**：抽獎 → 獎品盒流程
4. 完成**第四階段（US2）**：出貨 → 訂單建立 → 取消返回流程
5. **停止並驗收**：執行 `quickstart.md` 步驟 1–5
6. 部署核心流程

### 增量交付

1. Setup + Foundational → 基礎就緒
2. US1（T007–T010）→ 抽獎進盒流程就緒 → 可部署
3. US2（T011–T017）→ 出貨與取消流程就緒 → 可部署（**核心 MVP**）
4. US3（T018–T019）→ 連續抽獎支援就緒 → 可部署
5. US4（T020–T023）→ 訂單查詢就緒 → 完整功能上線
6. 最終階段（T024–T027）→ 端對端驗收 + 可選增強

### 並行團隊策略

多人開發時：

1. 全員完成 Setup + Foundational 階段（特別確認 **T005**）
2. 基礎完成後：
   - 開發者 A：US1 第三階段（T007–T010）
   - 開發者 B：US2 第四階段（T011–T017）
   - 開發者 C（稍後）：US3/US4 第五/六階段（T018–T023）
3. 各故事獨立完成後整合

---

## 任務摘要

| 階段 | 任務數 | 使用者故事 | 優先級 |
|------|--------|-----------|--------|
| 第一階段：設置 | 2（T001–T002） | — | 立即開始 |
| 第二階段：基礎 | 4（T003–T006） | — | P0（阻擋全部） |
| 第三階段 | 4（T007–T010） | US1 抽獎 → 獎品盒 | P1 🎯 MVP |
| 第四階段 | 7（T011–T017） | US2 出貨 → 訂單 | P1 🎯 MVP |
| 第五階段 | 2（T018–T019） | US3 連續抽獎導航 | P2 |
| 第六階段 | 4（T020–T023） | US4 訂單查詢 | P2 |
| 最終階段：收尾 | 4（T024–T027） | — | 收尾 |
| **合計** | **27 個任務** | | |

---

## 注意事項

- `[P]` 任務 = 不同檔案、無依賴，可平行執行
- `[Story]` 標籤將任務對應至特定使用者故事，確保可追溯性
- **術語對照**：spec 中的 `AVAILABLE` = 程式碼中的 `IN_BOX`（詳見 research.md 第 1 節）
- **T005 為最高優先缺口任務** — 取消返回邏輯影響 US2 測試及整體資料一致性
- 所有端點均位於 `/api/**` 下，受 `ApiJwtAuthenticationFilter` 保護，無需修改安全設定（research.md 第 9 節）
- MyBatis 狀態轉換使用 `updateByPrimaryKeySelective()`（MBG 生成方法），避免意外覆寫不相關欄位（research.md 第 8 節）
- **效能目標**：獎品盒清單 p95 < 500ms；多店家訂單建立 < 2s（plan.md）
- 每完成一個任務或邏輯群組後提交 commit
- 在各階段**檢查點**驗收後再繼續下一階段
