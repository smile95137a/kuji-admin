package com.group.admin.controller.admin;

import com.group.admin.BaseControllerTest;
import com.group.admin.condition.report.PrizeShipmentReportCondition;
import com.group.admin.dto.res.report.PrizeShipmentReportRes;
import com.group.admin.req.common.QueryReq;
import com.group.admin.security.UserPrincipal;
import com.group.admin.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AdminReportController — 獎品出貨報表端點 MockMvc 切片測試
 *
 * 測試覆蓋：
 *   T009 - givenStoreOwner_onlySeesOwnStore
 *   T010 - givenNoOrders_returnsAllZeros
 *   T013 - givenValidShippedOrders_avgShipDaysCorrect
 *   T014 - givenOverdueOrders_overdueCountCorrect
 *   T015 - givenNullDates_defaultsToLast30Days
 *   T018 - givenAdmin_returnsStoreDetails
 */
@DisplayName("AdminReportController — 獎品出貨報表測試")
class AdminReportControllerPrizeShipmentTest extends BaseControllerTest {

    @Mock
    private ReportService reportService;

    @InjectMocks
    private AdminReportController adminReportController;

    private static final String ADMIN_ID      = "admin-uuid-001";
    private static final String STORE_OWNER_ID = "store-owner-uuid-001";
    private static final String STORE_ID      = "store-uuid-A";

    @BeforeEach
    void setUp() {
        setupMockMvcWithExceptionHandler(adminReportController);
    }

    // ===== T009: StoreOwner storeId 強制覆蓋，storeDetails 為 null =====

    @Test
    @DisplayName("T009: StoreOwner 呼叫 — storeId 被後端覆蓋，storeDetails 為 null")
    void givenStoreOwner_onlySeesOwnStore() throws Exception {
        // 建立帶有 storeIds 的 StoreOwner principal
        setupStoreOwnerAuthentication(STORE_OWNER_ID, STORE_ID);

        PrizeShipmentReportRes mockRes = PrizeShipmentReportRes.builder()
                .startDate(LocalDate.now().minusDays(29))
                .endDate(LocalDate.now())
                .pendingCount(2)
                .preparingCount(1)
                .shippedCount(3)
                .completedCount(5)
                .avgShipDays(new BigDecimal("3.5"))
                .overdueCount(1)
                .dailyDetails(new ArrayList<>())
                .storeDetails(null)   // StoreOwner 不回傳 storeDetails
                .build();

        when(reportService.getPrizeShipmentReport(any())).thenReturn(mockRes);

        // 捕捉傳入 service 的請求條件
        @SuppressWarnings("unchecked")
        ArgumentCaptor<QueryReq<PrizeShipmentReportCondition>> captor =
                ArgumentCaptor.forClass(QueryReq.class);

        mockMvc.perform(post("/admin/report/prize-shipment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storeDetails").doesNotExist());

        verify(reportService).getPrizeShipmentReport(captor.capture());

        // 後端必須強制將 storeId 設定為 JWT 中的 store-uuid-A
        QueryReq<PrizeShipmentReportCondition> captured = captor.getValue();
        assertThat(captured).isNotNull();
        assertThat(captured.getCondition()).isNotNull();
        assertThat(captured.getCondition().getStoreId()).isEqualTo(STORE_ID);
    }

    // ===== T010: 無訂單時回傳全零 =====

