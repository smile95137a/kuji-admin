# 13 - 平台營收總覽報表前端須知

> 對應功能：`033-platform-revenue-report`  
> API：`POST /api/admin/report/platform-revenue`  
> 角色限制：`ADMIN` only

---

## 使用場景

此報表提供後台 Admin 觀看平台層級的資金流與抽獎消耗趨勢，適合放在：

- 報表總覽首頁卡片
- 營運分析頁的區間趨勢圖
- 各店家抽獎消耗排行表

StoreOwner / StoreEditor 不可看到此頁，也不應嘗試呼叫此 API。

---

## 請求格式

```http
POST /api/admin/report/platform-revenue
Authorization: Bearer {token}
Content-Type: application/json
```

```json
{
  "condition": {
    "startDate": "2026-04-01",
    "endDate": "2026-04-30"
  }
}
```

### 日期規則

- `condition` 可不傳
- `startDate`、`endDate` 可不傳
- 若前端送 `{}` 或 `{ "condition": {} }`，後端會自動補成最近 30 天
- 回應中的 `startDate`、`endDate` 一律是後端實際採用的日期

---

## 回應欄位說明

```typescript
interface PlatformRevenueReportRes {
  startDate: string;
  endDate: string;
  totalRecharge: number;
  totalSpend: number;
  netRevenue: number;
  drawCount: number;
  rechargeGrowthRate?: number | null;
  spendGrowthRate?: number | null;
  spendByType: {
    gold: number;
    bonus: number;
  };
  dailyRevenue: {
    date: string;
    recharge: number;
    spend: number;
    netRevenue: number;
  }[];
  storeBreakdown: {
    storeId: string;
    storeName: string;
    totalSpend: number;
    drawCount: number;
  }[];
}
```

### 欄位定義

- `totalRecharge`
  - 平台查詢區間內的總儲值金額
  - 來源為 `wallet_transaction.transaction_type = RECHARGE`
- `totalSpend`
  - 平台查詢區間內的總消耗金額
  - 目前版本只統計 `transaction_type = DRAW`
- `netRevenue`
  - `totalRecharge - totalSpend`
- `drawCount`
  - 抽獎交易筆數，不是抽中獎品數
- `rechargeGrowthRate` / `spendGrowthRate`
  - 以前一個「等長日期區間」作比較
  - 若上一期為 0 或沒有資料，後端回 `null`
- `spendByType.gold`
  - GOLD 類型的抽獎消耗
- `spendByType.bonus`
  - BONUS 類型的抽獎消耗
- `dailyRevenue`
  - 每日趨勢資料，後端已補齊缺漏日期
- `storeBreakdown`
  - 依店家聚合的抽獎消耗排行
  - 目前不是商城訂單營收排行

---

## UI 呈現建議

### 首屏 KPI 卡片

- 總儲值：`totalRecharge`
- 總消耗：`totalSpend`
- 淨營收：`netRevenue`
- 抽獎筆數：`drawCount`

### 趨勢圖

- X 軸使用 `dailyRevenue[].date`
- 主圖建議畫 3 條線：
  - `recharge`
  - `spend`
  - `netRevenue`

### 消耗結構

- 用圓餅圖或雙卡片呈現：
  - GOLD 消耗
  - BONUS 消耗

### 店家排行

- 表格欄位建議：
  - 排名
  - 店家名稱
  - `totalSpend`
  - `drawCount`
- 預設依 `totalSpend DESC`

---

## 前端實作注意事項

- 成長率可能為 `null`，請顯示 `--`，不要強制轉成 `0%`
- `dailyRevenue` 已補零，前端不要再自行插值
- 請把金額格式統一成有千分位的數字
- 若查詢區間很短，仍要完整顯示每一天，避免圖表斷點
- 頁面沒有店家切換器，因為此 API 是平台維度，不支援 `storeId`

---

## 錯誤處理

- `403 Forbidden`
  - 非 Admin 呼叫
  - 前端應直接顯示無權限頁或返回報表首頁
- `401 Unauthorized`
  - Token 過期或未登入
  - 依既有 refresh token 流程處理

---

## 串接範例

```typescript
const res = await api.post('/admin/report/platform-revenue', {
  condition: {
    startDate: filters.startDate,
    endDate: filters.endDate,
  },
})

const report = res.data
```

---

## 版本備註

- 目前 `totalSpend` / `storeBreakdown` 只含抽獎消耗
- 若後續要把一般訂單消費納入平台營收，前後端欄位定義需要再同步調整
