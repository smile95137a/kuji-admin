package com.group.admin.controller.api;

import com.group.admin.BaseControllerTest;
import com.group.admin.res.news.NewsRes;
import com.group.admin.service.NewsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * NewsController (前台) 測試
 */
@DisplayName("前台最新消息 API 測試")
class NewsControllerTest extends BaseControllerTest {

    @Mock
    private NewsService newsService;

    @InjectMocks
    private NewsController newsController;

    @BeforeEach
    void setUp() {
        setupMockMvc(newsController);
    }

    @Test
    @DisplayName("取得最新消息列表")
    void getNewsList_ShouldReturnList() throws Exception {
        when(newsService.getPublishedNews(any())).thenReturn(Collections.emptyList());
        
        mockMvc.perform(get("/news"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("取得最新消息詳情")
    void getNewsDetail_ShouldReturnNews() throws Exception {
        NewsRes mockRes = new NewsRes();
        mockRes.setStatus("PUBLISHED");
        when(newsService.getNewsById(anyString())).thenReturn(mockRes);
        
        mockMvc.perform(get("/news/test-id"))
                .andExpect(status().isOk());
    }
}
