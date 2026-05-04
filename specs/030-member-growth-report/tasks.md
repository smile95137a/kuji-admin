# Tasks: 030 - 會員成長報表

**Input**: Design documents from `/specs/030-member-growth-report/`
**Feature branch**: `030-member-growth-report`
**Generated**: 2026-04-28

---

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1, US2, US3)
- Exact file paths included in all descriptions

---

## Phase 1: Setup

**Purpose**: Confirm worktree is clean and target files do not yet exist

- [ ] T001 Verify no pre-existing MemberGrowthReport* files in `src/main/java/com/group/admin/condition/report/` and `src/main/java/com/group/admin/dto/res/report/`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Shared Condition class, Response DTO, service interface method, Controller endpoint shell, and ReportServiceImpl skeleton — all user story SQL phases depend on these.

**⚠️ CRITICAL**: No user story SQL work can begin until T002–T006 are complete.

- [ ] T002 [P] Create `MemberGrowthReportCondition` with `startDate` / `endDate` fields (extends `BaseCondition`, `@Data @EqualsAndHashCode(callSuper=true) @Schema`) in `src/main/java/com/group/admin/condition/report/MemberGrowthReportCondition.java`
- [ ] T003 [P] Create `MemberGrowthReportRes` DTO with all fields (`totalNewMembers`, `growthRate`, `registrationByProvider`, `dailyNewMembers`, `activeMembers`, `arpuGold`, `arpuBonus`, `retention7Days`, `retention30Days`) and nested static `DailyNewMember` class (`@Data @Builder`) in `src/main/java/com/group/admin/dto/res/report/MemberGrowthReportRes.java`
- [ ] T004 Add `getMemberGrowthReport(QueryReq<MemberGrowthReportCondition> req)` method signature with Javadoc to `src/main/java/com/group/admin/service/ReportService.java`
- [ ] T005 Add `@PostMapping("/member-growth") @PreAuthorize("hasRole('ADMIN')")` endpoint delegating to `reportService.getMemberGrowthReport(req)` in `src/main/java/com/group/admin/controller/admin/AdminReportController.java`
- [ ] T006 Create `getMemberGrowthReport()` skeleton `@Override` method in `src/main/java/com/group/admin/service/impl/ReportServiceImpl.java`: resolve null condition, apply default dates (`endDate=today`, `startDate=today-29`), convert to `LocalDateTime`, log entry, return empty `MemberGrowthReportRes.builder().build()` (all SQL stubs left as `// TODO`)

**Checkpoint**: Project compiles — endpoint returns HTTP 200 with empty/null fields; foundation ready for user story SQL phases

---

## Phase 3: User Story 1 — 新增會員統計 (Priority: P1) 🎯 MVP

**Goal**: `totalNewMembers`, `growthRate`, `registrationByProvider`, and `dailyNewMembers` (with zero-fill) are fully populated

**Independent Test**: `POST /admin/report/member-growth` with 30-day range returns `totalNewMembers = 150`, `dailyNewMembers.length = 30`, `registrationByProvider = {GOOGLE:80, EMAIL:70}` (TC-001 in contract)

### Implementation for User Story 1

- [ ] T007 [P] [US1] Implement **Q1** in `src/main/java/com/group/admin/service/impl/ReportServiceImpl.java`: query `SELECT COUNT(*) FROM user WHERE created_at BETWEEN ? AND ?` for `totalNewMembers`; derive previous period window (same duration ending just before `startDate`), run identical query for `prevPeriodCount`; compute `growthRate = (current-prev)/prev*100` scale=1 HALF_UP, null when `prevPeriodCount = 0`
- [ ] T008 [P] [US1] Implement **Q2** in `src/main/java/com/group/admin/service/impl/ReportServiceImpl.java`: query `SELECT DATE(created_at), COUNT(*) FROM user WHERE created_at BETWEEN ? AND ? GROUP BY DATE(created_at)`; iterate every day from `startDate` to `endDate` and fill with DB result or 0 to build `List<DailyNewMember>` (length = query range days)
- [ ] T009 [P] [US1] Implement **Q3** in `src/main/java/com/group/admin/service/impl/ReportServiceImpl.java`: query `SELECT provider, COUNT(*) FROM user WHERE created_at BETWEEN ? AND ? GROUP BY provider`; collect into `Map<String,Integer>` for `registrationByProvider` (empty map when no data)

**Checkpoint**: US1 fields correct and independently testable via MockMvc or curl

---

