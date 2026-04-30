# 功能規格書：商品管理重整

**功能分支**：`019-product-overhaul`
**建立日期**：2026-04-13
**狀態**：草稿
**輸入**：Controller 合併、欄位調整（新增 paymentType/freeDrawThreshold/delistStrategy，移除 multiDrawOptions/allowMultiDraw/protectionDraws/protectionMinutes）、GameMode 自動帶入

## Clarifications

### Session 2026-04-30

- Q: 刮刮樂商品的 freeDrawThreshold 是否必填？ → A: 選填；NULL 代表店家未啟用免費抽/免單機制，僅在啟用時必須 >= 1。
- Q: freeDrawThreshold 適用於哪些商品類型？ → A: 僅適用於 CUSTOM_GACHA + SCRATCH_MODE；其他類型一律為 NULL。
- Q: 歷史刮刮樂商品若 freeDrawThreshold 無值，資料遷移後如何處理？ → A: 維持 NULL，代表未啟用免費抽/免單機制。
- Q: CUSTOM_GACHA + LOTTERY_MODE 的建立規則為何？ → A: gameMode 不顯示且不傳、freeDrawThreshold 一律為 NULL、delistStrategy 固定為 ALL_DRAWN。

## 使用者情境與測試

### 使用者故事 1 — 店家建立商品時欄位精簡化（優先級：P1）

身為店家負責人，我希望建立商品時只需填寫必要欄位，系統根據分類自動帶入 gameMode 和 delistStrategy，減少操作複雜度。

**此優先級的原因**：目前建立商品有過多可選欄位（multiDrawOptions、protectionMinutes 等），部分已不需要，需精簡。

**獨立測試**：店家選擇分類「一番賞」，系統自動帶入 gameMode=TICKET、delistStrategy 下拉選項出現三個選項。店家選擇「刮刮樂」，系統要求選擇 gameMode（SCRATCH_STORE/SCRATCH_PLAYER/RANDOM）；freeDrawThreshold 可留空，若店家啟用免費抽/免單機制則必須填寫且值需 >= 1。

**驗收情境**：

1. **在** 店家選擇 category=OFFICIAL_ICHIBAN 的情況下，**當** 送出建立請求，**則** gameMode 自動設為 TICKET，delistStrategy 從請求中讀取（必填，可選 GRAND_PRIZE_DRAWN/ALL_DRAWN/MANUAL）。
2. **在** 店家選擇 category=GACHA 的情況下，**當** 送出建立請求，**則** gameMode 自動設為 RANDOM，delistStrategy 自動設為 ALL_DRAWN。
3. **在** 店家選擇 category=CUSTOM_GACHA + subCategory=LOTTERY_MODE 的情況下，**當** 送出建立請求，**則** 不顯示也不接收 gameMode，delistStrategy 自動設為 ALL_DRAWN，freeDrawThreshold 一律為 NULL。
4. **在** 店家選擇 category=CUSTOM_GACHA + subCategory=SCRATCH_MODE 的情況下，**當** 送出建立請求，**則** gameMode 必填（SCRATCH_STORE/SCRATCH_PLAYER/RANDOM），delistStrategy 自動設為 GRAND_PRIZE_DRAWN，freeDrawThreshold 可為 NULL；若店家啟用免費抽/免單機制則必須 >= 1。
5. **在** 請求中包含 multiDrawOptions 欄位的情況下，**當** 送出建立請求，**則** 欄位被忽略（已移除）。

---

### 使用者故事 2 — 商品支付方式選擇（優先級：P1）

身為店家負責人，我希望建立商品時選擇「用金幣抽」或「用紅利抽」，讓我可以針對不同商品使用不同的支付幣種。

**獨立測試**：店家建立一個 paymentType=BONUS 的商品，玩家抽獎時系統扣紅利而非金幣。

**驗收情境**：

1. **在** 商品 paymentType=GOLD 的情況下，**當** 玩家抽獎，**則** 系統扣金幣。
2. **在** 商品 paymentType=BONUS 的情況下，**當** 玩家抽獎，**則** 系統扣紅利。
3. **在** 建立商品未指定 paymentType 的情況下，**當** 送出請求，**則** 預設為 GOLD。

---

### 使用者故事 3 — 自動下架策略（優先級：P2）

身為店家負責人，我希望為一番賞商品選擇自動下架策略，讓系統在特定條件達成時自動將商品下架。

**獨立測試**：建立 delistStrategy=GRAND_PRIZE_DRAWN 的一番賞商品，最後一個大獎被抽走後，商品自動下架（status → ENDED）。

**驗收情境**：

1. **在** delistStrategy=GRAND_PRIZE_DRAWN 的情況下，**當** 最後一個 isGrandPrize=1 的獎品被抽完，**則** 商品 status → ENDED，前台不可見。
2. **在** delistStrategy=ALL_DRAWN 的情況下，**當** 所有獎品被抽完（totalDraws=0），**則** 商品 status → ENDED。
3. **在** delistStrategy=MANUAL 的情況下，**當** 所有獎品被抽完，**則** 商品 status → SOLD_OUT 但不自動下架，等店家手動處理。
4. **在** 刮刮樂商品的情況下，**當** 唯一的大獎被抽走，**則** 商品立即自動下架（GRAND_PRIZE_DRAWN 固定策略）。

---

