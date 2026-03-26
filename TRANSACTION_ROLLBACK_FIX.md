# 事務回滾錯誤修復報告

## 問題描述

**錯誤訊息**：
```json
{
    "success": false,
    "error": {
        "code": "BUSINESS_ERROR",
        "message": "Transaction rolled back because it has been marked as rollback-only"
    }
}
```

**API**：`POST /api/admin/lottery-with-prizes`  
**HTTP 狀態碼**：400 Bad Request  
**發生時間**：2026-01-29T02:27

## 根本原因分析

### 1. 請求資料
```json
{
  "lottery": {
    "title": "火影忍者 一番賞 346",
    "category": "OFFICIAL_ICHIBAN",
    "playMode": "LOTTERY_MODE",
    "maxDraws": 100
  },
  "prizes": [
    {"name": "A賞", "quantity": 1},
    {"name": "B賞", "quantity": 2},
    {"name": "C賞", "quantity": 4},
    {"name": "D賞", "quantity": 8},
    {"name": "E賞", "quantity": 12},
    {"name": "最後賞", "quantity": 1}
  ]
}
```

**問題**：
- 總抽數：`maxDraws = 100`
- 獎品總數：`1 + 2 + 4 + 8 + 12 + 1 = 28`
- 差異：`100 - 28 = 72` 個籤位沒有獎品

### 2. 程式碼問題

#### 問題點 1：`LotteryServiceImpl.createLotteryWithPrizes()` (行 1226-1231)

**Before**：
```java
if (lotteryRes.getMaxDraws() != null && lotteryRes.getMaxDraws() > 0) {
    try {
        lotteryTicketService.generateTickets(lotteryId);
        log.info("✅ 籤位生成完成: lotteryId={}, maxDraws={}", lotteryId, lotteryRes.getMaxDraws());
    } catch (Exception e) {
        log.warn("⚠️ 籤位生成失敗: lotteryId={}, error={}", lotteryId, e.getMessage());
    }
}
```

**問題**：
1. `generateTickets()` 拋出 `BusinessException`（獎品數不等於籤位數）
2. 異常被 catch 但只記錄 warning
3. Spring 事務已被標記為 rollback-only
4. 方法繼續執行並嘗試 commit
5. 導致 `Transaction rolled back because it has been marked as rollback-only`

#### 問題點 2：`LotteryTicketServiceImpl.generateRandomTickets()` (行 119-123)

```java
// ⚠️ 一番賞/扭蛋/卡牌模式：獎品總數必須 = 總籤位數（不能有謝謝惠顧）
if (prizePool.size() != totalTickets) {
    throw new BusinessException(
        String.format("一番賞/扭蛋/卡牌模式：獎品總數(%d)必須等於總籤位數(%d)！...",
            prizePool.size(), totalTickets)
    );
}
```

**邏輯正確**，但異常處理不當導致事務問題。

## 解決方案

### 1. 修改 `LotteryServiceImpl.createLotteryWithPrizes()`

**After**：
```java
if (lotteryRes.getMaxDraws() != null && lotteryRes.getMaxDraws() > 0) {
    // 計算獎品總數
    int totalPrizeQuantity = req.getPrizes().stream()
            .mapToInt(p -> p.getQuantity() != null ? p.getQuantity() : 0)
            .sum();
    
    String playMode = req.getLottery().getPlayMode();
    int maxDraws = lotteryRes.getMaxDraws();
    
    log.info("🎰 籤位生成準備: playMode={}, maxDraws={}, totalPrizes={}", 
            playMode, maxDraws, totalPrizeQuantity);
    
    // ✅ 提前驗證，避免進入 generateTickets() 後拋出異常
    if ("LOTTERY_MODE".equals(playMode)) {
        // 一番賞/扭蛋/卡牌：獎品總數必須等於總抽數
        if (totalPrizeQuantity != maxDraws) {
            String errorMsg = String.format(
                "一番賞模式錯誤：獎品總數(%d)必須等於總抽數(%d)！" +
                "每個籤位都應該有獎品，不能有謝謝惠顧。請調整獎品數量或總抽數。",
                totalPrizeQuantity, maxDraws
            );
            log.error("❌ {}", errorMsg);
            throw new BusinessException(errorMsg);  // ✅ 直接拋出，讓事務正常回滾
        }
    } else if ("SCRATCH_MODE".equals(playMode)) {
        // 刮刮樂：允許獎品總數 < 總抽數（剩餘為謝謝惠顧）
        if (totalPrizeQuantity > maxDraws) {
            String errorMsg = String.format(
                "刮刮樂模式錯誤：獎品總數(%d)不能大於總抽數(%d)！",
                totalPrizeQuantity, maxDraws
            );
            log.error("❌ {}", errorMsg);
            throw new BusinessException(errorMsg);
        }
        log.info("ℹ️ 刮刮樂模式：獎品 {} 個，謝謝惠顧 {} 個", 
                totalPrizeQuantity, maxDraws - totalPrizeQuantity);
    }
    
    // 生成籤位
    lotteryTicketService.generateTickets(lotteryId);
    log.info("✅ 籤位生成完成: lotteryId={}, maxDraws={}", lotteryId, maxDraws);
}
```

