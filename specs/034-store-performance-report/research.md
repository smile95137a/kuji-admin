# Research: 034 - 店家績效比較報表

**Phase**: 0 — Research  
**Date**: 2026-04-28  
**Branch**: `034-store-performance-report`

---

## R-01: `avgShipDays` 依賴 029 的 `preparing_at` 欄位

**Decision**: `avgShipDays` 在 029 未實作前回傳 `null`。

**Rationale**: 確認 `Order.java` 實體（MBG 生成）不含 `preparing_at` 欄位。  
`preparing_at` 是 029 規格書明定需新增的 DDL 欄位（`ALTER TABLE order ADD COLUMN preparing_at DATETIME NULL`），  
在 029 未合併前欄位不存在，若直接查詢會導致 SQL 錯誤。

**Implementation**: SQL 中用 `IF(COLUMN_EXISTS, AVG(...), NULL)` 不可移植；  
改採服務層偵測：以 `information_schema` 動態查詢是否有 `preparing_at`，  
或直接在 service 判斷「029 feature flag / column available」→ 簡化為：  
**回傳 `null`，並在 TODO 中標記待 029 合併後補上**。

**Alternatives considered**:  
- 直接查詢 `preparing_at`（若欄位不存在則 SQL 失敗，排除）  
- `TRY/CATCH` 動態 fallback（過複雜，排除）

---

## R-02: `totalRevenue` 計算：使用 `wallet_transaction.transaction_type = 'DRAW'`

**Decision**: 使用 `transaction_type = 'DRAW'`（不是 spec 草案所寫的 `DRAW_DEDUCTION`）。

**Rationale**: 確認 `TransactionTypeEnum` 的實際 code 值為 `DRAW`（對應「抽獎消費」），
`DRAW_DEDUCTION` 為 spec 撰寫時的草稿用詞。  
`wallet_transaction.amount` 對 DRAW 為**負數**（`isIncrease = false`），  
所以 `totalRevenue = ABS(SUM(amount))` 或 `SUM(amount) * -1`。

**Join path**:
```sql
wallet_transaction wt
  JOIN lottery_ticket lt ON wt.related_id = lt.id
  JOIN lottery l ON lt.lottery_id = l.id
WHERE wt.transaction_type = 'DRAW'
  AND l.store_id = ?
  AND wt.created_at BETWEEN ? AND ?
```

**Alternatives considered**:  
- `order.total_amount`（已被 RevenueReportService 用於訂單營業額，語意不同；抽獎消費記錄在 wallet_transaction，排除）

---

## R-03: `activeUsers` 定義與 SQL 實作

**Decision**: 每店家的 `activeUsers` = 期間內在該店有**抽獎行為**或**建立訂單**的不重複用戶數。  
登入（`user.last_login_at`）與儲值（`recharge_record`）為平台級行為，無 `store_id` 維度，  
無法與特定店家關聯，故**不納入**每店家的 activeUsers 計算。

**Rationale**: 店家 KPI 的語意是「與本店互動的用戶」；登入與儲值是平台行為，  
若納入會導致所有店家 activeUsers 相同（因為同一批用戶登入平台），失去比較意義。

**SQL 範本**:
```sql
SELECT COUNT(DISTINCT uid) FROM (
  -- 有抽獎行為（透過本店一番賞）
  SELECT lt.drawn_by AS uid
  FROM lottery_ticket lt
  JOIN lottery l ON lt.lottery_id = l.id
  WHERE l.store_id = ?
    AND lt.drawn_at BETWEEN ? AND ?
    AND lt.drawn_by IS NOT NULL
  UNION
  -- 有建立訂單
  SELECT o.user_id AS uid
  FROM `order` o
  WHERE o.store_id = ?
    AND o.created_at BETWEEN ? AND ?
) t
```

**Alternatives considered**:  
- 全平台 activeUsers（登入+儲值），但無法按店家區分（排除）

---

## R-04: `shipRate` 與 `overdueRate` 計算

**Decision**:
- `shipRate` = (SHIPPED + COMPLETED 訂單數) / 非 CANCELLED 訂單數 × 100，  
  分母為 0 時回傳 `null`（避免除以零）
