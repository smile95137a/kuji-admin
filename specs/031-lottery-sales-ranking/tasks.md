# Tasks: 031 - 商品銷售排行報表

**Input**: Design documents from `/specs/031-lottery-sales-ranking/`
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, contracts/ ✅, quickstart.md ✅

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies on incomplete tasks)
- **[Story]**: Which user story this task belongs to (`US1`, `US2`, `US3`)
- Exact file paths are included in every description

---

## Phase 1: Setup

**Purpose**: Verify existing architectural context before adding new code

- [ ] T001 Review existing report pattern in `src/main/java/com/group/admin/service/ReportService.java`, `src/main/java/com/group/admin/service/impl/ReportServiceImpl.java`, and `src/main/java/com/group/admin/controller/admin/AdminReportController.java` to confirm `JdbcTemplate`, `QueryReq<T>`, `SecurityUtils.getCurrentUserPrimaryStoreId()` usage aligns with plan.md Section "Pattern Confirmed"

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Create shared DTOs and extend the service interface — must complete before any user story implementation begins

**⚠️ CRITICAL**: T008 (Service impl) and T009 (Controller endpoint) cannot compile until T002, T003, T004 are complete

- [ ] T002 [P] Create `src/main/java/com/group/admin/condition/report/LotterySalesRankingCondition.java` — class annotated `@Data @EqualsAndHashCode(callSuper = true) @Schema(description = "商品銷售排行報表查詢條件")` extending `BaseCondition`; two fields: `@Schema(…) private String storeId` and `@Schema(description = "回傳筆數，預設 20，最大 100", example = "20") private Integer limit`; package `com.group.admin.condition.report`
- [ ] T003 [P] Create `src/main/java/com/group/admin/dto/res/report/LotterySalesRankingRes.java` — outer class annotated `@Data @Builder @Schema(description = "商品銷售排行報表回應")` with `private Integer totalRecords` and `private List<LotterySalesItem> items`; inner static class `LotterySalesItem` annotated `@Data @Builder @Schema` with fields `String lotteryId`, `String lotteryTitle`, `String storeName`, `Integer drawCount`, `Long revenue`, `Integer rank`; package `com.group.admin.dto.res.report`
- [ ] T004 Add method signature `LotterySalesRankingRes getLotterySalesRanking(QueryReq<LotterySalesRankingCondition> req);` to `src/main/java/com/group/admin/service/ReportService.java` (place after `getBonusReport` method); add imports `import com.group.admin.condition.report.LotterySalesRankingCondition;` and `import com.group.admin.dto.res.report.LotterySalesRankingRes;`

**Checkpoint**: DTOs compile; service interface updated → user story phases can proceed

---

## Phase 3: User Story 1 - 查看商品銷售排行榜 (P1) 🎯 MVP

**Goal**: StoreOwner 呼叫 `POST /admin/report/lottery-sales` 取得自己店家依 `drawCount` 降序排列的商品排行榜，最多 20 筆；後端強制綁定 storeId

**Independent Test**: Use StoreOwner JWT → `POST /admin/report/lottery-sales` `{"condition":{},"sortBy":"drawCount"}` → 200 OK; items sorted by `drawCount` DESC; result contains only own-store lotteries; default limit 20 applied

### Tests for User Story 1

- [ ] T005 [P] [US1] Create `src/test/java/com/group/admin/controller/admin/AdminReportControllerLotteryRankingTest.java` as `@WebMvcTest(AdminReportController.class)` class; write `testStoreOwnerStoreIdForced()` — mock `SecurityUtils.getCurrentUserPrimaryStoreId()` to return `"store-uuid-001"`; POST `/admin/report/lottery-sales` with empty condition; verify `reportService.getLotterySalesRanking()` is called with `condition.storeId == "store-uuid-001"` (backend override confirmed)
- [ ] T006 [P] [US1] In `AdminReportControllerLotteryRankingTest.java`: write `testDefaultDrawCountSortAndLimit()` — Admin JWT (storeId=null); POST with no sortBy; mock service returns list with 2 items; assert response status 200 and `items` size equals 2; verify service is invoked once
- [ ] T007 [P] [US1] In `AdminReportControllerLotteryRankingTest.java`: write `testUnauthenticated401()` — POST without Authorization header → assert 401; write `testForbiddenRole403()` — POST with JWT of disallowed role → assert 403

