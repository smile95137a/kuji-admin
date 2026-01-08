package com.group.admin.controller.admin;

import com.group.admin.condition.WalletTransactionCondition;
import com.group.admin.req.common.QueryReq;
import com.group.admin.req.wallet.WalletAdjustReq;
import com.group.admin.res.wallet.UserWalletRes;
import com.group.admin.res.wallet.WalletTransactionRes;
import com.group.admin.service.WalletService;
import com.group.admin.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 後台錢包管理 Controller
 * 
 * @author Kuji Admin
 * @since 2026-01-09
 */
@Slf4j
@RestController
@RequestMapping("/admin/wallet")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminWalletController {
    
    private final WalletService walletService;
    
    /**
     * 查詢玩家錢包
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserWalletRes> getWallet(@PathVariable String userId) {
        log.info("🔍 [Admin] 查詢玩家錢包：userId={}", userId);
        UserWalletRes wallet = walletService.getWallet(userId);
        return ResponseEntity.ok(wallet);
    }
    
    /**
     * 手動調整玩家點數（Admin 專用）
     */
    @PostMapping("/adjust")
    public ResponseEntity<Void> adjustCoins(@Valid @RequestBody WalletAdjustReq req) {
        String operatorId = SecurityUtils.getCurrentAdminUserId();
        log.info("🔍 [Admin] 手動調整點數：userId={}, coinType={}, amount={}, operator={}", 
                req.getUserId(), req.getCoinType(), req.getAmount(), operatorId);
        
        walletService.adjustCoins(req, operatorId);
        
        return ResponseEntity.ok().build();
    }
    
    /**
     * 查詢交易記錄
     */
    @PostMapping("/transactions/list")
    public ResponseEntity<List<WalletTransactionRes>> getTransactions(
            @RequestBody(required = false) QueryReq<WalletTransactionCondition> req) {
        log.info("🔍 [Admin] 查詢交易記錄");
        
        List<WalletTransactionRes> transactions = walletService.getTransactions(req);
        
        return ResponseEntity.ok(transactions);
    }
}
