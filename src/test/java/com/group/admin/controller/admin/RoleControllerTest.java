package com.group.admin.controller.admin;

import com.group.admin.BaseControllerTest;
import com.group.admin.service.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * RoleController 測試
 */
@DisplayName("後台角色管理 API 測試")
class RoleControllerTest extends BaseControllerTest {

    @Mock
    private RoleService roleService;

    @InjectMocks
    private RoleController roleController;

    @BeforeEach
    void setUp() {
        setupMockMvc(roleController);
    }

    @Test
    @DisplayName("取得角色列表")
    void getRoles_ShouldReturnList() throws Exception {
        mockMvc.perform(get("/admin/roles"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("取得角色詳情")
    void getRoleById_ShouldReturnRole() throws Exception {
        mockMvc.perform(get("/admin/roles/test-id"))
                .andExpect(status().isOk());
    }
}
