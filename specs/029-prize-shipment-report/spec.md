# Feature Specification: 獎品出貨報表

**Feature Branch**: `029-prize-shipment-report`  
**Created**: 2026-04-28  
**Status**: Draft  

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 店家查看出貨待辦概覽 (Priority: P1)

Store Owner 登入後台，進入「出貨報表」頁面，可立刻看到「目前有幾個獎品箱待出貨、幾個正在準備、幾個已完成」，並可依日期範圍篩選。

**Why this priority**: 出貨管理是店家最直接的日常操作需求，過去沒有彙總資料，只能逐一查訂單。

**Independent Test**: 呼叫 `POST /admin/report/prize-shipment`，確認回傳各狀態數量及每日出貨明細。

**Acceptance Scenarios**:

1. **Given** 店家有 5 個 PENDING、3 個 SHIPPING、10 個 COMPLETED 的獎品箱，**When** 呼叫報表 API 不帶日期條件，**Then** 回傳 `pendingCount: 5, shippingCount: 3, completedCount: 10`
2. **Given** 店家選擇 2026-04-01 ~ 2026-04-30，**When** 呼叫 API，**Then** dailyDetails 只包含該區間的每日出貨數量
3. **Given** 當前使用者為 StoreOwner，**When** 呼叫 API，**Then** 自動只回傳該店家的資料（不可查其他店）

---

### User Story 2 - 平均出貨時間與逾期風險 (Priority: P2)

店家想知道「從訂單確認到實際出貨，平均要幾天？有多少訂單超過 7 天還沒出貨？」以評估客服風險。

**Why this priority**: 幫助店家識別出貨效率問題，減少客訴。

**Independent Test**: 確認 `avgShipDays`（平均出貨天數）和 `overdueCount`（超過 7 天未出貨數）兩個欄位有正確計算。

**Acceptance Scenarios**:

1. **Given** 3 筆已完成訂單出貨天數分別為 2、4、6 天，**When** 呼叫報表，**Then** `avgShipDays` 為 4.0
2. **Given** 有 2 筆訂單已超過 7 天仍為 PENDING 狀態，**When** 呼叫報表，**Then** `overdueCount: 2`

---

### User Story 3 - Admin 跨店家出貨總覽 (Priority: P3)

Admin 想比較各店家的出貨效率，找出哪家店出貨最慢，決定是否介入處理。

**Why this priority**: 平台層級的品質監控。

**Independent Test**: Admin 呼叫報表，`storeDetails` 陣列包含所有店家的各自出貨統計。

**Acceptance Scenarios**:

1. **Given** 平台有 A、B 兩家店，**When** Admin 呼叫報表，**Then** `storeDetails` 包含兩家店的出貨數量與平均天數
2. **Given** B 店平均出貨天數為 12 天，**When** Admin 查看，**Then** B 店 `avgShipDays: 12.0` 且高於平台均值有標記

---

### Edge Cases

- 期間內沒有任何出貨訂單時，所有數值應為 0，dailyDetails 為空陣列，不應報錯
- StoreOwner 嘗試指定其他 `storeId` 時，後端自動以自己的 storeId 覆蓋
- 統計計算中，只有 `SHIPPED` / `COMPLETED` 狀態算「已出貨」；`PENDING` / `PREPARING` 算「待出貨」

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: 系統 MUST 提供 `POST /admin/report/prize-shipment` API，接受日期範圍與 storeId 條件
- **FR-002**: 系統 MUST 回傳各狀態獎品箱數量：`pendingCount`、`preparingCount`、`shippedCount`、`completedCount`
- **FR-003**: 系統 MUST 計算並回傳平均出貨天數 `avgShipDays`（從 PREPARING → SHIPPED 的天數差）
- **FR-004**: 系統 MUST 計算並回傳逾期未出貨數 `overdueCount`（超過 7 天仍為 PENDING）
- **FR-005**: 系統 MUST 回傳每日出貨明細 `dailyDetails`（日期 + 各狀態數量）
- **FR-006**: Admin 呼叫時，MUST 回傳各店家統計列表 `storeDetails`
- **FR-007**: StoreOwner 呼叫時，系統 MUST 自動限制只回傳該店家資料（無論是否傳入 storeId）
- **FR-008**: 不傳日期時，系統 MUST 預設查詢最近 30 天資料

