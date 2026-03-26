# 後台訂單管理 API 完整文檔

## 📊 API 列表

| API | Method | 權限 | 說明 |
|-----|--------|------|------|
| `/admin/orders/list` | POST | ADMIN, STORE_OWNER | 查詢訂單列表 |
| `/admin/orders/{orderId}` | GET | ADMIN, STORE_OWNER | 查詢訂單詳情 |
| `/admin/orders/{orderId}/prepare` | PUT | ADMIN, STORE_OWNER | 準備出貨 |
| `/admin/orders/{orderId}/ship` | PUT | ADMIN, STORE_OWNER | 訂單出貨 |
| `/admin/orders/{orderId}/complete` | PUT | ADMIN, STORE_OWNER | 完成訂單 |
| `/admin/orders/{orderId}/cancel` | PUT | ADMIN | 取消訂單 |

---

## 1. 查詢訂單列表

### 請求

```http
POST /api/admin/orders/list
Authorization: Bearer {token}
Content-Type: application/json
```

**權限**：`ROLE_ADMIN`, `ROLE_STORE_OWNER`

**Request Body**:
```json
{
  "condition": {
    "userId": "uuid-user-1",
    "storeId": "uuid-store-1",
    "status": "PENDING",
    "shippingStatus": "NOT_SHIPPED",
    "orderNo": "ORD20260208",
    "startDate": "2026-01-01T00:00:00",
    "endDate": "2026-02-09T23:59:59"
  },
  "sortBy": "createdAt",
  "sortOrder": "DESC"
}
```

**查詢條件說明**：
| 欄位 | 類型 | 必填 | 說明 |
|------|------|------|------|
| `userId` | String | ❌ | 玩家 ID（精確匹配） |
| `storeId` | String | ❌ | 店家 ID（StoreOwner 自動過濾） |
| `status` | String | ❌ | 訂單狀態 |
| `shippingStatus` | String | ❌ | 出貨狀態 |
| `orderNo` | String | ❌ | 訂單編號（模糊搜尋） |
| `startDate` | DateTime | ❌ | 建立開始日期 |
| `endDate` | DateTime | ❌ | 建立結束日期 |

**訂單狀態 (status)**：
- `PENDING`: 待處理
- `PREPARING`: 準備中
- `SHIPPED`: 已出貨
- `COMPLETED`: 已完成
- `CANCELLED`: 已取消

**出貨狀態 (shippingStatus)**：
- `NOT_SHIPPED`: 未出貨
- `SHIPPED`: 已出貨
- `DELIVERED`: 已送達

