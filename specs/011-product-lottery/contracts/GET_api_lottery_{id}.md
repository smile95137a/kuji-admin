# Contract: GET /api/lottery/{id} (Product Detail)

**Feature**: `011-product-lottery`  
**Method**: `GET`  
**Path**: `/api/lottery/{id}`  
**Auth**: Optional — JWT Bearer Token  
**Description**: Returns full product detail including prize pool information, remaining counts, and recent draw history. Publicly accessible for products in `ON_SHELF`, `DRAWABLE`, `IN_PROGRESS` or `SOLD_OUT` status.

---

## Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | String (UUID) | The lottery product ID |

---

## Response

### 200 OK

```json
{
  "success": true,
  "data": {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "storeId": "store-uuid",
    "storeName": "夢幻抽抽樂",
    "storeLogoUrl": "https://cdn.example.com/store/logo.jpg",
    "title": "2026 鬼滅之刃 官方一番賞",
    "category": "OFFICIAL_ICHIBAN",
    "subCategory": null,
    "description": "全新官方正版一番賞，共50個獎品",
    "content": "抽獎規則說明...",
    "imageUrl": "https://cdn.example.com/lottery/cover.jpg",
    "galleryImages": [
      "https://cdn.example.com/lottery/img1.jpg",
      "https://cdn.example.com/lottery/img2.jpg"
    ],
    "pricePerDraw": 150,
    "discountedPrice": 100,
    "currentPrice": 150,
    "autoDiscountEnabled": true,
    "discountTriggerLevel": "A,B",
    "discountTriggered": false,
    "allowMultiDraw": true,
    "multiDrawOptions": [10, 50],
    "status": "IN_PROGRESS",
    "totalDraws": 50,
    "remainingDraws": 32,
    "scheduledAt": null,
    "startTime": "2026-04-01T12:00:00",
    "protectionDraws": 10,
    "protectionMinutes": 5,
    "lastPrizeMode": "LAST_DRAW",
    "hasLastPrize": true,
    "hotCount": 18,
    "tags": "鬼滅,一番賞,限定",
    "theme": "dark",
    "isProtected": false,
    "protectionExpiresAt": null,
    "prizes": [
      {
        "id": "prize-uuid-1",
        "name": "A賞 炭治郎 原型",
        "level": "A",
        "imageUrl": "https://cdn.example.com/prize/a.jpg",
        "quantity": 1,
        "remaining": 0,
        "isLastPrize": false,
        "isGrandPrize": false,
        "orderNum": 1,
        "description": "高精度手辦",
        "recycleBonus": 200
      },
      {
        "id": "prize-uuid-last",
        "name": "LAST 最後賞 特製手提袋",
        "level": "LAST",
        "imageUrl": "https://cdn.example.com/prize/last.jpg",
        "quantity": 1,
        "remaining": 1,
        "isLastPrize": true,
        "isGrandPrize": false,
        "orderNum": 99,
        "description": "最終一抽特別獎",
        "recycleBonus": 0
      }
    ],
    "recentDraws": [
      {
        "userId": null,
        "userNickname": "玩家***123",
        "prizeName": "C賞 週邊組合",
        "prizeLevel": "C",
        "drawnAt": "2026-04-01T14:32:00"
      }
    ],
    "createdAt": "2026-03-22T09:00:00"
  }
}
```

### Response Fields

| Field | Type | Description |
|-------|------|-------------|
| `currentPrice` | Long | Effective price: `discountedPrice` if `discountTriggered=true`, else `pricePerDraw` |
| `discountTriggered` | Boolean | Whether the auto-discount condition is currently met |
| `isProtected` | Boolean | Whether a protection round is currently active |
| `protectionExpiresAt` | ISO 8601/null | When the protection lock expires (null if not locked) |
| `prizes[].remaining` | Int | Current remaining count for this prize (real-time) |
| `prizes[].recycleBonus` | Long | Recycle bonus amount; 0 = not recyclable |
| `recentDraws` | Array | Last 10 draw events (anonymized) |
| `recentDraws[].userId` | null | Always null (anonymized) |
| `recentDraws[].userNickname` | String | Partially masked nickname |

---

## Error Responses

| HTTP | Error Code | Condition |
|------|-----------|-----------|
| `404` | `LOTTERY_NOT_FOUND` | No lottery with given `id` |
| `403` | `LOTTERY_NOT_PUBLIC` | Lottery exists but is in DRAFT / CONFIGURED / FORCED_OFF status |

---

## Business Rules

1. Products in `DRAFT`, `CONFIGURED`, or `FORCED_OFF` status return `403 LOTTERY_NOT_PUBLIC`.
2. Products in `SOLD_OUT` status are returned with all prizes showing `remaining: 0`.
3. `recentDraws` contains the last 10 draw records ordered by `created_at` DESC. User identifiers are masked: only partial nickname is shown; `userId` is always null.
4. `isProtected` = `true` if an unexpired `lottery_lock` row exists. `protectionExpiresAt` shows the lock's `lockEndTime`.
5. `discountTriggered` = `true` when all prizes with levels matching `discountTriggerLevel` have `remaining = 0`.
6. Prize list is ordered by `orderNum` ASC. The last-prize entry is always present regardless of its `isLastPrize` flag (for display purposes).
7. `multiDrawOptions` is returned as a JSON array (deserialized from VARCHAR), e.g. `[10, 50]`.
