# Contract: Admin Draw History API — GET /admin/lottery/{id}/draws

**Feature**: `003-game-management`
**Version**: 1.0
**Auth**: Bearer JWT (ROLE_ADMIN or ROLE_STORE_OWNER for own store)

---

## Endpoint

```
GET /admin/lottery/{id}/draws
```

### Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | string (UUID) | Yes | Lottery ID |

### Query Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `page` | integer | No | 1 | Page number (1-based) |
| `size` | integer | No | 20 | Page size (max 100) |
| `userId` | string | No | - | Filter by player user ID |
| `status` | string | No | - | Filter by record status: SUCCESS / FAILED / PENDING |
| `startDate` | string | No | - | ISO date filter start: `2026-03-01T00:00:00` |
| `endDate` | string | No | - | ISO date filter end: `2026-03-31T23:59:59` |

---

## Response: Success (200 OK)

```json
{
  "success": true,
  "message": "查詢成功",
  "errorCode": null,
  "data": {
    "page": 1,
    "size": 20,
    "total": 78,
    "totalPages": 4,
    "records": [
      {
        "id": "record-uuid-001",
        "lotteryId": "lottery-uuid-123",
        "userId": "user-uuid-456",
        "prizeId": "prize-uuid-abc",
        "prizeName": "A賞 超人力霸王",
        "prizeLevel": "A",
        "prizeImageUrl": "https://cdn.example.com/prizes/ultraman-a.jpg",
        "isLastPrize": false,
        "costType": "GOLD",
        "costAmount": 300,
        "status": "SUCCESS",
        "createdAt": "2026-03-22T14:30:00"
      },
      {
        "id": "record-uuid-002",
        "lotteryId": "lottery-uuid-123",
        "userId": "user-uuid-789",
        "prizeId": "prize-uuid-def",
        "prizeName": "最後賞 限定版",
        "prizeLevel": "LAST",
        "prizeImageUrl": "https://cdn.example.com/prizes/last-prize.jpg",
        "isLastPrize": true,
        "costType": "GOLD",
        "costAmount": 300,
        "status": "SUCCESS",
        "createdAt": "2026-03-22T15:45:00"
      }
    ],
    "summary": {
      "totalDraws": 78,
      "successDraws": 77,
      "failedDraws": 1,
      "totalRevenue": 23100,
      "remainingDraws": 2
    }
  },
  "timestamp": "2026-03-22T16:00:00.000"
}
```

### Response Data Fields

| Field | Type | Description |
|-------|------|-------------|
| `page` | integer | Current page number |
| `size` | integer | Page size |
| `total` | long | Total record count matching filters |
| `totalPages` | integer | Total pages |
| `records` | array | Draw records for current page |
| `records[].id` | string | LotteryDrawRecord UUID |
| `records[].lotteryId` | string | Lottery UUID |
| `records[].userId` | string | Player UUID |
| `records[].prizeId` | string | LotteryPrize UUID |
| `records[].prizeName` | string | Prize name |
| `records[].prizeLevel` | string | A / B / C / D / LAST / THANKS |
| `records[].prizeImageUrl` | string | Prize image URL |
| `records[].isLastPrize` | boolean | Was this the 最後賞 draw? |
| `records[].costType` | string | GOLD / BONUS |
| `records[].costAmount` | long | Points deducted |
| `records[].status` | string | SUCCESS / FAILED / PENDING |
| `records[].createdAt` | datetime | Draw timestamp |
| `summary.totalDraws` | integer | Total draws ever for this lottery |
| `summary.successDraws` | integer | Successful draws |
| `summary.failedDraws` | integer | Failed draws |
| `summary.totalRevenue` | long | Total points consumed |
| `summary.remainingDraws` | integer | Current remaining count |

---

## Error Responses

### 403 Forbidden — Store owner accessing another store's lottery

```json
{
  "success": false,
  "message": "無權限查看此抽獎活動的抽獎記錄",
  "errorCode": "ACCESS_DENIED",
  "data": null,
  "timestamp": "2026-03-22T14:30:00.123"
}
```

### 404 Not Found

```json
{
  "success": false,
  "message": "抽獎活動不存在",
  "errorCode": "LOTTERY_NOT_FOUND",
  "data": null,
  "timestamp": "2026-03-22T14:30:00.123"
}
```

---

## Authorization Rules

| Role | Access |
|------|--------|
| `ROLE_ADMIN` | All lotteries |
| `ROLE_STORE_OWNER` | Only lotteries where `lottery.storeId == caller.storeId` |
| `ROLE_USER` (player) | No access (403) |

---

## Notes

- Results are ordered by `created_at DESC` (newest first).
- The `summary` block is computed over the full unfiltered dataset for the lottery, not just the current page.
- `userId` in the filter accepts UUID only; fuzzy search is not supported.
- Date filters use the server's timezone (Asia/Taipei by default).
