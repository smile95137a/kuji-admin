# 商品與獎品整合 API 使用指南

## 📝 概述

這是一個整合商品（Lottery）和獎品（LotteryPrize）管理的 API，讓前端可以用**一支 API 完成商品+獎品的新增、編輯、查詢**，不用分兩次呼叫。

## 🆚 與原 API 的差異

| 原 API | 整合 API | 優勢 |
|--------|---------|------|
| POST /admin/lottery<br>POST /admin/lotteries/{id}/prizes | POST /admin/lottery-with-prizes | **一次完成** |
| PUT /admin/lottery/{id}<br>PUT /admin/lotteries/prizes/{prizeId} | PUT /admin/lottery-with-prizes/{id} | **一次完成** |
| GET /admin/lottery/{id}<br>GET /admin/lotteries/{id}/prizes | GET /admin/lottery-with-prizes/{id} | **一次返回** |

## 🎯 API 清單

### 1️⃣ 建立商品與獎品

**端點：** `POST /admin/lottery-with-prizes`

**權限：** `ROLE_ADMIN`, `ROLE_STORE_OWNER`, `ROLE_STORE_EDITOR`

**請求範例：**
```json
{
  "lottery": {
    "title": "鬼滅之刃一番賞",
    "description": "限量發售的鬼滅之刃一番賞",
    "imageUrl": "https://example.com/kimetsu.jpg",
    "category": "OFFICIAL_ICHIBAN",
    "pricePerDraw": 80,
    "totalDraws": 100,
    "status": "OFF_SHELF"
  },
  "prizes": [
    {
      "name": "炭治郎 手辦",
      "description": "約 20cm 高的炭治郎精緻公仔",
      "imageUrl": "https://example.com/tanjiro.jpg",
      "level": "A",
      "quantity": 1,
      "weight": 5,
      "prizeType": "physical",
      "isGrandPrize": true,
      "isLastPrize": false
    },
    {
      "name": "禰豆子 吊飾",
      "imageUrl": "https://example.com/nezuko.jpg",
      "level": "B",
      "quantity": 5,
      "weight": 10,
      "prizeType": "physical"
    },
    {
      "name": "善逸 海報",
      "level": "C",
      "quantity": 20,
      "weight": 20,
      "prizeType": "physical"
    }
  ]
}
```

**回應範例：**
```json
{
  "id": "lottery-uuid",
  "storeId": "store-uuid",
  "storeName": "KUJI 一番賞專賣店",
  "title": "鬼滅之刃一番賞",
  "description": "限量發售的鬼滅之刃一番賞",
  "imageUrl": "https://example.com/kimetsu.jpg",
  "category": "OFFICIAL_ICHIBAN",
  "pricePerDraw": 80,
  "totalDraws": 100,
  "remainingDraws": 100,
  "status": "OFF_SHELF",
  "createdAt": "2025-12-25T10:00:00",
  "prizes": [
    {
      "id": "prize-uuid-1",
      "lotteryId": "lottery-uuid",
      "name": "炭治郎 手辦",
      "level": "A",
      "quantity": 1,
      "remaining": 1,
      "weight": 5,
      "isGrandPrize": true
    },
    {
      "id": "prize-uuid-2",
      "lotteryId": "lottery-uuid",
      "name": "禰豆子 吊飾",
      "level": "B",
      "quantity": 5,
      "remaining": 5,
      "weight": 10
    },
    {
      "id": "prize-uuid-3",
      "lotteryId": "lottery-uuid",
      "name": "善逸 海報",
      "level": "C",
      "quantity": 20,
      "remaining": 20,
      "weight": 20
    }
  ],
  "totalPrizeCount": 26,
  "remainingPrizeCount": 26,
  "progressPercentage": 0.0
}
```

### 2️⃣ 更新商品與獎品

**端點：** `PUT /admin/lottery-with-prizes/{lotteryId}`

**權限：** `ROLE_ADMIN`, `ROLE_STORE_OWNER`, `ROLE_STORE_EDITOR`

**更新邏輯：**
- 商品資訊：只更新有傳的欄位（部分更新）
- 獎品列表：
  - **有 ID** → 更新現有獎品
  - **沒有 ID** → 新增獎品
  - **沒傳** → 保留（不刪除）

