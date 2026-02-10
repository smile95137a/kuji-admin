package com.group.admin.service;

import com.group.admin.condition.CategoryCondition;
import com.group.admin.req.common.QueryReq;
import com.group.admin.res.category.CategoryRes;

import java.util.List;

/**
 * 商品類別服務介面
 * 
 * @author KUJI System
 * @since 1.0.0
 */
public interface CategoryService {
    
    /**
     * 查詢所有類別（按 category 分組）
     * 
     * @param req 查詢請求（可選條件）
     * @return 類別列表
     */
    List<CategoryRes> queryCategories(QueryReq<CategoryCondition> req);
    
    /**
     * 查詢所有主題（按 theme 分組）
     * 
     * @param req 查詢請求（可選條件）
     * @return 主題列表
     */
    List<CategoryRes> queryThemes(QueryReq<CategoryCondition> req);
    
    /**
     * 查詢所有標籤（按 tags 分組）
     * 
     * @param req 查詢請求（可選條件）
     * @return 標籤列表
     */
    List<CategoryRes> queryTags(QueryReq<CategoryCondition> req);
    
    /**
     * 查詢熱門主題（按商品數量或熱度排序）
     * 
     * @param limit 限制數量
     * @return 熱門主題列表
     */
    List<CategoryRes> getHotThemes(int limit);
}
