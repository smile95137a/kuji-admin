package com.group.admin.controller.api;

import com.group.admin.res.wallet.RechargePlanRes;
import com.group.admin.service.RechargePlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 前台儲值方案 API
 * 
 * @author Kuji Admin
 * @since 2026-01-09
 */
@Slf4j
@RestController
@RequestMapping("/api/recharge-plan")
@RequiredArgsConstructor
public class RechargePlanController {
    
    private final RechargePlanService rechargePlanService;
    
    /**
     * 查詢有效的儲值方案（前台顯示）
     * 自動過濾：is_active=true、未刪除、活動期間內
     */
    @GetMapping("/list")
    public ResponseEntity<List<RechargePlanRes>> getActivePlans() {
        log.info("🔍 [API] 查詢有效儲值方案");
        List<RechargePlanRes> plans = rechargePlanService.getActivePlans();
        return ResponseEntity.ok(plans);
    }
    
    /**
     * 查詢儲值方案詳情
     */
    @GetMapping("/{id}")
    public ResponseEntity<RechargePlanRes> getPlanDetail(@PathVariable String id) {
        log.info("🔍 [API] 查詢儲值方案詳情：id={}", id);
        RechargePlanRes plan = rechargePlanService.getPlanDetail(id);
        return ResponseEntity.ok(plan);
    }
}
