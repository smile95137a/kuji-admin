package com.group.admin.controller.admin;

import com.group.admin.req.referral.ReferralCodeCreateReq;
import com.group.admin.req.referral.ReferralCodeUpdateReq;
import com.group.admin.res.referral.ReferralCodeRes;
import com.group.admin.res.referral.ReferralRecordRes;
import com.group.admin.service.ReferralCodeService;
import com.group.admin.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 後台推薦碼管理 Controller
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/admin/referral-codes")
@RequiredArgsConstructor
@Tag(name = "後台 - 推薦碼管理", description = "推薦碼 CRUD 及統計 API")
public class AdminReferralCodeController {
    
    private final ReferralCodeService referralCodeService;
    
    /**
     * 建立推薦碼
     */
    @PostMapping
    @Operation(summary = "建立推薦碼", description = "為店家建立新的推薦碼")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER')")
    public ResponseEntity<ReferralCodeRes> create(@Valid @RequestBody ReferralCodeCreateReq req) {
        log.info("🎫 建立推薦碼請求: code={}", req.getCode());
        
        // 店家負責人自動帶入 storeId
        String storeId = SecurityUtils.getCurrentUserPrimaryStoreId();
        if (storeId != null && (req.getStoreId() == null || req.getStoreId().isEmpty())) {
            req.setStoreId(storeId);
        }
        
        ReferralCodeRes res = referralCodeService.create(req);
        return ResponseEntity.ok(res);
    }
    
    /**
     * 更新推薦碼
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新推薦碼", description = "更新推薦碼資訊")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER')")
    public ResponseEntity<ReferralCodeRes> update(
            @PathVariable String id,
            @Valid @RequestBody ReferralCodeUpdateReq req) {
        log.info("📝 更新推薦碼請求: id={}", id);
        ReferralCodeRes res = referralCodeService.update(id, req);
        return ResponseEntity.ok(res);
    }
    
    /**
     * 刪除推薦碼
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "刪除推薦碼", description = "刪除指定推薦碼")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        log.info("🗑️ 刪除推薦碼請求: id={}", id);
        referralCodeService.delete(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 取得推薦碼詳情
     */
    @GetMapping("/{id}")
    @Operation(summary = "取得推薦碼詳情", description = "取得指定推薦碼資訊")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")
    public ResponseEntity<ReferralCodeRes> getById(@PathVariable String id) {
        log.info("🔍 查詢推薦碼詳情: id={}", id);
        ReferralCodeRes res = referralCodeService.getById(id);
        return ResponseEntity.ok(res);
    }
    
    /**
     * 取得所有推薦碼（管理員）
     */
    @GetMapping
    @Operation(summary = "取得所有推薦碼", description = "取得系統所有推薦碼（管理員使用）")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReferralCodeRes>> getAll() {
        log.info("📋 查詢所有推薦碼");
        List<ReferralCodeRes> res = referralCodeService.getAll();
        return ResponseEntity.ok(res);
    }
    
    /**
     * 取得店家的推薦碼
     */
    @GetMapping("/store/{storeId}")
    @Operation(summary = "取得店家推薦碼", description = "取得指定店家的所有推薦碼")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")
    public ResponseEntity<List<ReferralCodeRes>> getByStoreId(@PathVariable String storeId) {
        log.info("📋 查詢店家推薦碼: storeId={}", storeId);
        List<ReferralCodeRes> res = referralCodeService.getByStoreId(storeId);
        return ResponseEntity.ok(res);
    }
    
    /**
     * 取得當前店家的推薦碼
     */
    @GetMapping("/my-store")
    @Operation(summary = "取得我的店家推薦碼", description = "取得當前店家負責人的所有推薦碼")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'STORE_EDITOR')")
    public ResponseEntity<List<ReferralCodeRes>> getMyStoreCodes() {
        String storeId = SecurityUtils.getCurrentUserPrimaryStoreId();
        log.info("📋 查詢我的店家推薦碼: storeId={}", storeId);
        
        if (storeId == null) {
            return ResponseEntity.ok(List.of());
        }
        
        List<ReferralCodeRes> res = referralCodeService.getByStoreId(storeId);
        return ResponseEntity.ok(res);
    }
    
    /**
     * 取得推薦碼的使用記錄
     */
    @GetMapping("/{id}/records")
    @Operation(summary = "取得推薦碼使用記錄", description = "取得指定推薦碼的所有使用記錄")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")
    public ResponseEntity<List<ReferralRecordRes>> getRecords(@PathVariable String id) {
        log.info("📊 查詢推薦碼使用記錄: codeId={}", id);
        List<ReferralRecordRes> res = referralCodeService.getRecordsByCodeId(id);
        return ResponseEntity.ok(res);
    }
    
    /**
     * 取得店家的所有推薦記錄
     */
    @GetMapping("/store/{storeId}/records")
    @Operation(summary = "取得店家推薦記錄", description = "取得指定店家的所有推薦記錄")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")
    public ResponseEntity<List<ReferralRecordRes>> getStoreRecords(@PathVariable String storeId) {
        log.info("📊 查詢店家推薦記錄: storeId={}", storeId);
        List<ReferralRecordRes> res = referralCodeService.getRecordsByStoreId(storeId);
        return ResponseEntity.ok(res);
    }
    
    /**
     * 驗證推薦碼是否有效
     */
    @GetMapping("/validate/{code}")
    @Operation(summary = "驗證推薦碼", description = "驗證推薦碼是否有效")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")
    public ResponseEntity<Boolean> validateCode(@PathVariable String code) {
        log.info("✅ 驗證推薦碼: code={}", code);
        boolean valid = referralCodeService.validateCode(code);
        return ResponseEntity.ok(valid);
    }
}
