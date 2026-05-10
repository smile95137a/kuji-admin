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
     * 期間中獎件數
     */
    private Integer totalWinningCount;
    
    /**
     * 熱門商品（按中獎件數）
     */
    private List<HotLottery> hotLotteries;
    
    /**
     * 逐筆中獎明細
     */
    private List<WinningDetail> winningDetails;
    
    @Data
    @Builder
    public static class HotLottery {
        private String lotteryId;
        private String lotteryTitle;
        private Integer drawCount;
        private Integer winningCount;
    }
    
    @Data
    @Builder
    public static class WinningDetail {
        private String ticketId;
        private LocalDate drawDate;
        private String drawTime;
        private String userId;
        private String userDisplayName;
        private String lotteryId;
        private String lotteryTitle;
        private String lotteryImageUrl;
        private String prizeId;
        private String prizeName;
        private String prizeLevel;
        private String prizeImageUrl;
        private Integer drawCount;
        private Integer ticketNumber;
        private Integer revealedNumber;
        private String storeId;
        private String storeName;
    }
}
