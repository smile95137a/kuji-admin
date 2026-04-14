package com.group.admin.gateway;

import com.group.admin.entity.RechargeOrder;

public interface PaymentGatewayClient {
    GatewayInitResult charge(RechargeOrder order);
    GatewayCallbackResult verifyCallback(String rawPayload, String signature);
}
