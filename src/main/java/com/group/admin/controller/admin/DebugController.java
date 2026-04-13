package com.group.admin.controller.admin;

import com.group.admin.res.admin.AdminUserRes;
import com.group.admin.security.UserPrincipal;
import com.group.admin.service.AdminUserService;
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

    private final AdminUserService adminUserService;

    /**
     * 診斷當前使用者的 storeId 問題
     */
    @GetMapping("/store-diagnosis")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")
    public ResponseEntity<Map<String, Object>> diagnosisStoreId() {
        Map<String, Object> result = new HashMap<>();

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        result.put("principalType", principal.getClass().getSimpleName());

        if (principal instanceof UserPrincipal userPrincipal) {
            result.put("userId", userPrincipal.getUserId());
            result.put("username", userPrincipal.getUsername());
            result.put("storeIdsFromPrincipal", userPrincipal.getStoreIds());
            result.put("roles", userPrincipal.getAuthorities().stream()
                    .map(a -> a.getAuthority())
                    .collect(Collectors.toList()));

            String adminUserId = userPrincipal.getUserId();
            try {
                AdminUserRes adminUser = adminUserService.getAdminUser(adminUserId);
                result.put("adminUser_id", adminUser.getId());
                result.put("adminUser_username", adminUser.getUsername());
                result.put("adminUser_email", adminUser.getEmail());

                List<AdminUserRes.StoreInfo> stores = adminUser.getStores();
                result.put("storeUserCount", stores != null ? stores.size() : 0);
                if (stores != null && !stores.isEmpty()) {
                    result.put("stores", stores.stream()
                            .map(s -> Map.of("id", s.getId(), "storeName", s.getStoreName()))
                            .collect(Collectors.toList()));
                } else {
                    result.put("stores", "EMPTY - 使用者沒有關聯任何店家！");
                }
            } catch (Exception e) {
                result.put("adminUser", "NOT FOUND: " + e.getMessage());
            }

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
        List<AdminUserRes> adminUsers = adminUserService.getAllAdminUsers();

        List<Map<String, Object>> result = adminUsers.stream()
                .map(au -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", au.getId());
                    m.put("username", au.getUsername());
                    m.put("email", au.getEmail());
                    m.put("status", au.getStatus());
                    List<AdminUserRes.StoreInfo> stores = au.getStores();
                    m.put("storeCount", stores != null ? stores.size() : 0);
                    m.put("storeIds", stores != null
                            ? stores.stream().map(AdminUserRes.StoreInfo::getId).collect(Collectors.toList())
                            : List.of());
                    return m;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}
