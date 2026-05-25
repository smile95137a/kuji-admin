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
     * 推薦碼總數
     */
    private Integer totalReferralCodeCount;

    /**
     * 啟用中的推薦碼總數
     */
    private Integer activeReferralCodeCount;

    /**
     * 歷史累計成功招商店數
     */
    private Integer successfulReferralStoreCount;

    /**
     * 使用推薦碼註冊的會員累計數
     */
    private Integer totalUserReferralCount;

    /**
     * 查詢區間內使用推薦碼註冊的會員數
     */
    private Integer currentPeriodUserReferralCount;

    /**
     * 本期成功招商店數
     */
    private Integer currentPeriodActivatedStoreCount;

    /**
     * 上期成功招商店數
     */
    private Integer previousPeriodActivatedStoreCount;
    
    /**
     * 與上期比較（%）
     */
    private BigDecimal growthRate;
    
    /**
     * 每日招商啟用明細
     */
    private List<DailyActivation> dailyActivations;
    
    /**
     * 各推薦店家招商成效
     */
    private List<StoreReferralPerformance> storePerformances;
    
    @Data
    @Builder
    public static class DailyActivation {
        private LocalDate date;
        private Integer activatedStoreCount;
    }
    
    @Data
    @Builder
    public static class StoreReferralPerformance {
        private String referrerStoreId;
        private String referrerStoreName;
        private Integer referralCodeCount;
        private Integer totalReferralCount;
        private Integer currentPeriodReferralCount;
        private Integer weeklyReferralCount;
        private Integer previousWeeklyReferralCount;
        private Integer weeklyReferralGrowthCount;
        private BigDecimal weeklyGrowthRate;
        private Integer monthlyReferralCount;
        private Integer previousMonthlyReferralCount;
        private Integer monthlyReferralGrowthCount;
        private BigDecimal monthlyGrowthRate;
        private Integer activatedStoreCount;
        private LocalDate lastActivatedDate;
        private Integer rank;
    }
}
