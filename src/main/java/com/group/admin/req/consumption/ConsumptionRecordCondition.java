package com.group.admin.req.consumption;

import com.group.admin.req.common.BaseCondition;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消費紀錄查詢條件
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "消費紀錄查詢條件")
public class ConsumptionRecordCondition extends BaseCondition {
    
    @Schema(description = "用戶 ID", example = "uuid-user-1")
    private String userId;
    
    @Schema(description = "消費類型（DRAW_GOLD/DRAW_BONUS/SHIPPING_FEE）", example = "DRAW_GOLD")
    private String type;
    
    @Schema(description = "相關賞品 ID", example = "uuid-lottery-1")
    private String lotteryId;
    
    @Schema(description = "訂單編號", example = "ORD20260209001")
    private String orderNumber;
}
