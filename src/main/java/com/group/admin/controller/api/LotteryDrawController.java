package com.group.admin.controller.api;

import com.group.admin.res.lottery.LotteryTicketRes;
import com.group.admin.service.LotteryTicketService;
import com.group.admin.service.LotteryTicketService.DrawResult;
import com.group.admin.service.LotteryTicketService.SessionInfo;
import com.group.admin.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 前台抽獎功能 API
 * 
 * 路由：/lottery/draw/**（context-path 是 /api，所以完整路徑是 /api/lottery/draw/**）
 * 
 * 職責：
 * - 執行抽獎
 * - 查詢籤位列表
 * - 刮刮樂獎項指定
 * - Session 管理
 * 
 * <p>⚠️ 安全重點：此 Controller 不會洩漏未抽籤位的獎品資訊</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/lottery/draw")  // 注意：context-path 是 /api，所以完整路徑是 /api/lottery/draw
@RequiredArgsConstructor
@Tag(name = "前台抽獎功能", description = "玩家抽獎、籤位查詢 API")
public class LotteryDrawController {

    private final LotteryTicketService ticketService;

    /**
     * 取得籤位列表（安全版本）
     * 
     * <p>⚠️ 未抽籤位只返回編號與狀態，不返回獎品資訊</p>
     */
    @GetMapping("/{lotteryId}/tickets")
    @Operation(summary = "取得籤位列表", description = "取得抽獎活動的所有籤位，未抽籤位不會顯示獎品資訊")
    public ResponseEntity<TicketListResponse> getTickets(
            @Parameter(description = "抽獎活動 ID") @PathVariable String lotteryId) {
        
        log.info("🎫 前台查詢籤位: lotteryId={}", lotteryId);
        
        String userId = SecurityUtils.getCurrentUserId();
        
        // 取得籤位（已過濾敏感資訊）
        List<LotteryTicketRes> tickets = ticketService.getTicketsForFrontend(lotteryId);
        
        // 取得場次資訊
        SessionInfo session = null;
        if (userId != null) {
            session = ticketService.getOrCreateSession(lotteryId, userId);
        }
        
        return ResponseEntity.ok(new TicketListResponse(
                tickets,
                session != null ? SessionResponse.from(session, userId) : null
        ));
    }

    /**
     * 執行抽獎
     * 
     * <p>支援兩種模式：</p>
     * <ul>
     *   <li>指定票券：提供 ticket UUID 列表（推薦）</li>
     *   <li>隨機抽獎：不提供 ticket 列表，系統隨機選擇</li>
     * </ul>
     */
    @PostMapping("/{lotteryId}/draw")
    @Operation(summary = "執行抽獎", description = "支援批次抽獎：指定票券 UUID 列表或隨機抽獎")
    public ResponseEntity<?> draw(
            @Parameter(description = "抽獎活動 ID") @PathVariable String lotteryId,
            @RequestBody DrawRequest request) {
        
        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        
        log.info("🎰 抽獎請求: lotteryId={}, userId={}, count={}, ticket列表長度={}", 
                lotteryId, userId, request.getCount(), 
                request.getTickets() != null ? request.getTickets().size() : 0);
        
        // 檢查是否需要玩家指定大獎（SCRATCH_MODE 且開套者未指定）
        SessionInfo session = ticketService.getOrCreateSession(lotteryId, userId);
        if (session.isOpener()) {
            // 只有開套者需要檢查
            DesignationRequiredResponse designationCheck = checkDesignationRequired(lotteryId, session);
            if (designationCheck != null) {
                log.info("⚠️ 需要先指定大獎位置");
                return ResponseEntity.ok(designationCheck);
            }
        }
        
        // 驗證 count
        Integer count = request.getCount();
        if (count == null || count < 1) {
            return ResponseEntity.badRequest().body("count 必須至少為 1");
        }
        if (count > 10) {
            return ResponseEntity.badRequest().body("單次最多只能抽 10 張票券");
        }
        
        // 模式 1：指定票券 UUID（推薦）
        if (request.getTickets() != null && !request.getTickets().isEmpty()) {
            List<String> tickets = request.getTickets();
            
            // 驗證：長度必須等於 count
            if (tickets.size() != count) {
                log.warn("❌ 票券列表長度不符: count={}, actual={}", count, tickets.size());
                return ResponseEntity.badRequest().body("ticket 列表的長度必須等於 count");
            }
            
            // 驗證：不可包含重複
            long distinct = tickets.stream().distinct().count();
            if (distinct != tickets.size()) {
                log.warn("❌ 票券列表包含重複項目");
                return ResponseEntity.badRequest().body("ticket 列表不可包含重複項目");
            }
            
            // 驗證：UUID 格式
            try {
                for (String t : tickets) {
                    java.util.UUID.fromString(t);
                }
            } catch (IllegalArgumentException ex) {
                log.warn("❌ UUID 格式錯誤: {}", ex.getMessage());
                return ResponseEntity.badRequest().body("ticket 列表必須包含有效的 UUID 格式");
            }
            
            log.info("✅ 驗證通過，開始執行批次抽獎: 票券={}", tickets);
            
            // 執行批次抽獎
            List<com.group.admin.service.LotteryTicketService.DrawResult> results = new java.util.ArrayList<>();
            for (String ticketId : tickets) {
                log.info("🎯 處理票券: {}", ticketId);
                com.group.admin.service.LotteryTicketService.DrawResult r = 
                    ticketService.drawByTicketId(lotteryId, userId, ticketId);
                results.add(r);
                log.info("📊 抽獎結果: success={}, message={}", r.success(), r.message());
            }
            
            log.info("✅ 批次抽獎完成，共 {} 張，成功 {} 張", 
                    results.size(), 
                    results.stream().filter(r -> r.success()).count());
            
            com.group.admin.entity.Lottery lottery = ticketService.getLottery(lotteryId);
            String playMode = lottery != null ? lottery.getPlayMode() : null;
            String gameMode = lottery != null ? lottery.getGameMode() : null;
            return ResponseEntity.ok(new DrawBatchResponse(playMode, gameMode, results));
        }
        
        // 模式 2：隨機抽獎（不指定票券）
        log.info("🎲 隨機抽獎模式: count={}", count);
        List<com.group.admin.service.LotteryTicketService.DrawResult> results = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            com.group.admin.service.LotteryTicketService.DrawResult r = 
                ticketService.draw(lotteryId, userId, null, 1);
            results.add(r);
        }
        
        com.group.admin.entity.Lottery lottery = ticketService.getLottery(lotteryId);
        String playMode = lottery != null ? lottery.getPlayMode() : null;
        String gameMode = lottery != null ? lottery.getGameMode() : null;
        return ResponseEntity.ok(new DrawBatchResponse(playMode, gameMode, results));
    }

    /**
     * 刮刮樂(玩家指定)：開套玩家指定大獎位置
     * 
     * <p>支援兩種模式：</p>
     * <ul>
     *   <li>簡易模式：只傳號碼列表，系統自動依序分配大獎</li>
     *   <li>精確模式：明確指定每個號碼對應的獎品 ID</li>
     * </ul>
     */
    @PostMapping("/{lotteryId}/designate")
    @Operation(summary = "指定大獎位置", description = "刮刮樂模式：開套玩家指定大獎位置")
    public ResponseEntity<Void> designatePrizePositions(
            @Parameter(description = "抽獎活動 ID") @PathVariable String lotteryId,
            @RequestBody DesignateRequest request) {
        
        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        
        log.info("🎯 指定大獎位置: lotteryId={}, userId={}, designations={}", 
                lotteryId, userId, request.designations());
        
        ticketService.designatePrizePositions(lotteryId, userId, request.designations());
        
        return ResponseEntity.ok().build();
    }

    /**
     * 取得目前場次資訊
     */
    @GetMapping("/{lotteryId}/session")
    @Operation(summary = "取得場次資訊", description = "取得目前的開套場次狀態")
    public ResponseEntity<SessionResponse> getSession(
            @Parameter(description = "抽獎活動 ID") @PathVariable String lotteryId) {
        
        String userId = SecurityUtils.getCurrentUserId();
        
        SessionInfo session = ticketService.getOrCreateSession(lotteryId, userId);
        
        return ResponseEntity.ok(SessionResponse.from(session, userId));
    }

    // ==================== Request/Response DTOs ====================

    /**
     * 抽獎請求
     * 
     * <p>兩種模式：</p>
     * <ul>
     *   <li>指定票券：提供 count + ticket（UUID 列表）</li>
     *   <li>隨機抽獎：只提供 count（系統隨機選票）</li>
     * </ul>
     */
    public static class DrawRequest {
        private Integer count;  // 必填：抽獎次數（1-10）
        
        @com.fasterxml.jackson.annotation.JsonProperty("ticket")
        private List<String> tickets;  // 選填：指定票券的 UUID 列表

        public Integer getCount() { return count; }
        public void setCount(Integer count) { this.count = count; }

        public List<String> getTickets() { return tickets; }
        public void setTickets(List<String> tickets) { this.tickets = tickets; }
    }

    /**
     * 大獎指定請求
     * 
     * <p>包含籤位號碼與對應的獎品 ID 映射</p>
     */
    public record DesignateRequest(
        @Parameter(description = "大獎指定列表")
        List<LotteryTicketService.PrizeDesignation> designations
    ) {}

    /**
     * 批次抽獎回應（包含 playMode/gameMode 讓前端知道顯示模式）
     */
    public record DrawBatchResponse(
        String playMode,   // LOTTERY_MODE / SCRATCH_MODE
        String gameMode,   // RANDOM / SCRATCH_STORE / SCRATCH_PLAYER
        List<DrawResult> results
    ) {}

    public record TicketListResponse(
        List<LotteryTicketRes> tickets,
        SessionResponse session
    ) {}

    public record SessionResponse(
        String sessionId,
        boolean isOpener,
        String openerNickname,
        int protectionDraws,
        String protectionEndTime,
        int openerDrawCount,
        boolean freeDrawEnabled,
        String status
    ) {
        public static SessionResponse from(SessionInfo info, String currentUserId) {
            return new SessionResponse(
                    info.sessionId(),
                    info.isOpener(),
                    null, // TODO: 查詢開套者暱稱
                    info.protectionDraws(),
                    info.protectionEndTime() != null ? info.protectionEndTime().toString() : null,
                    info.openerDrawCount(),
                    info.freeDrawEnabled(),
                    info.status()
            );
        }
    }

    /**
     * 指定大獎要求回應
     */
    public record DesignationRequiredResponse(
        boolean designationRequired,
        String message,
        List<Integer> availableNumbers,  // 可選的 revealedNumber 列表
        List<GrandPrizeInfo> grandPrizes  // 大獎清單（告知前端要指定幾個、哪些）
    ) {}

    /**
     * 大獎資訊（供前端顯示指定 UI 用）
     */
    public record GrandPrizeInfo(
        String prizeId,
        String prizeName,
        String prizeLevel,
        int quantity,         // 此獎品需要指定幾個 revealedNumber
        String prizeImageUrl
    ) {}

    // ==================== 私有輔助方法 ====================

    /**
     * 檢查是否需要玩家指定大獎
     * 
     * 只有 gameMode=SCRATCH_PLAYER 的開套者需要指定。
     * SCRATCH_STORE 由店家在建立商品時已指定，不攔截。
     */
    private DesignationRequiredResponse checkDesignationRequired(String lotteryId, SessionInfo session) {
        // 已指定過，不需要
        if (session.playerDesignatedNumbers() != null && !session.playerDesignatedNumbers().trim().isEmpty()) {
            return null;
        }

        com.group.admin.entity.Lottery lottery = ticketService.getLottery(lotteryId);
        if (lottery == null || lottery.getGameMode() == null) {
            return null;
        }

        // ⚠️ 只有 SCRATCH_PLAYER 才需要玩家指定大獎位置
        if (!"SCRATCH_PLAYER".equals(lottery.getGameMode())) {
            return null;
        }

        // 取得可用 revealedNumber（前端顯示「可選號碼格子」）
        List<Integer> availableNumbers = ticketService.getAvailableRevealedNumbers(lotteryId);

        // 取得大獎清單，告知前端要指定幾個位置、各類大獎的資訊
        List<com.group.admin.entity.LotteryPrize> grandPrizes = ticketService.getGrandPrizes(lotteryId);
        int requiredCount = grandPrizes.stream()
                .mapToInt(p -> p.getQuantity() != null ? p.getQuantity() : 0)
                .sum();

        List<GrandPrizeInfo> grandPrizeInfos = grandPrizes.stream()
                .map(p -> new GrandPrizeInfo(
                        p.getId(),
                        p.getName(),
                        p.getLevel(),
                        p.getQuantity() != null ? p.getQuantity() : 0,
                        p.getImageUrl()
                )).toList();

        return new DesignationRequiredResponse(
                true,
                "請先指定大獎位置（共需指定 " + requiredCount + " 個號碼）",
                availableNumbers,
                grandPrizeInfos
        );
    }
}
