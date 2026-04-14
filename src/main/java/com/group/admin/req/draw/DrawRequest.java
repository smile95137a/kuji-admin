package com.group.admin.req.draw;

import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.List;

/**
 * 統一抽獎請求 DTO
 * <p>適用於所有商品分類（GACHA / OFFICIAL_ICHIBAN / TRADING_CARD / CUSTOM_GACHA）</p>
 */
@Data
public class DrawRequest {

    /** 抽獎次數（扭蛋隨機抽用）*/
    @Min(value = 1, message = "count 必須至少為 1")
    private Integer count = 1;

    /** 選中的籤位號碼（一番賞/卡牌/刮刮樂：選號模式）*/
    private Integer ticketNumber;

    /** 指定票券 UUID 列表（批量選號模式）*/
    private List<String> tickets;
}
