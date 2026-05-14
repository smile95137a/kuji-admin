package com.group.admin.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@ConditionalOnProperty(name = "payment.gateway.provider", havingValue = "stub")
public class StubShippingPaymentGatewayClient implements ShippingPaymentGatewayClient {

    @Override
    public ShippingPaymentResult createPayment(ShippingPaymentRequest request) {
        String tradeNo = "STUB-SHIP-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String payUrl = String.format(
                "/api/payment/callback/stub?orderNo=%s&tradeNo=%s&success=true&paidAt=%s",
                request.getOrderNumber(),
                tradeNo,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        );

        log.info("🔧 [Stub][ShippingPayment] 建立付款單成功: orderNo={}, tradeNo={}", request.getOrderNumber(), tradeNo);
        return ShippingPaymentResult.builder()
                .success(true)
                .payUrl(payUrl)
                .submitMethod("GET")
                .actionUrl(payUrl)
                .gatewayTradeNo(tradeNo)
                .build();
    }

    @Override
    public ShippingCallbackResult parseCallback(Map<String, String> params) {
        String orderNo = firstNonBlank(params, "orderNo", "Order_No", "merchantOrderId");
        String tradeNo = firstNonBlank(params, "tradeNo", "TradeNo", "gatewayTradeNo");
        boolean success = Boolean.parseBoolean(firstNonBlank(params, "success", "isSuccess", "paid"));

        return ShippingCallbackResult.builder()
                .orderNumber(orderNo)
                .gatewayTradeNo(tradeNo)
                .success(success)
                .errorMessage(success ? null : "Stub payment failed")
                .rawPayload(String.valueOf(params))
                .build();
    }

    private String firstNonBlank(Map<String, String> params, String... keys) {
        for (String key : keys) {
            String value = params.get(key);
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return null;
    }
}
