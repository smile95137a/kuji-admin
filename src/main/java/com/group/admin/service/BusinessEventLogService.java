package com.group.admin.service;

import com.group.admin.condition.BusinessEventLogCondition;
import com.group.admin.entity.BusinessEventLog;

import java.util.List;
import java.util.Map;

public interface BusinessEventLogService {

    String EVENT_PAYMENT = "PAYMENT";
    String EVENT_LOGISTICS = "LOGISTICS";
    String EVENT_ORDER_STATUS = "ORDER_STATUS";
    String EVENT_WALLET = "WALLET";

    String RESULT_SUCCESS = "SUCCESS";
    String RESULT_FAILED = "FAILED";
    String RESULT_PENDING = "PENDING";
    String RESULT_DUPLICATE = "DUPLICATE";
    String RESULT_SKIPPED = "SKIPPED";

    void record(BusinessEventLog log);

    void recordCallback(String eventType, String action, String result,
                        String targetType, String targetId, String targetNo,
                        String userId, String orderId, String rechargeId,
                        String externalProvider, String externalRef,
                        Long amount, String paymentMethod,
                        String beforeStatus, String afterStatus,
                        Map<String, String> callbackParams,
                        String errorMessage);

    List<BusinessEventLog> find(BusinessEventLogCondition condition, int limit);

    int cleanupBeforeDays(int days);
}
