# 玩家訂單 API 契約

**功能**：訂單管理 (Order Management)  
**分支**：`008-order-management`  
**基礎路徑**：`/order`（現有前台 API 命名空間）  
**驗證**：所有 endpoints 皆需 JWT Bearer token  
**角色**：任何已驗證的玩家（`USER` 角色）

---

## 通用模式

### 驗證
```
Authorization: Bearer <jwt-token>
```

### 玩家隔離規則
所有 endpoints 自動將結果範圍限定於**當前已驗證的玩家**（`SecurityUtils.getCurrentUserId()`）。玩家無法存取其他玩家的訂單。

### 標準錯誤回應

| HTTP 狀態 | 代碼 | 情境 |
|-----------|------|------|
| 400 | `VALIDATION_ERROR` | 無效的請求主體 |
| 401 | `UNAUTHORIZED` | 缺少或無效的 JWT |
| 403 | `FORBIDDEN` | 嘗試存取其他玩家的訂單 |
| 404 | `NOT_FOUND` | 找不到獎品盒 ID 或訂單 |
| 409 | `PRIZE_BOX_UNAVAILABLE` | 獎品盒已出貨或已回收 |
| 422 | `STORE_INACTIVE` | 目標店家已停用 |

---

## POST /order/ship

從選取的獎品盒項目建立出貨訂單。若項目跨越多家店家，系統自動為每家店建立一筆訂單。

### 請求

```
POST /order/ship
Content-Type: application/json
Authorization: Bearer <token>
```

```jsonc
{
  "prizeBoxIds": [
    "box-uuid-1",
    "box-uuid-2",
    "box-uuid-3"
  ],
  "shippingMethod": "HOME_DELIVERY",   // HOME_DELIVERY | SEVEN_ELEVEN | FAMILY_MART
  "recipientName": "王小明",
  "recipientPhone": "0912345678",

  // Required when shippingMethod = HOME_DELIVERY
  "recipientAddress": "台北市信義區信義路五段7號",

  // Required when shippingMethod = SEVEN_ELEVEN or FAMILY_MART
  "storeCode": null,
  "storeName": null,
  "storeAddress": null
}
```

**驗證規則**：
- `prizeBoxIds`：非空，所有項目必須屬於呼叫者，且 `status = IN_BOX`
- `shippingMethod`：三個有效代碼之一
- `recipientName`：1–100 字元
- `recipientPhone`：有效的台灣電話號碼
- 各運送方式的條件地址欄位（見上方）
- 所有目標店家必須為 `ACTIVE`

### 回應——201 Created

回傳已建立的訂單 ID 清單（每個店家分組一筆）。

```jsonc
{
  "orderIds": [
    "550e8400-e29b-41d4-a716-446655440000",
    "660e9511-f30c-52e5-b827-557766551111"
  ],
  "orderCount": 2,
  "message": "已建立 2 筆訂單（依店家拆單）"
}
```

> **備註**：若所有獎品盒屬於同一家店，`orderCount` = 1，`orderIds` 包含一個元素。

### 回應——409 Conflict（獎品盒不可用）

```jsonc
{
  "code": "PRIZE_BOX_UNAVAILABLE",
  "message": "部分賞品盒已出貨或已回收，無法建立訂單",
  "unavailableBoxIds": ["box-uuid-2"]
}
```

### 回應——422 Unprocessable Entity（店家已停用）

```jsonc
{
  "code": "STORE_INACTIVE",
  "message": "店家已停用，無法建立新訂單",
  "storeId": "uuid-store"
}
```

---

## POST /order/list

取得已驗證玩家自己的訂單（分頁）。

### 請求

```
POST /order/list
Content-Type: application/json
Authorization: Bearer <token>
```

```jsonc
{
  "page": 1,
  "pageSize": 20,
  "sortBy": "createdAt",
  "sortDirection": "DESC",
  "condition": {
    "status": "PENDING",             // optional filter
    "shippingMethod": "HOME_DELIVERY" // optional filter
  }
}
```

> condition 中的 `userId` **始終由伺服器**以已驗證玩家的 ID 覆蓋。用戶端不應傳入此欄位。

### 回應——200 OK

```jsonc
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "orderNo": "ORD-20260322143000-AB1C2D",
    "status": "PREPARING",
    "statusLabel": "備貨中",
    "totalItems": 2,
    "shippingMethod": "HOME_DELIVERY",
    "shippingMethodLabel": "宅配到府",
    "storeName": "夢幻扭蛋",
    "recipientName": "王小明",
    "createdAt": "2026-03-22T14:30:00"
  }
]
```

---

## GET /order/{orderId}

取得單筆訂單的完整詳情。玩家只能存取自己的訂單。

### 請求

```
GET /order/{orderId}
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
  "recipientName": "王小明",
  "recipientPhone": "0912345678",
  "recipientAddress": "台北市信義區信義路五段7號",
  "storeCode": null,
  "storeName_pickup": null,
  "storeAddress_pickup": null,
  "trackingNo": null,
  "createdAt": "2026-03-22T14:30:00",
  "updatedAt": "2026-03-22T15:00:00",
  "shippedAt": null,
  "completedAt": null,
  "cancelledAt": null,
  "cancelReason": null,
  "items": [
    {
      "id": "item-uuid-1",
      "prizeName": "景品 A：限定版公仔",
      "prizeImageUrl": "https://cdn.example.com/prizes/a.jpg",
      "prizeLevel": "A",
      "lotteryTitle": "春節一番賞",
      "lotteryImageUrl": "https://cdn.example.com/lottery/spring.jpg"
    }
  ],
  "statusHistory": [
    {
      "toStatus": "PENDING",
      "toStatusLabel": "待處理",
      "operatorType": "PLAYER",
      "remark": "訂單建立",
      "createdAt": "2026-03-22T14:30:00"
    },
    {
      "fromStatus": "PENDING",
      "fromStatusLabel": "待處理",
      "toStatus": "PREPARING",
      "toStatusLabel": "備貨中",
      "operatorType": "STORE_OWNER",
      "remark": null,
      "createdAt": "2026-03-22T15:00:00"
    }
  ]
}
```

> **玩家隱私**：`operatorId` **不**在玩家端回應中揭露。僅顯示 `operatorType`。

### 回應——403 Forbidden

```jsonc
{
  "code": "FORBIDDEN",
  "message": "您沒有權限存取此訂單"
}
```

---

## 玩家取消訂單

**玩家無法取消訂單。** 依據 FR-004，取消功能僅限 `ADMIN`、`STORE_OWNER` 及 `STORE_EDITOR` 角色。玩家端 API 不提供取消 endpoint。

若玩家希望取消，必須直接聯絡店家（帶外管道）。店家負責人再透過 admin API 執行取消。

---

## 備註

### 玩家可見的狀態

玩家看到與管理員相同的狀態標籤，但狀態歷程隱藏操作者身分：

| 玩家可見 | 玩家不可見 |
|---------|----------|
| `toStatus` / `toStatusLabel` | `operatorId` |
| `operatorType` | — |
| `remark` | — |
| `createdAt` | — |

### 自動完成（未來規劃）
v1 不自動完成訂單。玩家將看到 `SHIPPED` 狀態，直到店家負責人手動標記為 `COMPLETED`。自動完成（例如出貨後 14 天）為未來擴充功能。
