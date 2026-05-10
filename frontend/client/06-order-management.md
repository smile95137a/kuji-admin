# 06 - 訂單管理（前台）

路由前綴：/order（需登入）

## API 一覽

1. POST /order/ship：從賞品盒建立出貨訂單
2. POST /order/list：查詢我的訂單列表
3. GET /order/{orderId}：查詢我的訂單詳情
4. POST /order/{orderId}/shipping-info：更新收件資訊
5. POST /order/{orderId}/repay：重付款
6. DELETE /order/{orderId}/cancel：取消訂單

---

## 訂單狀態

```typescript
type OrderStatus =
  | 'PAYMENT_PENDING'
  | 'PAYMENT_FAILED'
  | 'PENDING'
  | 'PREPARING'
  | 'SHIPPED'
  | 'COMPLETED'
  | 'CANCELLED';
```

付款狀態：

```typescript
type PaymentStatus = 'PAYMENT_PENDING' | 'PAID' | 'FAILED';
```

---

## 主要流程

1. 玩家送出 /order/ship。
2. 後端回傳 paymentUrl（OrderPaymentInitRes）。
3. 玩家前往 GoMyPay 付款。
4. callback success：訂單進入 PENDING。
5. callback failed：訂單進入 PAYMENT_FAILED。
6. PAYMENT_FAILED 可選擇：
   - 重付款（/order/{id}/repay）
   - 取消訂單（/order/{id}/cancel）

---

## shipping-info 規則

POST /api/order/{orderId}/shipping-info

```typescript
interface ShipInfoReq {
  shippingMethod: 'HOME_DELIVERY' | 'SEVEN_ELEVEN' | 'FAMILY_MART';
  recipientName?: string;
  recipientPhone?: string;
  recipientAddress?: string;
  storeCode?: string;
  storeName?: string;
  storeAddress?: string;
  remark?: string;
}
```

限制：
1. 僅 PAYMENT_PENDING 可修改。
2. HOME_DELIVERY 必填 recipientName / recipientPhone / recipientAddress。
3. 超商取貨必填 storeCode / storeName。

---

## 重付款回應

POST /api/order/{orderId}/repay

```typescript
interface OrderPaymentInitRes {
  orderId: string;
  orderNumber: string;
  shippingFee: number;
  paymentStatus: string;
  paymentUrl: string;
  gatewayTradeNo: string;
}
```

---

## 玩家取消規則

DELETE /api/order/{orderId}/cancel

```typescript
interface OrderCancelReq {
  cancelReason?: string;
}
```

玩家可取消狀態：
1. PAYMENT_PENDING
2. PAYMENT_FAILED
3. PENDING

不可取消：
1. PREPARING
2. SHIPPED
3. COMPLETED

---

## UI 建議

1. PAYMENT_PENDING 顯示「前往付款」按鈕。
2. PAYMENT_FAILED 顯示「重新付款」與「取消訂單」按鈕。
3. PENDING 顯示「取消訂單」按鈕。
4. PAYMENT_PENDING 才顯示「修改收件資訊」按鈕。
