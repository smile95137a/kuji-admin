# 獎品盒、訂單與錢包系統完整實作指南

## 📋 系統概述

根據業務需求文件，實作以下核心流程：

```
玩家儲值 → 獲得 Gold/Bonus 點數 → 使用點數抽獎 → 獎品進入賞品盒 
→ 從賞品盒選擇獎品出貨 → 產生訂單 → 店家處理出貨
```

---

## 🗄️ 資料庫設計

### 1. 錢包系統（Wallet）

```sql
-- 玩家錢包（每個玩家一個）
CREATE TABLE wallet (
    id VARCHAR(50) PRIMARY KEY COMMENT '錢包ID',
    user_id VARCHAR(50) NOT NULL UNIQUE COMMENT '玩家ID',
    gold BIGINT DEFAULT 0 COMMENT '儲值金（可消費）',
    bonus BIGINT DEFAULT 0 COMMENT '紅利金（贈送）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id)
) COMMENT '玩家錢包';

-- 錢包交易記錄
CREATE TABLE wallet_transaction (
    id VARCHAR(50) PRIMARY KEY COMMENT '交易ID',
    wallet_id VARCHAR(50) NOT NULL COMMENT '錢包ID',
    user_id VARCHAR(50) NOT NULL COMMENT '玩家ID',
    type VARCHAR(20) NOT NULL COMMENT '交易類型：RECHARGE/CONSUME/BONUS_GRANT/PRIZE_RECYCLE',
    amount BIGINT NOT NULL COMMENT '金額（正數=增加，負數=減少）',
    currency_type VARCHAR(10) NOT NULL COMMENT '貨幣類型：GOLD/BONUS',
    balance_after BIGINT NOT NULL COMMENT '交易後餘額',
    reference_id VARCHAR(50) COMMENT '關聯ID（抽獎ID/訂單ID等）',
    reference_type VARCHAR(20) COMMENT '關聯類型：DRAW/ORDER/SYSTEM',
    description VARCHAR(200) COMMENT '描述',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_wallet_user (wallet_id, user_id),
    INDEX idx_created (created_at),
    FOREIGN KEY (wallet_id) REFERENCES wallet(id)
) COMMENT '錢包交易記錄';
```

### 2. 抽獎結果與賞品盒（Prize Box）

```sql
-- 抽獎結果（進入賞品盒）
CREATE TABLE draw_result (
    id VARCHAR(50) PRIMARY KEY COMMENT '抽獎結果ID',
    user_id VARCHAR(50) NOT NULL COMMENT '玩家ID',
    lottery_id VARCHAR(50) NOT NULL COMMENT '商品ID',
    store_id VARCHAR(50) NOT NULL COMMENT '店家ID',
    prize_id VARCHAR(50) NOT NULL COMMENT '獎品ID',
    prize_name VARCHAR(100) NOT NULL COMMENT '獎品名稱',
    prize_image_url VARCHAR(500) COMMENT '獎品圖片',
    prize_level VARCHAR(10) NOT NULL COMMENT '獎項等級：A/B/C/D/E/LAST',
    status VARCHAR(20) DEFAULT 'IN_PRIZE_BOX' COMMENT '狀態：IN_PRIZE_BOX/IN_ORDER/RECYCLED',
    order_id VARCHAR(50) COMMENT '訂單ID（出貨後）',
    drawn_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '抽中時間',
    recycled_at DATETIME COMMENT '回收時間',
    INDEX idx_user_status (user_id, status),
    INDEX idx_store_status (store_id, status),
    INDEX idx_lottery (lottery_id),
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (lottery_id) REFERENCES lottery(id),
    FOREIGN KEY (store_id) REFERENCES store(id),
    FOREIGN KEY (prize_id) REFERENCES prize(id)
) COMMENT '抽獎結果（賞品盒）';
```

### 3. 訂單系統（Order）

