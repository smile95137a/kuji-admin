package com.group.admin.req.draw;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 抽獎請求 DTO
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Data
public class DrawReq {
    
    /**
     * 抽獎商品ID (UUID)
     */
    @NotNull(message = "商品ID不可為空")
    private String lotteryId;
    
    /**
     * 消費類型: gold/bonus
     * gold = 儲值金
     * bonus = 紅利金
     */
    @NotNull(message = "消費類型不可為空")
    private String costType;
    
    /**
     * 是否跳過保護時間檢查（管理員專用）
     */
    private Boolean skipLockCheck = false;
}
