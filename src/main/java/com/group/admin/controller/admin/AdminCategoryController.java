package com.group.admin.controller.admin;

import com.group.admin.condition.CategoryCondition;
import com.group.admin.req.common.QueryReq;
import com.group.admin.res.category.CategoryRes;
import com.group.admin.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 後台類別管理 API
 * 
 * <p>路由：/admin/category/**</p>
 * 
 * <p>權限：需要 ADMIN, STORE_OWNER, STORE_EDITOR 角色</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/admin/category")
@RequiredArgsConstructor
@Tag(name = "後台類別管理", description = "商品類別、主題、標籤管理 API")
public class AdminCategoryController {
    
    private final CategoryService categoryService;
    
    /**
     * 查詢所有類別（含下架商品）
     */
    @PostMapping("/categories")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")
    @Operation(summary = "查詢商品類別（後台）", description = "查詢所有商品類別，包含已下架商品")
    public ResponseEntity<List<CategoryRes>> queryCategories(
            @RequestBody(required = false) QueryReq<CategoryCondition> req) {
        
        log.info("📂 後台查詢類別");
        
        List<CategoryRes> categories = categoryService.queryCategories(req);
        
        return ResponseEntity.ok(categories);
    }
    
    /**
     * 查詢所有主題（含下架商品）
     */
    @PostMapping("/themes")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")
    @Operation(summary = "查詢商品主題（後台）", description = "查詢所有商品主題，包含已下架商品")
    public ResponseEntity<List<CategoryRes>> queryThemes(
            @RequestBody(required = false) QueryReq<CategoryCondition> req) {
        
        log.info("🎨 後台查詢主題");
        
        List<CategoryRes> themes = categoryService.queryThemes(req);
        
        return ResponseEntity.ok(themes);
    }
    
    /**
     * 查詢所有標籤（含下架商品）
     */
    @PostMapping("/tags")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")
    @Operation(summary = "查詢商品標籤（後台）", description = "查詢所有商品標籤，包含已下架商品")
    public ResponseEntity<List<CategoryRes>> queryTags(
            @RequestBody(required = false) QueryReq<CategoryCondition> req) {
        
        log.info("🏷️ 後台查詢標籤");
        
        List<CategoryRes> tags = categoryService.queryTags(req);
        
        return ResponseEntity.ok(tags);
    }
    
    /**
     * 類別統計資訊
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER')")
    @Operation(summary = "類別統計資訊", description = "取得類別、主題、標籤的統計資訊")
    public ResponseEntity<CategoryStatisticsRes> getStatistics() {
        
        log.info("📊 查詢類別統計資訊");
        
        // 查詢所有類別、主題、標籤
        List<CategoryRes> categories = categoryService.queryCategories(null);
        List<CategoryRes> themes = categoryService.queryThemes(null);
        List<CategoryRes> tags = categoryService.queryTags(null);
        
        // 計算總商品數
        long totalProducts = categories.stream()
                .mapToLong(CategoryRes::getProductCount)
                .sum();
        
        CategoryStatisticsRes statistics = new CategoryStatisticsRes(
                categories.size(),
                themes.size(),
                tags.size(),
                totalProducts
        );
        
        return ResponseEntity.ok(statistics);
    }
    
    // ==================== Response DTOs ====================
    
    /**
     * 類別統計資訊回應
     */
    public record CategoryStatisticsRes(
            int totalCategories,
            int totalThemes,
            int totalTags,
            long totalProducts
    ) {}
}
