# Feature Specification: 029 - 獎品出貨報表

**Feature Branch**: `029-prize-shipment-report`
**Created**: 2026-04-28
**Status**: Draft

---

## 背景說明

本報表以 **Order（訂單）** 為核心資料來源，追蹤各訂單的出貨狀態與時效。  
⚠️ DDL 變更：需在 `order` 表新增 `preparing_at DATETIME NULL` 欄位，  
在訂單狀態轉為 `PREPARING` 時自動記錄時間點，用於計算出貨時效。

---

## User Scenarios & Testing

### User Story 1 - 店家查看出貨狀態總覽 (Priority: P1)

Store Owner 登入後台，需要一眼看到目前店家的訂單出貨狀況（待處理/備貨中/已出貨/已完成各有多少筆），以便安排出貨優先順序。

**Why this priority**: 出貨狀態是店家最日常的運營需求，影響客戶滿意度。

**Independent Test**: 呼叫 `POST /admin/report/prize-shipment`，回傳值包含 4 個狀態計數。

**Acceptance Scenarios**:

1. **Given** 店家有 5 筆 PENDING、3 筆 PREPARING、8 筆 SHIPPED、10 筆 COMPLETED 訂單，
   **When** 呼叫報表 API 帶入當前 storeId，
   **Then** 回傳 `{ pendingCount: 5, preparingCount: 3, shippedCount: 8, completedCount: 10 }`

2. **Given** 查詢時間範圍為 2026-04-01 ~ 2026-04-30，
   **When** 呼叫 API，
   **Then** `dailyDetails` 只包含該範圍內每日的出貨筆數

3. **Given** 登入者為 StoreOwner，
   **When** 呼叫 API，
   **Then** 只回傳自己店家的訂單數據，不會看到其他店家資料

---

### User Story 2 - 出貨時效與逾期警示 (Priority: P2)

店家管理者需要知道平均出貨天數，以及有多少訂單已超過 7 天仍在 PENDING，以便主動追蹤並改善。

**Why this priority**: 出貨時效直接影響客戶體驗與店家評分。

**Independent Test**: 確認 `avgShipDays` 計算正確，並能顯示 `overdueCount`（超過 7 天未備貨的筆數）。

**Acceptance Scenarios**:

1. **Given** 3 筆已出貨訂單的 preparing_at → shipped_at 天數分別為 2、4、6 天，
   **When** 呼叫報表，
   **Then** `avgShipDays` 為 4.0

2. **Given** 有 2 筆訂單自建立已超過 7 天仍在 PENDING 狀態，
   **When** 呼叫報表，
   **Then** `overdueCount: 2`

---

### User Story 3 - Admin 跨店家出貨比較 (Priority: P3)

Admin 需要比較各店家的出貨時效，找出效率差的店家進行輔導。

**Why this priority**: 平台營運管控需求，優先級低於日常店家使用。

**Independent Test**: Admin 呼叫報表，`storeDetails` 包含每家店的出貨時效統計。

**Acceptance Scenarios**:

1. **Given** 平台有多家店，
   **When** Admin 呼叫報表（不帶 storeId），
   **Then** `storeDetails` 陣列包含每家店的 `storeName`、`avgShipDays`、各狀態計數

2. **Given** B 店平均出貨時效為 12 天，
   **When** Admin 查看，
   **Then** B 店的 `avgShipDays: 12.0` 明顯高於其他店，可篩選標記

---

### Edge Cases

- 查詢期間無任何訂單時，計數全為 0，`dailyDetails` 回傳空陣列，`avgShipDays` 為 null
- StoreOwner 不可指定其他 storeId，後端自動帶入 JWT 中的 storeId 覆蓋
- 排除狀態為 `CANCELLED` 的訂單，不列入任何計數與時效計算
- `preparing_at` 為 null（訂單在新功能上線前建立）時，該筆不計入 avgShipDays

---

## Requirements

### Functional Requirements

- **FR-001**: 系統 MUST 提供 `POST /admin/report/prize-shipment` API，支援 storeId 與日期範圍查詢
- **FR-002**: 系統 MUST 回傳 4 個訂單狀態計數：`pendingCount`、`preparingCount`、`shippedCount`、`completedCount`
- **FR-003**: 系統 MUST 計算平均出貨天數 `avgShipDays`，公式為 `preparing_at` → `shipped_at` 的天數差（不含 null 的筆數）
- **FR-004**: 系統 MUST 計算逾期未處理筆數 `overdueCount`（超過 7 天仍在 PENDING 的訂單）
- **FR-005**: 系統 MUST 回傳每日出貨明細 `dailyDetails`（日期 + 當日各狀態筆數）
- **FR-006**: Admin 查詢 MUST 回傳跨店家統計資料 `storeDetails`
- **FR-007**: StoreOwner 查詢 MUST 只回傳自己店家數據，後端強制綁定 storeId
- **FR-008**: 預設查詢時間範圍 MUST 為最近 30 天

### DDL 需求

- **DDL-001**: `order` 表新增 `preparing_at DATETIME NULL` 欄位
- **DDL-002**: 當訂單狀態轉換為 `PREPARING` 時，後端 Service 自動設定 `preparing_at = NOW()`

### Key Entities

- **Order（訂單）**: 主要資料來源，使用 `status`（PENDING/PREPARING/SHIPPED/COMPLETED）、`created_at`、`preparing_at`（新增）、`shipped_at`、`cancelled_at`
- **PrizeBox（賞品盒）**: 本報表不直接使用，由 Order 追蹤出貨流程

---

## Success Criteria

### Measurable Outcomes

- **SC-001**: 報表 API 在 10,000 筆訂單下查詢時間 < 3 秒
- **SC-002**: 店家在 1 個工作日內可看到即時的出貨狀態，不需要手動計算
- **SC-003**: `avgShipDays` 計算精確到 0.1 天
- **SC-004**: 逾期訂單識別準確率 100%（不漏報、不誤報）

---

## 澄清紀錄

| 問題 | 決定 |
|------|------|
| 報表基於 Order 還是 PrizeBox？ | **Order**（有完整 PENDING/PREPARING/SHIPPED/COMPLETED 狀態） |
| avgShipDays 計算起點？ | **新增 `preparing_at` 欄位**，計算 `preparing_at → shipped_at` |
| CANCELLED 訂單是否計入？ | **排除** |