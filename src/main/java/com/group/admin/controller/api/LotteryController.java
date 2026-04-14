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
 * 前台商品 API（公開）
 * 
 * 路由：/lottery/**（context-path /api，完整路徑 /api/lottery/**）
 * 角色：所有使用者（包含未登入）
 * 
 * GET /api/lottery/list  — 查詢上架商品列表（支援 storeId、category 過濾）
 * GET /api/lottery/{id}  — 查詢商品詳情（只開放 ON_SHELF）
 */
@Slf4j
@RestController
@RequestMapping("/lottery")
@RequiredArgsConstructor
@Tag(name = "前台商品", description = "前台商品查詢 API（公開）")
public class LotteryController {

    private final LotteryService lotteryService;

    /**
     * 查詢上架中的商品列表
     * 
     * 支援可選過濾條件：storeId、category。
     * 前端負責分頁，後端返回全部符合資料。
     */
    @GetMapping("/list")
    @Operation(summary = "查詢上架商品列表", description = "公開 API：返回所有 ON_SHELF 商品，可依 storeId / category 篩選")
    public ResponseEntity<List<LotteryRes>> listLotteries(
            @RequestParam(required = false) String storeId,
            @RequestParam(required = false) String category) {

        log.info("🔍 [前台] 查詢商品列表: storeId={}, category={}", storeId, category);

        QueryReq<LotteryCondition> req = new QueryReq<>();
        LotteryCondition condition = new LotteryCondition();
        condition.setStatus("ON_SHELF");
        if (storeId != null && !storeId.isBlank()) condition.setStoreId(storeId);
        if (category != null && !category.isBlank()) condition.setCategory(category);
        req.setCondition(condition);
        req.setSortBy("order_num");
        req.setSortOrder("ASC");

        List<LotteryRes> result = lotteryService.queryLotteries(req);
        log.info("✅ 查詢成功: {} 筆", result.size());
        return ResponseEntity.ok(result);
    }

    /**
     * 查詢單一商品詳情（僅限上架中）
     */
    @GetMapping("/{id}")
    @Operation(summary = "查詢商品詳情", description = "公開 API：僅返回 ON_SHELF 狀態的商品")
    public ResponseEntity<LotteryRes> getLottery(@PathVariable String id) {

        log.info("🔍 [前台] 查詢商品詳情: id={}", id);

        LotteryRes result = lotteryService.getLottery(id);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        if (!"ON_SHELF".equals(result.getStatus())) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }
}
