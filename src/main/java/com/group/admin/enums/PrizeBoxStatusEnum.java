package com.group.admin.enums;

import lombok.Getter;

/**
 * 賞品盒狀態枚舉
 *
 * @author Kuji Admin
 * @since 2026-01-09
 */
@Getter
public enum PrizeBoxStatusEnum implements DisplayableEnum {

    IN_BOX("IN_BOX", "在賞品盒中"),
    SHIPPED("SHIPPED", "已出貨"),
    RECYCLED("RECYCLED", "已回收");

    private final String code;
    private final String name;

    PrizeBoxStatusEnum(String code, String name) {
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

    public static PrizeBoxStatusEnum fromCode(String code) {
        for (PrizeBoxStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("無效的賞品盒狀態：" + code);
    }

    public static String getNameByCode(String code) {
        try {
            return fromCode(code).getName();
        } catch (IllegalArgumentException e) {
            return code;
        }
    }

    public static boolean isValid(String code) {
        for (PrizeBoxStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }
}
