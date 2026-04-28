package com.group.admin.controller.admin;

import com.group.admin.BaseControllerTest;
import com.group.admin.condition.report.StorePerformanceCondition;
import com.group.admin.dto.res.report.StorePerformanceReportRes;
import com.group.admin.req.common.QueryReq;
import com.group.admin.security.UserPrincipal;
import com.group.admin.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AdminReportController — 店家績效報表 MockMvc 單元測試
 * 覆蓋 US1（Admin 全店）、US3（Admin 單店 + dailyStats、StoreOwner 存取控制）、
 * 及 sortBy 白名單 fallback。
 */
@DisplayName("AdminReportController — 店家績效報表測試")
class AdminReportControllerTest extends BaseControllerTest {

    @Mock
    private ReportService reportService;

    @InjectMocks
    private AdminReportController adminReportController;

    private static final String ADMIN_ID      = "admin-uuid-001";
    private static final String STORE_ID_A    = "store-uuid-a";
    private static final String STORE_ID_B    = "store-uuid-b";

    @BeforeEach
    void setUp() {
        setupMockMvcWithExceptionHandler(adminReportController);
    }

    // ──────────────────────────────────────────────────────────────
    // Helper: 設定 StoreOwner SecurityContext（含 storeIds）
    // ──────────────────────────────────────────────────────────────

