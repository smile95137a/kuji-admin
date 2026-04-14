package com.group.admin.exception;

public class InsufficientBalanceException extends RuntimeException {
    private final String userId;
    private final long required;
    private final long available;

    public InsufficientBalanceException(String userId, long required, long available) {
        super(String.format("餘額不足：需要 %d，目前 %d", required, available));
        this.userId = userId;
        this.required = required;
        this.available = available;
    }

    public String getUserId() { return userId; }
    public long getRequired() { return required; }
    public long getAvailable() { return available; }
}
