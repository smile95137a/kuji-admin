# KUJI-Server API 完整文檔

> **專案**: KUJI-Server Admin Backend  
> **技術棧**: Spring Boot 3.3.3 + Java 21 + MyBatis + JWT  
> **Context Path**: `/api`  
> **Base URL**: `http://localhost:8080/api` (本地) / `http://18.179.187.129:8080/api` (EC2)  
> **最後更新**: 2025-12-26

---

## 📋 目錄

### 🎯 前台 API (`/api/**`)
1. [認證與授權](#1-認證與授權)
2. [抽獎瀏覽與抽獎](#2-抽獎瀏覽與抽獎)
3. [使用者管理](#3-使用者管理)
4. [地址管理](#4-地址管理)
5. [錢包與交易](#5-錢包與交易)
6. [獎品盒管理](#6-獎品盒管理)
7. [訂單管理](#7-訂單管理)
8. [店家查詢](#8-店家查詢)
9. [行政區劃](#9-行政區劃)
10. [新聞公告](#10-新聞公告)
11. [輪播橫幅](#11-輪播橫幅)
12. [跑馬燈](#12-跑馬燈)
13. [推薦碼驗證](#13-推薦碼驗證)
14. [儲值方案](#14-儲值方案)
15. [枚舉值查詢](#15-枚舉值查詢)

### 🔧 後台 API (`/admin/**`)
16. [後台認證](#16-後台認證)
17. [抽獎管理（含獎品）](#17-抽獎管理含獎品)
18. [獎品管理](#18-獎品管理)
19. [使用者管理](#19-使用者管理)
20. [店家管理](#20-店家管理)
21. [角色管理](#21-角色管理)
22. [選單管理](#22-選單管理)
23. [權限檢查](#23-權限檢查)
24. [推薦碼管理](#24-推薦碼管理)
25. [儲值方案管理](#25-儲值方案管理)
26. [錢包管理](#26-錢包管理)
27. [獎品盒管理](#27-獎品盒管理)
28. [報表分析](#28-報表分析)
29. [系統日誌](#29-系統日誌)
30. [檔案上傳](#30-檔案上傳)
31. [除錯工具](#31-除錯工具)

---

## 🔐 認證說明

### JWT Token 格式
所有需要認證的 API 都需要在 Header 中帶入 JWT Token：

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Token 內容結構
```json
{
  "sub": "user@example.com",
  "userId": "uuid-string",
  "userType": "user",  // "user" 或 "admin"
  "roles": ["ROLE_USER"],  // 或 ["ROLE_ADMIN", "ROLE_STORE_OWNER"]
  "exp": 1234567890,
  "iat": 1234567890
}
```

### 角色權限
- `ROLE_USER`: 一般使用者（前台）
- `ROLE_ADMIN`: 超級管理員（後台）
- `ROLE_STORE_OWNER`: 店家負責人（後台）
- `ROLE_STORE_EDITOR`: 店家編輯（後台）

---

# 🎯 前台 API

## 1. 認證與授權

### 1.1 使用者註冊
```http
POST /api/auth/register
Content-Type: application/json
```

**Request Body**:
```json
{
  "email": "user@example.com",        // 必填：信箱（唯一）
  "password": "password123",          // 必填：密碼（至少6位）
  "username": "使用者名稱",            // 必填：暱稱
  "phoneNumber": "0912345678",        // 選填：手機號碼
  "referralCode": "ABC123"            // 選填：推薦碼
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": "uuid-string",
    "email": "user@example.com",
    "username": "使用者名稱",
    "roles": ["ROLE_USER"],
    "tokenType": "Bearer",
    "expiresIn": 86400  // 秒（24小時）
  },
  "error": null,
  "meta": {
    "timestamp": "2025-12-26T10:30:00",
    "requestId": "uuid"
  }
}
```

**錯誤回應** (400 Bad Request):
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "EMAIL_ALREADY_EXISTS",
    "message": "此信箱已被註冊"
  },
  "meta": {
    "timestamp": "2025-12-26T10:30:00",
    "requestId": "uuid"
  }
}
```

---

### 1.2 使用者登入
```http
POST /api/auth/login
Content-Type: application/json
```

**Request Body**:
```json
{
  "email": "user@example.com",     // 必填：信箱
  "password": "password123"        // 必填：密碼
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": "uuid-string",
    "email": "user@example.com",
    "username": "使用者名稱",
    "roles": ["ROLE_USER"],
    "tokenType": "Bearer",
    "expiresIn": 86400
  },
  "error": null
}
```

---

### 1.3 OAuth2 登入（Google）
```http
GET /api/auth/oauth2/success?code={authorization_code}
```

**Query Parameters**:
- `code`: Google OAuth2 授權碼

**Response** (302 Redirect):
重定向到前端頁面，並在 URL 中帶入 token：
```
https://your-frontend.com/auth/callback?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## 2. 抽獎瀏覽與抽獎

### 2.1 查詢抽獎列表（公開）
```http
POST /api/lottery/browse/list
Content-Type: application/json
Authorization: Bearer {token}  // 可選，登入後可看更多資訊
```

**Request Body** (全部欄位可選):
```json
{
  "condition": {
    "title": "鬼滅",                    // 選填：標題關鍵字（模糊搜尋）
    "status": "ON_SHELF",              // 選填：ON_SHELF/OFF_SHELF
    "category": "OFFICIAL_ICHIBAN",    // 選填：分類
    "priceMin": 50,                    // 選填：最低單抽價格
    "priceMax": 100,                   // 選填：最高單抽價格
    "storeId": "uuid",                 // 選填：指定店家
    "createdAtStart": "2025-01-01T00:00:00",  // 選填：建立時間起
    "createdAtEnd": "2025-12-31T23:59:59",    // 選填：建立時間迄
    "keyword": "一番賞"                // 選填：全文搜尋關鍵字
  },
  "sortBy": "created_at",              // 選填：排序欄位
  "sortOrder": "DESC"                  // 選填：ASC/DESC
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "id": "lottery-uuid",
      "title": "鬼滅之刃一番賞",
      "description": "官方授權一番賞",
      "category": "OFFICIAL_ICHIBAN",
      "subCategory": "DEMON_SLAYER",
      "playMode": "LOTTERY_MODE",      // LOTTERY_MODE/SCRATCH_MODE
      "pricePerDraw": 80,
      "maxDraws": 100,
      "status": "ON_SHELF",
      "coverImageUrl": "https://s3.amazonaws.com/...",
      "storeId": "store-uuid",
      "storeName": "夢想抽獎店",
      "totalPrizeCount": 100,          // 獎品總數
      "remainingPrizeCount": 85,       // 剩餘獎品數
      "thanksgivingCount": 0,          // 謝謝惠顧數（僅刮刮樂）
      "progressPercentage": 15.0,      // 抽獎進度 (%)
      "createdAt": "2025-12-01T00:00:00",
      "updatedAt": "2025-12-26T10:00:00"
    }
  ],
  "error": null
}
```

---

### 2.2 查詢單一抽獎詳情
```http
GET /api/lottery/browse/{id}
Authorization: Bearer {token}  // 可選
```

**Path Parameters**:
- `id`: 抽獎活動 UUID

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "id": "lottery-uuid",
    "title": "鬼滅之刃刮刮樂",
    "description": "官方授權刮刮樂，含謝謝惠顧",
    "category": "OFFICIAL_SCRATCH",
    "subCategory": "DEMON_SLAYER",
    "playMode": "SCRATCH_MODE",
    "pricePerDraw": 80,
    "maxDraws": 100,
    "status": "ON_SHELF",
    "coverImageUrl": "https://s3.amazonaws.com/...",
    "additionalImages": [
      "https://s3.amazonaws.com/image1.jpg",
      "https://s3.amazonaws.com/image2.jpg"
    ],
    "storeId": "store-uuid",
    "storeName": "夢想抽獎店",
    "totalPrizeCount": 28,           // ⭐ 獎品總數
    "remainingPrizeCount": 20,       // ⭐ 剩餘獎品數
    "thanksgivingCount": 72,         // ⭐ 謝謝惠顧數（新增欄位）
    "progressPercentage": 28.57,     // ⭐ 抽獎進度
    "prizes": [
      {
        "id": "prize-uuid",
        "level": "A",
        "name": "炭治郎 景品",
        "description": "官方正版公仔",
        "type": "FIGURE",
        "imageUrl": "https://s3.amazonaws.com/prize.jpg",
        "quantity": 1,
        "remaining": 1,
        "orderNum": 1
      },
      {
        "id": "prize-uuid-2",
        "level": "B",
        "name": "禰豆子 景品",
        "description": "官方正版抱枕",
        "type": "PLUSH",
        "imageUrl": "https://s3.amazonaws.com/prize2.jpg",
        "quantity": 2,
        "remaining": 1,
        "orderNum": 2
      }
    ],
    "createdAt": "2025-12-01T00:00:00",
    "updatedAt": "2025-12-26T10:00:00"
  },
  "error": null
}
```

**說明**：
- `thanksgivingCount` = `maxDraws` - `totalPrizeCount`
- 一番賞模式：`thanksgivingCount` 永遠是 0（因為每個籤位都有獎品）
- 刮刮樂模式：`thanksgivingCount` >= 0（可以有謝謝惠顧）

---

### 2.3 查詢店家所有抽獎
```http
GET /api/lottery/browse/store/{storeId}
Authorization: Bearer {token}  // 可選
```

**Path Parameters**:
- `storeId`: 店家 UUID

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "id": "lottery-uuid",
      "title": "鬼滅之刃一番賞",
      "pricePerDraw": 80,
      "maxDraws": 100,
      "status": "ON_SHELF",
      // ... 其他欄位同 2.1
    }
  ],
  "error": null
}
```

---

### 2.4 抽獎（刮刮樂模式）
```http
POST /api/lottery/draw/{lotteryId}/draw
Content-Type: application/json
Authorization: Bearer {token}  // 必填
```

**Path Parameters**:
- `lotteryId`: 抽獎活動 UUID

**Request Body**:
```json
{
  "ticketPosition": 42  // 必填：籤位編號（1-100）
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "drawId": "draw-uuid",
    "lotteryId": "lottery-uuid",
    "lotteryTitle": "鬼滅之刃刮刮樂",
    "ticketPosition": 42,
    "result": "WIN",                     // WIN/THANKS
    "prize": {                           // result=WIN 時才有
      "id": "prize-uuid",
      "level": "B",
      "name": "禰豆子 景品",
      "description": "官方正版抱枕",
      "type": "PLUSH",
      "imageUrl": "https://s3.amazonaws.com/prize.jpg"
    },
    "pricePerDraw": 80,
    "userId": "user-uuid",
    "createdAt": "2025-12-26T10:30:00"
  },
  "error": null
}
```

**錯誤回應** (400 Bad Request):
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "TICKET_ALREADY_DRAWN",
    "message": "此籤位已被抽過"
  }
}
```

---

### 2.5 抽獎（一番賞模式 - 隨機）
```http
POST /api/lottery/random/{lotteryId}/draw
Content-Type: application/json
Authorization: Bearer {token}  // 必填
```

**Path Parameters**:
- `lotteryId`: 抽獎活動 UUID

**Request Body**:
```json
{
  "drawCount": 1  // 必填：抽幾抽（1-10）
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "draws": [
      {
        "drawId": "draw-uuid",
        "lotteryId": "lottery-uuid",
        "ticketPosition": 78,
        "result": "WIN",
        "prize": {
          "id": "prize-uuid",
          "level": "C",
          "name": "善逸 景品",
          "imageUrl": "https://s3.amazonaws.com/prize.jpg"
        },
        "pricePerDraw": 80,
        "createdAt": "2025-12-26T10:30:00"
      }
    ],
    "totalSpent": 80,
    "remainingBalance": 920
  },
  "error": null
}
```

---

### 2.6 查詢可用籤位（刮刮樂）
```http
GET /api/lottery/draw/{lotteryId}/tickets
Authorization: Bearer {token}  // 可選
```

**Path Parameters**:
- `lotteryId`: 抽獎活動 UUID

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "lotteryId": "lottery-uuid",
    "totalTickets": 100,
    "drawnTickets": 15,
    "availableTickets": 85,
    "tickets": [
      {
        "position": 1,
        "status": "AVAILABLE"   // AVAILABLE/DRAWN
      },
      {
        "position": 2,
        "status": "DRAWN"
      }
      // ... 共100個
    ]
  },
  "error": null
}
```

---

### 2.7 查詢抽獎場次資訊
```http
GET /api/lottery/draw/{lotteryId}/session
Authorization: Bearer {token}  // 可選
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "lotteryId": "lottery-uuid",
    "title": "鬼滅之刃刮刮樂",
    "playMode": "SCRATCH_MODE",
    "totalDraws": 100,
    "drawnCount": 15,
    "remainingCount": 85,
    "recentDraws": [
      {
        "position": 42,
        "result": "WIN",
        "prizeLevel": "B",
        "prizeName": "禰豆子 景品",
        "drawnAt": "2025-12-26T10:30:00"
      }
    ]
  },
  "error": null
}
```

---

## 3. 使用者管理

### 3.1 取得當前使用者資訊
```http
GET /api/user/me
Authorization: Bearer {token}  // 必填
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "id": "user-uuid",
    "email": "user@example.com",
    "username": "使用者名稱",
    "phoneNumber": "0912345678",
    "avatarUrl": "https://s3.amazonaws.com/avatar.jpg",
    "walletBalance": 1000,
    "bonusBalance": 50,
    "createdAt": "2025-01-01T00:00:00",
    "lastLoginAt": "2025-12-26T10:00:00"
  },
  "error": null
}
```

---

### 3.2 更新使用者資訊
```http
PUT /api/user/me
Content-Type: application/json
Authorization: Bearer {token}  // 必填
```

**Request Body**:
```json
{
  "username": "新的使用者名稱",      // 選填
  "phoneNumber": "0987654321",     // 選填
  "avatarUrl": "https://..."       // 選填
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "id": "user-uuid",
    "email": "user@example.com",
    "username": "新的使用者名稱",
    "phoneNumber": "0987654321",
    "avatarUrl": "https://...",
    "updatedAt": "2025-12-26T10:30:00"
  },
  "error": null
}
```

---

## 4. 地址管理

### 4.1 新增收件地址
```http
POST /api/user/addresses
Content-Type: application/json
Authorization: Bearer {token}  // 必填
```

**Request Body**:
```json
{
  "recipientName": "王小明",           // 必填：收件人姓名
  "phoneNumber": "0912345678",        // 必填：手機號碼
  "city": "台北市",                   // 必填：縣市
  "district": "信義區",               // 必填：區域
  "addressLine": "信義路五段7號",     // 必填：詳細地址
  "postalCode": "110",                // 選填：郵遞區號
  "isDefault": true                   // 選填：是否設為預設
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "id": "address-uuid",
    "userId": "user-uuid",
    "recipientName": "王小明",
    "phoneNumber": "0912345678",
    "city": "台北市",
    "district": "信義區",
    "addressLine": "信義路五段7號",
    "postalCode": "110",
    "isDefault": true,
    "createdAt": "2025-12-26T10:30:00"
  },
  "error": null
}
```

---

### 4.2 更新收件地址
```http
PUT /api/user/addresses/{id}
Content-Type: application/json
Authorization: Bearer {token}  // 必填
```

**Path Parameters**:
- `id`: 地址 UUID

**Request Body**: 同 4.1（全部欄位可選）

**Response** (200 OK): 同 4.1

---

### 4.3 刪除收件地址
```http
DELETE /api/user/addresses/{id}
Authorization: Bearer {token}  // 必填
```

**Path Parameters**:
- `id`: 地址 UUID

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "message": "地址已刪除"
  },
  "error": null
}
```

---

### 4.4 查詢單一地址
```http
GET /api/user/addresses/{id}
Authorization: Bearer {token}  // 必填
```

**Response** (200 OK): 同 4.1

---

### 4.5 查詢所有地址
```http
GET /api/user/addresses
Authorization: Bearer {token}  // 必填
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "id": "address-uuid-1",
      "recipientName": "王小明",
      "phoneNumber": "0912345678",
      "city": "台北市",
      "district": "信義區",
      "addressLine": "信義路五段7號",
      "isDefault": true
    },
    {
      "id": "address-uuid-2",
      "recipientName": "王小明",
      "phoneNumber": "0912345678",
      "city": "新北市",
      "district": "板橋區",
      "addressLine": "文化路一段188號",
      "isDefault": false
    }
  ],
  "error": null
}
```

---

### 4.6 查詢預設地址
```http
GET /api/user/addresses/default
Authorization: Bearer {token}  // 必填
```

**Response** (200 OK): 同 4.1

---

### 4.7 設定預設地址
```http
PUT /api/user/addresses/{id}/default
Authorization: Bearer {token}  // 必填
```

**Path Parameters**:
- `id`: 地址 UUID

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "message": "已設定為預設地址"
  },
  "error": null
}
```

---

## 5. 錢包與交易

### 5.1 查詢錢包餘額
```http
GET /api/wallet
Authorization: Bearer {token}  // 必填
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "userId": "user-uuid",
    "balance": 1000,           // 主錢包餘額
    "bonusBalance": 50,        // 紅利餘額
    "totalBalance": 1050,      // 總餘額
    "updatedAt": "2025-12-26T10:30:00"
  },
  "error": null
}
```

---

### 5.2 查詢交易記錄
```http
POST /api/wallet/transactions
Content-Type: application/json
Authorization: Bearer {token}  // 必填
```

**Request Body** (全部欄位可選):
```json
{
  "condition": {
    "type": "RECHARGE",               // 選填：RECHARGE/DRAW/REFUND/BONUS/SHIP
    "startDate": "2025-01-01T00:00:00",  // 選填：起始日期
    "endDate": "2025-12-31T23:59:59"     // 選填：結束日期
  },
  "sortBy": "created_at",
  "sortOrder": "DESC"
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "id": "transaction-uuid",
      "userId": "user-uuid",
      "type": "DRAW",
      "amount": -80,                   // 負數表示支出
      "balanceBefore": 1080,
      "balanceAfter": 1000,
      "description": "抽獎：鬼滅之刃刮刮樂",
      "relatedId": "draw-uuid",        // 關聯的 draw/order ID
      "createdAt": "2025-12-26T10:30:00"
    },
    {
      "id": "transaction-uuid-2",
      "type": "RECHARGE",
      "amount": 1000,
      "balanceBefore": 80,
      "balanceAfter": 1080,
      "description": "儲值：信用卡",
      "relatedId": "order-uuid",
      "createdAt": "2025-12-26T09:00:00"
    }
  ],
  "error": null
}
```

---

## 6. 獎品盒管理

### 6.1 查詢我的獎品盒
```http
GET /api/prize-box
Authorization: Bearer {token}  // 必填
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "id": "prize-box-uuid",
      "userId": "user-uuid",
      "drawId": "draw-uuid",
      "lotteryId": "lottery-uuid",
      "lotteryTitle": "鬼滅之刃刮刮樂",
      "prizeId": "prize-uuid",
      "prizeName": "禰豆子 景品",
      "prizeLevel": "B",
      "prizeType": "PLUSH",
      "prizeImageUrl": "https://s3.amazonaws.com/prize.jpg",
      "status": "IN_BOX",              // IN_BOX/SHIPPED/RECYCLED
      "shippedAt": null,
      "createdAt": "2025-12-26T10:30:00"
    }
  ],
  "error": null
}
```

---

### 6.2 查詢獎品盒統計
```http
GET /api/prize-box/summary
Authorization: Bearer {token}  // 必填
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "userId": "user-uuid",
    "totalPrizes": 10,
    "inBoxCount": 5,
    "shippedCount": 3,
    "recycledCount": 2
  },
  "error": null
}
```

---

### 6.3 申請寄送獎品
```http
POST /apiㄎ/ship
Content-Type: application/json
Authorization: Bearer {token}  // 必填
```

**Request Body**:
```json
{
  "prizeBoxIds": [               // 必填：獎品盒 UUID 陣列
    "prize-box-uuid-1",
    "prize-box-uuid-2"
  ],
  "addressId": "address-uuid"    // 必填：收件地址 UUID
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "orderId": "order-uuid",
    "prizeCount": 2,
    "recipientName": "王小明",
    "shippingAddress": "台北市信義區信義路五段7號",
    "status": "PENDING",
    "createdAt": "2025-12-26T10:30:00"
  },
  "error": null
}
```

---

### 6.4 回收獎品（換紅利）
```http
POST /api/prize-box/recycle
Content-Type: application/json
Authorization: Bearer {token}  // 必填
```

**Request Body**:
```json
{
  "prizeBoxIds": [               // 必填：獎品盒 UUID 陣列
    "prize-box-uuid-1",
    "prize-box-uuid-2"
  ]
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "recycledCount": 2,
    "bonusEarned": 20,             // 獲得紅利
    "newBonusBalance": 70
  },
  "error": null
}
```

---

## 7. 訂單管理

### 7.1 查詢訂單列表
```http
POST /api/order/list
Content-Type: application/json
Authorization: Bearer {token}  // 必填
```

**Request Body** (全部欄位可選):
```json
{
  "condition": {
    "status": "PENDING",                  // 選填：PENDING/SHIPPED/COMPLETED/CANCELLED
    "startDate": "2025-01-01T00:00:00",  // 選填：起始日期
    "endDate": "2025-12-31T23:59:59"     // 選填：結束日期
  },
  "sortBy": "created_at",
  "sortOrder": "DESC"
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "orderId": "order-uuid",
      "userId": "user-uuid",
      "type": "PRIZE_SHIP",            // PRIZE_SHIP/RECHARGE
      "status": "PENDING",
      "prizeCount": 2,
      "recipientName": "王小明",
      "shippingAddress": "台北市信義區信義路五段7號",
      "phoneNumber": "0912345678",
      "trackingNumber": null,
      "createdAt": "2025-12-26T10:30:00",
      "shippedAt": null
    }
  ],
  "error": null
}
```

---

### 7.2 查詢單一訂單
```http
GET /api/order/{orderId}
Authorization: Bearer {token}  // 必填
```

**Path Parameters**:
- `orderId`: 訂單 UUID

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "orderId": "order-uuid",
    "userId": "user-uuid",
    "type": "PRIZE_SHIP",
    "status": "SHIPPED",
    "prizeCount": 2,
    "prizes": [
      {
        "prizeBoxId": "prize-box-uuid-1",
        "prizeName": "禰豆子 景品",
        "prizeLevel": "B",
        "prizeImageUrl": "https://s3.amazonaws.com/prize.jpg"
      },
      {
        "prizeBoxId": "prize-box-uuid-2",
        "prizeName": "炭治郎 景品",
        "prizeLevel": "A",
        "prizeImageUrl": "https://s3.amazonaws.com/prize2.jpg"
      }
    ],
    "recipientName": "王小明",
    "shippingAddress": "台北市信義區信義路五段7號",
    "phoneNumber": "0912345678",
    "trackingNumber": "1234567890",
    "createdAt": "2025-12-26T10:30:00",
    "shippedAt": "2025-12-27T14:00:00"
  },
  "error": null
}
```

---

## 8. 店家查詢

### 8.1 查詢店家選項（下拉選單用）
```http
GET /api/stores/options
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "id": "store-uuid-1",
      "name": "夢想抽獎店",
      "status": "ACTIVE"
    },
    {
      "id": "store-uuid-2",
      "name": "幸運轉轉樂",
      "status": "ACTIVE"
    }
  ],
  "error": null
}
```

---

## 9. 行政區劃

### 9.1 查詢所有縣市
```http
GET /api/district/cities
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    "台北市",
    "新北市",
    "桃園市",
    "台中市",
    "台南市",
    "高雄市",
    "基隆市",
    "新竹市",
    "嘉義市"
  ],
  "error": null
}
```

---

### 9.2 查詢指定縣市的區域
```http
GET /api/district/districts/{city}
```

**Path Parameters**:
- `city`: 縣市名稱（例如：台北市）

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    "中正區",
    "大同區",
    "中山區",
    "松山區",
    "大安區",
    "萬華區",
    "信義區",
    "士林區",
    "北投區",
    "內湖區",
    "南港區",
    "文山區"
  ],
  "error": null
}
```

---

### 9.3 查詢行政區劃樹狀結構
```http
GET /api/district/tree
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "city": "台北市",
      "districts": [
        "中正區",
        "大同區",
        "中山區"
      ]
    },
    {
      "city": "新北市",
      "districts": [
        "板橋區",
        "三重區",
        "中和區"
      ]
    }
  ],
  "error": null
}
```

---

### 9.4 查詢所有行政區（含郵遞區號）
```http
GET /api/district/all
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "city": "台北市",
      "district": "中正區",
      "postalCode": "100"
    },
    {
      "city": "台北市",
      "district": "大同區",
      "postalCode": "103"
    }
  ],
  "error": null
}
```

---

## 10. 新聞公告

### 10.1 查詢新聞列表
```http
GET /api/news?page=1&size=10&status=PUBLISHED
```

**Query Parameters**:
- `page`: 頁碼（預設 1）
- `size`: 每頁數量（預設 10）
- `status`: 狀態（PUBLISHED/DRAFT，預設 PUBLISHED）

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "id": "news-uuid",
      "title": "聖誕節活動開跑",
      "summary": "聖誕節限定抽獎活動開跑，獎品豐富！",
      "coverImageUrl": "https://s3.amazonaws.com/news.jpg",
      "status": "PUBLISHED",
      "viewCount": 1500,
      "publishedAt": "2025-12-20T00:00:00",
      "createdAt": "2025-12-19T10:00:00"
    }
  ],
  "error": null
}
```

---

### 10.2 查詢單一新聞
```http
GET /api/news/{id}
```

**Path Parameters**:
- `id`: 新聞 UUID

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "id": "news-uuid",
    "title": "聖誕節活動開跑",
    "summary": "聖誕節限定抽獎活動開跑，獎品豐富！",
    "content": "完整新聞內容...",
    "coverImageUrl": "https://s3.amazonaws.com/news.jpg",
    "status": "PUBLISHED",
    "viewCount": 1501,                  // 自動 +1
    "publishedAt": "2025-12-20T00:00:00",
    "createdAt": "2025-12-19T10:00:00"
  },
  "error": null
}
```

---

## 11. 輪播橫幅

### 11.1 查詢首頁輪播橫幅
```http
GET /api/banner/carousel
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "id": "banner-uuid-1",
      "title": "聖誕節活動",
      "imageUrl": "https://s3.amazonaws.com/banner1.jpg",
      "linkUrl": "/lottery/christmas-special",
      "orderNum": 1,
      "status": "ACTIVE"
    },
    {
      "id": "banner-uuid-2",
      "title": "新年特惠",
      "imageUrl": "https://s3.amazonaws.com/banner2.jpg",
      "linkUrl": "/lottery/new-year-sale",
      "orderNum": 2,
      "status": "ACTIVE"
    }
  ],
  "error": null
}
```

---

## 12. 跑馬燈

### 12.1 查詢跑馬燈訊息
```http
GET /api/marquee
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "id": "marquee-uuid",
      "content": "恭喜 王小明 抽中 鬼滅之刃一番賞 A賞！",
      "type": "DRAW_RESULT",
      "createdAt": "2025-12-26T10:30:00"
    }
  ],
  "error": null
}
```

---

## 13. 推薦碼驗證

### 13.1 驗證推薦碼（註冊前檢查）
```http
GET /api/auth/referral-code/validate/{code}
```

**Path Parameters**:
- `code`: 推薦碼（例如：ABC123）

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "valid": true,
    "message": "推薦碼有效"
  },
  "error": null
}
```

**錯誤回應** (400 Bad Request):
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "REFERRAL_CODE_INVALID",
    "message": "推薦碼不存在或已過期"
  }
}
```

---

### 13.2 查詢推薦碼資訊
```http
GET /api/auth/referral-code/info/{code}
```

**Path Parameters**:
- `code`: 推薦碼

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "code": "ABC123",
    "storeId": "store-uuid",
    "storeName": "夢想抽獎店",
    "bonusAmount": 50,          // 註冊可獲得的紅利
    "isActive": true,
    "expiresAt": "2026-12-31T23:59:59"
  },
  "error": null
}
```

---

## 14. 儲值方案

### 14.1 查詢所有儲值方案
```http
GET /api/recharge-plan/list
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "id": "plan-uuid-1",
      "name": "基礎方案",
      "amount": 100,
      "bonusAmount": 0,
      "totalAmount": 100,
      "description": "基礎儲值方案",
      "isActive": true,
      "orderNum": 1
    },
    {
      "id": "plan-uuid-2",
      "name": "超值方案",
      "amount": 500,
      "bonusAmount": 50,
      "totalAmount": 550,
      "description": "儲值500送50",
      "isActive": true,
      "orderNum": 2
    }
  ],
  "error": null
}
```

---

### 14.2 查詢單一儲值方案
```http
GET /api/recharge-plan/{id}
```

**Path Parameters**:
- `id`: 方案 UUID

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "id": "plan-uuid",
    "name": "超值方案",
    "amount": 500,
    "bonusAmount": 50,
    "totalAmount": 550,
    "description": "儲值500送50",
    "isActive": true,
    "createdAt": "2025-01-01T00:00:00"
  },
  "error": null
}
```

---

## 15. 枚舉值查詢

### 15.1 查詢所有枚舉值
```http
GET /api/enums/all
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "prizeLevel": [
      {"value": "A", "label": "A賞", "order": 1},
      {"value": "B", "label": "B賞", "order": 2},
      {"value": "C", "label": "C賞", "order": 3}
    ],
    "prizeType": [
      {"value": "FIGURE", "label": "公仔"},
      {"value": "PLUSH", "label": "娃娃"},
      {"value": "POSTER", "label": "海報"}
    ],
    "storeStatus": [
      {"value": "ACTIVE", "label": "營業中"},
      {"value": "INACTIVE", "label": "暫停營業"}
    ],
    "newsStatus": [
      {"value": "DRAFT", "label": "草稿"},
      {"value": "PUBLISHED", "label": "已發布"}
    ]
  },
  "error": null
}
```

---

### 15.2 查詢獎品等級枚舉
```http
GET /api/enums/prize-level
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {"value": "A", "label": "A賞", "order": 1},
    {"value": "B", "label": "B賞", "order": 2},
    {"value": "C", "label": "C賞", "order": 3},
    {"value": "D", "label": "D賞", "order": 4},
    {"value": "E", "label": "E賞", "order": 5}
  ],
  "error": null
}
```

---

### 15.3 查詢獎品類型枚舉
```http
GET /api/enums/prize-type
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {"value": "FIGURE", "label": "公仔"},
    {"value": "PLUSH", "label": "娃娃"},
    {"value": "POSTER", "label": "海報"},
    {"value": "KEYCHAIN", "label": "鑰匙圈"},
    {"value": "STATIONERY", "label": "文具"}
  ],
  "error": null
}
```

---

# 🔧 後台 API

## 16. 後台認證

### 16.1 後台登入
```http
POST /api/auth/admin/login
Content-Type: application/json
```

**Request Body**:
```json
{
  "email": "admin@kuji.com",     // 必填：管理員信箱
  "password": "admin123"         // 必填：密碼
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": "admin-uuid",
    "email": "admin@kuji.com",
    "username": "系統管理員",
    "roles": ["ROLE_ADMIN"],       // 或 ["ROLE_STORE_OWNER"]
    "storeId": null,               // StoreOwner 才有
    "tokenType": "Bearer",
    "expiresIn": 86400
  },
  "error": null
}
```

---

## 17. 抽獎管理（含獎品）

### 17.1 新增抽獎活動（含獎品）
```http
POST /api/admin/lottery-with-prizes
Content-Type: application/json
Authorization: Bearer {admin_token}  // 必填
```

**權限要求**: `ROLE_ADMIN`, `ROLE_STORE_OWNER`, `ROLE_STORE_EDITOR`

**Request Body**:
```json
{
  "lottery": {
    "title": "鬼滅之刃刮刮樂",             // 必填：抽獎標題
    "description": "官方授權刮刮樂",       // 選填：描述
    "category": "OFFICIAL_SCRATCH",       // 必填：分類
    "subCategory": "DEMON_SLAYER",        // 選填：子分類
    "playMode": "SCRATCH_MODE",           // 必填：LOTTERY_MODE/SCRATCH_MODE
    "pricePerDraw": 80,                   // 必填：單抽價格
    "maxDraws": 100,                      // 條件必填：刮刮樂必須傳入
    "status": "ON_SHELF",                 // 必填：ON_SHELF/OFF_SHELF
    "coverImageUrl": "https://...",       // 必填：封面圖
    "additionalImages": [                 // 選填：額外圖片
      "https://...",
      "https://..."
    ],
    "storeId": "store-uuid"               // Admin必填，StoreOwner自動帶入
  },
  "prizes": [
    {
      "level": "A",                       // 必填：A/B/C/D/E/F/G/LAST
      "name": "炭治郎 景品",              // 必填：獎品名稱
      "description": "官方正版公仔",      // 選填：描述
      "type": "FIGURE",                   // 必填：FIGURE/PLUSH/POSTER等
      "imageUrl": "https://...",          // 必填：獎品圖片
      "quantity": 1,                      // 必填：數量
      "orderNum": 1                       // 必填：排序
    },
    {
      "level": "B",
      "name": "禰豆子 景品",
      "description": "官方正版抱枕",
      "type": "PLUSH",
      "imageUrl": "https://...",
      "quantity": 2,
      "orderNum": 2
    }
    // ... 可新增多個獎品
  ]
}
```

**關鍵規則**：
1. **一番賞模式** (`LOTTERY_MODE`):
   - `maxDraws` 不建議傳入（後端會自動計算 = 獎品總數）
   - 每個籤位都有獎品，不允許謝謝惠顧
   - 獎品總數必須等於 maxDraws

2. **刮刮樂模式** (`SCRATCH_MODE`):
   - `maxDraws` **必須傳入**（支援謝謝惠顧）
   - `maxDraws` 必須 >= 獎品總數
   - 剩餘的籤位會是謝謝惠顧
   - 範例：maxDraws=100, 獎品28個 → 72個謝謝惠顧

3. **StoreID 規則**:
   - `ROLE_ADMIN`: 必須明確提供 `storeId`
   - `ROLE_STORE_OWNER`: 自動從 JWT Token 取得，不用傳

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "id": "lottery-uuid",
    "title": "鬼滅之刃刮刮樂",
    "description": "官方授權刮刮樂",
    "category": "OFFICIAL_SCRATCH",
    "subCategory": "DEMON_SLAYER",
    "playMode": "SCRATCH_MODE",
    "pricePerDraw": 80,
    "maxDraws": 100,                   // ⭐ 後端自動計算或使用前端值
    "status": "ON_SHELF",
    "coverImageUrl": "https://...",
    "storeId": "store-uuid",
    "totalPrizeCount": 28,             // ⭐ 獎品總數
    "remainingPrizeCount": 28,         // ⭐ 剩餘獎品數
    "thanksgivingCount": 72,           // ⭐ 謝謝惠顧數（新增欄位）
    "progressPercentage": 0.0,
    "prizes": [
      {
        "id": "prize-uuid-1",
        "level": "A",
        "name": "炭治郎 景品",
        "description": "官方正版公仔",
        "type": "FIGURE",
        "imageUrl": "https://...",
        "quantity": 1,
        "remaining": 1,
        "orderNum": 1
      },
      {
        "id": "prize-uuid-2",
        "level": "B",
        "name": "禰豆子 景品",
        "description": "官方正版抱枕",
        "type": "PLUSH",
        "imageUrl": "https://...",
        "quantity": 2,
        "remaining": 2,
        "orderNum": 2
      }
    ],
    "createdAt": "2025-12-26T10:30:00",
    "updatedAt": "2025-12-26T10:30:00"
  },
  "error": null
}
```

**錯誤回應範例**:

1. **刮刮樂模式：總抽數小於獎品總數**
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "INVALID_MAX_DRAWS",
    "message": "刮刮樂模式錯誤：總抽數(50)不能小於獎品總數(100)！"
  }
}
```

