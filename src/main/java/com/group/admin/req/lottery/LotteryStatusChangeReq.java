package com.group.admin.req.lottery;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 商品狀態變更請求 DTO
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Schema(description = "商品狀態變更請求")
public class LotteryStatusChangeReq {

    @NotBlank(message = "目標狀態不可為空")
    @Schema(description = "目標狀態", example = "ON_SHELF",
            allowableValues = {"ON_SHELF", "OFF_SHELF", "FORCED_OFF", "DELETED"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String targetStatus;

    @Schema(description = "原因（強制下架時必填）", example = "違規商品")
    private String reason;
}
