# 抽獎流程 Skill（全模式）

## 適用情境
- 新增抽獎模式或修改抽獎邏輯
- 了解三種模式的差異與路由分工
- 修改 DrawService / LotteryTicketService / 刮刮樂服務
- 調整點數扣除或庫存邏輯

---

## 三種抽獎模式速覽

| 模式 | category | playMode | 抽獎 API | Service | 選號方式 |
|------|----------|----------|----------|---------|---------|
| **一番賞** | `OFFICIAL_ICHIBAN` | `LOTTERY_MODE` | `POST /api/lottery/draw/{id}/draw` | `LotteryTicketService` | 玩家選籤號 |
| **扭蛋** | `GACHA` | `LOTTERY_MODE` | `POST /api/lottery/random/{id}/draw?count=N` | `DrawService` | 後端加權隨機 |
| **刮刮樂** | `SCRATCH_CARD` | `SCRATCH_STORE` / `SCRATCH_PLAYER` | `POST /api/lottery/draw/{id}/draw` | `LotteryTicketService` | 玩家選號碼 |

---

## 模式一：扭蛋（GACHA）加權隨機

### 加權隨機演算法

```java
// DrawServiceImpl.java
private LotteryPrize weightedRandomSelect(List<LotteryPrize> prizes) {
    int totalWeight = prizes.stream()
            .mapToInt(p -> p.getWeight() != null ? p.getWeight() : 100)
            .sum();

    int randomValue = random.nextInt(totalWeight);

    int accumulated = 0;
    for (LotteryPrize prize : prizes) {
        accumulated += (prize.getWeight() != null ? prize.getWeight() : 100);
        if (randomValue < accumulated) {
            return prize;
        }
    }
    return prizes.get(prizes.size() - 1);
}
```

### 權重設定範例

| 獎品 | weight | 實際機率 |
|------|--------|---------|
| SSR 公仔 | 5 | 5/250 = 2% |
| SR 立牌 | 15 | 15/250 = 6% |
| R 資料夾 | 80 | 80/250 = 32% |
| 徽章 | 150 | 150/250 = 60% |

### 扭蛋抽獎完整流程

```
① 驗證商品狀態（ON_SHELF）
② 查詢可抽獎品（remaining > 0）
③ 計算費用（pricePerDraw × count）
④ 驗證錢包餘額（gold + bonus >= cost）
⑤ @Transactional 開始
    → 驗證 Gold 是否足夠（不足則提示儲值，不使用 Bonus 補足）
    → 扣除 Gold
    → 執行加權隨機選獎
    → 扣除獎品庫存（remaining - 1）
    → 新增至 PrizeBox（status=IN_BOX）
    → 記錄 WalletTransaction
⑥ 返回抽獎結果
```

---

## 模式二：一番賞（OFFICIAL_ICHIBAN）籤位制

### 核心概念
- 每個獎品有固定籤號（ticketNumber 1～N）
- 玩家選號 → 後端驗證該票未被抽取 → 扣點 → 回傳獎品
- 票數抽完即結束（不可重複抽）

### 抽獎保護鎖機制
- 玩家第一次抽時，商品鎖定 5 分鐘，其他玩家無法同時抽
- 使用 `LotteryLockService` + `LockCleanupScheduler` 管理
- Lock 相關 API：`POST /api/lottery/lock/{lotteryId}`

### 籤位流程

```
① 玩家查詢可用籤位列表
② 玩家選擇 ticketNumber
③ 後端鎖定籤位（行鎖或樂觀鎖）
④ @Transactional
    → 驗證籤位狀態（AVAILABLE）
    → 扣除點數
    → 更新籤位狀態（DRAWN）
    → 新增至 PrizeBox
    → 記錄 WalletTransaction
⑤ 返回結果
```

---

## 模式三：刮刮樂（SCRATCH_CARD）雙號碼機制

### 雙號碼規則（關鍵！）

