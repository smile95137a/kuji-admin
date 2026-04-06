package com.group.admin.controller.api;

import com.group.admin.res.lottery.LotteryTicketRes;
import com.group.admin.service.LotteryTicketService;
import com.group.admin.service.LotteryTicketService.DrawResult;
import com.group.admin.service.LotteryTicketService.SessionInfo;
import com.group.admin.service.LotteryTicketService.DesignatedWinningNumber;
import com.group.admin.service.impl.LotteryTicketServiceImpl;
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
    private final LotteryTicketServiceImpl ticketServiceImpl;  // 🆕 用於 getGachaLock

    /**
     * 執行抽獎
     * 
     * <p>支援兩種模式：</p>
     * <ul>
     *   <li>指定票券：提供 ticket UUID 列表（推薦）</li>
     *   <li>隨機抽獎：不提供 ticket 列表，系統隨機選擇</li>
     * </ul>
     * 
     * <p>🆕 保護時間機制：</p>
     * <ul>
     *   <li>扭蛋(GACHA)：不需要保護時間，使用 synchronized 確保同時只有一個請求</li>
     *   <li>一番賞/卡牌/刮刮樂：首次抽獎啟動保護時間，保護結束前其他玩家不能抽</li>
     *   <li>抽獎回應包含 protectionEndTime，前端可顯示倒數計時</li>
     * </ul>
     */
    @PostMapping("/{lotteryId}/draw")
    @Operation(summary = "執行抽獎", description = "支援批次抽獎：指定票券 UUID 列表或隨機抽獎。扭蛋使用 synchronized，其他模式使用保護時間。")
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
        
        // 驗證 count（提前驗證，避免不必要的查詢）
        Integer count = request.getCount();
        if (count == null || count < 1) {
            return ResponseEntity.badRequest().body("count 必須至少為 1");
        }
        if (count > 10) {
            return ResponseEntity.badRequest().body("單次最多只能抽 10 張票券");
        }
        
        // 取得商品資訊
        com.group.admin.entity.Lottery lottery = ticketService.getLottery(lotteryId);
        if (lottery == null) {
            return ResponseEntity.badRequest().body("商品不存在");
        }
        
        String playMode = lottery.getPlayMode();
        String gameMode = lottery.getGameMode();
        String category = lottery.getCategory();
        
        // 🆕 扭蛋(GACHA)：使用 synchronized 確保同一商品同時只處理一個抽獎請求
        if ("GACHA".equals(category)) {
            Object lock = ticketServiceImpl.getGachaLock(lotteryId);
            synchronized (lock) {
                log.info("🔒 扭蛋 synchronized 開始: lotteryId={}", lotteryId);
                List<DrawResult> results = executeDraws(lotteryId, userId, request);
                log.info("🔓 扭蛋 synchronized 結束: lotteryId={}", lotteryId);
                return ResponseEntity.ok(new DrawBatchResponse(playMode, gameMode, results, null));
            }
        }
        
        // 非扭蛋：檢查是否需要玩家指定大獎（SCRATCH_PLAYER 模式）
        SessionInfo session = ticketService.getOrCreateSession(lotteryId, userId);
        if (session.isOpener()) {
            DesignationRequiredResponse designationCheck = checkDesignationRequired(lotteryId, session);
            if (designationCheck != null) {
                log.info("⚠️ 需要先指定大獎位置");
                return ResponseEntity.ok(designationCheck);
            }
        }
        
        // 🆕 攔截非開套玩家（SCRATCH_PLAYER 模式且開套者尚未完成指定）
        if (!session.isOpener() && "SCRATCH_PLAYER".equals(gameMode)) {
            boolean notDesignated = session.playerDesignatedNumbers() == null
                    || session.playerDesignatedNumbers().isBlank();
            if (notDesignated) {
                String deadline = session.designationDeadline() != null
                        ? session.designationDeadline().toString() : null;
                log.info("🚫 非開套玩家等待指定中: lotteryId={}, openerDeadline={}", lotteryId, deadline);
                return ResponseEntity.ok(new DesignationPendingResponse(
                        true,
                        "開套玩家正在指定大獎位置，請稍候",
                        deadline));
            }
        }
        
        // 執行抽獎
        List<DrawResult> results = executeDraws(lotteryId, userId, request);
        
        // 🆕 取得更新後的場次資訊（包含保護結束時間）
        SessionInfo updatedSession = ticketService.getActiveSession(lotteryId, userId);
        String protectionEndTime = null;
        if (updatedSession != null && updatedSession.protectionEndTime() != null) {
            protectionEndTime = updatedSession.protectionEndTime().toString();
        }
        
        return ResponseEntity.ok(new DrawBatchResponse(playMode, gameMode, results, protectionEndTime));
    }

    /**
     * 刮刮樂(玩家指定)：開套玩家指定大獎位置
     * 
     * <p>🆕 回應包含已指定的大獎中獎號碼，讓前端即時顯示</p>
     */
    @PostMapping("/{lotteryId}/designate")
    @Operation(summary = "指定大獎位置", description = "刮刮樂模式：開套玩家指定大獎位置。回應包含已指定的中獎號碼清單。")
    public ResponseEntity<DesignateResponse> designatePrizePositions(
            @Parameter(description = "抽獎活動 ID") @PathVariable String lotteryId,
            @RequestBody DesignateRequest request) {
        
        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        
        log.info("🎯 指定大獎位置: lotteryId={}, userId={}, designations={}", 
                lotteryId, userId, request.designations());
        
        ticketService.designatePrizePositions(lotteryId, userId, request.designations());
        
        // 🆕 回傳已指定的大獎號碼清單
        List<DesignatedWinningNumber> designatedNumbers = ticketService.getDesignatedWinningNumbers(lotteryId);
        
        return ResponseEntity.ok(new DesignateResponse(
                true,
                "大獎位置指定完成，共 " + request.designations().size() + " 個",
                designatedNumbers
        ));
    }

    /**
     * 取得目前場次資訊
     */
    @GetMapping("/{lotteryId}/session")
    @Operation(summary = "取得場次資訊", description = "取得目前的開套場次狀態（唯讀）")
    public ResponseEntity<SessionResponse> getSession(
            @Parameter(description = "抽獎活動 ID") @PathVariable String lotteryId) {
        
        String userId = SecurityUtils.getCurrentUserId();
        
        // 🆕 使用唯讀查詢
        SessionInfo session = ticketService.getActiveSession(lotteryId, userId);
        
        if (session == null) {
            return ResponseEntity.ok(null);
        }
        
        return ResponseEntity.ok(SessionResponse.from(session, userId));
    }

    // ==================== 私有輔助方法 ====================

    /**
     * 執行批次抽獎（統一處理票券模式和隨機模式）
     */
    private List<DrawResult> executeDraws(String lotteryId, String userId, DrawRequest request) {
        List<DrawResult> results = new java.util.ArrayList<>();
        
        if (request.getTickets() != null && !request.getTickets().isEmpty()) {
            List<String> tickets = request.getTickets();
            Integer count = request.getCount();
            
            // 驗證：長度必須等於 count
            if (tickets.size() != count) {
                log.warn("❌ 票券列表長度不符: count={}, actual={}", count, tickets.size());
                results.add(new DrawResult(false, null, 0, null, null, null, null, null, false, false, 0L, 
                        "ticket 列表的長度必須等於 count"));
                return results;
            }
            
            // 驗證：不可包含重複
            long distinct = tickets.stream().distinct().count();
            if (distinct != tickets.size()) {
                results.add(new DrawResult(false, null, 0, null, null, null, null, null, false, false, 0L, 
                        "ticket 列表不可包含重複項目"));
                return results;
            }
            
            // 驗證：UUID 格式
            try {
                for (String t : tickets) {
                    java.util.UUID.fromString(t);
                }
            } catch (IllegalArgumentException ex) {
                results.add(new DrawResult(false, null, 0, null, null, null, null, null, false, false, 0L, 
                        "ticket 列表必須包含有效的 UUID 格式"));
                return results;
            }
            
            // 執行批次抽獎
            for (String ticketId : tickets) {
                DrawResult r = ticketService.drawByTicketId(lotteryId, userId, ticketId);
                results.add(r);
            }
        } else {
            // 隨機抽獎
            for (int i = 0; i < request.getCount(); i++) {
                DrawResult r = ticketService.draw(lotteryId, userId, null, 1);
                results.add(r);
            }
        }
        
        log.info("✅ 抽獎完成，共 {} 張，成功 {} 張", 
                results.size(), results.stream().filter(DrawResult::success).count());
        return results;
    }

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

    // ==================== Request/Response DTOs ====================

    /**
     * 抽獎請求
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
     */
    public record DesignateRequest(
        @Parameter(description = "大獎指定列表")
        List<LotteryTicketService.PrizeDesignation> designations
    ) {}

    /**
     * 🆕 大獎指定回應（包含已指定的中獎號碼清單）
     */
    public record DesignateResponse(
        boolean success,
        String message,
        List<DesignatedWinningNumber> designatedWinningNumbers  // 已指定的中獎號碼
    ) {}

    /**
     * 🆕 批次抽獎回應（包含 playMode/gameMode + protectionEndTime）
     */
    public record DrawBatchResponse(
        String playMode,   // LOTTERY_MODE / SCRATCH_MODE
        String gameMode,   // RANDOM / SCRATCH_STORE / SCRATCH_PLAYER
        List<DrawResult> results,
        String protectionEndTime  // 🆕 保護結束時間（ISO格式），前端用於顯示倒數計時；扭蛋為 null
    ) {}

    /**
     * 🆕 籤位列表回應（包含已指定的大獎中獎號碼）
     */
    public record TicketListResponse(
        List<LotteryTicketRes> tickets,
        SessionResponse session,
        List<DesignatedWinningNumber> designatedWinningNumbers  // 🆕 已指定的大獎中獎號碼
    ) {}

    public record SessionResponse(
        String sessionId,
        boolean isOpener,
        String openerNickname,
        int protectionDraws,
        String protectionEndTime,
        int openerDrawCount,
        boolean freeDrawEnabled,
        String status,
        String designationDeadline,     // ISO-8601；null = 非 SCRATCH_PLAYER 或指定已完成
        boolean isDesignationComplete   // true = 無需指定流程，或指定已完成
    ) {
        public static SessionResponse from(SessionInfo info, String currentUserId) {
            // 指定完成判斷：playerDesignatedNumbers 有值，或根本沒有 designationDeadline（非 SCRATCH_PLAYER）
            boolean designationComplete = (info.playerDesignatedNumbers() != null
                    && !info.playerDesignatedNumbers().isBlank())
                    || info.designationDeadline() == null;

            // 只有在指定尚未完成時才回傳截止時間（完成後前端不需要倒數）
            String deadline = (!designationComplete && info.designationDeadline() != null)
                    ? info.designationDeadline().toString()
                    : null;

            return new SessionResponse(
                    info.sessionId(),
                    info.isOpener(),
                    null, // TODO: 查詢開套者暱稱
                    info.protectionDraws(),
                    info.protectionEndTime() != null ? info.protectionEndTime().toString() : null,
                    info.openerDrawCount(),
                    info.freeDrawEnabled(),
                    info.status(),
                    deadline,
                    designationComplete
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
     * 等待開套者指定大獎時的回應（非開套玩家用）
     */
    public record DesignationPendingResponse(
        boolean awaitingDesignation,  // 固定 true
        String message,
        String openerDeadline  // ISO-8601，前端用來倒數計時
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
}
