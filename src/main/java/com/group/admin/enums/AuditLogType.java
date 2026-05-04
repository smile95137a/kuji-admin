package com.group.admin.enums;

/**
 * 稽核日誌分類，對應 5 張 log_* 分類表。
 */
public enum AuditLogType {
    /** 認證事件（登入/登出/Token 刷新） → log_auth */
    AUTH,
    /** 抽獎事件 → log_draw */
    DRAW,
    /** 儲值事件 → log_recharge */
    RECHARGE,
    /** 訂單操作事件 → log_order */
    ORDER,
    /** 後台管理操作（CRUD、上下架、帳號管理等） → log_admin_action */
    ADMIN_ACTION
}
