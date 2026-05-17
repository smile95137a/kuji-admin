package com.group.admin.controller.api;

import com.group.admin.constants.ErrorCodes;
import com.group.admin.exception.BusinessException;
import com.group.admin.req.prizebox.PrizeBoxRecycleReq;
import com.group.admin.req.prizebox.PrizeBoxShipReq;
import com.group.admin.res.PageResult;
import com.group.admin.res.order.OrderPaymentInitRes;
import com.group.admin.res.prizebox.PrizeBoxItemRes;
import com.group.admin.res.prizebox.PrizeBoxSummaryRes;
import com.group.admin.res.prizebox.RecycleResultRes;
import com.group.admin.service.PrizeBoxService;
import com.group.admin.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/prize-box")
@RequiredArgsConstructor
public class PrizeBoxController {

    private final PrizeBoxService prizeBoxService;

    @GetMapping
    public ResponseEntity<PageResult<PrizeBoxItemRes>> getMyPrizeBox(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        String userId = requireCurrentUserId();
        log.info("[API] 查詢我的賞品盒 userId={}", userId);
        return ResponseEntity.ok(prizeBoxService.getPrizeBoxPage(userId, page, size));
    }

    @GetMapping("/summary")
    public ResponseEntity<List<PrizeBoxSummaryRes>> getSummaryByStore() {
        String userId = requireCurrentUserId();
        log.info("[API] 查詢賞品盒店家摘要 userId={}", userId);
        return ResponseEntity.ok(prizeBoxService.getSummaryByStore(userId));
    }

    @GetMapping("/history")
    public ResponseEntity<PageResult<PrizeBoxItemRes>> getHistory(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        String userId = requireCurrentUserId();
        log.info("[API] 查詢賞品盒歷史 userId={}, status={}, page={}, size={}", userId, status, page, size);
        return ResponseEntity.ok(prizeBoxService.getPrizeBoxHistory(userId, status, page, size));
    }

    @PostMapping("/ship")
    public ResponseEntity<List<OrderPaymentInitRes>> shipPrizes(@Valid @RequestBody PrizeBoxShipReq req) {
        String userId = requireCurrentUserId();
        log.info("[API] 賞品盒出貨 userId={}, prizeBoxIds={}", userId, req.getPrizeBoxIds());
        return ResponseEntity.ok(prizeBoxService.shipPrizes(userId, req));
    }

    @PostMapping("/recycle")
    public ResponseEntity<RecycleResultRes> recyclePrizes(@Valid @RequestBody PrizeBoxRecycleReq req) {
        String userId = requireCurrentUserId();
        log.info("[API] 賞品盒回收 userId={}, prizeBoxIds={}", userId, req.getPrizeBoxIds());
        return ResponseEntity.ok(prizeBoxService.recyclePrizes(userId, req));
    }

    private String requireCurrentUserId() {
        String userId = SecurityUtils.getCurrentApiUserId();
        if (userId == null || userId.isBlank()) {
            throw new BusinessException(ErrorCodes.AUTH_TOKEN_INVALID, "登入資訊已失效，請重新登入");
        }
        return userId;
    }
}
