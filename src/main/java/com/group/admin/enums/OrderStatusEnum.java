package com.group.admin.enums;

import lombok.Getter;

/**
 * 訂單狀態枚舉
 *
 * @author Kuji Admin
 * @since 2026-01-09
 */
@Getter
public enum OrderStatusEnum implements DisplayableEnum {

    PAYMENT_PENDING("PAYMENT_PENDING", "待付款"),
    PAYMENT_FAILED("PAYMENT_FAILED", "付款失敗"),
    PENDING("PENDING", "待處理"),
    PREPARING("PREPARING", "備貨中"),
    SHIPPED("SHIPPED", "已出貨"),
    COMPLETED("COMPLETED", "已完成"),
    CANCELLED("CANCELLED", "已取消");

    private final String code;
    private final String name;

    OrderStatusEnum(String code, String name) {
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

    public static OrderStatusEnum fromCode(String code) {
        for (OrderStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("無效的訂單狀態：" + code);
    }

    public static String getNameByCode(String code) {
        try {
            return fromCode(code).getName();
        } catch (IllegalArgumentException e) {
            return code;
        }
    }

    public static boolean isValid(String code) {
        for (OrderStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }

    public boolean isCancellable() {
        return this == PAYMENT_PENDING || this == PAYMENT_FAILED || this == PENDING || this == PREPARING;
    }

    public boolean isFinished() {
        return this == SHIPPED || this == COMPLETED || this == CANCELLED;
    }

    public boolean isPaid() {
        return this != PAYMENT_PENDING && this != PAYMENT_FAILED && this != CANCELLED;
    }
}
