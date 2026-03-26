# 🎯 KUJI 抽獎系統架構說明

## 📊 現有架構總覽

### 核心實體關係

```
Store (店家)
  ↓
Lottery (商品/一番賞)
  ↓
LotteryPrize (商品明細/獎品) ← 這是商品下的獎品配置
  ↓
PrizeBox (賞品盒) ← 玩家抽到的獎品
  ↓
Order (訂單)
  ↓
OrderItem (訂單明細) ← 包含獎品資訊
```

---

## 🗄️ 資料表結構分析

### 1. `lottery` - 商品主表（一番賞）
```sql
CREATE TABLE lottery (
    id VARCHAR(50) PRIMARY KEY,
    store_id VARCHAR(50),           -- 所屬店家
    title VARCHAR(200),              -- 商品名稱（例：鬼滅之刃一番賞）
    category VARCHAR(50),            -- 商品分類
    price_per_draw BIGINT,           -- 單抽價格
    total_draws INT,                 -- 總抽數
    status VARCHAR(20),              -- 狀態：ON_SHELF/OFF_SHELF
    ...
)
```

**作用**：商品主檔，類似「鬼滅之刃一番賞」這個商品

---

### 2. `lottery_prize` - 商品明細/獎品配置
```sql
CREATE TABLE lottery_prize (
    id VARCHAR(50) PRIMARY KEY,
    lottery_id VARCHAR(50),          -- 關聯到 lottery.id
    name VARCHAR(100),               -- 獎品名稱（例：炭治郎公仔）
    level VARCHAR(10),               -- 獎項等級（A/B/C/D/E/LAST）
    prize_number VARCHAR(50),        -- 獎號（例：A-01）
    quantity INT,                    -- 總數量
    remaining INT,                   -- 剩餘數量
    weight INT,                      -- 權重（抽獎機率用）
    ...
)
```

**作用**：商品的「明細」，定義這個一番賞裡有哪些獎品、各有幾個

**範例**：
- 鬼滅之刃一番賞（lottery）
  - A賞：炭治郎公仔 × 5（lottery_prize）
  - B賞：禰豆子公仔 × 10（lottery_prize）
  - C賞：鑰匙圈 × 20（lottery_prize）
  - ...

---

### 3. `prize_box` - 賞品盒（玩家抽到的獎品）
```sql
CREATE TABLE prize_box (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50),             -- 玩家ID
    lottery_id VARCHAR(50),          -- 來自哪個商品
    prize_id VARCHAR(50),            -- 抽到的獎品（lottery_prize.id）
    store_id VARCHAR(50),            -- 店家ID
    status VARCHAR(20),              -- IN_BOX/IN_ORDER/RECYCLED
    order_id VARCHAR(50),            -- 如果已出貨，關聯到訂單
    ...
)
```

**作用**：玩家「抽中但尚未出貨」的獎品暫存區

**範例**：
- 玩家 A 抽了 5 次鬼滅之刃一番賞：
  - 抽到 A賞炭治郎公仔 1 個 → 進入 prize_box
  - 抽到 C賞鑰匙圈 3 個 → 進入 prize_box
  - 抽到 E賞文件夾 1 個 → 進入 prize_box

---

### 4. `order` - 訂單主表
```sql
CREATE TABLE `order` (
    id VARCHAR(50) PRIMARY KEY,
    order_number VARCHAR(50),        -- 訂單編號
    user_id VARCHAR(50),             -- 玩家ID
    store_id VARCHAR(50),            -- 店家ID（單店家訂單）
    total_items INT,                 -- 總件數
    shipping_method VARCHAR(20),     -- 配送方式：HOME/CVS_711/CVS_FAMILY
    status VARCHAR(20),              -- PENDING/PREPARING/SHIPPED/COMPLETED
    recipient_name VARCHAR(50),      -- 收件人
    ...
)
```

**作用**：玩家從賞品盒選擇獎品出貨時產生的訂單

---

### 5. `order_item` - 訂單明細（包含獎品資訊）
```sql
CREATE TABLE order_item (
    id VARCHAR(50) PRIMARY KEY,
    order_id VARCHAR(50),            -- 關聯到 order.id
    prize_box_id VARCHAR(50),        -- 關聯到 prize_box.id
    lottery_id VARCHAR(50),          -- 商品ID
    lottery_title VARCHAR(100),      -- 商品名稱
    prize_id VARCHAR(50),            -- 獎品ID
    prize_name VARCHAR(100),         -- 獎品名稱
    prize_level VARCHAR(10),         -- 獎項等級
    prize_image_url VARCHAR(500),    -- 獎品圖片
    ...
)
```

**作用**：訂單包含哪些獎品（每個獎品一筆 order_item）

**範例**：
- 訂單 ORD001（玩家 A 出貨 3 個獎品）
  - order_item 1: A賞炭治郎公仔（來自 prize_box_1）
  - order_item 2: C賞鑰匙圈（來自 prize_box_3）
  - order_item 3: E賞文件夾（來自 prize_box_5）

---

## 🎯 業務流程

