package com.group.admin.aop;

import com.group.admin.annotation.RequiresPermission;
import com.group.admin.entity.RoleMenu;
import com.group.admin.example.RoleMenuExample;
import com.group.admin.mapper.RoleMenuMapper;
import com.group.admin.security.UserPrincipal;
import com.group.admin.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 攔截 {@link RequiresPermission} 註解的 AOP 切面
 *
 * <p>ROLE_ADMIN 直接放行；其他角色透過 PermissionService 查詢 role_menu 確認權限。</p>
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PermissionCheckAspect {

    private final PermissionService permissionService;

    @Around("@annotation(com.group.admin.annotation.RequiresPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequiresPermission annotation = method.getAnnotation(RequiresPermission.class);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            throw new AccessDeniedException("未認證");
        }

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        // ROLE_ADMIN 直接放行
        if (principal.getRoles() != null && principal.getRoles().contains("ROLE_ADMIN")) {
            log.debug("✅ ROLE_ADMIN 自動放行: {}", method.getName());
            return joinPoint.proceed();
        }

        String userId = principal.getUserId();
        String menuCode = annotation.menuCode();
        RequiresPermission.PermissionLevel level = annotation.level();

        boolean hasPermission = switch (level) {
            case VIEW -> permissionService.canView(userId, menuCode);
            case EDIT -> permissionService.canEdit(userId, menuCode);
            case DELETE -> permissionService.canDelete(userId, menuCode);
        };

        if (!hasPermission) {
            log.warn("❌ 用戶 {} 對選單 {} 沒有 {} 權限", userId, menuCode, level);
            throw new AccessDeniedException(
                    String.format("您沒有存取此功能的權限 (選單: %s, 操作: %s)", menuCode, level));
        }

        log.debug("✅ 用戶 {} 對選單 {} 的 {} 權限驗證通過", userId, menuCode, level);
        return joinPoint.proceed();
    }
}
