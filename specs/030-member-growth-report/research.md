# Research: 030 - 會員成長報表

**Branch**: `030-member-growth-report` | **Phase**: 0 — Research & Unknowns Resolution

---

## 1. 交易類型對應（ARPU 計算）

**問題**：Spec 使用 `DRAW_DEDUCTION` / `BONUS_DEDUCTION`，但 `TransactionTypeEnum` 無此值。

- **Decision**: 使用 `transaction_type = 'DRAW'` + `coin_type` 欄位區分
  - arpuGold → `transaction_type = 'DRAW' AND coin_type = 'GOLD'`
  - arpuBonus → `transaction_type = 'DRAW' AND coin_type = 'BONUS'`
- **Rationale**: `TransactionTypeEnum.DRAW` (`isIncrease = false`) 是抽獎消費的唯一交易類型；`CoinTypeEnum.GOLD / BONUS` 用於區分幣種。
- **Alternatives considered**: 以 `amount < 0` 篩選 — 被排除，因為 `amount` 欄位儲存絕對值（從 `RECHARGE` 邏輯反推）。

---

## 2. 活躍會員定義（activeMembers 衝突解決）

**問題**：Spec 內部有衝突——
- **FR-005 / Edge Cases**：「有 `lottery_ticket.status = DRAWN` 紀錄」（窄定義）
- **澄清紀錄**：「有任一行為即算活躍：登入 OR 儲值 OR 抽獎 OR 建立訂單」（廣定義）

- **Decision**: 採用**廣定義（澄清紀錄版本）**，以 4 表 UNION DISTINCT 計算
  ```sql
  SELECT COUNT(DISTINCT user_id) AS active_members FROM (
      SELECT id AS user_id FROM user
          WHERE last_login_at BETWEEN ? AND ?
      UNION
      SELECT user_id FROM wallet_transaction
          WHERE transaction_type = 'RECHARGE' AND created_at BETWEEN ? AND ?
      UNION
      SELECT drawn_by AS user_id FROM lottery_ticket
          WHERE status = 'DRAWN' AND drawn_at BETWEEN ? AND ?
      UNION
      SELECT user_id FROM `order`
          WHERE created_at BETWEEN ? AND ?
  ) t
  ```
- **Rationale**: 澄清紀錄是針對「活躍會員定義」問題的明確答覆，應優先於 FR 的隱含假設。廣定義對 ARPU 的分母更保守（分母更大 → ARPU 更低），方向合理。
- **Alternatives considered**: 採 FR-005 窄定義（僅 DRAWN）— 簡單但不符合最終澄清；混合定義（按場景切換）— 增加 UI 複雜度。

---

## 3. LotteryTicket 的 userId 欄位

**問題**：Spec 提到 `lottery_ticket.user_id`，但實體類別中欄位名稱為 `drawnBy`（DB 欄位：`drawn_by`）。

- **Decision**: SQL 查詢使用 `drawn_by`，日期條件用 `drawn_at`（非 `created_at`）
- **Rationale**: Entity `LotteryTicket.drawnBy` 對應 DB `drawn_by`，`drawnAt` = 實際抽籤時間，語意正確。
- **Alternatives considered**: `created_at` — 被排除，建立時為 PENDING 狀態，drawn_at 才代表真正被抽取的時間。

---

## 4. 留存率計算基準

**問題**：Spec 說「計算基準為前一個完整月份的新增會員」，但 API 的 startDate/endDate 是任意範圍。

- **Decision**: 留存率固定以**前一個完整月份（prevMonthStart ~ prevMonthEnd）**的新增會員為基數，不受查詢日期範圍影響。
  - 7-day retention：prevMonth 新增會員中，在**加入後 7 天內**有任何活躍行為的比例
  - 30-day retention：prevMonth 新增會員中，在**加入後 30 天內**有任何活躍行為的比例
  - 當前月份進行中時，留存率返回 `null`（Edge Case：不適用於進行中月份）
- **Rationale**: 完整月份才有完整的 7/30 天觀察窗口，避免半月資料產生偏低的留存率。
- **Alternatives considered**: 以查詢起始日前推 30 天 — 被排除，因觀察窗口會不完整。

---

## 5. 預設日期範圍

- **Decision**: 若 condition.startDate 或 endDate 為 null，後端自動填入：
  - `endDate = LocalDate.now()`
  - `startDate = endDate.minusDays(29)` （涵蓋今天共 30 天）
- **Rationale**: FR-008 要求預設為最近 30 天；與 User Story 3 中「30 天明細陣列長度為 30」一致。

---

## 6. 安全性：僅 ADMIN

- **Decision**: `@PreAuthorize("hasRole('ADMIN')")` — 不包含 STORE_OWNER
- **Rationale**: 澄清紀錄明確：「僅 Admin，店家無法查看整體會員報表」；且會員資料為平台層級，非店家層級。
- **Alternatives considered**: `hasAnyRole('ADMIN', 'STORE_OWNER')` — 被排除，依澄清決定。

---

## 7. 效能分析

- **Decision**: 對以下欄位確認索引存在：`user.created_at`、`user.provider`、`lottery_ticket.drawn_at + status`、`wallet_transaction.transaction_type + coin_type + created_at`
- **Rationale**: 100,000 會員 × 多表 JOIN/UNION 在有索引情況下應可在 5 秒內完成；如超出，可考慮拆成分步查詢（不影響 API 接口）。
- **Alternatives considered**: 引入報表快照（ReportSnapshot）— 留存率計算可選，但本次優先直接查詢，維持與其他報表一致的架構；若效能不足再加快照。

---

## 8. registrationByProvider 格式

- **Decision**: 回傳 `Map<String, Integer>`，key 為 provider 值（`"EMAIL"` / `"GOOGLE"`）
- **Rationale**: User Story 1 驗收場景明確：`{ GOOGLE: 80, EMAIL: 70 }`；Map 形式前端展示彈性高。
- **Alternatives considered**: 固定 DTO 欄位（emailCount / googleCount）— 被排除，未來若新增 provider 需改 DTO。

---

## 解決狀態

| 未知項目 | 狀態 |
|---|---|
| DRAW_DEDUCTION / BONUS_DEDUCTION 實際對應 | ✅ 解決 |
| 活躍會員定義衝突 | ✅ 解決（廣定義） |
| LotteryTicket userId 欄位名稱 | ✅ 解決（drawn_by） |
| 留存率計算窗口 | ✅ 解決（前一完整月） |
| 預設日期範圍 | ✅ 解決（最近 30 天） |
| 安全角色限制 | ✅ 解決（ADMIN only） |
| 效能策略 | ✅ 解決（直接查詢 + 索引） |
| registrationByProvider 格式 | ✅ 解決（Map） |
