package com.group.admin.controller.admin;

import com.group.admin.annotation.AuditLog;
import com.group.admin.condition.StoreCondition;
import com.group.admin.enums.AuditLogType;
import com.group.admin.req.common.QueryReq;
import com.group.admin.req.store.CreateStoreReq;
import com.group.admin.req.store.UpdateStoreReq;
import com.group.admin.req.store.UpdateStoreStatusReq;
import com.group.admin.res.common.EnumOption;
import com.group.admin.res.store.StoreRes;
import com.group.admin.service.StoreService;
import com.group.admin.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 後台店家管理 API
 * 
 * 路由：/admin/stores/**
 * 角色：ROLE_ADMIN, ROLE_STORE_OWNER, ROLE_STORE_EDITOR
 * 
 * 功能：
 * 1. 提供店家選項給後台選擇（新增 Banner、商品時）
 * 2. 根據角色過濾：Admin 看全部，StoreOwner 只看自己的
 * 3. 完整的店家 CRUD 管理
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@RestController
@RequestMapping("/admin/stores")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "後台店家管理", description = "後台店家管理 API（需登入）")
public class AdminStoreController {

    private final StoreService storeService;

    /**
     * 取得店家選項列表（後台專用）
     * 
     * <p>權限邏輯：</p>
     * <ul>
     *   <li>ROLE_ADMIN：返回所有店家（可選包含停用店家）</li>
     *   <li>ROLE_STORE_OWNER：只返回自己的店家</li>
     *   <li>ROLE_STORE_EDITOR：只返回自己的店家</li>
     * </ul>
     * 
     * @param activeOnly 是否只返回啟用的店家（Admin 可設為 false 包含停用店家）
     * @return 店家選項列表
     */
    @GetMapping("/options")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")
    @Operation(summary = "取得店家選項（後台）", description = "根據角色過濾店家列表")
    public ResponseEntity<List<EnumOption>> getStoreOptions(
            @RequestParam(required = false, defaultValue = "true")
            @Parameter(description = "是否只返回啟用的店家（Admin 可設為 false）", example = "true")
            Boolean activeOnly) {
        
        String userId = SecurityUtils.getCurrentUserId();
        boolean isAdmin = SecurityUtils.isAdmin();
        
        log.info("🏪 [後台] 取得店家選項，userId：{}，isAdmin：{}，activeOnly：{}", userId, isAdmin, activeOnly);
        
        List<EnumOption> options = storeService.getStoreOptionsForUser(userId, isAdmin, activeOnly);
        
        log.info("✅ [後台] 返回 {} 個店家選項", options.size());
        return ResponseEntity.ok(options);
    }

    /**
     * 搜尋店家（後台專用，支援關鍵字）
     * 
     * @param keyword 搜尋關鍵字
     * @param activeOnly 是否只返回啟用的店家
     * @return 符合條件的店家列表
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")
    @Operation(summary = "搜尋店家（後台）", description = "根據店家名稱關鍵字搜尋")
    public ResponseEntity<List<EnumOption>> searchStores(
            @RequestParam
            @Parameter(description = "搜尋關鍵字", example = "玩具")
            String keyword,
            @RequestParam(required = false, defaultValue = "true")
            @Parameter(description = "是否只返回啟用的店家", example = "true")
            Boolean activeOnly) {
        
        String userId = SecurityUtils.getCurrentUserId();
        boolean isAdmin = SecurityUtils.isAdmin();
        List<String> storeIds = SecurityUtils.getCurrentUserStoreIds();
        
        log.info("🔍 [後台] 搜尋店家，keyword：{}，userId：{}，isAdmin：{}", keyword, userId, isAdmin);
        
        List<EnumOption> options = storeService.searchStoreOptions(userId, isAdmin, storeIds, keyword, activeOnly);
        
        log.info("✅ [後台] 搜尋到 {} 個店家", options.size());
        return ResponseEntity.ok(options);
    }

    /**
     * 建立店家（含負責人帳號）
     */
    @AuditLog(type = AuditLogType.ADMIN_ACTION, action = "CREATE", targetType = "STORE")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "建立店家", description = "建立店家並可選建立負責人帳號（ADMIN 專用）")
    public ResponseEntity<StoreRes> createStore(@Valid @RequestBody CreateStoreReq req) {
        String operatorId = SecurityUtils.getCurrentUserId();
        log.info("🏪 [後台] 建立店家: storeName={}, operatorId={}", req.getStoreName(), operatorId);
        StoreRes res = storeService.createStore(req, operatorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    /**
     * 取得所有店家選項（不受權限限制，Admin 專用）
     * 
     * @return 所有店家選項
     */
    @GetMapping("/all-options")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "取得所有店家選項（Admin 專用）", description = "返回所有店家，用於下拉選單")
    public ResponseEntity<List<EnumOption>> getAllStoreOptions() {
        log.info("📋 [後台] 取得所有店家選項（不受權限限制）");
        List<EnumOption> options = storeService.getAllActiveStoreOptions();
        log.info("✅ [後台] 返回 {} 個店家選項", options.size());
        return ResponseEntity.ok(options);
    }

    /**
     * 查詢店家列表（完整版）
     * 
     * @param req 查詢請求
     * @return 店家列表
     */
    @PostMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")
    @Operation(summary = "查詢店家列表", description = "支援條件查詢、排序")
    public ResponseEntity<List<StoreRes>> queryStores(
            @RequestBody(required = false)
            @Parameter(description = "查詢請求（所有條件可選）")
            QueryReq<StoreCondition> req) {
        
        log.info("📋 [後台] 查詢店家列表");
        
        List<StoreRes> stores = storeService.queryStores(req);
        
        log.info("✅ [後台] 返回 {} 個店家", stores.size());
        return ResponseEntity.ok(stores);
    }

    /**
     * 查詢店家詳情
     * 
     * @param storeId 店家 ID
     * @return 店家詳情
     */
    @GetMapping("/{storeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")
    @Operation(summary = "查詢店家詳情", description = "取得店家完整資訊")
    public ResponseEntity<StoreRes> getStoreById(
            @PathVariable
            @Parameter(description = "店家 ID", example = "uuid-store-1")
            String storeId) {
        
        log.info("🔍 [後台] 查詢店家詳情：storeId={}", storeId);
        
        StoreRes store = storeService.getStoreById(storeId);
        
        log.info("✅ [後台] 返回店家詳情");
        return ResponseEntity.ok(store);
    }

    /**
     * 更新店家資訊
     * 
     * @param storeId 店家 ID
     * @param req 更新請求
     * @return 更新後的店家資訊
     */
    @AuditLog(type = AuditLogType.ADMIN_ACTION, action = "UPDATE", targetType = "STORE")
    @PutMapping("/{storeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER')")
    @Operation(summary = "更新店家資訊", description = "店主或 Admin 可更新")
    public ResponseEntity<StoreRes> updateStore(
            @PathVariable
            @Parameter(description = "店家 ID", example = "uuid-store-1")
            String storeId,
            @Valid @RequestBody
            @Parameter(description = "更新請求")
            UpdateStoreReq req) {
        
        log.info("✏️ [後台] 更新店家：storeId={}", storeId);
        
        StoreRes store = storeService.updateStore(storeId, req);
        
        log.info("✅ [後台] 店家更新成功");
        return ResponseEntity.ok(store);
    }

    /**
     * 更新店家狀態（啟用或停用）
     * 
     * @param storeId 店家 ID
     * @param req 狀態更新請求
     * @return 成功訊息
     */
    @AuditLog(type = AuditLogType.ADMIN_ACTION, action = "UPDATE_STATUS", targetType = "STORE")
    @PutMapping("/{storeId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新店家狀態", description = "啟用或停用店家（ADMIN 專用）；停用時加 ?force=true 強制下架所有商品")
    public ResponseEntity<Object> updateStoreStatus(
            @PathVariable
            @Parameter(description = "店家 ID", example = "uuid-store-1")
            String storeId,
            @Valid @RequestBody UpdateStoreStatusReq req,
            @RequestParam(defaultValue = "false") boolean force) {
        String operatorId = SecurityUtils.getCurrentUserId();
        log.info("🔄 [後台] 更新店家狀態: storeId={}, status={}, force={}", storeId, req.getStatus(), force);
        Object result = storeService.updateStoreStatus(storeId, req, force, operatorId);
        return ResponseEntity.ok(result);
    }
}