**請求範例（同時更新商品和獎品）：**
```json
{
  "lottery": {
    "title": "鬼滅之刃一番賞（更新）",
    "pricePerDraw": 85,
    "status": "ON_SHELF"
  },
  "prizes": [
    {
      "id": "prize-uuid-1",  // ✅ 有 ID → 更新
      "name": "炭治郎 手辦（限量版）",
      "quantity": 2
    },
    {
      // ❌ 沒有 ID → 新增
      "name": "伊之助 徽章",
      "level": "D",
      "quantity": 30,
      "weight": 25,
      "prizeType": "physical"
    }
  ]
}
```

**使用場景：**

1. **只更新商品**
```json
{
  "lottery": {
    "status": "ON_SHELF"
  }
}
```

2. **只更新獎品**
```json
{
  "prizes": [
    {
      "id": "prize-uuid-1",
      "quantity": 3
    }
  ]
}
```

3. **同時更新商品和獎品**（如上範例）

### 3️⃣ 查詢商品與獎品

**端點：** `GET /admin/lottery-with-prizes/{lotteryId}`

**權限：** `ROLE_ADMIN`, `ROLE_STORE_OWNER`, `ROLE_STORE_EDITOR`

**回應範例：**
```json
{
  "id": "lottery-uuid",
  "storeId": "store-uuid",
  "storeName": "KUJI 一番賞專賣店",
  "title": "鬼滅之刃一番賞",
  "category": "OFFICIAL_ICHIBAN",
  "pricePerDraw": 80,
  "totalDraws": 100,
  "remainingDraws": 85,
  "status": "ON_SHELF",
  "prizes": [
    {
      "id": "prize-uuid-1",
      "name": "炭治郎 手辦",
      "level": "A",
      "quantity": 1,
      "remaining": 0,
      "weight": 5,
      "isGrandPrize": true
    },
    {
      "id": "prize-uuid-2",
      "name": "禰豆子 吊飾",
      "level": "B",
      "quantity": 5,
      "remaining": 3,
      "weight": 10
    }
  ],
  "totalPrizeCount": 26,
  "remainingPrizeCount": 18,
  "progressPercentage": 30.77
}
```

## 📊 統計欄位說明

回應中包含統計資訊：

- **totalPrizeCount**：所有獎品的總數量（Σ quantity）
- **remainingPrizeCount**：所有獎品的剩餘數量（Σ remaining）
- **progressPercentage**：抽獎進度百分比
  - 計算公式：`(totalPrizeCount - remainingPrizeCount) / totalPrizeCount * 100`
  - 例如：總共 26 個獎品，已抽出 8 個，進度 = 8/26 = 30.77%

## 🔒 權限說明

### StoreOwner / StoreEditor
- ✅ 自動帶入第一個店家 ID
- ✅ 只能管理自己店家的商品
- ❌ 不能跨店家操作

### Admin
- ✅ 可以管理所有店家的商品
- ⚠️ 新增商品時**必須明確指定 storeId**

## 💡 前端整合範例

### React + Axios

```typescript
// 1. 新增商品與獎品
const createLotteryWithPrizes = async (data: LotteryWithPrizesCreateReq) => {
  const response = await axios.post('/admin/lottery-with-prizes', data, {
    headers: { Authorization: `Bearer ${token}` }
  });
  return response.data;
};

// 2. 更新商品與獎品
const updateLotteryWithPrizes = async (lotteryId: string, data: LotteryWithPrizesUpdateReq) => {
  const response = await axios.put(`/admin/lottery-with-prizes/${lotteryId}`, data, {
    headers: { Authorization: `Bearer ${token}` }
  });
  return response.data;
};

// 3. 查詢商品與獎品
const getLotteryWithPrizes = async (lotteryId: string) => {
  const response = await axios.get(`/admin/lottery-with-prizes/${lotteryId}`, {
    headers: { Authorization: `Bearer ${token}` }
  });
  return response.data;
};

// 使用範例
const handleSubmit = async () => {
  const result = await createLotteryWithPrizes({
    lottery: {
      title: '鬼滅之刃一番賞',
      category: 'OFFICIAL_ICHIBAN',
      pricePerDraw: 80,
      totalDraws: 100
    },
    prizes: [
      {
        name: '炭治郎 手辦',
        level: 'A',
        quantity: 1,
        weight: 5,
        isGrandPrize: true
      },
      {
        name: '禰豆子 吊飾',
        level: 'B',
        quantity: 5,
        weight: 10
      }
    ]
  });
  
  console.log('建立成功！商品 ID:', result.id);
  console.log('獎品總數:', result.totalPrizeCount);
};
```

