package com.group.admin.controller.api;

import com.group.admin.req.referral.ApplyReferralReq;
import com.group.admin.res.referral.ReferralCodeRes;
import com.group.admin.res.referral.ReferralStatsRes;
import com.group.admin.service.ReferralCodeService;
import com.group.admin.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/referral")
@RequiredArgsConstructor
@Tag(name = "前台 - 推薦碼", description = "使用者推薦碼 API（需登入）")
public class ReferralController {

    private final ReferralCodeService referralCodeService;

    @PostMapping("/generate")
    @Operation(summary = "產生推薦碼", description = "為當前使用者產生專屬推薦碼")
    public ResponseEntity<ReferralCodeRes> generateCode() {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("🎫 產生推薦碼: userId={}", userId);
        ReferralCodeRes res = referralCodeService.generateCode(userId);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/validate")
    @Operation(summary = "驗證推薦碼", description = "驗證推薦碼是否可用（檢查有效性、上限、自推薦）")
    public ResponseEntity<ReferralCodeRes> validateCode(@RequestParam String code) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("🔍 驗證推薦碼: code={}, userId={}", code, userId);
        ReferralCodeRes res = referralCodeService.validateCodeForUser(code, userId);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/apply")
    @Operation(summary = "套用推薦碼", description = "使用推薦碼，雙方獲得獎勵")
    public ResponseEntity<Void> applyReferral(@Valid @RequestBody ApplyReferralReq req) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("🎁 套用推薦碼: userId={}, code={}", userId, req.getCode());
        referralCodeService.applyReferral(userId, req.getCode());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats")
    @Operation(summary = "推薦統計", description = "取得當前使用者的推薦統計")
    public ResponseEntity<ReferralStatsRes> getMyStats() {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("📊 取得推薦統計: userId={}", userId);
        ReferralStatsRes res = referralCodeService.getMyReferralStats(userId);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/{id}/disable")
    @Operation(summary = "停用推薦碼", description = "停用自己的推薦碼")
    public ResponseEntity<Void> disableCode(@PathVariable String id) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("🚫 停用推薦碼: codeId={}, userId={}", id, userId);
        referralCodeService.disableCode(id, userId);
        return ResponseEntity.ok().build();
    }
}
