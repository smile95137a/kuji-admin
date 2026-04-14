package com.group.admin.enums;

public enum RechargeOrderStatus {
    /** Initial state when order is created */
    PENDING,
    /** Payment confirmed, coins credited */
    SUCCESS,
    /** Payment rejected by gateway */
    FAILED,
    /** Order not paid within TTL */
    EXPIRED
}
