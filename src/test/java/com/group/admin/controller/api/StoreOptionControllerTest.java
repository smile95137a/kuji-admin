package com.group.admin.controller.api;

import com.group.admin.BaseControllerTest;
import com.group.admin.mapper.StoreMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * StoreOptionController 測試
 */
@DisplayName("前台店家選項 API 測試")
class StoreOptionControllerTest extends BaseControllerTest {

    @Mock
    private StoreMapper storeMapper;

    @InjectMocks
    private StoreOptionController storeOptionController;

    @BeforeEach
    void setUp() {
        setupMockMvc(storeOptionController);
    }

    @Test
    @DisplayName("取得店家選項")
    void getStoreOptions_ShouldReturnList() throws Exception {
        mockMvc.perform(get("/stores/options"))
                .andExpect(status().isOk());
    }
}
