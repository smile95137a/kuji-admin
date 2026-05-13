package com.group.admin.controller.admin;

import com.group.admin.entity.AdminUser;
import com.group.admin.req.cooperation.CooperationInquiryConvertVendorReq;
import com.group.admin.req.cooperation.CooperationInquiryFilterCondition;
import com.group.admin.req.cooperation.UpdateCooperationInquiryStatusReq;
import com.group.admin.res.cooperation.CooperationInquiryRes;
import com.group.admin.service.CooperationInquiryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 後台合作洽談管理 Controller
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/cooperation-inquiries")
@Tag(name = "合作洽談管理", description = "Admin 管理合作洽談表單 API")
public class AdminCooperationInquiryController {

    private final CooperationInquiryService cooperationInquiryService;

    /**
     * 查詢合作洽談列表
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> listInquiries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        log.info(
                "查詢合作洽談列表: page={}, size={}, status={}, type={}, keyword={}, sortBy={}, sortDir={}",
                page,
                size,
                status,
                type,
                keyword,
                sortBy,
                sortDir
        );

        CooperationInquiryFilterCondition filters = new CooperationInquiryFilterCondition();
        filters.setStatus(status);
        filters.setType(type);
        filters.setKeyword(keyword);
        filters.setSortBy(sortBy);
        filters.setSortDir(sortDir);

        return ResponseEntity.ok(
                cooperationInquiryService.listInquiries(filters, page, size)
        );
    }

    /**
     * 查詢合作洽談明細
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CooperationInquiryRes> getInquiry(
            @PathVariable String id
    ) {
        log.info("查詢合作洽談明細: id={}", id);

        return ResponseEntity.ok(
                cooperationInquiryService.getInquiry(id)
        );
    }

    /**
     * 更新合作洽談處理狀態
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CooperationInquiryRes> updateStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateCooperationInquiryStatusReq req
    ) {
        log.info("更新合作洽談狀態: id={}, status={}", id, req.getStatus());

        return ResponseEntity.ok(
                cooperationInquiryService.updateStatus(id, req)
        );
    }

    /**
     * 合作洽談轉成廠商帳號
     */
    @PostMapping("/{id}/convert-vendor")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminUser> convertToVendor(
            @PathVariable String id,
            @RequestBody(required = false) CooperationInquiryConvertVendorReq req
    ) {
        log.info("合作洽談轉成廠商帳號: id={}", id);

        CooperationInquiryConvertVendorReq safeReq =
                req == null ? new CooperationInquiryConvertVendorReq() : req;

        safeReq.setId(id);

        return ResponseEntity.ok(
                cooperationInquiryService.convertToVendor(safeReq)
        );
    }

    /**
     * 注記刪除合作洽談
     *
     * 注意：這裡不是實體刪除。
     * 實際邏輯由 Service 將 deleted 設為 true，並將狀態改成 CLOSED。
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteInquiry(
            @PathVariable String id
    ) {
        log.info("注記刪除合作洽談: id={}", id);

        cooperationInquiryService.deleteInquiry(id);

        return ResponseEntity.noContent().build();
    }
}