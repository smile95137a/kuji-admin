package com.group.admin.controller.admin;

import com.group.admin.dto.res.report.*;
import com.group.admin.service.ReportService;
import com.group.admin.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 報表管理 API（後台）
 */
@Slf4j
@RestController
@RequestMapping("/admin/report")
@RequiredArgsConstructor
public class AdminReportController {
    
    private final ReportService reportService;
    
    /**
     * 營業額報表
     */
    @GetMapping("/revenue")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER')")
    public ResponseEntity<RevenueReportRes> getRevenueReport(
            @RequestParam(required = false) String storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        // 非 Admin 只能查自己店家的報表
        String currentStoreId = SecurityUtils.getCurrentUserPrimaryStoreId();
        if (currentStoreId != null && storeId == null) {
            storeId = currentStoreId;
        }
        
        return ResponseEntity.ok(reportService.getRevenueReport(storeId, startDate, endDate));
    }
    
    /**
     * 推薦碼報表
     */
    @GetMapping("/referral")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER')")
    public ResponseEntity<ReferralReportRes> getReferralReport(
            @RequestParam(required = false) String storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        String currentStoreId = SecurityUtils.getCurrentUserPrimaryStoreId();
        if (currentStoreId != null && storeId == null) {
            storeId = currentStoreId;
        }
        
        return ResponseEntity.ok(reportService.getReferralReport(storeId, startDate, endDate));
    }
    
    /**
     * 開獎結果報表
     */
    @GetMapping("/lottery-result")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER')")
    public ResponseEntity<LotteryResultReportRes> getLotteryResultReport(
            @RequestParam(required = false) String storeId,
            @RequestParam(required = false) String lotteryId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        String currentStoreId = SecurityUtils.getCurrentUserPrimaryStoreId();
        if (currentStoreId != null && storeId == null) {
            storeId = currentStoreId;
        }
        
        return ResponseEntity.ok(reportService.getLotteryResultReport(storeId, lotteryId, startDate, endDate));
    }
    
    /**
     * 儲值報表
     */
    @GetMapping("/recharge")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER')")
    public ResponseEntity<RechargeReportRes> getRechargeReport(
            @RequestParam(required = false) String storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        String currentStoreId = SecurityUtils.getCurrentUserPrimaryStoreId();
        if (currentStoreId != null && storeId == null) {
            storeId = currentStoreId;
        }
        
        return ResponseEntity.ok(reportService.getRechargeReport(storeId, startDate, endDate));
    }
    
    /**
     * 贈送點數報表
     */
    @GetMapping("/bonus")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER')")
    public ResponseEntity<BonusReportRes> getBonusReport(
            @RequestParam(required = false) String storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        String currentStoreId = SecurityUtils.getCurrentUserPrimaryStoreId();
        if (currentStoreId != null && storeId == null) {
            storeId = currentStoreId;
        }
        
        return ResponseEntity.ok(reportService.getBonusReport(storeId, startDate, endDate));
    }
}
