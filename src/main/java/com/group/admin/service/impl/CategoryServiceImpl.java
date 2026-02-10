package com.group.admin.service.impl;

import com.group.admin.condition.CategoryCondition;
import com.group.admin.entity.Lottery;
import com.group.admin.example.LotteryExample;
import com.group.admin.mapper.LotteryMapper;
import com.group.admin.req.common.QueryReq;
import com.group.admin.res.category.CategoryRes;
import com.group.admin.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 商品類別服務實作
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    
    private final LotteryMapper lotteryMapper;
    
    @Override
    public List<CategoryRes> queryCategories(QueryReq<CategoryCondition> req) {
        log.info("🔍 查詢商品類別");
        
        CategoryCondition condition = req != null ? req.getCondition() : null;
        
        // 查詢所有商品
        LotteryExample example = buildExample(condition);
        List<Lottery> lotteries = lotteryMapper.selectByExample(example);
        
        // 按 category 分組
        Map<String, List<Lottery>> grouped = lotteries.stream()
                .filter(l -> l.getCategory() != null && !l.getCategory().trim().isEmpty())
                .collect(Collectors.groupingBy(Lottery::getCategory));
        
        // 轉換為 CategoryRes
        List<CategoryRes> result = grouped.entrySet().stream()
                .map(entry -> buildCategoryRes(entry.getKey(), "category", entry.getValue()))
                .sorted(Comparator.comparing(CategoryRes::getName))
                .collect(Collectors.toList());
        
        log.info("✅ 查詢完成，共 {} 個類別", result.size());
        return result;
    }
    
    @Override
    public List<CategoryRes> queryThemes(QueryReq<CategoryCondition> req) {
        log.info("🎨 查詢商品主題");
        
        CategoryCondition condition = req != null ? req.getCondition() : null;
        
        // 查詢所有商品
        LotteryExample example = buildExample(condition);
        List<Lottery> lotteries = lotteryMapper.selectByExample(example);
        
        // 按 theme 分組
        Map<String, List<Lottery>> grouped = lotteries.stream()
                .filter(l -> l.getTheme() != null && !l.getTheme().trim().isEmpty())
                .collect(Collectors.groupingBy(Lottery::getTheme));
        
        // 轉換為 CategoryRes
        List<CategoryRes> result = grouped.entrySet().stream()
                .map(entry -> buildCategoryRes(entry.getKey(), "theme", entry.getValue()))
                .sorted(Comparator.comparing(CategoryRes::getHotCount).reversed())  // 按熱度排序
                .collect(Collectors.toList());
        
        log.info("✅ 查詢完成，共 {} 個主題", result.size());
        return result;
    }
    
    @Override
    public List<CategoryRes> queryTags(QueryReq<CategoryCondition> req) {
        log.info("🏷️ 查詢商品標籤");
        
        CategoryCondition condition = req != null ? req.getCondition() : null;
        
        // 查詢所有商品
        LotteryExample example = buildExample(condition);
        List<Lottery> lotteries = lotteryMapper.selectByExample(example);
        
        // 收集所有標籤（tags 是逗號分隔的字串）
        Map<String, Long> tagCounts = new HashMap<>();
        Map<String, String> tagImages = new HashMap<>();
        Map<String, Long> tagHotCounts = new HashMap<>();
        
        for (Lottery lottery : lotteries) {
            if (lottery.getTags() != null && !lottery.getTags().trim().isEmpty()) {
                String[] tags = lottery.getTags().split(",");
                for (String tag : tags) {
                    String trimmedTag = tag.trim();
                    if (!trimmedTag.isEmpty()) {
                        tagCounts.put(trimmedTag, tagCounts.getOrDefault(trimmedTag, 0L) + 1);
                        
                        // 記錄第一個商品的圖片
                        if (!tagImages.containsKey(trimmedTag)) {
                            tagImages.put(trimmedTag, lottery.getImageUrl());
                        }
                        
                        // 累加熱度
                        Integer hotCount = lottery.getHotCount() != null ? lottery.getHotCount() : 0;
                        tagHotCounts.put(trimmedTag, tagHotCounts.getOrDefault(trimmedTag, 0L) + hotCount);
                    }
                }
            }
        }
        
        // 轉換為 CategoryRes
        List<CategoryRes> result = tagCounts.entrySet().stream()
                .map(entry -> CategoryRes.builder()
                        .name(entry.getKey())
                        .type("tag")
                        .productCount(entry.getValue())
                        .imageUrl(tagImages.get(entry.getKey()))
                        .hotCount(tagHotCounts.getOrDefault(entry.getKey(), 0L))
                        .build())
                .sorted(Comparator.comparing(CategoryRes::getProductCount).reversed())
                .collect(Collectors.toList());
        
        log.info("✅ 查詢完成，共 {} 個標籤", result.size());
        return result;
    }
    
    @Override
    public List<CategoryRes> getHotThemes(int limit) {
        log.info("🔥 查詢熱門主題，限制 {} 個", limit);
        
        // 查詢所有上架商品
        LotteryExample example = new LotteryExample();
        example.createCriteria().andStatusEqualTo("ON_SHELF");
        List<Lottery> lotteries = lotteryMapper.selectByExample(example);
        
        // 按 theme 分組並計算熱度
        Map<String, List<Lottery>> grouped = lotteries.stream()
                .filter(l -> l.getTheme() != null && !l.getTheme().trim().isEmpty())
                .collect(Collectors.groupingBy(Lottery::getTheme));
        
        // 轉換為 CategoryRes 並排序
        List<CategoryRes> result = grouped.entrySet().stream()
                .map(entry -> buildCategoryRes(entry.getKey(), "theme", entry.getValue()))
                .sorted(Comparator.comparing(CategoryRes::getHotCount).reversed())
                .limit(limit)
                .collect(Collectors.toList());
        
        log.info("✅ 查詢完成，返回 {} 個熱門主題", result.size());
        return result;
    }
    
    // ==================== 私有輔助方法 ====================
    
    /**
     * 建立查詢條件
     */
    private LotteryExample buildExample(CategoryCondition condition) {
        LotteryExample example = new LotteryExample();
        LotteryExample.Criteria criteria = example.createCriteria();
        
        if (condition != null) {
            // 狀態篩選
            if (condition.getStatus() != null && !condition.getStatus().isEmpty()) {
                criteria.andStatusEqualTo(condition.getStatus());
            } else {
                // 預設只查上架商品
                criteria.andStatusEqualTo("ON_SHELF");
            }
            
            // 類別篩選
            if (condition.getCategory() != null && !condition.getCategory().isEmpty()) {
                criteria.andCategoryEqualTo(condition.getCategory());
            }
            
            // 主題篩選
            if (condition.getTheme() != null && !condition.getTheme().isEmpty()) {
                criteria.andThemeEqualTo(condition.getTheme());
            }
            
            // 標籤篩選（模糊匹配）
            if (condition.getTags() != null && !condition.getTags().isEmpty()) {
                criteria.andTagsLike("%" + condition.getTags() + "%");
            }
            
            // 關鍵字搜尋（搜尋主題或標籤）
            if (condition.getKeyword() != null && !condition.getKeyword().isEmpty()) {
                LotteryExample.Criteria keywordCriteria = example.createCriteria();
                keywordCriteria.andThemeLike("%" + condition.getKeyword() + "%");
                
                LotteryExample.Criteria tagCriteria = example.createCriteria();
                tagCriteria.andTagsLike("%" + condition.getKeyword() + "%");
                
                example.or(keywordCriteria);
                example.or(tagCriteria);
            }
        } else {
            // 無條件時預設只查上架商品
            criteria.andStatusEqualTo("ON_SHELF");
        }
        
        return example;
    }
    
    /**
     * 建立 CategoryRes
     */
    private CategoryRes buildCategoryRes(String name, String type, List<Lottery> lotteries) {
        // 計算商品數量
        long count = lotteries.size();
        
        // 取得代表圖片（第一個商品的圖片）
        String imageUrl = !lotteries.isEmpty() ? lotteries.get(0).getImageUrl() : null;
        
        // 計算總熱度
        long totalHotCount = lotteries.stream()
                .mapToLong(l -> l.getHotCount() != null ? l.getHotCount() : 0)
                .sum();
        
        return CategoryRes.builder()
                .name(name)
                .type(type)
                .productCount(count)
                .imageUrl(imageUrl)
                .hotCount(totalHotCount)
                .build();
    }
}
