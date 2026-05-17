package com.group.admin.req.banner;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Banner 新增請求
 */
@Data
@Schema(description = "新增 Banner 請求")
public class BannerCreateReq {

    @Schema(description = "店家 ID，可為空", example = "uuid-store-123")
    private String storeId;

    @NotBlank(message = "標題不可為空")
    @Schema(description = "Banner 標題", example = "春節限時優惠")
    private String title;

    @NotBlank(message = "圖片 URL 不可為空")
    @Schema(description = "圖片 URL", example = "https://example.com/banner.jpg")
    private String imageUrl;

    @Schema(description = "連結 URL", example = "https://example.com/promo")
    private String linkUrl;

    @Schema(description = "顯示排序", example = "1")
    private Integer orderNum;

    @Schema(description = "狀態（PUBLISHED=已上架，UNPUBLISHED=已下架）", example = "UNPUBLISHED")
    private String status;

    @Schema(description = "開始顯示時間", example = "2026-01-10T00:00:00")
    private LocalDateTime startTime;

    @Schema(description = "結束顯示時間", example = "2026-02-10T23:59:59")
    private LocalDateTime endTime;
}
