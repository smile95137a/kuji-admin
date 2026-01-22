package com.group.admin.controller.api;

import com.group.admin.BaseControllerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * EnumController 測試
 */
@DisplayName("前台列舉 API 測試")
class EnumControllerTest extends BaseControllerTest {

    @InjectMocks
    private EnumController enumController;

    @BeforeEach
    void setUp() {
        setupMockMvc(enumController);
    }

    @Test
    @DisplayName("取得全部列舉")
    void getAllEnums_ShouldReturnMap() throws Exception {
        mockMvc.perform(get("/enums/all"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("取得獎品等級列舉")
    void getPrizeLevels_ShouldReturnList() throws Exception {
        mockMvc.perform(get("/enums/prize-level"))
                .andExpect(status().isOk());
    }
}
