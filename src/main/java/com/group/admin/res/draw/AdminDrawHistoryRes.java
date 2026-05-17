package com.group.admin.res.draw;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 後台抽獎歷史查詢回應
 */
@Data
@Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class AdminDrawHistoryRes {

    private int page;
    private int size;
    private long total;
    private int totalPages;
    private List<DrawRecordItem> records;
    private DrawSummary summary;

    @Data
    @Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DrawRecordItem {
        private String id;
        private String lotteryId;
        private String userId;
        private String prizeId;
        private String prizeName;
        private String prizeLevel;
        private String prizeImageUrl;
        private Boolean isLastPrize;
        private Boolean isOpenerDraw;
        private Boolean triggeredFreeDraw;
        private Integer openerDrawCount;
        private Long freeDrawRefundAmount;
        private String costType;
        private Long costAmount;
        private String status;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DrawSummary {
        private long totalDraws;
        private long successDraws;
        private long failedDraws;
        private long totalRevenue;
        private int remainingDraws;
    }
}
