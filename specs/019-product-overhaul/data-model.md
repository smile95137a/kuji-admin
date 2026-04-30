# Data Model: 商品管理重整

**Feature**: `019-product-overhaul`  
**Date**: 2026-04-30

---

## 1. 核心聚合：`Lottery`

本次變更集中在 `Lottery` 聚合的商品建立/更新規則，不新增新表，而是調整現有欄位語意與 request/response 正規化。

### 1.1 重要欄位與寫入規則

| 欄位 | DB 欄位 | 型別 | 寫入規則 | 說明 |
|------|---------|------|----------|------|
| `category` | `category` | `VARCHAR(50)` | 建立必填 | 商品主分類：`OFFICIAL_ICHIBAN` / `TRADING_CARD` / `GACHA` / `CUSTOM_GACHA` |
| `subCategory` | `sub_category` | `VARCHAR(50)` | 僅 `CUSTOM_GACHA` 必填 | `LOTTERY_MODE` / `SCRATCH_MODE` |
| `playMode` | `play_mode` | `VARCHAR(50)` | 後端推導，不信任 client 值 | `LOTTERY_MODE` 或 `SCRATCH_MODE` |
| `gameMode` | `game_mode` | `VARCHAR(50)` | 僅 `CUSTOM_GACHA + SCRATCH_MODE` 必填；其他情況忽略 client 值並正規化 | `TICKET` / `RANDOM` / `SCRATCH_STORE` / `SCRATCH_PLAYER` |
| `paymentType` | `payment_type` | `VARCHAR(20)` | 建立時預設 `GOLD`；更新僅 DRAFT 可改 | `GOLD` / `BONUS` |
| `freeDrawThreshold` | `free_draw_threshold` | `INT NULL` | 僅 `CUSTOM_GACHA + SCRATCH_MODE` 可使用；`NULL` 合法；若有值必須 `>= 1` | 店家免費抽/免單機制門檻 |
| `delistStrategy` | `delist_strategy` | `VARCHAR(30)` | 依分類固定或必選；非對應分類的 client 值忽略 | `GRAND_PRIZE_DRAWN` / `ALL_DRAWN` / `MANUAL` |
| `status` | `status` | `VARCHAR(30)` | 維持既有生命週期 | `DRAFT` / `ON_SHELF` / `OFF_SHELF` / `SOLD_OUT` / `ENDED` |

### 1.2 依分類推導規則

| Category | SubCategory | playMode | gameMode | freeDrawThreshold | delistStrategy |
|----------|-------------|----------|----------|-------------------|----------------|
| `OFFICIAL_ICHIBAN` | `NULL` | `LOTTERY_MODE` | `TICKET` | `NULL` | 店家必選：`GRAND_PRIZE_DRAWN` / `ALL_DRAWN` / `MANUAL` |
| `TRADING_CARD` | `NULL` | `LOTTERY_MODE` | `TICKET` | `NULL` | 固定 `ALL_DRAWN` |
| `GACHA` | `NULL` | `LOTTERY_MODE` | `RANDOM` | `NULL` | 固定 `ALL_DRAWN` |
| `CUSTOM_GACHA` | `LOTTERY_MODE` | `LOTTERY_MODE` | `NULL` | `NULL` | 固定 `ALL_DRAWN` |
| `CUSTOM_GACHA` | `SCRATCH_MODE` | `SCRATCH_MODE` | `SCRATCH_STORE` / `SCRATCH_PLAYER` / `RANDOM` | `NULL` 或 `>= 1` | 固定 `GRAND_PRIZE_DRAWN` |

---

## 2. DDL 變更

### 2.1 新增欄位

```sql
ALTER TABLE `lottery`
  ADD COLUMN `payment_type` VARCHAR(20) NOT NULL DEFAULT 'GOLD' COMMENT '支付方式：GOLD/BONUS' AFTER `game_mode`,
  ADD COLUMN `free_draw_threshold` INT NULL COMMENT '免費抽/免單門檻；僅 CUSTOM_GACHA + SCRATCH_MODE 使用' AFTER `payment_type`,
  ADD COLUMN `delist_strategy` VARCHAR(30) NOT NULL DEFAULT 'MANUAL' COMMENT '下架策略：GRAND_PRIZE_DRAWN/ALL_DRAWN/MANUAL' AFTER `free_draw_threshold`;
```

