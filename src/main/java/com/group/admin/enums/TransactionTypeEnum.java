package com.group.admin.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 交易類型枚舉
 *
 * @author Kuji Admin
 * @since 2026-01-09
 */
@Getter
public enum TransactionTypeEnum implements DisplayableEnum {

    RECHARGE("RECHARGE", "儲值", true),
    DRAW("DRAW", "抽獎消費", false),
    RECYCLE("RECYCLE", "獎品回收", true),
    REFUND("REFUND", "退款", true),
    ADMIN_ADJUST("ADMIN_ADJUST", "系統調整", true),
    BONUS_GRANT("BONUS_GRANT", "紅利贈送", true),
    BONUS_EXPIRE("BONUS_EXPIRE", "紅利過期", false),
    FREE_DRAW_REFUND("FREE_DRAW_REFUND", "免費抽退款", true);

    private final String code;
    private final String name;
    private final boolean isIncrease;

    TransactionTypeEnum(String code, String name, boolean isIncrease) {
        this.code = code;
        this.name = name;
        this.isIncrease = isIncrease;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getDisplayName() {
        return this.name;
    }

    /**
     * 根據 code 取得對應的枚舉
     */
    public static TransactionTypeEnum fromCode(String code) {
        for (TransactionTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("無效的交易類型：" + code);
    }

    public static String getNameByCode(String code) {
        try {
            return fromCode(code).getName();
        } catch (IllegalArgumentException e) {
            return code;
        }
    }

    public static boolean isValid(String code) {
        for (TransactionTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }
}
