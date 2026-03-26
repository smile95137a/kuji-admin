---

description: "002-express-shipping 功能任務清單"
---

# 任務清單：物流與出貨管理 (Express Shipping)

**輸入**：設計文件來自 `/specs/002-express-shipping/`  
**前置條件**：plan.md（已載入）、spec.md（已載入）、data-model.md（已載入）、research.md（已載入）、contracts/（已載入）、quickstart.md（已載入）

**測試**：測試任務已包含。research.md 第 7 節明確定義測試策略（MockMvc 控制器切片測試 + Mockito 服務單元測試），quickstart.md 亦列出具體測試情境。

**組織方式**：任務依使用者故事分組，每個故事可獨立實作與測試。

---

## 格式：`[ID] [P?] [Story?] 描述`

- **[P]**：可平行執行（不同檔案、無未完成相依）
- **[Story]**：對應 spec.md 中的使用者故事（US1–US4）
- 所有任務含確切檔案路徑

## 路徑規範

本功能為**單一 Spring Boot 專案**，路徑如下：

- 主程式碼：`src/main/java/com/group/admin/`
- 測試程式碼：`src/test/java/com/group/admin/`

---

## 第 1 階段：初始化設定

**目的**：確認開發環境就緒、現有骨架存在，並確保後續任務的起始點一致。

> **背景**：根據 research.md，Order 實體、列舉、服務方法與管理員控制器均已可投入生產。本階段不新增任何程式碼，僅驗證建構狀態。

- [ ] T001 確認專案能正常建構（執行 `mvn clean package -DskipTests` 於儲存庫根目錄，確認 BUILD SUCCESS）並確認 `OrderControllerTest.java` 與 `AdminOrderControllerTest.java` 空骨架存在於 `src/test/java/com/group/admin/controller/`

---

## 第 2 階段：基礎建設（阻塞性前置作業）

**目的**：新增第三、四階段所有任務共用的新 DTO 與介面方法宣告。

**⚠️ 重要**：US1 的服務實作與控制器端點均相依於本階段，必須優先完成。

- [ ] T002 建立 `ShipInfoReq` DTO 類別（含 `@NotBlank private String shippingMethod`、`recipientName`、`recipientPhone`、`recipientAddress`、`storeCode`、`storeName`、`storeAddress`、`remark` 欄位，均帶 `@Data` 與 Lombok 注解）於 `src/main/java/com/group/admin/req/order/ShipInfoReq.java`
- [ ] T003 在 `OrderService.java` 介面新增方法簽名 `void submitShippingInfo(String orderId, ShipInfoReq req, String userId)` 於 `src/main/java/com/group/admin/service/OrderService.java`

**檢查點**：DTO 與介面方法宣告就緒 — 第 3 階段以後的任務可開始進行

---

## 第 3 階段：使用者故事 1 — 玩家提交出貨資訊（優先級：P1）🎯 MVP

**目標**：玩家可在獎品盒訂單仍處於 PENDING 狀態時，提交或更新宅配到府 / 超商取貨的收件人資訊；狀態機守衛確保訂單進入 PREPARING 後無法再修改。

**獨立測試**：玩家對 PENDING 訂單呼叫 `POST /order/{id}/shipping-info`（宅配與超商各一筆），確認 200 回應且資料庫欄位正確更新；對 PREPARING 訂單呼叫同一端點，確認收到 409。

### US1 測試（先撰寫測試並確認失敗，再開始實作）

> **注意**：`OrderServiceTest.java` 為新建檔案；`OrderControllerTest.java` 為補充現有空白骨架。

