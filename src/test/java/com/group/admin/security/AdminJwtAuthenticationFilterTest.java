package com.group.admin.security;

import com.group.admin.constants.ErrorCodes;
import com.group.admin.entity.AdminUser;
import com.group.admin.example.AdminUserRoleExample;
import com.group.admin.mapper.AdminUserMapper;
import com.group.admin.mapper.AdminUserRoleMapper;
import com.group.admin.mapper.RoleMapper;
import com.group.admin.mapper.StoreUserMapper;
import com.group.admin.service.TokenBlacklistService;
import com.group.admin.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.FilterChain;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("後台 JWT Filter 測試")
class AdminJwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AdminUserMapper adminUserMapper;

    @Mock
    private AdminUserRoleMapper adminUserRoleMapper;

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private StoreUserMapper storeUserMapper;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    private AdminJwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new AdminJwtAuthenticationFilter(
                jwtUtil,
                adminUserMapper,
                adminUserRoleMapper,
                roleMapper,
                storeUserMapper,
                tokenBlacklistService
        );
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("forceChangePassword 為 true 時應攔截後台受保護 API")
    void doFilterInternal_ShouldReturn403_WhenForceChangePasswordRequired() throws Exception {
        MockHttpServletRequest request = buildBearerRequest("/admin/users/me");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        AdminUser adminUser = sampleAdminUser(true);
        mockAuthenticatedAdmin(adminUser, false);

        filter.doFilterInternal(request, response, chain);

        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString().contains(ErrorCodes.AUTH_FORCE_CHANGE_PASSWORD));
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("/admin/auth/** 路徑即使 forceChangePassword 也應放行")
    void doFilterInternal_ShouldAllowAuthPath_WhenForceChangePasswordRequired() throws Exception {
        MockHttpServletRequest request = buildBearerRequest("/admin/auth/refresh");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        AdminUser adminUser = sampleAdminUser(true);
        mockAuthenticatedAdmin(adminUser, false);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("黑名單 token 應回 401")
    void doFilterInternal_ShouldReturn401_WhenTokenRevoked() throws Exception {
        MockHttpServletRequest request = buildBearerRequest("/admin/users/me");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        AdminUser adminUser = sampleAdminUser(false);
        mockAuthenticatedAdmin(adminUser, true);

        filter.doFilterInternal(request, response, chain);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains(ErrorCodes.AUTH_TOKEN_REVOKED));
        verify(chain, never()).doFilter(request, response);
    }

    private MockHttpServletRequest buildBearerRequest(String servletPath) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", servletPath);
        request.setServletPath(servletPath);
        request.addHeader("Authorization", "Bearer valid-token");
        return request;
    }

    private void mockAuthenticatedAdmin(AdminUser adminUser, boolean blacklisted) {
        when(jwtUtil.validateToken("valid-token")).thenReturn(true);
        when(jwtUtil.getUsername("valid-token")).thenReturn(adminUser.getUsername());
        when(jwtUtil.getGen("valid-token")).thenReturn(1L);
        when(adminUserMapper.selectByExample(any())).thenReturn(List.of(adminUser));
        when(tokenBlacklistService.isBlacklisted(adminUser.getId(), 1L)).thenReturn(blacklisted);
        when(storeUserMapper.selectByExample(any())).thenReturn(List.of());
        when(adminUserRoleMapper.selectByExample(any(AdminUserRoleExample.class))).thenReturn(List.of());
    }

    private AdminUser sampleAdminUser(boolean forceChangePassword) {
        AdminUser adminUser = new AdminUser();
        adminUser.setId("admin-001");
        adminUser.setUsername("admin@kuji.com");
        adminUser.setEmail("admin@kuji.com");
        adminUser.setPassword("encoded-password");
        adminUser.setForceChangePassword(forceChangePassword);
        return adminUser;
    }
}