### 2.2 廢棄欄位

```sql
ALTER TABLE `lottery`
  MODIFY COLUMN `multi_draw_options` VARCHAR(100) NULL COMMENT '已廢棄',
  MODIFY COLUMN `allow_multi_draw` TINYINT NULL COMMENT '已廢棄',
  MODIFY COLUMN `protection_draws` INT NULL COMMENT '已廢棄，免費抽改由 free_draw_threshold 管理',
  MODIFY COLUMN `protection_minutes` INT NULL COMMENT '已廢棄，保護期改由 system_config 管理';
```

### 2.3 歷史資料遷移

```sql
UPDATE `lottery`
SET `payment_type` = 'GOLD'
WHERE `payment_type` IS NULL OR `payment_type` = '';

UPDATE `lottery`
SET `delist_strategy` = 'MANUAL'
WHERE `delist_strategy` IS NULL OR `delist_strategy` = '';

-- 歷史刮刮樂商品若未設定免費抽/免單機制，維持 NULL
UPDATE `lottery`
SET `free_draw_threshold` = NULL
WHERE `category` = 'CUSTOM_GACHA'
  AND `sub_category` = 'SCRATCH_MODE'
  AND `free_draw_threshold` IS NULL;
```

> 說明：不再將刮刮樂空值回填為 `3`。`NULL` 具有明確商業語意，代表店家未啟用免費抽/免單機制。

---

## 3. Request Model

### 3.1 `LotteryCreateReq`

| 欄位 | 規則 | 備註 |
|------|------|------|
| `category` | 必填 | 主分類 |
| `subCategory` | `CUSTOM_GACHA` 必填 | `LOTTERY_MODE` / `SCRATCH_MODE` |
| `playMode` | 可傳但忽略，後端重算 | 建議前端不傳 |
| `gameMode` | 僅 `CUSTOM_GACHA + SCRATCH_MODE` 必填 | 其他情況忽略並正規化為 `NULL` |
| `paymentType` | 選填，預設 `GOLD` | 非法值須拒絕 |
| `freeDrawThreshold` | 僅 `CUSTOM_GACHA + SCRATCH_MODE` 可用；`NULL` 合法；若有值則 `>= 1` | 其他情況一律正規化為 `NULL` |
| `delistStrategy` | 僅 `OFFICIAL_ICHIBAN` 必填 | 固定分類若有傳值則忽略並覆寫為固定策略 |
| `multiDrawOptions` / `allowMultiDraw` / `protectionDraws` / `protectionMinutes` | 不再接受 | 若 client 傳入，忽略或於 DTO 層移除 |

### 3.2 `LotteryUpdateReq`

| 欄位 | 規則 | 備註 |
|------|------|------|
| `category` / `subCategory` | 維持既有更新能力，但重算衍生欄位 | 若變更商品型態，需同步重算 `playMode` / `gameMode` / `freeDrawThreshold` / `delistStrategy` |
| `paymentType` | 僅 `DRAFT` 狀態可改 | 非 `DRAFT` 應拒絕或維持原值，依既有 service 規則落實 |
| `freeDrawThreshold` | 規則同 create | 非刮刮樂一律清為 `NULL` |
| `delistStrategy` | 僅 `OFFICIAL_ICHIBAN` 可由店家調整 | 其他分類固定覆寫 |

---

## 4. Derived Rules（Service 正規化）

### 4.1 `resolvePlayMode(category, subCategory)`

```java
switch (category) {
    case "CUSTOM_GACHA" -> return "SCRATCH_MODE".equals(subCategory) ? "SCRATCH_MODE" : "LOTTERY_MODE";
    case "SCRATCH" -> return "SCRATCH_MODE"; // 舊資料相容
    default -> return "LOTTERY_MODE";
}
```

