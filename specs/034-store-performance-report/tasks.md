# Tasks: 034 - 店家績效比較報表

**Branch**: `034-store-performance-report`
**Input**: Design documents from `/specs/034-store-performance-report/`
**Tests**: Test tasks included (plan.md 明定修改 `AdminReportControllerTest.java`)
**Organization**: Tasks grouped by user story to enable independent implementation and testing.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies on incomplete tasks)
- **[Story]**: Which user story this task belongs to (US1, US2, US3)
- Exact file paths included in all task descriptions

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Verify existing project structure. No DB migration, no MBG — pure JdbcTemplate extension of existing report layer.

- [X] T001 Verify `AdminReportController`, `ReportService`, and `ReportServiceImpl` all exist and that `mvn clean compile` passes from `src/main/java/com/group/admin/`

**Checkpoint**: Existing report infrastructure confirmed working — proceed to Foundational phase.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: DTO 資料合約與 Service 介面 — 所有 User Story 的實作都依賴這三個基礎物件。

⚠️ **CRITICAL**: No user story work can begin until this phase is complete.

- [X] T002 [P] Create `StorePerformanceCondition.java` with `@Data @EqualsAndHashCode(callSuper=true)` extending `BaseCondition`; fields: `String storeId`, `LocalDate startDate`, `LocalDate endDate`; add `@Schema` annotations in `src/main/java/com/group/admin/condition/report/StorePerformanceCondition.java`
- [X] T003 [P] Create `StorePerformanceReportRes.java` with `@Data @Builder`; outer fields: `LocalDate startDate`, `LocalDate endDate`, `List<StoreItem> stores`, `List<DailyStat> dailyStats`; inner static class `StoreItem` (`@Data @Builder`: `String storeId`, `String storeName`, `Long totalRevenue`, `Integer drawCount`, `Integer activeUsers`, `Double shipRate`, `Double overdueRate`, `Double avgShipDays`); inner static class `DailyStat` (`@Data @Builder`: `LocalDate date`, `Integer drawCount`, `Long revenue`, `Integer newUsers`) in `src/main/java/com/group/admin/dto/res/report/StorePerformanceReportRes.java`
- [X] T004 Add method signature `StorePerformanceReportRes getStorePerformanceReport(QueryReq<StorePerformanceCondition> req);` to `src/main/java/com/group/admin/service/ReportService.java`

**Checkpoint**: DTOs + service interface defined — user story implementation can now begin.

---

## Phase 3: User Story 1 — Admin 查看所有店家績效排行 (P1) 🎯 MVP

**Goal**: Admin 呼叫 `POST /admin/report/store-performance`（不帶 `storeId`），取得所有店家的 `totalRevenue`、`drawCount`、`activeUsers`、`shipRate`，並支援依任意 KPI 欄位自訂排序。

**Independent Test**: `POST /admin/report/store-performance` → HTTP 200，`stores[]` 含所有店家，`dailyStats: null`；帶 `sortBy: "drawCount", sortOrder: "DESC"` 時，drawCount 最高的店家排第一；時間範圍為 2026-04 時，KPI 僅計算該期間資料。

### Implementation for User Story 1

