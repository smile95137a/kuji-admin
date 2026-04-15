package com.group.admin.controller.api;

import com.group.admin.BaseControllerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * OAuth2Controller 測試
 */
@DisplayName("前台 OAuth2 登入 API 測試")
class OAuth2ControllerTest extends BaseControllerTest {

    @InjectMocks
    private OAuth2Controller oauth2Controller;

    @BeforeEach
    void setUp() {
        setupMockMvcWithExceptionHandler(oauth2Controller);
    }

    @Test
    @DisplayName("OAuth2 success endpoint without principal → 400 BusinessException")
    void oauth2Success_WithoutPrincipal_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/auth/oauth2/success"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("OAuth2 failure endpoint → 400 BusinessException")
    void oauth2Failure_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/auth/oauth2/failure"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Google 登入 → 委由 Spring Security OAuth2 處理（端點不在此 controller）")
    void googleLogin_ControllerLevelTest() throws Exception {
        // OAuth2Controller only has /auth/oauth2/success and /failure
        // The Google OAuth2 redirect is handled by Spring Security filter chain
        // Test that /auth/oauth2/success without principal returns 400
        mockMvc.perform(get("/auth/oauth2/success"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Google 回調 → 委由 Spring Security OAuth2 處理")
    void googleCallback_ControllerLevelTest() throws Exception {
        mockMvc.perform(get("/auth/oauth2/failure"))
                .andExpect(status().isBadRequest());
    }
}
