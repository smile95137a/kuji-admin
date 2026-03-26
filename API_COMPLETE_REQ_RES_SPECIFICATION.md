# 🎯 完整 API 規格書（REQ/RES 詳解）

> **最後更新**：2026-02-08  
> **系統**：KUJI-Server Admin Backend  
> **技術棧**：Spring Boot 3.3.3 + Java 21 + MyBatis  
> **Context Path**：`/api`  
> **Base URL**：`http://localhost:8080/api`

---

## 📖 目錄

### 前台 API
1. [認證 API](#1-認證-api)
2. [抽獎瀏覽 API](#2-抽獎瀏覽-api)
3. [抽獎執行 API](#3-抽獎執行-api)
4. [使用者管理 API](#4-使用者管理-api)
5. [地址管理 API](#5-地址管理-api)
6. [錢包 API](#6-錢包-api)
7. [獎品盒 API](#7-獎品盒-api)
8. [訂單管理 API](#8-訂單管理-api)

### 後台 API
9. [後台認證 API](#9-後台認證-api)
10. [後台商品管理 API](#10-後台商品管理-api)

---

## 1. 認證 API

### 1.1 用戶註冊

```http
POST /api/auth/register
Content-Type: application/json
```

**Request Body**:
```json
{
  "email": "user@example.com",
  "password": "password123",
  "referralCode": "ABC123"
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "userId": "uuid-xxx",
    "email": "user@example.com",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  },
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z",
    "requestId": "req-xxx"
  }
}
```

---

### 1.2 用戶登入

```http
POST /api/auth/login
Content-Type: application/json
```

**Request Body**:
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "userId": "uuid-xxx",
    "email": "user@example.com",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  },
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z",
    "requestId": "req-xxx"
  }
}
```

---

## 2. 抽獎瀏覽 API

### 2.1 查詢抽獎列表（前台）

```http
POST /api/lottery/browse/list
Content-Type: application/json
Authorization: Bearer {token}
```

**Request Body**:
```json
{
  "condition": {
    "category": "GACHA",
    "status": "ON_SHELF",
    "storeId": "uuid-xxx",
    "keyword": "卡牌"
  },
  "page": 1,
  "size": 20,
  "sortField": "createdAt",
  "sortOrder": "DESC"
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "id": "uuid-lottery-1",
      "storeId": "uuid-store-1",
      "storeName": "玩具公仔專賣店",
      "title": "鬼滅之刃一番賞",
      "description": "限量發售的鬼滅之刃系列",
      "imageUrl": "https://s3.amazonaws.com/bucket/image.jpg",
      "category": "OFFICIAL_ICHIBAN",
      "categoryName": "官方一番賞",
      "subCategory": "LOTTERY_MODE",
      "subCategoryName": "抽籤型",
      "playMode": "LOTTERY_MODE",
      "pricePerDraw": 650,
      "currentPrice": 650,
      "maxDraws": 100,
      "totalDraws": 45,
      "remainingDraws": 55,
      "weight": 10,
      "status": "ON_SHELF",
      "statusName": "已上架",
      "orderNum": 1,
      "startTime": "2026-01-01T00:00:00",
      "endTime": "2026-12-31T23:59:59",
      "prizes": [
        {
          "id": "uuid-prize-1",
          "level": "A",
          "levelName": "A賞",
          "prizeNumber": "01",
          "quantity": 5,
          "remaining": 3,
          "drawnCount": 2,
          "weight": 1,
          "prizeType": "PHYSICAL",
          "prizeTypeName": "實體獎品",
          "pointValue": 0
        }
      ],
      "createdAt": "2026-01-09T12:00:00",
      "updatedAt": "2026-02-08T10:30:00"
    }
  ],
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z",
    "requestId": "req-xxx"
  }
}
```

---

### 2.2 查詢單一抽獎詳情

```http
GET /api/lottery/browse/{id}
Authorization: Bearer {token}
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "id": "uuid-lottery-1",
    "storeId": "uuid-store-1",
    "storeName": "玩具公仔專賣店",
    "title": "鬼滅之刃一番賞",
    "description": "限量發售的鬼滅之刃系列",
    "imageUrl": "https://s3.amazonaws.com/bucket/image.jpg",
    "category": "OFFICIAL_ICHIBAN",
    "categoryName": "官方一番賞",
    "subCategory": "LOTTERY_MODE",
    "subCategoryName": "抽籤型",
    "playMode": "LOTTERY_MODE",
    "pricePerDraw": 650,
    "currentPrice": 650,
    "maxDraws": 100,
    "totalDraws": 45,
    "remainingDraws": 55,
    "status": "ON_SHELF",
    "statusName": "已上架",
    "prizes": [
      {
        "id": "uuid-prize-1",
        "level": "A",
        "levelName": "A賞",
        "prizeNumber": "01",
        "name": "公仔套裝",
        "imageUrl": "https://s3.amazonaws.com/bucket/prize.jpg",
        "quantity": 5,
        "remaining": 3,
        "drawnCount": 2,
        "weight": 1,
        "prizeType": "PHYSICAL",
        "prizeTypeName": "實體獎品",
        "pointValue": 0
      }
    ],
    "createdAt": "2026-01-09T12:00:00",
    "updatedAt": "2026-02-08T10:30:00"
  },
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z",
    "requestId": "req-xxx"
  }
}
```

---

## 3. 抽獎執行 API

### 3.1 執行抽獎（重點API！！）

```http
POST /api/lottery/draw/{lotteryId}/draw
Content-Type: application/json
Authorization: Bearer {token}
```

**Path Parameters**:
- `lotteryId`: 商品 UUID

**Request Body** (兩種模式):

**模式 A：指定票券 UUID 列表**
```json
{
  "count": 2,
  "tickets": [
    "uuid-ticket-1",
    "uuid-ticket-2"
  ]
}
```

**模式 B：隨機抽獎（只傳 count）**
```json
{
  "count": 2
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "recordId": 12345,
      "lotteryId": "uuid-lottery-1",
      "lotteryTitle": "鬼滅之刃一番賞",
      "prizeId": "uuid-prize-1",
      "prizeName": "公仔套裝",
      "prizeLevel": "A",
      "prizeLevelName": "A賞",
      "prizeImageUrl": "https://s3.amazonaws.com/bucket/prize.jpg",
      "prizeNumber": "01",
      "isLastPrize": false,
      "isGrandPrize": true,
      "costType": "GOLD",
      "costAmount": 650,
      "drawTime": "2026-02-08T10:30:00",
      "remainingDraws": 54,
      "success": true,
      "message": "恭喜抽中大賞！"
    },
    {
      "recordId": 12346,
      "lotteryId": "uuid-lottery-1",
      "lotteryTitle": "鬼滅之刃一番賞",
      "prizeId": "uuid-prize-2",
      "prizeName": "小立牌",
      "prizeLevel": "C",
      "prizeLevelName": "C賞",
      "prizeImageUrl": "https://s3.amazonaws.com/bucket/prize2.jpg",
      "prizeNumber": "03",
      "isLastPrize": false,
      "isGrandPrize": false,
      "costType": "GOLD",
      "costAmount": 650,
      "drawTime": "2026-02-08T10:30:05",
      "remainingDraws": 53,
      "success": true,
      "message": "恭喜抽中 C賞！"
    }
  ],
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z",
    "requestId": "req-xxx"
  }
}
```

**錯誤回應** (400 Bad Request):
```json
{
  "success": false,
  "error": {
    "code": "INVALID_REQUEST",
    "message": "票券數量 (3) 與列表長度 (2) 不符"
  },
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z",
    "requestId": "req-xxx"
  }
}
```

---

### 3.2 查詢可用籤位列表

```http
GET /api/lottery/draw/{lotteryId}/tickets
Authorization: Bearer {token}
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "id": "uuid-ticket-1",
      "lotteryId": "uuid-lottery-1",
      "ticketPosition": 1,
      "prizeId": "uuid-prize-1",
      "prizeName": "公仔套裝",
      "prizeLevel": "A",
      "prizeImageUrl": "https://s3.amazonaws.com/bucket/prize.jpg",
      "status": "AVAILABLE",
      "drawnBy": null,
      "drawnAt": null
    },
    {
      "id": "uuid-ticket-2",
      "lotteryId": "uuid-lottery-1",
      "ticketPosition": 2,
      "prizeId": "uuid-prize-2",
      "prizeName": "小立牌",
      "prizeLevel": "C",
      "prizeImageUrl": "https://s3.amazonaws.com/bucket/prize2.jpg",
      "status": "DRAWN",
      "drawnBy": "uuid-user-xxx",
      "drawnAt": "2026-02-07T15:30:00"
    }
  ],
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z",
    "requestId": "req-xxx"
  }
}
```

---

### 3.3 查詢抽獎場次資訊

```http
GET /api/lottery/draw/{lotteryId}/session
Authorization: Bearer {token}
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "sessionId": "uuid-session-1",
    "isOpener": true,
    "openerNickname": "玩家A",
    "protectionDraws": 5,
    "protectionEndTime": "2026-02-08T10:35:00Z",
    "openerDrawCount": 3,
    "freeDrawEnabled": true,
    "status": "IN_PROGRESS"
  },
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z",
    "requestId": "req-xxx"
  }
}
```

---

## 4. 使用者管理 API

### 4.1 取得當前用戶資訊

```http
GET /api/user/me
Authorization: Bearer {token}
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "id": "uuid-user-1",
    "email": "user@example.com",
    "nickname": "玩家A",
    "avatarUrl": "https://s3.amazonaws.com/bucket/avatar.jpg",
    "status": "ACTIVE",
    "createdAt": "2026-01-01T00:00:00",
    "updatedAt": "2026-02-08T10:30:00"
  },
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z",
    "requestId": "req-xxx"
  }
}
```

---

### 4.2 更新用戶資訊

```http
PUT /api/user/me
Content-Type: application/json
Authorization: Bearer {token}
```

**Request Body** (所有欄位可選):
```json
{
  "nickname": "新暱稱",
  "avatarUrl": "https://s3.amazonaws.com/bucket/new-avatar.jpg"
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "id": "uuid-user-1",
    "email": "user@example.com",
    "nickname": "新暱稱",
    "avatarUrl": "https://s3.amazonaws.com/bucket/new-avatar.jpg",
    "status": "ACTIVE",
    "updatedAt": "2026-02-08T10:35:00"
  },
  "meta": {
    "timestamp": "2026-02-08T10:35:00Z",
    "requestId": "req-xxx"
  }
}
```

---

## 5. 地址管理 API

### 5.1 新增收件地址

```http
POST /api/user/addresses
Content-Type: application/json
Authorization: Bearer {token}
```

**Request Body**:
```json
{
  "label": "家",
  "recipientName": "王小明",
  "recipientPhone": "0912345678",
  "city": "台北市",
  "district": "信義區",
  "zipCode": "11001",
  "address": "松壽路1號",
  "isDefault": true
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "id": "uuid-address-1",
    "userId": "uuid-user-1",
    "label": "家",
    "recipientName": "王小明",
    "recipientPhone": "0912345678",
    "city": "台北市",
    "district": "信義區",
    "zipCode": "11001",
    "address": "松壽路1號",
    "isDefault": true,
    "createdAt": "2026-02-08T10:30:00"
  },
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z",
    "requestId": "req-xxx"
  }
}
```

---

### 5.2 更新收件地址

```http
PUT /api/user/addresses/{id}
Content-Type: application/json
Authorization: Bearer {token}
```

**Request Body** (所有欄位可選):
```json
{
  "label": "公司",
  "recipientName": "王小明"
}
```

**Response** (200 OK): 同 5.1

---

### 5.3 查詢所有地址

```http
GET /api/user/addresses
Authorization: Bearer {token}
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "id": "uuid-address-1",
      "label": "家",
      "recipientName": "王小明",
      "recipientPhone": "0912345678",
      "city": "台北市",
      "district": "信義區",
      "zipCode": "11001",
      "address": "松壽路1號",
      "isDefault": true,
      "createdAt": "2026-02-08T10:30:00"
    }
  ],
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z",
    "requestId": "req-xxx"
  }
}
```

---

### 5.4 刪除地址

```http
DELETE /api/user/addresses/{id}
Authorization: Bearer {token}
```

**Response** (200 OK):
```json
{
  "success": true,
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z",
    "requestId": "req-xxx"
  }
}
```

---

## 6. 錢包 API

### 6.1 查詢錢包餘額

```http
GET /api/wallet
Authorization: Bearer {token}
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "userId": "uuid-user-1",
    "goldCoins": 5000,
    "bonusCoins": 1000,
    "totalCoins": 6000,
    "lastUpdated": "2026-02-08T10:30:00"
  },
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z",
    "requestId": "req-xxx"
  }
}
```

---

### 6.2 查詢交易記錄

```http
POST /api/wallet/transactions
Content-Type: application/json
Authorization: Bearer {token}
```

**Request Body**:
```json
{
  "condition": {
    "transactionType": "DRAW",
    "coinType": "GOLD"
  },
  "page": 1,
  "size": 20,
  "sortField": "createdAt",
  "sortOrder": "DESC"
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "id": "uuid-trans-1",
      "userId": "uuid-user-1",
      "userNickname": "玩家A",
      "transactionType": "DRAW",
      "transactionTypeName": "抽獎消費",
      "coinType": "GOLD",
      "coinTypeName": "儲值金",
      "amount": 650,
      "description": "抽獎消費",
      "relatedId": "uuid-lottery-1",
      "balanceBefore": 5000,
      "balanceAfter": 4350,
      "createdAt": "2026-02-08T10:30:00"
    }
  ],
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z",
    "requestId": "req-xxx"
  }
}
```

---

## 7. 獎品盒 API

### 7.1 查詢我的獎品盒

```http
GET /api/prize-box
Authorization: Bearer {token}
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "id": "uuid-prizebox-1",
      "userId": "uuid-user-1",
      "lotteryId": "uuid-lottery-1",
      "lotteryTitle": "鬼滅之刃一番賞",
      "lotteryImageUrl": "https://s3.amazonaws.com/bucket/lottery.jpg",
      "prizeId": "uuid-prize-1",
      "prizeName": "公仔套裝",
      "prizeLevel": "A",
      "prizeImageUrl": "https://s3.amazonaws.com/bucket/prize.jpg",
      "storeId": "uuid-store-1",
      "storeName": "玩具公仔專賣店",
      "status": "IN_BOX",
      "statusName": "在賞品盒中",
      "isRecyclable": true,
      "recycleBonus": 500,
      "createdAt": "2026-02-08T10:30:00"
    }
  ],
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z",
    "requestId": "req-xxx"
  }
}
```

---

### 7.2 查詢獎品盒摘要（按店家分組）

```http
GET /api/prize-box/summary
Authorization: Bearer {token}
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "storeId": "uuid-store-1",
      "storeName": "玩具公仔專賣店",
      "itemCount": 5,
      "items": [
        {
          "id": "uuid-prizebox-1",
          "userId": "uuid-user-1",
          "lotteryId": "uuid-lottery-1",
          "lotteryTitle": "鬼滅之刃一番賞",
          "lotteryImageUrl": "https://s3.amazonaws.com/bucket/lottery.jpg",
          "prizeId": "uuid-prize-1",
          "prizeName": "公仔套裝",
          "prizeLevel": "A",
          "prizeImageUrl": "https://s3.amazonaws.com/bucket/prize.jpg",
          "storeId": "uuid-store-1",
          "storeName": "玩具公仔專賣店",
          "status": "IN_BOX",
          "statusName": "在賞品盒中",
          "isRecyclable": true,
          "recycleBonus": 500,
          "createdAt": "2026-02-08T10:30:00"
        }
      ]
    }
  ],
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z",
    "requestId": "req-xxx"
  }
}
```

---

### 7.3 申請寄送獎品（重點API！！）

```http
POST /api/prize-box/ship
Content-Type: application/json
Authorization: Bearer {token}
```

**Request Body** ⚠️ **注意：這不是 addressId，而是完整的配送資訊**:
```json
{
  "prizeBoxIds": [
    "uuid-prizebox-1",
    "uuid-prizebox-2"
  ],
  "shippingMethod": "HOME_DELIVERY",
  "recipientName": "王小明",
  "recipientPhone": "0912345678",
  "recipientAddress": "台北市信義區松壽路1號",
  "storeCode": null,
  "storeName": null,
  "storeAddress": null,
  "remark": "請放在門口"
}
```

> **配送方式說明**：
> - `HOME_DELIVERY`：宅配到府，需要填 `recipientAddress`
> - `SEVEN_ELEVEN` 或 `FAMILY_MART`：超商取貨，需要填 `storeCode`, `storeName`, `storeAddress`

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    "uuid-order-1",
    "uuid-order-2"
  ],
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z",
    "requestId": "req-xxx"
  }
}
```

