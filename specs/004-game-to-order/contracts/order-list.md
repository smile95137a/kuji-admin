# 合約：訂單清單與詳情 — 玩家訂單歷史

**功能**：`004-game-to-order`  
**Controller**：`com.group.admin.controller.api.OrderController`  
**認證**：必須 — `Authorization: Bearer <jwt>`

---

## 端點

| 方法 | 路徑 | 用途 |
|--------|------|---------|
| `POST` | `/order/list` | 列出玩家訂單（含可選篩選條件） |
| `GET` | `/order/{orderId}` | 取得單筆訂單的完整詳情 |

> 客戶端完整路徑：`POST /api/order/list`、`GET /api/order/{orderId}`

> **REST 慣例說明**：spec 原先提議 `GET /api/orders`。現有實作使用 `POST /api/order/list` 以支援基於請求體的豐富篩選功能（狀態、日期區間、店家）。輕量的 `GET /api/orders` 別名可作為可選增強功能新增 — 詳見本文件末尾的備註。

---

## 端點 1：POST /order/list

### 用途

回傳已認證玩家的訂單清單（可分頁或不分頁），支援依狀態、日期區間或店家進行可選篩選。

### 請求

#### Headers

| Header | 必填 | 範例 |
|--------|----------|---------|
| `Authorization` | 是 | `Bearer eyJhbGci...` |
| `Content-Type` | 是 | `application/json` |

#### 請求體（可選）

請求體為可選。傳送空物件 `{}` 或省略請求體可取得所有訂單。

```json
{
  "pageNum": 1,
  "pageSize": 20,
  "condition": {
    "status": "PENDING",
    "startDate": "2026-01-01T00:00:00",
    "endDate": "2026-12-31T23:59:59",
    "storeId": "s1234567-89ab-cdef-0123-456789abcdef"
  }
}
```

#### 請求欄位說明

| 欄位 | 類型 | 必填 | 說明 |
|-------|------|----------|-------------|
| `pageNum` | `integer` | 否 | 頁碼（從 1 開始）；預設為 1 |
| `pageSize` | `integer` | 否 | 每頁筆數；預設為系統設定值 |
| `condition.status` | `string` | 否 | 依 `OrderStatusEnum` 篩選：`PENDING` / `PREPARING` / `SHIPPED` / `COMPLETED` / `CANCELLED` |
| `condition.startDate` | `ISO-8601` | 否 | 此日期之後建立的訂單 |
| `condition.endDate` | `ISO-8601` | 否 | 此日期之前建立的訂單 |
| `condition.storeId` | `string (UUID)` | 否 | 依店家篩選 |

> **安全性**：Service 自動注入 `userId = currentUserId` — 玩家無法取得其他玩家的訂單。

