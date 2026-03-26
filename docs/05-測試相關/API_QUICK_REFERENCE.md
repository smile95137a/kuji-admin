# 🎯 API 快速參考卡片

**快速查找常用 API**

---

## 🔐 認證

### 後台登入
```http
POST /admin/auth/login
{"email":"admin@kuji.com","password":"admin123"}
```

### 前台登入
```http
POST /api/auth/login
{"email":"test@example.com","password":"password123"}
```

---

## ⭐ 整合 API（推薦使用）

### 建立商品+獎品
```http
POST /admin/lottery-with-prizes
Authorization: Bearer {ADMIN_TOKEN}

{
  "lottery": {
    "title": "商品名稱",
    "pricePerDraw": 80,
    "totalDraws": 100,
    "status": "ON_SHELF"
  },
  "prizes": [
    {"name": "A賞", "level": "A", "quantity": 1, "weight": 5},
    {"name": "B賞", "level": "B", "quantity": 5, "weight": 10}
  ]
}
```

### 更新商品+獎品
```http
PUT /admin/lottery-with-prizes/{id}
Authorization: Bearer {ADMIN_TOKEN}

{
  "lottery": {"status": "ON_SHELF"},
  "prizes": [
    {"id": "prize-id", "quantity": 2},
    {"name": "新獎品", "level": "C", "quantity": 10}
  ]
}
```

### 查詢商品含獎品
```http
GET /admin/lottery-with-prizes/{id}
Authorization: Bearer {ADMIN_TOKEN}
```

---

## 🎲 抽獎

### 單抽
```http
POST /api/lottery/random/{id}/draw
Authorization: Bearer {USER_TOKEN}
{"drawCount": 1}
```

### 十連抽
```http
POST /api/lottery/random/{id}/draw
Authorization: Bearer {USER_TOKEN}
{"drawCount": 10}
```

---

## 📦 獎品池

### 查詢我的獎品
```http
GET /api/prize-box/my
Authorization: Bearer {USER_TOKEN}
```

### 兌換獎品
```http
POST /api/prize-box/{id}/redeem
Authorization: Bearer {USER_TOKEN}

{
  "recipientName": "王小明",
  "recipientPhone": "0912345678",
  "address": "台北市中正區測試路123號"
}
```

---

## 💰 錢包

### 查詢錢包
```http
GET /api/wallet/my
Authorization: Bearer {USER_TOKEN}
```

### 儲值
```http
POST /api/wallet/recharge
Authorization: Bearer {USER_TOKEN}
{"amount": 1000, "paymentMethod": "CREDIT_CARD"}
```

---

## 📋 訂單

### 查詢我的訂單
```http
GET /api/order/my
Authorization: Bearer {USER_TOKEN}
```

### 查詢訂單詳情
```http
GET /api/order/{id}
Authorization: Bearer {USER_TOKEN}
```

---

## 🏪 商品管理

### 查詢商品列表
```http
POST /admin/lottery/list
Authorization: Bearer {ADMIN_TOKEN}

{
  "condition": {"status": "ON_SHELF"},
  "sortBy": "created_at",
  "sortOrder": "DESC"
}
```

### 複製商品
```http
POST /admin/lottery/{id}/copy
Authorization: Bearer {ADMIN_TOKEN}
{"title": "商品名稱（副本）"}
```

---

## 📍 用戶地址

### 建立地址
```http
POST /api/user-address
Authorization: Bearer {USER_TOKEN}

{
  "recipientName": "王小明",
  "recipientPhone": "0912345678",
  "city": "台北市",
  "district": "中正區",
  "address": "測試路123號",
  "isDefault": true
}
```

### 查詢我的地址
```http
GET /api/user-address/my
Authorization: Bearer {USER_TOKEN}
```

---

## 🎁 推薦碼

### 驗證推薦碼
```http
POST /api/referral-code/validate
{"code": "WELCOME2024"}
```

### 建立推薦碼（後台）
```http
POST /admin/referral-code
Authorization: Bearer {ADMIN_TOKEN}

{
  "code": "WELCOME2024",
  "rewardGold": 100,
  "rewardBonus": 50,
  "maxUsage": 1000
}
```

---

## 🔍 常用查詢

### 查詢商品列表（前台）
```http
POST /api/lottery/list
{"condition": {"status": "ON_SHELF"}, "page": 1, "size": 20}
```

### 查詢商品詳情
```http
GET /api/lottery/{id}
```

### 查詢熱門商品
```http
GET /api/lottery/hot?limit=10
```

---

## 💡 小技巧

### 自動儲存 Token
使用 Postman 的 Tests 功能：
```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    pm.collectionVariables.set('adminToken', jsonData.data.token);
}
```

### 快速切換環境
- 本地：`http://localhost:8080/api`
- 生產：`https://your-domain.com/api`

### curl 轉 Postman
```bash
# 在 curl 命令上點右鍵
# 選擇 "Copy as cURL"
# 在 Postman 中點 Import → Paste cURL
```

---

## 📝 回應格式

### 成功
```json
{
  "success": true,
  "data": {...},
  "meta": {
    "timestamp": "2026-01-16T...",
    "requestId": "uuid"
  }
}
```

### 錯誤
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

**Base URL:** `http://localhost:8080/api`  
**完整文件:** `docs/02-API文件/COMPLETE_API_REFERENCE.md`  
**Postman Collection:** `docs/05-測試相關/KUJI_Complete_API.postman_collection.json`
