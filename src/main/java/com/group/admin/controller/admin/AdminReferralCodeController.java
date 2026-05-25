package com.group.admin.controller.admin;

import com.group.admin.condition.report.ReferralReportCondition;
import com.group.admin.req.referral.ReferralCodeCreateReq;
import com.group.admin.req.referral.ReferralCodeUpdateReq;
import com.group.admin.res.referral.AdminReferralStatsRes;
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
 */
@Slf4j
@RestController
@RequestMapping("/admin/referral-codes")
@RequiredArgsConstructor
@Tag(name = "後台 - 推薦碼管理", description = "推薦碼 CRUD 及統計 API")
public class AdminReferralCodeController {

    private final ReferralCodeService referralCodeService;

    @PostMapping
    @Operation(summary = "建立推薦碼", description = "為店家建立新的推薦碼（代碼自動生成）")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReferralCodeRes> create(@Valid @RequestBody ReferralCodeCreateReq req) {
        log.info("🎫 建立推薦碼請求: storeId={}", req.getStoreId());
        String storeId = SecurityUtils.getCurrentUserPrimaryStoreId();
        if (storeId != null && (req.getStoreId() == null || req.getStoreId().isEmpty())) {
            req.setStoreId(storeId);
        }
        ReferralCodeRes res = referralCodeService.create(req);
        return ResponseEntity.ok(res);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新推薦碼")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReferralCodeRes> update(
            @PathVariable String id,
            @Valid @RequestBody ReferralCodeUpdateReq req) {
        log.info("📝 更新推薦碼請求: id={}", id);
        ReferralCodeRes res = referralCodeService.update(id, req);
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "刪除推薦碼")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        log.info("🗑️ 刪除推薦碼請求: id={}", id);
        referralCodeService.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "取得推薦碼詳情")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")
    public ResponseEntity<ReferralCodeRes> getById(@PathVariable String id) {
        log.info("🔍 查詢推薦碼詳情: id={}", id);
        ReferralCodeRes res = referralCodeService.getById(id);
        return ResponseEntity.ok(res);
    }

    /** T006: GET with optional storeId + isActive filters */
    @GetMapping
    @Operation(summary = "取得所有推薦碼", description = "管理員取得所有推薦碼，支援 storeId / isActive 篩選")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReferralCodeRes>> getAll(
            @RequestParam(required = false) String storeId,
            @RequestParam(required = false) Boolean isActive) {
        log.info("📋 查詢所有推薦碼: storeId={}, isActive={}", storeId, isActive);
        List<ReferralCodeRes> res = referralCodeService.getAll(storeId, isActive);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/store/{storeId}")
    @Operation(summary = "取得店家推薦碼")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")
    public ResponseEntity<List<ReferralCodeRes>> getByStoreId(@PathVariable String storeId) {
        log.info("📋 查詢店家推薦碼: storeId={}", storeId);
        List<ReferralCodeRes> res = referralCodeService.getByStoreId(storeId);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/my-store")
    @Operation(summary = "取得我的店家推薦碼")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")
    public ResponseEntity<List<ReferralCodeRes>> getMyStoreCodes() {
        String storeId = SecurityUtils.getCurrentUserPrimaryStoreId();
        log.info("📋 查詢我的店家推薦碼: storeId={}", storeId);
        if (storeId == null) return ResponseEntity.ok(List.of());
        List<ReferralCodeRes> res = referralCodeService.getByStoreId(storeId);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/{id}/records")
    @Operation(summary = "取得推薦碼使用記錄")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")
    public ResponseEntity<List<ReferralRecordRes>> getRecords(@PathVariable String id) {
        log.info("📊 查詢推薦碼使用記錄: codeId={}", id);
        List<ReferralRecordRes> res = referralCodeService.getRecordsByCodeId(id);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/store/{storeId}/records")
    @Operation(summary = "取得店家推薦記錄")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")
    public ResponseEntity<List<ReferralRecordRes>> getStoreRecords(@PathVariable String storeId) {
        log.info("📊 查詢店家推薦記錄: storeId={}", storeId);
        List<ReferralRecordRes> res = referralCodeService.getRecordsByStoreId(storeId);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/validate/{code}")
    @Operation(summary = "驗證推薦碼")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")
    public ResponseEntity<Boolean> validateCode(@PathVariable String code) {
        log.info("✅ 驗證推薦碼: code={}", code);
        boolean valid = referralCodeService.validateCode(code);
        return ResponseEntity.ok(valid);
    }

    /** T009: Disable a referral code (admin only) */
    @PutMapping("/{id}/disable")
    @Operation(summary = "停用推薦碼", description = "管理員停用指定推薦碼（不可復原）")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReferralCodeRes> disableCode(@PathVariable String id) {
        log.info("🚫 停用推薦碼請求: id={}", id);
        ReferralCodeRes result = referralCodeService.disableCode(id);
        return ResponseEntity.ok(result);
    }

    /** T020: Admin referral stats */
    @GetMapping("/stats")
    @Operation(summary = "推薦統計", description = "管理員查看各店家推薦統計（含每日時間軸）")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminReferralStatsRes>> getReferralStats(
            ReferralReportCondition condition) {
        log.info("📈 查詢推薦統計");
        List<AdminReferralStatsRes> result = referralCodeService.getReferralStats(condition);
        return ResponseEntity.ok(result);
    }
}
