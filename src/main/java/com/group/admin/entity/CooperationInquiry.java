package com.group.admin.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CooperationInquiry {

    /** 主鍵 UUID */
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
    private LocalDateTime deletedAt;

    /** 刪除者 AdminUser ID */
    private String deletedBy;

    /** 建立時間 */
    private LocalDateTime createdAt;

    /** 更新時間 */
    private LocalDateTime updatedAt;
}