### 響應

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "orderId": "uuid-order-1",
      "orderNumber": "ORD20260208001",
      "status": "PENDING",
      "paymentStatus": "SUCCESS",
      "userId": "uuid-user-1",
      "userName": "王小明",
      "userEmail": "user@example.com",
      "userPhone": "0912345678",
      "storeId": "uuid-store-1",
      "storeName": "KUJI 台北旗艦店",
      "totalItems": 3,
      "shippingMethod": "HOME_DELIVERY",
      "recipientName": "王小明",
      "recipientPhone": "0912345678",
      "recipientAddress": "台北市信義區松壽路1號",
      "trackingNo": null,
      "createdAt": "2026-02-08T10:00:00",
      "updatedAt": "2026-02-08T10:00:00"
    },
    {
      "orderId": "uuid-order-2",
      "orderNumber": "ORD20260208002",
      "status": "SHIPPED",
      "paymentStatus": "SUCCESS",
      "userId": "uuid-user-2",
      "userName": "李小華",
      "userEmail": "user2@example.com",
      "userPhone": "0987654321",
      "storeId": "uuid-store-1",
      "storeName": "KUJI 台北旗艦店",
      "totalItems": 2,
      "shippingMethod": "STORE_PICKUP",
      "recipientName": "李小華",
      "recipientPhone": "0987654321",
      "storeCode": "711-12345",
      "storeName": "7-11 信義門市",
      "storeAddress": "台北市信義區...",
      "trackingNo": "1234567890",
      "createdAt": "2026-02-07T15:30:00",
      "updatedAt": "2026-02-08T09:00:00"
    }
  ],
  "meta": {
    "timestamp": "2026-02-09T10:30:00Z",
    "requestId": "req-xxx"
  }
}
```

---

## 2. 查詢訂單詳情

### 請求

```http
GET /api/admin/orders/{orderId}
Authorization: Bearer {token}
```

**權限**：`ROLE_ADMIN`, `ROLE_STORE_OWNER`

**Parameters**：
- `orderId` (path): 訂單 ID

### 響應

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "orderId": "uuid-order-1",
    "orderNumber": "ORD20260208001",
    "status": "PENDING",
    "statusName": "待處理",
    "paymentStatus": "SUCCESS",
    "paymentStatusName": "已付款",
    "userId": "uuid-user-1",
    "userName": "王小明",
    "userEmail": "user@example.com",
    "userPhone": "0912345678",
    "storeId": "uuid-store-1",
    "storeName": "KUJI 台北旗艦店",
    "items": [
      {
        "id": "uuid-item-1",
        "prizeBoxId": "uuid-prize-box-1",
        "lotteryId": "uuid-lottery-1",
        "lotteryTitle": "鬼滅之刃一番賞",
        "lotteryImageUrl": "https://s3.amazonaws.com/lottery.jpg",
        "prizeId": "uuid-prize-1",
        "prizeName": "炭治郎公仔（大）",
        "prizeLevel": "A",
        "prizeImageUrl": "https://s3.amazonaws.com/prize.jpg"
      },
      {
        "id": "uuid-item-2",
        "prizeBoxId": "uuid-prize-box-2",
        "lotteryId": "uuid-lottery-1",
        "lotteryTitle": "鬼滅之刃一番賞",
        "lotteryImageUrl": "https://s3.amazonaws.com/lottery.jpg",
        "prizeId": "uuid-prize-2",
        "prizeName": "禰豆子公仔",
        "prizeLevel": "B",
        "prizeImageUrl": "https://s3.amazonaws.com/prize2.jpg"
      }
    ],
    "totalItems": 2,
    "shippingMethod": "HOME_DELIVERY",
    "shippingMethodName": "宅配到府",
    "recipientName": "王小明",
    "recipientPhone": "0912345678",
    "recipientAddress": "台北市信義區松壽路1號",
    "storeCode": null,
    "trackingNo": null,
    "remark": null,
    "statusLogs": [
      {
        "id": "uuid-log-1",
        "fromStatus": null,
        "fromStatusName": null,
        "toStatus": "PENDING",
        "toStatusName": "待處理",
        "operatorId": "SYSTEM",
        "operatorType": "SYSTEM",
        "remark": "訂單自動建立",
        "createdAt": "2026-02-08T10:00:00"
      }
    ],
    "createdAt": "2026-02-08T10:00:00",
    "updatedAt": "2026-02-08T10:00:00"
  },
  "meta": {
    "timestamp": "2026-02-09T10:30:00Z",
    "requestId": "req-xxx"
  }
}
```

---

## 3. 準備出貨

### 請求

```http
PUT /api/admin/orders/{orderId}/prepare
Authorization: Bearer {token}
```

**權限**：`ROLE_ADMIN`, `ROLE_STORE_OWNER`

**Parameters**：
- `orderId` (path): 訂單 ID

**說明**：
- 店家確認備貨完成
- 狀態：`PENDING` → `PREPARING`
- 記錄操作者 ID

### 響應

**Response** (200 OK):
```json
{
  "success": true,
  "meta": {
    "timestamp": "2026-02-09T10:30:00Z",
    "requestId": "req-xxx"
  }
}
```

**錯誤情況**：
```json
{
  "success": false,
  "error": {
    "code": "INVALID_ORDER_STATUS",
    "message": "訂單狀態不符：當前狀態為 SHIPPED，無法準備出貨"
  }
}
```

---

## 4. 訂單出貨

### 請求

```http
PUT /api/admin/orders/{orderId}/ship
Authorization: Bearer {token}
Content-Type: application/json
```

**權限**：`ROLE_ADMIN`, `ROLE_STORE_OWNER`

**Parameters**：
- `orderId` (path): 訂單 ID

**Request Body**:
```json
{
  "trackingNo": "1234567890",
  "logisticsCompany": "黑貓宅急便",
  "remark": "已出貨"
}
```

