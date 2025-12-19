package com.group.admin.res.draw;

import java.util.List;
import lombok.Data;

/**
 * 多連抽結果 DTO
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Data
public class MultiDrawResultRes {
    
    /**
     * 商品ID
     */
    private Long lotteryId;
    
    /**
     * 商品名稱
     */
    private String lotteryTitle;
    
    /**
     * 實際抽取次數
     */
    private Integer actualDrawCount;
    
    /**
     * 請求的連抽次數
     */
    private Integer requestedDrawCount;
    
    /**
     * 總消費金額
     */
    private Long totalCost;
    
    /**
     * 消費類型
     */
    private String costType;
    
    /**
     * 各次抽獎結果
     */
    private List<DrawResultRes> results;
    
    /**
     * 該商品剩餘抽數
     */
    private Integer remainingDraws;
    
    /**
     * 是否提前結束（獎品抽完）
     */
    private Boolean endedEarly;
    
    /**
     * 是否觸發降價（大獎售完）
     */
    private Boolean discountTriggered;
    
    /**
     * 降價後價格
     */
    private Long discountedPrice;
}
