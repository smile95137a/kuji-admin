# Contract: GET /api/lottery (Public List)

**Feature**: `011-product-lottery`  
**Method**: `GET`  
**Path**: `/api/lottery`  
**Auth**: Optional — JWT Bearer Token (provides personalization if present)  
**Description**: Returns a paginated list of publicly visible lottery products. Only products in `ON_SHELF`, `DRAWABLE`, `IN_PROGRESS`, or `SOLD_OUT` (recent) status are returned.

---

## Query Parameters

| Parameter | Required | Type | Default | Description |
|-----------|----------|------|---------|-------------|
| `page` | ❌ | Int | `1` | Page number (1-based) |
| `pageSize` | ❌ | Int | `20` | Items per page (max 50) |
| `category` | ❌ | Enum | — | Filter by category: `OFFICIAL_ICHIBAN`, `GASHAPON`, `TRADING_CARD`, `CUSTOM_GASHAPON` |
| `storeId` | ❌ | UUID | — | Filter by store |
| `status` | ❌ | Enum | `ON_SHELF,DRAWABLE,IN_PROGRESS` | Comma-separated status filter |
| `keyword` | ❌ | String | — | Full-text search on `title` and `tags` |
| `sort` | ❌ | Enum | `HOT` | Sort: `HOT` (hotCount desc), `NEW` (createdAt desc), `PRICE_ASC`, `PRICE_DESC` |

---

## Response

### 200 OK

```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
        "storeId": "store-uuid",
        "storeName": "夢幻抽抽樂",
        "title": "2026 鬼滅之刃 官方一番賞",
        "category": "OFFICIAL_ICHIBAN",
        "subCategory": null,
        "imageUrl": "https://cdn.example.com/lottery/cover.jpg",
        "pricePerDraw": 150,
        "currentPrice": 150,
        "status": "IN_PROGRESS",
        "totalDraws": 50,
        "remainingDraws": 32,
        "hotCount": 18,
        "tags": "鬼滅,一番賞,限定",
        "scheduledAt": null,
        "startTime": "2026-04-01T12:00:00",
        "isProtected": false,
        "hasLastPrize": true,
        "lastPrizeMode": "LAST_DRAW"
      }
    ],
    "total": 128,
    "pageNum": 1,
    "pageSize": 20,
    "pages": 7
  }
}
```

### Response Fields

| Field | Type | Description |
|-------|------|-------------|
| `id` | String | Lottery UUID |
| `storeId` | String | Store UUID |
| `storeName` | String | Store display name |
| `title` | String | Product title |
| `category` | Enum | Product category |
| `subCategory` | Enum/null | Sub-category (for CUSTOM_GASHAPON) |
| `imageUrl` | String/null | Cover image URL |
| `pricePerDraw` | Long | Base price per draw |
| `currentPrice` | Long | Effective price (may differ if discount active) |
| `status` | Enum | Current lifecycle status |
| `totalDraws` | Int | Total tickets in pool |
| `remainingDraws` | Int | Tickets still available |
| `hotCount` | Int | Draw count for popularity ranking |
| `isProtected` | Boolean | Whether a protection round is currently active |
| `hasLastPrize` | Boolean | Whether this lottery has a 最後賞 |
| `lastPrizeMode` | Enum/null | `LAST_DRAW` or `POOL_IN` |

---

## Error Responses

| HTTP | Error Code | Condition |
|------|-----------|-----------|
| `400` | `INVALID_CATEGORY` | Unknown category filter value |
| `400` | `INVALID_PAGE_SIZE` | `pageSize` > 50 |

---

## Business Rules

1. Only products in statuses: `ON_SHELF`, `DRAWABLE`, `IN_PROGRESS` are returned by default. `SOLD_OUT` items are excluded unless explicitly requested via `status=SOLD_OUT`.
2. `DRAFT`, `CONFIGURED`, `FORCED_OFF` products are **never** returned by this endpoint.
3. `currentPrice` equals `discountedPrice` if the discount trigger condition has been met; otherwise equals `pricePerDraw`.
4. `isProtected` = `true` if an active `lottery_lock` record exists for this lottery.
5. Results are consistent to approximately 30 seconds (eventually consistent read); slight delays in `remainingDraws` are acceptable per the spec (FR-012).
6. If the caller is authenticated (JWT present) and has favourited a product, a `isFavourited: true` field may be added in future — not in scope for this feature.