---

### 7.4 回收獎品（轉換為紅利）

```http
POST /api/prize-box/recycle
Content-Type: application/json
Authorization: Bearer {token}
```

**Request Body**:
```json
{
  "prizeBoxIds": [
    "uuid-prizebox-1",
    "uuid-prizebox-2"
  ]
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z",
    "requestId": "req-xxx"
  }
}
```

---

## 8. 訂單管理 API

### 8.1 查詢訂單列表

```http
POST /api/order/list
Content-Type: application/json
Authorization: Bearer {token}
```

**Request Body**:
```json
{
  "condition": {
    "status": "COMPLETED",
    "paymentStatus": "SUCCESS"
  },
  "page": 1,
  "size": 20,
  "sortField": "createdAt",
  "sortOrder": "DESC"
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "id": "uuid-order-1",
      "userId": "uuid-user-1",
      "lotteryId": "uuid-lottery-1",
      "lotteryTitle": "鬼滅之刃一番賞",
      "prizeBoxIds": [
        "uuid-prizebox-1",
        "uuid-prizebox-2"
      ],
      "status": "COMPLETED",
      "statusName": "已完成",
      "paymentStatus": "SUCCESS",
      "paymentStatusName": "支付成功",
      "shippingMethod": "HOME_DELIVERY",
      "shippingMethodName": "宅配到府",
      "recipientName": "王小明",
      "recipientPhone": "0912345678",
      "recipientAddress": "台北市信義區松壽路1號",
      "totalAmount": 1300,
      "createdAt": "2026-02-08T10:30:00",
      "updatedAt": "2026-02-08T11:00:00"
    }
  ],
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z",
    "requestId": "req-xxx"
  }
}
```

