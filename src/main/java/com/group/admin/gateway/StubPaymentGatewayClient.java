package com.group.admin.gateway;

import com.group.admin.entity.RechargeOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@Primary
@ConditionalOnProperty(name = "payment.gateway.provider", havingValue = "stub", matchIfMissing = true)
public class StubPaymentGatewayClient implements PaymentGatewayClient {

    @Override
    public GatewayInitResult charge(RechargeOrder order, String paymentMethod) {
        log.info("🔧 [Stub] charge called for orderId={}", order.getId());
        String payUrl = "/api/wallet/recharge/callback/stub?orderId=" + order.getId() + "&success=true";
        return new GatewayInitResult(payUrl, "STUB-" + order.getId(), "stub");
    }

    @Override
    public GatewayCallbackResult verifyCallback(java.util.Map<String, String> params) {
        log.info("🔧 [Stub] verifyCallback called");
        return null; // not used directly; stub callback handled in controller
    }
}
