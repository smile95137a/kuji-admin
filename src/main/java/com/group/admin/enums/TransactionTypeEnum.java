package com.group.admin.enums;

import lombok.Getter;

/**
 * 交易類型枚舉
 * 
 * @author Kuji Admin
 * @since 2026-01-09
 */
@Getter
public enum TransactionTypeEnum {
    
    RECHARGE("RECHARGE", "儲值"),
    BONUS_GRANT("BONUS_GRANT", "紅利贈送"),
    DRAW("DRAW", "抽獎消費"),
    RECYCLE("RECYCLE", "獎品回收"),
    REFUND("REFUND", "退款"),
    ADMIN_ADJUST("ADMIN_ADJUST", "系統調整");
    
    private final String code;
    private final String name;
    
    TransactionTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
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
        for (TransactionTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }
}
