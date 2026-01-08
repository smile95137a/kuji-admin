package com.group.admin.req.recharge;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 更新儲值方案請求
 * 
 * @author Kuji Admin
 * @since 2026-01-09
 */
@Data
public class RechargePlanUpdateReq {
    
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
}
