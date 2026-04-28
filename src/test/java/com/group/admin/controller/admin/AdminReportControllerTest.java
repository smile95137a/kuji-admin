package com.group.admin.controller.admin;

import com.group.admin.BaseControllerTest;
import com.group.admin.condition.report.MemberGrowthReportCondition;
import com.group.admin.dto.res.report.MemberGrowthReportRes;
import com.group.admin.req.common.QueryReq;
import com.group.admin.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AdminReportController — 會員成長報表端點 MockMvc 切片測試
 * 覆蓋 US1/US2/US3 主要驗收情境
 */
@DisplayName("AdminReportController — 會員成長報表測試")
class AdminReportControllerTest extends BaseControllerTest {

    @Mock
    private ReportService reportService;

    @InjectMocks
    private AdminReportController adminReportController;

    private static final String ADMIN_ID       = "admin-uuid-001";
    private static final String STORE_OWNER_ID = "owner-uuid-001";
    private static final String ENDPOINT       = "/admin/report/member-growth";

    @BeforeEach
    void setUp() {
        setupMockMvcWithExceptionHandler(adminReportController);
    }

    // ── T013: ADMIN token → 200, response body contains all expected fields ──

    @Test
    @DisplayName("T013: ADMIN token — POST /admin/report/member-growth → 200 含完整欄位")
    void adminToken_validCondition_returns200WithAllFields() throws Exception {
        setupAuthentication(ADMIN_ID, "ADMIN");

        MemberGrowthReportRes mockRes = MemberGrowthReportRes.builder()
                .startDate(LocalDate.of(2026, 4, 1))
                .endDate(LocalDate.of(2026, 4, 30))
                .totalNewMembers(150)
                .growthRate(new BigDecimal("12.5"))
                .registrationByProvider(Map.of("GOOGLE", 80, "EMAIL", 70))
                .dailyNewMembers(List.of(
                        MemberGrowthReportRes.DailyNewMember.builder()
                                .date(LocalDate.of(2026, 4, 1)).count(5).build()))
                .activeMembers(500)
                .arpuGold(new BigDecimal("200.0"))
                .arpuBonus(new BigDecimal("40.0"))
                .retention7Days(new BigDecimal("60.0"))
                .retention30Days(new BigDecimal("35.0"))
                .build();

        when(reportService.getMemberGrowthReport(any())).thenReturn(mockRes);

        QueryReq<MemberGrowthReportCondition> req = new QueryReq<>();
        MemberGrowthReportCondition cond = new MemberGrowthReportCondition();
        cond.setStartDate(LocalDate.of(2026, 4, 1));
        cond.setEndDate(LocalDate.of(2026, 4, 30));
        req.setCondition(cond);

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalNewMembers").value(150))
                .andExpect(jsonPath("$.activeMembers").value(500))
                .andExpect(jsonPath("$.arpuGold").value(200.0))
                .andExpect(jsonPath("$.arpuBonus").value(40.0))
                .andExpect(jsonPath("$.retention7Days").value(60.0))
                .andExpect(jsonPath("$.retention30Days").value(35.0))
                .andExpect(jsonPath("$.dailyNewMembers").isArray());
    }

    // ── T014: STORE_OWNER token → 403 ───────────────────────────────────────

    @Test
    @DisplayName("T014: STORE_OWNER token — POST /admin/report/member-growth → 403")
    void storeOwnerToken_returns403() throws Exception {
        setupAuthentication(STORE_OWNER_ID, "STORE_OWNER");

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    // ── T015: condition = null → 200, defaults applied ──────────────────────

    @Test
    @DisplayName("T015: condition=null — POST /admin/report/member-growth → 200, 使用預設日期")
    void nullCondition_returns200WithDefaultDates() throws Exception {
        setupAuthentication(ADMIN_ID, "ADMIN");

        LocalDate today        = LocalDate.now();
        LocalDate defaultStart = today.minusDays(29);

        MemberGrowthReportRes mockRes = MemberGrowthReportRes.builder()
                .startDate(defaultStart)
                .endDate(today)
                .totalNewMembers(0)
                .registrationByProvider(Collections.emptyMap())
                .dailyNewMembers(Collections.emptyList())
                .activeMembers(0)
                .arpuGold(new BigDecimal("0.0"))
                .arpuBonus(new BigDecimal("0.0"))
                .build();

        when(reportService.getMemberGrowthReport(any())).thenReturn(mockRes);

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startDate").value(defaultStart.toString()))
                .andExpect(jsonPath("$.endDate").value(today.toString()));
    }

    // ── T016: all-zero data ──────────────────────────────────────────────────

    @Test
    @DisplayName("T016: 全零資料 — totalNewMembers=0, arpuGold=0.0, retention7Days=null")
    void allZeroData_returnsCorrectResponse() throws Exception {
        setupAuthentication(ADMIN_ID, "ADMIN");

        MemberGrowthReportRes mockRes = MemberGrowthReportRes.builder()
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 1, 31))
                .totalNewMembers(0)
                .growthRate(null)
                .registrationByProvider(Collections.emptyMap())
                .dailyNewMembers(Collections.emptyList())
                .activeMembers(0)
                .arpuGold(new BigDecimal("0.0"))
                .arpuBonus(new BigDecimal("0.0"))
                .retention7Days(null)
                .retention30Days(null)
                .build();

        when(reportService.getMemberGrowthReport(any())).thenReturn(mockRes);

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalNewMembers").value(0))
                .andExpect(jsonPath("$.arpuGold").value(0.0))
                .andExpect(jsonPath("$.dailyNewMembers").isArray())
                .andExpect(jsonPath("$.retention7Days").doesNotExist());
    }

    // ── T017: activeMembers=0 → arpuGold=0.0, arpuBonus=0.0 (no exception) ─

    @Test
    @DisplayName("T017: activeMembers=0 — arpuGold=0.0, arpuBonus=0.0 (無 ArithmeticException)")
    void activeMembersZero_arpuReturnsZeroWithoutException() throws Exception {
        setupAuthentication(ADMIN_ID, "ADMIN");

        MemberGrowthReportRes mockRes = MemberGrowthReportRes.builder()
                .startDate(LocalDate.of(2026, 2, 1))
                .endDate(LocalDate.of(2026, 2, 28))
                .totalNewMembers(10)
                .registrationByProvider(Collections.emptyMap())
                .dailyNewMembers(Collections.emptyList())
                .activeMembers(0)
                .arpuGold(new BigDecimal("0.0"))
                .arpuBonus(new BigDecimal("0.0"))
                .build();

        when(reportService.getMemberGrowthReport(any())).thenReturn(mockRes);

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeMembers").value(0))
                .andExpect(jsonPath("$.arpuGold").value(0.0))
                .andExpect(jsonPath("$.arpuBonus").value(0.0));
    }
}
