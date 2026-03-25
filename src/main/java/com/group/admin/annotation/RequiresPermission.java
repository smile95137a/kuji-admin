package com.group.admin.annotation;

import java.lang.annotation.*;

/**
 * 細粒度權限驗證註解（基於選單代碼 + 權限等級）
 *
 * <p>與 {@link RequirePermission} 搭配使用，由 PermissionCheckAspect 執行攔截。
 * ROLE_ADMIN 角色自動放行，其他角色查詢 role_menu 表確認權限。</p>
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresPermission {

    /**
     * 選單代碼
     */
    String menuCode();

    /**
     * 權限等級（預設 VIEW）
     */
    PermissionLevel level() default PermissionLevel.VIEW;

    /**
     * 權限等級列舉
     */
    enum PermissionLevel {
        VIEW,
        EDIT,
        DELETE
    }
}
