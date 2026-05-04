# Contract: `POST /admin/report/platform-revenue`

## Purpose

提供 Admin 查詢平台整體營收總覽，包括儲值、消費、淨收入、幣別拆分、每日趨勢、店家貢獻與成長率。

## Security

- Required role: `ADMIN`
- Annotation target: `@PreAuthorize("hasRole('ADMIN')")`
- Audit: add `@AuditLog` on controller method

## Request

### Method / Path

```http
POST /admin/report/platform-revenue
```

### Body

```json
{
  "condition": {
    "startDate": "2026-04-01",
    "endDate": "2026-04-30"
  }
}
```

### Request Rules

- `condition` 可省略，則後端套用最近 30 天
- `startDate` / `endDate` 需為 `yyyy-MM-dd`
- `startDate > endDate` 時應回 400 或 service validation error

## Response

### 200 OK

```json
{
  "startDate": "2026-04-01",
  "endDate": "2026-04-30",
  "totalRecharge": 50000,
  "totalSpend": 30000,
  "netRevenue": 20000,
  "drawCount": 1500,
  "spendByType": {
    "gold": 25000,
    "bonus": 5000
  },
  "rechargeGrowthRate": 25.0,
  "spendGrowthRate": 10.0,
  "dailyRevenue": [
    {
      "date": "2026-04-01",
      "recharge": 2000,
      "spend": 1500,
      "net": 500
    }
  ],
  "storeBreakdown": [
    {
      "storeId": "store-uuid-001",
      "storeName": "A店",
      "totalSpend": 15000,
      "drawCount": 300
    }
  ]
}
```

## Field Semantics

| Field | Type | Rule |
|------|------|------|
| `totalRecharge` | `Long` | `RECHARGE + GOLD` 加總 |
| `totalSpend` | `Long` | `DRAW` 加總取絕對值 |
| `netRevenue` | `Long` | `totalRecharge - totalSpend` |
| `drawCount` | `Long` | `lottery_ticket.status='DRAWN'` 筆數 |
| `spendByType.gold` | `Long` | `DRAW + GOLD` 加總取絕對值 |
| `spendByType.bonus` | `Long` | `DRAW + BONUS` 加總取絕對值 |
| `rechargeGrowthRate` | `BigDecimal/null` | 對上期儲值成長率，無上期資料為 `null` |
| `spendGrowthRate` | `BigDecimal/null` | 對上期消費成長率，無上期資料為 `null` |
| `dailyRevenue` | `List` | 日期連續，不可缺天 |
| `storeBreakdown` | `List` | 依 `totalSpend DESC` 排序 |

## Error Cases

### 403 Forbidden

- 非 `ADMIN` 角色呼叫

### 400 Bad Request

- `startDate > endDate`
- 日期格式錯誤
