package com.group.admin.service;

import com.group.admin.entity.User;
import com.group.admin.mapper.UserMapper;
import com.group.admin.req.AuthLoginReq;
import com.group.admin.req.AuthRegisterReq;
import com.group.admin.res.AuthRes;
import com.group.admin.service.impl.UserServiceImpl;
import com.group.admin.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 單元測試")
class UserServiceImplTest {

    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private ReferralCodeService referralCodeService;
    @Mock private EmailService emailService;
    @Mock private LoginHistoryService loginHistoryService;
    @Mock private UserTokenBlacklistService userTokenBlacklistService;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("EMAIL 帳號被誤標 GOOGLE 時，密碼登入應自動修復並成功登入")
    void login_WhenLegacyProviderCorrupted_ShouldRepairAndLogin() {
        User user = new User();
        user.setId("user-001");
        user.setEmail("user2@test.com");
        user.setPassword("hashed-password");
        user.setProvider("GOOGLE");
        user.setEmailVerified((byte) 1);
        user.setStatus("ACTIVE");

        when(userMapper.selectByExample(any())).thenReturn(List.of(user));
        when(passwordEncoder.matches("admin123", "hashed-password")).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyString(), anyString(), anyList())).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(anyString())).thenReturn("refresh-token");

        AuthLoginReq req = new AuthLoginReq();
        req.setEmail("user2@test.com");
        req.setPassword("admin123");

        AuthRes res = userService.login(req);

        assertThat(res.getAccessToken()).isEqualTo("access-token");
        assertThat(user.getProvider()).isEqualTo("EMAIL");

        ArgumentCaptor<User> repairCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).updateByPrimaryKeySelective(repairCaptor.capture());
        assertThat(repairCaptor.getValue().getProvider()).isEqualTo("EMAIL");
        assertThat(repairCaptor.getValue().getProviderId()).isNull();
    }

    @Test
    @DisplayName("註冊缺少 confirmPassword 與 nickname 時，應使用密碼本身與 email 前綴")
    void register_WhenLegacyPayloadMissingConfirmPasswordAndNickname_ShouldFallback() {
        when(userMapper.selectByExample(any())).thenReturn(List.of());
        when(passwordEncoder.encode("secret123")).thenReturn("encoded-password");

        AuthRegisterReq req = new AuthRegisterReq();
        req.setEmail("legacy@test.com");
        req.setPassword("secret123");

        User user = userService.register(req);

        assertThat(user.getNickname()).isEqualTo("legacy");
        assertThat(user.getProvider()).isEqualTo("EMAIL");

        ArgumentCaptor<User> insertCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(insertCaptor.capture());
        assertThat(insertCaptor.getValue().getNickname()).isEqualTo("legacy");
        assertThat(insertCaptor.getValue().getPassword()).isEqualTo("encoded-password");
    }
}
