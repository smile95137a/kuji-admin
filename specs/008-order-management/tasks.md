# 任務清單：訂單管理 (Order Management)

**輸入**：設計文件來自 `/specs/008-order-management/`
**前置條件**：plan.md ✅ · spec.md ✅ · research.md ✅ · data-model.md ✅ · contracts/ ✅ · quickstart.md ✅

**格式**：`[ID] [P?] [US?] 描述，含確切檔案路徑`

- **[P]**：可平行執行（不同檔案，無相依）
- **[USn]**：任務所屬的使用者故事（US1–US4）
- **Setup / Foundational / Polish 階段**：不附使用者故事標籤

## 路徑慣例

```
src/main/java/com/group/admin/   ← 所有 Java 原始碼
src/main/resources/mapper/       ← MyBatis XML mapper 檔案
src/test/java/com/group/admin/   ← JUnit 5 測試
```

> **已存在，無需建立**：`entity/Order.java`、`entity/OrderItem.java`、`entity/OrderStatusLog.java`、`mapper/OrderMapper.java`（基礎方法）、`mapper/OrderItemMapper.java`、`mapper/OrderStatusLogMapper.java`、`mapper/OrderMapper.xml`、`mapper/OrderItemMapper.xml`、`mapper/OrderStatusLogMapper.xml`、`enums/OrderStatusEnum.java`

---

## Phase 1：Setup（環境確認）

**目的**：確認現有資料庫資料表與程式碼基礎設施就緒，再開始任何功能開發。

- [ ] T001 確認資料庫中三張資料表存在（執行 `SHOW TABLES LIKE '%order%'` 預期回傳 `order`、`order_item`、`order_status_log`；若不存在請套用 `doc/sql/prize-box-wallet-order-ddl.sql`）
- [ ] T002 [P] 確認 `entity/Order.java` 所有欄位與 data-model.md 的 `Order` 實體表格一致（含 `orderNumber`、`shippedAt`、`completedAt`、`cancelledAt`、`cancelledBy`、`cancelReason` 等欄位）
- [ ] T003 [P] 確認 `enums/OrderStatusEnum.java` 包含全部五個值（`PENDING`、`PREPARING`、`SHIPPED`、`COMPLETED`、`CANCELLED`）且 ordinal 順序為 0–4（此順序為狀態機轉換驗證依據）

**檢查點**：環境就緒，可進入基礎設施建構。

---

## Phase 2：Foundational（基礎設施）

**目的**：建立 DTO、Mapper 方法、Service 介面等所有使用者故事共用的核心元件。此階段完成前，任何使用者故事均不得實作。

**⚠️ 重要**：此階段完成後，所有使用者故事才可平行推進。

### 查詢條件

- [ ] T004 建立 `condition/OrderCondition.java`，包含欄位：`storeId`（String, nullable）、`userId`（String, nullable）、`status`（String, nullable）、`shippingMethod`（String, nullable）、`orderNo`（String, nullable）、`startDate`（LocalDateTime, nullable）、`endDate`（LocalDateTime, nullable）

### 請求 DTO

- [ ] T005 [P] 建立 `req/order/CreateOrderReq.java`，含欄位：`prizeBoxIds`（`List<String>`, `@NotEmpty`）、`shippingMethod`（`@NotBlank`）、`recipientName`（`@NotBlank`, `@Size(max=100)`）、`recipientPhone`（`@NotBlank`, `@Pattern(regexp="09\\d{8}|0\\d{1,2}-?\\d{6,8}")`）、`recipientAddress`（String, nullable）、`storeCode`（String, nullable）、`storeName`（String, nullable）、`storeAddress`（String, nullable）
- [ ] T006 [P] 建立 `req/order/UpdateOrderStatusReq.java`，含欄位：`targetStatus`（`@NotBlank`）、`trackingNo`（String, nullable）、`remark`（String, nullable, `@Size(max=500)`）
- [ ] T007 [P] 建立 `req/order/CancelOrderReq.java`，含欄位：`cancelReason`（String, nullable, `@Size(max=500)`）

### 回應 DTO

