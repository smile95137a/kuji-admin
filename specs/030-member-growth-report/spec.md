# Feature Specification: 030 - 會員成長報表

**Feature Branch**: `030-member-growth-report`
**Created**: 2026-04-28
**Status**: Draft

---

## 背景說明

追蹤平台前台會員的成長趨勢，包含新增會員數、活躍度、消費行為，  
協助平台管理者了解用戶生命週期並制定行銷策略。

---

## User Scenarios & Testing

### User Story 1 - Admin 查看整體會員成長趨勢 (Priority: P1)

Admin 登入後台，需要知道每日/每月新增了多少會員，以評估行銷活動成效。

**Why this priority**: 會員成長是平台健康度最核心的指標。

**Independent Test**: 呼叫 `POST /admin/report/member-growth`，回傳新增會員數的時間序列資料。

**Acceptance Scenarios**:

1. **Given** 2026-04 月共新增 150 名會員，
   **When** Admin 查詢 2026-04-01 ~ 2026-04-30，
   **Then** 回傳 `totalNewMembers: 150`，`dailyNewMembers` 包含 30 天的每日數據

2. **Given** 以 Google OAuth 註冊的有 80 人、Email 註冊有 70 人，
   **When** 查詢同範圍，
   **Then** 回傳 `registrationByProvider: { GOOGLE: 80, EMAIL: 70 }`

3. **Given** 查詢時間範圍為過去 30 天，
   **When** 呼叫 API，
   **Then** `dailyNewMembers` 陣列長度為 30，每筆包含 `date` 和 `count`

---

### User Story 2 - 活躍會員與 ARPU 分析 (Priority: P2)

Admin 需要知道活躍會員（期間內有抽獎紀錄的會員）的消費狀況，分別統計金幣與紅利金的平均消費。

**Why this priority**: ARPU 是判斷平台營收健康度的關鍵指標。

**Independent Test**: 確認 `activeMembers`、`arpuGold`、`arpuBonus` 三個欄位計算正確。

**Acceptance Scenarios**:

1. **Given** 查詢期間有 500 名活躍會員，總金幣消費 100,000 點、總紅利金消費 20,000 點，
   **When** 查詢報表，
   **Then** 回傳 `activeMembers: 500`、`arpuGold: 200.0`、`arpuBonus: 40.0`

2. **Given** 某會員只用紅利金消費，沒有金幣消費，
   **When** 查詢，
   **Then** 該會員仍計入 `activeMembers`，`arpuGold` 不因此偏低

---

### User Story 3 - 會員留存率分析 (Priority: P3)

Admin 需要了解新會員的 7 天與 30 天留存率，以評估用戶體驗與留客策略。

**Why this priority**: 留存率反映平台黏著度，但實作複雜度較高，優先度稍低。

**Independent Test**: 查詢留存率欄位 `retention7Days`、`retention30Days`，值在 0~100 之間。

**Acceptance Scenarios**:

1. **Given** 上月新增 100 名會員，其中 60 人在 7 天內有第二次抽獎，
   **When** 查詢留存率，
   **Then** `retention7Days: 60.0`（百分比）

2. **Given** 100 名新會員中，30 天後仍活躍的有 35 人，
   **When** 查詢，
   **Then** `retention30Days: 35.0`

---

### Edge Cases

- 查詢期間無任何新增會員時，全部計數為 0，不回傳 null
- ARPU 計算只包含 `wallet_transaction.type = DRAW_DEDUCTION`（金幣）和 `BONUS_DEDUCTION`（紅利金）
- 活躍會員定義：查詢期間內有至少 1 次抽獎紀錄（`lottery_ticket.status = DRAWN`）
- 留存率計算基準為前一個完整月份的新增會員，不適用於當前進行中月份

---

## Requirements

### Functional Requirements

- **FR-001**: 系統 MUST 提供 `POST /admin/report/member-growth` API，僅 Admin 可存取
- **FR-002**: 系統 MUST 回傳查詢期間的總新增會員數 `totalNewMembers`
- **FR-003**: 系統 MUST 回傳每日新增會員明細 `dailyNewMembers`（日期 + 數量）
- **FR-004**: 系統 MUST 依照註冊方式分類 `registrationByProvider`（EMAIL、GOOGLE）
- **FR-005**: 系統 MUST 回傳活躍會員數 `activeMembers`（期間內有 DRAWN 紀錄的不重複 userId 數）
- **FR-006**: 系統 MUST 分別計算 `arpuGold` 與 `arpuBonus`，公式為各類型總消費 / activeMembers
- **FR-007**: 系統 MUST 計算 7 天與 30 天留存率 `retention7Days`、`retention30Days`（以百分比表示）
- **FR-008**: 預設查詢時間範圍 MUST 為最近 30 天

### Key Entities

- **User（會員）**: `created_at`、`provider`（EMAIL/GOOGLE）
- **LotteryTicket（籤位）**: `user_id`、`status=DRAWN`、`created_at` → 活躍度依據
- **WalletTransaction（錢包紀錄）**: `type`（DRAW_DEDUCTION / BONUS_DEDUCTION）、`amount`、`user_id` → ARPU 計算依據

---

## Success Criteria

### Measurable Outcomes

- **SC-001**: 報表 API 在 100,000 名會員資料下查詢時間 < 5 秒
- **SC-002**: ARPU 計算精確到小數點後 1 位
- **SC-003**: `arpuGold` 與 `arpuBonus` 分開呈現，不合併計算

---

## 澄清紀錄

| 問題 | 決定 |
|------|------|
| ARPU 包含 Gold 還是 Gold+Bonus？ | **兩者都計算但分開呈現**：`arpuGold` 和 `arpuBonus` 各自獨立 |
| StoreEditor 可否存取？ | **僅 Admin**，店家無法查看整體會員報表 |
| 「活躍會員」定義？ | **有任一行為即算活躍**：登入（last_login_at）OR 儲值（RECHARGE）OR 抽獎（DRAWN）OR 建立訂單（order 建立） |