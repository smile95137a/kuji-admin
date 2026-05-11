package com.group.admin.service;

import com.group.admin.condition.CategoryCondition;
import com.group.admin.req.common.QueryReq;
import com.group.admin.res.category.CategoryHealthRes;
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
     * 查詢前台顯示分類（聚合）
     *
     * 前台顯示分類規則：
     * 官方一番賞 / 自製一番賞 / 刮刮樂 / 扭蛋 / 卡牌
     *
     * @param req 查詢請求（可選條件）
     * @return 顯示分類列表
     */
    List<CategoryRes> queryDisplayCategories(QueryReq<CategoryCondition> req);
    
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

    /**
     * 依關鍵字模糊建議主題
     *
     * @param keyword 使用者輸入關鍵字
     * @param limit 回傳上限
     * @return 主題建議列表
     */
    List<CategoryRes> suggestThemes(String keyword, int limit);

    /**
     * 建立或返回既有主題（跨店家共享）
     *
     * @param name 主題名稱
     * @param imageUrl 主題圖片（可空）
     * @param displayOrder 顯示排序（可空）
     * @return 主題資訊
     */
    CategoryRes upsertTheme(String name, String imageUrl, Integer displayOrder);

    /**
     * 更新主題
     */
    CategoryRes updateTheme(String id, String name, String imageUrl, Integer displayOrder);

    /**
     * 刪除主題（需無商品引用）
     */
    void deleteTheme(String id);

    /**
     * 建立標籤
     */
    CategoryRes createTag(String name, Integer displayOrder, String status);

    /**
     * 更新標籤
     */
    CategoryRes updateTag(String id, String name, Integer displayOrder, String status);

    /**
     * 刪除標籤（需無商品引用）
     */
    void deleteTag(String id);

    /**
     * 驗證標籤名稱是否合法（僅允許存在於字典中的標籤）
     */
    void validateTagNames(List<String> tagNames);

    /**
     * 將輸入主題解析為 canonical 主題名稱。
     */
    String resolveCanonicalThemeName(String inputThemeName);

    /**
     * 建立主題同義詞。
     */
    CategoryRes createThemeAlias(String themeId, String aliasName);

    /**
     * 刪除主題同義詞（軟刪除）。
     */
    void deleteThemeAlias(String aliasId);

    /**
     * 查詢主題健康狀態（重複候選、非法 tags 等）。
     */
    CategoryHealthRes getCategoryHealth();
}
