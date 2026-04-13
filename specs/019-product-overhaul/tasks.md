# 任務清單：商品管理重整

**輸入**：設計文件來自 `/specs/019-product-overhaul/`
**分支**：`019-product-overhaul` | **建立日期**：2026-04-13

---

## 第一階段：DDL 與 MBG 重新生成

- [ ] T001 執行 DDL：新增 `payment_type`, `free_draw_threshold`, `delist_strategy` 欄位
- [ ] T002 執行 DDL：將 `multi_draw_options`, `allow_multi_draw`, `protection_draws`, `protection_minutes` 改為 NULL
- [ ] T003 執行歷史資料遷移 SQL
- [ ] T004 更新 generatorConfig.xml 並執行 `mvn mybatis-generator:generate`（重新生成 Lottery 相關）

**檢查點**：Lottery Entity 包含新欄位，MBG 完成

---

## 第二階段：DTO 調整

- [ ] T005 [P] 修改 `LotteryCreateReq.java`：
  - 新增 `paymentType`（@NotBlank, 預設 GOLD）、`freeDrawThreshold`（@Min(1), 刮刮樂必填）、`delistStrategy`
  - 移除 `multiDrawOptions`, `allowMultiDraw`（欄位保留但標記 @Deprecated 或直接移除）
- [ ] T006 [P] 修改 `LotteryRes.java`：
  - 新增 `paymentType`, `freeDrawThreshold`, `delistStrategy` 欄位
  - 移除 `multiDrawOptions`, `allowMultiDraw`, `protectionDraws`, `protectionMinutes`

**檢查點**：DTO 就緒

---

## 第三階段：Service 邏輯調整

- [ ] T007 修改 `LotteryServiceImpl.createLottery()`：
  - 呼叫 `resolveGameMode()` 自動帶入 gameMode
  - 呼叫 `resolveDelistStrategy()` 自動帶入 delistStrategy
  - 設定 `paymentType`（預設 GOLD）
  - 刮刮樂驗證 freeDrawThreshold 必填
  - 移除 multiDrawOptions / allowMultiDraw 的處理邏輯
- [ ] T008 修改 `LotteryServiceImpl.updateLottery()`：同步調整
- [ ] T009 修改 `LotteryServiceImpl.toLotteryRes()`：映射新欄位到 Res
- [ ] T010 新增 `LotteryServiceImpl.checkAndDelist(String lotteryId)`：
  - 讀取 lottery.delistStrategy
  - GRAND_PRIZE_DRAWN：檢查所有 isGrandPrize=1 的 prize 是否 remaining=0 → 下架
  - ALL_DRAWN：檢查 totalDraws 是否用完 → 下架
  - MANUAL：不自動下架
  - 下架動作：lottery.status = ENDED

**檢查點**：商品建立正確帶入 gameMode/delistStrategy/paymentType

---

## 第四階段：Controller 合併

- [ ] T011 將 `AdminLotteryWithPrizesController` 的所有方法移至 `AdminLotteryController`，刪除原檔
- [ ] T012 將 `AdminLotterySessionController`（若存在）的方法移至 `AdminLotteryController`，刪除原檔
- [ ] T013 將 `LotteryDetailController`（若存在）的方法移至 `LotteryController`，刪除原檔
- [ ] T014 更新所有被刪除 Controller 在 SecurityConfig 中的路由規則（若有）

**檢查點**：後台 1 個 + 前台 1 個 Controller，所有原有 API 路徑不變

---

## 第五階段：自動下架整合

- [ ] T015 在 `DrawServiceImpl` 抽獎後呼叫 `lotteryService.checkAndDelist(lotteryId)`
- [ ] T016 在 `LotteryTicketServiceImpl` 抽獎後呼叫 `lotteryService.checkAndDelist(lotteryId)`
- [ ] T017 `mvn clean package -DskipTests` 確認編譯通過

**檢查點**：抽獎後自動下架邏輯正確觸發

---

## 依賴關係

```
第一階段（DDL + MBG）     — 依賴 Spec 017（需要 GameModeEnum/PaymentTypeEnum/DelistStrategyEnum）
第二階段（DTO）           — 依賴第一階段
第三階段（Service）        — 依賴第二階段
第四階段（Controller 合併） — 無強依賴，可與第三階段平行
第五階段（自動下架）        — 依賴第三階段
```
