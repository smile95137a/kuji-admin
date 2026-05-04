package com.group.admin.controller.api;

import com.group.admin.BaseControllerTest;
import com.group.admin.mapper.LotteryMapper;
import com.group.admin.req.common.QueryReq;
import com.group.admin.res.lottery.LotteryRes;
import com.group.admin.service.LotteryService;
import com.group.admin.service.LotteryTicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * LotteryController (API) 測試
 */
@DisplayName("前台商品詳情 API 測試")
class LotteryControllerTest extends BaseControllerTest {

    @Mock
    private LotteryService lotteryService;

    @Mock
    private LotteryMapper lotteryMapper;

    @Mock
    private LotteryTicketService lotteryTicketService;

    @InjectMocks
    private LotteryController lotteryController;

    @BeforeEach
    void setUp() {
        setupMockMvc(lotteryController);
    }

    @Test
    @DisplayName("取得商品詳情")
    void getLotteryDetail_ShouldReturnLottery() throws Exception {
        LotteryRes mockRes = new LotteryRes();
        mockRes.setStatus("ON_SHELF");
        when(lotteryService.getLottery(anyString())).thenReturn(mockRes);

        mockMvc.perform(get("/lottery/test-id"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("取得商品列表（POST list 端點）")
    void getLotteryPrizes_ShouldReturnPrizes() throws Exception {
        doReturn(Collections.<LotteryRes>emptyList()).when(lotteryService).queryLotteries(any(QueryReq.class));

        mockMvc.perform(post("/lottery/list")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("取得商品剩餘抽數（透過詳情端點）")
    void getLotteryRemaining_ShouldReturnCount() throws Exception {
        LotteryRes mockRes = new LotteryRes();
        mockRes.setStatus("ON_SHELF");
        mockRes.setRemainingDraws(5);
        when(lotteryService.getLottery(anyString())).thenReturn(mockRes);

        mockMvc.perform(get("/lottery/test-id"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("相容路徑 /lottery/browse/list 應可查詢")
    void browseList_ShouldReturnOk() throws Exception {
        doReturn(Collections.<LotteryRes>emptyList()).when(lotteryService).queryLotteries(any(QueryReq.class));

        mockMvc.perform(post("/lottery/browse/list")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("相容路徑 /lottery/browse/{id}/detail 應可查詢")
    void browseDetail_ShouldReturnOk() throws Exception {
        LotteryRes mockRes = new LotteryRes();
        mockRes.setStatus("ON_SHELF");
        when(lotteryService.getLottery(anyString())).thenReturn(mockRes);
        when(lotteryService.getPrizesByLotteryId(anyString())).thenReturn(Collections.emptyList());
        when(lotteryTicketService.getTicketsForFrontend(anyString())).thenReturn(Collections.emptyList());
        when(lotteryTicketService.getDesignatedWinningNumbers(anyString())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/lottery/browse/test-id/detail"))
                .andExpect(status().isOk());
    }
}
