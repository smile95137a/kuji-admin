# 快速入門： 獎品盒 (Prize Box) — Feature 010

**分支**：`010-prize-box`  
**技術棧**：Java 21 + Spring Boot 3.3.3 + MyBatis + MySQL 8.3  
**最後更新**：2026-03-22

---

## 概覽

本指南協助開發者快速了解並實作「獎品盒」功能的剩餘工作項目。現有骨架已提供核心流程，需針對以下差距進行補強：

| 工作 | 類型 | 優先級 |
|------|------|--------|
| 修正 `@NotBlank` import 缺失 | Bug fix | P1 |
| 修正 `isRecyclable` 硬編碼邏輯 | Bug fix | P1 |
| 術語正名（賞品盒→獎品盒） | Terminology | P1 |
| 新增 `GET /prize-box/history` 端點 | Feature | P1 |
| 新增 `isShippable` 欄位與驗證 | Feature | P1 |
| 新增 `prizeValue` 欄位 | Feature | P2 |
| 整合 `UserAddress`（userAddressId） | Feature | P3 |
| 補充單元測試 | Quality | P2 |

---

## 前置條件

- Java 21 + Maven 已安裝
- MySQL 8.3 本地或測試環境已運行
- Spring Boot 應用可正常啟動（`./mvnw spring-boot:run`）
- 具備抽獎功能（`DrawService` 呼叫 `PrizeBoxService.addToPrizeBox`）

---

## 步驟 1: 修正編譯錯誤

### 修正 `PrizeBoxShipReq.java`

```java
// 在 import 區塊補上：
import jakarta.validation.constraints.NotBlank;
```

**檔案**: `src/main/java/com/group/admin/req/prizebox/PrizeBoxShipReq.java`

---

## 步驟 2: 修正 `isRecyclable` 邏輯

**檔案**: `src/main/java/com/group/admin/service/impl/PrizeBoxServiceImpl.java`

找到 `convertToItemRes` 方法，將：
```java
.isRecyclable(true) // 在賞品盒中的都可以回收
```
改為：
```java
.isRecyclable(prizeBox.getRecycleBonus() != null && prizeBox.getRecycleBonus() > 0)
```

同時在 `recyclePrizes` 方法的驗證區塊加入：
```java
if (prizeBox.getRecycleBonus() == null || prizeBox.getRecycleBonus() <= 0) {
    throw new BusinessException("此獎品不可回收：" + prizeBoxId);
}
```

---

## 步驟 3: 確認/新增 `is_shippable` 欄位

### 3a. 確認 DB 欄位存在

```sql
-- 執行確認
SHOW COLUMNS FROM prize_box LIKE 'is_shippable';

-- 若不存在，執行：
ALTER TABLE prize_box
  ADD COLUMN is_shippable TINYINT NOT NULL DEFAULT 1
  COMMENT '是否可出貨：1=可, 0=不可'
  AFTER is_recyclable;
```

### 3b. 確認 `PrizeBox` entity

```java
// PrizeBox.java 確認有此欄位：
private Byte isShippable;
```

### 3c. 新增 `isShippable` 驗證到 `shipPrizes`

```java
// 在驗證區塊加入：
if (prizeBox.getIsShippable() != null && prizeBox.getIsShippable() == 0) {
    throw new BusinessException("此獎品不可出貨：" + id);
}
```

---

## 步驟 4: 更新 Response DTO

**檔案**: `src/main/java/com/group/admin/res/prizebox/PrizeBoxItemRes.java`

新增欄位：
```java
/**
 * 是否可出貨
 */
private Boolean isShippable;

/**
 * 獎品市值
 */
private Long prizeValue;

/**
 * 出貨時間（歷史記錄用）
 */
private LocalDateTime shippedAt;

/**
 * 回收時間（歷史記錄用）
 */
private LocalDateTime recycledAt;
```

更新 `convertToItemRes` 填入新欄位：
```java
.isShippable(prizeBox.getIsShippable() == null || prizeBox.getIsShippable() != 0)
.prizeValue(prize != null ? prize.getPrizeValue() : null)
.shippedAt(prizeBox.getShippedAt())
.recycledAt(prizeBox.getRecycledAt())
```

---

## 步驟 5: 新增 History 端點

### 5a. `PrizeBoxService` 新增方法

```java
/**
 * 查詢玩家獎品盒完整歷史
 */
List<PrizeBoxItemRes> getPrizeBoxHistory(String userId, String status);
```

### 5b. `PrizeBoxServiceImpl` 實作

```java
@Override
public List<PrizeBoxItemRes> getPrizeBoxHistory(String userId, String status) {
    PrizeBoxExample example = new PrizeBoxExample();
    PrizeBoxExample.Criteria criteria = example.createCriteria()
            .andUserIdEqualTo(userId);
    if (status != null && !status.isBlank()) {
        criteria.andStatusEqualTo(status);
    }
    example.setOrderByClause("created_at DESC");
    
    List<PrizeBox> prizeBoxes = prizeBoxMapper.selectByExample(example);
    return prizeBoxes.stream().map(this::convertToItemRes).collect(Collectors.toList());
}
```

### 5c. `PrizeBoxController` 新增端點

