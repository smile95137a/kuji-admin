# Tasks: 033 - 平台營收總覽報表

**Branch**: `033-platform-revenue-report`  
**Input**: Design documents from `/specs/033-platform-revenue-report/`  
**Tests**: 包含 controller 測試與編譯驗證  
**Organization**: Tasks grouped by user story to enable independent implementation and testing.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: 可平行執行（不同檔案、無未完成依賴）
- **[Story]**: 對應 user story（US1、US2、US3）
- 所有任務都包含明確檔案路徑

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: 確認既有 report layer 可擴充，並準備 033 所需 SQL migration。

- [X] T001 驗證 `src/main/java/com/group/admin/controller/admin/AdminReportController.java`、`src/main/java/com/group/admin/service/ReportService.java`、`src/main/java/com/group/admin/service/impl/ReportServiceImpl.java` 可作為 033 擴充入口
- [X] T002 更新並驗證 `sql/V033__add_wallet_transaction_indexes.sql`，使用安全索引建立寫法（`INFORMATION_SCHEMA.STATISTICS` + `CREATE INDEX`）

**Checkpoint**: 033 的程式與 DB 入口確認完成，可進入基礎建模。

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: 建立 DTO、service interface 與 API 合約，所有 user story 都依賴這些基礎物件。

⚠️ **CRITICAL**: 本階段完成前，不應開始任何 user story 實作。

- [X] T003 [P] 建立 `src/main/java/com/group/admin/condition/report/PlatformRevenueReportCondition.java`，包含 `startDate`、`endDate` 與 Swagger 註解
- [X] T004 [P] 建立 `src/main/java/com/group/admin/dto/res/report/PlatformRevenueReportRes.java`，包含 `SpendByType`、`DailyRevenueItem`、`StoreBreakdownItem` inner classes
- [X] T005 在 `src/main/java/com/group/admin/service/ReportService.java` 新增 `PlatformRevenueReportRes getPlatformRevenueReport(QueryReq<PlatformRevenueReportCondition> req);`
- [X] T006 在 `src/main/java/com/group/admin/controller/admin/AdminReportController.java` 規劃 `POST /admin/report/platform-revenue` 端點，限制 `ADMIN` 與 `@AuditLog`

**Checkpoint**: 033 的 API 邊界與 service 合約固定，接下來可進入功能實作。

---

## Phase 3: User Story 1 - Admin 查看平台整體營收 (Priority: P1) 🎯 MVP

**Goal**: 回傳 `totalRecharge`、`totalSpend`、`netRevenue`、`drawCount`、`spendByType`、`dailyRevenue`。

**Independent Test**: `POST /admin/report/platform-revenue` 回 200，內含上述欄位；`dailyRevenue` 天數連續且無交易日補零。

### Tests for User Story 1

- [X] T007 [P] [US1] 建立 `src/test/java/com/group/admin/controller/admin/AdminReportControllerPlatformRevenueTest.java`，驗證 Admin 呼叫 `/admin/report/platform-revenue` 回 200 並含 `totalRecharge`、`totalSpend`、`netRevenue`
- [X] T008 [P] [US1] 在 `src/test/java/com/group/admin/controller/admin/AdminReportControllerPlatformRevenueTest.java` 增加 `condition=null` 時預設日期區間的驗證

### Implementation for User Story 1

- [X] T009 [US1] 在 `src/main/java/com/group/admin/service/impl/ReportServiceImpl.java` 實作 `getPlatformRevenueReport()` 骨架：預設日期區間、區間驗證、上期區間計算
- [X] T010 [P] [US1] 在 `src/main/java/com/group/admin/service/impl/ReportServiceImpl.java` 實作 `totalRecharge` 查詢 helper（`RECHARGE + GOLD`）
- [X] T011 [P] [US1] 在 `src/main/java/com/group/admin/service/impl/ReportServiceImpl.java` 實作 `totalSpend` 與 `spendByType` 查詢 helper（`DRAW + GOLD/BONUS`）
- [X] T012 [P] [US1] 在 `src/main/java/com/group/admin/service/impl/ReportServiceImpl.java` 實作 `drawCount` 查詢 helper（`lottery_ticket.status='DRAWN'`）
- [X] T013 [US1] 在 `src/main/java/com/group/admin/service/impl/ReportServiceImpl.java` 實作 `dailyRevenue` 查詢與 Java 端補零邏輯
- [X] T014 [US1] 在 `src/main/java/com/group/admin/controller/admin/AdminReportController.java` 新增 `/platform-revenue` endpoint，`@PreAuthorize("hasRole('ADMIN')")`，並呼叫 service

**Checkpoint**: US1 完成後，Admin 可取得平台總覽與每日趨勢。

---

## Phase 4: User Story 2 - 各店家營收貢獻分解 (Priority: P2)

**Goal**: 回傳 `storeBreakdown[]`，每筆包含 `storeId`、`storeName`、`totalSpend`、`drawCount`，並依 `totalSpend DESC` 排序。

