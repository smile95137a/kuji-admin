package com.group.admin.exception;

import lombok.Getter;

/**
 * 業務邏輯異常
 * 
 * <p>用於業務邏輯中的預期異常，會被 GlobalExceptionHandler 捕獲並轉換為 ApiResponse</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 錯誤碼
     */
    private final String errorCode;

    /**
     * 建立業務異常（使用預設錯誤碼 BIZ_ERROR）
     * 
     * @param message 錯誤訊息
     */
    public BusinessException(String message) {
        super(message);
        this.errorCode = "BIZ_ERROR";
    }

    /**
     * 建立業務異常
     * 
     * @param errorCode 錯誤碼
     * @param message 錯誤訊息
     */
    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 建立業務異常（帶原因）
     * 
     * @param errorCode 錯誤碼
     * @param message 錯誤訊息
     * @param cause 原因
     */
    public BusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
