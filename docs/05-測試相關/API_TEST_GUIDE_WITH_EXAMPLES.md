# 🧪 API 測試指南與請求範例

**更新時間：** 2026-01-16  
**測試工具：** Postman / curl / 測試腳本

---

## 📚 文件位置

| 文件 | 說明 |
|------|------|
| `COMPLETE_API_REFERENCE.md` | 完整 API 參考文件 |
| `KUJI_Complete_API.postman_collection.json` | Postman 測試集合 |
| `../test-api-complete.bat` | 自動測試腳本 |

---

## 🚀 快速開始

### 方法 1：使用 Postman（推薦）

1. **匯入 Collection**
   ```
   開啟 Postman → Import → 選擇檔案
   → KUJI_Complete_API.postman_collection.json
   ```

2. **設定環境變數**
   - `baseUrl`: `http://localhost:8080/api`
   - `adminToken`: （登入後自動設定）
   - `userToken`: （登入後自動設定）

3. **開始測試**
   - 執行 "後台登入" → 自動儲存 adminToken
   - 執行其他 API 測試

### 方法 2：使用測試腳本

```bash
# 1. 啟動後端
mvn spring-boot:run

# 2. 執行測試腳本（新終端機）
test-api-complete.bat
```

### 方法 3：使用 curl

參考下方的 curl 範例。

---

## 🎯 完整測試流程

### 階段 1：後台管理（建立商品）

#### 1.1 後台登入

```bash
curl -X POST http://localhost:8080/api/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@kuji.com",
    "password": "admin123"
  }'
```

**預期回應：**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": "admin-uuid",
      "email": "admin@kuji.com",
      "username": "管理員",
      "roles": ["ROLE_ADMIN"]
    }
  }
}
```

**複製 token 備用！**

---

#### 1.2 使用整合 API 建立商品+獎品

```bash
curl -X POST http://localhost:8080/api/admin/lottery-with-prizes \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -d '{
    "lottery": {
      "title": "鬼滅之刃一番賞",
      "description": "超人氣動漫周邊",
      "category": "OFFICIAL_ICHIBAN",
      "pricePerDraw": 80,
      "totalDraws": 100,
      "startTime": "2026-01-20T00:00:00",
      "endTime": "2026-03-20T23:59:59",
      "imageUrl": "https://example.com/kimetsu.jpg",
      "status": "ON_SHELF"
    },
    "prizes": [
      {
        "name": "炭治郎公仔",
        "level": "A",
        "quantity": 1,
        "weight": 5,
        "prizeType": "FIGURE",
        "isGrandPrize": true,
        "description": "超級大賞",
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
  }'
```

**預期回應：**
```json
{
  "success": true,
  "data": {
    "lottery": {
      "id": "lottery-uuid-here",
      "title": "鬼滅之刃一番賞",
      "status": "ON_SHELF",
      "pricePerDraw": 80,
      "totalDraws": 100,
      "storeName": "官方旗艦店"
    },
    "prizes": [
      {
        "id": "prize-uuid-1",
        "name": "炭治郎公仔",
        "level": "A",
        "quantity": 1,
        "remaining": 1,
        "weight": 5
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

**複製 lottery.id 備用！**

---

#### 1.3 查詢商品含獎品

```bash
curl -X GET http://localhost:8080/api/admin/lottery-with-prizes/YOUR_LOTTERY_ID \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

---

### 階段 2：前台用戶（註冊與抽獎）

#### 2.1 用戶註冊

```bash
curl -X POST http://localhost:8080/api/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "phone": "0912345678"
  }'
```

---

#### 2.2 用戶登入

```bash
curl -X POST http://localhost:8080/api/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

**預期回應：**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": "user-uuid",
      "email": "test@example.com",
      "username": "testuser"
    }
  }
}
```

**複製 token 備用！**

---

#### 2.3 查詢錢包

```bash
curl -X GET http://localhost:8080/api/api/wallet/my \
  -H "Authorization: Bearer YOUR_USER_TOKEN"
```

**預期回應：**
```json
{
  "success": true,
  "data": {
    "userId": "user-uuid",
    "goldBalance": 1000,
    "bonusBalance": 0,
    "totalBalance": 1000
  }
}
```

---

#### 2.4 查詢商品列表

```bash
curl -X POST http://localhost:8080/api/api/lottery/list \
  -H "Content-Type: application/json" \
  -d '{
    "condition": {
      "status": "ON_SHELF"
    },
    "page": 1,
    "size": 20
  }'
```

---

#### 2.5 單抽

```bash
curl -X POST http://localhost:8080/api/api/lottery/random/YOUR_LOTTERY_ID/draw \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_USER_TOKEN" \
  -d '{
    "drawCount": 1
  }'
