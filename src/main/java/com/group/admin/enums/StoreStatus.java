package com.group.admin.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 店家狀態
 * 
 * <p>用於 {@code store.status} 欄位</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum StoreStatus {
    
    /**
     * 啟用中（正常營運，前台可見）
     */
    ACTIVE("ACTIVE", "啟用"),
    
    /**
     * 已停用（前台不可見，商品自動下架）
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
    public static StoreStatus fromCode(String code) {
        for (StoreStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid StoreStatus code: " + code);
    }

    /**
     * 檢查是否為前台可見狀態
     * 
     * @return true 如果前台可見
     */
    public boolean isVisibleToFrontend() {
        return this == ACTIVE;
    }
}
