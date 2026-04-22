package com.group.admin.res.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPaymentInitRes {

    private String orderId;
    private String orderNumber;
    private Long shippingFee;
    private String paymentStatus;
    private String paymentUrl;
    private String gatewayTradeNo;
}