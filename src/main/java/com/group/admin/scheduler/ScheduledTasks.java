package com.group.admin.scheduler;

import com.group.admin.service.EmailService;
import com.group.admin.service.BannerService;
import com.group.admin.service.LotteryService;
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
    private final BannerService bannerService;
    private final LotteryService lotteryService;
    
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
        // 032 稽核日誌系統：舊 SystemLog 已移除，此排程保留供後續擴充
        log.debug("⏰ 日誌清除任務跳過（新系統對應表由 log_* 接管）");
    }

    /**
     * 每 60 秒自動推進排程的商品（WAITING_ON_SHELF → ON_SHELF）
     */
    @Scheduled(fixedRate = 60000)
    public void autoPromoteLotteries() {
        try {
            lotteryService.promoteScheduledLotteries();
        } catch (Exception e) {
            log.error("❌ 自動上架任務失敗: {}", e.getMessage());
        }
    }

    /**
     * 每 60 秒將活動時間到期的商品自動下架（ON_SHELF + end_time <= NOW → OFF_SHELF）
     */
    @Scheduled(fixedRate = 60000)
    public void autoExpireLotteries() {
        try {
            lotteryService.expireEndTimeLotteries();
        } catch (Exception e) {
            log.error("❌ 活動到期下架任務失敗: {}", e.getMessage());
        }
    }

    /**
     * 每 60 秒自動推進 Banner 狀態（SCHEDULED/PUBLISHED）
     */
    @Scheduled(fixedRate = 60000)
    public void autoSwitchBannerStatus() {
        try {
            bannerService.autoPublishBanners();
            bannerService.autoUnpublishBanners();
        } catch (Exception e) {
            log.error("❌ Banner 自動上下架任務失敗: {}", e.getMessage());
        }
    }

    // autoStartDrawable 已停用（DRAWABLE 狀態移除）
}