- [ ] T008 [P] 建立 `res/order/OrderItemRes.java`，含欄位：`id`、`prizeBoxId`、`prizeName`、`prizeImageUrl`、`prizeLevel`、`lotteryTitle`、`lotteryImageUrl`、`lotteryId`、`prizeId`（所有欄位均為 String/nullable）
- [ ] T009 [P] 建立 `res/order/StatusLogRes.java`，含欄位：`fromStatus`（nullable）、`fromStatusLabel`（nullable）、`toStatus`、`toStatusLabel`、`operatorId`（nullable，僅管理端回應填入）、`operatorType`、`remark`（nullable）、`createdAt`（LocalDateTime）
- [ ] T010 [P] 建立 `res/order/OrderRes.java`（列表檢視），含欄位：`id`、`orderNo`、`status`、`statusLabel`、`totalItems`、`shippingMethod`、`shippingMethodLabel`、`storeId`、`storeName`、`userId`、`playerName`、`playerPhone`、`recipientName`、`createdAt`（LocalDateTime）
- [ ] T011 [P] 建立 `res/order/OrderDetailRes.java`（詳情檢視），繼承或組合 `OrderRes` 所有欄位，額外包含：`recipientPhone`、`recipientAddress`、`storeCode`、`storeNamePickup`（超商名稱）、`storeAddressPickup`、`trackingNo`、`remark`、`updatedAt`、`shippedAt`、`completedAt`、`cancelledAt`、`cancelledBy`、`cancelReason`、`items`（`List<OrderItemRes>`）、`statusHistory`（`List<StatusLogRes>`）

### Mapper 擴充

- [ ] T012 在 `mapper/OrderMapper.java` 新增方法 `List<OrderRes> selectByCondition(QueryReq<OrderCondition> req)` 與 `long countByCondition(OrderCondition condition)`；在 `src/main/resources/mapper/OrderMapper.xml` 中實作動態 SQL `<select id="selectByCondition">`，依 `OrderCondition` 各欄位組合動態 WHERE 子句（storeId / userId / status / shippingMethod / orderNo / startDate / endDate），JOIN `user` 取玩家名稱與電話，JOIN `store` 取店家名稱，`ORDER BY created_at DESC` 並套用 `LIMIT #{offset}, #{pageSize}`
- [ ] T013 [P] 在 `mapper/OrderMapper.java` 新增方法 `OrderDetailRes selectDetailById(@Param("id") String id)`；在 `OrderMapper.xml` 中實作 `<select id="selectDetailById">`，以 `<collection>` 子查詢或 LEFT JOIN 帶入 `order_item` 列表與 `order_status_log` 歷程，同時 JOIN `user` 及 `store`，結果映射至 `OrderDetailRes`（含 `items` 與 `statusHistory` 嵌套集合）
- [ ] T014 [P] 在 `mapper/OrderMapper.java` 新增方法 `int insertOrder(Order order)`；在 `OrderMapper.xml` 中實作 `<insert id="insertOrder">` 帶入全部欄位（使用 `#{id}` UUID 主鍵，非自動遞增）
- [ ] T015 [P] 在 `mapper/OrderMapper.java` 新增方法 `int updateStatusAndTimestamps(Order order)`；在 `OrderMapper.xml` 中實作 `<update id="updateStatusAndTimestamps">`，動態更新 `status`、`shipped_at`、`completed_at`、`cancelled_at`、`cancelled_by`、`cancel_reason`、`tracking_no`、`remark`（使用 `<set>` 標籤，僅更新非 null 欄位）
- [ ] T016 [P] 在 `mapper/OrderItemMapper.java` 新增方法 `int insertOrderItem(OrderItem item)` 與 `int batchInsertOrderItems(List<OrderItem> items)`；在 `OrderItemMapper.xml` 中實作 `<insert id="batchInsertOrderItems">` 使用 `<foreach>` 批次插入
- [ ] T017 [P] 在 `mapper/OrderStatusLogMapper.java` 新增方法 `int insertLog(OrderStatusLog log)`；在 `OrderStatusLogMapper.xml` 中實作對應 `<insert id="insertLog">`（欄位：id / order_id / from_status / to_status / operator_id / operator_type / remark / created_at）
- [ ] T018 [P] 在 `mapper/PrizeBoxMapper.java` 新增方法 `int batchUpdateStatusByOrderId(@Param("orderId") String orderId, @Param("status") String status)`；在 `PrizeBoxMapper.xml` 中實作 `<update>`，當 `status='IN_BOX'` 時同時設 `order_id=NULL, shipped_at=NULL`，當 `status='SHIPPED'` 時設 `order_id=#{orderId}`（用於建立訂單與取消還原兩個場景）

