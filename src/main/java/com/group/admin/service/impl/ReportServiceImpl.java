package com.group.admin.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group.admin.condition.report.*;
import com.group.admin.dto.res.report.*;
import com.group.admin.entity.ReportSnapshot;
import com.group.admin.mapper.ReportSnapshotMapper;
import com.group.admin.repository.ReportSnapshotRepository;
import com.group.admin.req.common.QueryReq;
import com.group.admin.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 報表服務實作
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
    
    private static final String MYSQL_TEXT_COLLATION = "utf8mb4_unicode_ci";
    private static final int MYSQL_TEXT_LENGTH = 64;

    private final JdbcTemplate jdbcTemplate;
    private final ReportSnapshotMapper reportSnapshotMapper;
    private final ReportSnapshotRepository reportSnapshotRepository;
    private final ObjectMapper objectMapper;
    
    @Override
    public RevenueReportRes getRevenueReport(QueryReq<RevenueReportCondition> req) {
        RevenueReportCondition condition = req != null && req.getCondition() != null ? 
            req.getCondition() : new RevenueReportCondition();
        
        String storeId = condition.getStoreId();
        LocalDate startDate = condition.getStartDate();
        LocalDate endDate = condition.getEndDate();
        
        log.info("📊 產生營業額報表: storeId={}, {} ~ {}", storeId, startDate, endDate);
        
        // 基本統計
        String baseSql = """
            SELECT 
                COALESCE(SUM(total_amount), 0) as total_revenue,
                COUNT(*) as total_orders,
                COALESCE(SUM(draw_count), 0) as total_draws,
                COALESCE(AVG(total_amount), 0) as avg_amount
            FROM `order`
            WHERE status IN ('PAID', 'COMPLETED')
            AND created_at BETWEEN ? AND ?
            """;
        
        List<Object> params = new ArrayList<>();
        params.add(startDate.atStartOfDay());
        params.add(endDate.plusDays(1).atStartOfDay());
        
        if (storeId != null) {
            baseSql += " AND store_id = ?";
            params.add(storeId);
        }
        
        Map<String, Object> stats = jdbcTemplate.queryForMap(baseSql, params.toArray());
        
        // 計算成長率（與上期比較）
        LocalDate prevStartDate = startDate.minusDays(startDate.until(endDate).getDays() + 1);
        LocalDate prevEndDate = startDate.minusDays(1);
        
        List<Object> prevParams = new ArrayList<>();
        prevParams.add(prevStartDate.atStartOfDay());
        prevParams.add(prevEndDate.plusDays(1).atStartOfDay());
        if (storeId != null) prevParams.add(storeId);
        
        Map<String, Object> prevStats = jdbcTemplate.queryForMap(
            baseSql.replace("? AND ?", "? AND ?"), prevParams.toArray());
        
        BigDecimal currentRevenue = toBigDecimal(stats.get("total_revenue"));
        BigDecimal prevRevenue = toBigDecimal(prevStats.get("total_revenue"));
        BigDecimal growthRate = calculateGrowthRate(currentRevenue, prevRevenue);
        
        // 每日明細
        String dailySql = """
            SELECT 
                DATE(created_at) as date,
                COALESCE(SUM(total_amount), 0) as revenue,
                COUNT(*) as orders,
                COALESCE(SUM(draw_count), 0) as draws
            FROM `order`
            WHERE status IN ('PAID', 'COMPLETED')
            AND created_at BETWEEN ? AND ?
            """ + (storeId != null ? " AND store_id = ?" : "") + """
            GROUP BY DATE(created_at)
            ORDER BY date
            """;
        
        List<RevenueReportRes.DailyRevenue> dailyDetails = jdbcTemplate.query(dailySql, params.toArray(),
            (rs, rowNum) -> RevenueReportRes.DailyRevenue.builder()
                .date(rs.getDate("date").toLocalDate())
                .revenue(rs.getBigDecimal("revenue"))
                .orders(rs.getInt("orders"))
                .draws(rs.getInt("draws"))
                .build()
        );
        
        // 各店家統計
        String storeSql = """
            SELECT 
                o.store_id,
                s.name as store_name,
                COALESCE(SUM(o.total_amount), 0) as revenue,
                COUNT(*) as orders
            FROM `order` o
            LEFT JOIN store s ON %s
            WHERE o.status IN ('PAID', 'COMPLETED')
            AND o.created_at BETWEEN ? AND ?
            GROUP BY o.store_id, s.name
            ORDER BY revenue DESC
            """.formatted(utf8mb4Eq("o.store_id", "s.id"));
        
        List<Object> storeParams = new ArrayList<>();
        storeParams.add(startDate.atStartOfDay());
        storeParams.add(endDate.plusDays(1).atStartOfDay());
        
        List<RevenueReportRes.StoreRevenue> storeDetails = jdbcTemplate.query(storeSql, storeParams.toArray(),
            (rs, rowNum) -> RevenueReportRes.StoreRevenue.builder()
                .storeId(rs.getString("store_id"))
                .storeName(rs.getString("store_name"))
                .revenue(rs.getBigDecimal("revenue"))
                .orders(rs.getInt("orders"))
                .percentage(currentRevenue.compareTo(BigDecimal.ZERO) > 0 ?
                    rs.getBigDecimal("revenue").multiply(new BigDecimal("100"))
                        .divide(currentRevenue, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                .build()
        );
        
        return RevenueReportRes.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalRevenue(currentRevenue)
                .totalOrders(toInteger(stats.get("total_orders")))
                .totalDraws(toInteger(stats.get("total_draws")))
                .avgOrderAmount(toBigDecimal(stats.get("avg_amount")))
                .growthRate(growthRate)
                .dailyDetails(dailyDetails)
                .storeDetails(storeDetails)
                .build();
    }
    
    @Override
    public ReferralReportRes getReferralReport(QueryReq<ReferralReportCondition> req) {
        ReferralReportCondition condition = req != null && req.getCondition() != null
                ? req.getCondition() : new ReferralReportCondition();

        String storeId = condition.getStoreId();
        LocalDate endDate = condition.getEndDate() != null ? condition.getEndDate() : LocalDate.now();
        LocalDate startDate = condition.getStartDate() != null ? condition.getStartDate() : endDate.minusDays(29);

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate 不可晚於 endDate");
        }

        log.info("📊 產生店薦店招商報表: referrerStoreId={}, {} ~ {}", storeId, startDate, endDate);

        String codeSummarySql = """
            SELECT
                COUNT(*) AS total_code_count,
                COALESCE(SUM(CASE WHEN rc.is_active = 1 THEN 1 ELSE 0 END), 0) AS active_code_count
            FROM referral_code rc
            WHERE 1 = 1
            """;
        List<Object> codeSummaryParams = new ArrayList<>();
        if (storeId != null && !storeId.isBlank()) {
            codeSummarySql += " AND rc.store_id = ?";
            codeSummaryParams.add(storeId);
        }
        Map<String, Object> codeSummary = jdbcTemplate.queryForMap(codeSummarySql, codeSummaryParams.toArray());

        String successAllSql = """
            SELECT COUNT(*)
            FROM store s
            WHERE s.referrer_store_id IS NOT NULL
              AND s.activated_at IS NOT NULL
            """;
        List<Object> successAllParams = new ArrayList<>();
        if (storeId != null && !storeId.isBlank()) {
            successAllSql += " AND s.referrer_store_id = ?";
            successAllParams.add(storeId);
        }
        Integer successfulReferralStoreCount = jdbcTemplate.queryForObject(
                successAllSql,
                Integer.class,
                successAllParams.toArray());

        LocalDate[] previousRange = calculatePreviousRange(startDate, endDate);
        Integer currentPeriodActivatedStoreCount = queryActivatedReferralStoreCount(startDate, endDate, storeId);
        Integer previousPeriodActivatedStoreCount = queryActivatedReferralStoreCount(previousRange[0], previousRange[1], storeId);
        BigDecimal growthRate = calculateNullableGrowthRate(
                BigDecimal.valueOf(currentPeriodActivatedStoreCount != null ? currentPeriodActivatedStoreCount : 0),
                BigDecimal.valueOf(previousPeriodActivatedStoreCount != null ? previousPeriodActivatedStoreCount : 0));

        String dailySql = """
            SELECT
                DATE(s.activated_at) AS activated_date,
                COUNT(*) AS activated_store_count
            FROM store s
            WHERE s.referrer_store_id IS NOT NULL
              AND s.activated_at >= ?
              AND s.activated_at < ?
            """;
        List<Object> dailyParams = new ArrayList<>();
        dailyParams.add(startDate.atStartOfDay());
        dailyParams.add(endDate.plusDays(1).atStartOfDay());
        if (storeId != null && !storeId.isBlank()) {
            dailySql += " AND s.referrer_store_id = ?";
            dailyParams.add(storeId);
        }
        dailySql += " GROUP BY DATE(s.activated_at) ORDER BY activated_date ASC";

        Map<LocalDate, Integer> dailyMap = new LinkedHashMap<>();
        jdbcTemplate.query(dailySql, dailyParams.toArray(), rs ->
                dailyMap.put(rs.getDate("activated_date").toLocalDate(), rs.getInt("activated_store_count")));

        List<ReferralReportRes.DailyActivation> dailyActivations = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            dailyActivations.add(ReferralReportRes.DailyActivation.builder()
                    .date(date)
                    .activatedStoreCount(dailyMap.getOrDefault(date, 0))
                    .build());
        }

        String performanceSql = """
            SELECT
                s.referrer_store_id,
                ref.store_name AS referrer_store_name,
                COUNT(*) AS activated_store_count,
                MAX(DATE(s.activated_at)) AS last_activated_date,
                (
                    SELECT COUNT(*)
                    FROM referral_code rc
                    WHERE rc.store_id = s.referrer_store_id
                ) AS referral_code_count
            FROM store s
            JOIN store ref ON ref.id = s.referrer_store_id
            WHERE s.referrer_store_id IS NOT NULL
              AND s.activated_at >= ?
              AND s.activated_at < ?
            """;
        List<Object> performanceParams = new ArrayList<>();
        performanceParams.add(startDate.atStartOfDay());
        performanceParams.add(endDate.plusDays(1).atStartOfDay());
        if (storeId != null && !storeId.isBlank()) {
            performanceSql += " AND s.referrer_store_id = ?";
            performanceParams.add(storeId);
        }
        performanceSql += " GROUP BY s.referrer_store_id, ref.store_name ORDER BY activated_store_count DESC, ref.store_name ASC LIMIT 20";

        List<ReferralReportRes.StoreReferralPerformance> storePerformances = jdbcTemplate.query(
                performanceSql,
                performanceParams.toArray(),
                (rs, rowNum) -> ReferralReportRes.StoreReferralPerformance.builder()
                        .referrerStoreId(rs.getString("referrer_store_id"))
                        .referrerStoreName(rs.getString("referrer_store_name"))
                        .referralCodeCount(rs.getInt("referral_code_count"))
                        .activatedStoreCount(rs.getInt("activated_store_count"))
                        .lastActivatedDate(rs.getDate("last_activated_date") != null
                                ? rs.getDate("last_activated_date").toLocalDate()
                                : null)
                        .rank(rowNum + 1)
                        .build());

        return ReferralReportRes.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalReferralCodeCount(toInteger(codeSummary.get("total_code_count")))
                .activeReferralCodeCount(toInteger(codeSummary.get("active_code_count")))
                .successfulReferralStoreCount(successfulReferralStoreCount != null ? successfulReferralStoreCount : 0)
                .currentPeriodActivatedStoreCount(currentPeriodActivatedStoreCount != null ? currentPeriodActivatedStoreCount : 0)
                .previousPeriodActivatedStoreCount(previousPeriodActivatedStoreCount != null ? previousPeriodActivatedStoreCount : 0)
                .growthRate(growthRate)
                .dailyActivations(dailyActivations)
                .storePerformances(storePerformances)
                .build();
    }
    
    @Override
    public LotteryResultReportRes getLotteryResultReport(QueryReq<LotteryResultReportCondition> req) {
        LotteryResultReportCondition condition = req != null && req.getCondition() != null ? 
            req.getCondition() : new LotteryResultReportCondition();

        String storeId = condition.getStoreId();
        String lotteryId = condition.getLotteryId();
        LocalDate endDate = condition.getEndDate() != null ? condition.getEndDate() : LocalDate.now();
        LocalDate startDate = condition.getStartDate() != null ? condition.getStartDate() : endDate.minusDays(29);
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate 不可晚於 endDate");
        }

        log.info("📊 產生抽獎結果報表: storeId={}, lotteryId={}, {} ~ {}", storeId, lotteryId, startDate, endDate);

        StringBuilder where = new StringBuilder(" WHERE lt.status = 'DRAWN' AND lt.drawn_at BETWEEN ? AND ? ");
        List<Object> params = new ArrayList<>();
        params.add(startDate.atStartOfDay());
        params.add(endDate.plusDays(1).atStartOfDay());
        if (storeId != null && !storeId.isBlank()) {
            where.append(" AND l.store_id = ? ");
            params.add(storeId);
        }
        if (lotteryId != null && !lotteryId.isBlank()) {
            where.append(" AND l.id = ? ");
            params.add(lotteryId);
        }

        String summarySql = """
                SELECT
                    COUNT(*) AS total_draws,
                    COALESCE(SUM(CASE WHEN lt.prize_id IS NOT NULL THEN 1 ELSE 0 END), 0) AS total_winning_count
                FROM lottery_ticket lt
                JOIN lottery l ON %s
                """.formatted(utf8mb4Eq("lt.lottery_id", "l.id")) + where;

        Map<String, Object> summary = jdbcTemplate.queryForMap(summarySql, params.toArray());

        String hotSql = """
                SELECT
                    l.id AS lottery_id,
                    l.title AS lottery_title,
                    COUNT(*) AS draw_count,
                    COALESCE(SUM(CASE WHEN lt.prize_id IS NOT NULL THEN 1 ELSE 0 END), 0) AS winning_count
                FROM lottery_ticket lt
                JOIN lottery l ON %s
                """.formatted(utf8mb4Eq("lt.lottery_id", "l.id")) + where + """
                GROUP BY l.id, l.title
                ORDER BY winning_count DESC, draw_count DESC, l.title ASC
                LIMIT 10
                """;

        List<LotteryResultReportRes.HotLottery> hotLotteries = jdbcTemplate.query(
                hotSql,
                params.toArray(),
                (rs, rowNum) -> LotteryResultReportRes.HotLottery.builder()
                        .lotteryId(rs.getString("lottery_id"))
                        .lotteryTitle(rs.getString("lottery_title"))
                        .drawCount(rs.getInt("draw_count"))
                        .winningCount(rs.getInt("winning_count"))
                        .build());

        String detailSql = """
                SELECT
                    lt.id AS ticket_id,
                    DATE(lt.drawn_at) AS draw_date,
                    DATE_FORMAT(lt.drawn_at, '%Y-%m-%d %H:%i:%s') AS draw_time,
                    lt.drawn_by AS user_id,
                    COALESCE(u.nickname, u.email, lt.drawn_by) AS user_display_name,
                    l.id AS lottery_id,
                    l.title AS lottery_title,
                    l.image_url AS lottery_image_url,
                    lp.id AS prize_id,
                    COALESCE(lp.name, '謝謝惠顧') AS prize_name,
                    COALESCE(lp.level, lt.prize_level) AS prize_level,
                    lp.image_url AS prize_image_url,
                    lt.ticket_number,
                    lt.revealed_number,
                    s.id AS store_id,
                    s.store_name
                FROM lottery_ticket lt
                JOIN lottery l ON %s
                LEFT JOIN lottery_prize lp ON %s AND %s
                LEFT JOIN user u ON %s
                LEFT JOIN store s ON %s
                """.formatted(
                utf8mb4Eq("lt.lottery_id", "l.id"),
                utf8mb4Eq("lt.prize_id", "lp.id"),
                utf8mb4Eq("lp.lottery_id", "l.id"),
                utf8mb4Eq("lt.drawn_by", "u.id"),
                utf8mb4Eq("l.store_id", "s.id")) + where + """
                AND lt.prize_id IS NOT NULL
                ORDER BY lt.drawn_at DESC
                LIMIT 1000
                """;

        List<LotteryResultReportRes.WinningDetail> winningDetails = jdbcTemplate.query(
                detailSql,
                params.toArray(),
                (rs, rowNum) -> LotteryResultReportRes.WinningDetail.builder()
                        .ticketId(rs.getString("ticket_id"))
                        .drawDate(rs.getDate("draw_date") != null ? rs.getDate("draw_date").toLocalDate() : null)
                        .drawTime(rs.getString("draw_time"))
                        .userId(rs.getString("user_id"))
                        .userDisplayName(rs.getString("user_display_name"))
                        .lotteryId(rs.getString("lottery_id"))
                        .lotteryTitle(rs.getString("lottery_title"))
                        .lotteryImageUrl(rs.getString("lottery_image_url"))
                        .prizeId(rs.getString("prize_id"))
                        .prizeName(rs.getString("prize_name"))
                        .prizeLevel(rs.getString("prize_level"))
                        .prizeImageUrl(rs.getString("prize_image_url"))
                        .drawCount(1)
                        .ticketNumber(rs.getInt("ticket_number"))
                        .revealedNumber(rs.getObject("revealed_number") != null ? rs.getInt("revealed_number") : null)
                        .storeId(rs.getString("store_id"))
                        .storeName(rs.getString("store_name"))
                        .build());

        return LotteryResultReportRes.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalDraws(toInteger(summary.get("total_draws")))
                .totalWinningCount(toInteger(summary.get("total_winning_count")))
                .hotLotteries(hotLotteries)
                .winningDetails(winningDetails)
                .build();
    }
    
    @Override
    public RechargeReportRes getRechargeReport(QueryReq<RechargeReportCondition> req) {
        RechargeReportCondition condition = req != null && req.getCondition() != null ? 
            req.getCondition() : new RechargeReportCondition();

        LocalDate endDate = condition.getEndDate() != null ? condition.getEndDate() : LocalDate.now();
        LocalDate startDate = condition.getStartDate() != null ? condition.getStartDate() : endDate.minusDays(29);
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate 不可晚於 endDate");
        }

        log.info("📊 產生儲值報表（平台）: {} ~ {}", startDate, endDate);
        
        // 基本統計
        String baseSql = """
            SELECT 
                COALESCE(SUM(amount), 0) as total_amount,
                COUNT(*) as total_count,
                COALESCE(AVG(amount), 0) as avg_amount
            FROM wallet_transaction
            WHERE transaction_type = 'RECHARGE'
            AND status = 'SUCCESS'
            AND created_at BETWEEN ? AND ?
            """;
        
        List<Object> params = new ArrayList<>();
        params.add(startDate.atStartOfDay());
        params.add(endDate.plusDays(1).atStartOfDay());
        
        Map<String, Object> stats = jdbcTemplate.queryForMap(baseSql, params.toArray());
        
        // 計算成長率
        LocalDate[] previousRange = calculatePreviousRange(startDate, endDate);
        
        List<Object> prevParams = new ArrayList<>();
        prevParams.add(previousRange[0].atStartOfDay());
        prevParams.add(previousRange[1].plusDays(1).atStartOfDay());
        
        Map<String, Object> prevStats = jdbcTemplate.queryForMap(baseSql, prevParams.toArray());
        
        BigDecimal currentAmount = toBigDecimal(stats.get("total_amount"));
        BigDecimal prevAmount = toBigDecimal(prevStats.get("total_amount"));
        BigDecimal growthRate = calculateGrowthRate(currentAmount, prevAmount);
        
        // 每日明細
        String dailySql = """
            SELECT 
                DATE(created_at) as date,
                COALESCE(SUM(amount), 0) as amount,
                COUNT(*) as count,
                COUNT(DISTINCT user_id) as new_users
            FROM wallet_transaction
            WHERE transaction_type = 'RECHARGE'
            AND status = 'SUCCESS'
            AND created_at BETWEEN ? AND ?
            GROUP BY DATE(created_at)
            ORDER BY date
            """;
        
        List<RechargeReportRes.DailyRecharge> dailyDetails = jdbcTemplate.query(dailySql, params.toArray(),
            (rs, rowNum) -> RechargeReportRes.DailyRecharge.builder()
                .date(rs.getDate("date").toLocalDate())
                .amount(rs.getBigDecimal("amount"))
                .count(rs.getInt("count"))
                .newUsers(rs.getInt("new_users"))
                .build()
        );
        
        // 方案統計（根據金額分類）
        String planSql = """
            SELECT 
                amount as plan_price,
                COUNT(*) as purchase_count,
                SUM(amount) as total_amount
            FROM wallet_transaction
            WHERE transaction_type = 'RECHARGE'
            AND status = 'SUCCESS'
            AND created_at BETWEEN ? AND ?
            GROUP BY amount
            ORDER BY total_amount DESC
            """;
        
        List<RechargeReportRes.PlanStats> planStats = jdbcTemplate.query(planSql, params.toArray(),
            (rs, rowNum) -> RechargeReportRes.PlanStats.builder()
                .planId(rs.getBigDecimal("plan_price").toString())
                .planName("儲值 $" + rs.getBigDecimal("plan_price"))
                .planPrice(rs.getBigDecimal("plan_price"))
                .bonusPoints(BigDecimal.ZERO)
                .purchaseCount(rs.getInt("purchase_count"))
                .totalAmount(rs.getBigDecimal("total_amount"))
                .percentage(currentAmount.compareTo(BigDecimal.ZERO) > 0 ?
                    rs.getBigDecimal("total_amount").multiply(new BigDecimal("100"))
                        .divide(currentAmount, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                .build()
        );
        
        return RechargeReportRes.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalAmount(currentAmount)
                .totalCount(toInteger(stats.get("total_count")))
                .avgAmount(toBigDecimal(stats.get("avg_amount")))
                .growthRate(growthRate)
                .dailyDetails(dailyDetails)
                .planStats(planStats)
                .build();
    }
    
    @Override
    public BonusReportRes getBonusReport(QueryReq<BonusReportCondition> req) {
        BonusReportCondition condition = req != null && req.getCondition() != null ? 
            req.getCondition() : new BonusReportCondition();

        LocalDate endDate = condition.getEndDate() != null ? condition.getEndDate() : LocalDate.now();
        LocalDate startDate = condition.getStartDate() != null ? condition.getStartDate() : endDate.minusDays(29);
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate 不可晚於 endDate");
        }

        log.info("📊 產生贈送點數報表（平台）: {} ~ {}", startDate, endDate);
        
        // 基本統計（贈送類型交易）
        String baseSql = """
            SELECT 
                COALESCE(SUM(amount), 0) as total_bonus,
                COUNT(*) as total_count,
                COUNT(DISTINCT user_id) as benefit_users
            FROM wallet_transaction
            WHERE transaction_type IN ('BONUS', 'REFERRAL_BONUS', 'PROMOTION', 'ADJUSTMENT')
            AND amount > 0
            AND status = 'SUCCESS'
            AND created_at BETWEEN ? AND ?
            """;
        
        List<Object> params = new ArrayList<>();
        params.add(startDate.atStartOfDay());
        params.add(endDate.plusDays(1).atStartOfDay());
        
        Map<String, Object> stats = jdbcTemplate.queryForMap(baseSql, params.toArray());
        
        // 計算成長率
        LocalDate[] previousRange = calculatePreviousRange(startDate, endDate);
        
        List<Object> prevParams = new ArrayList<>();
        prevParams.add(previousRange[0].atStartOfDay());
        prevParams.add(previousRange[1].plusDays(1).atStartOfDay());
        
        Map<String, Object> prevStats = jdbcTemplate.queryForMap(baseSql, prevParams.toArray());
        
        BigDecimal currentBonus = toBigDecimal(stats.get("total_bonus"));
        BigDecimal prevBonus = toBigDecimal(prevStats.get("total_bonus"));
        BigDecimal growthRate = calculateGrowthRate(currentBonus, prevBonus);
        
        // 每日明細
        String dailySql = """
            SELECT 
                DATE(created_at) as date,
                COALESCE(SUM(amount), 0) as points,
                COUNT(*) as count
            FROM wallet_transaction
            WHERE transaction_type IN ('BONUS', 'REFERRAL_BONUS', 'PROMOTION', 'ADJUSTMENT')
            AND amount > 0
            AND status = 'SUCCESS'
            AND created_at BETWEEN ? AND ?
            GROUP BY DATE(created_at)
            ORDER BY date
            """;
        
        List<BonusReportRes.DailyBonus> dailyDetails = jdbcTemplate.query(dailySql, params.toArray(),
            (rs, rowNum) -> BonusReportRes.DailyBonus.builder()
                .date(rs.getDate("date").toLocalDate())
                .points(rs.getBigDecimal("points"))
                .count(rs.getInt("count"))
                .build()
        );
        
        // 類型統計
        String typeSql = """
            SELECT 
                transaction_type as bonus_type,
                COALESCE(SUM(amount), 0) as total_points,
                COUNT(*) as count
            FROM wallet_transaction
            WHERE transaction_type IN ('BONUS', 'REFERRAL_BONUS', 'PROMOTION', 'ADJUSTMENT')
            AND amount > 0
            AND status = 'SUCCESS'
            AND created_at BETWEEN ? AND ?
            GROUP BY transaction_type
            ORDER BY total_points DESC
            """;
        
        List<BonusReportRes.BonusTypeStats> typeStats = jdbcTemplate.query(typeSql, params.toArray(),
            (rs, rowNum) -> BonusReportRes.BonusTypeStats.builder()
                .bonusType(rs.getString("bonus_type"))
                .typeName(getBonusTypeName(rs.getString("bonus_type")))
                .totalPoints(rs.getBigDecimal("total_points"))
                .count(rs.getInt("count"))
                .percentage(currentBonus.compareTo(BigDecimal.ZERO) > 0 ?
                    rs.getBigDecimal("total_points").multiply(new BigDecimal("100"))
                        .divide(currentBonus, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                .build()
        );
        
        return BonusReportRes.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalBonusPoints(currentBonus)
                .totalCount(toInteger(stats.get("total_count")))
                .benefitUsers(toInteger(stats.get("benefit_users")))
                .growthRate(growthRate)
                .dailyDetails(dailyDetails)
                .typeStats(typeStats)
                .build();
    }
    
    @Override
    public void saveReportSnapshot(String reportType, String periodType, String storeId,
                                    Object data, String summary) {
        try {
            ReportSnapshot snapshot = new ReportSnapshot();
            snapshot.setId(UUID.randomUUID().toString());
            snapshot.setReportType(reportType);
            snapshot.setPeriodType(periodType);
            snapshot.setStoreId(storeId);
            snapshot.setPeriodStart(LocalDate.now());
            snapshot.setPeriodEnd(LocalDate.now());
            snapshot.setData(objectMapper.writeValueAsString(data));
            snapshot.setSummary(summary);
            snapshot.setCreatedAt(LocalDateTime.now());
            
            reportSnapshotMapper.insert(snapshot);
            log.info("✅ 報表快照已儲存: type={}, period={}", reportType, periodType);
        } catch (Exception e) {
            log.error("❌ 報表快照儲存失敗: {}", e.getMessage());
        }
    }
    
    @Override
    public List<?> getReportSnapshots(String reportType, String periodType, String storeId) {
        return reportSnapshotRepository.selectByTypeAndPeriod(reportType, periodType, 100);
    }
    
    // ========== 會員成長報表 ==========

    @Override
    public PlatformRevenueReportRes getPlatformRevenueReport(QueryReq<PlatformRevenueReportCondition> req) {
        PlatformRevenueReportCondition condition = req != null && req.getCondition() != null
                ? req.getCondition() : new PlatformRevenueReportCondition();

        LocalDate endDate = condition.getEndDate() != null ? condition.getEndDate() : LocalDate.now();
        LocalDate startDate = condition.getStartDate() != null ? condition.getStartDate() : endDate.minusDays(29);
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate 不可晚於 endDate");
        }

        log.info("📊 產生平台營收總覽報表: {} ~ {}", startDate, endDate);

        long totalRecharge = queryTotalRecharge(startDate, endDate);
        long totalSpend = queryTotalSpend(startDate, endDate);
        long drawCount = queryPlatformDrawCount(startDate, endDate);
        PlatformRevenueReportRes.SpendByType spendByType = querySpendByType(startDate, endDate);

        long netRevenue = totalRecharge - totalSpend;

        LocalDate[] previousRange = calculatePreviousRange(startDate, endDate);
        BigDecimal rechargeGrowthRate = calculateNullableGrowthRate(
                BigDecimal.valueOf(totalRecharge),
                BigDecimal.valueOf(queryTotalRecharge(previousRange[0], previousRange[1])));
        BigDecimal spendGrowthRate = calculateNullableGrowthRate(
                BigDecimal.valueOf(totalSpend),
                BigDecimal.valueOf(queryTotalSpend(previousRange[0], previousRange[1])));

        List<PlatformRevenueReportRes.DailyRevenueItem> dailyRevenue = buildDailyRevenue(startDate, endDate);
        List<PlatformRevenueReportRes.StoreBreakdownItem> storeBreakdown = queryStoreBreakdown(startDate, endDate);

        return PlatformRevenueReportRes.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalRecharge(totalRecharge)
                .totalSpend(totalSpend)
                .netRevenue(netRevenue)
                .drawCount(drawCount)
                .spendByType(spendByType)
                .rechargeGrowthRate(rechargeGrowthRate)
                .spendGrowthRate(spendGrowthRate)
                .dailyRevenue(dailyRevenue)
                .storeBreakdown(storeBreakdown)
                .build();
    }

    @Override
    public MemberGrowthReportRes getMemberGrowthReport(QueryReq<MemberGrowthReportCondition> req) {
        MemberGrowthReportCondition condition = req != null && req.getCondition() != null
                ? req.getCondition() : new MemberGrowthReportCondition();

        // 預設日期：最近 30 天
        LocalDate endDate   = condition.getEndDate()   != null ? condition.getEndDate()   : LocalDate.now();
        LocalDate startDate = condition.getStartDate() != null ? condition.getStartDate() : endDate.minusDays(29);

        log.info("📊 產生會員成長報表: {} ~ {}", startDate, endDate);

        LocalDateTime startDt = startDate.atStartOfDay();
        LocalDateTime endDt   = endDate.plusDays(1).atStartOfDay();

        // ── Q1: 新增會員總數 ──────────────────────────────────────────────────
        Integer totalNewMembers = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user WHERE created_at BETWEEN ? AND ?",
                Integer.class, startDt, endDt);
        if (totalNewMembers == null) totalNewMembers = 0;

        // 上期窗口（同等時長，緊接在 startDate 之前）
        long periodDays = startDate.until(endDate).getDays() + 1;
        LocalDate prevEndDate   = startDate.minusDays(1);
        LocalDate prevStartDate = prevEndDate.minusDays(periodDays - 1);

        Integer prevPeriodCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user WHERE created_at BETWEEN ? AND ?",
                Integer.class,
                prevStartDate.atStartOfDay(), prevEndDate.plusDays(1).atStartOfDay());
        if (prevPeriodCount == null) prevPeriodCount = 0;

        BigDecimal growthRate = null;
        if (prevPeriodCount > 0) {
            growthRate = new BigDecimal(totalNewMembers - prevPeriodCount)
                    .multiply(new BigDecimal("100"))
                    .divide(new BigDecimal(prevPeriodCount), 1, RoundingMode.HALF_UP);
        }

        // ── Q2: 每日明細（補零）──────────────────────────────────────────────
        List<Map<String, Object>> dailyRows = jdbcTemplate.queryForList(
                "SELECT DATE(created_at) AS d, COUNT(*) AS cnt " +
                "FROM user WHERE created_at BETWEEN ? AND ? " +
                "GROUP BY DATE(created_at)",
                startDt, endDt);

        Map<LocalDate, Integer> dailyMap = new java.util.LinkedHashMap<>();
        for (Map<String, Object> row : dailyRows) {
            Object d = row.get("d");
            LocalDate date = (d instanceof java.sql.Date)
                    ? ((java.sql.Date) d).toLocalDate()
                    : LocalDate.parse(d.toString());
            dailyMap.put(date, toInteger(row.get("cnt")));
        }

        List<MemberGrowthReportRes.DailyNewMember> dailyNewMembers = new ArrayList<>();
        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            dailyNewMembers.add(MemberGrowthReportRes.DailyNewMember.builder()
                    .date(d)
                    .count(dailyMap.getOrDefault(d, 0))
                    .build());
        }

        // ── Q3: 按 provider 分類 ─────────────────────────────────────────────
        List<Map<String, Object>> providerRows = jdbcTemplate.queryForList(
                "SELECT provider, COUNT(*) AS cnt " +
                "FROM user WHERE created_at BETWEEN ? AND ? " +
                "GROUP BY provider",
                startDt, endDt);

        Map<String, Integer> registrationByProvider = new java.util.LinkedHashMap<>();
        for (Map<String, Object> row : providerRows) {
            String provider = row.get("provider") != null ? row.get("provider").toString() : "UNKNOWN";
            registrationByProvider.put(provider, toInteger(row.get("cnt")));
        }

        // ── Q4: 活躍會員（4-table UNION）────────────────────────────────────
        String activeSql =
                "SELECT COUNT(DISTINCT user_id) FROM (" +
                "  SELECT " + utf8mb4Text("id") + " AS user_id FROM user WHERE last_login_at BETWEEN ? AND ? " +
                "  UNION " +
                "  SELECT " + utf8mb4Text("user_id") + " AS user_id FROM wallet_transaction WHERE transaction_type='RECHARGE' AND created_at BETWEEN ? AND ? " +
                "  UNION " +
                "  SELECT " + utf8mb4Text("drawn_by") + " AS user_id FROM lottery_ticket WHERE status='DRAWN' AND drawn_at BETWEEN ? AND ? " +
                "  UNION " +
                "  SELECT " + utf8mb4Text("user_id") + " AS user_id FROM `order` WHERE status != 'CANCELLED' AND created_at BETWEEN ? AND ?" +
                ") t";

        Integer activeMembers = jdbcTemplate.queryForObject(
                activeSql, Integer.class,
                startDt, endDt,
                startDt, endDt,
                startDt, endDt,
                startDt, endDt);
        if (activeMembers == null) activeMembers = 0;

        // ── Q5/Q6: ARPU (Gold / Bonus) ──────────────────────────────────────
        BigDecimal arpuGold;
        BigDecimal arpuBonus;
        if (activeMembers == 0) {
            arpuGold  = new BigDecimal("0.0");
            arpuBonus = new BigDecimal("0.0");
        } else {
            BigDecimal goldTotal = jdbcTemplate.queryForObject(
                    "SELECT COALESCE(SUM(amount), 0) FROM wallet_transaction " +
                    "WHERE transaction_type='DRAW' AND coin_type='GOLD' AND created_at BETWEEN ? AND ?",
                    BigDecimal.class, startDt, endDt);
            BigDecimal bonusTotal = jdbcTemplate.queryForObject(
                    "SELECT COALESCE(SUM(amount), 0) FROM wallet_transaction " +
                    "WHERE transaction_type='DRAW' AND coin_type='BONUS' AND created_at BETWEEN ? AND ?",
                    BigDecimal.class, startDt, endDt);
            if (goldTotal  == null) goldTotal  = BigDecimal.ZERO;
            if (bonusTotal == null) bonusTotal = BigDecimal.ZERO;

            BigDecimal activeBD = new BigDecimal(activeMembers);
            arpuGold  = goldTotal .divide(activeBD, 1, RoundingMode.HALF_UP);
            arpuBonus = bonusTotal.divide(activeBD, 1, RoundingMode.HALF_UP);
        }

        // ── Q7/Q8: 留存率（前一個完整月份）─────────────────────────────────
        BigDecimal retention7Days  = null;
        BigDecimal retention30Days = null;

        LocalDate prevMonthStart = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        LocalDate prevMonthEnd   = prevMonthStart.plusMonths(1).minusDays(1);

        Integer prevMonthTotal = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user WHERE created_at BETWEEN ? AND ?",
                Integer.class,
                prevMonthStart.atStartOfDay(), prevMonthEnd.plusDays(1).atStartOfDay());
        if (prevMonthTotal == null) prevMonthTotal = 0;

        if (prevMonthTotal > 0) {
            String retentionSql =
                "SELECT COUNT(DISTINCT u.id) FROM user u " +
                "WHERE u.created_at BETWEEN ? AND ? " +
                "AND EXISTS (" +
                "  SELECT 1 FROM user u2 WHERE " + utf8mb4Eq("u2.id", "u.id") +
                "    AND u2.last_login_at BETWEEN u.created_at AND DATE_ADD(u.created_at, INTERVAL ? DAY) " +
                "  UNION ALL " +
                "  SELECT 1 FROM wallet_transaction wt WHERE " + utf8mb4Eq("wt.user_id", "u.id") +
                "    AND wt.transaction_type = 'RECHARGE' " +
                "    AND wt.created_at BETWEEN u.created_at AND DATE_ADD(u.created_at, INTERVAL ? DAY) " +
                "  UNION ALL " +
                "  SELECT 1 FROM lottery_ticket lt WHERE " + utf8mb4Eq("lt.drawn_by", "u.id") +
                "    AND lt.status = 'DRAWN' " +
                "    AND lt.drawn_at BETWEEN u.created_at AND DATE_ADD(u.created_at, INTERVAL ? DAY) " +
                "  UNION ALL " +
                "  SELECT 1 FROM `order` o WHERE " + utf8mb4Eq("o.user_id", "u.id") +
                "    AND o.created_at BETWEEN u.created_at AND DATE_ADD(u.created_at, INTERVAL ? DAY)" +
                ")";

            LocalDateTime pmStart = prevMonthStart.atStartOfDay();
            LocalDateTime pmEnd   = prevMonthEnd.plusDays(1).atStartOfDay();

            Integer retained7 = jdbcTemplate.queryForObject(retentionSql, Integer.class,
                    pmStart, pmEnd, 7, 7, 7, 7);
            Integer retained30 = jdbcTemplate.queryForObject(retentionSql, Integer.class,
                    pmStart, pmEnd, 30, 30, 30, 30);
            if (retained7  == null) retained7  = 0;
            if (retained30 == null) retained30 = 0;

            BigDecimal baseBD = new BigDecimal(prevMonthTotal);
            retention7Days  = new BigDecimal(retained7 ).multiply(new BigDecimal("100"))
                    .divide(baseBD, 1, RoundingMode.HALF_UP);
            retention30Days = new BigDecimal(retained30).multiply(new BigDecimal("100"))
                    .divide(baseBD, 1, RoundingMode.HALF_UP);
        }

            // ── Q9: 會員消費模式分布（以本期新增會員為母體）────────────────────────
            String patternSql = """
                SELECT pattern_code, COUNT(*) AS user_count
                FROM (
                    SELECT
                    u.id,
                    CASE
                        WHEN r.user_id IS NOT NULL AND d.user_id IS NOT NULL THEN 'RECHARGE_AND_DRAW'
                        WHEN r.user_id IS NOT NULL AND d.user_id IS NULL THEN 'RECHARGE_ONLY'
                        WHEN r.user_id IS NULL AND d.user_id IS NOT NULL THEN 'DRAW_ONLY'
                        ELSE 'REGISTER_ONLY'
                    END AS pattern_code
                    FROM user u
                    LEFT JOIN (
                    SELECT DISTINCT user_id
                    FROM wallet_transaction
                    WHERE transaction_type = 'RECHARGE'
                      AND created_at BETWEEN ? AND ?
                    ) r ON %s
                    LEFT JOIN (
                    SELECT DISTINCT drawn_by AS user_id
                    FROM lottery_ticket
                    WHERE status = 'DRAWN'
                      AND drawn_at BETWEEN ? AND ?
                      AND drawn_by IS NOT NULL
                    ) d ON %s
                    WHERE u.created_at BETWEEN ? AND ?
                ) t
                GROUP BY pattern_code
                """.formatted(utf8mb4Eq("r.user_id", "u.id"), utf8mb4Eq("d.user_id", "u.id"));

            Map<String, Integer> patternMap = new LinkedHashMap<>();
            jdbcTemplate.query(patternSql,
                new Object[]{startDt, endDt, startDt, endDt, startDt, endDt},
                (RowCallbackHandler) rs -> patternMap.put(rs.getString("pattern_code"), rs.getInt("user_count")));

            String[] patternCodes = {"RECHARGE_AND_DRAW", "RECHARGE_ONLY", "DRAW_ONLY", "REGISTER_ONLY"};
            List<MemberGrowthReportRes.ConsumptionPattern> consumptionPatterns = new ArrayList<>();
            for (String patternCode : patternCodes) {
                int userCount = patternMap.getOrDefault(patternCode, 0);
                BigDecimal percentage = totalNewMembers > 0
                    ? BigDecimal.valueOf(userCount).multiply(new BigDecimal("100"))
                    .divide(BigDecimal.valueOf(totalNewMembers), 1, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
                consumptionPatterns.add(MemberGrowthReportRes.ConsumptionPattern.builder()
                    .patternCode(patternCode)
                    .patternName(switch (patternCode) {
                    case "RECHARGE_AND_DRAW" -> "有儲值且有抽獎";
                    case "RECHARGE_ONLY" -> "僅儲值";
                    case "DRAW_ONLY" -> "僅抽獎";
                    default -> "僅註冊未消費";
                    })
                    .userCount(userCount)
                    .percentage(percentage)
                    .build());
            }

            // ── Q10: 商品集中度（Top 10）───────────────────────────────────────
            Long totalDrawVolume = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM lottery_ticket WHERE status = 'DRAWN' AND drawn_at BETWEEN ? AND ?",
                Long.class,
                startDt,
                endDt);
            if (totalDrawVolume == null) {
                totalDrawVolume = 0L;
            }

            String concentrationSql = """
                SELECT
                    l.id AS lottery_id,
                    l.title AS lottery_title,
                    l.category,
                    COUNT(*) AS draw_count
                FROM lottery_ticket lt
                JOIN lottery l ON %s
                WHERE lt.status = 'DRAWN'
                  AND lt.drawn_at BETWEEN ? AND ?
                GROUP BY l.id, l.title, l.category
                ORDER BY draw_count DESC, l.title ASC
                LIMIT 10
                """.formatted(utf8mb4Eq("lt.lottery_id", "l.id"));

            final long drawBase = totalDrawVolume;
            List<MemberGrowthReportRes.ProductConcentration> productConcentrations = jdbcTemplate.query(
                concentrationSql,
                new Object[]{startDt, endDt},
                (rs, rowNum) -> {
                    int drawCount = rs.getInt("draw_count");
                    BigDecimal percentage = drawBase > 0
                        ? BigDecimal.valueOf(drawCount).multiply(new BigDecimal("100"))
                        .divide(BigDecimal.valueOf(drawBase), 1, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;
                    return MemberGrowthReportRes.ProductConcentration.builder()
                        .lotteryId(rs.getString("lottery_id"))
                        .lotteryTitle(rs.getString("lottery_title"))
                        .category(rs.getString("category"))
                        .drawCount(drawCount)
                        .drawPercentage(percentage)
                        .build();
                });

            // ── Q11: 金幣 / 紅利消耗分布 ───────────────────────────────────────
            String coinSql = """
                SELECT coin_type, COALESCE(ABS(SUM(amount)), 0) AS total
                FROM wallet_transaction
                WHERE transaction_type = 'DRAW'
                  AND created_at BETWEEN ? AND ?
                GROUP BY coin_type
                """;
            long goldSpend = 0L;
            long bonusSpend = 0L;
            List<Map<String, Object>> coinRows = jdbcTemplate.queryForList(coinSql, startDt, endDt);
            for (Map<String, Object> row : coinRows) {
                String coinType = row.get("coin_type") != null ? row.get("coin_type").toString() : "";
                long total = toLong(row.get("total"));
                if ("GOLD".equalsIgnoreCase(coinType)) {
                goldSpend = total;
                } else if ("BONUS".equalsIgnoreCase(coinType)) {
                bonusSpend = total;
                }
            }
            long totalSpend = goldSpend + bonusSpend;
            MemberGrowthReportRes.CoinUsageDistribution coinUsageDistribution = MemberGrowthReportRes.CoinUsageDistribution.builder()
                .goldSpend(goldSpend)
                .bonusSpend(bonusSpend)
                .goldPercentage(totalSpend > 0
                    ? BigDecimal.valueOf(goldSpend).multiply(new BigDecimal("100"))
                    .divide(BigDecimal.valueOf(totalSpend), 1, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO)
                .bonusPercentage(totalSpend > 0
                    ? BigDecimal.valueOf(bonusSpend).multiply(new BigDecimal("100"))
                    .divide(BigDecimal.valueOf(totalSpend), 1, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO)
                .build();

            // ── Q12: 支付型態分布（recharge_order）─────────────────────────────
            String paymentSql = """
                SELECT
                    COALESCE(gateway_provider, 'UNKNOWN') AS payment_method,
                    COUNT(*) AS txn_count,
                    COALESCE(SUM(price_twd), 0) AS total_amount
                FROM recharge_order
                WHERE status = 'PAID'
                  AND paid_at BETWEEN ? AND ?
                GROUP BY COALESCE(gateway_provider, 'UNKNOWN')
                ORDER BY total_amount DESC, txn_count DESC
                """;

            List<Map<String, Object>> paymentRows = jdbcTemplate.queryForList(paymentSql, startDt, endDt);
            BigDecimal paymentTotalAmount = BigDecimal.ZERO;
            for (Map<String, Object> row : paymentRows) {
                paymentTotalAmount = paymentTotalAmount.add(toBigDecimal(row.get("total_amount")));
            }
            final BigDecimal paymentBase = paymentTotalAmount;
            List<MemberGrowthReportRes.PaymentMethodDistribution> paymentMethodDistributions = paymentRows.stream()
                .map(row -> {
                    BigDecimal amount = toBigDecimal(row.get("total_amount"));
                    return MemberGrowthReportRes.PaymentMethodDistribution.builder()
                        .paymentMethod(row.get("payment_method") != null ? row.get("payment_method").toString() : "UNKNOWN")
                        .transactionCount(toInteger(row.get("txn_count")))
                        .totalAmount(amount)
                        .percentage(paymentBase.compareTo(BigDecimal.ZERO) > 0
                            ? amount.multiply(new BigDecimal("100")).divide(paymentBase, 1, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO)
                        .build();
                })
                .collect(Collectors.toList());

        return MemberGrowthReportRes.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalNewMembers(totalNewMembers)
                .growthRate(growthRate)
                .registrationByProvider(registrationByProvider)
                .dailyNewMembers(dailyNewMembers)
                .activeMembers(activeMembers)
                .arpuGold(arpuGold)
                .arpuBonus(arpuBonus)
                .retention7Days(retention7Days)
                .retention30Days(retention30Days)
                .consumptionPatterns(consumptionPatterns)
                .productConcentrations(productConcentrations)
                .coinUsageDistribution(coinUsageDistribution)
                .paymentMethodDistributions(paymentMethodDistributions)
                .build();
    }

    @Override
    public LotterySalesRankingRes getLotterySalesRanking(QueryReq<LotterySalesRankingCondition> req) {
        LotterySalesRankingCondition condition = (req != null && req.getCondition() != null)
                ? req.getCondition()
                : new LotterySalesRankingCondition();

        String storeId = condition.getStoreId();
        int limit = Math.min(condition.getLimit() != null ? condition.getLimit() : 20, 100);
        String sortBy = (req != null && "revenue".equalsIgnoreCase(req.getSortBy()))
                ? "revenue"
                : "draw_count";

        log.info("📊 商品銷售排行: storeId={}, limit={}, sortBy={}", storeId, limit, sortBy);

        String baseSql = """
                SELECT
                    l.id        AS lottery_id,
                    l.title     AS lottery_title,
                    s.store_name,
                    COALESCE(dc.draw_count, 0) AS draw_count,
                    COALESCE(rv.revenue, 0) AS revenue
                FROM lottery l
                JOIN store s ON %s
                LEFT JOIN (
                    SELECT lottery_id, COUNT(*) AS draw_count
                    FROM lottery_ticket
                    WHERE status = 'DRAWN'
                    GROUP BY lottery_id
                ) dc ON %s
                LEFT JOIN (
                    SELECT oi.lottery_id,
                           COUNT(oi.id) * MAX(l2.price_per_draw) AS revenue
                    FROM order_item oi
                    JOIN `order` o ON %s
                                  AND o.status != 'CANCELLED'
                    JOIN lottery l2 ON %s
                    GROUP BY oi.lottery_id
                ) rv ON %s
                WHERE 1 = 1
                """.formatted(
                utf8mb4Eq("l.store_id", "s.id"),
                utf8mb4Eq("dc.lottery_id", "l.id"),
                utf8mb4Eq("o.id", "oi.order_id"),
                utf8mb4Eq("l2.id", "oi.lottery_id"),
                utf8mb4Eq("rv.lottery_id", "l.id"));

        List<Object> params = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder(baseSql);
        if (storeId != null) {
            sqlBuilder.append("AND l.store_id = ?\n");
            params.add(storeId);
        }

        String filteredSql = sqlBuilder.toString();
        String countSql = "SELECT COUNT(*) FROM (" + filteredSql + ") AS total_count";
        Integer totalRecords = jdbcTemplate.queryForObject(countSql, Integer.class, params.toArray());
        if (totalRecords == null) totalRecords = 0;

        String querySql = filteredSql + " ORDER BY " + sortBy + " DESC LIMIT ?";
        List<Object> queryParams = new ArrayList<>(params);
        queryParams.add(limit);

        List<LotterySalesRankingRes.LotterySalesItem> items = jdbcTemplate.query(
                querySql,
                (rs, rowNum) -> LotterySalesRankingRes.LotterySalesItem.builder()
                        .lotteryId(rs.getString("lottery_id"))
                        .lotteryTitle(rs.getString("lottery_title"))
                        .storeName(rs.getString("store_name"))
                        .drawCount(rs.getInt("draw_count"))
                        .revenue(rs.getLong("revenue"))
                        .rank(rowNum + 1)
                        .build(),
                queryParams.toArray()
        );

        return LotterySalesRankingRes.builder()
                .totalRecords(totalRecords)
                .items(items)
                .build();
    }

    /** 允許 sortBy 的白名單欄位 */
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "totalRevenue", "drawCount", "activeUsers", "shipRate", "overdueRate", "avgShipDays");

    @Override
    public StorePerformanceReportRes getStorePerformanceReport(QueryReq<StorePerformanceCondition> req) {
        StorePerformanceCondition condition = req != null && req.getCondition() != null
                ? req.getCondition() : new StorePerformanceCondition();

        LocalDate startDate = condition.getStartDate() != null
                ? condition.getStartDate() : LocalDate.now().minusDays(30);
        LocalDate endDate = condition.getEndDate() != null
                ? condition.getEndDate() : LocalDate.now();
        String storeId = condition.getStoreId();

        String rawSortBy = req != null ? req.getSortBy() : null;
        String sortBy = (rawSortBy != null && ALLOWED_SORT_FIELDS.contains(rawSortBy))
                ? rawSortBy : "totalRevenue";
        String sortOrder = (req != null && req.getSortOrder() != null) ? req.getSortOrder() : "DESC";

        log.info("📊 店家績效報表: storeId={}, {} ~ {}, sortBy={} {}", storeId, startDate, endDate, sortBy, sortOrder);

        Map<String, Integer> drawCounts = queryStoreDrawCount(storeId, startDate, endDate);
        Map<String, Long> revenues = queryStoreTotalRevenue(storeId, startDate, endDate);
        Map<String, Integer> activeUsersMap = queryStoreActiveUsers(storeId, startDate, endDate);
        Map<String, OrderStats> orderStatsMap = queryStoreOrderStats(storeId, startDate, endDate);

        String storeSql = storeId != null
                ? "SELECT id, store_name FROM store WHERE id = ?"
                : "SELECT id, store_name FROM store";
        List<Map<String, Object>> storeRows = storeId != null
                ? jdbcTemplate.queryForList(storeSql, storeId)
                : jdbcTemplate.queryForList(storeSql);

        List<StorePerformanceReportRes.StoreItem> storeItems = storeRows.stream().map(row -> {
            String sid = (String) row.get("id");
            String sname = (String) row.get("store_name");

            Integer dc = drawCounts.getOrDefault(sid, 0);
            Long rev = revenues.getOrDefault(sid, 0L);
            Integer au = activeUsersMap.getOrDefault(sid, 0);
            OrderStats os = orderStatsMap.get(sid);

            Double shipRate = null;
            Double overdueRate = null;
            if (os != null) {
                if (os.nonCancelled > 0) {
                    shipRate = Math.round((double) os.shipped / os.nonCancelled * 100 * 10.0) / 10.0;
                }
                if (os.total > 0) {
                    overdueRate = Math.round((double) os.overdue / os.total * 100 * 10.0) / 10.0;
                }
            }

            return StorePerformanceReportRes.StoreItem.builder()
                    .storeId(sid)
                    .storeName(sname)
                    .totalRevenue(rev)
                    .drawCount(dc)
                    .activeUsers(au)
                    .shipRate(shipRate)
                    .overdueRate(overdueRate)
                    .avgShipDays(null)
                    .build();
        }).collect(Collectors.toCollection(ArrayList::new));

        Comparator<StorePerformanceReportRes.StoreItem> cmp = buildStoreComparator(sortBy);
        storeItems.sort("ASC".equalsIgnoreCase(sortOrder) ? cmp : cmp.reversed());

        List<StorePerformanceReportRes.DailyStat> dailyStats = null;
        if (storeId != null) {
            dailyStats = queryStoreDailyStats(storeId, startDate, endDate);
        }

        return StorePerformanceReportRes.builder()
                .startDate(startDate)
                .endDate(endDate)
                .stores(storeItems)
                .dailyStats(dailyStats)
                .build();
    }

    /** 訂單出貨率 / 逾期率原始資料 */
    private static class OrderStats {
        long shipped;
        long nonCancelled;
        long overdue;
        long total;
    }

    private Map<String, Integer> queryStoreDrawCount(String storeId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        List<Object> params = new ArrayList<>(List.of(start, end));
        String sql = ("""
                SELECT l.store_id, COUNT(*) AS draw_count
                FROM lottery_ticket lt
                JOIN lottery l ON %s
                WHERE lt.status = 'DRAWN'
                AND lt.drawn_at BETWEEN ? AND ?
                """ + (storeId != null ? "AND l.store_id = ? " : "") + """
                GROUP BY l.store_id
                """).formatted(utf8mb4Eq("lt.lottery_id", "l.id"));
        if (storeId != null) params.add(storeId);

        Map<String, Integer> result = new HashMap<>();
        jdbcTemplate.query(sql, params.toArray(), (RowCallbackHandler) rs ->
                result.put(rs.getString("store_id"), rs.getInt("draw_count")));
        return result;
    }

    private Map<String, Long> queryStoreTotalRevenue(String storeId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        List<Object> params = new ArrayList<>(List.of(start, end));
        String sql = ("""
                SELECT l.store_id, ABS(SUM(wt.amount)) AS total_revenue
                FROM wallet_transaction wt
                JOIN lottery_ticket lt ON %s
                JOIN lottery l ON %s
                WHERE wt.transaction_type = 'DRAW'
                AND wt.created_at BETWEEN ? AND ?
                """ + (storeId != null ? "AND l.store_id = ? " : "") + """
                GROUP BY l.store_id
                """).formatted(utf8mb4Eq("wt.related_id", "lt.id"), utf8mb4Eq("lt.lottery_id", "l.id"));
        if (storeId != null) params.add(storeId);

        Map<String, Long> result = new HashMap<>();
        jdbcTemplate.query(sql, params.toArray(), (RowCallbackHandler) rs ->
                result.put(rs.getString("store_id"), rs.getLong("total_revenue")));
        return result;
    }

    private Map<String, Integer> queryStoreActiveUsers(String storeId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        String sql;
        Object[] params;
        if (storeId != null) {
            sql = """
                    SELECT t.store_id, COUNT(DISTINCT uid) AS active_users FROM (
                      SELECT %s AS store_id, %s AS uid
                      FROM lottery_ticket lt
                      JOIN lottery l ON %s
                      WHERE lt.drawn_at BETWEEN ? AND ? AND lt.drawn_by IS NOT NULL
                      UNION
                      SELECT %s AS store_id, %s AS uid
                      FROM `order` o
                      WHERE o.created_at BETWEEN ? AND ?
                    ) t WHERE t.store_id = ?
                    GROUP BY t.store_id
                    """.formatted(
                    utf8mb4Text("l.store_id"),
                    utf8mb4Text("lt.drawn_by"),
                    utf8mb4Eq("lt.lottery_id", "l.id"),
                    utf8mb4Text("o.store_id"),
                    utf8mb4Text("o.user_id"));
            params = new Object[]{start, end, start, end, storeId};
        } else {
            sql = """
                    SELECT t.store_id, COUNT(DISTINCT uid) AS active_users FROM (
                      SELECT %s AS store_id, %s AS uid
                      FROM lottery_ticket lt
                      JOIN lottery l ON %s
                      WHERE lt.drawn_at BETWEEN ? AND ? AND lt.drawn_by IS NOT NULL
                      UNION
                      SELECT %s AS store_id, %s AS uid
                      FROM `order` o
                      WHERE o.created_at BETWEEN ? AND ?
                    ) t GROUP BY t.store_id
                    """.formatted(
                    utf8mb4Text("l.store_id"),
                    utf8mb4Text("lt.drawn_by"),
                    utf8mb4Eq("lt.lottery_id", "l.id"),
                    utf8mb4Text("o.store_id"),
                    utf8mb4Text("o.user_id"));
            params = new Object[]{start, end, start, end};
        }

        Map<String, Integer> result = new HashMap<>();
        jdbcTemplate.query(sql, params, (RowCallbackHandler) rs ->
                result.put(rs.getString("store_id"), rs.getInt("active_users")));
        return result;
    }

    private Map<String, OrderStats> queryStoreOrderStats(String storeId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        List<Object> params = new ArrayList<>(List.of(start, end));
        String sql = """
                SELECT store_id,
                  SUM(CASE WHEN status IN ('SHIPPED','COMPLETED') THEN 1 ELSE 0 END) AS shipped,
                  SUM(CASE WHEN status != 'CANCELLED' THEN 1 ELSE 0 END) AS non_cancelled,
                  SUM(CASE WHEN status = 'PENDING' AND created_at < DATE_SUB(NOW(), INTERVAL 7 DAY) THEN 1 ELSE 0 END) AS overdue,
                  COUNT(*) AS total
                FROM `order`
                WHERE created_at BETWEEN ? AND ?
                """ + (storeId != null ? "AND store_id = ? " : "") + """
                GROUP BY store_id
                """;
        if (storeId != null) params.add(storeId);

        Map<String, OrderStats> result = new HashMap<>();
        jdbcTemplate.query(sql, params.toArray(), (RowCallbackHandler) rs -> {
            OrderStats os = new OrderStats();
            os.shipped = rs.getLong("shipped");
            os.nonCancelled = rs.getLong("non_cancelled");
            os.overdue = rs.getLong("overdue");
            os.total = rs.getLong("total");
            result.put(rs.getString("store_id"), os);
        });
        return result;
    }

    private List<StorePerformanceReportRes.DailyStat> queryStoreDailyStats(
            String storeId, LocalDate startDate, LocalDate endDate) {

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        String dailySql = """
                SELECT DATE(lt.drawn_at) AS stat_date,
                       COUNT(*) AS draw_count,
                       ABS(COALESCE(SUM(wt.amount), 0)) AS revenue
                FROM lottery_ticket lt
                JOIN lottery l ON %s
                LEFT JOIN wallet_transaction wt ON %s AND wt.transaction_type = 'DRAW'
                WHERE l.store_id = ? AND lt.drawn_at BETWEEN ? AND ? AND lt.status = 'DRAWN'
                GROUP BY DATE(lt.drawn_at)
                ORDER BY stat_date
                """.formatted(utf8mb4Eq("lt.lottery_id", "l.id"), utf8mb4Eq("wt.related_id", "lt.id"));

        Map<LocalDate, StorePerformanceReportRes.DailyStat.DailyStatBuilder> builderMap = new LinkedHashMap<>();
        jdbcTemplate.query(dailySql, new Object[]{storeId, start, end}, (RowCallbackHandler) rs -> {
            LocalDate date = rs.getDate("stat_date").toLocalDate();
            builderMap.put(date, StorePerformanceReportRes.DailyStat.builder()
                    .date(date)
                    .drawCount(rs.getInt("draw_count"))
                    .revenue(rs.getLong("revenue"))
                    .newUsers(0));
        });

        String newUsersSql = """
                SELECT DATE(first_date) AS stat_date, COUNT(*) AS new_users FROM (
                  SELECT uid, MIN(activity_date) AS first_date FROM (
                    SELECT %s AS uid, DATE(lt.drawn_at) AS activity_date
                    FROM lottery_ticket lt
                    JOIN lottery l ON %s
                    WHERE l.store_id = ? AND lt.drawn_at BETWEEN ? AND ? AND lt.drawn_by IS NOT NULL
                    UNION ALL
                    SELECT %s AS uid, DATE(o.created_at) AS activity_date
                    FROM `order` o
                    WHERE o.store_id = ? AND o.created_at BETWEEN ? AND ?
                  ) all_activity GROUP BY uid
                ) first_seen GROUP BY DATE(first_date)
                """.formatted(
                utf8mb4Text("lt.drawn_by"),
                utf8mb4Eq("lt.lottery_id", "l.id"),
                utf8mb4Text("o.user_id"));

        jdbcTemplate.query(newUsersSql, new Object[]{storeId, start, end, storeId, start, end}, (RowCallbackHandler) rs -> {
            LocalDate date = rs.getDate("stat_date").toLocalDate();
            if (builderMap.containsKey(date)) {
                builderMap.get(date).newUsers(rs.getInt("new_users"));
            }
        });

        return builderMap.values().stream()
                .map(StorePerformanceReportRes.DailyStat.DailyStatBuilder::build)
                .collect(Collectors.toList());
    }

    private Comparator<StorePerformanceReportRes.StoreItem> buildStoreComparator(String sortBy) {
        return switch (sortBy) {
            case "drawCount" -> Comparator.comparingInt(
                    (StorePerformanceReportRes.StoreItem i) -> i.getDrawCount() != null ? i.getDrawCount() : 0);
            case "activeUsers" -> Comparator.comparingInt(
                    (StorePerformanceReportRes.StoreItem i) -> i.getActiveUsers() != null ? i.getActiveUsers() : 0);
            case "shipRate" -> Comparator.comparingDouble(
                    (StorePerformanceReportRes.StoreItem i) -> i.getShipRate() != null ? i.getShipRate() : 0.0);
            case "overdueRate" -> Comparator.comparingDouble(
                    (StorePerformanceReportRes.StoreItem i) -> i.getOverdueRate() != null ? i.getOverdueRate() : 0.0);
            case "avgShipDays" -> Comparator.comparingDouble(
                    (StorePerformanceReportRes.StoreItem i) -> i.getAvgShipDays() != null ? i.getAvgShipDays() : 0.0);
            default -> Comparator.comparingLong(
                    (StorePerformanceReportRes.StoreItem i) -> i.getTotalRevenue() != null ? i.getTotalRevenue() : 0L);
        };
    }

    private long queryTotalRecharge(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();
        BigDecimal value = jdbcTemplate.queryForObject("""
                SELECT COALESCE(SUM(amount), 0)
                FROM wallet_transaction
                WHERE transaction_type = 'RECHARGE'
                  AND coin_type = 'GOLD'
                  AND created_at BETWEEN ? AND ?
                """, BigDecimal.class, start, end);
        return value != null ? value.longValue() : 0L;
    }

    private long queryTotalSpend(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();
        BigDecimal value = jdbcTemplate.queryForObject("""
                SELECT COALESCE(ABS(SUM(amount)), 0)
                FROM wallet_transaction
                WHERE transaction_type = 'DRAW'
                  AND created_at BETWEEN ? AND ?
                """, BigDecimal.class, start, end);
        return value != null ? value.longValue() : 0L;
    }

    private PlatformRevenueReportRes.SpendByType querySpendByType(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT coin_type, COALESCE(ABS(SUM(amount)), 0) AS total
                FROM wallet_transaction
                WHERE transaction_type = 'DRAW'
                  AND created_at BETWEEN ? AND ?
                GROUP BY coin_type
                """, start, end);

        long gold = 0L;
        long bonus = 0L;
        for (Map<String, Object> row : rows) {
            String coinType = row.get("coin_type") != null ? row.get("coin_type").toString() : "";
            long total = toLong(row.get("total"));
            if ("GOLD".equalsIgnoreCase(coinType)) {
                gold = total;
            } else if ("BONUS".equalsIgnoreCase(coinType)) {
                bonus = total;
            }
        }

        return PlatformRevenueReportRes.SpendByType.builder()
                .gold(gold)
                .bonus(bonus)
                .build();
    }

    private long queryPlatformDrawCount(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();
        Long value = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM lottery_ticket
                WHERE status = 'DRAWN'
                  AND drawn_at BETWEEN ? AND ?
                """, Long.class, start, end);
        return value != null ? value : 0L;
    }

    private Integer queryActivatedReferralStoreCount(LocalDate startDate, LocalDate endDate, String referrerStoreId) {
        String sql = """
                SELECT COUNT(*)
                FROM store s
                WHERE s.referrer_store_id IS NOT NULL
                  AND s.activated_at >= ?
                  AND s.activated_at < ?
                """;
        List<Object> params = new ArrayList<>();
        params.add(startDate.atStartOfDay());
        params.add(endDate.plusDays(1).atStartOfDay());
        if (referrerStoreId != null && !referrerStoreId.isBlank()) {
            sql += " AND s.referrer_store_id = ?";
            params.add(referrerStoreId);
        }
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, params.toArray());
        return count != null ? count : 0;
    }

    private LocalDate[] calculatePreviousRange(LocalDate startDate, LocalDate endDate) {
        long days = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        LocalDate prevEnd = startDate.minusDays(1);
        LocalDate prevStart = prevEnd.minusDays(days - 1);
        return new LocalDate[]{prevStart, prevEnd};
    }

    private List<PlatformRevenueReportRes.DailyRevenueItem> buildDailyRevenue(LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, Long> rechargeMap = queryDailyAmountByType(startDate, endDate, "RECHARGE", "GOLD");
        Map<LocalDate, Long> spendMap = queryDailyAmountByType(startDate, endDate, "DRAW", null);

        List<PlatformRevenueReportRes.DailyRevenueItem> result = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            long recharge = rechargeMap.getOrDefault(date, 0L);
            long spend = spendMap.getOrDefault(date, 0L);
            result.add(PlatformRevenueReportRes.DailyRevenueItem.builder()
                    .date(date)
                    .recharge(recharge)
                    .spend(spend)
                    .net(recharge - spend)
                    .build());
        }
        return result;
    }

    private Map<LocalDate, Long> queryDailyAmountByType(LocalDate startDate, LocalDate endDate, String transactionType, String coinType) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();
        List<Object> params = new ArrayList<>(List.of(transactionType, start, end));
        String sql = """
                SELECT DATE(created_at) AS stat_date,
                       COALESCE(SUM(ABS(amount)), 0) AS total
                FROM wallet_transaction
                WHERE transaction_type = ?
                  AND created_at BETWEEN ? AND ?
                """;
        if (coinType != null) {
            sql += " AND coin_type = ? ";
            params.add(coinType);
        }
        sql += " GROUP BY DATE(created_at) ORDER BY stat_date";

        Map<LocalDate, Long> result = new LinkedHashMap<>();
        jdbcTemplate.query(sql, params.toArray(), (RowCallbackHandler) rs ->
                result.put(rs.getDate("stat_date").toLocalDate(), rs.getLong("total")));
        return result;
    }

    private List<PlatformRevenueReportRes.StoreBreakdownItem> queryStoreBreakdown(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        String spendSql = """
                SELECT mapped.store_id,
                       mapped.store_name,
                       ABS(SUM(mapped.amount)) AS total_spend
                FROM (
                    SELECT COALESCE(%s, %s) AS store_id,
                           s.store_name,
                           wt.amount AS amount
                    FROM wallet_transaction wt
                    LEFT JOIN lottery_ticket lt ON %s
                    LEFT JOIN lottery l ON %s
                    LEFT JOIN `order` o ON %s
                    LEFT JOIN store s ON %s
                    WHERE wt.transaction_type = 'DRAW'
                      AND wt.created_at BETWEEN ? AND ?
                ) mapped
                WHERE mapped.store_id IS NOT NULL
                GROUP BY mapped.store_id, mapped.store_name
                ORDER BY total_spend DESC
                """.formatted(
                utf8mb4Text("l.store_id"),
                utf8mb4Text("o.store_id"),
                utf8mb4Eq("wt.related_id", "lt.id"),
                utf8mb4Eq("lt.lottery_id", "l.id"),
                utf8mb4Eq("wt.related_id", "o.id"),
                utf8mb4Eq("s.id", "COALESCE(l.store_id, o.store_id)"));

        Map<String, Long> drawCounts = queryStoreDrawCountForPlatformRevenue(startDate, endDate);
        List<PlatformRevenueReportRes.StoreBreakdownItem> result = new ArrayList<>();
        jdbcTemplate.query(spendSql, new Object[]{start, end}, (RowCallbackHandler) rs -> {
            String storeId = rs.getString("store_id");
            result.add(PlatformRevenueReportRes.StoreBreakdownItem.builder()
                    .storeId(storeId)
                    .storeName(rs.getString("store_name"))
                    .totalSpend(rs.getLong("total_spend"))
                    .drawCount(drawCounts.getOrDefault(storeId, 0L))
                    .build());
        });
        return result;
    }

    private Map<String, Long> queryStoreDrawCountForPlatformRevenue(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();
        Map<String, Long> result = new HashMap<>();
        jdbcTemplate.query("""
                SELECT l.store_id, COUNT(*) AS draw_count
                FROM lottery_ticket lt
                JOIN lottery l ON %s
                WHERE lt.status = 'DRAWN'
                  AND lt.drawn_at BETWEEN ? AND ?
                GROUP BY l.store_id
                """.formatted(utf8mb4Eq("lt.lottery_id", "l.id")), new Object[]{start, end}, (RowCallbackHandler) rs ->
                result.put(rs.getString("store_id"), rs.getLong("draw_count")));
        return result;
    }

    // ========== 工具方法 ==========

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Number) return new BigDecimal(value.toString());
        return BigDecimal.ZERO;
    }
    
    private Integer toInteger(Object value) {
        if (value == null) return 0;
        if (value instanceof Number) return ((Number) value).intValue();
        return 0;
    }

    private long toLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Number) return ((Number) value).longValue();
        return 0L;
    }
    
    private BigDecimal calculateGrowthRate(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? new BigDecimal("100") : BigDecimal.ZERO;
        }
        return current.subtract(previous)
                .multiply(new BigDecimal("100"))
                .divide(previous, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateNullableGrowthRate(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        return current.subtract(previous)
                .multiply(new BigDecimal("100"))
                .divide(previous, 1, RoundingMode.HALF_UP);
    }
    
    private String getBonusTypeName(String bonusType) {
        return switch (bonusType) {
            case "BONUS" -> "一般贈送";
            case "REFERRAL_BONUS" -> "推薦獎勵";
            case "PROMOTION" -> "活動贈送";
            case "ADJUSTMENT" -> "人工調整";
            default -> bonusType;
        };
    }

    // ============================= 獎品出貨報表 =============================

    @Override
    public PrizeShipmentReportRes getPrizeShipmentReport(QueryReq<PrizeShipmentReportCondition> req) {
        PrizeShipmentReportCondition condition = (req != null && req.getCondition() != null)
                ? req.getCondition()
                : new PrizeShipmentReportCondition();

        String storeId = condition.getStoreId();

        // 日期預設：最近 30 天
        LocalDate endDate   = condition.getEndDate()   != null ? condition.getEndDate()   : LocalDate.now();
        LocalDate startDate = condition.getStartDate() != null ? condition.getStartDate() : endDate.minusDays(29);

        log.info("📦 產生獎品出貨報表: storeId={}, {} ~ {}", storeId, startDate, endDate);

        // --- US1: 狀態計數 ---
        Map<String, Object> statusCounts = queryStatusCounts(storeId, startDate, endDate);
        Integer pendingCount   = toInt(statusCounts.get("pending_count"));
        Integer preparingCount = toInt(statusCounts.get("preparing_count"));
        Integer shippedCount   = toInt(statusCounts.get("shipped_count"));
        Integer completedCount = toInt(statusCounts.get("completed_count"));

        // --- US1: 每日出貨明細 ---
        List<PrizeShipmentReportRes.DailyShipment> dailyDetails =
                queryDailyDetails(storeId, startDate, endDate);

        // --- US2: 平均出貨天數 ---
        BigDecimal avgShipDays = queryAvgShipDays(storeId, startDate, endDate);

        // --- US2: 逾期未備貨計數（不受日期範圍限制） ---
        Integer overdueCount = queryOverdueCount(storeId);

        // --- US3: 跨店家統計（Admin 限定：storeId == null） ---
        List<PrizeShipmentReportRes.StoreShipment> storeDetails = null;
        if (storeId == null) {
            storeDetails = queryStoreDetails(startDate, endDate);
        }

        return PrizeShipmentReportRes.builder()
                .startDate(startDate)
                .endDate(endDate)
                .pendingCount(pendingCount)
                .preparingCount(preparingCount)
                .shippedCount(shippedCount)
                .completedCount(completedCount)
                .avgShipDays(avgShipDays)
                .overdueCount(overdueCount)
                .dailyDetails(dailyDetails)
                .storeDetails(storeDetails)
                .build();
    }

    /** T011: 4 個狀態計數 */
    private Map<String, Object> queryStatusCounts(String storeId, LocalDate startDate, LocalDate endDate) {
        String sql = """
                SELECT
                    SUM(CASE WHEN status = 'PENDING'    THEN 1 ELSE 0 END) AS pending_count,
                    SUM(CASE WHEN status = 'PREPARING'  THEN 1 ELSE 0 END) AS preparing_count,
                    SUM(CASE WHEN status = 'SHIPPED'    THEN 1 ELSE 0 END) AS shipped_count,
                    SUM(CASE WHEN status = 'COMPLETED'  THEN 1 ELSE 0 END) AS completed_count
                FROM `order`
                WHERE status NOT IN ('CANCELLED', 'PAYMENT_PENDING')
                  AND created_at BETWEEN ? AND ?
                """;

        List<Object> params = new ArrayList<>();
        params.add(startDate.atStartOfDay());
        params.add(endDate.plusDays(1).atStartOfDay());

        if (storeId != null) {
            sql += " AND store_id = ?";
            params.add(storeId);
        }

        Map<String, Object> result = jdbcTemplate.queryForMap(sql, params.toArray());
        return result != null ? result : new HashMap<>();
    }

    /** T012: 每日出貨明細，按 DATE(shipped_at) 分組 */
    private List<PrizeShipmentReportRes.DailyShipment> queryDailyDetails(
            String storeId, LocalDate startDate, LocalDate endDate) {

        String sql = """
                SELECT
                    DATE(shipped_at) AS date,
                    COUNT(*) AS shipped_count
                FROM `order`
                WHERE status IN ('SHIPPED', 'COMPLETED')
                  AND shipped_at BETWEEN ? AND ?
                """;

        List<Object> params = new ArrayList<>();
        params.add(startDate.atStartOfDay());
        params.add(endDate.plusDays(1).atStartOfDay());

        if (storeId != null) {
            sql += " AND store_id = ?";
            params.add(storeId);
        }
        sql += " GROUP BY DATE(shipped_at) ORDER BY date";

        return jdbcTemplate.query(sql, params.toArray(), (rs, rowNum) ->
                PrizeShipmentReportRes.DailyShipment.builder()
                        .date(rs.getDate("date").toLocalDate())
                        .shippedCount(rs.getInt("shipped_count"))
                        .build()
        );
    }

    /** T016: 平均出貨天數（preparing_at → shipped_at），精確至 0.1 天 */
    private BigDecimal queryAvgShipDays(String storeId, LocalDate startDate, LocalDate endDate) {
        String sql = """
                SELECT ROUND(AVG(DATEDIFF(shipped_at, preparing_at)), 1) AS avg_ship_days
                FROM `order`
                WHERE status IN ('SHIPPED', 'COMPLETED')
                  AND preparing_at IS NOT NULL
                  AND shipped_at IS NOT NULL
                  AND created_at BETWEEN ? AND ?
                """;

        List<Object> params = new ArrayList<>();
        params.add(startDate.atStartOfDay());
        params.add(endDate.plusDays(1).atStartOfDay());

        if (storeId != null) {
            sql += " AND store_id = ?";
            params.add(storeId);
        }

        return jdbcTemplate.queryForObject(sql, params.toArray(), BigDecimal.class);
    }

    /** T017: 逾期未備貨計數（intentionally 不受日期範圍過濾，反映即時狀態） */
    private Integer queryOverdueCount(String storeId) {
        String sql = """
                SELECT COUNT(*) AS overdue_count
                FROM `order`
                WHERE status = 'PENDING'
                  AND created_at < NOW() - INTERVAL 7 DAY
                """;

        List<Object> params = new ArrayList<>();

        if (storeId != null) {
            sql += " AND store_id = ?";
            params.add(storeId);
        }

        Long count = jdbcTemplate.queryForObject(sql, params.toArray(), Long.class);
        return count != null ? count.intValue() : 0;
    }

    /** T019: 跨店家統計（Admin 限定，storeId == null 時執行） */
    private List<PrizeShipmentReportRes.StoreShipment> queryStoreDetails(
            LocalDate startDate, LocalDate endDate) {

        String sql = """
                SELECT
                    store_id,
                    store_name,
                    SUM(CASE WHEN status = 'PENDING'    THEN 1 ELSE 0 END) AS pending_count,
                    SUM(CASE WHEN status = 'PREPARING'  THEN 1 ELSE 0 END) AS preparing_count,
                    SUM(CASE WHEN status = 'SHIPPED'    THEN 1 ELSE 0 END) AS shipped_count,
                    SUM(CASE WHEN status = 'COMPLETED'  THEN 1 ELSE 0 END) AS completed_count,
                    ROUND(AVG(CASE
                        WHEN status IN ('SHIPPED', 'COMPLETED')
                             AND preparing_at IS NOT NULL
                             AND shipped_at IS NOT NULL
                        THEN DATEDIFF(shipped_at, preparing_at)
                    END), 1) AS avg_ship_days,
                    SUM(CASE
                        WHEN status = 'PENDING' AND created_at < NOW() - INTERVAL 7 DAY
                        THEN 1 ELSE 0
                    END) AS overdue_count
                FROM `order`
                WHERE status NOT IN ('CANCELLED', 'PAYMENT_PENDING')
                  AND created_at BETWEEN ? AND ?
                GROUP BY store_id, store_name
                ORDER BY avg_ship_days DESC
                """;

        return jdbcTemplate.query(sql,
                new Object[]{startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay()},
                (rs, rowNum) -> PrizeShipmentReportRes.StoreShipment.builder()
                        .storeId(rs.getString("store_id"))
                        .storeName(rs.getString("store_name"))
                        .pendingCount(rs.getInt("pending_count"))
                        .preparingCount(rs.getInt("preparing_count"))
                        .shippedCount(rs.getInt("shipped_count"))
                        .completedCount(rs.getInt("completed_count"))
                        .avgShipDays(rs.getBigDecimal("avg_ship_days"))
                        .overdueCount(rs.getInt("overdue_count"))
                        .build()
        );
    }

    /** null 安全的 Integer 轉換（處理 SUM 在無資料時回傳 null） */
    private Integer toInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Number) return ((Number) value).intValue();
        return 0;
    }

    private String utf8mb4Text(String expr) {
        return "CAST(" + expr + " AS CHAR(" + MYSQL_TEXT_LENGTH + ")) COLLATE " + MYSQL_TEXT_COLLATION;
    }

    private String utf8mb4Eq(String left, String right) {
        return utf8mb4Text(left) + " = " + utf8mb4Text(right);
    }
}
