# 合約：玩家查詢訂單

**端點**：
- `POST /order/list` — 取得我的訂單（分頁清單）
- `GET /order/{orderId}` — 取得訂單詳情

**功能**：002-express-shipping  
**執行角色**：玩家（USER 角色）  
**使用者故事**：US3 — 玩家查看訂單出貨狀態

---

## 概述

玩家可查看自己的訂單並追蹤出貨狀態。清單端點支援篩選與分頁；詳情端點回傳完整的出貨與項目資訊。玩家只能看到自己的訂單 — 伺服器端所有權強制執行為必要條件。

---

## 認證與授權

| 需求 | 詳情 |
|-------------|--------|
| 認證類型 | JWT Bearer token（ApiJwtAuthenticationFilter） |
| 必要角色 | `USER` |
| 所有權強制執行 | `userId` 篩選條件在伺服器端一律覆寫為已認證使用者的 ID |

---

## 端點一：取得我的訂單

### `POST /order/list`

回傳已認證玩家的分頁訂單清單，依 `created_at` 降序排列。

#### 請求本體（可選）

```json
{
  "page": 1,
  "pageSize": 10,
  "condition": {
    "status": "SHIPPED"
  }
}
```

若省略本體或傳入空本體，則回傳當前使用者的所有訂單。

| 欄位 | 型別 | 說明 |
|-------|------|-------------|
| `page` | Integer | 頁碼（從 1 開始），預設：1 |
| `pageSize` | Integer | 每頁筆數，預設：10，最大：50 |
| `condition.status` | String | 依 `OrderStatusEnum` 代碼篩選（可選） |
| `condition.shippingMethod` | String | 依 `ShippingMethodEnum` 代碼篩選（可選） |

#### 回應 — 200 OK

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 3,
    "page": 1,
    "pageSize": 10,
    "list": [
      {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "orderNo": "ORD20260322001",
        "storeId": "store-uuid",
        "storeName": "KUJI 台北門市",
        "totalItems": 2,
        "shippingMethod": "HOME_DELIVERY",
        "shippingMethodName": "宅配到府",
        "shippingStatus": "SHIPPED",
        "shippingStatusName": "已出貨",
        "recipientName": "王小明",
        "recipientPhone": "0912345678",
        "trackingNo": "1234567890",
        "totalAmount": 500,
        "paymentMethod": "GOLD",
        "createdAt": "2026-03-22T10:00:00",
        "shippedAt": "2026-03-23T14:30:00",
        "completedAt": null
      }
    ]
  }
}
```

**備註**：`userId` / `userEmail` **不**包含於玩家端清單回應中（隱私保護；這些欄位僅出現於管理員回應中）。

---

## 端點二：取得訂單詳情

### `GET /order/{orderId}`

回傳完整的訂單詳情，包含所有出貨資訊與獎品項目。

#### 路徑參數

| 參數 | 型別 | 必填 | 說明 |
|-----------|------|----------|-------------|
| `orderId` | String (UUID) | 是 | 要取得的訂單 |

#### 請求

```
GET /order/550e8400-e29b-41d4-a716-446655440000
Authorization: Bearer <user-jwt>
```

#### 回應 — 200 OK

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "orderNo": "ORD20260322001",
    "storeId": "store-uuid",
    "storeName": "KUJI 台北門市",
    "totalItems": 2,
    "shippingMethod": "HOME_DELIVERY",
    "shippingMethodName": "宅配到府",
    "shippingStatus": "SHIPPED",
    "shippingStatusName": "已出貨",
    "recipientName": "王小明",
    "recipientPhone": "0912345678",
    "recipientAddress": "台北市信義區信義路五段7號",
    "storeCode": null,
    "storeAddress": null,
    "trackingNo": "1234567890",
    "remark": "請放置門口",
    "items": [
      {
        "id": "item-uuid-1",
        "orderId": "550e8400-e29b-41d4-a716-446655440000",
        "prizeBoxId": "prize-box-uuid",
        "lotteryId": "lottery-uuid",
        "lotteryTitle": "一番賞 進擊的巨人",
        "lotteryImageUrl": "https://...",
        "prizeId": "prize-uuid",
        "prizeName": "A 賞 立體眼鏡",
        "prizeImageUrl": "https://...",
        "prizeLevel": "A",
        "createdAt": "2026-03-22T10:00:00"
      }
    ],
    "subtotal": 480,
    "shippingFee": 60,
    "discount": 40,
    "totalAmount": 500,
    "paymentMethod": "GOLD",
    "createdAt": "2026-03-22T10:00:00",
    "updatedAt": "2026-03-23T14:30:00",
    "shippedAt": "2026-03-23T14:30:00",
    "completedAt": null,
    "cancelledAt": null,
    "cancelledBy": null,
    "cancelReason": null
  }
}
```

#### 回應 — 403 Forbidden（訂單屬於其他使用者）

```json
{
  "code": 403,
  "message": "無權限查看此訂單",
  "data": null
}
```

#### 回應 — 404 Not Found

```json
{
  "code": 404,
  "message": "訂單不存在",
  "data": null
}
```

---

## 業務規則

1. 伺服器一律將已認證的 `userId` 注入為篩選條件 — 玩家無法透過操控 `condition` 來查詢其他使用者的訂單。
2. `shippingStatus` 反映即時的訂單狀態；客戶端應在輪詢後或使用者返回訂單清單時重新取得（SC-002、SC-003）。
3. 已取消的訂單包含在清單中（`shippingStatus = CANCELLED`）。
4. `OrderDetailRes` 中的超商 `storeName` 欄位在內部對應至 `storeName2`，以避免與 `storeName`（店家名稱）欄位衝突 — 對前端揭露的 JSON key：超商分店使用 `storeName`，KUJI 店家使用 `shopName`（或同等名稱）。

---

## 實作備註

- **控制器**：`OrderController.java` — 兩個端點均已實作。
- **服務**：`OrderService.getOrders()` 與 `getOrderDetail()` — 均已實作。
- **測試**：`OrderControllerTest.java` — 目前為空；必須補充以下測試：
  - `GET /order/{id}` 對所有者回傳 200
  - `GET /order/{id}` 對非所有者回傳 403
  - `POST /order/list` 僅回傳當前使用者的訂單