### Service 介面與骨架

- [ ] T019 定義 `service/OrderService.java` 介面，包含以下方法簽名：`createOrdersFromPrizeBox(String userId, CreateOrderReq req): List<String>`、`getOrderList(QueryReq<OrderCondition> req, String callerUserId, String callerRole): PageResult<OrderRes>`、`getOrderById(String id, String callerUserId, String callerRole): OrderDetailRes`、`getPlayerOrderList(QueryReq<OrderCondition> req, String playerId): PageResult<OrderRes>`、`getPlayerOrderById(String orderId, String playerId): OrderDetailRes`、`updateOrderStatus(String id, UpdateOrderStatusReq req, String operatorId, String operatorType): OrderRes`、`cancelOrder(String id, CancelOrderReq req, String operatorId, String operatorType): OrderRes`
- [ ] T020 建立 `service/impl/OrderServiceImpl.java` 骨架，標記 `@Service`，注入 `OrderMapper`、`OrderItemMapper`、`OrderStatusLogMapper`、`PrizeBoxMapper`、`StoreMapper`、`AdminUserMapper`（或 `SecurityUtils`），各方法拋 `UnsupportedOperationException`（等待後續各階段填入實作）
- [ ] T021 在 Spring Security 設定檔（`config/SecurityConfig.java` 或 `config/WebSecurityConfig.java`）中新增路由規則：`/order/**` 需要 `USER` 角色；`/admin/orders/**` 需要 `ADMIN`、`STORE_OWNER` 或 `STORE_EDITOR` 角色；`PLAYER` 角色存取 `/admin/orders/**` 回傳 403

**檢查點**：基礎設施就緒——所有使用者故事可開始平行實作。

---

## Phase 3：使用者故事 1——店家負責人查看並管理訂單（P1）🎯 MVP

**目標**：店家負責人登入後可查看屬於自己店家的訂單列表與詳情，跨店存取被強制拒絕。

**獨立測試**：以 STORE_OWNER 身分登入，呼叫 `POST /admin/orders/list` 確認只返回自己店家的訂單；呼叫另一家店的訂單 ID 取得 `GET /admin/orders/{id}` 確認回傳 403。

### Phase 3 實作

- [ ] T022 [US1] 在 `service/impl/OrderServiceImpl.java` 實作 `getOrderList()` 方法：若 `callerRole` 為 `STORE_OWNER` 或 `STORE_EDITOR`，透過 `store_user` 中介表解析 `callerUserId → storeId`，並將 `storeId` 寫入 `OrderCondition`；若為 `ADMIN` 則不強制覆蓋 `storeId`；呼叫 `OrderMapper.selectByCondition` + `countByCondition` 組成 `PageResult<OrderRes>` 回傳
- [ ] T023 [US1] 在 `service/impl/OrderServiceImpl.java` 實作 `getOrderById()` 方法：呼叫 `OrderMapper.selectDetailById(id)`；若 `callerRole` 為 `STORE_OWNER`/`STORE_EDITOR`，驗證 `order.storeId` 必須等於呼叫者的店家 ID，不符者拋出 HTTP 403（自定義 `ForbiddenException`）；回傳 `OrderDetailRes`
- [ ] T024 [US1] 建立 `controller/admin/AdminOrderController.java`，標記 `@RestController`、`@RequestMapping("/admin/orders")`、`@PreAuthorize`（ADMIN/STORE_OWNER/STORE_EDITOR）；新增 `POST /admin/orders/list` 端點，接受 `@RequestBody QueryReq<OrderCondition>`，從 `SecurityUtils.getCurrentUserId()` 與 `SecurityUtils.getCurrentUserRole()` 取得呼叫者資訊，委派至 `OrderService.getOrderList()`，回傳 `ResponseEntity<PageResult<OrderRes>>`
- [ ] T025 [US1] 在 `controller/admin/AdminOrderController.java` 新增 `GET /admin/orders/{id}` 端點，接受 `@PathVariable String id`，委派至 `OrderService.getOrderById()`，回傳 `ResponseEntity<OrderDetailRes>`；403 例外由全域例外處理器（`GlobalExceptionHandler`）攔截並回傳標準錯誤格式 `{"code":"FORBIDDEN","message":"..."}`
- [ ] T026 [P] [US1] 在 `src/test/java/com/group/admin/service/OrderServiceTest.java` 撰寫店家隔離單元測試：(a) STORE_OWNER 查詢列表——`OrderCondition.storeId` 應被強制設為呼叫者的店家 ID；(b) STORE_OWNER 查詢他店訂單詳情——應拋出 403 例外；使用 Mockito 模擬 mapper 與 SecurityUtils
- [ ] T027 [P] [US1] 在 `src/test/java/com/group/admin/controller/AdminOrderControllerTest.java` 撰寫整合測試：(a) STORE_OWNER JWT 呼叫 `POST /admin/orders/list` 只取得自己店家資料；(b) STORE_OWNER 存取他店訂單 ID 回傳 HTTP 403；使用 `@SpringBootTest` + `MockMvc` + 測試用 JWT

