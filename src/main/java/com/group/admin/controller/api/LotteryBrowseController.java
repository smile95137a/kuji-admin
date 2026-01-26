package com.group.admin.controller.api;

import com.group.admin.req.common.QueryReq;
import com.group.admin.req.lottery.LotteryCondition;
import com.group.admin.res.lottery.LotteryDetailRes;
import com.group.admin.res.lottery.LotteryListItemRes;
import com.group.admin.res.lottery.LotteryPrizeRes;
import com.group.admin.res.lottery.LotteryRes;
import com.group.admin.res.lottery.LotteryTicketRes;
import com.group.admin.service.LotteryService;
import com.group.admin.service.LotteryTicketService;
import com.group.admin.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
 * API 區分：
 * - /list：返回 LotteryListItemRes（簡化版，用於列表顯示）
 * - /{id}：返回 LotteryRes（基本資訊）
 * - /{id}/detail：返回 LotteryDetailRes（完整版，包含獎品+籤位）
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
    private final LotteryTicketService lotteryTicketService;

    /**
     * 查詢商品列表（前台）- 簡化版
     * 
     * ✅ 只查詢上架中的商品
     * ✅ 返回 LotteryListItemRes（只包含列表需要的資訊）
     * ✅ 前端做分頁
     * 
     * @param req 查詢請求（可選）
     * @return 商品列表（簡化版）
     */
    @PostMapping("/list")
    @Operation(summary = "查詢商品列表（簡化版）", description = "前台查詢上架中的商品，返回列表所需的基本資訊")
    public ResponseEntity<List<LotteryListItemRes>> queryLotteries(
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
        
        List<LotteryRes> fullList = lotteryService.queryLotteries(req);
        
        // 轉換為簡化版
        List<LotteryListItemRes> result = fullList.stream()
                .map(LotteryListItemRes::from)
                .collect(Collectors.toList());
        
        log.info("✅ 查詢成功: 共 {} 筆", result.size());
        return ResponseEntity.ok(result);
    }

    /**
     * 取得商品基本資訊
     * 
     * @param id 商品 ID
     * @return 商品基本資訊
     */
    @GetMapping("/{id}")
    @Operation(summary = "取得商品基本資訊")
    public ResponseEntity<LotteryRes> getLottery(@PathVariable String id) {
        
        log.info("🔍 [前台] 查詢商品基本資訊: lotteryId={}", id);
        
        LotteryRes result = lotteryService.getLottery(id);
        
        if (result == null) {
            log.warn("⚠️ 商品不存在: id={}", id);
            return ResponseEntity.notFound().build();
        }
        
        // 只能查詢上架中的商品
        if (!"ON_SHELF".equals(result.getStatus())) {
            log.warn("⚠️ 商品未上架: id={}, status={}", id, result.getStatus());
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 取得商品詳情（完整版，包含獎品+籤位）
     * 
     * 這個 API 用於進入商品頁面後顯示完整資訊
     * 
     * @param id 商品 ID
     * @return 商品詳情（包含獎品列表、籤位列表、場次資訊）
     */
    @GetMapping("/{id}/detail")
    @Operation(summary = "取得商品詳情（完整版）", description = "包含獎品列表、籤位列表（安全版）、場次資訊")
    public ResponseEntity<LotteryDetailRes> getLotteryDetail(@PathVariable String id) {
        
        log.info("🔍 [前台] 查詢商品詳情: lotteryId={}", id);
        
        // 1. 取得商品基本資訊
        LotteryRes lottery = lotteryService.getLottery(id);
        
        if (lottery == null) {
            log.warn("⚠️ 商品不存在: id={}", id);
            return ResponseEntity.notFound().build();
        }
        
        // 只能查詢上架中的商品
        if (!"ON_SHELF".equals(lottery.getStatus())) {
            log.warn("⚠️ 商品未上架: id={}, status={}", id, lottery.getStatus());
            return ResponseEntity.notFound().build();
        }
        
        // 2. 取得獎品列表
        List<LotteryPrizeRes> prizes = lotteryService.getPrizesByLotteryId(id);
        
        // 3. 取得籤位列表（安全版，隱藏未抽籤位的獎品資訊）
        List<LotteryTicketRes> tickets = lotteryTicketService.getTicketsForFrontend(id);
        
        // 4. 取得場次資訊
        String userId = SecurityUtils.getCurrentUserId();
        LotteryDetailRes.SessionInfoRes sessionInfo = null;
        
        if (userId != null) {
            boolean canDraw = lotteryTicketService.canDrawNow(id, userId);
            LotteryTicketService.SessionInfo session = lotteryTicketService.getOrCreateSession(id, userId);
            
            sessionInfo = LotteryDetailRes.SessionInfoRes.builder()
                    .isOpener(session.isOpener())
                    .openerNickname(null) // TODO: 查詢開套玩家暱稱
                    .protectionEndTime(session.protectionEndTime() != null 
                            ? session.protectionEndTime().toString() 
                            : null)
                    .status(session.status())
                    .canDraw(canDraw)
                    .cannotDrawReason(canDraw ? null : "商品正在被其他玩家抽獎中，請稍後再試")
                    .build();
        } else {
            // 未登入使用者
            sessionInfo = LotteryDetailRes.SessionInfoRes.builder()
                    .isOpener(false)
                    .canDraw(false)
                    .cannotDrawReason("請先登入")
                    .build();
        }
        
        // 5. 組裝回應
        LotteryDetailRes result = LotteryDetailRes.builder()
                .lottery(lottery)
                .prizes(prizes)
                .tickets(tickets)
                .session(sessionInfo)
                .build();
        
        log.info("✅ 查詢成功: lotteryId={}, prizes={}, tickets={}", 
                id, prizes.size(), tickets.size());
        return ResponseEntity.ok(result);
    }

    /**
     * 根據店家 ID 查詢該店家的所有上架商品（簡化版）
     * 
     * @param storeId 店家 ID
     * @return 該店家的商品列表（簡化版）
     */
    @GetMapping("/store/{storeId}")
    @Operation(summary = "查詢店家商品列表（簡化版）", description = "根據店家 ID 查詢該店家所有上架中的商品")
    public ResponseEntity<List<LotteryListItemRes>> getLotteriesByStore(@PathVariable String storeId) {
        
        log.info("🔍 [前台] 查詢店家商品: storeId={}", storeId);
        
        // 建立查詢條件
        QueryReq<LotteryCondition> req = new QueryReq<>();
        LotteryCondition condition = new LotteryCondition();
        condition.setStoreId(storeId);
        condition.setStatus("ON_SHELF");  // 只查詢上架中的商品
        req.setCondition(condition);
        req.setSortBy("created_at");
        req.setSortOrder("DESC");
        
        List<LotteryRes> fullList = lotteryService.queryLotteries(req);
        
        // 轉換為簡化版
        List<LotteryListItemRes> result = fullList.stream()
                .map(LotteryListItemRes::from)
                .collect(Collectors.toList());
        
        log.info("✅ 查詢成功: 店家 {} 共 {} 筆商品", storeId, result.size());
        return ResponseEntity.ok(result);
    }
}
