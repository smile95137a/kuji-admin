package com.group.admin.handler;

import com.group.admin.exception.BusinessException;
import com.group.admin.exception.UnprocessableEntityException;
import com.group.admin.result.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全域異常處理器
 * 
 * <p>統一處理所有 Controller 層拋出的異常，轉換為 ApiResponse 格式</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 處理業務規則驗證失敗 (HTTP 422)
     *
     * @param ex UnprocessableEntityException
     * @return 統一錯誤回應
     */
    @ExceptionHandler(UnprocessableEntityException.class)
    public ResponseEntity<ApiResponse<?>> handleUnprocessableEntityException(UnprocessableEntityException ex) {
        log.warn("⚠️ 業務規則驗證失敗: {}", ex.getMessage());
        Object details = ex.getErrors() != null && !ex.getErrors().isEmpty()
                ? java.util.Map.of("errors", ex.getErrors())
                : null;
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.error("VALIDATION_ERROR", ex.getMessage(), details));
    }

    /**
     * 處理業務邏輯異常
     *
     * @param ex BusinessException
     * @return 統一錯誤回應
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleBusinessException(BusinessException ex) {
        log.warn("⚠️ 業務邏輯例外: [{}] {}", ex.getErrorCode(), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getErrorCode(), ex.getMessage()));
    }

    /**
     * 處理參數驗證錯誤 (ex: @Valid 驗證失敗)
     * 
     * @param ex MethodArgumentNotValidException
     * @return 統一錯誤回應
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.warn("❗ 參數驗證錯誤: {}", message);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("COMMON_VALIDATION_001", message));
    }

    /**
     * 處理認證異常（帳號密碼錯誤）
     * 
     * @param ex BadCredentialsException
     * @return 統一錯誤回應
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<?>> handleBadCredentialsException(BadCredentialsException ex) {
        log.warn("🔐 認證失敗: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("AUTH_INVALID_001", "帳號或密碼錯誤"));
    }

    /**
     * 處理認證異常（通用）
     * 
     * @param ex AuthenticationException
     * @return 統一錯誤回應
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<?>> handleAuthenticationException(AuthenticationException ex) {
        log.warn("🔐 認證異常: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("AUTH_TOKEN_001", "認證失敗，請重新登入"));
    }

    /**
     * 處理權限不足異常
     * 
     * @param ex AccessDeniedException
     * @return 統一錯誤回應
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("🚫 權限不足: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("COMMON_ACCESS_001", "無權執行此操作"));
    }

    /**
     * 處理所有一般業務邏輯錯誤 (RuntimeException / IllegalArgumentException 等)
     * 
     * @param ex RuntimeException
     * @return 統一錯誤回應
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<?>> handleRuntimeException(RuntimeException ex) {
        log.warn("⚠️ 業務邏輯例外: {}", ex.getMessage(), ex);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("BUSINESS_ERROR", ex.getMessage()));
    }

    /**
     * 捕捉所有未處理異常 (系統級錯誤)
     * 
     * @param ex Exception
     * @return 統一錯誤回應
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGlobalException(Exception ex) {
        log.error("💥 系統未知錯誤: {}", ex.getMessage(), ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("COMMON_INTERNAL_001", "系統發生未知錯誤，請稍後再試"));
    }
}
