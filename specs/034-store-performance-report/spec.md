# Feature Specification: 034 - 店家績效比較報表

**Feature Branch**: `034-store-performance-report`
**Created**: 2026-04-28
**Status**: Draft

---

## 背景說明

提供 Admin 跨店家績效比較視圖，識別高效/低效店家，支援平台運營決策。  
資料來源：`order`、`lottery_ticket`、`user`（透過 lottery_ticket 活躍行為）、`store`。

---

## User Scenarios & Testing

### User Story 1 - Admin 查看所有店家績效排行 (Priority: P1)

Admin 需要一張表格比較所有店家的核心 KPI：總消費、抽籤數、活躍用戶數、出貨率。

**Why this priority**: 跨店比較是平台管理的核心工具，幫助識別需要輔導的店家。

**Independent Test**: 呼叫 `POST /admin/report/store-performance`，回傳所有店家的績效陣列。

**Acceptance Scenarios**:

1. **Given** 平台有 5 家店，
   **When** Admin 查詢，
   **Then** 回傳 5 筆，每筆包含 `storeId`、`storeName`、`totalRevenue`、`drawCount`、`activeUsers`、`shipRate`

2. **Given** A 店 drawCount 最高為 500，
   **When** 依 `drawCount` 降序排序，
   **Then** A 店排第一

3. **Given** 時間範圍為 2026-04，
   **When** 查詢，
   **Then** 所有 KPI 僅計算該時間段內的資料

---

### User Story 2 - 出貨率與逾期率比較 (Priority: P2)

Admin 需要識別哪些店家的出貨效率低下（出貨率低、逾期率高）。

**Why this priority**: 出貨品質直接影響用戶體驗與平台口碑。

**Independent Test**: 確認 `shipRate` 和 `overdueRate` 欄位計算正確。

**Acceptance Scenarios**:

1. **Given** A 店有 100 筆 SHIPPED+COMPLETED 訂單 / 120 筆非 CANCELLED 訂單，
   **When** 查詢，
   **Then** `shipRate: 83.3`（百分比）

2. **Given** B 店有 10 筆超過 7 天仍在 PENDING 的訂單 / 50 筆總訂單，
   **When** 查詢，
   **Then** `overdueRate: 20.0`（百分比）

---

### User Story 3 - 單店家詳細績效查詢 (Priority: P2)

Admin 或 StoreOwner 可查詢單一店家的詳細績效，包含趨勢圖資料。

**Why this priority**: 詳細視圖讓 Admin 輔導時有具體數據支撐。

**Independent Test**: 帶入 `storeId` 查詢，回傳單店的詳細月度/日度趨勢。

**Acceptance Scenarios**:

1. **Given** Admin 查詢特定 storeId，
   **When** 呼叫 API，
   **Then** 回傳該店的 `dailyStats` 陣列（每天的 drawCount、revenue、newUsers）

2. **Given** StoreOwner 查詢，
   **When** 呼叫 API，
   **Then** 只能查詢自己店家，查詢其他店家回傳 403

---

## Functional Requirements

- **FR-001**: 時間範圍篩選（startDate/endDate），預設過去 30 天
- **FR-002**: `totalRevenue` = 該店 `wallet_transaction.type = 'DRAW_DEDUCTION'` 加總（透過 lottery_ticket 關聯）
- **FR-003**: `drawCount` = 該店 `lottery_ticket.status = 'DRAWN'` 筆數
- **FR-004**: `activeUsers` = 期間內有任一行為（抽獎/登入/儲值/訂單）的不重複用戶數
- **FR-005**: `shipRate` = (SHIPPED + COMPLETED 訂單數) / 非 CANCELLED 訂單數 × 100
- **FR-006**: `overdueRate` = PENDING 超過 7 天的訂單數 / 全部訂單數 × 100
- **FR-007**: `avgShipDays` = 平均 `preparing_at → shipped_at` 天數（依賴 029 的 DDL 變更）
- **FR-008**: 預設依 `totalRevenue` 降序排列；支援前端指定排序欄位
- **FR-009**: Admin 查全部店家；StoreOwner 只能查自己的店（帶 storeId）
- **FR-010**: `dailyStats` 只在帶入 storeId 的情況下回傳

---

## Data Sources

| 欄位 | 來源 |
|------|------|
| totalRevenue | wallet_transaction JOIN lottery_ticket → lottery.store_id |
| drawCount | lottery_ticket JOIN lottery.store_id |
| activeUsers | lottery_ticket / order / user.last_login_at (UNION) |
| shipRate | order WHERE store_id |
| overdueRate | order WHERE status=PENDING AND created_at < NOW()-7days |
| avgShipDays | order WHERE preparing_at IS NOT NULL AND shipped_at IS NOT NULL |

---

## API 設計（草案）

```
POST /admin/report/store-performance
Authorization: Bearer {adminToken}

Request:
{
  "startDate": "2026-04-01",
  "endDate": "2026-04-30",
  "storeId": null,        // null = 全部店家
  "sortBy": "totalRevenue",
  "sortOrder": "DESC"
}

Response:
{
  "stores": [
    {
      "storeId": "xxx",
      "storeName": "A店",
      "totalRevenue": 15000,
      "drawCount": 300,
      "activeUsers": 85,
      "shipRate": 92.0,
      "overdueRate": 3.3,
      "avgShipDays": 2.1
    }
  ],
  "dailyStats": null  // 只在帶 storeId 時有資料
}
```

---

## Non-Functional Requirements

- **NFR-001**: 查詢時間 < 5 秒（跨所有店家聚合）
- **NFR-002**: Admin 可查全部；StoreOwner 僅能查自己的店
- **NFR-003**: 依賴 029 的 `preparing_at` DDL 變更（用於 avgShipDays）

---

## 依賴關係

- **依賴 029**: `order.preparing_at` 欄位（avgShipDays 計算）
- **029 實作後才能正確計算**: `avgShipDays`；若 029 未先實作，此欄位回傳 `null`

---

## Success Criteria

- **SC-001**: `shipRate` + `overdueRate` 計算涵蓋所有非 CANCELLED 訂單
- **SC-002**: StoreOwner 查詢其他店家時回傳 403
- **SC-003**: `stores` 陣列的 `totalRevenue` 加總等於 033 報表的 `totalSpend`（資料一致性）

---

## 澄清紀錄

| 問題 | 決定 |
|------|------|
| StoreOwner 可否查看此報表？ | 可查詢自己的店（帶 storeId），Admin 才能查全部 |
| activeUsers 定義？ | 有任一行為：抽獎 OR 登入 OR 儲值 OR 建立訂單 |
| 排序預設？ | totalRevenue 降序 |