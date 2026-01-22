package com.group.admin.controller.api;

import com.group.admin.BaseControllerTest;
import com.group.admin.service.ReferralCodeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ReferralCodeValidateController 測試
 */
@DisplayName("前台推薦碼驗證 API 測試")
class ReferralCodeValidateControllerTest extends BaseControllerTest {

    @MockBean
    private ReferralCodeService referralCodeService;

    @Test
    @DisplayName("驗證推薦碼")
    void validateReferralCode_ShouldReturnResult() throws Exception {
        mockMvc.perform(get("/referral-codes/validate")
                        .param("code", "NEWUSER2026"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("使用推薦碼")
    void useReferralCode_ShouldReturn200() throws Exception {
        mockMvc.perform(post("/referral-codes/use")
                        .param("code", "NEWUSER2026"))
                .andExpect(status().isOk());
    }
}
