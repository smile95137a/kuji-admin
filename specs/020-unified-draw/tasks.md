# 任務清單：統一抽獎系統

**輸入**：設計文件來自 `/specs/020-unified-draw/`
**分支**：`020-unified-draw` | **建立日期**：2026-04-13

---

## 第一階段：Strategy Pattern 基礎架構

**目的**：建立 DrawStrategy 介面、Factory、三個策略實作的骨架。

- [ ] T001 建立 `src/main/java/com/group/admin/service/draw/DrawStrategy.java`（介面：`DrawResultRes execute(String userId, Lottery lottery, DrawRequest request)`）
- [ ] T002 建立 `src/main/java/com/group/admin/service/draw/DrawStrategyFactory.java`（@Component，依 category 回傳對應 Strategy）
- [ ] T003 [P] 建立 `src/main/java/com/group/admin/service/draw/GachaDrawStrategy.java`（@Component，整合現有 DrawServiceImpl 的加權隨機邏輯）
- [ ] T004 [P] 建立 `src/main/java/com/group/admin/service/draw/TicketDrawStrategy.java`（@Component，整合現有 LotteryTicketServiceImpl 的籤位制邏輯）
- [ ] T005 [P] 建立 `src/main/java/com/group/admin/service/draw/ScratchDrawStrategy.java`（@Component，整合現有刮刮樂邏輯）
- [ ] T006 建立 `src/main/java/com/group/admin/req/draw/DrawRequest.java`（統一 DTO：count, ticketNumber, tickets）
- [ ] T007 強化 `src/main/java/com/group/admin/res/draw/DrawResultRes.java`（統一回傳格式，加入 freeDrawTriggered、designationRequired 等欄位）

**檢查點**：3 個 Strategy 骨架就緒，Factory 可正確派發

---

## 第二階段：統一 Controller

- [ ] T008 建立 `src/main/java/com/group/admin/controller/api/DrawController.java`：
  - `POST /api/lottery/{lotteryId}/draw` — 讀取 Lottery → DrawStrategyFactory.getStrategy(category) → strategy.execute()
  - `POST /api/lottery/{lotteryId}/designate` — 保留刮刮樂 SCRATCH_PLAYER 指定邏輯
  - `GET /api/lottery/{lotteryId}/session` — 保留 session 查詢
  - 整合保護時間取得/延長邏輯
  - 整合扣款邏輯（依 paymentType 呼叫 CoinService）
  - 整合多抽驗證（max count 從 system_config 讀取）
  - 整合庫存校驗（count > 剩餘 → 拒絕）
- [ ] T009 刪除 `RandomDrawController.java`（扭蛋入口已整合至 DrawController）
- [ ] T010 刪除 `LotteryDrawController.java`（功能已整合至 DrawController）

**檢查點**：統一入口正常運作，舊 Controller 刪除

---

## 第三階段：GachaDrawStrategy 實作

**目的**：將 DrawServiceImpl 的加權隨機邏輯遷移至 GachaDrawStrategy。

- [ ] T011 遷移 `DrawServiceImpl.executeDraw()` 的核心邏輯到 `GachaDrawStrategy.execute()`：
  - 加權隨機選獎（依 LotteryPrize.weight）
  - SELECT FOR UPDATE 防止超賣
  - remaining 遞減
  - 記錄 LotteryDrawRecord
  - 自動降價檢查（autoDiscountEnabled）
  - 使用 synchronized 確保並發安全
- [ ] T012 在 GachaDrawStrategy 中呼叫 `lotteryService.checkAndDelist(lotteryId)`（自動下架）

**檢查點**：扭蛋商品透過統一入口抽獎正常

---

## 第四階段：TicketDrawStrategy 實作

**目的**：將 LotteryTicketServiceImpl 的籤位制邏輯遷移至 TicketDrawStrategy。

- [ ] T013 遷移 `LotteryTicketServiceImpl.drawByTicketNumber()` 的核心邏輯到 `TicketDrawStrategy.execute()`：
  - 驗證 ticketNumber 存在且 status=AVAILABLE
  - 更新 ticket status → DRAWN
  - 分配獎品
  - 記錄 LotteryDrawRecord
  - Session 管理（開套/加入）
