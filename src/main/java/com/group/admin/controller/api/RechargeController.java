package com.group.admin.controller.api;

import com.group.admin.req.recharge.RechargeReq;
import com.group.admin.res.recharge.RechargeRes;
import com.group.admin.service.RechargeService;
import com.group.admin.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 前台使用者儲值 API
 * 
 * @author Kuji Admin
 * @since 2026-02-08
 */
@Slf4j
@RestController
@RequestMapping("/recharge")
@RequiredArgsConstructor
public class RechargeController {
    
    private final RechargeService rechargeService;
    
    /**
     * 建立儲值請求（✨ 測試模式：直接完成支付）
     * 
     * 流程：
     * 1. 驗證儲值方案存在且有效
     * 2. 建立 RechargeRecord（狀態 = COMPLETED，直接完成）
     * 3. 立即更新使用者金幣/紅利/累計儲值
     * 4. 建立 WalletTransaction 審計記錄
     * 5. 返回儲值記錄信息
     * 
     * ⚠️ 此為測試模式，直接完成儲值不需要金流
     * 未來串接金流後，需改為 PENDING 狀態並調用 confirm API
     */
    @PostMapping
    public ResponseEntity<RechargeRes> createRechargeRequest(
            @Valid @RequestBody RechargeReq req) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("💳 [API] 建立儲值請求（直接完成）：userId={}, planId={}", userId, req.getPlanId());
        
        RechargeRes res = rechargeService.createRechargeRequest(userId, req);
        return ResponseEntity.ok(res);
    }
    
    /**
     * 查詢我的儲值記錄
     */
    @GetMapping("/history")
    public ResponseEntity<List<RechargeRes>> getMyRechargeHistory(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("🔍 [API] 查詢儲值記錄：userId={}, page={}, size={}", userId, page, size);
        
        List<RechargeRes> history = rechargeService.getUserRechargeHistory(userId, page, size);
        return ResponseEntity.ok(history);
    }
    
    /**
     * 確認支付（支付網關回調）
     * 
     * ⚠️ 在實際環境中，應由支付網關（如綠界、歐付寶等）的回調端點調用
     * 此端點可用於：
     * 1. 測試環境模擬支付成功
     * 2. 前端確認支付後調用此 API 同步狀態
     */
    @PostMapping("/{rechargeId}/confirm")
    public ResponseEntity<RechargeRes> confirmPayment(
            @PathVariable String rechargeId,
            @RequestParam(required = false) String transactionId) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("💰 [API] 確認支付：userId={}, rechargeId={}", userId, rechargeId);
        
        RechargeRes res = rechargeService.confirmPayment(rechargeId, transactionId);
        return ResponseEntity.ok(res);
    }
    
    /**
     * 記錄支付失敗
     * 
     * ⚠️ 用於支付失敗或使用者主動取消的情況
     */
    @PostMapping("/{rechargeId}/failure")
    public ResponseEntity<RechargeRes> recordPaymentFailure(
            @PathVariable String rechargeId,
            @RequestParam(required = false) String failReason) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("❌ [API] 記錄支付失敗：userId={}, rechargeId={}, reason={}", 
                userId, rechargeId, failReason);
        
        RechargeRes res = rechargeService.recordPaymentFailure(rechargeId, failReason != null ? failReason : "使用者取消");
        return ResponseEntity.ok(res);
    }
}
