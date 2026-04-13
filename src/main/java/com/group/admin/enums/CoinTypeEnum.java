package com.group.admin.enums;

import lombok.Getter;

/**
 * 點數類型枚舉
 *
 * @author Kuji Admin
 * @since 2026-01-09
 */
@Getter
public enum CoinTypeEnum implements DisplayableEnum {

    GOLD("GOLD", "金幣"),
    BONUS("BONUS", "紅利");

    private final String code;
    private final String name;

    CoinTypeEnum(String code, String name) {
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

    public boolean isGold() {
        return this == GOLD;
    }

    public boolean isBonus() {
        return this == BONUS;
    }

    public static CoinTypeEnum fromCode(String code) {
        for (CoinTypeEnum type : values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("無效的點數類型：" + code);
    }

    public static String getNameByCode(String code) {
        try {
            return fromCode(code).getName();
        } catch (IllegalArgumentException e) {
            return code;
        }
    }

    public static boolean isValid(String code) {
        for (CoinTypeEnum type : values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return true;
            }
        }
        return false;
    }
}

