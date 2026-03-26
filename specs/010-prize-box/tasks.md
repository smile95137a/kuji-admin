# Tasks: 獎品盒 (Prize Box)

**輸入**：設計文件來自 `/specs/010-prize-box/`  
**先決條件**：plan.md ✅ | spec.md ✅ | research.md ✅ | data-model.md ✅ | contracts/ ✅ | quickstart.md ✅  
**分支**：`010-prize-box` | **日期**：2026-03-22

**背景**：本功能已有初步骨架（Controller、Service、Entity 均存在），任務聚焦於修正已知缺陷、補充缺失欄位與端點，以及整合 UserAddress。不需從零新建架構。

**測試**：依 quickstart.md 提供的測試案例，為核心業務邏輯補充 JUnit 5 單元測試。

**組織方式**：任務依使用者故事分組，各故事可獨立實作與測試。

---

## 格式說明：`[ID] [P?] [Story?] 描述`

- **[P]**：可平行執行（不同檔案，無相互依賴）
- **[Story]**：所屬使用者故事（US1、US2、US3、US4）
- 描述中包含明確的檔案路徑

---

## Phase 1：設置（基礎環境確認）

**目的**：確認資料庫結構與 Entity 欄位，為所有後續實作奠定基礎

- [ ] T001 確認 `prize_box` 資料表結構，並執行補充欄位 DDL（若 `is_shippable` 或 `updated_at` 欄位不存在，則執行 data-model.md 中的 ALTER TABLE 語句）
- [ ] T002 [P] 確認 `src/main/java/com/group/admin/entity/PrizeBox.java` 已含 `isShippable` (Byte) 欄位；若缺失則補充宣告（對應 DB 欄位 `is_shippable TINYINT DEFAULT 1`）

---

## Phase 2：基礎修正（阻斷性前置條件）

**目的**：修正編譯錯誤與術語不一致，此階段完成前所有使用者故事均無法進行

**⚠️ 重要**：T003 為編譯阻斷錯誤，必須最優先修正

- [ ] T003 修正編譯錯誤 — 於 `src/main/java/com/group/admin/req/prizebox/PrizeBoxShipReq.java` import 區塊補上 `import jakarta.validation.constraints.NotBlank;`
- [ ] T004 [P] 術語正名 — 更新 `src/main/java/com/group/admin/enums/PrizeBoxStatusEnum.java` 中 IN_BOX 的中文名稱由 `"在賞品盒中"` 改為 `"在獎品盒中"`
- [ ] T005 術語正名 — 全域搜尋替換 `src/main/java/com/group/admin/` 下所有 JavaDoc 與日誌字串中的「賞品盒」為「獎品盒」（API 路徑 `/prize-box` 與 DB 表名 `prize_box` 保持不變）

**檢查點**：編譯通過、術語一致後，各使用者故事可開始平行實作

---

## Phase 3：使用者故事 1 — 玩家查看獎品盒內容（優先級：P1）🎯 MVP

**目標**：玩家開啟獎品盒頁面，以店家分組顯示所有 IN\_BOX 獎品，含正確的 isRecyclable/isShippable/prizeValue 旗標；同時提供完整歷史查詢端點

**獨立測試**：玩家完成跨兩家店家的多筆抽獎，呼叫 `GET /api/prize-box`，確認：(1) 按店家正確分組、(2) recycleBonus=0 的獎品 `isRecyclable: false`、(3) 正確顯示 `prizeValue`、(4) 呼叫 `GET /api/prize-box/history` 回傳含已出貨/已回收記錄的分頁結果

### US1 實作

