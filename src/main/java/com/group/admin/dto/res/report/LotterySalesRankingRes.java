package com.group.admin.dto.res.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 商品銷售排行報表回應
 */
@Data
@Builder
@Schema(description = "商品銷售排行報表回應")
public class LotterySalesRankingRes {

    /**
     * 符合條件的商品總數（不受 limit 影響）
     */
    @Schema(description = "符合條件的商品總數（不受 limit 影響）", example = "15")
    private Integer totalRecords;

    /**
     * 排行榜清單，依 sortBy 降序排列
     */
    @Schema(description = "排行榜清單，依 sortBy 降序排列")
    private List<LotterySalesItem> items;

    /**
     * 單一商品銷售排行項目
     */
    @Data
    @Builder
    @Schema(description = "商品銷售排行項目")
    public static class LotterySalesItem {

        /**
         * 商品 UUID
         */
        @Schema(description = "商品 UUID", example = "550e8400-e29b-41d4-a716-446655440000")
        private String lotteryId;

        /**
         * 商品標題
         */
        @Schema(description = "商品標題", example = "鬼滅之刃 一番賞 Vol.1")
        private String lotteryTitle;

        /**
         * 所屬店家名稱
         */
        @Schema(description = "所屬店家名稱", example = "動漫星球")
        private String storeName;

        /**
         * 全生命期已抽籤數（lottery_ticket.status=DRAWN）
         */
        @Schema(description = "全生命期已抽籤數（lottery_ticket.status=DRAWN）", example = "320")
        private Integer drawCount;

        /**
         * 全生命期有效營收（金幣點數，排除 CANCELLED 訂單）
         */
        @Schema(description = "全生命期有效營收（金幣點數，排除 CANCELLED 訂單）", example = "25600")
        private Long revenue;

        /**
         * 排名（1-based，由 Service 計算）
         */
        @Schema(description = "排名（1-based）", example = "1")
        private Integer rank;
    }
}
