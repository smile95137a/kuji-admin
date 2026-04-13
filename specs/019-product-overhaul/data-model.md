# Data Model: 商品管理重整

**Feature**: `019-product-overhaul`
**Date**: 2026-04-13

---

## DDL 變更

### 新增欄位

```sql
ALTER TABLE `lottery`
  ADD COLUMN `payment_type` VARCHAR(20) NOT NULL DEFAULT 'GOLD' COMMENT '支付方式：GOLD/BONUS' AFTER `game_mode`,
  ADD COLUMN `free_draw_threshold` INT NULL COMMENT '免單門檻抽數（僅刮刮樂，店家設定）' AFTER `payment_type`,
  ADD COLUMN `delist_strategy` VARCHAR(30) NOT NULL DEFAULT 'MANUAL' COMMENT '下架策略：GRAND_PRIZE_DRAWN/ALL_DRAWN/MANUAL' AFTER `free_draw_threshold`;
```

### 移除（標記廢棄）欄位

```sql
-- 先設為可 NULL（向下相容），後續版本再 DROP
ALTER TABLE `lottery`
  MODIFY COLUMN `multi_draw_options` VARCHAR(100) NULL COMMENT '已廢棄',
  MODIFY COLUMN `allow_multi_draw` TINYINT NULL COMMENT '已廢棄',
  MODIFY COLUMN `protection_draws` INT NULL COMMENT '已廢棄，改用 system_config',
  MODIFY COLUMN `protection_minutes` INT NULL COMMENT '已廢棄，改用 system_config';
```

### 歷史資料遷移

```sql
-- 為現有商品設定預設值
UPDATE `lottery` SET `payment_type` = 'GOLD' WHERE `payment_type` IS NULL OR `payment_type` = '';
UPDATE `lottery` SET `delist_strategy` = 'MANUAL' WHERE `delist_strategy` IS NULL OR `delist_strategy` = '';

-- 刮刮樂商品若無 free_draw_threshold，設為 3（保守預設）
UPDATE `lottery` SET `free_draw_threshold` = 3
WHERE `category` = 'CUSTOM_GACHA' AND `sub_category` = 'SCRATCH_MODE' AND `free_draw_threshold` IS NULL;
```

---

## LotteryCreateReq 欄位調整

### 必填欄位

| 欄位 | 型別 | Validation | 說明 |
|------|------|------------|------|
| `title` | String | `@NotBlank @Size(max=255)` | 商品名稱 |
| `category` | String | `@NotBlank` | 分類（LotteryCategoryEnum） |
| `pricePerDraw` | Long | `@NotNull @Min(1)` | 單抽價格 |
| `imageUrl` | String | `@NotBlank` | 商品主圖 |
| `paymentType` | String | `@NotBlank` 預設 GOLD | 支付方式（PaymentTypeEnum） |

### 條件必填

| 欄位 | 條件 | 說明 |
|------|------|------|
| `gameMode` | category=CUSTOM_GACHA 時必填 | 刮刮樂遊戲模式 |
| `freeDrawThreshold` | category=CUSTOM_GACHA + SCRATCH_MODE 時必填 | 免單門檻 `@Min(1)` |
| `delistStrategy` | category=OFFICIAL_ICHIBAN 時必填 | 一番賞下架策略 |
| `subCategory` | category=CUSTOM_GACHA 時必填 | LOTTERY_MODE / SCRATCH_MODE |

### 選填

| 欄位 | 型別 | 說明 |
|------|------|------|
| `description` | String | 商品說明 |
| `galleryImages` | List\<String\> | 多圖 |
| `theme` | String | 主題 |
| `tags` | List\<String\> | 標籤 |
| `scheduledAt` | LocalDateTime | 排程上架時間 |
| `startTime` | LocalDateTime | 抽獎開始時間 |
| `endTime` | LocalDateTime | 結束時間 |
| `remark` | String | 備註 |
| `content` | String | 詳細內容（HTML） |
| `bonusEnabled` | Boolean | 是否啟用紅利回饋 |
| `bonusPointsPerDraw` | Integer | 每抽回饋紅利 |
| `discountedPrice` | Long | 折扣價 |
| `autoDiscountEnabled` | Boolean | 自動降價 |

### 已移除（不再接受）

| 欄位 | 原因 |
|------|------|
| `multiDrawOptions` | 所有商品預設可多抽，上限由 system_config |
| `allowMultiDraw` | 同上 |
| `protectionDraws` | 免單門檻改用 `freeDrawThreshold`；保護相關改用 system_config |
| `protectionMinutes` | 改用 system_config |

---

## Service 層自動帶入邏輯

### resolveGameMode()

```java
private String resolveGameMode(LotteryCreateReq req) {
    return switch (LotteryCategoryEnum.fromCode(req.getCategory())) {
        case OFFICIAL_ICHIBAN, TRADING_CARD -> GameModeEnum.TICKET.getCode();
        case GACHA -> GameModeEnum.RANDOM.getCode();
        case CUSTOM_GACHA -> {
            if (req.getGameMode() == null || req.getGameMode().isBlank()) {
                throw new BusinessException("刮刮樂商品必須選擇遊戲模式");
            }
            yield req.getGameMode();
        }
    };
}
```

### resolveDelistStrategy()

```java
private String resolveDelistStrategy(LotteryCreateReq req) {
    return switch (LotteryCategoryEnum.fromCode(req.getCategory())) {
        case GACHA, TRADING_CARD -> DelistStrategyEnum.ALL_DRAWN.getCode();
        case CUSTOM_GACHA -> DelistStrategyEnum.GRAND_PRIZE_DRAWN.getCode();
        case OFFICIAL_ICHIBAN -> {
            if (req.getDelistStrategy() == null || req.getDelistStrategy().isBlank()) {
                throw new BusinessException("一番賞商品必須選擇下架策略");
            }
            yield req.getDelistStrategy();
        }
    };
}
```

---

## Controller 合併計畫

### 後台（合併為 1 個）

| 原 Controller | 原 API | 移至 |
|---------------|--------|------|
| `AdminLotteryController` | 現有所有 | 保留 |
| `AdminLotteryWithPrizesController` | `/admin/lottery/*/prizes` | 合併到 AdminLotteryController |
| `AdminLotterySessionController` | `/admin/lottery/*/sessions` | 合併到 AdminLotteryController |

### 前台（合併為 1 個）

| 原 Controller | 原 API | 移至 |
|---------------|--------|------|
| `LotteryController` | 現有所有 | 保留 |
| `LotteryDetailController` | `/api/lottery/{id}/detail` | 合併到 LotteryController |
