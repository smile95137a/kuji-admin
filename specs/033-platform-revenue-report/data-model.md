# Data Model: 033 - 平台營收總覽報表

本功能不新增資料表，僅新增查詢條件 DTO、回應 DTO，並依既有表做聚合。

## 1. 查詢條件：PlatformRevenueReportCondition

**檔案**：`src/main/java/com/group/admin/condition/report/PlatformRevenueReportCondition.java`

| 欄位 | 型別 | 必填 | 說明 |
|------|------|------|------|
| `startDate` | `LocalDate` | 否 | 查詢開始日期；未傳時預設最近 30 天 |
| `endDate` | `LocalDate` | 否 | 查詢結束日期；未傳時預設今日 |

### Validation Rules

- 若 `startDate == null && endDate == null`：使用預設區間 `[today-29, today]`
- 若只傳其中一端：另一端以預設值補齊
- `startDate` 不可晚於 `endDate`

## 2. 回應模型：PlatformRevenueReportRes

**檔案**：`src/main/java/com/group/admin/dto/res/report/PlatformRevenueReportRes.java`

| 欄位 | 型別 | 說明 |
|------|------|------|
| `startDate` | `LocalDate` | 實際查詢起日 |
| `endDate` | `LocalDate` | 實際查詢迄日 |
| `totalRecharge` | `Long` | `RECHARGE + GOLD` 金額總和 |
| `totalSpend` | `Long` | `DRAW` 金額總和（GOLD + BONUS，取絕對值） |
| `netRevenue` | `Long` | `totalRecharge - totalSpend` |
| `drawCount` | `Long` | 期間 `lottery_ticket.status='DRAWN'` 筆數 |
| `spendByType` | `SpendByType` | 消費幣別拆分 |
| `rechargeGrowthRate` | `BigDecimal` | 相較上期的儲值成長率；無上期資料時 `null` |
| `spendGrowthRate` | `BigDecimal` | 相較上期的消費成長率；無上期資料時 `null` |
| `dailyRevenue` | `List<DailyRevenueItem>` | 每日儲值/消費/淨收入趨勢，缺漏日期補零 |
| `storeBreakdown` | `List<StoreBreakdownItem>` | 各店消費貢獻，依 `totalSpend` 降序 |

### Nested Object: SpendByType

| 欄位 | 型別 | 說明 |
|------|------|------|
| `gold` | `Long` | `DRAW + GOLD` 總額（取絕對值） |
| `bonus` | `Long` | `DRAW + BONUS` 總額（取絕對值） |

### Nested Object: DailyRevenueItem

| 欄位 | 型別 | 說明 |
|------|------|------|
| `date` | `LocalDate` | 日期 |
| `recharge` | `Long` | 當日儲值 |
| `spend` | `Long` | 當日消費 |
| `net` | `Long` | `recharge - spend` |

### Nested Object: StoreBreakdownItem

| 欄位 | 型別 | 說明 |
|------|------|------|
| `storeId` | `String` | 店家 UUID |
| `storeName` | `String` | 店名 |
| `totalSpend` | `Long` | 該店 DRAW 消費總額（取絕對值） |
| `drawCount` | `Long` | 該店期間內抽獎次數 |

## 3. 來源資料與映射

| 報表欄位 | 表 / 來源 | 規則 |
|---------|-----------|------|
| `totalRecharge` | `wallet_transaction` | `transaction_type='RECHARGE' AND coin_type='GOLD'` |
| `totalSpend` | `wallet_transaction` | `transaction_type='DRAW'`，`ABS(SUM(amount))` |
| `drawCount` | `lottery_ticket` | `status='DRAWN'` 且 `drawn_at` 落在區間 |
| `dailyRevenue.recharge` | `wallet_transaction` | `RECHARGE + GOLD`，`GROUP BY DATE(created_at)` |
| `dailyRevenue.spend` | `wallet_transaction` | `DRAW`，`GROUP BY DATE(created_at)` |
| `storeBreakdown.totalSpend` | `wallet_transaction -> lottery_ticket/order -> store` | 以 `related_id` 映射店家後加總 |
| `storeBreakdown.drawCount` | `lottery_ticket -> lottery -> store` | 以店家分組計數 |

## 4. 店家映射規則

`wallet_transaction.related_id` 沒有 `related_type` 欄位，因此採 heuristic:

1. 先 `LEFT JOIN lottery_ticket lt ON wt.related_id = lt.id`
2. 再 `LEFT JOIN lottery l ON lt.lottery_id = l.id`
3. 同時 `LEFT JOIN order o ON wt.related_id = o.id`
4. 店家解析以 `COALESCE(l.store_id, o.store_id)` 為準

### 異常資料處理

- 若 `COALESCE(l.store_id, o.store_id)` 為 `NULL`
  - 視為資料異常
  - 該筆不顯示於 `storeBreakdown`
  - 不產生 unknown bucket

## 5. 上期比較公式

令查詢區間天數為：

```text
days = DAYS.between(startDate, endDate) + 1
prevEnd = startDate.minusDays(1)
prevStart = prevEnd.minusDays(days - 1)
```

### Growth Rate

```text
current > 0, previous = 0/null -> null
previous > 0 -> ((current - previous) / previous) * 100
```

保留 1 位小數，採 `HALF_UP`。
