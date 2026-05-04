package com.group.admin.dto.res.report;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 平台營收總覽報表回應
 */
@Data
@Builder
public class PlatformRevenueReportRes {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /**
     * 期間內儲值總額（RECHARGE + GOLD）
     */
    private Long totalRecharge;

    /**
     * 期間內消費總額（DRAW，含 GOLD/BONUS，取絕對值）
     */
    private Long totalSpend;

    /**
     * 淨收入 = totalRecharge - totalSpend
     */
    private Long netRevenue;

    /**
     * 期間內抽獎次數
     */
    private Long drawCount;

    private SpendByType spendByType;

    private BigDecimal rechargeGrowthRate;

    private BigDecimal spendGrowthRate;

    private List<DailyRevenueItem> dailyRevenue;

    private List<StoreBreakdownItem> storeBreakdown;

    @Data
    @Builder
    public static class SpendByType {
        private Long gold;
        private Long bonus;
    }

    @Data
    @Builder
    public static class DailyRevenueItem {
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate date;
        private Long recharge;
        private Long spend;
        private Long net;
    }

    @Data
    @Builder
    public static class StoreBreakdownItem {
        private String storeId;
        private String storeName;
        private Long totalSpend;
        private Long drawCount;
    }
}
