package com.group.admin.controller.api;

import com.group.admin.BaseControllerTest;
import com.group.admin.service.LotteryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * FrontendLotteryController 測試
 */
@DisplayName("前台商品 API 測試")
class FrontendLotteryControllerTest extends BaseControllerTest {

    @MockBean
    private LotteryService lotteryService;

    @Test
    @DisplayName("取得商品列表")
    void getLotteries_ShouldReturnList() throws Exception {
        mockMvc.perform(get("/lotteries"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("取得商品列表（帶分類過濾）")
    void getLotteries_WithCategory_ShouldReturnFilteredList() throws Exception {
        mockMvc.perform(get("/lotteries")
                        .param("category", "OFFICIAL_ICHIBAN"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("取得商品詳情")
    void getLotteryById_ShouldReturnLottery() throws Exception {
        mockMvc.perform(get("/lotteries/test-id"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("取得商品獎品列表")
    void getLotteryPrizes_ShouldReturnPrizes() throws Exception {
        mockMvc.perform(get("/lotteries/test-id/prizes"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("搜尋商品")
    void searchLotteries_ShouldReturnResults() throws Exception {
        mockMvc.perform(get("/lotteries/search")
                        .param("keyword", "鬼滅"))
                .andExpect(status().isOk());
    }
}
