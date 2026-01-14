package com.group.admin.dto.res.report;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 推薦碼報表回應
 */
@Data
@Builder
public class ReferralReportRes {
    
    /**
     * 報表期間開始
     */
    private LocalDate startDate;
    
    /**
     * 報表期間結束
     */
    private LocalDate endDate;
    
    /**
     * 總推薦人數
     */
    private Integer totalReferrals;
    
    /**
     * 總推薦獎勵金額
     */
    private BigDecimal totalBonusAmount;
    
    /**
     * 推薦轉換率（%）
     */
    private BigDecimal conversionRate;
    
    /**
     * 與上期比較（%）
     */
    private BigDecimal growthRate;
    
    /**
     * 每日推薦明細
     */
    private List<DailyReferral> dailyDetails;
    
    /**
     * 推薦碼排行榜
     */
    private List<ReferralRanking> rankings;
    
    @Data
    @Builder
    public static class DailyReferral {
        private LocalDate date;
        private Integer referrals;
        private BigDecimal bonusAmount;
    }
    
    @Data
    @Builder
    public static class ReferralRanking {
        private String referralCode;
        private String userName;
        private String storeName;
        private Integer referralCount;
        private BigDecimal totalBonus;
        private Integer rank;
    }
}
