# 07 - 錢包與儲值

> **路由前綴**：`/wallet`、`/recharge`、`/recharge-plan`（均需 Authorization Token）

---

## API 列表

| 方法 | 路徑 | 說明 |
|------|------|------|
| GET | `/wallet` | 取得錢包資訊（餘額） |
| POST | `/wallet/transactions` | 查詢錢包交易記錄 |
| GET | `/recharge-plan/list` | 取得儲值方案列表 |
| GET | `/recharge-plan/{id}` | 取得單一儲值方案 |
| POST | `/recharge` | 建立儲值訂單 |
| GET | `/recharge/history` | 取得儲值紀錄 |
| POST | `/recharge/{id}/confirm` | 支付成功回呼（金流用） |
| POST | `/recharge/{id}/failure` | 支付失敗回呼（金流用） |

---

## 錢包資訊

```
GET /api/wallet
Authorization: Bearer {token}
```

### 回應
```typescript
interface WalletRes {
  userId: string;
  goldBalance: number;        // 金幣餘額
  bonusBalance: number;       // 紅利餘額
  totalRecharge: number;      // 歷史累計儲值金額（台幣）
  updatedAt: string;
}
```

---

## 錢包交易記錄

```
POST /api/wallet/transactions
Authorization: Bearer {token}
```

```typescript
interface WalletTransactionQueryReq {
  condition?: {
    type?: WalletTransactionType;
    createdAtStart?: string;
    createdAtEnd?: string;
  };
  page?: number;    // 從 1 開始
  size?: number;
  sortOrder?: 'ASC' | 'DESC';
}

type WalletTransactionType =
  | 'RECHARGE'         // 儲值
  | 'DRAW_GOLD'        // 抽獎扣減（金幣）
  | 'DRAW_BONUS'       // 抽獎扣減（紅利）
  | 'RECYCLE_BONUS'    // 回收獎品獲得紅利
  | 'REFERRAL_BONUS'   // 推薦獎勵
  | 'ADMIN_ADJUST'     // 管理員手動調整
  | 'EXPIRE';          // 紅利到期
```

### 回應
```typescript
interface WalletTransactionRes {
  id: string;
  type: WalletTransactionType;
  coinType: 'GOLD' | 'BONUS';
  amount: number;        // 正數 = 收入，負數 = 支出
  balanceAfter: number;  // 交易後餘額
  description: string;
  referenceId: string | null;   // 關聯 ID（如訂單 ID）
  createdAt: string;
}
```

---

## 儲值方案

```
GET /api/recharge-plan/list
Authorization: Bearer {token}
```

> 後端自動過濾：只返回 `isActive=true` 且在有效期限內的方案。

### 回應
```typescript
interface RechargePlanRes {
  id: string;
  name: string;            // 如：「小包」「超值包」
  description: string;
  price: number;           // 台幣（TWD）
  goldCoins: number;       // 購買後獲得金幣
  bonusCoins: number;      // 額外贈送紅利（贈品）
  imageUrl: string | null;
  isHot: boolean;          // 是否標記為「熱門」
  sortOrder: number;
  startAt: string | null;
  endAt: string | null;
}
```

---

## 建立儲值訂單

```
POST /api/recharge
Authorization: Bearer {token}
```

```typescript
interface RechargeReq {
  planId: string;    // 方案 ID
}
```

### 回應
```typescript
interface RechargeRes {
  id: string;
  planId: string;
  planName: string;
  amount: number;         // 應付金額（TWD）
  goldCoins: number;      // 購買金幣
  bonusCoins: number;     // 贈送紅利
  status: RechargeStatus;
  paymentUrl: string | null;   // 金流跳轉 URL（目前測試模式為 null）
  completedAt: string | null;
  createdAt: string;
}

type RechargeStatus =
  | 'PENDING'     // 待支付
  | 'COMPLETED'   // 已完成
  | 'FAILED'      // 失敗
  | 'CANCELLED';  // 已取消
```

---

## ⚠️ 測試模式注意事項

```
目前儲值功能為「測試模式」：
  - 呼叫 POST /recharge 後，後端直接將狀態設為 COMPLETED
  - 金幣立即到帳
  - paymentUrl = null（不需跳轉支付頁）
  - /recharge/{id}/confirm 和 /recharge/{id}/failure 
    是為未來金流串接保留的端點（目前前端勿呼叫）

  上線前需接入綠界/歐付寶等金流，屆時 paymentUrl 才會有值。
```

---

## 儲值紀錄

```
GET /api/recharge/history?page=1&size=10
Authorization: Bearer {token}
```

返回 `RechargeRes` 列表（當前玩家的儲值歷史）。

---

## 前端 UI 建議

### 錢包頁
- 頂部大字顯示金幣 🪙（goldBalance）和紅利 💎（bonusBalance）
- 下方顯示最近 10 筆交易記錄
- 「前往儲值」按鈕

### 儲值方案頁
- 卡片列表，`isHot=true` 顯示熱門 Badge
- 顯示價格、獲得金幣、贈送紅利
- 點擊方案 → 確認彈窗 → POST /recharge → 直接完成（測試模式）
- 完成後提示「儲值成功，金幣已到帳」並更新顯示

### 交易記錄
- 依類型篩選（全部 / 儲值 / 抽獎消費 / 回收獎勵）
- 金額正負數用顏色區分（正=綠 / 負=紅）
