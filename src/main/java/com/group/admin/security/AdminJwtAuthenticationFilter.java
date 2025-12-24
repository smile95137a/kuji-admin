package com.group.admin.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.group.admin.entity.AdminUser;
import com.group.admin.entity.AdminUserRole;
import com.group.admin.entity.Role;
import com.group.admin.example.AdminUserExample;
import com.group.admin.example.AdminUserRoleExample;
import com.group.admin.mapper.AdminUserMapper;
import com.group.admin.mapper.AdminUserRoleMapper;
import com.group.admin.mapper.RoleMapper;
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
 * 支援後台 AdminUser 的認證
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminJwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final AdminUserMapper adminUserMapper;
    private final AdminUserRoleMapper adminUserRoleMapper;
    private final RoleMapper roleMapper;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        // 取得 Servlet Path（已經去掉 context-path 的路徑）
        String path = request.getServletPath();
        log.info("🔍 [AdminJwtFilter] 收到請求: URI={}, ServletPath={}", request.getRequestURI(), path);

        // 僅處理 /admin/** 路徑（ServletPath 已經不包含 /api 前綴）
        if (!path.startsWith("/admin/")) {
            log.info("⏭️  [AdminJwtFilter] 非 /admin/** 路徑，跳過: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        // 跳過登入介面
        if (path.equals("/admin/auth/login") || path.startsWith("/admin/auth/")) {
            log.info("⏭️  [AdminJwtFilter] 登入路徑，跳過: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        log.info("🔐 [AdminJwtFilter] 開始認證: {}", path);

        try {
            String token = extractToken(request);
            if (token == null) {
                log.warn("⚠️  [AdminJwtFilter] 未提供 Token");
                filterChain.doFilter(request, response);
                return;
            }

            log.info("🎫 [AdminJwtFilter] Token 前20字元: {}...", token.substring(0, Math.min(20, token.length())));

            if (!jwtUtil.validateToken(token)) {
                log.warn("⚠️  [AdminJwtFilter] Token 驗證失敗");
                filterChain.doFilter(request, response);
                return;
            }

            String username = jwtUtil.getUsername(token);
            log.info("👤 [AdminJwtFilter] 使用者: {}", username);
                
            // 使用 Example 模式查詢管理員
            AdminUserExample example = new AdminUserExample();
            example.createCriteria().andUsernameEqualTo(username);
            List<AdminUser> adminUsers = adminUserMapper.selectByExample(example);
            
            if (adminUsers.isEmpty()) {
                log.warn("❌ [AdminJwtFilter] 找不到管理員: {}", username);
                filterChain.doFilter(request, response);
                return;
            }

            AdminUser adminUser = adminUsers.get(0);
            log.info("✅ [AdminJwtFilter] 找到管理員: {} (ID: {})", username, adminUser.getId());
            
            // 使用 Example 模式查詢角色
            AdminUserRoleExample roleExample = new AdminUserRoleExample();
            roleExample.createCriteria().andAdminUserIdEqualTo(adminUser.getId());
            List<AdminUserRole> userRoles = adminUserRoleMapper.selectByExample(roleExample);
            
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            List<String> roleNames = new ArrayList<>();
            
            for (AdminUserRole ur : userRoles) {
                Role role = roleMapper.selectByPrimaryKey(ur.getRoleId());
                if (role != null) {
                    // 從 role.code 取得角色名稱（例如 ROLE_ADMIN）
                    // Spring Security 會自動移除 ROLE_ 前綴進行比對
                    String roleName = role.getCode(); // 保留完整的 ROLE_ADMIN
                    roleNames.add(roleName);
                    authorities.add(new SimpleGrantedAuthority(roleName));
                    log.info("  ↳ 角色: {} (Code: {})", role.getName(), roleName);
                }
            }

            // 建立 UserPrincipal
            UserPrincipal principal = UserPrincipal.builder()
                    .userId(adminUser.getId())
                    .username(adminUser.getUsername())
                    .password(adminUser.getPassword())
                    .isAdmin(true)
                    .authorities(authorities)
                    .adminUser(adminUser)
                    .build();

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.info("✅ [AdminJwtFilter] 認證成功: {} (角色: {})", username, roleNames);
        } catch (Exception e) {
            log.error("❌ [AdminJwtFilter] 認證失敗: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
