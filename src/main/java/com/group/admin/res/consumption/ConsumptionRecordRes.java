package com.group.admin.res.consumption;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 消費紀錄回應 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "消費紀錄回應")
public class ConsumptionRecordRes {
    
    @Schema(description = "紀錄 ID")
    private String id;
    
    @Schema(description = "用戶 ID")
    private String userId;
    
    @Schema(description = "消費類型")
    private String type;
    
    @Schema(description = "消費類型中文")
    private String typeName;
    
    @Schema(description = "相關賞品 ID")
    private String lotteryId;
    
    @Schema(description = "賞品名稱")
    private String lotteryTitle;
    
    @Schema(description = "相關訂單 ID")
    private String orderId;
    
    @Schema(description = "訂單編號")
    private String orderNumber;
    
    @Schema(description = "消費金幣數")
    private Long goldAmount;
    
    @Schema(description = "消費紅利數")
    private Long bonusAmount;
    
    @Schema(description = "消費說明")
    private String description;
    
    @Schema(description = "建立時間")
    private LocalDateTime createdAt;
}
