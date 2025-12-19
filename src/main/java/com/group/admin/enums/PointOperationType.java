package com.group.admin.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 點數操作類型
 * 
 * <p>用於 {@code point_log.operation_type} 欄位</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum PointOperationType {
    
    /**
     * 儲值：使用者付費購買點數
     */
    DEPOSIT("deposit", "儲值", true),
    
    /**
     * 扣除：一般扣除（非抽獎）
     */
    DEDUCT("deduct", "扣除", false),
    
    /**
     * 抽獎消費：執行抽獎時扣除的點數
     */
    DRAW("draw", "抽獎消費", false),
    
    /**
     * 退款：訂單退款返還點數
     */
    REFUND("refund", "退款", true),
    
    /**
     * 紅利贈送：系統贈送紅利金（活動、簽到等）
     */
    BONUS_GRANT("bonus_grant", "紅利贈送", true),
    
    /**
     * 紅利過期：紅利金到期失效
     */
    BONUS_EXPIRE("bonus_expire", "紅利過期", false);

    /**
     * 操作類型代碼（存入資料庫的值）
     */
    private final String code;
    
    /**
     * 操作類型顯示名稱
     */
    private final String displayName;
    
    /**
     * 是否為增加點數的操作
     */
    private final boolean isIncrease;

    /**
     * 根據代碼取得列舉
     * 
     * @param code 操作類型代碼
     * @return 對應的列舉值
     * @throws IllegalArgumentException 如果代碼無效
     */
    public static PointOperationType fromCode(String code) {
        for (PointOperationType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid PointOperationType code: " + code);
    }

    /**
     * 檢查是否為減少點數的操作
     * 
     * @return true 如果是減少點數
     */
    public boolean isDecrease() {
        return !isIncrease;
    }
}
