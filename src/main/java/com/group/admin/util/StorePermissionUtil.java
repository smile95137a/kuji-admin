package com.group.admin.util;

import com.group.admin.security.UserPrincipal;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

/**
 * 店家資料存取權限工具
 *
 * <p>用於 Controller 層驗證當前使用者是否有權存取指定店家的資料。
 * ROLE_ADMIN 可存取所有店家，其他角色只能存取自己綁定的店家。</p>
 *
 * @author KUJI System
 * @since 1.0.0
 */
public class StorePermissionUtil {

    private StorePermissionUtil() {
        // 工具類，禁止實例化
    }

    /**
     * 斷言當前使用者可存取指定店家
     *
     * @param auth    Spring Security Authentication
     * @param storeId 目標店家 ID
     * @throws AccessDeniedException 如果無權存取
     */
    public static void assertStoreAccess(Authentication auth, String storeId) {
        if (auth == null || auth.getPrincipal() == null) {
            throw new AccessDeniedException("未認證");
        }

        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();

        // ROLE_ADMIN 可存取所有店家
        if (principal.getRoles() != null && principal.getRoles().contains("ROLE_ADMIN")) {
            return;
        }

        // 檢查使用者綁定的店家列表是否包含目標店家
        if (principal.getStoreIds() != null && principal.getStoreIds().contains(storeId)) {
            return;
        }

        throw new AccessDeniedException("無權存取此店家資料");
    }
}
