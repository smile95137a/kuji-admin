# 任務清單：金幣系統重構

**輸入**：設計文件來自 `/specs/018-coin-system/`
**分支**：`018-coin-system` | **建立日期**：2026-04-13

---

## 第一階段：重命名 Service 與 DTO

**目的**：將所有 Wallet 命名替換為 Coin。

- [ ] T001 複製 `WalletService.java` → `CoinService.java`，更新介面名稱與 Javadoc
- [ ] T002 複製 `WalletServiceImpl.java` → `CoinServiceImpl.java`，更新類別名稱、@Service 名稱
- [ ] T003 [P] 重命名 `UserWalletRes.java` → `UserCoinRes.java`
- [ ] T004 [P] 重命名 `WalletTransactionRes.java` → `CoinTransactionRes.java`
- [ ] T005 [P] 重命名 `WalletAdjustReq.java` → `CoinAdjustReq.java`
- [ ] T006 [P] 重命名 `WalletTransactionCondition.java` → `CoinTransactionCondition.java`
- [ ] T007 用 `grep_search` 找出所有 `WalletService` / `WalletServiceImpl` 引用，逐一替換為 `CoinService` / `CoinServiceImpl`（預估 ~15 個檔案）
- [ ] T008 刪除原始的 `WalletService.java` 和 `WalletServiceImpl.java`

**檢查點**：編譯通過，`grep -r "WalletService" src/main/` 回傳 0

---

## 第二階段：Controller 調整

- [ ] T009 刪除 `src/main/java/com/group/admin/controller/api/WalletController.java`（前台錢包 API 移除）
- [ ] T010 重命名 `AdminWalletController.java` → `AdminCoinController.java`：
  - 類別名稱改為 `AdminCoinController`
  - `@RequestMapping` 路徑改為 `/admin/coin`
  - 注入改為 `CoinService`
  - 所有方法中的 Wallet 相關 DTO 改為 Coin 命名
- [ ] T011 更新 `SecurityConfig.java`：移除 `/api/wallet/**` 路由規則（若有）

**檢查點**：`/api/wallet/*` 回傳 404；`/admin/coin/*` 正常運作

---

## 第三階段：修復 RechargeServiceImpl

**目的**：消除直接操作 user 表的反模式。

- [ ] T012 修改 `RechargeServiceImpl.createRecharge()`（約 L100-L167）：
  - 移除直接的 `user.setGoldCoins()` / `user.setBonusCoins()` / `userMapper.updateByPrimaryKeySelective()`
  - 改為呼叫 `coinService.addGold(userId, plan.getGoldCoins(), "RECHARGE", recordId, "儲值金幣")`
  - 若 plan 有 bonusCoins > 0，呼叫 `coinService.addBonus(userId, plan.getBonusCoins(), "RECHARGE", recordId, "儲值紅利")`
  - `totalRecharged` 的更新仍保留直接操作 user 表（這是非金幣欄位）
  - 移除手動插入 `WalletTransaction` 的程式碼（CoinService 會自動記錄）
- [ ] T013 修改 `RechargeServiceImpl.confirmPayment()`：同樣改用 CoinService

**檢查點**：儲值流程正常，coin_transaction 表有 RECHARGE 記錄且由 CoinService 寫入

---

## 第四階段：統一抽獎扣款邏輯

**目的**：依商品 paymentType 決定扣金幣或紅利，不做混合扣款。

- [ ] T014 修改 `DrawServiceImpl.executeDraw()`（扭蛋路徑，約 L112-L140）：
  - 讀取 `lottery.getPaymentType()`（需 Spec 019 先加欄位，若未加則暫用 "GOLD" 預設值）
  - paymentType = GOLD → 只呼叫 `coinService.deductGold()`，不足拋 `INSUFFICIENT_GOLD`
  - paymentType = BONUS → 只呼叫 `coinService.deductBonus()`，不足拋 `INSUFFICIENT_BONUS`
  - 移除原本的 Gold+Bonus 混合扣款邏輯
- [ ] T015 修改 `LotteryTicketServiceImpl.drawByTicketNumber()` 和 `drawByTicketId()`（一番賞/刮刮樂路徑）：
  - 同樣讀取 paymentType，改用對應的 `coinService.deductGold()` 或 `coinService.deductBonus()`
  - 免單退款改用 `coinService.addGold()` 或 `coinService.addBonus()`（依原始 paymentType）

**檢查點**：抽獎扣款統一，不再混合扣款

---

## 第五階段：測試更新

- [ ] T016 [P] 更新 `WalletControllerTest.java` → 刪除（前台 API 已移除）
- [ ] T017 [P] 更新 `AdminWalletControllerTest.java` → `AdminCoinControllerTest.java`（改路徑 + DTO 名稱）
- [ ] T018 執行 `mvn clean package -DskipTests` 確認編譯通過

**檢查點**：全專案零 Wallet 引用，編譯通過

---

## 依賴關係

```
第一階段（重命名 Service）     — 無依賴
第二階段（Controller）        — 依賴第一階段
第三階段（修復 Recharge）      — 依賴第一階段
第四階段（統一扣款）           — 依賴第一階段 + Spec 019 的 paymentType 欄位（可先寫預設值）
第五階段（測試更新）           — 依賴全部完成
```
