# 賞品盒 + 金流 + 訂單系統 API 測試指南

## 測試環境

- **Base URL**：`http://localhost:8080/api`
- **需要 JWT Token**：所有 API 皆需認證
- **Content-Type**：`application/json`

---

## 測試流程順序

### 1. 前置作業
1. 註冊玩家帳號
2. 登入取得 JWT Token
3. 系統自動建立錢包

### 2. 儲值流程
1. 查詢儲值方案
2. 選擇方案儲值（增加 Gold）

### 3. 抽獎流程
1. 查詢商品
2. 進行抽獎（扣除 Gold，獎品寫入賞品盒）
3. 查詢賞品盒

### 4. 出貨流程
1. 按店家分組查詢賞品盒
2. 選擇獎品出貨（產生訂單）
3. 查詢訂單狀態

### 5. 後台管理
1. 店家查看訂單
2. 準備出貨 → 出貨 → 完成

---

## 前台 API 測試

### 1. 錢包系統

#### 1.1 查詢我的錢包
```http
GET /api/wallet
Authorization: Bearer {token}
```

**回應範例**：
```json
{
  "success": true,
  "data": {
    "id": "wallet-uuid",
    "userId": "user-uuid",
    "userNickname": "玩家暱稱",
    "userEmail": "user@example.com",
    "goldCoins": 1000,
    "bonusCoins": 500,
    "totalRecharged": 5000,
    "createdAt": "2026-01-09T10:00:00",
    "updatedAt": "2026-01-09T15:00:00"
  }
}
```

#### 1.2 查詢我的交易記錄
```http
POST /api/wallet/transactions
Authorization: Bearer {token}
Content-Type: application/json

{
  "condition": {
    "transactionType": "DRAW",
    "coinType": "GOLD",
    "createdAtStart": "2026-01-01T00:00:00",
    "createdAtEnd": "2026-01-31T23:59:59"
  },
  "sortBy": "created_at",
  "sortOrder": "DESC"
}
```

**回應範例**：
```json
{
  "success": true,
  "data": [
    {
      "id": "transaction-uuid",
      "userId": "user-uuid",
      "userNickname": "玩家暱稱",
      "transactionType": "DRAW",
      "transactionTypeName": "抽獎消費",
      "coinType": "GOLD",
      "coinTypeName": "金幣",
      "amount": -80,
      "balanceAfter": 920,
      "relatedId": "lottery-uuid",
      "description": "抽獎：鬼滅之刃一番賞",
      "createdBy": null,
      "createdAt": "2026-01-09T14:30:00"
    }
  ]
}
```

---

### 2. 賞品盒系統

#### 2.1 查詢我的賞品盒
```http
GET /api/prize-box
Authorization: Bearer {token}
```

**回應範例**：
```json
{
  "success": true,
  "data": [
    {
      "id": "prize-box-uuid",
      "userId": "user-uuid",
      "userNickname": "玩家暱稱",
      "lotteryId": "lottery-uuid",
      "lotteryTitle": "鬼滅之刃一番賞",
      "prizeId": "prize-uuid",
      "prizeName": "炭治郎 Premium Figure",
      "prizeImage": "http://example.com/image.jpg",
      "prizeGrade": "A",
      "storeId": "store-uuid",
      "storeName": "KUJI 旗艦店",
      "status": "IN_BOX",
      "statusName": "在賞品盒中",
      "recycleBonus": 50,
      "recycledAt": null,
      "shippedAt": null,
      "orderId": null,
      "createdAt": "2026-01-09T14:30:00"
    }
  ]
}
```

#### 2.2 按店家分組查詢
```http
GET /api/prize-box/summary
Authorization: Bearer {token}
```

**回應範例**：
```json
{
  "success": true,
  "data": [
    {
      "storeId": "store-uuid",
      "storeName": "KUJI 旗艦店",
      "itemCount": 3,
      "items": [
        {
          "id": "prize-box-1",
          "prizeName": "炭治郎 Figure",
          "prizeGrade": "A",
          "recycleBonus": 50
        },
        {
          "id": "prize-box-2",
          "prizeName": "禰豆子 Figure",
          "prizeGrade": "B",
          "recycleBonus": 30
        }
      ]
    }
  ]
}
```

