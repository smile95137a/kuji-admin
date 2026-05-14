package com.group.admin.service.impl;

import com.group.admin.entity.RechargeOrder;
import com.group.admin.entity.RechargePlan;
import com.group.admin.entity.RechargeRecord;
import com.group.admin.entity.User;
import com.group.admin.enums.RechargeOrderStatus;
import com.group.admin.exception.BusinessException;
import com.group.admin.gateway.GatewayCallbackResult;
import com.group.admin.gateway.GatewayInitResult;
import com.group.admin.gateway.GoMyPaySupport;
import com.group.admin.gateway.PaymentGatewayClient;
import com.group.admin.mapper.RechargeOrderMapper;
import com.group.admin.mapper.RechargePlanMapper;
import com.group.admin.mapper.RechargeRecordMapper;
import com.group.admin.mapper.UserMapper;
import com.group.admin.req.recharge.RechargeReq;
import com.group.admin.res.PageResult;
import com.group.admin.res.recharge.RechargeRes;
import com.group.admin.service.CoinService;
import com.group.admin.service.RechargeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 前台使用者儲值服務實現
 * 
 * @author Kuji Admin
 * @since 2026-02-08
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RechargeServiceImpl implements RechargeService {
    
    private final RechargeRecordMapper rechargeRecordMapper;
    private final RechargeOrderMapper rechargeOrderMapper;
    private final RechargePlanMapper rechargePlanMapper;
    private final UserMapper userMapper;
    private final CoinService coinService;
    private final PaymentGatewayClient paymentGatewayClient;

    @Value("${wallet.recharge-order.ttl-minutes:30}")
    private long rechargeOrderTtlMinutes;

    @Override
    public com.group.admin.res.wallet.RechargeOrderRes createRechargeOrder(String userId, String planId, String paymentMethod) {
        log.info("💳 [Recharge] 建立儲值訂單：userId={}, planId={}, paymentMethod={}", userId, planId, paymentMethod);

        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            throw new BusinessException("使用者不存在");
        }

        RechargePlan plan = requireValidPlan(planId);
        String normalizedPaymentMethod = GoMyPaySupport.normalizePaymentMethod(paymentMethod);
        LocalDateTime now = LocalDateTime.now();

        RechargeOrder order = RechargeOrder.builder()
                .id(generateRechargeOrderId())
                .userId(userId)
                .planId(plan.getId())
                .goldAmount(plan.getGoldCoins())
                .bonusAmount(plan.getBonusCoins())
                .priceTwd(BigDecimal.valueOf(plan.getAmount()))
                .status(RechargeOrderStatus.PENDING)
                .buyerName(user.getNickname() != null && !user.getNickname().isBlank() ? user.getNickname() : "KUJI會員")
                .buyerEmail(user.getEmail())
                .buyerPhone(user.getPhoneNumber())
                .expiredAt(now.plusMinutes(rechargeOrderTtlMinutes))
                .createdAt(now)
                .updatedAt(now)
                .build();

        GatewayInitResult initResult = paymentGatewayClient.charge(order, normalizedPaymentMethod);
        order.setGatewayProvider(initResult.provider());
        order.setGatewayOrderId(initResult.gatewayOrderId());
        order.setGatewayRawResp(initResult.payUrl());
        rechargeOrderMapper.insert(order);

        return toRechargeOrderRes(order, initResult);
    }

    @Override
    @Transactional(readOnly = true)
    public com.group.admin.res.wallet.RechargeOrderRes getRechargeOrder(String userId, String rechargeOrderId) {
        RechargeOrder order = rechargeOrderMapper.selectById(rechargeOrderId);
        if (order == null || !userId.equals(order.getUserId())) {
            throw new BusinessException("找不到儲值訂單");
        }
        return toRechargeOrderRes(order, order.getGatewayRawResp());
    }
    
    @Override
    public RechargeRes createRechargeRequest(String userId, RechargeReq req) {
        log.info("💳 [Recharge] 建立儲值請求：userId={}, planId={}, paymentMethod={}", 
                userId, req.getPlanId(), req.getPaymentMethod());
        
        // Step 1: 驗證使用者存在
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            throw new BusinessException("使用者不存在");
        }
        
        log.info("✅ [Step 1] 使用者驗證通過：userId={}", userId);
        log.info("   目前金幣：goldCoins={}, bonusCoins={}, totalRecharged={}", 
                user.getGoldCoins(), user.getBonusCoins(), user.getTotalRecharged());
        
        // Step 2: 驗證儲值方案存在且有效
        RechargePlan plan = rechargePlanMapper.selectByPrimaryKey(req.getPlanId());
        if (plan == null) {
            throw new BusinessException("儲值方案不存在");
        }
        if (plan.getIsActive() == null || plan.getIsActive() != 1) {
            throw new BusinessException("儲值方案已禁用");
        }
        LocalDateTime now = LocalDateTime.now();
        if (plan.getStartDate() != null && now.isBefore(plan.getStartDate())) {
            throw new BusinessException("儲值方案尚未開始");
        }
        if (plan.getEndDate() != null && now.isAfter(plan.getEndDate())) {
            throw new BusinessException("儲值方案已結束");
        }
        if (plan.getDeletedAt() != null) {
            throw new BusinessException("儲值方案已刪除");
        }
        
        log.info("✅ [Step 2] 方案驗證通過：planId={}", req.getPlanId());
        log.info("   方案名稱：{}", plan.getName());
        log.info("   金額：{}, 金幣：{}, 紅利：{}", plan.getAmount(), plan.getGoldCoins(), plan.getBonusCoins());
        
        // Step 3: 建立 RechargeRecord（✨ 直接設為 COMPLETED，立即完成支付）
        RechargeRecord record = new RechargeRecord();
        record.setId(UUID.randomUUID().toString());
        record.setUserId(userId);
        record.setPlanId(req.getPlanId());
        record.setAmount(plan.getAmount());
        record.setGoldCoins(plan.getGoldCoins());
        record.setBonusCoins(plan.getBonusCoins());
        record.setPaymentMethod(req.getPaymentMethod());
        record.setPaymentStatus("COMPLETED");  // ✨ 直接完成（測試模式）
        record.setCreatedAt(now);
        record.setPaidAt(now);  // ✨ 立即設定支付時間
        record.setTransactionId("TEST-" + UUID.randomUUID().toString().substring(0, 8));  // ✨ 模擬交易 ID
        record.setPaymentInfo(req.getRemark());
        
        rechargeRecordMapper.insert(record);
        
        log.info("✅ [Step 3] 儲值記錄已建立：rechargeId={}, status=COMPLETED", record.getId());
        
        // Step 4: 透過 CoinService 更新使用者金幣（統一記錄交易流水）
        log.info("💰 [Step 4] 透過 CoinService 更新金幣...");
        
        coinService.addGold(userId, record.getGoldCoins(), "RECHARGE", record.getId(),
                "儲值：" + plan.getName());
        
        if (record.getBonusCoins() != null && record.getBonusCoins() > 0) {
            coinService.addBonus(userId, record.getBonusCoins(), "RECHARGE", record.getId(),
                    "儲值紅利：" + plan.getName());
        }
        
        // 更新累計儲值金額（非金幣欄位，直接更新 user 表）
        User updatedUser = userMapper.selectByPrimaryKey(userId);
        if (updatedUser != null) {
            Long totalBefore = updatedUser.getTotalRecharged() != null ? updatedUser.getTotalRecharged() : 0L;
            updatedUser.setTotalRecharged(totalBefore + record.getAmount());
            updatedUser.setUpdatedAt(now);
            userMapper.updateByPrimaryKeySelective(updatedUser);
        }
        
        log.info("✅ [Step 4] 金幣更新完成！goldCoins={}, bonusCoins={}",
                record.getGoldCoins(), record.getBonusCoins());
        
        // 重新查詢驗證
        User verifyUser = userMapper.selectByPrimaryKey(userId);
        log.info("🔍 [驗證] 重新查詢使用者金幣：goldCoins={}, bonusCoins={}, totalRecharged={}",
                verifyUser.getGoldCoins(), verifyUser.getBonusCoins(), verifyUser.getTotalRecharged());
        
        log.info("🎉 儲值完成！rechargeId={}, goldCoins={}, bonusCoins={}", 
                record.getId(), record.getGoldCoins(), record.getBonusCoins());
        
        return RechargeRes.from(record);
    }
    
    @Override
    @Transactional(readOnly = true)
    public PageResult<RechargeRes> getUserRechargeHistory(String userId, Integer page, Integer size) {
        log.info("🔍 [Recharge] 查詢儲值記錄：userId={}, page={}, size={}", userId, page, size);
        
        // Step 1: 驗證使用者存在
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            throw new BusinessException("使用者不存在");
        }
        
        int currentPage = resolvePage(page);
        int pageSize = resolveSize(size);
        int offset = (currentPage - 1) * pageSize;

        long total = rechargeRecordMapper.countByUserId(userId);
        if (total == 0) {
            return PageResult.empty(currentPage, pageSize);
        }

        List<RechargeRecord> records = rechargeRecordMapper.selectByUserIdPaged(userId, offset, pageSize);
        return PageResult.of(currentPage, pageSize, total, records.stream()
                .map(RechargeRes::from)
                .toList());
    }
    
    @Override
    public RechargeRes confirmPayment(String rechargeId, String transactionId) {
        log.info("💰 [Recharge] 確認支付：rechargeId={}, transactionId={}", rechargeId, transactionId);
        
        // Step 1: 查詢儲值記錄
        RechargeRecord record = rechargeRecordMapper.selectByPrimaryKey(rechargeId);
        if (record == null) {
            throw new BusinessException("儲值記錄不存在");
        }
        
        // Step 2: 驗證狀態為 PENDING
        if (!"PENDING".equals(record.getPaymentStatus())) {
            throw new BusinessException("只有待支付的儲值才能確認支付，當前狀態: " + record.getPaymentStatus());
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        // Step 3: 更新記錄狀態為 COMPLETED
        record.setPaymentStatus("COMPLETED");
        record.setPaidAt(now);
        if (transactionId != null) {
            record.setTransactionId(transactionId);
        }
        rechargeRecordMapper.updateByPrimaryKey(record);
        
        log.info("✅ 儲值記錄已標記為已支付：rechargeId={}", rechargeId);
        
        // Step 4: 查詢使用者並更新金幣（樂觀鎖）
        User user = userMapper.selectByPrimaryKey(record.getUserId());
        if (user == null) {
            throw new BusinessException("使用者不存在");
        }
        
        // 透過 CoinService 更新使用者金幣（統一記錄交易流水）
        coinService.addGold(record.getUserId(), record.getGoldCoins(), "RECHARGE", rechargeId,
                "儲值：" + record.getPlanId());
        
        if (record.getBonusCoins() != null && record.getBonusCoins() > 0) {
            coinService.addBonus(record.getUserId(), record.getBonusCoins(), "RECHARGE", rechargeId,
                    "儲值紅利：" + record.getPlanId());
        }
        
        // 更新累計儲值金額（非金幣欄位）
        Long totalBefore = user.getTotalRecharged() != null ? user.getTotalRecharged() : 0L;
        user.setTotalRecharged(totalBefore + record.getAmount());
        user.setUpdatedAt(now);
        userMapper.updateByPrimaryKeySelective(user);
        
        log.info("✅ 使用者金幣已更新：userId={}, +goldCoins={}, +bonusCoins={}", 
                record.getUserId(), record.getGoldCoins(), record.getBonusCoins());
        
        log.info("✅ 交易記錄已建立：userId={}", record.getUserId());
        
        return RechargeRes.from(record);
    }
    
    @Override
    public RechargeRes recordPaymentFailure(String rechargeId, String failReason) {
        log.warn("❌ [Recharge] 記錄支付失敗：rechargeId={}, reason={}", rechargeId, failReason);
        
        // Step 1: 查詢儲值記錄
        RechargeRecord record = rechargeRecordMapper.selectByPrimaryKey(rechargeId);
        if (record == null) {
            throw new BusinessException("儲值記錄不存在");
        }
        
        // Step 2: 更新狀態為 FAILED
        record.setPaymentStatus("FAILED");
        record.setFailReason(failReason);
        rechargeRecordMapper.updateByPrimaryKey(record);
        
        log.info("✅ 支付失敗已記錄：rechargeId={}", rechargeId);
        
        return RechargeRes.from(record);
    }

    @Override
    public void handleCallback(GatewayCallbackResult result) {
        log.info("📥 [Recharge] 支付閘道回調：merchantOrderId={}, success={}", result.merchantOrderId(), result.success());
        RechargeOrder order = rechargeOrderMapper.selectById(result.merchantOrderId());
        if (order == null) {
            throw new BusinessException("找不到儲值訂單：" + result.merchantOrderId());
        }

        if (result.success()) {
            int updated = rechargeOrderMapper.updateStatusByIdAndExpectStatus(
                    order.getId(),
                    RechargeOrderStatus.SUCCESS,
                    RechargeOrderStatus.PENDING,
                    result.gatewayOrderId(),
                    result.rawPayload(),
                    result.paidAt() != null ? result.paidAt() : LocalDateTime.now()
            );
            if (updated == 0) {
                log.info("ℹ️ [Recharge] 儲值訂單已處理過：{}", order.getId());
                return;
            }

            coinService.addGold(order.getUserId(), order.getGoldAmount(), "RECHARGE", order.getId(),
                    "儲值訂單：" + order.getPlanId());
            if (order.getBonusAmount() != null && order.getBonusAmount() > 0) {
                coinService.addBonus(order.getUserId(), order.getBonusAmount(), "RECHARGE", order.getId(),
                        "儲值紅利：" + order.getPlanId());
            }

            User user = userMapper.selectByPrimaryKey(order.getUserId());
            if (user != null) {
                Long totalBefore = user.getTotalRecharged() != null ? user.getTotalRecharged() : 0L;
                user.setTotalRecharged(totalBefore + order.getPriceTwd().longValue());
                user.setUpdatedAt(LocalDateTime.now());
                userMapper.updateByPrimaryKeySelective(user);
            }
            return;
        }

        rechargeOrderMapper.updateStatusByIdAndExpectStatus(
                order.getId(),
                RechargeOrderStatus.FAILED,
                RechargeOrderStatus.PENDING,
                result.gatewayOrderId(),
                result.rawPayload(),
                null
        );
        log.warn("⚠️ 支付閘道回報失敗：merchantOrderId={}", result.merchantOrderId());
    }

    @Override
    public GatewayCallbackResult verifyGatewayCallback(Map<String, String> params) {
        return paymentGatewayClient.verifyCallback(params);
    }

    private RechargePlan requireValidPlan(String planId) {
        RechargePlan plan = rechargePlanMapper.selectByPrimaryKey(planId);
        if (plan == null) {
            throw new BusinessException("儲值方案不存在");
        }
        if (plan.getIsActive() == null || plan.getIsActive() != 1) {
            throw new BusinessException("儲值方案已禁用");
        }
        LocalDateTime now = LocalDateTime.now();
        if (plan.getStartDate() != null && now.isBefore(plan.getStartDate())) {
            throw new BusinessException("儲值方案尚未開始");
        }
        if (plan.getEndDate() != null && now.isAfter(plan.getEndDate())) {
            throw new BusinessException("儲值方案已結束");
        }
        if (plan.getDeletedAt() != null) {
            throw new BusinessException("儲值方案已刪除");
        }
        return plan;
    }

    private com.group.admin.res.wallet.RechargeOrderRes toRechargeOrderRes(RechargeOrder order, GatewayInitResult initResult) {
        return com.group.admin.res.wallet.RechargeOrderRes.builder()
                .rechargeOrderId(order.getId())
                .payUrl(initResult.payUrl())
                .submitMethod(initResult.submitMethod())
                .actionUrl(initResult.actionUrl())
                .formFields(initResult.formFields())
                .goldAmount(order.getGoldAmount())
                .bonusAmount(order.getBonusAmount())
                .priceTwd(order.getPriceTwd())
                .status(order.getStatus() != null ? order.getStatus().name() : null)
                .expiredAt(order.getExpiredAt())
                .build();
    }

    private com.group.admin.res.wallet.RechargeOrderRes toRechargeOrderRes(RechargeOrder order, String payUrl) {
        return com.group.admin.res.wallet.RechargeOrderRes.builder()
                .rechargeOrderId(order.getId())
                .payUrl(payUrl)
                .goldAmount(order.getGoldAmount())
                .bonusAmount(order.getBonusAmount())
                .priceTwd(order.getPriceTwd())
                .status(order.getStatus() != null ? order.getStatus().name() : null)
                .expiredAt(order.getExpiredAt())
                .build();
    }

    private String generateRechargeOrderId() {
        return "RC" + DateTimeFormatter.ofPattern("yyMMddHHmmss").format(LocalDateTime.now())
                + UUID.randomUUID().toString().replace("-", "").substring(0, 9).toUpperCase();
    }

    private int resolvePage(Integer page) {
        return page != null && page > 0 ? page : 1;
    }

    private int resolveSize(Integer size) {
        if (size == null || size < 1) {
            return 10;
        }
        return Math.min(size, 100);
    }
}
