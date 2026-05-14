package com.group.admin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group.admin.dto.res.report.PlatformRevenueReportRes;
import com.group.admin.mapper.ReportSnapshotMapper;
import com.group.admin.repository.ReportSnapshotRepository;
import com.group.admin.service.impl.ReportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportServiceImpl 單元測試")
@SuppressWarnings("deprecation")
class ReportServiceImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private ReportSnapshotMapper reportSnapshotMapper;

    @Mock
    private ReportSnapshotRepository reportSnapshotRepository;

    private ReportServiceImpl reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportServiceImpl(
                jdbcTemplate,
                reportSnapshotMapper,
                reportSnapshotRepository,
                new ObjectMapper());
    }

    @Test
    @DisplayName("queryTotalRecharge 應使用半開區間查詢")
    void queryTotalRecharge_ShouldUseHalfOpenInterval() {
        when(jdbcTemplate.queryForObject(anyString(), eq(BigDecimal.class), any(), any()))
                .thenReturn(new BigDecimal("123"));

        Long total = ReflectionTestUtils.invokeMethod(
                reportService,
                "queryTotalRecharge",
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 7));

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).queryForObject(sqlCaptor.capture(), eq(BigDecimal.class), any(), any());

        String sql = sqlCaptor.getValue();
        assertThat(total).isEqualTo(123L);
        assertThat(sql)
                .contains("created_at >= ?")
                .contains("created_at < ?")
                .doesNotContain("BETWEEN ? AND ?");
    }

    @Test
    @DisplayName("queryDailyAmountByType 應使用半開區間且保留 coin_type 條件")
    void queryDailyAmountByType_ShouldUseHalfOpenIntervalAndCoinFilter() {
        Map<LocalDate, Long> result = ReflectionTestUtils.invokeMethod(
                reportService,
                "queryDailyAmountByType",
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 7),
                "DRAW",
                "GOLD");

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> paramsCaptor = ArgumentCaptor.forClass(Object[].class);
        verify(jdbcTemplate).query(sqlCaptor.capture(), paramsCaptor.capture(), any(RowCallbackHandler.class));

        String sql = sqlCaptor.getValue();
        Object[] params = paramsCaptor.getValue();

        assertThat(result).isEmpty();
        assertThat(sql)
                .contains("created_at >= ?")
                .contains("created_at < ?")
                .contains("AND coin_type = ?")
                .doesNotContain("BETWEEN ? AND ?");
        assertThat(params).hasSize(4);
        assertThat(params[0]).isEqualTo("DRAW");
        assertThat(params[3]).isEqualTo("GOLD");
    }

    @Test
    @DisplayName("queryStoreBreakdown 應包含三路店家映射與半開區間")
    void queryStoreBreakdown_ShouldContainThreePathStoreMappingAndHalfOpenInterval() {
        List<PlatformRevenueReportRes.StoreBreakdownItem> result = ReflectionTestUtils.invokeMethod(
                reportService,
                "queryStoreBreakdown",
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 7));

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate, atLeast(2)).query(sqlCaptor.capture(), any(Object[].class), any(RowCallbackHandler.class));

        List<String> sqlList = sqlCaptor.getAllValues();
        String spendSql = sqlList.stream()
                .filter(sql -> sql.contains("ABS(SUM(mapped.amount))"))
                .findFirst()
                .orElse("");

        assertThat(result).isEmpty();
        assertThat(spendSql)
                .contains("COALESCE(")
                .contains("l_direct.store_id")
                .contains("l_ticket.store_id")
                .contains("o.store_id")
                .contains("LEFT JOIN lottery l_direct")
                .contains("LEFT JOIN lottery_ticket lt")
                .contains("LEFT JOIN lottery l_ticket")
                .contains("LEFT JOIN `order` o")
                .contains("wt.created_at >= ?")
                .contains("wt.created_at < ?")
                .doesNotContain("BETWEEN ? AND ?");
    }

    @Test
    @DisplayName("queryRevenueAmount 應改用 wallet_transaction 並採半開區間")
    void queryRevenueAmount_ShouldUseWalletTransactionAndHalfOpenInterval() {
        when(jdbcTemplate.queryForObject(anyString(), eq(BigDecimal.class), any(Object[].class)))
                .thenReturn(new BigDecimal("300"));

        BigDecimal total = ReflectionTestUtils.invokeMethod(
                reportService,
                "queryRevenueAmount",
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 7),
                "store-001");

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).queryForObject(sqlCaptor.capture(), eq(BigDecimal.class), any(Object[].class));

        String sql = sqlCaptor.getValue();
        assertThat(total).isEqualByComparingTo("300");
        assertThat(sql)
                .contains("FROM wallet_transaction wt")
                .contains("wt.transaction_type = 'DRAW'")
                .contains("wt.created_at >= ?")
                .contains("wt.created_at < ?")
                .contains("COALESCE(l_direct.store_id, l_ticket.store_id, o.store_id) = ?")
                .doesNotContain("total_amount")
                .doesNotContain("draw_count")
                .doesNotContain("BETWEEN ? AND ?");
    }

    @Test
    @DisplayName("queryRevenueDrawCount 應採半開區間且以 lottery_ticket 計算")
    void queryRevenueDrawCount_ShouldUseLotteryTicketAndHalfOpenInterval() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any(Object[].class)))
                .thenReturn(12);

        Integer total = ReflectionTestUtils.invokeMethod(
                reportService,
                "queryRevenueDrawCount",
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 7),
                "store-001");

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).queryForObject(sqlCaptor.capture(), eq(Integer.class), any(Object[].class));

        String sql = sqlCaptor.getValue();
        assertThat(total).isEqualTo(12);
        assertThat(sql)
                .contains("FROM lottery_ticket lt")
                .contains("lt.status = 'DRAWN'")
                .contains("lt.drawn_at >= ?")
                .contains("lt.drawn_at < ?")
                .doesNotContain("BETWEEN ? AND ?");
    }

    @Test
    @DisplayName("getRechargeReport 應使用 wallet_transaction 與半開區間")
    void getRechargeReport_ShouldUseWalletTransactionAndHalfOpenInterval() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any(), any()))
                .thenReturn(0);
        when(jdbcTemplate.queryForMap(anyString(), any(Object[].class)))
                .thenReturn(Map.of(
                        "total_amount", BigDecimal.ZERO,
                        "total_count", 0,
                        "avg_amount", BigDecimal.ZERO));
        when(jdbcTemplate.query(anyString(), any(Object[].class), any(org.springframework.jdbc.core.RowMapper.class)))
                .thenReturn(List.of());

        reportService.getRechargeReport(null);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate, atLeast(2)).query(sqlCaptor.capture(), any(Object[].class), any(org.springframework.jdbc.core.RowMapper.class));

        assertThat(sqlCaptor.getAllValues())
                .allSatisfy(sql -> assertThat(sql)
                        .contains("FROM wallet_transaction")
                        .contains("transaction_type = 'RECHARGE'")
                        .contains("created_at >= ?")
                        .contains("created_at < ?")
                        .doesNotContain("BETWEEN ? AND ?"));
    }

    @Test
    @DisplayName("會員成長支付型態分布應統計 recharge_order SUCCESS 狀態")
    void getMemberGrowthReport_ShouldUseRechargeOrderSuccessStatus() {
        String source = java.util.Arrays.stream(ReportServiceImpl.class.getDeclaredMethods())
                .filter(method -> "getMemberGrowthReport".equals(method.getName()))
                .findFirst()
                .map(method -> {
                    try {
                        return java.nio.file.Files.readString(java.nio.file.Path.of(
                                "src/main/java/com/group/admin/service/impl/ReportServiceImpl.java"));
                    } catch (java.io.IOException e) {
                        throw new IllegalStateException(e);
                    }
                })
                .orElse("");

        assertThat(source)
                .contains("FROM recharge_order")
                .contains("WHERE status = 'SUCCESS'")
                .doesNotContain("WHERE status = 'PAID'");
    }
}
