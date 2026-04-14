package com.group.admin.controller.admin;

import com.group.admin.req.common.QueryReq;
import com.group.admin.req.news.NewsCondition;
import com.group.admin.req.news.NewsCreateReq;
import com.group.admin.req.news.NewsUpdateReq;
import com.group.admin.res.news.NewsRes;
import com.group.admin.service.NewsService;
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
 * 後台最新消息管理 Controller
 * 
 * <p>提供最新消息的 CRUD 與狀態管理功能（僅 Admin 可用）</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/admin/news")
@RequiredArgsConstructor
@Tag(name = "後台-最新消息管理", description = "最新消息 CRUD 與狀態管理（僅 Admin）")
@PreAuthorize("hasRole('ADMIN')")
public class AdminNewsController {

    private final NewsService newsService;

    /**
     * 查詢最新消息列表（GET 簡易版，支援 ?status= 篩選）
     */
    @GetMapping
    @Operation(summary = "查詢最新消息列表（GET）", description = "可選 ?status= 篩選狀態，預設按 created_at DESC 排序")
    public ResponseEntity<List<NewsRes>> listNews(
            @RequestParam(required = false)
            @Parameter(description = "狀態篩選 (DRAFT/PUBLISHED/UNPUBLISHED)", example = "PUBLISHED")
            String status) {

        log.info("📋 後台 GET 查詢最新消息列表，status：{}", status);
        QueryReq<NewsCondition> req = new QueryReq<>();
        if (status != null && !status.isBlank()) {
            NewsCondition condition = new NewsCondition();
            condition.setStatus(status);
            req.setCondition(condition);
        }
        List<NewsRes> results = newsService.queryNews(req);
        return ResponseEntity.ok(results);
    }

    /**
     * 查詢最新消息列表
     * 
     * <p>支援動態條件查詢（標題、狀態、時間範圍、關鍵字）</p>
     */
    @PostMapping("/list")
    @Operation(summary = "查詢最新消息列表（POST）", description = "支援動態條件查詢，所有條件皆可選")
    public ResponseEntity<List<NewsRes>> queryNews(
            @RequestBody(required = false) 
            @Parameter(description = "查詢條件（可選）")
            QueryReq<NewsCondition> req) {
        
        log.info("📋 後台查詢最新消息列表");
        List<NewsRes> results = newsService.queryNews(req);
        return ResponseEntity.ok(results);
    }

    /**
     * 查詢單一最新消息詳情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查詢最新消息詳情", description = "根據 ID 查詢單一最新消息")
    public ResponseEntity<NewsRes> getNewsById(
            @PathVariable 
            @Parameter(description = "最新消息 ID", example = "uuid-news-123")
            String id) {
        
        log.info("🔍 後台查詢最新消息詳情，ID：{}", id);
        NewsRes result = newsService.getNewsById(id);
        return ResponseEntity.ok(result);
    }

    /**
     * 新增最新消息
     */
    @PostMapping
    @Operation(summary = "新增最新消息", description = "建立新的最新消息，預設狀態為 DRAFT")
    public ResponseEntity<NewsRes> createNews(
            @Valid @RequestBody 
            @Parameter(description = "新增請求")
            NewsCreateReq req) {
        
        log.info("➕ 後台新增最新消息：{}", req.getTitle());
        NewsRes result = newsService.createNews(req);
        return ResponseEntity.ok(result);
    }

    /**
     * 更新最新消息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新最新消息", description = "更新最新消息資訊，僅更新提供的欄位")
    public ResponseEntity<NewsRes> updateNews(
            @PathVariable 
            @Parameter(description = "最新消息 ID", example = "uuid-news-123")
            String id,
            @Valid @RequestBody 
            @Parameter(description = "更新請求")
            NewsUpdateReq req) {
        
        log.info("✏️ 後台更新最新消息，ID：{}", id);
        NewsRes result = newsService.updateNews(id, req);
        return ResponseEntity.ok(result);
    }

    /**
     * 刪除最新消息
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "刪除最新消息", description = "永久刪除最新消息")
    public ResponseEntity<Void> deleteNews(
            @PathVariable 
            @Parameter(description = "最新消息 ID", example = "uuid-news-123")
            String id) {
        
        log.info("🗑️ 後台刪除最新消息，ID：{}", id);
        newsService.deleteNews(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 上架最新消息
     */
    @PostMapping("/{id}/publish")
    @Operation(summary = "上架最新消息", description = "將最新消息狀態設為 PUBLISHED，並設定上架時間為當前時間")
    public ResponseEntity<NewsRes> publishNews(
            @PathVariable 
            @Parameter(description = "最新消息 ID", example = "uuid-news-123")
            String id) {
        
        log.info("📢 後台上架最新消息，ID：{}", id);
        NewsRes result = newsService.publishNews(id);
        return ResponseEntity.ok(result);
    }

    /**
     * 下架最新消息
     */
    @PostMapping("/{id}/unpublish")
    @Operation(summary = "下架最新消息", description = "將最新消息狀態設為 UNPUBLISHED，並設定下架時間為當前時間")
    public ResponseEntity<NewsRes> unpublishNews(
            @PathVariable 
            @Parameter(description = "最新消息 ID", example = "uuid-news-123")
            String id) {
        
        log.info("📦 後台下架最新消息，ID：{}", id);
        NewsRes result = newsService.unpublishNews(id);
        return ResponseEntity.ok(result);
    }
}
