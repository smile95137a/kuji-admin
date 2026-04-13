package com.group.admin.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentTypeEnum implements DisplayableEnum {
    GOLD("GOLD", "金幣"),
    BONUS("BONUS", "紅利");

    private final String code;
    private final String displayName;

    public static PaymentTypeEnum fromCode(String code) {
        for (PaymentTypeEnum value : values()) {
            if (value.code.equalsIgnoreCase(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown payment type: " + code);
    }
}
