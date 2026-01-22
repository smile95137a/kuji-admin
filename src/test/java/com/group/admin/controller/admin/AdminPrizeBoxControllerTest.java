package com.group.admin.controller.admin;

import com.group.admin.BaseControllerTest;
import com.group.admin.service.PrizeBoxService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AdminPrizeBoxController 測試
 */
@DisplayName("後台獎品盒管理 API 測試")
class AdminPrizeBoxControllerTest extends BaseControllerTest {

    @Mock
    private PrizeBoxService prizeBoxService;

    @InjectMocks
    private AdminPrizeBoxController adminPrizeBoxController;

    @BeforeEach
    void setUp() {
        setupMockMvc(adminPrizeBoxController);
    }

    @Test
    @DisplayName("查詢獎品盒列表")
    void getPrizeBox_ShouldReturnList() throws Exception {
        when(prizeBoxService.getPrizeBox(anyString())).thenReturn(Collections.emptyList());
        
        mockMvc.perform(get("/admin/prize-box/test-user-id"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("取得獎品盒摘要")
    void getSummaryByStore_ShouldReturnSummary() throws Exception {
        when(prizeBoxService.getSummaryByStore(anyString())).thenReturn(Collections.emptyList());
        
        mockMvc.perform(get("/admin/prize-box/summary/test-user-id"))
                .andExpect(status().isOk());
    }
}
