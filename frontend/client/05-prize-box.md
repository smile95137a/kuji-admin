# 05 - 獎品盒管理

> **路由前綴**：`/prize-box`（均需 Authorization Token）  
> 抽獎後獎品會先進入獎品盒，玩家自行決定要「出貨（寄送）」還是「回收（換紅利）」。

---

## API 列表

| 方法 | 路徑 | 說明 |
|------|------|------|
| GET | `/prize-box` | 取得目前獎品盒（AVAILABLE 狀態） |
| GET | `/prize-box/history` | 取得獎品盒歷史紀錄 |
| GET | `/prize-box/summary` | 取得可出貨摘要（依店家分組） |
| POST | `/prize-box/ship` | 申請出貨（建立訂單） |
| POST | `/prize-box/recycle` | 回收換紅利 |

---

## 獎品盒資料結構

```typescript
interface PrizeBoxItemRes {
  id: string;                    // PrizeBox ID（出貨/回收時使用）
  lotteryId: string;
  lotteryTitle: string;
  storeId: string;
  storeName: string;
  prizeId: string;
  prizeName: string;
  prizeLevel: string;
  prizeImageUrl: string;
  isGrandPrize: boolean;
  status: 'AVAILABLE' | 'SHIPPED' | 'RECYCLED' | 'SHIPPING';
  recycleBonus: number;          // 可換紅利點數
  createdAt: string;             // 抽到的時間
  updatedAt: string;
}
```

---

## 取得目前獎品盒

```
GET /api/prize-box?status=AVAILABLE
Authorization: Bearer {token}
```

| 參數 | 說明 | 預設 |
|------|------|------|
| `status` | 篩選狀態（不填 = 返回 AVAILABLE） | `AVAILABLE` |

返回目前「可操作」的獎品。

---

## 取得歷史紀錄

```
GET /api/prize-box/history?status=&page=1&size=20
Authorization: Bearer {token}
```

| 參數 | 說明 |
|------|------|
| `status` | `SHIPPED` / `RECYCLED` / `SHIPPING`（不填 = 全部） |
| `page` | 頁碼（從 1 開始） |
| `size` | 每頁筆數 |

### 回應
```typescript
interface PageResult<PrizeBoxItemRes> {
  items: PrizeBoxItemRes[];
  total: number;
  page: number;
  size: number;
  totalPages: number;
}
```

---

## 取得可出貨摘要（依店家分組）

```
GET /api/prize-box/summary
Authorization: Bearer {token}
```

### 回應（依店家分組，方便出貨 UI 選取）
```typescript
interface PrizeBoxSummaryRes {
  storeId: string;
  storeName: string;
  storeLogo: string;
  availableCount: number;       // 可出貨數量
  totalRecycleBonus: number;    // 全部回收可得紅利總計
  items: PrizeBoxItemRes[];     // 該店家的獎品列表
}
```

---

## 查詢可用運送方式

出貨彈窗開啟時需先取得目前啟用的運送方式列表：

```
GET /api/shipping-methods
Authorization: Bearer {token}
```

```typescript
interface ShippingMethodRes {
  id: string;
  name: string;       // 顯示名稱（如「7-11 超商取貨」）
  code: string;       // 系統代碼（如 SEVEN_ELEVEN）
  provider: string;   // 物流商名稱
  fee: number;        // 運費（元），0 = 免運
}
```

---

## 申請出貨（建立訂單）

```
POST /api/prize-box/ship
Authorization: Bearer {token}
```

```typescript
interface PrizeBoxShipReq {
  prizeBoxIds: string[];          // 要出貨的 PrizeBox ID 列表（必須同一家店）
  shippingMethodId: string;       // 選擇的運送方式 ID
  shippingFee: number;            // 前端帶入所選方式的 fee（後端驗證一致性）

  recipientName: string;
  recipientPhone: string;

  // 宅配必填
  zipCode?: string;
  city?: string;
  district?: string;
  address?: string;

  // 超商取貨必填（由 GoMyPay 物流 API 回傳）
  storeType?: string;             // 超商類型代碼（如 SEVEN_ELEVEN）
  storeName?: string;             // 門市名稱
  storeCode?: string;             // 門市代碼
  storeAddress?: string;          // 門市地址

  note?: string;                  // 備註
}
```

> ⚠️ **同一張訂單只能包含同一家店的獎品**，跨店需分別申請。

### 回應
```typescript
interface ShipResult {
  orderId: string;                // 建立的訂單 ID
  orderNumber: string;            // 訂單編號
  prizeBoxCount: number;          // 本次出貨獎品數量
  shippingFee: number;            // 確認的運費（元）
  paymentUrl: string;             // GoMyPay 金流付款頁面 URL（導向此 URL 讓用戶付款）
}
```

