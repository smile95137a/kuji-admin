# Feature Specification: 商品銷售排行報表

**Feature Branch**: `031-lottery-sales-ranking`  
**Created**: 2026-04-28  
**Status**: Draft  

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 查看商品銷售排行（依抽獎次數） (Priority: P1)

StoreOwner 想知道「我的哪幾個商品最受歡迎？哪個商品被玩最多次？」，用來決定下一期要主推哪個商品。

**Why this priority**: 商品排行是店家最直接需要的商業洞察，可直接影響進貨與行銷決策。

**Independent Test**: 呼叫 `POST /admin/report/lottery-ranking`，確認回傳的 `items` 陣列依 `drawCount` 降冪排序，並含商品基本資訊。

**Acceptance Scenarios**:

1. **Given** 店家有 A 商品（抽 50 次）、B 商品（抽 30 次）、C 商品（抽 80 次），**When** 不帶排序條件呼叫，**Then** 順序為 C → A → B
2. **Given** StoreOwner 呼叫報表，**When** 未傳任何條件，**Then** 只顯示自己店家的商品，不包含其他店
3. **Given** Admin 呼叫報表，**When** 未傳 storeId，**Then** 顯示全平台所有商品排行

---

### User Story 2 - 依營業額排序與篩選 (Priority: P2)

StoreOwner 想知道「哪個商品幫我賺最多錢？」，以便評估商品投資報酬率。

**Why this priority**: 抽獎次數高不代表獲利高（單價不同），營業額排行才是真正的財務指標。

**Independent Test**: 呼叫 API 帶 `sortBy: "revenue"`，確認 `items` 陣列依 `revenue` 降冪排序。

**Acceptance Scenarios**:

1. **Given** A 商品（50 抽 × 100 元 = 5000）、B 商品（200 抽 × 20 元 = 4000），帶 `sortBy=revenue`，**When** 呼叫，**Then** A 排第一（revenue 5000 > 4000）
2. **Given** 傳入 `category: "GACHA"` 篩選條件，**When** 呼叫，**Then** 只返回 `category=GACHA` 的商品排行

---

### User Story 3 - 完售率排行與剩餘籤分析 (Priority: P3)

店家想知道「哪個商品最快被玩完、哪個賣不動？」，用來識別滯銷品並決定下架或促銷。

**Why this priority**: 完售率反映市場熱度，協助店家優化庫存配置。

**Independent Test**: 呼叫 API 帶 `sortBy: "sellThroughRate"`，確認回傳 `sellThroughRate`（已抽 / 總籤數）並依此排序。

**Acceptance Scenarios**:

1. **Given** A 商品（總 100 籤，已抽 95）、B 商品（總 50 籤，已抽 50），**When** 帶 `sortBy=sellThroughRate`，**Then** B 排第一（100% > 95%）
2. **Given** 商品尚未開始抽獎（已抽 0），**When** 查看排行，**Then** `sellThroughRate: 0.0`，不報錯
3. **Given** 傳入 `status: "ON_SHELF"` 篩選，**When** 呼叫，**Then** 只返回目前上架中的商品

---

### Edge Cases

- 期間內沒有任何抽獎紀錄的商品，`drawCount: 0`，`revenue: 0`，仍需出現在列表中（依 sortBy 排到最後）
- Admin 傳入特定 `storeId` 時，結果需限制在該店家範圍內
- StoreOwner 傳入其他店的 `storeId`，後端自動以自己的 storeId 覆蓋
- 同分（相同抽獎次數）時，以 `createdAt` 較新的商品排前面

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: 系統 MUST 提供 `POST /admin/report/lottery-ranking` API，Admin 和 StoreOwner 可存取
- **FR-002**: 系統 MUST 回傳排行列表 `items`，每項包含：`lotteryId`、`title`、`category`、`imageUrl`、`pricePerDraw`、`maxDraws`、`drawCount`、`revenue`、`sellThroughRate`
- **FR-003**: 系統 MUST 支援 `sortBy` 排序欄位（`drawCount`/`revenue`/`sellThroughRate`），預設 `drawCount` 降冪
- **FR-004**: 系統 MUST 支援 `category` 篩選（OFFICIAL_ICHIBAN / GACHA / TRADING_CARD / CUSTOM_GACHA）
- **FR-005**: 系統 MUST 支援 `status` 篩選（ON_SHELF / OFF_SHELF / DRAFT）
- **FR-006**: 系統 MUST 支援日期範圍 `dateStart` / `dateEnd`（統計該期間內的抽獎次數與營業額）
- **FR-007**: StoreOwner 呼叫時，系統 MUST 自動限制只返回該店家商品
- **FR-008**: Admin 呼叫不帶 storeId 時，返回全平台排行；Admin 帶 storeId 時，限制在該店家範圍
- **FR-009**: `revenue` = 期間內該商品的所有訂單金額總和（order.total_price WHERE lottery_id = ?）
- **FR-010**: `sellThroughRate` = 已抽籤數 / 總籤數（lottery_ticket 表計算）

### Key Entities

- **Lottery（商品）**: `id`, `title`, `category`, `status`, `price_per_draw`, `max_draws`, `store_id`
- **LotteryTicket（籤位）**: 每張票對應一次抽獎機會，`status=DRAWN` 表示已抽
- **Order（訂單）**: 包含 `lottery_id` 和 `total_price`，用於計算每商品的營業額

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 排行 API 在商品數量 500 個、訂單 50,000 筆時，回應時間在 3 秒內
- **SC-002**: Admin 可在 1 個請求取得全平台所有商品的完整排行，無需分頁（返回全量 List）
- **SC-003**: `drawCount` 與直接查 `lottery_ticket WHERE status=DRAWN` 的 COUNT 結果 100% 一致
- **SC-004**: `revenue` 與 RevenueReport 對應商品的收入數字一致，可交叉驗算