### 完整流程：商品 → 抽獎 → 賞品盒 → 訂單

```
1. 店家建立商品 (Lottery)
   ↓
2. 店家配置獎品池 (LotteryPrize)
   - A賞 × 5
   - B賞 × 10
   - C賞 × 20
   ↓
3. 玩家抽獎
   - 系統根據 weight 隨機抽取
   - 扣除 remaining 數量
   - 建立 PrizeBox 記錄（status: IN_BOX）
   ↓
4. 玩家查看賞品盒
   - 看到所有抽中的獎品
   - 可選擇出貨或回收
   ↓
5. 玩家選擇獎品出貨
   - 系統依 store_id 分組（不同店家 = 不同訂單）
   - 建立 Order
   - 建立 OrderItem（包含完整獎品資訊）
   - 更新 PrizeBox.status = IN_ORDER
   - 更新 PrizeBox.order_id
   ↓
6. 店家處理訂單
   - 查看訂單明細（包含所有獎品資訊）
   - 標記為已出貨
   - 更新訂單狀態
```

---

## 🔍 關鍵查詢範例

### 查詢商品的所有獎品（商品明細）
```sql
SELECT 
    lp.id,
    lp.name as prize_name,
    lp.level,
    lp.quantity,
    lp.remaining,
    l.title as lottery_title
FROM lottery_prize lp
LEFT JOIN lottery l ON lp.lottery_id = l.id
WHERE lp.lottery_id = 'lottery-uuid'
ORDER BY lp.order_num;
```

### 查詢玩家賞品盒（包含商品和獎品資訊）
```sql
SELECT 
    pb.id as prize_box_id,
    pb.status,
    l.title as lottery_title,
    lp.name as prize_name,
    lp.level as prize_level,
    lp.image_url as prize_image,
    s.name as store_name
FROM prize_box pb
LEFT JOIN lottery l ON pb.lottery_id = l.id
LEFT JOIN lottery_prize lp ON pb.prize_id = lp.id
LEFT JOIN store s ON pb.store_id = s.id
WHERE pb.user_id = 'user-uuid' 
  AND pb.status = 'IN_BOX'
ORDER BY pb.created_at DESC;
```

### 查詢訂單明細（包含所有獎品資訊）
```sql
SELECT 
    o.order_number,
    o.status,
    oi.lottery_title,
    oi.prize_name,
    oi.prize_level,
    oi.prize_image_url
FROM `order` o
LEFT JOIN order_item oi ON o.id = oi.order_id
WHERE o.id = 'order-uuid';
```

---

## ✅ 已經做好的部分

1. ✅ Entity 類別（MBG 已生成）
   - Lottery.java
   - LotteryPrize.java
   - PrizeBox.java
   - Order.java
   - OrderItem.java

2. ✅ Mapper 介面（MBG 已生成）
   - LotteryMapper.java
   - LotteryPrizeMapper.java
   - PrizeBoxMapper.java
   - OrderMapper.java
   - OrderItemMapper.java

3. ✅ 資料表（已存在）
   - lottery
   - lottery_prize
   - prize_box
   - order
   - order_item

---

## 🚧 需要實作的功能

### Phase 1: 商品獎品池管理（後台）

#### 1.1 建立商品時配置獎品
```java
// POST /admin/lottery
Request: {
  "title": "鬼滅之刃一番賞",
  "pricePerDraw": 80,
  "prizes": [  // ← 建立商品時同時配置獎品
    {
      "name": "炭治郎公仔",
      "level": "A",
      "quantity": 5,
      "weight": 5
    },
    {
      "name": "禰豆子公仔",
      "level": "B",
      "quantity": 10,
      "weight": 10
    }
  ]
}
```

#### 1.2 查詢商品獎品池
```java
// GET /admin/lottery/{lotteryId}/prizes
Response: {
  "lotteryId": "uuid",
  "lotteryTitle": "鬼滅之刃一番賞",
  "totalPrizes": 100,
  "remainingPrizes": 50,
  "prizes": [
    {
      "id": "prize-uuid-1",
      "name": "炭治郎公仔",
      "level": "A",
      "quantity": 5,
      "remaining": 2
    }
  ]
}
```

#### 1.3 更新獎品數量
```java
// PUT /admin/lottery/{lotteryId}/prizes/{prizeId}
Request: {
  "quantity": 10,
  "remaining": 8
}
```

---

### Phase 2: 抽獎功能（前台）

#### 2.1 執行抽獎
```java
// POST /api/lottery/{lotteryId}/draw
Request: {
  "count": 5  // 抽 5 次
}

Response: {
  "results": [
    {
      "prizeBoxId": "pb-uuid-1",
      "prizeName": "炭治郎公仔",
      "prizeLevel": "A",
      "prizeImageUrl": "https://..."
    },
    {
      "prizeBoxId": "pb-uuid-2",
      "prizeName": "鑰匙圈",
      "prizeLevel": "C",
      "prizeImageUrl": "https://..."
    }
  ],
  "goldUsed": 400
}
```

