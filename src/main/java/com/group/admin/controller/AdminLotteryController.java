package com.group.admin.controller;

import com.group.admin.req.lottery.LotteryCreateReq;
import com.group.admin.req.lottery.LotteryQueryReq;
import com.group.admin.req.lottery.LotteryUpdateReq;
import com.group.admin.res.PageResult;
import com.group.admin.res.lottery.LotteryListRes;
import com.group.admin.res.lottery.LotteryRes;
import com.group.admin.service.LotteryService;
import com.group.admin.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 後台抽獎商品管理 Controller
 *
 * <p>提供商品的 CRUD、狀態管理、降價控制等操作</p>
 * <p>所有 ID 均為 UUID (String)</p>
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Tag(name = "商品管理", description = "抽獎商品的新增、修改、刪除、上架下架等操作")
@RestController
@RequestMapping("/admin/lotteries")
@RequiredArgsConstructor
public class AdminLotteryController {

    private final LotteryService lotteryService;

    // ==================== CRUD 操作 ====================

    /**
     * 建立抽獎商品
     */
    @Operation(summary = "建立商品", description = "建立新的抽獎商品（狀態為草稿）")
    @PostMapping
    public ResponseEntity<LotteryRes> createLottery(
            @Valid @RequestBody LotteryCreateReq req) {
        String operatorId = SecurityUtils.getCurrentAdminUserId();
        LotteryRes res = lotteryService.createLottery(req, operatorId);
        return ResponseEntity.ok(res);
    }

    /**
     * 更新抽獎商品
     */
    @Operation(summary = "更新商品", description = "更新現有商品資訊（僅限草稿或已下架狀態）")
    @PutMapping
    public ResponseEntity<LotteryRes> updateLottery(
            @Valid @RequestBody LotteryUpdateReq req) {
        String operatorId = SecurityUtils.getCurrentAdminUserId();
        LotteryRes res = lotteryService.updateLottery(req, operatorId);
        return ResponseEntity.ok(res);
    }

    /**
     * 刪除抽獎商品
     */
    @Operation(summary = "刪除商品", description = "刪除指定商品（僅限草稿或已下架狀態）")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLottery(
            @Parameter(description = "商品ID (UUID)") @PathVariable String id) {
        String operatorId = SecurityUtils.getCurrentAdminUserId();
        lotteryService.deleteLottery(id, operatorId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 查詢單一商品詳情
     */
    @Operation(summary = "查詢商品詳情", description = "根據ID查詢商品完整資訊")
    @GetMapping("/{id}")
    public ResponseEntity<LotteryRes> getLotteryById(
            @Parameter(description = "商品ID (UUID)") @PathVariable String id) {
        LotteryRes res = lotteryService.getLotteryById(id);
        return ResponseEntity.ok(res);
    }

    /**
     * 分頁查詢商品列表
     */
    @Operation(summary = "查詢商品列表", description = "分頁查詢商品，支援篩選和排序")
    @GetMapping
    public ResponseEntity<PageResult<LotteryListRes>> queryLotteries(
            @Parameter(description = "店家ID篩選") @RequestParam(required = false) String storeId,
            @Parameter(description = "關鍵字搜尋") @RequestParam(required = false) String keyword,
            @Parameter(description = "分類篩選") @RequestParam(required = false) String category,
            @Parameter(description = "狀態篩選") @RequestParam(required = false) String status,
            @Parameter(description = "頁碼") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每頁筆數") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "排序欄位") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        LotteryQueryReq req = new LotteryQueryReq();
        req.setStoreId(storeId);
        req.setKeyword(keyword);
        req.setCategory(category);
        req.setStatus(status);
        req.setPage(page);
        req.setSize(size);
        req.setSortBy(sortBy);
        req.setSortDirection(sortDirection);
        
        PageResult<LotteryListRes> result = lotteryService.queryLotteries(req);
        return ResponseEntity.ok(result);
    }

    // ==================== 狀態管理 ====================

    /**
     * 上架商品
     */
    @Operation(summary = "上架商品", description = "將商品狀態設為已上架")
    @PostMapping("/{id}/publish")
    public ResponseEntity<LotteryRes> publishLottery(
            @Parameter(description = "商品ID (UUID)") @PathVariable String id) {
        String operatorId = SecurityUtils.getCurrentAdminUserId();
        LotteryRes res = lotteryService.publishLottery(id, operatorId);
        return ResponseEntity.ok(res);
    }

    /**
     * 下架商品
     */
    @Operation(summary = "下架商品", description = "將商品狀態設為已下架")
    @PostMapping("/{id}/unpublish")
    public ResponseEntity<LotteryRes> unpublishLottery(
            @Parameter(description = "商品ID (UUID)") @PathVariable String id) {
        String operatorId = SecurityUtils.getCurrentAdminUserId();
        LotteryRes res = lotteryService.unpublishLottery(id, operatorId);
        return ResponseEntity.ok(res);
    }

    /**
     * 強制下架商品
     */
    @Operation(summary = "強制下架", description = "強制下架商品並記錄原因")
    @PostMapping("/{id}/force-off")
    public ResponseEntity<LotteryRes> forceOffShelf(
            @Parameter(description = "商品ID (UUID)") @PathVariable String id,
            @Parameter(description = "下架原因") @RequestParam String reason) {
        String operatorId = SecurityUtils.getCurrentAdminUserId();
        LotteryRes res = lotteryService.forceOffShelf(id, reason, operatorId);
        return ResponseEntity.ok(res);
    }

    // ==================== 統計相關 ====================

    /**
     * 獲取商品獎項統計
     */
    @Operation(summary = "獲取獎項統計", description = "獲取商品的獎項數量統計資訊")
    @GetMapping("/{id}/statistics")
    public ResponseEntity<Map<String, Object>> getPrizeStatistics(
            @Parameter(description = "商品ID (UUID)") @PathVariable String id) {
        Map<String, Object> stats = lotteryService.getPrizeStatistics(id);
        return ResponseEntity.ok(stats);
    }

    /**
     * 獲取剩餘抽數
     */
    @Operation(summary = "獲取剩餘抽數", description = "從獎池計算商品的剩餘可抽次數")
    @GetMapping("/{id}/remaining")
    public ResponseEntity<Integer> getRemainingDrawCount(
            @Parameter(description = "商品ID (UUID)") @PathVariable String id) {
        int remaining = lotteryService.getRemainingDrawCount(id);
        return ResponseEntity.ok(remaining);
    }

    // ==================== 降價控制 ====================

    /**
     * 手動觸發降價
     */
    @Operation(summary = "手動觸發降價", description = "手動觸發大獎售完降價機制")
    @PostMapping("/{id}/trigger-discount")
    public ResponseEntity<Void> triggerDiscount(
            @Parameter(description = "商品ID (UUID)") @PathVariable String id) {
        lotteryService.triggerGrandPrizeDiscount(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 檢查降價狀態
     */
    @Operation(summary = "檢查降價狀態", description = "檢查商品是否滿足降價條件")
    @GetMapping("/{id}/discount-status")
    public ResponseEntity<Boolean> checkDiscountStatus(
            @Parameter(description = "商品ID (UUID)") @PathVariable String id) {
        boolean triggered = lotteryService.checkAndTriggerDiscount(id);
        return ResponseEntity.ok(triggered);
    }
}
