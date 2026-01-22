package com.group.admin.controller.api;

import com.group.admin.BaseControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * RandomDrawController 測試
 */
@DisplayName("前台隨機抽獎 API 測試")
class RandomDrawControllerTest extends BaseControllerTest {

    @Test
    @DisplayName("隨機抽獎")
    void randomDraw_ShouldReturnResult() throws Exception {
        mockMvc.perform(post("/random-draw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"lotteryId\": \"lottery-id\", \"count\": 1}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("取得隨機抽獎紀錄")
    void getRandomDrawHistory_ShouldReturnList() throws Exception {
        mockMvc.perform(get("/random-draw/history"))
                .andExpect(status().isOk());
    }
}
