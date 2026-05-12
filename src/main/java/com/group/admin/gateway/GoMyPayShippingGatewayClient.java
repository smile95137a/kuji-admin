package com.group.admin.gateway;

import com.group.admin.config.GoMyPayProperties;
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
public class GoMyPayShippingGatewayClient implements ShippingPaymentGatewayClient {

    private final GoMyPayProperties properties;

    @Override
    public ShippingPaymentResult createPayment(ShippingPaymentRequest request) {
        if (request == null || request.getMerchantOrderNo() == null) {
            return ShippingPaymentResult.builder()
                    .success(false)
                    .errorMessage("缺少訂單資訊")
                    .build();
        }

        String paymentMethod = GoMyPaySupport.normalizePaymentMethod(request.getPaymentMethod());
        long amount = GoMyPaySupport.normalizeAmount(request.getAmount());

        Map<String, String> params = new LinkedHashMap<>();
        params.put("Send_Type", GoMyPaySupport.isBankTransfer(paymentMethod) ? "4" : "0");
        params.put("Pay_Mode_No", "2");
        params.put("CustomerId", GoMyPaySupport.safe(properties.getShopId()));
        params.put("Order_No", request.getMerchantOrderNo());
        params.put("Amount", String.valueOf(amount));
        params.put("Buyer_Name", GoMyPaySupport.safe(request.getBuyerName()));
        params.put("Buyer_Telm", GoMyPaySupport.safe(request.getBuyerPhone()));
        params.put("Buyer_Mail", GoMyPaySupport.safe(request.getBuyerEmail()));
        params.put("Buyer_Memo", GoMyPaySupport.safe(request.getItemDescription()));
        if (!GoMyPaySupport.isBankTransfer(paymentMethod)) {
            params.put("TransCode", "00");
            params.put("TransMode", "1");
            params.put("Installment", "0");
        }
        params.put("Return_url", GoMyPaySupport.safe(properties.getShippingReturnUrl(), properties.getReturnUrl()));
        params.put("Callback_Url", GoMyPaySupport.safe(properties.getShippingNotifyUrl(), properties.getNotifyUrl()));
        params.put("Str_Check", GoMyPaySupport.safe(properties.getHashKey()));

        String payUrl = GoMyPaySupport.buildPayUrl(properties.getApiUrl(), params);
        log.info("💳 [GoMyPay] 建立運費付款單: merchantOrderNo={}, paymentMethod={}, amount={}",
            request.getMerchantOrderNo(), paymentMethod, amount);

        return ShippingPaymentResult.builder()
                .success(true)
            .gatewayTradeNo(request.getMerchantOrderNo())
                .payUrl(payUrl)
                .build();
    }

    @Override
    public ShippingCallbackResult parseCallback(Map<String, String> params) {
        GoMyPaySupport.verifyCallback(params, properties);
        String orderNo = GoMyPaySupport.firstNonBlank(params, "e_orderno", "Order_No", "orderNo", "MerchantOrderNo");
        String tradeNo = GoMyPaySupport.firstNonBlank(params, "OrderID", "TradeNo", "gatewayTradeNo", "Trade_No");

        String statusRaw = GoMyPaySupport.firstNonBlank(params,
                "Status", "status", "PayStatus", "payStatus", "RtnCode", "rtnCode", "success");
        if (statusRaw == null) {
            statusRaw = GoMyPaySupport.firstNonBlank(params, "result");
        }
        boolean success = GoMyPaySupport.isSuccess(statusRaw);

        String errorMessage = success ? null : GoMyPaySupport.firstNonBlank(params,
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
}