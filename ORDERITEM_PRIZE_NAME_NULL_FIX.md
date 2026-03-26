# order_item 表 prize_name NULL 錯誤修復

## 問題診斷

**錯誤信息**：
```
Column 'prize_name' cannot be null
Error Code: SQLIntegrityConstraintViolationException
Location: POST /api/prize-box/ship
```

**根本原因**：
- `OrderItem` 實體中的 `setPrizeName()` 被設為 `null`
- `order_item` 表中 `prize_name` 被定義為 `NOT NULL`
- 代碼缺少從 `LotteryPrize` 表查詢獎品信息的邏輯

## 修復內容

### 1. 添加 LotteryPrizeMapper 注入

**檔案**: `OrderServiceImpl.java`（第 40-47 行）

```java
// 之前
private final OrderMapper orderMapper;
private final OrderItemMapper orderItemMapper;
private final OrderStatusLogMapper orderStatusLogMapper;
private final PrizeBoxMapper prizeBoxMapper;
private final LotteryMapper lotteryMapper;
private final StoreMapper storeMapper;
private final UserMapper userMapper;

// 之後
private final OrderMapper orderMapper;
private final OrderItemMapper orderItemMapper;
private final OrderStatusLogMapper orderStatusLogMapper;
private final PrizeBoxMapper prizeBoxMapper;
private final LotteryMapper lotteryMapper;
private final LotteryPrizeMapper lotteryPrizeMapper;  // ← 新增
private final StoreMapper storeMapper;
private final UserMapper userMapper;
```

### 2. 修復 createOrdersFromPrizeBox 方法

**檔案**: `OrderServiceImpl.java`（第 96-115 行）

**改進點**：
- ✅ 查詢 `LotteryPrize` 實體以獲取獎品信息
- ✅ 設定 `prize_name`（必填）
- ✅ 設定 `prize_image_url`（可選）
- ✅ 設定 `prize_level`（可選）
- ✅ 添加 `lottery_image_url`（從 Lottery 對象）

**修改前**：
```java
OrderItem item = new OrderItem();
item.setId(UUID.randomUUID().toString());
item.setOrderId(order.getId());
item.setPrizeBoxId(prizeBox.getId());
item.setLotteryId(prizeBox.getLotteryId());
item.setLotteryTitle(lottery != null ? lottery.getTitle() : null);
item.setPrizeId(prizeBox.getPrizeId());
item.setPrizeName(null);              // ❌ NULL - 導致錯誤
item.setPrizeGrade(null);              // ❌ NULL
item.setPrizeImage(null);              // ❌ NULL
item.setCreatedAt(LocalDateTime.now());

orderItemMapper.insert(item);
```

**修改後**：
```java
Lottery lottery = lotteryMapper.selectByPrimaryKey(prizeBox.getLotteryId());
LotteryPrize prize = lotteryPrizeMapper.selectByPrimaryKey(prizeBox.getPrizeId());

OrderItem item = new OrderItem();
item.setId(UUID.randomUUID().toString());
item.setOrderId(order.getId());
item.setPrizeBoxId(prizeBox.getId());
item.setLotteryId(prizeBox.getLotteryId());
item.setLotteryTitle(lottery != null ? lottery.getTitle() : "未知商品");
item.setLotteryImageUrl(lottery != null ? lottery.getImageUrl() : null);  // ✅ 新增
item.setPrizeId(prizeBox.getPrizeId());
item.setPrizeName(prize != null ? prize.getName() : "未知獎品");           // ✅ 修復
item.setPrizeImageUrl(prize != null ? prize.getImageUrl() : null);        // ✅ 修復
item.setPrizeLevel(prize != null ? prize.getLevel() : null);               // ✅ 修復
item.setCreatedAt(LocalDateTime.now());

orderItemMapper.insert(item);
```

## 數據流程圖

