package com.group.admin.entity;

import lombok.Data;

import java.util.Date;

@Data
public class AdminEmergencyAnnouncement {

    /** 主鍵 UUID */
    private String id;

    /** 公告標題 */
    private String title;

    /** 公告內容 */
    private String content;

    /** 公告類型：MAINTENANCE / UPDATE / NOTICE */
    private String announcementType;

    /** 狀態：DRAFT / ACTIVE / INACTIVE */
    private String status;

    /** 公告顯示開始時間 */
    private Date displayStartTime;

    /** 公告顯示結束時間 */
    private Date displayEndTime;

    /** 維修/更新開始時間 */
    private Date maintenanceStartTime;

    /** 維修/更新結束時間 */
    private Date maintenanceEndTime;

    /** 是否強制顯示 */
    private Boolean forceShow;

    /** 排序 */
    private Integer sortOrder;

    /** 建立者 */
    private String createdBy;

    /** 建立時間 */
    private Date createdAt;

    /** 更新者 */
    private String updatedBy;

    /** 更新時間 */
    private Date updatedAt;
}