package com.group.admin.req.draw;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 選號抽獎請求 DTO（刮刮樂模式）
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Data
public class SelectNumberDrawReq {
    
    /**
     * 抽獎商品ID (UUID)
     */
    @NotNull(message = "商品ID不可為空")
    private String lotteryId;
    
    /**
     * 選擇的號碼
     */
    @NotNull(message = "選號不可為空")
    private String prizeNumber;
    
    /**
     * 消費類型: gold/bonus
     */
    @NotNull(message = "消費類型不可為空")
    private String costType;
}
