# 功能規格書：列舉統一與型別清理

**功能分支**：`017-enum-cleanup`
**建立日期**：2026-04-13
**狀態**：草稿
**輸入**：21 個現有 Enum、重複 / 缺失分析、前端統一回傳格式需求

## 使用者情境與測試

### 使用者故事 1 — 開發者消除重複 Enum 避免維護混亂（優先級：P1）

身為開發者，我希望每個業務概念只對應一個 Enum，避免同一個概念（如金幣類型）有兩個不同的 Enum（PointType vs CoinTypeEnum），導致 code 大小寫不一致和引用混亂。

**此優先級的原因**：重複 Enum 是技術債的根源，影響所有模組的維護與擴展。

**獨立測試**：編譯通過且全專案 grep 不到被移除 Enum 的引用；前端 API 回傳的 Enum 欄位統一為 `{ code, displayName }` 格式。

**驗收情境**：

1. **在** 合併 PointType 和 CoinTypeEnum 後，**當** 搜尋整個專案的 `PointType` 引用，**則** 只有合併後的 `CoinTypeEnum` 存在。
2. **在** 合併 PrizeLevel 和 PrizeLevelEnum 後，**當** 所有 API 回傳 prizeLevel 欄位，**則** 統一為 `{ code: "A", displayName: "A賞" }` 格式。
3. **在** 新建 GameModeEnum 後，**當** 搜尋專案中 "SCRATCH_STORE" 等 raw string，**則** 全部替換為 `GameModeEnum.SCRATCH_STORE`。

---

### 使用者故事 2 — 前端統一接收 Enum 格式（優先級：P1）

身為前端開發者，我希望後端所有 Enum 回傳給前端時，格式統一為 `{ code: "XXX", displayName: "中文名稱" }`，讓前端可以通用渲染。

**此優先級的原因**：前端需要統一的 Enum 渲染邏輯，目前混用 code/name/displayName 三種命名。

**驗收情境**：

1. **在** 所有 Enum 均實作 `getCode()/getDisplayName()` 的情況下，**當** API 回傳含 Enum 欄位，**則** JSON 格式為 `{ "code": "GOLD", "displayName": "金幣" }`。
2. **在** 前端需要列出所有選項時，**當** 呼叫 `GET /api/enums/{enumType}`，**則** 回傳該 Enum 所有值的 list。

---

### 邊界情況

- 合併 Enum 後 DB 中 raw string 值不一致（如 `"gold"` vs `"GOLD"`）？需同步更新 DB 資料遷移腳本。
- 新增 Enum 值後舊版前端不認識？前端應使用 `code` 做 key，不認識的 code 顯示原始值即可。

## 需求規格

### 功能需求

#### FR-001：合併重複 Enum

| 保留的 Enum | 移除的 Enum | 合併策略 |
|-------------|-------------|----------|
| `CoinTypeEnum` | `PointType` | 統一使用 UPPERCASE code（`GOLD`/`BONUS`），保留 `isGold()`/`isBonus()` 方法 |
| `PrizeLevelEnum` | `PrizeLevel` | 合併兩者功能：保留 `PrizeLevelEnum` 名稱，加入 `sortOrder`、`isGrandPrize()`、`isSpecialPrize()` |
| `TransactionTypeEnum` | `PointOperationType` | 合併為 `TransactionTypeEnum`，加入 `isIncrease` 欄位，補充 `BONUS_GRANT`/`BONUS_EXPIRE`/`FREE_DRAW_REFUND` |

#### FR-002：新建缺失 Enum

| 新 Enum | 值 | 用途 |
|---------|-----|------|
| `GameModeEnum` | `TICKET`, `RANDOM`, `SCRATCH_STORE`, `SCRATCH_PLAYER` | 遊戲模式。一番賞/卡牌=TICKET，扭蛋=RANDOM，刮刮樂=SCRATCH_STORE/SCRATCH_PLAYER/RANDOM |
| `PaymentTypeEnum` | `GOLD`, `BONUS` | 商品支付方式。建立商品時選定，抽獎時依此扣款 |
| `DelistStrategyEnum` | `GRAND_PRIZE_DRAWN`, `ALL_DRAWN`, `MANUAL` | 下架策略。一番賞店家自選；刮刮樂固定 GRAND_PRIZE_DRAWN；扭蛋/卡牌固定 ALL_DRAWN |

#### FR-003：移除待廢棄 Enum / 調整既有 Enum

| Enum | 動作 | 說明 |
|------|------|------|
| `ShippingMethodEnum` | **移除** | 改為 DB 表管理（見 Spec 021） |

#### FR-004：統一 Enum 回傳格式

所有 Enum 必須實作統一介面（或共同方法）：

```java
public interface DisplayableEnum {
    String getCode();
    String getDisplayName();
}
```

所有 Res DTO 中的 Enum 欄位，序列化時統一回傳 `{ code, displayName }`。

#### FR-005：Enum 查詢 API

新增通用端點：`GET /api/enums/{enumType}`，回傳指定 Enum 的所有值。

支援的 enumType：`coinType`, `prizeLevel`, `lotteryCategory`, `lotteryStatus`, `gameMode`, `paymentType`, `delistStrategy`, `orderStatus`, `prizeBoxStatus`, `paymentStatus`。

### 核心實體

無新增資料表。純粹重構程式碼層級的 Enum 定義。

### DB 資料遷移

若 DB 中存在小寫的 `"gold"` / `"bonus"`（來自 PointType），需更新為大寫 `"GOLD"` / `"BONUS"`：

```sql
-- wallet_transaction
UPDATE wallet_transaction SET coin_type = UPPER(coin_type) WHERE coin_type IN ('gold', 'bonus');

-- 其他可能使用 PointType 的表...
```

## 成功標準

### 可量化的成果

- **SC-001**：`mvn clean package -DskipTests` 編譯通過，零引用被移除的 Enum。
- **SC-002**：`grep -r "PointType" src/` 回傳 0 結果（合併後）。
- **SC-003**：`grep -r "PrizeLevel\b" src/` 僅回傳 `PrizeLevelEnum` 引用（合併後）。
- **SC-004**：所有 API 中 Enum 欄位回傳結構皆為 `{ code, displayName }`。
- **SC-005**：全專案不再有 raw string `"SCRATCH_STORE"` / `"SCRATCH_PLAYER"`，改用 `GameModeEnum`。

## 假設前提

- 前端能接受 Enum 回傳格式從純字串改為 `{ code, displayName }` 物件（需前端配合調整）。
- DB 中 Enum 值統一使用 UPPERCASE。
- `ShippingMethodEnum` 移除後，shipping method 改由 DB 表管理（Spec 021 處理）。
