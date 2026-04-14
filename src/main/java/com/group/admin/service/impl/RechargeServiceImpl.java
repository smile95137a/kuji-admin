package com.group.admin.service.impl;

import com.group.admin.entity.RechargeRecord;
import com.group.admin.entity.RechargePlan;
import com.group.admin.entity.RechargeOrder;
import com.group.admin.entity.User;
import com.group.admin.entity.WalletTransaction;
import com.group.admin.enums.RechargeOrderStatus;
import com.group.admin.exception.BusinessException;
import com.group.admin.gateway.GatewayCallbackResult;
import com.group.admin.gateway.GatewayInitResult;
import com.group.admin.gateway.PaymentGatewayClient;
import com.group.admin.mapper.RechargeRecordMapper;
import com.group.admin.mapper.RechargePlanMapper;
import com.group.admin.mapper.RechargeOrderMapper;
import com.group.admin.mapper.UserMapper;
import com.group.admin.mapper.WalletTransactionMapper;
import com.group.admin.req.recharge.RechargeReq;
import com.group.admin.res.recharge.RechargeRes;
import com.group.admin.res.wallet.RechargeOrderRes;
import com.group.admin.service.RechargeService;
import com.group.admin.config.WalletProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private final RechargePlanMapper rechargePlanMapper;
    private final UserMapper userMapper;
    private final WalletTransactionMapper walletTransactionMapper;
    private final RechargeOrderMapper rechargeOrderMapper;
    private final PaymentGatewayClient paymentGatewayClient;
    private final WalletProperties walletProperties;
    
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
        
        // Step 4: 立即更新使用者金幣（✨ 新增邏輯）
        Long goldBefore = user.getGoldCoins() != null ? user.getGoldCoins() : 0L;
        Long bonusBefore = user.getBonusCoins() != null ? user.getBonusCoins() : 0L;
        Long totalBefore = user.getTotalRecharged() != null ? user.getTotalRecharged() : 0L;
        
        Long goldAfter = goldBefore + record.getGoldCoins();
        Long bonusAfter = bonusBefore + record.getBonusCoins();
        Long totalAfter = totalBefore + record.getAmount();
        
        user.setGoldCoins(goldAfter);
        user.setBonusCoins(bonusAfter);
        user.setTotalRecharged(totalAfter);
        user.setUpdatedAt(now);
        
        log.info("💰 [Step 4] 準備更新使用者金幣...");
        log.info("   金幣：{} + {} = {}", goldBefore, record.getGoldCoins(), goldAfter);
        log.info("   紅利：{} + {} = {}", bonusBefore, record.getBonusCoins(), bonusAfter);
        log.info("   累計儲值：{} + {} = {}", totalBefore, record.getAmount(), totalAfter);
        
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        
        log.info("✅ [Step 4] 使用者金幣更新完成！updateCount={}", updateCount);
        
        if (updateCount == 0) {
            log.error("❌ 更新使用者金幣失敗！updateCount=0");
            throw new BusinessException("更新使用者金幣失敗，請重試");
        }
        
        // 重新查詢驗證
        User updatedUser = userMapper.selectByPrimaryKey(userId);
        log.info("� [驗證] 重新查詢使用者金幣：");
        log.info("   goldCoins={} (預期: {})", updatedUser.getGoldCoins(), goldAfter);
        log.info("   bonusCoins={} (預期: {})", updatedUser.getBonusCoins(), bonusAfter);
        log.info("   totalRecharged={} (預期: {})", updatedUser.getTotalRecharged(), totalAfter);
        
        // Step 5: 建立 WalletTransaction 記錄（✨ 新增邏輯）
        if (record.getGoldCoins() != null && record.getGoldCoins() > 0) {
            WalletTransaction transaction = new WalletTransaction();
            transaction.setId(UUID.randomUUID().toString());
            transaction.setUserId(userId);
            transaction.setTransactionType("RECHARGE");
            transaction.setCoinType("GOLD");
            transaction.setAmount(record.getGoldCoins());
            transaction.setBalanceAfter(goldAfter);
            transaction.setDescription("儲值：" + plan.getName());
            transaction.setRelatedId(record.getId());
            transaction.setCreatedAt(now);
            walletTransactionMapper.insert(transaction);
            
            log.info("✅ [Step 5] 金幣交易記錄已建立：amount={}, balanceAfter={}", 
                    record.getGoldCoins(), goldAfter);
        }
        
        if (record.getBonusCoins() != null && record.getBonusCoins() > 0) {
            WalletTransaction bonusTransaction = new WalletTransaction();
            bonusTransaction.setId(UUID.randomUUID().toString());
            bonusTransaction.setUserId(userId);
            bonusTransaction.setTransactionType("RECHARGE");
            bonusTransaction.setCoinType("BONUS");
            bonusTransaction.setAmount(record.getBonusCoins());
            bonusTransaction.setBalanceAfter(bonusAfter);
            bonusTransaction.setDescription("儲值紅利：" + plan.getName());
            bonusTransaction.setRelatedId(record.getId());
            bonusTransaction.setCreatedAt(now);
            walletTransactionMapper.insert(bonusTransaction);
            
            log.info("✅ [Step 5] 紅利交易記錄已建立：amount={}, balanceAfter={}", 
                    record.getBonusCoins(), bonusAfter);
        }
        
        log.info("🎉 儲值完成！rechargeId={}, goldCoins={}, bonusCoins={}", 
                record.getId(), record.getGoldCoins(), record.getBonusCoins());
        
        return RechargeRes.from(record);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<RechargeRes> getUserRechargeHistory(String userId, Integer page, Integer size) {
        log.info("🔍 [Recharge] 查詢儲值記錄：userId={}, page={}, size={}", userId, page, size);
        
        // Step 1: 驗證使用者存在
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            throw new BusinessException("使用者不存在");
        }
        
        // Step 2: 使用 Example 查詢儲值記錄（按建立時間降序）
        com.group.admin.example.RechargeRecordExample example = new com.group.admin.example.RechargeRecordExample();
        example.createCriteria().andUserIdEqualTo(userId);
        example.setOrderByClause("created_at DESC");
        
        List<RechargeRecord> records = rechargeRecordMapper.selectByExample(example);
        
        // Step 3: 簡單分頁（前端也可以做分頁）
        int start = (page - 1) * size;
        int end = Math.min(start + size, records.size());
        if (start >= records.size()) {
            return List.of();
        }
        
        return records.subList(start, end).stream()
                .map(RechargeRes::from)
                .collect(Collectors.toList());
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
        
        Long goldBefore = user.getGoldCoins() != null ? user.getGoldCoins() : 0L;
        Long bonusBefore = user.getBonusCoins() != null ? user.getBonusCoins() : 0L;
        
        user.setGoldCoins(goldBefore + record.getGoldCoins());
        user.setBonusCoins(bonusBefore + record.getBonusCoins());
        user.setTotalRecharged((user.getTotalRecharged() != null ? user.getTotalRecharged() : 0L) + record.getAmount());
        user.setUpdatedAt(now);
        
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if (updateCount == 0) {
            throw new BusinessException("更新使用者金幣失敗（並發沖突），請重試");
        }
        
        log.info("✅ 使用者金幣已更新：userId={}, +goldCoins={}, +bonusCoins={}", 
                record.getUserId(), record.getGoldCoins(), record.getBonusCoins());
        
        // Step 5: 建立 WalletTransaction 記錄（審計用）
        WalletTransaction transaction = new WalletTransaction();
        transaction.setId(UUID.randomUUID().toString());
        transaction.setUserId(record.getUserId());
        transaction.setTransactionType("RECHARGE");
        transaction.setCoinType("GOLD");  // 主要是金幣
        transaction.setAmount(record.getGoldCoins());
        transaction.setBalanceAfter(user.getGoldCoins());
        transaction.setDescription("儲值：" + record.getPlanId());
        transaction.setRelatedId(rechargeId);
        transaction.setCreatedAt(now);
        walletTransactionMapper.insert(transaction);
        
        // 如果有紅利也記錄一筆
        if (record.getBonusCoins() != null && record.getBonusCoins() > 0) {
            WalletTransaction bonusTransaction = new WalletTransaction();
            bonusTransaction.setId(UUID.randomUUID().toString());
            bonusTransaction.setUserId(record.getUserId());
            bonusTransaction.setTransactionType("RECHARGE");
            bonusTransaction.setCoinType("BONUS");
            bonusTransaction.setAmount(record.getBonusCoins());
            bonusTransaction.setBalanceAfter(user.getBonusCoins());
            bonusTransaction.setDescription("儲值紅利：" + record.getPlanId());
            bonusTransaction.setRelatedId(rechargeId);
            bonusTransaction.setCreatedAt(now);
            walletTransactionMapper.insert(bonusTransaction);
        }
        
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
    @Transactional
    public RechargeOrderRes createRechargeOrder(String userId, String planId) {
        log.info("💳 [RechargeOrder] 建立儲值訂單：userId={}, planId={}", userId, planId);

        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) throw new BusinessException("使用者不存在");

        RechargePlan plan = rechargePlanMapper.selectByPrimaryKey(planId);
        if (plan == null) throw new BusinessException("儲值方案不存在");
        if (plan.getIsActive() == null || plan.getIsActive() != 1) throw new BusinessException("儲值方案已禁用");
        LocalDateTime now = LocalDateTime.now();
        if (plan.getStartDate() != null && now.isBefore(plan.getStartDate())) throw new BusinessException("儲值方案尚未開始");
        if (plan.getEndDate() != null && now.isAfter(plan.getEndDate())) throw new BusinessException("儲值方案已結束");
        if (plan.getDeletedAt() != null) throw new BusinessException("儲值方案已刪除");

        RechargeOrder order = RechargeOrder.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .planId(planId)
                .goldAmount(plan.getGoldCoins())
                .bonusAmount(plan.getBonusCoins() != null ? plan.getBonusCoins() : 0L)
                .priceTwd(java.math.BigDecimal.valueOf(plan.getAmount()))
                .status(RechargeOrderStatus.PENDING)
                .expiredAt(now.plusMinutes(walletProperties.getRechargeOrder().getTtlMinutes()))
                .createdAt(now)
                .updatedAt(now)
                .build();

        rechargeOrderMapper.insert(order);

        GatewayInitResult gatewayResult = paymentGatewayClient.charge(order);

        log.info("✅ [RechargeOrder] 訂單建立成功：orderId={}, payUrl={}", order.getId(), gatewayResult.payUrl());

        return RechargeOrderRes.builder()
                .rechargeOrderId(order.getId())
                .payUrl(gatewayResult.payUrl())
                .goldAmount(order.getGoldAmount())
                .bonusAmount(order.getBonusAmount())
                .priceTwd(order.getPriceTwd())
                .expiredAt(order.getExpiredAt())
                .build();
    }

    @Override
    @Transactional
    public void handleCallback(GatewayCallbackResult result) {
        log.info("📞 [Callback] merchantOrderId={}, success={}", result.merchantOrderId(), result.success());

        RechargeOrder order = rechargeOrderMapper.selectById(result.merchantOrderId());
        if (order == null) {
            log.warn("⚠️ [Callback] 找不到訂單：{}", result.merchantOrderId());
            return;
        }

        if (order.getStatus() != RechargeOrderStatus.PENDING) {
            log.warn("⚠️ [Callback] 訂單狀態非 PENDING：{}", order.getStatus());
            return;
        }

        RechargeOrderStatus newStatus = result.success() ? RechargeOrderStatus.SUCCESS : RechargeOrderStatus.FAILED;
        LocalDateTime paidAt = result.success() ? (result.paidAt() != null ? result.paidAt() : LocalDateTime.now()) : null;

        int updated = rechargeOrderMapper.updateStatusByIdAndExpectStatus(
                order.getId(), newStatus, RechargeOrderStatus.PENDING,
                result.gatewayOrderId(), result.rawPayload(), paidAt);

        if (updated == 0) {
            log.warn("⚠️ [Callback] CAS 更新失敗（並發或已處理）：orderId={}", order.getId());
            return;
        }

        if (!result.success()) {
            log.info("❌ [Callback] 支付失敗：orderId={}", order.getId());
            return;
        }

        // Credit coins to user
        User user = userMapper.selectByPrimaryKey(order.getUserId());
        if (user == null) {
            log.error("❌ [Callback] 找不到使用者：{}", order.getUserId());
            return;
        }

        Long goldBefore = user.getGoldCoins() != null ? user.getGoldCoins() : 0L;
        Long bonusBefore = user.getBonusCoins() != null ? user.getBonusCoins() : 0L;
        Long totalBefore = user.getTotalRecharged() != null ? user.getTotalRecharged() : 0L;
        Long goldAfter = goldBefore + order.getGoldAmount();
        Long bonusAfter = bonusBefore + order.getBonusAmount();
        Long totalAfter = totalBefore + order.getPriceTwd().longValue();

        user.setGoldCoins(goldAfter);
        user.setBonusCoins(bonusAfter);
        user.setTotalRecharged(totalAfter);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateByPrimaryKeySelective(user);

        // Audit: gold transaction
        if (order.getGoldAmount() > 0) {
            WalletTransaction tx = new WalletTransaction();
            tx.setId(UUID.randomUUID().toString());
            tx.setUserId(order.getUserId());
            tx.setTransactionType("RECHARGE");
            tx.setCoinType("GOLD");
            tx.setAmount(order.getGoldAmount());
            tx.setBalanceAfter(goldAfter);
            tx.setGoldDelta(order.getGoldAmount());
            tx.setGoldAfter(goldAfter);
            tx.setBonusAfter(bonusAfter);
            tx.setReferenceId(order.getId());
            tx.setReason("儲值：planId=" + order.getPlanId());
            tx.setRelatedId(order.getId());
            tx.setDescription("儲值：planId=" + order.getPlanId());
            tx.setCreatedAt(LocalDateTime.now());
            walletTransactionMapper.insertSelective(tx);
        }

        // Audit: bonus transaction
        if (order.getBonusAmount() != null && order.getBonusAmount() > 0) {
            WalletTransaction bonusTx = new WalletTransaction();
            bonusTx.setId(UUID.randomUUID().toString());
            bonusTx.setUserId(order.getUserId());
            bonusTx.setTransactionType("BONUS_GRANT");
            bonusTx.setCoinType("BONUS");
            bonusTx.setAmount(order.getBonusAmount());
            bonusTx.setBalanceAfter(bonusAfter);
            bonusTx.setBonusDelta(order.getBonusAmount());
            bonusTx.setGoldAfter(goldAfter);
            bonusTx.setBonusAfter(bonusAfter);
            bonusTx.setReferenceId(order.getId());
            bonusTx.setReason("儲值紅利：planId=" + order.getPlanId());
            bonusTx.setRelatedId(order.getId());
            bonusTx.setDescription("儲值紅利：planId=" + order.getPlanId());
            bonusTx.setCreatedAt(LocalDateTime.now());
            walletTransactionMapper.insertSelective(bonusTx);
        }

        log.info("✅ [Callback] 金幣發放成功：userId={}, +gold={}, +bonus={}", 
                order.getUserId(), order.getGoldAmount(), order.getBonusAmount());
    }
}
