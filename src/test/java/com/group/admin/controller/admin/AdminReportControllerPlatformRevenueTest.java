package com.group.admin.controller.admin;

import com.group.admin.BaseControllerTest;
import com.group.admin.condition.report.PlatformRevenueReportCondition;
import com.group.admin.dto.res.report.PlatformRevenueReportRes;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("AdminReportController 平台營收總覽報表測試")
class AdminReportControllerPlatformRevenueTest extends BaseControllerTest {

    @Mock
    private ReportService reportService;

    @InjectMocks
    private AdminReportController adminReportController;

    @BeforeEach
    void setUp() {
        setupMockMvcWithExceptionHandler(adminReportController);
    }

    @Test
    @DisplayName("Admin 可查詢平台營收總覽")
    void adminCanQueryPlatformRevenue() throws Exception {
        setupAuthentication("admin-1", "ADMIN");
        when(reportService.getPlatformRevenueReport(any())).thenReturn(mockResponse());

        mockMvc.perform(post("/admin/report/platform-revenue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRecharge").value(50000))
                .andExpect(jsonPath("$.totalSpend").value(30000))
                .andExpect(jsonPath("$.netRevenue").value(20000))
                .andExpect(jsonPath("$.dailyRevenue.length()").value(2));
    }

    @Test
    @DisplayName("condition 為空時仍可使用預設日期")
    void nullConditionStillWorks() throws Exception {
        setupAuthentication("admin-1", "ADMIN");
        PlatformRevenueReportRes res = PlatformRevenueReportRes.builder()
                .startDate(LocalDate.now().minusDays(29))
                .endDate(LocalDate.now())
                .totalRecharge(0L)
                .totalSpend(0L)
                .netRevenue(0L)
                .drawCount(0L)
                .spendByType(PlatformRevenueReportRes.SpendByType.builder().gold(0L).bonus(0L).build())
                .dailyRevenue(List.of())
                .storeBreakdown(List.of())
                .build();
        when(reportService.getPlatformRevenueReport(any())).thenReturn(res);

        mockMvc.perform(post("/admin/report/platform-revenue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startDate").value(res.getStartDate().toString()))
                .andExpect(jsonPath("$.endDate").value(res.getEndDate().toString()));
    }

    @Test
    @DisplayName("storeBreakdown 與 growthRate 欄位應存在")
    void responseContainsStoreBreakdownAndGrowth() throws Exception {
        setupAuthentication("admin-1", "ADMIN");
        when(reportService.getPlatformRevenueReport(any())).thenReturn(mockResponse());

        QueryReq<PlatformRevenueReportCondition> req = new QueryReq<>();
        req.setCondition(new PlatformRevenueReportCondition());

        mockMvc.perform(post("/admin/report/platform-revenue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rechargeGrowthRate").value(25.0))
                .andExpect(jsonPath("$.spendGrowthRate").value(10.0))
                .andExpect(jsonPath("$.storeBreakdown.length()").value(1))
                .andExpect(jsonPath("$.storeBreakdown[0].storeId").value("store-1"))
                .andExpect(jsonPath("$.storeBreakdown[0].drawCount").value(300));
    }

    @Test
    @DisplayName("StoreOwner 不可查詢平台營收總覽")
    void storeOwnerForbidden() throws Exception {
        setupAuthentication("owner-1", "STORE_OWNER");

        mockMvc.perform(post("/admin/report/platform-revenue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());

        verify(reportService, never()).getPlatformRevenueReport(any());
    }

    private PlatformRevenueReportRes mockResponse() {
        return PlatformRevenueReportRes.builder()
                .startDate(LocalDate.of(2026, 4, 1))
                .endDate(LocalDate.of(2026, 4, 30))
                .totalRecharge(50000L)
                .totalSpend(30000L)
                .netRevenue(20000L)
                .drawCount(1500L)
                .spendByType(PlatformRevenueReportRes.SpendByType.builder()
                        .gold(25000L)
                        .bonus(5000L)
                        .build())
                .rechargeGrowthRate(new BigDecimal("25.0"))
                .spendGrowthRate(new BigDecimal("10.0"))
                .dailyRevenue(List.of(
                        PlatformRevenueReportRes.DailyRevenueItem.builder()
                                .date(LocalDate.of(2026, 4, 1))
                                .recharge(2000L)
                                .spend(1500L)
                                .net(500L)
                                .build(),
                        PlatformRevenueReportRes.DailyRevenueItem.builder()
                                .date(LocalDate.of(2026, 4, 2))
                                .recharge(0L)
                                .spend(0L)
                                .net(0L)
                                .build()
                ))
                .storeBreakdown(List.of(
                        PlatformRevenueReportRes.StoreBreakdownItem.builder()
                                .storeId("store-1")
                                .storeName("A店")
                                .totalSpend(15000L)
                                .drawCount(300L)
                                .build()
                ))
                .build();
    }
}
