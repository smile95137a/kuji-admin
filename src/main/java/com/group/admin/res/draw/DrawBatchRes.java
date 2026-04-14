package com.group.admin.res.draw;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 統一批次抽獎回應（包含保護時間、免單、指定大獎等資訊）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DrawBatchRes {

    private String playMode;
    private String gameMode;

    /** 各次抽獎結果 */
    private List<DrawItemRes> draws;

    /** 保護結束時間（ISO-8601，null=扭蛋無保護）*/
    private String protectionEndTime;

    /** 是否觸發免單 */
    private Boolean freeDrawTriggered;

    /** 免單退款金額 */
    private Long freeDrawRefundAmount;

    // ---- SCRATCH_PLAYER 指定大獎相關 ----

    /** 是否需要先指定大獎位置（開套者）*/
    private Boolean designationRequired;

    /** 指定要求說明訊息 */
    private String designationMessage;

    /** 可選的 revealedNumber 列表（供開套者選擇）*/
    private List<Integer> availableNumbers;

    /** 大獎清單（告知前端需指定幾個位置）*/
    private List<Map<String, Object>> grandPrizes;

    /** 是否等待開套者指定（非開套者用）*/
    private Boolean designationPending;

    /** 指定截止時間（ISO-8601）*/
    private String openerDeadline;
}
