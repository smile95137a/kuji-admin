package com.group.admin.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 店家與使用者關聯的角色類型
 * 
 * <p>用於 {@code store_user.role_type} 欄位</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum StoreUserRoleType {
    
    /**
     * 店家主帳號（一對一，透過 store.owner_id 綁定）
     */
    OWNER("OWNER", "店家主帳號"),
    
    /**
     * 店家小編（可多對多，透過 store_user 表綁定）
     */
    EDITOR("EDITOR", "店家小編");

    /**
     * 角色類型代碼（存入資料庫的值）
     */
    private final String code;
    
    /**
     * 角色類型顯示名稱
     */
    private final String displayName;

    /**
     * 根據代碼取得列舉
     * 
     * @param code 角色類型代碼
     * @return 對應的列舉值
     * @throws IllegalArgumentException 如果代碼無效
     */
    public static StoreUserRoleType fromCode(String code) {
        for (StoreUserRoleType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid StoreUserRoleType code: " + code);
    }

    /**
     * 檢查是否為店家主帳號
     * 
     * @return true 如果是 Owner
     */
    public boolean isOwner() {
        return this == OWNER;
    }
}
