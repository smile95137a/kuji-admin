# Data Model: 029 - 獎品出貨報表

> Phase 1 output — entities, DDL changes, validation rules, and state transitions.

---

## 1. DDL 變更

### 1.1 新增欄位：`order.preparing_at`

```sql
-- V029__add_order_preparing_at.sql
ALTER TABLE `order`
  ADD COLUMN `preparing_at` DATETIME NULL COMMENT '備貨開始時間（狀態轉為 PREPARING 時自動記錄）'
  AFTER `shipped_at`;

-- 效能索引（選擇性高，涵蓋主要報表查詢路徑）
CREATE INDEX idx_order_store_status_created ON `order`(store_id, status, created_at);
CREATE INDEX idx_order_store_shipped_at     ON `order`(store_id, shipped_at);
```

**注意**：
- `preparing_at` 為 nullable；上線前建立的舊訂單值為 NULL，avgShipDays 計算時自動排除。
- `shipped_at` 索引已有或需確認；若已存在請先 `SHOW INDEX FROM order` 確認。

---

## 2. 實體更新：`Order.java`

**修改**：在現有欄位後新增：

```java
// 在 cancelReason 欄位後新增
private LocalDateTime preparingAt;

// getter
public LocalDateTime getPreparingAt() {
    return preparingAt;
}

// setter
public void setPreparingAt(LocalDateTime preparingAt) {
    this.preparingAt = preparingAt;
}
```

---

## 3. MyBatis Mapper 更新：`OrderMapper.xml`

### 3.1 BaseResultMap

```xml
<!-- 在 cancelled_reason 後新增 -->
<result column="preparing_at" jdbcType="TIMESTAMP" property="preparingAt" />
```

### 3.2 Base_Column_List

```xml
<!-- 在 cancelled_reason 後追加 -->
..., preparing_at
```

### 3.3 insert / insertSelective

在 `insert` 語句的 column list 與 values list 中加入 `preparing_at`；  
在 `insertSelective` 的 `<if test="preparingAt != null">` 中加入。

### 3.4 updateByPrimaryKey / updateByPrimaryKeySelective

同上，加入 `preparing_at = #{preparingAt,jdbcType=TIMESTAMP}`。

---

## 4. OrderServiceImpl 更新：自動設定 `preparingAt`

**修改位置**：`markAsPreparing(String orderId, String operatorId)` 方法中，在設定 `status = PREPARING` 後加入：

```java
order.setStatus(OrderStatusEnum.PREPARING.getCode());
order.setPreparingAt(LocalDateTime.now());   // ← 新增這行
order.setUpdatedAt(LocalDateTime.now());
orderMapper.updateByPrimaryKeySelective(order);
```

---

## 5. 新增查詢條件類別：`PrizeShipmentReportCondition`

**Package**: `com.group.admin.condition.report`  
**Extends**: `BaseCondition`（繼承 `createdAtStart`、`createdAtEnd`、`keyword`）

```java
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "獎品出貨報表查詢條件")
public class PrizeShipmentReportCondition extends BaseCondition {

    /** 店家 ID（後端自動帶入，StoreOwner 只能查自己店家） */
    @Schema(description = "店家 ID（後端自動帶入）")
    private String storeId;

    /** 查詢開始日期（預設：今日 -29 天） */
    @Schema(description = "開始日期", example = "2026-04-01")
    private LocalDate startDate;

    /** 查詢結束日期（預設：今日） */
    @Schema(description = "結束日期", example = "2026-04-30")
    private LocalDate endDate;
}
```

---

## 6. 新增回應 DTO：`PrizeShipmentReportRes`

**Package**: `com.group.admin.dto.res.report`

```java
@Data
@Builder
public class PrizeShipmentReportRes {

    private LocalDate startDate;
    private LocalDate endDate;

    // === 狀態計數（排除 CANCELLED） ===
    private Integer pendingCount;       // PENDING
    private Integer preparingCount;     // PREPARING
    private Integer shippedCount;       // SHIPPED
    private Integer completedCount;     // COMPLETED

    // === 時效指標 ===
    /** 平均出貨天數（preparing_at → shipped_at）；無資料時為 null */
    private BigDecimal avgShipDays;

    /** 超過 7 天仍在 PENDING 的訂單筆數 */
    private Integer overdueCount;

    // === 每日出貨明細（按 shipped_at 分組） ===
    private List<DailyShipment> dailyDetails;

    // === 跨店家統計（Admin 限定；StoreOwner 查詢時為 null） ===
    private List<StoreShipment> storeDetails;

    // ---- Inner classes ----

    @Data
    @Builder
    public static class DailyShipment {
        private LocalDate date;
        private Integer shippedCount;   // 當日實際出貨筆數（shipped_at 落在該日）
    }

    @Data
    @Builder
    public static class StoreShipment {
        private String storeId;
        private String storeName;
        private Integer pendingCount;
        private Integer preparingCount;
        private Integer shippedCount;
        private Integer completedCount;
        private BigDecimal avgShipDays;  // 該店平均出貨天數
        private Integer overdueCount;    // 該店逾期訂單數
    }
}
```

