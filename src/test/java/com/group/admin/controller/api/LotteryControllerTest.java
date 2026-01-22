package com.group.admin.controller.api;

import com.group.admin.BaseControllerTest;
import com.group.admin.service.LotteryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * LotteryController (API) 測試
 */
@DisplayName("前台商品詳情 API 測試")
class LotteryControllerTest extends BaseControllerTest {

    @MockBean
    private LotteryService lotteryService;

    @Test
    @DisplayName("取得商品詳情")
    void getLotteryDetail_ShouldReturnLottery() throws Exception {
        mockMvc.perform(get("/lottery/test-id"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("取得商品獎品")
    void getLotteryPrizes_ShouldReturnPrizes() throws Exception {
        mockMvc.perform(get("/lottery/test-id/prizes"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("取得商品剩餘抽數")
    void getLotteryRemaining_ShouldReturnCount() throws Exception {
        mockMvc.perform(get("/lottery/test-id/remaining"))
                .andExpect(status().isOk());
    }
}
