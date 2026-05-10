package com.group.admin.handler;

import com.group.admin.constants.ErrorCodes;
import com.group.admin.exception.BusinessException;
import com.group.admin.result.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("全域例外處理器測試")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("AUTH_TOKEN 類錯誤應映射 401")
    void handleBusinessException_ShouldReturn401_WhenAuthTokenError() {
        ResponseEntity<ApiResponse<?>> response = handler.handleBusinessException(
                new BusinessException(ErrorCodes.AUTH_TOKEN_INVALID, "Refresh Token 無效或已過期")
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCodes.AUTH_TOKEN_INVALID, response.getBody().getError().getCode());
    }

    @Test
    @DisplayName("AUTH_ACCOUNT 類錯誤應映射 403")
    void handleBusinessException_ShouldReturn403_WhenAuthAccountError() {
        ResponseEntity<ApiResponse<?>> response = handler.handleBusinessException(
                new BusinessException(ErrorCodes.AUTH_ACCOUNT_DISABLED, "帳號已停用")
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(ErrorCodes.AUTH_ACCOUNT_DISABLED, response.getBody().getError().getCode());
    }

    @Test
    @DisplayName("NOT_FOUND 類錯誤應映射 404")
    void handleBusinessException_ShouldReturn404_WhenNotFoundError() {
        ResponseEntity<ApiResponse<?>> response = handler.handleBusinessException(
                new BusinessException(ErrorCodes.COMMON_NOT_FOUND, "找不到資料")
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(ErrorCodes.COMMON_NOT_FOUND, response.getBody().getError().getCode());
    }

    @Test
    @DisplayName("未知執行期錯誤應回 500")
    void handleRuntimeException_ShouldReturn500_WhenUnexpectedRuntimeException() {
        ResponseEntity<ApiResponse<?>> response = handler.handleRuntimeException(
                new RuntimeException("boom")
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(ErrorCodes.COMMON_INTERNAL_ERROR, response.getBody().getError().getCode());
    }
}