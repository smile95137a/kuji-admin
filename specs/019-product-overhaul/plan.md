# 實作計畫：商品管理重整

**Branch**: `019-product-overhaul` | **日期**: 2026-04-13 | **規格**: [spec.md](./spec.md)

## 摘要

重整商品管理模組：合併散落的 5 個 Controller 為 2 個（後台+前台各 1 個），調整 Lottery 表欄位（新增 paymentType/freeDrawThreshold/delistStrategy，廢棄 multiDrawOptions/allowMultiDraw/protectionDraws/protectionMinutes），實作 GameMode 自動帶入規則，建立自動下架策略系統。

## 技術背景

**語言／版本**：Java 21
**前置依賴**：Spec 017（需要 GameModeEnum、PaymentTypeEnum、DelistStrategyEnum）

## 架構規範檢查

| 關卡 | 狀態 | 備註 |
|------|------|------|
| DDL-first 原則 | ✅ | 先改 DB 再 MBG |
| 前後台 Controller 分離 | ✅ | admin/ + api/ 各一個 |
| storeId 自動帶入 | ✅ | SecurityUtils.getCurrentUserPrimaryStoreId() |
| AOP 自動包裝 | ✅ | 不手動建立 ApiResponse |

## 複雜度追蹤

| 面向 | 預估 |
|------|------|
| DDL 變更 | 新增 3 欄位 + 廢棄 4 欄位 |
| Controller 合併 | 減少 3 個檔案 |
| Service 邏輯調整 | createLottery + updateLottery + checkAndDelist |
| 預估工時 | 1 天 |
