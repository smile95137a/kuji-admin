package com.group.admin.service.draw;

import com.group.admin.entity.Lottery;
import com.group.admin.req.draw.DrawRequest;
import com.group.admin.res.draw.DrawItemRes;
import com.group.admin.service.LotteryTicketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 刮刮樂抽獎策略（CUSTOM_GACHA + SCRATCH_MODE）
 *
 * <p>底層抽獎邏輯與籤位制相同（委派 {@link LotteryTicketService}），
 * 差異在於：</p>
 * <ul>
 *   <li>DrawResult 包含 revealedNumber（刮開後顯示號碼）</li>
 *   <li>DrawController 會在呼叫此 Strategy 前完成 SCRATCH_PLAYER 指定大獎的前置檢查</li>
 *   <li>免單邏輯已內建於 LotteryTicketServiceImpl.draw() 中</li>
 * </ul>
 */
@Slf4j
@Component
public class ScratchDrawStrategy extends TicketDrawStrategy {

    public ScratchDrawStrategy(LotteryTicketService ticketService) {
        super(ticketService);
    }

    @Override
    public List<DrawItemRes> execute(String userId, Lottery lottery, DrawRequest request) {
        log.info("🎴 ScratchDrawStrategy: userId={}, lotteryId={}, gameMode={}",
                userId, lottery.getId(), lottery.getGameMode());
        // 委派給父類別（TicketDrawStrategy）執行相同的籤位制邏輯
        return super.execute(userId, lottery, request);
    }
}