- `overdueRate` = PENDING 超過 7 天訂單數 / **全部**訂單數 × 100，  
  與 spec 一致（分母為 total 非 non-cancelled）

**Rationale**: spec FR-005/FR-006 明確定義，無歧義。

**SQL 範本**（shipRate）:
```sql
SELECT
  SUM(CASE WHEN status IN ('SHIPPED','COMPLETED') THEN 1 ELSE 0 END) AS shipped,
  SUM(CASE WHEN status != 'CANCELLED' THEN 1 ELSE 0 END) AS non_cancelled,
  SUM(CASE WHEN status = 'PENDING' AND created_at < DATE_SUB(NOW(), INTERVAL 7 DAY) THEN 1 ELSE 0 END) AS overdue,
  COUNT(*) AS total
FROM `order`
WHERE store_id = ?
  AND created_at BETWEEN ? AND ?
```

---

## R-05: `sortBy` 白名單防 SQL Injection

**Decision**: 在 `ReportServiceImpl` 維護 `Set<String> ALLOWED_SORT_FIELDS`，  
非白名單值 fallback 為 `totalRevenue`。

**Allowed fields**: `totalRevenue`, `drawCount`, `activeUsers`, `shipRate`, `overdueRate`, `avgShipDays`

**Rationale**: `QueryReq.sortBy` 為前端字串，直接拼入 SQL 有 Injection 風險；  
白名單驗證是 Spring Boot JdbcTemplate 專案的最簡解法（不像 JPA 有 Sort 物件保護）。

---

## R-06: `dailyStats` 僅在帶入 `storeId` 時回傳

**Decision**: 完全沿用 spec FR-010。  
Controller 層根據 condition.storeId 是否非 null 決定是否呼叫 dailyStats 查詢。

**Daily stats content** (per day):
- `date`: DATE
- `drawCount`: lottery_ticket WHERE drawn_at on that date
- `revenue`: SUM(wallet_transaction amount) for that date
- `newUsers`: 當天第一次在此店有行為的用戶數（近似：首次 drawn_at 或 order.created_at）

**Rationale**: 全店家同時回傳 dailyStats 會導致資料量過大（N stores × days），  
單店詳細視圖才有意義。

---

## R-07: StoreOwner 存取控制

**Decision**: 沿用既有模式 — Controller 呼叫 `SecurityUtils.getCurrentUserPrimaryStoreId()`，  
若非 null（代表是 StoreOwner），強制覆蓋 condition.storeId；  
若 condition.storeId 與 StoreOwner 自己的 storeId 不符，回傳 403。

**Rationale**: 既有 `getRevenueReport`、`getReferralReport` 等皆用同一模式，保持一致。

**Implementation detail**:
```java
String currentStoreId = SecurityUtils.getCurrentUserPrimaryStoreId();
if (currentStoreId != null) {
    // StoreOwner — 只能查自己的店
    String requestedStoreId = condition.getStoreId();
    if (requestedStoreId != null && !requestedStoreId.equals(currentStoreId)) {
        return ResponseEntity.status(403).build();
    }
    condition.setStoreId(currentStoreId);
}
```

---

## R-08: `store.store_name` DB 欄位確認

**Decision**: DB 欄位名稱為 `store_name`（確認 `StoreMapper.xml` resultMap）。  
JOIN 查詢：`LEFT JOIN store s ON o.store_id = s.id` → `s.store_name`。

---

## Summary: All NEEDS CLARIFICATION Resolved

| 項目 | 結論 |
|------|------|
| `preparing_at` 欄位 | 029 未合併前不存在，`avgShipDays` 回傳 `null` |
| `totalRevenue` transaction type | `DRAW`（非 `DRAW_DEDUCTION`），金額取 ABS |
| `activeUsers` 定義 | 抽獎 + 訂單 UNION（登入/儲值為平台級，不含） |
| sortBy 安全 | 白名單 Set 驗證 |
| StoreOwner 403 | Controller 層強制覆蓋並驗證 storeId |
| `dailyStats` 條件 | 僅 storeId 非 null 時回傳 |
