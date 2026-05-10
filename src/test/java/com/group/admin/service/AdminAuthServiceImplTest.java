package com.group.admin.service;

import com.group.admin.entity.AdminUser;
import com.group.admin.mapper.AdminUserMapper;
import com.group.admin.mapper.AdminUserRoleMapper;
import com.group.admin.mapper.RoleMapper;
import com.group.admin.mapper.StoreUserMapper;
import com.group.admin.service.impl.AdminAuthServiceImpl;
import com.group.admin.util.JwtUtil;
import com.group.admin.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminAuthService 單元測試")
class AdminAuthServiceImplTest {

    @Mock private AdminUserMapper adminUserMapper;
    @Mock private AdminUserRoleMapper adminUserRoleMapper;
    @Mock private RoleMapper roleMapper;
    @Mock private StoreUserMapper storeUserMapper;
    @Mock private JwtUtil jwtUtil;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private TokenBlacklistService tokenBlacklistService;
    @Mock private PasswordUtil passwordUtil;
    @Mock private EmailService emailService;

    @InjectMocks
    private AdminAuthServiceImpl service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "frontendUrl", "http://localhost:3000");
    }

    @Test
    @DisplayName("後台忘記密碼應更新臨時密碼並寄信")
    void forgotPassword_ShouldUpdateTemporaryPasswordAndSendEmail() {
        AdminUser user = new AdminUser();
        user.setId("admin-001");
        user.setUsername("admin@kuji.com");
        user.setEmail("admin@kuji.com");
        user.setDisplayName("管理員");

        when(adminUserMapper.selectByExample(any())).thenReturn(List.of(user));
        when(passwordUtil.generateRandomPassword()).thenReturn("TempPass123");
        when(passwordEncoder.encode("TempPass123")).thenReturn("encoded-temp-password");

        service.forgotPassword("admin@kuji.com");

        ArgumentCaptor<AdminUser> updateCaptor = ArgumentCaptor.forClass(AdminUser.class);
        verify(adminUserMapper).updateByPrimaryKeySelective(updateCaptor.capture());
        assertThat(updateCaptor.getValue().getId()).isEqualTo("admin-001");
        assertThat(updateCaptor.getValue().getPassword()).isEqualTo("encoded-temp-password");
        assertThat(updateCaptor.getValue().getForceChangePassword()).isTrue();

        verify(emailService).sendTemporaryPasswordEmail(
                "admin@kuji.com",
                "管理員",
                "TempPass123",
                "http://localhost:3000/admin/login",
                "後台忘記密碼"
        );
    }

    @Test
    @DisplayName("後台忘記密碼遇到不存在帳號時應靜默返回")
    void forgotPassword_ShouldReturnSilently_WhenUserNotFound() {
        when(adminUserMapper.selectByExample(any())).thenReturn(List.of());

        service.forgotPassword("missing@kuji.com");

        verify(adminUserMapper, never()).updateByPrimaryKeySelective(any());
        verify(emailService, never()).sendTemporaryPasswordEmail(any(), any(), any(), any(), any());
    }
}