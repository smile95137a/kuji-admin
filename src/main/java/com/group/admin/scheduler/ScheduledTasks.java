package com.group.admin.scheduler;

import com.group.admin.service.EmailService;
import com.group.admin.service.NewsService;
import com.group.admin.service.SystemLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定時任務
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledTasks {
    
    private final EmailService emailService;
    private final SystemLogService systemLogService;
    private final NewsService newsService;
    
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
     * 每分鐘檢查並自動上架排程中的最新消息
     */
    @Scheduled(fixedRate = 60000)
    public void autoPublishNews() {
        log.debug("⏰ 執行最新消息自動上架任務");
        try {
            int count = newsService.autoPublishScheduledNews();
            if (count > 0) {
                log.info("📢 自動上架 {} 則最新消息", count);
            }
        } catch (Exception e) {
            log.error("❌ 最新消息自動上架任務失敗: {}", e.getMessage());
        }
    }

    /**
     * 每分鐘檢查並自動下架已過期的最新消息
     */
    @Scheduled(fixedRate = 60000)
    public void autoUnpublishNews() {
        log.debug("⏰ 執行最新消息自動下架任務");
        try {
            int count = newsService.autoUnpublishExpiredNews();
            if (count > 0) {
                log.info("📦 自動下架 {} 則最新消息", count);
            }
        } catch (Exception e) {
            log.error("❌ 最新消息自動下架任務失敗: {}", e.getMessage());
        }
    }
}
