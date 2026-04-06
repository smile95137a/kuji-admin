# 訂單生命週期 Skill

## 適用情境
- 修改訂單狀態流轉邏輯
- 新增訂單狀態或操作 API
- 了解 PrizeBox → Order 的建立流程
- 修改出貨通知或狀態回寫邏輯
- 調整運費計算方式

---

## 訂單狀態機

```
PrizeBox（IN_BOX）
    │
    │ 玩家申請出貨
    ▼
PENDING（待處理）
    │
    │ 店家確認並準備出貨
    ▼
PREPARING（準備出貨中）
    │
    │ 店家填寫物流單號
    ▼
SHIPPED（已出貨）
    │
    │ 玩家確認收貨
    ▼
COMPLETED（已完成）

PENDING / PREPARING → CANCELLED（已取消，僅限未出貨前）
```

### OrderStatusEnum

```java
PENDING    // 待處理
PREPARING  // 準備出貨中
SHIPPED    // 已出貨
COMPLETED  // 已完成
CANCELLED  // 已取消
```

---

## 建立訂單流程（PrizeBox → Order）

```
① 玩家從賞品盒選擇 prizeBoxIds
② 驗證每個 PrizeBox
   → box 存在
   → box.userId == 當前 userId（所有權）
   → box.status == "IN_BOX"（未出貨）
③ 按店家分組（groupingBy storeId）
④ @Transactional，每個店家建立一筆 Order：
   → 生成 orderId (UUID)
   → 生成 orderNumber（格式：yyyyMMdd + 8位隨機）
   → status = PENDING
   → 建立 OrderItem（一對多，每個 PrizeBox 一筆）
   → 更新 PrizeBox.status = SHIPPING
   → 更新 PrizeBox.orderId = order.getId()
   → 記錄 OrderStatusLog（null → PENDING）
   → 記錄 ConsumptionRecord（運費 60 元）
⑤ 返回 orderIds 列表
```

> ⚠️ 同一玩家、不同店家的 PrizeBox 會建立**多筆訂單**（每家一筆）。

---

## 運費規則

```java
// OrderServiceImpl.java
private static final Long SHIPPING_FEE = 60L;

// 每筆訂單（每個店家）固定收 60 元運費
// 運費計入 ConsumptionRecord（type=SHIPPING_FEE）
// 不計入 WalletTransaction（運費另行處理）
```

---

## 訂單 OrderItem 結構

```java
item.setOrderId(order.getId());
item.setPrizeBoxId(prizeBox.getId());
item.setLotteryId(prizeBox.getLotteryId());
item.setLotteryTitle(lottery.getTitle());      // 快照：商品名稱
item.setLotteryImageUrl(lottery.getImageUrl()); // 快照：商品圖片
item.setPrizeId(prizeBox.getPrizeId());
item.setPrizeName(prize.getName());            // 快照：獎品名稱
item.setPrizeImageUrl(prize.getImageUrl());    // 快照：獎品圖片
item.setPrizeLevel(prize.getLevel());          // 快照：獎品等級
```

> 使用快照原則：商品/獎品名稱存入訂單，避免後續修改影響歷史記錄。

---

## 狀態流轉 API（後台）

| 操作 | API | 可執行角色 | 前置狀態 |
|------|-----|-----------|---------|
| 確認並準備出貨 | `PUT /admin/orders/{id}/preparing` | ADMIN / STORE_OWNER | PENDING |
| 出貨（填物流單號） | `PUT /admin/orders/{id}/ship` | ADMIN / STORE_OWNER | PREPARING |
| 確認完成 | `PUT /admin/orders/{id}/complete` | ADMIN | SHIPPED |
| 取消訂單 | `PUT /admin/orders/{id}/cancel` | ADMIN / STORE_OWNER | PENDING / PREPARING |

### 出貨操作 Request

```json
POST /admin/orders/{orderId}/ship
{
  "trackingNo": "700123456789",
  "shippingCompany": "新竹物流",
  "remark": "已裝箱，明日寄出"
}
```

---

## 訂單取消規則

```java
// 只有 PENDING 和 PREPARING 狀態可取消
if (!order.getStatus().equals("PENDING") && !order.getStatus().equals("PREPARING")) {
    throw new BusinessException("訂單已出貨，無法取消");
}

// 取消時需回寫 PrizeBox 狀態
for (OrderItem item : items) {
    PrizeBox box = prizeBoxMapper.selectByPrimaryKey(item.getPrizeBoxId());
    box.setStatus("IN_BOX");  // 回到賞品盒
    box.setOrderId(null);
    prizeBoxMapper.updateByPrimaryKey(box);
}
```

---

## 訂單查詢範例

```java
// 後台查詢（依店家過濾）
OrderCondition condition = new OrderCondition();
condition.setStoreId(SecurityUtils.getCurrentUserPrimaryStoreId()); // 非Admin自動帶入
condition.setStatus("PENDING");
condition.setCreatedAtStart(LocalDate.now().minusDays(30));

QueryReq<OrderCondition> req = new QueryReq<>();
req.setCondition(condition);
req.setSortBy("created_at");
req.setSortOrder("DESC");

List<OrderRes> orders = orderService.getOrders(req);
```

---

## OrderStatusLog 記錄規範

每次狀態變更必須記錄：

```java
// recordStatusLog(orderId, fromStatus, toStatus, operatorId, operatorType, remark)
recordStatusLog(order.getId(), "PENDING", "PREPARING", adminUserId, "ADMIN", "開始處理");
```

---

## PrizeBox 狀態對照

| PrizeBox 狀態 | 說明 | 對應訂單狀態 |
|-------------|------|------------|
| `IN_BOX` | 在賞品盒，可申請出貨 | - |
| `SHIPPING` | 申請出貨中 | PENDING / PREPARING / SHIPPED |
| `DELIVERED` | 已送達 | COMPLETED |
| `RECYCLED` | 已回收換紅利 | - |

---

## ⚠️ 禁止操作

- ❌ 不要跳過 PrizeBox 所有權驗證（userId 對比）
- ❌ 不要讓 status 非 `IN_BOX` 的 PrizeBox 建立訂單
- ❌ 不要在不同店家的 PrizeBox 建立同一筆訂單（必須按店家分組）
- ❌ 狀態流轉不要跳 step（例如直接從 PENDING 到 COMPLETED）
- ❌ 出貨後（SHIPPED / COMPLETED）不要允許取消
- ❌ 忘記更新 PrizeBox 狀態時，會造成賞品盒與訂單狀態不一致