**邏輯**：
1. 驗證玩家錢包餘額
2. 扣除點數（Gold 優先）
3. 根據 weight 隨機抽取獎品
4. 扣除 lottery_prize.remaining
5. 建立 prize_box 記錄

---

### Phase 3: 賞品盒功能（前台）

#### 3.1 查詢賞品盒
```java
// GET /api/prize-box
Response: {
  "items": [
    {
      "prizeBoxId": "pb-uuid-1",
      "lotteryTitle": "鬼滅之刃一番賞",
      "prizeName": "炭治郎公仔",
      "prizeLevel": "A",
      "prizeImageUrl": "https://...",
      "storeName": "ABC玩具店",
      "storeId": "store-001",
      "drawnAt": "2025-01-15T10:30:00"
    }
  ],
  "groupedByStore": {
    "store-001": [...]
  }
}
```

#### 3.2 回收獎品換紅利
```java
// POST /api/prize-box/recycle
Request: {
  "prizeBoxIds": ["pb-uuid-1", "pb-uuid-2"]
}
Response: {
  "bonusEarned": 100,
  "recycledCount": 2
}
```

---

### Phase 4: 訂單功能

#### 4.1 從賞品盒建立訂單（前台）
```java
// POST /api/order/create-from-prize-box
Request: {
  "prizeBoxIds": ["pb-uuid-1", "pb-uuid-2", "pb-uuid-3"],
  "shippingMethod": "HOME",
  "recipientName": "王小明",
  "recipientPhone": "0912345678",
  "recipientAddress": "台北市信義區..."
}

Response: {
  "orders": [  // 依店家拆分
    {
      "orderId": "order-uuid-1",
      "orderNumber": "ORD20250115001",
      "storeId": "store-001",
      "storeName": "ABC玩具店",
      "itemCount": 2,
      "items": [
        {
          "prizeName": "炭治郎公仔",
          "prizeLevel": "A",
          "lotteryTitle": "鬼滅之刃一番賞"
        }
      ]
    }
  ]
}
```

#### 4.2 查詢訂單明細（前台）
```java
// GET /api/order/{orderId}
Response: {
  "orderNumber": "ORD20250115001",
  "status": "PENDING",
  "storeName": "ABC玩具店",
  "shippingMethod": "HOME",
  "recipientName": "王小明",
  "items": [
    {
      "lotteryTitle": "鬼滅之刃一番賞",
      "prizeName": "炭治郎公仔",
      "prizeLevel": "A",
      "prizeImageUrl": "https://..."
    }
  ],
  "createdAt": "2025-01-15T10:30:00"
}
```

#### 4.3 店家訂單管理（後台）
```java
// POST /admin/order/list
Request: {
  "condition": {
    "status": "PENDING"
  }
}

Response: {
  "items": [
    {
      "orderNumber": "ORD20250115001",
      "userName": "王小明",
      "totalItems": 2,
      "status": "PENDING",
      "items": [  // ← 包含獎品明細
        {
          "lotteryTitle": "鬼滅之刃一番賞",
          "prizeName": "炭治郎公仔",
          "prizeLevel": "A"
        }
      ]
    }
  ]
}
```

---

## 📝 實作順序建議

### Step 1: 商品獎品池 API（後台）✅ 優先
- LotteryPrizeService
- AdminLotteryController 擴充
  - POST /admin/lottery（建立商品 + 獎品）
  - GET /admin/lottery/{id}/prizes
  - PUT /admin/lottery/{id}/prizes/{prizeId}

### Step 2: 抽獎功能（前台）
- DrawService（抽獎邏輯 + 權重演算法）
- LotteryController
  - POST /api/lottery/{id}/draw

### Step 3: 賞品盒功能（前台）
- PrizeBoxService
- PrizeBoxController
  - GET /api/prize-box
  - POST /api/prize-box/recycle

### Step 4: 訂單功能（前台 + 後台）
- OrderService
- OrderController（前台）
  - POST /api/order/create-from-prize-box
  - GET /api/order/{id}
- AdminOrderController（後台）
  - POST /admin/order/list
  - PUT /admin/order/{id}/status

---

## 🎯 總結

### 核心概念
1. **Lottery** = 商品（一番賞）
2. **LotteryPrize** = 商品明細/獎品配置（這個商品有哪些獎品）
3. **PrizeBox** = 玩家抽到的獎品（實際抽中的）
4. **OrderItem** = 訂單明細（包含完整獎品資訊）

### 資料流
```
商品配置階段：
Lottery → LotteryPrize（店家配置獎品）

玩家抽獎階段：
LotteryPrize → PrizeBox（玩家抽獎）

訂單建立階段：
PrizeBox → Order + OrderItem（玩家出貨）
```

### 關鍵點
- ✅ 不需要重複建立 Entity（已經有了）
- ✅ 商品明細就是 LotteryPrize
- ✅ 訂單已經有獎品資訊（OrderItem）
- 🔄 需要實作 Service 和 Controller

---

**準備好了嗎？我們從 Step 1 開始實作商品獎品池管理！** 🚀
