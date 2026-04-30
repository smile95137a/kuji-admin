# Data Model: 034 - 店家績效比較報表

**Phase**: 1 — Design  
**Date**: 2026-04-28

---

## 新增 Java 類別

### 1. `StorePerformanceCondition` — 查詢條件

**Package**: `com.group.admin.condition.report`  
**File**: `StorePerformanceCondition.java`

```java
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "店家績效報表查詢條件")
public class StorePerformanceCondition extends BaseCondition {

    /** 店家 ID（Admin 可留 null 查全部；StoreOwner 後端強制填入） */
    @Schema(description = "店家 ID（非 Admin 後端自動帶入）")
    private String storeId;

    /** 開始日期（預設：今天往前 30 天） */
    @Schema(description = "開始日期", example = "2026-04-01")
    private LocalDate startDate;

    /** 結束日期（預設：今天） */
    @Schema(description = "結束日期", example = "2026-04-30")
    private LocalDate endDate;
}
```

---

### 2. `StorePerformanceReportRes` — 回應物件

**Package**: `com.group.admin.dto.res.report`  
**File**: `StorePerformanceReportRes.java`

```java
@Data
@Builder
public class StorePerformanceReportRes {

    /** 報表期間起 */
    private LocalDate startDate;

    /** 報表期間迄 */
    private LocalDate endDate;

    /** 各店家績效清單（排序由 sortBy/sortOrder 決定，預設 totalRevenue DESC） */
    private List<StoreItem> stores;

    /**
     * 單店每日趨勢（storeId 非 null 時才回傳；其餘情況為 null）
     */
    private List<DailyStat> dailyStats;

    // ── Inner classes ──────────────────────────────────────────

    @Data
    @Builder
    public static class StoreItem {
        /** 店家 UUID */
        private String storeId;

        /** 店家名稱 (store.store_name) */
        private String storeName;

        /**
         * 期間總抽獎消費金額（來自 wallet_transaction.type='DRAW'，取 ABS）
         * 單位：點數（Long）
         */
        private Long totalRevenue;

        /** 期間抽籤數 (lottery_ticket.status='DRAWN') */
        private Integer drawCount;

        /** 期間活躍不重複用戶數 (抽獎 UNION 建立訂單) */
        private Integer activeUsers;

        /**
         * 出貨率 = (SHIPPED + COMPLETED) / 非 CANCELLED × 100
         * 非 CANCELLED 為 0 時回傳 null
         */
        private Double shipRate;

        /**
         * 逾期率 = PENDING 超過 7 天 / 全部訂單 × 100
         * 全部訂單為 0 時回傳 null
         */
        private Double overdueRate;

        /**
         * 平均出貨天數 (preparing_at → shipped_at)
         * 依賴 029 DDL；029 未合併前恆為 null
         */
        private Double avgShipDays;
    }

    @Data
    @Builder
    public static class DailyStat {
        /** 日期 */
        private LocalDate date;

        /** 當日抽籤數 */
        private Integer drawCount;

        /** 當日抽獎消費金額（ABS） */
        private Long revenue;

        /**
         * 當日新用戶數：當天首次在本店有抽獎或建立訂單的用戶
         * （近似值：以 MIN(activity_date) per user per store 判斷）
         */
        private Integer newUsers;
    }
}
```

---

## 修改現有類別

### 3. `ReportService` — 新增方法簽名

```java
// 新增：
StorePerformanceReportRes getStorePerformanceReport(QueryReq<StorePerformanceCondition> req);
```

### 4. `AdminReportController` — 新增 endpoint

```java
@PostMapping("/store-performance")
@PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER')")
public ResponseEntity<StorePerformanceReportRes> getStorePerformanceReport(
        @RequestBody QueryReq<StorePerformanceCondition> req) { ... }
```

### 5. `ReportServiceImpl` — 實作邏輯

**核心 SQL 查詢策略**（全部使用 `JdbcTemplate`）：

#### 5-A. 店家基本 KPI（per-store，若無 storeId 則聚合所有店）

