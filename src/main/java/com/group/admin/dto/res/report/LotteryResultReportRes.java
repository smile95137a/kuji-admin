package com.group.admin.dto.res.report;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 開獎結果報表回應
 */
@Data
@Builder
public class LotteryResultReportRes {
    
    /**
     * 報表期間開始
     */
    private LocalDate startDate;
    
    /**
     * 報表期間結束
     */
    private LocalDate endDate;
    
    /**
     * 總抽獎次數
     */
    private Integer totalDraws;
    
    /**
     * 總獎品數量
     */
    private Integer totalPrizes;
    
    /**
     * 大獎數量（A、B、Last 獎）
     */
    private Integer bigPrizes;
    
    /**
     * 總抽獎金額
     */
    private BigDecimal totalAmount;
    
    /**
     * 獎品發放統計
     */
    private List<PrizeStats> prizeStats;
    
    /**
     * 一番賞統計
     */
    private List<LotteryStats> lotteryStats;
    
    @Data
    @Builder
    public static class PrizeStats {
        private String prizeLevel;  // A獎、B獎、C獎...
        private Integer totalCount;
        private Integer wonCount;
        private Integer remainCount;
        private BigDecimal wonPercentage;
    }
    
    @Data
    @Builder
    public static class LotteryStats {
        private String lotteryId;
        private String lotteryTitle;
        private String storeName;
        private Integer totalSlots;
        private Integer soldSlots;
        private Integer remainSlots;
        private BigDecimal soldPercentage;
        private BigDecimal revenue;
    }
}
