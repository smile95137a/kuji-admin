# Research: 033 - 平台營收總覽報表

## Decision 1: 使用專用 DTO，而非重用既有 RevenueReport DTO

- **Decision**: 新增 `PlatformRevenueReportCondition` 與 `PlatformRevenueReportRes`
- **Rationale**: 既有 `RevenueReportRes` 以店家營業額為主，欄位為 `totalOrders`、`totalDraws`、`storeDetails`，與本功能的 `totalRecharge`、`totalSpend`、`spendByType`、`storeBreakdown` 語意不同；硬重用會造成命名與 Swagger 合約混亂。
- **Alternatives considered**:
  - 重用 `RevenueReportCondition/Res`
  - 以 `Map<String,Object>` 臨時回傳

## Decision 2: 使用 JdbcTemplate 聚合 SQL，維持與既有 report layer 一致

- **Decision**: 在 `ReportServiceImpl` 內以多個 `JdbcTemplate` helper query 組裝回應
- **Rationale**: 034、030 等報表已採此模式，便於控制 SQL 與效能，不需新增 Mapper XML 或 MBG 實體。
- **Alternatives considered**:
  - 新增 MyBatis Mapper + XML
  - 以 JPA / Criteria API 重寫聚合

## Decision 3: `dailyRevenue` 缺漏日期在 Java 端補零

- **Decision**: SQL 只查出有交易的日期，Java 端以 `[startDate..endDate]` 逐日回填 `recharge=0`、`spend=0`、`net=0`
- **Rationale**: MySQL 端做日期序列較繁瑣，且本專案既有報表沒有日曆表；Java 端補零更簡單且易測試。
- **Alternatives considered**:
  - MySQL recursive CTE 產生日曆
  - 建立專用 calendar table

## Decision 4: `growthRate` 的上期定義採相同天數的前一區間

- **Decision**: 若查詢 `[startDate, endDate]`，上期區間為往前推相同天數的連續區間
- **Rationale**: 已在 spec 的 Session 2026-05-06 澄清，且可套用到 recharge 與 spend 兩個 growth rate
- **Alternatives considered**:
  - 固定月比月
  - 固定週比週

## Decision 5: `storeBreakdown` 只統計 DRAW 交易，無法映射店家者視為資料異常跳過

- **Decision**: `storeBreakdown` 的 base dataset 與 `totalSpend` 一致，僅取 `wallet_transaction.transaction_type='DRAW'`
- **Rationale**: FR-003 已明確定義 `totalSpend` 只含 DRAW；若 `storeBreakdown` 混入其他交易型別，會使 SC-002 失真。對於 `related_id` 無法映射到 `lottery_ticket` / `order` 的異常資料，不顯示 unknown bucket，直接跳過。
- **Alternatives considered**:
  - 顯示 `Unknown` bucket
  - 將所有交易型別都納入 storeBreakdown

## Decision 6: 稽核記錄沿用既有 `@AuditLog` AOP

- **Decision**: 在 `AdminReportController` 的 `/platform-revenue` 端點加上 `@AuditLog`
- **Rationale**: NFR-003 要求記錄敏感財務資料存取；專案已存在 `AuditLogAspect`，無須另建專用報表 audit service。
- **Alternatives considered**:
  - 在 controller/service 手動插入 `log_admin_action`
  - 不做稽核，僅一般 log

## Decision 7: V033 索引 migration 採 INFORMATION_SCHEMA + PREPARE 的安全寫法

- **Decision**: 不使用 `ADD INDEX IF NOT EXISTS`，改用查 `INFORMATION_SCHEMA.STATISTICS` 後 `CREATE INDEX`
- **Rationale**: 目前 MySQL 環境對 `ALTER TABLE ... ADD INDEX IF NOT EXISTS` 會報 1064；repo 舊 SQL 也已有相同安全模式。
- **Alternatives considered**:
  - 直接 `CREATE INDEX`（重複執行會失敗）
  - `ALTER TABLE ... ADD INDEX IF NOT EXISTS`（當前環境不支援）
