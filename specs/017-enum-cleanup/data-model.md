# Data Model: 列舉統一與型別清理

**Feature**: `017-enum-cleanup`
**Date**: 2026-04-13

> 無新資料表。本文件定義合併後各 Enum 的欄位結構與 DB 遷移。

---

## Enum: CoinTypeEnum (合併後)

**保留檔案**: `enums/CoinTypeEnum.java`
**合併來源**: `PointType.java`（刪除）

```java
@Getter @RequiredArgsConstructor
public enum CoinTypeEnum implements DisplayableEnum {
    GOLD("GOLD", "金幣"),
    BONUS("BONUS", "紅利");

    private final String code;
    private final String displayName;

    public boolean isGold() { return this == GOLD; }
    public boolean isBonus() { return this == BONUS; }

    public static CoinTypeEnum fromCode(String code) {
        for (CoinTypeEnum e : values()) {
            if (e.code.equalsIgnoreCase(code)) return e;
        }
        throw new IllegalArgumentException("Unknown CoinType: " + code);
    }
}
```

---

## Enum: PrizeLevelEnum (合併後)

**保留檔案**: `enums/PrizeLevelEnum.java`
**合併來源**: `PrizeLevel.java`（刪除）

```java
@Getter @RequiredArgsConstructor
public enum PrizeLevelEnum implements DisplayableEnum {
    A("A", "A賞", 1),
    B("B", "B賞", 2),
    C("C", "C賞", 3),
    D("D", "D賞", 4),
    E("E", "E賞", 5),
    F("F", "F賞", 6),
    G("G", "G賞", 7),
    LAST("LAST", "最後賞", 98),
    GRAND("GRAND", "大賞", 99);

    private final String code;
    private final String displayName;
    private final int sortOrder;

    public boolean isGrandPrize() { return this == GRAND; }
    public boolean isSpecialPrize() { return this == LAST || this == GRAND; }
}
```

---

## Enum: TransactionTypeEnum (合併後)

**保留檔案**: `enums/TransactionTypeEnum.java`
**合併來源**: `PointOperationType.java`（刪除）

```java
@Getter @RequiredArgsConstructor
public enum TransactionTypeEnum implements DisplayableEnum {
    RECHARGE("RECHARGE", "儲值", true),
    DRAW("DRAW", "抽獎消費", false),
    RECYCLE("RECYCLE", "獎品回收", true),
    REFUND("REFUND", "退款", true),
    ADMIN_ADJUST("ADMIN_ADJUST", "系統調整", true),
    BONUS_GRANT("BONUS_GRANT", "紅利贈送", true),
    BONUS_EXPIRE("BONUS_EXPIRE", "紅利過期", false),
    FREE_DRAW_REFUND("FREE_DRAW_REFUND", "免單退款", true);

    private final String code;
    private final String displayName;
    private final boolean isIncrease;

    public boolean isDecrease() { return !isIncrease; }
}
```

---

## Enum: GameModeEnum (新建)

```java
@Getter @RequiredArgsConstructor
public enum GameModeEnum implements DisplayableEnum {
    TICKET("TICKET", "籤位制"),
    RANDOM("RANDOM", "隨機抽獎"),
    SCRATCH_STORE("SCRATCH_STORE", "店家指定大獎"),
    SCRATCH_PLAYER("SCRATCH_PLAYER", "玩家指定大獎");

    private final String code;
    private final String displayName;
}
```

### Category → GameMode 對應規則

| Category | 允許的 GameMode | 預設值（建立時自動帶入） |
|----------|----------------|------------------------|
| OFFICIAL_ICHIBAN | TICKET | TICKET（自動） |
| TRADING_CARD | TICKET | TICKET（自動） |
| GACHA | RANDOM | RANDOM（自動） |
| CUSTOM_GACHA (SCRATCH_MODE) | SCRATCH_STORE, SCRATCH_PLAYER, RANDOM | 必填（店家選擇） |

---

## Enum: PaymentTypeEnum (新建)

```java
@Getter @RequiredArgsConstructor
public enum PaymentTypeEnum implements DisplayableEnum {
    GOLD("GOLD", "金幣"),
    BONUS("BONUS", "紅利");

    private final String code;
    private final String displayName;
}
```

---

## Enum: DelistStrategyEnum (新建)

```java
@Getter @RequiredArgsConstructor
public enum DelistStrategyEnum implements DisplayableEnum {
    GRAND_PRIZE_DRAWN("GRAND_PRIZE_DRAWN", "大獎抽走後下架"),
    ALL_DRAWN("ALL_DRAWN", "全部抽完後下架"),
    MANUAL("MANUAL", "手動下架");

    private final String code;
    private final String displayName;
}
```

### Category → DelistStrategy 對應規則

| Category | 允許的策略 | 預設值 |
|----------|-----------|--------|
| CUSTOM_GACHA (SCRATCH_MODE) | GRAND_PRIZE_DRAWN 固定 | GRAND_PRIZE_DRAWN |
| GACHA | ALL_DRAWN 固定 | ALL_DRAWN |
| TRADING_CARD | ALL_DRAWN 固定 | ALL_DRAWN |
| OFFICIAL_ICHIBAN | GRAND_PRIZE_DRAWN / ALL_DRAWN / MANUAL | 店家自選 |

---

## DB 遷移

```sql
-- V017__enum_cleanup.sql

-- 統一 coin_type 為 UPPERCASE
UPDATE wallet_transaction
SET coin_type = UPPER(coin_type)
WHERE coin_type IN ('gold', 'bonus');

-- 統一 transaction_type 為 UPPERCASE（若有小寫值）
UPDATE wallet_transaction
SET transaction_type = UPPER(transaction_type)
WHERE transaction_type != UPPER(transaction_type);
```
