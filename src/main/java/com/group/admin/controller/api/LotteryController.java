package com.group.admin.controller.api;

import com.group.admin.mapper.LotteryMapper;
import com.group.admin.req.common.QueryReq;
import com.group.admin.req.lottery.LotteryCondition;
import com.group.admin.req.lottery.LotteryListReq;
import com.group.admin.res.PageResult;
import com.group.admin.res.lottery.LotteryDetailRes;
import com.group.admin.res.lottery.LotteryListItemRes;
import com.group.admin.res.lottery.LotteryPrizeRes;
import com.group.admin.res.lottery.LotteryRes;
import com.group.admin.service.LotteryService;
import com.group.admin.service.LotteryTicketService;
import com.group.admin.service.LotteryTicketService.DesignatedWinningNumber;
import com.group.admin.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final LotteryTicketService lotteryTicketService;

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
    public ResponseEntity<PageResult<LotteryRes>> listLotteries(
            @RequestBody(required = false) QueryReq<LotteryCondition> req) {
        
        log.info("🔍 [前台] 查詢商品列表: condition={}", req);
        
        if (req == null) {
            req = new QueryReq<>();
        }
        if (req.getCondition() == null) {
            req.setCondition(new LotteryCondition());
        }
        req.getCondition().setStatus("ON_SHELF");
        
        PageResult<LotteryRes> result = lotteryService.queryLotteries(req);
        
        log.info("✅ 查詢成功: 共 {} 筆", result.getTotal());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/browse/list")
    @Operation(summary = "查詢商品列表（完整結構）", description = "前台查詢上架中的商品，返回與詳情頁相同結構（不含 tickets）")
    public ResponseEntity<PageResult<LotteryDetailRes>> browseLotteries(
            @RequestBody(required = false) QueryReq<LotteryCondition> req) {

        log.info("🔍 [前台] 查詢商品瀏覽列表: condition={}", req);

        if (req == null) {
            req = new QueryReq<>();
        }
        if (req.getCondition() == null) {
            req.setCondition(new LotteryCondition());
        }
        req.getCondition().setStatus("ON_SHELF");

        PageResult<LotteryRes> pageResult = lotteryService.queryLotteries(req);
        List<LotteryDetailRes> items = pageResult.getData().stream()
                .map(lotteryRes -> LotteryDetailRes.builder()
                        .lottery(lotteryRes)
                        .prizes(lotteryService.getPrizesByLotteryId(lotteryRes.getId()))
                        .tickets(null)
                        .session(null)
                        .build())
                .collect(Collectors.toList());

        PageResult<LotteryDetailRes> result = PageResult.of(pageResult.getPage(), pageResult.getSize(),
                pageResult.getTotal(), items);

        log.info("✅ 商品瀏覽列表查詢成功: 共 {} 筆", result.getTotal());
        return ResponseEntity.ok(result);
    }

    /**
     * 取得商品詳情（公開）
     * 
    * GET /api/lottery/{id}
    * 若狀態為 DRAFT / WAITING_ON_SHELF / OFF_SHELF / FORCED_OFF / DELETED 則回傳 403
     */
    @GetMapping("/{id:[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}}")
    @Operation(summary = "取得商品詳情（公開）", description = "查詢商品詳情，草稿/強制下架商品不可存取")
    public ResponseEntity<LotteryRes> getLotteryPublic(@PathVariable String id) {
        
        log.info("🔍 [前台] GET /api/lottery/{}", id);
        
        LotteryRes result = lotteryService.getLottery(id);
        
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        
        // 只有上架中與已完售狀態可公開詳情
        String status = result.getStatus();
        if (!isPublicDetailStatus(status)) {
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
        
        if (!isPublicDetailStatus(result.getStatus())) {
            log.warn("⚠️ 商品未上架: id={}, status={}", id, result.getStatus());
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/browse/{id}/detail")
    @Operation(summary = "取得商品詳情（完整版）", description = "包含商品資訊、獎品列表、籤位列表（安全版）、場次資訊")
    public ResponseEntity<LotteryDetailRes> getLotteryDetail(@PathVariable String id) {

        log.info("🔍 [前台] 查詢商品完整版詳情: lotteryId={}", id);

        LotteryRes lottery = lotteryService.getLottery(id);
        if (lottery == null) {
            log.warn("⚠️ 商品不存在: id={}", id);
            return ResponseEntity.notFound().build();
        }
        if (!isPublicDetailStatus(lottery.getStatus())) {
            log.warn("⚠️ 商品未上架: id={}, status={}", id, lottery.getStatus());
            return ResponseEntity.notFound().build();
        }

        List<LotteryPrizeRes> prizes = lotteryService.getPrizesByLotteryId(id);
        List<com.group.admin.res.lottery.LotteryTicketRes> tickets = lotteryTicketService.getTicketsForFrontend(id);

        String userId = SecurityUtils.getCurrentUserId();
        LotteryDetailRes.SessionInfoRes sessionInfo;
        if (userId != null) {
            boolean canDraw = lotteryTicketService.canDrawNow(id, userId);
            LotteryTicketService.SessionInfo session = lotteryTicketService.getActiveSession(id, userId);
            sessionInfo = LotteryDetailRes.SessionInfoRes.builder()
                    .isOpener(session != null && session.isOpener())
                    .openerNickname(null)
                    .protectionEndTime(session != null && session.protectionEndTime() != null
                            ? session.protectionEndTime().toString()
                            : null)
                    .status(session != null ? session.status() : null)
                    .canDraw(canDraw)
                    .cannotDrawReason(canDraw ? null : "商品正在被其他玩家抽獎中，請稍後再試")
                    .build();
        } else {
            sessionInfo = LotteryDetailRes.SessionInfoRes.builder()
                    .isOpener(false)
                    .canDraw(false)
                    .cannotDrawReason("請先登入")
                    .build();
        }

        List<DesignatedWinningNumber> designatedWinningNumbers = lotteryTicketService.getDesignatedWinningNumbers(id);

        LotteryDetailRes result = LotteryDetailRes.builder()
                .lottery(lottery)
                .prizes(prizes)
                .tickets(tickets)
                .session(sessionInfo)
                .designatedWinningNumbers(designatedWinningNumbers)
                .build();

        return ResponseEntity.ok(result);
    }

    private boolean isPublicDetailStatus(String status) {
        return "ON_SHELF".equals(status)
                || "GRAND_PRIZE_DRAWN".equals(status)
                || "ALL_DRAWN".equals(status);
    }

    @GetMapping("/browse/store/{storeId}")
    @Operation(summary = "查詢店家商品列表（簡化版）", description = "根據店家 ID 查詢該店家所有上架中的商品")
    public ResponseEntity<PageResult<LotteryListItemRes>> getLotteriesByStore(@PathVariable String storeId) {

        log.info("🔍 [前台] 查詢店家商品: storeId={}", storeId);

        QueryReq<LotteryCondition> req = new QueryReq<>();
        LotteryCondition condition = new LotteryCondition();
        condition.setStoreId(storeId);
        condition.setStatus("ON_SHELF");
        req.setCondition(condition);
        req.setSortBy("created_at");
        req.setSortOrder("DESC");

        PageResult<LotteryRes> pageResult = lotteryService.queryLotteries(req);
        List<LotteryListItemRes> items = pageResult.getData().stream()
                .map(LotteryListItemRes::from)
                .collect(Collectors.toList());

        PageResult<LotteryListItemRes> result = PageResult.of(pageResult.getPage(), pageResult.getSize(),
                pageResult.getTotal(), items);

        log.info("✅ 店家商品查詢成功: storeId={}, count={}", storeId, result.getTotal());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/browse/{id}/hot")
    @Operation(summary = "增加商品熱度", description = "使商品的 hotCount 加 1，用於追蹤商品熱門程度")
    public ResponseEntity<Integer> incrementHotCount(@PathVariable String id) {

        log.info("🔥 [前台] 增加商品熱度: lotteryId={}", id);

        try {
            int newHotCount = lotteryService.incrementHotCount(id);
            return ResponseEntity.ok(newHotCount);
        } catch (Exception e) {
            log.error("❌ 熱度更新失敗: lotteryId={}, error={}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
