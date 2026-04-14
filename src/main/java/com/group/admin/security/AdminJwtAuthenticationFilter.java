package com.group.admin.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.group.admin.entity.AdminUser;
import com.group.admin.entity.AdminUserRole;
import com.group.admin.entity.Role;
import com.group.admin.entity.StoreUser;
import com.group.admin.example.AdminUserExample;
import com.group.admin.example.AdminUserRoleExample;
import com.group.admin.example.StoreUserExample;
import com.group.admin.mapper.AdminUserMapper;
import com.group.admin.mapper.AdminUserRoleMapper;
import com.group.admin.mapper.RoleMapper;
import com.group.admin.mapper.StoreUserMapper;
import com.group.admin.service.TokenBlacklistService;
import com.group.admin.util.JwtUtil;

import org.springframework.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 後台 JWT 認證過濾器
 * 僅處理 /admin/** 路徑的請求
 * 支援後台 AdminUser 的認證與 Token 黑名單檢查
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminJwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final AdminUserMapper adminUserMapper;
    private final AdminUserRoleMapper adminUserRoleMapper;
    private final RoleMapper roleMapper;
    private final StoreUserMapper storeUserMapper;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();
        log.debug("🔍 [AdminJwtFilter] path={}", path);

        if (!path.startsWith("/admin/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Login endpoint: no token needed
        if (path.equals("/admin/auth/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        boolean isAuthPath = path.startsWith("/admin/auth/");

        String token = extractToken(request);
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!jwtUtil.validateToken(token)) {
            log.warn("⚠️  [AdminJwtFilter] Token 驗證失敗");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String username = jwtUtil.getUsername(token);

            AdminUserExample example = new AdminUserExample();
            example.createCriteria().andUsernameEqualTo(username);
            List<AdminUser> adminUsers = adminUserMapper.selectByExample(example);

            if (adminUsers.isEmpty()) {
                log.warn("❌ [AdminJwtFilter] 找不到管理員: {}", username);
                filterChain.doFilter(request, response);
                return;
            }

            AdminUser adminUser = adminUsers.get(0);

            // Check token blacklist (gen-based)
            Long tokenGen = jwtUtil.getGen(token);
            if (tokenGen != null && tokenBlacklistService.isBlacklisted(adminUser.getId(), tokenGen)) {
                log.warn("🚫 [AdminJwtFilter] Token 已失效 (blacklisted): userId={}", adminUser.getId());
                writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token has been invalidated");
                return;
            }

            // Build storeIds
            StoreUserExample storeUserExample = new StoreUserExample();
            storeUserExample.createCriteria().andAdminUserIdEqualTo(adminUser.getId());
            List<StoreUser> storeUsers = storeUserMapper.selectByExample(storeUserExample);
            List<String> storeIds = new ArrayList<>();
            for (StoreUser su : storeUsers) {
                storeIds.add(su.getStoreId());
            }

            // Build roles
            AdminUserRoleExample roleExample = new AdminUserRoleExample();
            roleExample.createCriteria().andAdminUserIdEqualTo(adminUser.getId());
            List<AdminUserRole> userRoles = adminUserRoleMapper.selectByExample(roleExample);
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            List<String> roleNames = new ArrayList<>();
            for (AdminUserRole ur : userRoles) {
                Role role = roleMapper.selectByPrimaryKey(ur.getRoleId());
                if (role != null) {
                    roleNames.add(role.getCode());
                    authorities.add(new SimpleGrantedAuthority(role.getCode()));
                }
            }

            UserPrincipal principal = UserPrincipal.builder()
                    .userId(adminUser.getId())
                    .username(adminUser.getUsername())
                    .password(adminUser.getPassword())
                    .isAdmin(true)
                    .storeIds(storeIds)
                    .authorities(authorities)
                    .adminUser(adminUser)
                    .build();

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("✅ [AdminJwtFilter] 認證成功: {} (角色: {})", username, roleNames);

            // For non-auth paths: check forceChangePassword
            if (!isAuthPath && Boolean.TRUE.equals(adminUser.getForceChangePassword())) {
                log.warn("⚠️  [AdminJwtFilter] 強制修改密碼 (forceChangePassword=true): userId={}", adminUser.getId());
                writeJsonError(response, HttpServletResponse.SC_FORBIDDEN,
                        "Password change required before accessing this resource");
                return;
            }

        } catch (Exception e) {
            log.error("❌ [AdminJwtFilter] 認證失敗: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }

    private void writeJsonError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("code", status);
        body.put("message", message);
        new ObjectMapper().writeValue(response.getWriter(), body);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
