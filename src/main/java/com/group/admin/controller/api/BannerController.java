package com.group.admin.controller.api;

import com.group.admin.res.banner.BannerRes;
import com.group.admin.service.BannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前台 Banner Controller
 * 
 * <p>提供前台輪播 Banner 查詢功能（僅顯示已上架且店家為 ACTIVE 的 Banner）</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/banner")
@RequiredArgsConstructor
@Tag(name = "前台-Banner 輪播", description = "前台 Banner 輪播查詢（無需登入）")
public class BannerController {

    private final BannerService bannerService;

    /**
     * 查詢前台輪播 Banner
     * 
     * <p>僅返回 PUBLISHED 狀態且店家為 ACTIVE 的 Banner，按 order_num 升序排列</p>
     */
    @GetMapping("/carousel")
    @Operation(summary = "查詢輪播 Banner", description = "查詢前台首頁輪播 Banner，按排序號碼升序排列")
    public ResponseEntity<List<BannerRes>> getCarouselBanners() {
        
        log.info("🎠 前台查詢輪播 Banner");
        List<BannerRes> results = bannerService.getCarouselBanners();
        return ResponseEntity.ok(results);
    }
}
