package com.group.admin.enums;

import lombok.Getter;

/**
 * 配送方式枚舉
 * 
 * @author Kuji Admin
 * @since 2026-01-09
 */
@Getter
public enum ShippingMethodEnum {
    
    HOME_DELIVERY("HOME_DELIVERY", "宅配到府"),
    SEVEN_ELEVEN("SEVEN_ELEVEN", "7-11 取貨"),
    FAMILY_MART("FAMILY_MART", "全家取貨");
    
    private final String code;
    private final String name;
    
    ShippingMethodEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    /**
     * 根據 code 取得對應的枚舉
     */
    public static ShippingMethodEnum fromCode(String code) {
        for (ShippingMethodEnum method : values()) {
            if (method.getCode().equals(code)) {
                return method;
            }
        }
        throw new IllegalArgumentException("無效的配送方式：" + code);
    }
    
    /**
     * 根據 code 取得對應的中文名稱
     */
    public static String getNameByCode(String code) {
        try {
            return fromCode(code).getName();
        } catch (IllegalArgumentException e) {
            return code;
        }
    }
    
    /**
     * 檢查 code 是否有效
     */
    public static boolean isValid(String code) {
        for (ShippingMethodEnum method : values()) {
            if (method.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }
}