```sql
-- 訂單主表
CREATE TABLE `order` (
    id VARCHAR(50) PRIMARY KEY COMMENT '訂單ID',
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '訂單編號',
    user_id VARCHAR(50) NOT NULL COMMENT '玩家ID',
    store_id VARCHAR(50) NOT NULL COMMENT '店家ID（單店家訂單）',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '狀態：PENDING/PREPARING/SHIPPED/COMPLETED/CANCELLED',
    
    -- 收件資訊
    shipping_type VARCHAR(20) NOT NULL COMMENT '配送方式：HOME/CVS_711/CVS_FAMILY',
    recipient_name VARCHAR(50) NOT NULL COMMENT '收件人姓名',
    recipient_phone VARCHAR(20) NOT NULL COMMENT '收件人電話',
    recipient_address VARCHAR(200) COMMENT '收件地址（宅配）',
    cvs_store_name VARCHAR(100) COMMENT '超商店名',
    cvs_store_code VARCHAR(50) COMMENT '超商店號',
    
    -- 訂單備註
    user_note VARCHAR(500) COMMENT '玩家備註',
    admin_note VARCHAR(500) COMMENT '店家備註',
    
    -- 時間記錄
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    preparing_at DATETIME COMMENT '準備出貨時間',
    shipped_at DATETIME COMMENT '已出貨時間',
    completed_at DATETIME COMMENT '完成時間',
    cancelled_at DATETIME COMMENT '取消時間',
    
    INDEX idx_user (user_id),
    INDEX idx_store (store_id),
    INDEX idx_status (status),
    INDEX idx_created (created_at),
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (store_id) REFERENCES store(id)
) COMMENT '訂單主表';

-- 訂單明細（包含的獎品）
CREATE TABLE order_item (
    id VARCHAR(50) PRIMARY KEY COMMENT '明細ID',
    order_id VARCHAR(50) NOT NULL COMMENT '訂單ID',
    draw_result_id VARCHAR(50) NOT NULL COMMENT '抽獎結果ID',
    prize_id VARCHAR(50) NOT NULL COMMENT '獎品ID',
    prize_name VARCHAR(100) NOT NULL COMMENT '獎品名稱',
    prize_image_url VARCHAR(500) COMMENT '獎品圖片',
    lottery_id VARCHAR(50) NOT NULL COMMENT '商品ID',
    lottery_title VARCHAR(100) NOT NULL COMMENT '商品標題',
    prize_level VARCHAR(10) NOT NULL COMMENT '獎項等級',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_order (order_id),
    INDEX idx_draw_result (draw_result_id),
    FOREIGN KEY (order_id) REFERENCES `order`(id),
    FOREIGN KEY (draw_result_id) REFERENCES draw_result(id)
) COMMENT '訂單明細';
```

### 4. 抽獎獎品池（Prize Pool）

```sql
-- 商品獎品池配置
CREATE TABLE lottery_prize_pool (
    id VARCHAR(50) PRIMARY KEY COMMENT '獎品池ID',
    lottery_id VARCHAR(50) NOT NULL COMMENT '商品ID',
    prize_id VARCHAR(50) NOT NULL COMMENT '獎品ID',
    prize_level VARCHAR(10) NOT NULL COMMENT '獎項等級：A/B/C/D/E/LAST',
    total_quantity INT NOT NULL COMMENT '總數量',
    remaining_quantity INT NOT NULL COMMENT '剩餘數量',
    probability DECIMAL(5,2) COMMENT '中獎機率（%）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_lottery (lottery_id),
    INDEX idx_prize (prize_id),
    FOREIGN KEY (lottery_id) REFERENCES lottery(id),
    FOREIGN KEY (prize_id) REFERENCES prize(id)
) COMMENT '商品獎品池';
```

---

## 📦 實體類別（Entity）

### 1. Wallet & WalletTransaction

```java
package com.group.admin.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Wallet {
    private String id;
    private String userId;
    private Long gold;          // 儲值金
    private Long bonus;         // 紅利金
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

@Data
public class WalletTransaction {
    private String id;
    private String walletId;
    private String userId;
    private String type;        // RECHARGE/CONSUME/BONUS_GRANT/PRIZE_RECYCLE
    private Long amount;
    private String currencyType; // GOLD/BONUS
    private Long balanceAfter;
    private String referenceId;
    private String referenceType;
    private String description;
    private LocalDateTime createdAt;
}
```

### 2. DrawResult（賞品盒）

```java
package com.group.admin.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DrawResult {
    private String id;
    private String userId;
    private String lotteryId;
    private String storeId;
    private String prizeId;
    private String prizeName;
    private String prizeImageUrl;
    private String prizeLevel;  // A/B/C/D/E/LAST
    private String status;      // IN_PRIZE_BOX/IN_ORDER/RECYCLED
    private String orderId;
    private LocalDateTime drawnAt;
    private LocalDateTime recycledAt;
}
```