2. **StoreOwner 無法取得店家資訊**
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "STORE_ID_REQUIRED",
    "message": "無法取得店家資訊"
  }
}
```

---

### 17.2 更新抽獎活動（含獎品）
```http
PUT /api/admin/lottery-with-prizes/{lotteryId}
Content-Type: application/json
Authorization: Bearer {admin_token}  // 必填
```

**權限要求**: `ROLE_ADMIN`, `ROLE_STORE_OWNER`, `ROLE_STORE_EDITOR`

**Path Parameters**:
- `lotteryId`: 抽獎活動 UUID

**Request Body**: 同 17.1（全部欄位可選）

**Response** (200 OK): 同 17.1

---

### 17.3 查詢單一抽獎（後台）
```http
GET /api/admin/lottery-with-prizes/{lotteryId}
Authorization: Bearer {admin_token}  // 必填
```

**權限要求**: `ROLE_ADMIN`, `ROLE_STORE_OWNER`, `ROLE_STORE_EDITOR`

**Path Parameters**:
- `lotteryId`: 抽獎活動 UUID

**Response** (200 OK): 同 17.1

---

### 17.4 查詢抽獎列表（後台）
```http
POST /api/admin/lottery-with-prizes/list
Content-Type: application/json
Authorization: Bearer {admin_token}  // 必填
```

**權限要求**: `ROLE_ADMIN`, `ROLE_STORE_OWNER`, `ROLE_STORE_EDITOR`

**Request Body** (全部欄位可選):
```json
{
  "condition": {
    "title": "鬼滅",                    // 選填：標題關鍵字
    "status": "ON_SHELF",              // 選填：ON_SHELF/OFF_SHELF
    "category": "OFFICIAL_ICHIBAN",    // 選填：分類
    "storeId": "uuid",                 // Admin可指定，StoreOwner自動帶入
    "createdAtStart": "2025-01-01T00:00:00",
    "createdAtEnd": "2025-12-31T23:59:59",
    "keyword": "一番賞"
  },
  "sortBy": "created_at",
  "sortOrder": "DESC"
}
```

**StoreID 規則**:
- `ROLE_ADMIN`: 可查詢所有店家（不傳 storeId）或指定店家
- `ROLE_STORE_OWNER`: 自動過濾為自己的店家

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "id": "lottery-uuid",
      "title": "鬼滅之刃刮刮樂",
      "playMode": "SCRATCH_MODE",
      "pricePerDraw": 80,
      "maxDraws": 100,
      "status": "ON_SHELF",
      "storeId": "store-uuid",
      "storeName": "夢想抽獎店",
      "totalPrizeCount": 28,
      "remainingPrizeCount": 20,
      "thanksgivingCount": 72,         // ⭐ 謝謝惠顧數
      "progressPercentage": 28.57,
      "createdAt": "2025-12-01T00:00:00"
    }
  ],
  "error": null
}
```

