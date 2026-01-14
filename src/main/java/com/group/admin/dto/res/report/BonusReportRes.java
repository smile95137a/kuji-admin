package com.group.admin.dto.res.report;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 贈送點數報表回應
 */
@Data
@Builder
public class BonusReportRes {
    
    /**
     * 報表期間開始
     */
    private LocalDate startDate;
    
    /**
     * 報表期間結束
     */
    private LocalDate endDate;
    
    /**
     * 總贈送點數
     */
    private BigDecimal totalBonusPoints;
    
    /**
     * 總贈送筆數
     */
    private Integer totalCount;
    
    /**
     * 受益會員數
     */
    private Integer benefitUsers;
    
    /**
     * 與上期比較（%）
     */
    private BigDecimal growthRate;
    
    /**
     * 每日贈送明細
     */
    private List<DailyBonus> dailyDetails;
    
    /**
     * 贈送類型統計
     */
    private List<BonusTypeStats> typeStats;
    
    @Data
    @Builder
    public static class DailyBonus {
        private LocalDate date;
        private BigDecimal points;
        private Integer count;
    }
    
    @Data
    @Builder
    public static class BonusTypeStats {
        private String bonusType;  // REFERRAL/PROMOTION/ADJUSTMENT/REGISTRATION
        private String typeName;
        private BigDecimal totalPoints;
        private Integer count;
        private BigDecimal percentage;
    }
}
