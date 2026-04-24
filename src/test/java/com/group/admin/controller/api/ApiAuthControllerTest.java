package com.group.admin.controller.api;

import com.group.admin.BaseControllerTest;
import com.group.admin.entity.User;
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
        when(jwtUtil.generateToken(anyString(), anyString(), anyString(), anyList())).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(anyString())).thenReturn("refresh-token");
        when(jwtUtil.getExpirationSeconds()).thenReturn(86400L);

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
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.user.email").value("legacy@test.com"));

        verify(userService).register(any());
    }
}