---

## 18. 獎品管理

### 18.1 新增單一獎品
```http
POST /api/admin/lotteries/{lotteryId}/prizes
Content-Type: application/json
Authorization: Bearer {admin_token}  // 必填
```

**權限要求**: `ROLE_ADMIN`, `ROLE_STORE_OWNER`, `ROLE_STORE_EDITOR`

**Path Parameters**:
- `lotteryId`: 抽獎活動 UUID

**Request Body**:
```json
{
  "level": "C",
  "name": "善逸 景品",
  "description": "官方正版鑰匙圈",
  "type": "KEYCHAIN",
  "imageUrl": "https://...",
  "quantity": 5,
  "orderNum": 3
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "id": "prize-uuid",
    "lotteryId": "lottery-uuid",
    "level": "C",
    "name": "善逸 景品",
    "description": "官方正版鑰匙圈",
    "type": "KEYCHAIN",
    "imageUrl": "https://...",
    "quantity": 5,
    "remaining": 5,
    "orderNum": 3,
    "createdAt": "2025-12-26T10:30:00"
  },
  "error": null
}
```

---

### 18.2 批次新增獎品
```http
POST /api/admin/lotteries/{lotteryId}/prizes/batch
Content-Type: application/json
Authorization: Bearer {admin_token}  // 必填
```