### 3. Order & OrderItem

```java
package com.group.admin.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Order {
    private String id;
    private String orderNo;
    private String userId;
    private String storeId;
    private String status;      // PENDING/PREPARING/SHIPPED/COMPLETED/CANCELLED
    
    // 收件資訊
    private String shippingType; // HOME/CVS_711/CVS_FAMILY
    private String recipientName;
    private String recipientPhone;
    private String recipientAddress;
    private String cvsStoreName;
    private String cvsStoreCode;
    
    // 備註
    private String userNote;
    private String adminNote;
    
    // 時間記錄
    private LocalDateTime createdAt;
    private LocalDateTime preparingAt;
    private LocalDateTime shippedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
}

@Data
public class OrderItem {
    private String id;
    private String orderId;
    private String drawResultId;
    private String prizeId;
    private String prizeName;
    private String prizeImageUrl;
    private String lotteryId;
    private String lotteryTitle;
    private String prizeLevel;
    private LocalDateTime createdAt;
}
```

### 4. LotteryPrizePool

```java
package com.group.admin.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class LotteryPrizePool {
    private String id;
    private String lotteryId;
    private String prizeId;
    private String prizeLevel;
    private Integer totalQuantity;
    private Integer remainingQuantity;
    private BigDecimal probability;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

---

## 🎯 API 設計

### 1. 錢包 API（/api/wallet）

#### 前台 API
```java
// 查詢我的錢包
GET /api/wallet
Response: {
  "gold": 10000,
  "bonus": 500,
  "totalBalance": 10500
}

// 查詢交易記錄
GET /api/wallet/transactions?page=1&size=20&type=CONSUME
Response: {
  "items": [...],
  "total": 100
}
```

#### 後台 API（Admin Only）
```java
// 手動調整玩家錢包
POST /admin/wallet/adjust
Request: {
  "userId": "uuid",
  "currencyType": "GOLD",
  "amount": 1000,
  "reason": "系統補償"
}

// 查詢玩家錢包
GET /admin/wallet/user/{userId}
```

### 2. 抽獎 API（/api/lottery）

#### 前台 API
```java
// 執行抽獎
POST /api/lottery/{lotteryId}/draw
Request: {
  "count": 5  // 抽獎次數
}
Response: {
  "results": [
    {
      "id": "draw-result-uuid",
      "prizeName": "鬼滅之刃 A賞",
      "prizeLevel": "A",
      "prizeImageUrl": "https://..."
    }
  ],
  "goldUsed": 400,
  "bonusUsed": 0,
  "remainingGold": 9600,
  "remainingBonus": 500
}
```

### 3. 賞品盒 API（/api/prize-box）

#### 前台 API
```java
// 查詢我的賞品盒
GET /api/prize-box?storeId={storeId}
Response: {
  "items": [
    {
      "id": "draw-result-uuid",
      "prizeName": "鬼滅之刃 A賞",
      "prizeLevel": "A",
      "prizeImageUrl": "https://...",
      "lotteryTitle": "鬼滅之刃一番賞",
      "storeName": "ABC玩具店",
      "storeId": "store-001",
      "drawnAt": "2025-01-15T10:30:00"
    }
  ],
  "groupedByStore": {
    "store-001": [...]
  }
}

// 回收獎品換紅利
POST /api/prize-box/recycle
Request: {
  "drawResultIds": ["uuid1", "uuid2"]
}
Response: {
  "bonusEarned": 100,
  "recycledCount": 2
}
```

### 4. 訂單 API

#### 前台 API（/api/order）
```java
// 從賞品盒建立訂單
POST /api/order/create-from-prize-box
Request: {
  "drawResultIds": ["uuid1", "uuid2"],
  "shippingType": "HOME",
  "recipientName": "王小明",
  "recipientPhone": "0912345678",
  "recipientAddress": "台北市信義區...",
  "userNote": "請小心包裝"
}
Response: {
  "orders": [  // 依店家拆分
    {
      "orderId": "order-uuid-1",
      "orderNo": "ORD20250115001",
      "storeId": "store-001",
      "storeName": "ABC玩具店",
      "itemCount": 2,
      "items": [...]
    }
  ]
}

