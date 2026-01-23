package com.group.admin.controller.api;

import com.group.admin.req.common.QueryReq;
import com.group.admin.req.lottery.LotteryCondition;
import com.group.admin.res.lottery.LotteryRes;
import com.group.admin.service.LotteryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 前台商品瀏覽 API
 * 
 * 路由：/lottery/browse/**（context-path 是 /api，所以完整路徑是 /api/lottery/browse/**）
 * 角色：所有使用者（包含未登入）
 * 
 * 特色：
 * 1. 只查詢上架中的商品
 * 2. 返回全部資料，前端做分頁
 * 3. 不需要帶入 storeId（查詢所有店家的商品）
 * 
 * 職責：商品查詢、商品詳情、商品列表
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@RestController
@RequestMapping("/lottery/browse")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "前台商品瀏覽", description = "前台商品查詢與瀏覽 API")
public class LotteryBrowseController {

    private final LotteryService lotteryService;

    /**
     * 查詢商品列表（前台）
     * 
     * ✅ 只查詢上架中的商品
     * ✅ 前端做分頁
     * 
     * @param req 查詢請求（可選）
     * @return 商品列表
     */
    @PostMapping("/list")
    @Operation(summary = "查詢商品列表", description = "前台查詢上架中的商品")
    public ResponseEntity<List<LotteryRes>> queryLotteries(
            @RequestBody(required = false) QueryReq<LotteryCondition> req) {
        
        log.info("🔍 [前台] 查詢商品列表: condition={}", req);
        
        // 強制設定為上架中
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
     * 取得商品詳情
     * 
     * @param id 商品 ID
     * @return 商品詳情
     */
    @GetMapping("/{id}")
    @Operation(summary = "取得商品詳情")
    public ResponseEntity<LotteryRes> getLottery(@PathVariable String id) {
        
        log.info("🔍 [前台] 查詢商品詳情: lotteryId={}", id);
        
        LotteryRes result = lotteryService.getLottery(id);
        
        // 只能查詢上架中的商品
        if (!"ON_SHELF".equals(result.getStatus())) {
            log.warn("⚠️ 商品未上架: id={}, status={}", id, result.getStatus());
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 根據店家 ID 查詢該店家的所有上架商品
     * 
     * @param storeId 店家 ID
     * @return 該店家的商品列表
     */
    @GetMapping("/store/{storeId}")
    @Operation(summary = "查詢店家商品列表", description = "根據店家 ID 查詢該店家所有上架中的商品")
    public ResponseEntity<List<LotteryRes>> getLotteriesByStore(@PathVariable String storeId) {
        
        log.info("🔍 [前台] 查詢店家商品: storeId={}", storeId);
        
        // 建立查詢條件
        QueryReq<LotteryCondition> req = new QueryReq<>();
        LotteryCondition condition = new LotteryCondition();
        condition.setStoreId(storeId);
        condition.setStatus("ON_SHELF");  // 只查詢上架中的商品
        req.setCondition(condition);
        req.setSortBy("created_at");
        req.setSortOrder("DESC");
        
        List<LotteryRes> result = lotteryService.queryLotteries(req);
        
        log.info("✅ 查詢成功: 店家 {} 共 {} 筆商品", storeId, result.size());
        return ResponseEntity.ok(result);
    }
}
