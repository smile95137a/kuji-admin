package com.group.admin.enums;

import lombok.Getter;

/**
 * 自製賞子類型枚舉
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Getter
public enum LotterySubCategoryEnum {
    
    LOTTERY_MODE("LOTTERY_MODE", "抽籤型"),
    SCRATCH_MODE("SCRATCH_MODE", "刮刮樂型");

    private final String code;
    private final String name;

    LotterySubCategoryEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根據代碼獲取枚舉
     */
    public static LotterySubCategoryEnum fromCode(String code) {
        if (code == null) return null;
        for (LotterySubCategoryEnum e : values()) {
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
        LotterySubCategoryEnum e = fromCode(code);
        return e != null ? e.name : code;
    }
}