- [ ] T004 [P] [US1] 建立 `OrderServiceTest.java` 並撰寫 Mockito 單元測試：狀態機完整正向路徑（PENDING→PREPARING→SHIPPED→COMPLETED），驗證每次轉換後 `OrderStatusLog` 有一筆新條目，於 `src/test/java/com/group/admin/service/OrderServiceTest.java`
- [ ] T005 [P] [US1] 在 `OrderServiceTest.java` 撰寫單元測試：非法逆向轉換（嘗試從 SHIPPED 呼叫 `/prepare` 返回 PENDING）應拋出 `BusinessException`，於 `src/test/java/com/group/admin/service/OrderServiceTest.java`
- [ ] T006 [P] [US1] 在 `OrderServiceTest.java` 撰寫單元測試：`submitShippingInfo()` 在 PREPARING 狀態時應拋出 `BusinessException("訂單已確認，無法修改出貨資訊")`，於 `src/test/java/com/group/admin/service/OrderServiceTest.java`
- [ ] T007 [P] [US1] 在 `OrderServiceTest.java` 撰寫單元測試：`submitShippingInfo()` 在他人訂單上應拋出 `BusinessException("無權限操作此訂單")`，於 `src/test/java/com/group/admin/service/OrderServiceTest.java`
- [ ] T008 [P] [US1] 在 `OrderControllerTest.java` 撰寫 MockMvc 控制器切片測試：`POST /order/{id}/shipping-info` 宅配到府正常路徑（`shippingMethod=HOME_DELIVERY` 含完整收件人資訊 → 200 `"出貨資訊已更新"`），於 `src/test/java/com/group/admin/controller/api/OrderControllerTest.java`
- [ ] T009 [P] [US1] 在 `OrderControllerTest.java` 撰寫 MockMvc 控制器切片測試：`POST /order/{id}/shipping-info` 超商取貨正常路徑（`shippingMethod=SEVEN_ELEVEN` 含 `storeCode`/`storeName` → 200），於 `src/test/java/com/group/admin/controller/api/OrderControllerTest.java`
- [ ] T010 [P] [US1] 在 `OrderControllerTest.java` 撰寫 MockMvc 控制器切片測試：HOME_DELIVERY 缺少 `recipientName` → 400 `"宅配需填入收件人姓名"`，於 `src/test/java/com/group/admin/controller/api/OrderControllerTest.java`
- [ ] T011 [P] [US1] 在 `OrderControllerTest.java` 撰寫 MockMvc 控制器切片測試：SEVEN_ELEVEN 缺少 `storeCode` → 400 `"超商取貨需填入分店代碼"`，於 `src/test/java/com/group/admin/controller/api/OrderControllerTest.java`
- [ ] T012 [P] [US1] 在 `OrderControllerTest.java` 撰寫 MockMvc 控制器切片測試：PREPARING 狀態提交出貨資訊被鎖定 → 409 `"訂單已確認，無法修改出貨資訊"`，於 `src/test/java/com/group/admin/controller/api/OrderControllerTest.java`
- [ ] T013 [P] [US1] 在 `OrderControllerTest.java` 撰寫 MockMvc 控制器切片測試：存取他人訂單 → 403 `"無權限操作此訂單"`，於 `src/test/java/com/group/admin/controller/api/OrderControllerTest.java`

### US1 實作

- [ ] T014 [US1] 在 `OrderServiceImpl.java` 實作 `submitShippingInfo()` 方法：依序執行訂單存在性守衛、所有權守衛（`order.userId == 已認證userId`）、PENDING 狀態守衛（非 PENDING 拋出例外），再依 `ShippingMethodEnum` 進行跨欄位條件式驗證（HOME_DELIVERY 驗 name/phone/address；超商取貨驗 storeCode/storeName），通過後呼叫 `orderMapper.updateByPrimaryKeySelective()` 更新出貨欄位及 `updatedAt`，於 `src/main/java/com/group/admin/service/impl/OrderServiceImpl.java`
- [ ] T015 [US1] 在 `OrderController.java` 新增 `@PostMapping("/{orderId}/shipping-info")` 端點（使用 `@Valid @RequestBody ShipInfoReq req`、從 `SecurityUtils.getCurrentApiUserId()` 取得 userId，呼叫 `orderService.submitShippingInfo()`，回傳 `Result.success("出貨資訊已更新")`），於 `src/main/java/com/group/admin/controller/api/OrderController.java`（依賴 T014）

**檢查點**：此時使用者故事 1 應可獨立運行並通過測試。執行 `mvn test -Dtest="OrderControllerTest,OrderServiceTest"` 驗證。

---

## 第 4 階段：使用者故事 2 — 店家管理出貨狀態（優先級：P1）

**目標**：為已實作的 `/prepare`、`/ship`、`/complete` 細粒度端點補充控制器切片測試，確保狀態機守衛、角色授權與 trackingNo 驗證均正確運作。

> **背景**：`AdminOrderController.java` 的所有端點均已實作；本階段僅補充空白的 `AdminOrderControllerTest.java`。

**獨立測試**：執行 `mvn test -Dtest="AdminOrderControllerTest"` 確認備貨、出貨、完成三個轉換路徑通過，並確認逆向轉換與無效輸入被正確拒絕。

### US2 測試

