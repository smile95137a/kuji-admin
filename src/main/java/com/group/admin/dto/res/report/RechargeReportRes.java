package com.group.admin.dto.res.report;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 儲值報表回應（平台視角）
 */
@Data
@Builder
public class RechargeReportRes {
    
    /**
     * 報表期間開始
     */
    private LocalDate startDate;
    
    /**
     * 報表期間結束
     */
    private LocalDate endDate;
    
    /**
     * 總儲值金額
     */
    private BigDecimal totalAmount;
    
    /**
     * 總儲值筆數
     */
    private Integer totalCount;
    
    /**
     * 平均單筆儲值金額
     */
    private BigDecimal avgAmount;
    
    /**
     * 與上期比較（%）
     */
    private BigDecimal growthRate;
    
    /**
     * 每日儲值明細
     */
    private List<DailyRecharge> dailyDetails;
    
    /**
    * 儲值方案分布
     */
    private List<PlanStats> planStats;
    
    @Data
    @Builder
    public static class DailyRecharge {
        private LocalDate date;
        private BigDecimal amount;
        private Integer count;
        private Integer newUsers;
    }
    
    @Data
    @Builder
    public static class PlanStats {
        private String planId;
        private String planName;
        private BigDecimal planPrice;
        private BigDecimal bonusPoints;
        private Integer purchaseCount;
        private BigDecimal totalAmount;
        private BigDecimal percentage;
    }
}
