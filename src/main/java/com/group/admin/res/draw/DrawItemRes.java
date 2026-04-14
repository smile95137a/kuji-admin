package com.group.admin.res.draw;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 單次抽獎結果（統一格式，適用於所有商品分類）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DrawItemRes {

    private Boolean success;
    private String ticketId;
    private Integer ticketNumber;

    /** 刮刮樂專用：刮開後顯示的號碼 */
    private Integer revealedNumber;

    private String prizeId;
    private String prizeLevel;
    private String prizeName;
    private String prizeImageUrl;
    private Boolean isGrandPrize;
    private Boolean isLastPrize;

    /** 是否觸發免單 */
    private Boolean triggeredFreeDraw;

    /** 免單退款金額 */
    private Long refundAmount;

    private String message;

    /** 抽獎消費金額 */
    private Long costAmount;

    /** 消費類型（GOLD/BONUS）*/
    private String costType;

    /** 商品標題（扭蛋模式）*/
    private String lotteryTitle;

    /** 最後賞是否觸發 */
    private Boolean lastPrizeAwarded;
    private String lastPrizeId;
    private String lastPrizeName;
    private String lastPrizeImageUrl;
}