- [ ] T006 [P] [US1] 確認或建立通用分頁包裝類別至 `src/main/java/com/group/admin/res/PageResult.java`（欄位：total Integer、page Integer、size Integer、items List\<T\>）；若專案已有同功能類別則直接複用
- [ ] T007 [P] [US1] 新增欄位至 `src/main/java/com/group/admin/res/prizebox/PrizeBoxItemRes.java`：`isShippable` (Boolean)、`prizeValue` (Long)、`shippedAt` (LocalDateTime)、`recycledAt` (LocalDateTime)，附上繁體中文 JavaDoc 說明
- [ ] T008 [US1] 修正 `src/main/java/com/group/admin/service/impl/PrizeBoxServiceImpl.java` 中 `convertToItemRes` 方法 — 將 `.isRecyclable(true)` 改為 `.isRecyclable(prizeBox.getRecycleBonus() != null && prizeBox.getRecycleBonus() > 0)`（修正 R-003 硬編碼錯誤）
- [ ] T009 [US1] 更新 `src/main/java/com/group/admin/service/impl/PrizeBoxServiceImpl.java` 中 `convertToItemRes` 方法 — 填入新增欄位：`.isShippable(prizeBox.getIsShippable() == null || prizeBox.getIsShippable() != 0)`、`.prizeValue(prize != null ? prize.getPrizeValue() : null)`、`.shippedAt(prizeBox.getShippedAt())`、`.recycledAt(prizeBox.getRecycledAt())`（依賴 T007、T008）
- [ ] T010 [US1] 於 `src/main/java/com/group/admin/service/PrizeBoxService.java` 新增方法簽名：`PageResult<PrizeBoxItemRes> getPrizeBoxHistory(String userId, String status, int page, int size)` — 附完整 JavaDoc（依賴 T006）
- [ ] T011 [US1] 於 `src/main/java/com/group/admin/service/impl/PrizeBoxServiceImpl.java` 實作 `getPrizeBoxHistory` — 使用 MyBatis PrizeBoxExample 查詢（無 status 時查全部狀態；有 status 時加 `andStatusEqualTo`）、排序 `created_at DESC`、LIMIT/OFFSET 分頁、總筆數查詢（依賴 T010）
- [ ] T012 [US1] 於 `src/main/java/com/group/admin/controller/api/PrizeBoxController.java` 新增 `GET /history` 端點 — `@GetMapping("/history")`、`@RequestParam(required = false) String status`、`@RequestParam(defaultValue = "1") int page`、`@RequestParam(defaultValue = "20") int size`、回傳 `ResponseEntity<PageResult<PrizeBoxItemRes>>`（依賴 T011、T012 契約）

**檢查點**：此時 US1 應完整可用並可獨立測試 — GET /prize-box 回傳正確欄位，GET /prize-box/history 回傳分頁歷史

---

## Phase 4：使用者故事 2 — 玩家出貨選取的獎品（優先級：P1）

**目標**：玩家選取獎品填寫地址後，系統原子性地移除獎品並依店家自動拆單建立訂單；不可出貨的獎品被正確拒絕

**獨立測試**：玩家選取來自 2 家店家的各 1 件 isShippable=1 獎品，呼叫 `POST /api/prize-box/ship`，確認：(1) 建立 2 筆獨立訂單、(2) 兩件獎品狀態變為 SHIPPED；接著嘗試出貨 isShippable=0 的獎品，確認回傳 400 `此獎品不可出貨`

### US2 實作

- [ ] T013 [US2] 於 `src/main/java/com/group/admin/service/impl/PrizeBoxServiceImpl.java` `shipPrizes` 方法驗證區塊新增 isShippable 檢查 — `if (prizeBox.getIsShippable() != null && prizeBox.getIsShippable() == 0) { throw new BusinessException("此獎品不可出貨：" + prizeBoxId); }`（依賴 T002）
- [ ] T014 [P] [US2] 確認 `src/main/java/com/group/admin/service/impl/PrizeBoxServiceImpl.java` `shipPrizes` 中按 `storeId` 自動拆單邏輯正確（FR-004），並補充已停用店家驗證：`if ("DISABLED".equals(store.getStatus())) { throw new BusinessException("店家已停用，無法建立訂單：" + storeId); }`

**檢查點**：此時 US1 與 US2 均完整可用並可獨立測試

---

## Phase 5：使用者故事 3 — 玩家回收獎品換取 Bonus 點數（優先級：P2）

**目標**：玩家可回收 recycleBonus > 0 的獎品換取 Bonus 點數，不可回收的獎品被正確拒絕，回收結果回傳 totalBonus 與 recycledCount

**獨立測試**：玩家選取 recycleBonus=50 與 recycleBonus=100 的 2 件獎品，呼叫 `POST /api/prize-box/recycle`，確認：(1) 回傳 `{ totalBonus: 150, recycledCount: 2 }`、(2) Bonus 餘額增加 150；再嘗試回收 recycleBonus=0 的獎品，確認回傳 400 `此獎品不可回收`

### US3 實作

