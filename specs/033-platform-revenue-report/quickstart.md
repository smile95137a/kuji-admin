# Quickstart: 033 - 平台營收總覽報表

## 1. 套用資料庫索引

執行：

```sql
SOURCE sql/V033__add_wallet_transaction_indexes.sql;
```

確認索引存在：

```sql
SHOW INDEX FROM wallet_transaction;
```

應至少看到：

- `idx_wt_related_id`
- `idx_wt_type_coin_created`

## 2. 啟動後端

```bash
mvn spring-boot:run
```

## 3. 呼叫 API

```http
POST /api/admin/report/platform-revenue
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "condition": {
    "startDate": "2026-04-01",
    "endDate": "2026-04-30"
  }
}
```

## 4. 驗收重點

### US1 — 平台整體營收總覽

- 回傳 `totalRecharge`、`totalSpend`、`netRevenue`
- `spendByType.gold + spendByType.bonus == totalSpend`
- `dailyRevenue` 日期筆數應等於查詢天數，無交易日也要出現 `0`

### US2 — 店家營收貢獻

- `storeBreakdown` 每筆含 `storeId`、`storeName`、`totalSpend`、`drawCount`
- 依 `totalSpend` 由大到小排序
- 正常資料下，`storeBreakdown.totalSpend` 加總應等於 `totalSpend`

### US3 — 本期 vs 上期

- `rechargeGrowthRate`、`spendGrowthRate` 依前一個相同天數區間計算
- 若上期值為 `0` 或無資料，growth rate 回 `null`

## 5. 權限驗證

### Admin

- `POST /admin/report/platform-revenue` → `200`

### StoreOwner

- `POST /admin/report/platform-revenue` → `403`

## 6. 測試指令

先跑目標測試：

```bash
mvn "-Dtest=AdminReportControllerPlatformRevenueTest" test
```

再跑基本編譯：

```bash
mvn compile
```
