# 🎯 KUJI 抽獎系統 - 當前實作狀態與待辦清單

## ✅ 已完成（不需要重複建立）

### 資料庫層 (Database)
- ✅ lottery 表
- ✅ lottery_prize 表
- ✅ prize_box 表
- ✅ order 表
- ✅ order_item 表
- ✅ user_wallet 表
- ✅ wallet_transaction 表

### 實體層 (Entity)
- ✅ Lottery.java
- ✅ LotteryPrize.java
- ✅ PrizeBox.java
- ✅ Order.java
- ✅ OrderItem.java
- ✅ UserWallet.java
- ✅ WalletTransaction.java

### Mapper 層
- ✅ LotteryMapper.java + XML
- ✅ LotteryPrizeMapper.java + XML
- ✅ PrizeBoxMapper.java + XML
- ✅ OrderMapper.java + XML
- ✅ OrderItemMapper.java + XML
- ✅ UserWalletMapper.java + XML
- ✅ WalletTransactionMapper.java + XML

### Service 介面
- ✅ LotteryService.java（已定義完整介面）
- ✅ LotteryPrizeService.java（已定義完整介面）
- ✅ PrizeBoxService.java（已定義完整介面）
- ✅ OrderService.java（已定義完整介面）
- ✅ WalletService.java（已定義完整介面）
- ⚠️ DrawService.java（檔案存在但是空的）

### Controller
- ✅ AdminLotteryController.java（後台商品管理）
- ✅ LotteryPrizeController.java（獎品管理）
- ✅ AdminOrderController.java（後台訂單管理）
- ✅ AdminPrizeBoxController.java（後台賞品盒管理）
- ✅ PrizeBoxController.java（前台賞品盒）
- ✅ OrderController.java（前台訂單）
- ✅ WalletController.java（前台錢包）
- ✅ AdminWalletController.java（後台錢包）

---

## 🔧 需要補齊的功能

### Priority 1: Service 實作層（impl）⚡ 最重要

檢查以下 Service 實作是否存在：

#### 1.1 LotteryServiceImpl ❓
位置：`src/main/java/com/group/admin/service/impl/LotteryServiceImpl.java`

**需要實作的方法**：
```java
✅ createLottery() - 建立商品
✅ updateLottery() - 更新商品
✅ deleteLottery() - 刪除商品
✅ getLotteryById() - 查詢商品
✅ queryLotteries() - 查詢商品列表（支援條件查詢）
❓ copyLottery() - 複製商品（包含獎品池）
```

#### 1.2 LotteryPrizeServiceImpl ❓
位置：`src/main/java/com/group/admin/service/impl/LotteryPrizeServiceImpl.java`

**需要實作的方法**：
```java
✅ createPrize() - 建立獎品
✅ createPrizes() - 批量建立獎品
✅ updatePrize() - 更新獎品
✅ deletePrize() - 刪除獎品
✅ getPrizeById() - 查詢獎品
✅ getPrizesByLotteryId() - 查詢商品的所有獎品
❓ decreaseRemaining() - 扣除剩餘數量（抽獎時用）
```

#### 1.3 DrawServiceImpl ❗ 空檔案，需要完整實作
位置：`src/main/java/com/group/admin/service/impl/DrawServiceImpl.java`

**需要實作的方法**：
```java
❌ executeDraw() - 執行抽獎
   - 驗證商品狀態
   - 驗證錢包餘額
   - 扣除點數（優先 Gold）
   - 根據 weight 隨機抽取獎品
   - 扣除 lottery_prize.remaining
   - 建立 prize_box 記錄
   - 建立 wallet_transaction 記錄
   - 返回抽獎結果

❌ getDrawHistory() - 查詢抽獎記錄
```

#### 1.4 PrizeBoxServiceImpl ❓
位置：`src/main/java/com/group/admin/service/impl/PrizeBoxServiceImpl.java`

**需要實作的方法**：
```java
✅ addToPrizeBox() - 新增獎品到賞品盒（抽獎後自動執行）
✅ getPrizeBox() - 查詢玩家賞品盒
✅ getSummaryByStore() - 按店家分組查詢
❓ shipPrizes() - 選擇獎品出貨（呼叫 OrderService）
❓ recyclePrizes() - 回收獎品換紅利
```

#### 1.5 OrderServiceImpl ❓
位置：`src/main/java/com/group/admin/service/impl/OrderServiceImpl.java`

**需要實作的方法**：
```java
✅ createOrdersFromPrizeBox() - 從賞品盒建立訂單
   - 驗證所有 prize_box 都屬於該玩家
   - 依 store_id 分組
   - 為每個店家建立一筆訂單
   - 建立 order_item
   - 更新 prize_box.status = IN_ORDER
   - 更新 prize_box.order_id

✅ getOrders() - 查詢訂單列表
✅ getOrderDetail() - 查詢訂單詳情
❓ updateOrderStatus() - 更新訂單狀態
❓ shipOrder() - 標記為已出貨
❓ cancelOrder() - 取消訂單（僅未出貨）
```

