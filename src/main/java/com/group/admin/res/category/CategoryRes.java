package com.group.admin.res.category;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品類別回應 DTO
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRes {

    /**
     * 字典資料 ID。由 lottery_theme / lottery_tag 等字典表提供；歷史資料聚合時可能為空。
     */
    private String id;
    
    /**
     * 類別名稱（category 或 theme）
     */
    private String name;
    
    /**
     * 類別類型（category / theme）
     */
    private String type;
    
    /**
     * 商品數量
     */
    private Long productCount;
    
    /**
     * 代表圖片（該類別第一個商品的圖片）
     */
    private String imageUrl;
    
    /**
     * 顯示順序
     */
    private Integer displayOrder;
    
    /**
     * 熱門度（該類別所有商品的總熱度）
     */
    private Long hotCount;

    /**
     * 狀態（ACTIVE / INACTIVE）。僅字典資料會回傳。
     */
    private String status;

    /**
     * 主題同義詞列表。僅 type=theme 時使用。
     */
    private List<ThemeAliasRes> aliases;
}
