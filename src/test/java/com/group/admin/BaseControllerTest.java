package com.group.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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
 * 3. 在 @BeforeEach 中呼叫 setupMockMvc(controller)
 */
@ExtendWith(MockitoExtension.class)
public abstract class BaseControllerTest {

    protected MockMvc mockMvc;
    
    protected ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 設定 MockMvc（子類別必須在 @BeforeEach 中呼叫）
     */
    protected void setupMockMvc(Object controller) {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    /**
     * 將物件轉換為 JSON 字串
     */
    protected String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }
}
