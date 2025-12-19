package com.group.admin.annotation;

import java.lang.annotation.*;

/**
 * 角色驗證註解
 * 
 * <p>標記在 Controller 方法上，用於驗證當前用戶是否具有指定角色</p>
 * 
 * <p>使用範例：</p>
 * <pre>
 * &#64;RequireRole({"ADMIN", "STORE_OWNER"})
 * public void manageStore(...) { ... }
 * </pre>
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {

    /**
     * 允許的角色代碼列表
     * <p>用戶只需具有其中任一角色即可通過驗證</p>
     */
    String[] value();
}
