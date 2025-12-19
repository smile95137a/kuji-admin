package com.group.admin.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 自定義扭蛋子分類
 * 
 * <p>用於 {@code lottery.sub_category} 欄位</p>
 * <p>僅當 {@code category = CUSTOM_GACHA} 時需要指定</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum LotterySubCategory {
    
    /**
     * 抽獎模式：一番賞類型，系統隨機分配
     */
    LOTTERY_MODE("LOTTERY_MODE", "抽獎模式"),
    
    /**
     * 刮刮卡模式：允許玩家指定號碼
     */
    SCRATCH_MODE("SCRATCH_MODE", "刮刮卡模式");

    /**
     * 子分類代碼（存入資料庫的值）
     */
    private final String code;
    
    /**
     * 子分類顯示名稱
     */
    private final String displayName;

    /**
     * 根據代碼取得列舉
     * 
     * @param code 子分類代碼
     * @return 對應的列舉值
     * @throws IllegalArgumentException 如果代碼無效
     */
    public static LotterySubCategory fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (LotterySubCategory subCategory : values()) {
            if (subCategory.code.equals(code)) {
                return subCategory;
            }
        }
        throw new IllegalArgumentException("Invalid LotterySubCategory code: " + code);
    }

    /**
     * 檢查是否允許選號
     * 
     * @return true 如果玩家可以選擇號碼
     */
    public boolean allowSelectNumber() {
        return this == SCRATCH_MODE;
    }
}
