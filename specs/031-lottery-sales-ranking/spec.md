# Feature Specification: 031 - 商品銷售排行報表

**Feature Branch**: `031-lottery-sales-ranking`
**Created**: 2026-04-28
**Status**: Draft

---

## 背景說明

統計各商品（Lottery）的全生命期銷售表現，包含抽籤次數與營收，  
提供排行榜視圖讓 Admin 與 StoreOwner 快速識別熱門商品。

---

## User Scenarios & Testing

### User Story 1 - 查看商品銷售排行榜 (Priority: P1)

StoreOwner 登入後，想知道哪些商品賣最好（抽籤次數最多），以便調整商品策略。

**Why this priority**: 銷售排行是最直接的商品表現指標。

**Independent Test**: 呼叫 `POST /admin/report/lottery-sales`，回傳依照 drawCount 排序的商品清單。

**Acceptance Scenarios**:

1. **Given** 店家有 10 個商品，各有不同的抽籤次數，
   **When** StoreOwner 呼叫排行 API，
   **Then** 回傳依 `drawCount` 降序排列的商品列表，最多前 20 名

2. **Given** 其中一個商品狀態為 CANCELLED 的訂單被排除，
   **When** 查詢排行，
   **Then** 該商品的 `revenue` 不含 CANCELLED 訂單金額

3. **Given** 登入者為 StoreOwner，
   **When** 呼叫 API，
   **Then** 只看到自己店家的商品排行

---

### User Story 2 - 查看商品營收排行 (Priority: P2)

Admin 需要知道哪些商品產生最多營收，以評估各店家的商品效益。

**Why this priority**: 營收排行幫助平台找到明星商品，輔助店家優化定價。

**Independent Test**: 確認 `revenue` 欄位透過 `order_item` JOIN 計算正確，且排除 CANCELLED 訂單。

**Acceptance Scenarios**:

1. **Given** 商品 A 共有 100 次抽籤，每次 80 點，所有訂單均非 CANCELLED，
   **When** 查詢 A 的營收，
   **Then** `revenue: 8000`

2. **Given** 商品 B 有 50 次抽籤，其中 10 次對應的訂單被 CANCELLED，
   **When** 查詢，
   **Then** `revenue` 只計算 40 筆有效訂單的金額

---

### User Story 3 - Admin 跨店家商品比較 (Priority: P3)

Admin 需要跨所有店家比較商品表現，找出平台最熱門的商品。

**Why this priority**: 跨店比較是平台層級的分析需求，優先度低於單店查看。

**Independent Test**: Admin 不帶 storeId 呼叫 API，回傳全平台商品排行。

**Acceptance Scenarios**:

1. **Given** 全平台有 100 個商品，
   **When** Admin 呼叫報表（不帶 storeId），
   **Then** 回傳所有商品的排行（或依 storeId 分組）

2. **Given** 需要篩選特定店家，
   **When** Admin 帶入特定 storeId，
   **Then** 只回傳該店家的商品排行

---

### Edge Cases

- `drawCount` 為 0 的商品仍應出現在排行中（未抽過的新商品）
- `revenue` 計算需 JOIN `order_item` 表（order 表無直接 lotteryId），需排除 CANCELLED 訂單
- StoreOwner 不可查看其他店家的商品排行
- 全生命期統計（不受日期範圍過濾），反映商品自上架以來的累積表現

---

## Requirements

### Functional Requirements

- **FR-001**: 系統 MUST 提供 `POST /admin/report/lottery-sales` API，支援 storeId 過濾
- **FR-002**: 系統 MUST 回傳商品清單，每筆包含 `lotteryId`、`lotteryTitle`、`drawCount`、`revenue`、`storeName`
- **FR-003**: 系統 MUST 依照 `drawCount` 降序排列（預設），支援改以 `revenue` 排序
- **FR-004**: `drawCount` MUST 來自 `lottery_ticket` 表中 `status = DRAWN` 的筆數（全生命期）
- **FR-005**: `revenue` MUST 透過 `order_item.lottery_id` JOIN `order` 計算，排除 `order.status = CANCELLED` 的訂單
- **FR-006**: 系統 MUST 預設只回傳前 20 名商品，支援 `limit` 參數調整（最大 100）
- **FR-007**: StoreOwner 查詢 MUST 只回傳自己店家商品，後端強制綁定 storeId
- **FR-008**: Admin 查詢 MUST 支援帶入 storeId 過濾，或不帶 storeId 查全平台

### Key Entities

- **Lottery（商品）**: `id`、`title`、`store_id`、`price_per_draw`
- **LotteryTicket（籤位）**: `lottery_id`、`status=DRAWN` → drawCount 來源
- **OrderItem（訂單項目）**: `lottery_id`、`order_id` → revenue JOIN 橋接表
- **Order（訂單）**: `status`（排除 CANCELLED）、`total_amount` 或以 price_per_draw 計算

---

## Success Criteria

### Measurable Outcomes

- **SC-001**: 排行 API 在 50,000 筆籤位資料下查詢時間 < 3 秒
- **SC-002**: `revenue` 計算與實際訂單金額誤差為 0（精確計算）
- **SC-003**: 排行榜排序結果與手動 SQL 查詢結果一致

---

## 澄清紀錄

| 問題 | 決定 |
|------|------|
| drawCount/revenue 是時間範圍篩選還是全生命期？ | **全生命期累積統計**（不受日期過濾） |
| CANCELLED 訂單是否計入 revenue？ | **排除**，只計算有效訂單 |
| revenue 計算方式？ | 透過 `order_item.lottery_id` JOIN `order`，不直接使用 order 表 |