// 查詢我的訂單
GET /api/order/my-orders?status=PENDING
Response: {
  "items": [...]
}

// 查詢訂單詳情
GET /api/order/{orderId}
Response: {
  "orderNo": "ORD20250115001",
  "status": "PREPARING",
  "items": [...],
  "shippingInfo": {...}
}
```

#### 後台 API（/admin/order）
```java
// 查詢店家訂單
POST /admin/order/list
Request: {
  "condition": {
    "status": "PENDING",
    "createdAtStart": "2025-01-01",
    "createdAtEnd": "2025-01-31"
  },
  "sortBy": "created_at",
  "sortOrder": "DESC"
}
Response: {
  "items": [...]
}

// 更新訂單狀態
PUT /admin/order/{orderId}/status
Request: {
  "status": "PREPARING",
  "adminNote": "準備包裝中"
}

// 標記為已出貨
PUT /admin/order/{orderId}/ship
Request: {
  "trackingNumber": "1234567890",
  "adminNote": "已寄出"
}

// 取消訂單（僅未出貨）
PUT /admin/order/{orderId}/cancel
Request: {
  "reason": "玩家要求取消"
}
```

### 5. 獎品池 API（/admin/lottery/{lotteryId}/prize-pool）

```java
// 建立獎品池
POST /admin/lottery/{lotteryId}/prize-pool
Request: {
  "prizes": [
    {
      "prizeId": "prize-uuid-1",
      "prizeLevel": "A",
      "quantity": 10,
      "probability": 5.0
    }
  ]
}

// 查詢獎品池
GET /admin/lottery/{lotteryId}/prize-pool
Response: {
  "lotteryId": "uuid",
  "lotteryTitle": "鬼滅之刃一番賞",
  "totalDraws": 100,
  "remainingDraws": 50,
  "prizes": [...]
}

// 更新獎品數量
PUT /admin/lottery/{lotteryId}/prize-pool/{poolId}/quantity
Request: {
  "quantity": 15
}
```

---

## 🔐 權限設計

### 前台 API（/api/\*\*）
- `Wallet`: 需登入（USER）
- `PrizeBox`: 需登入（USER）
- `Order`: 需登入（USER）
- `Lottery/Draw`: 需登入（USER）

### 後台 API（/admin/\*\*）
- `Wallet/Adjust`: ADMIN only
- `Order/List`: ADMIN, STORE_OWNER, STORE_EDITOR（店家隔離）
- `Order/Status`: ADMIN, STORE_OWNER, STORE_EDITOR（店家隔離）
- `PrizePool`: ADMIN, STORE_OWNER（店家隔離）

---

## 🔄 業務流程

### 流程 1：玩家抽獎
1. 前台呼叫 `POST /api/lottery/{lotteryId}/draw`
2. 系統扣除 Gold（優先）或 Bonus
3. 根據獎品池機率抽取獎品
4. 更新 `remaining_quantity`
5. 建立 `draw_result`（狀態：IN_PRIZE_BOX）
6. 建立 `wallet_transaction`（類型：CONSUME）
7. 返回抽獎結果

### 流程 2：查看賞品盒
1. 前台呼叫 `GET /api/prize-box`
2. 查詢 `draw_result` WHERE `user_id=? AND status='IN_PRIZE_BOX'`
3. 依 `store_id` 分組
4. 返回結果

### 流程 3：建立訂單
1. 前台選擇獎品，呼叫 `POST /api/order/create-from-prize-box`
2. 系統驗證所有 `draw_result` 都屬於該玩家
3. **依 `store_id` 分組**（不同店家 = 不同訂單）
4. 為每個店家建立一筆 `order`
5. 建立 `order_item` 關聯獎品
6. 更新 `draw_result.status = 'IN_ORDER'`
7. 更新 `draw_result.order_id`
8. 返回訂單列表

### 流程 4：店家處理訂單
1. 店家後台呼叫 `GET /admin/order/list`（自動帶入 storeId）
2. 只看到自己店家的訂單
3. 標記為準備出貨：`PUT /admin/order/{id}/status`
4. 標記為已出貨：`PUT /admin/order/{id}/ship`
5. 狀態變更：PENDING → PREPARING → SHIPPED → COMPLETED

### 流程 5：回收獎品換紅利
1. 前台呼叫 `POST /api/prize-box/recycle`
2. 驗證 `draw_result.status = 'IN_PRIZE_BOX'`
3. 更新 `draw_result.status = 'RECYCLED'`
4. 計算紅利（例如：獎品價值的 50%）
5. 增加 `wallet.bonus`
6. 建立 `wallet_transaction`（類型：PRIZE_RECYCLE）

---

## ⚠️ 重要規則

### 不可逆規則
1. **抽獎結果不可修改**：`draw_result` 建立後不可刪除
2. **訂單不可退換貨**：訂單狀態只能前進
3. **已出貨訂單不可取消**：`status='SHIPPED'` 後不可取消
4. **回收不可逆**：`status='RECYCLED'` 後不可恢復

### 店家隔離
1. 店家只能看到 `store_id` = 自己的訂單
2. 不同店家的獎品不可合併為同一訂單
3. 店家負責人自動帶入 `storeId`

### 錢包規則
1. 消費優先扣除 Gold
2. Gold 不足時才扣 Bonus
3. Bonus 不可轉現
4. 回收獎品只返還 Bonus

---

## 📊 統計報表

### 店家報表（/admin/report）
```java
// 抽獎營收報表
GET /admin/report/lottery-revenue?startDate=2025-01-01&endDate=2025-01-31
Response: {
  "totalDraws": 5000,
  "totalRevenue": 400000,
  "topLotteries": [...]
}

