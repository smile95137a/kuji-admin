package com.group.admin.controller.api;

import com.group.admin.BaseControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * OAuth2Controller 測試
 */
@DisplayName("前台 OAuth2 登入 API 測試")
class OAuth2ControllerTest extends BaseControllerTest {

    @Test
    @DisplayName("Google 登入")
    void googleLogin_ShouldRedirect() throws Exception {
        mockMvc.perform(get("/oauth2/google"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("Google 登入回調")
    void googleCallback_ShouldReturnToken() throws Exception {
        mockMvc.perform(get("/oauth2/google/callback")
                        .param("code", "auth-code"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("LINE 登入")
    void lineLogin_ShouldRedirect() throws Exception {
        mockMvc.perform(get("/oauth2/line"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("LINE 登入回調")
    void lineCallback_ShouldReturnToken() throws Exception {
        mockMvc.perform(get("/oauth2/line/callback")
                        .param("code", "auth-code"))
                .andExpect(status().isOk());
    }
}
