# Tasks: 029 - 獎品出貨報表

**Input**: Design documents from `/specs/029-prize-shipment-report/`  
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, contracts/ ✅, quickstart.md ✅

**Tests**: Included — `AdminReportControllerPrizeShipmentTest.java` is explicitly part of the implementation plan (plan.md project structure), with 6 test scenarios defined in quickstart.md.

**Organization**: Tasks grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1, US2, US3)
- Exact file paths included in every task description

---

## Phase 1: Setup (DDL Migration)

**Purpose**: Database schema change that must be applied before any code change can compile or run

- [ ] T001 Create DDL migration with `ALTER TABLE order ADD COLUMN preparing_at DATETIME NULL AFTER shipped_at` and two composite indexes (`idx_order_store_status_created`, `idx_order_store_shipped_at`) in `sql/V029__add_order_preparing_at.sql`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Order entity update + shared report infrastructure — ALL user stories depend on this phase

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T002 [P] Add `private LocalDateTime preparingAt` field with getter and setter after `cancelReason` in `src/main/java/com/group/admin/entity/Order.java`
- [ ] T003 [P] Add `preparing_at` to `BaseResultMap` (`<result column="preparing_at" jdbcType="TIMESTAMP" property="preparingAt"/>`), `Base_Column_List`, `insert`, `insertSelective`, `updateByPrimaryKey`, and `updateByPrimaryKeySelective` in `src/main/resources/mapper/OrderMapper.xml`
- [ ] T004 Add `order.setPreparingAt(LocalDateTime.now())` immediately after `order.setStatus(OrderStatusEnum.PREPARING.getCode())` in `markAsPreparing()` in `src/main/java/com/group/admin/service/impl/OrderServiceImpl.java`
- [ ] T005 [P] Create `PrizeShipmentReportCondition` class extending `BaseCondition` with `storeId`, `startDate (LocalDate)`, and `endDate (LocalDate)` fields and Swagger `@Schema` annotations in `src/main/java/com/group/admin/condition/report/PrizeShipmentReportCondition.java`
- [ ] T006 [P] Create `PrizeShipmentReportRes` class with `@Data @Builder` containing `startDate`, `endDate`, `pendingCount`, `preparingCount`, `shippedCount`, `completedCount`, `avgShipDays (BigDecimal)`, `overdueCount`, `dailyDetails (List<DailyShipment>)`, `storeDetails (List<StoreShipment>)`, and inner classes `DailyShipment` and `StoreShipment` in `src/main/java/com/group/admin/dto/res/report/PrizeShipmentReportRes.java`
- [ ] T007 Add `PrizeShipmentReportRes getPrizeShipmentReport(QueryReq<PrizeShipmentReportCondition> req)` method signature to `src/main/java/com/group/admin/service/ReportService.java`
- [ ] T008 Add `@PostMapping("/prize-shipment") @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER')")` endpoint that injects StoreOwner `storeId` via `SecurityUtils.getCurrentUserPrimaryStoreId()` before delegating to `reportService.getPrizeShipmentReport(req)` in `src/main/java/com/group/admin/controller/admin/AdminReportController.java`

**Checkpoint**: Foundation ready — Order entity updated, shared Condition/Res/Controller in place; user story SQL queries can now be implemented

---

## Phase 3: User Story 1 - 店家查看出貨狀態總覽 (Priority: P1) 🎯 MVP

**Goal**: Return 4 order status counts (`pendingCount`, `preparingCount`, `shippedCount`, `completedCount`) and `dailyDetails` (per-day shipped count grouped by `shipped_at`), with StoreOwner data isolation enforced at the Controller layer.

**Independent Test**: `POST /admin/report/prize-shipment` as StoreOwner → response contains 4 non-null status counts and `dailyDetails` array; `storeDetails` is null

### Tests for User Story 1

- [ ] T009 [US1] Write `givenStoreOwner_onlySeesOwnStore` test: mock service, call endpoint with StoreOwner JWT, assert `storeId` is overridden to current user's store and `storeDetails` is null in `src/test/java/com/group/admin/controller/admin/AdminReportControllerPrizeShipmentTest.java`
- [ ] T010 [US1] Write `givenNoOrders_returnsAllZeros` test: mock service to return all-zero counts, assert `pendingCount=0`, `preparingCount=0`, `shippedCount=0`, `completedCount=0`, `dailyDetails=[]`, `avgShipDays=null` in `src/test/java/com/group/admin/controller/admin/AdminReportControllerPrizeShipmentTest.java`

### Implementation for User Story 1

- [ ] T011 [US1] Implement status counts SQL query using `JdbcTemplate.queryForMap()` with `SUM(CASE WHEN status = ?) THEN 1 ELSE 0 END` for 4 statuses, `WHERE status NOT IN ('CANCELLED', 'PAYMENT_PENDING') AND created_at BETWEEN :startDate AND :endDate`, optional `AND store_id = :storeId` in `src/main/java/com/group/admin/service/impl/ReportServiceImpl.java`
- [ ] T012 [US1] Implement `dailyDetails` SQL query using `JdbcTemplate.query()` with `RowMapper<DailyShipment>` grouping `DATE(shipped_at)` for `status IN ('SHIPPED','COMPLETED')`, and add null-safe default date logic (`startDate = today-29`, `endDate = today` when null) in `src/main/java/com/group/admin/service/impl/ReportServiceImpl.java`

