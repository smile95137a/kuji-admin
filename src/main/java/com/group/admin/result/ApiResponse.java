package com.group.admin.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private ErrorInfo error;
    private MetaInfo meta;
    
    // 成功回應的靜態工廠方法
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
            .success(true)
            .data(data)
            .meta(MetaInfo.now())
            .build();
    }
    
    // 失敗回應的靜態工廠方法
    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
            .success(false)
            .error(ErrorInfo.of(code, message))
            .meta(MetaInfo.now())
            .build();
    }
}
