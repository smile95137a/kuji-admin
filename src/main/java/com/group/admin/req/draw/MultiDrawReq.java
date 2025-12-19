package com.group.admin.req.draw;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 多連抽請求 DTO
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Data
public class MultiDrawReq {
    
    /**
     * 抽獎商品ID (UUID)
     */
    @NotNull(message = "商品ID不可為空")
    private String lotteryId;
    
    /**
     * 連抽次數（必須是商品允許的選項之一，如 10, 50）
     */
    @NotNull(message = "連抽次數不可為空")
    @Min(value = 2, message = "連抽次數至少為2")
    private Integer drawCount;
    
    /**
     * 消費類型: gold/bonus
     */
    @NotNull(message = "消費類型不可為空")
    private String costType;
}
