package com.group.admin.controller.api;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.group.admin.entity.Lottery;
import com.group.admin.enums.GameModeEnum;
import com.group.admin.res.lottery.DesignationCheckResponse;
import com.group.admin.res.lottery.LotteryTicketRes;
import com.group.admin.service.CoinService;
import com.group.admin.service.ConsumptionRecordService;
import com.group.admin.service.DrawConfigService;
import com.group.admin.service.LotteryTicketService;
import com.group.admin.service.LotteryTicketService.DesignatedWinningNumber;
import com.group.admin.service.LotteryTicketService.DrawResult;
import com.group.admin.service.LotteryTicketService.SessionInfo;
import com.group.admin.service.SystemConfigService;
import com.group.admin.service.impl.LotteryTicketServiceImpl;
import com.group.admin.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/lottery/draw")
@RequiredArgsConstructor
@Tag(name = "抽獎流程", description = "抽獎與刮刮樂互動 API")
public class LotteryDrawController {

    private final LotteryTicketService ticketService;
    private final LotteryTicketServiceImpl ticketServiceImpl;
    private final SystemConfigService systemConfigService;
    private final DrawConfigService drawConfigService;
    private final CoinService coinService;
    private final ConsumptionRecordService consumptionRecordService;

    @PostMapping("/{lotteryId}/draw")
    @Operation(summary = "執行抽獎")
    public ResponseEntity<?> draw(
            @Parameter(description = "抽獎 ID") @PathVariable String lotteryId,
            @RequestBody DrawRequest request) {

        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        Integer count = request.getCount();
        if (count == null || count < 1) {
            return ResponseEntity.badRequest().body("count 至少要 1");
        }

        int maxCount = systemConfigService.getInt(SystemConfigService.KEY_MAX_DRAWS_PER_REQUEST, 10);
        if (count > maxCount) {
            return ResponseEntity.badRequest().body("單次最多只能抽 " + maxCount + " 次");
        }

        Lottery lottery = ticketService.getLottery(lotteryId);
        if (lottery == null) {
            return ResponseEntity.badRequest().body("找不到抽獎資料");
        }

        String playMode = lottery.getPlayMode();
        String gameMode = lottery.getGameMode();
        String category = lottery.getCategory();

        Object lock = ticketServiceImpl.getGachaLock(lotteryId);
        synchronized (lock) {
            if ("GACHA".equals(category)) {
                List<DrawResult> results = executeDraws(lotteryId, userId, request, playMode, category);
                grantBatchBonusIfEligible(userId, lotteryId, lottery.getTitle(), request.getCount(), results);
                return ResponseEntity.ok(new DrawBatchResponse(playMode, gameMode, results, null));
            }

            SessionInfo session = ticketService.getOrCreateSession(lotteryId, userId);
            if (GameModeEnum.SCRATCH_PLAYER.getCode().equals(gameMode)) {
                DesignationCheckResponse check = ticketService.getDesignationStatus(lotteryId, userId);
                if (check.isRequired()) {
                    if (check.isOpener()) {
                        return ResponseEntity.status(202).body(toDesignationRequiredResponse(check));
                    }
                    return ResponseEntity.status(423).body(new DesignationPendingResponse(
                            true,
                            true,
                            check.getMessage() != null ? check.getMessage() : "開局者尚未完成大獎位置指定",
                            session != null && session.designationDeadline() != null ? session.designationDeadline().toString() : null
                    ));
                }
            }

            List<DrawResult> results = executeDraws(lotteryId, userId, request, playMode, category);
            grantBatchBonusIfEligible(userId, lotteryId, lottery.getTitle(), request.getCount(), results);
            SessionInfo updatedSession = ticketService.getActiveSession(lotteryId, userId);
            String protectionEndTime = updatedSession != null && updatedSession.protectionEndTime() != null
                    ? updatedSession.protectionEndTime().toString()
                    : null;

            return ResponseEntity.ok(new DrawBatchResponse(playMode, gameMode, results, protectionEndTime));
        }
    }

    @PostMapping("/{lotteryId}/designate")
    @Operation(summary = "指定大獎位置")
    public ResponseEntity<DesignateResponse> designatePrizePositions(
            @Parameter(description = "抽獎 ID") @PathVariable String lotteryId,
            @RequestBody DesignateRequest request) {

        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        ticketService.designatePrizePositions(lotteryId, userId, request.designations());
        List<DesignatedWinningNumber> designatedNumbers = ticketService.getDesignatedWinningNumbers(lotteryId);

        return ResponseEntity.ok(new DesignateResponse(
                true,
                "指定成功",
                designatedNumbers
        ));
    }