    @Test
    @DisplayName("T010: 無訂單時 — 4 個計數全 0，dailyDetails 為空陣列，avgShipDays 為 null")
    void givenNoOrders_returnsAllZeros() throws Exception {
        setupAuthentication(ADMIN_ID, "ADMIN");

        PrizeShipmentReportRes mockRes = PrizeShipmentReportRes.builder()
                .startDate(LocalDate.of(2026, 4, 1))
                .endDate(LocalDate.of(2026, 4, 30))
                .pendingCount(0)
                .preparingCount(0)
                .shippedCount(0)
                .completedCount(0)
                .avgShipDays(null)
                .overdueCount(0)
                .dailyDetails(new ArrayList<>())
                .storeDetails(null)
                .build();

        when(reportService.getPrizeShipmentReport(any())).thenReturn(mockRes);

        mockMvc.perform(post("/admin/report/prize-shipment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "condition": {
                                    "startDate": "2026-04-01",
                                    "endDate":   "2026-04-30"
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingCount").value(0))
                .andExpect(jsonPath("$.preparingCount").value(0))
                .andExpect(jsonPath("$.shippedCount").value(0))
                .andExpect(jsonPath("$.completedCount").value(0))
                .andExpect(jsonPath("$.avgShipDays").doesNotExist())
                .andExpect(jsonPath("$.overdueCount").value(0))
                .andExpect(jsonPath("$.dailyDetails").isArray())
                .andExpect(jsonPath("$.dailyDetails").isEmpty());
    }

    // ===== T013: avgShipDays 正確序列化 =====

    @Test
    @DisplayName("T013: 3 筆已出貨訂單（2/4/6 天）— avgShipDays = 4.0")
    void givenValidShippedOrders_avgShipDaysCorrect() throws Exception {
        setupAuthentication(ADMIN_ID, "ADMIN");

        PrizeShipmentReportRes mockRes = PrizeShipmentReportRes.builder()
                .startDate(LocalDate.of(2026, 4, 1))
                .endDate(LocalDate.of(2026, 4, 30))
                .pendingCount(0)
                .preparingCount(0)
                .shippedCount(3)
                .completedCount(0)
                .avgShipDays(new BigDecimal("4.0"))
                .overdueCount(0)
                .dailyDetails(new ArrayList<>())
                .storeDetails(null)
                .build();

        when(reportService.getPrizeShipmentReport(any())).thenReturn(mockRes);

        mockMvc.perform(post("/admin/report/prize-shipment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "condition": {
                                    "startDate": "2026-04-01",
                                    "endDate":   "2026-04-30"
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avgShipDays").value(4.0))
                .andExpect(jsonPath("$.shippedCount").value(3));
    }

    // ===== T014: overdueCount 正確回傳 =====

    @Test
    @DisplayName("T014: 2 筆超過 7 天 PENDING 訂單 — overdueCount = 2")
    void givenOverdueOrders_overdueCountCorrect() throws Exception {
        setupAuthentication(ADMIN_ID, "ADMIN");

        PrizeShipmentReportRes mockRes = PrizeShipmentReportRes.builder()
                .startDate(LocalDate.of(2026, 4, 1))
                .endDate(LocalDate.of(2026, 4, 30))
                .pendingCount(2)
                .preparingCount(0)
                .shippedCount(0)
                .completedCount(0)
                .avgShipDays(null)
                .overdueCount(2)
                .dailyDetails(new ArrayList<>())
                .storeDetails(null)
                .build();

        when(reportService.getPrizeShipmentReport(any())).thenReturn(mockRes);

        mockMvc.perform(post("/admin/report/prize-shipment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "condition": {
                                    "startDate": "2026-04-01",
                                    "endDate":   "2026-04-30"
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overdueCount").value(2));
    }

    // ===== T015: 空 body → 預設最近 30 天 =====

    @Test
    @DisplayName("T015: 空 body {} — service 收到的 startDate = today-29，endDate = today")
    void givenNullDates_defaultsToLast30Days() throws Exception {
        setupAuthentication(ADMIN_ID, "ADMIN");

        LocalDate today = LocalDate.now();
        LocalDate expectedStart = today.minusDays(29);

        PrizeShipmentReportRes mockRes = PrizeShipmentReportRes.builder()
                .startDate(expectedStart)
                .endDate(today)
                .pendingCount(0)
                .preparingCount(0)
                .shippedCount(0)
                .completedCount(0)
                .avgShipDays(null)
                .overdueCount(0)
                .dailyDetails(new ArrayList<>())
                .storeDetails(null)
                .build();

        when(reportService.getPrizeShipmentReport(any())).thenReturn(mockRes);

        mockMvc.perform(post("/admin/report/prize-shipment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startDate").value(expectedStart.toString()))
                .andExpect(jsonPath("$.endDate").value(today.toString()));
    }

    // ===== T018: Admin 查詢回傳 storeDetails =====

    @Test
    @DisplayName("T018: Admin 查詢（無 storeId）— 回傳 storeDetails 陣列含各店統計")
    void givenAdmin_returnsStoreDetails() throws Exception {
        setupAuthentication(ADMIN_ID, "ADMIN");

        List<PrizeShipmentReportRes.StoreShipment> storeDetails = List.of(
                PrizeShipmentReportRes.StoreShipment.builder()
                        .storeId("store-uuid-A")
                        .storeName("A 店")
                        .pendingCount(2)
                        .preparingCount(1)
                        .shippedCount(4)
                        .completedCount(8)
                        .avgShipDays(new BigDecimal("3.5"))
                        .overdueCount(0)
                        .build(),
                PrizeShipmentReportRes.StoreShipment.builder()
                        .storeId("store-uuid-B")
                        .storeName("B 店")
                        .pendingCount(3)
                        .preparingCount(2)
                        .shippedCount(4)
                        .completedCount(2)
                        .avgShipDays(new BigDecimal("12.0"))
                        .overdueCount(2)
                        .build()
        );

        PrizeShipmentReportRes mockRes = PrizeShipmentReportRes.builder()
                .startDate(LocalDate.of(2026, 4, 1))
                .endDate(LocalDate.of(2026, 4, 30))
                .pendingCount(5)
                .preparingCount(3)
                .shippedCount(8)
                .completedCount(10)
                .avgShipDays(new BigDecimal("7.5"))
                .overdueCount(2)
                .dailyDetails(new ArrayList<>())
                .storeDetails(storeDetails)
                .build();

        when(reportService.getPrizeShipmentReport(any())).thenReturn(mockRes);

        mockMvc.perform(post("/admin/report/prize-shipment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "condition": {
                                    "startDate": "2026-04-01",
                                    "endDate":   "2026-04-30"
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storeDetails").isArray())
                .andExpect(jsonPath("$.storeDetails.length()").value(2))
                .andExpect(jsonPath("$.storeDetails[0].storeName").value("A 店"))
                .andExpect(jsonPath("$.storeDetails[0].avgShipDays").value(3.5))
                .andExpect(jsonPath("$.storeDetails[0].overdueCount").value(0))
                .andExpect(jsonPath("$.storeDetails[1].storeName").value("B 店"))
                .andExpect(jsonPath("$.storeDetails[1].avgShipDays").value(12.0))
                .andExpect(jsonPath("$.storeDetails[1].overdueCount").value(2));
    }

    // ===== Helper: set up StoreOwner with storeIds =====

    /**
     * 設定 StoreOwner 的 SecurityContext，帶有真實的 storeIds（讓 SecurityUtils 能取得 storeId）
     */
    private void setupStoreOwnerAuthentication(String userId, String storeId) {
        UserPrincipal principal = UserPrincipal.builder()
                .userId(userId)
                .username("test-" + userId + "@kuji.com")
                .roles(List.of("STORE_OWNER"))
                .storeIds(List.of(storeId))
                .build();

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_STORE_OWNER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
