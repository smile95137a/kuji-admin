# 快速入門：物流與出貨管理 (Express Shipping)

**功能分支**：`002-express-shipping`  
**產生日期**：2026-03-22

> **前置條件**：開始實作前，專案必須能正常建構（`mvn clean package -DskipTests`）。

---

## 功能說明

管理 KUJI 獎品配送的完整訂單履約生命週期：

1. **玩家**提交出貨資訊（宅配到府或超商取貨）
2. **店家負責人 / 管理員**推進訂單的出貨流程
3. **玩家**即時追蹤訂單狀態
4. **管理員**可在訂單出貨前取消

---

## 實作清單（依優先順序）

### P1 — 核心出貨流程

```
[ ] 1. Create ShipInfoReq DTO with conditional validation
[ ] 2. Add submitShippingInfo() method to OrderService interface
[ ] 3. Implement submitShippingInfo() in OrderServiceImpl
[ ] 4. Add POST /order/{orderId}/shipping-info endpoint in OrderController
[ ] 5. Write controller test: POST /order/{id}/shipping-info (OrderControllerTest)
[ ] 6. Write service unit test: state machine transitions (OrderServiceTest)
```

### P1 — 管理員狀態管理（已實作 — 需補測試）

```
[ ] 7. Write AdminOrderControllerTest for /prepare, /ship, /complete endpoints
[ ] 8. Write AdminOrderControllerTest for role-based cancel (ADMIN vs STORE_OWNER)
```

### P2 — 玩家訂單檢視（已實作 — 需補測試）

```
[ ] 9. Write OrderControllerTest for GET /order/{id} ownership check
[ ] 10. Write OrderControllerTest for POST /order/list isolation
```

### P3 — 取消（已實作 — 需補測試）

```
[ ] 11. Write AdminOrderControllerTest: cancel PENDING → 200
[ ] 12. Write AdminOrderControllerTest: cancel SHIPPED → 409
```

---

## 關鍵檔案

| 檔案 | 動作 | 優先級 |
|------|--------|----------|
| `req/order/ShipInfoReq.java` | **建立**新檔案 | P1 |
| `service/OrderService.java` | **新增** `submitShippingInfo()` 方法 | P1 |
| `service/impl/OrderServiceImpl.java` | **實作** `submitShippingInfo()` | P1 |
| `controller/api/OrderController.java` | **新增**出貨資訊端點 | P1 |
| `controller/api/OrderControllerTest.java` | **補充**（目前為空） | P1 |
| `controller/admin/AdminOrderControllerTest.java` | **補充**（目前為空） | P1 |
| `service/OrderServiceTest.java` | **建立**新測試檔案 | P1 |

**無需 DB 變更** — 所有 `Order` 與 `OrderStatusLog` 欄位均已存在。

---

## 建立 `ShipInfoReq.java`

```java
// src/main/java/com/group/admin/req/order/ShipInfoReq.java
package com.group.admin.req.order;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ShipInfoReq {
    @NotBlank(message = "出貨方式不可為空")
    private String shippingMethod;      // HOME_DELIVERY | SEVEN_ELEVEN | FAMILY_MART

    // Home delivery fields (required when shippingMethod = HOME_DELIVERY)
    private String recipientName;
    private String recipientPhone;
    private String recipientAddress;

    // Convenience store fields (required when shippingMethod = SEVEN_ELEVEN | FAMILY_MART)
    private String storeCode;
    private String storeName;
    private String storeAddress;

    private String remark;
}
```

---

## 新增玩家出貨資訊端點

```java
// In OrderController.java — add this method
@PostMapping("/{orderId}/shipping-info")
public Result<?> submitShippingInfo(
        @PathVariable String orderId,
        @Valid @RequestBody ShipInfoReq req) {
    String userId = SecurityUtils.getCurrentApiUserId();
    orderService.submitShippingInfo(orderId, req, userId);
    return Result.success("出貨資訊已更新");
}
```

---

## 在 Service 實作 `submitShippingInfo()`

