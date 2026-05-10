package com.group.admin.security;

import com.group.admin.constants.ErrorCodes;
import com.group.admin.entity.User;
import com.group.admin.mapper.AdminUserMapper;
import com.group.admin.mapper.AdminUserRoleMapper;
import com.group.admin.mapper.RoleMapper;
import com.group.admin.mapper.UserMapper;
import com.group.admin.service.UserTokenBlacklistService;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("前台 JWT Filter 測試")
class ApiJwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AdminUserMapper adminUserMapper;

    @Mock
    private AdminUserRoleMapper adminUserRoleMapper;

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private UserTokenBlacklistService userTokenBlacklistService;

    private ApiJwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new ApiJwtAuthenticationFilter(
                jwtUtil,
                userMapper,
                adminUserMapper,
                adminUserRoleMapper,
                roleMapper,
                userTokenBlacklistService
        );
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("臨時密碼使用者存取一般 API 時應回 403")
    void doFilterInternal_ShouldReturn403_WhenForceChangePasswordRequired() throws Exception {
        MockHttpServletRequest request = buildBearerRequest("/orders");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        User user = sampleUser("FORCE_CHANGE_PASSWORD");
        mockAuthenticatedUser(user);

        filter.doFilterInternal(request, response, chain);

        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString().contains(ErrorCodes.AUTH_FORCE_CHANGE_PASSWORD));
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("臨時密碼使用者存取改密碼 API 時應放行")
    void doFilterInternal_ShouldAllowChangePasswordPath_WhenForceChangePasswordRequired() throws Exception {
        MockHttpServletRequest request = buildBearerRequest("/user/me/change-password");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        User user = sampleUser("FORCE_CHANGE_PASSWORD");
        mockAuthenticatedUser(user);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("無強制改密碼標記時應正常放行")
    void doFilterInternal_ShouldAllowRequest_WhenNoForceChangePasswordMarker() throws Exception {
        MockHttpServletRequest request = buildBearerRequest("/orders");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        User user = sampleUser(null);
        mockAuthenticatedUser(user);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    private MockHttpServletRequest buildBearerRequest(String servletPath) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", servletPath);
        request.setRequestURI("/api" + servletPath);
        request.setServletPath(servletPath);
        request.addHeader("Authorization", "Bearer user-token");
        return request;
    }

    private void mockAuthenticatedUser(User user) {
        when(jwtUtil.validateToken("user-token")).thenReturn(true);
        when(jwtUtil.getUsername("user-token")).thenReturn(user.getEmail());
        when(jwtUtil.getUserType("user-token")).thenReturn("user");
        when(jwtUtil.getUserId("user-token")).thenReturn(user.getId());
        when(jwtUtil.getGen("user-token")).thenReturn(0L);
        when(userTokenBlacklistService.getBlacklistGen(user.getId())).thenReturn(0);
        when(userMapper.selectByExample(any())).thenReturn(List.of(user));
    }

    private User sampleUser(String passwordResetToken) {
        User user = new User();
        user.setId("user-001");
        user.setEmail("user@kuji.com");
        user.setPasswordResetToken(passwordResetToken);
        return user;
    }
}