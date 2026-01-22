package com.group.admin.controller.api;

import com.group.admin.BaseControllerTest;
import com.group.admin.service.MarqueeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MarqueeController (前台) 測試
 */
@DisplayName("前台跑馬燈 API 測試")
class MarqueeControllerTest extends BaseControllerTest {

    @Mock
    private MarqueeService marqueeService;

    @InjectMocks
    private MarqueeController marqueeController;

    @BeforeEach
    void setUp() {
        setupMockMvc(marqueeController);
    }

    @Test
    @DisplayName("取得跑馬燈列表")
    void getMarquees_ShouldReturnList() throws Exception {
        mockMvc.perform(get("/marquee"))
                .andExpect(status().isOk());
    }
}
