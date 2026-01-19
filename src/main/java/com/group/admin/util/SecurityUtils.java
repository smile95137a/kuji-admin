package com.group.admin.util;

import com.group.admin.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;

/**
 * 安全工具類
 *
 * @author KUJI System
 * @since 1.0.0
 */
public class SecurityUtils {

    /**
     * 獲取當前登入用戶ID
     *
     * @return 用戶ID (UUID 字串)，如果未登入則返回 null
     */
    public static String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal == null || "anonymousUser".equals(principal)) {
            return null;
        }

        // 如果是 UserPrincipal，直接取得 userId
        if (principal instanceof UserPrincipal) {
            return ((UserPrincipal) principal).getUserId();
        }

        // 兼容舊的 String 形式
        if (principal instanceof String) {
            return (String) principal;
        }
        
        return null;
    }

    /**
     * 獲取當前登入用戶名
     *
     * @return 用戶名，如果未登入則返回 null
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal == null || "anonymousUser".equals(principal)) {
            return null;
        }

        // 如果是 UserPrincipal，取得 username
        if (principal instanceof UserPrincipal) {
            return ((UserPrincipal) principal).getUsername();
        }

        // 兼容 String 形式
        if (principal instanceof String) {
            return (String) principal;
        }
        
        return authentication.getName();
    }

    /**
     * 判斷當前用戶是否已認證
     *
     * @return 是否已認證
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal());
    }

    /**
     * 判斷當前用戶是否擁有指定角色
     *
     * @param role 角色名稱（不含 ROLE_ 前綴）
     * @return 是否擁有該角色
     */
    public static boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals(roleWithPrefix));
    }

    /**
     * 判斷當前用戶是否為管理員
     *
     * @return 是否為管理員
     */
    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * 判斷當前用戶是否為店家主帳號
     *
     * @return 是否為店家主帳號
     */
    public static boolean isStoreOwner() {
        return hasRole("STORE_OWNER");
    }

    /**
     * 判斷當前用戶是否為店家編輯者
     *
     * @return 是否為店家編輯者
     */
    public static boolean isStoreEditor() {
        return hasRole("STORE_EDITOR");
    }

    /**
     * 獲取當前後台管理員用戶ID
     * 用於 /admin/** 路由
     *
     * @return 管理員用戶ID (UUID 字串)
     */
    public static String getCurrentAdminUserId() {
        return getCurrentUserId();
    }

    /**
     * 獲取當前前台API用戶ID
     * 用於 /api/** 路由
     *
     * @return 前台用戶ID (UUID 字串)
     */
    public static String getCurrentApiUserId() {
        return getCurrentUserId();
    }

    /**
     * 取得當前使用者的店家 ID 列表
     * 
     * @return 店家 ID 列表（可能有多個店家）
     *         如果是 ROLE_ADMIN，返回空列表（表示可存取所有店家）
     *         如果未登入或無店家，返回空列表
     */
    public static List<String> getCurrentUserStoreIds() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Collections.emptyList();
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserPrincipal)) {
            return Collections.emptyList();
        }

        UserPrincipal userPrincipal = (UserPrincipal) principal;
        
        // ✅ 修改：Admin 也返回 storeIds（如果有的話）
        // 原本的邏輯：如果是 Admin，返回空列表（表示可以存取所有店家）
        // 新邏輯：返回實際的 storeIds，讓 Admin 也能自動帶入 storeId
        
        // 返回店家 ID 列表
        List<String> storeIds = userPrincipal.getStoreIds();
        return storeIds != null ? storeIds : Collections.emptyList();
    }

    /**
     * 取得當前使用者的主要店家 ID（第一個店家）
     * 
     * @return 店家 ID（單一）
     *         如果使用者有多個店家，返回第一個
     *         如果是 ROLE_ADMIN 或無店家，返回 null
     */
    public static String getCurrentUserPrimaryStoreId() {
        List<String> storeIds = getCurrentUserStoreIds();
        return storeIds.isEmpty() ? null : storeIds.get(0);
    }

    /**
     * 檢查當前使用者是否有權限存取指定店家
     * 
     * @param storeId 要檢查的店家 ID
     * @return 是否有權限
     *         ROLE_ADMIN 永遠返回 true
     *         其他角色需要在 storeIds 列表中
     */
    public static boolean canAccessStore(String storeId) {
        if (storeId == null) {
            return false;
        }
        
        // Admin 可以存取所有店家
        if (isAdmin()) {
            return true;
        }
        
        List<String> storeIds = getCurrentUserStoreIds();
        return storeIds.contains(storeId);
    }
}
