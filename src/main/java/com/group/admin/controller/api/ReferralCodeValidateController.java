package com.group.admin.controller.api;

import com.group.admin.req.referral.ReferralValidateReq;
import com.group.admin.res.referral.ReferralCodeRes;
import com.group.admin.res.referral.ReferralValidateRes;
import com.group.admin.service.ReferralCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 前台推薦碼驗證 Controller
 * 允許未登入使用者驗證推薦碼
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "前台 - 推薦碼驗證", description = "註冊時驗證推薦碼 API")
public class ReferralCodeValidateController {

    private final ReferralCodeService referralCodeService;

    /**
     * T012: POST /api/auth/validate-referral
     * 驗證推薦碼是否有效（供前端即時驗證使用，無需登入）
     */
    @PostMapping("/validate-referral")
    @Operation(summary = "驗證推薦碼（POST）", description = "驗證推薦碼是否有效，回傳 storeName 供 UX 使用（不需要登入）")
    public ResponseEntity<ReferralValidateRes> validateForRegistration(
            @Valid @RequestBody ReferralValidateReq req) {
        log.info("✅ [API] POST 驗證推薦碼: code={}", req.getCode());
        ReferralValidateRes res = referralCodeService.validateForRegistration(req.getCode());
        return ResponseEntity.ok(res);
    }

    /**
     * 驗證推薦碼是否有效（舊版 GET，保留向後相容）
     */
    @GetMapping("/referral-code/validate/{code}")
    @Operation(summary = "驗證推薦碼（GET）", description = "驗證推薦碼是否有效（不需要登入）")
    public ResponseEntity<Boolean> validateCode(@PathVariable String code) {
        log.info("✅ [API] GET 驗證推薦碼: code={}", code);
        boolean valid = referralCodeService.validateCode(code);
        return ResponseEntity.ok(valid);
    }

    /**
     * 取得推薦碼資訊（不含敏感資訊）
     */
    @GetMapping("/referral-code/info/{code}")
    @Operation(summary = "取得推薦碼資訊", description = "取得推薦碼基本資訊（不需要登入）")
    public ResponseEntity<ReferralCodeRes> getCodeInfo(@PathVariable String code) {
        log.info("🔍 [API] 查詢推薦碼資訊: code={}", code);
        ReferralCodeRes res = referralCodeService.getByCode(code);
        if (res == null) {
            return ResponseEntity.notFound().build();
        }
        res.setId(null);
        res.setUsedCount(null);
        return ResponseEntity.ok(res);
    }
}
