package com.group.admin.scheduler;

import com.group.admin.entity.Lottery;
import com.group.admin.mapper.LotteryMapper;
import com.group.admin.service.EmailService;
import com.group.admin.service.SystemLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定時任務
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledTasks {
    
    private final EmailService emailService;
    private final SystemLogService systemLogService;
    private final LotteryMapper lotteryMapper;
    
    /**
     * 每 5 分鐘重試失敗的郵件
     */
    @Scheduled(fixedRate = 300000)  // 5 分鐘 = 300,000 毫秒
    public void retryFailedEmails() {
        log.debug("⏰ 執行郵件重試任務");
        try {
            emailService.retryFailedEmails();
        } catch (Exception e) {
            log.error("❌ 郵件重試任務失敗: {}", e.getMessage());
        }
    }
    
    /**
     * 每天凌晨 3 點清除超過 90 天的日誌
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupOldLogs() {
        log.info("⏰ 執行日誌清除任務");
        try {
            int deleted = systemLogService.deleteOldLogs(90);
            log.info("🗑️ 日誌清除完成: {} 筆", deleted);
        } catch (Exception e) {
            log.error("❌ 日誌清除任務失敗: {}", e.getMessage());
        }
    }

    /**
     * 每分鐘自動上架：status=OFF_SHELF AND scheduled_at <= now
     */
    @Scheduled(fixedDelay = 60000)
    public void autoOnShelf() {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<Lottery> toOnShelf = lotteryMapper.selectScheduledOnShelf(now);
            if (toOnShelf.isEmpty()) return;

            for (Lottery lottery : toOnShelf) {
                Lottery update = new Lottery();
                update.setId(lottery.getId());
                update.setStatus("ON_SHELF");
                update.setUpdatedAt(now);
                lotteryMapper.updateByPrimaryKeySelective(update);
                log.info("📤 [自動上架] lotteryId={}, title={}", lottery.getId(), lottery.getTitle());
            }
            log.info("✅ 自動上架完成: {} 筆", toOnShelf.size());
        } catch (Exception e) {
            log.error("❌ 自動上架任務失敗: {}", e.getMessage());
        }
    }

    /**
     * 每分鐘自動下架：status=ON_SHELF AND end_time <= now
     */
    @Scheduled(fixedDelay = 60000)
    public void autoOffShelf() {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<Lottery> toOffShelf = lotteryMapper.selectScheduledOffShelf(now);
            if (toOffShelf.isEmpty()) return;

            for (Lottery lottery : toOffShelf) {
                Lottery update = new Lottery();
                update.setId(lottery.getId());
                update.setStatus("OFF_SHELF");
                update.setUpdatedAt(now);
                lotteryMapper.updateByPrimaryKeySelective(update);
                log.info("📥 [自動下架] lotteryId={}, title={}", lottery.getId(), lottery.getTitle());
            }
            log.info("✅ 自動下架完成: {} 筆", toOffShelf.size());
        } catch (Exception e) {
            log.error("❌ 自動下架任務失敗: {}", e.getMessage());
        }
    }
}