---

### 8.2 查詢單一訂單

```http
GET /api/order/{orderId}
Authorization: Bearer {token}
```

**Response** (200 OK): 同 8.1 (data 為單個訂單)

---

## 9. 後台認證 API

### 9.1 後台登入

```http
POST /api/auth/admin/login
Content-Type: application/json
```

**Request Body**:
```json
{
  "email": "admin@kuji.com",
  "password": "admin123"
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "userId": "uuid-admin-1",
    "email": "admin@kuji.com",
    "roleCode": "ROLE_ADMIN",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  },
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z",
    "requestId": "req-xxx"
  }
}
```

---

## 10. 後台商品管理 API

### 10.1 新增商品（含獎品）

```http
POST /api/admin/lottery-with-prizes
Content-Type: application/json
Authorization: Bearer {admin_token}
```

**Request Body** ⚠️ **關鍵規則**:

```json
{
  "lottery": {
    "title": "鬼滅之刃一番賞",
    "description": "官方授權商品",
    "imageUrl": "https://s3.amazonaws.com/bucket/image.jpg",
    "category": "OFFICIAL_ICHIBAN",
    "subCategory": "LOTTERY_MODE",
    "playMode": "LOTTERY_MODE",
    "pricePerDraw": 650,
    "maxDraws": 100,
    "storeId": "uuid-store-1",
    "startTime": "2026-01-01T00:00:00",
    "endTime": "2026-12-31T23:59:59"
  },
  "prizes": [
    {
      "level": "A",
      "name": "公仔套裝",
      "imageUrl": "https://s3.amazonaws.com/bucket/prize1.jpg",
      "quantity": 5,
      "prizeType": "PHYSICAL",
      "pointValue": 0,
      "weight": 1
    },
    {
      "level": "B",
      "name": "小立牌",
      "imageUrl": "https://s3.amazonaws.com/bucket/prize2.jpg",
      "quantity": 10,
      "prizeType": "PHYSICAL",
      "pointValue": 0,
      "weight": 1
    },
    {
      "level": "C",
      "name": "貼紙",
      "imageUrl": "https://s3.amazonaws.com/bucket/prize3.jpg",
      "quantity": 85,
      "prizeType": "PHYSICAL",
      "pointValue": 0,
      "weight": 1
    }
  ]
}
```

