package com.group.admin.req.news;

import com.group.admin.req.common.BaseCondition;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * News 查詢條件
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "最新消息查詢條件")
public class NewsCondition extends BaseCondition {
    
    @Schema(description = "標題（模糊查詢）", example = "活動")
    private String title;
    
    @Schema(description = "狀態（DRAFT/PUBLISHED/ARCHIVED）", example = "PUBLISHED")
    private String status;
}
