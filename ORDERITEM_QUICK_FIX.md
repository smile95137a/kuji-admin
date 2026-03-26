# order_item prize_name NULL 錯誤 - 快速修復

## 🔴 錯誤
```
Column 'prize_name' cannot be null
POST /api/prize-box/ship
```

## ✅ 原因
OrderItem 未從 LotteryPrize 表查詢獎品信息，導致 prize_name 為 NULL。

## 🔧 修復（已完成）

### 改動 1：添加 LotteryPrizeMapper 注入
```java
// OrderServiceImpl.java 第 46 行
private final LotteryPrizeMapper lotteryPrizeMapper;  // ← 新增
```

### 改動 2：修復 createOrdersFromPrizeBox 方法
```java
// OrderServiceImpl.java 第 96-115 行
Lottery lottery = lotteryMapper.selectByPrimaryKey(prizeBox.getLotteryId());
LotteryPrize prize = lotteryPrizeMapper.selectByPrimaryKey(prizeBox.getPrizeId());

OrderItem item = new OrderItem();
item.setId(UUID.randomUUID().toString());
item.setOrderId(order.getId());
item.setPrizeBoxId(prizeBox.getId());
item.setLotteryId(prizeBox.getLotteryId());
item.setLotteryTitle(lottery != null ? lottery.getTitle() : "未知商品");
item.setLotteryImageUrl(lottery != null ? lottery.getImageUrl() : null);
item.setPrizeId(prizeBox.getPrizeId());
item.setPrizeName(prize != null ? prize.getName() : "未知獎品");        // ✅ 填入值
item.setPrizeImageUrl(prize != null ? prize.getImageUrl() : null);    // ✅ 填入值
item.setPrizeLevel(prize != null ? prize.getLevel() : null);          // ✅ 填入值
item.setCreatedAt(LocalDateTime.now());

orderItemMapper.insert(item);
```

## 📋 狀態
- ✅ 代碼修改完成
- ✅ 編譯檢查通過（0 errors）
- ⏳ 等待重新部署

## 🚀 下一步

### 1. 重新打包
```bash
mvn clean package -DskipTests
```

### 2. 部署到 EC2
```bash
pkill -f admin-1.0.0.jar
nohup java -jar target/admin-1.0.0.jar > app.log 2>&1 &
tail -f app.log
```

### 3. 測試
```bash
POST /api/prize-box/ship
{
  "prizeBoxIds": ["box-id-1", "box-id-2"],
  "shippingMethod": "HOME_DELIVERY",
  "recipientName": "test",
  "recipientPhone": "0912345678",
  "recipientAddress": "test address",
  "storeCode": "STORE001",
  "storeName": "Test Store",
  "storeAddress": "test address"
}

# 預期：success: true
```

## 📚 詳細文檔
`ORDERITEM_PRIZE_NAME_NULL_FIX.md`

---
**修復完成**: 2026-02-09  
**狀態**: ⏳ 等待部署
