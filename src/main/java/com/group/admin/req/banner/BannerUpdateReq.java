package com.group.admin.req.banner;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Banner 更新請求
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Schema(description = "更新 Banner 請求")
public class BannerUpdateReq {
    
    @Schema(description = "綁定店家 ID", example = "uuid-store-123")
    private String storeId;
    
    @Schema(description = "Banner 標題", example = "春節限時優惠")
    private String title;
    
    @Schema(description = "圖片 URL", example = "https://example.com/banner.jpg")
    private String imageUrl;
    
    @Schema(description = "排序", example = "1")
    private Integer orderNum;
    
    @Schema(description = "狀態（PUBLISHED/UNPUBLISHED）", example = "PUBLISHED")
    private String status;
    
    @Schema(description = "開始顯示時間", example = "2026-01-10T00:00:00")
    private LocalDateTime startTime;
    
    @Schema(description = "結束顯示時間", example = "2026-02-10T23:59:59")
    private LocalDateTime endTime;
}
