package com.group.admin.enums;

import lombok.Getter;

/**
 * 訂單狀態枚舉
 * 
 * @author Kuji Admin
 * @since 2026-01-09
 */
@Getter
public enum OrderStatusEnum {
    
    PENDING("PENDING", "待處理"),
    PREPARING("PREPARING", "準備出貨中"),
    SHIPPED("SHIPPED", "已出貨"),
    COMPLETED("COMPLETED", "已完成"),
    CANCELLED("CANCELLED", "已取消");
    
    private final String code;
    private final String name;
    
    OrderStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    /**
     * 根據 code 取得對應的枚舉
     */
    public static OrderStatusEnum fromCode(String code) {
        for (OrderStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("無效的訂單狀態：" + code);
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
        for (OrderStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 檢查是否可以取消
     * 只有 PENDING 和 PREPARING 狀態可以取消
     */
    public boolean isCancellable() {
        return this == PENDING || this == PREPARING;
    }
    
    /**
     * 檢查是否已完成（包含 SHIPPED, COMPLETED, CANCELLED）
     */
    public boolean isFinished() {
        return this == SHIPPED || this == COMPLETED || this == CANCELLED;
    }
}
