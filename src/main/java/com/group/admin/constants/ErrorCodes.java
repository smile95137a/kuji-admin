package com.group.admin.constants;

/**
 * 錯誤碼常量
 * 
 * <p>統一管理所有錯誤碼，格式：{模組}_{錯誤類型}_{編號}</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
public final class ErrorCodes {
    
    private ErrorCodes() {
        // 工具類不允許實例化
    }

    // ==================== 通用錯誤 (COMMON) ====================
    
    /**
     * 參數驗證失敗
     */
    public static final String COMMON_VALIDATION_ERROR = "COMMON_VALIDATION_001";
    
    /**
     * 資源不存在
     */
    public static final String COMMON_NOT_FOUND = "COMMON_NOT_FOUND_001";
    
    /**
     * 系統內部錯誤
     */
    public static final String COMMON_INTERNAL_ERROR = "COMMON_INTERNAL_001";
    
    /**
     * 操作被拒絕（無權限）
     */
    public static final String COMMON_ACCESS_DENIED = "COMMON_ACCESS_001";

    // ==================== 認證錯誤 (AUTH) ====================
    
    /**
     * 帳號或密碼錯誤
     */
    public static final String AUTH_INVALID_CREDENTIALS = "AUTH_INVALID_001";
    
    /**
     * Token 無效或已過期
     */
    public static final String AUTH_TOKEN_INVALID = "AUTH_TOKEN_001";
    
    /**
     * Token 已被撤銷
     */
    public static final String AUTH_TOKEN_REVOKED = "AUTH_TOKEN_002";
    
    /**
     * 需要首次登入修改密碼
     */
    public static final String AUTH_FORCE_CHANGE_PASSWORD = "AUTH_PASSWORD_001";
    
    /**
     * 帳號已停用
     */
    public static final String AUTH_ACCOUNT_DISABLED = "AUTH_ACCOUNT_001";
    
    /**
     * 帳號待啟用
     */
    public static final String AUTH_ACCOUNT_PENDING = "AUTH_ACCOUNT_002";

    /**
     * 帳號已鎖定
     */
    public static final String AUTH_ACCOUNT_LOCKED = "AUTH_ACCOUNT_003";

    // ==================== 使用者錯誤 (USER) ====================
    
    /**
     * Email 已存在
     */
    public static final String USER_EMAIL_EXISTS = "USER_EMAIL_001";
    
    /**
     * 使用者不存在
     */
    public static final String USER_NOT_FOUND = "USER_NOT_FOUND_001";
    
    /**
     * 密碼格式不符
     */
    public static final String USER_PASSWORD_INVALID = "USER_PASSWORD_001";
    
    /**
     * 舊密碼錯誤
     */
    public static final String USER_OLD_PASSWORD_WRONG = "USER_PASSWORD_002";

    // ==================== 店家錯誤 (STORE) ====================
    
    /**
     * 店家不存在
     */
    public static final String STORE_NOT_FOUND = "STORE_NOT_FOUND_001";
    
    /**
     * 店家已停用
     */
    public static final String STORE_DISABLED = "STORE_STATUS_001";
    
    /**
     * 無權操作此店家
     */
    public static final String STORE_ACCESS_DENIED = "STORE_ACCESS_001";

    // ==================== 商品錯誤 (LOTTERY) ====================
    
    /**
     * 商品不存在
     */
    public static final String LOTTERY_NOT_FOUND = "LOTTERY_NOT_FOUND_001";
    
    /**
     * 商品已下架
     */
    public static final String LOTTERY_OFF_SHELF = "LOTTERY_STATUS_001";
    
    /**
     * 獎項已抽完
     */
    public static final String LOTTERY_SOLD_OUT = "LOTTERY_STOCK_001";
    
    /**
     * 點數不足
     */
    public static final String LOTTERY_INSUFFICIENT_POINTS = "LOTTERY_POINTS_001";
    
    /**
     * 商品被他人鎖定中
     */
    public static final String LOTTERY_LOCKED = "LOTTERY_LOCK_001";
    
    /**
     * 號碼已被選取
     */
    public static final String LOTTERY_NUMBER_TAKEN = "LOTTERY_NUMBER_001";

    // ==================== 訂單錯誤 (ORDER) ====================
    
    /**
     * 訂單不存在
     */
    public static final String ORDER_NOT_FOUND = "ORDER_NOT_FOUND_001";
    
    /**
     * 訂單狀態不允許此操作
     */
    public static final String ORDER_STATUS_INVALID = "ORDER_STATUS_001";
}