```java
// Key logic in OrderServiceImpl
@Transactional
public void submitShippingInfo(String orderId, ShipInfoReq req, String userId) {
    Order order = orderMapper.selectByPrimaryKey(orderId);
    if (order == null) throw new BusinessException("訂單不存在");
    if (!order.getUserId().equals(userId)) throw new BusinessException("無權限操作此訂單");
    if (!OrderStatusEnum.PENDING.getCode().equals(order.getStatus()))
        throw new BusinessException("訂單已確認，無法修改出貨資訊");

    // Validate shipping method
    ShippingMethodEnum method = ShippingMethodEnum.fromCode(req.getShippingMethod());

    // Cross-field validation
    if (method == ShippingMethodEnum.HOME_DELIVERY) {
        if (isBlank(req.getRecipientName())) throw new BusinessException("宅配需填入收件人姓名");
        if (isBlank(req.getRecipientPhone())) throw new BusinessException("宅配需填入收件人電話");
        if (isBlank(req.getRecipientAddress())) throw new BusinessException("宅配需填入收件地址");
    } else {
        if (isBlank(req.getStoreCode())) throw new BusinessException("超商取貨需填入分店代碼");
        if (isBlank(req.getStoreName())) throw new BusinessException("超商取貨需填入分店名稱");
    }

    // Build update
    Order update = new Order();
    update.setId(orderId);
    update.setShippingMethod(req.getShippingMethod());
    update.setRecipientName(req.getRecipientName());
    update.setRecipientPhone(req.getRecipientPhone());
    update.setRecipientAddress(req.getRecipientAddress());
    update.setStoreCode(req.getStoreCode());
    update.setStoreName(req.getStoreName());
    update.setStoreAddress(req.getStoreAddress());
    update.setRemark(req.getRemark());
    update.setUpdatedAt(LocalDateTime.now());
    orderMapper.updateByPrimaryKeySelective(update);
}
```

---

## 狀態機快速參考

```
PENDING  ──[/prepare]──►  PREPARING  ──[/ship + trackingNo]──►  SHIPPED  ──[/complete]──►  COMPLETED
   │                           │
   └──[/cancel ADMIN only]──►  CANCELLED ◄── (from PREPARING too)

Reverse transitions → always 409 Conflict
Cancel after SHIPPED → always 409 Conflict
Player calling /cancel → 403 Forbidden
```

---

## 執行測試

```bash
# All tests
mvn test

# Only order-related tests
mvn test -Dtest="OrderController*,AdminOrderController*,OrderService*"

# Single test class
mvn test -Dtest="OrderControllerTest"
```

---

## Postman / API 快速測試

### 1. 玩家 — 提交出貨資訊
```
POST http://localhost:8080/order/{orderId}/shipping-info
Authorization: Bearer <user-token>
Content-Type: application/json

{
  "shippingMethod": "HOME_DELIVERY",
  "recipientName": "王小明",
  "recipientPhone": "0912345678",
  "recipientAddress": "台北市信義區信義路五段7號"
}
```

### 2. 管理員 — 標記為備貨中
```
PUT http://localhost:8080/admin/orders/{orderId}/prepare
Authorization: Bearer <admin-token>
```

### 3. 管理員 — 出貨並填入物流單號
```
PUT http://localhost:8080/admin/orders/{orderId}/ship
Authorization: Bearer <admin-token>
Content-Type: application/json

{"trackingNo": "1234567890", "remark": "黑貓宅急便"}
```

### 4. 玩家 — 查詢訂單狀態
```
GET http://localhost:8080/order/{orderId}
Authorization: Bearer <user-token>
```

### 5. 管理員 — 取消訂單（僅 ADMIN）
```
PUT http://localhost:8080/admin/orders/{orderId}/cancel
Authorization: Bearer <admin-token>
Content-Type: application/json

{"reason": "顧客要求取消"}
```

---

## 參考文件

| 文件 | 用途 |
|----------|---------|
| [spec.md](./spec.md) | 功能需求與驗收條件 |
| [research.md](./research.md) | 架構決策與已解析的未知項目 |
| [data-model.md](./data-model.md) | 實體綱要與狀態機詳情 |
| [contracts/player-submit-shipping.md](./contracts/player-submit-shipping.md) | 玩家出貨資訊端點 |
| [contracts/admin-update-status.md](./contracts/admin-update-status.md) | 管理員狀態轉換端點 |
| [contracts/player-get-orders.md](./contracts/player-get-orders.md) | 玩家訂單清單與詳情端點 |
| [contracts/admin-cancel-order.md](./contracts/admin-cancel-order.md) | 管理員取消端點 |
