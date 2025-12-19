package com.group.admin.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 後台操作類型（審計日誌用）
 * 
 * <p>用於 {@code admin_operation_log.operation_type} 欄位</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum OperationType {
    
    /**
     * 建立資料
     */
    CREATE("CREATE", "建立"),
    
    /**
     * 更新資料
     */
    UPDATE("UPDATE", "更新"),
    
    /**
     * 刪除資料
     */
    DELETE("DELETE", "刪除"),
    
    /**
     * 登入
     */
    LOGIN("LOGIN", "登入"),
    
    /**
     * 登出
     */
    LOGOUT("LOGOUT", "登出"),
    
    /**
     * 修改密碼
     */
    CHANGE_PASSWORD("CHANGE_PASSWORD", "修改密碼"),
    
    /**
     * 啟用帳號
     */
    ACTIVATE("ACTIVATE", "啟用帳號"),
    
    /**
     * 停用帳號
     */
    DEACTIVATE("DEACTIVATE", "停用帳號");

    /**
     * 操作類型代碼（存入資料庫的值）
     */
    private final String code;
    
    /**
     * 操作類型顯示名稱
     */
    private final String displayName;

    /**
     * 根據代碼取得列舉
     * 
     * @param code 操作類型代碼
     * @return 對應的列舉值
     * @throws IllegalArgumentException 如果代碼無效
     */
    public static OperationType fromCode(String code) {
        for (OperationType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid OperationType code: " + code);
    }
}
