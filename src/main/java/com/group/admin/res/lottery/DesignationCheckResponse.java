package com.group.admin.res.lottery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 大獎指定檢查回應 DTO
 *
 * <p>用於 SCRATCH_PLAYER 模式，檢查開套玩家是否需要指定大獎位置</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DesignationCheckResponse {
    private Boolean required;
    private String gameMode;
    private String sessionId;
    private Boolean isOpener;
    private Integer requiredDesignationCount;
    private List<GrandPrize> grandPrizes;
    private List<Integer> availableRevealedNumbers;
    private String openerNickname;
    private String message;
    private Boolean alreadyDesignated;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GrandPrize {
        private String prizeId;
        private String prizeName;
        private String prizeLevel;
        private Integer quantity;
        private String prizeImageUrl;
    }
}
