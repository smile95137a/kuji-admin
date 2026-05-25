package com.group.admin.gateway;

import com.group.admin.config.GoMyPayProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "payment.gateway.provider", havingValue = "gomypay")
public class GoMyPayShippingGatewayClient implements ShippingPaymentGatewayClient {

    private final GoMyPayProperties properties;
    private final GoMyPayFormClient formClient;

    @Override
    public ShippingPaymentResult createPayment(ShippingPaymentRequest request) {
        if (request == null || request.getMerchantOrderNo() == null) {
            return ShippingPaymentResult.builder()
                    .success(false)
                    .errorMessage("缺少付款訂單資料")
                    .build();
        }

        String paymentMethod = GoMyPaySupport.normalizePaymentMethod(request.getPaymentMethod());
        boolean bankTransfer = GoMyPaySupport.isBankTransfer(paymentMethod);
        long amount = GoMyPaySupport.normalizeAmount(request.getAmount());
        String returnUrl = GoMyPaySupport.safe(properties.getShippingReturnUrl(), properties.getReturnUrl());
        String callbackUrl = GoMyPaySupport.safe(properties.getShippingNotifyUrl(), properties.getNotifyUrl());
        GoMyPaySupport.validatePaymentRequestConfig(properties, returnUrl, callbackUrl);
        GoMyPaySupport.validateMerchantOrderNo(request.getMerchantOrderNo());

        Map<String, String> params = buildParams(request, amount, bankTransfer, returnUrl, callbackUrl);

        if (bankTransfer) {
            return createBankTransfer(request, params);
        }

        log.info("[GoMyPay][Shipping] 建立信用卡託管付款: merchantOrderNo={}, amount={}",
                request.getMerchantOrderNo(), amount);

        return ShippingPaymentResult.builder()
                .success(true)
                .gatewayTradeNo(request.getMerchantOrderNo())
                .payUrl(properties.getApiUrl())
                .submitMethod("POST")
                .actionUrl(properties.getApiUrl())
                .formFields(params)
                .rawPayload(GoMyPaySupport.toDebugParams(params).toString())
                .build();
    }

    @Override
    public ShippingCallbackResult parseCallback(Map<String, String> params) {
        GoMyPaySupport.verifyCallback(params, properties);
        String orderNo = GoMyPaySupport.firstNonBlank(params, "e_orderno", "Order_No", "orderNo", "MerchantOrderNo");
        String tradeNo = GoMyPaySupport.firstNonBlank(params, "OrderID", "TradeNo", "gatewayTradeNo", "Trade_No");
        String statusRaw = GoMyPaySupport.firstNonBlank(params, "Status", "status", "PayStatus", "payStatus", "RtnCode", "rtnCode", "success");
        if (statusRaw == null) {
            statusRaw = GoMyPaySupport.firstNonBlank(params, "result");
        }
        boolean success = GoMyPaySupport.isSuccess(statusRaw);
        String errorMessage = success ? null : GoMyPaySupport.firstNonBlank(params, "ret_msg", "Message", "ErrMsg", "error", "errorMessage", "msg");
        if (!success && (errorMessage == null || errorMessage.isBlank())) {
            errorMessage = "GoMyPay callback status=" + statusRaw;
        }

        log.info("[GoMyPay][Shipping] callback: orderNo={}, tradeNo={}, success={}, rawStatus={}",
                orderNo, tradeNo, success, statusRaw);

        return ShippingCallbackResult.builder()
                .orderNumber(orderNo)
                .gatewayTradeNo(tradeNo)
                .success(success)
                .errorMessage(errorMessage)
                .rawPayload(String.valueOf(params))
                .build();
    }

    private Map<String, String> buildParams(ShippingPaymentRequest request, long amount, boolean bankTransfer,
                                            String returnUrl, String callbackUrl) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("Send_Type", bankTransfer ? "4" : "0");
        params.put("Pay_Mode_No", "2");
        params.put("CustomerId", GoMyPaySupport.safe(properties.getShopId()));
        params.put("Order_No", request.getMerchantOrderNo());
        params.put("Amount", String.valueOf(amount));
        params.put("Buyer_Name", GoMyPaySupport.sanitizeBuyerName(request.getBuyerName(), "KUJI會員"));
        params.put("Buyer_Telm", GoMyPaySupport.sanitizePhone(request.getBuyerPhone(), "0900000000"));
        params.put("Buyer_Mail", GoMyPaySupport.sanitizeEmail(request.getBuyerEmail(), "noreply@kuji.local"));
        params.put("Buyer_Memo", GoMyPaySupport.sanitizeBuyerMemo(request.getItemDescription(), "KUJI 訂單運費"));
        params.put("Return_url", returnUrl);
        params.put("Callback_Url", callbackUrl);
        if (bankTransfer) {
            params.put("e_return", "1");
        } else {
            params.put("TransCode", "00");
            params.put("TransMode", "1");
            params.put("Installment", "0");
        }
        params.put("Str_Check", GoMyPaySupport.computeRequestChecksum(request.getMerchantOrderNo(), amount, properties));
        return params;
    }

    private ShippingPaymentResult createBankTransfer(ShippingPaymentRequest request, Map<String, String> params) {
        Map<String, String> response = formClient.postForm(properties.getApiUrl(), params);
        String result = GoMyPaySupport.firstNonBlank(response, "result");
        String retMsg = GoMyPaySupport.firstNonBlank(response, "ret_msg", "Message", "msg");
        String virtualAccount = GoMyPaySupport.firstNonBlank(response, "e_payaccount", "payaccount", "VirtualAccount");
        String payInfo = GoMyPaySupport.firstNonBlank(response, "e_PayInfo", "PayInfo", "bankname");
        String limitDate = GoMyPaySupport.firstNonBlank(response, "LimitDate", "limitDate", "limit_date");
        String gatewayTradeNo = request.getMerchantOrderNo();
        boolean success = GoMyPaySupport.isSuccess(result) || virtualAccount != null;
        String rawPayload = String.valueOf(response);

        if (!success && isJsonIntegrationRejected(retMsg)) {
            Map<String, String> redirectParams = new LinkedHashMap<>(params);
            redirectParams.remove("e_return");
            log.info("[GoMyPay][Shipping] 銀行轉帳 JSON 模式未啟用，改用頁面導轉: merchantOrderNo={}",
                    request.getMerchantOrderNo());
            return ShippingPaymentResult.builder()
                    .success(true)
                    .gatewayTradeNo(request.getMerchantOrderNo())
                    .payUrl(properties.getApiUrl())
                    .submitMethod("POST")
                    .actionUrl(properties.getApiUrl())
                    .formFields(redirectParams)
                    .gatewayResult(result)
                    .retMsg(retMsg)
                    .rawPayload(rawPayload)
                    .build();
        }

        log.info("[GoMyPay][Shipping] 建立銀行轉帳: merchantOrderNo={}, success={}, hasVirtualAccount={}",
                request.getMerchantOrderNo(), success, virtualAccount != null);

        return ShippingPaymentResult.builder()
                .success(success)
                .errorMessage(success ? null : GoMyPaySupport.safe(retMsg, "銀行轉帳付款建立失敗"))
                .gatewayTradeNo(request.getMerchantOrderNo())
                .gatewayResult(result)
                .retMsg(retMsg)
                .virtualAccount(virtualAccount)
                .payInfo(payInfo)
                .limitDate(limitDate)
                .rawPayload(rawPayload)
                .build();
    }

    private boolean isJsonIntegrationRejected(String retMsg) {
        return retMsg != null && retMsg.contains("無法用JSON串接");
    }
}
