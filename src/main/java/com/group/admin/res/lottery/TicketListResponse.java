package com.group.admin.res.lottery;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 票券列表回應 DTO（前台安全版）
 *
 * <p>FR-005, FR-006, SC-001: AVAILABLE 籤只含 ticketNumber + status，不洩漏獎品資訊。
 * DRAWN 籤含完整獎品資訊。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketListResponse {

    private String lotteryId;
    private String gameMode;
    private int totalTickets;
    private int availableCount;
    private int drawnCount;
    private List<TicketView> tickets;

    /**
     * 單一籤位的前台視圖。
     * AVAILABLE: 只有 ticketNumber + status（其餘欄位為 null）。
     * DRAWN: 完整欄位。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TicketView {

        private String id;
        private Integer ticketNumber;
        private String status;

        // ── DRAWN-only fields ──
        /** 刮刮樂模式才有；一番賞/扭蛋為 null */
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
