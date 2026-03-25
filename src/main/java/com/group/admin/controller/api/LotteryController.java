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

    /**
     * 查詢上架中的商品列表（公開）
     * 
     * ✅ 自動過濾只返回 ON_SHELF 狀態的商品
     * ✅ 前端做分頁，後端返回全部資料
     * 
     * @param req 查詢請求（可選）
     * @return 上架中的商品列表
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
     * ✅ 只能查詢上架中的商品
     * 
     * @param id 商品 ID
     * @return 商品詳情
     */
    @GetMapping("/{id}")
    @Operation(summary = "取得商品詳情（公開）", description = "查詢單一上架中的商品詳情")
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
