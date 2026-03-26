# 管理端訂單 API 契約

**功能**：訂單管理 (Order Management)  
**分支**：`008-order-management`  
**基礎路徑**：`/admin/orders`  
**驗證**：所有 endpoints 皆需 JWT Bearer token  
**角色**：`ADMIN`（所有店家）· `STORE_OWNER` / `STORE_EDITOR`（僅限自己的店家）

---

## 通用模式

### 驗證與授權
```
Authorization: Bearer <jwt-token>
```

### 店家隔離規則
- `ADMIN` 角色：可查看所有訂單；接受可選的 `storeId` 篩選參數
- `STORE_OWNER` / `STORE_EDITOR`：伺服器始終套用 `storeId = 呼叫者的店家`；用戶端提供的 `storeId` 參數將被靜默覆蓋

### 標準錯誤回應

| HTTP 狀態 | 代碼 | 情境 |
|-----------|------|------|
| 400 | `VALIDATION_ERROR` | 無效的請求主體 |
| 401 | `UNAUTHORIZED` | 缺少或無效的 JWT |
| 403 | `FORBIDDEN` | 角色／店家存取被拒 |
| 404 | `NOT_FOUND` | 找不到訂單 ID |
| 409 | `INVALID_STATE_TRANSITION` | 不允許的狀態轉換 |
| 422 | `STORE_INACTIVE` | 目標店家已停用 |

---

## GET /admin/orders

取得分頁的訂單列表。店家負責人僅能看到自己店家的訂單。

### 請求

```
POST /admin/orders/list
Content-Type: application/json
Authorization: Bearer <token>
```

> 遵循現有程式碼庫模式：列表查詢使用 `POST` 搭配 `QueryReq` 主體。

```jsonc
{
  "page": 1,            // default: 1
  "pageSize": 20,       // default: 20, max: 100
  "sortBy": "createdAt",
  "sortDirection": "DESC",
  "condition": {
    "storeId": "uuid",           // ADMIN only; ignored for STORE_OWNER
    "userId": "uuid",            // optional: filter by player
    "status": "PENDING",         // optional: PENDING|PREPARING|SHIPPED|COMPLETED|CANCELLED
    "shippingMethod": "HOME_DELIVERY", // optional
    "orderNo": "ORD-...",        // optional: exact match
    "startDate": "2026-01-01T00:00:00",
    "endDate": "2026-12-31T23:59:59"
  }
}
```