**Request Body**:
```json
{
  "prizes": [
    {
      "level": "D",
      "name": "伊之助 景品",
      "type": "FIGURE",
      "imageUrl": "https://...",
      "quantity": 10,
      "orderNum": 4
    },
    {
      "level": "E",
      "name": "我妻善逸 景品",
      "type": "POSTER",
      "imageUrl": "https://...",
      "quantity": 10,
      "orderNum": 5
    }
  ]
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "addedCount": 2,
    "prizes": [...]
  },
  "error": null
}
```

---

### 18.3 更新獎品
```http
PUT /api/admin/lotteries/prizes/{prizeId}
Content-Type: application/json
Authorization: Bearer {admin_token}  // 必填
```

**Path Parameters**:
- `prizeId`: 獎品 UUID

**Request Body**: 同 18.1（全部欄位可選）

**Response** (200 OK): 同 18.1

---

### 18.4 刪除獎品
```http
DELETE /api/admin/lotteries/prizes/{prizeId}
Authorization: Bearer {admin_token}  // 必填
```

**Path Parameters**:
- `prizeId`: 獎品 UUID

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "message": "獎品已刪除"
  },
  "error": null
}
```

---

### 18.5 查詢單一獎品
```http
GET /api/admin/lotteries/prizes/{prizeId}
Authorization: Bearer {admin_token}  // 必填
```

**Response** (200 OK): 同 18.1

---

### 18.6 查詢抽獎的所有獎品
```http
GET /api/admin/lotteries/{lotteryId}/prizes
Authorization: Bearer {admin_token}  // 必填
```

**Path Parameters**:
- `lotteryId`: 抽獎活動 UUID

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "id": "prize-uuid-1",
      "level": "A",
      "name": "炭治郎 景品",
      "quantity": 1,
      "remaining": 1
    },
    {
      "id": "prize-uuid-2",
      "level": "B",
      "name": "禰豆子 景品",
      "quantity": 2,
      "remaining": 1
    }
  ],
  "error": null
}
```

