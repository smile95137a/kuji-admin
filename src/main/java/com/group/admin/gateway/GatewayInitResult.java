package com.group.admin.gateway;

import java.util.Map;

public record GatewayInitResult(
    String payUrl,
    String gatewayOrderId,
    String provider,
    String submitMethod,
    String actionUrl,
    Map<String, String> formFields
) {}
