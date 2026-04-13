# 實作計畫：金幣系統重構

**Branch**: `018-coin-system` | **日期**: 2026-04-13 | **規格**: [spec.md](./spec.md)

## 摘要

重構金幣/紅利系統：WalletService → CoinService 全面重命名，移除前台 WalletController，修復 RechargeServiceImpl 直接操作 user 表的反模式，統一所有抽獎扣款邏輯為依商品 paymentType 單一幣種扣款。

## 技術背景

**語言／版本**：Java 21
**主要依賴**：Spring Boot 3.3.3, MyBatis 3.0.5
**影響模組**：WalletService（全引用）、RechargeServiceImpl、DrawServiceImpl、LotteryTicketServiceImpl、AdminWalletController
**風險等級**：高（影響金流核心邏輯）

## 架構規範檢查

| 關卡 | 狀態 | 備註 |
|------|------|------|
| Controller → Service → Mapper 分層 | ✅ | 修復 Recharge 的分層違規 |
| 不修改 MBG 生成的 Entity | ✅ | WalletTransaction Entity 保留不改 |
| 金幣操作必須記錄 coin_transaction | ✅ | 統一由 CoinService 處理 |
| 樂觀鎖（User.version） | ✅ | CoinService 已實作 |

## 專案結構

### 新建檔案

```text
src/main/java/com/group/admin/
├── service/CoinService.java                          (新建 — 取代 WalletService)
├── service/impl/CoinServiceImpl.java                 (新建 — 取代 WalletServiceImpl)
├── req/coin/CoinAdjustReq.java                       (重命名)
├── res/coin/UserCoinRes.java                         (重命名)
├── res/coin/CoinTransactionRes.java                  (重命名)
├── condition/CoinTransactionCondition.java           (重命名)
└── controller/admin/AdminCoinController.java         (重命名)
```

### 刪除檔案

```text
src/main/java/com/group/admin/
├── service/WalletService.java
├── service/impl/WalletServiceImpl.java
├── controller/api/WalletController.java
├── controller/admin/AdminWalletController.java
├── req/wallet/WalletAdjustReq.java
├── res/wallet/UserWalletRes.java
├── res/wallet/WalletTransactionRes.java
└── condition/WalletTransactionCondition.java

src/test/java/com/group/admin/
└── controller/api/WalletControllerTest.java
```

## 複雜度追蹤

| 面向 | 預估 |
|------|------|
| 重命名影響檔案數 | ~20 檔 |
| 業務邏輯修改 | 3 處（Recharge、DrawService、LotteryTicketService） |
| 預估工時 | 0.5 天 |
