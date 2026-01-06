package com.group.admin.req.news;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * News 更新請求
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Schema(description = "更新最新消息請求")
public class NewsUpdateReq {
    
    @Schema(description = "標題", example = "春節活動開跑！")
    private String title;
    
    @Schema(description = "內文（長文）", example = "春節期間推出限定活動...")
    private String content;
    
    @Schema(description = "封面圖片 URL", example = "https://example.com/news.jpg")
    private String imageUrl;
    
    @Schema(description = "狀態（DRAFT/PUBLISHED/ARCHIVED）", example = "PUBLISHED")
    private String status;
    
    @Schema(description = "上架時間", example = "2026-01-10T10:00:00")
    private LocalDateTime scheduledAt;
    
    @Schema(description = "下架時間", example = "2026-02-10T23:59:59")
    private LocalDateTime endTime;
}
