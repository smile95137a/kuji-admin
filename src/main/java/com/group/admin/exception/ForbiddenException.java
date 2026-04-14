package com.group.admin.exception;

import lombok.Getter;

/**
 * 禁止存取異常（HTTP 403）
 */
@Getter
public class ForbiddenException extends RuntimeException {

    private final String errorCode;

    public ForbiddenException(String message) {
        super(message);
        this.errorCode = "FORBIDDEN";
    }

    public ForbiddenException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
