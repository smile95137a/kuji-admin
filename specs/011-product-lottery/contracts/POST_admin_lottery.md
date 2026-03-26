# Contract: POST /admin/lottery

**Feature**: `011-product-lottery`  
**Method**: `POST`  
**Path**: `/admin/lottery`  
**Auth**: Required — JWT Bearer Token (Admin or Store Owner role)  
**Description**: Creates a new lottery product in `DRAFT` status for the caller's store.

---

## Request

### Headers

| Header | Value |
|--------|-------|
| `Authorization` | `Bearer <jwt>` |
| `Content-Type` | `application/json` |

### Body

```json
{
  "title": "2026 鬼滅之刃 官方一番賞",
  "category": "OFFICIAL_ICHIBAN",
  "subCategory": null,
  "description": "全新官方正版一番賞，共50個獎品",
  "content": "抽獎規則說明...",
  "imageUrl": "https://cdn.example.com/lottery/cover.jpg",
  "galleryImages": ["https://cdn.example.com/lottery/img1.jpg"],
  "pricePerDraw": 150,
  "discountedPrice": null,
  "autoDiscountEnabled": false,
  "discountTriggerLevel": null,
  "allowMultiDraw": false,
  "multiDrawOptions": null,
  "scheduledAt": "2026-04-01T10:00:00",
  "startTime": "2026-04-01T12:00:00",
  "protectionDraws": 10,
  "protectionMinutes": 5,
  "lastPrizeMode": "LAST_DRAW",
  "bonusEnabled": false,
  "bonusPointsPerDraw": null,
  "bonusCostPerDraw": null,
  "orderNum": 0,
  "tags": "鬼滅,一番賞,限定",
  "theme": "dark",
  "prizes": [
    {
      "name": "A賞 炭治郎 原型",
      "level": "A",
      "imageUrl": "https://cdn.example.com/prize/a.jpg",
      "quantity": 1,
      "prizeType": "PHYSICAL",
      "recycleBonus": 200,
      "isLastPrize": false,
      "isGrandPrize": false,
      "orderNum": 1,
      "description": "高精度手辦"
    },
    {
      "name": "LAST 最後賞 特製手提袋",
      "level": "LAST",
      "imageUrl": "https://cdn.example.com/prize/last.jpg",
      "quantity": 1,
      "prizeType": "PHYSICAL",
      "recycleBonus": 0,
      "isLastPrize": true,
      "isGrandPrize": false,
      "orderNum": 99,
      "description": "最終一抽特別獎"
    }
  ]
}
```

### Field Constraints

| Field | Required | Type | Rules |
|-------|----------|------|-------|
| `title` | ✅ | String | 1–200 chars |
| `category` | ✅ | Enum | `OFFICIAL_ICHIBAN`, `GASHAPON`, `TRADING_CARD`, `CUSTOM_GASHAPON` |
| `subCategory` | ❌ | Enum | Required if category=`CUSTOM_GASHAPON`: `LOTTERY_MODE` or `SCRATCH_MODE` |
| `pricePerDraw` | ✅ | Long | > 0 |
| `discountedPrice` | ❌ | Long | Required if `autoDiscountEnabled=true`; must be < `pricePerDraw` |
| `discountTriggerLevel` | ❌ | String | Required if `autoDiscountEnabled=true`; comma-sep levels e.g. "A,B" |
| `protectionDraws` | ❌ | Int | ≥ 0; default 0 |
| `protectionMinutes` | ❌ | Int | ≥ 0; default 5 |
| `lastPrizeMode` | ❌ | Enum | `LAST_DRAW` or `POOL_IN`; required if any prize has `isLastPrize=true` |
| `prizes` | ❌ | Array | May be empty on DRAFT creation; validated at CONFIGURED transition |
| `prizes[].quantity` | ✅ (if prizes) | Int | ≥ 1 |
| `prizes[].recycleBonus` | ✅ (if prizes) | Long | ≥ 0 |
| `prizes[].isLastPrize` | ✅ (if prizes) | Boolean | At most one prize per lottery |

---

## Response

### 201 Created

```json
{
  "success": true,
  "message": "商品已建立",
  "data": {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "status": "DRAFT",
    "title": "2026 鬼滅之刃 官方一番賞",
    "category": "OFFICIAL_ICHIBAN",
    "pricePerDraw": 150,
    "totalDraws": null,
    "remainingDraws": null,
    "createdAt": "2026-03-22T09:00:00"
  }
}
```

### Error Responses

| HTTP | Error Code | Condition |
|------|-----------|-----------|
| `400` | `INVALID_CATEGORY` | Unknown category value |
| `400` | `INVALID_PRICE` | `pricePerDraw` ≤ 0 |
| `400` | `SUBCATEGORY_REQUIRED` | `category=CUSTOM_GASHAPON` but no `subCategory` |
| `400` | `DISCOUNT_CONFIG_INCOMPLETE` | `autoDiscountEnabled=true` but missing `discountedPrice` or `discountTriggerLevel` |
| `400` | `MULTIPLE_LAST_PRIZE` | More than one prize with `isLastPrize=true` |
| `401` | `UNAUTHORIZED` | No valid JWT |
| `403` | `FORBIDDEN` | User has no store |

---

## Business Rules

1. The lottery is always created in `DRAFT` status regardless of `scheduledAt`.
2. If `prizes` is provided in the request, they are saved immediately; the lottery transitions to `CONFIGURED` only when explicitly triggered via `PUT /admin/lottery/{id}/status`.
3. `storeId` is auto-resolved from the JWT — callers cannot specify a different store.
4. `createdBy` is set from the JWT subject.
5. `totalDraws` and `remainingDraws` are computed and set when transitioning to `CONFIGURED`.