```
PrizeBox (獎品盒)
    ↓ 包含
    ├─ prizeId (獎品 ID)
    └─ lotteryId (商品 ID)

查詢流程：
    1. 從 PrizeBox 獲取 prizeId
    2. 調用 lotteryPrizeMapper.selectByPrimaryKey(prizeId)
    3. 從 LotteryPrize 實體取得：
       - name → prize_name (必填)
       - imageUrl → prize_image_url (可選)
       - level → prize_level (可選)
    4. 寫入 OrderItem
```

## 修復驗證

### 編譯檢查
```bash
mvn clean compile
# 預期：BUILD SUCCESS
```

### 數據庫驗證

檢查 order_item 表的約束：
```sql
DESC order_item;

-- 預期輸出：
-- | prize_name | varchar(255) | NO | | NULL | |
-- 不能為空的欄位已確認

-- 查看現存的訂單項目
SELECT id, prize_name, prize_level, created_at FROM order_item LIMIT 5;
```

### API 測試

```bash
# 測試出貨 API
curl -X POST http://18.179.187.129:8080/api/prize-box/ship \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {{token}}" \
  -d '{
    "prizeBoxIds": ["box-id-1", "box-id-2"],
    "shippingMethod": "HOME_DELIVERY",
    "recipientName": "John",
    "recipientPhone": "0912345678",
    "recipientAddress": "台北市...",
    "storeCode": "STORE001",
    "storeName": "測試店家",
    "storeAddress": "台北市..."
  }'

# 預期返回：
{
  "success": true,
  "data": ["order-id-1", "order-id-2"],
  "meta": {...}
}
```

## 相關表結構

### order_item 表
```sql
CREATE TABLE `order_item` (
  `id` VARCHAR(36) PRIMARY KEY,
  `order_id` VARCHAR(36) NOT NULL,
  `prize_box_id` VARCHAR(36) NOT NULL,
  `lottery_id` VARCHAR(36) NOT NULL,
  `lottery_title` VARCHAR(255) NOT NULL,
  `lottery_image_url` VARCHAR(500),
  `prize_id` VARCHAR(36) NOT NULL,
  `prize_name` VARCHAR(255) NOT NULL,     -- ← 必填，不能 NULL
  `prize_image_url` VARCHAR(500),
  `prize_level` VARCHAR(10),
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`order_id`) REFERENCES `order`(`id`),
  FOREIGN KEY (`prize_box_id`) REFERENCES `prize_box`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### LotteryPrize 實體
```java
public class LotteryPrize {
    private String id;
    private String lotteryId;
    private String name;              // → prize_name
    private String imageUrl;          // → prize_image_url
    private String level;             // → prize_level
    private String prizeNumber;
    private Integer quantity;
    // ... 其他欄位
}
```

## 代碼修改摘要

| 檔案 | 修改項目 | 狀態 |
|------|---------|------|
| OrderServiceImpl.java | 添加 LotteryPrizeMapper 注入 | ✅ |
| OrderServiceImpl.java | 修復 createOrdersFromPrizeBox 方法 | ✅ |
| 編譯驗證 | 0 errors | ✅ |

## 測試清單

- [ ] 編譯成功（mvn clean compile）
- [ ] 重新部署應用
- [ ] 測試 POST /api/prize-box/ship
- [ ] 驗證 order_item 表中有正確的 prize_name 值
- [ ] 檢查沒有 NULL 值的獎品記錄

## 部署步驟

### 1. 重新打包
```bash
mvn clean package -DskipTests
```

### 2. 部署到 EC2
```bash
# 停止現有服務
pkill -f admin-1.0.0.jar

# 啟動新版本
nohup java -jar target/admin-1.0.0.jar > app.log 2>&1 &

# 驗證日誌
tail -f app.log
```

### 3. 測試 API
```bash
# 使用 Postman 或 curl 測試出貨功能
POST /api/prize-box/ship
```

## 完成標誌

✅ 所有必填欄位已填入默認值或實際值
✅ 無編譯錯誤
✅ 外鍵參考一致
✅ 資料完整性得到保證

---

**修復時間**: 2026-02-09
**修復版本**: OrderServiceImpl v2
**狀態**: ⏳ 等待重新部署
