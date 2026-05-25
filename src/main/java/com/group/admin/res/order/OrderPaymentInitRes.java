package com.group.admin.res.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPaymentInitRes {

    private String orderId;
    private String orderNumber;
    private Long shippingFee;
    private String paymentStatus;
    private String paymentMethod;
    private String paymentUrl;
    private String submitMethod;
    private String actionUrl;
    private Map<String, String> formFields;
    private String gatewayTradeNo;
    private String gatewayResult;
    private String retMsg;
    private String virtualAccount;
    private String payInfo;
    private String limitDate;
}