---

### 18.7 重置獎品數量
```http
POST /api/admin/lotteries/{lotteryId}/prizes/reset
Authorization: Bearer {admin_token}  // 必填
```

**Path Parameters**:
- `lotteryId`: 抽獎活動 UUID

**說明**: 將所有獎品的 `remaining` 重置為 `quantity`

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "message": "獎品數量已重置",
    "resetCount": 5
  },
  "error": null
}
```

---

## 19. 使用者管理

### 19.1 新增店家負責人
```http
POST /api/admin/users/store-owner
Content-Type: application/json
Authorization: Bearer {admin_token}  // 必填
```

**權限要求**: `ROLE_ADMIN`

**Request Body**:
```json
{
  "email": "owner@example.com",      // 必填：信箱（唯一）
  "password": "password123",         // 必填：密碼
  "username": "店家負責人",          // 必填：姓名
  "phoneNumber": "0912345678",       // 選填：手機號碼
  "storeId": "store-uuid"            // 必填：所屬店家
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "id": "admin-user-uuid",
    "email": "owner@example.com",
    "username": "店家負責人",
    "phoneNumber": "0912345678",
    "roleCode": "ROLE_STORE_OWNER",
    "status": "ACTIVE",
    "storeId": "store-uuid",
    "createdAt": "2025-12-26T10:30:00"
  },
  "error": null
}
```

---

### 19.2 新增店家編輯
```http
POST /api/admin/users/store-editor
Content-Type: application/json
Authorization: Bearer {admin_token}  // 必填
```

**權限要求**: `ROLE_ADMIN`, `ROLE_STORE_OWNER`

**Request Body**: 同 19.1

**Response** (200 OK): 同 19.1（roleCode 為 `ROLE_STORE_EDITOR`）

---

### 19.3 查詢單一使用者
```http
GET /api/admin/users/{id}
Authorization: Bearer {admin_token}  // 必填
```

**權限要求**: `ROLE_ADMIN`

**Path Parameters**:
- `id`: 使用者 UUID

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "id": "admin-user-uuid",
    "email": "owner@example.com",
    "username": "店家負責人",
    "phoneNumber": "0912345678",
    "roleCode": "ROLE_STORE_OWNER",
    "status": "ACTIVE",
    "storeId": "store-uuid",
    "storeName": "夢想抽獎店",
    "createdAt": "2025-12-26T10:30:00",
    "lastLoginAt": "2025-12-26T09:00:00"
  },
  "error": null
}
```

---

### 19.4 查詢所有使用者
```http
GET /api/admin/users?page=1&size=20&roleCode=ROLE_STORE_OWNER&status=ACTIVE
Authorization: Bearer {admin_token}  // 必填
```

**權限要求**: `ROLE_ADMIN`

**Query Parameters**:
- `page`: 頁碼（預設 1）
- `size`: 每頁數量（預設 20）
- `roleCode`: 角色過濾（選填）
- `status`: 狀態過濾（ACTIVE/INACTIVE，選填）

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "id": "admin-user-uuid-1",
      "email": "owner1@example.com",
      "username": "店家負責人1",
      "roleCode": "ROLE_STORE_OWNER",
      "status": "ACTIVE",
      "storeId": "store-uuid-1",
      "storeName": "夢想抽獎店"
    },
    {
      "id": "admin-user-uuid-2",
      "email": "editor1@example.com",
      "username": "店家編輯1",
      "roleCode": "ROLE_STORE_EDITOR",
      "status": "ACTIVE",
      "storeId": "store-uuid-1",
      "storeName": "夢想抽獎店"
    }
  ],
  "error": null
}
```

---

### 19.5 查詢店家的所有使用者
```http
GET /api/admin/users/by-store/{storeId}
Authorization: Bearer {admin_token}  // 必填
```

**權限要求**: `ROLE_ADMIN`, `ROLE_STORE_OWNER`

**Path Parameters**:
- `storeId`: 店家 UUID

**Response** (200 OK): 同 19.4

---

### 19.6 啟用使用者
```http
POST /api/admin/users/{id}/activate
Authorization: Bearer {admin_token}  // 必填
```

**權限要求**: `ROLE_ADMIN`

**Path Parameters**:
- `id`: 使用者 UUID

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "message": "使用者已啟用",
    "userId": "admin-user-uuid",
    "status": "ACTIVE"
  },
  "error": null
}
```

---

### 19.7 停用使用者
```http
POST /api/admin/users/{id}/deactivate
Authorization: Bearer {admin_token}  // 必填
```

**權限要求**: `ROLE_ADMIN`

**Path Parameters**:
- `id`: 使用者 UUID

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "message": "使用者已停用",
    "userId": "admin-user-uuid",
    "status": "INACTIVE"
  },
  "error": null
}
```

---

### 19.8 重置使用者密碼
```http
POST /api/admin/users/{id}/reset-password
Content-Type: application/json
Authorization: Bearer {admin_token}  // 必填
```

**權限要求**: `ROLE_ADMIN`

**Path Parameters**:
- `id`: 使用者 UUID

**Request Body**:
```json
{
  "newPassword": "newpassword123"  // 必填：新密碼
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "message": "密碼已重置"
  },
  "error": null
}
```

---

### 19.9 刪除使用者
```http
DELETE /api/admin/users/{id}
Authorization: Bearer {admin_token}  // 必填
```

**權限要求**: `ROLE_ADMIN`

**Path Parameters**:
- `id`: 使用者 UUID

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "message": "使用者已刪除"
  },
  "error": null
}
```

---

## 20. 店家管理

### 20.1 查詢店家選項（後台下拉選單）
```http
GET /api/admin/stores/options
Authorization: Bearer {admin_token}  // 必填
```

**權限要求**: `ROLE_ADMIN`, `ROLE_STORE_OWNER`

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "id": "store-uuid-1",
      "name": "夢想抽獎店",
      "status": "ACTIVE"
    },
    {
      "id": "store-uuid-2",
      "name": "幸運轉轉樂",
      "status": "ACTIVE"
    }
  ],
  "error": null
}
```

**說明**:
- `ROLE_ADMIN`: 返回所有店家
- `ROLE_STORE_OWNER`: 只返回自己的店家

---

### 20.2 搜尋店家
```http
GET /api/admin/stores/search?keyword=夢想&status=ACTIVE
Authorization: Bearer {admin_token}  // 必填
```

**權限要求**: `ROLE_ADMIN`

**Query Parameters**:
- `keyword`: 關鍵字（選填）
- `status`: 狀態（ACTIVE/INACTIVE，選填）

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "id": "store-uuid",
      "name": "夢想抽獎店",
      "description": "專業一番賞抽獎店",
      "address": "台北市信義區信義路五段7號",
      "phoneNumber": "02-12345678",
      "status": "ACTIVE",
      "createdAt": "2025-01-01T00:00:00"
    }
  ],
  "error": null
}
```

---

## 21. 角色管理

### 21.1 新增角色
```http
POST /api/admin/roles
Content-Type: application/json
Authorization: Bearer {admin_token}  // 必填
```

**權限要求**: `ROLE_ADMIN`

**Request Body**:
```json
{
  "code": "ROLE_CUSTOM",
  "name": "自訂角色",
  "description": "自訂角色說明"
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "id": "role-uuid",
    "code": "ROLE_CUSTOM",
    "name": "自訂角色",
    "description": "自訂角色說明",
    "createdAt": "2025-12-26T10:30:00"
  },
  "error": null
}
```

---

### 21.2 更新角色
```http
PUT /api/admin/roles
Content-Type: application/json
Authorization: Bearer {admin_token}  // 必填
```

**Request Body**:
```json
{
  "id": "role-uuid",
  "name": "更新後的角色名稱",
  "description": "更新後的說明"
}
```

**Response** (200 OK): 同 21.1

---

### 21.3 刪除角色
```http
DELETE /api/admin/roles/{id}
Authorization: Bearer {admin_token}  // 必填
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "message": "角色已刪除"
  },
  "error": null
}
```

---

