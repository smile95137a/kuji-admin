package com.group.admin.res.prizebox;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 賞品盒項目回應
 * 
 * @author Kuji Admin
 * @since 2026-01-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrizeBoxItemRes {
    
    /**
     * 賞品盒項目 ID
     */
    private String id;
    
    /**
     * 玩家 ID
     */
    private String userId;
    
    /**
     * 商品 ID
     */
    private String lotteryId;
    
    /**
     * 商品名稱
     */
    private String lotteryTitle;
    
    /**
     * 商品圖片
     */
    private String lotteryImageUrl;
    
    /**
     * 獎項 ID
     */
    private String prizeId;
    
    /**
     * 獎項名稱
     */
    private String prizeName;
    
    /**
     * 獎項等級（A/B/C/D/E/F/G/Last）
     */
    private String prizeLevel;
    
    /**
     * 獎項圖片
     */
    private String prizeImageUrl;
    
    /**
     * 店家 ID
     */
    private String storeId;
    
    /**
     * 店家名稱
     */
    private String storeName;
    
    /**
     * 狀態代碼
     */
    private String status;
    
    /**
     * 狀態名稱
     */
    private String statusName;
    
    /**
     * 是否可回收
     */
    private Boolean isRecyclable;

    /**
     * 是否可出貨
     */
    private Boolean isShippable;

    /**
     * 獎品市值
     */
    private Long prizeValue;

    /**
     * 回收可得紅利
     */
    private Long recycleBonus;

    /**
     * 抽中時間
     */
    private LocalDateTime createdAt;

    /**
     * 出貨時間（歷史記錄用）
     */
    private LocalDateTime shippedAt;

    /**
     * 回收時間（歷史記錄用）
     */
    private LocalDateTime recycledAt;
}
