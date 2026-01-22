package com.group.admin.controller.admin;

import com.group.admin.BaseControllerTest;
import com.group.admin.service.SystemLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AdminSystemLogController 測試
 */
@DisplayName("後台系統日誌 API 測試")
class AdminSystemLogControllerTest extends BaseControllerTest {

    @Mock
    private SystemLogService systemLogService;

    @InjectMocks
    private AdminSystemLogController adminSystemLogController;

    @BeforeEach
    void setUp() {
        setupMockMvc(adminSystemLogController);
    }

    @Test
    @DisplayName("依類型查詢系統日誌")
    void getLogsByType_ShouldReturnList() throws Exception {
        when(systemLogService.getLogsByType(anyString(), anyInt())).thenReturn(Collections.emptyList());
        
        mockMvc.perform(get("/admin/system-log/type/LOGIN"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("依使用者查詢系統日誌")
    void getLogsByUser_ShouldReturnList() throws Exception {
        when(systemLogService.getLogsByUserId(anyString(), anyInt())).thenReturn(Collections.emptyList());
        
        mockMvc.perform(get("/admin/system-log/user/test-user-id"))
                .andExpect(status().isOk());
    }
}
