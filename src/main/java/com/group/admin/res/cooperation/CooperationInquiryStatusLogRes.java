package com.group.admin.res.cooperation;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 合作洽談狀態異動紀錄 Response
 */
@Data
@Builder
public class CooperationInquiryStatusLogRes {

    /** 主鍵 UUID */
    private String id;

    /** 合作洽談 ID */
    private String inquiryId;

    /** 異動前狀態 */
    private String beforeStatus;

    /** 異動後狀態 */
    private String afterStatus;

    /** 異動備註 */
    private String remark;

    /** 操作者 AdminUser ID */
    private String operatorId;

    /** 操作者帳號 */
    private String operatorUsername;

    /** 操作者顯示名稱 */
    private String operatorDisplayName;

    /** 建立時間 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Taipei")
    private LocalDateTime createdAt;
}