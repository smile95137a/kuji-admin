# Quickstart: 031 - 商品銷售排行報表

**Feature Branch**: `031-lottery-sales-ranking`  
**Target**: 後端開發者快速上手指南  
**Prerequisite**: 熟悉現有報表模式（AdminReportController / ReportService / JdbcTemplate）

---

## 概覽

本功能僅需修改/新增 **5 個檔案**，全程不接觸資料庫 DDL，是最輕量的報表功能。

| 動作 | 檔案 | 說明 |
|------|------|------|
| NEW | `LotterySalesRankingCondition.java` | 查詢條件 DTO |
| NEW | `LotterySalesRankingRes.java` | 回應 DTO |
| MODIFY | `ReportService.java` | 新增介面方法 |
| MODIFY | `ReportServiceImpl.java` | JdbcTemplate SQL 實作 |
| MODIFY | `AdminReportController.java` | 新增端點 |

---

## Step 1: 新增 Condition DTO

**路徑**: `src/main/java/com/group/admin/condition/report/LotterySalesRankingCondition.java`

```java
package com.group.admin.condition.report;

import com.group.admin.req.common.BaseCondition;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "商品銷售排行報表查詢條件")
public class LotterySalesRankingCondition extends BaseCondition {

    @Schema(description = "店家 ID（後端自動帶入，StoreOwner 不可覆蓋）", example = "uuid-string")
    private String storeId;

    @Schema(description = "回傳筆數，預設 20，最大 100", example = "20")
    private Integer limit;
}
```

---

## Step 2: 新增 Response DTO

**路徑**: `src/main/java/com/group/admin/dto/res/report/LotterySalesRankingRes.java`

```java
package com.group.admin.dto.res.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "商品銷售排行報表回應")
public class LotterySalesRankingRes {

    @Schema(description = "符合條件的商品總數")
    private Integer totalRecords;

    @Schema(description = "排行榜清單（依排序條件降序排列）")
    private List<LotterySalesItem> items;

    @Data
    @Builder
    @Schema(description = "單一商品銷售數據")
    public static class LotterySalesItem {

        @Schema(description = "商品 ID")
        private String lotteryId;

        @Schema(description = "商品標題")
        private String lotteryTitle;

        @Schema(description = "店家名稱")
        private String storeName;

        @Schema(description = "全生命期已抽籤數（lottery_ticket.status=DRAWN）")
        private Integer drawCount;

        @Schema(description = "全生命期有效營收（金幣點數，排除 CANCELLED 訂單）")
        private Long revenue;

        @Schema(description = "排名（1-based）")
        private Integer rank;
    }
}
```

---

## Step 3: 新增 Service 介面方法

**路徑**: `src/main/java/com/group/admin/service/ReportService.java`

在現有介面中新增一個方法（在 `getBonusReport` 後面）：

```java
/**
 * 商品銷售排行報表
 * @param req 查詢條件（包含店家ID、limit、sortBy）
 */
LotterySalesRankingRes getLotterySalesRanking(QueryReq<LotterySalesRankingCondition> req);
```

**Import 需要加**:
```java
import com.group.admin.condition.report.LotterySalesRankingCondition;
import com.group.admin.dto.res.report.LotterySalesRankingRes;
```

---

## Step 4: 實作 Service

**路徑**: `src/main/java/com/group/admin/service/impl/ReportServiceImpl.java`

在 `getBonusReport()` 後面新增：

```java
@Override
public LotterySalesRankingRes getLotterySalesRanking(QueryReq<LotterySalesRankingCondition> req) {
    LotterySalesRankingCondition condition = req != null && req.getCondition() != null
            ? req.getCondition() : new LotterySalesRankingCondition();

    String storeId = condition.getStoreId();
    int limit = condition.getLimit() != null ? Math.min(condition.getLimit(), 100) : 20;

    // 排序欄位白名單
    String sortBy = req != null && "revenue".equalsIgnoreCase(req.getSortBy())
            ? "revenue" : "draw_count";

    log.info("📊 產生商品銷售排行報表: storeId={}, limit={}, sortBy={}", storeId, limit, sortBy);

    // 主查詢
    StringBuilder sql = new StringBuilder("""
            SELECT
                l.id              AS lottery_id,
                l.title           AS lottery_title,
                s.store_name      AS store_name,
                COALESCE(dc.draw_count, 0) AS draw_count,
                COALESCE(rv.revenue, 0)    AS revenue
            FROM lottery l
            JOIN store s ON l.store_id = s.id
            LEFT JOIN (
                SELECT lottery_id, COUNT(*) AS draw_count
                FROM lottery_ticket
                WHERE status = 'DRAWN'
                GROUP BY lottery_id
            ) dc ON dc.lottery_id = l.id
            LEFT JOIN (
                SELECT oi.lottery_id,
                       COUNT(oi.id) * MAX(l2.price_per_draw) AS revenue
                FROM order_item oi
                JOIN `order` o   ON o.id  = oi.order_id AND o.status != 'CANCELLED'
                JOIN lottery l2  ON l2.id = oi.lottery_id
                GROUP BY oi.lottery_id
            ) rv ON rv.lottery_id = l.id
            WHERE 1=1
            """);

    List<Object> params = new ArrayList<>();
    if (storeId != null) {
        sql.append(" AND l.store_id = ?");
        params.add(storeId);
    }

    // 計算 totalRecords（不加 LIMIT）
    String countSql = "SELECT COUNT(*) FROM (" + sql + ") AS total";
    int totalRecords = jdbcTemplate.queryForObject(countSql, Integer.class, params.toArray());

    // 加排序與 LIMIT
    sql.append(" ORDER BY ").append(sortBy).append(" DESC LIMIT ?");
    params.add(limit);

    List<LotterySalesRankingRes.LotterySalesItem> items = jdbcTemplate.query(
            sql.toString(), params.toArray(),
            (rs, rowNum) -> LotterySalesRankingRes.LotterySalesItem.builder()
                    .lotteryId(rs.getString("lottery_id"))
                    .lotteryTitle(rs.getString("lottery_title"))
                    .storeName(rs.getString("store_name"))
                    .drawCount(rs.getInt("draw_count"))
                    .revenue(rs.getLong("revenue"))
                    .rank(rowNum + 1)   // 1-based ranking
                    .build()
    );

    return LotterySalesRankingRes.builder()
            .totalRecords(totalRecords)
            .items(items)
            .build();
}
```