### 回應 — 200 OK

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "ord-uuid-0001",
        "orderNo": "ORD20260322001",
        "userId": "u1234...",
        "storeId": "s1234...",
        "storeName": "台北旗艦店",
        "totalItems": 3,
        "shippingMethod": "HOME_DELIVERY",
        "shippingStatus": "PENDING",
        "shippingStatusName": "待處理",
        "recipientName": "王小明",
        "recipientPhone": "0912345678",
        "recipientAddress": "台北市信義區市府路1號",
        "trackingNo": null,
        "remark": "請輕放，謝謝",
        "createdAt": "2026-03-22T14:35:00",
        "shippedAt": null,
        "completedAt": null,
        "cancelledAt": null,
        "cancelReason": null,
        "items": [
          {
            "id": "item-uuid-001",
            "orderId": "ord-uuid-0001",
            "prizeBoxId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
            "lotteryTitle": "春節限定一番賞",
            "prizeName": "A賞 — 特製抱枕",
            "prizeLevel": "A",
            "prizeImageUrl": "https://s3.amazonaws.com/kuji/prizes/pillow-a.jpg",
            "createdAt": "2026-03-22T14:35:00"
          }
        ]
      }
    ],
    "total": 5,
    "pageNum": 1,
    "pageSize": 20
  }
}
```

### 訂單狀態值

| `shippingStatus` | `shippingStatusName` | 含義 |
|-----------------|---------------------|---------|
| `PENDING` | 待處理 | 訂單已建立，等待管理員處理 |
| `PREPARING` | 準備出貨中 | 管理員正在包裝訂單 |
| `SHIPPED` | 已出貨 | 物流已收到包裹 |
| `COMPLETED` | 已完成 | 玩家確認收貨 |
| `CANCELLED` | 已取消 | 訂單已取消；獎品返回 IN_BOX |

---

## 端點 2：GET /order/{orderId}

### 用途

回傳單筆訂單的完整詳情，包含所有訂單項目、配送資訊及時間戳記。

### 請求

#### 路徑參數

| 參數 | 類型 | 說明 |
|-------|------|-------------|
| `orderId` | `string (UUID)` | 來自訂單清單回應的訂單 ID |

#### Headers

| Header | 必填 |
|--------|----------|
| `Authorization` | 是 |

#### 請求體

無。

### 回應 — 200 OK

與上方清單中單筆項目的結構相同（完整的 `OrderDetailRes` 物件，含 `items` 陣列）。

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "ord-uuid-0001",
    "orderNo": "ORD20260322001",
    ...
    "items": [ ... ]
  }
}
```

### 回應 — 404 Not Found

```json
{
  "code": 404,
  "message": "訂單不存在",
  "data": null
}
```

### 回應 — 403 Forbidden

```json
{
  "code": 403,
  "message": "無權查看此訂單",
  "data": null
}
```

---

## 訂單取消流程（POST /order/cancel — 相關）

玩家取消訂單時：

1. 訂單 `status` → `CANCELLED`，設定 `cancelledAt` 和 `cancelReason`
2. 對此訂單關聯的每筆 `OrderItem`：
   - 依 `OrderItem.prizeBoxId` 取得 `PrizeBox`
   - 重置 `PrizeBox.status` → `IN_BOX`
   - 清除 `PrizeBox.orderId` → `null`
   - 清除 `PrizeBox.shippedAt` → `null`
3. 所有步驟在 `@Transactional` 內執行

**取消後不退還積分/錢包餘額**（依 2026-03-22 釐清結果）。

---

## 錯誤回應（共用）

| 代碼 | 訊息 | 觸發條件 |
|------|---------|---------|
| 401 | 未登入或 Token 已過期 | JWT 遺失或已過期 |
| 403 | 無權查看此訂單 | 訂單屬於其他使用者 |
| 404 | 訂單不存在 | 找不到訂單 ID |
| 500 | 系統錯誤，請稍後再試 | 非預期的伺服器錯誤 |

---

## 可選增強：GET /api/orders 別名

spec 原先提議用 `GET /api/orders` 作為訂單清單端點。此端點可作為簡單別名新增，支援基本的查詢參數篩選：

```
GET /api/orders?status=PENDING&page=1&size=20
```

這將是 `OrderController` 上的新 controller 方法，將查詢參數對應至 `QueryReq<OrderCondition>` 並委派給相同的 service 方法。**延後至 tasks.md 處理。**

---

## 實作參考

| 層級 | 類別 | 方法 |
|-------|-------|--------|
| Controller | `OrderController` | `getMyOrders(@RequestBody QueryReq<OrderCondition>)` |
| Controller | `OrderController` | `getOrderDetail(@PathVariable String orderId)` |
| Service | `OrderServiceImpl` | `getMyOrders(String userId, QueryReq<OrderCondition>)` |
| Service | `OrderServiceImpl` | `getOrderDetail(String userId, String orderId)` |
| Mapper | `OrderMapper` | `selectByExample(OrderExample)` |
| Mapper | `OrderItemMapper` | `selectByExample(OrderItemExample)` |
| DTO（回應） | `OrderDetailRes` | 回應體 |
| DTO（回應） | `OrderItemRes` | 巢狀項目 |
