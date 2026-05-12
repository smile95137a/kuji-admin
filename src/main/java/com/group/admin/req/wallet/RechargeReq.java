package com.group.admin.req.wallet;

import com.group.admin.gateway.GoMyPaySupport;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RechargeReq {
    @NotBlank(message = "planId 不可為空")
    private String planId;

    /** 付款方式：CREDIT_CARD / BANK_TRANSFER */
    private String paymentMethod = GoMyPaySupport.PAYMENT_METHOD_CREDIT_CARD;
}
