# Data Model: 訂單物流基礎

**Feature**: `021-order-logistics`
**Date**: 2026-04-13

---

## 新建 Entity: ShippingMethod (運送方式)

**Table**: `shipping_method`
**Package**: `com.group.admin.entity.ShippingMethod`

### DDL

```sql
CREATE TABLE `shipping_method` (
  `id`          VARCHAR(36)   NOT NULL PRIMARY KEY,
  `name`        VARCHAR(100)  NOT NULL COMMENT '運送方式名稱（如 7-11 取貨）',
  `code`        VARCHAR(50)   NOT NULL UNIQUE COMMENT '代碼（如 SEVEN_ELEVEN）',
  `provider`    VARCHAR(100)  NULL COMMENT '物流商名稱（如 綠界）',
  `fee`         BIGINT        NOT NULL DEFAULT 0 COMMENT '運費（分為單位）',
  `status`      VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE / INACTIVE',
  `sort_order`  INT           NOT NULL DEFAULT 0 COMMENT '排序',
  `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='運送方式管理表';
```

### 初始資料

```sql
INSERT INTO `shipping_method` (`id`, `name`, `code`, `provider`, `fee`, `status`, `sort_order`) VALUES
(UUID(), '宅配到府', 'HOME_DELIVERY', '黑貓宅急便', 100, 'ACTIVE', 1),
(UUID(), '7-11 取貨', 'SEVEN_ELEVEN', '綠界', 60, 'ACTIVE', 2),
(UUID(), '全家取貨', 'FAMILY_MART', '綠界', 60, 'ACTIVE', 3);
```

### Fields

| Field | Type | DB Column | Nullable | Description |
|-------|------|-----------|----------|-------------|
| `id` | `String` | `id` VARCHAR(36) PK | No | UUID |
| `name` | `String` | `name` VARCHAR(100) | No | 顯示名稱 |
| `code` | `String` | `code` VARCHAR(50) UNIQUE | No | 唯一代碼 |
| `provider` | `String` | `provider` VARCHAR(100) | Yes | 物流商 |
| `fee` | `Long` | `fee` BIGINT | No | 運費 |
| `status` | `String` | `status` VARCHAR(20) | No | ACTIVE / INACTIVE |
| `sortOrder` | `Integer` | `sort_order` INT | No | 排序 |
| `createdAt` | `LocalDateTime` | `created_at` | No | |
| `updatedAt` | `LocalDateTime` | `updated_at` | No | |

---

## Order 表變更

### 新增欄位

```sql
ALTER TABLE `order`
  ADD COLUMN `shipping_method_id` VARCHAR(36) NULL COMMENT 'FK → shipping_method' AFTER `shipping_address`,
  ADD COLUMN `payment_method` VARCHAR(30) NOT NULL DEFAULT 'STUB' COMMENT 'STUB / MASTERCARD / GOLD_COIN' AFTER `payment_status`,
  ADD COLUMN `shipping_fee` BIGINT NOT NULL DEFAULT 0 COMMENT '運費' AFTER `shipping_method_id`;
```

---

## DTO 設計

### ShippingMethodCreateReq

| Field | Type | Validation | Required |
|-------|------|------------|----------|
| `name` | String | `@NotBlank @Size(max=100)` | Yes |
| `code` | String | `@NotBlank @Size(max=50)`，唯一 | Yes |
| `provider` | String | `@Size(max=100)` | No |
| `fee` | Long | `@NotNull @Min(0)` | Yes |

### ShippingMethodRes

| Field | Type |
|-------|------|
| `id` | String |
| `name` | String |
| `code` | String |
| `provider` | String |
| `fee` | Long |
| `status` | String |

### OrderCreateReq（調整）

新增欄位：

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `shippingMethodId` | String | Yes | 選擇的運送方式 ID |
| `paymentMethod` | String | No | 支付方式（預設 STUB） |
| `recipientName` | String | Yes | 收件人姓名 |
| `recipientPhone` | String | Yes | 收件人電話 |
| `shippingAddress` | String | 宅配必填 | 完整地址 |
| `convenienceStoreType` | String | 超商必填 | SEVEN_ELEVEN / FAMILY_MART |
| `convenienceStoreName` | String | 超商必填 | 門市名稱 |
| `convenienceStoreCode` | String | 超商必填 | 門市代碼 |
| `remark` | String | No | 備註 |

---

## Stub 服務介面

### PaymentGatewayService

```java
public interface PaymentGatewayService {
    PaymentResult processPayment(String orderId, Long amount, String paymentMethod);
    PaymentResult queryPayment(String transactionId);
    PaymentResult refundPayment(String transactionId, Long amount);
}
```

### LogisticsService

```java
public interface LogisticsService {
    ShippingResult createShipment(String orderId, ShippingInfo info);
    ShippingResult queryShipment(String trackingNumber);
    List<ConvenienceStore> queryStores(String type, String city, String keyword);
}
```

---

## 同店驗證邏輯

```java
// OrderServiceImpl.createOrder():
Set<String> storeIds = prizeBoxes.stream()
    .map(PrizeBox::getStoreId)
    .collect(Collectors.toSet());

if (storeIds.size() > 1) {
    throw new BusinessException("不同店家的獎品請分開建立訂單");
}
```