### Key Entities

- **PrizeBox（獎品箱）**: 每次抽獎得到的實體獎品容器，狀態為 PENDING / PREPARING / SHIPPED / COMPLETED
- **Order（訂單）**: 關聯獎品箱的出貨請求，包含收件人資訊與出貨狀態

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 報表 API 在資料量 10,000 筆獎品箱時，回應時間在 3 秒內
- **SC-002**: 店家可在 1 個頁面內完整看到本期出貨狀況，不需跳轉其他頁面
- **SC-003**: `avgShipDays` 計算誤差不超過 0.1 天
- **SC-004**: 所有報表資料與訂單管理頁的實際狀態數量 100% 一致（無資料漂移）


## User Scenarios & Testing *(mandatory)*

<!--
  IMPORTANT: User stories should be PRIORITIZED as user journeys ordered by importance.
  Each user story/journey must be INDEPENDENTLY TESTABLE - meaning if you implement just ONE of them,
  you should still have a viable MVP (Minimum Viable Product) that delivers value.
  
  Assign priorities (P1, P2, P3, etc.) to each story, where P1 is the most critical.
  Think of each story as a standalone slice of functionality that can be:
  - Developed independently
  - Tested independently
  - Deployed independently
  - Demonstrated to users independently
-->

### User Story 1 - [Brief Title] (Priority: P1)

[Describe this user journey in plain language]

**Why this priority**: [Explain the value and why it has this priority level]

**Independent Test**: [Describe how this can be tested independently - e.g., "Can be fully tested by [specific action] and delivers [specific value]"]

**Acceptance Scenarios**:

1. **Given** [initial state], **When** [action], **Then** [expected outcome]
2. **Given** [initial state], **When** [action], **Then** [expected outcome]

---

### User Story 2 - [Brief Title] (Priority: P2)

[Describe this user journey in plain language]

**Why this priority**: [Explain the value and why it has this priority level]

**Independent Test**: [Describe how this can be tested independently]

**Acceptance Scenarios**:

1. **Given** [initial state], **When** [action], **Then** [expected outcome]

---

### User Story 3 - [Brief Title] (Priority: P3)

[Describe this user journey in plain language]

**Why this priority**: [Explain the value and why it has this priority level]

**Independent Test**: [Describe how this can be tested independently]

**Acceptance Scenarios**:

1. **Given** [initial state], **When** [action], **Then** [expected outcome]

---

[Add more user stories as needed, each with an assigned priority]

### Edge Cases

<!--
  ACTION REQUIRED: The content in this section represents placeholders.
  Fill them out with the right edge cases.
-->

- What happens when [boundary condition]?
- How does system handle [error scenario]?

## Requirements *(mandatory)*

<!--
  ACTION REQUIRED: The content in this section represents placeholders.
  Fill them out with the right functional requirements.
-->

### Functional Requirements

- **FR-001**: System MUST [specific capability, e.g., "allow users to create accounts"]
- **FR-002**: System MUST [specific capability, e.g., "validate email addresses"]  
- **FR-003**: Users MUST be able to [key interaction, e.g., "reset their password"]
- **FR-004**: System MUST [data requirement, e.g., "persist user preferences"]
- **FR-005**: System MUST [behavior, e.g., "log all security events"]

*Example of marking unclear requirements:*

- **FR-006**: System MUST authenticate users via [NEEDS CLARIFICATION: auth method not specified - email/password, SSO, OAuth?]
- **FR-007**: System MUST retain user data for [NEEDS CLARIFICATION: retention period not specified]

### Key Entities *(include if feature involves data)*

- **[Entity 1]**: [What it represents, key attributes without implementation]
- **[Entity 2]**: [What it represents, relationships to other entities]

## Success Criteria *(mandatory)*

<!--
  ACTION REQUIRED: Define measurable success criteria.
  These must be technology-agnostic and measurable.
-->

### Measurable Outcomes

- **SC-001**: [Measurable metric, e.g., "Users can complete account creation in under 2 minutes"]
- **SC-002**: [Measurable metric, e.g., "System handles 1000 concurrent users without degradation"]
- **SC-003**: [User satisfaction metric, e.g., "90% of users successfully complete primary task on first attempt"]
- **SC-004**: [Business metric, e.g., "Reduce support tickets related to [X] by 50%"]
