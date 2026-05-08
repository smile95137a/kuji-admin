package com.group.admin.controller.admin;

import com.group.admin.BaseControllerTest;
import com.group.admin.constants.ErrorCodes;
import com.group.admin.req.auth.AdminLoginReq;
import com.group.admin.req.auth.ChangePasswordReq;
import com.group.admin.req.auth.RefreshTokenReq;
import com.group.admin.res.auth.LoginRes;
import com.group.admin.service.AdminAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AdminAuthController 測試
 */
@DisplayName("後台認證 API 測試")
class AdminAuthControllerTest extends BaseControllerTest {

    @Mock
    private AdminAuthService adminAuthService;

    @InjectMocks
    private AdminAuthController adminAuthController;

    @BeforeEach
    void setUp() {
        setupMockMvcWithExceptionHandler(adminAuthController);
    }

    @Test
    @DisplayName("登入 - 成功")
    void login_ShouldReturnToken_WhenValidCredentials() throws Exception {
        // Given
        AdminLoginReq req = new AdminLoginReq();
        req.setUsername("admin@kuji.com");
        req.setPassword("admin123");

        LoginRes mockRes = new LoginRes();
        mockRes.setAccessToken("test-token");
        mockRes.setRefreshToken("test-refresh-token");
        
        when(adminAuthService.login(any())).thenReturn(mockRes);

        // When & Then
        mockMvc.perform(post("/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isOk());

        verify(adminAuthService).login(any());
    }

    @Test
    @DisplayName("修改密碼 - 成功")
    void changePassword_ShouldReturn200_WhenValid() throws Exception {
        ChangePasswordReq req = new ChangePasswordReq();
        req.setOldPassword("oldPassword");
        req.setNewPassword("newPassword123");
        req.setConfirmPassword("newPassword123");

        doNothing().when(adminAuthService).changePassword(any());

        mockMvc.perform(post("/admin/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("刷新 Token - 成功")
    void refreshToken_ShouldReturnNewToken_WhenValidRefreshToken() throws Exception {
        RefreshTokenReq req = new RefreshTokenReq();
        req.setRefreshToken("valid-refresh-token");

        LoginRes mockRes = new LoginRes();
        mockRes.setAccessToken("new-access-token");
        
        when(adminAuthService.refreshToken(any())).thenReturn(mockRes);

        mockMvc.perform(post("/admin/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isOk());
    }

        @Test
        @DisplayName("忘記密碼 - 成功")
        void forgotPassword_ShouldReturn200_WhenEmailProvided() throws Exception {
                doNothing().when(adminAuthService).forgotPassword("owner@kuji.com");

                mockMvc.perform(post("/admin/auth/forgot-password")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("""
                                                                {
                                                                    \"email\": \"owner@kuji.com\"
                                                                }
                                                                """))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("若帳號存在，系統已發送臨時密碼至註冊 Email"));

                verify(adminAuthService).forgotPassword("owner@kuji.com");
        }

        @Test
        @DisplayName("忘記密碼 - Email 格式錯誤")
        void forgotPassword_ShouldReturn400_WhenEmailInvalid() throws Exception {
                mockMvc.perform(post("/admin/auth/forgot-password")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("""
                                                                {
                                                                    \"email\": \"invalid-email\"
                                                                }
                                                                """))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.error.code").value(ErrorCodes.COMMON_VALIDATION_ERROR));

                verify(adminAuthService, never()).forgotPassword(any());
        }

    @Test
    @DisplayName("登出 - 成功")
    void logout_ShouldReturn200() throws Exception {
        doNothing().when(adminAuthService).logout();

        mockMvc.perform(post("/admin/auth/logout"))
                .andExpect(status().isOk());
    }
}
