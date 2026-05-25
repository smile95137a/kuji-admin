package com.group.admin.enums;

import lombok.Getter;

@Getter
public enum PrizeBoxStatusEnum implements DisplayableEnum {

    IN_BOX("IN_BOX", "在賞品盒中"),
    SHIPPING("SHIPPING", "配送中"),
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
        throw new IllegalArgumentException("未知的賞品盒狀態: " + code);
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
