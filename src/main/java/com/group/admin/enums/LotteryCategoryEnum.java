package com.group.admin.enums;

import lombok.Getter;

/**
 * 商品分類枚舉
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Getter
public enum LotteryCategoryEnum implements DisplayableEnum {

    OFFICIAL_ICHIBAN("OFFICIAL_ICHIBAN", "官方一番賞"),
    GACHA("GACHA", "扭蛋"),
    TRADING_CARD("TRADING_CARD", "卡牌"),
    CUSTOM_GACHA("CUSTOM_GACHA", "自製賞");

    private final String code;
    private final String name;

    LotteryCategoryEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getDisplayName() {
        return this.name;
    }

    public static LotteryCategoryEnum fromCode(String code) {
        if (code == null) return null;
        for (LotteryCategoryEnum e : values()) {
            if (e.code.equalsIgnoreCase(code)) {
                return e;
            }
        }
        return null;
    }

    public static String getNameByCode(String code) {
        LotteryCategoryEnum e = fromCode(code);
        return e != null ? e.name : code;
    }
}