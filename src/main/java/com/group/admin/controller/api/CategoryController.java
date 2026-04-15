package com.group.admin.controller.api;

import com.group.admin.condition.CategoryCondition;
import com.group.admin.req.common.QueryReq;
import com.group.admin.res.category.CategoryRes;
import com.group.admin.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 前台類別查詢 API
 * 
 * <p>路由：/category/**（context-path 是 /api，所以完整路徑是 /api/category/**）</p>
 * 
 * <p>功能：</p>
 * <ul>
 *   <li>查詢商品類別（一番賞、扭蛋、刮刮樂等）</li>
 *   <li>查詢商品主題（火影忍者、進擊的巨人、排球少年等）</li>
 *   <li>查詢商品標籤</li>
 *   <li>查詢熱門主題</li>
 * </ul>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
@Tag(name = "前台類別查詢", description = "商品類別、主題、標籤查詢 API")
public class CategoryController {
    
    private final CategoryService categoryService;
    
    /**
     * 查詢所有類別（一番賞、扭蛋、刮刮樂等）
     */
    @GetMapping("/categories")
    @Operation(summary = "查詢商品類別", description = "查詢所有商品類別（如一番賞、扭蛋、刮刮樂等），返回每個類別的商品數量")
    public ResponseEntity<List<CategoryRes>> queryCategories() {
        log.info("📂 前台查詢類別");
        return ResponseEntity.ok(categoryService.queryCategories(null));
    }
    
    /**
     * 查詢所有主題（火影忍者、進擊的巨人、排球少年等）
     */
    @GetMapping("/themes")
    @Operation(summary = "查詢商品主題", description = "查詢所有商品主題（如火影忍者、進擊的巨人等），返回每個主題的商品數量和熱度")
    public ResponseEntity<List<CategoryRes>> queryThemes() {
        log.info("🎨 前台查詢主題");
        return ResponseEntity.ok(categoryService.queryThemes(null));
    }
    
    /**
     * 查詢所有標籤
     */
    @GetMapping("/tags")
    @Operation(summary = "查詢商品標籤", description = "查詢所有商品標籤，返回每個標籤的商品數量")
    public ResponseEntity<List<CategoryRes>> queryTags() {
        log.info("🏷️ 前台查詢標籤");
        return ResponseEntity.ok(categoryService.queryTags(null));
    }
    
    /**
     * 查詢熱門主題
     */
    @GetMapping("/hot-themes")
    @Operation(summary = "查詢熱門主題", description = "查詢熱門主題排行榜（按商品數量和熱度排序）")
    public ResponseEntity<List<CategoryRes>> getHotThemes(
            @Parameter(description = "限制數量，預設 10") @RequestParam(defaultValue = "10") int limit) {
        
        log.info("🔥 前台查詢熱門主題，限制 {} 個", limit);
        
        List<CategoryRes> hotThemes = categoryService.getHotThemes(limit);
        
        return ResponseEntity.ok(hotThemes);
    }
    
    /**
     * 按主題查詢商品數量（快速查詢）
     */
    @GetMapping("/theme/{themeName}/count")
    @Operation(summary = "查詢指定主題的商品數量", description = "快速查詢某個主題有多少商品")
    public ResponseEntity<Long> getThemeProductCount(
            @Parameter(description = "主題名稱") @PathVariable String themeName) {
        
        log.info("🔢 查詢主題 {} 的商品數量", themeName);
        
        CategoryCondition condition = new CategoryCondition();
        condition.setTheme(themeName);
        condition.setStatus("ON_SHELF");
        
        QueryReq<CategoryCondition> req = new QueryReq<>();
        req.setCondition(condition);
        
        List<CategoryRes> themes = categoryService.queryThemes(req);
        Long count = themes.isEmpty() ? 0L : themes.get(0).getProductCount();
        
        return ResponseEntity.ok(count);
    }
}
