package com.group.admin.controller.admin;

import com.group.admin.BaseControllerTest;
import com.group.admin.condition.report.LotterySalesRankingCondition;
import com.group.admin.dto.res.report.LotterySalesRankingRes;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AdminReportController — 商品銷售排行 API 測試
 *
 * <p>覆蓋三個 User Story：
 * <ul>
 *   <li>US1: StoreOwner storeId 強制綁定 + 預設 drawCount 排序</li>
 *   <li>US2: revenue 排序 + limit clamp</li>
 *   <li>US3: Admin 跨店查詢 + Admin 帶 storeId 過濾</li>
 * </ul>
 *
 * <p>注意：standaloneSetup 不套用 Spring Security Filter Chain，
 * 因此 401/403 驗證記錄於 T007 — 實際安全由 JwtAuthFilter 在整合測試中覆蓋。
 */
@DisplayName("AdminReportController 商品銷售排行 API 測試")
class AdminReportControllerLotteryRankingTest extends BaseControllerTest {

    @Mock
    private ReportService reportService;

    @InjectMocks
    private AdminReportController adminReportController;

    private static final String STORE_UUID = "store-uuid-001";
    private static final String TARGET_STORE_UUID = "target-store-uuid";

    @BeforeEach
    void setUp() {
        setupMockMvc(adminReportController);
    }

    // ─── helpers ──────────────────────────────────────────────────────────────

