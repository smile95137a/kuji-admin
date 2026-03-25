package com.group.admin.res.draw;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 後台抽獎歷史回應 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDrawHistoryRes {

    private Integer page;
    private Integer size;
    private Long total;
    private Integer totalPages;
    private List<DrawRecordItem> records;
    private DrawSummary summary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
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
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DrawSummary {
        private Long totalDraws;
        private Long successDraws;
        private Long failedDraws;
        private Long totalRevenue;
        private Integer remainingDraws;
    }
}
