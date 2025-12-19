package com.group.admin.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 獎項等級
 * 
 * <p>用於 {@code lottery_prize.level} 欄位</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum PrizeLevel {
    
    /**
     * A 等獎（最高等級常規獎）
     */
    A("A", "A賞", 1),
    
    /**
     * B 等獎
     */
    B("B", "B賞", 2),
    
    /**
     * C 等獎
     */
    C("C", "C賞", 3),
    
    /**
     * D 等獎
     */
    D("D", "D賞", 4),
    
    /**
     * E 等獎
     */
    E("E", "E賞", 5),
    
    /**
     * F 等獎
     */
    F("F", "F賞", 6),
    
    /**
     * G 等獎
     */
    G("G", "G賞", 7),
    
    /**
     * 最後賞（特殊獎項，剩餘最後一張時觸發）
     */
    LAST("LAST", "最後賞", 98),
    
    /**
     * 大賞（特殊獎項，影響自動降價機制）
     */
    GRAND("GRAND", "大賞", 99);

    /**
     * 等級代碼（存入資料庫的值）
     */
    private final String code;
    
    /**
     * 等級顯示名稱
     */
    private final String displayName;
    
    /**
     * 排序順序（越小越前面）
     */
    private final int sortOrder;

    /**
     * 根據代碼取得列舉
     * 
     * @param code 等級代碼
     * @return 對應的列舉值
     * @throws IllegalArgumentException 如果代碼無效
     */
    public static PrizeLevel fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (PrizeLevel level : values()) {
            if (level.code.equals(code)) {
                return level;
            }
        }
        throw new IllegalArgumentException("Invalid PrizeLevel code: " + code);
    }

    /**
     * 檢查是否為特殊獎項（最後賞或大賞）
     * 
     * @return true 如果是特殊獎項
     */
    public boolean isSpecialPrize() {
        return this == LAST || this == GRAND;
    }

    /**
     * 檢查是否為大賞（影響自動降價）
     * 
     * @return true 如果是大賞
     */
    public boolean isGrandPrize() {
        return this == GRAND;
    }
}
