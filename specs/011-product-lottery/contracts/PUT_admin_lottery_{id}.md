# Contract: PUT /admin/lottery/{id}

**Feature**: `011-product-lottery`  
**Method**: `PUT`  
**Path**: `/admin/lottery/{id}`  
**Auth**: Required — JWT Bearer Token (Admin or owning Store Owner)  
**Description**: Updates an existing lottery product. Some fields are locked once the product is `IN_PROGRESS`.

---

## Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | String (UUID) | The lottery product ID |

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
  "title": "2026 鬼滅之刃 官方一番賞 (更新版)",
  "description": "更新後的說明",
  "imageUrl": "https://cdn.example.com/lottery/cover-v2.jpg",
  "galleryImages": ["https://cdn.example.com/lottery/img1.jpg"],
  "pricePerDraw": 150,
  "discountedPrice": 100,
  "autoDiscountEnabled": true,
  "discountTriggerLevel": "A,B",
  "allowMultiDraw": true,
  "multiDrawOptions": "[10, 50]",
  "scheduledAt": "2026-04-01T10:00:00",
  "startTime": "2026-04-01T12:00:00",
  "endTime": null,
  "protectionDraws": 10,
  "protectionMinutes": 5,
  "lastPrizeMode": "LAST_DRAW",
  "bonusEnabled": false,
  "orderNum": 1,
  "tags": "鬼滅,一番賞",
  "theme": "dark",
  "remark": "管理員備注"
}
```

### Field Lock Rules by Status

| Field | DRAFT | CONFIGURED | ON_SHELF | DRAWABLE | IN_PROGRESS | SOLD_OUT | FORCED_OFF |
|-------|-------|-----------|----------|---------|------------|---------|-----------|
| `title` | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ |
| `description` | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ |
| `imageUrl` | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ |
| `galleryImages` | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ |
| `tags` / `theme` | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ |
| `pricePerDraw` | ✅ | ✅ | ✅ | ✅ | 🔒 LOCKED | ❌ | ❌ |
| `discountedPrice` | ✅ | ✅ | ✅ | ✅ | 🔒 LOCKED | ❌ | ❌ |
| `autoDiscountEnabled` | ✅ | ✅ | ✅ | ✅ | 🔒 LOCKED | ❌ | ❌ |
| `discountTriggerLevel` | ✅ | ✅ | ✅ | ✅ | 🔒 LOCKED | ❌ | ❌ |
| `allowMultiDraw` | ✅ | ✅ | ✅ | ✅ | 🔒 LOCKED | ❌ | ❌ |
| `multiDrawOptions` | ✅ | ✅ | ✅ | ✅ | 🔒 LOCKED | ❌ | ❌ |
| `scheduledAt` | ✅ | ✅ | 🔒 LOCKED | 🔒 LOCKED | 🔒 LOCKED | ❌ | ❌ |
| `startTime` | ✅ | ✅ | ✅ | 🔒 LOCKED | 🔒 LOCKED | ❌ | ❌ |
| `protectionDraws/Minutes` | ✅ | ✅ | ✅ | ✅ | 🔒 LOCKED | ❌ | ❌ |
| `lastPrizeMode` | ✅ | ✅ | ✅ | 🔒 LOCKED | 🔒 LOCKED | ❌ | ❌ |
| Prize pool (via separate endpoints) | ✅ | ✅ | ✅ | 🔒 LOCKED | 🔒 LOCKED | ❌ | ❌ |
| `remark` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |

> Note: Prize pool modification is done via prize-level endpoints, not this endpoint.

---

## Response

### 200 OK

```json
{
  "success": true,
  "message": "商品已更新",
  "data": {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "status": "ON_SHELF",
    "title": "2026 鬼滅之刃 官方一番賞 (更新版)",
    "updatedAt": "2026-03-22T10:00:00"
  }
}
```

### Error Responses

| HTTP | Error Code | Condition |
|------|-----------|-----------|
| `400` | `FIELD_LOCKED` | Attempting to change a locked field for current status |
| `400` | `INVALID_PRICE` | `pricePerDraw` ≤ 0 |
| `400` | `DISCOUNT_CONFIG_INCOMPLETE` | `autoDiscountEnabled=true` but missing `discountedPrice` or `discountTriggerLevel` |
| `401` | `UNAUTHORIZED` | No valid JWT |
| `403` | `FORBIDDEN` | User does not own this lottery's store |
| `404` | `LOTTERY_NOT_FOUND` | No lottery with given `id` |

---

## Business Rules

1. The server validates which fields are mutable based on the current `status` before applying any changes.
2. If any locked field is present in the request body and the value differs from the current value, the server returns `400 FIELD_LOCKED`.
3. `updatedAt` is always refreshed.
4. Changing `scheduledAt` while in `CONFIGURED` status reschedules the automatic ON_SHELF transition.
