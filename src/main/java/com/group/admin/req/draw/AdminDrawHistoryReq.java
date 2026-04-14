package com.group.admin.req.draw;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 後台查詢抽獎歷史請求
 */
@Data
public class AdminDrawHistoryReq {

    /** 頁碼（從 1 開始） */
    private Integer page = 1;

    /** 每頁筆數 */
    private Integer size = 20;

    /** 篩選特定玩家 ID */
    private String userId;

    /** 篩選狀態（SUCCESS / PENDING / FAILED） */
    private String status;

    /** 起始時間 */
    private LocalDateTime startDate;

    /** 結束時間 */
    private LocalDateTime endDate;
}
