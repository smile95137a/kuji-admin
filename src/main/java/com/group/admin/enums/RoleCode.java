package com.group.admin.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 系統角色代碼
 * 
 * <p>用於 {@code role.code} 欄位，對應 RBAC 權限系統</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum RoleCode {
    
    /**
     * 平台最高管理者，擁有所有權限
     */
    ROLE_ADMIN("ROLE_ADMIN", "Admin", "平台管理員"),
    
    /**
     * 店家主帳號，可管理自己店家的商品、訂單、報表
     */
    ROLE_STORE_OWNER("ROLE_STORE_OWNER", "StoreOwner", "店家主帳號"),
    
    /**
     * 店家編輯者，僅可操作商品與部分訂單功能
     */
    ROLE_STORE_EDITOR("ROLE_STORE_EDITOR", "StoreEditor", "店家小編");

    /**
     * 角色代碼（存入資料庫的值，用於 Spring Security）
     */
    private final String code;
    
    /**
     * 角色名稱（簡短名稱）
     */
    private final String name;
    
    /**
     * 角色顯示名稱（中文描述）
     */
    private final String displayName;

    /**
     * 根據代碼取得列舉
     * 
     * @param code 角色代碼
     * @return 對應的列舉值
     * @throws IllegalArgumentException 如果代碼無效
     */
    public static RoleCode fromCode(String code) {
        for (RoleCode role : values()) {
            if (role.code.equals(code)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid RoleCode: " + code);
    }

    /**
     * 取得 Spring Security 用的角色名稱（不含 ROLE_ 前綴）
     * 
     * @return 角色名稱
     */
    public String getSecurityRole() {
        return code.replace("ROLE_", "");
    }

    /**
     * 檢查是否為管理員
     * 
     * @return true 如果是 Admin
     */
    public boolean isAdmin() {
        return this == ROLE_ADMIN;
    }

    /**
     * 檢查是否為店家相關角色
     * 
     * @return true 如果是 StoreOwner 或 StoreEditor
     */
    public boolean isStoreRole() {
        return this == ROLE_STORE_OWNER || this == ROLE_STORE_EDITOR;
    }
}
