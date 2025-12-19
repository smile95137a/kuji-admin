package com.group.admin.enums;

import lombok.Getter;

/**
 * 商品狀態枚舉
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Getter
public enum LotteryStatusEnum {
    
    DRAFT("DRAFT", "草稿"),
    OFF_SHELF("OFF_SHELF", "已下架"),
    ON_SHELF("ON_SHELF", "已上架"),
    IN_PROGRESS("IN_PROGRESS", "抽獎中"),
    ENDED("ENDED", "已結束"),
    FORCED_OFF("FORCED_OFF", "強制下架");

    private final String code;
    private final String name;

    LotteryStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根據代碼獲取枚舉
     */
    public static LotteryStatusEnum fromCode(String code) {
        if (code == null) return null;
        for (LotteryStatusEnum e : values()) {
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
        LotteryStatusEnum e = fromCode(code);
        return e != null ? e.name : code;
    }
}
