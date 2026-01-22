package com.group.admin.controller.api;

import com.group.admin.BaseControllerTest;
import com.group.admin.service.DistrictService;
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
 * DistrictController 測試
 */
@DisplayName("前台地區 API 測試")
class DistrictControllerTest extends BaseControllerTest {

    @Mock
    private DistrictService districtService;

    @InjectMocks
    private DistrictController districtController;

    @BeforeEach
    void setUp() {
        setupMockMvc(districtController);
    }

    @Test
    @DisplayName("取得城市列表")
    void getCities_ShouldReturnList() throws Exception {
        when(districtService.getAllCities()).thenReturn(Collections.emptyList());
        
        mockMvc.perform(get("/district/cities"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("取得區域列表")
    void getDistrictsByCity_ShouldReturnList() throws Exception {
        when(districtService.getDistrictsByCity(anyString())).thenReturn(Collections.emptyList());
        
        mockMvc.perform(get("/district/districts/台北市"))
                .andExpect(status().isOk());
    }
}