**欄位說明**：
| 欄位 | 類型 | 必填 | 說明 |
|------|------|------|------|
| `trackingNo` | String | ✅ | 物流單號 |
| `logisticsCompany` | String | ❌ | 物流公司 |
| `remark` | String | ❌ | 備註 |

**說明**：
- 填寫物流單號並出貨
- 狀態：`PREPARING` → `SHIPPED`
- 自動記錄出貨時間

### 響應

**Response** (200 OK):
```json
{
  "success": true,
  "meta": {
    "timestamp": "2026-02-09T10:30:00Z",
    "requestId": "req-xxx"
  }
}
```

---

## 5. 完成訂單

### 請求

```http
PUT /api/admin/orders/{orderId}/complete
Authorization: Bearer {token}
```

**權限**：`ROLE_ADMIN`, `ROLE_STORE_OWNER`

**Parameters**：
- `orderId` (path): 訂單 ID

**說明**：
- 玩家確認收貨或自動完成
- 狀態：`SHIPPED` → `COMPLETED`
- 記錄完成時間

### 響應

**Response** (200 OK):
```json
{
  "success": true,
  "meta": {
    "timestamp": "2026-02-09T10:30:00Z",
    "requestId": "req-xxx"
  }
}
```

---

## 6. 取消訂單

### 請求

```http
PUT /api/admin/orders/{orderId}/cancel
Authorization: Bearer {token}
Content-Type: application/json
```

**權限**：`ROLE_ADMIN` 專用

**Parameters**：
- `orderId` (path): 訂單 ID

**Request Body**:
```json
{
  "reason": "庫存不足",
  "refundAmount": 1300
}
```

**欄位說明**：
| 欄位 | 類型 | 必填 | 說明 |
|------|------|------|------|
| `reason` | String | ✅ | 取消原因 |
| `refundAmount` | Long | ❌ | 退款金額（台幣） |

**說明**：
- 僅限 `PENDING` 狀態可取消
- 狀態：`PENDING` → `CANCELLED`
- 記錄取消原因和時間

### 響應

**Response** (200 OK):
```json
{
  "success": true,
  "meta": {
    "timestamp": "2026-02-09T10:30:00Z",
    "requestId": "req-xxx"
  }
}
```

**錯誤情況**：
```json
{
  "success": false,
  "error": {
    "code": "CANNOT_CANCEL_ORDER",
    "message": "訂單已出貨，無法取消"
  }
}
```

---

## 訂單狀態流程圖

```
玩家從賞品盒出貨
    ↓
PENDING（待處理）
    ↓ [店家確認備貨] PUT /prepare
PREPARING（準備中）
    ↓ [填寫物流單號] PUT /ship
SHIPPED（已出貨）
    ↓ [玩家確認收貨 or 7天後自動] PUT /complete
COMPLETED（已完成）

取消流程：
PENDING → [管理員取消] PUT /cancel → CANCELLED（已取消）
```

---

## 出貨方式說明

| 代碼 | 名稱 | 說明 |
|------|------|------|
| `HOME_DELIVERY` | 宅配到府 | 需填寫 `recipientAddress` |
| `STORE_PICKUP` | 超商取貨 | 需填寫 `storeCode`, `storeName`, `storeAddress` |

---

## 前端使用範例

### 1. 查詢訂單列表

```javascript
const getOrders = async (filters) => {
  const response = await axios.post('/api/admin/orders/list', {
    condition: {
      status: filters.status,
      startDate: filters.startDate,
      endDate: filters.endDate
    },
    sortBy: 'createdAt',
    sortOrder: 'DESC'
  }, {
    headers: {
      'Authorization': `Bearer ${adminToken}`
    }
  });
  
  const orders = response.data.data;
  console.log(`共有 ${orders.length} 筆訂單`);
  return orders;
};
```

### 2. 出貨流程

```javascript
const shipOrder = async (orderId, trackingNo) => {
  // 1. 準備出貨
  await axios.put(`/api/admin/orders/${orderId}/prepare`, {}, {
    headers: { 'Authorization': `Bearer ${adminToken}` }
  });
  
  // 2. 填寫物流單號並出貨
  await axios.put(`/api/admin/orders/${orderId}/ship`, {
    trackingNo: trackingNo,
    logisticsCompany: '黑貓宅急便',
    remark: '已出貨'
  }, {
    headers: { 'Authorization': `Bearer ${adminToken}` }
  });
  
  alert('✅ 訂單已出貨！');
};
```