**Checkpoint**: US1 fully functional — StoreOwner can view order status overview and daily shipment details independently

---

## Phase 4: User Story 2 - 出貨時效與逾期警示 (Priority: P2)

**Goal**: Populate `avgShipDays` (`ROUND(AVG(DATEDIFF(shipped_at, preparing_at)), 1)`) and `overdueCount` (PENDING orders with `created_at < NOW() - INTERVAL 7 DAY`).

**Independent Test**: Report with 3 shipped orders (2/4/6-day gaps) → `avgShipDays = 4.0`; 2 orders past 7 days PENDING → `overdueCount = 2`

### Tests for User Story 2

- [ ] T013 [US2] Write `givenValidShippedOrders_avgShipDaysCorrect` test: mock service returning `avgShipDays = 4.0`, assert BigDecimal value serialized correctly in JSON response in `src/test/java/com/group/admin/controller/admin/AdminReportControllerPrizeShipmentTest.java`
- [ ] T014 [US2] Write `givenOverdueOrders_overdueCountCorrect` test: mock service returning `overdueCount = 2`, assert integer value in JSON response in `src/test/java/com/group/admin/controller/admin/AdminReportControllerPrizeShipmentTest.java`
- [ ] T015 [US2] Write `givenNullDates_defaultsToLast30Days` test: send request body `{}` (no dates), capture condition passed to service, assert `startDate = today-29` and `endDate = today` in `src/test/java/com/group/admin/controller/admin/AdminReportControllerPrizeShipmentTest.java`

### Implementation for User Story 2

- [ ] T016 [US2] Implement `avgShipDays` query using `JdbcTemplate.queryForObject()` with `ROUND(AVG(DATEDIFF(shipped_at, preparing_at)), 1)` where `status IN ('SHIPPED','COMPLETED') AND preparing_at IS NOT NULL AND shipped_at IS NOT NULL`, returning `null` when no rows match in `src/main/java/com/group/admin/service/impl/ReportServiceImpl.java`
- [ ] T017 [US2] Implement `overdueCount` query using `JdbcTemplate.queryForObject(Long.class)` with `status = 'PENDING' AND created_at < NOW() - INTERVAL 7 DAY` with optional `AND store_id = :storeId` (intentionally **no** date-range filter) in `src/main/java/com/group/admin/service/impl/ReportServiceImpl.java`

**Checkpoint**: US2 fully functional — `avgShipDays` and `overdueCount` populated; backward-compatible for pre-migration orders where `preparing_at = null`

---

## Phase 5: User Story 3 - Admin 跨店家出貨比較 (Priority: P3)

**Goal**: When Admin queries without `storeId` (`storeId == null`), populate `storeDetails` array with per-store shipment statistics (`storeName`, status counts, `avgShipDays`, `overdueCount`).

**Independent Test**: Admin calls endpoint without storeId → `storeDetails` is non-null array; each entry has `storeName`, 4 counts, `avgShipDays`, `overdueCount`

### Tests for User Story 3

- [ ] T018 [US3] Write `givenAdmin_returnsStoreDetails` test: mock service returning 2-store `storeDetails` list, assert Admin JWT request yields non-null `storeDetails` with `storeName`, `avgShipDays`, and `overdueCount` per store in `src/test/java/com/group/admin/controller/admin/AdminReportControllerPrizeShipmentTest.java`

### Implementation for User Story 3

- [ ] T019 [US3] Implement `storeDetails` query using `JdbcTemplate.query()` with `RowMapper<StoreShipment>`, `GROUP BY store_id, store_name ORDER BY avg_ship_days DESC`, including inline `overdue_count` via `SUM(CASE WHEN status='PENDING' AND created_at < NOW()-INTERVAL 7 DAY THEN 1 ELSE 0 END)`, executed **only when `storeId == null`** in `src/main/java/com/group/admin/service/impl/ReportServiceImpl.java`

**Checkpoint**: US3 fully functional — Admin can compare per-store shipment efficiency; StoreOwner still receives `storeDetails = null`

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Final validation and documentation alignment

- [ ] T020 [P] Verify `sql/V029__add_order_preparing_at.sql` contains all three DDL statements: `ADD COLUMN preparing_at`, `CREATE INDEX idx_order_store_status_created`, and `CREATE INDEX idx_order_store_shipped_at` (add a comment: "run `SHOW INDEX FROM \`order\`` before applying to avoid duplicate index errors") in `sql/V029__add_order_preparing_at.sql`
- [ ] T021 Run smoke verification per quickstart.md Step 9 scenarios using the three cURL examples from `specs/029-prize-shipment-report/contracts/prize-shipment-report-api.md`: StoreOwner default query, Admin full-platform query, Admin store-specific query — confirm all 3 return HTTP 200 with correct field structure

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately
- **Foundational (Phase 2)**: Depends on DDL applied (Phase 1) — **BLOCKS all user stories**
- **User Story 1 (Phase 3)**: Depends on Phase 2 completion
- **User Story 2 (Phase 4)**: Depends on Phase 2 completion — can run in parallel with US1 on separate branches
- **User Story 3 (Phase 5)**: Depends on Phase 2 completion — can run in parallel with US1/US2
- **Polish (Phase 6)**: All desired user stories complete