```sql
-- 每間店的 drawCount
SELECT l.store_id, COUNT(*) AS draw_count
FROM lottery_ticket lt
JOIN lottery l ON lt.lottery_id = l.id
WHERE lt.status = 'DRAWN'
  AND lt.drawn_at BETWEEN :start AND :end
  [AND l.store_id = :storeId]  -- 有 storeId 時
GROUP BY l.store_id

-- 每間店的 totalRevenue
SELECT l.store_id, ABS(SUM(wt.amount)) AS total_revenue
FROM wallet_transaction wt
JOIN lottery_ticket lt ON wt.related_id = lt.id
JOIN lottery l ON lt.lottery_id = l.id
WHERE wt.transaction_type = 'DRAW'
  AND wt.created_at BETWEEN :start AND :end
  [AND l.store_id = :storeId]
GROUP BY l.store_id

-- 每間店的 activeUsers
SELECT store_id, COUNT(DISTINCT uid) AS active_users FROM (
  SELECT l.store_id, lt.drawn_by AS uid
  FROM lottery_ticket lt
  JOIN lottery l ON lt.lottery_id = l.id
  WHERE lt.drawn_at BETWEEN :start AND :end AND lt.drawn_by IS NOT NULL
  UNION
  SELECT o.store_id, o.user_id AS uid
  FROM `order` o
  WHERE o.created_at BETWEEN :start AND :end
) t
[WHERE t.store_id = :storeId]
GROUP BY t.store_id

-- 每間店的 shipRate / overdueRate
SELECT
  store_id,
  SUM(CASE WHEN status IN ('SHIPPED','COMPLETED') THEN 1 ELSE 0 END) AS shipped,
  SUM(CASE WHEN status <> 'CANCELLED' THEN 1 ELSE 0 END)             AS non_cancelled,
  SUM(CASE WHEN status = 'PENDING'
               AND created_at < DATE_SUB(NOW(), INTERVAL 7 DAY)
           THEN 1 ELSE 0 END)                                         AS overdue,
  COUNT(*)                                                             AS total
FROM `order`
WHERE created_at BETWEEN :start AND :end
  [AND store_id = :storeId]
GROUP BY store_id
```

#### 5-B. 單店 dailyStats（僅 storeId 非 null 時執行）

```sql
SELECT
  DATE(lt.drawn_at) AS stat_date,
  COUNT(*) AS draw_count,
  ABS(COALESCE(SUM(wt.amount), 0)) AS revenue
FROM lottery_ticket lt
JOIN lottery l ON lt.lottery_id = l.id
LEFT JOIN wallet_transaction wt ON wt.related_id = lt.id
  AND wt.transaction_type = 'DRAW'
WHERE l.store_id = :storeId
  AND lt.drawn_at BETWEEN :start AND :end
  AND lt.status = 'DRAWN'
GROUP BY DATE(lt.drawn_at)
ORDER BY stat_date
```

#### 5-C. avgShipDays（待 029 合併後補全）

```java
// 029 DDL (preparing_at) 尚未合併 → 直接回傳 null
// TODO(029): 合併後改為:
//   AVG(DATEDIFF(shipped_at, preparing_at))
//   FROM `order` WHERE store_id=? AND preparing_at IS NOT NULL AND shipped_at IS NOT NULL
Double avgShipDays = null; // 029 dependency not yet available
```

---

## 資料來源彙整

| KPI | 主表 | JOIN | 條件 |
|-----|------|------|------|
| totalRevenue | wallet_transaction | lottery_ticket → lottery | transaction_type='DRAW', store_id |
| drawCount | lottery_ticket | lottery | status='DRAWN', store_id |
| activeUsers | lottery_ticket + order | lottery | UNION，store_id |
| shipRate | order | — | status IN ('SHIPPED','COMPLETED') / != 'CANCELLED' |
| overdueRate | order | — | status='PENDING' AND created_at < NOW()-7d |
| avgShipDays | order | — | **029 未就緒，null** |
| dailyStats | lottery_ticket | lottery, wallet_transaction | storeId 非 null |

---

## 狀態轉移（Order Status 相關計算）

```
PAYMENT_PENDING → PENDING → PREPARING → SHIPPED → COMPLETED
                                      ↓
                                 CANCELLED (可從任意前置狀態取消)
```

- **shipRate 分子**: `SHIPPED` + `COMPLETED`
- **shipRate 分母**: 非 `CANCELLED`（含 `PAYMENT_PENDING`）
- **overdueRate**: `PENDING` 狀態且 `created_at < NOW() - 7 days`
- **avgShipDays**: `PREPARING → SHIPPED`（需 `preparing_at` 欄位，029 依賴）
