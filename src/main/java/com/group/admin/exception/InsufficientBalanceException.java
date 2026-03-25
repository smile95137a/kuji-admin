package com.group.admin.exception;

import lombok.Getter;

@Getter
public class InsufficientBalanceException extends RuntimeException {
    private final String userId;
    private final long required;
    private final long available;

    public InsufficientBalanceException(String userId, long required, long available) {
        super(String.format("餘額不足: 需要 %d, 可用 %d", required, available));
        this.userId = userId;
        this.required = required;
        this.available = available;
    }
}