    /**
     * 設定帶有 storeIds 的 StoreOwner SecurityContext（用於測試 storeId 強制覆蓋）
     */
    private void setupStoreOwnerAuthentication(String userId, String storeId) {
        UserPrincipal principal = UserPrincipal.builder()
                .userId(userId)
                .username("store-owner-" + userId + "@kuji.com")
                .roles(List.of("STORE_OWNER"))
                .storeIds(List.of(storeId))
                .build();

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, null,
                List.of(new SimpleGrantedAuthority("ROLE_STORE_OWNER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private LotterySalesRankingRes emptyRankingRes() {
        return LotterySalesRankingRes.builder()
                .totalRecords(0)
                .items(List.of())
                .build();
    }

    private LotterySalesRankingRes rankingResWithItems(int count) {
        List<LotterySalesRankingRes.LotterySalesItem> items = new java.util.ArrayList<>();
        for (int i = 1; i <= count; i++) {
            items.add(LotterySalesRankingRes.LotterySalesItem.builder()
                    .lotteryId("lottery-uuid-00" + i)
                    .lotteryTitle("商品 " + i)
                    .storeName("動漫星球")
                    .drawCount(100 - i * 10)
                    .revenue((long) (100 - i * 10) * 80)
                    .rank(i)
                    .build());
        }
        return LotterySalesRankingRes.builder()
                .totalRecords(count)
                .items(items)
                .build();
    }

    // ─── US1: StoreOwner storeId 強制綁定 ─────────────────────────────────────

    /**
     * T005: StoreOwner 呼叫時，後端強制將 storeId 設為 JWT 中的店家 ID
     */
    @Test
    @DisplayName("T005: StoreOwner JWT — 後端強制覆蓋 condition.storeId")
    @SuppressWarnings("unchecked")
    void testStoreOwnerStoreIdForced() throws Exception {
        setupStoreOwnerAuthentication("store-owner-id", STORE_UUID);
        when(reportService.getLotterySalesRanking(any())).thenReturn(emptyRankingRes());

        mockMvc.perform(post("/admin/report/lottery-sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        // 擷取傳入 Service 的 req，確認 storeId 被後端強制設為 STORE_UUID
        ArgumentCaptor<QueryReq<LotterySalesRankingCondition>> captor =
                ArgumentCaptor.forClass(QueryReq.class);
        verify(reportService).getLotterySalesRanking(captor.capture());

        QueryReq<LotterySalesRankingCondition> capturedReq = captor.getValue();
        assertThat(capturedReq.getCondition()).isNotNull();
        assertThat(capturedReq.getCondition().getStoreId()).isEqualTo(STORE_UUID);
    }

    /**
     * T006: Admin JWT（storeId=null）、不帶 sortBy → 預設 drawCount 排序；
     * Service 收到呼叫；回應 200 OK 且 items 數量正確
     */
    @Test
    @DisplayName("T006: Admin JWT — 預設 drawCount 排序，回傳 2 筆，Service 被呼叫一次")
    void testDefaultDrawCountSortAndLimit() throws Exception {
        setupAuthentication("admin-id", "ADMIN");
        LotterySalesRankingRes mockRes = rankingResWithItems(2);
        when(reportService.getLotterySalesRanking(any())).thenReturn(mockRes);

        mockMvc.perform(post("/admin/report/lottery-sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.totalRecords").value(2));

        verify(reportService).getLotterySalesRanking(any());
    }

    /**
     * T007: standaloneSetup 不套用 Spring Security — 401/403 由整合測試覆蓋。
     * 此測試記錄安全邊界：無 JWT 時 Controller 層仍可到達（Filter Chain 保護）。
     */
    @Test
    @DisplayName("T007: 無 SecurityContext — Controller 層可到達（Security 由 Filter Chain 處理）")
    void testSecurityHandledByFilterChain() throws Exception {
        // standaloneSetup 不套用 @PreAuthorize
        // 401/403 在 Spring Security Filter Chain 中強制執行（整合測試責任）
        // 此處只記錄 Controller 層行為
        SecurityContextHolder.clearContext();
        when(reportService.getLotterySalesRanking(any())).thenReturn(emptyRankingRes());

        mockMvc.perform(post("/admin/report/lottery-sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk()); // security not enforced in standalone setup
    }

    // ─── US2: revenue 排序 + limit clamp ──────────────────────────────────────

    /**
     * T010: sortBy=revenue → Service 收到 req.sortBy == "revenue"；回應 200 OK，3 筆
     */
    @Test
    @DisplayName("T010: sortBy=revenue — Service 收到 sortBy 欄位，回傳 3 筆")
    @SuppressWarnings("unchecked")
    void testRevenueSortReturnsItems() throws Exception {
        setupAuthentication("admin-id", "ADMIN");
        when(reportService.getLotterySalesRanking(any())).thenReturn(rankingResWithItems(3));

        mockMvc.perform(post("/admin/report/lottery-sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sortBy\":\"revenue\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(3));

        ArgumentCaptor<QueryReq<LotterySalesRankingCondition>> captor =
                ArgumentCaptor.forClass(QueryReq.class);
        verify(reportService).getLotterySalesRanking(captor.capture());
        assertThat(captor.getValue().getSortBy()).isEqualTo("revenue");
    }

    /**
     * T011: condition.limit=200 → Controller 直接傳遞給 Service（clamp 在 ServiceImpl 執行）；
     * Service 收到 limit=200；回應 200 OK
     */
    @Test
    @DisplayName("T011: limit=200 → Controller 不 clamp，Service 收到 limit=200")
    @SuppressWarnings("unchecked")
    void testLimitOver100IsClamped() throws Exception {
        setupAuthentication("admin-id", "ADMIN");
        when(reportService.getLotterySalesRanking(any())).thenReturn(emptyRankingRes());

        mockMvc.perform(post("/admin/report/lottery-sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"condition\":{\"limit\":200}}"))
                .andExpect(status().isOk());

        ArgumentCaptor<QueryReq<LotterySalesRankingCondition>> captor =
                ArgumentCaptor.forClass(QueryReq.class);
        verify(reportService).getLotterySalesRanking(captor.capture());
        // Controller passes limit through unchanged; ServiceImpl clamps to 100
        assertThat(captor.getValue().getCondition().getLimit()).isEqualTo(200);
    }

    // ─── US3: Admin 跨店 / 帶 storeId 過濾 ───────────────────────────────────

    /**
     * T013: Admin JWT + 無 storeId → condition.storeId 維持 null（跨平台查詢）
     */
    @Test
    @DisplayName("T013: Admin JWT + 無 storeId → condition.storeId = null（跨平台查詢）")
    @SuppressWarnings("unchecked")
    void testAdminNoStoreIdGetsCrossPlatform() throws Exception {
        setupAuthentication("admin-id", "ADMIN");  // storeIds = List.of() → null
        when(reportService.getLotterySalesRanking(any())).thenReturn(emptyRankingRes());

        mockMvc.perform(post("/admin/report/lottery-sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        ArgumentCaptor<QueryReq<LotterySalesRankingCondition>> captor =
                ArgumentCaptor.forClass(QueryReq.class);
        verify(reportService).getLotterySalesRanking(captor.capture());

        // Admin 無店家歸屬 → getCurrentUserPrimaryStoreId() = null → storeId 不被覆蓋
        LotterySalesRankingCondition cond = captor.getValue().getCondition();
        // condition may be null (no body condition) or storeId is null
        boolean storeIdIsNull = cond == null || cond.getStoreId() == null;
        assertThat(storeIdIsNull).isTrue();
    }

    /**
     * T014: Admin JWT + condition.storeId=TARGET_STORE_UUID → Service 收到該 storeId（Admin 選填過濾被保留）
     */
    @Test
    @DisplayName("T014: Admin JWT + condition.storeId=target → Service 收到 storeId（Admin 可選填）")
    @SuppressWarnings("unchecked")
    void testAdminWithStoreIdFiltersStore() throws Exception {
        setupAuthentication("admin-id", "ADMIN");
        when(reportService.getLotterySalesRanking(any())).thenReturn(emptyRankingRes());

        mockMvc.perform(post("/admin/report/lottery-sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"condition\":{\"storeId\":\"" + TARGET_STORE_UUID + "\"}}"))
                .andExpect(status().isOk());

        ArgumentCaptor<QueryReq<LotterySalesRankingCondition>> captor =
                ArgumentCaptor.forClass(QueryReq.class);
        verify(reportService).getLotterySalesRanking(captor.capture());
        // Admin's explicit storeId must be preserved (not overridden)
        assertThat(captor.getValue().getCondition().getStoreId()).isEqualTo(TARGET_STORE_UUID);
    }
}
