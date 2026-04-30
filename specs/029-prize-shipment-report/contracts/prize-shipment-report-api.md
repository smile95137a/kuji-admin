# API Contract: 獎品出貨報表

**Endpoint**: `POST /admin/report/prize-shipment`  
**Auth**: Bearer JWT — roles `ADMIN` or `STORE_OWNER`  
**Feature**: 029-prize-shipment-report

---

## Request

### Headers

```
Authorization: Bearer <JWT token>
Content-Type: application/json
```

### Body: `QueryReq<PrizeShipmentReportCondition>`

```json
{
  "condition": {
    "storeId": "uuid-or-null",
    "startDate": "2026-04-01",
    "endDate":   "2026-04-30"
  }
}
```

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `condition.storeId` | `String` | No | StoreOwner：後端強制覆蓋為 JWT 中的 storeId；Admin：可傳 null 查全平台 |
| `condition.startDate` | `LocalDate` (`yyyy-MM-dd`) | No | 預設 = 今日 -29 天 |
| `condition.endDate` | `LocalDate` (`yyyy-MM-dd`) | No | 預設 = 今日 |

> **Note**: `condition` 整個物件可為 null（等同不帶條件，使用全部預設值）。

---

## Response: `PrizeShipmentReportRes`

### HTTP 200 OK

```json
{
  "startDate": "2026-04-01",
  "endDate":   "2026-04-30",

  "pendingCount":   5,
  "preparingCount": 3,
  "shippedCount":   8,
  "completedCount": 10,

  "avgShipDays":  4.0,
  "overdueCount": 2,

  "dailyDetails": [
    { "date": "2026-04-10", "shippedCount": 3 },
    { "date": "2026-04-15", "shippedCount": 5 }
  ],

  "storeDetails": null
}
```

> `storeDetails` 只有 **Admin** 查詢（`storeId == null`）時才有值；StoreOwner 回傳 `null`。

### storeDetails（Admin 限定）

```json
{
  "storeDetails": [
    {
      "storeId":        "store-uuid-A",
      "storeName":      "A 店",
      "pendingCount":   2,
      "preparingCount": 1,
      "shippedCount":   4,
      "completedCount": 8,
      "avgShipDays":    3.5,
      "overdueCount":   0
    },
    {
      "storeId":        "store-uuid-B",
      "storeName":      "B 店",
      "pendingCount":   3,
      "preparingCount": 2,
      "shippedCount":   4,
      "completedCount": 2,
      "avgShipDays":    12.0,
      "overdueCount":   2
    }
  ]
}
```

### Edge Cases

| Scenario | Response |
|----------|----------|
| 查詢期間無任何訂單 | 計數全 0，`dailyDetails: []`，`avgShipDays: null` |
| 所有已出貨訂單 `preparing_at` 均為 null | `avgShipDays: null` |
| StoreOwner 嘗試傳入他人 storeId | 後端自動覆蓋，只回傳自己店家資料 |

---

## Error Responses

| HTTP Status | Condition |
|-------------|-----------|
| `401 Unauthorized` | 未攜帶 JWT 或 token 過期 |
| `403 Forbidden` | 角色不含 `ADMIN` 或 `STORE_OWNER` |
| `500 Internal Server Error` | DB 查詢失敗 |

---

## Security Notes

- `@PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER')")` 強制角色驗證
- StoreOwner 的 `storeId` 由後端從 JWT 取得（`SecurityUtils.getCurrentUserPrimaryStoreId()`），前端傳入值**無效**
- Admin 的 `storeId` 為 null 時才觸發 `storeDetails` 計算，不會洩漏其他店家的詳細資料給 StoreOwner

---

## cURL Example

### StoreOwner 查自己店家（最近 30 天預設）
```bash
curl -X POST http://localhost:8080/admin/report/prize-shipment \
  -H "Authorization: Bearer <STORE_OWNER_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{}'
```

### Admin 查詢特定時間範圍（全平台）
```bash
curl -X POST http://localhost:8080/admin/report/prize-shipment \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "condition": {
      "startDate": "2026-04-01",
      "endDate": "2026-04-30"
    }
  }'
```

### Admin 查詢特定店家
```bash
curl -X POST http://localhost:8080/admin/report/prize-shipment \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "condition": {
      "storeId": "store-uuid-A",
      "startDate": "2026-04-01",
      "endDate": "2026-04-30"
    }
  }'
```
