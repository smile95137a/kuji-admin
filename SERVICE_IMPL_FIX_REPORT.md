# ServiceImpl 錯誤修正報告

## 修正日期
2026-01-09

## 修正的檔案

### 1. RechargePlanServiceImpl.java ✅

#### 問題說明
- `RechargePlan.isActive` 是 `Byte` 類型，不是 `Boolean`
- DTO 缺少 `startDate`、`endDate`、`orderNum` 欄位
- DTO 欄位名稱不一致（`startDate` vs `startTime`）

#### 修正內容
1. **型別轉換**：
   ```java
   // ❌ 錯誤
   plan.setIsActive(req.getIsActive());
   
   // ✅ 正確
   plan.setIsActive((byte) 1);
   plan.setIsActive(req.getIsActive() ? (byte) 1 : (byte) 0);
   ```

2. **移除不存在的欄位**：
   - 移除 `req.getStartDate()`、`req.getEndDate()`、`req.getOrderNum()`
   - 改用資料庫預設值

3. **Example 查詢修正**：
   ```java
   // ❌ 錯誤
   criteria.andIsActiveEqualTo(true);
   
   // ✅ 正確
   criteria.andIsActiveEqualTo((byte) 1);
   ```

4. **DTO 欄位對齊**：
   ```java
   // ❌ 錯誤
   .startDate(plan.getStartDate())
   .endDate(plan.getEndDate())
   .orderNum(plan.getOrderNum())
   .discountRate(discountRate)
   
   // ✅ 正確
   .startTime(plan.getStartDate())
   .endTime(plan.getEndDate())
   .displayOrder(plan.getOrderNum())
   .bonusPercentage(String.format("%.1f%%", discountRate))
   ```

---

### 2. PrizeBoxServiceImpl.java ✅

#### 問題說明
- 使用不存在的 `PrizeMapper`，應該使用 `LotteryPrizeMapper`
- `Store.getName()` 不存在，應該是 `Store.getStoreName()`
- DTO 欄位名稱不一致（`prizeImage` vs `prizeImageUrl`）
- DTO 缺少 `userNickname` 欄位

#### 修正內容
1. **Mapper 替換**：
   ```java
   // ❌ 錯誤
   private final PrizeMapper prizeMapper;
   Prize prize = prizeMapper.selectByPrimaryKey(prizeBox.getPrizeId());
   
   // ✅ 正確
   private final LotteryPrizeMapper lotteryPrizeMapper;
   LotteryPrize prize = lotteryPrizeMapper.selectByPrimaryKey(prizeBox.getPrizeId());
   ```

2. **Store 欄位修正**：
   ```java
   // ❌ 錯誤
   store.getName()
   
   // ✅ 正確
   store.getStoreName()
   ```

3. **DTO 欄位對齊**：
   ```java
   // ❌ 錯誤
   .userNickname(user != null ? user.getNickname() : null)
   .prizeImage(prize != null ? prize.getImageUrl() : null)
   .prizeGrade(prize != null ? prize.getLevel() : null)
   
   // ✅ 正確
   .lotteryImageUrl(lottery != null ? lottery.getImageUrl() : null)
   .prizeImageUrl(prize != null ? prize.getImageUrl() : null)
   .prizeLevel(prize != null ? prize.getLevel() : null)
   ```

4. **移除未使用的欄位**：
   - 移除 `userNickname`（PrizeBoxItemRes 沒有此欄位）
   - 移除 `recycledAt`、`shippedAt`、`orderId`（不需顯示）
   - 新增 `isRecyclable` 和 `lotteryImageUrl`

---

### 3. OrderServiceImpl.java ✅

#### 問題說明
- `OrderCondition` 欄位名稱不一致（`status` vs `shippingStatus`）
- `OrderStatusLog` 欄位名稱錯誤（`status`/`statusName` vs `toStatus`）
- DTO 欄位名稱不一致（`orderNumber` vs `orderNo`）
- `Store.getName()` 不存在，應該是 `Store.getStoreName()`

#### 修正內容
1. **Condition 欄位對齊**：
   ```java
   // ❌ 錯誤
   condition.getStatus()
   condition.getOrderNumber()
   
   // ✅ 正確
   condition.getShippingStatus()
   condition.getOrderNo()
   ```