- [ ] T016 [P] [US2] 在 `AdminOrderControllerTest.java` 撰寫 MockMvc 控制器切片測試：`PUT /admin/orders/{id}/prepare` PENDING→PREPARING 正常路徑（STORE_OWNER 角色 → 200 `"訂單已更新為備貨中"`），於 `src/test/java/com/group/admin/controller/admin/AdminOrderControllerTest.java`
- [ ] T017 [P] [US2] 在 `AdminOrderControllerTest.java` 撰寫 MockMvc 控制器切片測試：`PUT /admin/orders/{id}/ship` PREPARING→SHIPPED 正常路徑（含 `trackingNo` → 200 `"訂單已出貨"`，確認 `shipped_at` 已設定），於 `src/test/java/com/group/admin/controller/admin/AdminOrderControllerTest.java`
- [ ] T018 [P] [US2] 在 `AdminOrderControllerTest.java` 撰寫 MockMvc 控制器切片測試：`PUT /admin/orders/{id}/complete` SHIPPED→COMPLETED 正常路徑（ADMIN 角色 → 200 `"訂單已完成"`，確認 `completed_at` 已設定），於 `src/test/java/com/group/admin/controller/admin/AdminOrderControllerTest.java`
- [ ] T019 [P] [US2] 在 `AdminOrderControllerTest.java` 撰寫 MockMvc 控制器切片測試：逆向轉換（對 SHIPPED 訂單呼叫 `/prepare` → 409 `"訂單狀態不允許此操作"`），於 `src/test/java/com/group/admin/controller/admin/AdminOrderControllerTest.java`
- [ ] T020 [P] [US2] 在 `AdminOrderControllerTest.java` 撰寫 MockMvc 控制器切片測試：`/ship` 缺少 `trackingNo` → 400 `"物流單號不可為空"`，於 `src/test/java/com/group/admin/controller/admin/AdminOrderControllerTest.java`
- [ ] T021 [P] [US2] 在 `AdminOrderControllerTest.java` 撰寫 MockMvc 控制器切片測試：無授權角色（USER 角色）呼叫 `/prepare` → 403，於 `src/test/java/com/group/admin/controller/admin/AdminOrderControllerTest.java`

**檢查點**：此時使用者故事 1 與 2 應均可獨立運行並通過測試。

---

## 第 5 階段：使用者故事 3 — 玩家查看訂單出貨狀態（優先級：P2）

**目標**：為已實作的 `GET /order/{id}` 與 `POST /order/list` 端點補充控制器切片測試，驗證所有權強制執行與出貨資訊欄位正確揭露。

> **背景**：兩個玩家訂單端點均已存在於 `OrderController.java`；本階段僅補充 `OrderControllerTest.java` 中的讀取端點測試。

**獨立測試**：執行 `mvn test -Dtest="OrderControllerTest"` 確認所有權隔離與出貨欄位正確性通過。

### US3 測試

- [ ] T022 [P] [US3] 在 `OrderControllerTest.java` 撰寫 MockMvc 控制器切片測試：`GET /order/{id}` 訂單所有者正常路徑（→ 200，回應含 `shippingMethod`、`shippingStatus`、`recipientName`、`trackingNo`、`items` 等欄位），於 `src/test/java/com/group/admin/controller/api/OrderControllerTest.java`
- [ ] T023 [P] [US3] 在 `OrderControllerTest.java` 撰寫 MockMvc 控制器切片測試：`GET /order/{id}` 非訂單所有者（其他使用者存取 → 403 `"無權限查看此訂單"`），於 `src/test/java/com/group/admin/controller/api/OrderControllerTest.java`
- [ ] T024 [P] [US3] 在 `OrderControllerTest.java` 撰寫 MockMvc 控制器切片測試：`POST /order/list` 使用者隔離性（伺服器端強制 `userId` 篩選，確認只回傳當前使用者的訂單，不含他人訂單），於 `src/test/java/com/group/admin/controller/api/OrderControllerTest.java`
- [ ] T025 [P] [US3] 在 `OrderControllerTest.java` 撰寫 MockMvc 控制器切片測試：`POST /order/list` 依狀態篩選（`condition.status=SHIPPED`，回應清單中所有訂單狀態均為 SHIPPED），於 `src/test/java/com/group/admin/controller/api/OrderControllerTest.java`

**檢查點**：此時使用者故事 1、2、3 應均可獨立運行並通過測試。

---

## 第 6 階段：使用者故事 4 — 後台在出貨前取消訂單（優先級：P3）

**目標**：為已實作的 `PUT /admin/orders/{id}/cancel` 端點補充控制器切片測試，驗證 ADMIN 角色限制（FR-008）、可取消狀態矩陣（FR-007）及取消原因必填驗證。

