package com.group.admin.res.emergencyannouncement;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class AdminEmergencyAnnouncementRes {

    private String id;

    private String title;

    private String content;

    /** 公告類型：MAINTENANCE / UPDATE / NOTICE */
    private String announcementType;

    /** 狀態：DRAFT / ACTIVE / INACTIVE */
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Taipei")
    private Date displayStartTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Taipei")
    private Date displayEndTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Taipei")
    private Date maintenanceStartTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Taipei")
    private Date maintenanceEndTime;

    private Boolean forceShow;

    private Integer sortOrder;

    private String createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Taipei")
    private Date createdAt;

    private String updatedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Taipei")
    private Date updatedAt;
}