package com.group.admin.exception;

import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * 業務規則驗證失敗例外（HTTP 422）
 *
 * <p>用於欄位層級的業務規則違反，例如 canEdit=true 但 canView=false，
 * 或 StoreEditor 權限超過 StoreOwner。</p>
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Getter
public class UnprocessableEntityException extends RuntimeException {

    private final List<Map<String, String>> errors;

    public UnprocessableEntityException(String message, List<Map<String, String>> errors) {
        super(message);
        this.errors = errors;
    }

    public UnprocessableEntityException(String message) {
        super(message);
        this.errors = List.of();
    }
}
