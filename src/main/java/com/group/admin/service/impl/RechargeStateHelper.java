package com.group.admin.service.impl;

import com.group.admin.entity.RechargeOrder;
import com.group.admin.entity.RechargeRecord;
import com.group.admin.enums.RechargeOrderStatus;
import com.group.admin.mapper.RechargeOrderMapper;
import com.group.admin.mapper.RechargeRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RechargeStateHelper {

    private final RechargeOrderMapper rechargeOrderMapper;
    private final RechargeRecordMapper rechargeRecordMapper;

    /**
     * 建立初始 PENDING 訂單與紀錄，使用獨立事務立即提交。
     * 確保即使後續金流呼叫失敗，記錄也已存入 DB。
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveInitialPending(RechargeOrder order, RechargeRecord record) {
        rechargeOrderMapper.insert(order);
        rechargeRecordMapper.insert(record);
        log.debug("[Recharge] 初始訂單已持久化: orderId={}", order.getId());
    }

    /**
     * 更新 gateway 初始化結果（virtualAccount、limitDate 等），使用獨立事務。
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateGatewayInit(RechargeOrder order) {
        rechargeOrderMapper.updateGatewayInit(order);
    }

    /**
     * 將訂單及儲值記錄標記為 FAILED，使用獨立事務立即提交。
     * 即使外層事務 rollback，此 FAILED 狀態仍可持久化。
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(String orderId, String gatewayOrderId, String rawResp, String reason) {
        String safeReason = reason == null || reason.isBlank() ? "付款建立失敗" : reason;
        int updated = rechargeOrderMapper.updateStatusByIdAndExpectStatus(
                orderId,
                RechargeOrderStatus.FAILED,
                RechargeOrderStatus.PENDING,
                gatewayOrderId,
                rawResp,
                null
        );
        if (updated == 0) {
            log.warn("[Recharge] markFailed 未能更新訂單狀態，orderId={} 可能已非 PENDING", orderId);
        }

        RechargeRecord record = rechargeRecordMapper.selectByPrimaryKey(orderId);
        if (record != null) {
            record.setPaymentStatus("FAILED");
            record.setFailReason(safeReason);
            record.setPaymentInfo(rawResp);
            rechargeRecordMapper.updateByPrimaryKeyWithBLOBs(record);
        } else {
            log.warn("[Recharge] markFailed 找不到 RechargeRecord，orderId={}", orderId);
        }
        log.info("[Recharge] 訂單已標記 FAILED: orderId={}, reason={}", orderId, safeReason);
    }
}
