package com.group.admin.scheduler;

import com.group.admin.service.LotteryLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 過期鎖定清理排程
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LockCleanupScheduler {

    private final LotteryLockService lotteryLockService;

    @Scheduled(fixedDelay = 60000)
    public void cleanExpiredLocks() {
        lotteryLockService.cleanExpiredLocks();
    }
}
