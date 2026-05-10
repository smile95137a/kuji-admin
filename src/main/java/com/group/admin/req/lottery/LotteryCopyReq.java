package com.group.admin.req.lottery;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 複製商品請求 DTO
 *
 * @author Kuji Admin
 * @since 2026-01-09
 */
@Data
@Schema(description = "複製商品請求")
public class LotteryCopyReq {

    @NotBlank(message = "來源商品 ID 不可為空")
    @Schema(description = "要複製的來源商品 ID", 
            example = "uuid-string", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String sourceLotteryId;

    @Schema(description = "新商品標題（選填，若為空則自動加上「複製」後綴）", 
            example = "鬼滅之刃一番賞（複製）")
    private String newTitle;

    @Schema(description = "是否重新生成籤號（預設 true）", 
            example = "true")
    private Boolean regenerateTickets = true;

    @Schema(description = "新商品狀態（選填，若為空則預設為 OFF_SHELF）", 
            example = "OFF_SHELF",
            allowableValues = {"DRAFT", "ON_SHELF", "OFF_SHELF"})
    private String newStatus;
}
