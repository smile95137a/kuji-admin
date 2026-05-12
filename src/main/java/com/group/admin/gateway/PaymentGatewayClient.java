package com.group.admin.gateway;

import com.group.admin.entity.RechargeOrder;

import java.util.Map;

public interface PaymentGatewayClient {
    GatewayInitResult charge(RechargeOrder order, String paymentMethod);
    GatewayCallbackResult verifyCallback(Map<String, String> params);
}
