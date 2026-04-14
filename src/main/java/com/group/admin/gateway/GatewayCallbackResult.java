package com.group.admin.gateway;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record GatewayCallbackResult(
    String merchantOrderId,
    boolean success,
    String gatewayOrderId,
    BigDecimal amountTwd,
    LocalDateTime paidAt,
    String rawPayload
) {}