> **背景**：取消端點已實作於 `AdminOrderController.java`，帶有 `@PreAuthorize("hasRole('ADMIN')")`；本階段僅補充 `AdminOrderControllerTest.java` 中的取消相關測試。

**獨立測試**：執行 `mvn test -Dtest="AdminOrderControllerTest"` 確認取消正常路徑、守衛拒絕與角色限制均通過。

### US4 測試

- [ ] T026 [P] [US4] 在 `AdminOrderControllerTest.java` 撰寫 MockMvc 控制器切片測試：ADMIN 取消 PENDING 訂單（含 `reason` → 200 `"訂單已取消"`，確認 `cancelled_at`、`cancelled_by`、`cancel_reason` 已設定），於 `src/test/java/com/group/admin/controller/admin/AdminOrderControllerTest.java`
- [ ] T027 [P] [US4] 在 `AdminOrderControllerTest.java` 撰寫 MockMvc 控制器切片測試：ADMIN 取消 PREPARING 訂單（含 `reason` → 200），於 `src/test/java/com/group/admin/controller/admin/AdminOrderControllerTest.java`
- [ ] T028 [P] [US4] 在 `AdminOrderControllerTest.java` 撰寫 MockMvc 控制器切片測試：ADMIN 嘗試取消 SHIPPED 訂單（→ 409 `"訂單已出貨，無法取消"`），於 `src/test/java/com/group/admin/controller/admin/AdminOrderControllerTest.java`
- [ ] T029 [P] [US4] 在 `AdminOrderControllerTest.java` 撰寫 MockMvc 控制器切片測試：STORE_OWNER 角色嘗試取消（→ 403 Access Denied），於 `src/test/java/com/group/admin/controller/admin/AdminOrderControllerTest.java`
- [ ] T030 [P] [US4] 在 `AdminOrderControllerTest.java` 撰寫 MockMvc 控制器切片測試：缺少取消原因（空 `reason` → 400 `"取消原因不可為空"`），於 `src/test/java/com/group/admin/controller/admin/AdminOrderControllerTest.java`

**檢查點**：所有四個使用者故事應均可獨立運行並通過測試。

---

## 最終階段：收尾與跨功能關注點

**目的**：整體驗收測試、端對端流程驗證，以及回應 DTO 欄位命名一致性確認。

- [ ] T031 [P] 執行訂單相關完整測試套件（`mvn test -Dtest="OrderController*,AdminOrderController*,OrderService*"`）確認所有測試通過，無回歸錯誤
- [ ] T032 [P] 確認 `OrderDetailRes.java` 回應欄位命名符合 `contracts/player-get-orders.md` 規格（超商分店名稱 JSON key 為 `storeName`；KUJI 店家名稱 JSON key 與超商分店不衝突），於 `src/main/java/com/group/admin/res/order/OrderDetailRes.java`
- [ ] T033 依 `quickstart.md` 執行 Postman 端對端驗收清單（5 個情境：玩家提交出貨資訊 → 管理員備貨 → 管理員出貨 → 玩家查詢狀態 → 管理員取消），確認 quickstart.md 各情境均回傳預期 HTTP 狀態碼與訊息

---

## 相依性與執行順序

### 階段相依性

- **第 1 階段（設定）**：無前置相依 — 可立即開始
- **第 2 階段（基礎建設）**：依賴第 1 階段完成 — **阻塞 US1 的服務實作與控制器端點**
- **第 3 階段（US1）**：依賴第 2 階段完成（需要 `ShipInfoReq` DTO 與介面方法宣告）
- **第 4 階段（US2）**：依賴第 1 階段即可開始（`AdminOrderController` 已實作，只需補測試）
- **第 5 階段（US3）**：依賴第 1 階段即可開始（`OrderController` 讀取端點已實作，只需補測試）
- **第 6 階段（US4）**：依賴第 1 階段即可開始（`cancel` 端點已實作，只需補測試）
- **最終階段（收尾）**：依賴所有期望完成的使用者故事階段

### 使用者故事相依性

- **US1（P1）** → 依賴第 2 階段（需要 T002、T003）
- **US2（P1）** → 僅依賴第 1 階段；可與 US1 第 4 階段平行進行
- **US3（P2）** → 僅依賴第 1 階段；可與 US1、US2 平行進行
- **US4（P3）** → 僅依賴第 1 階段；可與所有其他故事平行進行

### 故事內部執行順序

