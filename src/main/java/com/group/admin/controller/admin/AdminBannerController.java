package com.group.admin.controller.admin;

import com.group.admin.req.banner.BannerCondition;
import com.group.admin.req.banner.BannerCreateReq;
import com.group.admin.req.banner.BannerUpdateReq;
import com.group.admin.req.common.QueryReq;
import com.group.admin.res.banner.BannerRes;
import com.group.admin.service.BannerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/banners")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','STORE_OWNER','STORE_EDITOR')")
public class AdminBannerController {

    private final BannerService bannerService;

    /** POST /admin/banners/list — list with optional status filter */
    @PostMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN','STORE_OWNER','STORE_EDITOR')")
    public ResponseEntity<List<BannerRes>> queryBanners(
            @RequestBody(required = false) QueryReq<BannerCondition> req) {
        log.info("📋 後台查詢 Banner 列表");
        return ResponseEntity.ok(bannerService.queryBanners(req));
    }

    /** GET /admin/banners/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<BannerRes> getBannerById(@PathVariable String id) {
        log.info("🔍 後台查詢 Banner 詳情，ID：{}", id);
        return ResponseEntity.ok(bannerService.getBannerById(id));
    }

    /** POST /admin/banners */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','STORE_OWNER')")
    public ResponseEntity<BannerRes> createBanner(@Valid @RequestBody BannerCreateReq req) {
        log.info("➕ 後台新增 Banner：{}", req.getTitle());
        return ResponseEntity.ok(bannerService.createBanner(req));
    }

    /** PUT /admin/banners/{id} */
    @PutMapping("/{id}")
    public ResponseEntity<BannerRes> updateBanner(@PathVariable String id,
                                                   @RequestBody BannerUpdateReq req) {
        log.info("✏️ 後台更新 Banner，ID：{}", id);
        return ResponseEntity.ok(bannerService.updateBanner(id, req));
    }

    /** DELETE /admin/banners/{id} */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STORE_OWNER')")
    public ResponseEntity<Void> deleteBanner(@PathVariable String id) {
        log.info("🗑️ 後台刪除 Banner，ID：{}", id);
        bannerService.deleteBanner(id);
        return ResponseEntity.ok().build();
    }

    /** PATCH /admin/banners/{id}/status?status=ACTIVE|INACTIVE|DRAFT */
    @PatchMapping("/{id}/status")
    public ResponseEntity<BannerRes> updateStatus(@PathVariable String id,
                                                   @RequestParam String status) {
        log.info("🔄 後台更新 Banner 狀態，ID：{}，status：{}", id, status);
        BannerRes result;
        if ("ACTIVE".equals(status)) {
            result = bannerService.publishBanner(id);
        } else {
            result = bannerService.unpublishBanner(id);
        }
        return ResponseEntity.ok(result);
    }

    /** POST /admin/banners/{id}/publish — explicit publish */
    @PostMapping("/{id}/publish")
    public ResponseEntity<BannerRes> publishBanner(@PathVariable String id) {
        log.info("📢 後台上架 Banner，ID：{}", id);
        return ResponseEntity.ok(bannerService.publishBanner(id));
    }

    /** POST /admin/banners/{id}/unpublish — explicit unpublish */
    @PostMapping("/{id}/unpublish")
    public ResponseEntity<BannerRes> unpublishBanner(@PathVariable String id) {
        log.info("📦 後台下架 Banner，ID：{}", id);
        return ResponseEntity.ok(bannerService.unpublishBanner(id));
    }

    /** PUT /admin/banners/{id}/order?orderNum=N */
    @PutMapping("/{id}/order")
    public ResponseEntity<BannerRes> updateBannerOrder(@PathVariable String id,
                                                        @RequestParam Integer orderNum) {
        log.info("🔢 後台更新 Banner 排序，ID：{}，orderNum：{}", id, orderNum);
        return ResponseEntity.ok(bannerService.updateBannerOrder(id, orderNum));
    }
}
