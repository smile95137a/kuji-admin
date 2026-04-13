package com.group.admin.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DelistStrategyEnum implements DisplayableEnum {
    GRAND_PRIZE_DRAWN("GRAND_PRIZE_DRAWN", "大獎抽完下架"),
    ALL_DRAWN("ALL_DRAWN", "全部抽完下架"),
    MANUAL("MANUAL", "手動下架");

    private final String code;
    private final String displayName;

    public static DelistStrategyEnum fromCode(String code) {
        for (DelistStrategyEnum value : values()) {
            if (value.code.equalsIgnoreCase(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown delist strategy: " + code);
    }
}
