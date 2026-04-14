package com.group.admin.scheduler;

import com.group.admin.mapper.LotteryLockMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 定時清理過期保護鎖定
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LockCleanupScheduler {

    private final LotteryLockMapper lotteryLockMapper;

    /**
     * 每 60 秒清理一次過期的抽獎鎖定
     */
    @Scheduled(fixedDelay = 60000)
    public void cleanExpiredLocks() {
        try {
            int cleaned = lotteryLockMapper.expireStaleLocksBeforeTime(LocalDateTime.now());
            if (cleaned > 0) {
                log.info("🔓 清理過期鎖定完成: {} 筆", cleaned);
            }
        } catch (Exception e) {
            log.error("❌ 清理過期鎖定失敗: {}", e.getMessage());
        }
    }
}
