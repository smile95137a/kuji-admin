# Data Model: 031 - 商品銷售排行報表

**Date**: 2026-04-28  
**Schema Changes**: 無（全部為讀取現有表）

---

## 涉及的現有實體

### 1. `lottery`（商品）

| 欄位 | 類型 | 說明 |
|------|------|------|
| `id` | VARCHAR(36) PK | 商品 UUID |
| `title` | VARCHAR | 商品標題 |
| `store_id` | VARCHAR(36) FK→store.id | 所屬店家 |
| `price_per_draw` | BIGINT | 每次抽籤點數 |
| `status` | VARCHAR | 商品狀態（不過濾，全生命期） |

**用途**：主要聚合對象，JOIN store 取得 store_name

---

### 2. `lottery_ticket`（籤位）

| 欄位 | 類型 | 說明 |
|------|------|------|
| `id` | VARCHAR(36) PK | 籤位 UUID |
| `lottery_id` | VARCHAR(36) FK→lottery.id | 所屬商品 |
| `status` | VARCHAR | **DRAWN** = 已抽出 |
| `drawn_at` | DATETIME | 抽出時間（不用於此查詢） |

**用途**：`drawCount` 來源 — `COUNT(*) WHERE status = 'DRAWN'`

**索引需求**: `(lottery_id, status)` — 需確認存在

---

### 3. `order_item`（訂單項目）

| 欄位 | 類型 | 說明 |
|------|------|------|
| `id` | VARCHAR(36) PK | 項目 UUID |
| `order_id` | VARCHAR(36) FK→order.id | 所屬訂單 |
| `lottery_id` | VARCHAR(36) FK→lottery.id | **橋接欄位** |
| `lottery_title` | VARCHAR | 商品標題快照（冗餘，不使用） |
| `prize_id` / `prize_name` / etc. | VARCHAR | 獎品資訊（不使用） |

**用途**：`lottery_id` 是 revenue 計算的橋接欄位，連結商品與訂單

**索引需求**: `(lottery_id, order_id)` — 需確認存在

---

### 4. `order`（訂單）

| 欄位 | 類型 | 說明 |
|------|------|------|
| `id` | VARCHAR(36) PK | 訂單 UUID |
| `store_id` | VARCHAR(36) FK→store.id | 所屬店家 |
| `status` | VARCHAR | **CANCELLED** = 排除 |
| `total_amount` | BIGINT | 訂單總金額（不直接使用） |
| `draw_count` | INT | 此訂單抽籤次數（不直接使用） |

**用途**：過濾 `status != 'CANCELLED'`，確認有效訂單

**索引需求**: `(id, status)` — 標準索引應已存在

---

### 5. `store`（店家）

| 欄位 | 類型 | 說明 |
|------|------|------|
| `id` | VARCHAR(36) PK | 店家 UUID |
| `store_name` | VARCHAR | 店家名稱 |
| `status` | VARCHAR | 不過濾 |

**用途**：取得 `store_name` 顯示欄位

---

## 實體關係圖

```
store (1) ─────────────────────── (N) lottery
                                         │
                                    (1)  │  (1)
                                  lottery_ticket (N)
                                  [status = DRAWN → drawCount]

lottery (1) ──── (N) order_item (N) ──── (1) order
                 [lottery_id 橋接]       [status != CANCELLED → revenue]
```

---

## 核心 SQL 查詢設計