- [X] T005 [US1] Implement `getStorePerformanceReport()` skeleton in `src/main/java/com/group/admin/service/impl/ReportServiceImpl.java`: default `startDate = LocalDate.now().minusDays(30)`, `endDate = LocalDate.now()` when null; declare `Set<String> ALLOWED_SORT_FIELDS = Set.of("totalRevenue","drawCount","activeUsers","shipRate","overdueRate","avgShipDays")`; fallback `sortBy` to `"totalRevenue"` if not in whitelist; read `sortOrder` default to `"DESC"`
- [X] T006 [P] [US1] Implement `drawCount` JdbcTemplate query in `src/main/java/com/group/admin/service/impl/ReportServiceImpl.java`: `SELECT l.store_id, COUNT(*) AS draw_count FROM lottery_ticket lt JOIN lottery l ON lt.lottery_id = l.id WHERE lt.status = 'DRAWN' AND lt.drawn_at BETWEEN ? AND ? [AND l.store_id = ?] GROUP BY l.store_id`; return `Map<String, Integer>` keyed by storeId
- [X] T007 [P] [US1] Implement `totalRevenue` JdbcTemplate query in `src/main/java/com/group/admin/service/impl/ReportServiceImpl.java`: `SELECT l.store_id, ABS(SUM(wt.amount)) AS total_revenue FROM wallet_transaction wt JOIN lottery_ticket lt ON wt.related_id = lt.id JOIN lottery l ON lt.lottery_id = l.id WHERE wt.transaction_type = 'DRAW' AND wt.created_at BETWEEN ? AND ? [AND l.store_id = ?] GROUP BY l.store_id`; return `Map<String, Long>`
- [X] T008 [P] [US1] Implement `activeUsers` JdbcTemplate query in `src/main/java/com/group/admin/service/impl/ReportServiceImpl.java`: `SELECT store_id, COUNT(DISTINCT uid) AS active_users FROM (SELECT l.store_id, lt.drawn_by AS uid FROM lottery_ticket lt JOIN lottery l ON lt.lottery_id = l.id WHERE lt.drawn_at BETWEEN ? AND ? AND lt.drawn_by IS NOT NULL UNION SELECT o.store_id, o.user_id AS uid FROM \`order\` o WHERE o.created_at BETWEEN ? AND ?) t [WHERE t.store_id = ?] GROUP BY t.store_id`; return `Map<String, Integer>`
- [X] T009 [US1] Implement result assembly in `src/main/java/com/group/admin/service/impl/ReportServiceImpl.java`: fetch all active stores via `SELECT id, store_name FROM store`; for each store merge `drawCount`, `totalRevenue`, `activeUsers` maps (default 0 if absent); set `avgShipDays = null` with comment `// TODO(029): replace with AVG(DATEDIFF(shipped_at, preparing_at)) after 029 merges`; apply in-memory sort on `stores` list by `sortBy` field using `Comparator` with null-last handling; wrap in `StorePerformanceReportRes` with `startDate`, `endDate`, `dailyStats = null`
- [X] T010 [US1] Add `@PostMapping("/store-performance") @PreAuthorize("hasAnyRole('ADMIN','STORE_OWNER')")` endpoint in `src/main/java/com/group/admin/controller/admin/AdminReportController.java`: accept `@RequestBody QueryReq<StorePerformanceCondition> req`, call `reportService.getStorePerformanceReport(req)`, return `ResponseEntity.ok(...)`

**Checkpoint**: US1 independently testable — Admin receives all-store KPI table with sorting, `dailyStats: null`.

---

## Phase 4: User Story 2 — 出貨率與逾期率比較 (P2)

**Goal**: `shipRate` 和 `overdueRate` 欄位計算正確，且分母為零時安全回傳 `null`。

**Independent Test**: A 店 100 SHIPPED+COMPLETED / 120 非CANCELLED → `shipRate: 83.3`；B 店 10 PENDING 超 7 天 / 50 總訂單 → `overdueRate: 20.0`；無訂單店家 → `shipRate: null`, `overdueRate: null`。

### Implementation for User Story 2

- [X] T011 [US2] Implement combined `shipRate`/`overdueRate` JdbcTemplate query in `src/main/java/com/group/admin/service/impl/ReportServiceImpl.java`: `SELECT store_id, SUM(CASE WHEN status IN ('SHIPPED','COMPLETED') THEN 1 ELSE 0 END) AS shipped, SUM(CASE WHEN status != 'CANCELLED' THEN 1 ELSE 0 END) AS non_cancelled, SUM(CASE WHEN status = 'PENDING' AND created_at < DATE_SUB(NOW(), INTERVAL 7 DAY) THEN 1 ELSE 0 END) AS overdue, COUNT(*) AS total FROM \`order\` WHERE created_at BETWEEN ? AND ? [AND store_id = ?] GROUP BY store_id`; return per-store rate data struct
- [X] T012 [US2] Add null-guard in result assembly in `src/main/java/com/group/admin/service/impl/ReportServiceImpl.java`: `shipRate = nonCancelled > 0 ? (double) shipped / nonCancelled * 100 : null`; `overdueRate = total > 0 ? (double) overdue / total * 100 : null`; round to one decimal place; merge into `StoreItem`

**Checkpoint**: US2 testable — `shipRate` and `overdueRate` compute correctly; zero-order stores return null safely.

---

## Phase 5: User Story 3 — 單店家詳細績效查詢 (P2)

**Goal**: 帶入 `storeId` 時，回傳該店的 `dailyStats[]`（每日 drawCount / revenue / newUsers）；StoreOwner 強制只能查自己的店，查他店回傳 HTTP 403。

