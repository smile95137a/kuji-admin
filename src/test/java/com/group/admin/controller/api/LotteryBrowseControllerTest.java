package com.group.admin.controller.api;

import com.group.admin.BaseControllerTest;
import com.group.admin.req.common.QueryReq;
import com.group.admin.req.lottery.LotteryCondition;
import com.group.admin.res.lottery.LotteryRes;
import com.group.admin.service.LotteryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * LotteryBrowseController 測試
 */
@DisplayName("前台一番賞瀏覽 API 測試")
class LotteryBrowseControllerTest extends BaseControllerTest {

    @Mock
    private LotteryService lotteryService;

    @InjectMocks
    private LotteryBrowseController lotteryBrowseController;

    @BeforeEach
    void setUp() {
        setupMockMvc(lotteryBrowseController);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("瀏覽一番賞列表")
    void browseLotteries_ShouldReturnList() throws Exception {
        when(lotteryService.queryLotteries(any(QueryReq.class))).thenReturn(Collections.emptyList());
        
        mockMvc.perform(post("/lottery/browse/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("取得一番賞詳情")
    void getLotteryDetail_ShouldReturnLottery() throws Exception {
        LotteryRes mockRes = new LotteryRes();
        mockRes.setStatus("ON_SHELF");
        when(lotteryService.getLottery(anyString())).thenReturn(mockRes);
        
        mockMvc.perform(get("/lottery/browse/test-id"))
                .andExpect(status().isOk());
    }
}
