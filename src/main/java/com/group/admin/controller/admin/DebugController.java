package com.group.admin.controller.admin;

import com.group.admin.entity.AdminUser;
import com.group.admin.entity.Store;
import com.group.admin.entity.StoreUser;
import com.group.admin.example.AdminUserExample;
import com.group.admin.example.StoreExample;
import com.group.admin.example.StoreUserExample;
import com.group.admin.mapper.AdminUserMapper;
import com.group.admin.mapper.StoreMapper;
import com.group.admin.mapper.StoreUserMapper;
import com.group.admin.security.UserPrincipal;
import com.group.admin.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 除錯用 API（可在正式環境刪除）
 */
@RestController
@RequestMapping("/admin/debug")
@RequiredArgsConstructor
@Slf4j
public class DebugController {

    private final AdminUserMapper adminUserMapper;
    private final StoreMapper storeMapper;
    private final StoreUserMapper storeUserMapper;

    /**
     * 診斷當前使用者的 storeId 問題
     */
    @GetMapping("/store-diagnosis")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")
    public ResponseEntity<Map<String, Object>> diagnosisStoreId() {
        Map<String, Object> result = new HashMap<>();
        
        // 1. 從 SecurityContext 取得資訊
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        result.put("principalType", principal.getClass().getSimpleName());
        
        if (principal instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) principal;
            result.put("userId", userPrincipal.getUserId());
            result.put("username", userPrincipal.getUsername());
            result.put("storeIdsFromPrincipal", userPrincipal.getStoreIds());
            result.put("roles", userPrincipal.getAuthorities().stream()
                    .map(a -> a.getAuthority())
                    .collect(Collectors.toList()));
            
            String adminUserId = userPrincipal.getUserId();
            
            // 2. 直接查詢資料庫 admin_user
            AdminUserExample adminUserExample = new AdminUserExample();
            adminUserExample.createCriteria().andIdEqualTo(adminUserId);
            List<AdminUser> adminUsers = adminUserMapper.selectByExample(adminUserExample);
            if (!adminUsers.isEmpty()) {
                AdminUser adminUser = adminUsers.get(0);
                result.put("adminUser_id", adminUser.getId());
                result.put("adminUser_username", adminUser.getUsername());
                result.put("adminUser_email", adminUser.getEmail());
            } else {
                result.put("adminUser", "NOT FOUND with id: " + adminUserId);
            }
            
            // 3. 查詢 store_user 表
            StoreUserExample storeUserExample = new StoreUserExample();
            storeUserExample.createCriteria().andAdminUserIdEqualTo(adminUserId);
            List<StoreUser> storeUsers = storeUserMapper.selectByExample(storeUserExample);
            result.put("storeUserCount", storeUsers.size());
            
            if (!storeUsers.isEmpty()) {
                List<Map<String, String>> storeUserList = storeUsers.stream()
                        .map(su -> {
                            Map<String, String> m = new HashMap<>();
                            m.put("storeId", su.getStoreId());
                            m.put("adminUserId", su.getAdminUserId());
                            return m;
                        })
                        .collect(Collectors.toList());
                result.put("storeUsers", storeUserList);
                
                // 4. 查詢對應的 store
                List<String> storeIds = storeUsers.stream()
                        .map(StoreUser::getStoreId)
                        .collect(Collectors.toList());
                
                StoreExample storeExample = new StoreExample();
                storeExample.createCriteria().andIdIn(storeIds);
                List<Store> stores = storeMapper.selectByExample(storeExample);
                
                List<Map<String, String>> storeList = stores.stream()
                        .map(s -> {
                            Map<String, String> m = new HashMap<>();
                            m.put("id", s.getId());
                            m.put("storeName", s.getStoreName());
                            m.put("status", s.getStatus());
                            return m;
                        })
                        .collect(Collectors.toList());
                result.put("stores", storeList);
            } else {
                result.put("storeUsers", "EMPTY - 使用者沒有關聯任何店家！");
                result.put("stores", "N/A");
            }
            
            // 5. SecurityUtils 的結果
            result.put("SecurityUtils.getCurrentUserId()", SecurityUtils.getCurrentUserId());
            result.put("SecurityUtils.getCurrentUserStoreIds()", SecurityUtils.getCurrentUserStoreIds());
            result.put("SecurityUtils.getCurrentUserPrimaryStoreId()", SecurityUtils.getCurrentUserPrimaryStoreId());
            result.put("SecurityUtils.isAdmin()", SecurityUtils.isAdmin());
            
        } else {
            result.put("error", "Principal is not UserPrincipal: " + principal);
        }
        
        log.info("🔍 Store 診斷結果: {}", result);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 查詢所有 admin_user 和他們的 store 關聯
     */
    @GetMapping("/all-admin-users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getAllAdminUsers() {
        List<AdminUser> adminUsers = adminUserMapper.selectByExample(new AdminUserExample());
        
        List<Map<String, Object>> result = adminUsers.stream()
                .map(au -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", au.getId());
                    m.put("username", au.getUsername());
                    m.put("email", au.getEmail());
                    m.put("status", au.getStatus());
                    
                    // 查詢關聯的店家
                    StoreUserExample sue = new StoreUserExample();
                    sue.createCriteria().andAdminUserIdEqualTo(au.getId());
                    List<StoreUser> storeUsers = storeUserMapper.selectByExample(sue);
                    
                    m.put("storeCount", storeUsers.size());
                    m.put("storeIds", storeUsers.stream()
                            .map(StoreUser::getStoreId)
                            .collect(Collectors.toList()));
                    
                    return m;
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(result);
    }
}
