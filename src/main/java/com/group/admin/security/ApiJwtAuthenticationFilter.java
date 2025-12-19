package com.group.admin.security;

import java.io.IOException;
import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.group.admin.entity.User;
import com.group.admin.example.UserExample;
import com.group.admin.mapper.UserMapper;
import com.group.admin.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 前台 JWT 認證過濾器
 * 僅處理 /api/** 路徑的請求
 * 支援前台 User 的認證
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiJwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // 只處理 /api/** 路徑（排除 /api/auth/**）
        String path = request.getRequestURI();
        if (!path.startsWith("/api/") || path.startsWith("/api/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 解析 JWT Token
        String token = extractToken(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 從 Token 取得使用者 Email
        String email = jwtUtil.getUsername(token);
        if (email == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 使用 Example 模式查詢前台使用者
        UserExample example = new UserExample();
        example.createCriteria().andEmailEqualTo(email);
        List<User> users = userMapper.selectByExample(example);

        if (users.isEmpty()) {
            log.warn("前台使用者不存在: {}", email);
            filterChain.doFilter(request, response);
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

        // 設定 Spring Security 認證資訊
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principal, null,
                principal.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.debug("前台使用者認證成功: {}", email);

        filterChain.doFilter(request, response);
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