    private void setupStoreOwnerAuthentication(String userId, String storeId) {
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_STORE_OWNER"));
        UserPrincipal principal = UserPrincipal.builder()
                .userId(userId)
                .username("storeowner@kuji.com")
                .roles(List.of("STORE_OWNER"))
                .storeIds(List.of(storeId))
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, authorities));
    }

    // ──────────────────────────────────────────────────────────────
    // T017: Admin 查詢全部店家 → HTTP 200，stores 2 筆，dailyStats null
    // ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("T017: Admin 全店查詢 → 200，stores.length==2，dailyStats==null")
    void storePerformance_Admin_AllStores_Returns200() throws Exception {
        setupAuthentication(ADMIN_ID, "ADMIN");

        StorePerformanceReportRes stubRes = StorePerformanceReportRes.builder()
                .startDate(LocalDate.of(2026, 4, 1))
                .endDate(LocalDate.of(2026, 4, 30))
                .stores(List.of(
                        StorePerformanceReportRes.StoreItem.builder()
                                .storeId(STORE_ID_A).storeName("A店")
                                .totalRevenue(15000L).drawCount(300).activeUsers(85)
                                .shipRate(92.0).overdueRate(3.3).avgShipDays(null).build(),
                        StorePerformanceReportRes.StoreItem.builder()
                                .storeId(STORE_ID_B).storeName("B店")
                                .totalRevenue(8200L).drawCount(180).activeUsers(42)
                                .shipRate(75.0).overdueRate(20.0).avgShipDays(null).build()
                ))
                .dailyStats(null)
                .build();

        when(reportService.getStorePerformanceReport(any())).thenReturn(stubRes);

        QueryReq<StorePerformanceCondition> req = new QueryReq<>();
        req.setCondition(new StorePerformanceCondition());

        mockMvc.perform(post("/admin/report/store-performance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stores.length()").value(2))
                .andExpect(jsonPath("$.dailyStats", nullValue()));

        verify(reportService).getStorePerformanceReport(any());
    }

    // ──────────────────────────────────────────────────────────────
    // T018: Admin 帶 storeId → 200，dailyStats 非 null
    // ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("T018: Admin 帶 storeId → 200，dailyStats 非 null")
    void storePerformance_Admin_SingleStore_ReturnsDailyStats() throws Exception {
        setupAuthentication(ADMIN_ID, "ADMIN");

        StorePerformanceReportRes stubRes = StorePerformanceReportRes.builder()
                .startDate(LocalDate.of(2026, 4, 1))
                .endDate(LocalDate.of(2026, 4, 30))
                .stores(List.of(
                        StorePerformanceReportRes.StoreItem.builder()
                                .storeId(STORE_ID_A).storeName("A店")
                                .totalRevenue(15000L).drawCount(300).activeUsers(85)
                                .shipRate(92.0).overdueRate(3.3).avgShipDays(null).build()
                ))
                .dailyStats(List.of(
                        StorePerformanceReportRes.DailyStat.builder()
                                .date(LocalDate.of(2026, 4, 1)).drawCount(12).revenue(600L).newUsers(3).build(),
                        StorePerformanceReportRes.DailyStat.builder()
                                .date(LocalDate.of(2026, 4, 2)).drawCount(8).revenue(400L).newUsers(1).build()
                ))
                .build();

        when(reportService.getStorePerformanceReport(any())).thenReturn(stubRes);

        StorePerformanceCondition cond = new StorePerformanceCondition();
        cond.setStoreId(STORE_ID_A);
        QueryReq<StorePerformanceCondition> req = new QueryReq<>();
        req.setCondition(cond);

        mockMvc.perform(post("/admin/report/store-performance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stores.length()").value(1))
                .andExpect(jsonPath("$.dailyStats").isArray())
                .andExpect(jsonPath("$.dailyStats.length()").value(2));

        verify(reportService).getStorePerformanceReport(any());
    }

    // ──────────────────────────────────────────────────────────────
    // T019a: StoreOwner 查詢自己的店 → 200
    // ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("T019a: StoreOwner 查詢自己的店 → 200")
    void storePerformance_StoreOwner_OwnStore_Returns200() throws Exception {
        setupStoreOwnerAuthentication("owner-uuid-001", STORE_ID_A);

        StorePerformanceReportRes stubRes = StorePerformanceReportRes.builder()
                .startDate(LocalDate.now().minusDays(30))
                .endDate(LocalDate.now())
                .stores(List.of(
                        StorePerformanceReportRes.StoreItem.builder()
                                .storeId(STORE_ID_A).storeName("A店")
                                .totalRevenue(5000L).drawCount(100).activeUsers(30)
                                .shipRate(80.0).overdueRate(5.0).avgShipDays(null).build()
                ))
                .dailyStats(null)
                .build();

        when(reportService.getStorePerformanceReport(any())).thenReturn(stubRes);

        // StoreOwner 帶自己的 storeId
        StorePerformanceCondition cond = new StorePerformanceCondition();
        cond.setStoreId(STORE_ID_A);
        QueryReq<StorePerformanceCondition> req = new QueryReq<>();
        req.setCondition(cond);

        mockMvc.perform(post("/admin/report/store-performance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isOk());

        verify(reportService).getStorePerformanceReport(any());
    }

    // ──────────────────────────────────────────────────────────────
    // T019b: StoreOwner 查詢其他店 → 403
    // ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("T019b: StoreOwner 查詢其他店 → 403")
    void storePerformance_StoreOwner_OtherStore_Returns403() throws Exception {
        setupStoreOwnerAuthentication("owner-uuid-001", STORE_ID_A);

        // 請求帶入其他店的 storeId
        StorePerformanceCondition cond = new StorePerformanceCondition();
        cond.setStoreId(STORE_ID_B); // 不同於 owner 的 STORE_ID_A
        QueryReq<StorePerformanceCondition> req = new QueryReq<>();
        req.setCondition(cond);

        mockMvc.perform(post("/admin/report/store-performance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isForbidden());
    }

    // ──────────────────────────────────────────────────────────────
    // T020: sortBy 非白名單值 → fallback，不拋錯，200
    // ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("T020: sortBy 非白名單值 → 200（silent fallback to totalRevenue）")
    void storePerformance_InvalidSortBy_FallsBackToTotalRevenue() throws Exception {
        setupAuthentication(ADMIN_ID, "ADMIN");

        StorePerformanceReportRes stubRes = StorePerformanceReportRes.builder()
                .startDate(LocalDate.now().minusDays(30))
                .endDate(LocalDate.now())
                .stores(List.of())
                .dailyStats(null)
                .build();

        when(reportService.getStorePerformanceReport(any())).thenReturn(stubRes);

        QueryReq<StorePerformanceCondition> req = new QueryReq<>();
        req.setCondition(new StorePerformanceCondition());
        req.setSortBy("invalidField"); // 非白名單值，應 fallback 不丟錯

        mockMvc.perform(post("/admin/report/store-performance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isOk());

        verify(reportService).getStorePerformanceReport(any());
    }
}
