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
            LEFT JOIN store s ON o.store_id = s.id
            WHERE o.status IN ('PAID', 'COMPLETED')
            AND o.created_at BETWEEN ? AND ?
            GROUP BY o.store_id, s.name
            ORDER BY revenue DESC
            """;
        
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
        ReferralReportCondition condition = req != null && req.getCondition() != null ? 
            req.getCondition():new ReferralReportCondition();
        
        String storeId = condition.getStoreId();
        LocalDate startDate = condition.getStartDate();
        LocalDate endDate = condition.getEndDate();
        
        log.info("📊 產生推薦碼報表: storeId={}, {} ~ {}", storeId, startDate, endDate);
        
        // 基本統計
        String baseSql = """
            SELECT 
                COUNT(DISTINCT u.id) as total_referrals,
                COALESCE(SUM(wt.amount), 0) as total_bonus
            FROM user u
            LEFT JOIN wallet_transaction wt ON wt.user_id = u.id 
                AND wt.transaction_type = 'REFERRAL_BONUS'
            WHERE u.referred_store_id IS NOT NULL
            AND u.created_at BETWEEN ? AND ?
            """;
        
        List<Object> params = new ArrayList<>();
        params.add(startDate.atStartOfDay());
        params.add(endDate.plusDays(1).atStartOfDay());
        
        if (storeId != null) {
            baseSql += " AND u.referred_store_id = ?";
            params.add(storeId);
        }
        
        Map<String, Object> stats = jdbcTemplate.queryForMap(baseSql, params.toArray());
        
        // 計算成長率
        LocalDate prevStartDate = startDate.minusDays(startDate.until(endDate).getDays() + 1);
        BigDecimal growthRate = BigDecimal.ZERO;
        
        // 每日明細
        List<ReferralReportRes.DailyReferral> dailyDetails = new ArrayList<>();
        
        // 排行榜
        String rankingSql = """
            SELECT 
                u.referral_code,
                u.nickname as user_name,
                s.name as store_name,
                COUNT(DISTINCT ref.id) as referral_count,
                COALESCE(SUM(wt.amount), 0) as total_bonus
            FROM user u
            LEFT JOIN store s ON u.referred_store_id = s.id
            LEFT JOIN user ref ON ref.referral_code = u.referral_code
            LEFT JOIN wallet_transaction wt ON wt.user_id = u.id 
                AND wt.transaction_type = 'REFERRAL_BONUS'
            WHERE u.referral_code IS NOT NULL
            GROUP BY u.id, u.referral_code, u.nickname, s.name
            HAVING referral_count > 0
            ORDER BY referral_count DESC
            LIMIT 20
            """;
        
        List<ReferralReportRes.ReferralRanking> rankings = jdbcTemplate.query(rankingSql,
            (rs, rowNum) -> ReferralReportRes.ReferralRanking.builder()
                .referralCode(rs.getString("referral_code"))
                .userName(rs.getString("user_name"))
                .storeName(rs.getString("store_name"))
                .referralCount(rs.getInt("referral_count"))
                .totalBonus(rs.getBigDecimal("total_bonus"))
                .rank(rowNum + 1)
                .build()
        );
        
        return ReferralReportRes.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalReferrals(toInteger(stats.get("total_referrals")))
                .totalBonusAmount(toBigDecimal(stats.get("total_bonus")))
                .conversionRate(BigDecimal.ZERO)
                .growthRate(growthRate)
                .dailyDetails(dailyDetails)
                .rankings(rankings)
                .build();
    }
    
    @Override
    public LotteryResultReportRes getLotteryResultReport(QueryReq<LotteryResultReportCondition> req) {
        LotteryResultReportCondition condition = req != null && req.getCondition() != null ? 
            req.getCondition() : new LotteryResultReportCondition();
        
        String storeId = condition.getStoreId();
        String lotteryId = condition.getLotteryId();
        LocalDate startDate = condition.getStartDate();
        LocalDate endDate = condition.getEndDate();
        
        // 基本統計
        String baseSql = """
            SELECT 
                COALESCE(SUM(draw_count), 0) as total_draws,
                COALESCE(SUM(total_amount), 0) as total_amount
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
        if (lotteryId != null) {
            baseSql += " AND lottery_id = ?";
            params.add(lotteryId);
        }
        
        Map<String, Object> stats = jdbcTemplate.queryForMap(baseSql, params.toArray());
        
        // 獎品統計
        String prizeSql = """
            SELECT 
                p.level as prize_level,
                COUNT(*) as total_count,
                SUM(CASE WHEN p.status = 'WON' THEN 1 ELSE 0 END) as won_count,
                SUM(CASE WHEN p.status != 'WON' THEN 1 ELSE 0 END) as remain_count
            FROM prize p
            JOIN lottery l ON p.lottery_id = l.id
            WHERE l.created_at BETWEEN ? AND ?
            """ + (storeId != null ? " AND l.store_id = ?" : "") +
            (lotteryId != null ? " AND p.lottery_id = ?" : "") + """
            GROUP BY p.level
            ORDER BY p.level
            """;
        
        List<Object> prizeParams = new ArrayList<>();
        prizeParams.add(startDate.atStartOfDay());
        prizeParams.add(endDate.plusDays(1).atStartOfDay());
        if (storeId != null) prizeParams.add(storeId);
        if (lotteryId != null) prizeParams.add(lotteryId);
        
        List<LotteryResultReportRes.PrizeStats> prizeStats = jdbcTemplate.query(prizeSql, prizeParams.toArray(),
            (rs, rowNum) -> {
                int totalCount = rs.getInt("total_count");
                int wonCount = rs.getInt("won_count");
                return LotteryResultReportRes.PrizeStats.builder()
                    .prizeLevel(rs.getString("prize_level"))
                    .totalCount(totalCount)
                    .wonCount(wonCount)
                    .remainCount(rs.getInt("remain_count"))
                    .wonPercentage(totalCount > 0 ? 
                        new BigDecimal(wonCount * 100).divide(new BigDecimal(totalCount), 2, RoundingMode.HALF_UP) 
                        : BigDecimal.ZERO)
                    .build();
            }
        );
        
        // 一番賞統計
        String lotterySql = """
            SELECT 
                l.id as lottery_id,
                l.title as lottery_title,
                s.name as store_name,
                l.total_slots,
                l.sold_slots,
                (l.total_slots - l.sold_slots) as remain_slots,
                l.price_per_draw * l.sold_slots as revenue
            FROM lottery l
            LEFT JOIN store s ON l.store_id = s.id
            WHERE l.created_at BETWEEN ? AND ?
            """ + (storeId != null ? " AND l.store_id = ?" : "") + """
            ORDER BY l.sold_slots DESC
            LIMIT 50
            """;
        
        List<Object> lotteryParams = new ArrayList<>();
        lotteryParams.add(startDate.atStartOfDay());
        lotteryParams.add(endDate.plusDays(1).atStartOfDay());
        if (storeId != null) lotteryParams.add(storeId);
        
        List<LotteryResultReportRes.LotteryStats> lotteryStats = jdbcTemplate.query(lotterySql, lotteryParams.toArray(),
            (rs, rowNum) -> {
                int totalSlots = rs.getInt("total_slots");
                int soldSlots = rs.getInt("sold_slots");
                return LotteryResultReportRes.LotteryStats.builder()
                    .lotteryId(rs.getString("lottery_id"))
                    .lotteryTitle(rs.getString("lottery_title"))
                    .storeName(rs.getString("store_name"))
                    .totalSlots(totalSlots)
                    .soldSlots(soldSlots)
                    .remainSlots(rs.getInt("remain_slots"))
                    .soldPercentage(totalSlots > 0 ?
                        new BigDecimal(soldSlots * 100).divide(new BigDecimal(totalSlots), 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO)
                    .revenue(rs.getBigDecimal("revenue"))
                    .build();
            }
        );
        
        // 計算大獎數量
        int bigPrizes = prizeStats.stream()
            .filter(p -> "A".equals(p.getPrizeLevel()) || 
                        "B".equals(p.getPrizeLevel()) || 
                        "LAST".equals(p.getPrizeLevel()))
            .mapToInt(LotteryResultReportRes.PrizeStats::getWonCount)
            .sum();
        
        int totalPrizes = prizeStats.stream()
            .mapToInt(LotteryResultReportRes.PrizeStats::getWonCount)
            .sum();
        
        return LotteryResultReportRes.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalDraws(toInteger(stats.get("total_draws")))
                .totalPrizes(totalPrizes)
                .bigPrizes(bigPrizes)
                .totalAmount(toBigDecimal(stats.get("total_amount")))
                .prizeStats(prizeStats)
                .lotteryStats(lotteryStats)
                .build();
    }
    
    @Override
    public RechargeReportRes getRechargeReport(QueryReq<RechargeReportCondition> req) {
        RechargeReportCondition condition = req != null && req.getCondition() != null ? 
            req.getCondition() : new RechargeReportCondition();
        
        String storeId = condition.getStoreId();
        LocalDate startDate = condition.getStartDate();
        LocalDate endDate = condition.getEndDate();
        
        log.info("📊 產生儲值報表: storeId={}, {} ~ {}", storeId, startDate, endDate);
        
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
        LocalDate prevStartDate = startDate.minusDays(startDate.until(endDate).getDays() + 1);
        LocalDate prevEndDate = startDate.minusDays(1);
        
        List<Object> prevParams = new ArrayList<>();
        prevParams.add(prevStartDate.atStartOfDay());
        prevParams.add(prevEndDate.plusDays(1).atStartOfDay());
        
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
        
        String storeId = condition.getStoreId();
        LocalDate startDate = condition.getStartDate();
        LocalDate endDate = condition.getEndDate();
        
        log.info("📊 產生贈送點數報表: storeId={}, {} ~ {}", storeId, startDate, endDate);
        
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
        LocalDate prevStartDate = startDate.minusDays(startDate.until(endDate).getDays() + 1);
        LocalDate prevEndDate = startDate.minusDays(1);
        
        List<Object> prevParams = new ArrayList<>();
        prevParams.add(prevStartDate.atStartOfDay());
        prevParams.add(prevEndDate.plusDays(1).atStartOfDay());
        
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
    
    private BigDecimal calculateGrowthRate(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? new BigDecimal("100") : BigDecimal.ZERO;
        }
        return current.subtract(previous)
                .multiply(new BigDecimal("100"))
                .divide(previous, 2, RoundingMode.HALF_UP);
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

    // ========== 店家績效報表 (034) ==========

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

        // --- KPI 查詢 ---
        Map<String, Integer> drawCounts   = queryStoreDrawCount(storeId, startDate, endDate);
        Map<String, Long>    revenues     = queryStoreTotalRevenue(storeId, startDate, endDate);
        Map<String, Integer> activeUsersMap = queryStoreActiveUsers(storeId, startDate, endDate);
        Map<String, OrderStats> orderStatsMap = queryStoreOrderStats(storeId, startDate, endDate);

        // --- 取得店家清單 ---
        String storeSql = storeId != null
                ? "SELECT id, store_name FROM store WHERE id = ?"
                : "SELECT id, store_name FROM store";
        List<Map<String, Object>> storeRows = storeId != null
                ? jdbcTemplate.queryForList(storeSql, storeId)
                : jdbcTemplate.queryForList(storeSql);

        // --- 組裝 StoreItem ---
        List<StorePerformanceReportRes.StoreItem> storeItems = storeRows.stream().map(row -> {
            String sid   = (String) row.get("id");
            String sname = (String) row.get("store_name");

            Integer dc = drawCounts.getOrDefault(sid, 0);
            Long    rev = revenues.getOrDefault(sid, 0L);
            Integer au  = activeUsersMap.getOrDefault(sid, 0);
            OrderStats os = orderStatsMap.get(sid);

            Double shipRate    = null;
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
                    .avgShipDays(null) // TODO(029): replace with AVG(DATEDIFF(shipped_at, preparing_at)) after 029 merges
                    .build();
        }).collect(Collectors.toCollection(ArrayList::new));

        // --- 在記憶體中排序 ---
        Comparator<StorePerformanceReportRes.StoreItem> cmp = buildStoreComparator(sortBy);
        storeItems.sort("ASC".equalsIgnoreCase(sortOrder) ? cmp : cmp.reversed());

        // --- dailyStats：僅帶入 storeId 時回傳 ---
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

    /** drawCount：lottery_ticket status=DRAWN，透過 lottery.store_id */
    private Map<String, Integer> queryStoreDrawCount(String storeId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end   = endDate.plusDays(1).atStartOfDay();

        List<Object> params = new ArrayList<>(List.of(start, end));
        String sql = """
                SELECT l.store_id, COUNT(*) AS draw_count
                FROM lottery_ticket lt
                JOIN lottery l ON lt.lottery_id = l.id
                WHERE lt.status = 'DRAWN'
                AND lt.drawn_at BETWEEN ? AND ?
                """ + (storeId != null ? "AND l.store_id = ? " : "") + """
                GROUP BY l.store_id
                """;
        if (storeId != null) params.add(storeId);

        Map<String, Integer> result = new HashMap<>();
        jdbcTemplate.query(sql, params.toArray(), (RowCallbackHandler) rs ->
                result.put(rs.getString("store_id"), rs.getInt("draw_count")));
        return result;
    }

    /** totalRevenue：ABS(SUM(wallet_transaction.amount)) WHERE transaction_type='DRAW' */
    private Map<String, Long> queryStoreTotalRevenue(String storeId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end   = endDate.plusDays(1).atStartOfDay();

        List<Object> params = new ArrayList<>(List.of(start, end));
        String sql = """
                SELECT l.store_id, ABS(SUM(wt.amount)) AS total_revenue
                FROM wallet_transaction wt
                JOIN lottery_ticket lt ON wt.related_id = lt.id
                JOIN lottery l ON lt.lottery_id = l.id
                WHERE wt.transaction_type = 'DRAW'
                AND wt.created_at BETWEEN ? AND ?
                """ + (storeId != null ? "AND l.store_id = ? " : "") + """
                GROUP BY l.store_id
                """;
        if (storeId != null) params.add(storeId);

        Map<String, Long> result = new HashMap<>();
        jdbcTemplate.query(sql, params.toArray(), (RowCallbackHandler) rs ->
                result.put(rs.getString("store_id"), rs.getLong("total_revenue")));
        return result;
    }

    /** activeUsers：抽獎(drawn_by) UNION 訂單(user_id) 的不重複用戶數，按 store_id 分組 */
    private Map<String, Integer> queryStoreActiveUsers(String storeId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end   = endDate.plusDays(1).atStartOfDay();

        String sql;
        Object[] params;

        if (storeId != null) {
            sql = """
                    SELECT t.store_id, COUNT(DISTINCT uid) AS active_users FROM (
                      SELECT l.store_id, lt.drawn_by AS uid
                      FROM lottery_ticket lt
                      JOIN lottery l ON lt.lottery_id = l.id
                      WHERE lt.drawn_at BETWEEN ? AND ? AND lt.drawn_by IS NOT NULL
                      UNION
                      SELECT o.store_id, o.user_id AS uid
                      FROM `order` o
                      WHERE o.created_at BETWEEN ? AND ?
                    ) t WHERE t.store_id = ?
                    GROUP BY t.store_id
                    """;
            params = new Object[]{start, end, start, end, storeId};
        } else {
            sql = """
                    SELECT t.store_id, COUNT(DISTINCT uid) AS active_users FROM (
                      SELECT l.store_id, lt.drawn_by AS uid
                      FROM lottery_ticket lt
                      JOIN lottery l ON lt.lottery_id = l.id
                      WHERE lt.drawn_at BETWEEN ? AND ? AND lt.drawn_by IS NOT NULL
                      UNION
                      SELECT o.store_id, o.user_id AS uid
                      FROM `order` o
                      WHERE o.created_at BETWEEN ? AND ?
                    ) t GROUP BY t.store_id
                    """;
            params = new Object[]{start, end, start, end};
        }

        Map<String, Integer> result = new HashMap<>();
        jdbcTemplate.query(sql, params, (RowCallbackHandler) rs ->
                result.put(rs.getString("store_id"), rs.getInt("active_users")));
        return result;
    }

    /** 訂單出貨率 / 逾期率原始資料 */
    private static class OrderStats {
        long shipped;
        long nonCancelled;
        long overdue;
        long total;
    }

    /** shipRate / overdueRate：單次 SQL 取所有需要的 order 統計 */
    private Map<String, OrderStats> queryStoreOrderStats(String storeId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end   = endDate.plusDays(1).atStartOfDay();

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
            os.shipped       = rs.getLong("shipped");
            os.nonCancelled  = rs.getLong("non_cancelled");
            os.overdue       = rs.getLong("overdue");
            os.total         = rs.getLong("total");
            result.put(rs.getString("store_id"), os);
        });
        return result;
    }

    /** dailyStats：每日 drawCount / revenue（from wallet_transaction） / newUsers */
    private List<StorePerformanceReportRes.DailyStat> queryStoreDailyStats(
            String storeId, LocalDate startDate, LocalDate endDate) {

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end   = endDate.plusDays(1).atStartOfDay();

        // Step 1: 每日 drawCount + revenue
        String dailySql = """
                SELECT DATE(lt.drawn_at) AS stat_date,
                       COUNT(*) AS draw_count,
                       ABS(COALESCE(SUM(wt.amount), 0)) AS revenue
                FROM lottery_ticket lt
                JOIN lottery l ON lt.lottery_id = l.id
                LEFT JOIN wallet_transaction wt ON wt.related_id = lt.id AND wt.transaction_type = 'DRAW'
                WHERE l.store_id = ? AND lt.drawn_at BETWEEN ? AND ? AND lt.status = 'DRAWN'
                GROUP BY DATE(lt.drawn_at)
                ORDER BY stat_date
                """;

        // 使用 LinkedHashMap 保持日期排序
        Map<LocalDate, StorePerformanceReportRes.DailyStat.DailyStatBuilder> builderMap = new LinkedHashMap<>();
        jdbcTemplate.query(dailySql, new Object[]{storeId, start, end}, (RowCallbackHandler) rs -> {
            LocalDate date = rs.getDate("stat_date").toLocalDate();
            builderMap.put(date, StorePerformanceReportRes.DailyStat.builder()
                    .date(date)
                    .drawCount(rs.getInt("draw_count"))
                    .revenue(rs.getLong("revenue"))
                    .newUsers(0));
        });

        // Step 2: 每日 newUsers（首次在本店有活動的用戶數）
        String newUsersSql = """
                SELECT DATE(first_date) AS stat_date, COUNT(*) AS new_users FROM (
                  SELECT uid, MIN(activity_date) AS first_date FROM (
                    SELECT lt.drawn_by AS uid, DATE(lt.drawn_at) AS activity_date
                    FROM lottery_ticket lt
                    JOIN lottery l ON lt.lottery_id = l.id
                    WHERE l.store_id = ? AND lt.drawn_at BETWEEN ? AND ? AND lt.drawn_by IS NOT NULL
                    UNION ALL
                    SELECT o.user_id AS uid, DATE(o.created_at) AS activity_date
                    FROM `order` o
                    WHERE o.store_id = ? AND o.created_at BETWEEN ? AND ?
                  ) all_activity GROUP BY uid
                ) first_seen GROUP BY DATE(first_date)
                """;

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

    /** 排序 Comparator 工廠（null-safe，預設 totalRevenue） */
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
            default -> Comparator.comparingLong( // totalRevenue (default)
                    (StorePerformanceReportRes.StoreItem i) -> i.getTotalRevenue() != null ? i.getTotalRevenue() : 0L);
        };
    }
}