**Independent Test**: Admin 帶 `storeId` → HTTP 200，`dailyStats[]` 非 null 且包含 `date`, `drawCount`, `revenue`, `newUsers`；StoreOwner 帶自己的 `storeId` → 200；StoreOwner 帶他店 `storeId` → 403。

### Implementation for User Story 3

- [X] T013 [US3] Implement `dailyStats` JdbcTemplate query in `src/main/java/com/group/admin/service/impl/ReportServiceImpl.java`: `SELECT DATE(lt.drawn_at) AS stat_date, COUNT(*) AS draw_count, ABS(COALESCE(SUM(wt.amount), 0)) AS revenue FROM lottery_ticket lt JOIN lottery l ON lt.lottery_id = l.id LEFT JOIN wallet_transaction wt ON wt.related_id = lt.id AND wt.transaction_type = 'DRAW' WHERE l.store_id = ? AND lt.drawn_at BETWEEN ? AND ? AND lt.status = 'DRAWN' GROUP BY DATE(lt.drawn_at) ORDER BY stat_date`; return `List<DailyStat>` (without `newUsers` yet)
- [X] T014 [US3] Implement `newUsers` calculation for `dailyStats` in `src/main/java/com/group/admin/service/impl/ReportServiceImpl.java`: `SELECT DATE(first_date) AS stat_date, COUNT(*) AS new_users FROM (SELECT uid, MIN(activity_date) AS first_date FROM (SELECT lt.drawn_by AS uid, DATE(lt.drawn_at) AS activity_date FROM lottery_ticket lt JOIN lottery l ON lt.lottery_id = l.id WHERE l.store_id = ? AND lt.drawn_at BETWEEN ? AND ? AND lt.drawn_by IS NOT NULL UNION ALL SELECT o.user_id AS uid, DATE(o.created_at) AS activity_date FROM \`order\` o WHERE o.store_id = ? AND o.created_at BETWEEN ? AND ?) all_activity GROUP BY uid) first_seen GROUP BY DATE(first_date)`; merge `newUsers` into the `DailyStat` list by date
- [X] T015 [US3] Add conditional branch in `getStorePerformanceReport()` in `src/main/java/com/group/admin/service/impl/ReportServiceImpl.java`: if `condition.getStoreId() != null` → apply `storeId` filter to all KPI queries (drawCount, totalRevenue, activeUsers, order stats) and execute `dailyStats` queries; else → query all stores without filter, set `dailyStats = null`
- [X] T016 [US3] Add StoreOwner access control in `src/main/java/com/group/admin/controller/admin/AdminReportController.java` `getStorePerformanceReport()` method: `String currentStoreId = SecurityUtils.getCurrentUserPrimaryStoreId(); if (currentStoreId != null) { String requested = req.getCondition().getStoreId(); if (requested != null && !requested.equals(currentStoreId)) { return ResponseEntity.status(403).build(); } req.getCondition().setStoreId(currentStoreId); }`

**Checkpoint**: US3 testable — single-store query returns dailyStats; StoreOwner cannot query other stores.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Controller 測試覆蓋、邊界條件驗證、最終編譯確認。

- [X] T017 [P] Add MockMvc test `storePerformance_Admin_AllStores_Returns200` in `src/test/java/com/group/admin/controller/AdminReportControllerTest.java`: mock `reportService.getStorePerformanceReport()` → return stub with 2 stores, `dailyStats: null`; assert HTTP 200, `$.stores.length() == 2`, `$.dailyStats == null`
- [X] T018 [P] Add MockMvc test `storePerformance_Admin_SingleStore_ReturnsDailyStats` in `src/test/java/com/group/admin/controller/AdminReportControllerTest.java`: request with `storeId`, mock returns 1 store + `dailyStats[]`; assert HTTP 200, `$.dailyStats` non-null
- [X] T019 [P] Add MockMvc tests for StoreOwner in `src/test/java/com/group/admin/controller/AdminReportControllerTest.java`: `storePerformance_StoreOwner_OwnStore_Returns200` (mock `SecurityUtils.getCurrentUserPrimaryStoreId()` → same storeId → 200); `storePerformance_StoreOwner_OtherStore_Returns403` (different storeId → 403)
- [X] T020 [P] Add MockMvc test `storePerformance_InvalidSortBy_FallsBackToTotalRevenue` in `src/test/java/com/group/admin/controller/AdminReportControllerTest.java`: send `sortBy: "invalidField"` → assert HTTP 200 (no error), service called without exception
- [X] T021 Run `mvn clean compile` from project root to confirm zero compilation errors across all modified and new files

