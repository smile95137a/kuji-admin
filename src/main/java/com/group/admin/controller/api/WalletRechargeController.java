package com.group.admin.controller.api;

import com.group.admin.gateway.GatewayCallbackResult;
import com.group.admin.result.ApiResponse;
import com.group.admin.res.wallet.RechargeOrderRes;
import com.group.admin.req.wallet.RechargeReq;
import com.group.admin.service.RechargeService;
import com.group.admin.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/wallet/recharge")
@RequiredArgsConstructor
public class WalletRechargeController {

    private final RechargeService rechargeService;

    @PostMapping
    public ResponseEntity<ApiResponse<RechargeOrderRes>> createRechargeOrder(
            @Valid @RequestBody RechargeReq req) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("💳 [API] POST /wallet/recharge: userId={}, planId={}, paymentMethod={}",
                userId, req.getPlanId(), req.getPaymentMethod());
        RechargeOrderRes res = rechargeService.createRechargeOrder(userId, req.getPlanId(), req.getPaymentMethod());
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @GetMapping("/{rechargeOrderId}")
    public ResponseEntity<ApiResponse<RechargeOrderRes>> getRechargeOrder(@PathVariable String rechargeOrderId) {
        String userId = SecurityUtils.getCurrentUserId();
        RechargeOrderRes res = rechargeService.getRechargeOrder(userId, rechargeOrderId);
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @GetMapping("/callback/stub")
    public ResponseEntity<ApiResponse<String>> stubCallback(
            @RequestParam String orderId,
            @RequestParam(defaultValue = "true") boolean success) {
        log.info("🔧 [Stub Callback] orderId={}, success={}", orderId, success);
        GatewayCallbackResult result = new GatewayCallbackResult(
                orderId, success, "STUB-" + orderId, null,
                success ? LocalDateTime.now() : null,
                "{\"stub\":true,\"orderId\":\"" + orderId + "\"}");
        rechargeService.handleCallback(result);
        return ResponseEntity.ok(ApiResponse.success("OK"));
    }

    @PostMapping("/callback")
    public ResponseEntity<String> gatewayCallback(@RequestParam java.util.Map<String, String> params) {
        log.info("📞 [Callback] POST /wallet/recharge/callback, paramsKeys={}", params.keySet());
        GatewayCallbackResult result = rechargeService.verifyGatewayCallback(params);
        rechargeService.handleCallback(result);
        return ResponseEntity.ok("OK");
    }
}
