package com.group.admin.controller.api;

import com.group.admin.condition.CoinTransactionCondition;
import com.group.admin.req.common.QueryReq;
import com.group.admin.res.PageResult;
import com.group.admin.res.wallet.CoinTransactionRes;
import com.group.admin.service.CoinService;
import com.group.admin.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台交易紀錄查詢 Controller
 */
@Slf4j
@RestController
@RequestMapping("/wallet/transactions")
@RequiredArgsConstructor
@Tag(name = "前台-交易紀錄", description = "玩家錢包交易流水查詢（需登入）")
public class WalletTransactionController {

    private final CoinService coinService;

    @PostMapping("/list")
    @Operation(summary = "查詢我的交易紀錄", description = "查詢當前用戶的錢包交易流水，支援分頁")
    public ResponseEntity<PageResult<CoinTransactionRes>> getMyTransactions(
            @RequestBody(required = false)
            @Parameter(description = "查詢條件（可選）")
            QueryReq<CoinTransactionCondition> req) {

        String userId = SecurityUtils.getCurrentUserId();
        log.info("🔍 [前台] 查詢交易紀錄：userId={}", userId);

        if (req == null) {
            req = new QueryReq<>();
        }
        if (req.getCondition() == null) {
            req.setCondition(new CoinTransactionCondition());
        }
        req.getCondition().setUserId(userId);

        PageResult<CoinTransactionRes> results = coinService.getTransactions(req);
        return ResponseEntity.ok(results);
    }
}
