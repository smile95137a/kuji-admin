package com.group.admin.enums;

import lombok.Getter;

/**
 * 商品狀態枚舉
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Getter
public enum LotteryStatusEnum implements DisplayableEnum {

    DRAFT("DRAFT", "草稿"),
    WAITING_ON_SHELF("WAITING_ON_SHELF", "等待時間到上架"),
    ON_SHELF("ON_SHELF", "已上架"),
    OFF_SHELF("OFF_SHELF", "已下架"),
    GRAND_PRIZE_DRAWN("GRAND_PRIZE_DRAWN", "大獎已抽完"),
    ALL_DRAWN("ALL_DRAWN", "全數已抽完"),
    FORCED_OFF("FORCED_OFF", "強制下架"),
    DELETED("DELETED", "已刪除");

    private final String code;
    private final String name;

    LotteryStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getDisplayName() {
        return this.name;
    }

    public static LotteryStatusEnum fromCode(String code) {
        if (code == null) return null;
        for (LotteryStatusEnum e : values()) {
            if (e.code.equalsIgnoreCase(code)) {
                return e;
            }
        }
        return null;
    }

    public static String getNameByCode(String code) {
        LotteryStatusEnum e = fromCode(code);
        return e != null ? e.name : code;
    }
}