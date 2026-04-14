package com.group.admin.res.lottery;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 大獎指定狀態回應 DTO（SCRATCH_PLAYER 模式）
 *
 * <p>四種情境：</p>
 * <ol>
 *   <li>非 SCRATCH_PLAYER 模式 → {@code required=false}</li>
 *   <li>無 ACTIVE Session 或已完成指定 → {@code required=false, alreadyDesignated=true/false}</li>
 *   <li>開套玩家尚未指定 → {@code required=true, isOpener=true}，附大獎清單與可選號碼</li>
 *   <li>非開套玩家尚未指定 → {@code required=true, isOpener=false}，附等待訊息</li>
 * </ol>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DesignationCheckResponse {

    /** 是否需要指定大獎位置 */
    private boolean required;

    /** 抽獎活動的遊戲模式 */
    private String gameMode;

    /** 進行中 Session ID（無 Session 則為 null） */
    private String sessionId;

    /** 呼叫者是否為本次 Session 的開套玩家 */
    private boolean isOpener;

    /** 需指定的大獎數量（required=true && isOpener=true 時出現） */
    private Integer requiredDesignationCount;

    /** 大獎獎品清單（required=true && isOpener=true 時出現） */
    private List<GrandPrize> grandPrizes;

    /** 所有 AVAILABLE 票券的 revealedNumber 清單（required=true && isOpener=true 時出現） */
    private List<Integer> availableRevealedNumbers;

    /** 開套玩家暱稱（required=true && isOpener=false 時出現） */
    private String openerNickname;

    /** 等待訊息（required=true && isOpener=false 時出現） */
    private String message;

    /** 指定已完成標誌（SCRATCH_PLAYER 且指定完成後出現） */
    private Boolean alreadyDesignated;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GrandPrize {
        private String prizeId;
        private String prizeName;
        private String prizeLevel;
        private String prizeImageUrl;
    }
}
