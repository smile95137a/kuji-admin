package com.group.admin.enums;

import lombok.Getter;

/**
 * 獎項類型枚舉
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Getter
public enum PrizeTypeEnum {
    
    PHYSICAL("physical", "實體獎品"),
    DIGITAL("digital", "數位獎品"),
    POINT("point", "點數獎品");

    private final String code;
    private final String name;

    PrizeTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根據代碼獲取枚舉
     */
    public static PrizeTypeEnum fromCode(String code) {
        if (code == null) return null;
        for (PrizeTypeEnum e : values()) {
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
        PrizeTypeEnum e = fromCode(code);
        return e != null ? e.name : code;
    }
}
