# Quickstart: 029 - 獎品出貨報表

> 給實作者的 5 分鐘上手指南。看完本文件即可開始實作，無需再讀 spec。

---

## 一眼掌握：這個 Feature 做什麼

新增一支報表 API `POST /admin/report/prize-shipment`，讓店家（StoreOwner）和平台管理者（Admin）查詢訂單出貨狀況：

| 指標 | 說明 |
|------|------|
| `pendingCount / preparingCount / shippedCount / completedCount` | 各狀態訂單數 |
| `avgShipDays` | 平均出貨天數（`preparing_at` → `shipped_at`） |
| `overdueCount` | 超過 7 天未備貨的 PENDING 訂單數 |
| `dailyDetails` | 每日出貨筆數（按 `shipped_at` 分組） |
| `storeDetails` | 各店家統計（Admin 限定） |

**前置 DDL**：需先在 `order` 表新增 `preparing_at DATETIME NULL` 並更新 MBG 相關檔案。

---

## 開發順序（建議）

```
1. DDL migration          → sql/V029__add_order_preparing_at.sql
2. Order entity 更新       → Order.java + OrderMapper.xml
3. OrderServiceImpl 更新   → markAsPreparing() 設定 preparingAt
4. Condition 類別           → PrizeShipmentReportCondition.java
5. Response DTO             → PrizeShipmentReportRes.java
6. Service 介面             → ReportService.java（加方法簽名）
7. Service 實作             → ReportServiceImpl.java（加方法）
8. Controller               → AdminReportController.java（加 endpoint）
9. 測試                     → AdminReportControllerPrizeShipmentTest.java
```

---

## 步驟 1：DDL Migration

建立 `sql/V029__add_order_preparing_at.sql`：

```sql
ALTER TABLE `order`
  ADD COLUMN `preparing_at` DATETIME NULL COMMENT '備貨開始時間'
  AFTER `shipped_at`;

CREATE INDEX idx_order_store_status_created ON `order`(store_id, status, created_at);
CREATE INDEX idx_order_store_shipped_at     ON `order`(store_id, shipped_at);
```

---

## 步驟 2：更新 Order 實體

**`Order.java`** — 加欄位 + getter/setter：
```java
private LocalDateTime preparingAt;
// ... 標準 getter/setter
```

**`OrderMapper.xml`** — 在 `BaseResultMap` 加一行：
```xml
<result column="preparing_at" jdbcType="TIMESTAMP" property="preparingAt" />
```
並在 `Base_Column_List`、`insert`、`insertSelective`、`updateByPrimaryKey`、`updateByPrimaryKeySelective` 對應加入。

---

## 步驟 3：更新 OrderServiceImpl

找到 `markAsPreparing()` 方法，在設定 status 後加一行：

```java
order.setStatus(OrderStatusEnum.PREPARING.getCode());
order.setPreparingAt(LocalDateTime.now());  // ← 加這行
order.setUpdatedAt(LocalDateTime.now());
```

---

## 步驟 4-5：Condition + Res

複製現有 `RevenueReportCondition.java` 為 `PrizeShipmentReportCondition.java`，保留 `storeId`、`startDate`、`endDate` 三個欄位。

複製現有 `RevenueReportRes.java` 結構，改為規格中的欄位（見 `data-model.md` 第 6 節）。

---

## 步驟 6-7：Service

**`ReportService.java`** 加入：
```java
PrizeShipmentReportRes getPrizeShipmentReport(QueryReq<PrizeShipmentReportCondition> req);
```

**`ReportServiceImpl.java`** 實作（參考現有 `getRevenueReport` 的 JdbcTemplate 模式）：

1. 解析 condition（null 安全）
2. 預設日期範圍（startDate = today-29, endDate = today）
3. 執行 5 個 SQL 查詢（詳見 `data-model.md` 第 7 節）：
   - 狀態計數（`queryForMap`）
   - avgShipDays（`queryForObject`）
   - overdueCount（`queryForObject`）
   - dailyDetails（`query` + RowMapper）
   - storeDetails（`query` + RowMapper，僅 storeId == null 時執行）
4. 組裝 `PrizeShipmentReportRes.builder()` 回傳

---

## 步驟 8：Controller

在 `AdminReportController.java` 加方法（與現有 `getRevenueReport` 完全相同結構）：

```java
@PostMapping("/prize-shipment")
@PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER')")
public ResponseEntity<PrizeShipmentReportRes> getPrizeShipmentReport(
        @RequestBody QueryReq<PrizeShipmentReportCondition> req) {

    String currentStoreId = SecurityUtils.getCurrentUserPrimaryStoreId();
    if (currentStoreId != null) {
        if (req == null) req = new QueryReq<>();
        if (req.getCondition() == null) req.setCondition(new PrizeShipmentReportCondition());
        req.getCondition().setStoreId(currentStoreId);
    }

    return ResponseEntity.ok(reportService.getPrizeShipmentReport(req));
}
```

---

## 步驟 9：測試

測試重點（MockMvc + Mockito）：

| Test | 驗證點 |
|------|--------|
| `givenStoreOwner_onlySeesOwnStore` | storeId 被強制覆蓋，不回傳 storeDetails |
| `givenAdmin_returnsStoreDetails` | storeDetails 不為 null |
| `givenNoOrders_returnsAllZeros` | count 全 0，dailyDetails 空，avgShipDays null |
| `givenOverdueOrders_overdueCountCorrect` | overdueCount = 2 |
| `givenValidShippedOrders_avgShipDaysCorrect` | avgShipDays = 4.0（(2+4+6)/3） |
| `givenNullDates_defaultsToLast30Days` | startDate 自動補足 |

---

## 常見陷阱

| 問題 | 解法 |
|------|------|
| `preparing_at` 為 null 導致 avgShipDays NPE | SQL 加 `AND preparing_at IS NOT NULL`；Java 端 `rs.getBigDecimal("avg_ship_days")` 可能為 null，要判斷 |
| overdueCount 不受日期範圍過濾 | overdueCount 的 SQL **不要**加 `created_at BETWEEN` |
| StoreOwner 收到 storeDetails | Controller 的 storeId 注入邏輯必須在 Service 之前執行（已在 Controller 層處理） |
| `PAYMENT_PENDING` 訂單混入計數 | SQL `WHERE status NOT IN ('CANCELLED', 'PAYMENT_PENDING')` |

---

## 參考檔案

| 用途 | 檔案 |
|------|------|
| 報表 Controller 範例 | `controller/admin/AdminReportController.java` |
| 報表 Service 範例 | `service/impl/ReportServiceImpl.java`（`getRevenueReport`） |
| Condition 範例 | `condition/report/RevenueReportCondition.java` |
| Res DTO 範例 | `dto/res/report/RevenueReportRes.java` |
| Order 現有欄位 | `entity/Order.java`、`mapper/OrderMapper.xml` |
| 完整 API 合約 | `contracts/prize-shipment-report-api.md` |
| SQL 設計 | `data-model.md` 第 7 節 |
