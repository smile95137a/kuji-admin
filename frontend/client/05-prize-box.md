# 05 - 獎品盒管理

路由前綴：/prize-box（需登入）

## API 一覽

1. GET /prize-box：取得可操作獎品
2. GET /prize-box/history：歷史紀錄
3. GET /prize-box/summary：依店家分組摘要
4. POST /prize-box/ship：申請出貨（建立訂單）
5. POST /prize-box/recycle：回收換紅利

---

## 獎品盒狀態

```typescript
type PrizeBoxStatus = 'AVAILABLE' | 'SHIPPING' | 'SHIPPED' | 'RECYCLED';
```

補充：
1. 舊資料可能仍有 IN_BOX，後端建單相容接受。
2. 前端顯示統一使用 AVAILABLE。

---

## 申請出貨（建立訂單）

POST /api/prize-box/ship

```typescript
interface PrizeBoxShipReq {
  prizeBoxIds: string[];
  shippingMethod: string;
  shippingMethodId?: string;
  shippingFee?: number;
  recipientName: string;
  recipientPhone: string;
  recipientAddress?: string;
  storeCode?: string;
  storeName?: string;
  storeAddress?: string;
}
```

注意：
1. 前端可一次送多店 prizeBoxIds，後端會按店家自動拆單。
2. 每筆訂單會回傳一筆支付初始化資訊。

回應：

```typescript
interface OrderPaymentInitRes {
  orderId: string;
  orderNumber: string;
  shippingFee: number;
  paymentStatus: string;
  paymentUrl: string;
  gatewayTradeNo: string;
}

type ShipResult = OrderPaymentInitRes[];
```

---

## 付款與後續狀態

1. 建單後初始為 PAYMENT_PENDING。
2. 付款成功後進入 PENDING。
3. 付款失敗進入 PAYMENT_FAILED，可在訂單頁重付款或取消。

---

## 狀態流轉

```text
抽獎成功
  -> AVAILABLE
      -> 出貨建立訂單 -> SHIPPING
          -> 訂單取消 -> AVAILABLE
          -> 店家出貨 -> SHIPPED
      -> 回收 -> RECYCLED
```

---

## UI 建議

1. 出貨前彈窗提示：可能拆單並產生多筆付款。
2. 建單回應為陣列時，逐筆顯示店家與付款入口。
3. 若任一筆付款失敗，導向訂單頁統一處理重付款。
