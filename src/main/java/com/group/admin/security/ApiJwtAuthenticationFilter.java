package com.group.admin.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group.admin.constants.ErrorCodes;
import com.group.admin.entity.AdminUser;
import com.group.admin.entity.AdminUserRole;
import com.group.admin.entity.Role;
import com.group.admin.entity.User;
import com.group.admin.example.AdminUserExample;
import com.group.admin.example.AdminUserRoleExample;
import com.group.admin.example.UserExample;
import com.group.admin.mapper.AdminUserMapper;
import com.group.admin.mapper.AdminUserRoleMapper;
import com.group.admin.mapper.RoleMapper;
import com.group.admin.mapper.UserMapper;
import com.group.admin.result.ApiResponse;
import com.group.admin.service.UserTokenBlacklistService;
import com.group.admin.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiJwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String FORCE_CHANGE_PASSWORD_MARKER = "FORCE_CHANGE_PASSWORD";

    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final AdminUserMapper adminUserMapper;
    private final AdminUserRoleMapper adminUserRoleMapper;
    private final RoleMapper roleMapper;
    private final UserTokenBlacklistService userTokenBlacklistService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
log.info("🔥 [ApiJwtFilter] RUN: requestURI={}, servletPath={}, authHeader={}",
        request.getRequestURI(),
        request.getServletPath(),
        request.getHeader("Authorization") != null ? "HAS_AUTH" : "NO_AUTH");
        String path = request.getServletPath();

        if (path.startsWith("/auth/") || path.startsWith("/admin/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractToken(request);
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }
        

        if (!jwtUtil.validateToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        String email = jwtUtil.getUsername(token);
        String userType = jwtUtil.getUserType(token);
        if (email == null || email.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        boolean shouldContinue;
        if ("admin".equalsIgnoreCase(userType)) {
            shouldContinue = authenticateAdmin(request, response, email);
        } else {
            shouldContinue = authenticateUser(request, response, path, email, token);
        }

        if (!shouldContinue) {
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean authenticateAdmin(HttpServletRequest request, HttpServletResponse response, String email)
            throws IOException {
        try {
            AdminUserExample example = new AdminUserExample();
            example.createCriteria().andEmailEqualTo(email);
            List<AdminUser> adminUsers = adminUserMapper.selectByExample(example);
            if (adminUsers.isEmpty()) {
                return true;
            }

            AdminUser adminUser = adminUsers.get(0);

            AdminUserRoleExample roleExample = new AdminUserRoleExample();
            roleExample.createCriteria().andAdminUserIdEqualTo(adminUser.getId());
            List<AdminUserRole> userRoles = adminUserRoleMapper.selectByExample(roleExample);

            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            List<String> roleNames = new ArrayList<>();
            for (AdminUserRole userRole : userRoles) {
                Role role = roleMapper.selectByPrimaryKey(userRole.getRoleId());
                if (role == null) {
                    continue;
                }
                String roleCode = role.getCode();
                roleNames.add(roleCode);
                authorities.add(new SimpleGrantedAuthority(
                        roleCode.startsWith("ROLE_") ? roleCode : "ROLE_" + roleCode));
            }

            UserPrincipal principal = UserPrincipal.builder()
                    .userId(adminUser.getId())
                    .username(adminUser.getEmail())
                    .password(adminUser.getPassword())
                    .roles(roleNames)
                    .isAdmin(true)
                    .authorities(authorities)
                    .adminUser(adminUser)
                    .build();

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return true;
        } catch (Exception e) {
            log.error("後台 JWT 認證失敗: {}", e.getMessage(), e);
            SecurityContextHolder.clearContext();
            writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    ErrorCodes.AUTH_TOKEN_INVALID,
                    "登入資訊驗證失敗，請重新登入");
            return false;
        }
    }

    private boolean authenticateUser(HttpServletRequest request, HttpServletResponse response,
        String path, String email, String token) throws IOException {
    try {
        String userId = jwtUtil.getUserId(token);
        Integer tokenGen = getTokenGen(token);

        if (userId != null && tokenGen != null) {
            int currentGen = userTokenBlacklistService.getBlacklistGen(userId);
            if (tokenGen < currentGen) {
                writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED,
                        ErrorCodes.AUTH_TOKEN_REVOKED,
                        "Token 已失效，請重新登入");
                return false;
            }
        }

        UserExample example = new UserExample();
        example.createCriteria().andEmailEqualTo(email);
        List<User> users = userMapper.selectByExample(example);

        if (users.isEmpty()) {
            log.warn("前台使用者不存在: email={}", email);
            return true;
        }

        User user = users.get(0);

        if ("INACTIVE".equals(user.getStatus()) || "SUSPENDED".equals(user.getStatus())) {
            writeJsonError(response, HttpServletResponse.SC_FORBIDDEN,
                    ErrorCodes.AUTH_ACCOUNT_DISABLED,
                    "帳號已停用或暫停使用，請聯繫客服");
            return false;
        }

        if ("DELETED".equals(user.getStatus())) {
            writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    ErrorCodes.AUTH_INVALID_CREDENTIALS,
                    "帳號或密碼錯誤");
            return false;
        }

        if (user.getEmailVerified() == null || user.getEmailVerified() == 0) {
            writeJsonError(response, HttpServletResponse.SC_FORBIDDEN,
                    ErrorCodes.COMMON_VALIDATION_ERROR,
                    "請先完成 Email 驗證");
            return false;
        }

        if (requiresForceChangePassword(user) && !path.equals("/user/me/change-password")) {
            writeJsonError(response, HttpServletResponse.SC_FORBIDDEN,
                    ErrorCodes.AUTH_FORCE_CHANGE_PASSWORD,
                    "需先修改密碼後才能繼續操作");
            return false;
        }

        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_USER")
        );

        UserPrincipal principal = UserPrincipal.builder()
                .userId(user.getId())
                .username(user.getEmail())
                .roles(List.of("USER"))
                .authorities(authorities)
                .isAdmin(false)
                .isUser(true)
                .user(user)
                .build();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, authorities);

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.info("前台 JWT 認證成功: email={}, userId={}, authorities={}",
                email, user.getId(), authorities);

        return true;
    } catch (Exception e) {
        log.error("前台 JWT 認證失敗: {}", e.getMessage(), e);
        SecurityContextHolder.clearContext();
        writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED,
                ErrorCodes.AUTH_TOKEN_INVALID,
                "登入資訊驗證失敗，請重新登入");
        return false;
    }
}
    private boolean requiresForceChangePassword(User user) {
        return user != null && FORCE_CHANGE_PASSWORD_MARKER.equals(user.getPasswordResetToken());
    }

    private void writeJsonError(HttpServletResponse response, int status, String code, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.error(code, message)));
    }

private Integer getTokenGen(String token) {
    try {
        Long gen = jwtUtil.getGen(token);
        return gen != null ? gen.intValue() : null;
    } catch (Exception e) {
        return null;
    }
}

private String extractToken(HttpServletRequest request) {
    String header = request.getHeader("Authorization");
    if (header != null && header.startsWith("Bearer ")) {
        return header.substring(7);
    }
    return null;
}
}
