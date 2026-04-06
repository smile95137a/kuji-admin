# 錢包與儲值流程 Skill

## 適用情境
- 修改金幣 / 紅利規則
- 新增儲值方案功能
- 修改點數扣除邏輯
- 調整 WalletTransaction 記錄格式
- 了解金幣與紅利的資料來源與差異

---

## 核心架構：金幣存在 user 表

> ⚠️ 重要：金幣 / 紅利直接存在 `user` 表，**不存在獨立的 wallet 表**。

```java
// user 表關鍵欄位
user.getGoldCoins()     // 金幣（儲值獲得，1:1 現金換算）
user.getBonusCoins()    // 紅利（儲值贈送或回收換算）
user.getTotalRecharged() // 累計儲值金額
user.getVersion()       // 樂觀鎖版本號
```

---

## 點數種類比較

| 種類 | 取得方式 | 扣除優先順序 | 用途 |
|------|---------|------------|------|
| **Gold（金幣）** | 儲值取得 | 優先扣 | 抽獎、出貨 |
| **Bonus（紅利）** | 儲值贈送、回收獎品 | 後扣 | 抽獎（部分限制） |

### 扣除規則實作

抽獎**只扣 Gold（金幣）**，金幣不足時提示儲值，**不**以 Bonus 自動補足。

```java
// WalletServiceImpl.java 中的扣除邏輯
long goldCoins = user.getGoldCoins() != null ? user.getGoldCoins() : 0L;

// ✅ 正確：金幣不足直接拒絕，明確告知缺口
if (goldCoins < totalCost) {
    throw new BusinessException(
        "金幣不足，請先儲值（需要 " + totalCost + " 金幣，目前剩餘 " + goldCoins + " 金幣）"
    );
}

// 原子性更新（樂觀鎖）
user.setGoldCoins(goldCoins - totalCost);
user.setVersion((user.getVersion() != null ? user.getVersion() : 0) + 1);
userMapper.updateByPrimaryKey(user);
```

---

## WalletTransaction 記錄規範

每次金幣變動都必須寫入 `wallet_transaction` 表：

```java
WalletTransaction tx = new WalletTransaction();
tx.setId(UUID.randomUUID().toString());
tx.setUserId(userId);
tx.setTransactionType(TransactionTypeEnum.DRAW.getCode()); // RECHARGE / DRAW / REFUND / RECYCLE
tx.setCoinType(CoinTypeEnum.GOLD.getCode());               // GOLD / BONUS
tx.setAmount(amount);          // 正數 = 增加，負數 = 扣除
tx.setBalanceAfter(newBalance);
tx.setRelatedId(relatedId);    // 關聯 ID（訂單ID / 抽獎ID / 儲值ID）
tx.setDescription(description);
tx.setCreatedAt(LocalDateTime.now());
walletTransactionMapper.insert(tx);
```

### TransactionTypeEnum 對應

| 枚舉值 | 說明 |
|--------|------|
| `RECHARGE` | 儲值增加金幣 |
| `DRAW` | 抽獎扣除點數 |
| `REFUND` | 退款返還 |
| `RECYCLE` | 回收獎品換紅利 |
| `ADMIN_ADJUST` | Admin 手動調整 |

---

## 儲值流程（RechargeService）

```
① 驗證 userId 存在（user 表）
② 驗證 RechargePlan 存在且有效
   → isActive=1
   → 未超出 startDate / endDate
   → deletedAt=null
③ @Transactional 開始
   → 建立 RechargeRecord（status=COMPLETED，立即完成）
   → 增加 user.goldCoins += plan.goldCoins
   → 增加 user.bonusCoins += plan.bonusCoins
   → 增加 user.totalRecharged += plan.amount
   → 寫入 WalletTransaction（type=RECHARGE）
④ 返回 RechargeRes
```

### 儲值方案欄位（recharge_plan 表）

```java
plan.getAmount()      // 實收金額（TWD）
plan.getGoldCoins()   // 獲得金幣數量
plan.getBonusCoins()  // 獲得紅利數量（贈送）
plan.getIsActive()    // 1=啟用，0=停用
plan.getStartDate()   // 活動開始時間（可null）
plan.getEndDate()     // 活動結束時間（可null）
```

---

## WalletService 方法速查

```java
// 取得錢包餘額
UserWalletRes wallet = walletService.getWallet(userId);
wallet.getGoldCoins();    // 金幣餘額
wallet.getBonusCoins();   // 紅利餘額
wallet.getTotalBalance(); // 總點數（gold + bonus）

// 扣除金幣（指定只扣 Gold）
walletService.deductGold(userId, amount, "DRAW", lotteryId, "抽獎扣費");

// 扣除紅利（指定只扣 Bonus）
walletService.deductBonus(userId, amount, "DRAW", lotteryId, "抽獎扣費");

// Admin 手動調整
walletService.adminAdjust(userId, goldDelta, bonusDelta, reason, adminId);
```

---

## 錢包初始化

新用戶註冊後，金幣欄位可能為 null，需要初始化：

```java
// WalletServiceImpl.createWallet()
if (user.getGoldCoins() == null) {
    user.setGoldCoins(0L);
    user.setBonusCoins(0L);
    user.setTotalRecharged(0L);
    user.setVersion(0);
    userMapper.updateByPrimaryKey(user);
}
// 查詢時也會自動初始化（getWallet 方法）
```

---

## Admin 後台調整錢包 API

```
POST /admin/wallet/{userId}/adjust
Authorization: Bearer {ADMIN_TOKEN}
Body: {
  "goldDelta": 100,      // 正數增加，負數減少
  "bonusDelta": 0,
  "reason": "客服補償"
}
```

---

## ⚠️ 禁止操作

- ❌ 不要直接修改 user 表的 `gold_coins` / `bonus_coins`（必須透過 WalletService）
- ❌ 不要忘記同時寫入 WalletTransaction（每次金幣異動都要有記錄）
- ❌ 不要用 Bonus 補足 Gold 的缺口（金幣不足 → 提示儲值，Bonus 是獨立的）
- ❌ 不要在沒有 `@Transactional` 的情況下執行扣點 + 寫記錄
- ❌ 修改儲值邏輯時不要跳過 RechargePlan 的有效期驗證
