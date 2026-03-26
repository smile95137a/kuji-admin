# 研究報告：付款與點數系統 (Payment & Points System)

**功能**：`006-payment-points`  
**階段**：0 — 研究  
**日期**：2026-03-22

---

## R-001 台灣金流業者選項

### 決策
透過**抽象化的 `PaymentGatewayClient` 介面**進行整合，將具體的金流業者隱藏在單一 `charge()` / `verifyCallback()` 合約後方。具體實作在業務團隊於 Sprint 開始前確認業者（TapPay、ECPay 或 NewebPay）後選定 — 錢包或儲值業務邏輯無需任何程式碼變更。

### 理由
規格明確指出金流業者在實作前確認。透過介面包裝，我們可以立即使用 Stub 建構完整流程，並在不影響服務邏輯的情況下，僅修改單一檔案即可換入真實的金流業者 SDK。

### 考慮過的替代方案

| 金流業者 | 優點 | 缺點 | 決策 |
|---------|------|------|---------|
| **TapPay** | 支援 Mastercard + JCB；開發者友善的 REST API；沙盒環境即開即用 | 月費 | ✅ 如預算允許，推薦使用 |
| **ECPay（綠界）** | 台灣採用率最高；webhook 文件完整；支援 Mastercard | SDK 較不現代 | ✅ 可行的備用方案 |
| **NewebPay（藍新）** | 具競爭力的定價；支援 Mastercard | API 風格較舊；簽章/驗證較複雜 | ⚠️ 第三優先 |

### 整合模式
三家業者均採用相同的 webhook 推送模型：
1. 前端呼叫 `POST /api/wallet/recharge` → 後端建立 `RechargeOrder`（status=PENDING）並回傳付款 URL。
2. 玩家在金流業者頁面完成付款。
3. 金流業者將回呼 POST 至 `POST /api/wallet/recharge/callback`（伺服器對伺服器）。
4. 後端驗證簽章 → 原子性入帳錢包 → 更新 `RechargeOrder`（status=SUCCESS）。

**冪等鍵**：`RechargeOrder.id` 為送出給業者的商戶訂單 ID；已是 SUCCESS 狀態的訂單收到回呼時直接靜默略過（不重複入帳）。

---

## R-002 原子性錢包交易模式

### 決策
使用**MySQL 樂觀鎖**（透過現有的 `User.version` 欄位），搭配 `@Transactional` 服務邊界。v1.0 規模（約 100 個並發抽獎）下不需要分散式鎖或 Redis。

### 理由

`User` 實體已帶有 `version INTEGER` 欄位，此為標準的 Spring / MyBatis 樂觀並發模式。`UPDATE users SET gold_coins = ?, bonus_coins = ?, version = version+1 WHERE id = ? AND version = ?` 語句在並發更新搶先的情況下會回傳 0 筆，觸發重試。

### 實作模式

```java
// WalletServiceImpl.deductCoins(userId, amount)
@Transactional
public void deductCoins(String userId, long amount, String orderId, String reason) {
    for (int attempt = 0; attempt < 3; attempt++) {
        User user = userMapper.selectByIdForUpdate(userId); // SELECT FOR UPDATE as fallback
        long remaining = amount;
        long newGold  = user.getGoldCoins();
        long newBonus = user.getBonusCoins();

        if (newGold >= remaining) {
            newGold -= remaining;
            remaining = 0;
        } else {
            remaining -= newGold;
            newGold = 0;
        }
        if (remaining > 0 && newBonus >= remaining) {
            newBonus -= remaining;
            remaining = 0;
        }
        if (remaining > 0) throw new InsufficientBalanceException();

        int rows = userMapper.updateBalanceWithVersion(userId, newGold, newBonus, user.getVersion());
        if (rows == 1) {
            walletTransactionMapper.insert(buildTx(userId, -amount, newGold, newBonus, reason));
            return;
        }
        // optimistic lock failed → retry
    }
    throw new ConcurrentModificationException("Wallet update failed after retries");
}
```

### 考慮過的替代方案

| 方案 | 結論 | 棄用原因 |
|----------|---------|----------------|
| `SELECT FOR UPDATE`（悲觀鎖） | ✅ 可接受的備用方案 | 鎖競爭較高；若不希望使用樂觀重試迴圈可採用 |
| Redis 分散式鎖 | ❌ 過度設計 | 增加 Redis 相依；約 100 個並發抽獎不需要 |
| 資料庫可序列化隔離等級 | ❌ 範圍過廣 | 鎖住整個交易範圍；對不相關的讀取有效能損耗 |
| 獨立的 `wallet` 表 | ⚠️ 未來選項 | 清楚分離但需要結構描述遷移；`User.goldCoins` 已在線上環境 |

