package com.group.admin.controller.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.group.admin.entity.Lottery;
import com.group.admin.entity.LotteryPrize;
import com.group.admin.enums.GameModeEnum;
import com.group.admin.req.draw.DrawRequest;
import com.group.admin.res.draw.DrawBatchRes;
import com.group.admin.res.draw.DrawItemRes;
import com.group.admin.res.lottery.LotteryTicketRes;
import com.group.admin.service.LotteryTicketService;
import com.group.admin.service.LotteryTicketService.DesignatedWinningNumber;
import com.group.admin.service.LotteryTicketService.SessionInfo;
import com.group.admin.service.SystemConfigService;
import com.group.admin.service.draw.DrawStrategy;
import com.group.admin.service.draw.DrawStrategyFactory;
import com.group.admin.service.impl.LotteryTicketServiceImpl;
import com.group.admin.util.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * 統一抽獎 API（前台）
 *
 * <p>路由：/lottery/**（context-path /api，完整路徑 /api/lottery/**）</p>
 *
 * <p>統一入口 {@code POST /api/lottery/{lotteryId}/draw}，
 * 由系統依商品 category / playMode 自動派發對應策略：</p>
 * <ul>
 *   <li>GACHA → GachaDrawStrategy（加權隨機，synchronized 並發控制）</li>
 *   <li>OFFICIAL_ICHIBAN / TRADING_CARD → TicketDrawStrategy（籤位制）</li>
 *   <li>CUSTOM_GACHA + SCRATCH_MODE → ScratchDrawStrategy（刮刮樂）</li>
 * </ul>
 *
 * <p>其他端點：</p>
 * <ul>
 *   <li>{@code POST /api/lottery/{lotteryId}/designate} — 刮刮樂 SCRATCH_PLAYER 指定大獎位置</li>
 *   <li>{@code GET /api/lottery/{lotteryId}/session} — 查詢場次資訊</li>
 *   <li>{@code GET /api/lottery/{lotteryId}/tickets} — 查詢籤位列表（前台，隱藏未抽獎品）</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/lottery")
@Tag(name = "統一抽獎", description = "前台統一抽獎 API（含保護時間、免單、刮刮樂指定）")
public class DrawController {

    private final LotteryTicketService ticketService;
    private final DrawStrategyFactory strategyFactory;
    private final SystemConfigService systemConfigService;

    /** 直接注入 Impl 以取得 getGachaLock() */
    @Lazy
    @Autowired
    private LotteryTicketServiceImpl ticketServiceImpl;

    public DrawController(LotteryTicketService ticketService,
                          DrawStrategyFactory strategyFactory,
                          SystemConfigService systemConfigService) {
        this.ticketService = ticketService;
        this.strategyFactory = strategyFactory;
        this.systemConfigService = systemConfigService;
    }

    // ==================== 統一抽獎入口 ====================

    /**
     * 統一抽獎（依商品分類自動派發策略）
     *
     * <p><b>請求格式：</b></p>
     * <ul>
     *   <li>扭蛋隨機：{@code {"count": 3}}</li>
     *   <li>一番賞/卡牌選號：{@code {"count": 1, "ticketNumber": 5}}</li>
     *   <li>批量選號：{@code {"count": 3, "tickets": ["uuid1","uuid2","uuid3"]}}</li>
     * </ul>
     */
    @PostMapping("/{lotteryId}/draw")
    @Operation(summary = "統一抽獎入口",
            description = "依商品 category/playMode 自動派發策略：GACHA=加權隨機、其他=籤位制/刮刮樂")
    public ResponseEntity<?> draw(
            @Parameter(description = "商品 ID") @PathVariable String lotteryId,
            @RequestBody DrawRequest request) {

        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        // ---- 基礎驗證 ----
        Integer count = request.getCount();
        if (count == null || count < 1) {
            return ResponseEntity.badRequest().body("count 必須至少為 1");
        }
        int maxCount = systemConfigService.getInt(SystemConfigService.KEY_MAX_DRAWS_PER_REQUEST, 10);
        if (count > maxCount) {
            return ResponseEntity.badRequest().body("單次最多只能抽 " + maxCount + " 張");
        }

        // ---- 取得商品 ----
        Lottery lottery = ticketService.getLottery(lotteryId);
        if (lottery == null) {
            return ResponseEntity.badRequest().body("商品不存在");
        }
        if (!"ON_SHELF".equals(lottery.getStatus())) {
            return ResponseEntity.badRequest().body("商品未上架，無法抽獎");
        }

        String category = lottery.getCategory();
        String playMode = lottery.getPlayMode();
        String gameMode = lottery.getGameMode();

        log.info("🎰 統一抽獎: lotteryId={}, userId={}, category={}, playMode={}, count={}",
                lotteryId, userId, category, playMode, count);

        // ---- GACHA：synchronized 並發控制 ----
        if ("GACHA".equals(category)) {
            Object lock = ticketServiceImpl.getGachaLock(lotteryId);
            synchronized (lock) {
                log.info("🔒 GACHA synchronized 開始: lotteryId={}", lotteryId);
                DrawStrategy strategy = strategyFactory.getStrategy(lottery);
                List<DrawItemRes> draws = strategy.execute(userId, lottery, request);
                log.info("🔓 GACHA synchronized 結束: lotteryId={}", lotteryId);

                return ResponseEntity.ok(DrawBatchRes.builder()
                        .playMode(playMode)
                        .gameMode(gameMode)
                        .draws(draws)
                        .build());
            }
        }

        // ---- 非 GACHA：建立/取得 Session ----
        SessionInfo session = ticketService.getOrCreateSession(lotteryId, userId);

        // ---- SCRATCH_PLAYER：開套者需先指定大獎 ----
        if (session.isOpener() && GameModeEnum.SCRATCH_PLAYER.getCode().equals(gameMode)) {
            boolean notDesignated = session.playerDesignatedNumbers() == null
                    || session.playerDesignatedNumbers().isBlank();
            if (notDesignated) {
                log.info("⚠️ SCRATCH_PLAYER 需要指定大獎: lotteryId={}, userId={}", lotteryId, userId);
                List<Integer> availableNumbers = ticketService.getAvailableRevealedNumbers(lotteryId);
                List<LotteryPrize> grandPrizes = ticketService.getGrandPrizes(lotteryId);
                List<Map<String, Object>> grandPrizeList = grandPrizes.stream()
                        .map(p -> {
                            Map<String, Object> m = new HashMap<>();
                            m.put("prizeId", p.getId());
                            m.put("prizeName", p.getName());
                            m.put("prizeLevel", p.getLevel());
                            m.put("quantity", p.getQuantity());
                            m.put("prizeImageUrl", p.getImageUrl());
                            return m;
                        })
                        .collect(Collectors.toList());

                return ResponseEntity.ok(DrawBatchRes.builder()
                        .playMode(playMode)
                        .gameMode(gameMode)
                        .designationRequired(true)
                        .designationMessage("請先指定大獎位置（共需指定 " +
                                grandPrizes.stream().mapToInt(p -> p.getQuantity() != null ? p.getQuantity() : 0).sum()
                                + " 個號碼）")
                        .availableNumbers(availableNumbers)
                        .grandPrizes(grandPrizeList)
                        .build());
            }
        }

        // ---- SCRATCH_PLAYER：非開套者等待指定 ----
        if (!session.isOpener() && GameModeEnum.SCRATCH_PLAYER.getCode().equals(gameMode)) {
            boolean notDesignated = session.playerDesignatedNumbers() == null
                    || session.playerDesignatedNumbers().isBlank();
            if (notDesignated) {
                String deadline = session.designationDeadline() != null
                        ? session.designationDeadline().toString() : null;
                log.info("🚫 非開套玩家等待指定: lotteryId={}, deadline={}", lotteryId, deadline);
                return ResponseEntity.ok(DrawBatchRes.builder()
                        .playMode(playMode)
                        .gameMode(gameMode)
                        .designationPending(true)
                        .openerDeadline(deadline)
                        .build());
            }
        }

        // ---- 執行抽獎 ----
        DrawStrategy strategy = strategyFactory.getStrategy(lottery);
        List<DrawItemRes> draws = strategy.execute(userId, lottery, request);

        // ---- 取得更新後的 Session（保護時間）----
        SessionInfo updatedSession = ticketService.getActiveSession(lotteryId, userId);
        String protectionEndTime = null;
        if (updatedSession != null && updatedSession.protectionEndTime() != null) {
            protectionEndTime = updatedSession.protectionEndTime().toString();
        }

        // ---- 彙整免單資訊 ----
        boolean freeDrawTriggered = draws.stream()
                .anyMatch(d -> Boolean.TRUE.equals(d.getTriggeredFreeDraw()));
        Long refundAmount = draws.stream()
                .filter(d -> Boolean.TRUE.equals(d.getTriggeredFreeDraw()))
                .mapToLong(d -> d.getRefundAmount() != null ? d.getRefundAmount() : 0L)
                .findFirst()
                .orElse(0L);

        return ResponseEntity.ok(DrawBatchRes.builder()
                .playMode(playMode)
                .gameMode(gameMode)
                .draws(draws)
                .protectionEndTime(protectionEndTime)
                .freeDrawTriggered(freeDrawTriggered)
                .freeDrawRefundAmount(freeDrawTriggered ? refundAmount : null)
                .build());
    }

    // ==================== 刮刮樂指定大獎 ====================

    /**
     * 刮刮樂(SCRATCH_PLAYER)：開套玩家指定大獎位置
     */
    @PostMapping("/{lotteryId}/designate")
    @Operation(summary = "指定大獎位置",
            description = "刮刮樂 SCRATCH_PLAYER 模式：開套玩家指定大獎 revealedNumber 對應獎品")
    public ResponseEntity<DesignateResponse> designatePrizePositions(
            @Parameter(description = "商品 ID") @PathVariable String lotteryId,
            @RequestBody DesignateRequest request) {

        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        log.info("🎯 指定大獎位置: lotteryId={}, userId={}, designations={}",
                lotteryId, userId, request.designations());

        ticketService.designatePrizePositions(lotteryId, userId, request.designations());

        List<DesignatedWinningNumber> designatedNumbers = ticketService.getDesignatedWinningNumbers(lotteryId);

        return ResponseEntity.ok(new DesignateResponse(
                true,
                "大獎位置指定完成，共 " + request.designations().size() + " 個",
                designatedNumbers
        ));
    }

    // ==================== 場次查詢 ====================

    /**
     * 取得目前場次資訊（唯讀）
     */
    @GetMapping("/{lotteryId}/session")
    @Operation(summary = "取得場次資訊", description = "查詢當前進行中的開套場次狀態（唯讀）")
    public ResponseEntity<SessionResponse> getSession(
            @Parameter(description = "商品 ID") @PathVariable String lotteryId) {

        String userId = SecurityUtils.getCurrentUserId();
        SessionInfo session = ticketService.getActiveSession(lotteryId, userId);

        if (session == null) {
            return ResponseEntity.ok(null);
        }

        Lottery lottery = ticketService.getLottery(lotteryId);
        String gameMode = lottery != null ? lottery.getGameMode() : null;

        return ResponseEntity.ok(SessionResponse.from(session, gameMode));
    }

    // ==================== 籤位查詢 ====================

    /**
     * 取得籤位列表（前台，隱藏未抽籤位的獎品資訊）
     */
    @GetMapping("/{lotteryId}/tickets")
    @Operation(summary = "取得籤位列表",
            description = "前台查詢：AVAILABLE 籤位隱藏獎品資訊，DRAWN 籤位顯示完整資訊")
    public ResponseEntity<TicketListResponse> getTickets(
            @Parameter(description = "商品 ID") @PathVariable String lotteryId) {

        String userId = SecurityUtils.getCurrentUserId();
        log.info("🔍 查詢籤位列表: lotteryId={}, userId={}", lotteryId, userId);

        List<LotteryTicketRes> tickets = ticketService.getTicketsForFrontend(lotteryId);
        SessionInfo session = ticketService.getActiveSession(lotteryId, userId);

        Lottery lottery = ticketService.getLottery(lotteryId);
        String gameMode = lottery != null ? lottery.getGameMode() : null;

        SessionResponse sessionResponse = session != null ? SessionResponse.from(session, gameMode) : null;

        List<DesignatedWinningNumber> designatedNumbers =
                ticketService.getDesignatedWinningNumbers(lotteryId);

        return ResponseEntity.ok(new TicketListResponse(tickets, sessionResponse, designatedNumbers));
    }

    // ==================== Inner DTOs ====================

    public record DesignateRequest(
            List<LotteryTicketService.PrizeDesignation> designations) {
    }

    public record DesignateResponse(
            boolean success,
            String message,
            List<DesignatedWinningNumber> designatedWinningNumbers) {
    }

    public record TicketListResponse(
            List<LotteryTicketRes> tickets,
            SessionResponse session,
            List<DesignatedWinningNumber> designatedWinningNumbers) {
    }

    public record SessionResponse(
            String sessionId,
            boolean isOpener,
            String openerNickname,
            int protectionDraws,
            String protectionEndTime,
            int openerDrawCount,
            boolean freeDrawEnabled,
            String status,
            String designationDeadline,
            boolean isDesignationComplete) {

        public static SessionResponse from(SessionInfo info, String gameMode) {
            boolean designationComplete;
            if (!GameModeEnum.SCRATCH_PLAYER.getCode().equals(gameMode)) {
                designationComplete = true;
            } else {
                designationComplete = info.playerDesignatedNumbers() != null
                        && !info.playerDesignatedNumbers().isBlank();
            }

            String deadline = (!designationComplete && info.designationDeadline() != null)
                    ? info.designationDeadline().toString()
                    : null;

            return new SessionResponse(
                    info.sessionId(),
                    info.isOpener(),
                    null,
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
}
