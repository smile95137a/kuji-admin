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

@Slf4j
@RestController
@RequestMapping({"/banner", "/banners"})
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    @GetMapping({"", "/list"})
    public ResponseEntity<List<BannerRes>> getActiveBanners() {
        log.info("查詢前台可見 Banner");
        return ResponseEntity.ok(bannerService.getCarouselBanners());
    }
}