### 3. 查詢訂單詳情

```javascript
const getOrderDetail = async (orderId) => {
  const response = await axios.get(`/api/admin/orders/${orderId}`, {
    headers: { 'Authorization': `Bearer ${adminToken}` }
  });
  
  const order = response.data.data;
  console.log('訂單編號:', order.orderNumber);
  console.log('訂單狀態:', order.statusName);
  console.log('訂單項目:', order.items.length);
  
  return order;
};
```

---

## 權限說明

| API | ADMIN | STORE_OWNER | STORE_EDITOR |
|-----|-------|-------------|--------------|
| 查詢訂單列表 | ✅ 所有訂單 | ✅ 自己店家 | ❌ |
| 查詢訂單詳情 | ✅ | ✅ 自己店家 | ❌ |
| 準備出貨 | ✅ | ✅ 自己店家 | ❌ |
| 訂單出貨 | ✅ | ✅ 自己店家 | ❌ |
| 完成訂單 | ✅ | ✅ 自己店家 | ❌ |
| 取消訂單 | ✅ | ❌ | ❌ |

---

## 注意事項

### ⚠️ StoreOwner 自動過濾

```java
// 店家負責人查詢時，自動過濾到自己的店家
if (SecurityUtils.hasRole("ROLE_STORE_OWNER")) {
    String storeId = SecurityUtils.getCurrentUserPrimaryStoreId();
    condition.setStoreId(storeId);
}
```

### ⚠️ 訂單狀態限制

- **準備出貨**：只有 `PENDING` 可以執行
- **訂單出貨**：只有 `PREPARING` 可以執行
- **完成訂單**：只有 `SHIPPED` 可以執行
- **取消訂單**：只有 `PENDING` 可以取消

### ⚠️ 權限檢查

- **取消訂單**：只有 `ROLE_ADMIN` 可以執行
- **店家操作**：只能操作自己店家的訂單

---

## API 測試範例

### Postman Collection

```json
{
  "info": {
    "name": "後台訂單管理 API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "查詢訂單列表",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer {{adminToken}}"
          }
        ],
        "url": "{{baseUrl}}/api/admin/orders/list",
        "body": {
          "mode": "raw",
          "raw": "{\n  \"condition\": {\n    \"status\": \"PENDING\"\n  },\n  \"sortBy\": \"createdAt\",\n  \"sortOrder\": \"DESC\"\n}"
        }
      }
    },
    {
      "name": "查詢訂單詳情",
      "request": {
        "method": "GET",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer {{adminToken}}"
          }
        ],
        "url": "{{baseUrl}}/api/admin/orders/{{orderId}}"
      }
    },
    {
      "name": "準備出貨",
      "request": {
        "method": "PUT",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer {{adminToken}}"
          }
        ],
        "url": "{{baseUrl}}/api/admin/orders/{{orderId}}/prepare"
      }
    },
    {
      "name": "訂單出貨",
      "request": {
        "method": "PUT",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer {{adminToken}}"
          }
        ],
        "url": "{{baseUrl}}/api/admin/orders/{{orderId}}/ship",
        "body": {
          "mode": "raw",
          "raw": "{\n  \"trackingNo\": \"1234567890\",\n  \"logisticsCompany\": \"黑貓宅急便\"\n}"
        }
      }
    },
    {
      "name": "完成訂單",
      "request": {
        "method": "PUT",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer {{adminToken}}"
          }
        ],
        "url": "{{baseUrl}}/api/admin/orders/{{orderId}}/complete"
      }
    },
    {
      "name": "取消訂單",
      "request": {
        "method": "PUT",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer {{adminToken}}"
          }
        ],
        "url": "{{baseUrl}}/api/admin/orders/{{orderId}}/cancel",
        "body": {
          "mode": "raw",
          "raw": "{\n  \"reason\": \"庫存不足\"\n}"
        }
      }
    }
  ]
}
```

---

**最後更新**：2026-02-09  
**狀態**：✅ 完整可用  
**API 路徑**：`/api/admin/orders`（已修復為複數形式）
