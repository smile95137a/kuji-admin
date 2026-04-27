# Feature Specification: 會員成長報表

**Feature Branch**: `030-member-growth-report`  
**Created**: 2026-04-28  
**Status**: Draft  

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Admin 查看平台新會員趨勢 (Priority: P1)

Admin 想了解「上個月新增了幾位會員？每天的趨勢如何？Google 登入佔幾成？」，用來評估推廣效果。

**Why this priority**: 會員成長是平台最重要的健康指標，Admin 需要每週關注。

**Independent Test**: 呼叫 `POST /admin/report/member-growth`，確認回傳 `newMemberCount`、`dailyNewMembers`、`providerDistribution` 三個核心欄位。

**Acceptance Scenarios**:

1. **Given** 2026-04 有 30 位新會員，其中 Email 20 位、Google 10 位，**When** 呼叫報表帶 `dateStart=2026-04-01, dateEnd=2026-04-30`，**Then** `newMemberCount: 30`，`providerDistribution.EMAIL: 20`，`providerDistribution.GOOGLE: 10`
2. **Given** 不帶日期條件，**When** 呼叫 API，**Then** 預設查最近 30 天，`dailyNewMembers` 陣列長度為 30
3. **Given** 某日有 5 位新會員，**When** 查看 `dailyNewMembers`，**Then** 該日期的 `count: 5`

---

### User Story 2 - 活躍會員與 ARPU 分析 (Priority: P2)

Admin 想知道「期間內有多少位會員有消費？平均每人消費多少？」，用來評估用戶貨幣化效率。

**Why this priority**: ARPU（每用戶平均收入）是電商平台的核心財務指標。

**Independent Test**: 確認回傳 `activeMemberCount`（有消費的會員數）和 `arpu`（總收入 / 活躍人數）欄位。

**Acceptance Scenarios**:

1. **Given** 期間內有 10 位會員有消費，總消費為 5000，**When** 呼叫報表，**Then** `activeMemberCount: 10`，`arpu: 500.0`
2. **Given** 期間內所有會員都沒有消費，**When** 呼叫報表，**Then** `activeMemberCount: 0`，`arpu: 0.0`，不報錯

---

### User Story 3 - 登入方式分布與留存率 (Priority: P3)

Admin 想知道「這個月有多少老會員回來登入？Google 跟 Email 用戶的留存率有差異嗎？」，以便調整 UX 策略。

**Why this priority**: 留存率是會員品質的關鍵指標，協助判斷投資 Google OAuth 是否值得。

**Independent Test**: 確認回傳 `returningMemberCount`（期間內有登入但非新會員）欄位。

**Acceptance Scenarios**:

1. **Given** 期間內有 50 位老會員登入，**When** 呼叫報表，**Then** `returningMemberCount: 50`
2. **Given** Email 會員登入率 60%、Google 會員登入率 80%，**When** 查看 `providerDistribution`，**Then** 包含各 provider 的活躍率佔比

---

### Edge Cases

- 期間內沒有任何新會員時，`newMemberCount: 0`，`dailyNewMembers` 為空陣列，不報錯
- ARPU 計算使用 `gold_coins` 消費流水（wallet_transaction type=DRAW_DEDUCTION），非儲值金額
- StoreOwner 和 StoreEditor 角色沒有查看此報表的權限（ADMIN 限定）

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: 系統 MUST 提供 `POST /admin/report/member-growth` API，只允許 Admin 角色存取
- **FR-002**: 系統 MUST 回傳新會員總數 `newMemberCount` 與每日明細 `dailyNewMembers`
- **FR-003**: 系統 MUST 回傳登入方式分布 `providerDistribution`（key: EMAIL/GOOGLE，value: 數量）
- **FR-004**: 系統 MUST 計算活躍會員數 `activeMemberCount`（期間有消費紀錄的唯一用戶數）
- **FR-005**: 系統 MUST 計算 ARPU `arpu`（期間總金幣消費 / activeMemberCount），活躍為 0 時回傳 0
- **FR-006**: 系統 MUST 回傳回訪會員數 `returningMemberCount`（期間有登入且 created_at 早於 dateStart）
- **FR-007**: 不帶日期條件時，系統 MUST 預設查詢最近 30 天
- **FR-008**: 系統 MUST 拒絕非 Admin 角色存取，回傳 403

### Key Entities

- **User（前台會員）**: `created_at`（註冊時間）、`last_login_at`（最後登入）、`provider`（EMAIL/GOOGLE）
- **WalletTransaction（消費紀錄）**: `type=DRAW_DEDUCTION` 表示花費金幣，`user_id` 對應消費者

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Admin 可在 1 個 API 呼叫中取得完整的會員成長概覽，不需多次請求
- **SC-002**: 報表在資料量 100,000 用戶時，回應時間在 3 秒內
- **SC-003**: `newMemberCount` 與直接查 `user` 表 `COUNT(*) WHERE created_at IN 日期範圍` 的結果 100% 一致
- **SC-004**: ARPU 計算值可讓 Admin 與財務報表的總收入數字對照驗算，誤差 < 1%
