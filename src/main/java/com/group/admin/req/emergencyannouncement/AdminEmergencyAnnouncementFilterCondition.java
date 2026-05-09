package com.group.admin.req.emergencyannouncement;

import lombok.Data;

@Data
public class AdminEmergencyAnnouncementFilterCondition {

    /** 狀態：DRAFT / ACTIVE / INACTIVE */
    private String status;

    /** 公告類型：MAINTENANCE / UPDATE / NOTICE */
    private String announcementType;

    /** 關鍵字：標題 / 內容 */
    private String keyword;

    /** 公告顯示開始時間 */
    private String displayStartTime;

    /** 公告顯示結束時間 */
    private String displayEndTime;

    /** 排序欄位 */
    private String sortBy;

    /** 排序方向：ASC / DESC */
    private String sortDir;
}