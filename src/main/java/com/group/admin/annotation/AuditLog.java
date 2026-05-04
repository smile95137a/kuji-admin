package com.group.admin.annotation;

import com.group.admin.enums.AuditLogType;

import java.lang.annotation.*;

/**
 * 標記需要記錄稽核日誌的 Controller 方法。
 * AuditLogAspect 會攔截並非同步寫入對應的 log_* 表。
 *
 * <pre>
 * 使用範例：
 * {@literal @}AuditLog(type = AuditLogType.ADMIN_ACTION, action = "CREATE", targetType = "LOTTERY")
 * public ResponseEntity<?> createLottery(...) { ... }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {

    /** 日誌分類（決定寫入哪張表） */
    AuditLogType type();

    /** 操作動作（如 CREATE / UPDATE / ON_SHELF / 後台登入） */
    String action() default "";

    /** 操作目標類型（如 LOTTERY / STORE / ADMIN_USER），AUTH/DRAW/RECHARGE/ORDER 類型可留空 */
    String targetType() default "";
}
