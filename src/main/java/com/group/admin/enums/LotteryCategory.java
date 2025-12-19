package com.group.admin.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 商品分類（固定列舉）
 * 
 * <p>用於 {@code lottery.category} 欄位</p>
 * <p>依據 permissions-rbac.prompt.md 設計：後端不支援動態新增分類</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum LotteryCategory {
    
    /**
     * 官方一番賞：官方授權的一番賞商品
     */
    OFFICIAL_ICHIBAN("OFFICIAL_ICHIBAN", "官方一番賞"),
    
    /**
     * 扭蛋：傳統扭蛋機商品
     */
    GACHA("GACHA", "扭蛋"),
    
    /**
     * 收藏卡：卡牌類收藏品
     */
    TRADING_CARD("TRADING_CARD", "收藏卡"),
    
    /**
     * 自定義扭蛋：店家自行設計的抽獎商品（可選擇刮刮樂或抽獎模式）
     */
    CUSTOM_GACHA("CUSTOM_GACHA", "自定義扭蛋");

    /**
     * 分類代碼（存入資料庫的值）
     */
    private final String code;
    
    /**
     * 分類顯示名稱
     */
    private final String displayName;

    /**
     * 根據代碼取得列舉
     * 
     * @param code 分類代碼
     * @return 對應的列舉值
     * @throws IllegalArgumentException 如果代碼無效
     */
    public static LotteryCategory fromCode(String code) {
        for (LotteryCategory category : values()) {
            if (category.code.equals(code)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Invalid LotteryCategory code: " + code);
    }

    /**
     * 檢查是否需要子分類
     * 
     * @return true 如果需要指定子分類
     */
    public boolean requiresSubCategory() {
        return this == CUSTOM_GACHA;
    }
}
