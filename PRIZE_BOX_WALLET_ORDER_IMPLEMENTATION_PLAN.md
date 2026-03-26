# 賞品盒 + 金流 + 訂單系統 實作計畫

## 📋 系統架構總覽

```
玩家抽獎 
  ↓ (扣除 Gold/Bonus)
抽獎結果 
  ↓ (自動進入)
賞品盒（Prize Box）
  ↓ (玩家選擇出貨)
訂單（Order）
  ↓ (店家處理)
出貨完成
```

---

## 🎯 核心原則

1. **抽獎 ≠ 訂單**：抽獎只消耗點數，不產生訂單
2. **賞品盒是緩衝**：玩家可決定何時出貨
3. **出貨才產生訂單**：只有確認出貨才建立訂單
4. **訂單店家隔離**：不同店家的獎品不能合併訂單
5. **不可逆原則**：訂單出貨後不可取消、不可退換貨

---

## 📦 Phase 1: 資料表設計

### 1.1 prize_box（賞品盒）
```sql
CREATE TABLE `prize_box` (
  `id` VARCHAR(36) PRIMARY KEY COMMENT 'UUID',
  `user_id` VARCHAR(36) NOT NULL COMMENT '玩家 ID',
  `lottery_id` VARCHAR(36) NOT NULL COMMENT '商品 ID',
  `prize_id` VARCHAR(36) NOT NULL COMMENT '獎項 ID',
  `store_id` VARCHAR(36) NOT NULL COMMENT '店家 ID',
  `draw_result_id` VARCHAR(36) COMMENT '抽獎結果 ID',
  `status` VARCHAR(20) NOT NULL DEFAULT 'IN_BOX' COMMENT '狀態：IN_BOX/SHIPPED/RECYCLED',
  `is_recyclable` TINYINT DEFAULT 1 COMMENT '是否可回收',
  `recycle_bonus` BIGINT COMMENT '回收可得紅利',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
  FOREIGN KEY (`lottery_id`) REFERENCES `lottery`(`id`),
  FOREIGN KEY (`prize_id`) REFERENCES `lottery_prize`(`id`),
  FOREIGN KEY (`store_id`) REFERENCES `store`(`id`),
  INDEX `idx_user_status` (`user_id`, `status`),
  INDEX `idx_store` (`store_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='賞品盒';
