package com.group.admin.controller.api;

import com.group.admin.BaseControllerTest;
import com.group.admin.entity.User;
import com.group.admin.exception.BusinessException;
import com.group.admin.req.AuthGoogleReq;
import com.group.admin.res.AuthRes;
import com.group.admin.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * OAuth2Controller 測試
 *
 * <p>測試 POST /auth/oauth2/google（前端主導 A 方案）</p>
 */
@DisplayName("前台 OAuth2 登入 API 測試")
class OAuth2ControllerTest extends BaseControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private OAuth2Controller oauth2Controller;

    @BeforeEach
    void setUp() {
        setupMockMvcWithExceptionHandler(oauth2Controller);
    }

    // -------------------------------------------------------------------------
    // POST /auth/oauth2/google
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Google 登入成功（新用戶）→ 200, isNewUser=true")
    void googleLogin_NewUser_ShouldReturn200() throws Exception {
        User user = new User();
        user.setId("uuid-001");
        user.setEmail("test@gmail.com");
        user.setProvider("GOOGLE");

        AuthRes authRes = new AuthRes();
        authRes.setAccessToken("access-token");
        authRes.setRefreshToken("refresh-token");
        authRes.setExpiresIn(1800L);
        authRes.setUser(user);
        authRes.setIsNewUser(true);

        when(userService.loginWithGoogle(any())).thenReturn(authRes);

        String body = objectMapper.writeValueAsString(new AuthGoogleReq() {{
            setIdToken("valid-google-id-token");
        }});

        mockMvc.perform(post("/auth/oauth2/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.isNewUser").value(true));
    }

    @Test
    @DisplayName("Google 登入成功（既有 GOOGLE 帳號）→ 200, isNewUser=false")
    void googleLogin_ExistingUser_ShouldReturn200() throws Exception {
        User user = new User();
        user.setId("uuid-002");
        user.setEmail("existing@gmail.com");
        user.setProvider("GOOGLE");

        AuthRes authRes = new AuthRes();
        authRes.setAccessToken("access-token-2");
        authRes.setRefreshToken("refresh-token-2");
        authRes.setExpiresIn(1800L);
        authRes.setUser(user);
        authRes.setIsNewUser(false);

        when(userService.loginWithGoogle(any())).thenReturn(authRes);

        String body = objectMapper.writeValueAsString(new AuthGoogleReq() {{
            setIdToken("valid-google-id-token-2");
        }});

        mockMvc.perform(post("/auth/oauth2/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isNewUser").value(false));
    }

    @Test
    @DisplayName("Email 已用 local 方式註冊，再用 Google 登入 → 409 EMAIL_PROVIDER_CONFLICT")
    void googleLogin_EmailProviderConflict_ShouldReturn409() throws Exception {
        when(userService.loginWithGoogle(any()))
                .thenThrow(new BusinessException("EMAIL_PROVIDER_CONFLICT",
                        "此 Email 已用 Email/密碼方式註冊，請改用密碼登入"));

        String body = objectMapper.writeValueAsString(new AuthGoogleReq() {{
            setIdToken("conflict-token");
        }});

        mockMvc.perform(post("/auth/oauth2/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Google Token 無效 → 400")
    void googleLogin_InvalidToken_ShouldReturn400() throws Exception {
        when(userService.loginWithGoogle(any()))
                .thenThrow(new IllegalArgumentException("Invalid Google token"));

        String body = objectMapper.writeValueAsString(new AuthGoogleReq() {{
            setIdToken("bad-token");
        }});

        mockMvc.perform(post("/auth/oauth2/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
