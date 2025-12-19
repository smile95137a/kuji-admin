package com.group.admin.res.draw;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 抽獎結果 DTO
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Data
public class DrawResultRes {
    
    /**
     * 抽獎記錄ID
     */
    private Long recordId;
    
    /**
     * 商品ID
     */
    private Long lotteryId;
    
    /**
     * 商品名稱
     */
    private String lotteryTitle;
    
    /**
     * 獎項ID
     */
    private Long prizeId;
    
    /**
     * 獎項名稱
     */
    private String prizeName;
    
    /**
     * 獎項等級（A/B/C/D/E/F/G/LAST/GRAND）
     */
    private String prizeLevel;
    
    /**
     * 獎項等級名稱（中文）
     */
    private String prizeLevelName;
    
    /**
     * 獎項圖片URL
     */
    private String prizeImageUrl;
    
    /**
     * 籤號/號碼
     */
    private String prizeNumber;
    
    /**
     * 是否為最後賞
     */
    private Boolean isLastPrize;
    
    /**
     * 是否為大賞
     */
    private Boolean isGrandPrize;
    
    /**
     * 抽獎消費類型
     */
    private String costType;
    
    /**
     * 抽獎消費金額
     */
    private Long costAmount;
    
    /**
     * 抽獎時間
     */
    private LocalDateTime drawTime;
    
    /**
     * 該商品剩餘抽數
     */
    private Integer remainingDraws;
    
    /**
     * 該商品總抽數
     */
    private Integer totalDraws;
    
    /**
     * 是否觸發降價（大獎售完）
     */
    private Boolean discountTriggered;
    
    /**
     * 降價後價格（若觸發降價）
     */
    private Long discountedPrice;
    
    /**
     * 是否為活動結束抽（抽完最後一個）
     */
    private Boolean isLastDraw;
}
