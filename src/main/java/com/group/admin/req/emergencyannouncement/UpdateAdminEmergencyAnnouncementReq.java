package com.group.admin.req.emergencyannouncement;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
public class UpdateAdminEmergencyAnnouncementReq {

    /** 公告標題 */
    @NotBlank(message = "請輸入公告標題")
    private String title;

    /** 公告內容 */
    @NotBlank(message = "請輸入公告內容")
    private String content;

    /** 公告類型：MAINTENANCE / UPDATE / NOTICE */
    @NotBlank(message = "請選擇公告類型")
    private String announcementType;

    /** 狀態：DRAFT / ACTIVE / INACTIVE */
    @NotBlank(message = "請選擇公告狀態")
    private String status;

    /** 公告顯示開始時間 */
    @NotNull(message = "請選擇公告顯示開始時間")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Taipei")
    private Date displayStartTime;

    /** 公告顯示結束時間 */
    @NotNull(message = "請選擇公告顯示結束時間")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Taipei")
    private Date displayEndTime;

    /** 維修/更新開始時間 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Taipei")
    private Date maintenanceStartTime;

    /** 維修/更新結束時間 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Taipei")
    private Date maintenanceEndTime;

    /** 是否強制顯示 */
    private Boolean forceShow;

    /** 排序 */
    private Integer sortOrder;
}