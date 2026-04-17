package com.group.admin.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

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
import com.group.admin.service.UserTokenBlacklistService;
import com.group.admin.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * API JWT 認證過濾器
 * 處理 /api/** 路徑的請求
 * 支援前台 User 和後台 AdminUser 的認證
 * 
 * 認證邏輯：
 * 1. 先檢查 JWT token 的 userType 欄位
 * 2. 如果是 admin，從 AdminUser 表查詢並賦予對應角色
 * 3. 如果是 user 或沒有 userType，從 User 表查詢並賦予 ROLE_USER
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiJwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final AdminUserMapper adminUserMapper;
    private final AdminUserRoleMapper adminUserRoleMapper;
    private final RoleMapper roleMapper;
    private final UserTokenBlacklistService userTokenBlacklistService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // 取得 Servlet Path（已經去掉 context-path 的路徑）
        String path = request.getServletPath();
        log.info("🔍 [ApiJwtFilter] 收到請求: URI={}, ServletPath={}", request.getRequestURI(), path);

        // 排除 /auth/** 和 /admin/auth/** 路徑（ServletPath 已經不包含 /api 前綴）
        if (path.startsWith("/auth/") || path.startsWith("/admin/auth/")) {
            log.info("⏭️  [ApiJwtFilter] 跳過認證路徑: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        // 處理所有其他路徑（因為所有請求都有 /api context-path）
        log.info("🔐 [ApiJwtFilter] 開始處理認證: {}", path);

        // 解析 JWT Token
        String token = extractToken(request);
        if (token == null) {
            log.warn("⚠️  [ApiJwtAuthenticationFilter] 未提供 Token");
            filterChain.doFilter(request, response);
            return;
        }

        log.info("🎫 [ApiJwtAuthenticationFilter] Token: {}...", token.substring(0, Math.min(20, token.length())));

        if (!jwtUtil.validateToken(token)) {
            log.warn("⚠️  [ApiJwtAuthenticationFilter] Token 驗證失敗");
            filterChain.doFilter(request, response);
            return;
        }

        // 從 Token 取得使用者資訊
        String email = jwtUtil.getUsername(token);
        String userType = jwtUtil.getUserType(token); // 取得 userType
        
        log.info("👤 [ApiJwtAuthenticationFilter] 使用者: {}, 類型: {}", email, userType);
        
        if (email == null) {
            log.warn("⚠️  [ApiJwtAuthenticationFilter] 無法取得使用者 Email");
            filterChain.doFilter(request, response);
            return;
        }

        // 根據 userType 決定查詢哪個表
        if ("admin".equalsIgnoreCase(userType)) {
            log.info("🔑 [ApiJwtAuthenticationFilter] 執行後台管理員認證: {}", email);
            authenticateAdmin(request, email);
        } else {
            log.info("🔑 [ApiJwtAuthenticationFilter] 執行前台使用者認證: {}", email);
            authenticateUser(request, email, token);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 認證後台管理員
     */
    private void authenticateAdmin(HttpServletRequest request, String email) {
        try {
            log.info("🔍 [Admin Auth] 開始查詢後台管理員: {}", email);
            
            AdminUserExample example = new AdminUserExample();
            example.createCriteria().andEmailEqualTo(email);
            List<AdminUser> adminUsers = adminUserMapper.selectByExample(example);

            if (adminUsers.isEmpty()) {
                log.warn("❌ [Admin Auth] 後台管理員不存在: {}", email);
                return;
            }

            AdminUser adminUser = adminUsers.get(0);
            log.info("✅ [Admin Auth] 找到管理員: {} (ID: {})", email, adminUser.getId());

            // 查詢角色
            AdminUserRoleExample roleExample = new AdminUserRoleExample();
            roleExample.createCriteria().andAdminUserIdEqualTo(adminUser.getId());
            List<AdminUserRole> userRoles = adminUserRoleMapper.selectByExample(roleExample);
            
            log.info("🎭 [Admin Auth] 找到 {} 個角色關聯", userRoles.size());
            
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            List<String> roleNames = new ArrayList<>();
            
            for (AdminUserRole ur : userRoles) {
                Role role = roleMapper.selectByPrimaryKey(ur.getRoleId());
                if (role != null) {
                    // 保持完整的 role code (包含 ROLE_ 前綴)
                    String roleCode = role.getCode();
                    roleNames.add(roleCode);
                    
                    // 如果沒有 ROLE_ 前綴，則加上
                    String authorityName = roleCode.startsWith("ROLE_") ? roleCode : "ROLE_" + roleCode;
                    authorities.add(new SimpleGrantedAuthority(authorityName));
                    log.info("  ↳ 角色: {} (Code: {})", role.getName(), roleCode);
                }
            }

            // 建立 AdminUserPrincipal
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

            log.info("✅ [Admin Auth] 後台管理員認證成功: {} (角色: {})", email, roleNames);
            
        } catch (Exception e) {
            log.error("❌ [Admin Auth] 後台管理員認證失敗: {}", e.getMessage(), e);
        }
    }

    /**
     * 認證前台使用者
     */
    private void authenticateUser(HttpServletRequest request, String email, String token) {
        try {
            String userId = jwtUtil.getUserId(token);
            Integer tokenGen = getTokenGen(token);
            if (userId != null && tokenGen != null) {
                int currentGen = userTokenBlacklistService.getBlacklistGen(userId);
                if (tokenGen < currentGen) {
                    log.warn("🚫 用戶 token 已失效 (gen mismatch): email={}", email);
                    return;
                }
            }
            UserExample example = new UserExample();
            example.createCriteria().andEmailEqualTo(email);
            List<User> users = userMapper.selectByExample(example);

            if (users.isEmpty()) {
                log.warn("❌ 前台使用者不存在: {}", email);
                return;
            }

            User user = users.get(0);

            // 建立 UserPrincipal（前台使用者固定為 USER 角色）
            UserPrincipal principal = UserPrincipal.builder()
                    .userId(user.getId())
                    .username(user.getEmail())
                    .roles(List.of("USER"))
                    .isAdmin(false)
                    .isUser(true)
                    .user(user)
                    .build();

            UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("✅ [API] 前台使用者認證成功: {}", email);
            
        } catch (Exception e) {
            log.error("❌ [API] 前台使用者認證失敗: {}", e.getMessage(), e);
        }
    }

    private Integer getTokenGen(String token) {
        try {
            Long gen = jwtUtil.getGen(token);
            return gen != null ? gen.intValue() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 從請求中提取 JWT Token
     */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
