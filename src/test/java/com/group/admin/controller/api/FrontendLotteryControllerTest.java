package com.group.admin.controller.api;

import com.group.admin.BaseControllerTest;
import com.group.admin.mapper.LotteryMapper;
import com.group.admin.req.common.QueryReq;
import com.group.admin.res.lottery.LotteryRes;
import com.group.admin.service.LotteryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * FrontendLotteryController 測試（實際測試 LotteryController）
 */
@DisplayName("前台商品 API 測試")
class FrontendLotteryControllerTest extends BaseControllerTest {

    @Mock
    private LotteryService lotteryService;

    @Mock
    private LotteryMapper lotteryMapper;

    @InjectMocks
    private LotteryController lotteryController;

    @BeforeEach
    void setUp() {
        setupMockMvc(lotteryController);
    }

    @Test
    @DisplayName("取得商品列表")
    void getLotteries_ShouldReturnList() throws Exception {
        doReturn(Collections.<LotteryRes>emptyList()).when(lotteryService).queryLotteries(any(QueryReq.class));

        mockMvc.perform(post("/lottery/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("取得商品列表（帶分類過濾）")
    void getLotteries_WithCategory_ShouldReturnFilteredList() throws Exception {
        doReturn(Collections.<LotteryRes>emptyList()).when(lotteryService).queryLotteries(any(QueryReq.class));

        mockMvc.perform(post("/lottery/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"condition\":{\"category\":\"OFFICIAL_ICHIBAN\"}}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("取得商品詳情")
    void getLotteryById_ShouldReturnLottery() throws Exception {
        LotteryRes mockRes = new LotteryRes();
        mockRes.setStatus("ON_SHELF");
        when(lotteryService.getLottery(anyString())).thenReturn(mockRes);

        mockMvc.perform(get("/lottery/test-id"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("取得商品獎品列表（透過 list 端點帶條件查詢）")
    void getLotteryPrizes_ShouldReturnPrizes() throws Exception {
        doReturn(Collections.<LotteryRes>emptyList()).when(lotteryService).queryLotteries(any(QueryReq.class));

        mockMvc.perform(post("/lottery/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("搜尋商品")
    void searchLotteries_ShouldReturnResults() throws Exception {
        doReturn(Collections.<LotteryRes>emptyList()).when(lotteryService).queryLotteries(any(QueryReq.class));

        mockMvc.perform(post("/lottery/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"condition\":{\"keyword\":\"鬼滅\"}}"))
                .andExpect(status().isOk());
    }
}
