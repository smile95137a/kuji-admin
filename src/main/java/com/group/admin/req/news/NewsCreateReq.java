package com.group.admin.req.news;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * News 新增請求
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Schema(description = "新增最新消息請求")
public class NewsCreateReq {
    
    @NotBlank(message = "標題不能為空")
    @Schema(description = "標題", example = "春節活動開跑！", required = true)
    private String title;
    
    @NotBlank(message = "內文不能為空")
    @Schema(description = "內文（長文）", example = "春節期間推出限定活動...", required = true)
    private String content;
    
    @Schema(description = "封面圖片 URL", example = "https://example.com/news.jpg")
    private String imageUrl;
    
    @Schema(description = "狀態（DRAFT/PUBLISHED）", example = "DRAFT")
    private String status;
    
    @Schema(description = "上架時間", example = "2026-01-10T10:00:00")
    private LocalDateTime scheduledAt;
    
    @Schema(description = "下架時間", example = "2026-02-10T23:59:59")
    private LocalDateTime endTime;
}
