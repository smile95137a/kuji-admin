package com.group.admin.controller.admin;

import com.group.admin.req.common.QueryReq;
import com.group.admin.req.recharge.RechargePlanCondition;
import com.group.admin.req.recharge.RechargePlanCreateReq;
import com.group.admin.req.recharge.RechargePlanUpdateReq;
import com.group.admin.res.wallet.RechargePlanRes;
import com.group.admin.service.RechargePlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 後台儲值方案管理 Controller
 * 
 * @author Kuji Admin
 * @since 2026-01-09
 */
@Slf4j
@RestController
@RequestMapping("/admin/recharge-plan")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminRechargePlanController {
    
    private final RechargePlanService rechargePlanService;
    
    /**
     * 新增儲值方案
     */
    @PostMapping
    public ResponseEntity<String> createPlan(@Valid @RequestBody RechargePlanCreateReq req) {
        log.info("🔍 [Admin] 新增儲值方案：name={}, amount={}", req.getName(), req.getAmount());
        String planId = rechargePlanService.createPlan(req);
        return ResponseEntity.ok(planId);
    }
    
    /**
     * 更新儲值方案
     */
    @PutMapping("/{id}")
    public ResponseEntity<Void> updatePlan(
            @PathVariable String id,
            @Valid @RequestBody RechargePlanUpdateReq req) {
        log.info("🔍 [Admin] 更新儲值方案：id={}", id);
        rechargePlanService.updatePlan(id, req);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 刪除儲值方案（軟刪除）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlan(@PathVariable String id) {
        log.info("🔍 [Admin] 刪除儲值方案：id={}", id);
        rechargePlanService.deletePlan(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 查詢所有儲值方案（後台管理）
     */
    @GetMapping("/list")
    public ResponseEntity<List<RechargePlanRes>> getAllPlans() {
        log.info("🔍 [Admin] 查詢所有儲值方案");
        List<RechargePlanRes> plans = rechargePlanService.getAllPlans();
        return ResponseEntity.ok(plans);
    }
    
    /**
     * 查詢儲值方案（支援條件查詢）
     * 
     * 可選條件：name（名稱模糊）、isActive、amountMin、amountMax
     */
    @PostMapping("/query")
    public ResponseEntity<List<RechargePlanRes>> queryPlans(
            @RequestBody(required = false) QueryReq<RechargePlanCondition> req) {
        log.info("🔍 [Admin] 查詢儲值方案（條件查詢）: {}", req);
        List<RechargePlanRes> plans = rechargePlanService.queryPlans(req);
        return ResponseEntity.ok(plans);
    }
    
    /**
     * 查詢儲值方案詳情
     */
    @GetMapping("/{id}")
    public ResponseEntity<RechargePlanRes> getPlanDetail(@PathVariable String id) {
        log.info("🔍 [Admin] 查詢儲值方案詳情：id={}", id);
        RechargePlanRes plan = rechargePlanService.getPlanDetail(id);
        return ResponseEntity.ok(plan);
    }
}
