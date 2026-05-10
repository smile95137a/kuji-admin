package com.group.admin.req.lottery;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 前台商品列表查詢請求
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Schema(description = "前台商品列表查詢請求")
public class LotteryListReq {

    @Schema(description = "頁碼（從1開始）", example = "1", defaultValue = "1")
    private Integer page = 1;

    @Schema(description = "每頁筆數（最大50）", example = "20", defaultValue = "20")
    private Integer pageSize = 20;

    @Schema(description = "商品分類：OFFICIAL_ICHIBAN/GACHA/TRADING_CARD/CUSTOM_GACHA", example = "OFFICIAL_ICHIBAN")
    private String category;

    @Schema(description = "店家ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String storeId;

    @Schema(description = "商品狀態過濾", example = "ON_SHELF")
    private String status;

    @Schema(description = "關鍵字搜尋（商品名稱或標籤）", example = "鬼滅之刃")
    private String keyword;

    @Schema(description = "排序：HOT / NEW / PRICE_ASC / PRICE_DESC", example = "HOT", defaultValue = "NEW")
    private String sort = "NEW";

    public int getOffset() {
        int p = (page != null && page > 0) ? page : 1;
        int ps = (pageSize != null && pageSize > 0) ? Math.min(pageSize, 50) : 20;
        return (p - 1) * ps;
    }

    public int getEffectivePageSize() {
        return (pageSize != null && pageSize > 0) ? Math.min(pageSize, 50) : 20;
    }
}
