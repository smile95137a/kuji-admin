package com.group.admin.controller.admin;

import com.group.admin.BaseControllerTest;
import com.group.admin.service.StoreService;
import com.group.admin.util.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AdminStoreController 測試
 */
@DisplayName("後台店家管理 API 測試")
class AdminStoreControllerTest extends BaseControllerTest {

    @Mock
    private StoreService storeService;

    @InjectMocks
    private AdminStoreController adminStoreController;

    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn("test-admin-id");
        securityUtilsMock.when(SecurityUtils::isAdmin).thenReturn(true);
        securityUtilsMock.when(SecurityUtils::getCurrentUserStoreIds).thenReturn(Collections.emptyList());
        lenient().when(storeService.getStoreOptionsForUser(anyString(), anyBoolean(), anyBoolean()))
                .thenReturn(Collections.emptyList());
        lenient().when(storeService.searchStoreOptions(anyString(), anyBoolean(), any(), anyString(), anyBoolean()))
                .thenReturn(Collections.emptyList());
        setupMockMvc(adminStoreController);
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    @DisplayName("取得店家選項")
    void getStoreOptions_ShouldReturnList() throws Exception {
        mockMvc.perform(get("/admin/stores/options"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("搜尋店家")
    void searchStores_ShouldReturnList() throws Exception {
        mockMvc.perform(get("/admin/stores/search")
                        .param("keyword", "test"))
                .andExpect(status().isOk());
    }
}