#### 1.6 WalletServiceImpl ❓
位置：`src/main/java/com/group/admin/service/impl/WalletServiceImpl.java`

**需要實作的方法**：
```java
✅ getOrCreateWallet() - 取得或建立錢包
✅ getWalletByUserId() - 查詢錢包
❓ consume() - 消費點數（抽獎時用）
   - 優先扣除 Gold
   - Gold 不足時扣 Bonus
   - 建立 wallet_transaction
   
❓ recharge() - 儲值（管理員手動調整）
❓ grantBonus() - 贈送紅利
❓ recyclePrizeToBonus() - 回收獎品換紅利
```

---

### Priority 2: 前台抽獎 Controller ⏰ 重要

#### 2.1 LotteryDrawController ❓
位置：`src/main/java/com/group/admin/controller/api/LotteryDrawController.java`

**API 端點**：
```java
POST /api/lottery/{lotteryId}/draw
Request: {
  "count": 5  // 抽獎次數
}

Response: {
  "results": [
    {
      "prizeBoxId": "pb-uuid-1",
      "lotteryTitle": "鬼滅之刃一番賞",
      "prizeName": "炭治郎公仔",
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

---

### Priority 3: API 測試文件 📝

需要建立 Postman Collection：
- [ ] 商品管理 API
- [ ] 獎品管理 API
- [ ] 抽獎 API
- [ ] 賞品盒 API
- [ ] 訂單 API
- [ ] 錢包 API

---

## 📋 實作檢查清單

### Step 1: 檢查現有實作 ⏰ 立即執行
```bash
# 檢查 Service 實作是否存在
ls src/main/java/com/group/admin/service/impl/

# 檢查哪些 Service 已經實作
grep -r "class.*ServiceImpl" src/main/java/com/group/admin/service/impl/
```

### Step 2: 補齊 DrawService ⏰ 最優先
1. 建立 `DrawServiceImpl.java`
2. 實作抽獎邏輯：
   - 加權隨機演算法
   - 錢包扣款
   - 獎品池扣庫存
   - 建立賞品盒記錄

### Step 3: 補齊 PrizeBoxService 的出貨與回收功能
1. `shipPrizes()` - 呼叫 OrderService
2. `recyclePrizes()` - 回收換紅利

### Step 4: 補齊 OrderService 的狀態管理
1. `updateOrderStatus()`
2. `shipOrder()`
3. `cancelOrder()`

### Step 5: 補齊 WalletService 的消費與回收
1. `consume()` - 抽獎消費
2. `recyclePrizeToBonus()` - 回收獎品

### Step 6: 建立 LotteryDrawController
1. POST /api/lottery/{id}/draw
2. GET /api/lottery/{id}/draw-history

### Step 7: 整合測試
1. 完整流程測試：儲值 → 抽獎 → 賞品盒 → 出貨 → 訂單
2. 權限測試
3. 併發測試

---

## 🎯 下一步行動

### 選項 A：立即檢查現有實作狀態（推薦）
```bash
告訴我：「檢查現有實作」
```
我會幫你檢查所有 Service 實作的完整度

### 選項 B：直接開始補齊 DrawService
```bash
告訴我：「開始實作 DrawService」
```
我會幫你建立完整的抽獎邏輯

### 選項 C：測試現有功能
```bash
告訴我：「測試商品獎品管理」
```
我會幫你建立測試腳本

---

## 📝 關鍵業務邏輯提醒

### 抽獎邏輯（DrawService）
```java
// 1. 驗證商品狀態與庫存
// 2. 計算消費金額
// 3. 驗證錢包餘額
// 4. 扣除點數（優先 Gold，不足時用 Bonus）
// 5. 根據 weight 隨機抽取獎品（加權隨機）
// 6. 扣除 lottery_prize.remaining（樂觀鎖）
// 7. 建立 prize_box（status: IN_BOX）
// 8. 建立 wallet_transaction
// 9. 返回結果
```

### 出貨邏輯（PrizeBoxService + OrderService）
```java
// 1. 驗證所有 prize_box 都是 IN_BOX 狀態
// 2. 驗證都屬於該玩家
// 3. 依 store_id 分組
// 4. 為每個店家建立一筆訂單
// 5. 建立 order_item（複製獎品資訊）
// 6. 更新 prize_box.status = IN_ORDER
// 7. 更新 prize_box.order_id
```

### 回收邏輯（PrizeBoxService + WalletService）
```java
// 1. 驗證 prize_box.status = IN_BOX
// 2. 驗證 is_recyclable = true
// 3. 計算回收紅利
// 4. 更新 wallet.bonus
// 5. 建立 wallet_transaction（type: PRIZE_RECYCLE）
// 6. 更新 prize_box.status = RECYCLED
// 7. 更新 prize_box.recycled_at
```

---

**準備好了嗎？請告訴我要從哪裡開始！** 🚀

建議從「檢查現有實作」開始，這樣我們可以知道具體還缺什麼。
