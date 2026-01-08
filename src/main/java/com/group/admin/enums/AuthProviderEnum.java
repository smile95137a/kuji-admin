package com.group.admin.enums;

import lombok.Getter;

/**
 * 登入方式 Enum
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Getter
public enum AuthProviderEnum {
    
    LOCAL("LOCAL", "本地註冊"),
    GOOGLE("GOOGLE", "Google 登入"),
    FACEBOOK("FACEBOOK", "Facebook 登入"),
    LINE("LINE", "Line 登入");
    
    private final String code;
    private final String name;
    
    AuthProviderEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    public static String getNameByCode(String code) {
        if (code == null) {
            return null;
        }
        for (AuthProviderEnum provider : values()) {
            if (provider.getCode().equals(code)) {
                return provider.getName();
            }
        }
        return code;
    }
}
