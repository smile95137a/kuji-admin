package com.group.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "payment.gateway")
public class PaymentProperties {
    private String provider = "stub";
    private Stub stub = new Stub();
    private String callbackBaseUrl = "http://localhost:8080";

    @Data
    public static class Stub {
        private boolean alwaysSuccess = true;
    }
}
