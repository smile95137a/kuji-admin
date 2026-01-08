package com.group.admin.res.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 訂單項目回應
 * 
 * @author Kuji Admin
 * @since 2026-01-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRes {
    
    /**
     * 訂單項目 ID
     */
    private String id;
    
    /**
     * 訂單 ID
     */
    private String orderId;
    
    /**
     * 賞品盒 ID
     */
    private String prizeBoxId;
    
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
     * 獎項圖片
     */
    private String prizeImageUrl;
    
    /**
     * 獎項等級
     */
    private String prizeLevel;
    
    /**
     * 建立時間
     */
    private LocalDateTime createdAt;
}
