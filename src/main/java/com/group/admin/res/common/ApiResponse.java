package com.group.admin.res.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 通用 API 回應封裝
 * 
 * <p>所有 API 回應的標準格式</p>
 * 
 * @param <T> 資料類型
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "通用 API 回應")
public class ApiResponse<T> {

    /**
     * 是否成功
     */
    @Schema(description = "是否成功", example = "true")
    private Boolean success;

    /**
     * 回應訊息
     */
    @Schema(description = "回應訊息", example = "操作成功")
    private String message;

    /**
     * 錯誤代碼（失敗時使用）
     */
    @Schema(description = "錯誤代碼", example = "AUTH_INVALID_001")
    private String errorCode;

    /**
     * 回應資料
     */
    @Schema(description = "回應資料")
    private T data;

    /**
     * 回應時間戳
     */
    @Schema(description = "回應時間戳")
    private LocalDateTime timestamp;

    /**
     * 成功回應（無資料）
     */
    public static <T> ApiResponse<T> success() {
        return ApiResponse.<T>builder()
                .success(true)
                .message("操作成功")
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 成功回應（含資料）
     * 
     * @param data 資料
     * @param <T> 資料類型
     * @return API 回應
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("操作成功")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 成功回應（含訊息與資料）
     * 
     * @param message 訊息
     * @param data 資料
     * @param <T> 資料類型
     * @return API 回應
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 失敗回應
     * 
     * @param errorCode 錯誤代碼
     * @param message 錯誤訊息
     * @param <T> 資料類型
     * @return API 回應
     */
    public static <T> ApiResponse<T> error(String errorCode, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .errorCode(errorCode)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 失敗回應（僅訊息）
     * 
     * @param message 錯誤訊息
     * @param <T> 資料類型
     * @return API 回應
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
