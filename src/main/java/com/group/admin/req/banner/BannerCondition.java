package com.group.admin.req.banner;

import com.group.admin.req.common.BaseCondition;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Banner 查詢條件
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Banner 查詢條件")
public class BannerCondition extends BaseCondition {
    
    @Schema(description = "店家 ID", example = "uuid-store-123")
    private String storeId;
    
    @Schema(description = "標題（模糊查詢）", example = "優惠")
    private String title;
    
    @Schema(description = "狀態（PUBLISHED/UNPUBLISHED）", example = "PUBLISHED")
    private String status;
}
