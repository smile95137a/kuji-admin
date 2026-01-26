package com.group.admin.res.lottery;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

/**
 * 前台商品詳情（完整版）
 * 
 * 包含商品完整資訊 + 獎品列表 + 籤位列表
 * 用於商品詳情頁顯示
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "前台商品詳情")
public class LotteryDetailRes {

    @Schema(description = "商品基本資訊")
    private LotteryRes lottery;

    @Schema(description = "獎品列表")
    private List<LotteryPrizeRes> prizes;

    @Schema(description = "籤位列表（前台安全版，未抽籤位不顯示獎品資訊）")
    private List<LotteryTicketRes> tickets;

    @Schema(description = "場次資訊")
    private SessionInfoRes session;

    /**
     * 場次資訊
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionInfoRes {
        @Schema(description = "是否為開套玩家")
        private Boolean isOpener;

        @Schema(description = "開套玩家暱稱")
        private String openerNickname;

        @Schema(description = "保護結束時間")
        private String protectionEndTime;

        @Schema(description = "場次狀態")
        private String status;

        @Schema(description = "是否可以抽獎")
        private Boolean canDraw;

        @Schema(description = "不能抽獎的原因")
        private String cannotDrawReason;
    }
}