**檢查點**：US1 完整可測試——店家負責人可獨立查看及管理自己的訂單。

---

## Phase 4：使用者故事 2——玩家從獎品盒建立出貨訂單（P1）

**目標**：玩家可從獎品盒選取獎品建立出貨訂單；跨店獎品自動拆單；獎品盒原子性扣除，部分失敗全部回滾。

**獨立測試**：玩家以 `POST /order/ship` 帶入兩個不同店家的獎品盒 ID，系統回傳兩筆訂單 ID；確認 `prize_box.status` 已更新為 `SHIPPED`；以失效的 `prizeBoxId` 重試確認回傳 409。

### Phase 4 實作

- [ ] T028 [US2] 在 `service/impl/OrderServiceImpl.java` 實作 `createOrdersFromPrizeBox()` 整體框架（標記 `@Transactional`）：(1) 載入所有 `prizeBoxIds` 對應的 `PrizeBox` 記錄；(2) 依 `storeId` 分組；(3) 對每個分組建立訂單並批次插入 `order_item`；(4) 更新 `prize_box.status→SHIPPED`；(5) 回傳已建立的訂單 ID 清單
- [ ] T029 [US2] 在 `createOrdersFromPrizeBox()` 中實作獎品盒驗證邏輯：所有 `prizeBoxId` 必須存在、`status=IN_BOX`、`userId` 等於呼叫玩家；任何不符者拋出自定義 `PrizeBoxUnavailableException`（HTTP 409），回應體含 `{"code":"PRIZE_BOX_UNAVAILABLE","unavailableBoxIds":[...]}`
- [ ] T030 [US2] 在 `createOrdersFromPrizeBox()` 中實作店家啟用驗證：對每個分組的 `storeId` 查詢 `store.status`；若為非 `ACTIVE` 則拋出 `StoreInactiveException`（HTTP 422），回應體含 `{"code":"STORE_INACTIVE","storeId":"..."}`
- [ ] T031 [US2] 在 `service/impl/OrderServiceImpl.java` 實作私有方法 `generateOrderNumber()`：格式 `ORD-{yyyyMMddHHmmss}-{6位大寫英數字亂數}`，範例 `ORD-20260322143000-AB1C2D`；使用 `LocalDateTime.now()` + `RandomStringUtils.randomAlphanumeric(6).toUpperCase()`
- [ ] T032 [US2] 在 `createOrdersFromPrizeBox()` 中完成每筆訂單的插入流程：(a) `generateOrderNumber()` 產生訂單編號；(b) `OrderMapper.insertOrder()` 插入訂單，`status=PENDING`；(c) `OrderItemMapper.batchInsertOrderItems()` 批次插入快照資料（prizeName、prizeImageUrl、prizeLevel、lotteryTitle、lotteryImageUrl）；(d) `PrizeBoxMapper.batchUpdateStatusByOrderId()` 更新獎品盒 `status=SHIPPED, order_id=?`；(e) `OrderStatusLogMapper.insertLog()` 記錄初始日誌（`fromStatus=null, toStatus=PENDING, operatorType=PLAYER`）
- [ ] T033 [US2] 建立 `controller/api/OrderController.java`，標記 `@RestController`、`@RequestMapping("/order")`、`@PreAuthorize("hasRole('USER')")`；新增 `POST /order/ship` 端點，接受 `@Valid @RequestBody CreateOrderReq`，呼叫 `OrderService.createOrdersFromPrizeBox(playerId, req)`，成功回傳 HTTP 201 含 `{"orderIds":[...],"orderCount":N,"message":"已建立 N 筆訂單（依店家拆單）"}`
- [ ] T034 [US2] 在 `controller/api/OrderController.java` 新增 `POST /order/list` 端點，接受 `@RequestBody QueryReq<OrderCondition>`，呼叫 `OrderService.getPlayerOrderList()`（伺服器端強制覆蓋 `condition.userId` 為當前玩家 ID），回傳 `PageResult<OrderRes>`
- [ ] T035 [US2] 在 `controller/api/OrderController.java` 新增 `GET /order/{orderId}` 端點，呼叫 `OrderService.getPlayerOrderById()`，確認回應中 `statusHistory` 的每筆日誌**不含** `operatorId`（玩家隱私保護）；若訂單 `userId` 不符則回傳 403
- [ ] T036 [P] [US2] 在 `service/impl/OrderServiceImpl.java` 實作 `getPlayerOrderList()` 與 `getPlayerOrderById()`：兩者均強制設定 `OrderCondition.userId = playerId`；`getPlayerOrderById` 額外驗證 `order.userId == playerId`，不符拋 403
- [ ] T037 [P] [US2] 在 `src/test/java/com/group/admin/service/OrderServiceTest.java` 補充建立訂單的測試：(a) 兩個不同店家的獎品盒 → 應產生兩筆訂單；(b) 獎品盒 `status=SHIPPED` → 應拋 409；(c) 任一步驟丟出 `RuntimeException` → 整個交易應回滾（Mockito verify 無 `insertOrder` 呼叫）

