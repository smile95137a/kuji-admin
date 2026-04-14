package com.group.admin.service.draw;

import com.group.admin.entity.Lottery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 抽獎策略工廠
 *
 * <p>根據商品的 category / playMode 派發對應的抽獎策略。</p>
 */
@Component
@RequiredArgsConstructor
public class DrawStrategyFactory {

    private final GachaDrawStrategy gachaDrawStrategy;
    private final TicketDrawStrategy ticketDrawStrategy;
    private final ScratchDrawStrategy scratchDrawStrategy;

    /**
     * 取得對應的抽獎策略
     *
     * @param lottery 商品資訊
     * @return 對應策略
     */
    public DrawStrategy getStrategy(Lottery lottery) {
        String category = lottery.getCategory();
        String playMode = lottery.getPlayMode();

        // 扭蛋：加權隨機
        if ("GACHA".equals(category)) {
            return gachaDrawStrategy;
        }

        // 刮刮樂：雙號碼機制
        if ("SCRATCH_MODE".equals(playMode)) {
            return scratchDrawStrategy;
        }

        // 預設：籤位制（一番賞 / 卡牌 / 自製賞 LOTTERY_MODE）
        return ticketDrawStrategy;
    }
}
