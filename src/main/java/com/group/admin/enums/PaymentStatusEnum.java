package com.group.admin.enums;

import lombok.Getter;

/**
 * 支付狀態枚舉
 * 
 * @author Kuji Admin
 * @since 2026-01-09
 */
@Getter
public enum PaymentStatusEnum {
    
    PENDING("PENDING", "待支付"),
    SUCCESS("SUCCESS", "支付成功"),
    FAILED("FAILED", "支付失敗"),
    CANCELLED("CANCELLED", "已取消");
    
    private final String code;
    private final String name;
    
    PaymentStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    /**
     * 根據 code 取得對應的枚舉
     */
    public static PaymentStatusEnum fromCode(String code) {
        for (PaymentStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("無效的支付狀態：" + code);
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
        for (PaymentStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }
}
