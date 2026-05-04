package com.group.admin.aop;

import com.group.admin.annotation.AuditLog;
import com.group.admin.enums.AuditLogType;
import com.group.admin.security.UserPrincipal;
import com.group.admin.service.AuditLogService;
import com.group.admin.util.AuditContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 稽核日誌 AOP 切面。
 * 攔截所有帶有 {@link AuditLog} 的 Controller 方法，
 * 執行完成（或例外）後依 type 非同步寫入對應的 log_* 分類表。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogService auditLogService;

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        long start = System.currentTimeMillis();
        String result = "SUCCESS";
        String errorMessage = null;

        try {
            return joinPoint.proceed();
        } catch (Throwable t) {
            result = "FAIL";
            errorMessage = t.getMessage();
            throw t;  // 必須重新拋出，不能吞例外
        } finally {
            long durationMs = System.currentTimeMillis() - start;
            String beforeSnapshot = AuditContext.getBefore();
            String afterSnapshot  = AuditContext.getAfter();
            AuditContext.clear();  // ⚠️ 必須在 finally 清理，防止 ThreadLocal 洩漏

            try {
                dispatch(auditLog, result, errorMessage, beforeSnapshot, afterSnapshot, (int) durationMs);
            } catch (Exception e) {
                log.warn("⚠️ [AuditLogAspect] 日誌分派失敗: {}", e.getMessage());
            }
        }
    }

    // ---------------------------------------------------------------
    // 私有：依 type 分流到對應 service 方法
    // ---------------------------------------------------------------

    private void dispatch(AuditLog auditLog, String result, String errorMessage,
                          String beforeSnapshot, String afterSnapshot, int durationMs) {
        AuditLogType type = auditLog.type();

        if (type == AuditLogType.AUTH) {
            dispatchAuth(auditLog, result, errorMessage);
        } else if (type == AuditLogType.ADMIN_ACTION) {
            dispatchAdminAction(auditLog, result, errorMessage, beforeSnapshot, afterSnapshot);
        }
        // DRAW / RECHARGE / ORDER 由各自業務 Service 直接呼叫，AOP 不處理
    }

    private void dispatchAuth(AuditLog auditLog, String result, String errorMessage) {
        // 認證事件：在 SecurityContext 尚未建立時（如登入失敗），userId 可能為 null
        String userId    = getCurrentUserId();
        String userType  = "ADMIN";  // 目前只切入後台登入，前台另行擴充
        String email     = getCurrentUsername();
        String loginMethod = auditLog.action().isEmpty() ? "EMAIL" : auditLog.action();
        String ip        = getClientIp();
        String userAgent = getUserAgent();

        auditLogService.logAuth(userId, userType, email, loginMethod, result, errorMessage, ip, userAgent);
    }

    private void dispatchAdminAction(AuditLog auditLog, String result, String errorMessage,
                                     String beforeSnapshot, String afterSnapshot) {
        String adminId    = getCurrentUserId();
        String adminEmail = getCurrentUsername();
        String adminRole  = getCurrentPrimaryRole();
        String targetType = auditLog.targetType();
        String action     = auditLog.action();
        String ip         = getClientIp();

        // targetId / targetName 由業務層透過 AuditContext 傳入（可選）
        String targetId   = null;
        String targetName = null;

        auditLogService.logAdminAction(adminId, adminEmail, adminRole,
                targetType, targetId, targetName,
                action, beforeSnapshot, afterSnapshot,
                result, errorMessage, ip);
    }

    // ---------------------------------------------------------------
    // SecurityContext 工具方法
    // ---------------------------------------------------------------

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof UserPrincipal up) return up.getUserId();
        return null;
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof UserPrincipal up) return up.getUsername();
        return auth.getName();
    }

    private String getCurrentPrimaryRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        List<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        return roles.isEmpty() ? null : roles.get(0);
    }

    // ---------------------------------------------------------------
    // HTTP 請求工具方法
    // ---------------------------------------------------------------

    private String getClientIp() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return null;
            HttpServletRequest request = attrs.getRequest();
            String ip = request.getHeader("X-Forwarded-For");
            if (ip != null && !ip.isBlank()) {
                // X-Forwarded-For 可能有多個 IP，取第一個（最接近客戶端）
                return ip.split(",")[0].trim();
            }
            ip = request.getHeader("X-Real-IP");
            if (ip != null && !ip.isBlank()) return ip.trim();
            return request.getRemoteAddr();
        } catch (Exception e) {
            return null;
        }
    }

    private String getUserAgent() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return null;
            return attrs.getRequest().getHeader("User-Agent");
        } catch (Exception e) {
            return null;
        }
    }
}