```sql
SELECT
    l.id              AS lottery_id,
    l.title           AS lottery_title,
    s.store_name      AS store_name,
    COALESCE(dc.draw_count, 0) AS draw_count,
    COALESCE(rv.revenue, 0)    AS revenue
FROM lottery l
JOIN store s ON l.store_id = s.id
LEFT JOIN (
    -- drawCount 子查詢：全生命期已抽籤位數
    SELECT lottery_id, COUNT(*) AS draw_count
    FROM lottery_ticket
    WHERE status = 'DRAWN'
    GROUP BY lottery_id
) dc ON dc.lottery_id = l.id
LEFT JOIN (
    -- revenue 子查詢：有效訂單 × 每次抽籤點數
    SELECT
        oi.lottery_id,
        COUNT(oi.id) * MAX(l2.price_per_draw) AS revenue
    FROM order_item oi
    JOIN `order` o   ON o.id  = oi.order_id AND o.status != 'CANCELLED'
    JOIN lottery l2  ON l2.id = oi.lottery_id
    GROUP BY oi.lottery_id
) rv ON rv.lottery_id = l.id
WHERE 1=1
  -- 條件：StoreOwner 強制帶入，Admin 可選
  -- [AND l.store_id = ?]
ORDER BY
  -- 預設 draw_count DESC，支援 revenue DESC
  draw_count DESC   -- 或 revenue DESC
LIMIT 20            -- 預設 20，最大 100
```

### 查詢說明

| 部分 | 邏輯 |
|------|------|
| drawCount 子查詢 | 先 GROUP BY lottery_id 預聚合，避免笛卡爾積 |
| revenue 子查詢 | COUNT(order_items for this lottery, valid orders) × price_per_draw |
| LEFT JOIN | drawCount=0 或 revenue=0 的商品仍包含（Edge Case：新商品未抽過）|
| LIMIT | 直接限制結果集大小（整數參數，防止 SQL Injection）|

---

## DTO 設計

### `LotterySalesRankingCondition` (查詢條件)

```java
package com.group.admin.condition.report;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "商品銷售排行報表查詢條件")
public class LotterySalesRankingCondition extends BaseCondition {

    @Schema(description = "店家 ID（後端自動帶入，StoreOwner 不可覆蓋）")
    private String storeId;

    @Schema(description = "回傳筆數限制，預設 20，最大 100", example = "20")
    private Integer limit;
}
```

**Notes:**
- 無日期欄位（全生命期統計，FR 設計決定）
- `sortBy` 使用 `QueryReq.sortBy`（`drawCount` | `revenue`）
- `storeId` 由 Controller 後端強制帶入

---

### `LotterySalesRankingRes` (回應)

```java
package com.group.admin.dto.res.report;

@Data
@Builder
public class LotterySalesRankingRes {

    @Schema(description = "符合條件的商品總數")
    private Integer totalRecords;

    @Schema(description = "排行榜清單（依排序條件降序）")
    private List<LotterySalesItem> items;

    @Data
    @Builder
    public static class LotterySalesItem {
        @Schema(description = "商品 ID")
        private String lotteryId;

        @Schema(description = "商品標題")
        private String lotteryTitle;

        @Schema(description = "店家名稱")
        private String storeName;

        @Schema(description = "全生命期已抽籤數（status=DRAWN）")
        private Integer drawCount;

        @Schema(description = "全生命期有效營收（金幣點數，排除 CANCELLED）")
        private Long revenue;

        @Schema(description = "排名（1-based）")
        private Integer rank;
    }
}
```

---

## 驗證規則

| 規則 | 層次 | 說明 |
|------|------|------|
| `limit` 最大 100 | Service | `Math.min(limit, 100)` |
| `sortBy` 白名單 | Service | 只接受 `drawCount` / `revenue` |
| `storeId` 強制帶入 | Controller | StoreOwner 不可自行設定 |
| `drawCount >= 0` | DB（LEFT JOIN） | 新商品 drawCount=0 仍出現 |
| `revenue >= 0` | DB（LEFT JOIN）| 新商品 revenue=0 仍出現 |

---

## 狀態說明（無狀態轉移）

此功能為純查詢報表，無實體狀態轉移。

相關狀態常數：

| 常數 | 表 | 值 | 說明 |
|------|----|----|------|
| `DRAWN` | `lottery_ticket.status` | `"DRAWN"` | 已抽出的籤位（計入 drawCount） |
| `CANCELLED` | `order.status` | `"CANCELLED"` | 已取消的訂單（排除於 revenue） |
