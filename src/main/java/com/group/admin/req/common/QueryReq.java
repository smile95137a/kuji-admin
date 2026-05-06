package com.group.admin.req.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用查詢請求包裝類別
 * 組合 Condition（查詢條件）與分頁/排序參數
 * 
 * 設計原則：
 * 1. 查詢條件與查詢行為分離
 * 2. Condition 只負責查詢條件
 * 3. QueryReq 負責分頁、排序等查詢行為
 * 4. 所有欄位都是可選的
 * 
 * @param <T> 查詢條件類別（必須繼承 BaseCondition）
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryReq<T> {
    
    /**
     * 查詢條件（可選）
     * 如果為 null，表示不使用任何查詢條件
     */
    private T condition;
    
    /**
     * 分頁參數
     * 前後台查詢都可共用
     */
    private Integer page;
    
    /**
     * 每頁筆數
     */
    private Integer size;
    
    /**
     * 排序欄位
     * 例如：created_at, title, price_per_draw
     * 
     * 對應資料庫欄位名稱（snake_case）
     */
    private String sortBy;
    
    /**
     * 排序方向：ASC/DESC
     * 預設：ASC
     */
    private String sortOrder;
}
