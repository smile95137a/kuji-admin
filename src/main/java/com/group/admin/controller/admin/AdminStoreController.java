package com.group.admin.controller.admin;

import com.group.admin.entity.Store;
import com.group.admin.entity.StoreUser;
import com.group.admin.example.StoreExample;
import com.group.admin.example.StoreUserExample;
import com.group.admin.mapper.StoreMapper;
import com.group.admin.mapper.StoreUserMapper;
import com.group.admin.res.common.EnumOption;
import com.group.admin.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 後台店家管理 API
 * 
 * 路由：/admin/stores/**
 * 角色：ROLE_ADMIN, ROLE_STORE_OWNER, ROLE_STORE_EDITOR
 * 
 * 功能：
 * 1. 提供店家選項給後台選擇（新增 Banner、商品時）
 * 2. 根據角色過濾：Admin 看全部，StoreOwner 只看自己的
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@RestController
@RequestMapping("/admin/stores")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "後台店家管理", description = "後台店家選項 API（需登入）")
public class AdminStoreController {

    private final StoreMapper storeMapper;
    private final StoreUserMapper storeUserMapper;

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
        
        log.info("🏪 [後台] 取得店家選項，userId：{}，isAdmin：{}，activeOnly：{}", 
                 userId, isAdmin, activeOnly);
        
        StoreExample example = new StoreExample();
        StoreExample.Criteria criteria = example.createCriteria();
        
        // 權限過濾：非 Admin 只能看自己的店家
        if (!isAdmin) {
            // 從資料庫查詢使用者的店家列表
            StoreUserExample storeUserExample = new StoreUserExample();
            storeUserExample.createCriteria().andAdminUserIdEqualTo(userId);
            List<StoreUser> storeUsers = storeUserMapper.selectByExample(storeUserExample);
            
            if (storeUsers.isEmpty()) {
                log.warn("⚠️ [後台] 使用者沒有關聯的店家：userId={}", userId);
                return ResponseEntity.ok(List.of());
            }
            
            List<String> storeIds = storeUsers.stream()
                    .map(StoreUser::getStoreId)
                    .collect(Collectors.toList());
            
            criteria.andIdIn(storeIds);
            log.info("🔒 [後台] 過濾店家：只顯示 {} 個店家", storeIds.size());
        } else {
            log.info("👑 [後台] Admin 可看所有店家");
        }
        
        // 狀態過濾
        if (activeOnly) {
            criteria.andStatusEqualTo("ACTIVE");
        }
        
        example.setOrderByClause("store_name ASC");
        
        List<Store> stores = storeMapper.selectByExample(example);
        
        List<EnumOption> options = stores.stream()
                .map(store -> EnumOption.builder()
                        .label(store.getStoreName())
                        .value(store.getId())
                        .description(String.format("%s (%s)", 
                                store.getShortDescription() != null ? store.getShortDescription() : "",
                                store.getStatus()))
                        .build())
                .collect(Collectors.toList());
        
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
        
        StoreExample example = new StoreExample();
        StoreExample.Criteria criteria = example.createCriteria();
        
        // 權限過濾
        if (!isAdmin && storeIds != null && !storeIds.isEmpty()) {
            criteria.andIdIn(storeIds);
        }
        
        // 關鍵字搜尋
        criteria.andStoreNameLike("%" + keyword + "%");
        
        // 狀態過濾
        if (activeOnly) {
            criteria.andStatusEqualTo("ACTIVE");
        }
        
        example.setOrderByClause("store_name ASC");
        
        List<Store> stores = storeMapper.selectByExample(example);
        
        List<EnumOption> options = stores.stream()
                .map(store -> EnumOption.builder()
                        .label(store.getStoreName())
                        .value(store.getId())
                        .description(store.getShortDescription())
                        .build())
                .collect(Collectors.toList());
        
        log.info("✅ [後台] 搜尋到 {} 個店家", options.size());
        return ResponseEntity.ok(options);
    }
}
