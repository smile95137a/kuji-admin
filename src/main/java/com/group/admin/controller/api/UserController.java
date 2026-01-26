package com.group.admin.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.group.admin.entity.User;
import com.group.admin.mapper.UserMapper;
import com.group.admin.req.user.FrontendUserUpdateReq;
import com.group.admin.res.user.UserRes;
import com.group.admin.res.wallet.UserWalletRes;
import com.group.admin.service.UserService;
import com.group.admin.service.WalletService;
import com.group.admin.util.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 前台使用者 API
 * 
 * URL: /api/user/**
 * 角色：前台使用者
 */
@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "前台使用者", description = "使用者個人資訊 API")
public class UserController {

    private final UserService userService;
    private final WalletService walletService;
    private final UserMapper userMapper;

    @GetMapping("/hello")
    @Operation(summary = "測試 API", description = "測試使用者 API 是否正常")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("hello world");
    }

    @GetMapping("/me")
    @Operation(summary = "取得我的資訊", description = "取得當前登入使用者的個人資訊（含錢包餘額）")
    public ResponseEntity<UserRes> me() {
        log.info("🔍 [API] 查詢我的資訊");
        
        // ✅ 正確：使用 SecurityUtils 取得當前使用者 ID
        String userId = SecurityUtils.getCurrentUserId();
        
        if (userId == null) {
            log.warn("⚠️ 未登入或 Token 無效");
            return ResponseEntity.status(401).build();
        }
        
        // 根據 userId 查詢使用者（不是 email）
        User user = userService.findById(userId);
        
        if (user == null) {
            log.warn("⚠️ 使用者不存在: userId={}", userId);
            return ResponseEntity.status(404).build();
        }
        
        // 查詢錢包餘額
        UserWalletRes wallet = walletService.getWallet(userId);
        
        // 組合回應（包含使用者資訊 + 錢包餘額）
        UserRes res = UserRes.from(user);
        res.setGoldCoins(wallet.getGoldCoins());
        res.setBonusCoins(wallet.getBonusCoins());
        
        log.info("✅ 查詢成功: userId={}, email={}, gold={}, bonus={}", 
                userId, user.getEmail(), wallet.getGoldCoins(), wallet.getBonusCoins());
        
        // ✅ 回傳 DTO，不包含密碼等敏感資訊
        return ResponseEntity.ok(res);
    }

    @PutMapping("/me")
    @Operation(summary = "編輯我的資訊", description = "更新當前登入使用者的個人資訊（暱稱等）")
    public ResponseEntity<UserRes> updateMe(@Valid @RequestBody FrontendUserUpdateReq req) {
        log.info("✏️ [API] 編輯我的資訊：req={}", req);
        
        // ✅ 正確：使用 SecurityUtils 取得當前使用者 ID
        String userId = SecurityUtils.getCurrentUserId();
        
        if (userId == null) {
            log.warn("⚠️ 未登入或 Token 無效");
            return ResponseEntity.status(401).build();
        }
        
        // 根據 userId 查詢使用者
        User user = userService.findById(userId);
        
        if (user == null) {
            log.warn("⚠️ 使用者不存在: userId={}", userId);
            return ResponseEntity.status(404).build();
        }
        
        // 更新欄位（只更新非 null 的欄位）
        boolean updated = false;
        
        if (req.getNickname() != null && !req.getNickname().equals(user.getNickname())) {
            user.setNickname(req.getNickname());
            updated = true;
        }
        
        if (req.getEmail() != null && !req.getEmail().equals(user.getEmail())) {
            // 檢查 Email 是否已被其他人使用
            User existingUser = userService.findByEmail(req.getEmail());
            if (existingUser != null && !existingUser.getId().equals(userId)) {
                log.warn("⚠️ Email 已被使用: {}", req.getEmail());
                return ResponseEntity.status(409).build();  // 409 Conflict
            }
            user.setEmail(req.getEmail());
            updated = true;
        }
        
        if (req.getAvatar() != null) {
            user.setAvatar(req.getAvatar());
            updated = true;
        }
        
        if (req.getPhoneNumber() != null) {
            user.setPhoneNumber(req.getPhoneNumber());
            updated = true;
        }
        
        if (req.getLineId() != null) {
            user.setLineId(req.getLineId());
            updated = true;
        }
        
        if (req.getRecipientName() != null) {
            user.setRecipientName(req.getRecipientName());
            updated = true;
        }
        
        if (req.getRecipientPhone() != null) {
            user.setRecipientPhone(req.getRecipientPhone());
            updated = true;
        }
        
        if (req.getCity() != null) {
            user.setCity(req.getCity());
            updated = true;
        }
        
        if (req.getDistrict() != null) {
            user.setDistrict(req.getDistrict());
            updated = true;
        }
        
        if (req.getAddressDetail() != null) {
            user.setAddressDetail(req.getAddressDetail());
            updated = true;
        }
        
        if (req.getInvoiceType() != null) {
            user.setInvoiceType(req.getInvoiceType());
            updated = true;
        }
        
        if (req.getInvoiceEmail() != null) {
            user.setInvoiceEmail(req.getInvoiceEmail());
            updated = true;
        }
        
        if (req.getCarrierCode() != null) {
            user.setCarrierCode(req.getCarrierCode());
            updated = true;
        }
        
        if (req.getTaxId() != null) {
            user.setTaxId(req.getTaxId());
            updated = true;
        }
        
        if (req.getCompanyName() != null) {
            user.setCompanyName(req.getCompanyName());
            updated = true;
        }
        
        // 如果有更新,儲存到資料庫
        if (updated) {
            user.setUpdatedAt(java.time.LocalDateTime.now());
            userMapper.updateByPrimaryKey(user);
            log.info("✅ 更新成功: userId={}", userId);
        } else {
            log.info("ℹ️ 無需更新（沒有欄位變更）");
        }
        
        // 查詢錢包餘額
        UserWalletRes wallet = walletService.getWallet(userId);
        
        // 組合回應
        UserRes res = UserRes.from(user);
        res.setGoldCoins(wallet.getGoldCoins());
        res.setBonusCoins(wallet.getBonusCoins());
        
        return ResponseEntity.ok(res);
    }

}
