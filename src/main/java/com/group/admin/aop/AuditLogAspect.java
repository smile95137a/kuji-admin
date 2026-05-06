package com.group.admin.aop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private final ObjectMapper objectMapper;

    // ---------------------------------------------------------------
    // 自動攔截：admin 包下所有未標記 @AuditLog 的寫入操作
    // 排除 DebugController 與 AdminSystemLogController（查詢用，不需記錄）
    // ---------------------------------------------------------------

    @Around("execution(* com.group.admin.controller.admin..*(..)) " +
            "&& !@annotation(com.group.admin.annotation.AuditLog) " +
            "&& !within(com.group.admin.controller.admin.DebugController) " +
            "&& !within(com.group.admin.controller.admin.AdminSystemLogController)")
    public Object autoAround(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) return joinPoint.proceed();

        String httpMethod = request.getMethod().toUpperCase();
        // 只記錄寫入操作，GET/HEAD/OPTIONS 直接放行
        if ("GET".equals(httpMethod) || "HEAD".equals(httpMethod) || "OPTIONS".equals(httpMethod)) {
            return joinPoint.proceed();
        }

        String result = "SUCCESS";
        String errorMessage = null;
        Object response = null;
        String beforeSnapshot = buildRequestSnapshot(joinPoint);

        try {
            response = joinPoint.proceed();
            return response;
        } catch (Throwable t) {
            result = "FAIL";
            errorMessage = t.getMessage();
            throw t;
        } finally {
            String afterSnapshot = buildResponseSnapshot(response, result, joinPoint);
            try {
                String targetType = inferTargetTypeFromController(joinPoint);
                String action     = inferActionFromRequest(httpMethod, joinPoint);
                String adminId    = getCurrentUserId();
                String adminEmail = getCurrentUserEmailOrUsername();
                String adminRole  = getCurrentPrimaryRole();
                String ip         = getClientIp();
                String targetId   = inferTargetId(joinPoint, response);
                String targetName = inferTargetName(joinPoint, response);

                auditLogService.logAdminAction(adminId, adminEmail, adminRole,
                        targetType, targetId, targetName,
                        action, beforeSnapshot, afterSnapshot,
                        result, errorMessage, ip);
            } catch (Exception e) {
                log.warn("⚠️ [AutoAudit] 自動日誌寫入失敗: {}", e.getMessage());
            } finally {
                AuditContext.clear();
            }
        }
    }

    // ---------------------------------------------------------------
    // 手動標記：帶有 @AuditLog 的方法
    // ---------------------------------------------------------------

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        long start = System.currentTimeMillis();
        String result = "SUCCESS";
        String errorMessage = null;
        Object response = null;
        String fallbackBeforeSnapshot = buildRequestSnapshot(joinPoint);

        try {
            response = joinPoint.proceed();
            return response;
        } catch (Throwable t) {
            result = "FAIL";
            errorMessage = t.getMessage();
            throw t;  // 必須重新拋出，不能吞例外
        } finally {
            long durationMs = System.currentTimeMillis() - start;
            String beforeSnapshot = firstNonBlank(AuditContext.getBefore(), fallbackBeforeSnapshot);
            String afterSnapshot  = firstNonBlank(AuditContext.getAfter(), buildResponseSnapshot(response, result, joinPoint));

            try {
                dispatch(auditLog, result, errorMessage, beforeSnapshot, afterSnapshot, (int) durationMs, joinPoint, response);
            } catch (Exception e) {
                log.warn("⚠️ [AuditLogAspect] 日誌分派失敗: {}", e.getMessage());
            } finally {
                AuditContext.clear();  // ⚠️ 必須在 finally 清理，防止 ThreadLocal 洩漏
            }
        }
    }

    // ---------------------------------------------------------------
    // 私有：依 type 分流到對應 service 方法
    // ---------------------------------------------------------------

    private void dispatch(AuditLog auditLog, String result, String errorMessage,
                          String beforeSnapshot, String afterSnapshot, int durationMs,
                          ProceedingJoinPoint joinPoint, Object response) {
        AuditLogType type = auditLog.type();

        if (type == AuditLogType.AUTH) {
            dispatchAuth(auditLog, result, errorMessage);
        } else if (type == AuditLogType.ADMIN_ACTION) {
            dispatchAdminAction(auditLog, result, errorMessage, beforeSnapshot, afterSnapshot, joinPoint, response);
        }
        // DRAW / RECHARGE / ORDER 由各自業務 Service 直接呼叫，AOP 不處理
    }

    private void dispatchAuth(AuditLog auditLog, String result, String errorMessage) {
        // 認證事件：在 SecurityContext 尚未建立時（如登入失敗），userId 可能為 null
        String userId    = firstNonBlank(AuditContext.getAuthUserId(), getCurrentUserId());
        String userType  = firstNonBlank(AuditContext.getAuthUserType(), "ADMIN");
        String email     = firstNonBlank(
                AuditContext.getAuthResolvedEmail(),
                AuditContext.getAuthAttemptedUsername(),
                AuditContext.getAuthResolvedUsername(),
                getCurrentUserEmailOrUsername()
        );
        String loginMethod = auditLog.action().isEmpty() ? "EMAIL" : auditLog.action();
        String ip        = getClientIp();
        String userAgent = getUserAgent();

        auditLogService.logAuth(userId, userType, email, loginMethod, result, errorMessage, ip, userAgent);
    }

    private void dispatchAdminAction(AuditLog auditLog, String result, String errorMessage,
                                     String beforeSnapshot, String afterSnapshot,
                                     ProceedingJoinPoint joinPoint, Object response) {
        String adminId    = getCurrentUserId();
        String adminEmail = getCurrentUserEmailOrUsername();
        String adminRole  = getCurrentPrimaryRole();
        String targetType = auditLog.targetType();
        String action     = auditLog.action();
        String ip         = getClientIp();

        String targetId   = firstNonBlank(AuditContext.getTargetId(), inferTargetId(joinPoint, response));
        String targetName = firstNonBlank(AuditContext.getTargetName(), inferTargetName(joinPoint, response));

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
        if (Objects.equals("anonymousUser", principal)) return null;
        if (principal instanceof UserPrincipal up) return up.getUserId();
        return null;
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        Object principal = auth.getPrincipal();
        if (Objects.equals("anonymousUser", principal)) return null;
        if (principal instanceof UserPrincipal up) return up.getUsername();
        return Objects.equals("anonymousUser", auth.getName()) ? null : auth.getName();
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

    private String getCurrentUserEmailOrUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        Object principal = auth.getPrincipal();
        if (Objects.equals("anonymousUser", principal)) return null;
        if (principal instanceof UserPrincipal up) {
            String email = extractProperty(up.getAdminUser(), "getEmail");
            return isNotBlank(email) ? email : up.getUsername();
        }
        return getCurrentUsername();
    }

    private String buildRequestSnapshot(ProceedingJoinPoint joinPoint) {
        try {
            Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            Object[] args = joinPoint.getArgs();
            Map<String, Object> snapshot = new LinkedHashMap<>();
            Map<String, Object> pathVariables = new LinkedHashMap<>();
            List<Object> requestBodies = new java.util.ArrayList<>();

            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (arg == null) {
                    continue;
                }
                for (Annotation annotation : parameterAnnotations[i]) {
                    if (annotation instanceof PathVariable pathVariable) {
                        String key = isNotBlank(pathVariable.value()) ? pathVariable.value() : pathVariable.name();
                        if (!isNotBlank(key)) {
                            key = "arg" + i;
                        }
                        pathVariables.put(key, arg);
                    } else if (annotation instanceof RequestBody) {
                        requestBodies.add(arg);
                    }
                }
            }

            if (!pathVariables.isEmpty()) {
                snapshot.put("pathVariables", pathVariables);
            }
            if (!requestBodies.isEmpty()) {
                List<Object> sanitizedBodies = requestBodies.stream()
                        .map(this::sanitizeForAudit)
                        .collect(Collectors.toCollection(ArrayList::new));
                snapshot.put("requestBody", sanitizedBodies.size() == 1 ? sanitizedBodies.get(0) : sanitizedBodies);
            }

            return snapshot.isEmpty() ? null : objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException e) {
            return null;
        } catch (Exception e) {
            log.debug("建立請求快照失敗: {}", e.getMessage());
            return null;
        }
    }

    private String buildResponseSnapshot(Object response, String result, ProceedingJoinPoint joinPoint) {
        try {
            Object body = unwrapResponseBody(response);
            if (body != null) {
                return objectMapper.writeValueAsString(sanitizeForAudit(body));
            }

            String targetId = inferTargetId(joinPoint, response);
            String targetName = inferTargetName(joinPoint, response);
            Map<String, Object> fallback = new LinkedHashMap<>();
            fallback.put("result", result);
            if (isNotBlank(targetId)) {
                fallback.put("targetId", targetId);
            }
            if (isNotBlank(targetName)) {
                fallback.put("targetName", targetName);
            }
            return fallback.size() > 1 ? objectMapper.writeValueAsString(fallback) : null;
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private String inferTargetId(ProceedingJoinPoint joinPoint, Object response) {
        String fromResponse = extractProperty(unwrapResponseBody(response), "getId");
        if (isNotBlank(fromResponse)) {
            return fromResponse;
        }

        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg == null) {
                continue;
            }
            for (Annotation annotation : parameterAnnotations[i]) {
                if (annotation instanceof PathVariable) {
                    return String.valueOf(arg);
                }
            }
        }

        for (Object arg : args) {
            String fromArg = extractProperty(arg, "getId");
            if (isNotBlank(fromArg)) {
                return fromArg;
            }
        }
        return null;
    }

    private String inferTargetName(ProceedingJoinPoint joinPoint, Object response) {
        Object body = unwrapResponseBody(response);
        String fromResponse = extractFirstNonBlankProperty(body,
                "getTitle", "getStoreName", "getDisplayName", "getUsername", "getEmail", "getName");
        if (isNotBlank(fromResponse)) {
            return fromResponse;
        }

        for (Object arg : joinPoint.getArgs()) {
            String fromArg = extractFirstNonBlankProperty(arg,
                    "getTitle", "getStoreName", "getDisplayName", "getUsername", "getEmail", "getName");
            if (isNotBlank(fromArg)) {
                return fromArg;
            }
        }
        return null;
    }

    private Object unwrapResponseBody(Object response) {
        if (response instanceof ResponseEntity<?> responseEntity) {
            return responseEntity.getBody();
        }
        return response;
    }

    private String extractFirstNonBlankProperty(Object source, String... methods) {
        for (String method : methods) {
            String value = extractProperty(source, method);
            if (isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }

    private String extractProperty(Object source, String methodName) {
        if (source == null || methodName == null) {
            return null;
        }
        try {
            Method method = source.getClass().getMethod(methodName);
            Object value = method.invoke(source);
            return value == null ? null : String.valueOf(value);
        } catch (Exception e) {
            return null;
        }
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }

    // ---------------------------------------------------------------
    // 自動推斷 targetType / action 的輔助方法
    // ---------------------------------------------------------------

    /**
     * 從 Controller 類別名稱推斷 targetType。
     * 去除 "Admin" 前綴與 "Controller" 後綴，再轉 UPPER_SNAKE_CASE。
     * 例如：AdminFrontendUserController → FRONTEND_USER
     */
    private String inferTargetTypeFromController(ProceedingJoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String name = className
                .replaceFirst("^Admin", "")
                .replaceFirst("Controller$", "");
        return name.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase();
    }

    /**
     * 從 HTTP Method + Java method 名稱推斷語意動作。
     * method 名稱中的動詞關鍵字優先；最後回退到 HTTP Method 對應。
     */
    private String inferActionFromRequest(String httpMethod, ProceedingJoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName().toLowerCase();
        if (methodName.contains("unpublish"))                          return "UNPUBLISH";
        if (methodName.contains("publish"))                            return "PUBLISH";
        if (methodName.contains("activate") || methodName.contains("enable"))  return "ENABLE";
        if (methodName.contains("deactivate") || methodName.contains("disable")) return "DISABLE";
        if (methodName.contains("suspend"))                            return "SUSPEND";
        if (methodName.contains("unlock"))                             return "UNLOCK";
        if (methodName.contains("cancel"))                             return "CANCEL";
        if (methodName.contains("complete"))                           return "COMPLETE";
        if (methodName.contains("prepare"))                            return "PREPARE";
        if (methodName.contains("ship"))                               return "SHIP";
        if (methodName.contains("reset"))                              return "RESET";
        if (methodName.contains("broadcast"))                          return "BROADCAST";
        if (methodName.contains("adjust"))                             return "ADJUST";
        if (methodName.contains("upload"))                             return "UPLOAD";
        if (methodName.contains("delete"))                             return "DELETE";
        return switch (httpMethod) {
            case "POST"  -> "CREATE";
            case "PUT"   -> "UPDATE";
            case "PATCH" -> "UPDATE";
            case "DELETE" -> "DELETE";
            default -> httpMethod;
        };
    }

    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attrs != null ? attrs.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private Object sanitizeForAudit(Object source) {
        if (source == null) {
            return null;
        }
        try {
            Object normalized = objectMapper.convertValue(source, Object.class);
            return sanitizeNode(null, normalized);
        } catch (IllegalArgumentException e) {
            return String.valueOf(source);
        }
    }

    @SuppressWarnings("unchecked")
    private Object sanitizeNode(String key, Object value) {
        if (value == null) {
            return null;
        }
        if (isSensitiveKey(key)) {
            return "***";
        }
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> sanitized = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String childKey = entry.getKey() == null ? null : String.valueOf(entry.getKey());
                sanitized.put(childKey, sanitizeNode(childKey, entry.getValue()));
            }
            return sanitized;
        }
        if (value instanceof List<?> list) {
            List<Object> sanitized = new ArrayList<>(list.size());
            for (Object item : list) {
                sanitized.add(sanitizeNode(key, item));
            }
            return sanitized;
        }
        return value;
    }

    private boolean isSensitiveKey(String key) {
        if (!isNotBlank(key)) {
            return false;
        }
        String normalized = key.toLowerCase();
        return normalized.contains("password")
                || normalized.contains("token")
                || normalized.contains("secret")
                || normalized.contains("hashkey")
                || normalized.contains("hashiv")
                || normalized.contains("credential");
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
}