**檢查點**：US2 完整可測試——玩家可獨立建立出貨訂單並看到訂單歷程。

---

## Phase 5：使用者故事 3——店家負責人推進訂單出貨流程（P2）

**目標**：店家負責人可依序推進訂單（待處理→備貨中→已出貨→已完成）；後退被拒絕；取消（出貨前）還原獎品盒。

**獨立測試**：從 `PENDING` 依序推進至 `COMPLETED`；嘗試從 `SHIPPED` 退回 `PREPARING` 確認 409；取消 `PREPARING` 訂單確認獎品盒回到 `IN_BOX`；嘗試取消 `SHIPPED` 訂單確認 409。

### Phase 5 實作

- [ ] T038 [US3] 在 `service/impl/OrderServiceImpl.java` 實作 `updateOrderStatus()` 核心邏輯（標記 `@Transactional`）：(a) 查詢現有訂單（不存在拋 404）；(b) 若 `currentStatus == targetStatus`，冪等直接回傳 200（不插入日誌，不更新 DB）；(c) 驗證 `targetStatus.ordinal() == currentStatus.ordinal() + 1`，否則拋 `InvalidStateTransitionException`（HTTP 409，代碼 `INVALID_STATE_TRANSITION`）；(d) 呼叫 `OrderMapper.updateStatusAndTimestamps()`；(e) 呼叫 `OrderStatusLogMapper.insertLog()`；(f) 回傳更新後的 `OrderRes`
- [ ] T039 [US3] 在 `updateOrderStatus()` 中補充 SHIPPED 與 COMPLETED 的時間戳記邏輯：當 `targetStatus=SHIPPED` 時，設 `order.shippedAt=now()`、`order.trackingNo=req.trackingNo`；當 `targetStatus=COMPLETED` 時，設 `order.completedAt=now()`；時間戳記透過 `updateStatusAndTimestamps()` 持久化
- [ ] T040 [US3] 在 `updateOrderStatus()` 中實作呼叫者店家所有權驗證：若角色為 `STORE_OWNER`/`STORE_EDITOR`，`order.storeId` 須等於呼叫者的店家 ID；不符則拋 HTTP 403
- [ ] T041 [US3] 在 `controller/admin/AdminOrderController.java` 新增 `PUT /admin/orders/{id}/status` 端點，接受 `@Valid @RequestBody UpdateOrderStatusReq`，從 `SecurityUtils` 取得 `operatorId` 與 `operatorType`，委派至 `OrderService.updateOrderStatus()`，回傳 `ResponseEntity<OrderRes>`
- [ ] T042 [US3] 在 `service/impl/OrderServiceImpl.java` 實作 `cancelOrder()` 方法（標記 `@Transactional`）：(a) 查詢訂單（不存在拋 404）；(b) 驗證 `status ∈ {PENDING, PREPARING}`，否則拋 `CancelNotAllowedException`（HTTP 409，代碼 `CANCEL_NOT_ALLOWED`）；(c) 若角色為 `STORE_OWNER`/`STORE_EDITOR` 驗證店家所有權（不符拋 403）；(d) 更新訂單 `status=CANCELLED`、`cancelledAt=now()`、`cancelledBy`、`cancelReason`；(e) 還原獎品盒（步驟詳見 T043）；(f) 插入 `OrderStatusLog`；(g) 回傳更新後的 `OrderRes`（含 `itemsReturnedToBox` 數量）
- [ ] T043 [US3] 在 `cancelOrder()` 中實作獎品盒還原邏輯：透過 `OrderItemMapper` 查詢此訂單所有 `order_item` 的 `prizeBoxId` 清單；批次呼叫 `PrizeBoxMapper.batchUpdateStatusByOrderId(orderId, "IN_BOX")`，將 `prize_box.status→IN_BOX`、`order_id→NULL`、`shipped_at→NULL`（**不退還點數**——依 FR-004a）
- [ ] T044 [US3] 在 `controller/admin/AdminOrderController.java` 新增 `DELETE /admin/orders/{id}` 端點，接受 `@RequestBody(required=false) CancelOrderReq`（body 可選），委派至 `OrderService.cancelOrder()`，成功回傳 HTTP 200 含取消結果（含 `itemsReturnedToBox` 欄位）
- [ ] T045 [P] [US3] 在 `src/test/java/com/group/admin/service/OrderServiceTest.java` 補充狀態機單元測試：(a) `PENDING→PREPARING→SHIPPED→COMPLETED` 全流程逐步推進，驗證每步日誌插入；(b) `SHIPPED→PREPARING` 後退嘗試 → 應拋 409；(c) 相同狀態重複送出 → 應回傳 200 且不插入重複日誌（冪等性驗證）
- [ ] T046 [P] [US3] 在 `src/test/java/com/group/admin/service/OrderServiceTest.java` 補充取消邏輯單元測試：(a) `PENDING` 訂單取消 → `prize_box` 更新為 `IN_BOX`、日誌記錄、不退點數；(b) `PREPARING` 訂單取消 → 同上；(c) `SHIPPED` 訂單取消嘗試 → 應拋 409（`CANCEL_NOT_ALLOWED`）；(d) STORE_OWNER 取消他店訂單 → 應拋 403

