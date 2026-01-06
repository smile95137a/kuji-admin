package com.group.admin.controller.admin;

import com.group.admin.req.banner.BannerCondition;
import com.group.admin.req.banner.BannerCreateReq;
import com.group.admin.req.banner.BannerUpdateReq;
import com.group.admin.req.common.QueryReq;
import com.group.admin.res.banner.BannerRes;
import com.group.admin.service.BannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 後台 Banner 管理 Controller
 * 
 * <p>提供 Banner 的 CRUD、排序與狀態管理功能（僅 Admin 可用）</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/admin/banner")
@RequiredArgsConstructor
@Tag(name = "後台-Banner 管理", description = "Banner CRUD、排序與狀態管理（僅 Admin）")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBannerController {

    private final BannerService bannerService;

    /**
     * 查詢 Banner 列表
     * 
     * <p>支援動態條件查詢（店家、標題、狀態、時間範圍）</p>
     */
    @PostMapping("/list")
    @Operation(summary = "查詢 Banner 列表", description = "支援動態條件查詢，所有條件皆可選")
    public ResponseEntity<List<BannerRes>> queryBanners(
            @RequestBody(required = false) 
            @Parameter(description = "查詢條件（可選）")
            QueryReq<BannerCondition> req) {
        
        log.info("📋 後台查詢 Banner 列表");
        List<BannerRes> results = bannerService.queryBanners(req);
        return ResponseEntity.ok(results);
    }

    /**
     * 查詢單一 Banner 詳情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查詢 Banner 詳情", description = "根據 ID 查詢單一 Banner")
    public ResponseEntity<BannerRes> getBannerById(
            @PathVariable 
            @Parameter(description = "Banner ID", example = "uuid-banner-123")
            String id) {
        
        log.info("🔍 後台查詢 Banner 詳情，ID：{}", id);
        BannerRes result = bannerService.getBannerById(id);
        return ResponseEntity.ok(result);
    }

    /**
     * 新增 Banner
     */
    @PostMapping
    @Operation(summary = "新增 Banner", description = "建立新的 Banner，預設狀態為 UNPUBLISHED")
    public ResponseEntity<BannerRes> createBanner(
            @Valid @RequestBody 
            @Parameter(description = "新增請求")
            BannerCreateReq req) {
        
        log.info("➕ 後台新增 Banner：{}", req.getTitle());
        BannerRes result = bannerService.createBanner(req);
        return ResponseEntity.ok(result);
    }

    /**
     * 更新 Banner
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新 Banner", description = "更新 Banner 資訊，僅更新提供的欄位")
    public ResponseEntity<BannerRes> updateBanner(
            @PathVariable 
            @Parameter(description = "Banner ID", example = "uuid-banner-123")
            String id,
            @Valid @RequestBody 
            @Parameter(description = "更新請求")
            BannerUpdateReq req) {
        
        log.info("✏️ 後台更新 Banner，ID：{}", id);
        BannerRes result = bannerService.updateBanner(id, req);
        return ResponseEntity.ok(result);
    }

    /**
     * 刪除 Banner
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "刪除 Banner", description = "永久刪除 Banner")
    public ResponseEntity<Void> deleteBanner(
            @PathVariable 
            @Parameter(description = "Banner ID", example = "uuid-banner-123")
            String id) {
        
        log.info("🗑️ 後台刪除 Banner，ID：{}", id);
        bannerService.deleteBanner(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 上架 Banner
     */
    @PostMapping("/{id}/publish")
    @Operation(summary = "上架 Banner", description = "將 Banner 狀態設為 PUBLISHED，並設定上架時間為當前時間")
    public ResponseEntity<BannerRes> publishBanner(
            @PathVariable 
            @Parameter(description = "Banner ID", example = "uuid-banner-123")
            String id) {
        
        log.info("📢 後台上架 Banner，ID：{}", id);
        BannerRes result = bannerService.publishBanner(id);
        return ResponseEntity.ok(result);
    }

    /**
     * 下架 Banner
     */
    @PostMapping("/{id}/unpublish")
    @Operation(summary = "下架 Banner", description = "將 Banner 狀態設為 UNPUBLISHED，並設定下架時間為當前時間")
    public ResponseEntity<BannerRes> unpublishBanner(
            @PathVariable 
            @Parameter(description = "Banner ID", example = "uuid-banner-123")
            String id) {
        
        log.info("📦 後台下架 Banner，ID：{}", id);
        BannerRes result = bannerService.unpublishBanner(id);
        return ResponseEntity.ok(result);
    }

    /**
     * 更新 Banner 排序
     */
    @PutMapping("/{id}/order")
    @Operation(summary = "更新 Banner 排序", description = "更新 Banner 的排序號碼（影響前台輪播順序）")
    public ResponseEntity<BannerRes> updateBannerOrder(
            @PathVariable 
            @Parameter(description = "Banner ID", example = "uuid-banner-123")
            String id,
            @RequestParam 
            @Parameter(description = "新的排序號碼", example = "1")
            Integer orderNum) {
        
        log.info("🔢 後台更新 Banner 排序，ID：{}，orderNum：{}", id, orderNum);
        BannerRes result = bannerService.updateBannerOrder(id, orderNum);
        return ResponseEntity.ok(result);
    }
}