```

**預期回應：**
```json
{
  "success": true,
  "data": {
    "drawResults": [
      {
        "prizeId": "prize-uuid",
        "prizeName": "鑰匙圈",
        "prizeLevel": "E",
        "prizeType": "ACCESSORY",
        "imageUrl": "https://example.com/keychain.jpg"
      }
    ],
    "totalCost": 80,
    "remainingGold": 920,
    "remainingBonus": 0
  }
}
```

---

#### 2.6 十連抽

```bash
curl -X POST http://localhost:8080/api/api/lottery/random/YOUR_LOTTERY_ID/draw \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_USER_TOKEN" \
  -d '{
    "drawCount": 10
  }'
```

---

#### 2.7 查詢獎品池

```bash
curl -X GET http://localhost:8080/api/api/prize-box/my \
  -H "Authorization: Bearer YOUR_USER_TOKEN"
```

**預期回應：**
```json
{
  "success": true,
  "data": [
    {
      "id": "prizebox-uuid-1",
      "prizeName": "鑰匙圈",
      "prizeLevel": "E",
      "lotteryTitle": "鬼滅之刃一番賞",
      "status": "UNREDEEMED",
      "drawTime": "2026-01-16T10:30:00"
    }
  ]
}
```

---

#### 2.8 兌換獎品（建立訂單）

```bash
curl -X POST http://localhost:8080/api/api/prize-box/YOUR_PRIZEBOX_ID/redeem \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_USER_TOKEN" \
  -d '{
    "recipientName": "王小明",
    "recipientPhone": "0912345678",
    "postalCode": "100",
    "city": "台北市",
    "district": "中正區",
    "address": "重慶南路一段122號",
    "notes": "請於平日送達"
  }'
```

---

#### 2.9 查詢訂單

```bash
curl -X GET http://localhost:8080/api/api/order/my \
  -H "Authorization: Bearer YOUR_USER_TOKEN"
```

---

## 🎯 核心功能測試檢查清單

### ✅ 整合 API 功能

- [ ] 一次建立商品+獎品
- [ ] 商品資訊正確儲存
- [ ] 所有獎品正確關聯到商品
- [ ] 統計數據正確計算
- [ ] 回應格式符合規範

### ✅ 抽獎功能

- [ ] 加權隨機演算法正常運作
- [ ] Gold 優先扣款機制正確
- [ ] 獎品庫存正確減少
- [ ] 獎品進入獎品池
- [ ] 十連抽正常運作

### ✅ 獎品池與訂單

- [ ] 獎品池正確顯示
- [ ] 兌換功能正常
- [ ] 訂單正確建立
- [ ] 訂單狀態正確更新

### ✅ 錢包功能

- [ ] 餘額正確顯示
- [ ] 扣款正確執行
- [ ] 交易記錄正確儲存

---

## 🐛 常見問題

### 1. 401 Unauthorized

**原因：** Token 過期或無效

**解決：** 重新登入取得新的 Token

---

### 2. 403 Forbidden

**原因：** 沒有權限

**檢查：**
- 後台 API 是否使用 adminToken
- 前台 API 是否使用 userToken

---

### 3. 餘額不足

**原因：** Gold/Bonus 不足以抽獎

**解決：**
```bash
# 後台直接修改用戶餘額
PUT /admin/user/{userId}
{
  "goldBalance": 10000
}
```

---

### 4. 商品狀態錯誤

**原因：** 商品狀態為 OFF_SHELF

**解決：**
```bash
PUT /admin/lottery-with-prizes/{lotteryId}
{
  "lottery": {
    "status": "ON_SHELF"
  }
}
```

---

## 📊 測試資料建議

### 商品設定

```json
{
  "pricePerDraw": 80,
  "totalDraws": 100
}
```

### 獎品設定（權重建議）

| 等級 | 數量 | 權重 | 機率 |
|------|------|------|------|
| A | 1 | 5 | 4.2% |
| B | 5 | 10 | 8.3% |
| C | 10 | 20 | 16.7% |
| D | 20 | 30 | 25% |
| E | 30 | 50 | 45.8% |

---

## 🎉 測試成功標準

### 後台管理

- [✅] 整合 API 成功建立商品+獎品
- [✅] 查詢 API 正確返回完整資料
- [✅] 統計數據計算正確

### 前台功能

- [✅] 用戶註冊登入成功
- [✅] 抽獎功能正常運作
- [✅] 獎品正確進入獎品池
- [✅] 兌換建立訂單成功

### 資料完整性

- [✅] 商品與獎品正確關聯
- [✅] 錢包交易記錄完整
- [✅] 訂單資訊完整

---

**最後更新：** 2026-01-16  
**測試工具：** Postman Collection 已準備好
