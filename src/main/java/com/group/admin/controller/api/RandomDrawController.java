package com.group.admin.controller.api;

import com.group.admin.res.draw.DrawResponseRes;
import com.group.admin.res.draw.DrawResultRes;
import com.group.admin.service.CoinService;
import com.group.admin.service.ConsumptionRecordService;
import com.group.admin.service.DrawConfigService;
import com.group.admin.service.DrawService;
import com.group.admin.service.SystemConfigService;
import com.group.admin.service.impl.LotteryTicketServiceImpl;
import com.group.admin.util.SecurityUtils;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/lottery/random")
@Validated
@Slf4j
public class RandomDrawController {

    @Autowired
    private DrawService drawService;

    @Autowired
    private CoinService walletService;

    @Autowired
    private DrawConfigService drawConfigService;

    @Autowired
    private ConsumptionRecordService consumptionRecordService;

    @Autowired
    private LotteryTicketServiceImpl ticketServiceImpl;

    @Autowired
    private SystemConfigService systemConfigService;

    @PostMapping("/{lotteryId}/draw")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<DrawResponseRes> executeDraw(
            @PathVariable String lotteryId,
            @RequestParam
            @Min(value = 1, message = "抽數至少要 1")
            Integer count) {

        int maxCount = systemConfigService.getInt(SystemConfigService.KEY_MAX_DRAWS_PER_REQUEST, 10);
        if (count > maxCount) {
            return ResponseEntity.badRequest().build();
        }

        String userId = SecurityUtils.getCurrentUserId();
        log.info("使用者 {} 執行 GACHA 抽獎，lotteryId={}, count={}", userId, lotteryId, count);

        Long goldBefore;
        Long bonusBefore;
        Long goldAfter;
        Long bonusAfter;
        List<DrawResultRes> results;

        Object lock = ticketServiceImpl.getGachaLock(lotteryId);
        synchronized (lock) {
            var walletBefore = walletService.getWallet(userId);
            goldBefore = walletBefore.getGoldCoins();
            bonusBefore = walletBefore.getBonusCoins();
            results = drawService.executeDraw(userId, lotteryId, count);
            grantBatchBonusIfEligible(userId, lotteryId, count, results);
            var walletAfter = walletService.getWallet(userId);
            goldAfter = walletAfter.getGoldCoins();
            bonusAfter = walletAfter.getBonusCoins();
        }

        Long goldUsed = Math.max(0L, goldBefore - goldAfter);
        Long bonusUsed = Math.max(0L, bonusBefore - bonusAfter);

        DrawResponseRes response = DrawResponseRes.builder()
                .results(results)
                .goldUsed(goldUsed)
                .bonusUsed(bonusUsed)
                .remainingGold(goldAfter)
                .remainingBonus(bonusAfter)
                .totalCount(results.size())
                .build();

        log.info("GACHA 抽獎完成 userId={}, count={}", userId, results.size());
        log.info("本次扣除 Gold={}, Bonus={}，剩餘 Gold={}, Bonus={}", goldUsed, bonusUsed, goldAfter, bonusAfter);

        return ResponseEntity.ok(response);
    }

    private void grantBatchBonusIfEligible(String userId, String lotteryId, int requestedCount, List<DrawResultRes> results) {
        int successCount = results != null ? results.size() : 0;
        if (requestedCount <= 1 || successCount < requestedCount) {
            return;
        }

        long bonus = drawConfigService.resolveBonusForDrawCount(requestedCount);
        if (bonus <= 0) {
            return;
        }

        walletService.addBonus(
                userId,
                bonus,
                "BONUS_GRANT",
                lotteryId,
                "多抽優惠贈送（" + requestedCount + " 抽）");
        consumptionRecordService.recordConsumption(
                userId,
                "BONUS_GRANT",
                lotteryId,
                null,
                null,
                null,
                0L,
                bonus,
                "多抽優惠贈送（" + successCount + " 抽）");
        log.info("GACHA 多抽紅利發送成功 userId={}, lotteryId={}, requestedCount={}, successCount={}, bonus={}",
                userId, lotteryId, requestedCount, successCount, bonus);
    }
}
