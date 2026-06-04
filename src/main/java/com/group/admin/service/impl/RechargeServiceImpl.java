package com.group.admin.service.impl;

import com.group.admin.entity.BusinessEventLog;
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
import com.group.admin.res.wallet.RechargeOrderRes;
import com.group.admin.service.BusinessEventLogService;
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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RechargeServiceImpl implements RechargeService {

    private static final String PAYMENT_GATEWAY_GOMYPAY = "gomypay";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_FAILED = "FAILED";

    private final RechargeRecordMapper rechargeRecordMapper;
    private final RechargeOrderMapper rechargeOrderMapper;
    private final RechargePlanMapper rechargePlanMapper;
    private final UserMapper userMapper;
    private final CoinService coinService;
    private final PaymentGatewayClient paymentGatewayClient;
    private final RechargeStateHelper rechargeStateHelper;
    private final BusinessEventLogService businessEventLogService;

    @Value("${wallet.recharge-order.ttl-minutes:30}")
    private long rechargeOrderTtlMinutes;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RechargeOrderRes createRechargeOrder(String userId, String planId, String paymentMethod) {
        log.info("[Recharge] 建立儲值訂單 userId={}, planId={}, paymentMethod={}", userId, planId, paymentMethod);

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
                .paymentMethod(normalizedPaymentMethod)
                .buyerName(user.getNickname() != null && !user.getNickname().isBlank() ? user.getNickname() : "KUJI會員")
                .buyerEmail(user.getEmail())
                .buyerPhone(user.getPhoneNumber())
                .expiredAt(now.plusMinutes(rechargeOrderTtlMinutes))
                .createdAt(now)
                .updatedAt(now)
                .build();

        // REQUIRES_NEW：立即提交，確保即使後續 gateway 呼叫失敗，記錄仍可查詢
        rechargeStateHelper.saveInitialPending(order, buildPendingRecord(order, plan, normalizedPaymentMethod, now));
        recordRechargeEvent("RECHARGE_CREATE", BusinessEventLogService.RESULT_PENDING, order,
                null, RechargeOrderStatus.PENDING.name(), null, null);

        GatewayInitResult initResult;
        try {
            initResult = paymentGatewayClient.charge(order, normalizedPaymentMethod);
        } catch (RuntimeException e) {
            // REQUIRES_NEW：外層事務已不含初始 insert，故此 markFailed 可直接找到記錄並更新
            rechargeStateHelper.markFailed(order.getId(), null, null, e.getMessage());
            recordRechargeEvent("RECHARGE_GATEWAY_INIT_FAILED", BusinessEventLogService.RESULT_FAILED, order,
                    RechargeOrderStatus.PENDING.name(), RechargeOrderStatus.FAILED.name(), null, e.getMessage());
            throw e;
        }

        applyGatewayInit(order, initResult);
        syncPendingRecordPaymentInfo(order);
        // REQUIRES_NEW：更新 gateway 初始化結果
        rechargeStateHelper.updateGatewayInit(order);

        if (!initResult.success()) {
            rechargeStateHelper.markFailed(order.getId(),
                    order.getGatewayOrderId(), order.getGatewayRawResp(), initResult.errorMessage());
            order.setStatus(RechargeOrderStatus.FAILED);
            recordRechargeEvent("RECHARGE_GATEWAY_INIT_FAILED", BusinessEventLogService.RESULT_FAILED, order,
                    RechargeOrderStatus.PENDING.name(), RechargeOrderStatus.FAILED.name(),
                    initResult.gatewayOrderId(), initResult.errorMessage());
        } else {
            recordRechargeEvent("RECHARGE_GATEWAY_INIT_SUCCESS", BusinessEventLogService.RESULT_PENDING, order,
                    RechargeOrderStatus.PENDING.name(), RechargeOrderStatus.PENDING.name(),
                    initResult.gatewayOrderId(), null);
        }

        return toRechargeOrderRes(order, initResult);
    }

    @Override
    @Transactional(readOnly = true)
    public RechargeOrderRes getRechargeOrder(String userId, String rechargeOrderId) {
        RechargeOrder order = rechargeOrderMapper.selectById(rechargeOrderId);
        if (order == null || !userId.equals(order.getUserId())) {
            throw new BusinessException("找不到儲值訂單");
        }
        return toRechargeOrderRes(order, null);
    }

    @Override
    public RechargeRes createRechargeRequest(String userId, RechargeReq req) {
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            throw new BusinessException("使用者不存在");
        }
        RechargePlan plan = requireValidPlan(req.getPlanId());
        LocalDateTime now = LocalDateTime.now();

        RechargeRecord record = new RechargeRecord();
        record.setId(UUID.randomUUID().toString());
        record.setUserId(userId);
        record.setPlanId(plan.getId());
        record.setAmount(plan.getAmount());
        record.setGoldCoins(plan.getGoldCoins());
        record.setBonusCoins(plan.getBonusCoins());
        record.setPaymentMethod(req.getPaymentMethod());
        record.setPaymentStatus(STATUS_COMPLETED);
        record.setPaymentGateway("manual");
        record.setCreatedAt(now);
        record.setPaidAt(now);
        record.setTransactionId("TEST-" + UUID.randomUUID().toString().substring(0, 8));
        record.setPaymentInfo(req.getRemark());
        rechargeRecordMapper.insert(record);

        creditRechargeCoins(record);
        return RechargeRes.from(record);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<RechargeRes> getUserRechargeHistory(String userId, Integer page, Integer size,
                                                          String paymentStatus, String transactionId,
                                                          String createdAtStart, String createdAtEnd) {
        if (userMapper.selectByPrimaryKey(userId) == null) {
            throw new BusinessException("使用者不存在");
        }

        int currentPage = resolvePage(page);
        int pageSize = resolveSize(size);
        int offset = (currentPage - 1) * pageSize;
        String normalizedStatus = normalizeStatus(paymentStatus);
        String normalizedTransactionId = blankToNull(transactionId);
        String normalizedStart = blankToNull(createdAtStart);
        String normalizedEnd = blankToNull(createdAtEnd);
        long total = rechargeRecordMapper.countByUserIdFiltered(
                userId, normalizedStatus, normalizedTransactionId, normalizedStart, normalizedEnd);
        if (total == 0) {
            return PageResult.empty(currentPage, pageSize);
        }
        List<RechargeRecord> records = rechargeRecordMapper.selectByUserIdPagedFiltered(
                userId, normalizedStatus, normalizedTransactionId, normalizedStart, normalizedEnd, offset, pageSize);
        return PageResult.of(currentPage, pageSize, total, records.stream().map(RechargeRes::from).toList());
    }

    @Override
    public RechargeRes confirmPayment(String rechargeId, String transactionId) {
        RechargeRecord record = rechargeRecordMapper.selectByPrimaryKey(rechargeId);
        if (record == null) {
            throw new BusinessException("儲值紀錄不存在");
        }
        if (!STATUS_PENDING.equals(record.getPaymentStatus())) {
            throw new BusinessException("此儲值紀錄狀態不可確認付款：" + record.getPaymentStatus());
        }

        record.setPaymentStatus(STATUS_COMPLETED);
        record.setPaidAt(LocalDateTime.now());
        if (transactionId != null && !transactionId.isBlank()) {
            record.setTransactionId(transactionId);
        }
        rechargeRecordMapper.updateByPrimaryKeyWithBLOBs(record);
        creditRechargeCoins(record);
        return RechargeRes.from(record);
    }

    @Override
    public RechargeRes recordPaymentFailure(String rechargeId, String failReason) {
        RechargeRecord record = rechargeRecordMapper.selectByPrimaryKey(rechargeId);
        if (record == null) {
            throw new BusinessException("儲值紀錄不存在");
        }
        record.setPaymentStatus(STATUS_FAILED);
        record.setFailReason(failReason);
        rechargeRecordMapper.updateByPrimaryKeyWithBLOBs(record);
        return RechargeRes.from(record);
    }

    @Override
    public void handleCallback(GatewayCallbackResult result) {
        log.info("[Recharge] 收到付款回調 merchantOrderId={}, success={}", result.merchantOrderId(), result.success());
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
                log.info("[Recharge] 儲值訂單已處理，略過重複 callback：{}", order.getId());
                recordRechargeEvent("RECHARGE_CALLBACK_DUPLICATE", BusinessEventLogService.RESULT_DUPLICATE, order,
                        order.getStatus() != null ? order.getStatus().name() : null,
                        order.getStatus() != null ? order.getStatus().name() : null,
                        result.gatewayOrderId(), null);
                return;
            }

            RechargeRecord record = rechargeRecordMapper.selectByPrimaryKey(order.getId());
            if (record != null) {
                record.setPaymentStatus(STATUS_COMPLETED);
                record.setTransactionId(result.gatewayOrderId());
                record.setPaidAt(result.paidAt() != null ? result.paidAt() : LocalDateTime.now());
                record.setPaymentInfo(result.rawPayload());
                rechargeRecordMapper.updateByPrimaryKeyWithBLOBs(record);
                creditRechargeCoins(record);
            }
            recordRechargeEvent("RECHARGE_CALLBACK_SUCCESS", BusinessEventLogService.RESULT_SUCCESS, order,
                    RechargeOrderStatus.PENDING.name(), RechargeOrderStatus.SUCCESS.name(),
                    result.gatewayOrderId(), null);
            return;
        }

        int failedUpdated = rechargeOrderMapper.updateStatusByIdAndExpectStatus(
                order.getId(),
                RechargeOrderStatus.FAILED,
                RechargeOrderStatus.PENDING,
                result.gatewayOrderId(),
                result.rawPayload(),
                null
        );
        RechargeRecord record = rechargeRecordMapper.selectByPrimaryKey(order.getId());
        if (record != null) {
            record.setPaymentStatus(STATUS_FAILED);
            record.setTransactionId(result.gatewayOrderId());
            record.setFailReason("GoMyPay 付款失敗");
            record.setPaymentInfo(result.rawPayload());
            rechargeRecordMapper.updateByPrimaryKeyWithBLOBs(record);
        }
        recordRechargeEvent(
                failedUpdated == 0 ? "RECHARGE_CALLBACK_DUPLICATE" : "RECHARGE_CALLBACK_FAILED",
                failedUpdated == 0 ? BusinessEventLogService.RESULT_DUPLICATE : BusinessEventLogService.RESULT_FAILED,
                order,
                order.getStatus() != null ? order.getStatus().name() : RechargeOrderStatus.PENDING.name(),
                failedUpdated == 0 && order.getStatus() != null ? order.getStatus().name() : RechargeOrderStatus.FAILED.name(),
                result.gatewayOrderId(),
                "GoMyPay 付款失敗");
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
            throw new BusinessException("儲值方案尚未啟用");
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

    private RechargeRecord buildPendingRecord(RechargeOrder order, RechargePlan plan, String paymentMethod, LocalDateTime now) {
        RechargeRecord record = new RechargeRecord();
        record.setId(order.getId());
        record.setUserId(order.getUserId());
        record.setPlanId(plan.getId());
        record.setAmount(plan.getAmount());
        record.setGoldCoins(plan.getGoldCoins());
        record.setBonusCoins(plan.getBonusCoins());
        record.setPaymentMethod(paymentMethod);
        record.setPaymentStatus(STATUS_PENDING);
        record.setPaymentGateway(PAYMENT_GATEWAY_GOMYPAY);
        record.setCreatedAt(now);
        return record;
    }

    private void applyGatewayInit(RechargeOrder order, GatewayInitResult initResult) {
        order.setGatewayProvider(initResult.provider());
        order.setGatewayOrderId(initResult.gatewayOrderId());
        order.setGatewayResult(initResult.gatewayResult());
        order.setGatewayRetMsg(initResult.retMsg());
        order.setVirtualAccount(initResult.virtualAccount());
        order.setPaymentInfo(initResult.payInfo());
        order.setLimitDate(initResult.limitDate());
        order.setGatewayRawResp(initResult.rawPayload());
    }

    private void syncPendingRecordPaymentInfo(RechargeOrder order) {
        if (!hasText(order.getVirtualAccount()) && !hasText(order.getPaymentInfo()) && !hasText(order.getLimitDate())) {
            return;
        }
        RechargeRecord record = rechargeRecordMapper.selectByPrimaryKey(order.getId());
        if (record == null) {
            return;
        }
        record.setTransactionId(order.getGatewayOrderId());
        record.setPaymentInfo(buildTransferPaymentInfo(order));
        rechargeRecordMapper.updateByPrimaryKeyWithBLOBs(record);
    }

    private String buildTransferPaymentInfo(RechargeOrder order) {
        StringBuilder info = new StringBuilder();
        if (hasText(order.getVirtualAccount())) {
            info.append("轉帳帳號：").append(order.getVirtualAccount());
        }
        if (hasText(order.getPaymentInfo())) {
            if (!info.isEmpty()) {
                info.append("；");
            }
            info.append("付款資訊：").append(order.getPaymentInfo());
        }
        if (hasText(order.getLimitDate())) {
            if (!info.isEmpty()) {
                info.append("；");
            }
            info.append("繳費期限：").append(order.getLimitDate());
        }
        return info.toString();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private void creditRechargeCoins(RechargeRecord record) {
        coinService.addGold(record.getUserId(), record.getGoldCoins(), "RECHARGE", record.getId(),
                "儲值方案 " + record.getPlanId());
        if (record.getBonusCoins() != null && record.getBonusCoins() > 0) {
            coinService.addBonus(record.getUserId(), record.getBonusCoins(), "RECHARGE", record.getId(),
                    "儲值贈送紅利 " + record.getPlanId());
        }
    }

    private RechargeOrderRes toRechargeOrderRes(RechargeOrder order, GatewayInitResult initResult) {
        return RechargeOrderRes.builder()
                .rechargeOrderId(order.getId())
                .payUrl(initResult != null ? initResult.payUrl() : null)
                .submitMethod(initResult != null ? initResult.submitMethod() : null)
                .actionUrl(initResult != null ? initResult.actionUrl() : null)
                .formFields(initResult != null ? initResult.formFields() : null)
                .paymentMethod(order.getPaymentMethod())
                .gatewayResult(order.getGatewayResult())
                .retMsg(order.getGatewayRetMsg())
                .virtualAccount(order.getVirtualAccount())
                .payInfo(order.getPaymentInfo())
                .limitDate(order.getLimitDate())
                .goldAmount(order.getGoldAmount())
                .bonusAmount(order.getBonusAmount())
                .priceTwd(order.getPriceTwd())
                .status(order.getStatus() != null ? order.getStatus().name() : null)
                .expiredAt(order.getExpiredAt())
                .build();
    }

    private void recordRechargeEvent(String action, String result, RechargeOrder order,
                                     String beforeStatus, String afterStatus,
                                     String externalRef, String errorMessage) {
        if (order == null) {
            return;
        }
        businessEventLogService.record(BusinessEventLog.builder()
                .eventType(BusinessEventLogService.EVENT_PAYMENT)
                .action(action)
                .result(result)
                .actorType("USER")
                .actorId(order.getUserId())
                .targetType("RECHARGE")
                .targetId(order.getId())
                .targetNo(order.getId())
                .userId(order.getUserId())
                .rechargeId(order.getId())
                .externalProvider(order.getGatewayProvider())
                .externalRef(externalRef != null ? externalRef : order.getGatewayOrderId())
                .amount(order.getPriceTwd() != null ? order.getPriceTwd().longValue() : null)
                .paymentMethod(order.getPaymentMethod())
                .beforeStatus(beforeStatus)
                .afterStatus(afterStatus)
                .errorMessage(errorMessage)
                .build());
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

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalizeStatus(String status) {
        String normalized = blankToNull(status);
        if ("CANCELED".equals(normalized)) {
            return "CANCELLED";
        }
        return normalized;
    }
}
