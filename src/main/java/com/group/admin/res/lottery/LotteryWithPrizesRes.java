package com.group.admin.res.lottery;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品與獎品整合回應 DTO
 * 
 * 一支 API 返回完整的商品和獎品資訊。
 * 
 * @author KUJI Team
 * @since 2025-12-25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "商品與獎品整合回應（包含商品詳情和獎品列表）")
public class LotteryWithPrizesRes {
    
    // ==================== 商品基本資訊 ====================
    
    @Schema(description = "商品 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;
    
    @Schema(description = "所屬店家 ID", example = "store-uuid")
    private String storeId;
    
    @Schema(description = "店家名稱", example = "KUJI 一番賞專賣店")
    private String storeName;
    
    @Schema(description = "商品名稱", example = "鬼滅之刃一番賞")
    private String title;
    
    @Schema(description = "商品描述", example = "限量發售的鬼滅之刃一番賞")
    private String description;
    
    @Schema(description = "商品主圖 URL", example = "https://example.com/images/kimetsu.jpg")
    private String imageUrl;
    
    @Schema(description = "商品分類", example = "OFFICIAL_ICHIBAN")
    private String category;
    
    @Schema(description = "自製賞子類型", example = "LOTTERY_MODE")
    private String subCategory;
    
    @Schema(description = "每抽價格", example = "80")
    private Long pricePerDraw;
    
    @Schema(description = "折扣價格", example = "60")
    private Long discountedPrice;
    
    @Schema(description = "是否啟用自動降價", example = "true")
    private Boolean autoDiscountEnabled;
    
    @Schema(description = "總抽數", example = "100")
    private Integer totalDraws;
    
    @Schema(description = "剩餘抽數", example = "85")
    private Integer remainingDraws;
    
    @Schema(description = "狀態：ON_SHELF/OFF_SHELF/SOLD_OUT", example = "ON_SHELF")
    private String status;
    
    @Schema(description = "預計上架時間")
    private LocalDateTime scheduledAt;
    
    @Schema(description = "建立時間")
    private LocalDateTime createdAt;
    
    @Schema(description = "更新時間")
    private LocalDateTime updatedAt;
    
    // ==================== 獎品列表 ====================
    
    @Schema(description = "獎品列表（完整的獎品資訊）")
    private List<LotteryPrizeRes> prizes;
    
    // ==================== 統計資訊 ====================
    
    @Schema(description = "獎品總數量", example = "26")
    private Integer totalPrizeCount;
    
    @Schema(description = "剩餘獎品數量", example = "18")
    private Integer remainingPrizeCount;
    
    @Schema(description = "抽獎進度百分比", example = "30.77")
    private Double progressPercentage;
}
