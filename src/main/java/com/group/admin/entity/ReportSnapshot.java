package com.group.admin.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 報表快照
 * 儲存日報/週報/月報的彙總資料
 */
@Data
public class ReportSnapshot {
    private String id;
    
    /**
     * 報表類型: REVENUE/REFERRAL/LOTTERY_RESULT/RECHARGE/BONUS_GRANT
     */
    private String reportType;
    
    /**
     * 週期類型: DAILY/WEEKLY/MONTHLY
     */
    private String periodType;
    
    /**
     * 週期開始日期
     */
    private LocalDate periodStart;
    
    /**
     * 週期結束日期
     */
    private LocalDate periodEnd;
    
    /**
     * 店家ID（若為店家報表）
     */
    private String storeId;
    
    /**
     * 報表資料（JSON）
     */
    private String data;
    
    /**
     * 摘要統計（JSON）
     */
    private String summary;
    
    private LocalDateTime createdAt;
}