### 回應——200 OK
    "totalItems": 3,
    "shippingMethod": "HOME_DELIVERY",
    "shippingMethodLabel": "宅配到府",
    "storeName": "夢幻扭蛋",
    "storeId": "uuid-store",
    "playerName": "玩家小明",
    "playerPhone": "0912345678",
    "recipientName": "王小明",
    "createdAt": "2026-03-22T14:30:00"
  }
]
```

---

## GET /admin/orders/{id}

取得訂單的完整詳情，包含項目及狀態歷程。

### 請求

```
GET /admin/orders/{id}
Authorization: Bearer <token>
```

### 回應——200 OK

```jsonc
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "orderNo": "ORD-20260322143000-AB1C2D",
  "status": "PREPARING",
  "statusLabel": "備貨中",
  "totalItems": 2,
  "shippingMethod": "HOME_DELIVERY",
  "shippingMethodLabel": "宅配到府",
  "storeId": "uuid-store",
  "storeName": "夢幻扭蛋",
  "userId": "uuid-player",
  "playerName": "玩家小明",
  "playerPhone": "0912345678",
  "recipientName": "王小明",
  "recipientPhone": "0912345678",
  "recipientAddress": "台北市信義區信義路五段7號",
  "storeCode": null,
  "storeName_pickup": null,
  "storeAddress_pickup": null,
  "trackingNo": null,
  "remark": null,
  "createdAt": "2026-03-22T14:30:00",
  "updatedAt": "2026-03-22T15:00:00",
  "shippedAt": null,
  "completedAt": null,
  "cancelledAt": null,
  "cancelledBy": null,
  "cancelReason": null,
  "items": [
    {
      "id": "item-uuid-1",
      "prizeBoxId": "box-uuid-1",
      "prizeName": "景品 A：限定版公仔",
      "prizeImageUrl": "https://cdn.example.com/prizes/a.jpg",
      "prizeLevel": "A",
      "lotteryTitle": "春節一番賞",
      "lotteryImageUrl": "https://cdn.example.com/lottery/spring.jpg",
      "lotteryId": "lottery-uuid-1",
      "prizeId": "prize-uuid-1"
    },
    {
      "id": "item-uuid-2",
      "prizeBoxId": "box-uuid-2",
      "prizeName": "景品 B：壓克力立牌",
      "prizeImageUrl": "https://cdn.example.com/prizes/b.jpg",
      "prizeLevel": "B",
      "lotteryTitle": "春節一番賞",
      "lotteryImageUrl": "https://cdn.example.com/lottery/spring.jpg",
      "lotteryId": "lottery-uuid-1",
      "prizeId": "prize-uuid-2"
    }
  ],
  "statusHistory": [
    {
      "fromStatus": null,
      "fromStatusLabel": null,
      "toStatus": "PENDING",
      "toStatusLabel": "待處理",
      "operatorId": "player-uuid",
      "operatorType": "PLAYER",
      "remark": "訂單建立",
      "createdAt": "2026-03-22T14:30:00"
    },
    {
      "fromStatus": "PENDING",
      "fromStatusLabel": "待處理",
      "toStatus": "PREPARING",
      "toStatusLabel": "備貨中",
      "operatorId": "admin-uuid",
      "operatorType": "STORE_OWNER",
      "remark": null,
      "createdAt": "2026-03-22T15:00:00"
    }
  ]
}
```

### 回應——403 Forbidden
當 `STORE_OWNER`／`STORE_EDITOR` 嘗試存取屬於其他店家的訂單時回傳。

```jsonc
{
  "code": "FORBIDDEN",
  "message": "您沒有權限存取此訂單"
}
```

---

## PUT /admin/orders/{id}/status

將訂單推進至狀態機中的下一個狀態。每次只能前進一步。

### 請求

```
PUT /admin/orders/{id}/status
Content-Type: application/json
Authorization: Bearer <token>
```

```jsonc
{
  "targetStatus": "PREPARING",  // Must be exactly the next state
  "trackingNo": "799123456789", // Required when targetStatus = SHIPPED
  "remark": "已確認庫存齊全"    // Optional; stored in status log
}
```

**有效的 `targetStatus` 值**：`PREPARING` · `SHIPPED` · `COMPLETED`  
（取消請使用 `DELETE /admin/orders/{id}`）

### 回應——200 OK

```jsonc
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "orderNo": "ORD-20260322143000-AB1C2D",
  "status": "PREPARING",
  "statusLabel": "備貨中",
  "updatedAt": "2026-03-22T15:00:00"
}
```

### 回應——409 Conflict（無效轉換）

```jsonc
{
  "code": "INVALID_STATE_TRANSITION",
  "message": "訂單狀態無法從 SHIPPED 退回至 PREPARING",
  "currentStatus": "SHIPPED",
  "requestedStatus": "PREPARING"
}
```

### 回應——409 Conflict（相同狀態／冪等）

當相同的狀態被提交兩次時，回傳 200（不報錯）並附帶未變更的訂單狀態——此操作視為成功的無操作。

---

## DELETE /admin/orders/{id}

取消訂單。僅允許在狀態為 `PENDING` 或 `PREPARING` 時執行。獎品退回玩家的獎品盒。點數**不**退還。

### 請求

```
DELETE /admin/orders/{id}
Content-Type: application/json
Authorization: Bearer <token>
```

```jsonc
{
  "cancelReason": "顧客要求取消"  // Optional; max 500 chars
}
```

### 回應——200 OK
  "statusLabel": "已取消",
  "cancelledAt": "2026-03-22T16:00:00",
  "cancelledBy": "admin-uuid",
  "cancelReason": "顧客要求取消",
  "itemsReturnedToBox": 2
}
```

### 回應——409 Conflict（已出貨）

```jsonc
{
  "code": "CANCEL_NOT_ALLOWED",
  "message": "訂單已出貨，無法取消",
  "currentStatus": "SHIPPED"
}
```

### 回應——403 Forbidden

當 `STORE_OWNER`／`STORE_EDITOR` 嘗試取消屬於其他店家的訂單，或 `PLAYER` 角色嘗試呼叫此 endpoint 時回傳。

---

## 備註

### 狀態標籤對應

| 代碼 | 中文標籤 |
|------|---------|
| `PENDING` | 待處理 |
| `PREPARING` | 備貨中 |
| `SHIPPED` | 已出貨 |
| `COMPLETED` | 已完成 |
| `CANCELLED` | 已取消 |

### 運送方式標籤對應

| 代碼 | 中文標籤 |
|------|---------|
| `HOME_DELIVERY` | 宅配到府 |
| `SEVEN_ELEVEN` | 7-ELEVEN 超商取貨 |
| `FAMILY_MART` | 全家超商取貨 |
