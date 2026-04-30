# API Contract: POST /admin/report/lottery-sales

**Feature**: 031 - 商品銷售排行報表  
**Auth**: JWT Bearer Token (後台)  
**Roles**: `ADMIN`, `STORE_OWNER`

---

## Endpoint

```
POST /admin/report/lottery-sales
```

---

## Request

### Headers

```
Authorization: Bearer <jwt-token>
Content-Type: application/json
```

### Body

```json
{
  "condition": {
    "storeId": "uuid-string-optional",
    "limit": 20
  },
  "sortBy": "drawCount",
  "sortOrder": "DESC"
}
```

### Request Fields

| 欄位 | 類型 | 必填 | 預設值 | 說明 |
|------|------|------|--------|------|
| `condition` | Object | No | `{}` | 查詢條件 |
| `condition.storeId` | String | No | `null` | 店家 ID（StoreOwner 後端強制帶入，Admin 可選填） |
| `condition.limit` | Integer | No | `20` | 回傳筆數上限（最大 100） |
| `sortBy` | String | No | `"drawCount"` | 排序欄位：`drawCount` \| `revenue` |
| `sortOrder` | String | No | `"DESC"` | 排序方向（目前僅支援 DESC） |

**⚠️ StoreOwner 注意事項**：  
即使傳入 `condition.storeId`，後端會強制以 JWT Token 中的 storeId 覆蓋。`storeId` 欄位對 StoreOwner 無效。

---

## Response

### 200 OK

```json
{
  "totalRecords": 15,
  "items": [
    {
      "lotteryId": "550e8400-e29b-41d4-a716-446655440000",
      "lotteryTitle": "鬼滅之刃 一番賞 Vol.1",
      "storeName": "動漫星球",
      "drawCount": 320,
      "revenue": 25600,
      "rank": 1
    },
    {
      "lotteryId": "660f9511-f30c-52e5-b827-557766551111",
      "lotteryTitle": "進擊的巨人 特別版",
      "storeName": "動漫星球",
      "drawCount": 180,
      "revenue": 18000,
      "rank": 2
    }
  ]
}
```

### Response Fields

| 欄位 | 類型 | 說明 |
|------|------|------|
| `totalRecords` | Integer | 符合條件的商品總數（不受 limit 影響） |
| `items` | Array | 排行榜清單，依 sortBy 降序排列 |
| `items[].lotteryId` | String | 商品 UUID |
| `items[].lotteryTitle` | String | 商品標題 |
| `items[].storeName` | String | 所屬店家名稱 |
| `items[].drawCount` | Integer | 全生命期已抽籤數（`lottery_ticket.status=DRAWN`） |
| `items[].revenue` | Long | 全生命期有效營收（金幣點數，排除 CANCELLED 訂單） |
| `items[].rank` | Integer | 排名（1-based，由 Service 計算） |

---

## Business Rules

### FR-001 ~ FR-008 覆蓋說明

| FR | 規則 | 實作方式 |
|----|------|---------|
| FR-001 | 提供 `POST /admin/report/lottery-sales` API，支援 storeId 過濾 | Controller 新增端點 |
| FR-002 | 回傳 `lotteryId`, `lotteryTitle`, `drawCount`, `revenue`, `storeName` | Response DTO |
| FR-003 | 依 `drawCount` 降序排列（預設），支援 `revenue` | `sortBy` 白名單驗證 |
| FR-004 | `drawCount` 來自 `lottery_ticket` WHERE `status = DRAWN` | 子查詢 LEFT JOIN |
| FR-005 | `revenue` 透過 `order_item.lottery_id` JOIN `order`，排除 CANCELLED | 子查詢 LEFT JOIN |
| FR-006 | 預設回傳前 20，支援 `limit` 調整（最大 100） | `Math.min(limit, 100)` |
| FR-007 | StoreOwner 只看自己店家商品 | Controller 強制覆蓋 storeId |
| FR-008 | Admin 支援 storeId 過濾或全平台查詢 | storeId 為 null 時不加 WHERE |

---

## Revenue 計算邏輯

```
revenue per lottery = COUNT(order_items linked to non-CANCELLED orders) × price_per_draw
```

**範例驗證（來自 Spec）**：
- 商品 A：100 次有效抽籤，price_per_draw = 80 → revenue = `8000` ✅
- 商品 B：50 次抽籤，10 次對應 CANCELLED 訂單 → 只算 40 次 → revenue = `40 × price_per_draw` ✅

---

## Security

| 角色 | 行為 |
|------|------|
| `ADMIN` | `storeId=null` 查全平台；可帶 `storeId` 過濾特定店家 |
| `STORE_OWNER` | 後端強制使用 JWT 中的 storeId，只能查自己店家 |
| 未認證 | `401 Unauthorized` |
| 其他角色 | `403 Forbidden` |

---

## Error Responses

| Status | 情境 |
|--------|------|
| `401 Unauthorized` | 未帶 JWT Token 或 Token 已過期 |
| `403 Forbidden` | 角色不符（非 ADMIN / STORE_OWNER） |
| `500 Internal Server Error` | DB 查詢失敗 |

---

## Examples

### Example 1 — StoreOwner 查自己的排行（依 drawCount 降序）

**Request:**
```json
{
  "condition": {},
  "sortBy": "drawCount"
}
```

**Response**: 只回傳當前 StoreOwner 所屬店家的商品排行（後端強制 storeId）

---

### Example 2 — Admin 查全平台，依 revenue 排序

**Request:**
```json
{
  "condition": {
    "limit": 10
  },
  "sortBy": "revenue"
}
```

**Response**: 全平台前 10 名商品，依 revenue 降序

---

### Example 3 — Admin 查特定店家，limit 50

**Request:**
```json
{
  "condition": {
    "storeId": "store-uuid-here",
    "limit": 50
  },
  "sortBy": "drawCount"
}
```

**Response**: 特定店家前 50 名商品

---

## Notes

- `revenue` 單位為**金幣點數**（整數），非貨幣金額
- 全生命期統計，無日期範圍過濾
- `drawCount = 0` 且 `revenue = 0` 的商品（從未被抽過）仍出現在清單中
- `limit > 100` 的請求自動截斷至 100