- [ ] T015 [P] [US3] 建立 `src/main/java/com/group/admin/res/prizebox/RecycleResultRes.java` 回應 DTO（Lombok @Data @Builder，欄位：`totalBonus` Long、`recycledCount` Integer，附繁體中文 JavaDoc）
- [ ] T016 [US3] 修正 `src/main/java/com/group/admin/service/impl/PrizeBoxServiceImpl.java` `recyclePrizes` 方法 — 在 status 驗證後加入：`if (prizeBox.getRecycleBonus() == null || prizeBox.getRecycleBonus() <= 0) { throw new BusinessException("此獎品不可回收：" + prizeBoxId); }`；累計 totalBonus 並於 WalletService.addBonus 後回傳（依賴 T015）
- [ ] T017 [US3] 更新 `src/main/java/com/group/admin/controller/api/PrizeBoxController.java` recycle 端點回傳型別 — 由 `void`/204 改為 `ResponseEntity<RecycleResultRes>`（totalBonus、recycledCount），符合 POST_prize-box_recycle.md 契約（依賴 T015、T016）

**檢查點**：此時 US1、US2、US3 均完整可用並可獨立測試

---

## Phase 6：使用者故事 4 — 玩家使用已儲存的收件地址（優先級：P3）

**目標**：玩家出貨時可傳入 `userAddressId` 直接使用儲存的地址，免除重複輸入

**獨立測試**：玩家帳號已有一筆 UserAddress，呼叫 `POST /api/prize-box/ship` 傳入 `userAddressId`（不傳 recipientName/Phone/Address），確認訂單以該地址的 recipientName、recipientPhone、city+district+address 建立

### US4 實作

- [ ] T018 [P] [US4] 新增 `userAddressId` 選填欄位（`private String userAddressId;`）至 `src/main/java/com/group/admin/req/prizebox/PrizeBoxShipReq.java`，附 JavaDoc 說明「已儲存地址 ID（選填，有值時優先於請求欄位）」
- [ ] T019 [US4] 於 `src/main/java/com/group/admin/service/impl/PrizeBoxServiceImpl.java` 建構子注入 `UserAddressMapper userAddressMapper`（依賴 T018）
- [ ] T020 [US4] 實作 `src/main/java/com/group/admin/service/impl/PrizeBoxServiceImpl.java` `shipPrizes` 地址解析優先順序邏輯 — (1) 若 `req.getUserAddressId()` 非空則查詢 `UserAddressMapper.selectByPrimaryKey`，驗證 `savedAddress.getUserId().equals(userId)` 後填入 recipientName/Phone 及 `city+district+address`；(2) 否則沿用現有邏輯（依賴 T018、T019）

**檢查點**：所有 4 個使用者故事均完整可用

---

## Phase 7：品質保證與收尾

**目的**：補充核心業務邏輯單元測試，執行全流程驗證

- [ ] T021 [P] 建立 `src/test/java/com/group/admin/service/PrizeBoxServiceTest.java`，撰寫以下 5 項 JUnit 5 + Mockito 單元測試：(1) `recyclePrizes_應於不可回收時拋出例外`（recycleBonus=0）、(2) `shipPrizes_應依店家拆單`（2 家店家 → 2 筆訂單）、(3) `shipPrizes_應於不可出貨時拋出例外`（isShippable=0）、(4) `getPrizeBox_應僅回傳IN_BOX項目`、(5) `getPrizeBoxHistory_應回傳全狀態記錄`（null status 時）
- [ ] T022 執行 quickstart.md 全流程驗證腳本（cURL 測試），確認 GET /prize-box、POST /prize-box/ship、POST /prize-box/recycle、GET /prize-box/history 四個端點回應均符合 contracts/ 中的 API 契約規格

---

## 依賴關係與執行順序

### 階段依賴

```
Phase 1（設置）       → 無依賴，立即開始
Phase 2（基礎修正）   → 依賴 Phase 1 完成 ⚠️ 阻斷所有使用者故事
Phase 3（US1 P1）     → 依賴 Phase 2 完成
Phase 4（US2 P1）     → 依賴 Phase 2 完成（T002 entity 欄位確認）
Phase 5（US3 P2）     → 依賴 Phase 2 完成
Phase 6（US4 P3）     → 依賴 Phase 4 完成（shipPrizes 邏輯）
Phase 7（收尾）       → 依賴 Phase 3–6 完成
```

