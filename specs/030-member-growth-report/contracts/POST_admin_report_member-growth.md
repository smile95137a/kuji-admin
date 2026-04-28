# API Contract: POST /admin/report/member-growth

**Feature**: 030 - 會員成長報表  
**Branch**: `030-member-growth-report`

---

## Endpoint

| 屬性 | 值 |
|---|---|
| Method | `POST` |
| Path | `/admin/report/member-growth` |
| Auth | JWT（Bearer Token） |
| Role | `ADMIN` only |
| Content-Type | `application/json` |

---

## Request Body

```json
{
  "condition": {
    "startDate": "2026-04-01",
    "endDate":   "2026-04-30"
  }
}
```

### Schema: `QueryReq<MemberGrowthReportCondition>`

| 欄位 | 類型 | 必填 | 說明 |
|---|---|---|---|
| `condition` | Object | 否 | 查詢條件（null 時使用預設值） |
| `condition.startDate` | `String (yyyy-MM-dd)` | 否 | 查詢開始日期；預設 today - 29 days |
| `condition.endDate` | `String (yyyy-MM-dd)` | 否 | 查詢結束日期；預設 today |

---

## Response Body

HTTP 200 OK

```json
{
  "startDate": "2026-04-01",
  "endDate":   "2026-04-30",

  "totalNewMembers": 150,
  "growthRate": 12.5,
  "registrationByProvider": {
    "EMAIL":  70,
    "GOOGLE": 80
  },
  "dailyNewMembers": [
    { "date": "2026-04-01", "count": 5 },
    { "date": "2026-04-02", "count": 3 },
    "..."
  ],

  "activeMembers": 500,
  "arpuGold":  200.0,
  "arpuBonus": 40.0,

  "retention7Days":  60.0,
  "retention30Days": 35.0
}
```

### Schema: `MemberGrowthReportRes`

| 欄位 | 類型 | Nullable | 說明 |
|---|---|---|---|
| `startDate` | `String (yyyy-MM-dd)` | No | 實際查詢起始日 |
| `endDate` | `String (yyyy-MM-dd)` | No | 實際查詢結束日 |
| `totalNewMembers` | `Integer` | No | 查詢期間新增會員總數（無資料時為 0） |
| `growthRate` | `BigDecimal` | Yes | 與上期相比成長率（%）；上期無資料時 null |
| `registrationByProvider` | `Map<String,Integer>` | No | 按 provider 分類，無資料時為空 Map `{}` |
| `dailyNewMembers` | `Array<DailyNewMember>` | No | 每日明細，長度 = 查詢天數；無新增時 count = 0 |
| `activeMembers` | `Integer` | No | 活躍會員數；無活躍時為 0 |
| `arpuGold` | `BigDecimal` | No | 金幣 ARPU（精確到 0.1）；activeMembers = 0 時為 0.0 |
| `arpuBonus` | `BigDecimal` | No | 紅利 ARPU（精確到 0.1）；activeMembers = 0 時為 0.0 |
| `retention7Days` | `BigDecimal` | Yes | 7 天留存率（%）；前月無新增會員時 null |
| `retention30Days` | `BigDecimal` | Yes | 30 天留存率（%）；前月無新增會員時 null |

#### DailyNewMember

| 欄位 | 類型 | 說明 |
|---|---|---|
| `date` | `String (yyyy-MM-dd)` | 日期 |
| `count` | `Integer` | 當日新增會員數（補零） |

---

## Error Responses

| HTTP Status | 情境 |
|---|---|
| 401 Unauthorized | JWT 未提供或已過期 |
| 403 Forbidden | 非 ADMIN 角色（STORE_OWNER 呼叫此端點） |
| 400 Bad Request | startDate > endDate |
| 500 Internal Server Error | 資料庫查詢異常 |

---

## Business Rules

1. **活躍會員** = 查詢期間內有以下任一行為的不重複會員：
   - `user.last_login_at` 在範圍內（登入）
   - `wallet_transaction.transaction_type = 'RECHARGE'` 在範圍內（儲值）
   - `lottery_ticket.status = 'DRAWN' AND drawn_at` 在範圍內（抽獎）
   - `order.created_at` 在範圍內（建立訂單）

2. **arpuGold** = SUM(`wallet_transaction.amount` WHERE `transaction_type='DRAW' AND coin_type='GOLD'`) / `activeMembers`

3. **arpuBonus** = SUM(`wallet_transaction.amount` WHERE `transaction_type='DRAW' AND coin_type='BONUS'`) / `activeMembers`

4. **留存率基準** = 前一個完整月份的新增會員（不受查詢日期範圍影響）

5. **dailyNewMembers** 必須包含查詢範圍內每一天（無新增的日期補 `count: 0`）

6. **無資料邊界**：所有計數為 0 時回傳 0，不回傳 null（retention 除外）

---

## Test Cases（對應驗收場景）

### TC-001：月度新增會員統計

```
POST /admin/report/member-growth
{ "condition": { "startDate": "2026-04-01", "endDate": "2026-04-30" } }

→ totalNewMembers = 150
→ dailyNewMembers.length = 30
→ registrationByProvider = { "GOOGLE": 80, "EMAIL": 70 }
```

### TC-002：ARPU 計算

```
activeMembers = 500
totalGoldDraw = 100000
totalBonusDraw = 20000

→ arpuGold  = 200.0
→ arpuBonus = 40.0
```

### TC-003：活躍會員包含只用紅利消費的用戶

```
user A: DRAW + BONUS only (無 GOLD)
→ 仍計入 activeMembers
→ arpuGold 不受影響（分子為 0 的部分不計）
```

### TC-004：7/30 天留存率

```
前月新增 100 人
→ 7 天內再活躍 60 人  → retention7Days  = 60.0
→ 30 天內再活躍 35 人 → retention30Days = 35.0
```

### TC-005：無資料期間

```
查詢無任何新增會員
→ totalNewMembers = 0
→ activeMembers = 0
→ arpuGold = 0.0, arpuBonus = 0.0
→ dailyNewMembers[*].count = 0
```
