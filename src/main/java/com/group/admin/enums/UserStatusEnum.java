package com.group.admin.enums;

import lombok.Getter;

/**
 * 前台會員狀態 Enum
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Getter
public enum UserStatusEnum {
    
    ACTIVE("ACTIVE", "正常"),
    INACTIVE("INACTIVE", "停用"),
    SUSPENDED("SUSPENDED", "暫停使用"),
    DELETED("DELETED", "已刪除");
    
    private final String code;
    private final String name;
    
    UserStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    public static String getNameByCode(String code) {
        if (code == null) {
            return null;
        }
        for (UserStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status.getName();
            }
        }
        return code;
    }
}
