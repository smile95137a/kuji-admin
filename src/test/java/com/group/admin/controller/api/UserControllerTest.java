package com.group.admin.controller.api;

import com.group.admin.BaseControllerTest;
import com.group.admin.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserController (前台) 測試
 */
@DisplayName("前台使用者 API 測試")
class UserControllerTest extends BaseControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        setupMockMvc(userController);
    }

    @Test
    @DisplayName("Hello World")
    void hello_ShouldReturnHello() throws Exception {
        mockMvc.perform(get("/user/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("hello world"));
    }
}