// 訂單統計
GET /admin/report/order-stats
Response: {
  "pending": 50,
  "preparing": 30,
  "shipped": 100,
  "completed": 500
}
```

---

## 🔧 技術要點

### 1. 並發控制（樂觀鎖）
```java
// lottery_prize_pool 表需要 version 欄位
UPDATE lottery_prize_pool 
SET remaining_quantity = remaining_quantity - 1,
    version = version + 1
WHERE id = ? AND version = ? AND remaining_quantity > 0
```

### 2. 交易管理
```java
@Transactional
public DrawResultRes executeDraw(String lotteryId, int count) {
    // 1. 扣點
    // 2. 抽獎
    // 3. 扣庫存
    // 4. 建立 draw_result
    // 全部成功才 commit
}
```

### 3. 店家隔離（AOP）
```java
@Aspect
public class StoreIsolationAspect {
    @Before("@annotation(StoreRestricted)")
    public void checkStoreAccess(JoinPoint jp) {
        String currentStoreId = SecurityUtils.getCurrentUserPrimaryStoreId();
        // 驗證是否有權限訪問該店家資料
    }
}
```

---

## 📝 實作順序

### Phase 1: 基礎設施（1-2天）
1. 建立資料表（SQL）
2. 建立 Entity 類別
3. 建立 Mapper 介面
4. 單元測試

### Phase 2: 錢包系統（1天）
1. WalletService
2. WalletController（前台 + 後台）
3. 交易記錄查詢

### Phase 3: 抽獎與賞品盒（2天）
1. LotteryPrizePoolService
2. DrawService（抽獎邏輯）
3. PrizeBoxService
4. DrawController（前台）

### Phase 4: 訂單系統（2-3天）
1. OrderService
2. OrderController（前台 + 後台）
3. 狀態流轉
4. 店家隔離驗證

### Phase 5: 測試與優化（1天）
1. 整合測試
2. 權限測試
3. 並發測試
4. 性能優化

---

## 🎉 完成後的測試場景

### 場景 1：完整抽獎流程
1. Admin 建立商品與獎品池
2. User 儲值（Gold = 1000）
3. User 抽獎 5 次（消耗 400 Gold）
4. 查看賞品盒（5 個獎品）
5. 選擇 3 個獎品建立訂單
6. 店家標記為已出貨
7. 剩餘 2 個獎品回收換 Bonus

### 場景 2：多店家訂單拆分
1. User 在店家 A 抽到 3 個獎品
2. User 在店家 B 抽到 2 個獎品
3. 一次選擇全部 5 個獎品出貨
4. 系統自動拆分為 2 筆訂單
5. 店家 A 只看到自己的訂單
6. 店家 B 只看到自己的訂單

---

準備好開始實作了嗎？我們從 Phase 1 開始！ 🚀
