package com.group.admin.service;

import com.group.admin.req.draw.AdminDrawHistoryReq;
import com.group.admin.res.draw.AdminDrawHistoryRes;

/**
 * 後台抽獎歷史查詢服務
 */
public interface AdminDrawHistoryService {

    /**
     * 查詢指定抽獎活動的抽獎歷史記錄
     *
     * @param lotteryId  抽獎活動 ID
     * @param callerId   呼叫者的 adminUserId
     * @param callerRole 呼叫者角色 (ROLE_ADMIN / ROLE_STORE_OWNER)
     * @param req        查詢條件
     * @return 分頁結果與匯總
     */
    AdminDrawHistoryRes getDrawHistory(String lotteryId, String callerId, String callerRole, AdminDrawHistoryReq req);
}
