package com.group.admin.service.draw;

import com.group.admin.entity.Lottery;
import com.group.admin.req.draw.DrawRequest;
import com.group.admin.res.draw.DrawItemRes;

import java.util.List;

/**
 * 抽獎策略介面（Strategy Pattern）
 *
 * <p>不同商品分類由對應的 Strategy 實作：</p>
 * <ul>
 *   <li>GACHA → GachaDrawStrategy（加權隨機）</li>
 *   <li>OFFICIAL_ICHIBAN / TRADING_CARD / CUSTOM_GACHA(LOTTERY_MODE) → TicketDrawStrategy（籤位制）</li>
 *   <li>CUSTOM_GACHA(SCRATCH_MODE) → ScratchDrawStrategy（刮刮樂）</li>
 * </ul>
 */
public interface DrawStrategy {

    /**
     * 執行抽獎
     *
     * @param userId  玩家 ID
     * @param lottery 商品資訊
     * @param request 抽獎請求（count / ticketNumber / tickets）
     * @return 各次抽獎結果
     */
    List<DrawItemRes> execute(String userId, Lottery lottery, DrawRequest request);
}
