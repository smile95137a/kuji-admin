package com.group.admin.scheduler;

import com.group.admin.entity.RechargeOrder;
import com.group.admin.entity.RechargeRecord;
import com.group.admin.mapper.RechargeOrderMapper;
import com.group.admin.mapper.RechargeRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RechargeOrderScheduler {

    private final RechargeOrderMapper rechargeOrderMapper;
    private final RechargeRecordMapper rechargeRecordMapper;

    @Scheduled(fixedRate = 300000)
    public void expireRechargeOrders() {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<RechargeOrder> expiredOrders = rechargeOrderMapper.selectExpiredPendingOrders(now);
            for (RechargeOrder order : expiredOrders) {
                RechargeRecord record = rechargeRecordMapper.selectByPrimaryKey(order.getId());
                if (record != null && "PENDING".equals(record.getPaymentStatus())) {
                    record.setPaymentStatus("CANCELLED");
                    record.setFailReason("儲值付款逾期");
                    rechargeRecordMapper.updateByPrimaryKeyWithBLOBs(record);
                }
            }

            int expired = rechargeOrderMapper.updateExpiredOrders(now);
            if (expired > 0) {
                log.info("已將 {} 筆逾期儲值訂單標記為 EXPIRED", expired);
            }
        } catch (Exception e) {
            log.error("儲值訂單逾期排程失敗: {}", e.getMessage(), e);
        }
    }
}