### 21.4 查詢所有角色
```http
GET /api/admin/roles
Authorization: Bearer {admin_token}  // 必填
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "id": "role-uuid-1",
      "code": "ROLE_ADMIN",
      "name": "超級管理員",
      "description": "擁有所有權限"
    },
    {
      "id": "role-uuid-2",
      "code": "ROLE_STORE_OWNER",
      "name": "店家負責人",
      "description": "管理店家抽獎活動"
    }
  ],
  "error": null
}
```

---

### 21.5 設定角色權限
```http
POST /api/admin/roles/permissions
Content-Type: application/json
Authorization: Bearer {admin_token}  // 必填
```

**Request Body**:
```json
{
  "roleId": "role-uuid",
  "menuIds": [
    "menu-uuid-1",
    "menu-uuid-2",
    "menu-uuid-3"
  ]
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "message": "角色權限已更新",
    "roleId": "role-uuid",
    "menuCount": 3
  },
  "error": null
}
```

---

## 22. 選單管理

### 22.1 新增選單
```http
POST /api/admin/menus
Content-Type: application/json
Authorization: Bearer {admin_token}  // 必填
```

**權限要求**: `ROLE_ADMIN`

**Request Body**:
```json
{
  "code": "lottery-management",
  "name": "抽獎管理",
  "icon": "lottery-icon",
  "path": "/admin/lottery",
  "parentId": null,              // 頂層選單為 null
  "orderNum": 1,
  "isVisible": true
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "id": "menu-uuid",
    "code": "lottery-management",
    "name": "抽獎管理",
    "icon": "lottery-icon",
    "path": "/admin/lottery",
    "parentId": null,
    "orderNum": 1,
    "isVisible": true,
    "createdAt": "2025-12-26T10:30:00"
  },
  "error": null
}
```

---

### 22.2 更新選單
```http
PUT /api/admin/menus
Content-Type: application/json
Authorization: Bearer {admin_token}  // 必填
```

**Request Body**: 同 22.1（需包含 id）

**Response** (200 OK): 同 22.1

---

### 22.3 刪除選單
```http
DELETE /api/admin/menus/{id}
Authorization: Bearer {admin_token}  // 必填
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "message": "選單已刪除"
  },
  "error": null
}
```

---

### 22.4 查詢所有選單
```http
GET /api/admin/menus
Authorization: Bearer {admin_token}  // 必填
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "id": "menu-uuid-1",
      "code": "lottery-management",
      "name": "抽獎管理",
      "icon": "lottery-icon",
      "path": "/admin/lottery",
      "parentId": null,
      "orderNum": 1,
      "isVisible": true
    }
  ],
  "error": null
}
```

---

### 22.5 查詢選單樹狀結構
```http
GET /api/admin/menus/tree
Authorization: Bearer {admin_token}  // 必填
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "id": "menu-uuid-1",
      "code": "lottery-management",
      "name": "抽獎管理",
      "icon": "lottery-icon",
      "path": "/admin/lottery",
      "orderNum": 1,
      "children": [
        {
          "id": "menu-uuid-2",
          "code": "lottery-list",
          "name": "抽獎列表",
          "icon": "list-icon",
          "path": "/admin/lottery/list",
          "orderNum": 1,
          "children": []
        },
        {
          "id": "menu-uuid-3",
          "code": "lottery-create",
          "name": "新增抽獎",
          "icon": "plus-icon",
          "path": "/admin/lottery/create",
          "orderNum": 2,
          "children": []
        }
      ]
    }
  ],
  "error": null
}
```

---

### 22.6 查詢當前使用者可存取的選單
```http
GET /api/admin/menus/accessible
Authorization: Bearer {admin_token}  // 必填
```

**說明**: 根據當前使用者的角色返回有權限的選單

**Response** (200 OK): 同 22.5（樹狀結構）

---

## 23. 權限檢查

### 23.1 檢查選單權限
```http
GET /api/admin/permissions/check/{menuCode}
Authorization: Bearer {admin_token}  // 必填
```

**Path Parameters**:
- `menuCode`: 選單代碼（例如：lottery-management）

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "hasPermission": true
  },
  "error": null
}
```

---

### 23.2 檢查是否可檢視
```http
GET /api/admin/permissions/can-view/{menuCode}
Authorization: Bearer {admin_token}  // 必填
```

**Response** (200 OK): 同 23.1

---

### 23.3 檢查是否可編輯
```http
GET /api/admin/permissions/can-edit/{menuCode}
Authorization: Bearer {admin_token}  // 必填
```

**Response** (200 OK): 同 23.1

---

### 23.4 檢查是否可刪除
```http
GET /api/admin/permissions/can-delete/{menuCode}
Authorization: Bearer {admin_token}  // 必填
```

**Response** (200 OK): 同 23.1

---

### 23.5 查詢當前使用者角色
```http
GET /api/admin/permissions/roles
Authorization: Bearer {admin_token}  // 必填
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "roles": ["ROLE_ADMIN"]
  },
  "error": null
}
```

---

### 23.6 檢查是否為管理員
```http
GET /api/admin/permissions/is-admin
Authorization: Bearer {admin_token}  // 必填
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "isAdmin": true
  },
  "error": null
}
```

---

### 23.7 查詢可存取的店家
```http
GET /api/admin/permissions/accessible-stores
Authorization: Bearer {admin_token}  // 必填
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "id": "store-uuid-1",
      "name": "夢想抽獎店"
    },
    {
      "id": "store-uuid-2",
      "name": "幸運轉轉樂"
    }
  ],
  "error": null
}
```

**說明**:
- `ROLE_ADMIN`: 返回所有店家
- `ROLE_STORE_OWNER`: 只返回自己的店家

---

## 24. 推薦碼管理

### 24.1 新增推薦碼
```http
POST /api/admin/referral-codes
Content-Type: application/json
Authorization: Bearer {admin_token}  // 必填
```

**權限要求**: `ROLE_ADMIN`, `ROLE_STORE_OWNER`

**Request Body**:
```json
{
  "code": "ABC123",                   // 必填：推薦碼（唯一）
  "storeId": "store-uuid",            // Admin必填，StoreOwner自動帶入
  "bonusAmount": 50,                  // 必填：註冊獎勵紅利
  "description": "聖誕節活動推薦碼",   // 選填：說明
  "maxUses": 100,                     // 選填：最大使用次數（null=無限制）
  "expiresAt": "2026-12-31T23:59:59"  // 選填：過期時間（null=永久）
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "id": "referral-code-uuid",
    "code": "ABC123",
    "storeId": "store-uuid",
    "storeName": "夢想抽獎店",
    "bonusAmount": 50,
    "description": "聖誕節活動推薦碼",
    "maxUses": 100,
    "usedCount": 0,
    "isActive": true,
    "expiresAt": "2026-12-31T23:59:59",
    "createdAt": "2025-12-26T10:30:00"
  },
  "error": null
}
```

---

### 24.2 更新推薦碼
```http
PUT /api/admin/referral-codes/{id}
Content-Type: application/json
Authorization: Bearer {admin_token}  // 必填
```

**Request Body**: 同 24.1（全部欄位可選，不含 code）

**Response** (200 OK): 同 24.1

---

### 24.3 刪除推薦碼
```http
DELETE /api/admin/referral-codes/{id}
Authorization: Bearer {admin_token}  // 必填
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "message": "推薦碼已刪除"
  },
  "error": null
}
```

---

### 24.4 查詢所有推薦碼
```http
GET /api/admin/referral-codes
Authorization: Bearer {admin_token}  // 必填
```

**權限要求**: `ROLE_ADMIN`

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "id": "referral-code-uuid-1",
      "code": "ABC123",
      "storeId": "store-uuid-1",
      "storeName": "夢想抽獎店",
      "bonusAmount": 50,
      "maxUses": 100,
      "usedCount": 25,
      "isActive": true,
      "expiresAt": "2026-12-31T23:59:59"
    }
  ],
  "error": null
}
```

---

### 24.5 查詢店家的推薦碼
```http
GET /api/admin/referral-codes/store/{storeId}
Authorization: Bearer {admin_token}  // 必填
```

**權限要求**: `ROLE_ADMIN`

**Path Parameters**:
- `storeId`: 店家 UUID

**Response** (200 OK): 同 24.4

---

### 24.6 查詢我的店家推薦碼
```http
GET /api/admin/referral-codes/my-store
Authorization: Bearer {admin_token}  // 必填
```

**權限要求**: `ROLE_STORE_OWNER`

**說明**: 自動查詢當前店家負責人的店家推薦碼

**Response** (200 OK): 同 24.4

---

### 24.7 查詢推薦碼使用記錄
```http
GET /api/admin/referral-codes/{id}/records
Authorization: Bearer {admin_token}  // 必填
```

**Path Parameters**:
- `id`: 推薦碼 UUID

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "id": "record-uuid",
      "referralCodeId": "referral-code-uuid",
      "code": "ABC123",
      "userId": "user-uuid",
      "userEmail": "user@example.com",
      "bonusEarned": 50,
      "usedAt": "2025-12-26T10:30:00"
    }
  ],
  "error": null
}
```

---

## 25. 儲值方案管理

### 25.1 新增儲值方案
```http
POST /api/admin/recharge-plan
Content-Type: application/json
Authorization: Bearer {admin_token}  // 必填
```

**權限要求**: `ROLE_ADMIN`

**Request Body**:
```json
{
  "name": "超值方案",              // 必填：方案名稱
  "amount": 500,                  // 必填：儲值金額
  "bonusAmount": 50,              // 必填：贈送紅利（可為0）
  "description": "儲值500送50",    // 選填：說明
  "orderNum": 2                   // 必填：排序
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "id": "plan-uuid",
    "name": "超值方案",
    "amount": 500,
    "bonusAmount": 50,
    "totalAmount": 550,
    "description": "儲值500送50",
    "isActive": true,
    "orderNum": 2,
    "createdAt": "2025-12-26T10:30:00"
  },
  "error": null
}
```

---

### 25.2 更新儲值方案
```http
PUT /api/admin/recharge-plan/{id}
Content-Type: application/json
Authorization: Bearer {admin_token}  // 必填
```

**Request Body**: 同 25.1（全部欄位可選）

**Response** (200 OK): 同 25.1

---

### 25.3 刪除儲值方案
```http
DELETE /api/admin/recharge-plan/{id}
Authorization: Bearer {admin_token}  // 必填
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "message": "儲值方案已刪除"
  },
  "error": null
}
```

---

### 25.4 查詢所有儲值方案（後台）
```http
GET /api/admin/recharge-plan/list
Authorization: Bearer {admin_token}  // 必填
```

**Response** (200 OK): 同前台 14.1

---

### 25.5 條件查詢儲值方案
```http
POST /api/admin/recharge-plan/query
Content-Type: application/json
Authorization: Bearer {admin_token}  // 必填
```

**Request Body**:
```json
{
  "condition": {
    "isActive": true,
    "minAmount": 100,
    "maxAmount": 1000
  },
  "sortBy": "order_num",
  "sortOrder": "ASC"
}
```

**Response** (200 OK): 同前台 14.1

---

## 26. 錢包管理

### 26.1 查詢使用者錢包（後台）
```http
GET /api/admin/wallet/{userId}
Authorization: Bearer {admin_token}  // 必填
```

**權限要求**: `ROLE_ADMIN`

**Path Parameters**:
- `userId`: 使用者 UUID

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "userId": "user-uuid",
    "balance": 1000,
    "bonusBalance": 50,
    "totalBalance": 1050,
    "updatedAt": "2025-12-26T10:30:00"
  },
  "error": null
}
```

