package com.group.admin.enums;

import lombok.Getter;

/**
 * 獎項等級枚舉
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Getter
public enum PrizeLevelEnum {
    
    A("A", "A賞"),
    B("B", "B賞"),
    C("C", "C賞"),
    D("D", "D賞"),
    E("E", "E賞"),
    F("F", "F賞"),
    G("G", "G賞"),
    LAST("LAST", "最後賞"),
    GRAND("GRAND", "大賞");

    private final String code;
    private final String name;

    PrizeLevelEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根據代碼獲取枚舉
     */
    public static PrizeLevelEnum fromCode(String code) {
        if (code == null) return null;
        for (PrizeLevelEnum e : values()) {
            if (e.code.equalsIgnoreCase(code)) {
                return e;
            }
        }
        return null;
    }

    /**
     * 獲取中文名稱
     */
    public static String getNameByCode(String code) {
        PrizeLevelEnum e = fromCode(code);
        return e != null ? e.name : code;
    }
}
