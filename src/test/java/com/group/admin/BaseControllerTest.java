package com.group.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.group.admin.handler.GlobalExceptionHandler;
import com.group.admin.security.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller 測試基礎類別
 * 
 * 使用純 Mockito 單元測試模式：
 * - 不需要啟動 Spring Context
 * - 手動設定 MockMvc
 * - 速度更快、更輕量
 * 
 * 子類別需要：
 * 1. 使用 @Mock 注入 Service
 * 2. 使用 @InjectMocks 注入 Controller
 * 3. 在 @BeforeEach 中呼叫 setupMockMvc(controller) 或 setupMockMvcWithExceptionHandler(controller)
 */
@ExtendWith(MockitoExtension.class)
public abstract class BaseControllerTest {

    protected MockMvc mockMvc;
    
    protected ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    /**
     * 設定 MockMvc（子類別必須在 @BeforeEach 中呼叫）
     */
    protected void setupMockMvc(Object controller) {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    /**
     * 設定 MockMvc 並加入 GlobalExceptionHandler（用於需要驗證 HTTP 狀態碼的錯誤測試）
     */
    protected void setupMockMvcWithExceptionHandler(Object controller) {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    /**
     * 設定測試用的 SecurityContext（模擬已認證用戶）
     *
     * @param userId  用戶 ID
     * @param roles   角色列表（不含 ROLE_ 前綴，例如 "USER", "ADMIN", "STORE_OWNER"）
     */
    protected void setupAuthentication(String userId, String... roles) {
        List<SimpleGrantedAuthority> authorities = Arrays.stream(roles)
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                .collect(Collectors.toList());

        UserPrincipal principal = UserPrincipal.builder()
                .userId(userId)
                .username("test-" + userId + "@kuji.com")
                .roles(Arrays.asList(roles))
                .storeIds(List.of())
                .build();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    /**
     * 每個測試後清除 SecurityContext
     */
    @AfterEach
    void tearDownSecurity() {
        SecurityContextHolder.clearContext();
    }

    /**
     * 將物件轉換為 JSON 字串
     */
    protected String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }
}