---

## Dependencies & Execution Order

### Phase Dependencies

```
Phase 1: Setup            → no dependencies
Phase 2: Foundational     → depends on Phase 1 (BLOCKS all user stories)
Phase 3: US1 (P1)         → depends on Phase 2
Phase 4: US2 (P2)         → depends on Phase 2; builds on same ReportServiceImpl method as US1
Phase 5: US3 (P2)         → depends on Phase 2; dailyStats + StoreOwner are orthogonal to US1/US2
Phase 6: Polish           → depends on Phase 3, 4, 5 all complete
```

### User Story Dependencies

- **US1 (P1)**: Starts after Phase 2 — no inter-story dependencies
- **US2 (P2)**: Starts after Phase 2 — extends the same `getStorePerformanceReport()` method; can run in parallel with US1 on separate feature increments if staffed
- **US3 (P2)**: Starts after Phase 2 — `dailyStats` queries and StoreOwner control do not depend on US1/US2 implementation details

### Within Each User Story

- T002, T003 (Foundational DTOs) → parallel
- T005 (skeleton) → must complete before T006, T007, T008
- T006, T007, T008 (SQL queries) → parallel (separate private methods in same file)
- T006 + T007 + T008 → T009 (result assembly)
- T009 → T010 (controller endpoint)
- T011 → T012 (null-guard after SQL in place)
- T013 → T014 (newUsers extends dailyStats query)
- T013 + T014 → T015 (conditional branch wires everything)
- T015 → T016 (StoreOwner control in controller, after service is ready)

---

## Parallel Execution Examples

### Phase 2 — Foundational (run together)

```
Task T002: Create StorePerformanceCondition.java
Task T003: Create StorePerformanceReportRes.java
```

### Phase 3 — US1 SQL Queries (run after T005)

```
Task T006: drawCount JdbcTemplate query
Task T007: totalRevenue JdbcTemplate query
Task T008: activeUsers JdbcTemplate query
```

### Phase 6 — Tests (all parallel)

```
Task T017: Admin all-stores test
Task T018: Admin single-store + dailyStats test
Task T019: StoreOwner own/other store test
Task T020: Invalid sortBy fallback test
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (verify compile)
2. Complete Phase 2: Foundational (T002, T003, T004)
3. Complete Phase 3: US1 (T005 → T006+T007+T008 → T009 → T010)
4. **STOP and VALIDATE**: `POST /admin/report/store-performance` returns `stores[]` with `totalRevenue`, `drawCount`, `activeUsers`, `shipRate` (shipRate will be 0 until US2 wires it); `dailyStats: null`
5. Demo to stakeholders if ready

### Incremental Delivery

1. Phase 1 + 2 → Foundation ready (DTOs + interface)
2. Phase 3 → US1 live — Admin KPI ranking table ✅
3. Phase 4 → US2 — `shipRate` / `overdueRate` calculation verified ✅
4. Phase 5 → US3 — single-store `dailyStats` + StoreOwner access control ✅
5. Phase 6 → Controller tests + build validation ✅

### Parallel Team Strategy

With multiple developers (after Phase 2 completes):
- **Dev A**: US1 (Phase 3) — basic KPI + endpoint
- **Dev B**: US2 (Phase 4) — order rate calculations
- **Dev C**: US3 (Phase 5) — dailyStats + StoreOwner control

---

## Notes

- **[P]** = tasks can run in parallel (different files or independent methods, no incomplete-task dependencies)
- **No MBG required**: Pure JdbcTemplate SQL; no new DB entities, no DDL migration
- **029 dependency**: `avgShipDays` is `null` until feature-029 merges `order.preparing_at`; mark with `// TODO(029)` comment
- **sortBy injection safety**: Non-whitelist values silently fall back to `totalRevenue` — do NOT return 400
- **Null guards**: `shipRate` → null when `nonCancelled == 0`; `overdueRate` → null when `total == 0`
- **Performance**: Target < 5s for cross-store aggregation (NFR-001); candidate indexes: `(store_id, created_at)` on `order`, `(lottery_id, status, drawn_at)` on `lottery_ticket`
- **Data consistency**: Sum of `stores[].totalRevenue` should equal feature-033 `totalSpend` (SC-003)
- **Commit cadence**: Commit after each phase or logical group; tag US1 completion for potential early deploy
