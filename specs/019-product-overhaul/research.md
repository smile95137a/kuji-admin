# 研究紀錄：商品管理重整

**Feature**: `019-product-overhaul`  
**Date**: 2026-04-30

## Decision 1：`freeDrawThreshold` 採刮刮樂專用可空欄位

- **Decision**: `freeDrawThreshold` 僅適用於 `CUSTOM_GACHA + SCRATCH_MODE`，可為 `NULL`；`NULL` 表示店家未啟用免費抽/免單機制；只有在店家提供數值時才驗證 `>= 1`。
- **Rationale**: 這與澄清結果一致，也符合現有前端「店家自行決定是否啟用」的操作語意。將 `NULL` 視為合法狀態，可避免刮刮樂商品因未啟用免費抽機制而無法建立，並保留後續開關式 UI 的延展性。
- **Alternatives considered**:
  - `SCRATCH_MODE` 一律必填：與商業規則衝突，會造成不必要的建立失敗。
  - 歷史資料一律補 `3`：會把未啟用機制的歷史商品誤判為已啟用。

## Decision 2：`playMode` 為後端推導欄位，不信任 client 傳值

- **Decision**: `playMode` 仍保留於 request / response 物件以兼容既有程式結構，但建立與更新時一律由後端依 `category + subCategory` 重新計算，不以 client 傳入值為準。
- **Rationale**: 目前前端文件已明確寫出「playMode 由後端自動推算」，而 `LotteryServiceImpl` 也已有 `resolvePlayMode()` 邏輯。延續此做法可降低前後端資料不一致風險，避免外部請求透過錯誤 `playMode` 污染抽獎策略分派。
- **Alternatives considered**:
  - 由前端直接傳 `playMode` 並信任：容易出現 `CUSTOM_GACHA + LOTTERY_MODE` 與 `SCRATCH_MODE` 規則錯配。
  - 直接移除 request 上的 `playMode` 欄位：改動面較大，超出本 feature 範圍。

## Decision 3：`CUSTOM_GACHA` 以 `subCategory` 分流兩套寫入規則

- **Decision**:
  - `CUSTOM_GACHA + LOTTERY_MODE`：不顯示且不接收 `gameMode`，`freeDrawThreshold = NULL`，`delistStrategy = ALL_DRAWN`。
  - `CUSTOM_GACHA + SCRATCH_MODE`：`gameMode` 必填，`freeDrawThreshold` 可空但若有值必須 `>= 1`，`delistStrategy = GRAND_PRIZE_DRAWN`。
- **Rationale**: 目前 repo 已存在 `LOTTERY_MODE` / `SCRATCH_MODE` 的雙模式分流與 `resolvePlayMode()` 實作。若不在計畫階段把兩者規則切乾淨，後續 Service 驗證、前端表單顯示與測試案例都會互相干擾。
- **Alternatives considered**:
  - 對所有 `CUSTOM_GACHA` 都要求 `gameMode`：會讓抽籤型商品增加無意義欄位。
  - 讓 `LOTTERY_MODE` 的 `delistStrategy` 可選：與既有固定型商品（GACHA/TRADING_CARD）不一致，測試矩陣也更大。

## Decision 4：Controller 合併採「路徑不變、歸屬收斂」策略

- **Decision**: 後台將 `AdminLotteryWithPrizesController` 能力收斂到 `AdminLotteryController`；前台以 `LotteryController` 既有列表/詳情路徑為主，若規格提及舊 `LotteryDetailController`，在實作上視為相容性驗證而非新增重構任務。
- **Rationale**: 目前程式碼中 `AdminLotteryWithPrizesController` 仍存在，是真正待合併目標；但 public 端已找不到 `LotteryDetailController` 類別，說明部分收斂可能已完成。規劃應反映 repo 現況，而不是對不存在的 class 再做虛構性拆併。
- **Alternatives considered**:
  - 直接依舊 spec 假設 public 端仍有 `LotteryDetailController`：會導致 tasks 與實作落點失真。
  - 另起新 controller 承接合併結果：違反路徑與責任收斂目標。

## Decision 5：自動下架維持交易後同步檢查，不改成排程補償

- **Decision**: 自動下架仍由 `DrawService` / `LotteryTicketService` 在抽獎流程結束後同步呼叫 `checkAndDelist()`，只檢查當前 `lotteryId` 與必要獎品/剩餘抽數條件。
- **Rationale**: 這可保持抽獎結果與商品狀態在同一業務流程內收斂，避免排程式延遲導致前台短時間看到已應下架的商品。對目前單體式 Spring Boot + MyBatis 架構來說，這是最簡單且可測試的方案。
- **Alternatives considered**:
  - 改成排程輪詢下架：會增加狀態延遲與補償複雜度。
  - 在 controller 層觸發下架：違反 Service 層收斂原則，難以保證所有抽獎路徑一致。

## Decision 6：測試以規則矩陣回歸，而非只測單一 happy path

- **Decision**: 規劃階段即要求補齊 service/controller/integration 三層測試，至少涵蓋 `OFFICIAL_ICHIBAN`、`GACHA`、`CUSTOM_GACHA + LOTTERY_MODE`、`CUSTOM_GACHA + SCRATCH_MODE` 四組建立/更新規則，以及 `GRAND_PRIZE_DRAWN` / `ALL_DRAWN` / `MANUAL` 三種下架策略。
- **Rationale**: 本 feature 的風險不是單一欄位新增，而是多組分類規則交叉後容易回歸。若只驗 happy path，極可能漏掉 `NULL` threshold、固定 delistStrategy、舊欄位忽略等案例。
- **Alternatives considered**:
  - 僅補單元測試：不足以驗證 controller 合約與抽獎流程整合點。
  - 僅跑手動驗證：無法形成穩定回歸保護網。
