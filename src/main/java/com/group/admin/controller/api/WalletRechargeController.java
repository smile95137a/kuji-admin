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
        log.info("💳 [API] POST /wallet/recharge: userId={}, planId={}", userId, req.getPlanId());
        RechargeOrderRes res = rechargeService.createRechargeOrder(userId, req.getPlanId());
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
    public ResponseEntity<String> gatewayCallback(
            @RequestBody String rawPayload,
            @RequestHeader(value = "X-Signature", required = false) String signature) {
        log.info("📞 [Callback] POST /wallet/recharge/callback");
        GatewayCallbackResult result = new GatewayCallbackResult(
                rawPayload, true, null, null, LocalDateTime.now(), rawPayload);
        rechargeService.handleCallback(result);
        return ResponseEntity.ok("OK");
    }
}
