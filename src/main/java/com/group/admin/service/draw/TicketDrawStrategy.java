package com.group.admin.service.draw;

import com.group.admin.entity.Lottery;
import com.group.admin.req.draw.DrawRequest;
import com.group.admin.res.draw.DrawItemRes;
import com.group.admin.service.LotteryTicketService;
import com.group.admin.service.LotteryTicketService.DrawResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 籤位制抽獎策略（一番賞 / 卡牌 / 自製賞 LOTTERY_MODE）
 *
 * <p>委派給現有的 {@link LotteryTicketService} 執行，
 * 支援選號抽、批量選號抽、隨機抽三種模式。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TicketDrawStrategy implements DrawStrategy {

    private final LotteryTicketService ticketService;

    @Override
    public List<DrawItemRes> execute(String userId, Lottery lottery, DrawRequest request) {
        String lotteryId = lottery.getId();
        log.info("🎫 TicketDrawStrategy: userId={}, lotteryId={}", userId, lotteryId);

        List<DrawItemRes> results = new ArrayList<>();

        if (request.getTickets() != null && !request.getTickets().isEmpty()) {
            // 批量指定票券 UUID
            List<String> tickets = request.getTickets();
            validateTicketList(tickets, request.getCount());

            for (String ticketId : tickets) {
                DrawResult r = ticketService.drawByTicketId(lotteryId, userId, ticketId);
                results.add(toDrawItemRes(r));
            }

        } else if (request.getTicketNumber() != null) {
            // 選號模式
            DrawResult r = ticketService.draw(lotteryId, userId, request.getTicketNumber(), 1);
            results.add(toDrawItemRes(r));

        } else {
            // 隨機抽
            int count = request.getCount() != null ? request.getCount() : 1;
            for (int i = 0; i < count; i++) {
                DrawResult r = ticketService.draw(lotteryId, userId, null, 1);
                results.add(toDrawItemRes(r));
            }
        }

        return results;
    }

    /**
     * 驗證票券列表：長度需等於 count、不可重複、必須為合法 UUID
     */
    private void validateTicketList(List<String> tickets, Integer count) {
        if (count != null && tickets.size() != count) {
            throw new com.group.admin.exception.BusinessException(
                    "ticket 列表的長度必須等於 count（count=" + count + ", actual=" + tickets.size() + "）");
        }
        long distinct = tickets.stream().distinct().count();
        if (distinct != tickets.size()) {
            throw new com.group.admin.exception.BusinessException("ticket 列表不可包含重複項目");
        }
        for (String t : tickets) {
            try {
                UUID.fromString(t);
            } catch (IllegalArgumentException ex) {
                throw new com.group.admin.exception.BusinessException("ticket 列表包含非法 UUID：" + t);
            }
        }
    }

    protected DrawItemRes toDrawItemRes(DrawResult r) {
        return DrawItemRes.builder()
                .success(r.success())
                .ticketId(r.ticketId())
                .ticketNumber(r.ticketNumber())
                .revealedNumber(r.revealedNumber())
                .prizeId(r.prizeId())
                .prizeLevel(r.prizeLevel())
                .prizeName(r.prizeName())
                .prizeImageUrl(r.prizeImageUrl())
                .isGrandPrize(r.isGrandPrize())
                .triggeredFreeDraw(r.triggeredFreeDraw())
                .refundAmount(r.refundAmount())
                .message(r.message())
                .lastPrizeAwarded(r.lastPrizeAwarded())
                .lastPrizeId(r.lastPrizeId())
                .lastPrizeName(r.lastPrizeName())
                .lastPrizeImageUrl(r.lastPrizeImageUrl())
                .build();
    }
}
