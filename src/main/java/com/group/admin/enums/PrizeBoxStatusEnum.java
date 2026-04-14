package com.group.admin.enums;

import lombok.Getter;

/**
 * 賞品盒狀態枚舉
 * 
 * @author Kuji Admin
 * @since 2026-01-09
 */
@Getter
public enum PrizeBoxStatusEnum {
    
    IN_BOX("IN_BOX", "在獎品盒中"),
    SHIPPED("SHIPPED", "已出貨"),
    RECYCLED("RECYCLED", "已回收");
    
    private final String code;
    private final String name;
    
    PrizeBoxStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    /**
     * 根據 code 取得對應的枚舉
     */
    public static PrizeBoxStatusEnum fromCode(String code) {
        for (PrizeBoxStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("無效的獎品盒狀態：" + code);
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
        for (PrizeBoxStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }
}
