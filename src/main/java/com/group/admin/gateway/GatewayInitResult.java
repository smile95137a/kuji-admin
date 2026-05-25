package com.group.admin.gateway;

import java.util.Map;

public record GatewayInitResult(
    boolean success,
    String errorMessage,
    String payUrl,
    String gatewayOrderId,
    String provider,
    String submitMethod,
    String actionUrl,
    Map<String, String> formFields,
    String gatewayResult,
    String retMsg,
    String virtualAccount,
    String payInfo,
    String limitDate,
    String rawPayload
) {}
