package com.group.admin.res.lottery;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 抽獎商品列表項目 DTO（簡化版，用於列表顯示）
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Schema(description = "抽獎商品列表項目")
public class LotteryListRes {

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
     * 店家名稱
     */
    @Schema(description = "店家名稱", example = "玩具公仔專賣店")
    private String storeName;

    /**
     * 商品/活動名稱
     */
    @Schema(description = "商品/活動名稱", example = "鬼滅之刃一番賞")
    private String title;

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
     * 每抽價格
     */
    @Schema(description = "每抽價格", example = "650")
    private Long pricePerDraw;

    /**
     * 當前價格
     */
    @Schema(description = "當前價格", example = "500")
    private Long currentPrice;

    /**
     * 商品狀態
     */
    @Schema(description = "商品狀態", example = "ON_SHELF")
    private String status;

    /**
     * 狀態中文
     */
    @Schema(description = "狀態中文", example = "已上架")
    private String statusName;

    /**
     * 目前已抽次數
     */
    @Schema(description = "目前已抽次數", example = "25")
    private Integer totalDraws;

    /**
     * 剩餘抽數
     */
    @Schema(description = "剩餘抽數", example = "55")
    private Integer remainingDraws;

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

    /**
     * 顯示排序
     */
    @Schema(description = "顯示排序", example = "1")
    private Integer orderNum;

    /**
     * 建立時間
     */
    @Schema(description = "建立時間")
    private LocalDateTime createdAt;
}
