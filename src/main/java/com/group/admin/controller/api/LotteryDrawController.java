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
     */
    @PostMapping("/{lotteryId}/draw")
    @Operation(summary = "執行抽獎", description = "執行一次抽獎，可選擇籤位或隨機抽")
    public ResponseEntity<DrawResult> draw(
            @Parameter(description = "抽獎活動 ID") @PathVariable String lotteryId,
            @RequestBody DrawRequest request) {
        
        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        
        log.info("🎰 執行抽獎: lotteryId={}, userId={}, ticketNumber={}", 
                lotteryId, userId, request.ticketNumber());
        
        DrawResult result = ticketService.draw(
                lotteryId, 
                userId, 
                request.ticketNumber(), 
                request.drawCount() != null ? request.drawCount() : 1
        );
        
        return ResponseEntity.ok(result);
    }

    /**
     * 刮刮樂(玩家指定)：開套玩家指定大獎位置
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
        
        log.info("🎯 指定大獎位置: lotteryId={}, userId={}, numbers={}", 
                lotteryId, userId, request.prizeNumbers());
        
        ticketService.designatePrizePositions(lotteryId, userId, request.prizeNumbers());
        
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

    public record DrawRequest(
        Integer ticketNumber,  // null = 隨機抽
        Integer drawCount      // 連抽次數，預設 1
    ) {}

    public record DesignateRequest(
        List<Integer> prizeNumbers
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
}