| 欄位 | 說明 | 前端可見 |
|------|------|---------|
| `ticketNumber` | 物理序號（1～N），玩家選的格子 | ✅ AVAILABLE 時可見 |
| `revealedNumber` | 刮開後顯示的亂數，用於大獎指定 | ❌ AVAILABLE 時隱藏 |

> ⚠️ 兩者不可混用。前端指定大獎傳的是 `revealedNumber`，不是 `ticketNumber`。

### SCRATCH_STORE（店家指定大獎）

```
① Admin/StoreOwner 建立刮刮樂商品
② 後端呼叫 generateScratchTickets()
    → 生成全部票（ticketNumber + revealedNumber）
    → 指定大獎 revealedNumber（isDesignatedPrize=1）
③ 呼叫 autoAssignNonGrandPrizes()
    → 對所有 isDesignatedPrize=0 的票位隨機分配非大獎
④ 玩家直接選 ticketNumber 抽獎
```

### SCRATCH_PLAYER（玩家指定大獎）

```
① 玩家開新套（第一個玩）→ 成為「開套者」
② 後端呼叫 checkDesignationRequired()
    → 若 gameMode=SCRATCH_PLAYER 且未完成指定，攔截並返回：
      { designationRequired: true, availableNumbers: [...], grandPrizes: [...] }
③ 開套者呼叫 /designate，提交選定的 revealedNumber 作為大獎位置
④ 後端呼叫 autoAssignNonGrandPrizes()
⑤ 所有玩家可正常抽籤
```

### checkDesignationRequired() 回傳格式（SCRATCH_PLAYER 攔截時）

```json
{
  "designationRequired": true,
  "availableNumbers": [101, 102, 103, ...],
  "grandPrizes": [
    {
      "prizeId": "uuid",
      "prizeName": "SSR 超級大獎",
      "prizeLevel": "A",
      "quantity": 2,
      "prizeImageUrl": "https://..."
    }
  ]
}
```

> 前端用 `grandPrizes[].quantity` 加總，決定要提交幾個指定位置。

### autoAssignNonGrandPrizes() 邏輯

```
對所有 isDesignatedPrize=0 且 status=AVAILABLE 的籤位
→ 隨機分配非大獎獎品（prizeId 指向非大獎）
→ 多餘籤位保持 prizeId=null（視同謝謝惠顧）
```

---

## 通用：點數扣除規則

抽獎**只扣 Gold（金幣）**，金幣不足時直接提示儲值，**不**以 Bonus 補足。

```java
// ✅ 正確：金幣不足直接拒絕，提示使用者儲值
long goldCoins = user.getGoldCoins() != null ? user.getGoldCoins() : 0L;
if (goldCoins < totalCost) {
    throw new BusinessException("金幣不足，請先儲值（需要 " + totalCost + " 金幣，目前剩餘 " + goldCoins + " 金幣）");
}

user.setGoldCoins(goldCoins - totalCost);

// ❌ 錯誤：不要讓 Bonus 自動補足金幣缺口
// long goldToDeduct = Math.min(totalCost, user.getGoldCoins());
// long bonusToDeduct = totalCost - goldToDeduct; ← 禁止這樣做
```

---

## 通用：PrizeBox 狀態

| 狀態 | 說明 |
|------|------|
| `IN_BOX` | 抽到，存放於賞品盒 |
| `SHIPPING` | 已申請出貨 |
| `RECYCLED` | 已回收換紅利 |

---

## ⚠️ 禁止操作

- ❌ 不要在 `getAvailableRevealedNumbers()` 返回 `ticketNumber`
- ❌ 不要對 `isDesignatedPrize=1` 的票執行 `autoAssignNonGrandPrizes`
- ❌ 不要在 `designatePrizePositions` 後忘記呼叫 `autoAssignNonGrandPrizes`
- ❌ 不要讓前端傳 `ticketNumber` 給 `/designate`（應傳 `revealedNumber`）
- ❌ 不要把 `designationRequired` 邏輯套用到 `SCRATCH_STORE`
- ❌ 不要在未使用 `@Transactional` 的情況下執行扣點 + 扣庫存組合操作
