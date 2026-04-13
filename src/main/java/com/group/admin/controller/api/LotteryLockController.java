package com.group.admin.controller.api;

import com.group.admin.res.draw.LockStatusRes;
import com.group.admin.res.lottery.LotteryRes;
import com.group.admin.service.LotteryLockService;
import com.group.admin.service.LotteryLockService.LockStatus;
import com.group.admin.service.LotteryService;
import com.group.admin.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 前台鎖定狀態查詢 API
 */
@Slf4j
@RestController
@RequestMapping("/lottery")
@RequiredArgsConstructor
public class LotteryLockController {

    private final LotteryLockService lotteryLockService;
    private final LotteryService lotteryService;

    @GetMapping("/{lotteryId}/lock-status")
    public ResponseEntity<LockStatusRes> getLockStatus(@PathVariable String lotteryId) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("🔍 查詢鎖定狀態: lotteryId={}, userId={}", lotteryId, userId);

        LotteryRes lottery = lotteryService.getLottery(lotteryId);

        LockStatus status = lotteryLockService.checkLockStatus(lotteryId, userId);

        LockStatusRes res = new LockStatusRes();
        res.setLotteryId(lotteryId);
        res.setCanDraw(status.canDraw());
        res.setIsLockedByMe(status.isLockedByMe());
        res.setLockedByOther(!status.canDraw() && !status.isLockedByMe());
        res.setRemainingLockSeconds(status.remainingSeconds());
        res.setIsLocked(status.isLockedByMe() || !status.canDraw());
        res.setLockedByUserId(status.lockedByUserId());
        res.setProtectionMinutes(lottery.getProtectionMinutes());

        var lock = lotteryLockService.getActiveLock(lotteryId);
        if (lock != null) {
            res.setLockStartTime(lock.getLockStartTime());
            res.setLockEndTime(lock.getLockEndTime());
        }

        return ResponseEntity.ok(res);
    }
}
