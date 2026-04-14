package com.group.admin.controller.api;

import com.group.admin.mapper.LotteryMapper;
import com.group.admin.req.common.QueryReq;
import com.group.admin.req.lottery.LotteryCondition;
import com.group.admin.req.lottery.LotteryListReq;
import com.group.admin.res.lottery.LotteryRes;
import com.group.admin.service.LotteryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 前台商品公開 API
 * 
 * 路由：/lottery/**（context-path 是 /api，完整路徑 /api/lottery/**）
 * 角色：所有使用者（公開端點，catch-all security chain → permitAll）
 * 
 * 提供簡易的商品列表與詳情查詢，
 * 只返回上架中的商品。
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@RestController
@RequestMapping("/lottery")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "前台商品查詢", description = "公開商品列表與詳情 API")
public class LotteryController {

    private final LotteryService lotteryService;
    private final LotteryMapper lotteryMapper;

    /**
     * 查詢上架中的商品列表（公開，分頁）
     * 
     * GET /api/lottery?page=1&pageSize=20&category=OFFICIAL_ICHIBAN&sort=HOT
     */
    @GetMapping
    @Operation(summary = "查詢商品列表（公開，分頁）", description = "查詢所有上架中的商品，支援分頁和排序")
    public ResponseEntity<Map<String, Object>> listLotteriesPublic(
            @ModelAttribute LotteryListReq req) {
        
        log.info("🔍 [前台] GET /api/lottery: page={}, pageSize={}, category={}, sort={}",
                req.getPage(), req.getPageSize(), req.getCategory(), req.getSort());
        
        List<Map<String, Object>> items = lotteryMapper.selectPublicList(
                req.getCategory(), req.getStoreId(), req.getKeyword(),
                req.getSort(), req.getOffset(), req.getEffectivePageSize());
        
        Long total = lotteryMapper.countPublicList(req.getCategory(), req.getStoreId(), req.getKeyword());
        
        int ps = req.getEffectivePageSize();
        int p = req.getPage() != null ? req.getPage() : 1;
        long pages = (total + ps - 1) / ps;
        
        Map<String, Object> result = new HashMap<>();
        result.put("items", items);
        result.put("total", total);
        result.put("pageNum", p);
        result.put("pageSize", ps);
        result.put("pages", pages);
        
        log.info("✅ 查詢成功: 共 {} 筆（本頁 {} 筆）", total, items.size());
        return ResponseEntity.ok(result);
    }

    /**
     * 查詢上架中的商品列表（公開，舊格式 POST）
     * 
     * ✅ 自動過濾只返回 ON_SHELF 狀態的商品
     */
    @PostMapping("/list")
    @Operation(summary = "查詢商品列表（公開）", description = "查詢所有上架中的商品")
    public ResponseEntity<List<LotteryRes>> listLotteries(
            @RequestBody(required = false) QueryReq<LotteryCondition> req) {
        
        log.info("🔍 [前台] 查詢商品列表: condition={}", req);
        
        if (req == null) {
            req = new QueryReq<>();
        }
        if (req.getCondition() == null) {
            req.setCondition(new LotteryCondition());
        }
        req.getCondition().setStatus("ON_SHELF");
        
        List<LotteryRes> result = lotteryService.queryLotteries(req);
        
        log.info("✅ 查詢成功: 共 {} 筆", result.size());
        return ResponseEntity.ok(result);
    }

    /**
     * 取得商品詳情（公開）
     * 
     * GET /api/lottery/{id}
     * 若狀態為 DRAFT / CONFIGURED / FORCED_OFF 則回傳 403
     */
    @GetMapping("/{id:[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}}")
    @Operation(summary = "取得商品詳情（公開）", description = "查詢商品詳情，草稿/強制下架商品不可存取")
    public ResponseEntity<LotteryRes> getLotteryPublic(@PathVariable String id) {
        
        log.info("🔍 [前台] GET /api/lottery/{}", id);
        
        LotteryRes result = lotteryService.getLottery(id);
        
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        
        // DRAFT / CONFIGURED / FORCED_OFF 不對外公開
        String status = result.getStatus();
        if ("DRAFT".equals(status) || "CONFIGURED".equals(status) || "FORCED_OFF".equals(status)) {
            log.warn("⚠️ 商品不公開: id={}, status={}", id, status);
            return ResponseEntity.status(403).build();
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 取得商品詳情（舊格式，不含 UUID 格式限制）
     */
    @GetMapping("/{id}")
    @Operation(summary = "取得商品詳情（公開，舊格式）", description = "查詢單一上架中的商品詳情")
    public ResponseEntity<LotteryRes> getLottery(@PathVariable String id) {
        
        log.info("🔍 [前台] 查詢商品詳情: lotteryId={}", id);
        
        LotteryRes result = lotteryService.getLottery(id);
        
        if (result == null) {
            log.warn("⚠️ 商品不存在: id={}", id);
            return ResponseEntity.notFound().build();
        }
        
        if (!"ON_SHELF".equals(result.getStatus())) {
            log.warn("⚠️ 商品未上架: id={}, status={}", id, result.getStatus());
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(result);
    }
}
