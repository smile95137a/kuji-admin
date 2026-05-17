package com.group.admin.controller.admin;

import com.group.admin.req.banner.BannerCondition;
import com.group.admin.req.banner.BannerCreateReq;
import com.group.admin.req.banner.BannerReorderReq;
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

    @PostMapping("/list")
    public ResponseEntity<List<BannerRes>> queryBanners(
            @RequestBody(required = false) QueryReq<BannerCondition> req) {
        return ResponseEntity.ok(bannerService.queryBanners(req));
    }

    @GetMapping
    public ResponseEntity<List<BannerRes>> queryBannersLegacy() {
        return ResponseEntity.ok(bannerService.queryBanners(null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BannerRes> getBannerById(@PathVariable String id) {
        return ResponseEntity.ok(bannerService.getBannerById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','STORE_OWNER')")
    public ResponseEntity<BannerRes> createBanner(@Valid @RequestBody BannerCreateReq req) {
        return ResponseEntity.ok(bannerService.createBanner(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BannerRes> updateBanner(@PathVariable String id,
                                                  @RequestBody BannerUpdateReq req) {
        return ResponseEntity.ok(bannerService.updateBanner(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STORE_OWNER')")
    public ResponseEntity<Void> deleteBanner(@PathVariable String id) {
        bannerService.deleteBanner(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<BannerRes> updateStatus(@PathVariable String id,
                                                  @RequestParam String status) {
        String normalizedStatus = status == null ? "" : status.trim().toUpperCase();
        if ("PUBLISHED".equals(normalizedStatus) || "ACTIVE".equals(normalizedStatus)) {
            return ResponseEntity.ok(bannerService.publishBanner(id));
        }
        if ("UNPUBLISHED".equals(normalizedStatus) || "INACTIVE".equals(normalizedStatus)) {
            return ResponseEntity.ok(bannerService.unpublishBanner(id));
        }
        throw new IllegalArgumentException("banner status 僅支援 PUBLISHED/UNPUBLISHED");
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<BannerRes> publishBanner(@PathVariable String id) {
        return ResponseEntity.ok(bannerService.publishBanner(id));
    }

    @PostMapping("/{id}/unpublish")
    public ResponseEntity<BannerRes> unpublishBanner(@PathVariable String id) {
        return ResponseEntity.ok(bannerService.unpublishBanner(id));
    }

    @PutMapping("/{id}/order")
    public ResponseEntity<BannerRes> updateBannerOrder(@PathVariable String id,
                                                       @RequestParam Integer orderNum) {
        return ResponseEntity.ok(bannerService.updateBannerOrder(id, orderNum));
    }

    @PutMapping("/reorder")
    @PreAuthorize("hasAnyRole('ADMIN','STORE_OWNER')")
    public ResponseEntity<Void> reorderBanners(@Valid @RequestBody BannerReorderReq req) {
        bannerService.reorderBanners(req.getIds());
        return ResponseEntity.ok().build();
    }
}