```

### 1.2 user_wallet（玩家錢包）
```sql
CREATE TABLE `user_wallet` (
  `id` VARCHAR(36) PRIMARY KEY COMMENT 'UUID',
  `user_id` VARCHAR(36) NOT NULL UNIQUE COMMENT '玩家 ID',
  `gold_coins` BIGINT NOT NULL DEFAULT 0 COMMENT '金幣（儲值金）',
  `bonus_coins` BIGINT NOT NULL DEFAULT 0 COMMENT '紅利幣',
  `version` INT NOT NULL DEFAULT 0 COMMENT '樂觀鎖版本號',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
  INDEX `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='玩家錢包';
```

### 1.3 wallet_transaction（點數異動記錄）
```sql
CREATE TABLE `wallet_transaction` (
  `id` VARCHAR(36) PRIMARY KEY COMMENT 'UUID',
  `user_id` VARCHAR(36) NOT NULL COMMENT '玩家 ID',
  `transaction_type` VARCHAR(20) NOT NULL COMMENT '類型：RECHARGE/DRAW/RECYCLE/REFUND/ADMIN_ADJUST',
  `coin_type` VARCHAR(10) NOT NULL COMMENT '幣種：GOLD/BONUS',
  `amount` BIGINT NOT NULL COMMENT '金額（正數=增加，負數=減少）',
  `balance_after` BIGINT NOT NULL COMMENT '異動後餘額',
  `related_id` VARCHAR(36) COMMENT '關聯 ID（抽獎ID、訂單ID等）',
  `description` VARCHAR(500) COMMENT '說明',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
  INDEX `idx_user_time` (`user_id`, `created_at`),
  INDEX `idx_type` (`transaction_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='點數異動記錄';
```

### 1.4 recharge_plan（儲值方案）
```sql
CREATE TABLE `recharge_plan` (
  `id` VARCHAR(36) PRIMARY KEY COMMENT 'UUID',
  `name` VARCHAR(100) NOT NULL COMMENT '方案名稱',
  `amount` BIGINT NOT NULL COMMENT '儲值金額（台幣）',
  `gold_coins` BIGINT NOT NULL COMMENT '獲得金幣',
  `bonus_coins` BIGINT DEFAULT 0 COMMENT '贈送紅利',
  `is_active` TINYINT DEFAULT 1 COMMENT '是否啟用',
  `display_order` INT DEFAULT 0 COMMENT '顯示順序',
  `start_time` DATETIME COMMENT '活動開始時間',
  `end_time` DATETIME COMMENT '活動結束時間',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_active` (`is_active`, `display_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='儲值方案';
```

### 1.5 recharge_record（儲值記錄）
```sql
CREATE TABLE `recharge_record` (
  `id` VARCHAR(36) PRIMARY KEY COMMENT 'UUID',
  `user_id` VARCHAR(36) NOT NULL COMMENT '玩家 ID',
  `plan_id` VARCHAR(36) COMMENT '儲值方案 ID',
  `amount` BIGINT NOT NULL COMMENT '儲值金額（台幣）',
  `gold_coins` BIGINT NOT NULL COMMENT '獲得金幣',
  `bonus_coins` BIGINT DEFAULT 0 COMMENT '贈送紅利',
  `payment_method` VARCHAR(20) NOT NULL COMMENT '支付方式：CREDIT_CARD',
  `payment_status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '支付狀態：PENDING/SUCCESS/FAILED',
  `payment_gateway` VARCHAR(50) COMMENT '金流商：MASTERCARD',
  `transaction_id` VARCHAR(100) COMMENT '交易序號（金流商回傳）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `paid_at` DATETIME COMMENT '付款完成時間',
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
  FOREIGN KEY (`plan_id`) REFERENCES `recharge_plan`(`id`),
  INDEX `idx_user_time` (`user_id`, `created_at`),
  INDEX `idx_status` (`payment_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='儲值記錄';
```

### 1.6 order（訂單）
```sql
CREATE TABLE `order` (
  `id` VARCHAR(36) PRIMARY KEY COMMENT 'UUID',
  `order_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '訂單編號',
  `user_id` VARCHAR(36) NOT NULL COMMENT '玩家 ID',
  `store_id` VARCHAR(36) NOT NULL COMMENT '店家 ID',
  `total_items` INT NOT NULL COMMENT '商品總數',
  `shipping_method` VARCHAR(20) NOT NULL COMMENT '配送方式：HOME_DELIVERY/SEVEN_ELEVEN/FAMILY_MART',
  `shipping_status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '狀態：PENDING/PREPARING/SHIPPED/COMPLETED/CANCELLED',
  `recipient_name` VARCHAR(100) NOT NULL COMMENT '收件人姓名',
  `recipient_phone` VARCHAR(20) NOT NULL COMMENT '收件人電話',
  `recipient_address` VARCHAR(500) COMMENT '收件地址（宅配）',
  `store_code` VARCHAR(20) COMMENT '超商店號（超商取貨）',
  `store_name` VARCHAR(100) COMMENT '超商店名',
  `tracking_no` VARCHAR(100) COMMENT '物流單號',
  `remark` VARCHAR(500) COMMENT '備註',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `shipped_at` DATETIME COMMENT '出貨時間',
  `completed_at` DATETIME COMMENT '完成時間',
  `cancelled_at` DATETIME COMMENT '取消時間',
  `cancelled_by` VARCHAR(36) COMMENT '取消者 ID',
  `cancel_reason` VARCHAR(500) COMMENT '取消原因',
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
  FOREIGN KEY (`store_id`) REFERENCES `store`(`id`),
  INDEX `idx_user` (`user_id`),
  INDEX `idx_store` (`store_id`),
  INDEX `idx_status` (`shipping_status`),
  INDEX `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='訂單';
```

### 1.7 order_item（訂單明細）
```sql
CREATE TABLE `order_item` (
  `id` VARCHAR(36) PRIMARY KEY COMMENT 'UUID',
  `order_id` VARCHAR(36) NOT NULL COMMENT '訂單 ID',
  `prize_box_id` VARCHAR(36) NOT NULL COMMENT '賞品盒 ID',
  `lottery_id` VARCHAR(36) NOT NULL COMMENT '商品 ID',
  `lottery_title` VARCHAR(255) NOT NULL COMMENT '商品名稱',
  `prize_id` VARCHAR(36) NOT NULL COMMENT '獎項 ID',
  `prize_name` VARCHAR(255) NOT NULL COMMENT '獎項名稱',
  `prize_image_url` VARCHAR(500) COMMENT '獎項圖片',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`order_id`) REFERENCES `order`(`id`),
  FOREIGN KEY (`prize_box_id`) REFERENCES `prize_box`(`id`),
  INDEX `idx_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='訂單明細';
```

---

## 📊 Phase 2: Enum 設計

### 2.1 賞品盒狀態
```java
public enum PrizeBoxStatusEnum {
    IN_BOX("IN_BOX", "在賞品盒中"),
    SHIPPED("SHIPPED", "已出貨"),
    RECYCLED("RECYCLED", "已回收");
}
```

### 2.2 點數類型
```java
public enum CoinTypeEnum {
    GOLD("GOLD", "金幣"),
    BONUS("BONUS", "紅利");
}
```

### 2.3 交易類型
```java
public enum TransactionTypeEnum {
    RECHARGE("RECHARGE", "儲值"),
    DRAW("DRAW", "抽獎消費"),
    RECYCLE("RECYCLE", "獎品回收"),
    REFUND("REFUND", "退款"),
    ADMIN_ADJUST("ADMIN_ADJUST", "系統調整");
}
```

### 2.4 配送方式
```java
public enum ShippingMethodEnum {
    HOME_DELIVERY("HOME_DELIVERY", "宅配到府"),
    SEVEN_ELEVEN("SEVEN_ELEVEN", "7-11 取貨"),
    FAMILY_MART("FAMILY_MART", "全家取貨");
}
```

### 2.5 訂單狀態
```java
public enum OrderStatusEnum {
    PENDING("PENDING", "待處理"),
    PREPARING("PREPARING", "準備出貨中"),
    SHIPPED("SHIPPED", "已出貨"),
    COMPLETED("COMPLETED", "已完成"),
    CANCELLED("CANCELLED", "已取消");
}
```

---

## 🔄 Phase 3: 業務流程實作

### 3.1 抽獎流程（修改）
```
1. 檢查玩家點數是否足夠
2. 扣除點數（Gold 優先）
3. 執行抽獎邏輯
4. 抽獎結果寫入 prize_box
5. 返回抽獎結果
```

### 3.2 賞品盒流程
```
1. 玩家查看賞品盒
2. 選擇要出貨的獎品
3. 系統按店家分組
4. 玩家填寫收件資訊
5. 產生訂單（一店家一訂單）
6. 賞品盒狀態改為 SHIPPED
```

### 3.3 獎品回收流程
```
1. 玩家選擇要回收的獎品
2. 系統檢查是否可回收
3. 計算回收紅利
4. 增加玩家 Bonus
5. 賞品盒狀態改為 RECYCLED
6. 記錄交易
```

### 3.4 訂單出貨流程
```
1. 店家查看訂單列表
2. 店家準備出貨（更新狀態為 PREPARING）
3. 店家填寫物流單號
4. 更新狀態為 SHIPPED
5. 玩家收到通知
6. 玩家確認收貨（更新為 COMPLETED）
```

---

## 🎯 Phase 4: API 設計

### 4.1 前台 API

#### 賞品盒
- `GET /api/prize-box` - 查看賞品盒
- `POST /api/prize-box/ship` - 選擇出貨
- `POST /api/prize-box/recycle` - 回收獎品

#### 錢包
- `GET /api/wallet` - 查看錢包餘額
- `GET /api/wallet/transactions` - 查看交易記錄

#### 儲值
- `GET /api/recharge/plans` - 查看儲值方案
- `POST /api/recharge` - 發起儲值
- `POST /api/recharge/callback` - 金流回調

#### 訂單
- `GET /api/orders` - 查看我的訂單
- `GET /api/orders/{id}` - 訂單詳情

### 4.2 後台 API（店家）

#### 訂單管理
- `POST /api/admin/orders/list` - 查詢訂單列表（自己店家）
- `GET /api/admin/orders/{id}` - 訂單詳情
- `PUT /api/admin/orders/{id}/prepare` - 準備出貨
- `PUT /api/admin/orders/{id}/ship` - 確認出貨（填寫物流單號）
- `PUT /api/admin/orders/{id}/cancel` - 取消訂單（僅未出貨前）

### 4.3 後台 API（Admin）

#### 儲值方案管理
- `POST /api/admin/recharge-plans` - 新增方案
- `PUT /api/admin/recharge-plans/{id}` - 更新方案
- `DELETE /api/admin/recharge-plans/{id}` - 刪除方案

#### 錢包管理
- `POST /api/admin/wallets/adjust` - 手動調整玩家點數
- `GET /api/admin/wallets/{userId}` - 查看玩家錢包
- `GET /api/admin/wallets/transactions` - 查看所有交易記錄

#### 訂單管理
- `POST /api/admin/all-orders/list` - 查詢所有訂單
- `GET /api/admin/all-orders/{id}` - 訂單詳情

---

## ⚠️ 關鍵技術點

### 1. 樂觀鎖（防止超賣）
```java
@Version
private Integer version;

// 更新時會自動檢查版本號
UPDATE user_wallet 
SET gold_coins = gold_coins - 100, version = version + 1
WHERE id = ? AND version = ?
```

### 2. 交易原子性
```java
@Transactional
public DrawResult draw(String userId, String lotteryId) {
    // 1. 扣點
    // 2. 抽獎
    // 3. 寫入 prize_box
    // 任一失敗全部回滾
}
```

### 3. 訂單編號生成
```java
// 格式：ORD + YYYYMMDD + 6位流水號
// 例如：ORD20260107000001
String orderNo = "ORD" + LocalDate.now().format("yyyyMMdd") + sequence;
```

---

## 📅 實作順序建議

### Week 1: 基礎建設
- [ ] 建立所有資料表 DDL
- [ ] 執行 MyBatis Generator
- [ ] 建立所有 Enum
- [ ] 建立所有 DTO

### Week 2: 錢包系統
- [ ] 實作 WalletService
- [ ] 實作點數扣除/增加
- [ ] 實作交易記錄
- [ ] 實作錢包查詢 API

### Week 3: 賞品盒
- [ ] 實作 PrizeBoxService
- [ ] 修改抽獎流程（寫入 prize_box）
- [ ] 實作獎品回收
- [ ] 實作賞品盒查詢 API

### Week 4: 訂單系統
- [ ] 實作 OrderService
- [ ] 實作出貨邏輯（賞品盒 → 訂單）
- [ ] 實作訂單狀態變更
- [ ] 實作訂單查詢 API

### Week 5: 儲值系統
- [ ] 實作 RechargeService
- [ ] 實作儲值方案管理
- [ ] 預留金流 API 串接介面
- [ ] 實作儲值記錄查詢

### Week 6: 測試與優化
- [ ] 單元測試
- [ ] 整合測試
- [ ] 效能優化
- [ ] 文件完善

---

## 🎯 立即開始？

要我現在開始實作嗎？建議順序：

1. **先建立 DDL** - 確保資料表結構正確
2. **執行 MyBatis Generator** - 生成 Entity/Mapper
3. **建立 Enum 和 DTO** - 定義資料結構
4. **實作 Service 層** - 核心業務邏輯
5. **建立 Controller** - API 接口
6. **測試驗證** - 確保功能正確

你想從哪一步開始？還是要我一次全部建立好？
