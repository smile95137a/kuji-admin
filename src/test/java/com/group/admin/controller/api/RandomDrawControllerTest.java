package com.group.admin.controller.api;

import com.group.admin.BaseControllerTest;
import com.group.admin.res.wallet.UserCoinRes;
import com.group.admin.service.CoinService;
import com.group.admin.service.DrawService;
import com.group.admin.service.SystemConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * RandomDrawController 測試
 */
@DisplayName("前台隨機抽獎 API 測試")
class RandomDrawControllerTest extends BaseControllerTest {

    @Mock
    private DrawService drawService;

    @Mock
    private CoinService walletService;

    @Mock
    private SystemConfigService systemConfigService;

    @InjectMocks
    private RandomDrawController randomDrawController;

    private static final String USER_ID = "user-uuid-001";

    @BeforeEach
    void setUp() {
        setupMockMvc(randomDrawController);
        setupAuthentication(USER_ID, "USER");

        UserCoinRes mockWallet = UserCoinRes.builder()
                .userId(USER_ID)
                .goldCoins(1000L)
                .bonusCoins(500L)
                .build();

        when(systemConfigService.getInt(anyString(), anyInt())).thenReturn(10);
        when(walletService.getWallet(anyString())).thenReturn(mockWallet);
        when(drawService.executeDraw(anyString(), anyString(), anyInt())).thenReturn(Collections.emptyList());
    }

    @Test
    @DisplayName("隨機抽獎 → 200")
    void randomDraw_ShouldReturnResult() throws Exception {
        mockMvc.perform(post("/lottery/random/lottery-id/draw")
                        .param("count", "1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("隨機抽獎（多次）→ 200")
    void getRandomDrawHistory_ShouldReturnList() throws Exception {
        mockMvc.perform(post("/lottery/random/lottery-id/draw")
                        .param("count", "3"))
                .andExpect(status().isOk());
    }
}
