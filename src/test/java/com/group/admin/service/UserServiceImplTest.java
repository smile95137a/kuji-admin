package com.group.admin.service;

import com.group.admin.entity.User;
import com.group.admin.mapper.UserMapper;
import com.group.admin.req.AuthLoginReq;
import com.group.admin.req.AuthRegisterReq;
import com.group.admin.req.auth.ChangePasswordReq;
import com.group.admin.res.AuthRes;
import com.group.admin.service.impl.UserServiceImpl;
import com.group.admin.util.JwtUtil;
import com.group.admin.util.PasswordUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
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
    @Mock private PasswordUtil passwordUtil;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "frontendUrl", "http://localhost:3000");
    }

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

    @Test
    @DisplayName("忘記密碼應改為臨時密碼並標記強制改密碼")
    void requestPasswordReset_ShouldSetTemporaryPasswordMarkerAndSendEmail() {
        User user = new User();
        user.setId("user-001");
        user.setEmail("user@test.com");
        user.setNickname("小明");
        user.setProvider("EMAIL");

        when(userMapper.selectByExample(any())).thenReturn(List.of(user));
        when(passwordUtil.generateRandomPassword()).thenReturn("TempPass123");
        when(passwordEncoder.encode("TempPass123")).thenReturn("encoded-temp-password");

        userService.requestPasswordReset("user@test.com");

        ArgumentCaptor<User> updateCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).updateByPrimaryKeySelective(updateCaptor.capture());
        assertThat(updateCaptor.getValue().getPassword()).isEqualTo("encoded-temp-password");
        assertThat(updateCaptor.getValue().getPasswordResetToken()).isEqualTo("FORCE_CHANGE_PASSWORD");
        assertThat(updateCaptor.getValue().getPasswordResetExpires()).isNotNull();

        verify(emailService).sendTemporaryPasswordEmail(
                "user@test.com",
                "小明",
                "TempPass123",
                "http://localhost:3000/login",
                "前台忘記密碼"
        );
    }

    @Test
    @DisplayName("帶有強制改密碼標記的使用者登入時應回傳 forceChangePassword=true")
    void login_ShouldReturnForceChangePassword_WhenMarkerExists() {
        User user = new User();
        user.setId("user-001");
        user.setEmail("user@test.com");
        user.setPassword("hashed-password");
        user.setProvider("EMAIL");
        user.setEmailVerified((byte) 1);
        user.setStatus("ACTIVE");
        user.setPasswordResetToken("FORCE_CHANGE_PASSWORD");

        when(userMapper.selectByExample(any())).thenReturn(List.of(user));
        when(passwordEncoder.matches("admin123", "hashed-password")).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyString(), anyString(), anyList())).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(anyString())).thenReturn("refresh-token");

        AuthLoginReq req = new AuthLoginReq();
        req.setEmail("user@test.com");
        req.setPassword("admin123");

        AuthRes res = userService.login(req);

        assertThat(res.getForceChangePassword()).isTrue();
    }

    @Test
    @DisplayName("前台使用者修改密碼成功後應清除強制改密碼標記")
    void changePassword_ShouldClearForceChangeMarker_WhenSuccessful() {
        User user = new User();
        user.setId("user-001");
        user.setEmail("user@test.com");
        user.setPassword("hashed-password");
        user.setProvider("EMAIL");
        user.setPasswordResetToken("FORCE_CHANGE_PASSWORD");

        when(userMapper.selectByPrimaryKey("user-001")).thenReturn(user);
        when(passwordEncoder.matches("OldPass123", "hashed-password")).thenReturn(true);
        when(passwordEncoder.encode("NewPass123")).thenReturn("encoded-new-password");

        ChangePasswordReq req = new ChangePasswordReq();
        req.setOldPassword("OldPass123");
        req.setNewPassword("NewPass123");
        req.setConfirmPassword("NewPass123");

        userService.changePassword("user-001", req);

        ArgumentCaptor<User> updateCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).updateByPrimaryKey(updateCaptor.capture());
        assertThat(updateCaptor.getValue().getPassword()).isEqualTo("encoded-new-password");
        assertThat(updateCaptor.getValue().getPasswordResetToken()).isNull();
        assertThat(updateCaptor.getValue().getPasswordResetExpires()).isNull();
    }
}
