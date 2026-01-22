package com.group.admin.controller.admin;

import com.group.admin.BaseControllerTest;
import com.group.admin.service.MenuService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MenuController 測試
 */
@DisplayName("後台選單管理 API 測試")
class MenuControllerTest extends BaseControllerTest {

    @Mock
    private MenuService menuService;

    @InjectMocks
    private MenuController menuController;

    @BeforeEach
    void setUp() {
        setupMockMvc(menuController);
    }

    @Test
    @DisplayName("取得選單列表")
    void getMenus_ShouldReturnList() throws Exception {
        mockMvc.perform(get("/admin/menus"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("取得使用者選單")
    void getUserMenus_ShouldReturnList() throws Exception {
        mockMvc.perform(get("/admin/menus/user"))
                .andExpect(status().isOk());
    }
}
