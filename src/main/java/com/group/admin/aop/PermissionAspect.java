package com.group.admin.aop;

import com.group.admin.annotation.RequirePermission;
import com.group.admin.annotation.RequireRole;
import com.group.admin.exception.BusinessException;
import com.group.admin.mapper.AdminUserMapper;
import com.group.admin.entity.AdminUser;
import com.group.admin.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 權限驗證 AOP
 * 
 * <p>攔截標記 {@link RequirePermission} 和 {@link RequireRole} 註解的方法，
 * 執行權限檢查，若無權限則拋出 {@link BusinessException}</p>
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {

    private final PermissionService permissionService;
    private final AdminUserMapper adminUserMapper;

    /**
     * 檢查選單權限
     */
    @Before("@annotation(com.group.admin.annotation.RequirePermission)")
    public void checkPermission(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequirePermission annotation = method.getAnnotation(RequirePermission.class);

        String adminUserId = getCurrentAdminUserId();
        if (adminUserId == null) {
            log.warn("無法取得當前用戶ID，權限檢查失敗");
            throw new BusinessException("AUTH_REQUIRED", "請先登入");
        }

        String menuCode = annotation.menuCode();
        RequirePermission.PermissionType permissionType = annotation.permission();

        boolean hasPermission;
        switch (permissionType) {
            case VIEW:
                hasPermission = permissionService.canView(adminUserId, menuCode);
                break;
            case EDIT:
                hasPermission = permissionService.canEdit(adminUserId, menuCode);
                break;
            case DELETE:
                hasPermission = permissionService.canDelete(adminUserId, menuCode);
                break;
            default:
                hasPermission = false;
        }

        if (!hasPermission) {
            log.warn("用戶 {} 對選單 {} 沒有 {} 權限", adminUserId, menuCode, permissionType);
            throw new BusinessException("PERMISSION_DENIED", 
                    String.format("您沒有存取此功能的權限 (選單: %s, 操作: %s)", menuCode, permissionType));
        }

        log.debug("用戶 {} 對選單 {} 的 {} 權限驗證通過", adminUserId, menuCode, permissionType);
    }

    /**
     * 檢查角色權限
     */
    @Before("@annotation(com.group.admin.annotation.RequireRole)")
    public void checkRole(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequireRole annotation = method.getAnnotation(RequireRole.class);

        String adminUserId = getCurrentAdminUserId();
        if (adminUserId == null) {
            log.warn("無法取得當前用戶ID，角色檢查失敗");
            throw new BusinessException("AUTH_REQUIRED", "請先登入");
        }

        String[] requiredRoles = annotation.value();
        List<String> userRoles = permissionService.getUserRoleCodes(adminUserId);

        boolean hasRole = false;
        for (String requiredRole : requiredRoles) {
            if (userRoles.contains(requiredRole)) {
                hasRole = true;
                break;
            }
        }

        if (!hasRole) {
            log.warn("用戶 {} 沒有所需角色: {}", adminUserId, String.join(", ", requiredRoles));
            throw new BusinessException("ROLE_DENIED", 
                    String.format("您沒有存取此功能的權限 (需要角色: %s)", String.join(" 或 ", requiredRoles)));
        }

        log.debug("用戶 {} 角色驗證通過", adminUserId);
    }

    /**
     * 取得當前登入的管理者用戶ID
     */
    private String getCurrentAdminUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof String) {
            String username = (String) principal;
            // 使用 Example 模式查詢
            com.group.admin.example.AdminUserExample example = new com.group.admin.example.AdminUserExample();
            example.createCriteria().andUsernameEqualTo(username);
            java.util.List<AdminUser> users = adminUserMapper.selectByExample(example);
            if (!users.isEmpty()) {
                return users.get(0).getId();
            }
            // 嘗試用 email 查詢
            example.clear();
            example.createCriteria().andEmailEqualTo(username);
            users = adminUserMapper.selectByExample(example);
            if (!users.isEmpty()) {
                return users.get(0).getId();
            }
        }

        return null;
    }
}
