package com.group.admin.controller.api;

import com.group.admin.req.prizebox.PrizeBoxRecycleReq;
import com.group.admin.req.prizebox.PrizeBoxShipReq;
import com.group.admin.res.PageResult;
import com.group.admin.res.prizebox.PrizeBoxItemRes;
import com.group.admin.res.prizebox.PrizeBoxSummaryRes;
import com.group.admin.res.prizebox.RecycleResultRes;
import com.group.admin.service.PrizeBoxService;
import com.group.admin.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 前台獎品盒 API
 *
 * @author Kuji Admin
 * @since 2026-01-09
 */
@Slf4j
@RestController
@RequestMapping("/prize-box")
@RequiredArgsConstructor
public class PrizeBoxController {

    private final PrizeBoxService prizeBoxService;

    /**
     * 查詢我的獎品盒（IN_BOX，無分組）
     */
    @GetMapping
    public ResponseEntity<List<PrizeBoxItemRes>> getMyPrizeBox() {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("🔍 [API] 查詢我的獎品盒：userId={}", userId);

        List<PrizeBoxItemRes> prizeBox = prizeBoxService.getPrizeBox(userId);

        return ResponseEntity.ok(prizeBox);
    }

    /**
     * 按店家分組查詢獎品盒（用於出貨選擇）
     */
    @GetMapping("/summary")
    public ResponseEntity<List<PrizeBoxSummaryRes>> getSummaryByStore() {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("🔍 [API] 查詢我的獎品盒（按店家分組）：userId={}", userId);

        List<PrizeBoxSummaryRes> summary = prizeBoxService.getSummaryByStore(userId);

        return ResponseEntity.ok(summary);
    }

    /**
     * 查詢獎品盒歷史（含已出貨與已回收，支援分頁）
     */
    @GetMapping("/history")
    public ResponseEntity<PageResult<PrizeBoxItemRes>> getHistory(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("🔍 [API] 查詢獎品盒歷史：userId={}, status={}, page={}, size={}", userId, status, page, size);

        PageResult<PrizeBoxItemRes> history = prizeBoxService.getPrizeBoxHistory(userId, status, page, size);

        return ResponseEntity.ok(history);
    }

    /**
     * 出貨（將選定的獎品產生訂單）
     */
    @PostMapping("/ship")
    public ResponseEntity<List<String>> shipPrizes(@Valid @RequestBody PrizeBoxShipReq req) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("🔍 [API] 出貨獎品：userId={}, prizeBoxIds={}", userId, req.getPrizeBoxIds());

        List<String> orderIds = prizeBoxService.shipPrizes(userId, req);

        return ResponseEntity.ok(orderIds);
    }

    /**
     * 回收獎品（轉換為紅利）
     */
    @PostMapping("/recycle")
    public ResponseEntity<RecycleResultRes> recyclePrizes(@Valid @RequestBody PrizeBoxRecycleReq req) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("🔍 [API] 回收獎品：userId={}, prizeBoxIds={}", userId, req.getPrizeBoxIds());

        RecycleResultRes result = prizeBoxService.recyclePrizes(userId, req);

        return ResponseEntity.ok(result);
    }
}
