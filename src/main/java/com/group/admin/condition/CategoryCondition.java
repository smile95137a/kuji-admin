package com.group.admin.condition;

import com.group.admin.req.common.BaseCondition;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品類別查詢條件
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CategoryCondition extends BaseCondition {
    
    /**
     * 類別類型（一番賞、扭蛋、刮刮樂等）
     */
    private String category;
    
    /**
     * 主題（火影忍者、進擊的巨人、排球少年等）
     */
    private String theme;
    
    /**
     * 標籤
     */
    private String tags;
    
    /**
     * 商品狀態（ON_SHELF/OFF_SHELF）
     */
    private String status;
    
    /**
     * 關鍵字搜尋（搜尋主題或標籤）
     */
    private String keyword;

    /**
     * 名稱模糊搜尋（contains，前端使用 name 欄位時對應此欄位）
     */
    private String name;
}
