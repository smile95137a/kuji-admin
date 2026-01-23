package com.group.admin.controller.api;

import com.group.admin.res.referral.ReferralCodeRes;
import com.group.admin.service.ReferralCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 前台推薦碼驗證 Controller
 * 允許未登入使用者驗證推薦碼
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/auth/referral-code")
@RequiredArgsConstructor
@Tag(name = "前台 - 推薦碼驗證", description = "註冊時驗證推薦碼 API")
public class ReferralCodeValidateController {
    
    private final ReferralCodeService referralCodeService;
    
    /**
     * 驗證推薦碼是否有效
     * 供註冊頁面即時驗證使用
     */
    @GetMapping("/validate/{code}")
    @Operation(summary = "驗證推薦碼", description = "驗證推薦碼是否有效（不需要登入）")
    public ResponseEntity<Boolean> validateCode(@PathVariable String code) {
        log.info("✅ [API] 驗證推薦碼: code={}", code);
        boolean valid = referralCodeService.validateCode(code);
        return ResponseEntity.ok(valid);
    }
    
    /**
     * 取得推薦碼資訊（不含敏感資訊）
     * 供顯示推薦碼所屬店家資訊
     */
    @GetMapping("/info/{code}")
    @Operation(summary = "取得推薦碼資訊", description = "取得推薦碼基本資訊（不需要登入）")
    public ResponseEntity<ReferralCodeRes> getCodeInfo(@PathVariable String code) {
        log.info("🔍 [API] 查詢推薦碼資訊: code={}", code);
        ReferralCodeRes res = referralCodeService.getByCode(code);
        
        if (res == null) {
            return ResponseEntity.notFound().build();
        }
        
        // 移除敏感資訊
        res.setId(null);
        res.setUsedCount(null);
        
        return ResponseEntity.ok(res);
    }
}