## ❓ 常見問題

### Q1: 原來的 API 還可以用嗎？
**A:** 可以！整合 API 和原 API 是**並存**的，不會互相影響。

- 原 API：`/admin/lottery/**`, `/admin/lotteries/{id}/prizes/**`
- 整合 API：`/admin/lottery-with-prizes/**`

### Q2: 更新時獎品會被刪除嗎？
**A:** 不會！更新邏輯是：
- 有 ID → 更新
- 沒有 ID → 新增
- 沒傳 → **保留（不刪除）**

如果要刪除獎品，請使用原 API 的 DELETE endpoint。

### Q3: 可以只更新商品不更新獎品嗎？
**A:** 可以！只傳 `lottery` 不傳 `prizes` 即可。

```json
{
  "lottery": {
    "status": "ON_SHELF"
  }
}
```

### Q4: 可以只更新獎品不更新商品嗎？
**A:** 可以！只傳 `prizes` 不傳 `lottery` 即可。

```json
{
  "prizes": [
    {
      "id": "prize-uuid-1",
      "quantity": 3
    }
  ]
}
```

## 📝 完整欄位對照表

### Lottery 商品欄位

| 欄位 | 類型 | 必填 | 說明 | 範例 |
|------|------|------|------|------|
| storeId | String | ❌ | 店家 ID（StoreOwner 可不傳） | "store-uuid" |
| title | String | ✅ | 商品名稱 | "鬼滅之刃一番賞" |
| description | String | ❌ | 商品描述 | "限量發售" |
| imageUrl | String | ❌ | 商品主圖 | "https://..." |
| category | String | ✅ | 商品分類 | "OFFICIAL_ICHIBAN" |
| subCategory | String | ❌ | 子分類 | "LOTTERY_MODE" |
| pricePerDraw | Long | ✅ | 每抽價格 | 80 |
| discountedPrice | Long | ❌ | 折扣價格 | 60 |
| autoDiscountEnabled | Boolean | ❌ | 是否自動降價 | true |
| totalDraws | Integer | ❌ | 總抽數 | 100 |
| status | String | ❌ | 狀態 | "ON_SHELF" |

### LotteryPrize 獎品欄位

| 欄位 | 類型 | 必填 | 說明 | 範例 |
|------|------|------|------|------|
| id | String | 更新時必填 | 獎品 ID（新增時不傳） | "prize-uuid" |
| name | String | ✅ | 獎品名稱 | "炭治郎 手辦" |
| description | String | ❌ | 獎品描述 | "20cm 高公仔" |
| imageUrl | String | ❌ | 獎品圖片 | "https://..." |
| level | String | ❌ | 獎項等級 | "A" |
| prizeNumber | String | ❌ | 籤號 | "01" |
| quantity | Integer | ✅ | 總數量 | 1 |
| weight | Integer | ❌ | 權重 | 5 |
| prizeType | String | ❌ | 獎品類型 | "physical" |
| pointValue | Long | ❌ | 點數價值 | 100 |
| isLastPrize | Boolean | ❌ | 是否為最後賞 | false |
| isGrandPrize | Boolean | ❌ | 是否為大賞 | true |

## 🎉 總結

使用整合 API 的優勢：

1. **減少 API 呼叫次數**：原本 2 次呼叫 → 現在 1 次
2. **簡化前端邏輯**：不用處理 lotteryId 傳遞
3. **資料一致性**：商品和獎品在同一個交易中完成
4. **更好的開發體驗**：一次查詢返回完整資料

如有任何問題，請聯繫開發團隊！
