package com.group.admin.res.wallet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 儲值方案回應
 * 
 * @author Kuji Admin
 * @since 2026-01-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RechargePlanRes {
    
    /**
     * 方案 ID
     */
    private String id;
    
    /**
     * 方案名稱
     */
    private String name;
    
    /**
     * 方案說明
     */
    private String description;
    
    /**
     * 儲值金額（台幣，單位：元）
     */
    private Long amount;
    
    /**
     * 獲得金幣
     */
    private Long goldCoins;
    
    /**
     * 贈送紅利
     */
    private Long bonusCoins;
    
    /**
     * 是否啟用
     */
    private Boolean isActive;
    
    /**
     * 是否為活動方案
     */
    private Boolean isPromotional;
    
    /**
     * 顯示順序
     */
    private Integer displayOrder;
    
    /**
     * 活動開始時間
     */
    private LocalDateTime startTime;
    
    /**
     * 活動結束時間
     */
    private LocalDateTime endTime;
    
    /**
     * 建立時間
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新時間
     */
    private LocalDateTime updatedAt;
    
    /**
     * 是否在活動期間內
     */
    private Boolean isInPeriod;
    
    /**
     * 優惠比例（顯示用，例如：贈送 30%）
     */
    private String bonusPercentage;
}
