package com.group.admin.res.draw;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 後台抽獎歷史查詢回應
 */
@Data
public class AdminDrawHistoryRes {

    private int page;
    private int size;
    private long total;
    private int totalPages;
    private List<DrawRecordItem> records;
    private DrawSummary summary;

    @Data
    public static class DrawRecordItem {
        private String id;
        private String lotteryId;
        private String userId;
        private String prizeId;
        private String prizeName;
        private String prizeLevel;
        private String prizeImageUrl;
        private Boolean isLastPrize;
        private String costType;
        private Long costAmount;
        private String status;
        private LocalDateTime createdAt;
    }

    @Data
    public static class DrawSummary {
        private long totalDraws;
        private long successDraws;
        private long failedDraws;
        private long totalRevenue;
        private int remainingDraws;
    }
}
