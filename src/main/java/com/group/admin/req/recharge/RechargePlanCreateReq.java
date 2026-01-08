package com.group.admin.req.recharge;

import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * 新增儲值方案請求
 * 
 * @author Kuji Admin
 * @since 2026-01-09
 */
@Data
public class RechargePlanCreateReq {
    
    /**
     * 方案名稱
     */
    @NotBlank(message = "方案名稱不可為空")
    private String name;
    
    /**
     * 方案說明
     */
    private String description;
    
    /**
     * 儲值金額（台幣，單位：元）
     */
    @NotNull(message = "儲值金額不可為空")
    @Min(value = 1, message = "儲值金額必須大於 0")
    private Long amount;
    
    /**
     * 獲得金幣
     */
    @NotNull(message = "獲得金幣不可為空")
    @Min(value = 1, message = "獲得金幣必須大於 0")
    private Long goldCoins;
    
    /**
     * 贈送紅利（選填，預設 0）
     */
    private Long bonusCoins = 0L;
    
    /**
     * 是否啟用（選填，預設啟用）
     */
    private Boolean isActive = true;
    
    /**
     * 是否為活動方案（選填，預設否）
     */
    private Boolean isPromotional = false;
    
    /**
     * 顯示順序（選填，預設 0）
     */
    private Integer displayOrder = 0;
    
    /**
     * 活動開始時間（活動方案必填）
     */
    private LocalDateTime startTime;
    
    /**
     * 活動結束時間（活動方案必填）
     */
    private LocalDateTime endTime;
}
