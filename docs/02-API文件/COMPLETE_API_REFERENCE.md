# 🎯 KUJI 完整 API 參考文件

**更新時間：** 2026-01-16  
**Base URL：** `http://localhost:8080/api`  
**生產環境：** `https://your-domain.com/api`

---

## 📚 目錄

- [認證 API](#認證-api)
- [後台管理 API](#後台管理-api)
  - [商品管理](#商品管理)
  - [獎品管理](#獎品管理)
  - [整合 API（商品+獎品）](#整合-api商品獎品)
  - [店家管理](#店家管理)
  - [用戶管理](#用戶管理)
  - [訂單管理](#訂單管理)
  - [系統日誌](#系統日誌)
  - [推薦碼管理](#推薦碼管理)
  - [跑馬燈管理](#跑馬燈管理)
  - [報表管理](#報表管理)
- [前台 API](#前台-api)
  - [商品瀏覽](#商品瀏覽)
  - [抽獎功能](#抽獎功能)
  - [獎品池](#獎品池)
  - [訂單](#訂單)
  - [錢包](#錢包)
  - [用戶地址](#用戶地址)
  - [推薦碼驗證](#推薦碼驗證)

---

## 🔐 認證說明

### 後台認證
- **路由：** `/admin/**`
- **Header：** `Authorization: Bearer {ADMIN_TOKEN}`
- **角色：** ROLE_ADMIN, ROLE_STORE_OWNER, ROLE_STORE_EDITOR

### 前台認證
- **路由：** `/api/**`
- **Header：** `Authorization: Bearer {USER_TOKEN}`
- **角色：** ROLE_USER

---

## 認證 API

### 1. 後台登入

```http
POST /admin/auth/login
Content-Type: application/json

{
  "email": "admin@kuji.com",
  "password": "admin123"
}
```

**回應：**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": "uuid",
      "email": "admin@kuji.com",
      "username": "管理員",
      "roles": ["ROLE_ADMIN"]
    }
  }
}
```

### 2. 前台註冊

```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "testuser",
  "email": "test@example.com",
  "password": "password123",
  "phone": "0912345678",
  "referralCode": "WELCOME2024"
}
```

### 3. 前台登入

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "password123"
}
```

---

## 後台管理 API

### 商品管理

#### 1. 建立商品

```http
POST /admin/lottery
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "title": "鬼滅之刃一番賞",
  "description": "超人氣動漫周邊",
  "category": "OFFICIAL_ICHIBAN",
  "pricePerDraw": 80,
  "totalDraws": 100,
  "startTime": "2026-01-20T00:00:00",
  "endTime": "2026-03-20T23:59:59",
  "imageUrl": "https://example.com/image.jpg",
  "status": "OFF_SHELF"
}
```

#### 2. 更新商品

```http
PUT /admin/lottery/{lotteryId}
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "title": "鬼滅之刃一番賞（更新）",
  "status": "ON_SHELF"
}
```

#### 3. 查詢商品列表

```http
POST /admin/lottery/list
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "condition": {
    "title": "鬼滅",
    "status": "ON_SHELF",
    "category": "OFFICIAL_ICHIBAN"
  },
  "page": 1,
  "size": 20,
  "sortBy": "created_at",
  "sortOrder": "DESC"
}
```

#### 4. 查詢單一商品

```http
GET /admin/lottery/{lotteryId}
Authorization: Bearer {ADMIN_TOKEN}
```

#### 5. 刪除商品

```http
DELETE /admin/lottery/{lotteryId}
Authorization: Bearer {ADMIN_TOKEN}
```

#### 6. 複製商品

```http
POST /admin/lottery/{lotteryId}/copy
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "title": "鬼滅之刃一番賞（副本）"
}
```

---

### 獎品管理

#### 1. 建立獎品

```http
POST /admin/lotteries/{lotteryId}/prizes
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "name": "炭治郎公仔",
  "level": "A",
  "quantity": 1,
  "weight": 5,
  "prizeType": "FIGURE",
  "isGrandPrize": true,
  "description": "超級大賞",
  "imageUrl": "https://example.com/tanjiro.jpg"
}
```

#### 2. 批量建立獎品

```http
POST /admin/lotteries/{lotteryId}/prizes/batch
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "prizes": [
    {
      "name": "炭治郎公仔",
      "level": "A",
      "quantity": 1,
      "weight": 5
    },
    {
      "name": "禰豆子公仔",
      "level": "B",
      "quantity": 5,
      "weight": 10
    },
    {
      "name": "鑰匙圈",
      "level": "C",
      "quantity": 20,
      "weight": 30
    }
  ]
}
```

#### 3. 更新獎品

```http
PUT /admin/lotteries/{lotteryId}/prizes/{prizeId}
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "quantity": 2,
  "weight": 10
}
```

#### 4. 查詢獎品列表

```http
POST /admin/lotteries/{lotteryId}/prizes/list
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "condition": {
    "level": "A",
    "prizeType": "FIGURE"
  }
}
```

#### 5. 刪除獎品

```http
DELETE /admin/lotteries/{lotteryId}/prizes/{prizeId}
Authorization: Bearer {ADMIN_TOKEN}
```

---

### 整合 API（商品+獎品）

#### 1. 一次建立商品和獎品

```http
POST /admin/lottery-with-prizes
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "lottery": {
    "title": "鬼滅之刃一番賞",
    "description": "超人氣動漫周邊",
    "category": "OFFICIAL_ICHIBAN",
    "pricePerDraw": 80,
    "totalDraws": 100,
    "startTime": "2026-01-20T00:00:00",
    "endTime": "2026-03-20T23:59:59",
    "imageUrl": "https://example.com/kimetsu.jpg",
    "status": "OFF_SHELF"
  },
  "prizes": [
    {
      "name": "炭治郎公仔",
      "level": "A",
      "quantity": 1,
      "weight": 5,
      "prizeType": "FIGURE",
      "isGrandPrize": true,
      "imageUrl": "https://example.com/tanjiro.jpg"
    },
    {
      "name": "禰豆子公仔",
      "level": "B",
      "quantity": 5,
      "weight": 10,
      "prizeType": "FIGURE",
      "imageUrl": "https://example.com/nezuko.jpg"
    },
    {
      "name": "善逸公仔",
      "level": "C",
      "quantity": 10,
      "weight": 20,
      "prizeType": "FIGURE"
    },
    {
      "name": "伊之助公仔",
      "level": "D",
      "quantity": 20,
      "weight": 30,
      "prizeType": "FIGURE"
    },
    {
      "name": "鑰匙圈",
      "level": "E",
      "quantity": 30,
      "weight": 50,
      "prizeType": "ACCESSORY"
    }
  ]
}
```

**回應：**
```json
{
  "success": true,
  "data": {
    "lottery": {
      "id": "lottery-uuid",
      "title": "鬼滅之刃一番賞",
      "status": "OFF_SHELF",
      "pricePerDraw": 80,
      "totalDraws": 100
    },
    "prizes": [
      {
        "id": "prize-uuid-1",
        "name": "炭治郎公仔",
        "level": "A",
        "quantity": 1,
        "remaining": 1
      }
    ],
    "statistics": {
      "totalPrizeCount": 66,
      "remainingPrizeCount": 66,
      "progressPercentage": 0.0
    }
  }
}
```

#### 2. 更新商品和獎品

```http
PUT /admin/lottery-with-prizes/{lotteryId}
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "lottery": {
    "status": "ON_SHELF",
    "pricePerDraw": 85
  },
  "prizes": [
    {
      "id": "prize-uuid-1",
      "quantity": 2
    },
    {
      "name": "新增的獎品",
      "level": "F",
      "quantity": 50,
      "weight": 100
    }
  ]
}
```

#### 3. 查詢商品含獎品

```http
GET /admin/lottery-with-prizes/{lotteryId}
Authorization: Bearer {ADMIN_TOKEN}
```

---

### 店家管理

#### 1. 建立店家

```http
POST /admin/store
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "name": "KUJI 官方旗艦店",
  "description": "官方認證店家",
  "logoUrl": "https://example.com/logo.jpg",
  "status": "ACTIVE"
}
```

#### 2. 更新店家

```http
PUT /admin/store/{storeId}
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "name": "KUJI 官方旗艦店（更新）",
  "status": "ACTIVE"
}
```

#### 3. 查詢店家列表

```http
POST /admin/store/list
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "condition": {
    "name": "KUJI",
    "status": "ACTIVE"
  },
  "page": 1,
  "size": 20
}
```

#### 4. 刪除店家

```http
DELETE /admin/store/{storeId}
Authorization: Bearer {ADMIN_TOKEN}
```

---

### 用戶管理

#### 1. 建立用戶

```http
POST /admin/user
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "username": "newuser",
  "email": "newuser@example.com",
  "password": "password123",
  "phone": "0912345678",
  "goldBalance": 1000,
  "bonusBalance": 500
}
```

#### 2. 更新用戶

```http
PUT /admin/user/{userId}
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "username": "updateduser",
  "phone": "0987654321"
}
```

#### 3. 查詢用戶列表

```http
POST /admin/user/list
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "condition": {
    "username": "test",
    "email": "example.com"
  },
  "page": 1,
  "size": 20
}
```

#### 4. 查詢單一用戶

```http
GET /admin/user/{userId}
Authorization: Bearer {ADMIN_TOKEN}
```

---

### 訂單管理

#### 1. 查詢訂單列表

```http
POST /admin/order/list
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "condition": {
    "userId": "user-uuid",
    "status": "PENDING",
    "createdAtStart": "2026-01-01T00:00:00",
    "createdAtEnd": "2026-01-31T23:59:59"
  },
  "page": 1,
  "size": 20,
  "sortBy": "created_at",
  "sortOrder": "DESC"
}
```

#### 2. 查詢訂單詳情

```http
GET /admin/order/{orderId}
Authorization: Bearer {ADMIN_TOKEN}
```

#### 3. 更新訂單狀態

```http
PUT /admin/order/{orderId}/status
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "status": "SHIPPED",
  "trackingNumber": "1234567890"
}
```

---

### 系統日誌

#### 1. 查詢系統日誌

```http
POST /admin/system-log/list
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "condition": {
    "action": "CREATE_LOTTERY",
    "module": "LOTTERY",
    "userId": "admin-uuid",
    "createdAtStart": "2026-01-01T00:00:00",
    "createdAtEnd": "2026-01-31T23:59:59"
  },
  "page": 1,
  "size": 50,
  "sortBy": "created_at",
  "sortOrder": "DESC"
}
```

#### 2. 查詢日誌詳情

```http
GET /admin/system-log/{logId}
Authorization: Bearer {ADMIN_TOKEN}
```

---

### 推薦碼管理

#### 1. 建立推薦碼

```http
POST /admin/referral-code
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "code": "WELCOME2024",
  "ownerType": "ADMIN",
  "rewardGold": 100,
  "rewardBonus": 50,
  "maxUsage": 1000,
  "validFrom": "2026-01-01T00:00:00",
  "validUntil": "2026-12-31T23:59:59"
}
```

#### 2. 更新推薦碼

```http
PUT /admin/referral-code/{codeId}
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "rewardGold": 200,
  "maxUsage": 2000,
  "isActive": true
}
```

#### 3. 查詢推薦碼列表

```http
POST /admin/referral-code/list
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "condition": {
    "code": "WELCOME",
    "ownerType": "ADMIN",
    "isActive": true
  },
  "page": 1,
  "size": 20
}
```

#### 4. 查詢推薦記錄

```http
POST /admin/referral-code/{codeId}/records
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "page": 1,
  "size": 50
}
```

---

### 跑馬燈管理

#### 1. 建立跑馬燈

```http
POST /admin/marquee
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "title": "新年優惠活動",
  "content": "全館商品 8 折優惠！",
  "linkUrl": "https://example.com/promotion",
  "startTime": "2026-01-01T00:00:00",
  "endTime": "2026-01-31T23:59:59",
  "isEnabled": true,
  "displayOrder": 1
}
```

#### 2. 更新跑馬燈

```http
PUT /admin/marquee/{marqueeId}
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "title": "新年優惠活動（更新）",
  "isEnabled": false
}
```

#### 3. 查詢跑馬燈列表

```http
POST /admin/marquee/list
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "condition": {
    "isEnabled": true
  },
  "sortBy": "display_order",
  "sortOrder": "ASC"
}
```

#### 4. 刪除跑馬燈

```http
DELETE /admin/marquee/{marqueeId}
Authorization: Bearer {ADMIN_TOKEN}
```

---

### 報表管理

#### 1. 生成報表快照

```http
POST /admin/report/snapshot
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "reportType": "DAILY",
  "reportDate": "2026-01-15",
  "storeId": "store-uuid"
}
```

#### 2. 查詢報表列表

```http
POST /admin/report/list
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "condition": {
    "reportType": "DAILY",
    "reportDate": "2026-01-15",
    "storeId": "store-uuid"
  },
  "page": 1,
  "size": 20
}
```

#### 3. 查詢報表詳情

```http
GET /admin/report/{reportId}
Authorization: Bearer {ADMIN_TOKEN}
```

---

## 前台 API

### 商品瀏覽

#### 1. 查詢商品列表

```http
POST /api/lottery/list
Content-Type: application/json

{
  "condition": {
    "title": "鬼滅",
    "category": "OFFICIAL_ICHIBAN",
    "status": "ON_SHELF"
  },
  "page": 1,
  "size": 20,
  "sortBy": "created_at",
  "sortOrder": "DESC"
}
```

#### 2. 查詢商品詳情

```http
GET /api/lottery/{lotteryId}
```

#### 3. 查詢熱門商品

```http
GET /api/lottery/hot?limit=10
```

---

### 抽獎功能

#### 1. 加權隨機抽獎

```http
POST /api/lottery/random/{lotteryId}/draw
Authorization: Bearer {USER_TOKEN}
Content-Type: application/json

{
  "drawCount": 1
}
```

**回應：**
```json
{
  "success": true,
  "data": {
    "drawResults": [
      {
        "prizeId": "prize-uuid",
        "prizeName": "炭治郎公仔",
        "prizeLevel": "A",
        "prizeType": "FIGURE",
        "imageUrl": "https://example.com/tanjiro.jpg"
      }
    ],
    "totalCost": 80,
    "remainingGold": 920,
    "remainingBonus": 0
  }
}
```

#### 2. 十連抽

```http
POST /api/lottery/random/{lotteryId}/draw
Authorization: Bearer {USER_TOKEN}
Content-Type: application/json

{
  "drawCount": 10
}
```

---

### 獎品池

#### 1. 查詢我的獎品池

```http
GET /api/prize-box/my
Authorization: Bearer {USER_TOKEN}
```

#### 2. 查詢獎品池詳情

```http
GET /api/prize-box/{prizeBoxId}
Authorization: Bearer {USER_TOKEN}
```

#### 3. 兌換獎品（建立訂單）

```http
POST /api/prize-box/{prizeBoxId}/redeem
Authorization: Bearer {USER_TOKEN}
Content-Type: application/json

{
  "addressId": "address-uuid",
  "recipientName": "王小明",
  "recipientPhone": "0912345678",
  "address": "台北市中正區重慶南路一段122號",
  "notes": "請於平日送達"
}
```

---

### 訂單

#### 1. 查詢我的訂單

```http
GET /api/order/my
Authorization: Bearer {USER_TOKEN}
```

#### 2. 查詢訂單詳情

```http
GET /api/order/{orderId}
Authorization: Bearer {USER_TOKEN}
```

#### 3. 取消訂單

```http
PUT /api/order/{orderId}/cancel
Authorization: Bearer {USER_TOKEN}
```

---

### 錢包

#### 1. 查詢我的錢包

```http
GET /api/wallet/my
Authorization: Bearer {USER_TOKEN}
```

**回應：**
```json
{
  "success": true,
  "data": {
    "userId": "user-uuid",
    "goldBalance": 1000,
    "bonusBalance": 500,
    "totalBalance": 1500
  }
}
```

#### 2. 查詢交易記錄

```http
GET /api/wallet/transactions
Authorization: Bearer {USER_TOKEN}
?page=1&size=20
```

#### 3. 儲值 Gold

```http
POST /api/wallet/recharge
Authorization: Bearer {USER_TOKEN}
Content-Type: application/json

{
  "amount": 1000,
  "paymentMethod": "CREDIT_CARD",
  "rechargePlanId": "plan-uuid"
}
```

---

### 用戶地址

#### 1. 建立地址

```http
POST /api/user-address
Authorization: Bearer {USER_TOKEN}
Content-Type: application/json

{
  "recipientName": "王小明",
  "recipientPhone": "0912345678",
  "postalCode": "100",
  "city": "台北市",
  "district": "中正區",
  "address": "重慶南路一段122號",
  "isDefault": true
}
```

#### 2. 更新地址

```http
PUT /api/user-address/{addressId}
Authorization: Bearer {USER_TOKEN}
Content-Type: application/json

{
  "recipientName": "王小華",
  "isDefault": false
}
```

#### 3. 查詢我的地址列表

```http
GET /api/user-address/my
Authorization: Bearer {USER_TOKEN}
```

#### 4. 設定預設地址

```http
PUT /api/user-address/{addressId}/set-default
Authorization: Bearer {USER_TOKEN}
```

#### 5. 刪除地址

```http
DELETE /api/user-address/{addressId}
Authorization: Bearer {USER_TOKEN}
```

---

### 推薦碼驗證

#### 1. 驗證推薦碼

```http
POST /api/referral-code/validate
Content-Type: application/json

{
  "code": "WELCOME2024"
}
```

**回應：**
```json
{
  "success": true,
  "data": {
    "valid": true,
    "code": "WELCOME2024",
    "rewardGold": 100,
    "rewardBonus": 50,
    "message": "推薦碼有效！註冊後可獲得 100 Gold + 50 Bonus"
  }
}
```

---

## 🎯 完整測試流程

### 場景 1：後台建立商品和獎品

```bash
# 1. 後台登入
POST /admin/auth/login
{
  "email": "admin@kuji.com",
  "password": "admin123"
}
# 取得 ADMIN_TOKEN

# 2. 使用整合 API 建立商品+獎品
POST /admin/lottery-with-prizes
Authorization: Bearer {ADMIN_TOKEN}
{
  "lottery": {
    "title": "測試商品",
    "pricePerDraw": 80,
    "totalDraws": 100,
    "status": "ON_SHELF"
  },
  "prizes": [
    {"name": "A賞", "level": "A", "quantity": 1, "weight": 5},
    {"name": "B賞", "level": "B", "quantity": 5, "weight": 10}
  ]
}
# 取得 LOTTERY_ID
```

### 場景 2：前台用戶註冊並抽獎

```bash
# 1. 註冊用戶
POST /api/auth/register
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "password123",
  "referralCode": "WELCOME2024"
}
# 取得 USER_TOKEN

# 2. 查詢錢包
GET /api/wallet/my
Authorization: Bearer {USER_TOKEN}

# 3. 儲值（如果餘額不足）
POST /api/wallet/recharge
Authorization: Bearer {USER_TOKEN}
{
  "amount": 1000,
  "paymentMethod": "CREDIT_CARD"
}

# 4. 抽獎
POST /api/lottery/random/{LOTTERY_ID}/draw
Authorization: Bearer {USER_TOKEN}
{
  "drawCount": 1
}

# 5. 查詢獎品池
GET /api/prize-box/my
Authorization: Bearer {USER_TOKEN}

# 6. 兌換獎品
POST /api/prize-box/{PRIZE_BOX_ID}/redeem
Authorization: Bearer {USER_TOKEN}
{
  "recipientName": "測試用戶",
  "recipientPhone": "0912345678",
  "address": "台北市中正區測試路123號"
}

# 7. 查詢訂單
GET /api/order/my
Authorization: Bearer {USER_TOKEN}
```

---

## 📝 注意事項

### 1. 認證 Token
- 後台 Token 和前台 Token 不通用
- Token 有效期：24 小時
- Token 格式：`Bearer {token}`

### 2. StoreID 自動帶入
- STORE_OWNER 和 STORE_EDITOR 建立商品時，storeId 自動從 JWT 提取
- 不需要前端傳遞 storeId

### 3. 分頁查詢
- 後台列表查詢返回全部資料（前端做分頁）
- 前台列表查詢支援分頁參數

### 4. 錯誤處理
所有 API 錯誤統一格式：
```json
{
  "success": false,
  "error": {
    "code": "BUSINESS_ERROR",
    "message": "錯誤訊息"
  }
}
```

---

**最後更新：** 2026-01-16  
**測試工具：** Postman Collection 可在 `docs/05-測試相關/` 找到