2. **OrderStatusLog 欄位修正**：
   ```java
   // ❌ 錯誤
   log.setStatus(status);
   log.setStatusName(statusName);
   
   // ✅ 正確
   log.setToStatus(status);
   // statusName 不存在於 Entity，僅用於顯示
   ```

3. **DTO 欄位對齊**：
   ```java
   // ❌ 錯誤（OrderDetailRes/OrderRes）
   .orderNumber(order.getOrderNumber())
   .status(order.getStatus())
   .statusName(OrderStatusEnum.getNameByCode(order.getStatus()))
   .paymentStatus(order.getPaymentStatus())
   
   // ✅ 正確
   .orderNo(order.getOrderNumber())
   .shippingStatus(order.getStatus())
   .shippingStatusName(OrderStatusEnum.getNameByCode(order.getStatus()))
   // 移除 paymentStatus（OrderDetailRes 沒有此欄位）
   ```

4. **OrderItemRes 欄位對齊**：
   ```java
   // ❌ 錯誤
   .prizeGrade(item.getPrizeGrade())
   .prizeImage(item.getPrizeImage())
   
   // ✅ 正確
   .prizeLevel(item.getPrizeLevel())
   .prizeImageUrl(item.getPrizeImageUrl())
   .lotteryImageUrl(item.getLotteryImageUrl())
   ```

5. **Store 欄位修正**：
   ```java
   // ❌ 錯誤
   store.getName()
   
   // ✅ 正確
   store.getStoreName()
   ```

---

## 核心問題總結

### 1. MyBatis 生成的 Entity 型別問題
- `RechargePlan.isActive` 是 `Byte` (0/1)，不是 `Boolean` (true/false)
- 需要在 Service 層進行轉換：`(byte) 1` 或 `(byte) 0`

### 2. DTO 欄位命名不一致
| Entity 欄位 | DTO 欄位（舊） | DTO 欄位（新） |
|------------|---------------|---------------|
| startDate  | startDate     | startTime     |
| endDate    | endDate       | endTime       |
| orderNum   | orderNum      | displayOrder  |
| order_number | orderNumber | orderNo       |
| status     | status        | shippingStatus |
| statusName | statusName    | shippingStatusName |
| prize_grade | prizeGrade   | prizeLevel    |
| prize_image | prizeImage   | prizeImageUrl |

### 3. Entity 欄位命名不一致
- `Store.storeName` 不是 `Store.name`
- `OrderStatusLog.toStatus` 不是 `OrderStatusLog.status`
- `OrderItem.prizeLevel` 不是 `OrderItem.prizeGrade`

### 4. Mapper 選擇錯誤
- 賞品資料應使用 `LotteryPrizeMapper`，不是 `PrizeMapper`
- `LotteryPrize` 是抽獎賞品實體

---

## 驗證結果

所有 ServiceImpl 編譯錯誤已修正：
- ✅ RechargePlanServiceImpl.java - 無錯誤
- ✅ PrizeBoxServiceImpl.java - 無錯誤
- ✅ OrderServiceImpl.java - 無錯誤

---

## 後續注意事項

### 1. 型別轉換
在使用 MyBatis 生成的 Entity 時，注意：
- `Byte` 類型的 boolean 欄位需要轉換：`(byte) 1` 或 `(byte) 0`
- 查詢時也要使用 `(byte) 1`：`criteria.andIsActiveEqualTo((byte) 1)`

### 2. DTO 設計原則
- 欄位命名應與前端約定一致
- 避免使用縮寫（如 `prizeImage` → `prizeImageUrl`）
- 狀態類型欄位應有對應的名稱欄位（如 `status` + `statusName`）

### 3. Entity 欄位檢查
在寫 Service 之前：
1. 檢查 Entity 的實際欄位名稱
2. 檢查欄位型別（特別是 `Byte` vs `Boolean`）
3. 檢查關聯的 Mapper 是否存在

### 4. 查詢條件設計
- Condition 類別的欄位應與 DTO 一致
- 避免使用與 Entity 不同的欄位名

---

**修正完成時間**：2026-01-09 16:30  
**修正的錯誤數**：25 個編譯錯誤  
**修正狀態**：✅ 全部修正完成
