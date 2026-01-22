package com.group.admin.controller.admin;

import com.group.admin.BaseControllerTest;
import com.group.admin.res.banner.BannerRes;
import com.group.admin.service.BannerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AdminBannerController 測試
 */
@DisplayName("後台 Banner 管理 API 測試")
class AdminBannerControllerTest extends BaseControllerTest {

    @Mock
    private BannerService bannerService;

    @InjectMocks
    private AdminBannerController adminBannerController;

    @BeforeEach
    void setUp() {
        setupMockMvc(adminBannerController);
    }

    @Test
    @DisplayName("查詢 Banner 列表 - 成功")
    void queryBanners_ShouldReturnList() throws Exception {
        List<BannerRes> mockList = Arrays.asList(new BannerRes(), new BannerRes());
        when(bannerService.queryBanners(any())).thenReturn(mockList);

        mockMvc.perform(post("/admin/banner/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        verify(bannerService).queryBanners(any());
    }

    @Test
    @DisplayName("建立 Banner - 成功")
    void createBanner_ShouldReturnCreated() throws Exception {
        BannerRes mockBanner = new BannerRes();
        mockBanner.setId("test-id");
        when(bannerService.createBanner(any())).thenReturn(mockBanner);

        String requestBody = """
            {
                "storeId": "test-store-id",
                "title": "Test Banner",
                "imageUrl": "https://example.com/image.jpg",
                "linkUrl": "https://example.com",
                "position": "HOME_TOP"
            }
            """;

        mockMvc.perform(post("/admin/banner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("更新 Banner - 成功")
    void updateBanner_ShouldReturnUpdated() throws Exception {
        BannerRes mockBanner = new BannerRes();
        mockBanner.setId("test-id");
        when(bannerService.updateBanner(any(), any())).thenReturn(mockBanner);

        String requestBody = """
            {
                "title": "Updated Banner"
            }
            """;

        mockMvc.perform(put("/admin/banner/test-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("刪除 Banner - 成功")
    void deleteBanner_ShouldReturn204() throws Exception {
        doNothing().when(bannerService).deleteBanner(any());

        mockMvc.perform(delete("/admin/banner/test-id"))
                .andExpect(status().isOk());
    }
}
