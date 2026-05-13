package com.group.admin.res.cooperation;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 合作洽談 Response
 */
@Data
@Builder
public class CooperationInquiryRes {

    /** 合作洽談 ID */
    private String id;

    /** 公司 / 單位名稱 */
    private String company;

    /** 聯絡人姓名 */
    private String name;

    /** 電子郵件 */
    private String email;

    /** 聯絡電話 */
    private String phone;

    /** 合作類型：IP / SUPPLY / CHANNEL / MARKETING */
    private String type;

    /** 需求簡述 */
    private String message;

    /** 處理狀態：PENDING / PROCESSING / DONE / CLOSED */
    private String status;

    /** 後台備註 */
    private String remark;

    /** 是否已轉成廠商 */
    private Boolean convertedToVendor;

    /** 轉成的廠商 AdminUser ID */
    private String vendorAdminUserId;

    /** 是否刪除 */
    private Boolean deleted;

    /** 刪除時間 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Taipei")
    private LocalDateTime deletedAt;

    /** 刪除者 AdminUser ID */
    private String deletedBy;

    /** 建立時間 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Taipei")
    private LocalDateTime createdAt;

    /** 更新時間 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Taipei")
    private LocalDateTime updatedAt;

    /** 狀態異動歷程 */
    private List<CooperationInquiryStatusLogRes> statusLogs;
}