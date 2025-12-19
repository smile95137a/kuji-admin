package com.group.admin.constants;

/**
 * API 路徑常量
 * 
 * <p>統一管理所有 API 路徑前綴</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
public final class ApiPaths {
    
    private ApiPaths() {
        // 工具類不允許實例化
    }

    // ==================== 後台 API ====================
    
    /**
     * 後台 API 根路徑
     */
    public static final String ADMIN = "/admin";
    
    /**
     * 後台認證 API
     */
    public static final String ADMIN_AUTH = ADMIN + "/auth";
    
    /**
     * 後台使用者管理 API
     */
    public static final String ADMIN_USERS = ADMIN + "/users";
    
    /**
     * 後台店家管理 API
     */
    public static final String ADMIN_STORES = ADMIN + "/stores";
    
    /**
     * 後台商品管理 API
     */
    public static final String ADMIN_LOTTERIES = ADMIN + "/lotteries";
    
    /**
     * 後台角色管理 API
     */
    public static final String ADMIN_ROLES = ADMIN + "/roles";
    
    /**
     * 後台選單管理 API
     */
    public static final String ADMIN_MENUS = ADMIN + "/menus";

    // ==================== 前台 API ====================
    
    /**
     * 前台 API 根路徑
     */
    public static final String API = "/api";
    
    /**
     * 前台認證 API
     */
    public static final String API_AUTH = API + "/auth";
    
    /**
     * 前台使用者 API
     */
    public static final String API_USERS = API + "/users";
    
    /**
     * 前台店家 API
     */
    public static final String API_STORES = API + "/stores";
    
    /**
     * 前台商品 API
     */
    public static final String API_LOTTERIES = API + "/lotteries";
}