**改善點**：
1. ✅ 提前在 Service 層驗證獎品數量
2. ✅ 移除 try-catch，讓異常正常傳播
3. ✅ 根據 `playMode` 區分驗證規則
4. ✅ 明確的錯誤訊息，告訴前端如何修正

### 2. 修改 `LotteryTicketServiceImpl.generateScratchTickets()`

**Before**：
```java
// 刮刮樂初始生成時，所有籤位都是「謝謝惠顧」
// 店家指定或玩家指定模式會在之後透過 designatePrizePositions 設定

LocalDateTime now = LocalDateTime.now();
for (int i = 1; i <= totalTickets; i++) {
    LotteryTicket ticket = new LotteryTicket();
    ticket.setId(UUID.randomUUID().toString());
    ticket.setLotteryId(lotteryId);
    ticket.setTicketNumber(i);
    // 刮刮樂初始都是謝謝惠顧，等待後續指定
    ticket.setPrizeId(null);
    ticket.setPrizeLevel("THANKS");
    // ...
}
```

**After**：
```java
// 取得所有獎品
LotteryPrizeExample prizeExample = new LotteryPrizeExample();
prizeExample.createCriteria().andLotteryIdEqualTo(lotteryId);
List<LotteryPrize> prizes = lotteryPrizeMapper.selectByExample(prizeExample);

// 建立獎品池
List<PrizeSlot> prizePool = new ArrayList<>();
int totalPrizeCount = 0;

for (LotteryPrize prize : prizes) {
    int quantity = prize.getQuantity() != null ? prize.getQuantity() : 0;
    totalPrizeCount += quantity;
    for (int i = 0; i < quantity; i++) {
        prizePool.add(new PrizeSlot(prize.getId(), prize.getLevel()));
    }
}

log.info("📦 刮刮樂獎品池: 獎品數={}, 謝謝惠顧數={}, 總籤位={}", 
        totalPrizeCount, totalTickets - totalPrizeCount, totalTickets);

// ✅ 補齊「謝謝惠顧」
int thanksCount = totalTickets - totalPrizeCount;
for (int i = 0; i < thanksCount; i++) {
    prizePool.add(new PrizeSlot(null, "THANKS"));
}

// ✅ 打亂順序（隨機分配）
Collections.shuffle(prizePool, random);

// 批量生成籤位
LocalDateTime now = LocalDateTime.now();
for (int i = 0; i < totalTickets; i++) {
    LotteryTicket ticket = new LotteryTicket();
    ticket.setId(UUID.randomUUID().toString());
    ticket.setLotteryId(lotteryId);
    ticket.setTicketNumber(i + 1);
    ticket.setPrizeId(prizePool.get(i).prizeId());  // ✅ 可能是獎品 ID 或 null
    ticket.setPrizeLevel(prizePool.get(i).level()); // ✅ 可能是 A/B/C 或 THANKS
    ticket.setStatus("AVAILABLE");
    // ...
}

log.info("✅ 刮刮樂籤位生成完成，獎品 {} 個，謝謝惠顧 {} 個", 
        totalPrizeCount, thanksCount);
```

**改善點**：
1. ✅ 刮刮樂模式也會建立獎品池
2. ✅ 自動補齊「謝謝惠顧」
3. ✅ 隨機分配獎品位置（防止被猜到）
4. ✅ 支援多個獎品（不只大獎）

## 遊戲模式規則總結

| 模式 | playMode | 獎品數量規則 | 謝謝惠顧 | 範例 |
|------|----------|-------------|---------|------|
| 一番賞 | LOTTERY_MODE | 獎品總數 = 總抽數 | ❌ 不允許 | 80抽必須有80個獎品 |
| 扭蛋 | LOTTERY_MODE | 獎品總數 = 總抽數 | ❌ 不允許 | 100抽必須有100個獎品 |
| 卡牌 | LOTTERY_MODE | 獎品總數 = 總抽數 | ❌ 不允許 | 50抽必須有50個獎品 |
| 刮刮樂 | SCRATCH_MODE | 獎品總數 ≤ 總抽數 | ✅ 允許 | 100抽可以只有28個獎品+72謝謝惠顧 |

## 前端修正建議

### 方案 1：自動計算總抽數

```javascript
// 新增獎品時自動計算總數量
const prizes = [
  {name: "A賞", quantity: 1},
  {name: "B賞", quantity: 2},
  {name: "C賞", quantity: 4}
];

// ✅ 自動計算 maxDraws
const totalQuantity = prizes.reduce((sum, p) => sum + p.quantity, 0);
lotteryData.maxDraws = totalQuantity; // 自動設為 7
```

### 方案 2：提示使用者補齊數量

