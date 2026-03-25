package com.group.admin.res.prizebox;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 獎品盒項目回應
 * 
 * @author Kuji Admin
 * @since 2026-01-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrizeBoxItemRes {
    
    private String id;
    private String userId;
    private String lotteryId;
    private String lotteryTitle;
    private String lotteryImageUrl;
    private String prizeId;
    private String prizeName;
    private String prizeLevel;
    private String prizeImageUrl;
    private String storeId;
    private String storeName;
    private String status;
    private String statusName;
    private Boolean isRecyclable;
    private Boolean isShippable;
    private Long recycleBonus;
    private Long prizeValue;
    private String orderId;
    private LocalDateTime createdAt;
    private LocalDateTime shippedAt;
    private LocalDateTime recycledAt;
}