### 使用者故事依賴

| 使用者故事 | 優先級 | 依賴 | 備註 |
|-----------|--------|------|------|
| US1（查看獎品盒）| P1 | Phase 2 | MVP 最小範圍 |
| US2（出貨獎品） | P1 | Phase 2 + T002 | 可與 US1 平行開發 |
| US3（回收換 Bonus）| P2 | Phase 2 | 可與 US1/US2 平行開發 |
| US4（已儲存地址）| P3 | US2（shipPrizes）| 需在 shipPrizes 基礎上擴充 |

### 任務內部依賴

```
T003 → （任何 US 開始前必須完成）
T007 → T009 → T011 → T012 （US1 串行）
T006 → T010 → T011 （PageResult → Service 簽名 → 實作）
T008 → T009 （isRecyclable 修正 → convertToItemRes 完整更新）
T002 → T013 （Entity 欄位確認 → isShippable 驗證）
T015 → T016 → T017 （RecycleResultRes DTO → Service → Controller）
T018 → T019 → T020 （欄位 → 注入 → 邏輯）
```

---

## 平行執行範例

### Phase 2（基礎修正）可完全平行執行

```bash
# 三個工程師可同時處理：
Task: "T003 修正 PrizeBoxShipReq.java @NotBlank import"
Task: "T004 更新 PrizeBoxStatusEnum.java 術語"
Task: "T005 全域搜尋替換 JavaDoc/日誌中的賞品盒"
```

### US1 與 US2 可在 Phase 2 完成後平行執行

```bash
# 工程師 A 處理 US1：
Task: "T006 建立 PageResult.java"  + "T007 更新 PrizeBoxItemRes.java"  (平行)
→ Task: "T008 修正 convertToItemRes isRecyclable"
→ Task: "T009 更新 convertToItemRes 新欄位填入"
→ Task: "T010 新增 getPrizeBoxHistory 方法簽名"
→ Task: "T011 實作 getPrizeBoxHistory"
→ Task: "T012 新增 GET /history 端點"

# 工程師 B 處理 US2（同時）：
Task: "T013 新增 isShippable 驗證至 shipPrizes"
Task: "T014 確認自動拆單邏輯與停用店家驗證"  (平行)
```

### US3 可與 US1/US2 平行執行（Phase 2 完成後）

```bash
Task: "T015 建立 RecycleResultRes.java"  (立即開始)
→ Task: "T016 修正 recyclePrizes 驗證邏輯"
→ Task: "T017 更新 recycle 端點回傳型別"
```

---

## 實作策略

### MVP 優先（僅 US1 + US2）

1. 完成 Phase 1：資料庫與 Entity 確認
2. 完成 Phase 2：修正編譯錯誤與術語（**關鍵阻斷點**）
3. 完成 Phase 3：US1 查看獎品盒（含歷史端點）
4. 完成 Phase 4：US2 出貨功能
5. **暫停驗證**：執行 quickstart.md cURL 測試，確認 GET /prize-box 與 POST /prize-box/ship 正常運作
6. 依需求繼續 US3（P2）→ US4（P3）

### 逐步交付

1. Phase 1 + 2 → 編譯通過、術語統一
2. + Phase 3（US1） → 獎品盒查看功能完整（含 `GET /history`）
3. + Phase 4（US2） → 出貨功能完整，跨店自動拆單
4. + Phase 5（US3） → 回收功能完整，Bonus 正確入帳
5. + Phase 6（US4） → 地址整合完整，重複出貨更便利
6. + Phase 7 → 測試與全流程驗證

---

## 備註

- `[P]` 任務表示操作不同檔案且無相互依賴，可平行執行
- `[US?]` 標籤確保每個任務可追溯至對應使用者故事
- 所有任務皆有明確的檔案路徑，LLM 可直接執行而無需額外查詢
- T003（@NotBlank import）為阻斷性編譯錯誤，必須在任何功能實作前完成
- `isRecyclable` 邏輯修正（T008）是 US3 正確性的前提，但已列於 US1 階段以保持 DTO 一致性
- API 路徑 `/prize-box` 與 DB 表名 `prize_box` 在術語正名時**不應修改**
- 超商取貨 API 整合延後至未來版本；`storeCode`/`storeName`/`storeAddress` 欄位保留但不實作邏輯
