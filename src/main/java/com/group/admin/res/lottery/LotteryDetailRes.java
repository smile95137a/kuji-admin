package com.group.admin.res.lottery;

import com.group.admin.service.LotteryTicketService.DesignatedWinningNumber;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "抽獎頁面詳細資料")
public class LotteryDetailRes {

    @Schema(description = "抽獎主資料")
    private LotteryRes lottery;

    @Schema(description = "獎項列表")
    private List<LotteryPrizeRes> prizes;

    @Schema(description = "票券列表")
    private List<LotteryTicketRes> tickets;

    @Schema(description = "目前 session 資訊")
    private SessionInfoRes session;

    @Schema(description = "已指定的大獎號碼")
    private List<DesignatedWinningNumber> designatedWinningNumbers;

    @Schema(description = "BONUS 與保護期說明設定")
    private NoticeConfigRes noticeConfig;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionInfoRes {
        @Schema(description = "是否為開局者")
        private Boolean isOpener;

        @Schema(description = "開局者暱稱")
        private String openerNickname;

        @Schema(description = "保護期結束時間")
        private String protectionEndTime;

        @Schema(description = "session 狀態")
        private String status;

        @Schema(description = "是否可抽")
        private Boolean canDraw;

        @Schema(description = "不可抽的原因")
        private String cannotDrawReason;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NoticeConfigRes {
        @Schema(description = "BONUS 區塊標題")
        private String bonusTitle;

        @Schema(description = "BONUS 區塊說明")
        private String bonusDescription;

        @Schema(description = "保護期區塊標題")
        private String protectionTitle;

        @Schema(description = "保護期區塊說明")
        private String protectionDescription;

        @Schema(description = "多抽紅利階梯")
        private List<BonusTierRes> bonusTiers;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BonusTierRes {
        @Schema(description = "抽數", example = "3")
        private Integer drawCount;

        @Schema(description = "贈送紅利", example = "60")
        private Long bonus;
    }
}
