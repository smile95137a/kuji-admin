# 06 - 訂單管理

> **路由前綴**：`/order`（均需 Authorization Token）  
> 訂單由「申請出貨（POST /prize-box/ship）」自動建立，不需要玩家手動建立。

---

## API 列表

| 方法 | 路徑 | 說明 |
|------|------|------|
| POST | `/order/list` | 查詢訂單列表 |
| GET | `/order/{orderId}` | 取得訂單詳情 |
| POST | `/order/{orderId}/shipping-info` | 更新物流資訊（補填）|

---

## 訂單資料結構

```typescript
interface OrderRes {
  id: string;
  storeId: string;
  storeName: string;
  status: OrderStatus;
  totalAmount: number;           // 訂單總金額（目前為 0，以金幣為主）
  recipientName: string;
  recipientPhone: string;
  zipCode: string;
  city: string;
  district: string;
  address: string;
  note: string | null;
  shippingMethodId: string;      // 運送方式 ID
  shippingMethodName: string;    // 運送方式顯示名稱（如「7-11 超商取貨」）
  shippingMethodCode: string;    // 運送方式代碼（判斷是否超商）
  shippingFee: number;           // 運費（元）
  paymentStatus: string;         // PAYMENT_PENDING | PAID | FAILED
  trackingNumber: string | null; // 物流追蹤號（後台出貨時填入，出貨前為 null）
  trackingUrl: string | null;    // 外部物流追蹤連結（可為 null）
  storeCode: string | null;      // 超商門市代碼（超商取貨時才有）
  storeName: string | null;      // 超商門市名稱
  storeAddress: string | null;   // 超商門市地址
  shippedAt: string | null;
  deliveredAt: string | null;
  createdAt: string;
  items: OrderItemRes[];
}

interface OrderItemRes {
  id: string;
  prizeBoxId: string;
  prizeId: string;
  prizeName: string;
  prizeLevel: string;
  prizeImageUrl: string;
  isGrandPrize: boolean;
  lotteryTitle: string;
}

type OrderStatus =
  | 'PENDING'      // 待處理
  | 'CONFIRMED'    // 已確認
  | 'SHIPPING'     // 出貨中
  | 'DELIVERED'    // 已送達
  | 'CANCELLED'    // 已取消
  | 'RETURNED';    // 已退回
```

---

## 查詢訂單列表

```
POST /api/order/list
Authorization: Bearer {token}
```

```typescript
interface OrderListReq {
  condition?: {
    status?: OrderStatus;
    keyword?: string;       // 模糊搜尋訂單 ID 或商品名稱
    createdAtStart?: string;
    createdAtEnd?: string;
  };
  sortBy?: string;
  sortOrder?: 'ASC' | 'DESC';
}
```

> 後端自動過濾：只返回**當前登入玩家**的訂單，不需傳 `userId`。

---

## 取得訂單詳情

```
GET /api/order/{orderId}
Authorization: Bearer {token}
```

> 如果訂單不屬於當前玩家，後端回傳 `403 Forbidden`。

---

## 更新收件資訊

```
POST /api/order/{orderId}/shipping-info
Authorization: Bearer {token}
```

```typescript
interface ShipInfoReq {
  recipientName: string;
  recipientPhone: string;
  zipCode?: string;
  city: string;
  district: string;
  address: string;
  note?: string;
}
```

> ⚠️ 僅限 `status = PENDING` 的訂單可修改，其他狀態回傳 `400`。

---

## 訂單狀態流轉

```
申請出貨（/prize-box/ship）
        │
        ▼
  PAYMENT_PENDING（待付款運費）
        │
        │ GoMyPay 回調付款成功
        ▼
    PENDING（待店家處理）
        │
        │ 店家確認備貨
        ▼
    CONFIRMED（備貨中）
        │
        │ 店家填入物流單號並標記出貨
        │ → 系統自動寄送 Email 給會員（見下方）
        ▼
    SHIPPING（出貨中）
        │
        │ 送達確認
        ▼
    DELIVERED（已送達）
```

---

## 出貨通知 Email（後台出貨時自動觸發）

**觸發時機**：店家後台標記訂單出貨（填入 `trackingNumber`）就完成時，後端自動發送 Email。

**收件人**：訂單所屬會員的帳號 Email

**Email 內容必含**：
- 訂單編號（`orderNumber`）
- 出貨獎品列表（獎品名稱 + 等級）
- 運送方式名稱
- 物流進跨編號（`trackingNumber`）
- 外部追蹤連結（若有）
- 收件人資訊（姓名、電話、地址或門市名稱）

> 後端負責發送，前端不需另外處理。

---

## 前端 UI 建議

### 訂單列表頁
- Tab 切換訂單狀態
- 顯示：店家名稱、訂單建立時間、商品縮圖（前 3 個）、狀態 Badge
- 點擊進入詳情頁

### 訂單詳情頁
- 顯示完整收件資訊：
  - **宅配**：地址、郵遞區號
  - **超商取貨**：門市名稱、門市地址
- 運送方式、運費金額
- 物流追蹤號（`trackingNumber`）— 有値才顯示，否則顯示「店家備貨中」
- 如果 `trackingUrl` 不為 null，顯示「查詢物流進度」外部連結按鈕
- 商品列表（獎品圖 + 名稱 + 等級）
- `paymentStatus = PAYMENT_PENDING` 晋顯示「訂單待付款型按鈕」（重新前往 GoMyPay 付款）
- `status = PENDING` 時：顯示「修改地址」按鈕
