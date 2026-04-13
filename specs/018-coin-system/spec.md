# 功能規格書：金幣系統重構

**功能分支**：`018-coin-system`
**建立日期**：2026-04-13
**狀態**：草稿
**輸入**：移除前台錢包 UI、WalletService 重命名為 CoinService、統一扣款邏輯、修復 RechargeServiceImpl 直接操作 user 表問題

## 使用者情境與測試

### 使用者故事 1 — 統一金幣/紅利扣款邏輯（優先級：P1）

身為系統架構師，我希望所有金幣/紅利的增減操作都經由統一的 `CoinService`，避免不同模組直接操作 user 表造成資料不一致。

**此優先級的原因**：目前 RechargeServiceImpl 繞過 WalletService 直接操作 user 表，DrawServiceImpl 自動混合扣金幣+紅利，這些不一致邏輯需要統一。

**獨立測試**：儲值後金幣增加且 coin_transaction 有記錄；抽獎扣金幣且 coin_transaction 有記錄；兩者都透過同一個 CoinService。

**驗收情境**：

1. **在** 玩家儲值 100 金幣的情況下，**當** 呼叫 RechargeService.createRecharge()，**則** 改為透過 `CoinService.addGold()` 操作，coin_transaction 有一筆 RECHARGE 記錄。
2. **在** 玩家抽獎（商品 paymentType=GOLD）的情況下，**當** 扣款，**則** 只扣金幣，不自動補紅利，金幣不足直接拒絕。
3. **在** 玩家抽獎（商品 paymentType=BONUS）的情況下，**當** 扣款，**則** 只扣紅利，紅利不足直接拒絕。

---

### 使用者故事 2 — 移除前台錢包 UI（優先級：P2）

身為產品負責人，我希望移除前台的「錢包」概念，金幣/紅利餘額直接顯示在個人資料中，不需要獨立的錢包頁面。

**此優先級的原因**：用戶明確表示金幣/紅利已在個人資料中，錢包頁面是多餘的。

**驗收情境**：

1. **在** 移除 WalletController 後，**當** 前端呼叫 `/api/wallet/*`，**則** 回傳 404。
2. **在** 後台管理員查看/調整用戶金幣的情況下，**當** 呼叫 AdminCoinController，**則** 功能正常。
3. **在** 其他模組需要查詢餘額的情況下，**當** 呼叫 `CoinService.hasEnoughGold()`，**則** 正常回傳。

---

### 邊界情況

- 重命名 Service 後，所有 @Autowired 引用是否更新？必須全域搜索替換。
- coin_transaction 表（原 wallet_transaction）的歷史資料是否受影響？不影響，Entity 名稱改但表名可不改（或一起改）。
- 併發扣款時的安全性？CoinService 使用 User 表的 version 欄位做樂觀鎖。

## 需求規格

### 功能需求

- **FR-001**：`WalletService` → `CoinService` 全面重命名（介面 + 實作 + 所有引用）。
- **FR-002**：`WalletController`（前台）刪除。
- **FR-003**：`AdminWalletController` → `AdminCoinController` 重命名，API 路徑改為 `/admin/coin/*`。
- **FR-004**：`RechargeServiceImpl` 修復：改用 `CoinService.addGold()` 和 `CoinService.addBonus()`，不再直接操作 user 表。
- **FR-005**：`DrawServiceImpl`（扭蛋）修復：移除 Bonus 自動補扣邏輯，改為依商品 `paymentType` 統一扣款。
- **FR-006**：所有抽獎扣款邏輯統一：
  - paymentType=GOLD → `CoinService.deductGold()`，不足拒絕
  - paymentType=BONUS → `CoinService.deductBonus()`，不足拒絕
  - 不做混合扣款
- **FR-007**：`coin_transaction` 表保留作為金幣流水記錄（可選擇是否重命名表名）。
- **FR-008**：DTO 重命名：`UserWalletRes` → `UserCoinRes`，`WalletTransactionRes` → `CoinTransactionRes`，`WalletAdjustReq` → `CoinAdjustReq`。

### 核心實體

無新資料表。重構既有的 `wallet_transaction` 表（可選重命名為 `coin_transaction`）。

## 成功標準

- **SC-001**：`grep -r "WalletService\|WalletController\|WalletServiceImpl" src/` 回傳 0 結果。
- **SC-002**：`RechargeServiceImpl` 中不再直接呼叫 `userMapper.updateByPrimaryKey` 修改 goldCoins/bonusCoins。
- **SC-003**：`DrawServiceImpl` 中不再呼叫 `walletService.deductBonus()` 做自動補扣。
- **SC-004**：`mvn clean package -DskipTests` 編譯通過。

## 假設前提

- wallet_transaction 表名可保留不改（避免太多 DB 變更），只改 Java 層命名。
- 若決定改表名，需提供 DDL（`RENAME TABLE wallet_transaction TO coin_transaction`）。
- 金幣/紅利餘額仍存在 user 表的 goldCoins/bonusCoins 欄位，不新增獨立錢包表。
