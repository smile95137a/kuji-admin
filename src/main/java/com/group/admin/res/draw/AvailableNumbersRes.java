package com.group.admin.res.draw;

import java.util.List;
import lombok.Data;

/**
 * 可選號碼列表回應 DTO
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Data
public class AvailableNumbersRes {
    
    /**
     * 商品ID
     */
    private Long lotteryId;
    
    /**
     * 商品名稱
     */
    private String lotteryTitle;
    
    /**
     * 總號碼數
     */
    private Integer totalNumbers;
    
    /**
     * 剩餘可選號碼數
     */
    private Integer availableCount;
    
    /**
     * 可選號碼列表
     */
    private List<NumberInfo> availableNumbers;
    
    /**
     * 號碼資訊
     */
    @Data
    public static class NumberInfo {
        /**
         * 號碼
         */
        private String number;
        
        /**
         * 顯示用號碼（可能帶前綴 0）
         */
        private String displayNumber;
        
        /**
         * 獎項等級（前端可選擇是否顯示）
         */
        private String prizeLevel;
        
        /**
         * 是否為最後賞
         */
        private Boolean isLastPrize;
        
        /**
         * 是否為大賞
         */
        private Boolean isGrandPrize;
    }
}
