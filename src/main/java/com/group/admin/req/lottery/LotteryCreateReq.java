package com.group.admin.req.lottery;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 抽獎商品建立請求 DTO
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Schema(description = "抽獎商品建立請求")
public class LotteryCreateReq {

    /**
     * 所屬店家ID
     * 
     * ⚠️ 不加 @NotBlank 驗證，因為：
     * - StoreOwner 可以不傳，後端自動帶入
     * - Admin 必須明確指定，由 Controller 驗證
     */
    @Schema(description = "所屬店家ID（StoreOwner 可不傳，後端自動帶入）", 
            example = "550e8400-e29b-41d4-a716-446655440000", 
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String storeId;

    /**
     * 商品/活動名稱
     */
    @NotBlank(message = "商品名稱不可為空")
    @Size(max = 255, message = "商品名稱最多255字")
    @Schema(description = "商品/活動名稱", example = "鬼滅之刃一番賞", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    /**
     * 詳細描述
     */
    @Schema(description = "商品詳細描述", example = "限量發售的鬼滅之刃一番賞，共有 A~G 獎項")
    private String description;

    /**
     * 商品主圖 URL
     */
    @Schema(description = "商品主圖 URL", example = "https://example.com/images/kimetsu.jpg")
    private String imageUrl;

    /**
     * 商品分類
     * OFFICIAL_ICHIBAN: 官方一番賞
     * GACHA: 扭蛋
     * TRADING_CARD: 卡牌
     * CUSTOM_GACHA: 自製賞
     */
    @NotBlank(message = "商品分類不可為空")
    @Schema(description = "商品分類：OFFICIAL_ICHIBAN/GACHA/TRADING_CARD/CUSTOM_GACHA", 
            example = "OFFICIAL_ICHIBAN", requiredMode = Schema.RequiredMode.REQUIRED)
    private String category;

    /**
     * 自製賞子類型
     * LOTTERY_MODE: 抽籤型
     * SCRATCH_MODE: 刮刮樂型
     */
    @Schema(description = "自製賞子類型：LOTTERY_MODE/SCRATCH_MODE（僅 CUSTOM_GACHA 需要）", example = "LOTTERY_MODE")
    private String subCategory;

    /**
     * 每抽價格
     */
    @NotNull(message = "每抽價格不可為空")
    @Min(value = 0, message = "價格不可為負數")
    @Schema(description = "每抽價格", example = "650", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long pricePerDraw;

    /**
     * 大獎售完後折扣價（若有設定）
     */
    @Schema(description = "大獎售完後的折扣價（留空=不折扣）", example = "500")
    private Long discountedPrice;

    /**
     * 是否啟用自動降價
     */
    @Schema(description = "是否啟用大獎售完後自動降價", example = "true")
    private Boolean autoDiscountEnabled;

    /**
     * 是否允許多抽
     */
    @Schema(description = "是否允許多抽", example = "true")
    private Boolean allowMultiDraw;

    /**
     * 多抽選項
     */
    @Schema(description = "多抽選項列表", example = "[10, 50]")
    private List<Integer> multiDrawOptions;

    /**
     * 定時上架時間
     */
    @Schema(description = "定時上架時間（留空=手動上架）", example = "2025-01-01T10:00:00")
    private LocalDateTime scheduledAt;

    /**
     * 活動開始時間
     */
    @Schema(description = "活動開始時間", example = "2025-01-01T10:00:00")
    private LocalDateTime startTime;

    /**
     * 活動結束時間
     */
    @Schema(description = "活動結束時間", example = "2025-12-31T23:59:59")
    private LocalDateTime endTime;

    /**
     * 總抽數上限（0=無限制）
     */
    @Min(value = 0, message = "抽數上限不可為負數")
    @Schema(description = "總抽數上限（0=無限制）", example = "80")
    private Integer maxDraws;

    /**
     * 顯示排序（數字越小越前面）
     */
    @Schema(description = "顯示排序", example = "1")
    private Integer orderNum;

    /**
     * 推薦權重
     */
    @Schema(description = "推薦權重（用於排序）", example = "10")
    private Integer weight;

    /**
     * 內部備註
     */
    @Schema(description = "內部備註（不對外顯示）", example = "這批貨進價較高")
    private String remark;
}
