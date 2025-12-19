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

import io.micrometer.common.lang.NonNull;
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

        // 僅處理 /admin/** 路徑
        String path = request.getRequestURI();
        if (!path.startsWith("/admin/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 跳過登入介面
        if (path.equals("/admin/auth/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = extractToken(request);
            if (token != null && jwtUtil.validateToken(token)) {
                String username = jwtUtil.getUsername(token);
                
                // 使用 Example 模式查詢管理員
                AdminUserExample example = new AdminUserExample();
                example.createCriteria().andUsernameEqualTo(username);
                List<AdminUser> adminUsers = adminUserMapper.selectByExample(example);
                
                if (!adminUsers.isEmpty()) {
                    AdminUser adminUser = adminUsers.get(0);
                    
                    // 使用 Example 模式查詢角色
                    AdminUserRoleExample roleExample = new AdminUserRoleExample();
                    roleExample.createCriteria().andAdminUserIdEqualTo(adminUser.getId());
                    List<AdminUserRole> userRoles = adminUserRoleMapper.selectByExample(roleExample);
                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    
                    for (AdminUserRole ur : userRoles) {
                        Role role = roleMapper.selectByPrimaryKey(ur.getRoleId());
                        if (role != null) {
                            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
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

                    log.debug("✅ 後台認證成功: {}", username);
                }
            }
        } catch (Exception e) {
            log.warn("❌ 後台認證失敗: {}", e.getMessage());
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