---

### 26.2 調整使用者錢包餘額
```http
POST /api/admin/wallet/adjust
Content-Type: application/json
Authorization: Bearer {admin_token}  // 必填
```

**權限要求**: `ROLE_ADMIN`

**Request Body**:
```json
{
  "userId": "user-uuid",          // 必填：使用者 UUID
  "type": "ADJUSTMENT",           // 必填：調整類型
  "amount": 100,                  // 必填：金額（正數=增加，負數=扣除）
  "description": "系統補償"        // 必填：調整原因
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "transactionId": "transaction-uuid",
    "userId": "user-uuid",
    "type": "ADJUSTMENT",
    "amount": 100,
    "balanceBefore": 1000,
    "balanceAfter": 1100,
    "description": "系統補償",
    "createdAt": "2025-12-26T10:30:00"
  },
  "error": null
}
```

---

### 26.3 查詢使用者交易記錄（後台）
```http
POST /api/admin/wallet/transactions/list
Content-Type: application/json
Authorization: Bearer {admin_token}  // 必填
```

**權限要求**: `ROLE_ADMIN`

**Request Body**:
```json
{
  "condition": {
    "userId": "user-uuid",              // 選填：使用者 UUID
    "type": "RECHARGE",                 // 選填：交易類型
    "startDate": "2025-01-01T00:00:00",
    "endDate": "2025-12-31T23:59:59"
  },
  "sortBy": "created_at",
  "sortOrder": "DESC"
}
```

**Response** (200 OK): 同前台 5.2

---

## 27. 獎品盒管理（後台）

### 27.1 查詢使用者獎品盒（後台）
```http
GET /api/admin/prize-box/{userId}
Authorization: Bearer {admin_token}  // 必填
```

**權限要求**: `ROLE_ADMIN`

**Path Parameters**:
- `userId`: 使用者 UUID

**Response** (200 OK): 同前台 6.1

---

### 27.2 查詢使用者獎品盒統計（後台）
```http
GET /api/admin/prize-box/summary/{userId}
Authorization: Bearer {admin_token}  // 必填
```

**權限要求**: `ROLE_ADMIN`

**Path Parameters**:
- `userId`: 使用者 UUID

**Response** (200 OK): 同前台 6.2

---

## 28. 報表分析

### 28.1 營收報表
```http
GET /api/admin/report/revenue?startDate=2025-01-01&endDate=2025-12-31&storeId=uuid
Authorization: Bearer {admin_token}  // 必填
```

**權限要求**: `ROLE_ADMIN`, `ROLE_STORE_OWNER`

**Query Parameters**:
- `startDate`: 起始日期（必填）
- `endDate`: 結束日期（必填）
- `storeId`: 店家 UUID（Admin必填，StoreOwner自動帶入）

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "storeId": "store-uuid",
    "storeName": "夢想抽獎店",
    "startDate": "2025-01-01",
    "endDate": "2025-12-31",
    "totalRevenue": 500000,
    "totalDraws": 6250,
    "totalRecharges": 600000,
    "dailyData": [
      {
        "date": "2025-01-01",
        "revenue": 5000,
        "draws": 62
      }
    ]
  },
  "error": null
}
```

---

### 28.2 推薦碼報表
```http
GET /api/admin/report/referral?startDate=2025-01-01&endDate=2025-12-31&storeId=uuid
Authorization: Bearer {admin_token}  // 必填
```

**權限要求**: `ROLE_ADMIN`, `ROLE_STORE_OWNER`

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "storeId": "store-uuid",
    "totalCodes": 10,
    "totalUses": 250,
    "totalBonusGiven": 12500,
    "topCodes": [
      {
        "code": "ABC123",
        "uses": 100,
        "bonusGiven": 5000
      }
    ]
  },
  "error": null
}
```

---

### 28.3 抽獎結果報表
```http
GET /api/admin/report/lottery-result?lotteryId=uuid&startDate=2025-01-01&endDate=2025-12-31
Authorization: Bearer {admin_token}  // 必填
```

**權限要求**: `ROLE_ADMIN`, `ROLE_STORE_OWNER`

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "lotteryId": "lottery-uuid",
    "lotteryTitle": "鬼滅之刃刮刮樂",
    "totalDraws": 100,
    "totalRevenue": 8000,
    "prizeDistribution": [
      {
        "level": "A",
        "totalQuantity": 1,
        "drawnQuantity": 1,
        "drawnPercentage": 100.0
      },
      {
        "level": "THANKS",
        "totalQuantity": 72,
        "drawnQuantity": 50,
        "drawnPercentage": 69.44
      }
    ]
  },
  "error": null
}
```

---

### 28.4 儲值報表
```http
GET /api/admin/report/recharge?startDate=2025-01-01&endDate=2025-12-31
Authorization: Bearer {admin_token}  // 必填
```

**權限要求**: `ROLE_ADMIN`

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "totalRecharges": 600000,
    "totalOrders": 1200,
    "averageOrderValue": 500,
    "dailyData": [
      {
        "date": "2025-01-01",
        "recharges": 10000,
        "orders": 20
      }
    ]
  },
  "error": null
}
```

---

### 28.5 紅利報表
```http
GET /api/admin/report/bonus?startDate=2025-01-01&endDate=2025-12-31
Authorization: Bearer {admin_token}  // 必填
```

**權限要求**: `ROLE_ADMIN`

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "totalBonusGiven": 50000,
    "totalBonusUsed": 30000,
    "totalBonusRemaining": 20000,
    "sources": [
      {
        "source": "REFERRAL",
        "amount": 25000
      },
      {
        "source": "RECHARGE",
        "amount": 25000
      }
    ]
  },
  "error": null
}
```

---

## 29. 系統日誌

### 29.1 查詢指定類型日誌
```http
GET /api/admin/system-log/type/{logType}?page=1&size=50
Authorization: Bearer {admin_token}  // 必填
```

**權限要求**: `ROLE_ADMIN`

**Path Parameters**:
- `logType`: 日誌類型（LOGIN/LOGOUT/CREATE/UPDATE/DELETE等）

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "id": "log-uuid",
      "userId": "admin-user-uuid",
      "username": "系統管理員",
      "logType": "LOGIN",
      "action": "管理員登入",
      "ipAddress": "192.168.1.1",
      "userAgent": "Mozilla/5.0...",
      "createdAt": "2025-12-26T10:30:00"
    }
  ],
  "error": null
}
```

---

### 29.2 查詢使用者日誌
```http
GET /api/admin/system-log/user/{userId}?page=1&size=50
Authorization: Bearer {admin_token}  // 必填
```

**Path Parameters**:
- `userId`: 使用者 UUID

**Response** (200 OK): 同 29.1

---

### 29.3 查詢日期範圍日誌
```http
GET /api/admin/system-log/date-range?startDate=2025-01-01T00:00:00&endDate=2025-12-31T23:59:59&page=1&size=50
Authorization: Bearer {admin_token}  // 必填
```

**Response** (200 OK): 同 29.1

---

### 29.4 清理舊日誌
```http
DELETE /api/admin/system-log/cleanup?beforeDate=2024-12-31T23:59:59
Authorization: Bearer {admin_token}  // 必填
```

**權限要求**: `ROLE_ADMIN`

**Query Parameters**:
- `beforeDate`: 刪除此日期之前的日誌

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "message": "日誌清理完成",
    "deletedCount": 1500
  },
  "error": null
}
```

---

## 30. 檔案上傳

### 30.1 上傳新聞圖片
```http
POST /api/admin/upload/news
Content-Type: multipart/form-data
Authorization: Bearer {admin_token}  // 必填
```

**權限要求**: `ROLE_ADMIN`

**Request Body** (multipart/form-data):
- `file`: 圖片檔案（必填，支援 JPG/PNG/GIF）

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "url": "https://s3.amazonaws.com/kuji-bucket/news/2025/12/26/uuid.jpg",
    "fileName": "uuid.jpg",
    "fileSize": 102400,
    "uploadedAt": "2025-12-26T10:30:00"
  },
  "error": null
}
```

---

### 30.2 上傳橫幅圖片
```http
POST /api/admin/upload/banner
Content-Type: multipart/form-data
Authorization: Bearer {admin_token}  // 必填
```

**Request Body**: 同 30.1

**Response** (200 OK): 同 30.1

---

### 30.3 上傳抽獎圖片
```http
POST /api/admin/upload/lottery
Content-Type: multipart/form-data
Authorization: Bearer {admin_token}  // 必填
```

**Request Body**: 同 30.1

**Response** (200 OK): 同 30.1

---

### 30.4 上傳獎品圖片
```http
POST /api/admin/upload/prize
Content-Type: multipart/form-data
Authorization: Bearer {admin_token}  // 必填
```

**Request Body**: 同 30.1

**Response** (200 OK): 同 30.1

---

### 30.5 刪除檔案
```http
DELETE /api/admin/upload?fileUrl=https://s3.amazonaws.com/kuji-bucket/news/uuid.jpg
Authorization: Bearer {admin_token}  // 必填
```

