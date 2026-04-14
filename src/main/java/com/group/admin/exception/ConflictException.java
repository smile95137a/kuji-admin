package com.group.admin.exception;

import lombok.Getter;

/**
 * 狀態衝突異常（HTTP 409）
 */
@Getter
public class ConflictException extends RuntimeException {

    private final String errorCode;

    public ConflictException(String message) {
        super(message);
        this.errorCode = "CONFLICT";
    }

    public ConflictException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