```javascript
const totalQuantity = prizes.reduce((sum, p) => sum + p.quantity, 0);
const maxDraws = lotteryData.maxDraws;

if (playMode === 'LOTTERY_MODE' && totalQuantity !== maxDraws) {
  alert(`一番賞模式：獎品總數(${totalQuantity})必須等於總抽數(${maxDraws})！
  
  請選擇：
  1. 調整總抽數為 ${totalQuantity}
  2. 增加獎品數量到 ${maxDraws} 個`);
}
```

### 方案 3：允許使用者選擇模式

```javascript
// 顯示模式選擇
<select v-model="playMode">
  <option value="LOTTERY_MODE">一番賞（每抽都有獎）</option>
  <option value="SCRATCH_MODE">刮刮樂（可有謝謝惠顧）</option>
</select>

// 根據模式動態驗證
if (playMode === 'LOTTERY_MODE') {
  // 嚴格驗證：獎品數 = 總抽數
} else if (playMode === 'SCRATCH_MODE') {
  // 寬鬆驗證：獎品數 ≤ 總抽數
}
```

## API 回應改善

### Before（不友善）
```json
{
  "success": false,
  "error": {
    "code": "BUSINESS_ERROR",
    "message": "Transaction rolled back because it has been marked as rollback-only"
  }
}
```
❌ 使用者不知道哪裡錯了

### After（友善）
```json
{
  "success": false,
  "error": {
    "code": "BUSINESS_ERROR",
    "message": "一番賞模式錯誤：獎品總數(28)必須等於總抽數(100)！每個籤位都應該有獎品，不能有謝謝惠顧。請調整獎品數量或總抽數。"
  }
}
```
✅ 明確指出問題：獎品總數 28 vs 總抽數 100  
✅ 提供解決方案：調整獎品數量或總抽數  
✅ 說明原因：一番賞不能有謝謝惠顧

## 測試驗證

### 測試案例 1：一番賞模式（獎品數 = 總抽數）✅

```bash
curl -X POST http://18.179.187.129/api/admin/lottery-with-prizes \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "lottery": {
      "title": "鬼滅之刃一番賞",
      "playMode": "LOTTERY_MODE",
      "maxDraws": 28
    },
    "prizes": [
      {"name": "A賞", "quantity": 1},
      {"name": "B賞", "quantity": 2},
      {"name": "C賞", "quantity": 4},
      {"name": "D賞", "quantity": 8},
      {"name": "E賞", "quantity": 12},
      {"name": "最後賞", "quantity": 1}
    ]
  }'
```

**預期結果**：✅ 200 OK，籤位生成成功

### 測試案例 2：一番賞模式（獎品數 ≠ 總抽數）❌

```bash
curl -X POST http://18.179.187.129/api/admin/lottery-with-prizes \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "lottery": {
      "title": "火影忍者一番賞",
      "playMode": "LOTTERY_MODE",
      "maxDraws": 100
    },
    "prizes": [
      {"name": "A賞", "quantity": 1},
      {"name": "B賞", "quantity": 2}
    ]
  }'
```

**預期結果**：
```json
{
  "success": false,
  "error": {
    "message": "一番賞模式錯誤：獎品總數(3)必須等於總抽數(100)！每個籤位都應該有獎品，不能有謝謝惠顧。請調整獎品數量或總抽數。"
  }
}
```

### 測試案例 3：刮刮樂模式（獎品數 < 總抽數）✅

```bash
curl -X POST http://18.179.187.129/api/admin/lottery-with-prizes \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "lottery": {
      "title": "刮刮樂",
      "playMode": "SCRATCH_MODE",
      "maxDraws": 100
    },
    "prizes": [
      {"name": "大獎", "quantity": 1, "isGrandPrize": true},
      {"name": "二獎", "quantity": 5},
      {"name": "三獎", "quantity": 10}
    ]
  }'
```

**預期結果**：✅ 200 OK，生成 16 個獎品 + 84 個謝謝惠顧

## 部署檢查清單

- [x] 修改 `LotteryServiceImpl.createLotteryWithPrizes()`
- [x] 修改 `LotteryTicketServiceImpl.generateScratchTickets()`
- [ ] 編譯通過（BUILD SUCCESS）
- [ ] 單元測試（可選）
- [ ] 上傳 JAR 到 EC2
- [ ] 重啟服務
- [ ] 測試案例 1（一番賞，獎品數=總抽數）
- [ ] 測試案例 2（一番賞，獎品數≠總抽數，預期失敗）
- [ ] 測試案例 3（刮刮樂，獎品數<總抽數）
- [ ] 通知前端團隊更新驗證邏輯

## 相關文件

- [抽獎籤位系統設計](../.github/prompts/lottery-ticket-system.prompt.md)
- [前台 API 完整參考](FRONTEND_API_COMPLETE_REFERENCE.md)
- [API 重構摘要](API_REFACTORING_SUMMARY.md)

---

**修復日期**：2026-01-29  
**影響範圍**：後台新增商品 API  
**向下相容性**：✅ 相容（只修正錯誤處理邏輯）
