package com.group.admin.controller.api;

import com.group.admin.BaseControllerTest;
import com.group.admin.res.address.UserAddressRes;
import com.group.admin.service.UserAddressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserAddressController 測試
 */
@DisplayName("前台收貨地址 API 測試")
class UserAddressControllerTest extends BaseControllerTest {

    @Mock
    private UserAddressService userAddressService;

    @InjectMocks
    private UserAddressController userAddressController;

    private static final String USER_ID = "user-uuid-001";

    @BeforeEach
    void setUp() {
        setupMockMvc(userAddressController);
        setupAuthentication(USER_ID, "USER");
    }

    @Test
    @DisplayName("取得我的地址列表")
    void getMyAddresses_ShouldReturnList() throws Exception {
        when(userAddressService.getByUserId(anyString())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/user/addresses"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("新增地址")
    void createAddress_ShouldReturnAddress() throws Exception {
        when(userAddressService.create(anyString(), any())).thenReturn(new UserAddressRes());

        mockMvc.perform(post("/user/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recipientName\": \"小明\", \"recipientPhone\": \"0912345678\", \"city\": \"台北市\", \"district\": \"中正區\", \"address\": \"XX路XX號\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("更新地址")
    void updateAddress_ShouldReturnUpdatedAddress() throws Exception {
        when(userAddressService.update(anyString(), anyString(), any())).thenReturn(new UserAddressRes());

        mockMvc.perform(put("/user/addresses/test-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"小華\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("刪除地址")
    void deleteAddress_ShouldReturn200() throws Exception {
        mockMvc.perform(delete("/user/addresses/test-id"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("設為預設地址")
    void setDefaultAddress_ShouldReturn200() throws Exception {
        when(userAddressService.setDefault(anyString(), anyString())).thenReturn(new UserAddressRes());

        mockMvc.perform(put("/user/addresses/test-id/default"))
                .andExpect(status().isOk());
    }
}
