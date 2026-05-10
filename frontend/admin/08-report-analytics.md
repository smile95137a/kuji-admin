# 08 - 報表與統計分析

> **路由前綴**：`/admin/report`  
> **允許角色**：依報表而定；推薦碼報表與平台金流相關報表為 ADMIN Only

---

## 資料隔離

- **ADMIN**：可查詢任何店家的報表，`condition.storeId` 選填
- **STORE_OWNER**：只能查詢自己店家，`storeId` 後端強制注入，前端傳了也無效

---

## 營業額報表

```
POST /api/admin/report/revenue
Authorization: Bearer {token}
```

### 請求
```typescript
interface RevenueReportCondition {
  storeId?: string;         // Admin 可選；StoreOwner 後端自動注入
  startDate?: string;       // 開始日期（YYYY-MM-DD）
  endDate?: string;         // 結束日期（YYYY-MM-DD）
  groupBy?: 'DAY' | 'WEEK' | 'MONTH';  // 統計維度（預設 DAY）
}
```

### 回應
```typescript
interface RevenueReportRes {
  summary: {
    totalRevenue: number;         // 總營業額（金幣）
    totalOrders: number;          // 總訂單數
    totalDraws: number;           // 總抽獎次數
    avgRevenuePerDay: number;
  };
  daily: {
    date: string;                 // YYYY-MM-DD
    revenue: number;
    orderCount: number;
    drawCount: number;
  }[];
  byStore?: {                     // Admin 才有，依店家分群
    storeId: string;
    storeName: string;
    revenue: number;
    orderCount: number;
  }[];
}
```

---

## 開獎結果報表

```
POST /api/admin/report/lottery-result
Authorization: Bearer {token}
```

### 請求
```typescript
interface LotteryResultReportCondition {
  storeId?: string;
  lotteryId?: string;   // 篩選特定商品
  startDate?: string;
  endDate?: string;
}
```

### 回應
```typescript
interface LotteryResultReportRes {
  summary: {
    totalDraws: number;
    totalGrandPrizes: number;     // 大賞出現次數
    completionRate: number;       // 商品完銷率（%）
  };
  prizeStats: {                   // 各獎品抽取統計
    prizeId: string;
    prizeName: string;
    prizeLevel: string;
    totalQuantity: number;
    drawnQuantity: number;
    remainingQuantity: number;
    drawRate: number;             // 已抽出率（%）
  }[];
  lotteryStats: {                 // 各商品統計
    lotteryId: string;
    lotteryTitle: string;
    totalDraws: number;
    revenue: number;
    isCompleted: boolean;
  }[];
}
```

---

## 推薦碼報表

```
POST /api/admin/report/referral
Authorization: Bearer {token}（需 ADMIN）
```

### 請求
```typescript
interface ReferralReportCondition {
  storeId?: string;     // 推薦店家 ID，Admin 篩選用
  startDate?: string;
  endDate?: string;
}
```

### 回應
```typescript
interface ReferralReportRes {
  startDate: string;
  endDate: string;
  totalReferralCodeCount: number;         // 推薦碼總數
  activeReferralCodeCount: number;        // 啟用中的推薦碼總數
  successfulReferralStoreCount: number;   // 歷史累計成功招商店數
  currentPeriodActivatedStoreCount: number;
  previousPeriodActivatedStoreCount: number;
  growthRate?: number;                    // 上期無資料時可能為 null
  dailyActivations: {
    date: string;
    activatedStoreCount: number;
  }[];
  storePerformances: {
    referrerStoreId: string;
    referrerStoreName: string;
    referralCodeCount: number;
    activatedStoreCount: number;
    lastActivatedDate?: string;
    rank: number;
  }[];
}
```

### 前端實作重點

- 此報表已改為 **店薦店招商報表**，不是會員推薦獎勵報表
- 此報表為 **ADMIN only**，StoreOwner / StoreEditor 不應顯示入口
- 若有店家篩選器，語意應顯示為「推薦店家」而不是「被推薦店家」
- 首屏摘要建議顯示：推薦碼總數、啟用中推薦碼數、累計成功招商店數、本期對比上期成長率
- 主表格建議使用 `storePerformances`
- 趨勢圖建議使用 `dailyActivations`

---

## 儲值報表

```
POST /api/admin/report/recharge
Authorization: Bearer {token}（需 ADMIN）
```

### 請求
```typescript
interface RechargeReportCondition {
  startDate?: string;
  endDate?: string;
  planId?: string;  // 篩選特定儲值方案
}
```

### 回應
```typescript
interface RechargeReportRes {
  summary: {
    totalAmount: number;    // 實際收入金額（台幣）
    totalGoldIssued: number;// 發放金幣總數
    totalBonusIssued: number;
    orderCount: number;
  };
  daily: {
    date: string;
    amount: number;
    goldIssued: number;
    orderCount: number;
  }[];
  planStats: {              // 各儲值方案統計
    planId: string;
    planName: string;
    amount: number;
    orderCount: number;
  }[];
}
```

---

## 平台營收總覽報表

```
POST /api/admin/report/platform-revenue
Authorization: Bearer {token}（需 ADMIN）
```

### 請求
```typescript
interface PlatformRevenueReportCondition {
  startDate?: string;  // YYYY-MM-DD，不傳時後端預設最近 30 天
  endDate?: string;    // YYYY-MM-DD，不傳時後端預設今天
}
```

### 回應
```typescript
interface PlatformRevenueReportRes {
  startDate: string;
  endDate: string;
  totalRecharge: number;          // 平台總儲值金額（wallet_transaction.amount，RECHARGE）
  totalSpend: number;             // 平台總消耗金額（目前僅統計 DRAW）
  netRevenue: number;             // totalRecharge - totalSpend
  drawCount: number;              // 抽獎交易筆數
  rechargeGrowthRate?: number;    // 與上一個等長期間相比成長率，無基期時為 null
  spendGrowthRate?: number;
  spendByType: {
    gold: number;                 // GOLD 消耗
    bonus: number;                // BONUS 消耗
  };
  dailyRevenue: {
    date: string;                 // YYYY-MM-DD
    recharge: number;
    spend: number;
    netRevenue: number;
  }[];
  storeBreakdown: {
    storeId: string;
    storeName: string;
    totalSpend: number;           // 該店商品的 DRAW 消耗
    drawCount: number;
  }[];
}
```

### 前端實作重點

- 此報表為 **Admin Only**，StoreOwner / StoreEditor 不應顯示入口。
- 日期區間可不傳；若前端送空 body，後端會補成最近 30 天。
- `dailyRevenue` 已由後端補零，前端可直接拿來畫折線圖，不需要自行補缺日。
- `rechargeGrowthRate`、`spendGrowthRate` 可能是 `null`，代表上一期間沒有可比基期，前端應顯示 `--`。
- `storeBreakdown` 目前只統計抽獎消耗，不代表一般商城訂單營收。

詳細欄位與畫面建議請看 [13-platform-revenue-report.md](./13-platform-revenue-report.md)。

---

## 前端 UI 建議

### 報表頁面
- 日期範圍快捷選項：今天/本週/本月/上月/自訂
- 圖表：
  - 折線圖（日期 × 營業額）
  - 長條圖（各商品業績比較）
  - 圓餅圖（獎品等級分佈）
- 表格支援匯出（CSV / Excel）

### 注意事項
- 所有金額單位為**金幣**（不是台幣），除了儲值報表的 `amount` 是台幣
- `remaining` 欄位代表庫存，若需換算比率使用 `drawnQuantity / totalQuantity`