## Phase 4: User Story 2 — 活躍度與 ARPU (Priority: P2)

**Goal**: `activeMembers`, `arpuGold`, and `arpuBonus` are fully populated using the broad active-member definition (login OR recharge OR draw OR order)

**Independent Test**: With `activeMembers=500`, `totalGoldDraw=100000`, `totalBonusDraw=20000` → `arpuGold=200.0`, `arpuBonus=40.0` (TC-002); user with BONUS-only draw still counted in `activeMembers` (TC-003)

### Implementation for User Story 2

- [ ] T010 [P] [US2] Implement **Q4** in `src/main/java/com/group/admin/service/impl/ReportServiceImpl.java`: `SELECT COUNT(DISTINCT user_id) FROM (SELECT id AS user_id FROM user WHERE last_login_at BETWEEN ? AND ? UNION SELECT user_id FROM wallet_transaction WHERE transaction_type='RECHARGE' AND created_at BETWEEN ? AND ? UNION SELECT drawn_by AS user_id FROM lottery_ticket WHERE status='DRAWN' AND drawn_at BETWEEN ? AND ? UNION SELECT user_id FROM \`order\` WHERE created_at BETWEEN ? AND ?) t` for `activeMembers`
- [ ] T011 [US2] Implement **Q5 + Q6** in `src/main/java/com/group/admin/service/impl/ReportServiceImpl.java`: Q5 = `SUM(amount) WHERE transaction_type='DRAW' AND coin_type='GOLD' AND created_at BETWEEN ? AND ?` for gold total; Q6 = same with `coin_type='BONUS'` for bonus total; compute `arpuGold = goldTotal / activeMembers` scale=1 HALF_UP (returns `BigDecimal("0.0")` when `activeMembers=0`); same for `arpuBonus`

**Checkpoint**: US2 fields correct; `activeMembers=0` returns `0.0` for both ARPU fields (no ArithmeticException)

---

## Phase 5: User Story 3 — 7/30 天留存率 (Priority: P3)

**Goal**: `retention7Days` and `retention30Days` are correctly computed from the previous complete month's new members, independent of the query date range

**Independent Test**: With 100 prev-month new members, 60 re-active within 7 days → `retention7Days=60.0`; 35 re-active within 30 days → `retention30Days=35.0` (TC-004); prev-month has 0 new members → both fields are `null`

### Implementation for User Story 3

- [ ] T012 [US3] Implement **Q7 + Q8** in `src/main/java/com/group/admin/service/impl/ReportServiceImpl.java`: compute `prevMonthStart = LocalDate.now().minusMonths(1).withDayOfMonth(1)` and `prevMonthEnd = prevMonthStart.plusMonths(1).minusDays(1)`; Q7 = `SELECT COUNT(*) FROM user WHERE created_at BETWEEN prevMonthStart AND prevMonthEnd+1day` for `prevMonthTotal`; return `null` for both retention fields when `prevMonthTotal=0`; Q8 (7-day window) = for each prev-month new member (`id`, `created_at`), count those with any active event after `created_at` and within `(created_at, created_at+7days]` using 4-table UNION (same pattern as Q4 but per-user window), explicitly excluding the registration instant; `retention7Days = retainedCount7 / prevMonthTotal * 100` scale=1 HALF_UP; repeat with 30-day window for `retention30Days`

**Checkpoint**: US3 fields correct; null returned when prev-month has no data

---

## Phase 6: Controller Tests

**Purpose**: MockMvc + Mockito coverage for the new endpoint per quickstart.md § Step 6

- [ ] T013 [P] Write MockMvc test — ADMIN token, `POST /admin/report/member-growth` with valid condition → HTTP 200, response body contains all expected fields — in `src/test/java/com/group/admin/controller/AdminReportControllerTest.java`
- [ ] T014 [P] Write MockMvc test — STORE_OWNER token → HTTP 403 — in `src/test/java/com/group/admin/controller/AdminReportControllerTest.java`
- [ ] T015 [P] Write MockMvc test — `condition = null` (omitted in JSON) → HTTP 200, `startDate = today-29`, `endDate = today` in response — in `src/test/java/com/group/admin/controller/AdminReportControllerTest.java`
- [ ] T016 [P] Write MockMvc test — mocked service returns all-zero data — response has `totalNewMembers=0`, `arpuGold=0.0`, `dailyNewMembers` non-null (empty list or zero-filled), `retention7Days=null` — in `src/test/java/com/group/admin/controller/AdminReportControllerTest.java`
- [ ] T017 [P] Write MockMvc test — `activeMembers=0` case — response has `arpuGold=0.0`, `arpuBonus=0.0` (no ArithmeticException) — in `src/test/java/com/group/admin/controller/AdminReportControllerTest.java`