### Implementation for User Story 1

- [ ] T008 [US1] Implement `@Override public LotterySalesRankingRes getLotterySalesRanking(QueryReq<LotterySalesRankingCondition> req)` in `src/main/java/com/group/admin/service/impl/ReportServiceImpl.java` (add after `getBonusReport()`): (1) extract `condition` (null-safe fallback to `new LotterySalesRankingCondition()`); (2) `int limit = Math.min(condition.getLimit() != null ? condition.getLimit() : 20, 100)`; (3) `sortBy` whitelist — `"revenue".equalsIgnoreCase(req.getSortBy()) ? "revenue" : "draw_count"`; (4) build base SQL: `SELECT l.id AS lottery_id, l.title AS lottery_title, s.store_name, COALESCE(dc.draw_count,0) AS draw_count, COALESCE(rv.revenue,0) AS revenue FROM lottery l JOIN store s ON l.store_id=s.id LEFT JOIN (SELECT lottery_id, COUNT(*) AS draw_count FROM lottery_ticket WHERE status='DRAWN' GROUP BY lottery_id) dc ON dc.lottery_id=l.id LEFT JOIN (SELECT oi.lottery_id, COUNT(oi.id)*MAX(l2.price_per_draw) AS revenue FROM order_item oi JOIN \`order\` o ON o.id=oi.order_id AND o.status!='CANCELLED' JOIN lottery l2 ON l2.id=oi.lottery_id GROUP BY oi.lottery_id) rv ON rv.lottery_id=l.id WHERE 1=1`; (5) conditionally append `AND l.store_id=?` when `storeId != null`; (6) run `SELECT COUNT(*) FROM (<baseSql>) AS total` for `totalRecords`; (7) append `ORDER BY <sortBy> DESC LIMIT ?`; (8) execute `jdbcTemplate.query()` mapping rows to `LotterySalesItem` with `rank = rowNum+1`; (9) return `LotterySalesRankingRes.builder().totalRecords(totalRecords).items(items).build()`; add log line `log.info("📊 商品銷售排行: storeId={}, limit={}, sortBy={}", storeId, limit, sortBy)`
- [ ] T009 [US1] Add endpoint to `src/main/java/com/group/admin/controller/admin/AdminReportController.java` (after `getBonusReport` method): `@PostMapping("/lottery-sales")` + `@PreAuthorize("hasAnyRole('ADMIN','STORE_OWNER')")` + method `getLotterySalesRanking(@RequestBody QueryReq<LotterySalesRankingCondition> req)`; call `SecurityUtils.getCurrentUserPrimaryStoreId()`; when non-null: null-safe init `req` and `req.condition`, then `req.getCondition().setStoreId(currentStoreId)`; return `ResponseEntity.ok(reportService.getLotterySalesRanking(req))`; add imports for `LotterySalesRankingCondition` and `LotterySalesRankingRes`

**Checkpoint**: Build passes; T005–T007 tests pass; `curl -X POST /admin/report/lottery-sales` with StoreOwner JWT returns drawCount-sorted items — US1 fully functional and independently testable

---

## Phase 4: User Story 2 - 查看商品營收排行 (P2)

**Goal**: `revenue` 欄位正確計算（`COUNT(valid order_items) × price_per_draw`，排除 CANCELLED 訂單）；`sortBy=revenue` 回傳依 revenue 降序排列的結果

**Independent Test**: POST `{"sortBy":"revenue"}` → items in `revenue DESC` order; manual SQL `SELECT COUNT(oi.id)*MAX(l.price_per_draw) … WHERE o.status!='CANCELLED'` matches API revenue value; `limit=200` clamped to 100

### Tests for User Story 2

