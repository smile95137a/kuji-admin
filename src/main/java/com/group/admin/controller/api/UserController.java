package com.group.admin.controller.api;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.group.admin.entity.User;
import com.group.admin.mapper.UserMapper;
import com.group.admin.req.referral.ApplyReferralReq;
import com.group.admin.req.user.FrontendUserUpdateReq;
import com.group.admin.res.user.UserRes;
import com.group.admin.service.S3Service;
import com.group.admin.service.UserService;
import com.group.admin.util.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    private final S3Service s3Service;

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
        // ✅ 金幣/紅利已直接存在 user 表，不再需要查詢 user_wallet
        
        // 組合回應（包含使用者資訊 + 金幣餘額）
        UserRes res = UserRes.from(user);
        
        log.info("✅ 查詢成功: userId={}, email={}, gold={}, bonus={}", 
                userId, user.getEmail(), user.getGoldCoins(), user.getBonusCoins());
        
        // ✅ 回傳 DTO，不包含密碼等敏感資訊
        return ResponseEntity.ok(res);
    }

    @PutMapping("/me")
    @Operation(summary = "編輯我的資訊", description = "更新當前登入使用者的個人資訊（暱稱等）")
    public ResponseEntity<UserRes> updateMe(@RequestBody FrontendUserUpdateReq req) {
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
        
        if (req.getNickname() != null && !req.getNickname().isEmpty() && !req.getNickname().equals(user.getNickname())) {
            user.setNickname(req.getNickname());
            updated = true;
        }
        
        if (req.getEmail() != null && !req.getEmail().isEmpty() && !req.getEmail().equals(user.getEmail())) {
            // 檢查 Email 是否已被其他人使用
            User existingUser = userService.findByEmail(req.getEmail());
            if (existingUser != null && !existingUser.getId().equals(userId)) {
                log.warn("⚠️ Email 已被使用: {}", req.getEmail());
                return ResponseEntity.status(409).build();  // 409 Conflict
            }
            user.setEmail(req.getEmail());
            user.setEmailVerified((byte) 0);  // Email 改變後需重新驗證
            updated = true;
        }
        
        // ✅ 支援移除頭像：傳空字串或 null 清空頭像
        if (req.getAvatar() != null) {
            if (req.getAvatar().isEmpty()) {
                log.info("🗑️ 移除頭像");
                user.setAvatar(null);  // 清空頭像
                updated = true;
            } else if (!req.getAvatar().equals(user.getAvatar())) {
                log.info("🖼️ 更新頭像: {}", req.getAvatar());
                user.setAvatar(req.getAvatar());
                updated = true;
            }
        }
        
        if (req.getPhoneNumber() != null && !req.getPhoneNumber().isEmpty()) {
            user.setPhoneNumber(req.getPhoneNumber());
            updated = true;
        }
        
        if (req.getLineId() != null && !req.getLineId().isEmpty()) {
            user.setLineId(req.getLineId());
            updated = true;
        }
        
        if (req.getRecipientName() != null && !req.getRecipientName().isEmpty()) {
            user.setRecipientName(req.getRecipientName());
            updated = true;
        }
        
        if (req.getRecipientPhone() != null && !req.getRecipientPhone().isEmpty()) {
            user.setRecipientPhone(req.getRecipientPhone());
            updated = true;
        }
        
        if (req.getCity() != null && !req.getCity().isEmpty()) {
            user.setCity(req.getCity());
            updated = true;
        }
        
        if (req.getDistrict() != null && !req.getDistrict().isEmpty()) {
            user.setDistrict(req.getDistrict());
            updated = true;
        }
        
        if (req.getAddressDetail() != null && !req.getAddressDetail().isEmpty()) {
            user.setAddressDetail(req.getAddressDetail());
            updated = true;
        }
        
        if (req.getInvoiceType() != null && !req.getInvoiceType().isEmpty()) {
            user.setInvoiceType(req.getInvoiceType());
            updated = true;
        }
        
        if (req.getInvoiceEmail() != null && !req.getInvoiceEmail().isEmpty()) {
            user.setInvoiceEmail(req.getInvoiceEmail());
            updated = true;
        }
        
        if (req.getCarrierCode() != null && !req.getCarrierCode().isEmpty()) {
            user.setCarrierCode(req.getCarrierCode());
            updated = true;
        }
        
        if (req.getTaxId() != null && !req.getTaxId().isEmpty()) {
            user.setTaxId(req.getTaxId());
            updated = true;
        }
        
        if (req.getCompanyName() != null && !req.getCompanyName().isEmpty()) {
            user.setCompanyName(req.getCompanyName());
            updated = true;
        }
        
        // 如果有更新,儲存到資料庫
        if (updated) {
            user.setUpdatedAt(java.time.LocalDateTime.now());
            userService.updateUser(user);
            log.info("✅ 更新成功: userId={}", userId);
        } else {
            log.info("ℹ️ 無需更新（沒有欄位變更）");
        }
        
        // 查詢錢包餘額
        // ✅ 金幣/紅利已直接存在 user 表，不再需要查詢 user_wallet
        
        // 組合回應
        UserRes res = UserRes.from(user);
        
        return ResponseEntity.ok(res);
    }

    /**
     * 上傳使用者頭像
     * 
     * <p>使用方式：</p>
     * <ol>
     *   <li>前端呼叫此 API 上傳圖片檔案</li>
     *   <li>後端上傳到 S3 並返回圖片 URL</li>
     *   <li>前端拿到 URL 後，再呼叫 PUT /user/me 更新 avatar 欄位</li>
     * </ol>
     * 
     * @param file 圖片檔案（限制 5MB，支援 jpg/png/gif/webp）
     * @return 圖片 URL
     */
    @PostMapping({"/avatar", "/me/avatar"})
    @Operation(summary = "上傳使用者頭像", description = "上傳頭像圖片到 S3，返回圖片 URL")
    public ResponseEntity<Map<String, String>> uploadAvatar(
            @RequestParam("file") 
            @Parameter(description = "圖片檔案（限制 5MB，支援 jpg/png/gif/webp）")
            MultipartFile file) {
        
        log.info("📤 [API] 上傳使用者頭像：檔案名稱={}", file.getOriginalFilename());
        
        // 檢查是否登入
        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            log.warn("⚠️ 未登入或 Token 無效");
            return ResponseEntity.status(401).build();
        }
        
        // 上傳到 S3（user 資料夾）
        String imageUrl = s3Service.uploadImage(file, "user");
        
        log.info("✅ 頭像上傳成功：userId={}, imageUrl={}", userId, imageUrl);
        
        // 返回圖片 URL
        Map<String, String> response = new HashMap<>();
        response.put("imageUrl", imageUrl);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 上傳頭像並更新到使用者資料（一步完成）
     * 
     * <p>此 API 會同時完成：</p>
     * <ol>
     *   <li>上傳圖片到 S3</li>
     *   <li>更新使用者的 avatar 欄位</li>
     *   <li>返回完整的使用者資訊</li>
     * </ol>
     * 
     * @param file 圖片檔案
     * @return 更新後的使用者資訊（含新頭像 URL）
     */
    @PostMapping({"/avatar/update", "/me/avatar/update"})
    @Operation(summary = "上傳並更新頭像（一步完成）", description = "上傳頭像到 S3 並自動更新使用者資料")
    public ResponseEntity<UserRes> uploadAndUpdateAvatar(
            @RequestParam("file") 
            @Parameter(description = "圖片檔案（限制 5MB，支援 jpg/png/gif/webp）")
            MultipartFile file) {
        
        log.info("📤 [API] 上傳並更新頭像：檔案名稱={}", file.getOriginalFilename());
        
        // 檢查是否登入
        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            log.warn("⚠️ 未登入或 Token 無效");
            return ResponseEntity.status(401).build();
        }
        
        // 查詢使用者
        User user = userService.findById(userId);
        if (user == null) {
            log.warn("⚠️ 使用者不存在: userId={}", userId);
            return ResponseEntity.status(404).build();
        }
        
        // 上傳到 S3
        String newImageUrl = s3Service.uploadImage(file, "user");
        
        // 刪除舊頭像（如果存在且是 S3 URL）
        String oldAvatar = user.getAvatar();
        if (oldAvatar != null && oldAvatar.contains("s3.amazonaws.com")) {
            try {
                s3Service.deleteImage(oldAvatar);
                log.info("🗑️ 已刪除舊頭像：{}", oldAvatar);
            } catch (Exception e) {
                log.warn("⚠️ 刪除舊頭像失敗（繼續執行）：{}", e.getMessage());
            }
        }
        
        // 更新使用者 avatar 欄位
        user.setAvatar(newImageUrl);
        user.setUpdatedAt(java.time.LocalDateTime.now());
        userService.updateUser(user);
        
        log.info("✅ 頭像更新成功：userId={}, imageUrl={}", userId, newImageUrl);
        
        // ✅ 金幣/紅利已直接存在 user 表，不再需要查詢 user_wallet
        
        // 組合回應
        UserRes res = UserRes.from(user);
        
        return ResponseEntity.ok(res);
    }

    /**
     * OAuth 新用戶補上推薦碼（一次性）
     * POST /api/user/apply-referral
     */
    @PostMapping("/apply-referral")
    @Operation(summary = "補上推薦碼", description = "OAuth 新用戶登入後補上推薦碼（一次性，已綁定則拋例外）")
    public ResponseEntity<Void> applyReferral(@Valid @RequestBody ApplyReferralReq req) {
        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        log.info("🎁 [API] 補上推薦碼: userId={}, code={}", userId, req.getCode());
        userService.applyReferral(userId, req.getCode());
        log.info("✅ [API] 推薦碼綁定成功: userId={}", userId);
        return ResponseEntity.ok().build();
    }

}