```java
/**
 * 查詢獎品盒歷史（含已出貨與已回收）
 */
@GetMapping("/history")
public ResponseEntity<List<PrizeBoxItemRes>> getHistory(
        @RequestParam(required = false) String status) {
    String userId = SecurityUtils.getCurrentUserId();
    log.info("🔍 [API] 查詢獎品盒歷史：userId={}, status={}", userId, status);
    List<PrizeBoxItemRes> history = prizeBoxService.getPrizeBoxHistory(userId, status);
    return ResponseEntity.ok(history);
}
```

---

## 步驟 6: 術語正名

### 6a. `PrizeBoxStatusEnum`

```java
// 修改 IN_BOX 的中文名稱：
IN_BOX("IN_BOX", "在獎品盒中"),  // 原: 在賞品盒中
```

### 6b. Controller/Service Javadoc

全域搜尋 `賞品盒`，替換為 `獎品盒`：
```
grep -r "賞品盒" src/main/java/com/group/admin/ --include="*.java"
```

---

## 步驟 7: 整合 UserAddress（P3）

**檔案**: `src/main/java/com/group/admin/req/prizebox/PrizeBoxShipReq.java`

新增欄位：
```java
/**
 * 已儲存地址 ID（選填，有值時優先使用）
 */
private String userAddressId;
```

**檔案**: `src/main/java/com/group/admin/service/impl/PrizeBoxServiceImpl.java`

在 `shipPrizes` 方法開頭加入：
```java
// 優先使用已儲存地址
if (req.getUserAddressId() != null && !req.getUserAddressId().isBlank()) {
    UserAddress savedAddress = userAddressMapper.selectByPrimaryKey(req.getUserAddressId());
    if (savedAddress != null && savedAddress.getUserId().equals(userId)) {
        req.setRecipientName(savedAddress.getRecipientName());
        req.setRecipientPhone(savedAddress.getRecipientPhone());
        req.setRecipientAddress(
            savedAddress.getCity() + savedAddress.getDistrict() + savedAddress.getAddress()
        );
    }
}
```

注入 `UserAddressMapper`：
```java
private final UserAddressMapper userAddressMapper;
```

---

## 步驟 8: 撰寫單元測試

**檔案**: `src/test/java/com/group/admin/service/PrizeBoxServiceTest.java`

關鍵測試案例：

```java
@Test
void recyclePrizes_shouldFailWhenNotRecyclable() {
    // Given: prize with recycleBonus = 0
    // When: recyclePrizes called
    // Then: BusinessException thrown with "此獎品不可回收"
}

@Test
void shipPrizes_shouldSplitByStore() {
    // Given: 2 prizes from different stores
    // When: shipPrizes called
    // Then: createOrdersFromPrizeBox called twice (once per store)
}

@Test
void shipPrizes_shouldFailWhenNotShippable() {
    // Given: prize with isShippable = 0
    // When: shipPrizes called
    // Then: BusinessException thrown
}

@Test
void getPrizeBox_shouldReturnOnlyInBoxItems() {
    // Given: user has IN_BOX + SHIPPED + RECYCLED prizes
    // When: getPrizeBox called
    // Then: only IN_BOX items returned
}

@Test
void getPrizeBoxHistory_shouldReturnAllStatuses() {
    // Given: user has all 3 status prizes
    // When: getPrizeBoxHistory called with null status
    // Then: all items returned
}
```

---

## API 快速參考

| 方法 | 路徑 | 驗證 | 說明 |
|--------|------|------|-------------|
| GET | `/api/prize-box` | JWT | 查詢當前獎品盒（IN_BOX，按店家分組） |
| GET | `/api/prize-box/summary` | JWT | 同上（替代端點，回傳格式相同） |
| POST | `/api/prize-box/ship` | JWT | 出貨選取的獎品，建立訂單 |
| POST | `/api/prize-box/recycle` | JWT | 回收選取的獎品，獲得 Bonus |
| GET | `/api/prize-box/history` | JWT | **[NEW]** 查詢完整歷史記錄 |

---

## 使用 cURL 測試

```bash
# 設定 JWT Token
TOKEN="eyJhbGciOiJIUzI1NiJ9..."

# 查詢獎品盒
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/prize-box

# 查詢歷史
curl -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/prize-box/history?status=SHIPPED"

# 出貨
curl -X POST \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "prizeBoxIds": ["prizebox-uuid-001"],
    "shippingMethod": "HOME_DELIVERY",
    "recipientName": "王小明",
    "recipientPhone": "0912345678",
    "recipientAddress": "台北市信義區信義路五段7號"
  }' \
  http://localhost:8080/api/prize-box/ship

# 回收
curl -X POST \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"prizeBoxIds": ["prizebox-uuid-002"]}' \
  http://localhost:8080/api/prize-box/recycle
```

---

## 疑難排解

| 問題 | 解決方法 |
|------|---------|
| 編譯錯誤 `NotBlank cannot be resolved` | 補上 `import jakarta.validation.constraints.NotBlank;` |
| 回收後 Bonus 未增加 | 確認 `WalletService.addBonus` 呼叫成功，查 `wallet_transaction` 表 |
| 所有獎品都顯示 `isRecyclable: true` | 修正 `PrizeBoxServiceImpl.convertToItemRes` 的硬編碼邏輯 |
| `GET /prize-box/history` 回傳 404 | 確認已新增 `@GetMapping("/history")` 端點並重啟服務 |
| 出貨時未自動拆單 | 確認 `shipPrizes` 按 `storeId` 分組邏輯正常 |
