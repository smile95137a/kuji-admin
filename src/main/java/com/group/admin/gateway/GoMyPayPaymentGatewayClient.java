package com.group.admin.gateway;

import com.group.admin.config.GoMyPayProperties;
import com.group.admin.entity.RechargeOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "payment.gateway.provider", havingValue = "gomypay")
public class GoMyPayPaymentGatewayClient implements PaymentGatewayClient {

    private final GoMyPayProperties properties;

    @Override
    public GatewayInitResult charge(RechargeOrder order, String paymentMethod) {
        String normalizedMethod = GoMyPaySupport.normalizePaymentMethod(paymentMethod);
        boolean bankTransfer = GoMyPaySupport.isBankTransfer(normalizedMethod);
        long amount = GoMyPaySupport.normalizeAmount(order.getPriceTwd());
        String returnUrl = GoMyPaySupport.safe(properties.getRechargeReturnUrl());
        String callbackUrl = GoMyPaySupport.safe(properties.getRechargeNotifyUrl());
        GoMyPaySupport.validatePaymentRequestConfig(properties, returnUrl, callbackUrl);
        GoMyPaySupport.validateMerchantOrderNo(order.getId());

        Map<String, String> params = new LinkedHashMap<>();
        params.put("Send_Type", bankTransfer ? "4" : "0");
        params.put("Pay_Mode_No", "2");
        params.put("CustomerId", GoMyPaySupport.safe(properties.getShopId()));
        params.put("Order_No", order.getId());
        params.put("Amount", String.valueOf(amount));
        params.put("Buyer_Name", GoMyPaySupport.sanitizeBuyerName(order.getBuyerName(), "KUJI會員"));
        params.put("Buyer_Telm", GoMyPaySupport.sanitizePhone(order.getBuyerPhone(), "0900000000"));
        params.put("Buyer_Mail", GoMyPaySupport.sanitizeEmail(order.getBuyerEmail(), "noreply@kuji.local"));
        params.put("Buyer_Memo", GoMyPaySupport.sanitizeBuyerMemo("KUJI 儲值訂單 " + order.getId(), "KUJI 儲值訂單"));

        if (bankTransfer) {
            params.put("e_return", "1");
        } else {
            params.put("TransCode", "00");
            params.put("TransMode", "1");
            params.put("Installment", "0");
        }
        params.put("Return_url", returnUrl);
        params.put("Callback_Url", callbackUrl);

        params.put("Str_Check", GoMyPaySupport.computeRequestChecksum(order.getId(), amount, properties));
        String payUrl = GoMyPaySupport.buildPayUrl(properties.getApiUrl(), params);

        log.info("[GoMyPay][Recharge] 初始化付款參數 rechargeOrderId={}, paymentMethod={}, amount={}, customerIdLength={}, hasReturnUrl={}, hasCallbackUrl={}",
                order.getId(),
                normalizedMethod,
                amount,
                GoMyPaySupport.safe(properties.getShopId()).length(),
                !returnUrl.isBlank(),
                !callbackUrl.isBlank());

        return new GatewayInitResult(
                payUrl,
                order.getId(),
                "gomypay",
                "POST",
                properties.getApiUrl(),
                params
        );
    }

    @Override
    public GatewayCallbackResult verifyCallback(Map<String, String> params) {
        GoMyPaySupport.verifyCallback(params, properties);
        String merchantOrderId = GoMyPaySupport.firstNonBlank(params, "e_orderno", "Order_No", "orderNo", "MerchantOrderNo");
        boolean success = GoMyPaySupport.isSuccess(GoMyPaySupport.firstNonBlank(params, "result"));
        String amountRaw = GoMyPaySupport.firstNonBlank(params, "PayAmount", "e_money", "Amount");
        BigDecimal amount = amountRaw != null ? new BigDecimal(amountRaw) : BigDecimal.ZERO;
        String gatewayOrderId = GoMyPaySupport.firstNonBlank(params, "OrderID", "TradeNo", "gatewayTradeNo", "Trade_No");
        return new GatewayCallbackResult(
                merchantOrderId,
                success,
                gatewayOrderId,
                amount,
                GoMyPaySupport.parsePaidAt(params),
                String.valueOf(params)
        );
    }
}