**檢查點**：US3 完整可測試——完整出貨生命週期與取消流程均可獨立驗證。

---

## Phase 6：使用者故事 4——管理員跨店查詢訂單（P3）

**目標**：管理員可跨所有店家查詢訂單，支援店家、狀態、日期範圍等複合篩選。

**獨立測試**：以 ADMIN JWT 呼叫 `POST /admin/orders/list`（不帶 `storeId`）確認回傳所有店家訂單；加上 `storeId` 篩選確認只返回該店訂單；加上 `startDate`/`endDate` 確認日期範圍篩選正確。

### Phase 6 實作

- [ ] T047 [US4] 在 `service/impl/OrderServiceImpl.java` 的 `getOrderList()` 中補充 ADMIN 跨店邏輯：當角色為 `ADMIN` 且請求 `condition.storeId == null` 時，不套用任何店家篩選（查詢全平台訂單）；當 `condition.storeId` 有值時，依該值篩選；確認 `STORE_OWNER`/`STORE_EDITOR` 提供的 `storeId` 參數被**靜默覆蓋**（不使用用戶端值，始終用呼叫者店家 ID）
- [ ] T048 [US4] 驗證 `src/main/resources/mapper/OrderMapper.xml` 中 `selectByCondition` 的 `<if test="condition.startDate != null">` 與 `<if test="condition.endDate != null">` 動態 SQL 正確生成 `AND created_at >= #{condition.startDate}` 與 `AND created_at <= #{condition.endDate}`（直接測試 mapper 以驗證 SQL）
- [ ] T049 [US4] 在 `service/impl/OrderServiceImpl.java` 中確認 `getOrderById()` 對 ADMIN 角色不限制店家——ADMIN 可查看任意訂單詳情（US1 已實作店家檢查，此步驟確認 ADMIN 繞過條件）
- [ ] T050 [P] [US4] 在 `src/test/java/com/group/admin/controller/AdminOrderControllerTest.java` 補充管理員跨店整合測試：(a) ADMIN JWT 查詢全部訂單（無 `storeId`）→ 含多家店訂單；(b) ADMIN JWT 帶 `storeId` 篩選 → 只含該店訂單；(c) ADMIN JWT 帶日期範圍 → 只含該範圍訂單；(d) ADMIN JWT 存取任意訂單詳情 → 成功（200）

