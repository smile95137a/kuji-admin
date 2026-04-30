# Research: 029 - 獎品出貨報表

> Phase 0 output — all NEEDS CLARIFICATION resolved before Phase 1 design.

---

## R-001: 報表 SQL 執行層

**Decision**: 使用 `JdbcTemplate` 直接執行原生 SQL 查詢  
**Rationale**: 現有所有報表（Revenue、Referral、LotteryResult、Recharge、Bonus）均使用 `JdbcTemplate`，複雜聚合查詢不適合 MyBatis Example API。`ReportServiceImpl` 已注入 `JdbcTemplate`，可直接沿用。  
**Alternatives Considered**:
- MyBatis Mapper XML custom SQL：可行，但需多建立一個 Mapper 方法；JdbcTemplate 更簡潔直觀，符合現有慣例。
- Spring Data JPA：專案未引入 JPA，排除。

---

## R-002: DDL 策略（adding `preparing_at`）

**Decision**: 在 `order` 表新增 `preparing_at DATETIME NULL`，並透過命名式 migration 腳本 `V029__add_order_preparing_at.sql` 執行  
**Rationale**:
- `DATETIME NULL`（不使用 NOT NULL DEFAULT）確保向後相容——遷移後舊訂單 `preparing_at = NULL`，不影響現有資料。
- 專案有 `sql/` 目錄下多個 migration 檔案（含版本化命名如 `V_2026_04_14__xxx.sql`、`V012__xxx.sql`）；本功能使用 `V029__` 前綴對齊其他報表功能編號。
- MBG 設定檔（`generatorConfig.xml`）指向真實 DB；若需重新跑 MBG，可執行 `run-mbg.ps1`。但本次為避免覆蓋手工改動，手動更新 `Order.java`、`OrderMapper.xml`、`OrderExample.java`。  
**Alternatives Considered**:
- 只更新 `OrderServiceImpl`（跳過 DDL/Entity）：無法在查詢時過濾/計算 `preparing_at`，排除。
- 用 `OrderStatusLog` 回推 PREPARING 時間：可行但需額外 JOIN，效能差且複雜度高，排除。

---

## R-003: Order 實體與 Mapper 更新策略

**Decision**: 手動更新（不重跑 MBG），修改三個檔案：
1. `Order.java` — 新增 `private LocalDateTime preparingAt;` + getter/setter
2. `OrderMapper.xml` — 在 `BaseResultMap` 新增 `<result column="preparing_at" jdbcType="TIMESTAMP" property="preparingAt" />`；在 `Base_Column_List`、`insert`、`insertSelective`、`updateByPrimaryKey`、`updateByPrimaryKeySelective` 加入欄位
3. `OrderExample.java` — 若需按 preparingAt 篩選，加入對應 criterion（本次報表透過 JdbcTemplate 查詢，OrderExample 不需強制更新）

**Rationale**: 專案遵循 DDL-first → MBG 模式，但 MBG 會覆蓋全部已有手工改動。手動方式最安全，修改量有限且明確。  
**Alternatives Considered**: 重跑 MBG：快速但高風險（覆蓋 `OrderRepository`、`OrderMapper` 自訂方法），排除。

---

## R-004: avgShipDays 計算公式

**Decision**: `ROUND(AVG(DATEDIFF(shipped_at, preparing_at)), 1)` WHERE `status IN ('SHIPPED', 'COMPLETED') AND preparing_at IS NOT NULL AND shipped_at IS NOT NULL`  
**Rationale**:
- Spec 明確：`preparing_at → shipped_at` 的天數差（SC-003 精確至 0.1 天）。
- `DATEDIFF` 回傳整數天（MySQL）；`AVG` 後 `ROUND(..., 1)` 達成 0.1 精度。
- `preparing_at IS NOT NULL` 排除新功能上線前建立的訂單（spec edge case）。
- 無符合筆數時 SQL 回傳 `NULL`，Java 端傳回 `null`（符合 spec edge case）。  
**Alternatives Considered**:
- `TIMESTAMPDIFF(HOUR, ...)` 計算小時再除 24：更精確但 spec 說「天數」且精確至 0.1，DATEDIFF+ROUND 已足夠，排除。

---

## R-005: overdueCount 定義與 SQL