**Checkpoint**: Run `mvn test -pl admin` — all 5 new tests pass

---

## Phase 7: Polish & Cross-Cutting Concerns

- [ ] T018 [P] Add `@Operation(summary = "會員成長報表", description = "Admin Only")` and `@ApiResponses` Swagger annotations to the new endpoint in `src/main/java/com/group/admin/controller/admin/AdminReportController.java`
- [ ] T019 Run `mvn test -pl admin` from repo root and confirm all existing + new tests pass (zero regressions)
- [ ] T020 Validate API response JSON structure against `specs/030-member-growth-report/contracts/POST_admin_report_member-growth.md` (field names, types, nullable rules) — fix any mismatches

---

## Dependencies & Execution Order

### Phase Dependencies

```
Phase 1 (Setup)
    └─► Phase 2 (Foundational) — BLOCKS all story phases
            ├─► Phase 3 (US1) — independent of US2, US3
            ├─► Phase 4 (US2) — T011 depends on T010 (needs activeMembers)
            └─► Phase 5 (US3) — independent of US1, US2
                        └─► Phase 6 (Tests) — can start after Phase 2; parallel with story phases
                                    └─► Phase 7 (Polish)
```

### User Story Dependencies

| Story | Depends on | Notes |
|---|---|---|
| **US1** | Phase 2 complete | Q1, Q2, Q3 all parallelizable with each other |
| **US2** | Phase 2 complete; T011 depends on T010 | Q4 then Q5+Q6 |
| **US3** | Phase 2 complete | Fully independent retention SQL |
| **Tests** | Phase 2 complete (Mockito mocks service layer) | All 5 test tasks parallelizable |

### Within User Story 2

- T010 (Q4 activeMembers) must complete before T011 (Q5+Q6 ARPU uses `activeMembers` variable)

---

## Parallel Opportunities

### Phase 2 Parallel

```
# T002 and T003 can run simultaneously (different new files):
Task: "Create MemberGrowthReportCondition.java"     → T002
Task: "Create MemberGrowthReportRes.java"           → T003
```

### Phase 3 (US1) Parallel — all 3 SQL blocks are in the same file but different methods

```
# After T006 skeleton is in place, Q1/Q2/Q3 touch separate code blocks in ReportServiceImpl:
Task: T007 Q1 totalNewMembers + growthRate
Task: T008 Q2 dailyNewMembers zero-fill
Task: T009 Q3 registrationByProvider
```

### Phase 6 (Tests) Parallel

```
# All 5 test methods are independent MockMvc test cases in the same file:
Task: T013 ADMIN 200 test
Task: T014 STORE_OWNER 403 test
Task: T015 null condition defaults test
Task: T016 all-zero data test
Task: T017 activeMembers=0 ARPU test
```

---

## Implementation Strategy

### MVP First (US1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL — compiles; endpoint returns 200)
3. Complete Phase 3: US1 (totalNewMembers, growthRate, registrationByProvider, dailyNewMembers)
4. **STOP and VALIDATE**: POST `/admin/report/member-growth` returns correct US1 fields
5. Continue with US2 → US3

### Incremental Delivery

1. Phase 1 + 2 → endpoint live, all fields null/default
2. Phase 3 (US1) → new-member stats work ✅
3. Phase 4 (US2) → activity + ARPU work ✅
4. Phase 5 (US3) → retention rates work ✅
5. Phase 6 + 7 → tests green, Swagger docs updated ✅

---

## Notes

- All SQL uses `JdbcTemplate` — no new repository/framework (see plan.md § Technical Context)
- `BigDecimal` precision: scale=1, `RoundingMode.HALF_UP` for all ARPU and retention fields
- `activeMembers=0` guard: check before division, return `BigDecimal("0.0")` (never throw `ArithmeticException`)
- `prevMonthTotal=0` guard: return `null` for both retention fields
- `dailyNewMembers` zero-fill: iterate every day in `[startDate, endDate]` inclusive, map DB results by date, default missing dates to `count=0`
- Security: `@PreAuthorize("hasRole('ADMIN')")` — no `storeId` injection needed (platform-level report)
- Reference existing impl: `ReportServiceImpl.getRevenueReport()` / `getRechargeReport()` for JdbcTemplate patterns
