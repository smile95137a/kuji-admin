package com.group.admin.dto.res.report;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 營業額報表回應
 */
@Data
@Builder
public class RevenueReportRes {
    
    /**
     * 報表期間開始
     */
    private LocalDate startDate;
    
    /**
     * 報表期間結束
     */
    private LocalDate endDate;
    
    /**
     * 總營業額
     */
    private BigDecimal totalRevenue;
    
    /**
     * 總訂單數
     */
    private Integer totalOrders;
    
    /**
     * 總抽獎次數
     */
    private Integer totalDraws;
    
    /**
     * 平均訂單金額
     */
    private BigDecimal avgOrderAmount;
    
    /**
     * 與上期比較（%）
     */
    private BigDecimal growthRate;
    
    /**
     * 每日營業額明細
     */
    private List<DailyRevenue> dailyDetails;
    
    /**
     * 各店家營業額
     */
    private List<StoreRevenue> storeDetails;
    
    @Data
    @Builder
    public static class DailyRevenue {
        private LocalDate date;
        private BigDecimal revenue;
        private Integer orders;
        private Integer draws;
    }
    
    @Data
    @Builder
    public static class StoreRevenue {
        private String storeId;
        private String storeName;
        private BigDecimal revenue;
        private Integer orders;
        private BigDecimal percentage;
    }
}
