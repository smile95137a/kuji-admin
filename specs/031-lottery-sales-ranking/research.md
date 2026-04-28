# Research: 031 - 商品銷售排行報表

**Date**: 2026-04-28  
**Status**: Complete — all unknowns resolved

---

## 1. 現有報表架構 (Existing Report Patterns)

### Decision
沿用現有 `AdminReportController` + `ReportService` + `JdbcTemplate` 三層架構，不引入任何新模式。

### Rationale
專案已有 5 個成熟的報表端點（revenue、referral、lottery-result、recharge、bonus），全部使用相同模式。本功能是第 6 個，完全符合現有架構。

### Pattern Confirmed

```
POST /admin/report/{type}
  ↓ AdminReportController
  ↓ SecurityUtils.getCurrentUserPrimaryStoreId() → 強制 storeId
  ↓ ReportService.getXyzReport(QueryReq<XyzCondition>)
  ↓ ReportServiceImpl 用 JdbcTemplate 執行 SQL
  ↓ 回傳 ResponseEntity<XyzRes>
```

**關鍵資訊：**
- Controller: `com.group.admin.controller.admin.AdminReportController`
- Service Interface: `com.group.admin.service.ReportService`
- Service Impl: `com.group.admin.service.impl.ReportServiceImpl`
- Condition base: `com.group.admin.req.common.BaseCondition`
- Request wrapper: `com.group.admin.req.common.QueryReq<T>`（含 `condition`, `sortBy`, `sortOrder`, `page`, `size`）
- Res pattern: `@Data @Builder public class XyzRes { ... }`

---

## 2. Revenue 計算方式

### Decision
`revenue = COUNT(valid order_items per lottery) × lottery.price_per_draw`

透過子查詢：
```sql
LEFT JOIN (
    SELECT oi.lottery_id, COUNT(oi.id) * MAX(l2.price_per_draw) AS revenue
    FROM order_item oi
    JOIN `order` o ON o.id = oi.order_id AND o.status != 'CANCELLED'
    JOIN lottery l2 ON l2.id = oi.lottery_id
    GROUP BY oi.lottery_id
) rv ON rv.lottery_id = l.id
```

### Rationale
- `order_item` 中每一列代表一次抽籤結果（一個 prize box）
- `lottery.price_per_draw` 是商品固定價格
- 這與 spec 澄清紀錄一致：「100 次 × 80 點 = 8000 點」
- 排除 CANCELLED 訂單：只 JOIN `status != 'CANCELLED'` 的 order
- 不直接使用 `order.total_amount`，因為透過 `order_item.lottery_id` 跳轉才能識別特定商品的訂單

### Alternatives Considered
| 方案 | 問題 |
|------|------|
| `SUM(o.total_amount)` via order_item | 若一個 order 有多個 order_items 指向同一 lottery，會重複計算 |
| `order.draw_count × price_per_draw` | 需要 join order 再 join lottery，且 `draw_count` 可能未正確填入 |
| 子查詢先 DISTINCT order_id 再 SUM total_amount | 邏輯更複雜，且依賴「一 order 只屬一 lottery」假設 |

---

## 3. drawCount 計算方式

### Decision
`drawCount = COUNT(*) FROM lottery_ticket WHERE lottery_id = ? AND status = 'DRAWN'`

```sql
LEFT JOIN (
    SELECT lottery_id, COUNT(*) AS draw_count
    FROM lottery_ticket
    WHERE status = 'DRAWN'
    GROUP BY lottery_id
) dc ON dc.lottery_id = l.id
```

### Rationale
- Spec FR-004 明確規定：來自 `lottery_ticket` 表 `status = DRAWN`
- 全生命期統計，不加時間範圍過濾
- `drawCount = 0` 的商品仍出現（使用 LEFT JOIN）

---

## 4. StoreOwner vs Admin 隔離

### Decision
與所有現有報表一致：

