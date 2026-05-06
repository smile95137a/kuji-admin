package com.group.admin.controller.admin;

import com.group.admin.condition.CategoryCondition;
import com.group.admin.req.category.TagUpsertReq;
import com.group.admin.req.category.ThemeAliasUpsertReq;
import com.group.admin.req.category.ThemeUpsertReq;
import com.group.admin.req.common.QueryReq;
import com.group.admin.res.category.CategoryHealthRes;
import com.group.admin.res.category.CategoryRes;
import com.group.admin.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
     * 模糊查詢主題建議（給店家輸入時輔助）
     */
    @GetMapping("/theme/suggest")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")
    @Operation(summary = "主題模糊建議", description = "依關鍵字回傳既有主題，避免店家建立重複主題")
    public ResponseEntity<List<CategoryRes>> suggestThemes(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "10") int limit) {

        log.info("🔎 後台主題建議查詢: keyword={}, limit={}", keyword, limit);
        return ResponseEntity.ok(categoryService.suggestThemes(keyword, limit));
    }

    /**
     * 建立或更新主題（同名則更新）
     */
    @PostMapping("/theme")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")
    @Operation(summary = "建立或更新主題", description = "店家輸入新主題時可直接建立，若同名已存在則回傳既有主題")
    public ResponseEntity<CategoryRes> upsertTheme(@Valid @RequestBody ThemeUpsertReq req) {

        log.info("🆕 後台建立/更新主題: {}", req.getName());
        return ResponseEntity.ok(categoryService.upsertTheme(req.getName(), req.getImageUrl(), req.getDisplayOrder()));
    }

    /**
     * 更新主題
     */
    @PutMapping("/theme/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")
    @Operation(summary = "更新主題", description = "更新主題名稱、圖片與排序")
    public ResponseEntity<CategoryRes> updateTheme(
            @PathVariable String id,
            @Valid @RequestBody ThemeUpsertReq req) {

        log.info("✏️ 後台更新主題: id={}", id);
        return ResponseEntity.ok(categoryService.updateTheme(id, req.getName(), req.getImageUrl(), req.getDisplayOrder()));
    }

    /**
     * 刪除主題
     */
    @DeleteMapping("/theme/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "刪除主題", description = "若主題已被商品使用則不可刪除")
    public ResponseEntity<Void> deleteTheme(@PathVariable String id) {
        log.info("🗑️ 後台刪除主題: id={}", id);
        categoryService.deleteTheme(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 建立主題同義詞
     */
    @PostMapping("/theme/{id}/alias")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "建立主題同義詞", description = "例如將「火影」綁定到 canonical 主題「火影忍者」")
    public ResponseEntity<CategoryRes> createThemeAlias(
            @PathVariable String id,
            @Valid @RequestBody ThemeAliasUpsertReq req) {
        log.info("🔗 建立主題同義詞: themeId={}, alias={}", id, req.getAliasName());
        return ResponseEntity.ok(categoryService.createThemeAlias(id, req.getAliasName()));
    }

    /**
     * 刪除主題同義詞（軟刪除）
     */
    @DeleteMapping("/theme/alias/{aliasId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "刪除主題同義詞", description = "將 alias 停用，不影響 canonical 主題")
    public ResponseEntity<Void> deleteThemeAlias(@PathVariable String aliasId) {
        log.info("🧹 刪除主題同義詞: aliasId={}", aliasId);
        categoryService.deleteThemeAlias(aliasId);
        return ResponseEntity.ok().build();
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
     * 建立標籤（平台維護）
     */
    @PostMapping("/tag")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "建立標籤", description = "建立全域標籤，店家建立商品只能使用已存在標籤")
    public ResponseEntity<CategoryRes> createTag(@Valid @RequestBody TagUpsertReq req) {
        log.info("🆕 後台建立標籤: {}", req.getName());
        return ResponseEntity.ok(categoryService.createTag(req.getName(), req.getDisplayOrder(), req.getStatus()));
    }

    /**
     * 更新標籤
     */
    @PutMapping("/tag/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新標籤", description = "更新全域標籤資料")
    public ResponseEntity<CategoryRes> updateTag(
            @PathVariable String id,
            @Valid @RequestBody TagUpsertReq req) {
        log.info("✏️ 後台更新標籤: id={}", id);
        return ResponseEntity.ok(categoryService.updateTag(id, req.getName(), req.getDisplayOrder(), req.getStatus()));
    }

    /**
     * 刪除標籤
     */
    @DeleteMapping("/tag/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "刪除標籤", description = "若標籤已被商品使用則不可刪除")
    public ResponseEntity<Void> deleteTag(@PathVariable String id) {
        log.info("🗑️ 後台刪除標籤: id={}", id);
        categoryService.deleteTag(id);
        return ResponseEntity.ok().build();
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

    /**
     * 分類資料健康檢查
     */
    @GetMapping("/health")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "分類健康檢查", description = "檢查重複候選主題、非法標籤與字典覆蓋率")
    public ResponseEntity<CategoryHealthRes> getCategoryHealth() {
        log.info("🩺 查詢分類健康檢查");
        return ResponseEntity.ok(categoryService.getCategoryHealth());
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
