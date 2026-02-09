package com.group.admin.req.lottery;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 獎項建立請求 DTO
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Schema(description = "獎項建立請求")
public class LotteryPrizeCreateReq {

    /**
     * 所屬抽獎活動ID
     * 
     * ⚠️ 不加 @NotBlank 驗證，因為：
     * - 整合創建時由後端自動帶入
     * - 單獨創建時必須明確指定
     */
    @Schema(description = "所屬抽獎活動ID（整合創建時後端自動帶入）", 
            example = "550e8400-e29b-41d4-a716-446655440000", 
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String lotteryId;

    /**
     * 獎項名稱
     */
    @NotBlank(message = "獎項名稱不可為空")
    @Size(max = 255, message = "獎項名稱最多255字")
    @Schema(description = "獎項名稱", example = "炭治郎公仔", requiredMode = Schema.RequiredMode.REQUIRED)
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
     * A/B/C/D/E/F/G/LAST/GRAND
     */
    @Schema(description = "獎項等級：A/B/C/D/E/F/G/LAST/GRAND", example = "A")
    private String level;

    /**
     * 籤號（刮刮樂模式使用）
     */
    @Schema(description = "籤號（刮刮樂模式使用）", example = "01")
    private String prizeNumber;

    /**
     * 總數量
     */
    @NotNull(message = "獎項數量不可為空")
    @Min(value = 1, message = "獎項數量至少為1")
    @Schema(description = "總數量", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer quantity;

    /**
     * 權重（抽獎機率權重，數字越大機率越高）
     */
    @Schema(description = "抽獎權重（用於隨機抽取，數字越大機率越高）", example = "10")
    private Integer weight;

    /**
     * 獎項類型
     * physical: 實體獎品
     * digital: 數位獎品
     * point: 點數獎品
     */
    @Schema(description = "獎項類型：physical/digital/point", example = "physical")
    private String prizeType;

    /**
     * 點數金額（若為點數獎品）
     */
    @Schema(description = "點數金額（僅 point 類型需要）", example = "100")
    private Long pointValue;

    /**
     * 是否為最後賞
     */
    @Schema(description = "是否為最後賞", example = "false")
    private Boolean isLastPrize;

    /**
     * 是否為大賞（影響自動降價）
     */
    @Schema(description = "是否為大賞（會觸發降價）", example = "true")
    private Boolean isGrandPrize;

    /**
     * 顯示排序
     */
    @Schema(description = "顯示排序", example = "1")
    private Integer orderNum;
}
