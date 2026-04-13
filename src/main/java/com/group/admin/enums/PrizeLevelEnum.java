package com.group.admin.enums;

import lombok.Getter;

/**
 * 獎項等級枚舉
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Getter
public enum PrizeLevelEnum implements DisplayableEnum {

    A("A", "A賞", 1),
    B("B", "B賞", 2),
    C("C", "C賞", 3),
    D("D", "D賞", 4),
    E("E", "E賞", 5),
    F("F", "F賞", 6),
    G("G", "G賞", 7),
    LAST("LAST", "最後賞", 98),
    GRAND("GRAND", "大賞", 99);

    private final String code;
    private final String name;
    private final int sortOrder;

    PrizeLevelEnum(String code, String name, int sortOrder) {
        this.code = code;
        this.name = name;
        this.sortOrder = sortOrder;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getDisplayName() {
        return this.name;
    }

    public boolean isGrandPrize() {
        return this == GRAND;
    }

    public boolean isSpecialPrize() {
        return this == LAST || this == GRAND;
    }

    public static PrizeLevelEnum fromCode(String code) {
        if (code == null) return null;
        for (PrizeLevelEnum e : values()) {
            if (e.code.equalsIgnoreCase(code)) {
                return e;
            }
        }
        return null;
    }

    public static String getNameByCode(String code) {
        PrizeLevelEnum e = fromCode(code);
        return e != null ? e.name : code;
    }
}