### 付款流程（GoMyPay 萬事達金流）

1. 前端建立訂單 → 後端向 GoMyPay 建立付款單 → 回傳 `paymentUrl`
2. 前端開啟 `paymentUrl`（新分頁或 redirect）讓用戶完成付款
3. GoMyPay 付款完成後 callback 後端，後端更新訂單 `paymentStatus = PAID`
4. 前台輪詢或 WebSocket 確認付款狀態後，關閉彈窗并跳至訂單詳情頁

> 付款金額 = 運費（`shippingFee`）。賞品本身已由金幣抽取，不再收費。

---

## 回收換紅利

```
POST /api/prize-box/recycle
Authorization: Bearer {token}
```

```typescript
interface PrizeBoxRecycleReq {
  prizeBoxIds: string[];         // 要回收的 PrizeBox ID 列表（可跨店）
}
```

### 回應
```typescript
interface RecycleResultRes {
  recycledCount: number;
  bonusEarned: number;          // 本次獲得紅利點數
  newBonusBalance: number;      // 回收後紅利餘額
}
```

---

## 狀態流轉圖

```
抽獎成功
    │
    ▼
AVAILABLE ──→ 選擇出貨 ──→ SHIPPING ──→ 店家出貨後 ──→ SHIPPED
    │
    └─────────→ 選擇回收 ──→ RECYCLED（紅利+N）
```

---

## 前端 UI 建議

### 獎品盒頁面
- 顯示所有 `AVAILABLE` 的獎品卡片
- 每張卡片顯示：獎品圖、名稱、等級、可回收紅利
- 底部固定「多選出貨」和「多選回收」按鈕
- 選取模式：可選擇多個獎品
  - 選擇出貨：跳出填寫地址彈窗
  - 選擇回收：確認對話框（顯示共可得 X 紅利）

### 出貨彈窗流程（Step-by-Step Modal）

點擊「確定出貨」按鈕後，彈出多步驟 Modal：

#### Step 1 — 選擇運送方式

- 呼叫 `GET /api/shipping-methods` 取得啟用中的運送方式列表
- 以卡片或 Radio 按鈕顯示每個選項：
  - 顯示：名稱、物流商、運費（`{fee} 元` / 免運）
- 選擇後即時更新底部「應付金額」= 選定方式的運費
- 若尚未選擇，「下一步」按鈕為 disabled

#### Step 2a — 填寫收件資訊（宅配）

當選擇的 `code` **不包含** `SEVEN_ELEVEN / FAMILY_MART / HI_LIFE` 等超商代碼時顯示：

| 欄位 | 必填 |
|------|------|
| 收件人姓名 | ✅ |
| 手機號碼 | ✅ |
| 郵遞區號 | ❌ |
| 縣市 | ✅ |
| 區域 | ✅ |
| 詳細地址 | ✅ |
| 備註 | ❌ |

- 預設帶入 `GET /api/user/addresses/default`（若有則自動填入）
- 可手動修改

#### Step 2b — 選擇門市（超商取貨）

當選擇的 `code` 為超商類型時顯示：

- 顯示「選擇門市」按鈕
- 點擊後透過 **GoMyPay 物流 API** 開啟原生選店地圖（Map CVS）
- 用戶選定門市後，GoMyPay callback 回傳門市資訊，自動填入：
  - 門市名稱（`storeName`）
  - 門市代碼（`storeCode`）
  - 門市地址（`storeAddress`）
- 以 Read-only 方式顯示已選門市資訊（可重新選擇）
- 必填：收件人姓名、手機號碼

> ⚠️ GoMyPay 選店地圖整合方式：透過 form POST 開啟 GoMyPay 提供的選店頁面（`LogisticsApi`），  
> 選店後回傳門市資訊到指定 callback URL，前端接收並更新欄位。

#### Step 3 — 確認與付款

彈窗底部固定顯示付款摘要：

```
本次出貨：N 件獎品
運送方式：7-11 超商取貨（統一速達）
應付運費：NT$ 60
```

- 點擊「確認並前往付款」：
  1. 呼叫 `POST /api/prize-box/ship`
  2. 後端回傳 `paymentUrl`
  3. 前端 redirect 或新分頁開啟 GoMyPay 付款頁面
  4. 付款完成後 GoMyPay 導回前台，前台顯示「訂單建立成功」並跳至訂單詳情頁

- 付款失敗或取消：訂單留在 `PAYMENT_PENDING` 狀態，用戶可從訂單頁重新付款

### 歷史紀錄
- Tab 切換：全部 / 已出貨 / 已回收
- 顯示時間、店家名稱、獎品圖（縮圖）
