# 實作計畫：列舉統一與型別清理

**Branch**: `017-enum-cleanup` | **日期**: 2026-04-13 | **規格**: [spec.md](./spec.md)

## 摘要

清理專案中 21 個 Enum 的重複與不一致問題。合併 3 對重複 Enum（PointType/CoinTypeEnum、PrizeLevel/PrizeLevelEnum、PointOperationType/TransactionTypeEnum），新建 3 個缺失 Enum（GameModeEnum、PaymentTypeEnum、DelistStrategyEnum），建立統一介面 `DisplayableEnum`，消除所有 gameMode raw string，移除待廢棄的 ShippingMethodEnum，並提供通用 Enum 查詢 API。

## 技術背景

**語言／版本**：Java 21
**影響範圍**：主要為重構性質，涉及 Entity、Service、Controller、DTO 的 import 更新
**風險等級**：中（需謹慎處理 DB 中的 Enum 值大小寫遷移）

## 架構規範檢查

| 關卡 | 狀態 | 備註 |
|------|------|------|
| 不修改 MBG 生成的 Entity | ✅ | Enum 合併不觸及 Entity 結構 |
| Enum code 值一律 UPPERCASE | ✅ | 合併後統一 |
| 不破壞現有 API 回傳格式 | ⚠️ 注意 | Enum 回傳格式從純字串改為 `{ code, displayName }` 需前端配合 |

## 專案結構

### 新建檔案

```text
src/main/java/com/group/admin/enums/
├── DisplayableEnum.java          (新建 — 統一介面)
├── GameModeEnum.java             (新建)
├── PaymentTypeEnum.java          (新建)
└── DelistStrategyEnum.java       (新建)

src/main/java/com/group/admin/controller/api/
└── EnumController.java           (新建 — Enum 查詢 API)
```

### 修改檔案

```text
src/main/java/com/group/admin/enums/
├── CoinTypeEnum.java             (加入 isGold/isBonus，實作 DisplayableEnum)
├── PrizeLevelEnum.java           (加入 sortOrder/isGrandPrize，實作 DisplayableEnum)
├── TransactionTypeEnum.java      (加入 isIncrease、新值，實作 DisplayableEnum)
└── [其他 8 個 Enum]              (實作 DisplayableEnum)
```

### 刪除檔案

```text
src/main/java/com/group/admin/enums/
├── PointType.java                (合併至 CoinTypeEnum)
├── PrizeLevel.java               (合併至 PrizeLevelEnum)
├── PointOperationType.java       (合併至 TransactionTypeEnum)
└── ShippingMethodEnum.java       (改 DB 管理)
```

## 複雜度追蹤

| 面向 | 預估 | 說明 |
|------|------|------|
| 新建 Enum | 4 | DisplayableEnum + GameModeEnum + PaymentTypeEnum + DelistStrategyEnum |
| 合併 Enum（含引用更新） | 3 對 | 涉及 ~30-50 個檔案的 import 更新 |
| 刪除 Enum | 4 | PointType + PrizeLevel + PointOperationType + ShippingMethodEnum |
| DB 遷移 SQL | 1 | coin_type UPPERCASE |
| 預估工時 | 1 天 | 引用掃描與替換為主要工時 |