---

## Step 5: 新增 Controller 端點

**路徑**: `src/main/java/com/group/admin/controller/admin/AdminReportController.java`

在現有 `getBonusReport()` 方法後面新增（與其他報表端點完全一致的模式）：

```java
/**
 * 商品銷售排行報表
 */
@PostMapping("/lottery-sales")
@PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER')")
public ResponseEntity<LotterySalesRankingRes> getLotterySalesRanking(
        @RequestBody QueryReq<LotterySalesRankingCondition> req) {

    // 非 Admin 只能查自己店家的報表（後端強制覆蓋 storeId）
    String currentStoreId = SecurityUtils.getCurrentUserPrimaryStoreId();
    if (currentStoreId != null) {
        if (req == null) req = new QueryReq<>();
        if (req.getCondition() == null) req.setCondition(new LotterySalesRankingCondition());
        req.getCondition().setStoreId(currentStoreId);
    }

    return ResponseEntity.ok(reportService.getLotterySalesRanking(req));
}
```

**Imports 需要加**:
```java
import com.group.admin.condition.report.LotterySalesRankingCondition;
import com.group.admin.dto.res.report.LotterySalesRankingRes;
```

---

## Step 6: 撰寫 Controller 測試

**路徑**: `src/test/java/com/group/admin/controller/admin/AdminReportControllerLotteryRankingTest.java`

參考現有 Controller 測試模式（MockMvc + JUnit 5 + Mockito）：

```java
@WebMvcTest(AdminReportController.class)
class AdminReportControllerLotteryRankingTest {

    @Autowired MockMvc mockMvc;
    @MockBean  ReportService reportService;

    // Test 1: Admin 查全平台 → storeId 不被覆蓋
    // Test 2: StoreOwner → storeId 被強制覆蓋為 JWT 中的 storeId
    // Test 3: limit=200 → Service 截斷至 100
    // Test 4: sortBy=revenue → 回傳 revenue 排序結果
    // Test 5: 未認證 → 401
    // Test 6: 角色不符 → 403
}
```

---

## 驗收測試（手動）

### SC-001: 效能 < 3 秒

```bash
# 確認查詢時間（含 50k lottery_ticket 資料）
curl -X POST http://localhost:8080/admin/report/lottery-sales \
  -H "Authorization: Bearer <admin-jwt>" \
  -H "Content-Type: application/json" \
  -d '{"condition": {}, "sortBy": "drawCount"}'
# 預期回應時間 < 3 秒
```

### SC-002: revenue 精確計算

```sql
-- 手動驗算：取商品 A 的有效訂單數 × price_per_draw
SELECT COUNT(oi.id) * MAX(l.price_per_draw) AS expected_revenue
FROM order_item oi
JOIN `order` o ON o.id = oi.order_id AND o.status != 'CANCELLED'
JOIN lottery l ON l.id = oi.lottery_id
WHERE oi.lottery_id = '<lottery-A-id>';
-- 對比 API 回傳的 revenue 值，應完全一致
```

### SC-003: 排序一致性

```sql
-- 手動 SQL 查詢，比對 API 回傳排序
SELECT l.id, COUNT(lt.id) as draw_count
FROM lottery l
LEFT JOIN lottery_ticket lt ON lt.lottery_id = l.id AND lt.status = 'DRAWN'
GROUP BY l.id
ORDER BY draw_count DESC
LIMIT 20;
```

---

## 常見問題

**Q: 為何不使用 `order.total_amount` 計算 revenue？**  
A: `order_item` 每列代表一次抽籤結果，`COUNT(order_items) × price_per_draw` 最精確。使用 `total_amount` 需先確認一個 order 只屬一個 lottery（可能引入假設風險）。

**Q: `drawCount = 0` 的商品會出現嗎？**  
A: 是的，使用 `LEFT JOIN` 確保未抽過的商品也出現，`COALESCE(dc.draw_count, 0) = 0`。

**Q: `limit > 100` 怎麼處理？**  
A: Service 層 `Math.min(condition.getLimit(), 100)` 自動截斷，不回錯誤。

**Q: StoreOwner 帶了 `storeId` 在 request 裡，會使用嗎？**  
A: 不會。Controller 強制用 JWT 中的 `storeId` 覆蓋，與其他報表行為一致。
