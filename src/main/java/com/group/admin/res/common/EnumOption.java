package com.group.admin.res.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Enum 選項回應 DTO
 * 
 * <p>統一的 Enum 選項格式，用於前端下拉選單等</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Enum 選項")
public class EnumOption {
    
    @Schema(description = "顯示文字（中文）", example = "已上架")
    private String label;
    
    @Schema(description = "實際值（英文代碼）", example = "ON_SHELF")
    private String value;
    
    @Schema(description = "額外說明（可選）", example = "商品已上架，前台可見")
    private String description;
}