#### 2.3 出貨（產生訂單）
```http
POST /api/prize-box/ship
Authorization: Bearer {token}
Content-Type: application/json

{
  "prizeBoxIds": ["prize-box-1", "prize-box-2"],
  "shippingMethod": "HOME_DELIVERY",
  "recipientName": "王小明",
  "recipientPhone": "0912345678",
  "recipientAddress": "台北市信義區信義路五段7號",
  "storeCode": null,
  "storeName": null,
  "storeAddress": null
}
```

**回應範例**：
```json
{
  "success": true,
  "data": ["order-uuid-1"]
}
```

#### 2.4 回收獎品（轉紅利）
```http
POST /api/prize-box/recycle
Authorization: Bearer {token}
Content-Type: application/json

{
  "prizeBoxIds": ["prize-box-3", "prize-box-4"]
}
```

**回應範例**：
```json
{
  "success": true,
  "data": null
}
```

---

### 3. 訂單系統

#### 3.1 查詢我的訂單
```http
POST /api/order/list
Authorization: Bearer {token}
Content-Type: application/json

{
  "condition": {
    "status": "PENDING",
    "createdAtStart": "2026-01-01T00:00:00"
  },
  "sortBy": "created_at",
  "sortOrder": "DESC"
}
```

**回應範例**：
```json
{
  "success": true,
  "data": [
    {
      "id": "order-uuid",
      "orderNumber": "ORD20260109000001",
      "userId": "user-uuid",
      "userNickname": "玩家暱稱",
      "storeId": "store-uuid",
      "storeName": "KUJI 旗艦店",
      "status": "PENDING",
      "statusName": "訂單成立",
      "totalItems": 2,
      "shippingMethod": "HOME_DELIVERY",
      "createdAt": "2026-01-09T15:00:00"
    }
  ]
}
```

#### 3.2 查詢訂單詳情
```http
GET /api/order/{orderId}
Authorization: Bearer {token}
```

**回應範例**：
```json
{
  "success": true,
  "data": {
    "id": "order-uuid",
    "orderNumber": "ORD20260109000001",
    "userId": "user-uuid",
    "userNickname": "玩家暱稱",
    "storeId": "store-uuid",
    "storeName": "KUJI 旗艦店",
    "status": "PENDING",
    "statusName": "訂單成立",
    "paymentStatus": "SUCCESS",
    "paymentStatusName": "已付款",
    "totalItems": 2,
    "shippingMethod": "HOME_DELIVERY",
    "recipientName": "王小明",
    "recipientPhone": "0912345678",
    "recipientAddress": "台北市信義區信義路五段7號",
    "trackingNo": null,
    "remark": null,
    "items": [
      {
        "id": "item-uuid-1",
        "prizeBoxId": "prize-box-1",
        "lotteryId": "lottery-uuid",
        "lotteryTitle": "鬼滅之刃一番賞",
        "prizeId": "prize-uuid-1",
        "prizeName": "炭治郎 Figure",
        "prizeGrade": "A",
        "prizeImage": "http://example.com/image1.jpg",
        "createdAt": "2026-01-09T15:00:00"
      },
      {
        "id": "item-uuid-2",
        "prizeBoxId": "prize-box-2",
        "lotteryId": "lottery-uuid",
        "lotteryTitle": "鬼滅之刃一番賞",
        "prizeId": "prize-uuid-2",
        "prizeName": "禰豆子 Figure",
        "prizeGrade": "B",
        "prizeImage": "http://example.com/image2.jpg",
        "createdAt": "2026-01-09T15:00:00"
      }
    ],
    "createdAt": "2026-01-09T15:00:00",
    "updatedAt": "2026-01-09T15:00:00"
  }
}
```

---

