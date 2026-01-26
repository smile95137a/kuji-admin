package com.group.admin.controller.api;

import com.group.admin.condition.WalletTransactionCondition;
import com.group.admin.req.common.QueryReq;
import com.group.admin.res.wallet.UserWalletRes;
import com.group.admin.res.wallet.WalletTransactionRes;
import com.group.admin.service.WalletService;
import com.group.admin.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 前台錢包 API
 * 
 * @author Kuji Admin
 * @since 2026-01-09
 */
@Slf4j
@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
public class WalletController {
    
    private final WalletService walletService;
    
    /**
     * 查詢我的錢包
     */
    @GetMapping
    public ResponseEntity<UserWalletRes> getMyWallet() {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("🔍 [API] 查詢我的錢包：userId={}", userId);
        
        UserWalletRes wallet = walletService.getWallet(userId);
        
        return ResponseEntity.ok(wallet);
    }
    
    /**
     * 查詢我的交易記錄
     */
    @PostMapping("/transactions")
    public ResponseEntity<List<WalletTransactionRes>> getMyTransactions(
            @RequestBody(required = false) QueryReq<WalletTransactionCondition> req) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("🔍 [API] 查詢我的交易記錄：userId={}", userId);
        
        // 強制設定為當前玩家
        if (req == null) {
            req = new QueryReq<>();
        }
        if (req.getCondition() == null) {
            req.setCondition(new WalletTransactionCondition());
        }
        req.getCondition().setUserId(userId);
        
        List<WalletTransactionRes> transactions = walletService.getTransactions(req);
        
        return ResponseEntity.ok(transactions);
    }
}