### User Story Dependencies

- **US1 (P1)**: After Phase 2 — adds status counts + dailyDetails to `getPrizeShipmentReport()`
- **US2 (P2)**: After Phase 2 — adds `avgShipDays` + `overdueCount` to same service method; independent of US1 SQL queries
- **US3 (P3)**: After Phase 2 — adds conditional `storeDetails` branch to service method; independent of US1/US2

### Within Each User Story

- **Tests FIRST** — write T009/T010, T013–T015, T018 and confirm they **fail** before writing implementation
- Models/DTOs before services (`T005`, `T006` before `T007`)
- Service interface before implementation (`T007` before `T011–T019`)
- Controller after interface (`T007` before `T008`)
- ReportServiceImpl tasks within each story are sequential (same file/method)

### Parallel Opportunities

- **Phase 2 parallel batch**: T002 (Order.java) ∥ T003 (OrderMapper.xml) ∥ T005 (Condition) ∥ T006 (Res DTO)
- **Post-Phase 2**: US1, US2, US3 can start simultaneously (different SQL blocks in ReportServiceImpl)
- **Multi-developer**: Developer A → Phase 3, Developer B → Phase 4, Developer C → Phase 5

---

## Parallel Example: Phase 2

```
# Launch all parallel tasks simultaneously:
Task T002: Add preparingAt to src/main/java/com/group/admin/entity/Order.java
Task T003: Add preparing_at to src/main/resources/mapper/OrderMapper.xml
Task T005: Create src/main/java/com/group/admin/condition/report/PrizeShipmentReportCondition.java
Task T006: Create src/main/java/com/group/admin/dto/res/report/PrizeShipmentReportRes.java

# Then sequential (each depends on above):
Task T004: Update OrderServiceImpl.markAsPreparing()
Task T007: Add method signature to ReportService.java
Task T008: Add endpoint to AdminReportController.java
```

## Parallel Example: Post-Phase 2 (Multi-Developer)

```
Developer A — Phase 3 (US1):
  T009 → T010 → T011 → T012

Developer B — Phase 4 (US2):
  T013 → T014 → T015 → T016 → T017

Developer C — Phase 5 (US3):
  T018 → T019
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete **Phase 1**: Apply DDL (`V029__add_order_preparing_at.sql`)
2. Complete **Phase 2**: Order entity + Condition/Res/Controller stub (CRITICAL — blocks everything)
3. Complete **Phase 3**: Status counts + dailyDetails + StoreOwner isolation
4. **STOP and VALIDATE**: `POST /admin/report/prize-shipment` as StoreOwner — verify 4 counts, `dailyDetails` array, `storeDetails = null`
5. Deploy/demo MVP

### Incremental Delivery

1. Phase 1 + 2 → DDL applied, infrastructure ready
2. US1 (Phase 3) → StoreOwner sees shipment status overview → **MVP Demo** ✅
3. US2 (Phase 4) → Add `avgShipDays` + `overdueCount` → Efficiency insights ✅
4. US3 (Phase 5) → Add `storeDetails` for Admin → Platform management view ✅
5. Each story incrementally adds value without breaking the previous

### Single-Developer Recommended Sequence

```
T001
↓
T002 ∥ T003 ∥ T005 ∥ T006
↓
T004 → T007 → T008
↓
T009 → T010 → T011 → T012   ← US1 done ✓
↓
T013 → T014 → T015 → T016 → T017   ← US2 done ✓
↓
T018 → T019   ← US3 done ✓
↓
T020 ∥ T021   ← Polish done ✓
```

---

## Notes

- **[P]** = different files, no data dependencies — safe to run simultaneously
- **[US*]** label provides full traceability back to spec.md acceptance criteria
- `overdueCount` SQL intentionally **omits** the date range filter — it always reflects the current real-time overdue state (R-005)
- `preparing_at IS NOT NULL` guard in `avgShipDays` SQL handles backward compatibility for orders created before the DDL migration (R-004)
- `PAYMENT_PENDING` excluded alongside `CANCELLED` from all status counts (edge case in spec.md)
- StoreOwner `storeId` injection is in the Controller (T008), guaranteeing it cannot be bypassed from any Service path
- Run `SHOW INDEX FROM \`order\`` before applying DDL to confirm `idx_order_store_shipped_at` does not already exist
- Reference implementations: `RevenueReportCondition.java`, `RevenueReportRes.java`, `ReportServiceImpl.getRevenueReport()` (quickstart.md §參考檔案)