- [ ] T014 在 TicketDrawStrategy 中呼叫 `lotteryService.checkAndDelist(lotteryId)`

**檢查點**：一番賞/卡牌透過統一入口抽獎正常

---

## 第五階段：ScratchDrawStrategy 實作

**目的**：將刮刮樂邏輯遷移至 ScratchDrawStrategy，含免單機制。

- [ ] T015 遷移刮刮樂抽獎邏輯到 `ScratchDrawStrategy.execute()`：
  - 雙號碼機制（ticketNumber / revealedNumber）
  - SCRATCH_PLAYER 的 designation 檢查（checkDesignationRequired）
  - Session 管理
- [ ] T016 實作免單邏輯（在 ScratchDrawStrategy 或獨立 Service 中）：
  - 條件 1：商品 category=CUSTOM_GACHA + SCRATCH_MODE
  - 條件 2：freeDrawEnabled=1（sessionLevel）
  - 條件 3：是開套者（isOpener）
  - 條件 4：尚未觸發過免單（freeDrawTriggered=0）
  - 條件 5：開套者抽數 ≤ freeDrawThreshold（從 Lottery 讀取，非 protectionDraws）
  - 條件 6：抽中大獎（isGrandPrize=1）
  - 條件 7：保護時間未過期
  - 觸發動作：退還 openerTotalCost（幣種依 paymentType），商品自動下架
- [ ] T017 在 ScratchDrawStrategy 中免單觸發後呼叫 `lotteryService.checkAndDelist(lotteryId)`（GRAND_PRIZE_DRAWN → 自動下架）

**檢查點**：刮刮樂全流程正常，含免單觸發和自動下架

---

## 第六階段：保護時間重構

**目的**：將 hardcode 保護時間改為讀 system_config，實作延長邏輯。

- [ ] T018 重構保護時間建立邏輯：
  - 初始時間 = `systemConfigService.getInt("protection_initial_minutes", 5)`
  - 延長時間 = `systemConfigService.getInt("protection_extension_minutes", 2)`
  - 最大時間 = `systemConfigService.getInt("protection_max_minutes", 10)`
- [ ] T019 實作延長邏輯：
  - 在 DrawController 的 draw 端點中，若玩家已有保護鎖，延長 extension_minutes
  - currentEndTime + extension = newEndTime
  - 若 newEndTime > protectionStartTime + max_minutes → 不再延長
  - 多抽（count=10）視為一次操作，只延長一次
- [ ] T020 更新 `LockStatusRes` 新增 `protectionStartTime`、`maxProtectionEndTime` 欄位

**檢查點**：保護時間初始/延長/封頂正確

---

## 第七階段：清理與驗證

- [ ] T021 [P] 確認 `DrawServiceImpl` 可標記為 @Deprecated 或刪除（邏輯已遷移至 GachaDrawStrategy）
- [ ] T022 [P] 確認 `LotteryTicketServiceImpl` 的 draw 方法標記為 @Deprecated 或刪除（邏輯已遷移至 Strategy）
- [ ] T023 更新 SecurityConfig：移除 `/api/lottery/random/**` 路由規則（已不存在）
- [ ] T024 `mvn clean package -DskipTests` 確認編譯通過
- [ ] T025 按功能整合測試：
  - 扭蛋單抽 / 多抽
  - 一番賞選號抽
  - 卡牌選號抽
  - 刮刮樂選號抽 + 免單觸發
  - 保護時間延長
  - 並發鎖定拒絕

---

## 依賴關係

```
前置依賴：
  Spec 016（system_config 表 + SystemConfigService）
  Spec 017（GameModeEnum、PaymentTypeEnum、DelistStrategyEnum）
  Spec 018（CoinService — 統一扣款）
  Spec 019（paymentType/freeDrawThreshold/delistStrategy 欄位 + checkAndDelist()）

內部依賴：
  第一階段 → 第二~五階段（Strategy 基礎就緒後方可填充邏輯）
  第六階段 ← 第一階段（需 SystemConfigService，可與三~五平行）
  第七階段 ← 全部完成
```