### 4.2 `resolveGameMode(category, subCategory, requestGameMode)`

```java
switch (category) {
    case "OFFICIAL_ICHIBAN", "TRADING_CARD" -> return "TICKET";
    case "GACHA" -> return "RANDOM";
    case "CUSTOM_GACHA" -> {
        if ("SCRATCH_MODE".equals(subCategory)) {
            requireNonBlank(requestGameMode);
            return requestGameMode;
        }
        return null;
    }
    default -> throw new BusinessException("不支援的商品分類");
}
```

### 4.3 `resolveDelistStrategy(category, subCategory, requestDelistStrategy)`

```java
switch (category) {
    case "OFFICIAL_ICHIBAN" -> {
        requireNonBlank(requestDelistStrategy);
        return requestDelistStrategy;
    }
    case "TRADING_CARD", "GACHA" -> return "ALL_DRAWN";
    case "CUSTOM_GACHA" -> {
        return "SCRATCH_MODE".equals(subCategory) ? "GRAND_PRIZE_DRAWN" : "ALL_DRAWN";
    }
    default -> throw new BusinessException("不支援的下架策略分類");
}
```

### 4.4 `normalizeFreeDrawThreshold(category, subCategory, threshold)`

```java
if (!"CUSTOM_GACHA".equals(category) || !"SCRATCH_MODE".equals(subCategory)) {
    return null;
}
if (threshold == null) {
    return null;
}
if (threshold < 1) {
    throw new BusinessException("免費抽門檻必須大於或等於 1");
}
return threshold;
```

---

## 5. Response Model

`LotteryRes` 需維持以下輸出一致性：

| 欄位 | 輸出規則 |
|------|----------|
| `subCategory` | `CUSTOM_GACHA` 顯示實際子分類，其餘可為 `NULL` |
| `playMode` | 後端推導結果 |
| `gameMode` | 僅刮刮樂有值；`CUSTOM_GACHA + LOTTERY_MODE` 應為 `NULL` |
| `paymentType` | 無值時以 `GOLD` 呈現，避免歷史資料空值外漏 |
| `freeDrawThreshold` | 僅 `CUSTOM_GACHA + SCRATCH_MODE` 可能有值；未啟用機制時回傳 `NULL` |
| `delistStrategy` | 顯示實際生效策略，不回傳 client 原始錯誤輸入 |

---

## 6. 狀態與生命週期

### 6.1 下架策略狀態流轉

| 策略 | 觸發條件 | 結果 |
|------|----------|------|
| `GRAND_PRIZE_DRAWN` | 最後一個大獎被抽走 | `status -> ENDED` |
| `ALL_DRAWN` | 所有可抽內容耗盡 | `status -> ENDED` |
| `MANUAL` | 所有內容耗盡但未手動下架 | `status -> SOLD_OUT` |

### 6.2 更新限制

| 條件 | 限制 |
|------|------|
| `status != DRAFT` | 不允許修改 `paymentType` |
| 非 `SCRATCH_MODE` | `freeDrawThreshold` 必須清為 `NULL` |
| 固定策略分類 | `delistStrategy` 由後端覆寫，不接受 client 自訂 |

---

## 7. Controller 收斂對照

### 7.1 後台

| 舊控制器 | 對外路徑 | 新歸屬 |
|----------|----------|--------|
| `AdminLotteryController` | `/admin/lottery/**` | 保留 |
| `AdminLotteryWithPrizesController` | `/admin/lottery-with-prizes/**` | 收斂至 `AdminLotteryController` 的 `/admin/lottery/with-prizes/**` 能力 |

### 7.2 前台

| 現況 | 規劃 |
|------|------|
| `LotteryController` 已承接列表與詳情路徑 | 保持相容，驗證不需再拆出 detail controller |
| 其他 draw 相關 controller | 不在本次收斂範圍，但需確保 `playMode` / `gameMode` 新規則不破壞抽獎流程 |