    @GetMapping("/{lotteryId}/tickets")
    @Operation(summary = "取得票券列表")
    public ResponseEntity<Map<String, Object>> getTickets(
            @Parameter(description = "抽獎 ID") @PathVariable String lotteryId) {

        String userId = SecurityUtils.getCurrentUserId();
        List<LotteryTicketRes> tickets = ticketService.getTicketsForFrontend(lotteryId);
        SessionInfo session = ticketService.getActiveSession(lotteryId, userId);
        List<DesignatedWinningNumber> designatedNumbers = ticketService.getDesignatedWinningNumbers(lotteryId);

        Map<String, Object> payload = new HashMap<>();
        payload.put("tickets", tickets);
        payload.put("session", session != null ? SessionResponse.from(session) : null);
        payload.put("designatedWinningNumbers", designatedNumbers);
        return ResponseEntity.ok(payload);
    }

    @GetMapping("/{lotteryId}/designation-check")
    @Operation(summary = "檢查是否需要指定大獎")
    public ResponseEntity<DesignationCheckResponse> getDesignationCheck(
            @Parameter(description = "抽獎 ID") @PathVariable String lotteryId) {
        String userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ticketService.getDesignationStatus(lotteryId, userId));
    }

    @GetMapping("/{lotteryId}/session")
    @Operation(summary = "取得目前 session")
    public ResponseEntity<SessionResponse> getSession(
            @Parameter(description = "抽獎 ID") @PathVariable String lotteryId) {

        String userId = SecurityUtils.getCurrentUserId();
        boolean canDraw = userId != null && ticketService.canDrawNow(lotteryId, userId);
        SessionInfo session = ticketService.getActiveSession(lotteryId, userId);
        if (session == null) {
            return ResponseEntity.ok(null);
        }
        return ResponseEntity.ok(SessionResponse.from(session, canDraw));
    }

    private List<DrawResult> executeDraws(String lotteryId, String userId, DrawRequest request, String playMode, String category) {
        List<DrawResult> results = new ArrayList<>();
        boolean isScratchMode = "SCRATCH_MODE".equals(playMode) || "SCRATCH_CARD_MODE".equals(playMode);
        boolean isCustomLotteryMode = "CUSTOM_GACHA".equals(category) && "LOTTERY_MODE".equals(playMode);

        if (request.getTickets() != null && !request.getTickets().isEmpty()) {
            List<String> tickets = request.getTickets();
            Integer count = request.getCount();
            if (count == null || tickets.size() != count) {
                results.add(new DrawResult(false, null, 0, null, null, null, null, null, false, false, 0L,
                        "ticket 數量必須與 count 相同", false, null, null, null));
                return results;
            }

            long distinct = tickets.stream().distinct().count();
            if (distinct != tickets.size()) {
                results.add(new DrawResult(false, null, 0, null, null, null, null, null, false, false, 0L,
                        "ticket 不可重複", false, null, null, null));
                return results;
            }

            try {
                for (String ticketId : tickets) {
                    UUID.fromString(ticketId);
                }
            } catch (IllegalArgumentException ex) {
                results.add(new DrawResult(false, null, 0, null, null, null, null, null, false, false, 0L,
                        "ticket 格式錯誤，必須為 UUID", false, null, null, null));
                return results;
            }

            for (int i = 0; i < tickets.size(); i++) {
                String ticketId = tickets.get(i);
                int batchCount = i == 0 ? tickets.size() : 0;
                results.add(ticketService.drawByTicketId(lotteryId, userId, ticketId, batchCount));
            }
            return results;
        }

        if (request.getTicketNumber() != null) {
            if (request.getCount() != null && request.getCount() > 1) {
                results.add(new DrawResult(false, null, 0, null, null, null, null, null, false, false, 0L,
                        "ticketNumber 模式一次只能抽 1 張", false, null, null, null));
                return results;
            }
            results.add(ticketService.draw(lotteryId, userId, request.getTicketNumber(), 1));
            return results;
        }

        if (isScratchMode) {
            results.add(new DrawResult(false, null, 0, null, null, null, null, null, false, false, 0L,
                    "刮刮樂模式請傳 ticket UUID 或 ticketNumber", false, null, null, null));
            return results;
        }

        if (isCustomLotteryMode) {
            results.add(new DrawResult(false, null, 0, null, null, null, null, null, false, false, 0L,
                    "指定票位模式請傳 ticket UUID 或 ticketNumber", false, null, null, null));
            return results;
        }

        for (int i = 0; i < request.getCount(); i++) {
            int batchCount = i == 0 ? request.getCount() : 0;
            results.add(ticketService.draw(lotteryId, userId, null, batchCount));
        }
        return results;
    }

    private void grantBatchBonusIfEligible(String userId, String lotteryId, String lotteryTitle, int requestedCount, List<DrawResult> results) {
        long successCount = results == null ? 0 : results.stream().filter(DrawResult::success).count();
        if (requestedCount <= 1 || successCount < requestedCount) {
            return;
        }

        long bonus = drawConfigService.resolveBonusForDrawCount(requestedCount);
        if (bonus <= 0) {
            return;
        }

        coinService.addBonus(
                userId,
                bonus,
                "BONUS_GRANT",
                lotteryId,
                "多抽優惠贈送 - " + lotteryTitle + "（" + requestedCount + " 抽）");
        consumptionRecordService.recordConsumption(
                userId,
                "BONUS_GRANT",
                lotteryId,
                lotteryTitle,
                null,
                null,
                0L,
                bonus,
                "多抽優惠贈送 - " + lotteryTitle + "（" + requestedCount + " 抽）");
        log.info("發送多抽紅利成功 userId={}, lotteryId={}, requestedCount={}, successCount={}, bonus={}",
                userId, lotteryId, requestedCount, successCount, bonus);
    }

    private DesignationRequiredResponse toDesignationRequiredResponse(DesignationCheckResponse check) {
        List<GrandPrizeInfo> grandPrizes = check.getGrandPrizes() == null
                ? List.of()
                : check.getGrandPrizes().stream()
                .map(prize -> new GrandPrizeInfo(
                        prize.getPrizeId(),
                        prize.getPrizeName(),
                        prize.getPrizeLevel(),
                        1,
                        prize.getPrizeImageUrl()))
                .toList();

        int requiredCount = check.getRequiredDesignationCount() != null
                ? check.getRequiredDesignationCount()
                : grandPrizes.size();

        if (!grandPrizes.isEmpty() && requiredCount > grandPrizes.size()) {
            List<GrandPrizeInfo> expanded = new ArrayList<>(grandPrizes);
            while (expanded.size() < requiredCount) {
                GrandPrizeInfo template = grandPrizes.get(expanded.size() % grandPrizes.size());
                expanded.add(new GrandPrizeInfo(
                        template.prizeId(),
                        template.prizeName(),
                        template.prizeLevel(),
                        template.quantity(),
                        template.prizeImageUrl()));
            }
            grandPrizes = expanded;
        }

        return new DesignationRequiredResponse(
                true,
                check.getMessage() != null ? check.getMessage() : "請先指定大獎位置",
                check.getAvailableRevealedNumbers() != null ? check.getAvailableRevealedNumbers() : List.of(),
                grandPrizes
        );
    }

    public static class DrawRequest {
        private Integer count;

        @JsonAlias({"ticket", "tickets"})
        private List<String> tickets;

        private Integer ticketNumber;

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        public List<String> getTickets() {
            return tickets;
        }

        public void setTickets(List<String> tickets) {
            this.tickets = tickets;
        }

        public Integer getTicketNumber() {
            return ticketNumber;
        }

        public void setTicketNumber(Integer ticketNumber) {
            this.ticketNumber = ticketNumber;
        }
    }

    public record DesignateRequest(List<LotteryTicketService.PrizeDesignation> designations) {
    }

    public record DesignateResponse(
            boolean success,
            String message,
            List<DesignatedWinningNumber> designatedWinningNumbers) {
    }

    public record DrawBatchResponse(
            String playMode,
            String gameMode,
            List<DrawResult> results,
            String protectionEndTime) {
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
            boolean canDraw,
            String cannotDrawReason,
            boolean isDesignationComplete) {

        public static SessionResponse from(SessionInfo info) {
            return from(info, true);
        }

        public static SessionResponse from(SessionInfo info, boolean canDraw) {
            return new SessionResponse(
                    info.sessionId(),
                    info.isOpener(),
                    null,
                    info.protectionDraws(),
                    info.protectionEndTime() != null ? info.protectionEndTime().toString() : null,
                    info.openerDrawCount(),
                    info.freeDrawEnabled(),
                    info.status(),
                    info.designationDeadline() != null ? info.designationDeadline().toString() : null,
                    canDraw,
                    canDraw ? null : "Draw is blocked until protection ends",
                    info.playerDesignatedNumbers() != null && !info.playerDesignatedNumbers().isBlank()
            );
        }
    }

    public record DesignationRequiredResponse(
            boolean designationRequired,
            String message,
            List<Integer> availableNumbers,
            List<GrandPrizeInfo> grandPrizes) {
    }

    public record DesignationPendingResponse(
            boolean awaitingDesignation,
            boolean designationPending,
            String message,
            String openerDeadline) {
    }

    public record GrandPrizeInfo(
            String prizeId,
            String prizeName,
            String prizeLevel,
            int quantity,
            String prizeImageUrl) {
    }
}
