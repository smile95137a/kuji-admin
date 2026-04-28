# Feature Specification: 033 - 平台營收總覽報表

**Feature Branch**: `033-platform-revenue-report`
**Created**: 2026-04-28
**Status**: Draft

---

## 背景說明

提供平台整體財務健康指標，讓 Admin 掌握平台金幣儲值、消費、淨收入的趨勢。  
資料來源：`wallet_transaction`（儲值/消費紀錄）+ `lottery_ticket`（抽獎紀錄）+ `order`（訂單紀錄）。

---

## User Scenarios & Testing

### User Story 1 - Admin 查看平台整體營收 (Priority: P1)

Admin 需要快速了解指定時間段內平台的金幣儲值量、消費量與淨收入，以評估平台財務狀況。

**Why this priority**: 財務總覽是平台最基礎的商業指標，影響所有決策。

**Independent Test**: 呼叫 `POST /admin/report/platform-revenue`，回傳 `totalRecharge`、`totalSpend`、`netRevenue`。

**Acceptance Scenarios**:

1. **Given** 2026-04 共有 500 筆 RECHARGE 交易總計 50,000 金幣、300 筆 DRAW_DEDUCTION 總計 30,000 金幣，
   **When** Admin 查詢 2026-04-01 ~ 2026-04-30，
   **Then** 回傳 `{ totalRecharge: 50000, totalSpend: 30000, netRevenue: 20000 }`

2. **Given** 查詢時間範圍為過去 7 天，
   **When** 呼叫 API，
   **Then** `dailyRevenue` 包含 7 筆，每筆含 `date`、`recharge`、`spend`、`net`

3. **Given** 有些交易是用 Bonus（紅利金）消費，
   **When** 查詢，
   **Then** `spendByType` 分開顯示：`{ GOLD: 25000, BONUS: 5000 }`

---

### User Story 2 - 各店家營收貢獻分解 (Priority: P2)

Admin 需要知道各店家分別貢獻了多少消費，以評估店家績效並分配資源。

**Why this priority**: 店家層級分解是制定行銷和補貼策略的基礎。

**Independent Test**: 確認回傳值包含 `storeBreakdown` 陣列，每筆含 `storeId`、`storeName`、`totalSpend`、`drawCount`。

**Acceptance Scenarios**:

1. **Given** 平台有 5 家店，
   **When** Admin 查詢整體報表，
   **Then** `storeBreakdown` 包含 5 筆，按 `totalSpend` 降序排列

2. **Given** A 店本月消費 15,000 金幣、B 店 8,000 金幣，
   **When** 查詢，
   **Then** A 店排在 B 店前面，`totalSpend` 分別為 15000 和 8000

---

### User Story 3 - 時間趨勢比較 (Priority: P3)

Admin 需要比較本期 vs 上期的營收表現，識別成長或衰退趨勢。

**Why this priority**: 環比分析幫助 Admin 快速識別異常。

**Independent Test**: 回傳值包含 `growthRate` 欄位（相較上期的百分比變化）。

**Acceptance Scenarios**:

1. **Given** 本月 totalRecharge 為 50,000，上月為 40,000，
   **When** 查詢包含趨勢比較，
   **Then** `rechargeGrowthRate: 25.0`（百分比）

2. **Given** 無上期資料（第一個月），
   **When** 查詢，
   **Then** `rechargeGrowthRate: null`，不顯示 N/A 或錯誤

---

## Functional Requirements

- **FR-001**: 時間範圍篩選（start/end），預設過去 30 天
- **FR-002**: `totalRecharge` = 期間內所有 `wallet_transaction.transaction_type = 'RECHARGE'` 的 `amount` 加總（coinType=GOLD）
- **FR-003**: `totalSpend` = 期間內所有 `wallet_transaction.transaction_type = 'DRAW'` 的 `amount` 加總（含 GOLD 和 BONUS）
- **FR-004**: `netRevenue` = `totalRecharge - totalSpend`
- **FR-005**: `spendByType` 分別統計：GOLD（`transactionType='DRAW' AND coinType='GOLD'`）和 BONUS（`transactionType='DRAW' AND coinType='BONUS'`）
- **FR-006**: `dailyRevenue` 按 `wallet_transaction.created_at` 日期分組
- **FR-007**: `storeBreakdown` 透過 `lottery_ticket` → `lottery.store_id` 關聯計算各店消費
- **FR-008**: `drawCount` = 期間內 `lottery_ticket.status = 'DRAWN'` 的筆數
- **FR-009**: 權限：僅 Admin 可存取，StoreOwner 不可查看

---

## Data Sources

| 欄位 | 來源 |
|------|------|
| totalRecharge | wallet_transaction (transactionType=RECHARGE, coinType=GOLD) |
| totalSpend | wallet_transaction (transactionType=DRAW, 含 GOLD+BONUS) |
| drawCount | lottery_ticket (status=DRAWN) |
| storeBreakdown | lottery_ticket JOIN lottery (store_id) |
| dailyRevenue | wallet_transaction GROUP BY DATE(created_at) |

---

## API 設計（草案）

```
POST /admin/report/platform-revenue
Authorization: Bearer {adminToken}

Request:
{
  "startDate": "2026-04-01",
  "endDate": "2026-04-30"
}

Response:
{
  "totalRecharge": 50000,
  "totalSpend": 30000,
  "netRevenue": 20000,
  "drawCount": 1500,
  "spendByType": { "GOLD": 25000, "BONUS": 5000 },
  "rechargeGrowthRate": 25.0,
  "spendGrowthRate": 10.0,
  "dailyRevenue": [
    { "date": "2026-04-01", "recharge": 2000, "spend": 1500, "net": 500 }
  ],
  "storeBreakdown": [
    { "storeId": "xxx", "storeName": "A店", "totalSpend": 15000, "drawCount": 300 }
  ]
}
```

---

## Non-Functional Requirements

- **NFR-001**: 查詢時間 < 3 秒（10 萬筆交易以內）
- **NFR-002**: 僅 Admin 角色可存取（`@PreAuthorize("hasRole('ADMIN')")`）
- **NFR-003**: 敏感財務資料，需記錄存取 audit log

---

## Success Criteria

- **SC-001**: `totalRecharge`、`totalSpend`、`netRevenue` 計算準確度 100%
- **SC-002**: `storeBreakdown` 加總的 `totalSpend` 等於全平台 `totalSpend`
- **SC-003**: `dailyRevenue` 每天資料無缺漏（即使當天無交易也回傳 count=0）

---

## 澄清紀錄

| 問題 | 決定 |
|------|------|
| 誰可以存取？ | 僅 Admin |
| netRevenue 計算方式？ | totalRecharge - totalSpend（金幣點數） |
| spendByType 是否分開？ | 是，GOLD 和 BONUS 分開統計 |