package com.group.admin.controller.api;

import com.group.admin.res.news.NewsRes;
import com.group.admin.service.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 前台最新消息 Controller
 * 
 * <p>提供前台用戶瀏覽最新消息功能（僅顯示已上架的消息）</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
@Tag(name = "前台-最新消息", description = "前台最新消息瀏覽（無需登入）")
public class NewsController {

    private final NewsService newsService;

    /**
     * 查詢前台最新消息列表
     * 
     * <p>僅返回 PUBLISHED 狀態的消息，按上架時間降序排列</p>
     */
    @GetMapping
    @Operation(summary = "查詢最新消息列表", description = "查詢前台已上架的最新消息，支援限制數量（首頁用）")
    public ResponseEntity<List<NewsRes>> getPublishedNews(
            @RequestParam(required = false) 
            @Parameter(description = "限制數量（可選，用於首頁顯示最新 N 則）", example = "5")
            Integer limit) {
        
        log.info("🔍 前台查詢最新消息列表，limit：{}", limit);
        List<NewsRes> results = newsService.getPublishedNews(limit);
        return ResponseEntity.ok(results);
    }

    /**
     * 查詢單一最新消息詳情
     * 
     * <p>用於顯示完整的最新消息內容</p>
     */
    @GetMapping("/{id}")
    @Operation(summary = "查詢最新消息詳情", description = "根據 ID 查詢單一最新消息的完整內容")
    public ResponseEntity<NewsRes> getNewsDetail(
            @PathVariable 
            @Parameter(description = "最新消息 ID", example = "uuid-news-123")
            String id) {
        
        log.info("🔍 前台查詢最新消息詳情，ID：{}", id);
        NewsRes result = newsService.getNewsById(id);
        
        // 前台只能查看已上架的消息
        if (!"PUBLISHED".equals(result.getStatus())) {
            log.warn("⚠️ 前台嘗試查看未上架的消息，ID：{}", id);
            throw new RuntimeException("該消息不存在或已下架");
        }
        
        return ResponseEntity.ok(result);
    }
}
