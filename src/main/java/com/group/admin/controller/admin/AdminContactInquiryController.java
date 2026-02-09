package com.group.admin.controller.admin;

import com.group.admin.req.common.QueryReq;
import com.group.admin.req.contact.ContactInquiryCondition;
import com.group.admin.res.contact.ContactInquiryRes;
import com.group.admin.service.ContactInquiryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 後台合作諮詢管理 Controller
 * 
 * <p>提供合作諮詢的查詢、狀態管理與刪除功能（僅 Admin 可用）</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/admin/contact-inquiries")
@RequiredArgsConstructor
@Tag(name = "後台-合作諮詢管理", description = "合作諮詢查詢與狀態管理（僅 Admin）")
@PreAuthorize("hasRole('ADMIN')")
public class AdminContactInquiryController {

    private final ContactInquiryService contactInquiryService;

    /**
     * 查詢合作諮詢列表
     * 
     * <p>支援動態條件查詢（公司名稱、狀態、合作類型、時間範圍、關鍵字）</p>
     */
    @PostMapping("/list")
    @Operation(summary = "查詢合作諮詢列表", description = "支援動態條件查詢，所有條件皆可選")
    public ResponseEntity<List<ContactInquiryRes>> queryInquiries(
            @RequestBody(required = false)
            @Parameter(description = "查詢條件（可選）")
            QueryReq<ContactInquiryCondition> req) {
        
        log.info("📋 [後台] 查詢合作諮詢列表");
        List<ContactInquiryRes> results = contactInquiryService.queryInquiries(req);
        return ResponseEntity.ok(results);
    }

    /**
     * 查詢單一合作諮詢詳情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查詢合作諮詢詳情", description = "根據 ID 查詢單一合作諮詢")
    public ResponseEntity<ContactInquiryRes> getInquiryById(
            @PathVariable
            @Parameter(description = "合作諮詢 ID", example = "uuid-inquiry-123")
            String id) {
        
        log.info("🔍 [後台] 查詢合作諮詢詳情: id={}", id);
        ContactInquiryRes result = contactInquiryService.getInquiryById(id);
        return ResponseEntity.ok(result);
    }

    /**
     * 更新合作諮詢狀態
     * 
     * <p>可更新為：PROCESSING（處理中）、COMPLETED（已完成）、REJECTED（已拒絕）</p>
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "更新合作諮詢狀態", description = "更新狀態：PROCESSING / COMPLETED / REJECTED")
    public ResponseEntity<ContactInquiryRes> updateStatus(
            @PathVariable
            @Parameter(description = "合作諮詢 ID")
            String id,
            @RequestBody Map<String, String> body) {
        
        String status = body.get("status");
        String remark = body.get("remark");
        
        log.info("✏️ [後台] 更新合作諮詢狀態: id={}, status={}", id, status);
        ContactInquiryRes result = contactInquiryService.updateInquiryStatus(id, status, remark);
        return ResponseEntity.ok(result);
    }

    /**
     * 刪除合作諮詢
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "刪除合作諮詢", description = "永久刪除合作諮詢")
    public ResponseEntity<Void> deleteInquiry(
            @PathVariable
            @Parameter(description = "合作諮詢 ID")
            String id) {
        
        log.info("🗑️ [後台] 刪除合作諮詢: id={}", id);
        contactInquiryService.deleteInquiry(id);
        return ResponseEntity.ok().build();
    }
}
