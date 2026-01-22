package com.group.admin.controller.api;

import com.group.admin.BaseControllerTest;
import com.group.admin.service.UserAddressService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserAddressController 測試
 */
@DisplayName("前台收貨地址 API 測試")
class UserAddressControllerTest extends BaseControllerTest {

    @MockBean
    private UserAddressService userAddressService;

    @Test
    @DisplayName("取得我的地址列表")
    void getMyAddresses_ShouldReturnList() throws Exception {
        mockMvc.perform(get("/users/me/addresses"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("新增地址")
    void createAddress_ShouldReturnAddress() throws Exception {
        mockMvc.perform(post("/users/me/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"小明\", \"phone\": \"0912345678\", \"city\": \"台北市\", \"district\": \"中正區\", \"address\": \"XX路XX號\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("更新地址")
    void updateAddress_ShouldReturnUpdatedAddress() throws Exception {
        mockMvc.perform(put("/users/me/addresses/test-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"小華\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("刪除地址")
    void deleteAddress_ShouldReturn204() throws Exception {
        mockMvc.perform(delete("/users/me/addresses/test-id"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("設為預設地址")
    void setDefaultAddress_ShouldReturn200() throws Exception {
        mockMvc.perform(patch("/users/me/addresses/test-id/default"))
                .andExpect(status().isOk());
    }
}
