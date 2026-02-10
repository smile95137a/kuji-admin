package com.group.admin.result;

import com.group.admin.page.PageResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 統一 API 回應格式
 * 所有 API 都應該回傳此格式，透過 GlobalResponseAspect 自動包裝
 * 
 * 使用範例：
 * 1. 成功回應：return ApiResponse.success(data);
 * 2. 失敗回應：return ApiResponse.error("ERR_001", "錯誤訊息");
 * 3. 分頁回應：return ApiResponse.success(PageResponse.of(items, total, pageRequest));
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    
    /**
     * 請求是否成功
     */
    private Boolean success;
    
    /**
     * 簡短訊息（成功或錯誤的摘要）
     * <p>錯誤時：此欄位為必填，包含錯誤摘要</p>
     * <p>成功時：可選，包含操作結果說明</p>
     */
    private String message;
    
    /**
     * 回應資料（成功時才有）
     */
    private T data;
    
    /**
     * 錯誤資訊（失敗時才有）
     */
    private ErrorInfo error;
    
    /**
     * 後設資料（時間戳記、請求 ID 等）
     */
    private MetaInfo meta;
    
    // ==================== 靜態工廠方法 ====================
    
    /**
     * 成功回應（帶資料）
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .meta(MetaInfo.now())
                .build();
    }
    
    /**
     * 成功回應（無資料）
     */
    public static <T> ApiResponse<T> success() {
        return ApiResponse.<T>builder()
                .success(true)
                .meta(MetaInfo.now())
                .build();
    }
    
    /**
     * 成功回應（帶訊息）
     */
    public static <T> ApiResponse<T> successWithMessage(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .data((T) message)
                .meta(MetaInfo.now())
                .build();
    }
    
    /**
     * 分頁成功回應
     */
    public static <T> ApiResponse<PageResponse<T>> successPage(PageResponse<T> pageData) {
        return ApiResponse.<PageResponse<T>>builder()
                .success(true)
                .data(pageData)
                .meta(MetaInfo.now())
                .build();
    }
    
    /**
     * 失敗回應（錯誤碼 + 訊息）
     */
    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)  // ← Root-level message
                .error(ErrorInfo.of(code, message))
                .meta(MetaInfo.now())
                .build();
    }
    
    /**
     * 失敗回應（錯誤碼 + 訊息 + 詳細資訊）
     */
    public static <T> ApiResponse<T> error(String code, String message, Object details) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)  // ← Root-level message
                .error(ErrorInfo.builder()
                        .code(code)
                        .message(message)
                        .details(details)
                        .build())
                .meta(MetaInfo.now())
                .build();
    }
    
    /**
     * 失敗回應（僅訊息，預設錯誤碼 UNKNOWN_ERROR）
     */
    public static <T> ApiResponse<T> error(String message) {
        return error("UNKNOWN_ERROR", message);
    }
}
