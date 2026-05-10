package com.group.admin.req.lottery;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 獎項更新請求 DTO
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Schema(description = "獎項更新請求")
public class LotteryPrizeUpdateReq {

    /**
     * 獎項ID（有值=更新，無值=新增）
     */
    @Schema(description = "獎項ID（有值=更新，無值=新增）", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String id;

    /**
     * 獎項名稱
     */
    @Size(max = 255, message = "獎項名稱最多255字")
    @Schema(description = "獎項名稱", example = "炭治郎公仔（限定版）")
    private String name;

    /**
     * 獎項描述
     */
    @Schema(description = "獎項描述", example = "約20cm高的炭治郎精緻公仔")
    private String description;

    /**
     * 獎項詳細內容（HTML 或富文本）
     */
    @Schema(description = "獎項詳細內容")
    private String content;

    /**
     * 獎項圖片URL
     */
    @Schema(description = "獎項圖片URL", example = "https://example.com/images/tanjiro.jpg")
    private String imageUrl;

    /**
     * 獎項等級
     */
    @Schema(description = "獎項等級：A/B/C/D/E/F/G/LAST/GRAND", example = "A")
    private String level;

    /**
     * 籤號
     */
    @Schema(description = "籤號", example = "01")
    private String prizeNumber;

    /**
     * 總數量（增加數量時使用，會同步增加剩餘數量）
     */
    @Min(value = 1, message = "獎項數量至少為1")
    @Schema(description = "總數量", example = "2")
    private Integer quantity;

    /**
     * 抽中權重
     */
    @Min(value = 0, message = "權重不可為負數")
    @Schema(description = "抽中權重", example = "1")
    private Integer weight;

    /**
     * 獎項類型
     */
    @Schema(description = "獎項類型：physical/digital/point", example = "physical")
    private String prizeType;

    /**
     * 點數金額
     */
    @Schema(description = "點數金額", example = "100")
    private Long pointValue;

    /**
     * 是否為最後賞
     */
    @Schema(description = "是否為最後賞", example = "false")
    private Boolean isLastPrize;

    /**
     * 是否為大賞
     */
    @Schema(description = "是否為大賞", example = "true")
    private Boolean isGrandPrize;

    /**
     * 顯示排序
     */
    @Schema(description = "顯示排序", example = "1")
    private Integer orderNum;
}
