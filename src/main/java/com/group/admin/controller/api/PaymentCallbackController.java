package com.group.admin.controller.api;

import com.group.admin.gateway.ShippingCallbackResult;
import com.group.admin.gateway.ShippingPaymentGatewayClient;
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

    @PostMapping("/callback")
    public ResponseEntity<String> gomypayCallback(@RequestParam Map<String, String> params) {
        log.info("📞 [PaymentCallback] 收到 GoMyPay callback，paramsKeys={}", params.keySet());
        ShippingCallbackResult callbackResult = shippingPaymentGatewayClient.parseCallback(params);
        orderService.handleShippingPaymentCallback(callbackResult);
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/callback/stub")
    public ResponseEntity<String> stubCallback(@RequestParam Map<String, String> params) {
        log.info("🔧 [PaymentCallback][Stub] 收到 callback: {}", params);
        ShippingCallbackResult callbackResult = shippingPaymentGatewayClient.parseCallback(params);
        orderService.handleShippingPaymentCallback(callbackResult);
        return ResponseEntity.ok("OK");
    }
}