**Query Parameters**:
- `fileUrl`: 完整的 S3 檔案 URL

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "message": "檔案已刪除",
    "fileUrl": "https://s3.amazonaws.com/kuji-bucket/news/uuid.jpg"
  },
  "error": null
}
```

---

## 31. 除錯工具

### 31.1 店家診斷
```http
GET /api/admin/debug/store-diagnosis
Authorization: Bearer {admin_token}  // 必填
```

**權限要求**: `ROLE_ADMIN`

**說明**: 診斷店家資料完整性（店家、使用者、角色關聯）

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "totalStores": 5,
    "totalAdminUsers": 12,
    "storeOwners": 5,
    "storeEditors": 7,
    "orphanedUsers": 0,              // 無店家關聯的使用者
    "storesWithoutOwner": 0,         // 無負責人的店家
    "details": [
      {
        "storeId": "store-uuid",
        "storeName": "夢想抽獎店",
        "ownerCount": 1,
        "editorCount": 2,
        "status": "HEALTHY"
      }
    ]
  },
  "error": null
}
```

---

### 31.2 查詢所有管理員使用者
```http
GET /api/admin/debug/all-admin-users
Authorization: Bearer {admin_token}  // 必填
```

**權限要求**: `ROLE_ADMIN`

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "id": "admin-user-uuid",
      "email": "admin@kuji.com",
      "username": "系統管理員",
      "roleCode": "ROLE_ADMIN",
      "status": "ACTIVE",
      "storeId": null,
      "createdAt": "2025-01-01T00:00:00"
    },
    {
      "id": "owner-uuid",
      "email": "owner@example.com",
      "username": "店家負責人",
      "roleCode": "ROLE_STORE_OWNER",
      "status": "ACTIVE",
      "storeId": "store-uuid",
      "storeName": "夢想抽獎店",
      "createdAt": "2025-01-15T00:00:00"
    }
  ],
  "error": null
}
```

---

## 🔗 OAuth2 認證

### OAuth2 登入成功回調
```http
GET /api/auth/oauth2/success?code={authorization_code}
```

**說明**: Google OAuth2 登入成功後的回調處理

**Response** (302 Redirect):
重定向到前端頁面，並在 URL 中帶入 token

---

### OAuth2 登入失敗回調
```http
GET /api/auth/oauth2/failure?error={error_message}
```

**說明**: Google OAuth2 登入失敗後的回調處理

**Response** (302 Redirect):
重定向到前端錯誤頁面

---

## 📊 統一回應格式說明

所有 API 回應都遵循統一格式（由 `GlobalResponseAspect` AOP 自動包裝）：

### 成功回應格式
```json
{
  "success": true,
  "data": {
    // 實際資料
  },
  "error": null,
  "meta": {
    "timestamp": "2025-12-26T10:30:00",
    "requestId": "uuid",
    "executionTime": 125  // 執行時間（毫秒）
  }
}
```

### 錯誤回應格式
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "ERROR_CODE",
    "message": "錯誤訊息"
  },
  "meta": {
    "timestamp": "2025-12-26T10:30:00",
    "requestId": "uuid"
  }
}
```

### 常見錯誤代碼

| HTTP Status | Error Code | 說明 |
|-------------|-----------|------|
| 400 | INVALID_REQUEST | 請求參數錯誤 |
| 400 | EMAIL_ALREADY_EXISTS | 信箱已被註冊 |
| 400 | REFERRAL_CODE_INVALID | 推薦碼無效 |
| 400 | INVALID_MAX_DRAWS | 總抽數設定錯誤 |
| 400 | TICKET_ALREADY_DRAWN | 籤位已被抽過 |
| 400 | INSUFFICIENT_BALANCE | 餘額不足 |
| 401 | UNAUTHORIZED | 未登入或 Token 過期 |
| 403 | FORBIDDEN | 無權限存取 |
| 404 | NOT_FOUND | 資源不存在 |
| 409 | CONFLICT | 資源衝突 |
| 500 | INTERNAL_SERVER_ERROR | 伺服器內部錯誤 |

---

## 🎮 遊戲模式說明

### 一番賞模式 (LOTTERY_MODE)

**特性**:
- 每個籤位都必須有獎品（不允許謝謝惠顧）
- `maxDraws` 由後端自動計算 = 獎品總數
- 前端如果傳入 `maxDraws`，會被後端覆寫

**範例**:
```json
{
  "lottery": {
    "playMode": "LOTTERY_MODE",
    "pricePerDraw": 80
    // maxDraws 不用傳，後端自動計算
  },
  "prizes": [
    {"level": "A", "quantity": 1},
    {"level": "B", "quantity": 2},
    {"level": "C", "quantity": 5}
  ]
}

// 後端自動計算: maxDraws = 1 + 2 + 5 = 8
```

---

### 刮刮樂模式 (SCRATCH_MODE)

**特性**:
- 支援謝謝惠顧（剩餘的籤位會是謝謝惠顧）
- `maxDraws` 必須由前端提供
- `maxDraws` 必須 >= 獎品總數

**範例**:
```json
{
  "lottery": {
    "playMode": "SCRATCH_MODE",
    "pricePerDraw": 80,
    "maxDraws": 100  // ⭐ 必須傳入
  },
  "prizes": [
    {"level": "A", "quantity": 1},
    {"level": "B", "quantity": 2},
    {"level": "C", "quantity": 25}
  ]
}

// 結果：
// - 總籤位：100
// - 獎品數：28
// - 謝謝惠顧：72
```

---

## 🔑 StoreID 自動帶入機制

### 問題背景
店家負責人新增抽獎活動時，前端無法取得 StoreID，需要從 JWT Token 自動提取。

### 解決方案

#### 1. JWT Token 包含 storeIds
```json
{
  "sub": "owner@example.com",
  "userId": "admin-user-uuid",
  "userType": "admin",
  "roles": ["ROLE_STORE_OWNER"],
  "storeIds": ["store-uuid-1", "store-uuid-2"]  // ⭐ 店家 ID 列表
}
```

#### 2. Filter 中自動查詢並設定
- `AdminJwtAuthenticationFilter` 在驗證 JWT 時，會自動查詢 `store_user` 表
- 將店家 ID 列表設定到 `UserPrincipal.storeIds`

#### 3. Controller 中自動帶入
```java
// 新增抽獎活動
@PostMapping
public ResponseEntity<LotteryRes> createLottery(@RequestBody LotteryCreateReq req) {
    // ✅ 自動取得並設定 storeId
    if (SecurityUtils.hasRole("ROLE_STORE_OWNER")) {
        String storeId = SecurityUtils.getCurrentUserPrimaryStoreId();
        req.setStoreId(storeId);
    }
    
    return ResponseEntity.ok(service.createLottery(req));
}
```

#### 4. 查詢 API 也自動過濾
```java
// 查詢抽獎列表
@PostMapping("/list")
public ResponseEntity<List<LotteryRes>> queryLotteries(@RequestBody QueryReq<LotteryCondition> req) {
    // ✅ StoreOwner 自動過濾為自己的店家
    if (SecurityUtils.hasRole("ROLE_STORE_OWNER")) {
        String storeId = SecurityUtils.getCurrentUserPrimaryStoreId();
        req.getCondition().setStoreId(storeId);
    }
    
    return ResponseEntity.ok(service.queryLotteries(req));
}
```

### 前端使用方式

#### 後台：新增抽獎（不用傳 storeId）
```javascript
// StoreOwner 不用傳 storeId，後端自動帶入
const response = await axios.post('/api/admin/lottery-with-prizes', {
  lottery: {
    title: '鬼滅之刃刮刮樂',
    playMode: 'SCRATCH_MODE',
    pricePerDraw: 80,
    maxDraws: 100
    // storeId 不用傳，後端自動帶入
  },
  prizes: [...]
}, {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
```

#### 後台：查詢抽獎（不用傳 storeId）
```javascript
// StoreOwner 不用傳 storeId，後端自動過濾
const response = await axios.post('/api/admin/lottery-with-prizes/list', {
  condition: {
    title: '鬼滅',
    status: 'ON_SHELF'
    // storeId 不用傳，後端自動帶入
  }
}, {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
```

---

## 📝 開發注意事項

### 1. 查詢 API 設計模式
- **所有條件可選**: 使用 MyBatis 動態 SQL
- **前端做分頁**: 後端返回全部資料（List 不用 PageHelper）
- **組合而非繼承**: QueryReq 包裝 Condition

### 2. 常見錯誤
- ❌ 不要在前端傳 storeId（後端自動帶入）
- ❌ 不要在 Service 使用 PageHelper（前端做分頁）
- ❌ 不要忘記檢查 Condition 欄位是否為 null
- ❌ 不要在查詢 API 要求所有條件必填（全部可選）

### 3. 權限檢查
- Service 層使用 `permissionService.isAdmin()`
- Controller 層使用 `@PreAuthorize("hasRole('ADMIN')")`

### 4. JWT Token 驗證
- 所有需要認證的 API 都需要在 Header 中帶入 JWT Token
- Token 格式: `Authorization: Bearer {token}`
- Token 過期時間: 24 小時

---

## 📚 相關文檔

- [專案架構說明](./copilot-instructions.md)
- [刮刮樂模式支援文檔](./SCRATCH_MODE_THANKSGIVING_SUPPORT.md)
- [謝謝惠顧數量修復文檔](./THANKSGIVING_COUNT_FIX.md)
- [自動帶入 StoreID 機制](./ARCHITECTURE_IMPROVEMENT_AUTO_MAXDRAWS_UNIFIED_API.md)

---

## 🔄 版本歷史

| 版本 | 日期 | 更新內容 |
|------|------|----------|
| 1.0.0 | 2025-12-26 | 初版，包含所有前後台 API |
| 1.1.0 | 2025-12-26 | 新增刮刮樂模式 thanksgiving 支援 |
| 1.2.0 | 2025-12-26 | 新增 thanksgivingCount 欄位 |
| 1.3.0 | 2025-12-26 | 新增 StoreID 自動帶入機制 |

---

**文檔維護者**: GitHub Copilot  
**最後更新**: 2025-12-26  
**專案**: KUJI-Server Admin Backend
