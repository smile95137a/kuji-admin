package com.group.admin.req.lottery;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 抽獎商品更新請求 DTO
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Schema(description = "抽獎商品更新請求")
public class LotteryUpdateReq {

    /**
     * 商品ID（必填）
     */
    @Schema(description = "商品ID", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    /**
     * 商品/活動名稱
     */
    @Size(max = 255, message = "商品名稱最多255字")
    @Schema(description = "商品/活動名稱", example = "鬼滅之刃一番賞 - 第二彈")
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
     * 商品分類（通常建立後不變更）
     */
    @Schema(description = "商品分類：OFFICIAL_ICHIBAN/GACHA/TRADING_CARD/CUSTOM_GACHA", example = "OFFICIAL_ICHIBAN")
    private String category;

    /**
     * 自製賞子類型
     */
    @Schema(description = "自製賞子類型：LOTTERY_MODE/SCRATCH_MODE", example = "LOTTERY_MODE")
    private String subCategory;

    /**
     * 每抽價格
     */
    @Min(value = 0, message = "價格不可為負數")
    @Schema(description = "每抽價格", example = "650")
    private Long pricePerDraw;

    /**
     * 大獎售完後折扣價
     */
    @Schema(description = "大獎售完後的折扣價", example = "500")
    private Long discountedPrice;

    /**
     * 是否啟用自動降價
     */
    @Schema(description = "是否啟用大獎售完後自動降價", example = "true")
    private Boolean autoDiscountEnabled;

    /**
     * 是否允許多抽
     */
    @Deprecated
    @Schema(description = "已廢棄欄位（後端忽略）", example = "true", deprecated = true)
    private Boolean allowMultiDraw;

    /**
     * 是否啟用紅利金支付
     */
    @Schema(description = "是否啟用紅利金支付", example = "true")
    private Boolean bonusEnabled;

    /**
     * 多抽選項
     */
    @Deprecated
    @Schema(description = "已廢棄欄位（後端忽略）", example = "[10, 50]", deprecated = true)
    private List<Integer> multiDrawOptions;

    /**
     * 標籤列表
     */
    @Schema(description = "標籤列表", example = "[\"熱門\", \"限量\"]")
    private List<String> tags;

    /**
     * 圖庫圖片 URL 列表
     */
    @Schema(description = "圖庫圖片 URL 列表")
    private List<String> galleryImages;

    /**
     * 遊戲模式
     */
    @Schema(description = "遊戲模式：LOTTERY_MODE/SCRATCH_MODE", example = "LOTTERY_MODE")
    private String playMode;

    /**
     * 遊戲子模式（刮刮樂用）
     * SCRATCH_STORE/SCRATCH_PLAYER/RANDOM
     */
    @Schema(description = "遊戲子模式：SCRATCH_STORE/SCRATCH_PLAYER/RANDOM", example = "RANDOM")
    private String gameMode;

    /**
     * 商品狀態
     */
    @Schema(description = "商品狀態：DRAFT/ON_SHELF/OFF_SHELF/IN_PROGRESS/ENDED", example = "ON_SHELF")
    private String status;

    /**
     * 定時上架時間
     */
    @Schema(description = "定時上架時間", example = "2025-01-01T10:00:00")
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
     * 總抽數上限
     */
    @Min(value = 0, message = "抽數上限不可為負數")
    @Schema(description = "總抽數上限（0=無限制）", example = "80")
    private Integer maxDraws;

    /**
     * 顯示排序
     */
    @Schema(description = "顯示排序", example = "1")
    private Integer orderNum;

    /**
     * 推薦權重
     */
    @Schema(description = "推薦權重", example = "10")
    private Integer weight;

    /**
     * 內部備註
     */
    @Schema(description = "內部備註", example = "這批貨進價較高")
    private String remark;

    /**
     * 商品主題分類
     */
    @Schema(description = "商品主題分類（火影忍者、航海王等）", example = "鬼滅之刃")
    private String theme;

    /**
     * 商品詳細內容（HTML 格式）
     */
    @Schema(description = "商品詳細內容（HTML 格式）")
    private String content;

    /**
     * 熱門程度
     */
    @Schema(description = "熱門程度", example = "999")
    private Integer hotCount;

    /**
     * 每抽贈送紅利點數
     */
    @Schema(description = "每抽贈送紅利點數", example = "10")
    private Integer bonusPointsPerDraw;

    /**
     * 每抽消耗紅利點數
     */
    @Schema(description = "每抽消耗紅利點數", example = "200")
    private Integer bonusCostPerDraw;

    /**
     * 付款方式：GOLD / BONUS
     */
    @Schema(description = "付款方式：GOLD/BONUS", example = "GOLD")
    private String paymentType;

    /**
     * 是否啟用免費抽
     */
    @Schema(description = "是否啟用免費抽", example = "false")
    private Boolean freeDrawEnabled;

    /**
     * 免單保護抽數（開套者在此抽數內中大獎可免單）
     */
    @Min(value = 1, message = "免單保護抽數至少為 1")
    @Schema(description = "免單保護抽數（開套者在此抽數內中大獎可免單）", example = "5")
    private Integer protectionDraws;

    /**
     * 免費抽門檻（刮刮樂專用）
     */
    @Schema(description = "免費抽門檻（僅 CUSTOM_GACHA+SCRATCH_MODE；NULL=未啟用；若有值必須>=1）", example = "10")
    private Integer freeDrawThreshold;

    /**
     * 下架策略：GRAND_PRIZE_DRAWN / ALL_DRAWN / MANUAL
     */
    @Schema(description = "下架策略：GRAND_PRIZE_DRAWN/ALL_DRAWN/MANUAL", example = "ALL_DRAWN")
    private String delistStrategy;

    /**
     * 店家指定大獎號碼（SCRATCH_STORE 專用）
     * 更新此欄位時，現有籤位會被刪除並重新生成
     * 格式：逗號分隔或 JSON 陣列，例如 "5,12,30" 或 "[5,12,30]"
     */
    @Schema(description = "店家指定大獎 revealed_number（僅 SCRATCH_STORE；更新時籤位會重新生成；格式：'5,12,30' 或 '[5,12,30]'）",
            example = "[5]")
    private String designatedPrizeNumbers;
}