- [ ] T010 [P] [US2] In `AdminReportControllerLotteryRankingTest.java`: write `testRevenueSortReturnsItems()` — Admin JWT; POST `{"sortBy":"revenue"}`; mock service returns list of 3 items; assert 200 OK; verify `reportService.getLotterySalesRanking()` is called with `req.getSortBy()=="revenue"`
- [ ] T011 [P] [US2] In `AdminReportControllerLotteryRankingTest.java`: write `testLimitOver100IsClamped()` — POST `{"condition":{"limit":200}}`; verify service receives `condition.limit==200` (clamping is in ServiceImpl, not Controller); mock service returns empty list; assert 200 OK (Controller passes through, ServiceImpl clamps)

### Implementation for User Story 2

- [ ] T012 [US2] Verify revenue subquery is present in `getLotterySalesRanking()` in `src/main/java/com/group/admin/service/impl/ReportServiceImpl.java` (already built in T008); confirm: (a) LEFT JOIN revenue subquery excludes `o.status='CANCELLED'`; (b) `COALESCE(rv.revenue, 0)` handles new lotteries with no valid orders; (c) `sortBy` resolves to `"revenue"` column name (not alias mismatch) when `req.getSortBy()=="revenue"`; (d) `Math.min(limit, 100)` clamp is applied before appending `LIMIT ?` to SQL — add inline comment `// FR-005: exclude CANCELLED orders` next to the revenue JOIN condition

**Checkpoint**: revenue field correct for all cases; sortBy=revenue works; limit clamped at 100 — US2 independently verifiable alongside US1

---

## Phase 5: User Story 3 - Admin 跨店家商品比較 (P3)

**Goal**: Admin 不帶 `storeId` 時查全平台商品；帶 `storeId` 時只回傳該店家商品；StoreOwner 無法繞過 storeId 強制覆蓋

**Independent Test**: Admin JWT + no storeId → `WHERE l.store_id=?` clause absent; Admin JWT + `condition.storeId=<uuid>` → filtered; StoreOwner JWT + any storeId in body → overridden by JWT storeId

### Tests for User Story 3

