# Data Model：訂單物流與運費付款

Feature：021-order-logistics  
Date：2026-05-11

---

## 1) ShippingMethod

Table：shipping_method

```sql
CREATE TABLE shipping_method (
  id VARCHAR(36) NOT NULL PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  code VARCHAR(50) NOT NULL UNIQUE,
  provider VARCHAR(100) NULL,
  fee BIGINT NOT NULL DEFAULT 0,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

主要欄位：
1. code：HOME_DELIVERY / SEVEN_ELEVEN / FAMILY_MART
2. fee：運費（元）
3. status：ACTIVE / INACTIVE

---

## 2) Order（物流付款相關欄位）

既有 order 表中與物流付款相關欄位：
1. shipping_method_id
2. shipping_method
3. shipping_fee
4. payment_status（PAYMENT_PENDING / PAID / FAILED）
5. payment_method（目前主流為 GOMYPAY）
6. gomypay_trade_no
7. tracking_no

主狀態（status）與付款狀態分離：
1. status：PAYMENT_PENDING / PAYMENT_FAILED / PENDING / PREPARING / SHIPPED / COMPLETED / CANCELLED
2. payment_status：PAYMENT_PENDING / PAID / FAILED

---

## 3) 前台建單與重付款回應模型

### OrderPaymentInitRes

```java
class OrderPaymentInitRes {
  String orderId;
  String orderNumber;
  Long shippingFee;
  String paymentStatus;
  String paymentUrl;
  String gatewayTradeNo;
}
```

用途：
1. /order/ship：建單後返回支付初始化資訊。
2. /order/{orderId}/repay：重付款返回新的支付初始化資訊。

---

## 4) callback 模型

### ShippingCallbackResult

關鍵欄位：
1. orderNumber
2. gatewayTradeNo
3. success
4. errorMessage

callback 狀態寫回規則：
1. success -> payment_status = PAID，status -> PENDING
2. failed -> payment_status = FAILED，status -> PAYMENT_FAILED

---

## 5) PrizeBox 關聯規則

建單後：
1. PrizeBox.status -> SHIPPING
2. PrizeBox.order_id -> 綁定對應訂單

取消後：
1. PrizeBox.status -> AVAILABLE
2. PrizeBox.order_id -> null

相容規則：
1. 建單驗證接受 AVAILABLE 與舊值 IN_BOX。
