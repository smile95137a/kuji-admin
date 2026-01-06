package com.group.admin.controller.admin;

import com.group.admin.exception.BusinessException;
import com.group.admin.req.common.QueryReq;
import com.group.admin.req.lottery.LotteryCondition;
import com.group.admin.req.lottery.LotteryCreateReq;
import com.group.admin.req.lottery.LotteryUpdateReq;
import com.group.admin.res.lottery.LotteryRes;
import com.group.admin.service.LotteryService;
import com.group.admin.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 後台商品管理 API
 * 
 * 路由：/admin/lottery/**
 * 角色：ROLE_ADMIN, ROLE_STORE_OWNER, ROLE_STORE_EDITOR
 * 
 * 特色：
 * 1. 自動從 JWT Token 帶入 storeId
 * 2. 使用 Condition + QueryReq 模式查詢
 * 3. 返回全部資料，前端做分頁
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@RestController
@RequestMapping("/admin/lottery")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "後台商品管理", description = "後台商品 CRUD API")
public class AdminLotteryController {

    private final LotteryService lotteryService;

    /**
     * 查詢商品列表（後台）
     * 
     * ✅ 自動帶入當前使用者的 StoreID
     * ✅ 前端做分頁，後端返回全部資料
     * ✅ 所有查詢條件都是可選的
     * 
     * @param req 查詢請求（可選）
     * @return 商品列表
     */
    @PostMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")
    @Operation(summary = "查詢商品列表", description = "後台查詢商品，自動過濾當前店家")
    public ResponseEntity<List<LotteryRes>> queryLotteries(
            @RequestBody(required = false) QueryReq<LotteryCondition> req) {
        
        // 取得當前使用者的店家 ID
        String storeId = SecurityUtils.getCurrentUserPrimaryStoreId();
        
        log.info("🔍 查詢商品列表: userId={}, storeId={}, condition={}", 
                 SecurityUtils.getCurrentUserId(), storeId, req);
        
        // 自動設定 storeId（如果不是 Admin）
        if (storeId != null) {
            if (req == null) {
                req = new QueryReq<>();
            }
            if (req.getCondition() == null) {
                req.setCondition(new LotteryCondition());
            }
            req.getCondition().setStoreId(storeId);
        }
        
        List<LotteryRes> result = lotteryService.queryLotteries(req);
        
        log.info("✅ 查詢成功: 共 {} 筆", result.size());
        return ResponseEntity.ok(result);
    }

    /**
     * 新增商品
     * 
     * ✅ 自動帶入當前使用者的 StoreID
     * 
     * @param req 商品建立請求
     * @return 建立的商品
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")
    @Operation(summary = "新增商品", description = "自動帶入店家 ID")
    public ResponseEntity<LotteryRes> createLottery(
            @Valid @RequestBody LotteryCreateReq req) {
        
        String storeId = SecurityUtils.getCurrentUserPrimaryStoreId();
        String userId = SecurityUtils.getCurrentUserId();
        
        if (storeId == null && !SecurityUtils.isAdmin()) {
            throw new BusinessException("無法取得店家資訊");
        }
        
        log.info("➕ 新增商品: userId={}, storeId={}, title={}", userId, storeId, req.getTitle());
        
        // 自動設定 storeId
        if (storeId != null) {
            req.setStoreId(storeId);
        }
        
        LotteryRes result = lotteryService.createLottery(req);
        
        log.info("✅ 新增成功: id={}", result.getId());
        return ResponseEntity.ok(result);
    }

    /**
     * 更新商品
     * 
     * ✅ 驗證使用者是否有權限修改此商品
     * 
     * @param id 商品 ID
     * @param req 更新請求
     * @return 更新後的商品
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")
    @Operation(summary = "更新商品", description = "更新商品資訊")
    public ResponseEntity<LotteryRes> updateLottery(
            @PathVariable String id,
            @Valid @RequestBody LotteryUpdateReq req) {
        
        String userId = SecurityUtils.getCurrentUserId();
        
        log.info("✏️ 更新商品: userId={}, lotteryId={}", userId, id);
        
        // 驗證權限（Service 層會檢查）
        LotteryRes result = lotteryService.updateLottery(id, req);
        
        log.info("✅ 更新成功");
        return ResponseEntity.ok(result);
    }

    /**
     * 刪除商品
     * 
     * ✅ 驗證使用者是否有權限刪除此商品
     * 
     * @param id 商品 ID
     * @return 刪除結果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER')")
    @Operation(summary = "刪除商品", description = "刪除商品（僅 Admin 和店主）")
    public ResponseEntity<Void> deleteLottery(@PathVariable String id) {
        
        String userId = SecurityUtils.getCurrentUserId();
        
        log.info("🗑️ 刪除商品: userId={}, lotteryId={}", userId, id);
        
        lotteryService.deleteLottery(id);
        
        log.info("✅ 刪除成功");
        return ResponseEntity.noContent().build();
    }

    /**
     * 取得商品詳情
     * 
     * @param id 商品 ID
     * @return 商品詳情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")
    @Operation(summary = "取得商品詳情", description = "查詢單一商品")
    public ResponseEntity<LotteryRes> getLottery(@PathVariable String id) {
        
        log.info("🔍 查詢商品詳情: lotteryId={}", id);
        
        LotteryRes result = lotteryService.getLottery(id);
        
        return ResponseEntity.ok(result);
    }

    /**
     * 上架商品
     * 
     * @param id 商品 ID
     * @return 更新後的商品
     */
    @PostMapping("/{id}/on-shelf")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER')")
    @Operation(summary = "上架商品", description = "將商品設為上架狀態")
    public ResponseEntity<LotteryRes> onShelf(@PathVariable String id) {
        
        String userId = SecurityUtils.getCurrentUserId();
        
        log.info("📤 上架商品: userId={}, lotteryId={}", userId, id);
        
        LotteryRes result = lotteryService.updateStatus(id, "ON_SHELF");
        
        log.info("✅ 上架成功");
        return ResponseEntity.ok(result);
    }

    /**
     * 下架商品
     * 
     * @param id 商品 ID
     * @return 更新後的商品
     */
    @PostMapping("/{id}/off-shelf")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER')")
    @Operation(summary = "下架商品", description = "將商品設為下架狀態")
    public ResponseEntity<LotteryRes> offShelf(@PathVariable String id) {
        
        String userId = SecurityUtils.getCurrentUserId();
        
        log.info("📥 下架商品: userId={}, lotteryId={}", userId, id);
        
        LotteryRes result = lotteryService.updateStatus(id, "OFF_SHELF");
        
        log.info("✅ 下架成功");
        return ResponseEntity.ok(result);
    }

    /**
     * 取得店家 ID 列表（前端用）
     * 
     * @return 店家 ID 列表
     */
    @GetMapping("/my-stores")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")
    @Operation(summary = "取得我的店家列表", description = "前端用於顯示店家選擇器")
    public ResponseEntity<List<String>> getMyStores() {
        
        List<String> storeIds = SecurityUtils.getCurrentUserStoreIds();
        
        log.info("🏪 取得店家列表: userId={}, storeIds={}", 
                 SecurityUtils.getCurrentUserId(), storeIds);
        
        return ResponseEntity.ok(storeIds);
    }
}
