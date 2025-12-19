package com.group.admin.annotation;

import java.lang.annotation.*;

/**
 * 權限驗證註解
 * 
 * <p>標記在 Controller 方法上，用於驗證當前用戶是否具有指定選單的操作權限</p>
 * 
 * <p>使用範例：</p>
 * <pre>
 * &#64;RequirePermission(menuCode = "product_management", permission = PermissionType.EDIT)
 * public void updateProduct(...) { ... }
 * </pre>
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    /**
     * 選單代碼
     */
    String menuCode();

    /**
     * 權限類型
     */
    PermissionType permission() default PermissionType.VIEW;

    /**
     * 權限類型列舉
     */
    enum PermissionType {
        /**
         * 查看權限
         */
        VIEW,
        
        /**
         * 編輯權限
         */
        EDIT,
        
        /**
         * 刪除權限
         */
        DELETE
    }
}
