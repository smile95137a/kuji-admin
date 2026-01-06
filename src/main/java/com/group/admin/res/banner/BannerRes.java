package com.group.admin.res.banner;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Banner 回應 DTO
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Banner 回應")
public class BannerRes {
    
    @Schema(description = "Banner ID", example = "uuid-banner-123")
    private String id;
    
    @Schema(description = "店家 ID", example = "uuid-store-456")
    private String storeId;
    
    @Schema(description = "店家名稱", example = "玩具公仔專賣店")
    private String storeName;
    
    @Schema(description = "Banner 標題", example = "春節限時優惠")
    private String title;
    
    @Schema(description = "圖片 URL", example = "https://example.com/banner.jpg")
    private String imageUrl;
    
    @Schema(description = "排序", example = "1")
    private Integer orderNum;
    
    @Schema(description = "狀態", example = "PUBLISHED")
    private String status;
    
    @Schema(description = "狀態中文", example = "已上架")
    private String statusName;
    
    @Schema(description = "開始顯示時間", example = "2026-01-10T00:00:00")
    private LocalDateTime startTime;
    
    @Schema(description = "結束顯示時間", example = "2026-02-10T23:59:59")
    private LocalDateTime endTime;
    
    @Schema(description = "建立時間")
    private LocalDateTime createdAt;
    
    @Schema(description = "更新時間")
    private LocalDateTime updatedAt;
}
