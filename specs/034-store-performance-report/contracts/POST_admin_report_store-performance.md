# API Contract: POST /admin/report/store-performance

**Feature**: 034 - 店家績效比較報表  
**Method**: `POST`  
**Path**: `/admin/report/store-performance`  
**Auth**: Bearer Token (JWT)  
**Roles**: `ADMIN`, `STORE_OWNER`

---

## Request

### Headers

| Header | Value |
|--------|-------|
| Authorization | `Bearer {token}` |
| Content-Type | `application/json` |

### Body — `QueryReq<StorePerformanceCondition>`

```json
{
  "condition": {
    "storeId": "uuid-or-null",
    "startDate": "2026-04-01",
    "endDate": "2026-04-30"
  },
  "sortBy": "totalRevenue",
  "sortOrder": "DESC"
}
```

| 欄位 | 型別 | 必填 | 說明 |
|------|------|------|------|
| `condition.storeId` | String | No | Admin 可留 null 查全部；StoreOwner 後端自動覆蓋為自己的 storeId |
| `condition.startDate` | String (yyyy-MM-dd) | No | 查詢起始日期；未傳時預設今天往前 30 天 |
| `condition.endDate` | String (yyyy-MM-dd) | No | 查詢結束日期；未傳時預設今天 |
| `sortBy` | String | No | 排序欄位，白名單：`totalRevenue`、`drawCount`、`activeUsers`、`shipRate`、`overdueRate`、`avgShipDays`；預設 `totalRevenue` |
| `sortOrder` | String | No | `ASC` 或 `DESC`；預設 `DESC` |

---

## Response

### 200 OK

```json
{
  "startDate": "2026-04-01",
  "endDate": "2026-04-30",
  "stores": [
    {
      "storeId": "store-uuid-a",
      "storeName": "A店",
      "totalRevenue": 15000,
      "drawCount": 300,
      "activeUsers": 85,
      "shipRate": 92.0,
      "overdueRate": 3.3,
      "avgShipDays": null
    },
    {
      "storeId": "store-uuid-b",
      "storeName": "B店",
      "totalRevenue": 8200,
      "drawCount": 180,
      "activeUsers": 42,
      "shipRate": 75.0,
      "overdueRate": 20.0,
      "avgShipDays": null
    }
  ],
  "dailyStats": null
}
```

### 200 OK — 帶 storeId（含 dailyStats）

```json
{
  "startDate": "2026-04-01",
  "endDate": "2026-04-30",
  "stores": [
    {
      "storeId": "store-uuid-a",
      "storeName": "A店",
      "totalRevenue": 15000,
      "drawCount": 300,
      "activeUsers": 85,
      "shipRate": 92.0,
      "overdueRate": 3.3,
      "avgShipDays": null
    }
  ],
  "dailyStats": [
    {
      "date": "2026-04-01",
      "drawCount": 12,
      "revenue": 600,
      "newUsers": 3
    },
    {
      "date": "2026-04-02",
      "drawCount": 8,
      "revenue": 400,
      "newUsers": 1
    }
  ]
}
```

### Response Fields

| 欄位 | 型別 | 說明 |
|------|------|------|
| `startDate` | String | 報表期間起 |
| `endDate` | String | 報表期間迄 |
| `stores[]` | Array | 各店家績效清單 |
| `stores[].storeId` | String | 店家 UUID |
| `stores[].storeName` | String | 店家名稱 |
| `stores[].totalRevenue` | Long | 抽獎消費總點數（ABS of wallet_transaction DRAW） |
| `stores[].drawCount` | Integer | 抽籤總數（lottery_ticket status=DRAWN） |
| `stores[].activeUsers` | Integer | 不重複活躍用戶數 |
| `stores[].shipRate` | Double (nullable) | 出貨率 %；分母為 0 時 null |
| `stores[].overdueRate` | Double (nullable) | 逾期率 %；無訂單時 null |
| `stores[].avgShipDays` | Double (nullable) | 平均出貨天數；**029 未合併前恆為 null** |
| `dailyStats[]` | Array (nullable) | 單店每日趨勢；未帶 storeId 時為 null |
| `dailyStats[].date` | String | 日期 |
| `dailyStats[].drawCount` | Integer | 當日抽籤數 |
| `dailyStats[].revenue` | Long | 當日抽獎消費點數 |
| `dailyStats[].newUsers` | Integer | 當日新用戶數（首次在本店有活動） |

---

## Error Responses

| HTTP Status | 情境 |
|-------------|------|
| 401 Unauthorized | Token 無效或過期 |
| 403 Forbidden | StoreOwner 嘗試查詢其他店家的 storeId |
| 400 Bad Request | startDate > endDate |

---

## Access Control

| Role | 行為 |
|------|------|
| `ADMIN` | 可查全部店家（condition.storeId = null → 回傳所有店） |
| `ADMIN` + storeId | 查詢指定單一店家（含 dailyStats） |
| `STORE_OWNER` | 後端強制覆蓋 storeId 為自己的店；嘗試查其他店回傳 403 |

---

## Business Rules

1. **FR-001**: startDate/endDate 未傳時，預設過去 30 天。
2. **FR-002**: `totalRevenue` = `ABS(SUM(wallet_transaction.amount))` WHERE `transaction_type = 'DRAW'`，透過 `lottery_ticket → lottery.store_id` JOIN。
3. **FR-003**: `drawCount` = `lottery_ticket WHERE status = 'DRAWN'`，透過 `lottery.store_id`。
4. **FR-004**: `activeUsers` = 抽獎（lottery_ticket.drawn_by）UNION 訂單（order.user_id）的不重複用戶數，按 `store_id` 分組。
5. **FR-005**: `shipRate` = `(SHIPPED + COMPLETED) / 非CANCELLED × 100`；分母為 0 → null。
6. **FR-006**: `overdueRate` = `PENDING 超 7 天 / 全部訂單 × 100`；全部為 0 → null。
7. **FR-007**: `avgShipDays` = 029 依賴（`preparing_at → shipped_at`）；029 未就緒前恆為 null。
8. **FR-008**: 預設依 `totalRevenue DESC` 排列；`sortBy` 非白名單值 fallback 為 `totalRevenue`。
9. **FR-009**: Admin 查全部；StoreOwner 強制自己店。
10. **FR-010**: `dailyStats` 僅 storeId 非 null 時回傳，否則為 null。
