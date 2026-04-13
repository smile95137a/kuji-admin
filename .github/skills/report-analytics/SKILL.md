---
name: report-analytics
description: "報表與統計分析指南。銷售報表、訂單分析、用戶統計、數據彙總、報表查詢 API。"
---

# 報表統計

## When to Use
- 新增報表統計 API
- 修改 JdbcTemplate 聚合查詢
- 了解 ReportSnapshot 快照機制
- 調整報表時間範圍或分組邏輯

## 核心原則
- **三種報表類別**：銷售報表（窗口商品）、訂單分析（訂單約一月）、用戶統計
- **分組計算**：按時間粗度分組（日/月/年）、按店家/水平分組
- **JdbcTemplate 聚合查詢**：使用 JdbcTemplate 而非 MyBatis，跨表必用 SUM/COUNT/GROUP BY
- **狀態數據轉移**：訂單狀態轉移（不遲/已謝/我）購對狀態數據變更記錄

---

## 核心設計：JdbcTemplate 聚合查詢

> ⚠️ 報表統計使用 `JdbcTemplate` 而非 MyBatis Example，因為需要跨表聚合（SUM、COUNT、GROUP BY）。

```java
// ReportServiceImpl 使用 JdbcTemplate
private final JdbcTemplate jdbcTemplate;
private final ReportSnapshotMapper reportSnapshotMapper;
```

---

## 營業額報表

```java
// POST /admin/report/revenue
// Body: { "condition": { "storeId": "...", "startDate": "2025-01-01", "endDate": "2025-12-31" } }

public RevenueReportRes getRevenueReport(QueryReq<RevenueReportCondition> req) {
    RevenueReportCondition condition = req.getCondition();
    String storeId = condition.getStoreId(); // null = 全平台（Admin only）

    StringBuilder sql = new StringBuilder("""
        SELECT
            COALESCE(SUM(wt.amount), 0)  AS total_revenue,
            COUNT(DISTINCT wt.id)         AS total_transactions,
            COUNT(DISTINCT wt.user_id)    AS unique_users
        FROM wallet_transaction wt
        WHERE wt.transaction_type = 'DRAW'
          AND wt.coin_type = 'GOLD'
          AND wt.amount < 0  -- 扣點（負數）才算收入
    """);

    List<Object> params = new ArrayList<>();

    if (storeId != null) {
        // 需 JOIN lottery 表過濾 store
        sql.append(" AND wt.related_id IN (SELECT id FROM lottery WHERE store_id = ?)");
        params.add(storeId);
    }

    if (condition.getStartDate() != null) {
        sql.append(" AND wt.created_at >= ?");
        params.add(condition.getStartDate().atStartOfDay());
    }

    if (condition.getEndDate() != null) {
        sql.append(" AND wt.created_at < ?");
        params.add(condition.getEndDate().plusDays(1).atStartOfDay());
    }

    Map<String, Object> row = jdbcTemplate.queryForMap(sql.toString(), params.toArray());
    // ... 組裝 RevenueReportRes
}
```

---

## 日結報表（每日統計）

```java
// POST /admin/report/daily
public List<DailyReportRes> getDailyReport(QueryReq<DailyReportCondition> req) {
    String sql = """
        SELECT
            DATE(wt.created_at)           AS report_date,
            COUNT(DISTINCT wt.user_id)    AS active_users,
            COUNT(*)                      AS total_draws,
            COALESCE(SUM(ABS(wt.amount)), 0) AS revenue
        FROM wallet_transaction wt
        WHERE wt.transaction_type = 'DRAW'
          AND wt.created_at BETWEEN ? AND ?
        GROUP BY DATE(wt.created_at)
        ORDER BY report_date ASC
    """;

    return jdbcTemplate.query(sql,
        (rs, rowNum) -> DailyReportRes.builder()
            .reportDate(rs.getDate("report_date").toLocalDate())
            .activeUsers(rs.getInt("active_users"))
            .totalDraws(rs.getInt("total_draws"))
            .revenue(rs.getLong("revenue"))
            .build(),
        startDate, endDate
    );
}
```

---

## ReportSnapshot 快照機制

避免每次都跑慢查詢，定時（每日深夜）將報表存入 `report_snapshot` 表：

```java
// ScheduledReportTask.java（每日凌晨 1 點執行）
@Scheduled(cron = "0 0 1 * * ?")
public void generateDailySnapshot() {
    LocalDate yesterday = LocalDate.now().minusDays(1);

    RevenueReportRes report = reportService.getRevenueReport(...yesterday...);

    ReportSnapshot snapshot = new ReportSnapshot();
    snapshot.setId(UUID.randomUUID().toString());
    snapshot.setSnapshotDate(yesterday);
    snapshot.setSnapshotType("DAILY");
    snapshot.setDataJson(objectMapper.writeValueAsString(report));
    snapshot.setCreatedAt(LocalDateTime.now());
    reportSnapshotMapper.insert(snapshot);

    log.info("✅ 日報快照已生成: {}", yesterday);
}
```

### 快照查詢（讀取已存快照）

```java
// 查詢已有快照（快速）
ReportSnapshotExample example = new ReportSnapshotExample();
example.createCriteria()
    .andSnapshotDateEqualTo(targetDate)
    .andSnapshotTypeEqualTo("DAILY");
List<ReportSnapshot> snapshots = reportSnapshotMapper.selectByExample(example);

if (!snapshots.isEmpty()) {
    return objectMapper.readValue(snapshots.get(0).getDataJson(), RevenueReportRes.class);
}
// 快照不存在：即時計算（較慢）
return reportService.getRevenueReport(...);
```

---

## 權限控制

| 報表種類 | ADMIN | STORE_OWNER | STORE_EDITOR |
|---------|-------|-------------|-------------|
| 全平台報表 | ✅ | ❌ | ❌ |
| 自家店報表 | ✅ | ✅（storeId自動帶入） | ❌ |

```java
// Controller 中自動帶入 storeId
if (!SecurityUtils.isAdmin()) {
    String storeId = SecurityUtils.getCurrentUserPrimaryStoreId();
    req.getCondition().setStoreId(storeId);
}
```

---

## ⚠️ 禁止操作

- ❌ 報表查詢不要用 MyBatis Example（用 JdbcTemplate）
- ❌ 不要在沒有日期範圍限制的情況下查詢全量資料（避免 OOM）
- ❌ 不要讓 StoreOwner 查看其他店家的報表
- ❌ 快照不存在時不要報錯（改為即時計算）
- ❌ 不要直接讓前端傳 SQL 片段（SQL injection 風險）