### 使用者故事 4 — 合併重複 Controller（優先級：P2）

身為開發者，我希望將目前分散的 3 個後台商品 Controller 和 2 個前台商品 Controller 合併，減少維護成本。

**驗收情境**：

1. **在** 合併完成後，**當** 呼叫原有的所有商品 API，**則** 功能與路徑不變。
2. **在** 搜尋 `AdminLotteryWithPrizesController` 的情況下，**則** 已刪除，功能移至 `AdminLotteryController`。

---

### 邊界情況

- 現有商品沒有 paymentType/delistStrategy 欄位？DDL 新增欄位時給預設值（paymentType=GOLD, delistStrategy=MANUAL）。
- 歷史 `CUSTOM_GACHA + SCRATCH_MODE` 商品若 freeDrawThreshold 無值？資料遷移後維持 NULL，代表未啟用免費抽/免單機制。
- 修改已上架商品的 paymentType？不允許，只有 DRAFT 狀態可修改。
- freeDrawThreshold 為 NULL？合法，代表店家未啟用免費抽/免單機制。
- freeDrawThreshold 設為 0？不合法；僅在啟用免費抽/免單機制時允許填值，且至少為 1。

## 需求規格

### 功能需求

- **FR-001**：Lottery 表新增欄位：`payment_type`（VARCHAR(20), DEFAULT 'GOLD'）、`free_draw_threshold`（INTEGER, NULL）、`delist_strategy`（VARCHAR(30), DEFAULT 'MANUAL'）。
- **FR-002**：Lottery 表移除欄位（或標記廢棄）：`multi_draw_options`、`allow_multi_draw`、`protection_draws`、`protection_minutes`。
- **FR-003**：LotteryCreateReq 欄位調整（見 data-model.md）。
- **FR-004**：建立商品時依 category 自動設定 gameMode 和 delistStrategy（見規則表）。
- **FR-005**：freeDrawThreshold 僅適用於 `CUSTOM_GACHA + SCRATCH_MODE`，其他類型一律為 NULL。此欄位為選填；NULL 代表店家未啟用免費抽/免單機制。刮刮樂商品不得因 freeDrawThreshold 為 NULL 而建立失敗；若店家啟用免費抽/免單機制，freeDrawThreshold 必須 >= 1。
- **FR-006**：paymentType 必填（預設 GOLD）。
- **FR-006A**：`CUSTOM_GACHA + LOTTERY_MODE` 不顯示也不接收 gameMode，freeDrawThreshold 一律為 NULL，delistStrategy 固定為 `ALL_DRAWN`。
- **FR-007**：合併 AdminLotteryController + AdminLotteryWithPrizesController + AdminLotterySessionController → 統一的 AdminLotteryController。
- **FR-008**：合併 LotteryController + LotteryDetailController → 統一的 LotteryController。
- **FR-009**：自動下架邏輯實作於 Service 層（DrawService/LotteryTicketService 抽獎後呼叫 `checkAndDelist()`）。

### GameMode 自動帶入規則

| Category | SubCategory | gameMode 行為 | 前端顯示 |
|----------|-------------|---------------|----------|
| OFFICIAL_ICHIBAN | — | 自動帶入 TICKET | 不顯示 gameMode 選擇 |
| TRADING_CARD | — | 自動帶入 TICKET | 不顯示 gameMode 選擇 |
| GACHA | — | 自動帶入 RANDOM | 不顯示 gameMode 選擇 |
| CUSTOM_GACHA | LOTTERY_MODE | 不顯示且不接收 gameMode | 不顯示 gameMode 選擇 |
| CUSTOM_GACHA | SCRATCH_MODE | 必填，下拉選擇 | 顯示 3 個選項 |

### DelistStrategy 規則

| Category | 允許值 | 預設值 |
|----------|--------|--------|
| OFFICIAL_ICHIBAN | GRAND_PRIZE_DRAWN / ALL_DRAWN / MANUAL | 必選 |
| TRADING_CARD | ALL_DRAWN（固定） | ALL_DRAWN |
| GACHA | ALL_DRAWN（固定） | ALL_DRAWN |
| CUSTOM_GACHA (LOTTERY_MODE) | ALL_DRAWN（固定） | ALL_DRAWN |
| CUSTOM_GACHA (SCRATCH) | GRAND_PRIZE_DRAWN（固定） | GRAND_PRIZE_DRAWN |

### 核心實體變更

見 data-model.md。

## 成功標準

- **SC-001**：後台只有 1 個 `AdminLotteryController` 和 1 個前台 `LotteryController`。
- **SC-002**：建立刮刮樂商品必填 gameMode；freeDrawThreshold 可為 NULL，且僅在店家啟用免費抽/免單機制時必須 >= 1；建立一番賞不需填 gameMode。
- **SC-003**：paymentType=BONUS 的商品抽獎時扣紅利。
- **SC-004**：自動下架邏輯依 delistStrategy 正確執行。
- **SC-005**：`mvn clean package -DskipTests` 編譯通過。

## 假設前提

- 移除 protectionDraws/protectionMinutes 不影響免單邏輯（免單門檻改為 freeDrawThreshold 獨立欄位）。
- 移除 multiDrawOptions/allowMultiDraw 後所有商品預設可多抽（上限由 system_config 管理）。
- 現有 DRAFT 狀態商品可正常遷移（新欄位有預設值）。
