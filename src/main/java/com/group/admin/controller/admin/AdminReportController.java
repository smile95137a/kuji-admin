package com.group.admin.controller.admin;

import com.group.admin.annotation.AuditLog;
import com.group.admin.condition.report.*;
import com.group.admin.dto.res.report.*;
import com.group.admin.enums.AuditLogType;
import com.group.admin.req.common.QueryReq;
import com.group.admin.service.ReportService;
import com.group.admin.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    @PostMapping("/revenue")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER')")
    public ResponseEntity<RevenueReportRes> getRevenueReport(
            @RequestBody(required = false) QueryReq<RevenueReportCondition> req) {
        
        // 非 Admin 只能查自己店家的報表
        String currentStoreId = SecurityUtils.getCurrentUserPrimaryStoreId();
        if (!SecurityUtils.isAdmin() && currentStoreId != null) {
           if (req == null) req = new QueryReq<>();
            if (req.getCondition() == null) req.setCondition(new RevenueReportCondition());
            req.getCondition().setStoreId(currentStoreId);
        }
        
        return ResponseEntity.ok(reportService.getRevenueReport(req));
    }
    
    /**
     * 推薦碼報表
     */
    @PostMapping("/referral")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReferralReportRes> getReferralReport(
            @RequestBody(required = false) QueryReq<ReferralReportCondition> req) {
        return ResponseEntity.ok(reportService.getReferralReport(req));
    }
    
    /**
     * 開獎結果報表
     */
    @PostMapping("/lottery-result")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER')")
    public ResponseEntity<LotteryResultReportRes> getLotteryResultReport(
            @RequestBody(required = false) QueryReq<LotteryResultReportCondition> req) {
        
        String currentStoreId = SecurityUtils.getCurrentUserPrimaryStoreId();
        if (!SecurityUtils.isAdmin() && currentStoreId != null) {
            if (req == null) req = new QueryReq<>();
            if (req.getCondition() == null) req.setCondition(new LotteryResultReportCondition());
            req.getCondition().setStoreId(currentStoreId);
        }
        
        return ResponseEntity.ok(reportService.getLotteryResultReport(req));
    }
    
    /**
     * 儲值報表
     */
    @PostMapping("/recharge")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RechargeReportRes> getRechargeReport(
            @RequestBody(required = false) QueryReq<RechargeReportCondition> req) {
        return ResponseEntity.ok(reportService.getRechargeReport(req));
    }
    
    /**
     * 贈送點數報表
     */
    @PostMapping("/bonus")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BonusReportRes> getBonusReport(
            @RequestBody(required = false) QueryReq<BonusReportCondition> req) {
        return ResponseEntity.ok(reportService.getBonusReport(req));
    }

    /**
     * 會員成長報表（Admin Only）
     */
    @io.swagger.v3.oas.annotations.Operation(summary = "會員成長報表", description = "Admin Only")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "查詢成功"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "非 ADMIN 角色"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未提供 JWT 或已過期")
    })
    @PostMapping("/member-growth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MemberGrowthReportRes> getMemberGrowthReport(
            @RequestBody(required = false) QueryReq<MemberGrowthReportCondition> req) {
        return ResponseEntity.ok(reportService.getMemberGrowthReport(req));
    }

    /**
     * 平台營收總覽報表（Admin Only）
     */
    @AuditLog(type = AuditLogType.ADMIN_ACTION, action = "VIEW", targetType = "PLATFORM_REVENUE_REPORT")
    @PostMapping("/platform-revenue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlatformRevenueReportRes> getPlatformRevenueReport(
            @RequestBody(required = false) QueryReq<PlatformRevenueReportCondition> req) {
        return ResponseEntity.ok(reportService.getPlatformRevenueReport(req));
    }

    /**
     * 商品銷售排行報表
     * StoreOwner 後端強制綁定 storeId；Admin 可帶 storeId 或查全平台
     */
    @PostMapping("/lottery-sales")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER')")
    public ResponseEntity<LotterySalesRankingRes> getLotterySalesRanking(
            @RequestBody(required = false) QueryReq<LotterySalesRankingCondition> req) {

        String currentStoreId = SecurityUtils.getCurrentUserPrimaryStoreId();
        if (!SecurityUtils.isAdmin() && currentStoreId != null) {
            if (req == null) req = new QueryReq<>();
            if (req.getCondition() == null) req.setCondition(new LotterySalesRankingCondition());
            req.getCondition().setStoreId(currentStoreId);
        }

        return ResponseEntity.ok(reportService.getLotterySalesRanking(req));
    }

    /**
     * 店家績效比較報表
     */
    @io.swagger.v3.oas.annotations.Operation(
            summary = "店家績效比較報表",
            description = "比較店家的抽獎消費、抽獎次數、活躍會員、出貨率、逾期率與平均出貨天數；Admin 可看全平台，StoreOwner 只能看自己的店家。")
    @PostMapping("/store-performance")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER')")
    public ResponseEntity<StorePerformanceReportRes> getStorePerformanceReport(
            @RequestBody(required = false) QueryReq<StorePerformanceCondition> req) {

        if (req == null) req = new QueryReq<>();
        if (req.getCondition() == null) req.setCondition(new StorePerformanceCondition());

        String currentStoreId = SecurityUtils.getCurrentUserPrimaryStoreId();
        if (!SecurityUtils.isAdmin() && currentStoreId != null) {
            String requested = req.getCondition().getStoreId();
            if (requested != null && !requested.equals(currentStoreId)) {
                return ResponseEntity.status(403).build();
            }
            req.getCondition().setStoreId(currentStoreId);
        }

        return ResponseEntity.ok(reportService.getStorePerformanceReport(req));
    }

    /**
     * 獎品出貨報表
     */
    @io.swagger.v3.oas.annotations.Operation(
            summary = "獎品出貨報表",
            description = "觀察訂單備貨、出貨與完成狀態，包含每日出貨趨勢、平均出貨天數與逾期未備貨數。")
    @PostMapping("/prize-shipment")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER')")
    public ResponseEntity<PrizeShipmentReportRes> getPrizeShipmentReport(
            @RequestBody(required = false) QueryReq<PrizeShipmentReportCondition> req) {

        // StoreOwner 只能查自己店家的報表（後端強制覆蓋 storeId）
        String currentStoreId = SecurityUtils.getCurrentUserPrimaryStoreId();
        if (!SecurityUtils.isAdmin() && currentStoreId != null) {
            if (req == null) req = new QueryReq<>();
            if (req.getCondition() == null) req.setCondition(new PrizeShipmentReportCondition());
            req.getCondition().setStoreId(currentStoreId);
        }

        return ResponseEntity.ok(reportService.getPrizeShipmentReport(req));
    }
}
