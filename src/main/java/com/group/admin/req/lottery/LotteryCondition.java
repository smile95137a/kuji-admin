package com.group.admin.req.lottery;

import com.group.admin.req.common.BaseCondition;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品查詢條件
 * 
 * 使用方式：
 * 1. 所有欄位都是可選的
 * 2. Service 層使用 MyBatis Example 動態 SQL
 * 3. 前端不用傳 storeId，由 Controller 自動帶入
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LotteryCondition extends BaseCondition {
    
    /**
     * 店家 ID
     * 
     * ⚠️ 前端不用傳此欄位
     * 後端會從 JWT Token 自動帶入
     * 
     * 如果是 ROLE_ADMIN，此欄位為 null（查詢所有店家）
     */
    private String storeId;
    
    /**
     * 商品名稱（模糊查詢）
     */
    private String title;
    
    /**
     * 狀態：ON_SHELF（上架）/ OFF_SHELF（下架）
     */
    private String status;
    
    /**
     * 分類
     * 例如：OFFICIAL_ICHIBAN（官方一番賞）、GACHA（扭蛋）等
     */
    private String category;
    
    /**
     * 主題（模糊查詢）
     * 例如：火影忍者、進擊的巨人、排球少年等
     */
    private String theme;
    
    /**
     * 每抽價格（最小值）
     */
    private Long priceMin;
    
    /**
     * 每抽價格（最大值）
     */
    private Long priceMax;
    
    /**
     * 總抽數（最小值）
     */
    private Integer totalQuantityMin;
    
    /**
     * 總抽數（最大值）
     */
    private Integer totalQuantityMax;
}
