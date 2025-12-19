package com.group.admin.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 點數類型（雙軌制）
 * 
 * <p>用於 {@code point_log.point_type} 欄位</p>
 * <p>依據 user-member-system.prompt.md 設計：優先消耗 Bonus，再消耗 Gold</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum PointType {
    
    /**
     * 儲值金：使用者透過付費購買，用於所有抽獎遊戲，不可轉讓、不可退款
     */
    GOLD("gold", "儲值金"),
    
    /**
     * 紅利金：系統贈送（新手禮、活動獎勵、簽到），同樣可用於抽獎
     */
    BONUS("bonus", "紅利金");

    /**
     * 點數類型代碼（存入資料庫的值）
     */
    private final String code;
    
    /**
     * 點數類型顯示名稱
     */
    private final String displayName;

    /**
     * 根據代碼取得列舉
     * 
     * @param code 點數類型代碼
     * @return 對應的列舉值
     * @throws IllegalArgumentException 如果代碼無效
     */
    public static PointType fromCode(String code) {
        for (PointType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid PointType code: " + code);
    }

    /**
     * 檢查是否為儲值金
     * 
     * @return true 如果是儲值金
     */
    public boolean isGold() {
        return this == GOLD;
    }

    /**
     * 檢查是否為紅利金
     * 
     * @return true 如果是紅利金
     */
    public boolean isBonus() {
        return this == BONUS;
    }
}