---

## 7. SQL 查詢設計（ReportServiceImpl 參考）

### 7.1 狀態計數

```sql
SELECT
    SUM(CASE WHEN status = 'PENDING'    THEN 1 ELSE 0 END) AS pending_count,
    SUM(CASE WHEN status = 'PREPARING'  THEN 1 ELSE 0 END) AS preparing_count,
    SUM(CASE WHEN status = 'SHIPPED'    THEN 1 ELSE 0 END) AS shipped_count,
    SUM(CASE WHEN status = 'COMPLETED'  THEN 1 ELSE 0 END) AS completed_count
FROM `order`
WHERE status NOT IN ('CANCELLED', 'PAYMENT_PENDING')
  AND created_at BETWEEN :startDate AND :endDate
  [AND store_id = :storeId]
```

### 7.2 平均出貨天數

```sql
SELECT ROUND(AVG(DATEDIFF(shipped_at, preparing_at)), 1) AS avg_ship_days
FROM `order`
WHERE status IN ('SHIPPED', 'COMPLETED')
  AND preparing_at IS NOT NULL
  AND shipped_at IS NOT NULL
  AND created_at BETWEEN :startDate AND :endDate
  [AND store_id = :storeId]
```

### 7.3 逾期未備貨計數

```sql
SELECT COUNT(*) AS overdue_count
FROM `order`
WHERE status = 'PENDING'
  AND created_at < NOW() - INTERVAL 7 DAY
  [AND store_id = :storeId]
```

> 逾期計數不受日期範圍過濾（反映「現在」的逾期狀況）。

### 7.4 每日出貨明細

```sql
SELECT
    DATE(shipped_at) AS date,
    COUNT(*) AS shipped_count
FROM `order`
WHERE status IN ('SHIPPED', 'COMPLETED')
  AND shipped_at BETWEEN :startDate AND :endDate
  [AND store_id = :storeId]
GROUP BY DATE(shipped_at)
ORDER BY date
```

### 7.5 跨店家統計（Admin 限定）

```sql
SELECT
    store_id,
    store_name,
    SUM(CASE WHEN status = 'PENDING'    THEN 1 ELSE 0 END) AS pending_count,
    SUM(CASE WHEN status = 'PREPARING'  THEN 1 ELSE 0 END) AS preparing_count,
    SUM(CASE WHEN status = 'SHIPPED'    THEN 1 ELSE 0 END) AS shipped_count,
    SUM(CASE WHEN status = 'COMPLETED'  THEN 1 ELSE 0 END) AS completed_count,
    ROUND(AVG(CASE
        WHEN status IN ('SHIPPED','COMPLETED') AND preparing_at IS NOT NULL AND shipped_at IS NOT NULL
        THEN DATEDIFF(shipped_at, preparing_at)
    END), 1) AS avg_ship_days,
    SUM(CASE
        WHEN status = 'PENDING' AND created_at < NOW() - INTERVAL 7 DAY
        THEN 1 ELSE 0
    END) AS overdue_count
FROM `order`
WHERE status NOT IN ('CANCELLED', 'PAYMENT_PENDING')
  AND created_at BETWEEN :startDate AND :endDate
GROUP BY store_id, store_name
ORDER BY avg_ship_days DESC
```

---

## 8. 狀態轉移（無變更，僅說明 `preparing_at` 插入點）

```
PAYMENT_PENDING → PENDING → PREPARING* → SHIPPED → COMPLETED
                                ↓
                        (自動設定 preparing_at = NOW())
                                        ↓
                                (shipped_at 已有)
```

- `preparing_at` 在 `OrderServiceImpl.markAsPreparing()` 設定
- `shipped_at` 在既有 `markAsShipped()` 設定（現有功能，無需更改）
- `CANCELLED` 訂單完全排除於報表計算

---

## 9. Validation Rules

| 欄位 | 規則 |
|------|------|
| `storeId` | StoreOwner：後端強制從 JWT 取得，忽略前端傳入值；Admin：可為 null（查全部） |
| `startDate` / `endDate` | null 時預設補足最近 30 天；`endDate >= startDate` |
| `overdueCount` | 固定以「目前時間」計算，不受日期範圍影響 |
| CANCELLED / PAYMENT_PENDING | 所有計數、avgShipDays、dailyDetails 均排除 |
| `preparing_at = null` | 不計入 avgShipDays（舊訂單向後相容） |
