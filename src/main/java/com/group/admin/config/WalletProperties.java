package com.group.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "wallet")
public class WalletProperties {
    private RechargeOrder rechargeOrder = new RechargeOrder();
    private OptimisticLock optimisticLock = new OptimisticLock();

    @Data
    public static class RechargeOrder {
        private int ttlMinutes = 30;
    }

    @Data
    public static class OptimisticLock {
        private int maxRetries = 3;
    }
}
