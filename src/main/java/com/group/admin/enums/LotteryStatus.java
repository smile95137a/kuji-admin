package com.group.admin.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 商品（抽獎活動）狀態
 * 
 * <p>用於 {@code lottery.status} 欄位</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum LotteryStatus {
    
    /**
     * 上架中（前台可見、可抽獎）
     */
    ON_SHELF("ON_SHELF", "上架中"),
    
    /**
     * 已下架（前台不可見）
     */
    OFF_SHELF("OFF_SHELF", "已下架");

    /**
     * 狀態代碼（存入資料庫的值）
     */
    private final String code;
    
    /**
     * 狀態顯示名稱
     */
    private final String displayName;

    /**
     * 根據代碼取得列舉
     * 
     * @param code 狀態代碼
     * @return 對應的列舉值
     * @throws IllegalArgumentException 如果代碼無效
     */
    public static LotteryStatus fromCode(String code) {
        for (LotteryStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid LotteryStatus code: " + code);
    }

    /**
     * 檢查是否為可抽獎狀態
     * 
     * @return true 如果可以抽獎
     */
    public boolean canDraw() {
        return this == ON_SHELF;
    }
}
