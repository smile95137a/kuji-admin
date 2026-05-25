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
    private final GoMyPayFormClient formClient;

    @Override
    public GatewayInitResult charge(RechargeOrder order, String paymentMethod) {
        String normalizedMethod = GoMyPaySupport.normalizePaymentMethod(paymentMethod);
        boolean bankTransfer = GoMyPaySupport.isBankTransfer(normalizedMethod);
        long amount = GoMyPaySupport.normalizeAmount(order.getPriceTwd());
        String returnUrl = GoMyPaySupport.safe(properties.getRechargeReturnUrl());
        String callbackUrl = GoMyPaySupport.safe(properties.getRechargeNotifyUrl());
        GoMyPaySupport.validatePaymentRequestConfig(properties, returnUrl, callbackUrl);
        GoMyPaySupport.validateMerchantOrderNo(order.getId());

        Map<String, String> params = buildParams(order, amount, bankTransfer, returnUrl, callbackUrl);

        if (bankTransfer) {
            return createBankTransfer(order, params);
        }

        log.info("[GoMyPay][Recharge] 建立信用卡託管付款: rechargeOrderId={}, amount={}", order.getId(), amount);

        return new GatewayInitResult(
                true,
                null,
                properties.getApiUrl(),
                order.getId(),
                "gomypay",
                "POST",
                properties.getApiUrl(),
                params,
                null,
                null,
                null,
                null,
                null,
                GoMyPaySupport.toDebugParams(params).toString()
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

    private Map<String, String> buildParams(RechargeOrder order, long amount, boolean bankTransfer,
                                            String returnUrl, String callbackUrl) {
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
        params.put("Return_url", returnUrl);
        params.put("Callback_Url", callbackUrl);
        if (bankTransfer) {
            params.put("e_return", "1");
        } else {
            params.put("TransCode", "00");
            params.put("TransMode", "1");
            params.put("Installment", "0");
        }
        params.put("Str_Check", GoMyPaySupport.computeRequestChecksum(order.getId(), amount, properties));
        return params;
    }

    private GatewayInitResult createBankTransfer(RechargeOrder order, Map<String, String> params) {
        Map<String, String> response = formClient.postForm(properties.getApiUrl(), params);
        String result = GoMyPaySupport.firstNonBlank(response, "result");
        String retMsg = GoMyPaySupport.firstNonBlank(response, "ret_msg", "Message", "msg");
        String virtualAccount = GoMyPaySupport.firstNonBlank(response, "e_payaccount", "payaccount", "VirtualAccount");
        String payInfo = GoMyPaySupport.firstNonBlank(response, "e_PayInfo", "PayInfo", "bankname");
        String limitDate = GoMyPaySupport.firstNonBlank(response, "LimitDate", "limitDate", "limit_date");
        String gatewayOrderId = GoMyPaySupport.firstNonBlank(response, "OrderID", "TradeNo", "gatewayTradeNo", "Trade_No");
        if (gatewayOrderId == null) {
            gatewayOrderId = order.getId();
        }
        boolean success = GoMyPaySupport.isSuccess(result) || virtualAccount != null;
        String rawPayload = String.valueOf(response);

        if (!success && isJsonIntegrationRejected(retMsg)) {
            Map<String, String> redirectParams = new LinkedHashMap<>(params);
            redirectParams.remove("e_return");
            log.info("[GoMyPay][Recharge] 銀行轉帳 JSON 模式未啟用，改用頁面導轉: rechargeOrderId={}", order.getId());
            return new GatewayInitResult(
                    true,
                    null,
                    properties.getApiUrl(),
                    order.getId(),
                    "gomypay",
                    "POST",
                    properties.getApiUrl(),
                    redirectParams,
                    result,
                    retMsg,
                    null,
                    null,
                    null,
                    rawPayload
            );
        }

        log.info("[GoMyPay][Recharge] 建立銀行轉帳: rechargeOrderId={}, success={}, hasVirtualAccount={}",
                order.getId(), success, virtualAccount != null);

        return new GatewayInitResult(
                success,
                success ? null : GoMyPaySupport.safe(retMsg, "銀行轉帳付款建立失敗"),
                null,
                gatewayOrderId,
                "gomypay",
                null,
                null,
                null,
                result,
                retMsg,
                virtualAccount,
                payInfo,
                limitDate,
                rawPayload
        );
    }

    private boolean isJsonIntegrationRejected(String retMsg) {
        return retMsg != null && retMsg.contains("無法用JSON串接");
    }
}
