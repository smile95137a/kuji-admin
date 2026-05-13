package com.group.admin.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 合作洽詢狀態異動紀錄
 */
@Data
public class CooperationInquiryStatusLog {

    /**
     * 主鍵 UUID
     */
    private String id;

    /**
     * 合作洽詢 ID
     */
    private String inquiryId;

    /**
     * 異動前狀態
     *
     * 例如：
     * PENDING
     * PROCESSING
     * DONE
     * CLOSED
     */
    private String beforeStatus;

    /**
     * 異動後狀態
     *
     * 例如：
     * PENDING
     * PROCESSING
     * DONE
     * CLOSED
     */
    private String afterStatus;

    /**
     * 異動備註
     */
    private String remark;

    /**
     * 操作者 AdminUser ID
     */
    private String operatorId;

    /**
     * 操作者帳號
     */
    private String operatorUsername;

    /**
     * 操作者顯示名稱
     */
    private String operatorDisplayName;

    /**
     * 建立時間
     */
    private LocalDateTime createdAt;
}