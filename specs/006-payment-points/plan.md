# 實作計畫：付款與點數系統 (Payment & Points System)

**分支**：`006-payment-points` | **日期**：2026-03-22 | **規格**：[spec.md](./spec.md)  
**輸入**：功能規格來自 `/specs/006-payment-points/spec.md`

## 摘要

為 KUJI 抽獎平台實作雙幣別錢包系統（金幣 = 購買取得；紅利點數 = 贏取/獎勵）。錢包以 `goldCoins` / `bonusCoins` 儲存於現有 `User` 實體，並透過樂觀鎖（`version` 欄位）加以保護。本實作完成以下已在程式碼中建立骨架的層次：

1. **金流閘道回呼** — 整合台灣金流業者（TapPay / ECPay / NewebPay，於實作前確認）進行信用卡儲值；僅在驗證成功的回呼後才入帳金幣。
2. **原子性錢包操作** — `SELECT … FOR UPDATE` + 樂觀鎖，消除雙重消費與競爭條件。
3. **儲值套餐** — 管理員管理的固定金額方案（金幣 + 可選紅利點數加贈）。
4. **獎品回收** — 以未出貨的 PrizeBox 物品換取紅利點數；永久不可逆。
5. **稽核軌跡** — 每次餘額變動皆記錄於 `wallet_transaction`，含變動後餘額快照。

## 技術背景

**語言/版本**：Java 21  
**主要相依**：Spring Boot 3.3.3 · MyBatis 3.0.5 · Spring Security · JWT · Lombok  
**儲存**：MySQL 8.3 (AWS RDS)  
**測試**：JUnit 5 · Spring Boot Test · Mockito  
**目標平台**：AWS EC2 (Linux) — 單一區域  
**專案類型**：REST API（web-service）  
**效能目標**：錢包讀取 < 100 ms p95；扣款 < 200 ms p95  
**限制條件**：零雙重消費（由 DB 層樂觀鎖強制執行）；不允許負餘額；v1.0 不提供退款  
**規模/範疇**：約 1 萬名活躍使用者；約 100 個並發抽獎  
**幣別優先順序**：優先消費金幣；金幣耗盡後才使用紅利點數（v1.0 固定規則）  
**套件根目錄**：`com.group.admin`

## 規範檢查

*規範檔案為佔位模板 — 未定義專案特定的驗收標準。套用標準品質驗收標準：*

| 驗收標準 | 狀態 | 備註 |
|------|--------|-------|
| 原子性錢包操作 (FR-014) | ✅ PASS | `User.version` 樂觀鎖已存在於實體 |
| 不允許負餘額 (SC-002) | ✅ PASS | `hasEnoughGold()` 防護 + DB 約束 |
| 金幣優先扣款 (FR-005 / SC-004) | ✅ PASS | `WalletServiceImpl` 中固定優先順序 |
| 每次餘額變動皆記錄稽核日誌 (FR-006) | ✅ PASS | `WalletTransaction` 實體 + mapper 已存在 |
| 商店無法存取玩家錢包 (FR-011) | ✅ PASS | 獨立 `/admin/*` 路徑加角色防護 |
| 金流回呼冪等性 | ✅ PASS | `RechargeOrder.status` 狀態機防止重複播放 |

**第一階段後重新檢查**：合約定稿後重新評估。未發現任何違規。

## 專案結構

### 文件（本功能）

```text
specs/006-payment-points/
├── plan.md              # 本文件
├── research.md          # 第 0 階段 — 閘道研究、並發模式
├── data-model.md        # 第 1 階段 — 實體與 DB 結構描述詳細說明
├── quickstart.md        # 第 1 階段 — 開發環境設定與冒煙測試指南
├── contracts/           # 第 1 階段 — REST API 合約
│   ├── wallet-api.md
│   ├── recharge-packages-admin-api.md
│   └── admin-wallet-adjust-api.md
└── tasks.md             # 第 2 階段輸出 — 非由 /speckit.plan 建立
```

### 原始碼（儲存庫根目錄）

```text
src/main/java/com/group/admin/
├── entity/
│   ├── User.java                    # goldCoins, bonusCoins, version（現有）
│   ├── WalletTransaction.java       # 稽核日誌（現有 — 擴充類型）
│   ├── RechargeOrder.java           # 新增：金流閘道訂單狀態機
│   └── RechargePlan.java            # 現有（儲值套餐）
├── mapper/
│   ├── UserMapper.java              # updateGoldAndBonus 含版本檢查（擴充）
│   ├── WalletTransactionMapper.java # insertTransaction, selectByUserId（現有）
│   ├── RechargeOrderMapper.java     # 新增：閘道訂單 CRUD
│   └── RechargePlanMapper.java      # 現有（如需擴充）
├── service/
│   ├── WalletService.java           # 現有介面（為閘道擴充）
│   ├── impl/WalletServiceImpl.java  # 現有實作（完成原子操作）
│   ├── RechargeService.java         # 現有 — 完成回呼處理
│   └── impl/RechargeServiceImpl.java
├── controller/
│   ├── api/WalletController.java        # GET /api/wallet, GET /api/wallet/transactions
│   ├── api/RechargeController.java      # POST /api/wallet/recharge
│   ├── api/PrizeBoxController.java      # POST /api/prize-box/recycle（擴充）
│   ├── admin/AdminWalletController.java # POST /admin/wallet/adjust（擴充）
│   └── admin/AdminRechargePlanController.java # 現有（擴充）
├── req/wallet/
│   ├── RechargeReq.java             # planId
│   ├── GatewayCallbackReq.java      # 新增：閘道 webhook 載荷
│   └── AdminAdjustReq.java         # userId, delta, currency, reason
├── res/wallet/
│   ├── WalletRes.java              # goldBalance, bonusBalance, userId
│   └── TransactionRes.java         # id, type, amount, currency, balanceAfter, createdAt
└── enums/
    ├── TransactionType.java         # RECHARGE, DRAW, BONUS_GRANT, RECYCLE, ADMIN_ADJUST, REFUND
    └── RechargeOrderStatus.java     # PENDING, SUCCESS, FAILED, EXPIRED

src/main/resources/mapper/
├── WalletTransactionMapper.xml
├── RechargeOrderMapper.xml
└── UserMapper.xml                  # 加入 updateGoldBonusWithVersion

src/test/java/com/group/admin/
├── service/WalletServiceTest.java
├── service/RechargeServiceTest.java
└── controller/WalletControllerTest.java
```

**結構決策**：單一 Spring Boot 專案（現有程式碼庫）。不需要新模組。新程式碼加入到現有已建立骨架的錢包/儲值類別旁邊。

## 複雜度追蹤

> 無規範違規需要說明。現有的樂觀鎖模式（`User.version`）是在 DB 層防止雙重消費而無需分散式鎖的正確方法。