```java
String currentStoreId = SecurityUtils.getCurrentUserPrimaryStoreId();
if (currentStoreId != null) {  // null = ADMIN
    if (req == null) req = new QueryReq<>();
    if (req.getCondition() == null) req.setCondition(new LotterySalesRankingCondition());
    req.getCondition().setStoreId(currentStoreId);  // 後端強制覆蓋
}
```

### Rationale
- `SecurityUtils.getCurrentUserPrimaryStoreId()` 回傳 null → Admin
- 回傳非 null → StoreOwner，強制綁定自己的 storeId
- Admin 可帶 `condition.storeId` 過濾特定店家，或不帶查全平台

---

## 5. 排序機制

### Decision
使用 `QueryReq.sortBy` 欄位，允許 `drawCount`（預設）或 `revenue`。

```java
String sortBy = req != null && req.getSortBy() != null ? req.getSortBy() : "drawCount";
String orderClause = "revenue".equalsIgnoreCase(sortBy) ? "revenue DESC" : "draw_count DESC";
```

**白名單驗證**：只接受 `drawCount` / `revenue`，避免 SQL Injection。

### Rationale
- `QueryReq` 已有 `sortBy` 欄位，無需新增
- Spec FR-003：預設 `drawCount` 降序，支援改以 `revenue` 排序

---

## 6. Limit 參數

### Decision
從 `LotterySalesRankingCondition.limit` 取得，預設 20，上限 100：

```java
int limit = condition.getLimit() != null ? 
    Math.min(condition.getLimit(), 100) : 20;
```

### Rationale
- Spec FR-006：預設 20，最大 100
- 避免 SQL Injection，直接 `Math.min` 限制上限後傳入 `?` 參數

---

## 7. 效能考量

### Decision
SQL 使用子查詢預先聚合（`GROUP BY lottery_id`），避免大量笛卡爾積。

### Indexes Required (需確認存在)

| 表 | 欄位 | 用途 |
|----|------|------|
| `lottery_ticket` | `(lottery_id, status)` | drawCount 子查詢 |
| `order_item` | `(lottery_id, order_id)` | revenue 子查詢 |
| `order` | `(status, id)` | 排除 CANCELLED |
| `lottery` | `(store_id)` | StoreOwner 過濾 |

### Performance Estimate
- 50k lottery_ticket 記錄：`GROUP BY lottery_id WHERE status='DRAWN'` + covering index → < 100ms
- order_item 聚合：取決於訂單量，預期 < 500ms
- LIMIT 20 fetch：最終排序後只取 20 筆，效能可控
- **整體預估 < 1 秒**，遠低於 SC-001 要求的 3 秒

---

## 8. 新增檔案清單

| 檔案 | 類型 | 說明 |
|------|------|------|
| `LotterySalesRankingCondition.java` | NEW | 查詢條件 DTO |
| `LotterySalesRankingRes.java` | NEW | 回應 DTO（含 nested `LotterySalesItem`）|
| `AdminReportControllerLotteryRankingTest.java` | NEW | Controller 測試 |
| `ReportService.java` | MODIFY | 新增介面方法 |
| `ReportServiceImpl.java` | MODIFY | 新增 JdbcTemplate 實作 |
| `AdminReportController.java` | MODIFY | 新增 `POST /report/lottery-sales` 端點 |

---

## 澄清紀錄對照

| 問題 | Spec 決定 | Research 確認 |
|------|-----------|---------------|
| drawCount/revenue 是時間範圍還是全生命期？ | 全生命期累積 | ✅ SQL 不加時間過濾 |
| CANCELLED 訂單計入 revenue？ | 排除 | ✅ `status != 'CANCELLED'` |
| revenue 計算方式？ | via `order_item.lottery_id` JOIN `order` | ✅ COUNT(order_items) × price_per_draw |
| revenue 單位？ | 金幣點數 | ✅ 整數點數，使用 `Long` |