- 測試必須先撰寫並確認**失敗**，再開始實作（US1 T004–T013 在 T014–T015 之前）
- 服務層（T014）先於控制器層（T015）
- US2、US3、US4 各階段中，所有 `[P]` 標記的測試任務可同時撰寫

### 平行機會

- 第 1 階段（T001）與第 2 階段（T002、T003）完成後：
  - US1 的測試（T004–T013）可全部平行撰寫
  - US2 的測試（T016–T021）可平行撰寫（不依賴 US1）
  - US3 的測試（T022–T025）可平行撰寫（不依賴 US1）
  - US4 的測試（T026–T030）可平行撰寫（不依賴 US1）
- US1 的測試（T004–T013）全部標記 `[P]`，可同步交給多個代理人執行

---

## 平行執行範例

### US1 測試撰寫（T004–T013 全部可平行）

```bash
# 同時啟動以下任務：
任務：「在 OrderServiceTest.java 撰寫狀態機正向路徑測試」
任務：「在 OrderServiceTest.java 撰寫非法逆向轉換測試」
任務：「在 OrderServiceTest.java 撰寫 PREPARING 狀態提交鎖定測試」
任務：「在 OrderServiceTest.java 撰寫他人訂單所有權測試」
任務：「在 OrderControllerTest.java 撰寫 HOME_DELIVERY 正常路徑測試」
任務：「在 OrderControllerTest.java 撰寫 SEVEN_ELEVEN 正常路徑測試」
任務：「在 OrderControllerTest.java 撰寫缺少 recipientName 驗證測試」
任務：「在 OrderControllerTest.java 撰寫缺少 storeCode 驗證測試」
任務：「在 OrderControllerTest.java 撰寫 PREPARING 狀態鎖定測試」
任務：「在 OrderControllerTest.java 撰寫非所有者存取測試」
```

### US2–US4 測試撰寫（可與 US1 實作同步進行）

```bash
# 第 2 階段（T002–T003）完成後，可同步啟動：
任務：「補充 AdminOrderControllerTest.java 的 US2 測試（T016–T021）」
任務：「補充 OrderControllerTest.java 的 US3 測試（T022–T025）」
任務：「補充 AdminOrderControllerTest.java 的 US4 測試（T026–T030）」
```

---

## 實作策略

### MVP 優先（僅使用者故事 1）

1. 完成第 1 階段：設定（T001）
2. 完成第 2 階段：基礎建設（T002–T003）—— **關鍵阻塞點**
3. 完成第 3 階段：US1（T004–T015）
4. **停止並驗證**：執行 `mvn test -Dtest="OrderControllerTest,OrderServiceTest"` 確認 US1 獨立可用
5. 部署 / 展示 MVP

### 逐步交付

1. 完成設定 + 基礎建設 → 基礎就緒
2. 加入 US1 → 獨立測試 → 部署（MVP！玩家可提交出貨資訊）
3. 加入 US2 → 獨立測試 → 部署（店家可推進出貨狀態）
4. 加入 US3 → 獨立測試 → 部署（玩家可查看出貨狀態）
5. 加入 US4 → 獨立測試 → 部署（管理員可在出貨前取消）
6. 每個故事均獨立增加價值，不影響前面故事

### 多人平行策略

雙開發者分工建議：

1. 雙方共同完成第 1、2 階段（設定 + 基礎建設）
2. 基礎建設完成後：
   - **開發者 A**：US1（T004–T015）— 新 DTO + 服務方法 + 控制器端點
   - **開發者 B**：US2 + US3 + US4 測試（T016–T030）— 補充現有空白測試骨架
3. 各自完成後執行收尾階段

---

## 備註

- `[P]` 任務 = 不同檔案，無未完成前置相依
- `[Story]` 標籤將任務對應至特定使用者故事以供追溯
- 每個使用者故事均可獨立完成與測試
- US2、US3、US4 的核心邏輯已存在 — 本次僅補充測試覆蓋
- **無資料庫變更** — `Order` 與 `OrderStatusLog` 的所有欄位均已存在（研究階段確認）
- 地址預填（FR-004）為客戶端責任，使用 `GET /user/me` 回傳的 `defaultAddress` 資料 — 後端無需額外任務
- 超商代碼外部 API 驗證延後至 v2 — v1 僅做非空白檢查
- 每完成一個任務或邏輯群組後提交一次 commit
- 在任意檢查點停下來，單獨驗證該故事是否正確運作
- 避免：模糊任務、同一檔案衝突、破壞故事獨立性的跨故事相依
