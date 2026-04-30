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
    
    @Schema(description = "遊戲模式：LOTTERY_MODE/SCRATCH_MODE", example = "LOTTERY_MODE")
    private String playMode;
    
    @Schema(description = "每抽價格", example = "80")
    private Long pricePerDraw;
    
    @Schema(description = "折扣價格", example = "60")
    private Long discountedPrice;
    
    @Schema(description = "是否啟用自動降價", example = "true")
    private Boolean autoDiscountEnabled;
    
    @Deprecated
    @Schema(description = "已廢棄欄位", example = "true", deprecated = true)
    private Boolean allowMultiDraw;
    
    @Deprecated
    @Schema(description = "已廢棄欄位", example = "[10, 50]", deprecated = true)
    private List<Integer> multiDrawOptions;
    
    @Schema(description = "是否啟用紅利", example = "true")
    private Boolean bonusEnabled;
    
    @Schema(description = "每抽贈送紅利", example = "5")
    private Integer bonusPointsPerDraw;
    
    @Schema(description = "每抽消耗紅利", example = "100")
    private Integer bonusCostPerDraw;
    
    @Schema(description = "標籤列表", example = "[\"新品\", \"一番賞\"]")
    private List<String> tags;
    
    @Schema(description = "圖庫圖片", example = "[\"url1\", \"url2\"]")
    private List<String> galleryImages;
    
    @Schema(description = "商品主題", example = "鬼滅之刃")
    private String theme;
    
    @Schema(description = "熱門度", example = "999")
    private Integer hotCount;

    @Schema(description = "付款方式：GOLD/BONUS", example = "GOLD")
    private String paymentType;

    @Schema(description = "免費抽門檻（僅 CUSTOM_GACHA+SCRATCH_MODE；NULL=未啟用）", example = "10")
    private Integer freeDrawThreshold;

    @Schema(description = "下架策略：GRAND_PRIZE_DRAWN/ALL_DRAWN/MANUAL", example = "ALL_DRAWN")
    private String delistStrategy;
    
    @Schema(description = "顯示排序", example = "1")
    private Integer orderNum;
    
    @Schema(description = "內部備註", example = "這批貨進價較高")
    private String remark;
    
    @Schema(description = "詳細內容（HTML）", example = "<p>活動說明</p>")
    private String content;
    
    @Schema(description = "活動開始時間")
    private LocalDateTime startTime;
    
    @Schema(description = "活動結束時間")
    private LocalDateTime endTime;
    
    @Schema(description = "最大抽數限制", example = "28")
    private Integer maxDraws;
    
    @Schema(description = "總抽數（已抽次數）", example = "0")
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
    
    @Schema(description = "獎品總數量（中獎籤位數）", example = "28")
    private Integer totalPrizeCount;
    
    @Schema(description = "剩餘獎品數量", example = "18")
    private Integer remainingPrizeCount;
    
    @Schema(description = "謝謝惠顧數量（maxDraws - totalPrizeCount）", example = "72")
    private Integer thanksgivingCount;
    
    @Schema(description = "抽獎進度百分比", example = "30.77")
    private Double progressPercentage;
}
