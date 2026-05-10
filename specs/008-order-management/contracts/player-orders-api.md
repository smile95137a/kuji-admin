# 玩家訂單 API 契約

功能：玩家端訂單管理  
基礎路徑：/order  
驗證：JWT Bearer Token（USER）

## API 一覽

1. POST /order/ship：從賞品盒建立訂單（按店家自動拆單）
2. POST /order/list：查詢我的訂單列表
3. GET /order/{orderId}：查詢我的訂單詳情
4. POST /order/{orderId}/shipping-info：更新收件資訊
5. POST /order/{orderId}/repay：運費重付款
6. DELETE /order/{orderId}/cancel：玩家取消訂單

## 角色與隔離

1. 所有查詢與操作都限定當前登入玩家。
2. 非本人訂單回傳 403。

## 狀態定義

1. PAYMENT_PENDING：待付款
2. PAYMENT_FAILED：付款失敗
3. PENDING：待店家處理
4. PREPARING：備貨中
5. SHIPPED：已出貨
6. COMPLETED：已完成
7. CANCELLED：已取消

---

## 1) 建立訂單

POST /order/ship

```json
{
  "prizeBoxIds": ["box-1", "box-2"],
  "shippingMethod": "HOME_DELIVERY",
  "shippingMethodId": "method-uuid",
  "shippingFee": 60,
  "recipientName": "王小明",
  "recipientPhone": "0912345678",
  "recipientAddress": "台北市信義區...",
  "storeCode": null,
  "storeName": null,
  "storeAddress": null
}
```

回傳：List<OrderPaymentInitRes>

```json
[
  {
    "orderId": "order-1",
    "orderNumber": "ORD20260511...",
    "shippingFee": 60,
    "paymentStatus": "PAYMENT_PENDING",
    "paymentUrl": "https://...gomypay...",
    "gatewayTradeNo": "GMP..."
  }
]
```

規則：
1. 後端會驗證 prizeBox 所有權。
2. prizeBox 狀態允許 AVAILABLE 與舊值 IN_BOX（相容）。
3. 可跨店提交，後端按店家拆單回傳多筆結果。

---

## 2) 查詢列表

POST /order/list

```json
{
  "condition": {
    "status": "PENDING",
    "orderNo": "ORD",
    "shippingMethod": "HOME_DELIVERY",
    "createdAtStart": "2026-05-01T00:00:00",
    "createdAtEnd": "2026-05-31T23:59:59"
  },
  "page": 1,
  "size": 20,
  "sortBy": "created_at",
  "sortOrder": "DESC"
}
```

回傳：PageResult<OrderRes>

---

## 3) 查詢詳情

GET /order/{orderId}

回傳：OrderDetailRes（包含 items 與 statusHistory）

---

## 4) 更新收件資訊

POST /order/{orderId}/shipping-info

```json
{
  "shippingMethod": "HOME_DELIVERY",
  "recipientName": "王小明",
  "recipientPhone": "0912345678",
  "recipientAddress": "台北市...",
  "storeCode": null,
  "storeName": null,
  "storeAddress": null,
  "remark": "請白天配送"
}
```

規則：
1. 僅 PAYMENT_PENDING 可修改。
2. HOME_DELIVERY 必填 recipientName/recipientPhone/recipientAddress。
3. SEVEN_ELEVEN / FAMILY_MART 必填 storeCode/storeName。

---

## 5) 運費重付款

POST /order/{orderId}/repay

回傳：OrderPaymentInitRes

規則：
1. 僅 PAYMENT_PENDING / PAYMENT_FAILED 可重付款。
2. 成功後回傳新的 paymentUrl 與 gatewayTradeNo。

---

## 6) 玩家取消訂單

DELETE /order/{orderId}/cancel

```json
{
  "cancelReason": "不需要了"
}
```

規則：
1. 玩家可取消狀態：PAYMENT_PENDING / PAYMENT_FAILED / PENDING。
2. 取消後 PrizeBox 回 AVAILABLE，解除 orderId。
3. 已 SHIPPED / COMPLETED 不可取消。

---

## 付款回調語意（系統行為）

1. callback success：PAYMENT_PENDING 或 PAYMENT_FAILED -> PENDING。
2. callback failed：轉為 PAYMENT_FAILED。
3. PAYMENT_FAILED 狀態下可選擇重付款或取消。