**檢查點**：US4 完整可測試——管理員跨店查詢可獨立驗證。

---

## Phase 7：Polish & 跨切面關注點

**目的**：完善文件、確認事務邊界、錯誤碼規範，執行整合驗收。

- [ ] T051 [P] 在 `controller/admin/AdminOrderController.java` 所有端點新增 SpringDoc Swagger 注解（`@Tag`、`@Operation(summary="...")`、`@ApiResponse`），描述語言使用中文；標記錯誤碼（400/401/403/404/409/422）對應 `contracts/admin-orders-api.md` 規格
- [ ] T052 [P] 在 `controller/api/OrderController.java` 所有端點新增 SpringDoc Swagger 注解，描述語言使用中文；標記玩家端錯誤碼（400/401/403/404/409/422）對應 `contracts/player-orders-api.md` 規格
- [ ] T053 [P] 審查 `service/impl/OrderServiceImpl.java`：確認所有寫入方法（`createOrdersFromPrizeBox`、`updateOrderStatus`、`cancelOrder`）標記 `@Transactional`；所有唯讀查詢方法（`getOrderList`、`getOrderById`、`getPlayerOrderList`、`getPlayerOrderById`）標記 `@Transactional(readOnly = true)`
- [ ] T054 [P] 確認 `GlobalExceptionHandler`（或專案現有例外處理類別）正確處理以下自定義例外並回傳規格定義的 JSON 格式：`ForbiddenException→403`、`NotFoundException→404`、`InvalidStateTransitionException→409`（代碼 `INVALID_STATE_TRANSITION`）、`CancelNotAllowedException→409`（代碼 `CANCEL_NOT_ALLOWED`）、`PrizeBoxUnavailableException→409`（代碼 `PRIZE_BOX_UNAVAILABLE`）、`StoreInactiveException→422`（代碼 `STORE_INACTIVE`）
- [ ] T055 執行 `mvn test -Dtest=OrderServiceTest,AdminOrderControllerTest` 確認所有測試通過（零失敗）
- [ ] T056 [P] 依照 `specs/008-order-management/quickstart.md` 手動執行完整 API 驗收流程：登入取 JWT → `POST /order/ship` 建立出貨訂單 → `PUT /admin/orders/{id}/status` 推進至 `COMPLETED` → 另建訂單推進至 `PREPARING` 後 `DELETE /admin/orders/{id}` 取消 → 確認獎品盒 `status` 回到 `IN_BOX`

---

## 依賴關係與執行順序

### 階段依賴

- **Phase 1（環境確認）**：無依賴——立即可開始
- **Phase 2（基礎設施）**：依賴 Phase 1 完成——**阻擋所有使用者故事**
- **Phase 3 & 4（US1 & US2，均為 P1）**：均依賴 Phase 2 完成；兩者可平行推進
- **Phase 5（US3）**：依賴 Phase 2；建議 Phase 3/4 完成後——共用 `AdminOrderController` 與 `OrderServiceImpl` 骨架
- **Phase 6（US4）**：依賴 Phase 2 與 US1 的 `getOrderList` 骨架
- **Phase 7（Polish）**：依賴 Phase 3–6 全部完成

