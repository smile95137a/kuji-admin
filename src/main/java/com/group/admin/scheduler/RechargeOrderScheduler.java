package com.group.admin.scheduler;

import com.group.admin.mapper.RechargeOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class RechargeOrderScheduler {
    
    private final RechargeOrderMapper rechargeOrderMapper;
    
    @Scheduled(fixedRate = 300000)  // every 5 minutes
    public void expireRechargeOrders() {
        try {
            int expired = rechargeOrderMapper.updateExpiredOrders(LocalDateTime.now());
            if (expired > 0) {
                log.info("⏰ Expired {} pending recharge orders", expired);
            }
        } catch (Exception e) {
            log.error("❌ Failed to expire recharge orders: {}", e.getMessage());
        }
    }
}