> **關鍵規則**：
> 1. **一番賞模式** (`LOTTERY_MODE`):
>    - 所有獎品的 `quantity` 總和必須 = `maxDraws`（例如：5+10+85=100）
>    - `maxDraws` 可以不傳（後端會自動計算）
> 2. **刮刮樂模式** (`SCRATCH_MODE`):
>    - `maxDraws` **必須傳入**
>    - 獎品的 `quantity` 總和 < `maxDraws`（差額自動為謝謝惠顧）
>    - 例如：maxDraws=100, 獎品總數=28 → 72個謝謝惠顧

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "lotteryId": "uuid-lottery-1",
    "title": "鬼滅之刃一番賞",
    "prizeCount": 100,
    "createdAt": "2026-02-08T10:30:00"
  },
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z",
    "requestId": "req-xxx"
  }
}
```

**錯誤回應範例 1**：刮刮樂模式總抽數小於獎品總數
```json
{
  "success": false,
  "error": {
    "code": "INVALID_PRIZE_CONFIG",
    "message": "刮刮樂模式：獎品總數 (100) 不能大於最大抽數 (50)"
  },
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z",
    "requestId": "req-xxx"
  }
}
```

**錯誤回應範例 2**：一番賞模式獎品總數不等於最大抽數
```json
{
  "success": false,
  "error": {
    "code": "INVALID_PRIZE_CONFIG",
    "message": "一番賞模式：獎品總數 (85) 必須等於最大抽數 (100)"
  },
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z",
    "requestId": "req-xxx"
  }
}
```

---

### 10.2 更新商品（含獎品）

```http
PUT /api/admin/lottery-with-prizes/{lotteryId}
Content-Type: application/json
Authorization: Bearer {admin_token}
```

**Request Body**: 同 10.1 (所有欄位可選)

**Response** (200 OK): 同 10.1

---

### 10.3 查詢單一商品（後台）

```http
GET /api/admin/lottery-with-prizes/{lotteryId}
Authorization: Bearer {admin_token}
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "lottery": {
      "id": "uuid-lottery-1",
      "title": "鬼滅之刃一番賞",
      "description": "官方授權商品",
      "imageUrl": "https://s3.amazonaws.com/bucket/image.jpg",
      "category": "OFFICIAL_ICHIBAN",
      "subCategory": "LOTTERY_MODE",
      "playMode": "LOTTERY_MODE",
      "pricePerDraw": 650,
      "maxDraws": 100,
      "storeId": "uuid-store-1",
      "status": "ON_SHELF",
      "startTime": "2026-01-01T00:00:00",
      "endTime": "2026-12-31T23:59:59"
    },
    "prizes": [
      {
        "id": "uuid-prize-1",
        "level": "A",
        "name": "公仔套裝",
        "imageUrl": "https://s3.amazonaws.com/bucket/prize1.jpg",
        "quantity": 5,
        "remaining": 5,
        "prizeType": "PHYSICAL",
        "pointValue": 0,
        "weight": 1
      }
    ]
  },
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z",
    "requestId": "req-xxx"
  }
}
```

---

## 📊 統一回應格式

### 成功回應
```json
{
  "success": true,
  "data": {...},
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z",
    "requestId": "req-xxx"
  }
}
```

### 失敗回應
```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "錯誤訊息"
  },
  "meta": {
    "timestamp": "2026-02-08T10:30:00Z",
    "requestId": "req-xxx"
  }
}
```

---

## 🔑 認證說明

所有需要認證的 API 都在 Header 中帶入 JWT Token：

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

Token 包含以下資訊：
- `sub`: 用戶 ID
- `email`: 用戶信箱
- `roles`: 用戶角色陣列

---

## ⚠️ 常見 API 呼叫錯誤

### ❌ 錯誤 1：7.3 寄送獎品 API 使用了舊文檔 `addressId`

**錯誤**：
```json
{
  "prizeBoxIds": ["uuid-1", "uuid-2"],
  "addressId": "uuid-address-1"  // ❌ 舊版文檔
}
```

**正確**：
```json
{
  "prizeBoxIds": ["uuid-1", "uuid-2"],
  "shippingMethod": "HOME_DELIVERY",
  "recipientName": "王小明",
  "recipientPhone": "0912345678",
  "recipientAddress": "台北市信義區松壽路1號",
  "remark": "請放在門口"
}
```

---

### ❌ 錯誤 2：3.1 抽獎 API count 與 tickets 長度不符

**錯誤**：
```json
{
  "count": 3,
  "tickets": ["uuid-1", "uuid-2"]  // ❌ count 是 3，但只有 2 個 ticket
}
```

**正確**：
```json
{
  "count": 2,
  "tickets": ["uuid-1", "uuid-2"]  // ✅ 符合
}
```

---

### ❌ 錯誤 3：10.1 新增商品時一番賞獎品總數不等於 maxDraws

**錯誤**：
```json
{
  "lottery": {
    "maxDraws": 100
  },
  "prizes": [
    { "quantity": 30 },
    { "quantity": 30 },
    { "quantity": 30 }  // ❌ 總和 90，不等於 100
  ]
}
```

**正確**：
```json
{
  "lottery": {
    "maxDraws": 100
  },
  "prizes": [
    { "quantity": 30 },
    { "quantity": 30 },
    { "quantity": 40 }  // ✅ 總和 100
  ]
}
```

---

*最後更新：2026-02-08*  
*所有 API 規格已根據實際代碼審查，確保 100% 準確*