### 4. 儲值方案

#### 4.1 查詢有效方案
```http
GET /api/recharge-plan/list
Authorization: Bearer {token}
```

**回應範例**：
```json
{
  "success": true,
  "data": [
    {
      "id": "plan-uuid-1",
      "name": "新手體驗包",
      "description": "首次儲值優惠",
      "amount": 100,
      "goldCoins": 100,
      "bonusCoins": 50,
      "isActive": true,
      "startDate": "2026-01-01T00:00:00",
      "endDate": "2026-12-31T23:59:59",
      "orderNum": 1,
      "discountRate": 50.0,
      "createdAt": "2026-01-01T00:00:00",
      "updatedAt": "2026-01-01T00:00:00"
    }
  ]
}
```

---

## 後台 API 測試

### 1. 錢包管理

#### 1.1 查詢玩家錢包
```http
GET /admin/wallet/{userId}
Authorization: Bearer {admin-token}
```

#### 1.2 手動調整點數
```http
POST /admin/wallet/adjust
Authorization: Bearer {admin-token}
Content-Type: application/json

{
  "userId": "user-uuid",
  "coinType": "GOLD",
  "amount": 1000,
  "reason": "活動贈送"
}
```

---

### 2. 訂單管理

#### 2.1 準備出貨
```http
PUT /admin/order/{orderId}/prepare
Authorization: Bearer {admin-token}
```

#### 2.2 訂單出貨
```http
PUT /admin/order/{orderId}/ship
Authorization: Bearer {admin-token}
Content-Type: application/json

{
  "trackingNo": "1234567890",
  "remark": "已寄出，預計 3 天送達"
}
```

#### 2.3 完成訂單
```http
PUT /admin/order/{orderId}/complete
Authorization: Bearer {admin-token}
```

#### 2.4 取消訂單（僅 ADMIN）
```http
PUT /admin/order/{orderId}/cancel
Authorization: Bearer {admin-token}
Content-Type: application/json

{
  "reason": "玩家要求取消"
}
```

---

### 3. 儲值方案管理

#### 3.1 新增方案
```http
POST /admin/recharge-plan
Authorization: Bearer {admin-token}
Content-Type: application/json

{
  "name": "春節特惠包",
  "description": "春節限定優惠",
  "amount": 500,
  "goldCoins": 500,
  "bonusCoins": 200,
  "isActive": true
}
```

---

## 測試場景

### 場景 1：完整流程測試

1. **玩家註冊登入**
2. **查詢儲值方案** → `GET /api/recharge-plan/list`
3. **儲值** → 增加 Gold（需金流整合）
4. **查詢錢包** → `GET /api/wallet`
5. **抽獎** → 扣除 Gold（需整合 LotteryService）
6. **查詢賞品盒** → `GET /api/prize-box`
7. **出貨** → `POST /api/prize-box/ship`
8. **查詢訂單** → `POST /api/order/list`
9. **店家準備出貨** → `PUT /admin/order/{orderId}/prepare`
10. **店家出貨** → `PUT /admin/order/{orderId}/ship`
11. **完成訂單** → `PUT /admin/order/{orderId}/complete`

### 場景 2：回收流程測試

1. **查詢賞品盒** → `GET /api/prize-box`
2. **回收獎品** → `POST /api/prize-box/recycle`
3. **查詢錢包** → 確認 Bonus 增加
4. **查詢交易記錄** → `POST /api/wallet/transactions`

---

## 注意事項

1. **所有 API 都需要 JWT Token**
2. **前台 API 自動過濾當前玩家**（不需傳 userId）
3. **後台 API 需要對應權限**（ADMIN/STORE_OWNER）
4. **訂單狀態流轉不可逆**（PENDING → PREPARING → SHIPPED → COMPLETED）
5. **僅 PENDING 狀態可取消訂單**
6. **玩家無法自行取消訂單**（需聯絡店家）

---

**測試指南完成**  
**版本**：v1.0  
**更新日期**：2026-01-09
