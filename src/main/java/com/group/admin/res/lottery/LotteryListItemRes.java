package com.group.admin.res.lottery;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 前台商品列表項目（簡化版）
 * 
 * 用於前台商品列表顯示，只包含必要資訊
 * 不包含詳細描述、獎品等資訊
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "前台商品列表項目")
public class LotteryListItemRes {

    @Schema(description = "商品 ID")
    private String id;

    @Schema(description = "店家 ID")
    private String storeId;

    @Schema(description = "店家名稱")
    private String storeName;

    @Schema(description = "商品標題")
    private String title;

    @Schema(description = "商品主圖")
    private String imageUrl;

    @Schema(description = "商品分類")
    private String category;

    @Schema(description = "商品分類中文")
    private String categoryName;

    @Schema(description = "每抽價格")
    private Long pricePerDraw;

    @Schema(description = "當前價格（折扣後）")
    private Long currentPrice;

    @Schema(description = "總抽數")
    private Integer maxDraws;

    @Schema(description = "剩餘抽數")
    private Integer remainingDraws;

    @Schema(description = "商品狀態")
    private String status;

    @Schema(description = "建立時間")
    private String createdAt;

    @Schema(description = "更新時間")
    private String updatedAt;

    @Schema(description = "活動開始時間")
    private String startTime;

    @Schema(description = "活動結束時間")
    private String endTime;

    /**
     * 從 LotteryRes 轉換
     */
    public static LotteryListItemRes from(LotteryRes res) {
        return LotteryListItemRes.builder()
                .id(res.getId())
                .storeId(res.getStoreId())
                .storeName(res.getStoreName())
                .title(res.getTitle())
                .imageUrl(res.getImageUrl())
                .category(res.getCategory())
                .categoryName(res.getCategoryName())
                .pricePerDraw(res.getPricePerDraw())
                .currentPrice(res.getCurrentPrice())
                .maxDraws(res.getMaxDraws())
                .remainingDraws(res.getRemainingDraws())
                .status(res.getStatus())
                .createdAt(res.getCreatedAt() != null ? res.getCreatedAt().toString() : null)
                .updatedAt(res.getUpdatedAt() != null ? res.getUpdatedAt().toString() : null)
                .startTime(res.getStartTime() != null ? res.getStartTime().toString() : null)
                .endTime(res.getEndTime() != null ? res.getEndTime().toString() : null)
                .build();
    }
}