---

## R-003 防止雙重消費

### 決策
三層防禦：

1. **應用層** — 進入 `@Transactional` 區塊前先執行 `hasEnoughGold()` 預先檢查（快速失敗，回傳 HTTP 422 給客戶端）。
2. **資料庫層** — 樂觀鎖版本號檢查（防止並發競爭勝出）。
3. **DB 約束層** — `CHECK (gold_coins >= 0 AND bonus_coins >= 0)` 套用於 `users` 表（捕捉任何繞過情況）。

### 理由
縱深防禦。應用層檢查避免對明顯餘額不足的請求進行不必要的 DB 往返。樂觀鎖是權威性防護。DB 約束是最後一道安全防線。

### 模式：金流回呼冪等性
`recharge_order` 表在 `merchant_order_id` 上建立 `UNIQUE` 約束，儲存金流業者訂單 ID。同一筆訂單的重複回呼會在 DB 插入/更新時被捕捉，並回傳 HTTP 200 給業者（以防止重試風暴），而不會再次入帳錢包。

```sql
-- State machine: only PENDING → SUCCESS transition credits wallet
UPDATE recharge_order
SET status = 'SUCCESS', paid_at = NOW()
WHERE id = ? AND status = 'PENDING';
-- rowsAffected == 0 → already processed → idempotent skip
```

---

## R-004 金幣優先扣款順序

### 決策
在 `WalletServiceImpl.deductCoins()` 中為 v1.0 硬編碼金幣優先順序。**不**將其開放為可配置旗標。

### 理由
FR-005 和 SC-004 要求 100% 正確執行金幣優先。允許配置會引入設定錯誤的風險。規格明確將此標示為 v1.0 固定規則。若 v2.0 需要玩家自選優先順序，屆時再提取為 `DeductionStrategy` 介面。

---

## R-005 獎品回收安全性

### 決策
回收交易對 `prize_box` 列使用 `SELECT FOR UPDATE`，防止對同一物品同時執行回收和出貨。狀態在入帳紅利點數前即以原子方式設定為 `RECYCLED`。

### 模式

```
BEGIN TRANSACTION
  SELECT * FROM prize_box WHERE id = ? AND user_id = ? FOR UPDATE
  IF status != 'AVAILABLE' → throw PrizeNotRecyclableException
  UPDATE prize_box SET status = 'RECYCLED', recycled_at = NOW() WHERE id = ?
  UPDATE users SET bonus_coins = bonus_coins + ?, version = version + 1 WHERE id = ? AND version = ?
  INSERT INTO wallet_transaction (type='RECYCLE', ...)
COMMIT
```

---

## R-006 RechargeOrder 狀態機

### 決策
每次付款嘗試持久化一筆 `recharge_order` 記錄，以支援：
- 冪等回呼（防止雙重入帳）
- 管理員稽核付款嘗試記錄
- 未來退款支援（即使 v1.0 無此功能）

### 狀態

```
PENDING → SUCCESS   (gateway callback, verified signature)
PENDING → FAILED    (gateway callback, payment declined)
PENDING → EXPIRED   (scheduled job after 30 min TTL)
```

---

## R-007 交易稽核日誌設計

### 決策
擴充 `WalletTransaction`，新增欄位：`transactionType`（enum）、`goldDelta`、`bonusDelta`、`goldAfter`、`bonusAfter`、`referenceId`（orderId / prizeBoxId）、`reason`（管理員調整的自由文字）。

### 理由
SC-001 要求即時可視性；FR-006 要求類型、金額及變動後餘額快照。同時記錄 `goldAfter` 和 `bonusAfter` 允許不掃描所有交易即可重建餘額 — 對稽核查詢至關重要。

---

## 決策摘要

| # | 領域 | 決策 |
|---|------|---------|
| R-001 | 金流業者 | 抽象化介面；推薦 TapPay；ECPay 備用 |
| R-002 | 並發控制 | `User.version` 樂觀鎖搭配 3 次重試迴圈 |
| R-003 | 防止雙重消費 | 三層：預先檢查 + 樂觀鎖 + DB 約束 |
| R-004 | 扣款順序 | 金幣優先硬編碼；無配置旗標 |
| R-005 | 回收安全性 | 對 prize_box 列使用 `SELECT FOR UPDATE` |
| R-006 | 儲值狀態 | 含冪等鍵的 `RechargeOrder` 狀態機 |
| R-007 | 稽核日誌 | 每筆交易記錄雙幣別差值 + 變動後餘額快照 |
