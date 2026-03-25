package com.group.admin.res.referral;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "推薦統計回應")
public class ReferralStatsRes {

    @Schema(description = "總推薦人數")
    private Integer totalReferrals;

    @Schema(description = "總獲得獎勵金額")
    private Long totalBonusEarned;

    @Schema(description = "目前啟用的推薦碼")
    private ReferralCodeRes activeCode;

    @Schema(description = "推薦歷史記錄")
    private List<ReferralHistoryItem> referralHistory;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "推薦歷史項目")
    public static class ReferralHistoryItem {

        @Schema(description = "被推薦人 ID")
        private String refereeId;

        @Schema(description = "被推薦人名稱")
        private String refereeUsername;

        @Schema(description = "獎勵金額")
        private Long bonusAmount;

        @Schema(description = "推薦時間")
        private LocalDateTime createdAt;
    }
}
