package com.group.admin.res.lottery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 籤位列表回應 DTO（前台用）
 *
 * <p>包含籤位統計和安全過濾後的籤位清單</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketListResponse {
    private String lotteryId;
    private String gameMode;
    private Integer totalTickets;
    private Integer availableCount;
    private Integer drawnCount;
    private List<TicketView> tickets;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TicketView {
        private Integer ticketNumber;
        private String status;
        // DRAWN-only fields (null for AVAILABLE)
        private Integer revealedNumber;
        private String prizeId;
        private String prizeLevel;
        private String prizeName;
        private String prizeImageUrl;
        private Boolean isGrandPrize;
        private String drawnBy;
        private LocalDateTime drawnAt;
    }
}
