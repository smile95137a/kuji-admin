package com.group.admin.service.draw;

import com.group.admin.entity.Lottery;
import com.group.admin.req.draw.DrawRequest;
import com.group.admin.res.draw.DrawItemRes;
import com.group.admin.res.draw.DrawResultRes;
import com.group.admin.service.DrawService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 扭蛋抽獎策略（加權隨機）
 *
 * <p>委派給現有的 {@link DrawService#executeDraw} 執行，
 * 將結果轉換為統一的 {@link DrawItemRes} 格式。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GachaDrawStrategy implements DrawStrategy {

    private final DrawService drawService;

    @Override
    public List<DrawItemRes> execute(String userId, Lottery lottery, DrawRequest request) {
        int count = request.getCount() != null ? request.getCount() : 1;
        log.info("🎰 GachaDrawStrategy: userId={}, lotteryId={}, count={}", userId, lottery.getId(), count);

        List<DrawResultRes> results = drawService.executeDraw(userId, lottery.getId(), count);

        return results.stream()
                .map(this::toDrawItemRes)
                .collect(Collectors.toList());
    }

    private DrawItemRes toDrawItemRes(DrawResultRes r) {
        return DrawItemRes.builder()
                .success(true)
                .prizeName(r.getPrizeName())
                .prizeLevel(r.getPrizeLevel())
                .prizeImageUrl(r.getPrizeImageUrl())
                .isGrandPrize(r.getIsGrandPrize())
                .isLastPrize(r.getIsLastPrize())
                .costType(r.getCostType())
                .costAmount(r.getCostAmount())
                .lotteryTitle(r.getLotteryTitle())
                .message("抽獎成功！恭喜獲得 " + r.getPrizeName())
                .build();
    }
}