### 使用者故事依賴

| 使用者故事 | 優先級 | 依賴 | 備註 |
|-----------|-------|------|------|
| US1（店家查看訂單） | P1 | Phase 2 完成 | 與 US2 可平行 |
| US2（玩家建立訂單） | P1 | Phase 2 完成 | 與 US1 可平行 |
| US3（推進出貨生命週期） | P2 | Phase 2（骨架）、建議 US1/US2 後 | 擴充 AdminOrderController |
| US4（管理員跨店查詢） | P3 | Phase 2、US1 的 getOrderList | 在 getOrderList 上補充 ADMIN 邏輯 |

### 各使用者故事內部順序

1. 先實作 service 方法 → 再實作 controller 端點
2. 讀取方法（query）先於寫入方法（create/update/cancel）
3. 驗證邏輯（guard checks）必須在持久化邏輯之前撰寫

---

## 平行執行範例

### Phase 2 可同時平行執行的任務

```
同時啟動（不同檔案，無相依）：
  T005  建立 CreateOrderReq.java
  T006  建立 UpdateOrderStatusReq.java
  T007  建立 CancelOrderReq.java
  T008  建立 OrderItemRes.java
  T009  建立 StatusLogRes.java
  T010  建立 OrderRes.java
  T011  建立 OrderDetailRes.java
  T013  更新 OrderMapper.xml（詳情查詢）
  T014  更新 OrderMapper（insertOrder）
  T015  更新 OrderMapper（updateStatusAndTimestamps）
  T016  更新 OrderItemMapper（batchInsert）
  T017  更新 OrderStatusLogMapper（insertLog）
  T018  更新 PrizeBoxMapper（batchUpdateStatus）
```

### Phase 3 + Phase 4 可同時平行執行

```
開發者 A（Phase 3 / US1）：
  T022 → T023 → T024 → T025

開發者 B（Phase 4 / US2）：
  T028 → T029 → T030 → T031 → T032 → T033 → T034 → T035
```

### Phase 5 US3 測試任務可平行

```
同時啟動（獨立測試類別）：
  T045  OrderServiceTest 狀態機測試
  T046  OrderServiceTest 取消邏輯測試
```

---

## 實作策略

### MVP 優先（僅使用者故事 1 & 2）

1. 完成 Phase 1：環境確認
2. 完成 Phase 2：基礎設施（**關鍵路徑，阻擋所有故事**）
3. 同步完成 Phase 3（US1）與 Phase 4（US2）
4. **停止並驗證**：以 quickstart.md 的 curl 指令測試店家負責人查看訂單 + 玩家建立訂單流程
5. 可部署或演示 MVP

### 增量交付

1. Phase 1 + Phase 2 完成 → 基礎就緒
2. Phase 3 + Phase 4（US1 + US2）→ 獨立測試 → 部署（MVP！）
3. Phase 5（US3）→ 獨立測試 → 部署
4. Phase 6（US4）→ 獨立測試 → 部署
5. Phase 7 → 收尾驗收

### 平行團隊策略

```
所有人共同完成 Phase 1 + Phase 2

Phase 2 完成後：
  開發者 A  → Phase 3（US1：店家查看訂單）
  開發者 B  → Phase 4（US2：玩家建立訂單）
  開發者 C  → Phase 5（US3：出貨生命週期）[待 A 完成 US1 骨架]

所有使用者故事完成後：
  全員共同完成 Phase 6（US4）+ Phase 7（Polish）
```

---

## 備註

- `[P]` 任務代表可平行執行（不同檔案，無相依關係）
- `[USn]` 標籤將任務映射至特定使用者故事，便於追蹤
- 每個使用者故事均應可獨立完成與測試
- 狀態機驗證依賴 `OrderStatusEnum.ordinal()` 順序，請勿更改 enum 順序
- 獎品盒扣除與取消還原均須在 `@Transactional` 內執行（全成功或全失敗）
- 店家隔離為強制要求：任何查詢層（mapper）不得硬編碼跨店邏輯，全部透過 `OrderCondition.storeId` 動態注入
- `operatorId` 欄位不得出現在玩家端（`/order/**`）的任何回應中
- 每完成一個 Phase 或邏輯群組後建議 git commit，方便回滾
