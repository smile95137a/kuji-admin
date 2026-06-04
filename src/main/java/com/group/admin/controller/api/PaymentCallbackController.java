package com.group.admin.controller.api;

import com.group.admin.gateway.ShippingCallbackResult;
import com.group.admin.gateway.ShippingPaymentGatewayClient;
import com.group.admin.service.BusinessEventLogService;
import com.group.admin.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentCallbackController {

    private final ShippingPaymentGatewayClient shippingPaymentGatewayClient;
    private final OrderService orderService;
    private final BusinessEventLogService businessEventLogService;

    @PostMapping("/shipping/callback")
    public ResponseEntity<String> gomypayCallback(@RequestParam Map<String, String> params) {
        log.info("📞 [PaymentCallback] 收到 GoMyPay shipping callback，paramsKeys={}", params.keySet());
        handleShippingCallback(params);
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/shipping/callback")
    public ResponseEntity<String> gomypayReturnCallback(@RequestParam Map<String, String> params) {
        log.info("?? [PaymentCallback] ?嗅 GoMyPay shipping return/callback嚗aramsKeys={}", params.keySet());
        handleShippingCallback(params);
        return ResponseEntity.ok("OK");
    }

    private void handleShippingCallback(Map<String, String> params) {
        ShippingCallbackResult callbackResult = null;
        try {
            callbackResult = shippingPaymentGatewayClient.parseCallback(params);
            orderService.handleShippingPaymentCallback(callbackResult);
            businessEventLogService.recordCallback(
                    BusinessEventLogService.EVENT_PAYMENT,
                    "SHIPPING_PAYMENT_CALLBACK_RECEIVED",
                    callbackResult.isSuccess() ? BusinessEventLogService.RESULT_SUCCESS : BusinessEventLogService.RESULT_FAILED,
                    "SHIPPING_PAYMENT",
                    callbackResult.getOrderNumber(),
                    callbackResult.getOrderNumber(),
                    null,
                    null,
                    null,
                    "GOMYPAY",
                    callbackResult.getGatewayTradeNo(),
                    null,
                    null,
                    null,
                    null,
                    params,
                    callbackResult.getErrorMessage());
        } catch (RuntimeException ex) {
            businessEventLogService.recordCallback(
                    BusinessEventLogService.EVENT_PAYMENT,
                    "SHIPPING_PAYMENT_CALLBACK_INVALID",
                    BusinessEventLogService.RESULT_FAILED,
                    "SHIPPING_PAYMENT",
                    params.get("e_orderno"),
                    params.get("e_orderno"),
                    null,
                    null,
                    null,
                    "GOMYPAY",
                    params.get("OrderID"),
                    null,
                    null,
                    null,
                    null,
                    params,
                    ex.getMessage());
            throw ex;
        }
    }

    @GetMapping("/shipping/callback/stub")
    public ResponseEntity<String> stubCallback(@RequestParam Map<String, String> params) {
        log.info("🔧 [PaymentCallback][Stub] 收到 callback: {}", params);
        ShippingCallbackResult callbackResult = shippingPaymentGatewayClient.parseCallback(params);
        orderService.handleShippingPaymentCallback(callbackResult);
        return ResponseEntity.ok("OK");
    }
}
