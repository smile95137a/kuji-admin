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
        long amount = GoMyPaySupport.normalizeAmount(order.getPriceTwd());

        Map<String, String> params = new LinkedHashMap<>();
        params.put("Send_Type", GoMyPaySupport.isBankTransfer(normalizedMethod) ? "4" : "0");
        params.put("Pay_Mode_No", "2");
        params.put("CustomerId", GoMyPaySupport.safe(properties.getShopId()));
        params.put("Order_No", order.getId());
        params.put("Amount", String.valueOf(amount));
        params.put("Buyer_Name", GoMyPaySupport.safe(order.getBuyerName(), "KUJI會員"));
        params.put("Buyer_Telm", GoMyPaySupport.safe(order.getBuyerPhone(), "0900000000"));
        params.put("Buyer_Mail", GoMyPaySupport.safe(order.getBuyerEmail(), "noreply@kuji.local"));
        params.put("Buyer_Memo", "KUJI 儲值訂單 " + order.getId());
        if (!GoMyPaySupport.isBankTransfer(normalizedMethod)) {
            params.put("TransCode", "00");
            params.put("TransMode", "1");
            params.put("Installment", "0");
        }
        params.put("Return_url", GoMyPaySupport.safe(properties.getRechargeReturnUrl()));
        params.put("Callback_Url", GoMyPaySupport.safe(properties.getRechargeNotifyUrl()));
        params.put("Str_Check", GoMyPaySupport.safe(properties.getHashKey()));

        String payUrl = GoMyPaySupport.buildPayUrl(properties.getApiUrl(), params);
        log.info("💳 [GoMyPay][Recharge] 建立付款單：rechargeOrderId={}, paymentMethod={}, amount={}",
                order.getId(), normalizedMethod, amount);
        return new GatewayInitResult(payUrl, order.getId(), "gomypay");
    }

    @Override
    public GatewayCallbackResult verifyCallback(Map<String, String> params) {
        GoMyPaySupport.verifyCallback(params, properties);
        String merchantOrderId = GoMyPaySupport.firstNonBlank(params, "e_orderno", "Order_No");
        boolean success = GoMyPaySupport.isSuccess(GoMyPaySupport.firstNonBlank(params, "result"));
        BigDecimal amount = new BigDecimal(GoMyPaySupport.firstNonBlank(params, "PayAmount", "e_money", "Amount", "0"));
        String gatewayOrderId = GoMyPaySupport.firstNonBlank(params, "OrderID");
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
