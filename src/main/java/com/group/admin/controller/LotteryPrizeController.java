package com.group.admin.controller;

import com.group.admin.req.lottery.LotteryPrizeCreateReq;
import com.group.admin.req.lottery.LotteryPrizeUpdateReq;
import com.group.admin.res.lottery.LotteryPrizeRes;
import com.group.admin.service.LotteryPrizeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 獎項管理 Controller
 *
 * <p>提供獎項的 CRUD 操作</p>
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Tag(name = "獎項管理", description = "獎項的新增、修改、刪除、查詢等操作")
@RestController
@RequestMapping("/admin/lotteries")
@RequiredArgsConstructor
public class LotteryPrizeController {

    private final LotteryPrizeService lotteryPrizeService;

    // ==================== CRUD 操作 ====================

    /**
     * 建立獎項
     */
    @Operation(summary = "建立獎項", description = "為指定商品建立新的獎項")
    @PostMapping("/{lotteryId}/prizes")
    public ResponseEntity<LotteryPrizeRes> createPrize(
            @Parameter(description = "商品ID") @PathVariable String lotteryId,
            @Valid @RequestBody LotteryPrizeCreateReq req) {
        req.setLotteryId(lotteryId);
        LotteryPrizeRes res = lotteryPrizeService.createPrize(req);
        return ResponseEntity.ok(res);
    }

    /**
     * 批量建立獎項
     */
    @Operation(summary = "批量建立獎項", description = "為指定商品批量建立多個獎項")
    @PostMapping("/{lotteryId}/prizes/batch")
    public ResponseEntity<List<LotteryPrizeRes>> createPrizes(
            @Parameter(description = "商品ID") @PathVariable String lotteryId,
            @Valid @RequestBody List<LotteryPrizeCreateReq> reqList) {
        List<LotteryPrizeRes> res = lotteryPrizeService.createPrizes(lotteryId, reqList);
        return ResponseEntity.ok(res);
    }

    /**
     * 更新獎項
     */
    @Operation(summary = "更新獎項", description = "更新現有獎項資訊")
    @PutMapping("/prizes/{prizeId}")
    public ResponseEntity<LotteryPrizeRes> updatePrize(
            @Parameter(description = "獎項ID") @PathVariable String prizeId,
            @Valid @RequestBody LotteryPrizeUpdateReq req) {
        req.setId(prizeId);
        LotteryPrizeRes res = lotteryPrizeService.updatePrize(req);
        return ResponseEntity.ok(res);
    }

    /**
     * 刪除獎項
     */
    @Operation(summary = "刪除獎項", description = "刪除指定獎項")
    @DeleteMapping("/prizes/{prizeId}")
    public ResponseEntity<Void> deletePrize(
            @Parameter(description = "獎項ID") @PathVariable String prizeId) {
        lotteryPrizeService.deletePrize(prizeId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 查詢單一獎項
     */
    @Operation(summary = "查詢獎項", description = "根據ID查詢獎項詳情")
    @GetMapping("/prizes/{prizeId}")
    public ResponseEntity<LotteryPrizeRes> getPrizeById(
            @Parameter(description = "獎項ID") @PathVariable String prizeId) {
        LotteryPrizeRes res = lotteryPrizeService.getPrizeById(prizeId);
        return ResponseEntity.ok(res);
    }

    /**
     * 查詢商品的所有獎項
     */
    @Operation(summary = "查詢商品獎項", description = "查詢指定商品的所有獎項")
    @GetMapping("/{lotteryId}/prizes")
    public ResponseEntity<List<LotteryPrizeRes>> getPrizesByLotteryId(
            @Parameter(description = "商品ID") @PathVariable String lotteryId) {
        List<LotteryPrizeRes> res = lotteryPrizeService.getPrizesByLotteryId(lotteryId);
        return ResponseEntity.ok(res);
    }

    /**
     * 根據等級查詢獎項
     */
    @Operation(summary = "根據等級查詢獎項", description = "查詢指定商品中特定等級的獎項")
    @GetMapping("/{lotteryId}/prizes/level/{level}")
    public ResponseEntity<List<LotteryPrizeRes>> getPrizesByLevel(
            @Parameter(description = "商品ID") @PathVariable String lotteryId,
            @Parameter(description = "獎項等級") @PathVariable String level) {
        List<LotteryPrizeRes> res = lotteryPrizeService.getPrizesByLevel(lotteryId, level);
        return ResponseEntity.ok(res);
    }

    // ==================== 特殊操作 ====================

    /**
     * 重置獎項剩餘數量
     */
    @Operation(summary = "重置剩餘數量", description = "將所有獎項的剩餘數量重置為總數量")
    @PostMapping("/{lotteryId}/prizes/reset")
    public ResponseEntity<Void> resetPrizeRemaining(
            @Parameter(description = "商品ID") @PathVariable String lotteryId) {
        lotteryPrizeService.resetPrizeRemaining(lotteryId);
        return ResponseEntity.ok().build();
    }

    /**
     * 查詢可選號碼清單（刮刮樂模式）
     */
    @Operation(summary = "查詢可選號碼", description = "查詢刮刮樂模式下可選的號碼清單")
    @GetMapping("/{lotteryId}/available-numbers")
    public ResponseEntity<List<String>> getAvailableNumbers(
            @Parameter(description = "商品ID") @PathVariable String lotteryId) {
        List<String> numbers = lotteryPrizeService.getAvailableNumbers(lotteryId);
        return ResponseEntity.ok(numbers);
    }
}
