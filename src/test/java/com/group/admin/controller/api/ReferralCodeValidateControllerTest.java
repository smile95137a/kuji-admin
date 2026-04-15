package com.group.admin.controller.api;

import com.group.admin.BaseControllerTest;
import com.group.admin.res.referral.ReferralValidateRes;
import com.group.admin.service.ReferralCodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ReferralCodeValidateController 測試
 */
@DisplayName("前台推薦碼驗證 API 測試")
class ReferralCodeValidateControllerTest extends BaseControllerTest {

    @Mock
    private ReferralCodeService referralCodeService;

    @InjectMocks
    private ReferralCodeValidateController referralCodeValidateController;

    @BeforeEach
    void setUp() {
        setupMockMvc(referralCodeValidateController);
    }

    @Test
    @DisplayName("驗證推薦碼（POST）")
    void validateReferralCode_ShouldReturnResult() throws Exception {
        ReferralValidateRes mockRes = new ReferralValidateRes(true, "NEWUSER2026", "Test Store");
        when(referralCodeService.validateForRegistration(anyString())).thenReturn(mockRes);

        mockMvc.perform(post("/auth/validate-referral")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"NEWUSER2026\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("驗證推薦碼（GET）")
    void useReferralCode_ShouldReturn200() throws Exception {
        when(referralCodeService.validateCode(anyString())).thenReturn(true);

        mockMvc.perform(get("/auth/referral-code/validate/NEWUSER2026"))
                .andExpect(status().isOk());
    }
}