- [ ] T013 [P] [US3] In `AdminReportControllerLotteryRankingTest.java`: write `testAdminNoStoreIdGetsCrossPlatform()` — mock `SecurityUtils.getCurrentUserPrimaryStoreId()` returns `null` (Admin); POST with no condition; verify `reportService.getLotterySalesRanking()` is called with `condition.storeId == null` (no store filter)
- [ ] T014 [P] [US3] In `AdminReportControllerLotteryRankingTest.java`: write `testAdminWithStoreIdFiltersStore()` — Admin JWT; POST `{"condition":{"storeId":"target-store-uuid"}}`; verify `reportService.getLotterySalesRanking()` receives `condition.storeId == "target-store-uuid"` (Admin's explicit storeId is preserved, not overridden)

### Implementation for User Story 3

- [ ] T015 [US3] Review Admin cross-store null-storeId path in `src/main/java/com/group/admin/service/impl/ReportServiceImpl.java`: confirm `if (storeId != null)` guard means `storeId=null` skips `WHERE l.store_id=?` entirely (cross-platform query); confirm `params` list stays empty for count query when `storeId=null`; add inline comment `// FR-008: Admin cross-store — no WHERE filter when storeId is null`; no code change needed if T008 correctly handles null

**Checkpoint**: Admin global and per-store queries verified; all 3 user stories complete and independently testable

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Swagger completeness, validation, and acceptance criteria sign-off

- [ ] T016 [P] Verify all `@Schema` annotation `description` values in `LotterySalesRankingCondition.java` and `LotterySalesRankingRes.java` match field descriptions in `specs/031-lottery-sales-ranking/contracts/POST_admin_report_lottery-sales.md`; confirm `@Schema` is present on outer class, inner class, and every field
- [ ] T017 Run quickstart.md acceptance scenarios and confirm: SC-001 — `POST /admin/report/lottery-sales` completes in < 3 seconds with 50k `lottery_ticket` rows; SC-002 — `revenue` from API matches manual SQL `SELECT COUNT(oi.id)*MAX(l.price_per_draw)…WHERE o.status!='CANCELLED'` for a known lottery; SC-003 — API item order matches `ORDER BY draw_count DESC LIMIT 20` manual query; record pass/fail in PR description

---

## Dependencies & Execution Order

### Phase Dependencies

```
Phase 1 (Setup)
    └─► Phase 2 (Foundational: T002, T003, T004)
              ├─► Phase 3 US1 (T005–T009)
              │       └─► Phase 4 US2 (T010–T012)  [extends T008 SQL]
              │               └─► Phase 5 US3 (T013–T015)  [verifies T008/T009 logic]
              │                       └─► Phase 6 Polish (T016, T017)
```

### User Story Dependencies

| Story | Depends On | Notes |
|-------|-----------|-------|
| US1 (P1) | Phase 2 complete | Core implementation — builds the full SQL + Controller |
| US2 (P2) | T008 (Service impl) | Revenue subquery already in T008 SQL; T012 is verification + annotation |
| US3 (P3) | T008 + T009 | Admin null-storeId path already handled in T008; T015 is verification |

### Within Each User Story

- Tests (T005–T007, T010–T011, T013–T014) MUST be written and confirmed FAILING before corresponding implementation tasks (T008, T009, T012, T015)
- T008 (Service impl) must complete before T009 (Controller) — Controller references service method
- T002 ‖ T003 (parallel — different new files)
- T005 ‖ T006 ‖ T007 (parallel — different test methods in same new file)
- T010 ‖ T011 (parallel — different test methods)
- T013 ‖ T014 (parallel — different test methods)
- T016 ‖ T017 (parallel — different concerns)

---

## Parallel Example: User Story 1

```bash
# Phase 2 — create both DTOs in parallel:
Task T002: "Create LotterySalesRankingCondition.java"
Task T003: "Create LotterySalesRankingRes.java"

# After T002+T003 done, write all US1 tests in parallel:
Task T005: "testStoreOwnerStoreIdForced()"
Task T006: "testDefaultDrawCountSortAndLimit()"
Task T007: "testUnauthenticated401() + testForbiddenRole403()"

# After T004 done + tests failing, implement:
Task T008: "getLotterySalesRanking() in ReportServiceImpl"
# After T008 done:
Task T009: "POST /lottery-sales in AdminReportController"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Verify project context (T001)
2. Complete Phase 2: Create DTOs + service interface (T002, T003, T004) — both DTOs in parallel
3. Write US1 tests (T005, T006, T007 in parallel) — confirm they FAIL
4. Complete Phase 3: Service impl (T008) → Controller endpoint (T009)
5. **STOP and VALIDATE**: T005–T007 pass; `curl -X POST localhost:8080/admin/report/lottery-sales` with StoreOwner JWT returns correct drawCount-sorted list
6. Deploy/demo if ready — US1 is independently complete

### Incremental Delivery

1. Phase 2 → DTOs + interface (T002–T004) — parallel pair
2. Phase 3 US1 (T005–T009) → drawCount ranking + StoreOwner isolation → MVP testable
3. Phase 4 US2 (T010–T012) → Revenue correctness + sortBy=revenue → adds revenue dimension
4. Phase 5 US3 (T013–T015) → Admin cross-store verified → platform-wide analysis available
5. Phase 6 Polish (T016–T017) → Swagger complete + SC-001/SC-002/SC-003 signed off → production ready

### Notes

- **No DDL changes**: Zero risk to existing tables — pure read-only queries
- **5 files touched**: 2 new DTOs, 1 new test class, 2 existing files modified (service + controller)
- **T008 is the heart**: Contains the full SQL with both subqueries; US2 and US3 are verification phases on top of T008
- **SQL injection prevention**: `sortBy` whitelist (`drawCount`/`revenue`) in T008; `LIMIT ?` uses parameterised binding via `Math.min`
- **Test file accumulation**: T005 creates `AdminReportControllerLotteryRankingTest.java`; subsequent test tasks (T006, T007, T010, T011, T013, T014) add methods to the same file
- **Commit strategy**: Commit after each phase checkpoint for clean rollback points
