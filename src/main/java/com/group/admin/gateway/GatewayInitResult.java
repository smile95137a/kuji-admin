package com.group.admin.gateway;

public record GatewayInitResult(
    String payUrl,
    String gatewayOrderId,
    String provider
) {}
