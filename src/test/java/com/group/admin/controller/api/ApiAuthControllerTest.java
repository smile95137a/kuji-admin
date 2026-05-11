package com.group.admin.controller.api;

import com.group.admin.BaseControllerTest;
import com.group.admin.constants.ErrorCodes;
import com.group.admin.entity.User;
import com.group.admin.service.UserTokenBlacklistService;
import com.group.admin.service.UserService;
import com.group.admin.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.MediaType;

@DisplayName("前台認證 API 測試")
class ApiAuthControllerTest extends BaseControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserTokenBlacklistService userTokenBlacklistService;

    @InjectMocks
    private ApiAuthController apiAuthController;

    @BeforeEach
    void setUp() {
        setupMockMvcWithExceptionHandler(apiAuthController);
    }

    @Test
    @DisplayName("舊版註冊 payload 仍可成功註冊")
    void register_WithLegacyPayload_ShouldReturn200() throws Exception {
        User user = new User();
        user.setId("user-001");
        user.setEmail("legacy@test.com");
        user.setNickname("legacyUser");
        user.setProvider("EMAIL");

        when(userService.register(any())).thenReturn(user);

        String body = """
                {
                  "email": "legacy@test.com",
                  "password": "secret123",
                  "username": "legacyUser",
                  "phone": "0912345678"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
          .andExpect(jsonPath("$.forceChangePassword").value(false))
                .andExpect(jsonPath("$.user.email").value("legacy@test.com"));

        verify(userService).register(any());
    }

        @Test
        @DisplayName("忘記密碼 - 成功回傳通用訊息")
        void forgotPassword_ShouldReturn200_WhenValidEmail() throws Exception {
        String body = """
            {
              "email": "user@test.com"
            }
            """;

        mockMvc.perform(post("/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("如果此 Email 已註冊，將會收到臨時密碼郵件"));

        verify(userService).requestPasswordReset("user@test.com");
        }

        @Test
        @DisplayName("重設密碼舊流程 - 已停用")
        void resetPassword_ShouldReturn400_WhenLegacyEndpointCalled() throws Exception {
        String body = """
            {
              "token": "legacy-token",
              "newPassword": "NewPass123",
              "confirmPassword": "NewPass123"
            }
            """;

        mockMvc.perform(post("/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value(ErrorCodes.COMMON_VALIDATION_ERROR))
            .andExpect(jsonPath("$.error.message").value("重設連結流程已停用，請使用忘記密碼取得臨時密碼後登入修改"));

        verify(userService, never()).resetPassword(anyString(), anyString());
        }

        @Test
        @DisplayName("刷新 Token - 無效 token")
        void refresh_ShouldReturn401_WhenTokenInvalid() throws Exception {
        when(jwtUtil.validateToken("invalid-token")).thenReturn(false);

        mockMvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      \"refreshToken\": \"invalid-token\"
                    }
                    """))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value(ErrorCodes.AUTH_TOKEN_INVALID));

        verify(userService, never()).findByEmail(anyString());
        }

        @Test
        @DisplayName("刷新 Token - 缺少必要欄位")
        void refresh_ShouldReturn401_WhenTokenClaimsMissing() throws Exception {
        when(jwtUtil.validateToken("missing-claims-token")).thenReturn(true);
        when(jwtUtil.getUsername("missing-claims-token")).thenReturn("user@test.com");
        when(jwtUtil.getUserId("missing-claims-token")).thenReturn(null);
        when(jwtUtil.getUserType("missing-claims-token")).thenReturn("user");
        when(jwtUtil.getGen("missing-claims-token")).thenReturn(3L);

        mockMvc.perform(post("/auth/refresh")
          .contentType(MediaType.APPLICATION_JSON)
          .content("""
              {
                \"refreshToken\": \"missing-claims-token\"
              }
              """))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value(ErrorCodes.AUTH_TOKEN_INVALID));

        verify(userService, never()).findByEmail(anyString());
        }

        @Test
        @DisplayName("刷新 Token - gen 不匹配應拒絕")
        void refresh_ShouldReturn401_WhenTokenGenMismatch() throws Exception {
        User user = new User();
        user.setId("user-001");
        user.setEmail("user@test.com");
        user.setStatus("ACTIVE");
        user.setEmailVerified((byte) 1);

        when(jwtUtil.validateToken("gen-mismatch-token")).thenReturn(true);
        when(jwtUtil.getUsername("gen-mismatch-token")).thenReturn("user@test.com");
        when(jwtUtil.getUserId("gen-mismatch-token")).thenReturn("user-001");
        when(jwtUtil.getUserType("gen-mismatch-token")).thenReturn("user");
        when(jwtUtil.getGen("gen-mismatch-token")).thenReturn(2L);
        when(userService.findByEmail("user@test.com")).thenReturn(user);
        when(userTokenBlacklistService.getBlacklistGen("user-001")).thenReturn(3);

        mockMvc.perform(post("/auth/refresh")
          .contentType(MediaType.APPLICATION_JSON)
          .content("""
              {
                \"refreshToken\": \"gen-mismatch-token\"
              }
              """))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value(ErrorCodes.AUTH_TOKEN_REVOKED));
        }

        @Test
        @DisplayName("刷新 Token - 成功時應旋轉 access/refresh")
        void refresh_ShouldReturn200_WhenTokenValid() throws Exception {
        User user = new User();
        user.setId("user-001");
        user.setEmail("user@test.com");
        user.setStatus("ACTIVE");
        user.setEmailVerified((byte) 1);

        when(jwtUtil.validateToken("valid-refresh-token")).thenReturn(true);
        when(jwtUtil.getUsername("valid-refresh-token")).thenReturn("user@test.com");
        when(jwtUtil.getUserId("valid-refresh-token")).thenReturn("user-001");
        when(jwtUtil.getUserType("valid-refresh-token")).thenReturn("user");
        when(jwtUtil.getGen("valid-refresh-token")).thenReturn(5L);
        when(userService.findByEmail("user@test.com")).thenReturn(user);
        when(userTokenBlacklistService.getBlacklistGen("user-001")).thenReturn(5);
        when(jwtUtil.generateToken(eq("user@test.com"), eq("user-001"), eq("user"), anyList(), eq(null), eq(5L)))
          .thenReturn("new-access-token");
        when(jwtUtil.generateRefreshToken("user@test.com", "user-001", "user", 5L))
          .thenReturn("new-refresh-token");
        when(jwtUtil.getExpirationSeconds()).thenReturn(86400L);

        mockMvc.perform(post("/auth/refresh")
          .contentType(MediaType.APPLICATION_JSON)
          .content("""
              {
                \"refreshToken\": \"valid-refresh-token\"
              }
              """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value("new-access-token"))
            .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"))
            .andExpect(jsonPath("$.user.email").value("user@test.com"));
        }

        @Test
        @DisplayName("註冊 - 密碼與確認密碼不一致")
        void register_ShouldReturn400_WhenPasswordMismatch() throws Exception {
        String body = """
            {
              "email": "legacy@test.com",
              "password": "secret123",
              "confirmPassword": "another123",
              "username": "legacyUser",
              "phone": "0912345678"
            }
            """;

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value(ErrorCodes.COMMON_VALIDATION_ERROR));

        verify(userService, never()).register(any());
        }
}
