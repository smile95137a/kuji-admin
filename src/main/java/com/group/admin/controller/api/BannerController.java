package com.group.admin.controller.api;

import com.group.admin.res.banner.BannerRes;
import com.group.admin.service.BannerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Public Banner API — returns ACTIVE banners ordered by displayOrder.
 * No authentication required.
 */
@Slf4j
@RestController
@RequestMapping("/banner")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    /** GET /api/banner 或 GET /api/banner/list — returns only ACTIVE banners, ordered by displayOrder */
    @GetMapping({"", "/list"})
    public ResponseEntity<List<BannerRes>> getActiveBanners() {
        log.info("🎠 前台查詢輪播 Banner");
        return ResponseEntity.ok(bannerService.getCarouselBanners());
    }
}
