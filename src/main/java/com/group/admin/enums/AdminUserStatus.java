package com.group.admin.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 後台管理者帳號狀態
 * 
 * <p>用於 {@code admin_user.status} 欄位</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum AdminUserStatus {
    
    /**
     * 待啟用（新建立的帳號，尚未首次登入改密碼）
     */
    PENDING("PENDING", "待啟用"),
    
    /**
     * 啟用中（正常使用）
     */
    ACTIVE("ACTIVE", "啟用"),
    
    /**
     * 已停用（被 Admin 停用）
     */
    INACTIVE("INACTIVE", "停用");

    /**
     * 狀態代碼（存入資料庫的值）
     */
    private final String code;
    
    /**
     * 狀態顯示名稱
     */
    private final String displayName;

    /**
     * 根據代碼取得列舉
     * 
     * @param code 狀態代碼
     * @return 對應的列舉值
     * @throws IllegalArgumentException 如果代碼無效
     */
    public static AdminUserStatus fromCode(String code) {
        for (AdminUserStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid AdminUserStatus code: " + code);
    }

    /**
     * 檢查是否為可登入狀態
     * 
     * @return true 如果可以登入
     */
    public boolean canLogin() {
        return this == ACTIVE || this == PENDING;
    }
}
