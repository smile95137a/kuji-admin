package com.group.admin.service.payment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentResult {
    private boolean success;
    private String transactionId;
    private String message;
    private Long amount;
}