**Decision**: `COUNT(*) WHERE status = 'PENDING' AND DATEDIFF(NOW(), created_at) > 7`，加上 storeId 過濾  
**Rationale**:
- Spec：「超過 7 天仍在 PENDING 的訂單」，逾期基準是 `created_at`（訂單建立時間），不是 `preparing_at`（尚為 null）。
- `DATEDIFF(NOW(), created_at) > 7`（嚴格大於 7 天）。
- 不受查詢日期範圍約束（逾期訂單是「目前」狀態）；但需加 storeId 隔離。  
**Alternatives Considered**:
- 用 `created_at < NOW() - INTERVAL 7 DAY`：等效且更易讀，採用此寫法。

---

## R-006: dailyDetails 內容

**Decision**: 按 `shipped_at` 日期分組，只計 status 已到 SHIPPED 或 COMPLETED 的訂單，欄位為 `{ date, shippedCount }`  
**Rationale**: Spec 澄清欄位：「只計 SHIPPED 狀態變更的實際出貨數（按 shipped_at 日期分組）」。`shipped_at` 為 OrderMapper 現有欄位，無需額外 DDL。需過濾 `shipped_at BETWEEN startDate AND endDate`。  
**Alternatives Considered**:
- 每日各狀態計數：Spec 澄清已明確只計出貨數，排除。

---

## R-007: storeDetails（Admin 限定）

**Decision**: 只在 `storeId == null`（Admin 無店家約束）時計算，GROUP BY `store_id`，回傳每家店的 `storeName`、`avgShipDays`、各狀態計數  
**Rationale**:
- Spec FR-006：Admin 查詢回傳 `storeDetails`；StoreOwner 不需此欄位（回傳 null 或 empty）。
- `storeName` 可直接從 `order` 表的 `store_name` 欄位取得（已有 denormalized 欄位），不需 JOIN `store` 表。  
**Alternatives Considered**:
- 所有角色都回傳 storeDetails：浪費資源且 StoreOwner 不需要，排除。

---

## R-008: 效能索引

**Decision**: 新增兩個複合索引以確保 SC-001（10k 筆 < 3 秒）：
```sql
CREATE INDEX idx_order_store_status_created ON `order`(store_id, status, created_at);
CREATE INDEX idx_order_store_shipped_at     ON `order`(store_id, shipped_at);
```
**Rationale**:
- 狀態計數 + overdueCount 主要過濾 `store_id` + `status` + `created_at`。
- dailyDetails 主要過濾 `store_id` + `shipped_at`。
- 10,000 筆資料量，InnoDB B-tree 複合索引足夠達到 < 3 秒目標。  
**Alternatives Considered**:
- 單欄 `store_id` 索引：選擇性不足，排除。
- `preparing_at` 索引：avgShipDays 為 full scan on filtered result，無需獨立索引。

---

## R-009: 預設日期範圍

**Decision**: 若 `startDate` 或 `endDate` 為 null，後端預設補足 `endDate = today`、`startDate = today - 29 days`（最近 30 天，含今日）  
**Rationale**: Spec FR-008：預設最近 30 天。現有 `RevenueReportCondition` 無預設值邏輯，本次在 `ReportServiceImpl` 加入 null 判斷。  
**Alternatives Considered**:
- 在 Condition 類別加 `@JsonDeserialize`：過度設計，排除。

---

## Summary Table

| # | Question | Resolution |
|---|----------|------------|
| R-001 | 報表用 JdbcTemplate 還是 Mapper？ | JdbcTemplate（現有慣例） |
| R-002 | DDL migration 策略？ | V029__add_order_preparing_at.sql，NULLABLE |
| R-003 | 是否重跑 MBG？ | 手動更新 Order.java + OrderMapper.xml |
| R-004 | avgShipDays 公式？ | ROUND(AVG(DATEDIFF(shipped_at, preparing_at)), 1) |
| R-005 | overdueCount 基準時間？ | created_at（建立時間），嚴格 > 7 天 |
| R-006 | dailyDetails 計什麼？ | 按 shipped_at 分組的每日出貨筆數 |
| R-007 | storeDetails 何時回傳？ | 僅 Admin（storeId == null） |
| R-008 | 索引策略？ | 兩個複合索引涵蓋主要查詢路徑 |
| R-009 | 預設日期範圍？ | 後端填補：最近 30 天 |
