package com.group.admin.res.lottery;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 獎項響應 DTO
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Schema(description = "獎項響應")
public class LotteryPrizeRes {

    /**
     * 獎項ID
     */
    @Schema(description = "獎項ID (UUID)", example = "uuid-prize-id")
    private String id;

    /**
     * 所屬抽獎活動ID
     */
    @Schema(description = "所屬抽獎活動ID (UUID)", example = "uuid-lottery-id")
    private String lotteryId;

    /**
     * 獎項名稱
     */
    @Schema(description = "獎項名稱", example = "炭治郎公仔")
    private String name;

    /**
     * 獎項描述
     */
    @Schema(description = "獎項描述", example = "約20cm高的炭治郎精緻公仔")
    private String description;

    /**
     * 獎項詳細內容
     */
    @Schema(description = "獎項詳細內容（HTML 或富文本）")
    private String content;

    /**
     * 獎項圖片URL
     */
    @Schema(description = "獎項圖片URL", example = "https://example.com/images/tanjiro.jpg")
    private String imageUrl;

    /**
     * 獎項等級
     */
    @Schema(description = "獎項等級", example = "A")
    private String level;

    /**
     * 等級中文名稱
     */
    @Schema(description = "等級中文", example = "A賞")
    private String levelName;

    /**
     * 籤號
     */
    @Schema(description = "籤號", example = "01")
    private String prizeNumber;

    /**
     * 總數量
     */
    @Schema(description = "總數量", example = "1")
    private Integer quantity;

    /**
     * 剩餘數量
     */
    @Schema(description = "剩餘數量", example = "1")
    private Integer remaining;

    /**
     * 已抽出數量
     */
    @Schema(description = "已抽出數量", example = "0")
    private Integer drawnCount;

    /**
     * 抽中權重
     */
    @Schema(description = "抽中權重", example = "1")
    private Integer weight;

    /**
     * 獎項類型
     */
    @Schema(description = "獎項類型", example = "physical")
    private String prizeType;

    /**
     * 獎項類型中文
     */
    @Schema(description = "獎項類型中文", example = "實體獎品")
    private String prizeTypeName;

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

    /**
     * 建立時間
     */
    @Schema(description = "建立時間")
    private LocalDateTime createdAt;

    /**
     * 更新時間
     */
    @Schema(description = "更新時間")
    private LocalDateTime updatedAt;
}
