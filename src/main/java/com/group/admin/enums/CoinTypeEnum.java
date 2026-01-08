package com.group.admin.enums;

import lombok.Getter;

/**
 * 點數類型枚舉
 * 
 * @author Kuji Admin
 * @since 2026-01-09
 */
@Getter
public enum CoinTypeEnum {
    
    GOLD("GOLD", "金幣"),
    BONUS("BONUS", "紅利");
    
    private final String code;
    private final String name;
    
    CoinTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    /**
     * 根據 code 取得對應的枚舉
     */
    public static CoinTypeEnum fromCode(String code) {
        for (CoinTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("無效的點數類型：" + code);
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
        for (CoinTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }
}
