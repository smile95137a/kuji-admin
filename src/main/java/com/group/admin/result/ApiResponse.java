package com.group.admin.result;

import com.group.admin.page.PageResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 統一 API 回應格式
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private Boolean success;
    private String message;
    private T data;
    private ErrorInfo error;
    private MetaInfo meta;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> res = new ApiResponse<>();
        res.setSuccess(true);
        res.setData(data);
        res.setMeta(MetaInfo.now());
        return res;
    }

    public static <T> ApiResponse<T> success() {
        ApiResponse<T> res = new ApiResponse<>();
        res.setSuccess(true);
        res.setMeta(MetaInfo.now());
        return res;
    }

    @SuppressWarnings("unchecked")
    public static <T> ApiResponse<T> successWithMessage(String message) {
        ApiResponse<T> res = new ApiResponse<>();
        res.setSuccess(true);
        res.setData((T) message);
        res.setMeta(MetaInfo.now());
        return res;
    }

    public static <T> ApiResponse<PageResponse<T>> successPage(PageResponse<T> pageData) {
        ApiResponse<PageResponse<T>> res = new ApiResponse<>();
        res.setSuccess(true);
        res.setData(pageData);
        res.setMeta(MetaInfo.now());
        return res;
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        ApiResponse<T> res = new ApiResponse<>();
        res.setSuccess(false);
        res.setMessage(message);
        res.setError(ErrorInfo.of(code, message));
        res.setMeta(MetaInfo.now());
        return res;
    }

    public static <T> ApiResponse<T> error(String code, String message, Object details) {
        ApiResponse<T> res = new ApiResponse<>();
        res.setSuccess(false);
        res.setMessage(message);
        res.setError(new ErrorInfo(code, message, details));
        res.setMeta(MetaInfo.now());
        return res;
    }

    public static <T> ApiResponse<T> error(String message) {
        return error("UNKNOWN_ERROR", message);
    }
}