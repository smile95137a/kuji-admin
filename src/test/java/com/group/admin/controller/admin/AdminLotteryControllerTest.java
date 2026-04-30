package com.group.admin.controller.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import com.group.admin.BaseControllerTest;
import com.group.admin.entity.StoreUser;
import com.group.admin.mapper.StoreUserMapper;
import com.group.admin.service.LotteryService;
import com.group.admin.util.SecurityUtils;

@DisplayName("AdminLotteryController 驗證測試")
class AdminLotteryControllerTest extends BaseControllerTest {

    @Mock
    private LotteryService lotteryService;

    @Mock
    private StoreUserMapper storeUserMapper;

    @InjectMocks
    private AdminLotteryController adminLotteryController;

    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn("admin-1");
        securityUtilsMock.when(SecurityUtils::isAdmin).thenReturn(true);
        securityUtilsMock.when(SecurityUtils::isStoreOwner).thenReturn(false);
        securityUtilsMock.when(SecurityUtils::isStoreEditor).thenReturn(false);

        StoreUser su = new StoreUser();
        su.setStoreId("store-1");
        org.mockito.Mockito.lenient().when(storeUserMapper.selectByExample(any())).thenReturn(List.of(su));

        setupMockMvcWithExceptionHandler(adminLotteryController);
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    @DisplayName("POST /admin/lottery 缺少 title 時應回傳 400")
    void createLottery_MissingTitle_ShouldReturnBadRequest() throws Exception {
        String body = """
                {
                  "storeId":"store-1",
                  "category":"CUSTOM_GACHA",
                  "subCategory":"SCRATCH_MODE",
                  "gameMode":"RANDOM",
                  "pricePerDraw":100
                }
                """;

        mockMvc.perform(post("/admin/lottery")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(lotteryService, never()).createLottery(any());
    }

    @Test
    @DisplayName("PUT /admin/lottery/{id} pricePerDraw < 0 時應回傳 400")
    void updateLottery_NegativePrice_ShouldReturnBadRequest() throws Exception {
        String body = """
                {
                  "pricePerDraw": -1
                }
                """;

        mockMvc.perform(put("/admin/lottery/550e8400-e29b-41d4-a716-446655440000")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(lotteryService, never()).updateLottery(anyString(), any());
    }

    @Test
    @DisplayName("POST /admin/lottery/with-prizes 缺少 lottery 區塊時應回傳 400")
    void createWithPrizes_MissingLottery_ShouldReturnBadRequest() throws Exception {
        String body = """
                {
                  "prizes":[
                    {"name":"A賞","quantity":1}
                  ]
                }
                """;

        mockMvc.perform(post("/admin/lottery/with-prizes")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(lotteryService, never()).createLotteryWithPrizes(any(), anyString());
    }

    @Test
    @DisplayName("PUT /admin/lottery/with-prizes/{id} 獎品缺少 id 時應回傳 400")
    void updateWithPrizes_PrizeMissingId_ShouldReturnBadRequest() throws Exception {
        String body = """
                {
                  "prizes":[
                    {"name":"B賞"}
                  ]
                }
                """;

        mockMvc.perform(put("/admin/lottery/with-prizes/550e8400-e29b-41d4-a716-446655440000")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(lotteryService, never()).updateLotteryWithPrizes(any(), anyString());
    }
}
