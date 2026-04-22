package com.group.admin.gateway;

import com.group.admin.config.GoMyPayProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "payment.gateway.provider", havingValue = "gomypay")
public class GoMyPayShippingGatewayClient implements ShippingPaymentGatewayClient {

    private final GoMyPayProperties properties;

    @Override
    public ShippingPaymentResult createPayment(ShippingPaymentRequest request) {
        if (request == null || request.getOrderNumber() == null) {
            return ShippingPaymentResult.builder()
                    .success(false)
                    .errorMessage("缺少訂單資訊")
                    .build();
        }

        String tradeNo = "GMP-SHIP-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        long amount = request.getAmount() == null
                ? 0L
                : request.getAmount().setScale(0, RoundingMode.HALF_UP).longValue();

        Map<String, String> params = new LinkedHashMap<>();
        params.put("ShopID", safe(properties.getShopId()));
        params.put("Order_No", request.getOrderNumber());
        params.put("Amount", String.valueOf(amount));
        params.put("ItemDesc", safe(request.getItemDescription()));
        params.put("BuyerName", safe(request.getBuyerName()));
        params.put("BuyerEmail", safe(request.getBuyerEmail()));
        params.put("BuyerPhone", safe(request.getBuyerPhone()));
        params.put("ReturnURL", safe(properties.getReturnUrl()));
        params.put("NotifyURL", safe(properties.getNotifyUrl()));
        params.put("TimeStamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        params.put("TradeNo", tradeNo);

        String payUrl = buildPayUrl(params);
        log.info("💳 [GoMyPay] 建立付款單: orderNo={}, tradeNo={}, amount={}", request.getOrderNumber(), tradeNo, amount);

        return ShippingPaymentResult.builder()
                .success(true)
                .gatewayTradeNo(tradeNo)
                .payUrl(payUrl)
                .build();
    }

    @Override
    public ShippingCallbackResult parseCallback(Map<String, String> params) {
        String orderNo = firstNonBlank(params, "Order_No", "orderNo", "MerchantOrderNo");
        String tradeNo = firstNonBlank(params, "TradeNo", "gatewayTradeNo", "Trade_No");

        String statusRaw = firstNonBlank(params,
                "Status", "status", "PayStatus", "payStatus", "RtnCode", "rtnCode", "success");
        boolean success = isSuccess(statusRaw);

        String errorMessage = success ? null : firstNonBlank(params,
                "Message", "ErrMsg", "error", "errorMessage", "msg");
        if (!success && (errorMessage == null || errorMessage.isBlank())) {
            errorMessage = "GoMyPay callback status=" + statusRaw;
        }

        log.info("📞 [GoMyPay] callback: orderNo={}, tradeNo={}, success={}, rawStatus={}",
                orderNo, tradeNo, success, statusRaw);

        return ShippingCallbackResult.builder()
                .orderNumber(orderNo)
                .gatewayTradeNo(tradeNo)
                .success(success)
                .errorMessage(errorMessage)
                .rawPayload(String.valueOf(params))
                .build();
    }

    private String buildPayUrl(Map<String, String> params) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(safe(properties.getApiUrl()));
        params.forEach(builder::queryParam);
        return builder.build(true).encode(StandardCharsets.UTF_8).toUriString();
    }

    private String safe(String value) {
        return value == null ? "" : value;
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

    private boolean isSuccess(String value) {
        if (value == null) {
            return false;
        }
        String normalized = value.trim().toUpperCase();
        return "1".equals(normalized)
                || "Y".equals(normalized)
                || "TRUE".equals(normalized)
                || "SUCCESS".equals(normalized)
                || "PAID".equals(normalized)
                || "00".equals(normalized);
    }
}