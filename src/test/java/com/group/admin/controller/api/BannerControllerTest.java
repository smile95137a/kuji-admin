package com.group.admin.controller.api;

import com.group.admin.BaseControllerTest;
import com.group.admin.service.BannerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * BannerController (前台) 測試
 */
@DisplayName("前台 Banner API 測試")
class BannerControllerTest extends BaseControllerTest {

    @Mock
    private BannerService bannerService;

    @InjectMocks
    private BannerController bannerController;

    @BeforeEach
    void setUp() {
        setupMockMvc(bannerController);
    }

    @Test
    @DisplayName("取得輪播 Banner 列表")
    void getCarouselBanners_ShouldReturnList() throws Exception {
        when(bannerService.getCarouselBanners()).thenReturn(Collections.emptyList());
        
        mockMvc.perform(get("/api/banners"))
                .andExpect(status().isOk());
    }
}
