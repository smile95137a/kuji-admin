package com.group.admin.res.lottery;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 抽獎商品響應 DTO
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Schema(description = "抽獎商品響應")
public class LotteryRes {

    /**
     * 商品ID
     */
    @Schema(description = "商品ID (UUID)", example = "uuid-lottery-id")
    private String id;

    /**
     * 所屬店家ID
     */
    @Schema(description = "所屬店家ID (UUID)", example = "uuid-store-id")
    private String storeId;

    /**
     * 店家名稱（關聯查詢）
     */
    @Schema(description = "店家名稱", example = "玩具公仔專賣店")
    private String storeName;

    /**
     * 商品/活動名稱
     */
    @Schema(description = "商品/活動名稱", example = "鬼滅之刃一番賞")
    private String title;

    /**
     * 詳細描述
     */
    @Schema(description = "商品詳細描述", example = "限量發售的鬼滅之刃一番賞")
    private String description;

    /**
     * 商品主圖 URL
     */
    @Schema(description = "商品主圖 URL", example = "https://example.com/images/kimetsu.jpg")
    private String imageUrl;

    /**
     * 商品分類
     */
    @Schema(description = "商品分類", example = "OFFICIAL_ICHIBAN")
    private String category;

    /**
     * 商品分類中文名稱
     */
    @Schema(description = "商品分類中文", example = "官方一番賞")
    private String categoryName;

    /**
     * 自製賞子類型
     */
    @Schema(description = "自製賞子類型", example = "LOTTERY_MODE")
    private String subCategory;

    /**
     * 每抽價格
     */
    @Schema(description = "每抽價格", example = "650")
    private Long pricePerDraw;

    /**
     * 當前價格（考慮折扣後）
     */
    @Schema(description = "當前價格（折扣後）", example = "500")
    private Long currentPrice;

    /**
     * 大獎售完後折扣價
     */
    @Schema(description = "大獎售完後的折扣價", example = "500")
    private Long discountedPrice;

    /**
     * 是否啟用自動降價
     */
    @Schema(description = "是否啟用自動降價", example = "true")
    private Boolean autoDiscountEnabled;

    /**
     * 是否已觸發降價
     */
    @Schema(description = "是否已觸發降價", example = "false")
    private Boolean discountTriggered;

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
     * 目前已抽次數
     */
    @Schema(description = "目前已抽次數", example = "25")
    private Integer totalDraws;

    /**
     * 總抽數上限
     */
    @Schema(description = "總抽數上限（0=無限制）", example = "80")
    private Integer maxDraws;

    /**
     * 剩餘抽數（從獎池計算）
     */
    @Schema(description = "剩餘抽數", example = "55")
    private Integer remainingDraws;

    /**
     * 商品狀態
     */
    @Schema(description = "商品狀態", example = "ON_SHELF")
    private String status;

    /**
     * 狀態中文名稱
     */
    @Schema(description = "狀態中文", example = "已上架")
    private String statusName;

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
     * 建立者ID
     */
    @Schema(description = "建立者ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String createdBy;

    /**
     * 建立時間
     */
    @Schema(description = "建立時間")
    private LocalDateTime createdAt;

    /**
     * 更新時間
     */
    @Schema(description = "更新時間")
    private LocalDateTime updatedAt;

    /**
     * 內部備註
     */
    @Schema(description = "內部備註")
    private String remark;

    /**
     * 獎項總數
     */
    @Schema(description = "獎項總數量", example = "80")
    private Integer totalPrizes;

    /**
     * 剩餘獎項數
     */
    @Schema(description = "剩餘獎項數量", example = "55")
    private Integer remainingPrizes;
    
    // ==================== 新增欄位（前台商品詳情需要）====================
    
    /**
     * 保護抽數（大獎保底機制）
     */
    @Schema(description = "保護抽數（幾抽內必出大獎）", example = "50")
    private Integer protectionDraws;
    
    /**
     * 保護時間（分鐘）
     */
    @Schema(description = "保護時間（分鐘，搶購保護）", example = "10")
    private Integer protectionMinutes;
    
    /**
     * 商品內容詳情（CKEditor 富文本內容）
     */
    @Schema(description = "商品內容詳情（HTML 格式）", example = "<p>商品詳細說明...</p>")
    private String content;
    
    /**
     * 遊戲模式
     */
    @Schema(description = "遊戲模式", example = "NORMAL")
    private String gameMode;
    
    /**
     * 是否啟用免費抽
     */
    @Schema(description = "是否啟用免費抽", example = "false")
    private Boolean freeDrawEnabled;
    
    /**
     * 指定號碼（籤況顯示用）
     */
    @Schema(description = "指定號碼（逗號分隔）", example = "1,5,10,15")
    private String designatedPrizeNumbers;
    
    /**
     * 是否已生成抽籤號碼
     */
    @Schema(description = "是否已生成抽籤號碼", example = "true")
    private Boolean ticketsGenerated;
}
