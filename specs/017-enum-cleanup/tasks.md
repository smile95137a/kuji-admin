# 任務清單：列舉統一與型別清理

**輸入**：設計文件來自 `/specs/017-enum-cleanup/`
**分支**：`017-enum-cleanup` | **建立日期**：2026-04-13

---

## 第一階段：統一介面與新建 Enum

**目的**：建立 `DisplayableEnum` 統一介面，新建缺失的 Enum。

- [X] T001 建立 `src/main/java/com/group/admin/enums/DisplayableEnum.java`（介面，方法：`getCode()`, `getDisplayName()`）
- [X] T002 [P] 建立 `src/main/java/com/group/admin/enums/GameModeEnum.java`（TICKET/RANDOM/SCRATCH_STORE/SCRATCH_PLAYER，實作 DisplayableEnum）
- [X] T003 [P] 建立 `src/main/java/com/group/admin/enums/PaymentTypeEnum.java`（GOLD/BONUS，實作 DisplayableEnum）
- [X] T004 [P] 建立 `src/main/java/com/group/admin/enums/DelistStrategyEnum.java`（GRAND_PRIZE_DRAWN/ALL_DRAWN/MANUAL，實作 DisplayableEnum）

**檢查點**：新 Enum 就緒，編譯通過

---

## 第二階段：合併重複 Enum

**目的**：將三對重複 Enum 合併，消除冗餘。每對需同時完成：修改保留的 Enum + 更新所有引用 + 刪除被移除的 Enum。

- [ ] T005 **合併 CoinTypeEnum ← PointType**：
  - 在 `CoinTypeEnum` 加入 `isGold()`, `isBonus()` 方法，統一 code 為 UPPERCASE
  - 用 `list_code_usages` 找出所有 `PointType` 引用 → 逐一改為 `CoinTypeEnum`
  - 更新 DB 中 `coin_type` 欄位值為 UPPERCASE（遷移 SQL）
  - 刪除 `PointType.java`
- [ ] T006 **合併 PrizeLevelEnum ← PrizeLevel**：
  - 在 `PrizeLevelEnum` 加入 `sortOrder` 欄位、`isGrandPrize()`, `isSpecialPrize()` 方法
  - 用 `list_code_usages` 找出所有 `PrizeLevel` 引用 → 逐一改為 `PrizeLevelEnum`
  - 刪除 `PrizeLevel.java`
- [ ] T007 **合併 TransactionTypeEnum ← PointOperationType**：
  - 在 `TransactionTypeEnum` 加入 `isIncrease` 欄位，補充 `BONUS_GRANT`, `BONUS_EXPIRE`, `FREE_DRAW_REFUND` 值
  - 用 `list_code_usages` 找出所有 `PointOperationType` 引用 → 逐一改為 `TransactionTypeEnum`
  - 刪除 `PointOperationType.java`

**檢查點**：`grep -r "PointType\|PrizeLevel[^E]" src/` 回傳 0 結果

---

## 第三階段：替換 Raw String 為 Enum

**目的**：將散落在程式碼中的 gameMode raw string 替換為 `GameModeEnum`。

- [X] T008 [P] 掃描並替換 `LotteryTicketServiceImpl` 中所有 `"SCRATCH_STORE"`, `"SCRATCH_PLAYER"`, `"RANDOM"` → `GameModeEnum.SCRATCH_STORE.getCode()` 等
- [X] T009 [P] 掃描並替換 `LotteryServiceImpl` 中所有 gameMode raw string → `GameModeEnum`
- [X] T010 [P] 掃描並替換 `LotteryDrawController` 中所有 gameMode raw string → `GameModeEnum`
- [ ] T011 [P] 掃描並替換 `LotteryCreateReq` / `LotteryRes` 中 gameMode 欄位，加入 `GameModeEnum` 驗證

**檢查點**：`grep -r '"SCRATCH_STORE"\|"SCRATCH_PLAYER"' src/` 回傳 0 結果（排除 Enum 定義本身）

---

## 第四階段：讓既有 Enum 實作統一介面

**目的**：讓所有業務 Enum 實作 `DisplayableEnum`，統一 code/displayName 命名。

- [ ] T012 [P] 修改 `CoinTypeEnum` 實作 `DisplayableEnum`（`name` → `displayName`）
- [ ] T013 [P] 修改 `PrizeLevelEnum` 實作 `DisplayableEnum`（`name` → `displayName`）
- [ ] T014 [P] 修改 `TransactionTypeEnum` 實作 `DisplayableEnum`（`name` → `displayName`）
- [ ] T015 [P] 修改 `LotteryCategoryEnum` 實作 `DisplayableEnum`
- [ ] T016 [P] 修改 `LotteryStatusEnum` 實作 `DisplayableEnum`
- [ ] T017 [P] 修改 `OrderStatusEnum` 實作 `DisplayableEnum`
- [ ] T018 [P] 修改 `PrizeBoxStatusEnum` 實作 `DisplayableEnum`
- [ ] T019 [P] 修改 `PaymentStatusEnum` 實作 `DisplayableEnum`

**檢查點**：所有業務 Enum 有統一的 `getCode()` 和 `getDisplayName()` 方法

---

## 第五階段：Enum API 與 ShippingMethodEnum 移除

- [ ] T020 建立 `src/main/java/com/group/admin/controller/api/EnumController.java`：
  - `GET /api/enums/{enumType}` — 回傳指定 Enum 的所有 `{ code, displayName }` list
  - 支援：coinType, prizeLevel, lotteryCategory, lotteryStatus, gameMode, paymentType, delistStrategy, orderStatus, prizeBoxStatus, paymentStatus
- [ ] T021 移除 `ShippingMethodEnum.java`，更新所有引用（改為讀 DB shipping_method 表，見 Spec 021）

**檢查點**：`mvn clean package -DskipTests` 編譯通過；Enum API 正常回傳

---

## 第六階段：DB 資料遷移

- [ ] T022 撰寫 SQL 遷移腳本 `src/main/resources/db/migration/V017__enum_cleanup.sql`：
  - `UPDATE wallet_transaction SET coin_type = UPPER(coin_type) WHERE coin_type IN ('gold', 'bonus')`
  - 其他使用小寫 point type 的表（若有）

**檢查點**：DB 中所有 Enum 值統一為 UPPERCASE

---

## 依賴關係

```
第一階段（介面 + 新 Enum）  — 無依賴
第二階段（合併重複）        — 依賴第一階段
第三階段（替換 raw string）  — 依賴第一階段（需要 GameModeEnum）
第四階段（實作統一介面）     — 依賴第一階段 + 第二階段
第五階段（API + 移除）       — 依賴第四階段
第六階段（DB 遷移）          — 第二階段完成後即可開始
```
