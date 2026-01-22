package com.group.admin.controller.admin;

import com.group.admin.BaseControllerTest;
import com.group.admin.service.LotteryPrizeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * LotteryPrizeController 測試
 */
@DisplayName("後台獎品管理 API 測試")
class LotteryPrizeControllerTest extends BaseControllerTest {

    @Mock
    private LotteryPrizeService lotteryPrizeService;

    @InjectMocks
    private LotteryPrizeController lotteryPrizeController;

    @BeforeEach
    void setUp() {
        setupMockMvc(lotteryPrizeController);
    }

    @Test
    @DisplayName("取得抽獎的獎品列表")
    void getLotteryPrizes_ShouldReturnList() throws Exception {
        mockMvc.perform(get("/admin/lotteries/test-lottery-id/prizes"))
                .andExpect(status().isOk());
    }
}
