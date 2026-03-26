# 合約：玩家提交出貨資訊

**端點**：`POST /order/{orderId}/shipping-info`  
**功能**：002-express-shipping  
**執行角色**：玩家（USER 角色）  
**使用者故事**：US1 — 玩家選擇出貨方式並送出訂單

---

## 概述

允許玩家在訂單仍處於 `PENDING` 狀態時，提交或更新自己訂單的出貨資訊。一旦訂單進入 `PENDING` 之後的狀態，此端點將拒絕進一步的變更（FR-005）。

---

## 認證與授權

| 需求 | 詳情 |
|-------------|--------|
| 認證類型 | JWT Bearer token（ApiJwtAuthenticationFilter） |
| 必要角色 | `USER` |
| 所有權檢查 | `order.user_id` 必須與已認證使用者的 ID 相符 |

---

## 請求

### 路徑參數

| 參數 | 型別 | 必填 | 說明 |
|-----------|------|----------|-------------|
| `orderId` | `String` (UUID) | 是 | 要附加出貨資訊的訂單 |

### 標頭

```
Authorization: Bearer <jwt-token>
Content-Type: application/json
```

### 請求本體：`ShipInfoReq`

```json
{
  "shippingMethod": "HOME_DELIVERY",
  "recipientName": "王小明",
  "recipientPhone": "0912345678",
  "recipientAddress": "台北市信義區信義路五段7號",
  "storeCode": null,
  "storeName": null,
  "storeAddress": null,
  "remark": "請放置門口"
}
```

**替代範例 — 超商取貨：**

```json
{
  "shippingMethod": "SEVEN_ELEVEN",
  "recipientName": null,
  "recipientPhone": null,
  "recipientAddress": null,
  "storeCode": "ABC123",
  "storeName": "統一超商信義門市",
  "storeAddress": "台北市信義區信義路一段100號",
  "remark": null
}
```

### 欄位驗證規則

| 欄位 | 限制條件 | 錯誤訊息 |
|-------|-----------|--------------|
| `shippingMethod` | `@NotBlank` + 有效的 `ShippingMethodEnum` 代碼 | `出貨方式不可為空` / `無效的出貨方式` |
| `recipientName` | `HOME_DELIVERY` 時必填 | `宅配需填入收件人姓名` |
| `recipientPhone` | `HOME_DELIVERY` 時必填 | `宅配需填入收件人電話` |
| `recipientAddress` | `HOME_DELIVERY` 時必填 | `宅配需填入收件地址` |
| `storeCode` | `SEVEN_ELEVEN` 或 `FAMILY_MART` 時必填 | `超商取貨需填入分店代碼` |
| `storeName` | `SEVEN_ELEVEN` 或 `FAMILY_MART` 時必填 | `超商取貨需填入分店名稱` |

---

## 回應

### 200 OK — 成功

```json
{
  "code": 200,
  "message": "出貨資訊已更新",
  "data": null
}
```

### 400 Bad Request — 驗證錯誤

```json
{
  "code": 400,
  "message": "宅配需填入收件人姓名",
  "data": null
}
```

### 403 Forbidden — 非訂單所有者

```json
{
  "code": 403,
  "message": "無權限操作此訂單",
  "data": null
}
```

### 404 Not Found — 訂單不存在

```json
{
  "code": 404,
  "message": "訂單不存在",
  "data": null
}
```

### 409 Conflict — 訂單狀態非 PENDING

```json
{
  "code": 409,
  "message": "訂單已確認，無法修改出貨資訊",
  "data": null
}
```

---

## 業務規則

1. 只有訂單所有者（已認證的 USER）可呼叫此端點。
2. 僅在 `order.status == PENDING` 時接受更新。
3. 成功呼叫後，將更新 `order.shipping_method`、對應的收件人 / 超商欄位，以及 `order.updated_at`。
4. 不會建立 `OrderStatusLog` 條目（此操作非狀態轉換）。
5. 表單的地址預填由客戶端負責，使用 `GET /user/me` 資料。

---

## 實作備註

- **控制器**：`OrderController.java` → 新增 `@PostMapping("/{orderId}/shipping-info")`
- **服務**：`OrderService.submitShippingInfo(String orderId, ShipInfoReq req, String userId)`
- **DTO**：建立 `com.group.admin.req.order.ShipInfoReq`
- **驗證**：在 `OrderServiceImpl` 實作跨欄位條件式驗證（對 `shippingMethod` 值使用明確的 if/else）
