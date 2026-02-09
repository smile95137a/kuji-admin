package com.group.admin.res.news;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * News 回應 DTO
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "最新消息回應")
public class NewsRes {
    
    @Schema(description = "最新消息 ID", example = "uuid-news-123")
    private String id;
    
    @Schema(description = "標題", example = "春節活動開跑！")
    private String title;
    
    @Schema(description = "內文（長文）", example = "春節期間推出限定活動...")
    private String content;
    
    @Schema(description = "封面圖片 URL", example = "https://example.com/news.jpg")
    private String imageUrl;
    
    @Schema(description = "狀態", example = "PUBLISHED")
    private String status;
    
    @Schema(description = "狀態中文", example = "已上架")
    private String statusName;
    
    @Schema(description = "分類", example = "ANNOUNCEMENT")
    private String category;
    
    @Schema(description = "分類中文", example = "公告")
    private String categoryName;
    
    @Schema(description = "是否為重要提醒", example = "false")
    private Boolean important;
    
    @Schema(description = "上架時間", example = "2026-01-10T10:00:00")
    private LocalDateTime scheduledAt;
    
    @Schema(description = "下架時間", example = "2026-02-10T23:59:59")
    private LocalDateTime endTime;
    
    @Schema(description = "建立者 ID", example = "uuid-admin-456")
    private String createdBy;
    
    @Schema(description = "建立時間")
    private LocalDateTime createdAt;
    
    @Schema(description = "最後修改時間")
    private LocalDateTime updatedAt;
}