**Independent Test**: `storeBreakdown` 至少包含多店資料，排序正確，且正常資料下 `storeBreakdown.totalSpend` 加總等於 `totalSpend`。

### Tests for User Story 2

- [X] T015 [P] [US2] 在 `src/test/java/com/group/admin/controller/admin/AdminReportControllerPlatformRevenueTest.java` 增加 `storeBreakdown` 陣列與欄位驗證

### Implementation for User Story 2

- [X] T016 [US2] 在 `src/main/java/com/group/admin/service/impl/ReportServiceImpl.java` 實作 `storeBreakdown` 聚合 SQL：`wallet_transaction.related_id` 左連 `lottery_ticket/lottery` 與 `order/store`
- [X] T017 [US2] 在 `src/main/java/com/group/admin/service/impl/ReportServiceImpl.java` 實作 `storeBreakdown` 的 `drawCount` 店家聚合查詢
- [X] T018 [US2] 在 `src/main/java/com/group/admin/service/impl/ReportServiceImpl.java` 組裝 `storeBreakdown` 回應，過濾無法映射店家的異常資料並按 `totalSpend DESC` 排序

**Checkpoint**: US2 完成後，Admin 可看到平台消費由哪些店家貢獻。

---

## Phase 5: User Story 3 - 時間趨勢比較 (Priority: P3)

**Goal**: 回傳 `rechargeGrowthRate`、`spendGrowthRate`，以同長度前一區間做比較；上期無資料時回 `null`。

**Independent Test**: 本期 / 上期有值時成長率正確；上期 `0` 或無資料時回 `null`。

### Tests for User Story 3

- [X] T019 [P] [US3] 在 `src/test/java/com/group/admin/controller/admin/AdminReportControllerPlatformRevenueTest.java` 增加 growth rate 欄位存在與 `null` 回傳驗證
- [X] T020 [P] [US3] 在 `src/test/java/com/group/admin/controller/admin/AdminReportControllerPlatformRevenueTest.java` 增加 StoreOwner 呼叫 `/platform-revenue` 回 403 驗證

### Implementation for User Story 3

- [X] T021 [US3] 在 `src/main/java/com/group/admin/service/impl/ReportServiceImpl.java` 實作上期 `totalRecharge` / `totalSpend` 查詢 helper
- [X] T022 [US3] 在 `src/main/java/com/group/admin/service/impl/ReportServiceImpl.java` 實作 growth rate 計算與 `previous<=0 -> null` 規則
- [X] T023 [US3] 在 `src/main/java/com/group/admin/controller/admin/AdminReportController.java` 補 `@AuditLog` 與 Admin-only 權限限制，確保敏感財務資料存取可稽核

**Checkpoint**: US3 完成後，平台營收總覽具備環比分析能力。

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: 收尾編譯、文件同步與最終驗證。

- [X] T024 [P] 更新 `specs/033-platform-revenue-report/quickstart.md` 的實際驗證結果與注意事項
- [X] T025 [P] 執行 `mvn "-Dtest=AdminReportControllerPlatformRevenueTest" test` 驗證 controller 行為
- [X] T026 執行 `mvn compile` 驗證專案可編譯

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: 無依賴，可立即開始
- **Foundational (Phase 2)**: 依賴 Setup 完成，阻塞所有 user stories
- **US1 (Phase 3)**: 依賴 Foundational 完成
- **US2 (Phase 4)**: 依賴 US1 的 summary/draw query 基礎
- **US3 (Phase 5)**: 依賴 US1 完成後的區間計算骨架
- **Polish (Phase 6)**: 依賴前面所有目標故事完成

### User Story Dependencies

- **US1 (P1)**: 無其他故事依賴，是 MVP
- **US2 (P2)**: 依賴 US1 的整體 spend/draw 統計
- **US3 (P3)**: 依賴 US1 的日期區間與總額查詢邏輯

### Within Each User Story

- Controller 測試先於 endpoint 實作
- Service 骨架先於各 helper query
- 聚合 query 先於 response assembly

---

## Parallel Opportunities

- T003 與 T004 可平行
- T010、T011、T012 可平行
- T015、T019、T020 可平行
- T025 與文件整理可平行

---

## Implementation Strategy

### MVP First (US1 Only)

1. 完成 Phase 1、2
2. 完成 Phase 3（US1）
3. 驗證 `/admin/report/platform-revenue` 可返回平台總覽
4. 確認 `dailyRevenue` 日期補零正確

### Incremental Delivery

1. 先交付 US1：平台整體營收
2. 再交付 US2：店家貢獻拆分
3. 最後交付 US3：環比成長率

---

## Notes

- `storeBreakdown` 的 base dataset 與 `totalSpend` 一致，僅統計 `transaction_type='DRAW'`
- `unknown bucket` 不顯示；無法映射的資料視為異常略過
- 索引 migration 屬於 033 必要配套，不是可選優化
