package com.group.admin.controller.admin;

import com.group.admin.condition.CoinTransactionCondition;
import com.group.admin.req.common.QueryReq;
import com.group.admin.req.wallet.CoinAdjustReq;
import com.group.admin.res.PageResult;
import com.group.admin.res.wallet.UserCoinRes;
import com.group.admin.res.wallet.CoinTransactionRes;
import com.group.admin.service.CoinService;
import com.group.admin.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 後台金幣管理 Controller
 * 
 * @author Kuji Admin
 * @since 2026-01-09
 */
@Slf4j
@RestController
@RequestMapping("/admin/coin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCoinController {
    
    private final CoinService coinService;
    
    /**
     * 查詢玩家金幣
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserCoinRes> getWallet(@PathVariable String userId) {
        log.info("🔍 [Admin] 查詢玩家金幣：userId={}", userId);
        UserCoinRes coins = coinService.getWallet(userId);
        return ResponseEntity.ok(coins);
    }
    
    /**
     * 手動調整玩家點數（Admin 專用）
     */
    @PostMapping("/adjust")
    public ResponseEntity<Void> adjustCoins(@Valid @RequestBody CoinAdjustReq req) {
        String operatorId = SecurityUtils.getCurrentAdminUserId();
        log.info("🔍 [Admin] 手動調整點數：userId={}, coinType={}, amount={}, operator={}", 
                req.getUserId(), req.getCoinType(), req.getAmount(), operatorId);
        
        coinService.adjustCoins(req, operatorId);
        
        return ResponseEntity.ok().build();
    }
    
    /**
     * 查詢交易記錄
     */
    @PostMapping("/transactions/list")
    public ResponseEntity<PageResult<CoinTransactionRes>> getTransactions(
            @RequestBody(required = false) QueryReq<CoinTransactionCondition> req) {
        log.info("🔍 [Admin] 查詢交易記錄");
        
        PageResult<CoinTransactionRes> transactions = coinService.getTransactions(req);
        
        return ResponseEntity.ok(transactions);
    }
}
