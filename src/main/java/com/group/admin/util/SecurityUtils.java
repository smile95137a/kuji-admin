package com.group.admin.util;

import com.group.admin.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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
}
