package com.group.admin.req.lottery;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 抽獎商品查詢請求 DTO
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Schema(description = "抽獎商品查詢請求")
public class LotteryQueryReq {

    /**
     * 店家ID（Admin 可查所有店家，StoreOwner 僅能查詢自己的店家）
     */
    @Schema(description = "店家ID（篩選條件）", example = "uuid-store-id")
    private String storeId;

    /**
     * 商品名稱關鍵字
     */
    @Schema(description = "商品名稱關鍵字搜尋", example = "鬼滅")
    private String keyword;

    /**
     * 商品分類
     */
    @Schema(description = "商品分類：OFFICIAL_ICHIBAN/GACHA/TRADING_CARD/CUSTOM_GACHA", example = "OFFICIAL_ICHIBAN")
    private String category;

    /**
     * 商品狀態
     */
    @Schema(description = "商品狀態：ON_SHELF/OFF_SHELF", example = "ON_SHELF")
    private String status;

    /**
     * 頁碼（從1開始）
     */
    @Schema(description = "頁碼", example = "1")
    private Integer page = 1;

    /**
     * 每頁筆數
     */
    @Schema(description = "每頁筆數", example = "10")
    private Integer size = 10;

    /**
     * 排序欄位
     */
    @Schema(description = "排序欄位：createdAt/orderNum/weight", example = "createdAt")
    private String sortBy = "createdAt";

    /**
     * 排序方向
     */
    @Schema(description = "排序方向：ASC/DESC", example = "DESC")
    private String sortDirection = "DESC";
}
